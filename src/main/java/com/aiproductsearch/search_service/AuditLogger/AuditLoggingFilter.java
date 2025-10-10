package com.aiproductsearch.search_service.AuditLogger;

import java.io.IOException;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuditLoggingFilter implements Filter{

	public static final String CORR_ID_HEADER = "X-Correlation-Id";
	private static final String MDC_CORR_ID = "corrId";
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		long start = System.nanoTime();
		
		String corrId = req.getHeader(CORR_ID_HEADER);
		if(corrId == null || corrId.isBlank()) corrId = UUID.randomUUID().toString();
		
		MDC.put(MDC_CORR_ID, corrId);        // add to logging context
	    resp.setHeader(CORR_ID_HEADER, corrId); // echo back to caller
	    
	    int status = 500;
	    
	    try {
	    	chain.doFilter(request, response);
	    	status = resp.getStatus();
	    }
	    finally {
	    	long ms = (System.nanoTime() - start)/1_000_000;
	    	
	    	String method = req.getMethod();
	    	String path = req.getRequestURI();
	    	String q = req.getQueryString();
	    	
	    	if(q!=null&&!q.isBlank()) path+= "?"+q;
	    	
	    	String user = "dev-user-123";
	    	
	    	log.info("AUDIT corrId={} user={} {} {} status={} latencyMs={}", corrId, user, method, path, status, ms);
	    	
	    	MDC.remove(MDC_CORR_ID);
	    }
		
	}

}
