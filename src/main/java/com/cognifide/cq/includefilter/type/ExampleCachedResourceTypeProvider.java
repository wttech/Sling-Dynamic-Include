package com.cognifide.cq.includefilter.type;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Component(immediate=true, metatype=true)
// @Service
public class ExampleCachedResourceTypeProvider implements ResourceTypesProvider {
	private static final Logger LOG = LoggerFactory.getLogger(ExampleCachedResourceTypeProvider.class);

	@Property(label = "Resource type list", value = { "a", "b" })
	private static final String TYPE_LIST = "typesList";

	private static final String[] DEFAULT_TYPE_LIST = new String[] { "a", "b" };

	private String[] types;

	@Activate
	protected void activate(Map<String, Object> properties) {
		String[] configTypes = PropertiesUtil.toStringArray(properties.get(TYPE_LIST));
		if (configTypes == null) {
			configTypes = DEFAULT_TYPE_LIST;
		}
		this.types = configTypes;
	}

	@Override
	public String[] getResourceTypes() {
		LOG.info("Getting resource types");
		return types;
	}
}
