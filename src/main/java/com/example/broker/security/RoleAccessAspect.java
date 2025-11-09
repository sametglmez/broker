package com.example.broker.security;


import com.example.broker.entity.User;
import com.example.broker.exception.CustomException;
import com.example.broker.exception.ErrorType;
import com.example.broker.service.strategy.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAccessAspect {

    private final Map<String, RoleValidator> roleValidator;

    @Around(value = "@annotation(checkRoleAccess)", argNames = "jointPoint,checkRoleAccess")
    public Object validateRoleAccess(ProceedingJoinPoint joinPoint, CheckRoleAccess checkRoleAccess) throws Throwable {

        Long customerId = evalutaionRoleAccess(joinPoint,checkRoleAccess);

        // SecurityUser ve Role kontrolü
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        User user = securityUser.getUser();
        RoleValidator roleStrategy = roleValidator.get(user.getRole().getName());

        if (!roleStrategy.validateAccess(user, customerId)) {
            throw new CustomException(ErrorType.NO_ACCESS);
        }

        return joinPoint.proceed();
    }

    private Long evalutaionRoleAccess(ProceedingJoinPoint joinPoint, CheckRoleAccess checkRoleAccess) {
        Object param = joinPoint.getArgs()[0]; // args[0] yerine parametre dizinini annotation ile verebilirsin
        if (param instanceof Long) {
            return (Long) param; // Eğer Long ise direkt al
        }
        // DTO ise SpEL ile property oku
        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("arg", param);

        Object value = parser.parseExpression("#arg." + checkRoleAccess.customerIdParam())
                .getValue(context);

        if (value == null) throw new RuntimeException("CustomerId değeri null");
        return Long.parseLong(value.toString());
    }

}