package com.cognifide.cq.includefilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.includefilter.generator.IncludeGenerator;
import com.cognifide.cq.includefilter.generator.IncludeGeneratorWhiteboard;

@SlingFilter(scope = SlingFilterScope.INCLUDE, order = 0)
public class IncludeTagWritingFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(IncludeTagWritingFilter.class);

	private static final String COMMENT = "<!-- SDI include (path: %s, resourceType: %s) -->\n";

	@Reference
	private ConfigurationWhiteboard configurationWhiteboard;

	@Reference
	private IncludeGeneratorWhiteboard generatorWhiteboard;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
		final String resourceType = slingRequest.getResource().getResourceType();

		final Configuration config = configurationWhiteboard.getConfiguration(slingRequest, resourceType);
		if (config == null) {
			chain.doFilter(request, response);
			return;
		}

		final IncludeGenerator generator = generatorWhiteboard.getGenerator(config.getIncludeTypeName());
		if (generator == null) {
			LOG.error("Invalid generator: " + config.getIncludeTypeName());
			chain.doFilter(request, response);
			return;
		}

		final PrintWriter writer = response.getWriter();
		final String url = getUrl(config, slingRequest);

		if (config.getAddComment()) {
			writer.append(String.format(COMMENT, url, resourceType));
		}

		// Only write the includes markup if the required, configurable request header is present
		if (shouldWriteIncludes(config, slingRequest)) {
			String include = generator.getInclude(url);
			LOG.debug(include);
			writer.append(include);
		} else {
			chain.doFilter(request, response);
		}
	}

	private boolean shouldWriteIncludes(Configuration config, SlingHttpServletRequest request) {
		if (requestHasParameters(config.getIgnoreUrlParams(), request)) {
			return false;
		}
		final String requiredHeader = config.getRequiredHeader();
		return StringUtils.isBlank(requiredHeader) || containsHeader(requiredHeader, request);
	}

	private boolean requestHasParameters(List<String> ignoreUrlParams, SlingHttpServletRequest request) {
		final Enumeration<?> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			final String paramName = (String) paramNames.nextElement();
			if (!ignoreUrlParams.contains(paramName)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsHeader(String requiredHeader, SlingHttpServletRequest request) {
		final String name, expectedValue;
		if (StringUtils.contains(requiredHeader, '=')) {
			final String split[] = StringUtils.split(requiredHeader, '=');
			name = split[0];
			expectedValue = split[1];
		} else {
			name = requiredHeader;
			expectedValue = null;
		}

		final String actualValue = request.getHeader(name);
		if (actualValue == null) {
			return false;
		} else if (expectedValue == null) {
			return true;
		} else {
			return actualValue.equalsIgnoreCase(expectedValue);
		}
	}

	private String getUrl(Configuration config, SlingHttpServletRequest request) {
		final boolean synthetic = ResourceUtil.isSyntheticResource(request.getResource());
		final Resource resource = request.getResource();
		final StringBuilder builder = new StringBuilder();
		final RequestPathInfo pathInfo = request.getRequestPathInfo();

		builder.append(pathInfo.getResourcePath());
		if (pathInfo.getSelectorString() != null) {
			builder.append('.').append(sanitize(pathInfo.getSelectorString()));
		}
		builder.append('.').append(config.getIncludeSelector());
		builder.append('.').append(pathInfo.getExtension());
		if (synthetic) {
			builder.append('/').append(resource.getResourceType());
		} else {
			builder.append(sanitize(pathInfo.getSuffix()));
		}
		return builder.toString();
	}

	private String sanitize(String dirtyString) {
		if (StringUtils.isBlank(dirtyString)) {
			return "";
		} else {
			return dirtyString.replaceAll("[^0-9a-zA-Z:.\\-/]", "");
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}
}
