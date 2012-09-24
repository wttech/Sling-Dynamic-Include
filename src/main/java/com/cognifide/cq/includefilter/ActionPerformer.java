package com.cognifide.cq.includefilter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.includefilter.generators.IncludeGenerator;
import com.cognifide.cq.includefilter.generators.IncludeGeneratorFactory;

public class ActionPerformer {
	private static final String COMMENT = "<!-- Following component is included by DynamicIncludeFilter (path: %s ) -->\n";

	static final String INCLUDED_ATTRIBUTE = DynamicIncludeFilter.class.getName() + ".included";

	private static final Logger LOG = LoggerFactory.getLogger(DynamicIncludeFilter.class);

	private Configuration config;

	private IncludeGeneratorFactory generatorFactory;

	public ActionPerformer(Configuration config, IncludeGeneratorFactory generatorFactory) {
		this.config = config;
		this.generatorFactory = generatorFactory;
	}

	/**
	 * Writes include using chosen generator.
	 * 
	 * @param slingRequest
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	void replaceWithIncludeTag(SlingHttpServletRequest slingRequest, ServletResponse response,
			boolean synthetic) throws ServletException, IOException {
		IncludeGenerator generator = generatorFactory.getGenerator(config.getIncludeTypeName());
		if (generator != null) {
			String url = getUrl(slingRequest, synthetic);
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

	void includeSyntheticResource(SlingHttpServletRequest slingRequest, ServletResponse response)
			throws IOException, ServletException {
		UrlManipulator url = new UrlManipulator(slingRequest, false);
		String resourceType = url.getResourceTypeFromSuffix();
		boolean synthetic = ResourceUtil.isSyntheticResource(slingRequest.getResource());

		if (synthetic && config.isSupportedResourceType(resourceType, slingRequest)) {
			RequestDispatcherOptions options = new RequestDispatcherOptions();
			options.setForceResourceType(resourceType);

			Resource res = slingRequest.getResource();
			String existingResource = getParentExistingResource(res.getPath(), res.getResourceResolver());

			String path = url.getPath();
			// add component name to the most related parent existing resource
			if (!existingResource.isEmpty()) {
				path = existingResource + path.substring(path.lastIndexOf('/'));
			}

			RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(path, options);
			slingRequest.setAttribute(INCLUDED_ATTRIBUTE, Boolean.TRUE);
			dispatcher.include(slingRequest, response);
		} else if (!synthetic) {
			LOG.error("User tries to include " + slingRequest.getResource().getPath()
					+ " but it is not a synthetic resource.");
		} else {
			LOG.error("User tries to include " + resourceType
					+ " but it is not in the dynamic resource type list.");
		}
	}

	void includeResource(SlingHttpServletRequest slingRequest, ServletResponse response) throws IOException,
			ServletException {
		UrlManipulator url = new UrlManipulator(slingRequest, false);
		Resource resource = slingRequest.getResource();
		String resourceType = resource.getResourceType();

		if (config.isSupportedResourceType(resourceType, slingRequest)) {
			url.removeSelector(config.getIncludeSelector());

			RequestDispatcherOptions options = new RequestDispatcherOptions();
			RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(url.toString(), options);
			slingRequest.setAttribute(INCLUDED_ATTRIBUTE, Boolean.TRUE);
			dispatcher.include(slingRequest, response);
		} else {
			LOG.error("User tries to include " + resourceType
					+ " but it is not in the dynamic resource type list.");
		}
	}

	private String getUrl(SlingHttpServletRequest slingRequest, boolean synthetic) {
		boolean isSynthetic = synthetic;
		Resource resource = slingRequest.getResource();
		UrlManipulator url = new UrlManipulator(slingRequest, true);

		url.setDefaultExtension(config.getDefaultExtension());
		url.addSelector(config.getIncludeSelector());
		if (isSynthetic) {
			url.replaceSuffix(resource.getResourceType());
		}
		return url.toString();
	}

	private String getParentExistingResource(String syntheticResourcePath, ResourceResolver resolver) {
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
