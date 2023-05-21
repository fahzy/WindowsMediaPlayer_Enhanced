const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const { MongoClient, ObjectId } = require('mongodb');
const crypto = require('crypto');

// Apply middleware for parsing request bodies
app.use(bodyParser.urlencoded({ extended: true }));

// MongoDB connection URL
const MONGO_URL = 'mongodb://localhost:27017';

// MongoDB database name
const DB_NAME = 'sessiondb';

// Collection name for storing user sessions
const SESSION_COLLECTION = 'sessions';

// Session token expiration time (30 days in milliseconds)
const SESSION_EXPIRATION_TIME = 30 * 24 * 60 * 60 * 1000;

// MongoDB client
let mongoClient;

// Connect to MongoDB
MongoClient.connect(MONGO_URL, { useUnifiedTopology: true })
    .then((client) => {
        console.log('Connected to MongoDB');
        mongoClient = client;
    })
    .catch((err) => {
        console.error('Failed to connect to MongoDB:', err);
        process.exit(1);
    });

// Register user endpoint
app.post('/register', (req, res) => {
    const { username, password } = req.body;

    // Hash the password before storing it in the database
    const hashedPassword = hashPassword(password);

    // Store the username and hashed password in the database
    storeUserCredentials(username, hashedPassword)
        .then(() => {
            res.sendStatus(200);
        })
        .catch((err) => {
            console.error('Failed to store user credentials:', err);
            res.sendStatus(500);
        });
});

// Login endpoint
app.post('/login', (req, res) => {
    const { username, password } = req.body;

    // Retrieve the hashed password from the database for the provided username
    retrieveUserCredentials(username)
        .then((hashedPassword) => {
            // Verify the provided password against the stored hashed password
            if (!verifyPassword(password, hashedPassword)) {
                return res.sendStatus(401);
            }

            // Generate a new session token
            const sessionToken = generateSessionToken();

            // Store the session token in MongoDB with an expiration timestamp
            storeSessionToken(username, sessionToken)
                .then(() => {
                    res.status(200).json({ sessionToken });
                })
                .catch((err) => {
                    console.error('Failed to store session token:', err);
                    res.sendStatus(500);
                });
        })
        .catch((err) => {
            console.error('Failed to retrieve user credentials:', err);
            res.sendStatus(500);
        });
});

// Middleware to verify the session token
function verifySessionToken(req, res, next) {
    const sessionToken = req.headers.authorization;

    // Retrieve the session information from MongoDB
    getSessionInfo(sessionToken)
        .then((sessionInfo) => {
            if (!sessionInfo || sessionInfo.expired) {
                return res.sendStatus(401);
            }

            // Add the session information to the request object
            req.sessionInfo = sessionInfo;

            next();
        })
        .catch((err) => {
            console.error('Failed to retrieve session information:', err);
            res.sendStatus(500);
        });
}

// Backup media endpoint
app.post('/backup', verifySessionToken, (req, res) => {
    // Only authenticated and authorized users can access this endpoint
    const { username } = req.sessionInfo;
    const { fileName, fileData, fileHash } = req.body;

    // Store the media file in AWS S3 bucket and update the media metadata in MongoDB
    storeMediaFile(username, fileName, fileData, fileHash)
        .then(() => {
            res.sendStatus(200);
        })
        .catch((err) => {
            console.error('Failed to store media file:', err);
            res.sendStatus(500);
        });
});

// Retrieve media endpoint
app.get('/media/:id', verifySessionToken, (req, res) => {
    // Only authenticated and authorized users can access this endpoint
    const { username } = req.sessionInfo;
    const mediaId = req.params.id;

    // Retrieve the media file from AWS S3 bucket and send it as a response
    retrieveMediaFile(username, mediaId)
        .then((mediaData) => {
            res.status(200).send(mediaData);
        })
        .catch((err) => {
            console.error('Failed to retrieve media file:', err);
            res.sendStatus(500);
        });
});

