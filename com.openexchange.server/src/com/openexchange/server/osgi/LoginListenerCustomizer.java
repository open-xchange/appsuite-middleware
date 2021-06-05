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

package com.openexchange.server.osgi;

import static com.openexchange.osgi.util.RankedService.getRanking;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.login.listener.AutoLoginAwareLoginListener;
import com.openexchange.login.listener.LoginListener;
import com.openexchange.login.listener.internal.LoginListenerRegistryImpl;

/**
 * {@link LoginListenerCustomizer} - Registers/unregisters a login listener to/from {@link LoginListenerRegistryImpl}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class LoginListenerCustomizer implements ServiceTrackerCustomizer<Object, Object> {

    private final BundleContext context;

    /**
     * Initializes a new {@link LoginListenerCustomizer}.
     *
     * @param context The bundle context
     */
    public LoginListenerCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public Object addingService(ServiceReference<Object> serviceReference) {
        Object service = context.getService(serviceReference);
        if ((service instanceof LoginListener) || (service instanceof AutoLoginAwareLoginListener)) {
            int ranking = getRanking(service, serviceReference, 0);
            if (LoginListenerRegistryImpl.getInstance().addLoginListener((LoginListener) service, ranking)) {
                return service;
            }
        }
        // Nothing to track
        context.ungetService(serviceReference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<Object> serviceReference, Object service) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<Object> serviceReference, Object service) {
        if ((service instanceof LoginListener) || (service instanceof AutoLoginAwareLoginListener)) {
            LoginListenerRegistryImpl.getInstance().removeLoginListener((LoginListener) service);
            context.ungetService(serviceReference);
        }
    }

}
