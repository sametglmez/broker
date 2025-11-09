package com.example.broker.service;


import com.example.broker.security.SensitiveData;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class GlobalLoggingAspect {

    /**
     * com.example.broker paketindeki ve alt paketlerdeki tüm public methodları loglar.
     */
    @Around("execution(public * com.example.broker.service..*(..)) || execution(public * com.example.broker.controller..*(..))")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {


        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        log.info(">>> REQUEST -> {}.{}() | Parameters: {}", className, methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Response logu
            log.info("<<< RESPONSE <- {}.{}() | Returned: {} | Execution time: {} ms",
                    className, methodName, result, duration);

            return result;

        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("XXX EXCEPTION in {}.{}() | Execution time: {} ms | Exception: {}",
                    className, methodName, duration, ex.getMessage(), ex);

            throw ex;
        }
    }
}