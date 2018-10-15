package com.sunsan.framework.filters;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;


@Component
public class ApiOriginFilter implements javax.servlet.Filter {
    private String[] allowDomain = {
            "http://127.0.0.1:8081",
            "http://localhost:8081",
            "http://192.168.1.14:8080",
            "https://www.test2.com",
            
    };
    

    
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        if (false) {
            String originHeader = ((HttpServletRequest) request).getHeader("Origin");
            if (ArrayUtils.contains(allowDomain, originHeader))
                res.addHeader("Access-Control-Allow-Origin", originHeader);
        } else {
            res.addHeader("Access-Control-Allow-Origin", "*");
        }
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        res.addHeader("Access-Control-Max-Age", "3600");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type, x-requested-with, X-Custom-Header, api_key");
        res.addHeader("Content-Type", "text/html;charset=UTF-8");
        chain.doFilter(request, response);
    }

    
    public void destroy() {
    }

    
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}
