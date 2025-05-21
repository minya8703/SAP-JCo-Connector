package jco.jcosaprfclink.config.saprfc.connection.handler;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RfcLoggingAspect {
    
    @Around("execution(* jco.jcosaprfclink.config.saprfc.connection.handler.*.handleRequest(..))")
    public Object logRfcExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        SapRfcServerHandler handler = (SapRfcServerHandler) joinPoint.getTarget();
        log.info("RFC 요청 시작: function={}, handler={}", 
            handler.getFunctionName(), handler.getClass().getSimpleName());
        
        try {
            Object result = joinPoint.proceed();
            log.info("RFC 요청 완료: function={}", handler.getFunctionName());
            return result;
        } catch (Exception e) {
            log.error("RFC 요청 실패: function={}, error={}", 
                handler.getFunctionName(), e.getMessage(), e);
            throw e;
        }
    }
} 