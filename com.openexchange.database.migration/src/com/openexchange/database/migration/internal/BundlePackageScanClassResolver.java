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
package com.openexchange.database.migration.internal;

import java.util.Collection;
import java.util.Set;
import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.PackageScanFilter;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Package scan resolver that works with OSGI frameworks.
 */
public class BundlePackageScanClassResolver extends DefaultPackageScanClassResolver {

    private final BundleWiring bundleWiring;

    public BundlePackageScanClassResolver(Bundle bundle) {
        this.bundleWiring = bundle.adapt(BundleWiring.class);
    }

    @Override
    protected void find(PackageScanFilter test, String packageName, Set<Class<?>> classes) {
        packageName = packageName.replace('.', '/');

        Collection<String> names =
            bundleWiring.listResources(packageName, "*.class", BundleWiring.LISTRESOURCES_RECURSE);
        if (names == null) {
            return;
        }
        ClassLoader bundleClassLoader = bundleWiring.getClassLoader();
        for (String name : names) {
            String fixedName = name.substring(0, name.indexOf('.')).replace('/', '.');

            try {
                Class<?> klass = bundleClassLoader.loadClass(fixedName);
                if (test.matches(klass)) {
                    classes.add(klass);
                }
            } catch (ClassNotFoundException e) {
                log.debug("Can't load class: " + e.getMessage());
            }
        }
    }
}
