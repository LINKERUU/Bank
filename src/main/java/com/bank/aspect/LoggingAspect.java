package com.bank.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging method execution in controller classes.
 * Provides logging before method execution, after successful execution,
 * and when exceptions are thrown.
 */
@Aspect
@Component
public class LoggingAspect {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  // Логирование перед выполнением метода
  /**
   * Logs method execution before it is invoked.
   *
   * @param joinPoint the join point representing the method execution
   */
  @Before("execution(* com.bank.controller.*.*(..))")
  public void logBefore(JoinPoint joinPoint) {
    if (logger.isInfoEnabled()) { // Проверяем, включен ли уровень INFO
      logger.info("Executing method: {}", joinPoint.getSignature().toShortString());
    }
  }

  // Логирование после успешного выполнения метода
  /**
   * Logs method execution after successful completion.
   *
   * @param joinPoint the join point representing the method execution
   * @param result the value returned by the method execution
   */
  @AfterReturning(pointcut = "execution(* com.bank.controller.*.*(..))", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, Object result) {
    if (logger.isInfoEnabled()) { // Проверяем, включен ли уровень INFO
      logger.info("Method {} executed successfully. Result: {}",
              joinPoint.getSignature().toShortString(), result);
    }
  }

  // Логирование ошибок
  /**
   * Logs exceptions thrown during method execution.
   *
   * @param joinPoint the join point representing the method execution
   * @param error the exception thrown by the method
   */
  @AfterThrowing(pointcut = "execution(* com.bank.controller.*.*(..))", throwing = "error")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
    if (logger.isErrorEnabled()) { // Проверяем, включен ли уровень ERROR
      logger.error("Error in method: {}. Error: {}",
              joinPoint.getSignature().toShortString(), error.getMessage());
    }
  }
}