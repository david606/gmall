package org.sirius.gmall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author david
 */
@SpringBootApplication
@MapperScan(value = {"org.sirius.gmall.order.dao"})
public class GmallOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallOrderApplication.class, args);
    }
}
