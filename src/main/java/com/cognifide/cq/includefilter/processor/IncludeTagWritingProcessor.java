package com.cognifide.cq.includefilter.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.includefilter.Configuration;
import com.cognifide.cq.includefilter.RequestProcessor;
import com.cognifide.cq.includefilter.MutableUrl;
import com.cognifide.cq.includefilter.generator.IncludeGenerator;
import com.cognifide.cq.includefilter.generator.IncludeGeneratorFactory;

public class IncludeTagWritingProcessor implements RequestProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(IncludeTagWritingProcessor.class);

	private static final String COMMENT = "<!-- SDI include (path: %s, resourceType: %s) -->\n";

	private final Configuration config;

	private final IncludeGeneratorFactory generatorFactory;

	public IncludeTagWritingProcessor(Configuration config, IncludeGeneratorFactory generatorFactory) {
		this.config = config;
		this.generatorFactory = generatorFactory;
	}

	@Override
	public boolean accepts(SlingHttpServletRequest request) {
		Object servletPath = request.getAttribute("javax.servlet.include.servlet_path");
		Enumeration<?> params = request.getParameterNames();
		if (params.hasMoreElements()) {
			return false;
		}
		if (servletPath == null) {
			return false;
		}
		String resourceType = request.getResource().getResourceType();
		return config.isSupportedResourceType(resourceType, request);
	}

	@Override
	public void process(SlingHttpServletRequest request, SlingHttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		IncludeGenerator generator = generatorFactory.getGenerator(config.getIncludeTypeName());
		if (generator == null) {
			LOG.error("Can't filter; includeGenerator is null");
			return;
		}

		PrintWriter writer = response.getWriter();
		boolean synthetic = ResourceUtil.isSyntheticResource(request.getResource());
		String url = getUrl(request, synthetic);

		if (config.getAddComment()) {
			writer.append(String.format(COMMENT, url, request.getResource().getResourceType()));
		}

		// Only write the includes markup if the required, configurable request header is present
		if (shouldWriteIncludes(request)) {
			String include = generator.getInclude(url);
			LOG.debug(include);

			writer.append(include);
		} else {
			chain.doFilter(request, response);
		}
	}

	private boolean shouldWriteIncludes(final SlingHttpServletRequest request) {
		final String requiredHeader = config.getRequiredHeader();
		return StringUtils.isBlank(requiredHeader) || containsHeader(requiredHeader, request);
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

	private String getUrl(SlingHttpServletRequest request, boolean synthetic) {
		boolean isSynthetic = synthetic;
		Resource resource = request.getResource();
		MutableUrl url = new MutableUrl(request, true);

		url.setDefaultExtension("html");
		url.addSelector(config.getIncludeSelector());
		if (isSynthetic) {
			url.replaceSuffix(resource.getResourceType());
		}
		return url.toString();
	}
}
