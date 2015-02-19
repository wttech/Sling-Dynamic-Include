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

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.engine.EngineConstants;

import com.cognifide.cq.includefilter.generator.IncludeGeneratorFactory;
import com.cognifide.cq.includefilter.processor.IncludeTagWritingProcessor;
import com.cognifide.cq.includefilter.processor.RequestPassingProcessor;
import com.cognifide.cq.includefilter.processor.SyntheticResourceIncludingProcessor;
import com.cognifide.cq.includefilter.processor.ResourceIncludingProcessor;

/**
 * Dynamic Include Filter (based on Sling Caching Filter)
 * 
 * @author tomasz.rekawek
 */

//@formatter:off
@Component
@Service
@Properties({
	@Property(
		name = EngineConstants.SLING_FILTER_SCOPE,
		value = {EngineConstants.FILTER_SCOPE_REQUEST, EngineConstants.FILTER_SCOPE_INCLUDE },
		propertyPrivate = true)})
//@formatter:on
public class DynamicIncludeFilter implements Filter {

	@Reference(referenceInterface = Configuration.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
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

		for (Configuration c : configs) {
			if (isEnabled(c, slingRequest)) {
				if (process(c, slingRequest, slingResponse, chain)) {
					return;
				}
			}
		}
		chain.doFilter(request, response);
	}

	private boolean isEnabled(Configuration config, SlingHttpServletRequest request) {
		final String requestPath = request.getRequestPathInfo().getResourcePath();
		return config.isEnabled() && StringUtils.startsWith(requestPath, config.getBasePath());
	}

	private boolean process(Configuration config, SlingHttpServletRequest slingRequest,
			SlingHttpServletResponse slingResponse, FilterChain chain) throws IOException, ServletException {
		List<RequestProcessor> processors = new ArrayList<RequestProcessor>();
		processors.add(new RequestPassingProcessor());
		processors.add(new SyntheticResourceIncludingProcessor(config));
		processors.add(new ResourceIncludingProcessor(config));
		processors.add(new IncludeTagWritingProcessor(config, generatorFactory));
		for (RequestProcessor p : processors) {
			if (p.accepts(slingRequest)) {
				p.process(slingRequest, slingResponse, chain);
				return true;
			}
		}
		return false;
	}

	protected void bindConfigs(final Configuration config) {
		configs.add(config);
	}

	protected void unbindConfigs(final Configuration config) {
		configs.remove(config);
	}
}
