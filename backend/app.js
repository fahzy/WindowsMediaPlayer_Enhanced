const express = require('express');
const {AWS, iam, s3} =require('./aws')
const {db, User, Media} = require('./schemas')
const multer = require('multer');
const jwt = require('jsonwebtoken');
const configs = require("./config");

const BUCKET = configs.AWS.bucketName
const JWT_SECRET_KEY = configs.jwt_secretKey

// Configure Express app
const app = express();
const upload = multer({ dest: 'uploads/' });

// Middleware to authenticate requests
const authenticate = (req, res, next) => {
    const token = req.headers.authorization;

    if (!token) {
        return res.status(401).json({ message: 'Authentication token not provided' });
    }

    try {
        const decodedToken = jwt.verify(token, JWT_SECRET_KEY);
        req.user = decodedToken.user;
        next();
    } catch (error) {
        return res.status(401).json({ message: 'Invalid authentication token' });
    }
};

// Middleware to authorize user
function authorize(allowedRoles) {
    return (req, res, next) => {
        const { role } = req.user;
        if (!allowedRoles.includes(role)) {
            return res.status(403).json({ message: 'Access Denied' });
        }
        next();
    };
}

// Register endpoint
// User registration endpoint
app.post('/register', async (req, res) => {
    try {
        const { username, password, role } = req.body;

        // Check if the username already exists
        const existingUser = await User.findOne({ username });
        if (existingUser) {
            return res.status(400).json({ message: 'Username already exists' });
        }

        // Hash the password
        const hashedPassword = await bcrypt.hash(password, 10);

        // Create the user in IAM
        const createUserParams = {
            UserName: username,
            Tags: [
                { Key: 'WMP', Value: 'app_user' },
            ],
        };
        let user;
        try {
            user = await iam.createUser(createUserParams).promise();
        } catch (error) {
            console.error('Error creating IAM user:', error);
            return res.status(500).json({ message: 'Error creating user' });
        }

        // Save the user details to MongoDB
        const newUser = new User({ username, hashedPassword, iamUsername: user.UserName, role });
        try {
            await newUser.save();
        } catch (error) {
            console.error('Error saving user to MongoDB:', error);
            // Rollback the IAM user creation if MongoDB save fails
            try {
                await iam.deleteUser({ UserName: user.UserName }).promise();
            } catch (error) {
                console.error('Error deleting IAM user:', error);
            }
            return res.status(500).json({ message: 'Error registering user' });
        }

        // Generate a JWT token
        const token = jwt.sign({ user: { id: newUser._id, username: newUser.username, role: newUser.role } }, JWT_SECRET_KEY);

        return res.status(200).json({ token });
    } catch (error) {
        console.error('Error registering user:', error);
        return res.status(500).json({ message: 'Error registering user' });
    }
});

// Login endpoint
app.post('/login', async (req, res) => {
    try {
        const { username, password } = req.body;

        // Find the user by username
        const user = await User.findOne({ username });
        if (!user) {
            return res.status(401).json({ message: 'Invalid username or password' });
        }

        // Check if the password is correct
        const isPasswordValid = await bcrypt.compare(password, user.password);
        if (!isPasswordValid) {
            return res.status(401).json({ message: 'Invalid username or password' });
        }

        // Generate a JWT token
        const token = jwt.sign({ user: { id: user._id, username: user.username, role: user.role } }, JWT_SECRET);

        return res.status(200).json({ token });
    } catch (error) {
        console.error('Error logging in user:', error);
        return res.status(500).json({ message: 'Error logging in user' });
    }
});

// Upload media endpoint
app.post('/upload', authenticate, authorize(['admin', 'user']), upload.single('file'), async (req, res) => {
    try {
        const { filename, path, mimetype } = req.file;

        // Upload the file to S3 bucket
        const uploadParams = {
            Bucket: 'your-bucket-name',
            Key: filename,
            Body: fs.createReadStream(path),
            ContentType: mimetype,
        };
        await s3.upload(uploadParams).promise();

        // Save the media details to MongoDB
        const media = new Media({
            userId: req.user.id,
            filename,
            url: `https://your-bucket-name.s3.amazonaws.com/${filename}`,
            mimetype,
            metadata: req.body.metadata,
        });
        await media.save();

        // Remove the temporary file
        fs.unlinkSync(path);

        return res.status(200).json({ message: 'File uploaded successfully' });
    } catch (error) {
        console.error('Error uploading file:', error);
        return res.status(500).json({ message: 'Error uploading file' });
    }
});

// Retrieve media endpoint
app.get('/media', authenticate, authorize(['admin', 'user']), async (req, res) => {
    try {
        // Retrieve all media for the authenticated user
        const media = await Media.find({ userId: req.user.id });

        return res.status(200).json(media);
    } catch (error) {
        console.error('Error retrieving media:', error);
        return res.status(500).json({ message: 'Error retrieving media' });
    }
});

// Download media endpoint
app.get('/media/:id', authenticate, authorize(['admin', 'user']), async (req, res) => {
    try {
        const { id } = req.params;

        // Find the media by ID
        const media = await Media.findById(id);
        if (!media) {
            return res.status(404).json({ message: 'Media not found' });
        }

        // Check if the authenticated user has access to the media
        if (media.userId.toString() !== req.user.id.toString()) {
            return res.status(403).json({ message: 'Access denied' });
        }

        // Download the media from S3 bucket
        const downloadParams = {
            Bucket: 'your-bucket-name',
            Key: media.filename,
        };
        const fileStream = s3.getObject(downloadParams).createReadStream();
        res.attachment(media.filename);
        fileStream.pipe(res);
    } catch (error) {
        console.error('Error downloading media:', error);
        return res.status(500).json({ message: 'Error downloading media' });
    }
});

// Delete media endpoint
app.delete('/media/:id', authenticate, authorize(['admin', 'user']), async (req, res) => {
    try {
        const { id } = req.params;

        // Find the media by ID
        const media = await Media.findById(id);
        if (!media) {
            return res.status(404).json({ message: 'Media not found' });
        }

        // Check if the authenticated user has access to the media
        if (media.userId.toString() !== req.user.id.toString()) {
            return res.status(403).json({ message: 'Access denied' });
        }

        // Delete the media from S3 bucket
        const deleteParams = {
            Bucket: 'your-bucket-name',
            Key: media.filename,
        };
        await s3.deleteObject(deleteParams).promise();

        // Delete the media from MongoDB
        await Media.findByIdAndDelete(id);

        return res.status(200).json({ message: 'Media deleted successfully' });
    } catch (error) {
        console.error('Error deleting media:', error);
        return res.status(500).json({ message: 'Error deleting media' });
    }
});

// Start the server
app.listen(3000, () => {
    console.log('Server listening on port 3000');
});
