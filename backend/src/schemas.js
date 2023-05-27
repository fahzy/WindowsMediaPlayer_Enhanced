const mongoose = require('mongoose');
const configs = require('./config');

const MONGOURL = configs.databaseUrl;

// Connect to MongoDB
mongoose.connect(MONGOURL, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
});

const db = mongoose.connection;
db.on('error', console.error.bind(console, 'MongoDB connection error:'));
db.once('open', () => {
    console.log('Connected to MongoDB');
});

// Define the user schema
const userSchema = new mongoose.Schema({
    username: { type: String, required: true },
    password: { type: String, required: true },
    role: { type: String, required: true, enum: ['adminRole', 'wmpUsers', 'guestRole'] },
    authenticationKey: { type: String },
    fileCount: { type: Number, default: 0 },
    deviceCount: { type: Number, default: 0 },
    iamUser: String,
    iamAccessKey: String,
    iamSecretKey: String,
});

// Define the media schema
const mediaSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
    },
    key: String,
    uploadTime: Number,
    filename: String,
    url: String,
    metadata: {
        type: Map,
        of: String,
    },
    playCount: Number,
    sharedWith: [
        {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
        },
    ],
});

// Create the models/collections
const User = mongoose.model('User', userSchema);
const Media = mongoose.model('Media', mediaSchema);

// Create the required collections (if they don't exist)
User.createCollection();
Media.createCollection();

module.exports = {
    db,
    User,
    Media
}



