package org.sirius.gmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/7 下午9:10
 */
@EnableFeignClients(basePackages = {"org.sirius.gmall.search.feign"})
@EnableDiscoveryClient
@SpringBootApplication
public class GmallSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallSearchApplication.class, args);
    }
}
