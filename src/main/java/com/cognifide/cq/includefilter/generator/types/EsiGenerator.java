package com.cognifide.cq.includefilter.generator.types;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.cq.includefilter.generator.IncludeGenerator;

/**
 * ESI include generator
 * 
 * @author tomasz.rekawek
 * 
 */
@Component(immediate=true)
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
		buf.append(url);
		buf.append("\"/>");
		return buf.toString();
	}
}
