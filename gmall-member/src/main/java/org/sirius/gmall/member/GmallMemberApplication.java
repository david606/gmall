package org.sirius.gmall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author david
 */
@SpringBootApplication
@MapperScan(value = {"org.sirius.gmall.member.dao"})
public class GmallMemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallMemberApplication.class, args);
    }
}
