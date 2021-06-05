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

package com.openexchange.database.migration.resource.accessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import liquibase.resource.ResourceAccessor;

/**
 * A {@link BundleResourceAccessor} allows to access arbitrary resources of
 * a specific OSGi bundle.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.1
 */
public class BundleResourceAccessor implements ResourceAccessor {

    private static final Logger LOG = LoggerFactory.getLogger(BundleResourceAccessor.class);

    /**
     * ClassLoader of the bundle to search for migration files within
     */
    private final ClassLoader bundleClassLoader;

    private final BundleWiring bundleWiring;

    /**
     * Initializes a new {@link BundleResourceAccessor} for classloader bundle wiring.
     *
     * @param context The {@link BundleContext} of the {@link Bundle} whose contents shall be accessed.
     */
    public BundleResourceAccessor(final BundleContext context) {
        Bundle bundle = context.getBundle();
        bundleWiring = bundle.adapt(BundleWiring.class);
        if (bundleWiring == null) {
            throw new IllegalArgumentException("The passed contexts bundle cannot be adapted to org.osgi.framework.wiring.BundleWiring!");
        }

        this.bundleClassLoader = bundleWiring.getClassLoader();
    }

    /**
     * Initializes a new {@link BundleResourceAccessor} for classloader bundle wiring.
     *
     * @param bundle The {@link Bundle} whose contents shall be accessed.
     */
    public BundleResourceAccessor(final Bundle bundle) {
        bundleWiring = bundle.adapt(BundleWiring.class);
        if (bundleWiring == null) {
            throw new IllegalArgumentException("The passed bundle cannot be adapted to org.osgi.framework.wiring.BundleWiring!");
        }

        this.bundleClassLoader = bundleWiring.getClassLoader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResourceAsStream(String file) {
        if (file == null || file.length() == 0) {
            return null;
        }

        String filePattern = file;
        int i = file.lastIndexOf('/');
        if (i > 0) {
            if (i < file.length() - 1) {
                filePattern = file.substring(i + 1);
            }
        }

        URL fileUrl = null;
        List<URL> entries = bundleWiring.findEntries("/", filePattern, BundleWiring.FINDENTRIES_RECURSE);
        for (URL entry : entries) {
            if (entry.toExternalForm().endsWith(file)) {
                fileUrl = entry;
                break;
            }
        }

        if (fileUrl != null) {
            try {
                return fileUrl.openStream();
            } catch (IOException e) {
                LOG.error("Could not open ChangeLog file!", e);
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<URL> getResources(String packageName) throws IOException {
        return this.bundleClassLoader.getResources(packageName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader toClassLoader() {
        return this.bundleClassLoader;
    }
}
