package com.cognifide.cq.includefilter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;

/**
 * Include filter configuration.
 * 
 * @author tomasz.rekawek
 * 
 */
@Component(metatype = true, configurationFactory = true, label = "SDI Configuration", immediate = true, policy = ConfigurationPolicy.REQUIRE)
@Service(Configuration.class)
@Properties({
		@Property(name = Configuration.PROPERTY_FILTER_ENABLED, boolValue = Configuration.DEFAULT_FILTER_ENABLED, label = "Enabled", description = "Check to enable the filter"),
		@Property(name = Configuration.PROPERTY_FILTER_PATH, value = Configuration.DEFAULT_FILTER_PATH, label = "Base path", description = "This SDI configuration will work only for this path"),
		@Property(name = Configuration.PROPERTY_FILTER_RESOURCE_TYPES, value = {
				"foundation/components/carousel", "foundation/components/userinfo" }, cardinality = Integer.MAX_VALUE, label = "Resource types", description = "Filter will replace components with selected resource types"),
		@Property(name = Configuration.PROPERTY_INCLUDE_TYPE, value = Configuration.DEFAULT_INCLUDE_TYPE, label = "Include type", description = "Type of generated include tags", options = {
				@PropertyOption(name = "SSI", value = "Apache SSI"),
				@PropertyOption(name = "ESI", value = "ESI"),
				@PropertyOption(name = "JSI", value = "Javascript") }),
		@Property(name = Configuration.PROPERTY_ADD_COMMENT, boolValue = Configuration.DEFAULT_ADD_COMMENT, label = "Add comment", description = "Add comment to included components"),
		@Property(name = Configuration.PROPERTY_FILTER_SELECTOR, value = Configuration.DEFAULT_FILTER_SELECTOR, label = "Filter selector", description = "Selector used to mark included resources"),
		@Property(name = Configuration.PROPERTY_REQUIRED_HEADER, value = Configuration.DEFAULT_REQUIRED_HEADER, label = "Required header", description = "SDI will work only for requests with given header")
})
public class Configuration {

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

	static final String PROPERTY_REQUIRED_HEADER = "include-filter.config.required_header";

	static final String DEFAULT_REQUIRED_HEADER = "Server-Agent=Communique-Dispatcher";

	private boolean isEnabled;

	private String path;

	private String includeSelector;

	private List<String> resourceTypes;

	private boolean addComment;

	private String includeTypeName;

	private String requiredHeader;

	@Activate
	public void activate(ComponentContext context, Map<String, ?> properties) {
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
		this.resourceTypes = Arrays.asList(resourceTypeList);

		includeSelector = PropertiesUtil.toString(properties.get(PROPERTY_FILTER_SELECTOR),
				DEFAULT_FILTER_SELECTOR);
		addComment = PropertiesUtil.toBoolean(properties.get(PROPERTY_ADD_COMMENT), DEFAULT_ADD_COMMENT);
		includeTypeName = PropertiesUtil
				.toString(properties.get(PROPERTY_INCLUDE_TYPE), DEFAULT_INCLUDE_TYPE);
		requiredHeader = PropertiesUtil.toString(properties.get(PROPERTY_REQUIRED_HEADER),
				DEFAULT_REQUIRED_HEADER);
	}

	public String getBasePath() {
		return path;
	}

	public boolean hasIncludeSelector(SlingHttpServletRequest request) {
		return ArrayUtils.contains(request.getRequestPathInfo().getSelectors(), includeSelector);
	}

	public String getIncludeSelector() {
		return includeSelector;
	}

	public boolean isSupportedResourceType(String resourceType) {
		return StringUtils.isNotBlank(resourceType) && resourceTypes.contains(resourceType);
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

	public String getRequiredHeader() {
		return requiredHeader;
	}
}
