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

package com.openexchange.password.mechanism.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.impl.mech.PasswordMechRegistryImpl;

/**
 * {@link PasswordMechServiceTrackerCustomizer}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class PasswordMechServiceTrackerCustomizer implements ServiceTrackerCustomizer<PasswordMech, PasswordMech> {

    private PasswordMechRegistryImpl passwordMechRegistry;
    private BundleContext context;

    public PasswordMechServiceTrackerCustomizer(BundleContext context, PasswordMechRegistryImpl passwordMechRegistry) {
        this.context = context;
        this.passwordMechRegistry = passwordMechRegistry;
    }

    @Override
    public PasswordMech addingService(ServiceReference<PasswordMech> reference) {
        PasswordMech passwordMech = context.getService(reference);
        passwordMechRegistry.register(passwordMech);
        return passwordMech;
    }

    @Override
    public void modifiedService(ServiceReference<PasswordMech> reference, PasswordMech service) {
        // nothing to do
    }

    @Override
    public void removedService(ServiceReference<PasswordMech> reference, PasswordMech service) {
        PasswordMech passwordMech = context.getService(reference);
        passwordMechRegistry.unregister(passwordMech);

    }

}
