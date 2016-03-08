package org.apache.sling.dynamicinclude.generator.types;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.dynamicinclude.generator.IncludeGenerator;

/**
 * ESI include generator
 * 
 * @author tomasz.rekawek
 * 
 */
@Component
@Service
public class EsiGenerator implements IncludeGenerator {
	private static final String GENERATOR_NAME = "ESI";
	
	@Override
	public String getType() {
		return GENERATOR_NAME;
	}
	
	@Override
	public String getInclude(String url) {
		StringBuffer buf = new StringBuffer();
		buf.append("<esi:include src=\"");
		buf.append(StringEscapeUtils.escapeHtml(url));
		buf.append("\"/>");
		return buf.toString();
	}
}
