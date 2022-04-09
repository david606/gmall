package org.sirius.gmall.ware.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/9 上午11:34
 */
@Configuration
@MapperScan(value = {"org.sirius.gmall.ware.dao"})
public class MyBatisConfiguration {
}
