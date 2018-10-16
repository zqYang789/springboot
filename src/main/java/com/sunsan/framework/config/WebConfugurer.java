package com.sunsan.framework.config;

import com.sunsan.framework.interceptors.FrequencyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import com.sunsan.framework.interceptors.LoginInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfugurer extends WebMvcConfigurerAdapter {
	
	@Autowired
	private LoginInterceptor loginInterceptor;
	@Autowired
	private FrequencyInterceptor  frequencyInterceptor;


	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(frequencyInterceptor);
		registry.addInterceptor(loginInterceptor);
		super.addInterceptors(registry);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		super.addResourceHandlers(registry);
	}
	
}
