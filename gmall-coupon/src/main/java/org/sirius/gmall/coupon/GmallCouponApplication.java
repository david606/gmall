package org.sirius.gmall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author david
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"org.sirius.gmall.coupon.feign"})
@MapperScan(value = {"org.sirius.gmall.coupon.dao"})
public class GmallCouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallCouponApplication.class, args);
    }
}
