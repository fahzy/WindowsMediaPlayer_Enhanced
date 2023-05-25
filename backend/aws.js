const AWS = require('aws-sdk');
const configs = require('./config')

const REGION = configs.AWS.region
const ACCESS_KEY = configs.AWS.accessKeyId
const SECRET_KEY = configs.AWS.secretAccessKey
// Configure AWS SDK
AWS.config.update({
    region: REGION,
    accessKeyId: ACCESS_KEY,
    secretAccessKey: SECRET_KEY
});

// Create an instance of the IAM service
const iam = new AWS.IAM();
const s3 = new AWS.S3();

const BUCKET = configs.AWS.bucketName

// Define the IAM policies
const adminPolicyDocument = {
    Version: '2012-10-17',
    Statement: [
        {
            Effect: 'Allow',
            Action: 's3:*',
            Resource: 'arn:aws:s3:::'+BUCKET+'/*'
        },
        {
            Effect: 'Allow',
            Action: 'iam:*',
            Resource: '*'
        },
        {
            Effect: 'Allow',
            Action: 'cloudwatch:*',
            Resource: '*'
        },
        {
            Effect: 'Allow',
            Action: 'sns:*',
            Resource: '*'
        },
        {
            Effect: 'Allow',
            Action: 'dynamodb:*',
            Resource: '*'
        }
    ]
};

const userPolicyDocument = {
    Version: '2012-10-17',
    Statement: [
        {
            Effect: 'Allow',
            Action: 's3:ListBucket',
            Resource: 'arn:aws:s3:::'+ BUCKET
        },
        {
            Effect: 'Allow',
            Action: 's3:GetObject',
            Resource: 'arn:aws:s3:::'+ BUCKET +'/*'
        },
        {
            Effect: 'Allow',
            Action: 's3:PutObject',
            Resource: 'arn:aws:s3:::'+ BUCKET +'/*'
        },
        {
            Effect: 'Allow',
            Action: 's3:DeleteObject',
            Resource: 'arn:aws:s3:::'+ BUCKET +'/*'
        },
        {
            Effect: 'Allow',
            Action: 'cloudwatch:PutMetricData',
            Resource: '*'
        }
    ]
};
// Create the IAM policies
const createPolicy = async (policyName, policyDocument) => {
    try {
        // Check if the policy already exists
        const listPoliciesResponse = await iam.listPolicies({}).promise();
        const policyExists = listPoliciesResponse.Policies.some(policy => policy.PolicyName === policyName);

        if (!policyExists) {
            const params = {
                PolicyDocument: JSON.stringify(policyDocument),
                PolicyName: policyName
            };

            await iam.createPolicy(params).promise();
            console.log(`IAM policy "${policyName}" created successfully.`);
        } else {
            console.log(`IAM policy "${policyName}" already exists.`);
        }
    } catch (error) {
        console.error(`Error creating IAM policy "${policyName}":`, error);
    }
};

// Create the IAM roles and attach the policies
const createRoles = async () => {
    try {
        // Check if the admin role already exists
        const listRolesResponse = await iam.listRoles({}).promise();
        const adminRoleExists = listRolesResponse.Roles.some(role => role.RoleName === 'adminRole');

        if (!adminRoleExists) {
            // Create the admin role
            const adminRoleParams = {
                AssumeRolePolicyDocument: JSON.stringify({
                    Version: '2012-10-17',
                    Statement: [
                        {
                            Effect: 'Allow',
                            Principal: {
                                Service: 'ec2.amazonaws.com'
                            },
                            Action: 'sts:AssumeRole'
                        }
                    ]
                }),
                RoleName: 'adminRole'
            };

            const adminRole = await iam.createRole(adminRoleParams).promise();
            console.log('Admin role created successfully.');
        } else {
            console.log('Admin role already exists.');
        }

        // Check if the user role already exists
        const userRoleExists = listRolesResponse.Roles.some(role => role.RoleName === 'userRole');

        if (!userRoleExists) {
            // Create the user role
            const userRoleParams = {
                AssumeRolePolicyDocument: JSON.stringify({
                    Version: '2012-10-17',
                    Statement: [
                        {
                            Effect: 'Allow',
                            Principal: {
                                Service: 'ec2.amazonaws.com'
                            },
                            Action: 'sts:AssumeRole'
                        }
                    ]
                }),
                RoleName: 'userRole'
            };

            const userRole = await iam.createRole(userRoleParams).promise();
            console.log('User role created successfully.');
        } else {
            console.log('User role already exists.');
        }

        // Attach the admin policy to the admin role
        await iam.attachRolePolicy({ RoleName: 'adminRole', PolicyArn: 'your-admin-policy-arn' }).promise();
        console.log('Admin policy attached to the admin role successfully.');

        // Attach the user policy to the user role
        await iam.attachRolePolicy({ RoleName: 'userRole', PolicyArn: 'your-user-policy-arn' }).promise();
        console.log('User policy attached to the user role successfully.');

        console.log('IAM roles and policies created successfully.');
    } catch (error) {
        console.error('Error creating IAM roles and policies:', error);
    }
};

// Run the function to create the roles
createRoles();

module.exports = {
    AWS,
    iam,
    s3
}