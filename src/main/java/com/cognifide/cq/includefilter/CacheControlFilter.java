package com.cognifide.cq.includefilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingFilter(scope = SlingFilterScope.REQUEST, order = 0)
public class CacheControlFilter implements Filter {

	private static final String HEADER_CACHE_CONTROL = "Cache-Control";

	private static final Logger LOG = LoggerFactory.getLogger(CacheControlFilter.class);

	@Reference
	private ConfigurationWhiteboard configurationWhiteboard;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
		final String resourceType = slingRequest.getResource().getResourceType();
		final Configuration config = configurationWhiteboard.getConfiguration(slingRequest, resourceType);

		if (config != null && config.hasTtlSet()) {
			SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
			slingResponse.setHeader(HEADER_CACHE_CONTROL, "max-age=" + config.getTtl());
			LOG.debug("set \"{}: max-age={}\" to {}", HEADER_CACHE_CONTROL, config.getTtl(), resourceType);
		}

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}
}
