package org.sirius.gmall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/6 下午2:12
 */
@ConfigurationProperties(prefix = "gmall.thread")
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
