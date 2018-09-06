package com.test.qcloud.config;

import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class COSCredentialsConfigure {
    @Value("${cos.qcloud.secret.id}")
    private String accessKey;
    @Value("${cos.qcloud.secret.key}")
    private String secretKey;

    @Bean
    public COSCredentials getCOSCredentials() {
        // 1 初始化用户身份信息(secretId, secretKey)
        return new BasicCOSCredentials(accessKey, secretKey);
    }
}
