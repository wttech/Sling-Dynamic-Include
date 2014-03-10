package com.cognifide.cq.includefilter.processor;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import com.cognifide.cq.includefilter.RequestProcessor;

public class RequestPassingProcessor implements RequestProcessor {

	@Override
	public boolean accepts(SlingHttpServletRequest request) {
		return Boolean.TRUE.equals(request.getAttribute(ResourceIncludingProcessor.INCLUDED_ATTRIBUTE));
	}

	@Override
	public void process(SlingHttpServletRequest request, SlingHttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		response.setHeader("Expires", "-1"); // because IE likes to cache
		chain.doFilter(request, response);
	}
}
