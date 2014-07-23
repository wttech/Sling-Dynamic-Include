package com.cognifide.cq.includefilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.includefilter.type.NoCache;
import com.cognifide.cq.includefilter.type.ResourceTypesProvider;

/**
 * Include filter configuration.
 * 
 * @author tomasz.rekawek
 * 
 */
@Component(metatype = true, configurationFactory = true)
@Service(Configuration.class)
@Properties({
		@Property(name = Configuration.PROPERTY_FILTER_ENABLED, boolValue = Configuration.DEFAULT_FILTER_ENABLED, label = "Enabled", description = "Check to enable the filter"),
		@Property(name = Configuration.PROPERTY_FILTER_RESOURCE_TYPES, value = {
				"foundation/components/carousel", "foundation/components/userinfo" }, cardinality = Integer.MAX_VALUE, label = "Resource types", description = "Filter will replace components with selected resource types"),
		@Property(name = Configuration.PROPERTY_INCLUDE_TYPE, value = Configuration.DEFAULT_INCLUDE_TYPE, label = "Include type", description = "Type of generated include tags", options = {
				@PropertyOption(name = "SSI", value = "Apache SSI"),
				@PropertyOption(name = "ESI", value = "ESI"),
				@PropertyOption(name = "JSI", value = "Javascript") }),
		@Property(name = Configuration.PROPERTY_ADD_COMMENT, boolValue = Configuration.DEFAULT_ADD_COMMENT, label = "Add comment", description = "Add comment to included components"),
		@Property(name = Configuration.PROPERTY_FILTER_SELECTOR, value = Configuration.DEFAULT_FILTER_SELECTOR, label = "Filter selector", description = "Selector used to mark included resources") })
// @formatter:on
public class Configuration {

	private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

	static final String PROPERTY_FILTER_PATH = "include-filter.config.path";

	static final String DEFAULT_FILTER_PATH = "/content";

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

	@Reference(referenceInterface = ResourceTypesProvider.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private Set<SupportedResourceTypes> resourceTypeProviders = new CopyOnWriteArraySet<SupportedResourceTypes>();

	private boolean isEnabled;

	private String path;

	private String includeSelector;

	private String[] resourceTypes;

	private boolean addComment;

	private String includeTypeName;

	@Activate
	public void activate(Map<String, Object> properties) {
		isEnabled = PropertiesUtil.toBoolean(properties.get(PROPERTY_FILTER_ENABLED), DEFAULT_FILTER_ENABLED);
		path = PropertiesUtil.toString(properties.get(PROPERTY_FILTER_PATH), DEFAULT_FILTER_PATH);
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

		includeSelector = PropertiesUtil.toString(properties.get(PROPERTY_FILTER_SELECTOR),
				DEFAULT_FILTER_SELECTOR);
		addComment = PropertiesUtil.toBoolean(properties.get(PROPERTY_ADD_COMMENT), DEFAULT_ADD_COMMENT);
		includeTypeName = PropertiesUtil
				.toString(properties.get(PROPERTY_INCLUDE_TYPE), DEFAULT_INCLUDE_TYPE);
	}

	public String getPath() {
		return path;
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

	void bindResourceTypeProviders(ResourceTypesProvider provider) {
		LOG.info("bind new provider: " + provider.getClass().getName());
		resourceTypeProviders.add(new SupportedResourceTypes(provider));
		LOG.info("registered providers: " + resourceTypeProviders.size());
	}

	void unbindResourceTypeProviders(ResourceTypesProvider provider) {
		LOG.info("unbind provider: " + provider.getClass().getName());
		SupportedResourceTypes toRemove = null;
		for (SupportedResourceTypes type : resourceTypeProviders) {
			if (type.getProvider().equals(provider)) {
				toRemove = type;
				break;
			}
		}
		if (toRemove != null) {
			resourceTypeProviders.remove(toRemove);
		}
		LOG.info("registered providers: " + resourceTypeProviders.size());
	}

	static final class SupportedResourceTypes {
		private ResourceTypesProvider provider;

		private String[] cachedResourceTypes;

		private SupportedResourceTypes(ResourceTypesProvider provider) {
			this.provider = provider;
			if (!provider.getClass().isAnnotationPresent(NoCache.class)) {
				String[] providedTypes = provider.getResourceTypes();
				cachedResourceTypes = Arrays.copyOf(providedTypes, providedTypes.length);
			}
		}

		public String[] getResourceTypes() {
			if (cachedResourceTypes == null) {
				return provider.getResourceTypes();
			} else {
				return cachedResourceTypes;
			}
		}

		public ResourceTypesProvider getProvider() {
			return provider;
		}
	}
}
