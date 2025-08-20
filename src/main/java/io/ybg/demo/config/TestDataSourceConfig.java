package io.ybg.demo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@EnableTransactionManagement
@Configuration
@MapperScan(basePackages = "${mybatis.mapper-package}") // MyBatis MapperScannerConfigurer 설정
public class TestDataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(TestDataSourceConfig.class);

    @Bean
    @ConfigurationProperties("spring.test-datasource")
    public DataSource testDataSource() {
        return new HikariDataSource();
    }


    // MyBatis SqlSessionFactory 설정
    @Bean
    public SqlSessionFactory sqlSessionFactory(@Qualifier("testDataSource") DataSource testDataSource, @Value("${mybatis.mapper-location}") String mapperLocation) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(testDataSource);

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            sessionFactoryBean.setMapperLocations(resolver.getResources(mapperLocation));
        } catch (Exception e) {
            logger.error("PathMatchingResourcePatternResolver Error : {}", e.getMessage());
        }

        return sessionFactoryBean.getObject();
    }

    // MyBatis 트랜잭션 관리 설정
    @Bean
    public PlatformTransactionManager testTransactionManager(@Qualifier("testDataSource") DataSource testDataSource) {
        return new DataSourceTransactionManager(testDataSource);
    }
}
