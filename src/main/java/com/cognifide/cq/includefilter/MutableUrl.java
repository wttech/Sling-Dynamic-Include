package com.cognifide.cq.includefilter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;

/**
 * Class providing useful methods for URL manipulation (add/remove selector, set default extension, change
 * suffix, etc.)
 * 
 * @author tomasz.rekawek
 * 
 */
public class MutableUrl {
	private final RequestPathInfo originalPathInfo;

	private List<String> selectorsToRemove;

	private List<String> selectorsToAdd;

	private String replaceSuffix;

	private String replaceExt;

	private String replacePath;

	private String defaultExt;
	
	private boolean escapeNamespace;

	public MutableUrl(SlingHttpServletRequest request, boolean escapeNamespace) {
		originalPathInfo = request.getRequestPathInfo();
		selectorsToAdd = new ArrayList<String>();
		selectorsToRemove = new ArrayList<String>();
		this.escapeNamespace = escapeNamespace;
	}

	public void addSelector(String selector) {
		selectorsToAdd.add(selector);
		selectorsToRemove.remove(selector);
	}

	public void removeSelector(String selector) {
		selectorsToRemove.add(selector);
		selectorsToAdd.remove(selector);
	}

	public void replacePath(String path) {
		replacePath = path;
	}

	public void replaceSuffix(String suffix) {
		replaceSuffix = suffix;
	}

	public void setDefaultExtension(String extension) {
		defaultExt = extension;
	}

	public void replaceExtension(String extension) {
		replaceExt = extension;
	}

	public String getPath() {
		String resPath;

		if (replacePath != null) {
			resPath = replacePath;
		} else {
			resPath = originalPathInfo.getResourcePath();
		}

		// According to
		// http://sling.apache.org/apidocs/sling5/org/apache/sling/api/request/RequestPathInfo.html
		// it shouldn't contain dot or slash. Unfortunately, sometimes contains - especially if the resource
		// doesn't exist.
		if (resPath.contains(".")) {
			resPath = resPath.substring(0, resPath.indexOf('.'));
		}
		return resPath;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getPath());
		buildSelectors(buf);

		if (replaceExt != null) {
			if (!replaceExt.isEmpty()) {
				buf.append('.');
				buf.append(replaceExt);
			}
		} else if (originalPathInfo.getExtension() == null && defaultExt != null) {
			buf.append('.');
			buf.append(defaultExt);
		} else if (originalPathInfo.getExtension() != null) {
			buf.append('.');
			buf.append(originalPathInfo.getExtension());
		}

		if (replaceSuffix != null) {
			if (!replaceSuffix.isEmpty()) {
				buf.append('/');
				buf.append(sanitize(replaceSuffix));
			}
		} else if (originalPathInfo.getSuffix() != null) {
			buf.append(sanitize(originalPathInfo.getSuffix()));
		}

		String url = buf.toString();
		if(escapeNamespace) {
			url = url.replaceAll("(\\w+):(\\w+)", "_$1_$2");
		}
		return url;
	}

	private void buildSelectors(StringBuffer buf) {
		String[] selectors = originalPathInfo.getSelectors();
		for (String sel : selectors) {
			if (!selectorsToRemove.contains(sel) && !selectorsToAdd.contains(sel)) {
				buf.append('.');
				buf.append(sanitize(sel));
			}
		}
		for (String sel : selectorsToAdd) {
			buf.append('.');
			buf.append(sanitize(sel));
		}
	}

	private static String sanitize(String dirtyString) {
		if (StringUtils.isBlank(dirtyString)) {
			return "";
		} else {
			return dirtyString.replaceAll("[^0-9a-zA-Z:.\\-/_=]", "");
		}
	}
}
