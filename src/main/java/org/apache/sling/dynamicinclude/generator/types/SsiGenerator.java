package com.cognifide.cq.includefilter.generator.types;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.cq.includefilter.generator.IncludeGenerator;

/**
 * Apache SSI include generator
 * 
 * @author tomasz.rekawek
 * 
 */
@Component
@Service
public class SsiGenerator implements IncludeGenerator {
	private static final String GENERATOR_NAME = "SSI";
	
	@Override
	public String getType() {
		return GENERATOR_NAME;
	}
	
	@Override
	public String getInclude(String url) {
		StringBuffer buf = new StringBuffer();
		buf.append("<!--#include virtual=\"");
		buf.append(escapeForApache(url));
		buf.append("\" -->");
		return buf.toString();
	}

	/**
	 * Escapes $ to \$
	 * 
	 * @param url url to escape
	 * @return escaped url
	 */
	private Object escapeForApache(String url) {
		return url.replace("$", "\\$");
	}
}
