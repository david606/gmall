package org.sirius.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author david
 */
@MapperScan(value = {"org.sirius.gmall.product.dao"})
@EnableFeignClients(basePackages = {"org.sirius.gmall.product.feign"})
@EnableDiscoveryClient
@SpringBootApplication
public class GmallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallProductApplication.class, args);
    }
}
