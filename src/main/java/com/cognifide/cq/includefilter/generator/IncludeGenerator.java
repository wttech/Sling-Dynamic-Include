package com.cognifide.cq.includefilter.generator;

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
