package com.cognifide.cq.includefilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.ResourceUtil;

@SlingFilter(scope = SlingFilterScope.REQUEST, order = 0)
public class SyntheticResourceIncludingFilter implements Filter {

	@Reference
	private ConfigurationWhiteboard configurationWhiteboard;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
		final String resourceType = getResourceTypeFromSuffix(slingRequest);
		final Configuration config = configurationWhiteboard.getConfiguration(slingRequest, resourceType);

		if (config == null
				|| !config.hasIncludeSelector(slingRequest)
				|| !ResourceUtil.isSyntheticResource(slingRequest.getResource())) {
			chain.doFilter(request, response);
			return;
		}

		final RequestDispatcherOptions options = new RequestDispatcherOptions();
		options.setForceResourceType(resourceType);
		final RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(slingRequest.getResource(),
				options);
		dispatcher.forward(request, response);
	}

	private static String getResourceTypeFromSuffix(SlingHttpServletRequest request) {
		final String suffix = request.getRequestPathInfo().getSuffix();
		return StringUtils.removeStart(suffix, "/");
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}
}
