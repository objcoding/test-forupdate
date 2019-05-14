package com.objcoding.transaction;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author zhangchenghui.dev@gmail.com
 * @since 2019-05-14
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        dataSource.setUrl("jdbc:mysql://193.112.61.178:3306/test?useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("objcoding@MYSQL0");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setInitialSize(20);
        dataSource.setMaxActive(20);

        return dataSource;
    }
}
