package com.cognifide.cq.includefilter.types;

//import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
//import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//@Component(immediate=true, metatype=true)
//@Service
public class ExampleCachedResourceTypeProvider implements ResourceTypesProvider {
	private static final Logger LOG = LoggerFactory.getLogger(ExampleCachedResourceTypeProvider.class);
	
	@Property(label="Resource type list", value={"a", "b"})
	private static final String TYPE_LIST = "typesList";

	private static final String[] DEFAULT_TYPE_LIST = new String[] {"a", "b"};
	
	private String[] types;
	
	protected void activate(ComponentContext context) {
		String[] configTypes = OsgiUtil.toStringArray(readProperty(context, TYPE_LIST));
		if(configTypes == null) {
			configTypes = DEFAULT_TYPE_LIST;
		}
		this.types = configTypes;
	}
	
	protected void deactivate(ComponentContext context) {
		types = null;
	}
	
	@Override
	public String[] getResourceTypes() {
		LOG.info("Getting resource types");
		return types;
	}
	
	private static Object readProperty(ComponentContext context, String name) {
		return context.getProperties().get(name);
	}
}
