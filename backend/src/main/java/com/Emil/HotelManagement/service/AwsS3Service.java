package com.Emil.HotelManagement.service;

import com.Emil.HotelManagement.exception.MyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class AwsS3Service {

    private final String bucketName = "emil-demo-hotel-images";

    @Value("${aws.s3.access.key}")
    private String awsS3AccessKey;

    @Value("${aws.s3.secret.key}")
    private String awsS3SecretKey;

    public String saveImageToS3(MultipartFile photo) {
        try {
            String s3FileName = photo.getOriginalFilename();

            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(awsS3AccessKey, awsS3SecretKey);

            S3Client s3Client = S3Client.builder()
                    .region(Region.EU_NORTH_1)
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3FileName)
                    .contentType("image/jpeg")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(photo.getBytes()));

            return "https://" + bucketName + ".s3." + Region.EU_NORTH_1 + ".amazonaws.com/" + s3FileName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new MyException("Unable to upload image to S3 bucket: " + e.getMessage());
        }
    }
}