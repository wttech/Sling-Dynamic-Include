package com.cognifide.cq.includefilter.generator;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;

/**
 * Service that provides include generator of given type.
 * 
 * @author tomasz.rekawek
 */

@Component(immediate = true)
@Service
public class IncludeGeneratorWhiteboard implements IncludeGeneratorFactory {
	@Reference(referenceInterface = IncludeGenerator.class, cardinality = ReferenceCardinality.MANDATORY_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private Set<IncludeGenerator> generators = new CopyOnWriteArraySet<IncludeGenerator>();

	@Override
	public IncludeGenerator getGenerator(String type) {
		for (IncludeGenerator generator : generators) {
			if (type.equals(generator.getType())) {
				return generator;
			}
		}
		return null;
	}

	void bindGenerators(IncludeGenerator generator) {
		generators.add(generator);
	}

	void unbindGenerators(IncludeGenerator generator) {
		generators.remove(generator);
	}

}
