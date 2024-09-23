package com.powernobug.mall.product.config;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.config
 * @author: HuangWeiLong
 * @date: 2024/9/21 17:07
 */
import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    // @Value("${minio.endpointUrl}")
    String endpointUrl;

    // @Value("${minio.accessKey}")
    String accessKey;

    // @Value("${minio.secreKey}")
    String secreKey;

    // @Value("${minio.bucketName}")
    String bucketName;

    @Bean
    public MinioClient minioClient(){


        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpointUrl)
                .credentials(accessKey, secreKey)
                .build();


        return minioClient;
    }
}
