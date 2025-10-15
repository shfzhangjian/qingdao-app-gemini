package com.lucksoft.qingdao.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 第二数据源 (qdjl) 配置.
 */
@Configuration
// 指定此配置只扫描 qdjl 包下的 Mapper 接口
@MapperScan(basePackages = "com.lucksoft.qingdao.qdjl.mapper", sqlSessionFactoryRef = "qdjlSqlSessionFactory")
public class QdjlDataSourceConfig {

    /**
     * 步骤 1: 创建一个专门用于绑定 "spring.datasource.qdjl" 属性的 Bean.
     */
    @Bean(name = "qdjlDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.qdjl")
    public DataSourceProperties qdjlDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 步骤 2: 使用上面配置好的 properties Bean 来创建真正的数据源 DataSource.
     */
    @Bean(name = "qdjlDataSource")
    public DataSource qdjlDataSource(@Qualifier("qdjlDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    /**
     * 为 qdjl 数据源创建 MyBatis 的 SqlSessionFactory.
     * 注意 Bean 名称的唯一性。
     */
    @Bean(name = "qdjlSqlSessionFactory")
    public SqlSessionFactory qdjlSqlSessionFactory(@Qualifier("qdjlDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    /**
     * 为 qdjl 数据源配置事务管理器.
     * 注意 Bean 名称的唯一性。
     */
    @Bean(name = "qdjlTransactionManager")
    public DataSourceTransactionManager qdjlTransactionManager(@Qualifier("qdjlDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
