const chai = require('chai');
const chaiHttp = require('chai-http');
const app = require('../src/app'); // assuming your Node.js app is named "app"
const {User} = require('../src/schemas');

chai.use(chaiHttp);
const expect = chai.expect;

describe('Authentication', () => {
    beforeEach(async () => {
        // Before each test, reset the database or perform any necessary setup
        await User.deleteMany();
        // Add test data if needed
    });

    it('should authenticate a valid user', (done) => {
        // Perform the authentication flow and assert the expected behavior
        // You can simulate requests to your server using chai-http
        // Example:
        chai.request(app)
            .post('/login')
            .send({ username: 'testuser', password: 'password' })
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('token');
                done();
            });
    });

    // Add more test cases for different authentication scenarios

});
