package org.sirius.gmall.ware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author david
 */
@EnableFeignClients(basePackages = {"org.sirius.gmall.ware.feign"})
@EnableDiscoveryClient
@SpringBootApplication
public class GmallWareApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallWareApplication.class, args);
    }
}
