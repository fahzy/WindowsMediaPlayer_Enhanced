const chai = require('chai');
const expect = chai.expect;
const request = require('supertest');
const app = require('../src/app');
const {User, Media} = require("../src/schemas");
const configs = require('./config')
const AWS = require("aws-sdk");

const REGION = configs.AWS.region
const ACCESS_KEY = configs.AWS.accessKeyId
const SECRET_KEY = configs.AWS.secretAccessKey

AWS.config.update({
    region: REGION,
    accessKeyId: ACCESS_KEY,
    secretAccessKey: SECRET_KEY
});

// Create an IAM instance
const iam = new AWS.IAM();
describe('User Registration', function()  {
    this.timeout(10000)
    before(async function()  {
        // Before each test, reset the database or perform any necessary setup
        const users = await getUsersByGroup("wmpUsers")
        for (const user of users){
            await deleteIAMUser(user)
        }
        await User.deleteMany();
        await Media.deleteMany();
        // Add test data if needed
    });

    it('should register a new user', (done) => {
        request(app)
            .post('/register')
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send({
                username: 'testuser',
                password: 'testpassword',
                role: 'wmpUsers'
            })
            .expect(200)
            .end((err, res)=>{
                expect(res.body).to.have.property('token');
                done();
            })

    });

    // Add more test cases for edge cases and error scenarios
});

describe('User Authentication', function()  {
    this.timeout(10000)
    before(async function()  {
        // Before each test, reset the database or perform any necessary setup
        const users = await getUsersByGroup("wmpUsers")
        for (const user of users){
            await deleteIAMUser(user)
        }
        await User.deleteMany();
        await Media.deleteMany();
        // Add test data if needed

        await request(app)
            .post('/register')
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send({
                username: 'testuser',
                password: 'testpassword',
                role: 'wmpUsers'
            })
    })

    it('should authenticate a user and return a session token', async function() {
        const res = await request(app)
            .post('/login')
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send({
                username: 'testuser',
                password: 'testpassword',
            })

        expect(res.statusCode).to.equal(200);
        expect(res.body).to.have.property('token');

    });

    // Add more test cases for edge cases and error scenarios
});

describe('Media Upload', function() {
    this.timeout(25000)
    before(async function()  {
        // Before each test, reset the database or perform any necessary setup
        const users = await getUsersByGroup("wmpUsers")
        for (const user of users){
            await deleteIAMUser(user)
        }
        await User.deleteMany();
        await Media.deleteMany();

        await request(app)
            .post('/register')
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send({
                username: 'testuser',
                password: 'testpassword',
                role: 'wmpUsers'
            })
    });

    it('should upload a file and return success message or file ID', async function() {
        var res = await request(app)
            .post('/login')
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send({
                username: 'testuser',
                password: 'testpassword',
            })

        const authKey = res.body.token;

        res = await request(app)
            .post('/upload')
            .set('Content-Type', 'multipart/form-data')
            .set('Authorization', authKey)
            .field('filename', "Heaven\'s EP")
            .field( 'mimetype',"mp3")
            .attach('file', 'C:\\Users\\lenovo\\Documents\\University\\Honours\\COS730\\A3\\WindowsMediaPlayer_Enhanced\\backend\\test\\uploads\\J. Cole - Heaven\'s EP (Official Music Video).mp4')
            .expect(200)

        expect(res.body.message).to.equals('File uploaded successfully');

    });

    // Add more test cases for edge cases and error scenarios
});

