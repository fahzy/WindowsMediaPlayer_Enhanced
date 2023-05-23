const mongoose = require('mongoose');
const configs = require('./config');

const MONGOURL = configs.databaseUrl;
const dbName = 'your-database';

// Connect to MongoDB
mongoose.connect(MONGOURL, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
});

// Define the user schema
const userSchema = new mongoose.Schema({
    username: { type: String, required: true },
    password: { type: String, required: true },
    authenticationKeys: [{ type: String }],
    fileCount: { type: Number, default: 0 },
    deviceCount: { type: Number, default: 0 },
});

// Define the media schema
const mediaSchema = new mongoose.Schema({
    username: { type: String, required: true },
    mediaName: { type: String, required: true },
    mediaUrl: { type: String, required: true },
    metadata: { type: Object },
    playCount: { type: Number, default: 0 },
});

// Create the models/collections
const User = mongoose.model('User', userSchema);
const Media = mongoose.model('Media', mediaSchema);

// Create the required collections (if they don't exist)
User.createCollection();
Media.createCollection();



