package org.sirius.gmall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author david
 */
@SpringBootApplication
@MapperScan(value = {"org.sirius.gmall.coupon.dao"})
public class GmallCouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallCouponApplication.class, args);
    }
}
