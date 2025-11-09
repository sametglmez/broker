package com.example.broker.service;


import com.example.broker.security.SensitiveData;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.stream.StreamSupport;

@Aspect
@Component
@Slf4j
public class GlobalLoggingAspect {

    /**
     * com.example.broker paketindeki ve alt paketlerdeki tüm public methodları loglar.
     */
    @Around("execution(public * com.example.broker.service..*(..)) || execution(public * com.example.broker.controller..*(..))")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {


        Object[] args = joinPoint.getArgs();

        Object[] maskedArgs = Arrays.stream(args)
                .map(this::maskSensitiveData)
                .toArray();

        log.info(">>> REQUEST -> {}.{}() | Parameters: {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(maskedArgs));

        Object result = joinPoint.proceed();

        Object maskedResult = maskSensitiveData(result);

        log.info("<<< RESPONSE <- {}.{}() | Returned: {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                maskedResult);

        return result;
    }

    private Object maskSensitiveData(Object obj) {
        if (obj == null) return null;

        if (obj instanceof Iterable<?> iterable) {
            return StreamSupport.stream(iterable.spliterator(), false)
                    .map(this::maskSensitiveData)
                    .collect(Collectors.toList());
        }

        if (obj.getClass().isArray()) {
            return Arrays.stream((Object[]) obj)
                    .map(this::maskSensitiveData)
                    .toArray();
        }

        try {
            Object copy = obj.getClass().getDeclaredConstructor().newInstance();
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);

                if (field.isAnnotationPresent(SensitiveData.class)) {
                    field.set(copy, "***MASKED***");
                } else {
                    field.set(copy, value);
                }
            }

            return copy;

        } catch (Exception e) {
            return obj;
        }
    }
}