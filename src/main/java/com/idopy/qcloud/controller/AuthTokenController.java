package com.idopy.qcloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.COSSigner;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.Date;

/**
 * 使用签名上传文件
 */
@RestController
@RequestMapping("/qcloud/auth")
public class AuthTokenController {

    @Autowired
    private COSCredentials cred;
    @Value("${cos.qcloud.token.expire.millis}")
    private Long expiredMills = 1000L;
    @Value("${cos.qcloud.upload.path}")
    private String uploadPath;
    @Value("${cos.qcloud.host}")
    private String host;

    /**
     * 生成签名
     */
    @RequestMapping("/token")
    public String genToken(String method){
        COSSigner signer = new COSSigner();
        Date expiredTime = new Date(System.currentTimeMillis() + expiredMills);
        HttpMethodName httpMethodName = HttpMethodName.PUT;
        if ("post".equalsIgnoreCase(method)) {
            httpMethodName = HttpMethodName.POST;
        }
        String key = "/";
        String sign = signer.buildAuthorizationStr(httpMethodName, key, cred, expiredTime);
        JSONObject res = new JSONObject();
        res.put("token", sign);
        res.put("postUrl", host + key);
        return res.toJSONString();
    }

    /**
     * 生成带签名的上传URL
     * @return
     */
    @RequestMapping("uploadUrl")
    public String genPresignedUploadUrl() {
        ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing"));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // bucket名需包含appid
        String bucketName = "idopy-1253901570";

        String key = "/upload/1.png";
        Date expirationTime = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
        URL url = cosclient.generatePresignedUrl(bucketName, key, expirationTime, HttpMethodName.PUT);
        JSONObject res = new JSONObject();
        res.put("url", url);
        return res.toJSONString();
    }
}
