package com.openexchange.scripting.rhino.require;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

public class OXRequire extends ScriptableObject implements Function {
	private DependencyResolver resolver;
	
	public OXRequire(DependencyResolver resolver) {
		super();
		this.resolver = resolver;
	}

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj,
			Object[] args) {
		String[] dependencies = null;
		Function fun = null;
		for (Object arg : args) {
			if (arg instanceof Wrapper) {
				arg = ((Wrapper)arg).unwrap();
			}
			if (arg instanceof String[]) {
				if (dependencies == null) {
					dependencies = (String[]) arg;
				} else {
					throw new IllegalArgumentException("Invalid call to 'require'");
				}
			}
			
			if (arg instanceof Function) {
				if (fun == null) {
					fun = (Function) arg;
				} else {
					throw new IllegalArgumentException("Invalid call to 'require'");
				}
			}
		}
		
		if (fun == null) {
			throw new IllegalArgumentException("Invalid call to 'require'");
		}
		
		if (dependencies == null) {
			dependencies = new String[]{"require", "exports", "module"};
			
		}
		Object[] resolved = new Object[dependencies.length];
		for (int i = 0; i < dependencies.length; i++) {
			resolved[i] = resolver.get(dependencies[i], cx, scope);
		}
		DeferredResolution.awaitResolution(cx, scope, thisObj, resolved, fun, null);
		
		return null;
	}

	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		return (Scriptable) call(cx, scope, null, args);
	}

	@Override
	public String getClassName() {
		return "OXRequire";
	}
	
}