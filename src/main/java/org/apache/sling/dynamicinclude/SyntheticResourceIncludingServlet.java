/*-
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */ 

package org.apache.sling.dynamicinclude;

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
