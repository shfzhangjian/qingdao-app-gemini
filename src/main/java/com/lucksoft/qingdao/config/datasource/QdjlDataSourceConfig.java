package com.lucksoft.qingdao.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
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
     * 创建 qdjl 数据源 Bean.
     * @ConfigurationProperties 会自动从 application.properties 中读取 "spring.datasource.qdjl" 前缀的配置。
     */
    @Bean(name = "qdjlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.qdjl")
    public DataSource qdjlDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * 为 qdjl 数据源创建 MyBatis 的 SqlSessionFactory.
     * 注意 Bean 名称的唯一性。
     */
    @Bean(name = "qdjlSqlSessionFactory")
    public SqlSessionFactory qdjlSqlSessionFactory(@Qualifier("qdjlDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // 如果您有XML映射文件，可以在这里指定它们的位置
        // bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/qdjl/*.xml"));
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
