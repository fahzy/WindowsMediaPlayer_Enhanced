const { MongoClient } = require('mongodb');
const configs = require('./config')

const mongoUrl = configs.databaseUrl;
const dbName = 'your-database';

async function createCollections() {
    try {
        const client = await MongoClient.connect(mongoUrl);
        const db = client.db(dbName);

        await db.createCollection('users');
        await db.createCollection('media');

        console.log('Collections created successfully');

        client.close();
    } catch (error) {
        console.error('Error occurred:', error);
    }
}

createCollections();
