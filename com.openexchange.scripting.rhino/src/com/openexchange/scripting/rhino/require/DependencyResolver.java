/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
