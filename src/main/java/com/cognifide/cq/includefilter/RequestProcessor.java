package com.cognifide.cq.includefilter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

public interface RequestProcessor {
	boolean accepts(SlingHttpServletRequest request);

	void process(SlingHttpServletRequest request, SlingHttpServletResponse response, FilterChain chain) throws IOException, ServletException;
}