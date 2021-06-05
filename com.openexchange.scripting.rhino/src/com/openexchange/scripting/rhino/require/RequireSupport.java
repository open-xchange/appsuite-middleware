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
                DeferredResolution.resolve(dependencyResolver);
            }
        } finally {
            Context.exit();
        }

    }

    public static void initialize(final Scriptable serviceScope, JSBundle bundle, Map<String, Object> additionalModules) {

        DependencyResolver resolver = new DependencyResolver(bundle);
        bundleResolvers.put(bundle.getSymbolicName(), resolver);

        serviceScope.put("define", serviceScope, new OXDefine(bundle.getSymbolicName() + "/main", resolver));

        OXRequire oxRequire = new OXRequire(resolver);
        serviceScope.put("require", serviceScope, oxRequire);
        resolver.remember("require", oxRequire);

        for (ResolveEnhancement enhancement : resolveEnhancements) {
            enhancement.enhance(resolver, converter);
        }

        for (Map.Entry<String, Object> additional : additionalModules.entrySet()) {
            resolver.remember(additional.getKey(), Context.javaToJS(additional.getValue(), serviceScope));
        }

        DeferredResolution.resolve(resolver);
    }

}
