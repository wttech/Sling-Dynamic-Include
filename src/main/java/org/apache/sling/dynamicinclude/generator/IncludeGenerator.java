package org.apache.sling.dynamicinclude.generator;

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
