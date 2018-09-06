package com.idopy.qcloud.controller;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * 文件上传
 *
 * @author Jack
 * @since 2018-09-06
 */
@RestController
@RequestMapping("/qcloud/upload")
public class UploadController {
    @Autowired
    private COSCredentials cred;
    @Autowired
    private ClientConfig clientConfig;
    @Value("${cos.qcloud.bucket}")
    private String bucketName;
    @Value("${cos.qcloud.upload.path}")
    private String uploadPath;
    /**
     * 首页
     * @return
     */
    @RequestMapping(value = {"", "/"})
    public ModelAndView index() {
        return new ModelAndView("qcloud/upload");
    }

    @PostMapping(value = "/single")
    public ModelAndView singleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        ModelAndView result = new ModelAndView("redirect:qcloud/upload/result");
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg", "请选择文件");
            return result;
        }
        COSClient cosClient = new COSClient(cred, clientConfig);
        String key = uploadPath + file.getName();
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            // 从输入流上传必须制定content length, 否则http客户端可能会缓存所有数据，存在内存OOM的情况
            objectMetadata.setContentLength(file.getSize());
            // 默认下载时根据cos路径key的后缀返回响应的contenttype, 上传时设置contenttype会覆盖默认值
            objectMetadata.setContentType(file.getContentType());

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), objectMetadata);
            // 设置存储类型, 默认是标准(Standard), 低频(standard_ia)
            putObjectRequest.setStorageClass(StorageClass.Standard_IA);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            // putobjectResult会返回文件的etag
            String etag = putObjectResult.getETag();
            redirectAttributes.addFlashAttribute("msg", "上传成功");
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭客户端
            cosClient.shutdown();
        }
        return result;
    }

    /**
     * 上传文件结果
     * @return
     */
    public ModelAndView result() {
        return new ModelAndView("qcloud/uploadResult");
    }
}
