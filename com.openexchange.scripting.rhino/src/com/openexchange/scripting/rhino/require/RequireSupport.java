package com.openexchange.scripting.rhino.require;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * We'll implement the AMD spec here as best we can, with a few extensions accounting for us living in OSGi
 * @author francisco.laguna@open-xchange.com
 *
 */
public class RequireSupport {
	
	static final ConcurrentHashMap<String, DependencyResolver> bundleResolvers = new ConcurrentHashMap<String, DependencyResolver>();
	
	public static final List<ResolveEnhancement> resolveEnhancements = new ArrayList<ResolveEnhancement>();
	
	public static void initialize(final Scriptable serviceScope, Context cx,
			JSBundle bundle, Map<String, Object> additionalModules) {
		
		DependencyResolver resolver = new DependencyResolver(bundle);
		bundleResolvers.put(bundle.getSymbolicName(), resolver);
		
		serviceScope.put("define", serviceScope, new OXDefine(bundle.getSymbolicName()+"/main", resolver));
		
		OXRequire oxRequire = new OXRequire(resolver);
		serviceScope.put("require", serviceScope, oxRequire);
		resolver.remember("require", oxRequire);
	
		JSConverter converter = new JSConverter() {
			
			@Override
			public Object toJS(Object o) {
				return Context.javaToJS(o, serviceScope);
			}
		};
		
		for(ResolveEnhancement enhancement : resolveEnhancements) {
			enhancement.enhance(resolver, converter);
		}
		
		for(Map.Entry<String, Object> additional : additionalModules.entrySet()) {
			resolver.remember(additional.getKey(), Context.javaToJS(additional.getValue(), serviceScope));
		}
		
		DeferredResolution.resolve(bundle, resolver);
	}

}
