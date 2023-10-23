//package com.yidiansishiyi.aimodule.config;
//
//import javax.sql.DataSource;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//
//@Configuration
//public class DataSourceConfiguration extends WebMvcConfigurerAdapter {
//	 @Bean(name = "dataSource")
//	 @Qualifier(value = "dataSource")
//	 @Primary
//	 @ConfigurationProperties(prefix = "spring.datasource")
//	 public DataSource dataSource(){
//		 DataSource dataSource= DataSourceBuilder.create()
//				 .type(com.mchange.v2.c3p0.ComboPooledDataSource.class).build();
//	   return dataSource;
//	 }
//}