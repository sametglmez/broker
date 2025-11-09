package com.example.broker.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Correlation ID oluştur (request başına)
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        // Thread ID
        MDC.put("threadId", String.valueOf(Thread.currentThread().getId()));

        return true; // request devam eder
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.clear(); // Thread-local temizle
    }
}