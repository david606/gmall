package org.sirius.gmall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author david
 */
@EnableFeignClients(basePackages = {"org.sirius.gmall.order.feign"})
@SpringBootApplication
@MapperScan(value = {"org.sirius.gmall.order.dao"})
public class GmallOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallOrderApplication.class, args);
    }
}
