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

package com.openexchange.serialization;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * {@link BundleClassResolver} - Uses an OSGi bundle to resolve classes by name.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class BundleClassResolver implements ClassResolver {

    private final Bundle bundle;

    /**
     * Initializes a new {@link BundleClassResolver}.
     *
     * @param bundleContext The bundle context providing the associated bundle
     */
    public BundleClassResolver(BundleContext bundleContext) {
        this(bundleContext.getBundle());
    }

    /**
     * Initializes a new {@link BundleClassResolver}.
     *
     * @param bundle The bundle to use to load a class (using bundle's class loader)
     */
    public BundleClassResolver(Bundle bundle) {
        super();
        this.bundle = bundle;
    }

    @Override
    public Class<?> resolveClass(String className) throws ClassNotFoundException {
        return bundle.loadClass(className);
    }

}
