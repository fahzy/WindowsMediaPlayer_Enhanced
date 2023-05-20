const express = require('express');
const multer = require('multer');
const AWS = require('aws-sdk');

const app = express();
const upload = multer({ dest: 'uploads/' });

app.post('/backup', upload.single('file'), (req, res) => {
    const s3 = new AWS.S3();

    const params = {
        Bucket: 'your-bucket-name',
        Key: 'backup-file.txt',
        Body: req.file.buffer
    };

    s3.upload(params, (err, data) => {
        if (err) {
            console.error(err);
            res.status(500).send('Error uploading file to S3');
        } else {
            console.log('File uploaded successfully:', data.Location);
            res.status(200).send('File uploaded to S3');
        }
    });
});

app.listen(3000, () => {
    console.log('Server is running on http://localhost:3000');
});
