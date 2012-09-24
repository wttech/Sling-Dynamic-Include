package com.cognifide.cq.includefilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

import com.cognifide.cq.includefilter.DynamicIncludeFilter.SupportedResourceTypes;

/**
 * Include filter configuration.
 * 
 * @author tomasz.rekawek
 * 
 */
public class Configuration {
	static final String PROPERTY_FILTER_ENABLED = "include-filter.config.enabled";

	static final boolean DEFAULT_FILTER_ENABLED = false;

	static final String PROPERTY_FILTER_RESOURCE_TYPES = "include-filter.config.resource-types";

	static final String PROPERTY_FILTER_SELECTOR = "include-filter.config.selector";

	static final String DEFAULT_FILTER_SELECTOR = "nocache";

	static final String PROPERTY_INCLUDE_TYPE = "include-filter.config.include-type";

	static final String DEFAULT_INCLUDE_TYPE = "SSI";

	static final String PROPERTY_DEFAULT_EXT = "include-filter.config.default-extension";

	static final String DEFAULT_DEFAULT_EXT = "html";

	static final String PROPERTY_ADD_COMMENT = "include-filter.config.add_comment";

	static final boolean DEFAULT_ADD_COMMENT = false;

	static final String[] DEFAULT_FILTER_RESOURCE_TYPES = new String[] { "foundation/components/carousel",
			"foundation/components/userinfo" };

	static final String PROPERTY_SKIP_WITH_PARAMS = "include-filter.config.skip-with-params";

	static final boolean DEFAULT_SKIP_WITH_PARAMS = true;

	static final String PROPERTY_ONLY_INCLUDED = "include-filter.config.only-included";

	static final boolean DEFAULT_ONLY_INCLUDED = true;
	
	private static final String RESOURCE_TYPES_ATTR = Configuration.class.getName() + ".resourceTypes";

	// Properties read from configuration
	private String includeSelector;

	private String[] resourceTypes;

	private String defaultExtension;

	private Boolean addComment;

	private String includeTypeName;

	private Boolean skipWithParams;

	private Boolean onlyIncluded;
	
	private Set<SupportedResourceTypes> resourceTypeProviders;

	Configuration(ComponentContext context, Set<SupportedResourceTypes> resourceTypeProviders) {
		resourceTypes = OsgiUtil.toStringArray(readProperty(context, PROPERTY_FILTER_RESOURCE_TYPES));
		if (resourceTypes == null) {
			resourceTypes = DEFAULT_FILTER_RESOURCE_TYPES;
		}
		String[] trimmedResourceTypes = new String[resourceTypes.length];
		for (int i = 0; i < resourceTypes.length; i++) {
			String[] s = resourceTypes[i].split(";");
			String name = s[0].trim();
			trimmedResourceTypes[i] = name;
		}
		resourceTypes = trimmedResourceTypes;
		this.resourceTypeProviders = resourceTypeProviders;

		includeSelector = OsgiUtil.toString(readProperty(context, PROPERTY_FILTER_SELECTOR),
				DEFAULT_FILTER_SELECTOR);
		defaultExtension = OsgiUtil
				.toString(readProperty(context, PROPERTY_DEFAULT_EXT), DEFAULT_DEFAULT_EXT);
		addComment = OsgiUtil.toBoolean(readProperty(context, PROPERTY_ADD_COMMENT), DEFAULT_ADD_COMMENT);
		includeTypeName = OsgiUtil.toString(readProperty(context, PROPERTY_INCLUDE_TYPE),
				DEFAULT_INCLUDE_TYPE);
		skipWithParams = OsgiUtil.toBoolean(readProperty(context, PROPERTY_SKIP_WITH_PARAMS),
				DEFAULT_SKIP_WITH_PARAMS);
		onlyIncluded = OsgiUtil.toBoolean(readProperty(context, PROPERTY_ONLY_INCLUDED),
				DEFAULT_ONLY_INCLUDED);
	}

	public String getIncludeSelector() {
		return includeSelector;
	}

	public boolean isSupportedResourceType(String type, SlingHttpServletRequest request) {
		@SuppressWarnings("unchecked")
		List<String> cachedTypes = (List<String>) request.getAttribute(RESOURCE_TYPES_ATTR);
		
		if(cachedTypes == null) {
			cachedTypes = new ArrayList<String>();
			cachedTypes.addAll(Arrays.asList(resourceTypes));
			for(SupportedResourceTypes provider : resourceTypeProviders) {
				String[] providedResourceTypes = provider.getResourceTypes();
				if(!ArrayUtils.isEmpty(providedResourceTypes)) {
					cachedTypes.addAll(Arrays.asList(providedResourceTypes));
				}
			}
			request.setAttribute(RESOURCE_TYPES_ATTR, cachedTypes);
		}
		
		return cachedTypes.contains(type);
	}
	
	public String getDefaultExtension() {
		return defaultExtension;
	}

	public Boolean getAddComment() {
		return addComment;
	}

	static boolean isFilterEnabled(ComponentContext context) {
		return OsgiUtil.toBoolean(readProperty(context, PROPERTY_FILTER_ENABLED), DEFAULT_FILTER_ENABLED);
	}

	private static Object readProperty(ComponentContext context, String name) {
		return context.getProperties().get(name);
	}

	public String getIncludeTypeName() {
		return includeTypeName;
	}

	public Boolean getSkipWithParams() {
		return skipWithParams;
	}

	public Boolean getOnlyIncluded() {
		return onlyIncluded;
	}
}
