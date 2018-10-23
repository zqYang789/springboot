package com.sunsan.framework.config;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.ext.spring.BeetlGroupUtilConfiguration;
import org.beetl.ext.spring.BeetlSpringViewResolver;
import org.beetl.sql.core.ClasspathLoader;
import org.beetl.sql.core.Interceptor;
import org.beetl.sql.core.UnderlinedNameConversion;
import org.beetl.sql.core.db.MySqlStyle;
import org.beetl.sql.ext.DebugInterceptor;
import org.beetl.sql.ext.spring4.BeetlSqlDataSource;
import org.beetl.sql.ext.spring4.BeetlSqlScannerConfigurer;
import org.beetl.sql.ext.spring4.SqlManagerFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class DataSourceConfig {

		//配置文件
		@Bean(initMethod = "init", name = "beetlConfig")
		public BeetlGroupUtilConfiguration getBeetlGroupUtilConfiguration() {
			BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = new BeetlGroupUtilConfiguration();
			ResourcePatternResolver patternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
			try {
				// WebAppResourceLoader 配置root路径是关键

				ClasspathResourceLoader classPathLoader = new ClasspathResourceLoader(this.getClass().getClassLoader(),"classpath:/templates");
				beetlGroupUtilConfiguration.setResourceLoader(classPathLoader);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//读取配置文件信息
			return beetlGroupUtilConfiguration;

		}

		@Bean(name = "beetlViewResolver")
		public BeetlSpringViewResolver getBeetlSpringViewResolver(@Qualifier("beetlConfig") BeetlGroupUtilConfiguration beetlGroupUtilConfiguration) {
			BeetlSpringViewResolver beetlSpringViewResolver = new BeetlSpringViewResolver();
			beetlSpringViewResolver.setContentType("text/html;charset=UTF-8");
			beetlSpringViewResolver.setOrder(0);
			beetlSpringViewResolver.setConfig(beetlGroupUtilConfiguration);
			return beetlSpringViewResolver;
		}

		//配置包扫描
		@Bean(name = "beetlSqlScannerConfigurer")
		public BeetlSqlScannerConfigurer getBeetlSqlScannerConfigurer() {
			BeetlSqlScannerConfigurer conf = new BeetlSqlScannerConfigurer();
			conf.setBasePackage("com.sunsan.project.dao");
			conf.setDaoSuffix("Dao");
			conf.setSqlManagerFactoryBeanName("sqlManagerFactoryBean");
			return conf;
		}

		@Bean(name = "sqlManagerFactoryBean")
		@Primary
		public SqlManagerFactoryBean getSqlManagerFactoryBean(@Qualifier("datasource") DataSource datasource) {
			SqlManagerFactoryBean factory = new SqlManagerFactoryBean();

			BeetlSqlDataSource source = new BeetlSqlDataSource();
			source.setMasterSource(datasource);
			factory.setCs(source);
			factory.setDbStyle(new MySqlStyle());
			factory.setInterceptors(new Interceptor[]{new DebugInterceptor()});
			factory.setNc(new UnderlinedNameConversion());//开启驼峰
			factory.setSqlLoader(new ClasspathLoader("/sql"));//sql文件路径
			return factory;
		}


		//配置数据库
		@Bean(name = "datasource")
		@ConfigurationProperties("spring.datasource.admin")
        @Primary
		public DruidDataSource adminDataSource() {
			DruidDataSource druidDataSource = new DruidDataSource();
			return druidDataSource;
		}

		//开启事务
		@Bean(name = "txManager")
		public DataSourceTransactionManager getDataSourceTransactionManager(@Qualifier("datasource") DataSource datasource) {
			DataSourceTransactionManager dsm = new DataSourceTransactionManager();
			dsm.setDataSource(datasource);
			return dsm;
		}
}