// Delete media endpoint
app.delete('/media/:id', verifySessionToken, (req, res) => {
    // Only authenticated and authorized users can access this endpoint
    const { username } = req.sessionInfo;
    const mediaId = req.params.id;

    // Delete the media file from AWS S3 bucket and update the media metadata in MongoDB
    deleteMediaFile(username, mediaId)
        .then(() => {
            res.sendStatus(200);
        })
        .catch((err) => {
            console.error('Failed to delete media file:', err);
            res.sendStatus(500);
        });
});

// Generate a random session token
function generateSessionToken() {
    return crypto.randomBytes(64).toString('hex');
}

// Hash a password using a secure cryptographic algorithm
function hashPassword(password) {
    const salt = crypto.randomBytes(16).toString('hex');
    const hash = crypto.pbkdf2Sync(password, salt, 10000, 64, 'sha512').toString('hex');
    return `${salt}:${hash}`;
}

// Verify a password against a stored hashed password
function verifyPassword(password, hashedPassword) {
    const [salt, hash] = hashedPassword.split(':');
    const verifyHash = crypto.pbkdf2Sync(password, salt, 10000, 64, 'sha512').toString('hex');
    return hash === verifyHash;
}

// Store user credentials in MongoDB
function storeUserCredentials(username, hashedPassword) {
    return mongoClient.db(DB_NAME).collection('users').insertOne({ username, password: hashedPassword });
}

// Retrieve user credentials from MongoDB
function retrieveUserCredentials(username) {
    return mongoClient.db(DB_NAME).collection('users').findOne({ username })
        .then((user) => {
            return user ? user.password : null;
        });
}

// Store the session token in MongoDB with an expiration timestamp
function storeSessionToken(username, sessionToken) {
    const sessionExpiration = new Date(Date.now() + SESSION_EXPIRATION_TIME);
    const sessionInfo = { sessionToken, expiration: sessionExpiration };

    return mongoClient.db(DB_NAME).collection(SESSION_COLLECTION).updateOne(
        { username },
        { $push: { sessionTokens: sessionInfo } },
        { upsert: true }
    );
}

// Retrieve session information from MongoDB
function getSessionInfo(sessionToken) {
    return mongoClient.db(DB_NAME).collection(SESSION_COLLECTION).findOne(
        { 'sessionTokens.sessionToken': sessionToken },
        { projection: { sessionTokens: { $elemMatch: { sessionToken } } } }
    )
        .then((user) => {
            if (user && user.sessionTokens.length > 0) {
                const sessionInfo = user.sessionTokens[0];
                if (!sessionInfo.expired && sessionInfo.expiration > new Date()) {
                    return { username: user.username, sessionToken, expiration: sessionInfo.expiration };
                }
            }
            return null;
        });
}

// Store the media file in AWS S3 bucket and update the media metadata in MongoDB
function storeMediaFile(username, fileName, fileData, fileHash) {
    // Code for storing media file in AWS S3 bucket
    // ...

    const mediaMetadata = {
        username,
        fileName,
        fileHash,
        // Additional metadata fields
        // ...
    };

    return mongoClient.db(DB_NAME).collection('media').insertOne(mediaMetadata);
}

// Retrieve the media file from AWS S3 bucket
function retrieveMediaFile(username, mediaId) {
    // Code for retrieving media file from AWS S3 bucket
    // ...

    // Simulated media data for demonstration purposes
    const mediaData = {
        mediaId,
        mediaUrl: `https://s3.example.com/bucket/${mediaId}`,
        // Additional metadata fields
        // ...
    };

    return Promise.resolve(mediaData);
}

// Delete the media file from AWS S3 bucket and update the media metadata in MongoDB
function deleteMediaFile(username, mediaId) {
    // Code for deleting media file from AWS S3 bucket
    // ...

    return mongoClient.db(DB_NAME).collection('media').deleteOne({ username, mediaId });
}

// Start the server
app.listen(3000, () => {
    console.log('Server listening on port 3000');
});