describe('All Media Retrieval', function() {
    this.timeout(25000)
    before(async function()  {

        // Before each test, reset the database or perform any necessary setup
        const users = await getUsersByGroup("wmpUsers")
        for (const user of users){
            await deleteIAMUser(user)
        }
        await User.deleteMany();
        await Media.deleteMany();

        let res = await request(app)
            .post('/register')
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send({
                username: 'testuser',
                password: 'testpassword',
                role: 'wmpUsers'
            })
        const authKey = res.body.token

        await request(app)
            .post('/upload')
            .set('Content-Type', 'multipart/form-data')
            .set('Authorization', authKey)
            .field('filename', "Heaven\'s EP")
            .field( 'mimetype',"mp3")
            .attach('file', 'C:\\Users\\lenovo\\Documents\\University\\Honours\\COS730\\A3\\WindowsMediaPlayer_Enhanced\\backend\\test\\uploads\\J. Cole - Heaven\'s EP (Official Music Video).mp4')
            .expect(200)

    });

    it('should retrieve a list of all the files uploaded by the user', async function()  {

        var res = await request(app)
            .post('/login')
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send({
                username: 'testuser',
                password: 'testpassword',
            })
        const authKey = res.body.token

        res = await request.agent(app)
            .get('/media')
            .set('Authorization', authKey)
            .expect(200)

        expect(res.body[0]).to.have.property("_id")
        expect(res.body[0]).to.have.property("userId")
        expect(res.body[0]).to.have.property("filename")
        expect(res.body[0]).to.have.property('url');
        expect(res.body[0]).to.have.property("sharedWith")

    });

    // Add more test cases for edge cases and error scenarios
});

describe('Media Deletion', function() {
    this.timeout(25000)
    before(async function()  {

        // Before each test, reset the database or perform any necessary setup
        const users = await getUsersByGroup("wmpUsers")
        for (const user of users){
            await deleteIAMUser(user)
        }
        await User.deleteMany();
        await Media.deleteMany();

        let res = await request(app)
            .post('/register')
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send({
                username: 'testuser',
                password: 'testpassword',
                role: 'wmpUsers'
            })
        const authKey = res.body.token

        await request(app)
            .post('/upload')
            .set('Content-Type', 'multipart/form-data')
            .set('Authorization', authKey)
            .field('filename', "Heaven\'s EP")
            .field( 'mimetype',"mp3")
            .attach('file', 'C:\\Users\\lenovo\\Documents\\University\\Honours\\COS730\\A3\\WindowsMediaPlayer_Enhanced\\backend\\test\\uploads\\J. Cole - Heaven\'s EP (Official Music Video).mp4')
            .expect(200)

    });


    it('should delete a file and return success message', async function() {
        var res = await request(app)
            .post('/login')
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send({
                username: 'testuser',
                password: 'testpassword',
            })
        const authKey = res.body.token

        res = await request.agent(app)
            .get('/media')
            .set('Authorization', authKey)
            .expect(200)

        const mediaId = res.body[0]._id

        res = await request(app)
            .delete(`/media/${mediaId}`)
            .set('Authorization', authKey)
            .expect(200)

        expect(res.body.message).to.equal('Media deleted successfully');
    });

    // Add more test cases for edge cases and error scenarios
});

// Helper functions
const deleteIAMUser = async (userName) => {
    try {
        const params = {
            UserName: userName,
        };
        await removeUserFromGroups((userName));
        await iam.deleteUser(params).promise();

        console.log(`IAM user '${userName}' deleted successfully.`);
    } catch (err) {
        console.error('Error deleting IAM user:', err);
    }
};

async function getUsersByGroup(groupName) {
    try {
        const groupResponse = await iam.getGroup({ GroupName: groupName }).promise();

        const userList = [];

        for (const user of groupResponse.Users) {
            const userDetails = await iam.listGroupsForUser({ UserName: user.UserName }).promise();
            const groups = userDetails.Groups.map(group => group.GroupName);
            userList.push( user.UserName);
        }

        return userList;

    } catch (err) {
        console.error('Error retrieving users:', err);
    }
}

async function removeUserFromGroups(username) {
    try {
        const groups = await iam.listGroupsForUser({ UserName: username }).promise();
        const groupNames = groups.Groups.map(group => group.GroupName);

        // Remove user from each group
        await Promise.all(
            groupNames.map(groupName => {
                return iam.removeUserFromGroup({ GroupName: groupName, UserName: username }).promise();
            })
        );

        console.log('User removed from groups successfully.');
    } catch (error) {
        console.error('Error removing user from groups:', error);
        throw error;
    }
}

