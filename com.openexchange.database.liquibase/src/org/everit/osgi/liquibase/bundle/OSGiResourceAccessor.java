/**
 * This file is part of Everit OSGi Liquibase Bundle.
 *
 * Everit OSGi Liquibase Bundle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit OSGi Liquibase Bundle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit OSGi Liquibase Bundle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.liquibase.bundle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

/**
 * The resource accessor that should be used in OSGi environments.
 *
 */
public class OSGiResourceAccessor extends CompositeResourceAccessor {

    private static class BundleResourceAccessor extends ClassLoaderResourceAccessor implements ResourceAccessor {

        private ClassLoader bundleClassLoader;

        public BundleResourceAccessor(final Bundle bundle) {
            BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
            this.bundleClassLoader = bundleWiring.getClassLoader();
        }

        @Override
        public ClassLoader toClassLoader() {
            return this.bundleClassLoader;
        }
    }

    private final Bundle bundle;

    private final Map<String, Object> attributes;

    /**
     * Creating a new resource accessor for the specified bundle without any attributes.
     *
     * @param bundle The bundle.
     */
    public OSGiResourceAccessor(Bundle bundle) {
        this(bundle, null);
    }

    /**
     * Creating a new {@link OSGiResourceAccessor} for the specified bundle with the specified attributes.
     *
     * @param bundle The bundle.
     * @param attributes See {@link #getAttributes()}.
     */
    public OSGiResourceAccessor(Bundle bundle, Map<String, Object> attributes) {
        super(new BundleResourceAccessor(bundle), new ClassLoaderResourceAccessor(
            OSGiResourceAccessor.class.getClassLoader()));
        this.bundle = bundle;
        if (attributes == null) {
            this.attributes = Collections.emptyMap();
        } else {
            this.attributes = Collections.unmodifiableMap(new HashMap<String, Object>(attributes));
        }
    }

    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Attributes are normally coming from the liquibase.schema capability definition.
     *
     * @return The attributes of the resource accessor.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
