package com.cognifide.cq.includefilter;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

/**
 * Experimental implementation for synthetic resource include
 * 
 * @author miroslaw.stawniak
 *
 */
//@SlingServlet(resourceTypes = "sling:nonexisting", methods="GET")
public class SyntheticResourceIncludingServlet extends SlingSafeMethodsServlet implements OptingServlet {

	private static final long serialVersionUID = 1L;
	
	@Reference
	private ConfigurationWhiteboard configurationWhiteboard;
	
	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		
		final String resourceType = getResourceTypeFromSuffix(request);
		
		final RequestDispatcherOptions options = new RequestDispatcherOptions();
		options.setForceResourceType(resourceType);
		final RequestDispatcher dispatcher = request.getRequestDispatcher(request.getResource(),
				options);
		dispatcher.forward(request, response);
	}

	@Override
	public boolean accepts(SlingHttpServletRequest request) {
		final String resourceType = getResourceTypeFromSuffix(request);
		final Configuration config = configurationWhiteboard.getConfiguration(request, resourceType);

		return config != null
				&& config.hasIncludeSelector(request);
	}

	private static String getResourceTypeFromSuffix(SlingHttpServletRequest request) {
		final String suffix = request.getRequestPathInfo().getSuffix();
		return StringUtils.removeStart(suffix, "/");
	}
}
