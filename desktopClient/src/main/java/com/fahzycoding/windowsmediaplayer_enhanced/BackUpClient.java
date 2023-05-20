package com.fahzycoding.windowsmediaplayer_enhanced;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
public class BackUpClient {

    String bucketName = "your-bucket-name";
    String objectKey = "backup-file.txt";
    String filePath = "path/to/backup-file.txt";

    S3Client s3Client = S3Client.create();

            try

    {
        File file = new File(filePath);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(fileToByteArray(file)));

        System.out.println("File uploaded successfully. ETag: " + response.eTag());

    } catch(
    IOException e)

    {
        e.printStackTrace();
    } finally

    {
        s3Client.close();
    }

}

        private static byte[] fileToByteArray(File file) throws IOException {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                return data;
            }
        }
    }


