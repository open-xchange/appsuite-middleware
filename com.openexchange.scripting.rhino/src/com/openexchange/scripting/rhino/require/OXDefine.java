package com.openexchange.scripting.rhino.require;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;


public class OXDefine extends ScriptableObject implements Function {

	private final DependencyResolver resolver;
	private final String defaultModuleName;

	public OXDefine(String defaultModuleName, DependencyResolver resolver) {
		this.defaultModuleName = defaultModuleName;
		this.resolver = resolver;
	}

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj,
			Object[] args) {
		String id = null;
		String[] dependencies = null;
		Function factory = null;
		for (Object arg : args) {
			if (arg instanceof Wrapper) {
				arg = ((Wrapper)arg).unwrap();
			}
			if (arg instanceof String) {
				if (id == null) {
					id = (String) arg;
				} else {
					throw new IllegalArgumentException("Invalid call to 'define'");
				}
			}
			if (arg instanceof NativeArray) {
				if (dependencies == null) {
					NativeArray deps = (NativeArray) arg;
					dependencies = new String[(int)deps.getLength()];
					for(int i = 0; i < dependencies.length; i++) {
						dependencies[i] = deps.get(i).toString();
					}
				} else {
					throw new IllegalArgumentException("Invalid call to 'define'");
				}
			}

			if (arg instanceof Function) {
				if (factory == null) {
					factory = (Function) arg;
				} else {
					throw new IllegalArgumentException("Invalid call to 'define'");
				}
			}
		}

		if (factory == null) {
			throw new IllegalArgumentException("Invalid call to 'define'");
		}

		if (id == null) {
			id = defaultModuleName;
		}
		if (dependencies == null) {
			dependencies = new String[]{"require"}; // TODO: exports, module

		}
		Object[] resolved = new Object[dependencies.length];
		for (int i = 0; i < dependencies.length; i++) {
			resolved[i] = resolver.get(dependencies[i], cx, scope);
		}

		final String theId = id;
		DeferredResolution.awaitResolution(cx, scope, thisObj, resolved, factory, new Callback() {

			@Override
			public void handle(Object o) {
				resolver.remember(theId, o);
			}

		}, new boolean[]{false});

		return null;
	}

	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		return (Scriptable) call(cx, scope, null, args);
	}


	@Override
	public String getClassName() {
		return "OXDefine";
	}
}
