package org.apache.sling.dynamicinclude;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.engine.EngineConstants;
import org.osgi.framework.Constants;

@Component(metatype = true, label="Sling Dynamic Include : Synthetic Resource Include Filter")
@Service
@Properties({
	@Property(name = Constants.SERVICE_VENDOR, value = "The Apache Software Foundation"),
	@Property(name = EngineConstants.SLING_FILTER_SCOPE, value = EngineConstants.FILTER_SCOPE_REQUEST, propertyPrivate = true),
	@Property(name = Constants.SERVICE_RANKING, intValue = Integer.MIN_VALUE, propertyPrivate = false),
})
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
