package org.apache.sling.dynamicinclude;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;

@Component
@Service(ConfigurationWhiteboard.class)
public class ConfigurationWhiteboard {

	@Reference(referenceInterface = Configuration.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private Set<Configuration> configs = new CopyOnWriteArraySet<Configuration>();

	public Configuration getConfiguration(SlingHttpServletRequest request, String resourceType) {
		for (Configuration c : configs) {
			if (isEnabled(c, request) && c.isSupportedResourceType(resourceType)) {
				return c;
			}
		}
		return null;
	}

	private boolean isEnabled(Configuration config, SlingHttpServletRequest request) {
		final String requestPath = request.getRequestPathInfo().getResourcePath();
		return config.isEnabled() && StringUtils.startsWith(requestPath, config.getBasePath());
	}

	protected void bindConfigs(final Configuration config) {
		configs.add(config);
	}

	protected void unbindConfigs(final Configuration config) {
		configs.remove(config);
	}
}
