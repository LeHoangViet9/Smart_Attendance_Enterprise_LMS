package edufit_com_lms.module.lms.service;

import edufit_com_lms.common.exception.BadRequestException;
import edufit_com_lms.module.lms.dto.request.PresignedUrlRequest;
import edufit_com_lms.module.lms.dto.response.PresignedUrlResponse;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${aws.s3.bucket-name:lms-media-bucket}")
    private String bucketName;

    @Value("${aws.s3.expiry-minutes:60}")
    private int defaultExpiryMinutes;

    @Value("${aws.s3.endpoint:http://localhost:9000}")
    private String minioUrl;

    @PostConstruct
    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Successfully initialized MinIO bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("Failed to check or initialize MinIO bucket at startup (please ensure MinIO server is running): {}", e.getMessage());
        }
    }

    public PresignedUrlResponse generatePresignedUploadUrl(PresignedUrlRequest request) {
        try {
            String extension = "";
            if (request.getFileName() != null && request.getFileName().contains(".")) {
                extension = request.getFileName().substring(request.getFileName().lastIndexOf(".")).toLowerCase();
            }

            // Organize storage path by year/month (e.g. 2026/08/uuid.pdf)
            String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String objectKey = datePrefix + "/" + UUID.randomUUID() + extension;

            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(defaultExpiryMinutes, TimeUnit.MINUTES)
                            .build()
            );

            String fileUrl = minioUrl + "/" + bucketName + "/" + objectKey;

            return PresignedUrlResponse.builder()
                    .uploadUrl(presignedUrl)
                    .objectKey(objectKey)
                    .fileUrl(fileUrl)
                    .expiryMinutes(defaultExpiryMinutes)
                    .build();

        } catch (Exception e) {
            log.error("Error generating MinIO Pre-signed URL: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to generate Pre-signed upload URL: " + e.getMessage());
        }
    }

    public String generatePresignedDownloadUrl(String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(expiryMinutes > 0 ? expiryMinutes : defaultExpiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating Pre-signed download URL for object {}: {}", objectKey, e.getMessage());
            throw new BadRequestException("Failed to generate download URL: " + e.getMessage());
        }
    }
}
