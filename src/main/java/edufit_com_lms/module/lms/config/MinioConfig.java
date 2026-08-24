package edufit_com_lms.module.lms.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${aws.s3.endpoint:http://localhost:9000}")

    private String url;

    @Value("${aws.s3.access-key:minio_admin}")
    private String accessKey;

    @Value("${aws.s3.secret-key:minio_password_123}")
    private String secretKey;


    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
