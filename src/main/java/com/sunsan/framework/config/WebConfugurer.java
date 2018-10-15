package com.sunsan.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.sunsan.framework.interceptors.LoginInterceptor;
@Configuration
public class WebConfugurer extends WebMvcConfigurationSupport {
	
	@Autowired
	private LoginInterceptor loginInterceptor;

	@Override
	protected void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(loginInterceptor);
		super.addInterceptors(registry);
	}

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		
		
		
		super.addResourceHandlers(registry);
	}
	
}
