package org.sirius.gmall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/6 下午2:36
 */
@Configuration
public class ProductRedissonConfig {

    /**
     * 所有对Redisson的使用都是通过RedissonClient对象
     *
     * @param url redis host
     * @return RedissonClient
     */
    @Bean
    public RedissonClient redisson(@Value("${spring.redis.host}") String url) {
        /*
         1.创建配置
           Redis url should start with redis:// or rediss://
        */
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + url + ":6379");

        // 2.根据Config创建出RedissonClient示例
        return Redisson.create(config);
    }
}
