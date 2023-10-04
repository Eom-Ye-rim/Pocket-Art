package com.pocekt.art.service;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public AmazonS3Client amazonS3Client() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }
    public String Sketchupload(File file) {
        String imageUrl = "";
        String key = "sketch/" + file;

        // 파일 객체를 직접 전달
        s3Client.putObject(new PutObjectRequest(bucket, key, file));

        // 업로드된 객체의 공개 URL을 가져오기
        imageUrl = s3Client.getUrl(bucket, key).toString();
        System.out.println("imageUrl"+imageUrl);

        return imageUrl;
    }
    public String upload(MultipartFile file) {
        String imageUrl = "";
        String fileName = createFileName(file.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            imageUrl = s3Client.getUrl(bucket, fileName).toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("IMAGE_UPLOAD_ERROR");
        }
        return imageUrl;
    }

    // 이미지파일명 중복 방지
    private String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    // 파일 유효성 검사
    private String getFileExtension(String fileName) {
        if (fileName.length() == 0) {
            throw new IllegalArgumentException("IMAGE_UPLOAD_ERROR");
        }
        ArrayList<String> fileValidate = new ArrayList<>();
        fileValidate.add(".jpg");
        fileValidate.add(".jpeg");
        fileValidate.add(".png");
        fileValidate.add(".JPG");
        fileValidate.add(".JPEG");
        fileValidate.add(".PNG");
        fileValidate.add(".mp4");
        String idxFileName = fileName.substring(fileName.lastIndexOf("."));
        if (!fileValidate.contains(idxFileName)) {
            throw new IllegalArgumentException("IMAGE_UPLOAD_ERROR");
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // DeleteObject를 통해 S3 파일 삭제
    public void deleteFile(String fileName) {
        String objectKey = parseObjectKeyFromUrl(fileName);

        // 삭제
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, objectKey);
        s3Client.deleteObject(deleteObjectRequest);
    }

    private String parseObjectKeyFromUrl(String objectUrl) {
        return objectUrl.substring(objectUrl.lastIndexOf('/') + 1);
    }


    public String upload(byte[] imageBytes, String s3DestinationPath) {
        try {
            // Create an ObjectMetadata to provide content type information
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/png"); // Set the appropriate content type

            // Upload the image bytes to S3
            s3Client.putObject(new PutObjectRequest(
                    bucket, // Replace with your S3 bucket name
                    s3DestinationPath,    // The S3 destination path (including the object key)
                    new ByteArrayInputStream(imageBytes), // Provide the image bytes as input stream
                    metadata));

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, s3DestinationPath);
            Date expiration = new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)); // Set the expiration time for the URL (e.g., 7 days)
            generatePresignedUrlRequest.setExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            // Optionally, you can log or handle the URL here
            return url.toString();
            // Optionally, you can log or handle a successful upload here
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., log the error)
        }
        return s3DestinationPath;
    }
}


