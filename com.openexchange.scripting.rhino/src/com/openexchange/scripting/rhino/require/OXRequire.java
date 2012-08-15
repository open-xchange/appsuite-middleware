package com.openexchange.scripting.rhino.require;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

public class OXRequire extends ScriptableObject implements Function {
	private final DependencyResolver resolver;

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
				if (fun == null) {
					fun = (Function) arg;
				} else {
					throw new IllegalArgumentException("Invalid call to 'define'");
				}
			}
		}

		if (fun == null) {
			throw new IllegalArgumentException("Invalid call to 'define'");
		}

		if (dependencies == null) {
			dependencies = new String[]{"require"}; // TODO: exports, module

		}

		Object[] resolved = new Object[dependencies.length];
		for (int i = 0; i < dependencies.length; i++) {
			resolved[i] = resolver.get(dependencies[i], cx, scope);
		}
		DeferredResolution.awaitResolution(cx, scope, thisObj, resolved, fun, null, new boolean[]{false});

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
