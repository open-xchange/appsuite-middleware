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

package com.openexchange.database.migration.ox.internal.accessors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

public class SimpleClassLoaderResourceAccessor implements ResourceAccessor {
    private ClassLoader classLoader;

    public SimpleClassLoaderResourceAccessor() {
        classLoader = getClass().getClassLoader();
    }

    public SimpleClassLoaderResourceAccessor(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public InputStream getResourceAsStream(final String file) throws IOException {
        return classLoader.getResourceAsStream(file);
    }

    @Override
    public Enumeration<URL> getResources(final String packageName) throws IOException {
        return classLoader.getResources(packageName);
    }

    @Override
    public ClassLoader toClassLoader() {
        return classLoader;
    }

    @Override
    public String toString() {
        String description;
        if (classLoader instanceof URLClassLoader) {
            List<String> urls = new ArrayList<String>();
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                urls.add(url.toExternalForm());
            }
            description = StringUtils.join(urls, ",");
        } else {
            description = classLoader.getClass().getName();
        }
        return getClass().getName() + "(" + description + ")";

    }
}
