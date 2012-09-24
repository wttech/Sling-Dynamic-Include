package com.cognifide.cq.includefilter;

import java.util.Enumeration;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;

/**
 * List of states in which the processed resource can be.
 * 
 * @author tomasz.rekawek
 * 
 */
public enum ResourceState {
	STATIC {
		@Override
		boolean isStateMatch(SlingHttpServletRequest slingRequest, UrlManipulator url, Configuration config) {
			return true;
		}
	},
	DYNAMIC {
		@Override
		boolean isStateMatch(SlingHttpServletRequest slingRequest, UrlManipulator url, Configuration config) {
			Object servletPath = slingRequest.getAttribute("javax.servlet.include.servlet_path");

			@SuppressWarnings("rawtypes")
			Enumeration params = slingRequest.getParameterNames();
			if (config.getSkipWithParams() && params.hasMoreElements()) {
				return false;
			}
			if (config.getOnlyIncluded() && servletPath == null) {
				return false;
			}
			String resourceType = slingRequest.getResource().getResourceType();
			return config.isSupportedResourceType(resourceType, slingRequest);
		}
	},
	DYNAMIC_AND_SYNTHETIC {
		@Override
		boolean isStateMatch(SlingHttpServletRequest slingRequest, UrlManipulator url, Configuration config) {
			Resource res = slingRequest.getResource();
			return ResourceState.DYNAMIC.isStateMatch(slingRequest, url, config)
					&& ResourceUtil.isSyntheticResource(res);
		}
	},
	WITH_NOCACHE_SELECTOR {
		@Override
		boolean isStateMatch(SlingHttpServletRequest slingRequest, UrlManipulator url, Configuration config) {
			return url.hasSelector(config.getIncludeSelector());
		}
	},
	WITH_NOCACHE_AND_VALID_SUFFIX {
		@Override
		boolean isStateMatch(SlingHttpServletRequest slingRequest, UrlManipulator url, Configuration config) {
			String resourceType = url.getResourceTypeFromSuffix();
			return resourceType != null && url.hasSelector(config.getIncludeSelector())
					&& config.isSupportedResourceType(resourceType, slingRequest);
		}
	},
	WITH_INCLUDED_ATTRIBUTE {
		@Override
		boolean isStateMatch(SlingHttpServletRequest slingRequest, UrlManipulator url, Configuration config) {
			return Boolean.TRUE.equals(slingRequest.getAttribute(ActionPerformer.INCLUDED_ATTRIBUTE));
		}
	};

	abstract boolean isStateMatch(SlingHttpServletRequest slingRequest, UrlManipulator url,
			Configuration config);

	public static ResourceState getState(SlingHttpServletRequest slingRequest, UrlManipulator url,
			Configuration config) {
		ResourceState state = STATIC;
		for (ResourceState s : ResourceState.values()) {
			if (s.isStateMatch(slingRequest, url, config)) {
				state = s;
			}
		}
		return state;
	}
}
