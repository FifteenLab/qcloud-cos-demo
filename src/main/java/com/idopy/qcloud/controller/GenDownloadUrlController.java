package com.idopy.qcloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.Date;

/**
 * 生成下载链接
 *
 */
@RestController
@RequestMapping("/qcloud/download/url")
public class GenDownloadUrlController {
    @Autowired
    private COSCredentials cred;
    @Value("${cos.qcloud.download.url.expire.millis}")
    private Long expiredMills = 1000L;
    // bucket名需包含appid
    @Value("${cos.qcloud.bucket}")
    private String bucketName;
    @Value("${cos.qcloud.region}")
    private String regionName;
    @Value("${cos.qcloud.upload.path}")
    private String uploadPath;

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public String simple(String filename) {
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);

        GeneratePresignedUrlRequest req =
                new GeneratePresignedUrlRequest(bucketName, uploadPath + filename, HttpMethodName.GET);
        // 设置签名过期时间(可选), 若未进行设置则默认使用ClientConfig中的签名过期时间(5分钟)
        Date expirationDate = new Date(System.currentTimeMillis() + expiredMills);
        req.setExpiration(expirationDate);

        URL url = cosclient.generatePresignedUrl(req);
        cosclient.shutdown();
        JSONObject res = new JSONObject();
        res.put("url", url);
        return res.toJSONString();
    }
}
