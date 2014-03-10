package com.cognifide.cq.includefilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.PropertiesUtil;

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

	private final boolean isEnabled;

	private final String includeSelector;

	private final String[] resourceTypes;

	private final String defaultExtension;

	private final boolean addComment;

	private final String includeTypeName;

	private final boolean skipWithParams;

	private final boolean onlyIncluded;

	private final Set<SupportedResourceTypes> resourceTypeProviders;

	Configuration(Map<String, Object> properties, Set<SupportedResourceTypes> resourceTypeProviders) {
		isEnabled = PropertiesUtil.toBoolean(properties.get(PROPERTY_FILTER_ENABLED), DEFAULT_FILTER_ENABLED);
		String[] resourceTypeList;
		resourceTypeList = PropertiesUtil.toStringArray(properties.get(PROPERTY_FILTER_RESOURCE_TYPES));
		if (resourceTypeList == null) {
			resourceTypeList = DEFAULT_FILTER_RESOURCE_TYPES;
		}
		for (int i = 0; i < resourceTypeList.length; i++) {
			String[] s = resourceTypeList[i].split(";");
			String name = s[0].trim();
			resourceTypeList[i] = name;
		}
		this.resourceTypes = resourceTypeList;
		this.resourceTypeProviders = resourceTypeProviders;

		includeSelector = PropertiesUtil.toString(PROPERTY_FILTER_SELECTOR, DEFAULT_FILTER_SELECTOR);
		defaultExtension = PropertiesUtil.toString(PROPERTY_DEFAULT_EXT, DEFAULT_DEFAULT_EXT);
		addComment = PropertiesUtil.toBoolean(PROPERTY_ADD_COMMENT, DEFAULT_ADD_COMMENT);
		includeTypeName = PropertiesUtil.toString(PROPERTY_INCLUDE_TYPE, DEFAULT_INCLUDE_TYPE);
		skipWithParams = PropertiesUtil.toBoolean(PROPERTY_SKIP_WITH_PARAMS, DEFAULT_SKIP_WITH_PARAMS);
		onlyIncluded = PropertiesUtil.toBoolean(PROPERTY_ONLY_INCLUDED, DEFAULT_ONLY_INCLUDED);
	}

	public boolean hasIncludeSelector(SlingHttpServletRequest request) {
		return ArrayUtils.contains(request.getRequestPathInfo().getSelectors(), includeSelector);
	}

	public String getIncludeSelector() {
		return includeSelector;
	}

	public boolean isSupportedResourceType(String type, SlingHttpServletRequest request) {
		@SuppressWarnings("unchecked")
		List<String> cachedTypes = (List<String>) request.getAttribute(RESOURCE_TYPES_ATTR);

		if (cachedTypes == null) {
			cachedTypes = new ArrayList<String>();
			cachedTypes.addAll(Arrays.asList(resourceTypes));
			for (SupportedResourceTypes provider : resourceTypeProviders) {
				String[] providedResourceTypes = provider.getResourceTypes();
				if (!ArrayUtils.isEmpty(providedResourceTypes)) {
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

	public boolean getAddComment() {
		return addComment;
	}

	public String getIncludeTypeName() {
		return includeTypeName;
	}

	public boolean getSkipWithParams() {
		return skipWithParams;
	}

	public boolean getOnlyIncluded() {
		return onlyIncluded;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
}
