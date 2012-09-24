package com.cognifide.cq.includefilter.generators;

/**
 * Include generator interface
 * 
 * @author tomasz.rekawek
 * 
 */
public interface IncludeGenerator {
	String getType();
	
	String getInclude(String url);
}
