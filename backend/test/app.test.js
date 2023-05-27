const chai = require('chai');
const expect = chai.expect;
const request = require('supertest');
const app = require('../src/app');
const {User, Media} = require("../src/schemas");

describe('User Registration', () => {
    beforeEach(async () => {
        // Before each test, reset the database or perform any necessary setup
        await User.deleteMany();
        await Media.deleteMany()
        // Add test data if needed
    });

    it('should register a new user', async(done) => {
        const res = await request(app)
            .post('/register')
            .send({
                username: 'testuser',
                password: 'testpassword',
                role: 'wmpUsers'
            })
            .expect(200);

        expect(res.body).to.have.property('token');
        done();
    });

    // Add more test cases for edge cases and error scenarios
});

describe('User Authentication', () => {

    beforeEach(async () => {
        // Before each test, reset the database or perform any necessary setup
        await User.deleteMany();
        await Media.deleteMany()

        request(app)
            .post('/register')
            .send({
                username: 'testuser',
                password: 'testpassword',
                role: 'wmpUsers'
            }).end()
    })

    it('should authenticate a user and return a session token', (done) => {
        request(app)
            .post('/login')
            .send({
                username: 'testuser',
                password: 'testpassword',
            })
            .expect(200)
            .end((err, res) => {
                expect(res.body).to.have.property('token');
                done();
            });
    });

    // Add more test cases for edge cases and error scenarios
});


describe('File Upload', () => {
    let authkey;
    beforeEach(async () => {
        // Before each test, reset the database or perform any necessary setup
        await User.deleteMany();
        await Media.deleteMany()

        await request(app)
            .post('/register')
            .send({
                username: 'testuser',
                password: 'testpassword',
                role: 'wmpUsers'
            }).end((err, res)=>{
                authkey = res.body.token;
                done();
            })
    });

    it('should upload a file and return success message or file ID', (done) => {
        request(app)
            .post('/upload')
            .set('Authorization', authKey)
            .send({
                filename: "Heaven\'s EP",
                mimetype: "mp3"
            })
            .attach('file', '../uploads/J. Cole - Heaven\'s EP (Official Music Video).mp4')
            .end((err, res) => {
                expect(res.body).to.have.property("_id")
                expect(res.body).to.have.property("userId")
                expect(res.body).to.have.property("filename")
                expect(res.body).to.have.property('url');
                expect(res.body).to.have.property("sharedWith")
                done();
            });
    });

    // Add more test cases for edge cases and error scenarios
});

describe('File Retrieval', () => {
    let authKey;
    beforeEach(async () => {
        // Before each test, reset the database or perform any necessary setup
        await User.deleteMany();
        await Media.deleteMany()
        await request(app)
            .post('/register')
            .send({
                username: 'testuserfile',
                password: 'testpassword',
                role: 'wmpUsers'
            })
            .end((err, res) =>{
                authKey = res.body.token;
                done();
            })
        await request(app)
            .post('/upload')
            .set('Authorization', authKey)
            .send({
                filename: "Heaven\'s EP",
                mimetype: "mp3"
            })
            .attach('file', '../uploads/J. Cole - Heaven\'s EP (Official Music Video).mp4')
            .end()

    });

    it('should retrieve a list of all the files uploaded by the user', (done) => {
        request(app)
            .set('Authorization', authKey)
            .expect(200)
            .get('/media')
            .end((err, res) => {
                expect(res.body.message).to.equals('File uploaded successfully');
                done();
            });
    });

    // Add more test cases for edge cases and error scenarios
});

describe('File Deletion', () => {
    let authKey;
    beforeEach(async () => {
        // Before each test, reset the database or perform any necessary setup
        await User.deleteMany();
        await Media.deleteMany()
        await request(app)
            .post('/register')
            .send({
                username: 'testuserfile',
                password: 'testpassword',
                role: 'wmpUsers'
            })
            .end((err, res) =>{
                authKey = res.body.token;
                done();
            })
        await request(app)
            .post('/upload')
            .set('Authorization', authKey)
            .send({
                filename: "Heaven\'s EP",
                mimetype: "mp3"
            })
            .attach('file', '../uploads/J. Cole - Heaven\'s EP (Official Music Video).mp4')
            .end()

    });


    it('should delete a file and return success message', (done) => {
        request(app)
            .delete(`/file/Heaven's EP`)
            .set('Authorization', authKey)
            .expect(200)
            .end((err, res) => {
                expect(res.body.message).to.equal('File deleted successfully');
                done();
            });
    });

    // Add more test cases for edge cases and error scenarios
});
