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

	static final String[] DEFAULT_FILTER_RESOURCE_TYPES = new String[] { "foundation/components/carousel",
			"foundation/components/userinfo" };

	static final String PROPERTY_FILTER_SELECTOR = "include-filter.config.selector";

	static final String DEFAULT_FILTER_SELECTOR = "nocache";

	static final String PROPERTY_INCLUDE_TYPE = "include-filter.config.include-type";

	static final String DEFAULT_INCLUDE_TYPE = "SSI";

	static final String PROPERTY_ADD_COMMENT = "include-filter.config.add_comment";

	static final boolean DEFAULT_ADD_COMMENT = false;

	private static final String RESOURCE_TYPES_ATTR = Configuration.class.getName() + ".resourceTypes";

	private final boolean isEnabled;

	private final String includeSelector;

	private final String[] resourceTypes;

	private final boolean addComment;

	private final String includeTypeName;

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

		includeSelector = PropertiesUtil.toString(properties.get(PROPERTY_FILTER_SELECTOR),
				DEFAULT_FILTER_SELECTOR);
		addComment = PropertiesUtil.toBoolean(properties.get(PROPERTY_ADD_COMMENT), DEFAULT_ADD_COMMENT);
		includeTypeName = PropertiesUtil
				.toString(properties.get(PROPERTY_INCLUDE_TYPE), DEFAULT_INCLUDE_TYPE);
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

	public boolean getAddComment() {
		return addComment;
	}

	public String getIncludeTypeName() {
		return includeTypeName;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
}
