package com.openexchange.scripting.rhino.require;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.osgi.framework.Bundle;

/**
 * We'll implement the AMD spec here as best we can, with a few extensions accounting for us living in OSGi
 * @author francisco.laguna@open-xchange.com
 *
 */
public class RequireSupport {
	
	private static final ConcurrentHashMap<String, DependencyResolver> bundleResolvers = new ConcurrentHashMap<String, RequireSupport.DependencyResolver>();
	
	public static void initialize(Scriptable serviceScope, Context cx,
			Bundle bundle) {

		DependencyResolver resolver = new DependencyResolver(bundle);
		bundleResolvers.put(bundle.getSymbolicName(), resolver);
		
		serviceScope.put("define", serviceScope, new OXDefine(bundle.getSymbolicName()+":/main.js", resolver));
		
		OXRequire oxRequire = new OXRequire(resolver);
		serviceScope.put("require", serviceScope, oxRequire);
		resolver.remember("require", oxRequire);
	
		DeferredResolution.resolve(bundle, resolver);
	}
	
	private static final class DependencyResolver {

		private DependencyResolver resolver = null;
		private Bundle bundle;
		private ConcurrentHashMap<String, Object> registeredModules = new ConcurrentHashMap<String, Object>();
		
		public DependencyResolver(Bundle bundle) {
			this(null, bundle);
		}
		
		public DependencyResolver(DependencyResolver resolver, Bundle bundle) {
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
			
			for (DependencyResolver other : bundleResolvers.values()) {
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
		
	
	}
	
	private static final class OXRequire extends ScriptableObject implements Function {
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
			awaitResolution(cx, scope, thisObj, resolved, fun, null);
			
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
	
	
	private static final class OXDefine extends ScriptableObject implements Function {
		
		private DependencyResolver resolver; 
		private String defaultModuleName;

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
			awaitResolution(cx, scope, thisObj, resolved, factory, new Callback() {

				@Override
				public void handle(Object o) {
					resolver.remember(theId, o);
				}
				
			});
			
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
	
	private static final class DeferredResolution {
		
		private static final LinkedList<DeferredResolution> outstanding = new LinkedList<DeferredResolution>();
		private static final Lock LOCK = new ReentrantLock();
		
		public static void resolve(Bundle bundle, DependencyResolver resolver) {
			
			try {
				LOCK.lock();
				Context cx = Context.enter();
				Iterator<DeferredResolution> iterator = outstanding.iterator();
				while(iterator.hasNext()) {
					DeferredResolution next = iterator.next();
					if (resolver.knows(next.id) && next.callback != null) {
						next.callback.handle(resolver.get(next.id, cx, next.scope));
					}
				}
			} finally {
				Context.exit();
				LOCK.unlock();
				
			}
		}

		private String id;
		private Scriptable scope;
		private Callback callback;

		public DeferredResolution(String id, Scriptable scope) {
			this.id = id;
			this.scope = scope;
			outstanding.add(this);
		}

		public void done(Callback callback) {
			this.callback = callback;
		}
		
	}
	
	public static void awaitResolution(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args, final Function function, final Callback cb) {
		for(int i = 0; i < args.length; i++) {
			Object o = args[i];
			if (o instanceof DeferredResolution) {
				final int index = i;
				DeferredResolution resolution = (DeferredResolution) o;
				resolution.done(new Callback() {

					@Override
					public void handle(Object o) {
						args[index] = o;
						awaitResolution(cx, scope, thisObj, args, function, cb);
					}
					
				});
				return;
			}
		}
		Object retval = function.call(cx, scope, thisObj, args);
		if (cb != null) {
			cb.handle(retval);
		}
	}
	
	static interface Callback {
		public void handle(Object o);
	}

}
