package org.sirius.gmall.thirdparty.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import lombok.extern.slf4j.Slf4j;
import org.sirius.common.utils.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/4 下午5:21
 */
@Slf4j
@RestController
@RequestMapping("/thirdparty/oss")
public class OssController {

    @Resource
    private OSS ossClient;

    @Value("${alibaba.cloud.access-key}")
    String accessId;
    @Value("${alibaba.cloud.secret-key}")
    String accessKey;
    @Value("${alibaba.cloud.oss.endpoint}")
    String endpoint;
    @Value("${alibaba.cloud.bucket}")
    String bucket = "bucket-name";

    @RequestMapping("/policy")
    protected R policy() {

        // https://goldenmall.oss-cn-beijing.aliyuncs.com/test.docx
        String host = "https://" + bucket + "." + endpoint;

        // 用户上传文件时指定的前缀：按当天日期创建目录保存文件
        String dir = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/";

        Map<String, String> respMap = null;
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);

            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConditions = new PolicyConditions();
            policyConditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConditions.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConditions);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));

        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return R.ok().put("data", respMap);
    }
}
