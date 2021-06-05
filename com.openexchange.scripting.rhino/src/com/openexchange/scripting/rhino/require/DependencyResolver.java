/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

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
	private final JSBundle bundle;
	private final ConcurrentHashMap<String, Object> registeredModules = new ConcurrentHashMap<String, Object>();

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
					} catch (@SuppressWarnings("unused") IOException e) {
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
