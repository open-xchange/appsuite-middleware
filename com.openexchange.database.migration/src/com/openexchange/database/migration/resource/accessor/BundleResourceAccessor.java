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

package com.openexchange.database.migration.resource.accessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import liquibase.resource.ResourceAccessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private ClassLoader bundleClassLoader;

    private BundleWiring bundleWiring;

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
