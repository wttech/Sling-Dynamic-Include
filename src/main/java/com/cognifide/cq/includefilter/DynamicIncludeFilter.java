package com.cognifide.cq.includefilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import com.cognifide.cq.includefilter.generator.IncludeGeneratorFactory;
import com.cognifide.cq.includefilter.processor.IncludeTagWritingProcessor;
import com.cognifide.cq.includefilter.processor.RequestPassingProcessor;
import com.cognifide.cq.includefilter.processor.SyntheticResourceIncludingProcessor;
import com.cognifide.cq.includefilter.processor.ResourceIncludingProcessor;
import com.cognifide.cq.includefilter.type.ResourceTypesProvider;

/**
 * Dynamic Include Filter (based on Sling Caching Filter)
 * 
 * @author tomasz.rekawek
 */

//@formatter:off
@SlingFilter(scope = {SlingFilterScope.REQUEST, SlingFilterScope.INCLUDE}, order = 0)
public class DynamicIncludeFilter implements Filter {

	@Reference(referenceInterface = ResourceTypesProvider.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private Set<Configuration> configs = new CopyOnWriteArraySet<Configuration>();

	@Reference
	private IncludeGeneratorFactory generatorFactory;

	@Override
	public void init(FilterConfig filterConfig) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof SlingHttpServletRequest && response instanceof SlingHttpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}
		final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
		final SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
		final String path = slingRequest.getRequestPathInfo().getResourcePath();
		for (Configuration c : configs) {
			if (c.isEnabled() && path.startsWith(c.getPath())) {
				process(c, slingRequest, slingResponse, chain);
				return;
			}
		}
		chain.doFilter(request, response);
	}
	
	private void process(Configuration config, SlingHttpServletRequest slingRequest,SlingHttpServletResponse slingResponse, FilterChain chain ) throws IOException, ServletException {
		List<RequestProcessor> processors = new ArrayList<RequestProcessor>();
		processors.add(new RequestPassingProcessor());
		processors.add(new SyntheticResourceIncludingProcessor(config));
		processors.add(new ResourceIncludingProcessor(config));
		processors.add(new IncludeTagWritingProcessor(config, generatorFactory));
		for (RequestProcessor p : processors) {
			if (p.accepts(slingRequest)) {
				p.process(slingRequest, slingResponse, chain);
				return;
			}
		}
	}
	
	protected void bindConfigs(final Configuration config) {
		configs.add(config);
	}

	protected void unbindConfigs(final Configuration config) {
		configs.remove(config);
	}
}
