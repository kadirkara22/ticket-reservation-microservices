package com.ticketguru.user_service;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.stereotype.Component;

import java.io.IOException;


@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}

@Component
@Slf4j
class RequestLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        // GELEN İSTEĞİN ADRESİNİ YAZDIRIYORUZ
        log.info("📢 USER SERVICE'E GELEN İSTEK: Method={} | URL={}", req.getMethod(), req.getRequestURI());
        chain.doFilter(request, response);
    }
}