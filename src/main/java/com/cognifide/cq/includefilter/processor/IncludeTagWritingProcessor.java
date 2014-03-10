package com.cognifide.cq.includefilter.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.FilterChain;

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

	private static final String COMMENT = "<!-- Following component is included by DynamicIncludeFilter (path: %s ) -->\n";

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
		if (config.getSkipWithParams() && params.hasMoreElements()) {
			return false;
		}
		if (config.getOnlyIncluded() && servletPath == null) {
			return false;
		}
		String resourceType = request.getResource().getResourceType();
		return config.isSupportedResourceType(resourceType, request);
	}

	@Override
	public void process(SlingHttpServletRequest request, SlingHttpServletResponse response, FilterChain chain)
			throws IOException {
		boolean synthetic = ResourceUtil.isSyntheticResource(request.getResource());
		IncludeGenerator generator = generatorFactory.getGenerator(config.getIncludeTypeName());
		if (generator != null) {
			String url = getUrl(request, synthetic);
			String include = generator.getInclude(url);
			LOG.debug(include);

			PrintWriter writer = response.getWriter();
			if (config.getAddComment()) {
				writer.append(String.format(COMMENT, url));
			}
			writer.append(include);
		} else {
			LOG.error("Can't filter; includeGenerator is null");
		}
	}

	private String getUrl(SlingHttpServletRequest request, boolean synthetic) {
		boolean isSynthetic = synthetic;
		Resource resource = request.getResource();
		MutableUrl url = new MutableUrl(request, true);

		url.setDefaultExtension(config.getDefaultExtension());
		url.addSelector(config.getIncludeSelector());
		if (isSynthetic) {
			url.replaceSuffix(resource.getResourceType());
		}
		return url.toString();
	}
}
