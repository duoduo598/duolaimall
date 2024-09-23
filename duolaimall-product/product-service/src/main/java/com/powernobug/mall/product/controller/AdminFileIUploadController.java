package com.powernobug.mall.product.controller;


import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.config.MinioConfig;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

/**
 * 后台图片上传功能
 */

@RestController
public class AdminFileIUploadController {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioConfig minioConfig;


    /**
     *
     * 在这个方法中，做两件事情
     * 1. 上传文件到Minio中
     * 2. 返回文件（图片）的链接
     */
    @PostMapping("/admin/product/fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception {


        // 使用UUId来当做对象的名称
        String objectName = UUID.randomUUID().toString().replaceAll("-", "");


        // 2. 调用文件上传的方法
        minioClient.putObject(PutObjectArgs.builder()

                .bucket(minioConfig.getBucketName())        // 设置桶的名称
                .contentType(file.getContentType())         // 设置文件类型
                .stream(file.getInputStream(), file.getSize(), -1)  // 设置文件大小
                .object(objectName+Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")))
                .build());

        // 3. 计算图片的url
        String url = minioConfig.getEndpointUrl() + "/" + minioConfig.getBucketName() + "/"
                + objectName+Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));


        // 4. 返回
        return Result.ok(url);
    }
}
