package com.openexchange.scripting.rhino.require;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


public class DependencyResolver {

	private DependencyResolver resolver = null;
	private JSBundle bundle;
	private ConcurrentHashMap<String, Object> registeredModules = new ConcurrentHashMap<String, Object>();
	
	public DependencyResolver(JSBundle bundle) {
		this(null, bundle);
	}
	
	public DependencyResolver(DependencyResolver resolver, JSBundle bundle) {
		super();
		this.resolver = resolver;
		this.bundle = bundle;
	}
	
	public boolean knows(String id) {
		// TODO: Check import/export declarations
		if (registeredModules.containsKey(id)) {
			return true;
		}
		
		if (registeredModules.containsKey(bundle.getSymbolicName()+"/"+id)) {
			return true;
		}
		
		if (bundle.getEntry(id+".js") != null) {
			return true;
		}
		
		if (id.startsWith(bundle.getSymbolicName()+"/") && bundle.getEntry(id.substring(bundle.getSymbolicName().length()+1)+".js") != null) {
			return true;
		}
		
		return false;
	}

	public Object get(String id, Context cx, Scriptable scope) {
		return get(id, cx, scope, true);
	}
	
	public Object get(String id, Context cx, Scriptable scope, boolean executeScript) {
		// TODO: Check import/export declarations
		// Firstly try the registered modules
		Object object = registeredModules.get(id);
		if (object != null) {
			return object;
		}
		
		object = registeredModules.get(bundle.getSymbolicName()+"/"+id);
		if (object != null) {
			return object;
		}
		
		// Secondly try to load locally
		URL entry = bundle.getEntry(id+".js");
		if (entry == null && id.startsWith(bundle.getSymbolicName()+"/")) {
			entry = bundle.getEntry(id.substring(bundle.getSymbolicName().length()+1)+".js");
		}
		if (entry != null && executeScript) {
			Reader in = null;
			try {
				in = new InputStreamReader(entry.openStream(), "UTF-8");
				Scriptable subScope = cx.newObject(scope);
				subScope.setPrototype(scope);
				subScope.setParentScope(null);
				subScope.put("define", subScope, new OXDefine(id, this));
				subScope.put("require", subScope, new OXRequire(this));
				
				cx.evaluateReader(subScope, in, entry.toString(), 1, null);
				return get(id, cx, scope, false);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// IGNORE
					}
				}
			}
		} 
		
		if (resolver != null) {
			return resolver.get(id, cx, scope);
		}
		
		for (DependencyResolver other : RequireSupport.bundleResolvers.values()) {
			if (other.knows(id)) {
				return other.get(id, cx, scope);
			}
		}
		
		// Hm. Maybe this package will turn up eventually
		return new DeferredResolution(id, scope);
	}
	

	public void remember(String id, Object object) {
		registeredModules.put(id, object);
	}

	public JSBundle getBundle() {
		return bundle;
	}
	

}