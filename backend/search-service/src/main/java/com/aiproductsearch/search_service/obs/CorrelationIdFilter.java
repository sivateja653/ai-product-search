package com.aiproductsearch.search_service.obs;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;

@Component
public class CorrelationIdFilter implements Filter {

  private static final String HDR = "X-Request-ID";
  private static final String MDC_KEY = "CorrelationId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      var http = (HttpServletRequest) request;
      String id = http.getHeader(HDR);
      if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
      MDC.put(MDC_KEY, id);
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
