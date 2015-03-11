package com.cognifide.cq.includefilter.processor;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.includefilter.Configuration;
import com.cognifide.cq.includefilter.RequestProcessor;
import com.cognifide.cq.includefilter.MutableUrl;

public class SyntheticResourceIncludingProcessor implements RequestProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(SyntheticResourceIncludingProcessor.class);

	private final Configuration config;

	public SyntheticResourceIncludingProcessor(Configuration config) {
		this.config = config;
	}

	@Override
	public boolean accepts(SlingHttpServletRequest request) {
		String resourceType = getResourceTypeFromSuffix(request);
		return resourceType != null && config.hasIncludeSelector(request)
				&& config.isSupportedResourceType(resourceType, request);
	}

	@Override
	public void process(SlingHttpServletRequest request, SlingHttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		MutableUrl url = new MutableUrl(request, false);
		String resourceType = getResourceTypeFromSuffix(request);
		boolean synthetic = ResourceUtil.isSyntheticResource(request.getResource());

		if (synthetic && config.isSupportedResourceType(resourceType, request)) {
			RequestDispatcherOptions options = new RequestDispatcherOptions();
			options.setForceResourceType(resourceType);

			Resource res = request.getResource();
			String existingResource = getParentExistingResource(res.getPath(), res.getResourceResolver());

			String path = url.getPath();
			// add component name to the most related parent existing resource
			if (!existingResource.isEmpty()) {
				path = existingResource + path.substring(path.lastIndexOf('/'));
			}

			RequestDispatcher dispatcher = request.getRequestDispatcher(path, options);
			request.setAttribute(ResourceIncludingProcessor.INCLUDED_ATTRIBUTE, Boolean.TRUE);
			dispatcher.forward(request, response);
		} else if (!synthetic) {
			LOG.error("User tries to include " + request.getResource().getPath()
					+ " but it is not a synthetic resource.");
		} else {
			LOG.error("User tries to include " + resourceType
					+ " but it is not in the dynamic resource type list.");
		}

	}

	private static String getResourceTypeFromSuffix(SlingHttpServletRequest request) {
		String suffix = request.getRequestPathInfo().getSuffix();
		if (suffix != null && suffix.charAt(0) == '/') {
			suffix = suffix.substring(1);
		}
		return suffix;
	}

	private static String getParentExistingResource(String syntheticResourcePath, ResourceResolver resolver) {
		String[] synthPath = syntheticResourcePath.split("/");
		StringBuilder path = new StringBuilder();
		for (int i = 1; i < synthPath.length; i++) {
			String s = synthPath[i];
			path.append('/').append(s);
			if (resolver.getResource(path.toString()) == null) {
				path.delete(path.length() - (s.length() + 1), path.length());
				break;
			}
		}
		return path.toString();
	}

}
