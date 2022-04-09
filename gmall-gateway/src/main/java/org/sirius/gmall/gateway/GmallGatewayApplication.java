package org.sirius.gmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author david
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GmallGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallGatewayApplication.class, args);
    }
}
