package org.sirius.gmall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/4 下午5:35
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GmallThirdPartyApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallThirdPartyApplication.class, args);
    }
}
