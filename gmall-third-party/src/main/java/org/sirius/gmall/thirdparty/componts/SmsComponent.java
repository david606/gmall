package org.sirius.gmall.thirdparty.componts;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.sirius.gmall.thirdparty.utils.HttpUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/4 下午5:10
 */
@ConfigurationProperties(prefix = "spring.aliyun.sms")
@Component
@Data
@Slf4j
public class SmsComponent {
    private String host;
    private String path;
    private String method;
    private String template;
    private String appCode;

    public void sendSms(String phone, String code) {

        log.info("sms: send msg to {} ...", phone);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appCode);

        Map<String, String> query = new HashMap<String, String>();
        query.put("receive", phone);
        query.put("tag", code);
        query.put("templateId", template);

        Map<String, String> body = new HashMap<String, String>();

        try {
            log.info("sms: start to send... ");
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, query, body);
            log.info("sms: start to send done");
            //获取response的body
            log.info(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
