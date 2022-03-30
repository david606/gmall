package org.sirius.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author david
 */
@MapperScan(value = {"org.sirius.gmall.product.dao"})
@SpringBootApplication
public class GmallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallProductApplication.class, args);
    }
}
