package com.cognifide.cq.includefilter.processor;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.includefilter.Configuration;
import com.cognifide.cq.includefilter.DynamicIncludeFilter;
import com.cognifide.cq.includefilter.RequestProcessor;
import com.cognifide.cq.includefilter.MutableUrl;

public class ResourceIncludingProcessor implements RequestProcessor {

	static final String INCLUDED_ATTRIBUTE = DynamicIncludeFilter.class.getName() + ".included";

	private static final Logger LOG = LoggerFactory.getLogger(ResourceIncludingProcessor.class);

	private final Configuration config;

	public ResourceIncludingProcessor(Configuration config) {
		this.config = config;
	}

	@Override
	public boolean accepts(SlingHttpServletRequest request) {
		return config.hasIncludeSelector(request);
	}

	@Override
	public void process(SlingHttpServletRequest request, SlingHttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		MutableUrl url = new MutableUrl(request, false);
		Resource resource = request.getResource();
		String resourceType = resource.getResourceType();

		if (config.isSupportedResourceType(resourceType, request)) {
			url.removeSelector(config.getIncludeSelector());

			RequestDispatcherOptions options = new RequestDispatcherOptions();
			RequestDispatcher dispatcher = request.getRequestDispatcher(url.toString(), options);
			request.setAttribute(INCLUDED_ATTRIBUTE, Boolean.TRUE);
			dispatcher.include(request, response);
		} else {
			LOG.error("User tries to include " + resourceType
					+ " but it is not in the dynamic resource type list.");
		}
	}
}
