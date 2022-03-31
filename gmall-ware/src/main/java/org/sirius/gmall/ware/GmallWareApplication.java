package org.sirius.gmall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author david
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"org.sirius.gmall.ware.feign"})
@MapperScan(value = {"org.sirius.gmall.ware.dao"})
public class GmallWareApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallWareApplication.class, args);
    }
}
