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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.openexchange.scripting.rhino.SharedScope;

/**
 * We'll implement the AMD spec here as best we can, with a few extensions
 * accounting for us living in OSGi
 *
 * @author francisco.laguna@open-xchange.com
 *
 */
public class RequireSupport {

	static final ConcurrentHashMap<String, DependencyResolver> bundleResolvers = new ConcurrentHashMap<String, DependencyResolver>();

	private static JSConverter converter = new JSConverter() {

		@Override
		public Object toJS(Object o) {
			return Context.javaToJS(o, SharedScope.SHARED_SCOPE);
		}
	};

	public static final List<ResolveEnhancement> resolveEnhancements = new ArrayList<ResolveEnhancement>();

	public static void addResolveEnhancement(ResolveEnhancement enhancement) {
		try {
			Context.enter();
			resolveEnhancements.add(enhancement);
			Collection<DependencyResolver> values = bundleResolvers.values();
			for (DependencyResolver dependencyResolver : values) {
				enhancement.enhance(dependencyResolver, converter);
			}
			for (DependencyResolver dependencyResolver : values) {
				DeferredResolution.resolve(dependencyResolver.getBundle(),
						dependencyResolver);
			}
		} finally {
			Context.exit();
		}

	}

	public static void initialize(final Scriptable serviceScope, Context cx,
			JSBundle bundle, Map<String, Object> additionalModules) {

		DependencyResolver resolver = new DependencyResolver(bundle);
		bundleResolvers.put(bundle.getSymbolicName(), resolver);

		serviceScope.put("define", serviceScope,
				new OXDefine(bundle.getSymbolicName() + "/main", resolver));

		OXRequire oxRequire = new OXRequire(resolver);
		serviceScope.put("require", serviceScope, oxRequire);
		resolver.remember("require", oxRequire);

		for (ResolveEnhancement enhancement : resolveEnhancements) {
			enhancement.enhance(resolver, converter);
		}

		for (Map.Entry<String, Object> additional : additionalModules
				.entrySet()) {
			resolver.remember(additional.getKey(),
					Context.javaToJS(additional.getValue(), serviceScope));
		}

		DeferredResolution.resolve(bundle, resolver);
	}

}
