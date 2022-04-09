package org.sirius.gmall.product.config;

import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/4 下午11:12
 */
@Configuration
@EnableTransactionManagement
@MapperScan(value = {"org.sirius.gmall.product.dao"})
public class ProductBatisConfig {

    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor interceptor = new PaginationInnerInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        interceptor.setOverflow(true);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        interceptor.setMaxLimit(1000L);
        return interceptor;
    }
}
