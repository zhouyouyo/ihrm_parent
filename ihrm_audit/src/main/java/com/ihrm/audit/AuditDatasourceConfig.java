package com.ihrm.audit;

import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;


/**
 *  多数据源配置类
 *      1.activiti数据库连接池
 *          默认
 *      2.IHRM业务数据库连接池
 *          明确指定 ihrmDataSource
 */
@Configuration//1.activiti数据库连接池
public class AuditDatasourceConfig extends AbstractProcessEngineAutoConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.act")
    @Qualifier("activitiDataSource")
    public DataSource activitiDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.ihrm")
    @Qualifier("ihrmDataSource")
    public DataSource ihrmDataSource() {
        return DataSourceBuilder.create().build();
    }
}
