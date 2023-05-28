const { MongoClient } = require('mongodb');
const { expect } = require('chai');
const request = require('supertest');
const app = require('./your-express-app'); // Replace with the actual path to your Express app file

describe('Integration Tests', () => {
    let db;
    let server;

    before(async () => {
        // Establish a connection to the MongoDB instance
        const mongoURI = 'mongodb://localhost:27017/your-db-name';
        const client = await MongoClient.connect(mongoURI, { useNewUrlParser: true, useUnifiedTopology: true });
        db = client.db();

        // Start the Node.js server
        server = app.listen(3000);
    });

    after(async () => {
        // Close the MongoDB connection and stop the server
        await db.close();
        server.close();
    });

    beforeEach(async () => {
        // Clear the MongoDB collection before each test
        await db.collection('users').deleteMany({});
    });

    it('should retrieve users from the database', async () => {
        // Insert test data into the MongoDB collection
        const users = [
            { name: 'John Doe', email: 'john@example.com' },
            { name: 'Jane Smith', email: 'jane@example.com' },
        ];
        await db.collection('users').insertMany(users);

        // Send a request to your server endpoint
        const res = await request(app).get('/users');

        // Assert the response
        expect(res.status).to.equal(200);
        expect(res.body).to.have.lengthOf(2);
        expect(res.body[0].name).to.equal('John Doe');
        expect(res.body[1].email).to.equal('jane@example.com');
    });

    // Add more integration test cases as needed
});
