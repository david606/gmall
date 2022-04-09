//package org.sirius.gmall.product.config;
//
//import com.alibaba.druid.pool.DruidDataSource;
//import com.zaxxer.hikari.HikariDataSource;
//import io.seata.rm.datasource.DataSourceProxy;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.StringUtils;
//
//import javax.annotation.Resource;
//import javax.sql.DataSource;
//
///**
// * @author david
// * @email my_good_dream@126.com
// * @date 2022/4/6 下午2:41
// */
//@Configuration
//public class ProductSeataConfig {
//
//    @Resource
//    private DataSourceProperties dataSourceProperties;
//
//    @Bean
//    public DataSource hikariDataSource(DataSourceProperties dataSourceProperties) {
//
//        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
//        if (StringUtils.hasText(dataSourceProperties.getName())) {
//            dataSource.setPoolName(dataSourceProperties.getName());
//        }
//
//        return new DataSourceProxy(dataSource);
//    }

// TODO seata 比较麻烦，配置比较复杂，后面再研究
//    public DataSource druidDataSource(DataSourceProperties dataSourceProperties) {
//        DruidDataSource dataSource = dataSourceProperties.
//                initializeDataSourceBuilder()
//                .type(DruidDataSource.class)
//                .build();
//       return new DataSourceProxy(dataSource);
//    }
//}
