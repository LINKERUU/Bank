package com.bank.aspect;

import com.bank.service.impl.VisitCounterServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aspect that tracks visits to controller endpoints by incrementing visit counters.
 * This aspect intercepts all controller method executions and logs the visit.
 */
@Aspect
@Component
public class VisitCounterAspect {

  @Autowired
  private VisitCounterServiceImpl visitCounterService;

  @Autowired
  private HttpServletRequest request;

  /**
   * Advice that increments visit count before controller method execution.
   * Captures the request URI and increments its visit counter.
   */
  @Before("execution(* com.bank.controller..*.*(..))")
  public void countVisit() {
    String url = request.getRequestURI();
    visitCounterService.incrementVisitCount(url);
  }
}