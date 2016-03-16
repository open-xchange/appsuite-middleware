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
