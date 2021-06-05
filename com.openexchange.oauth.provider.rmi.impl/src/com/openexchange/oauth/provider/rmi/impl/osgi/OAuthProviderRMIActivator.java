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

package com.openexchange.oauth.provider.rmi.impl.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.oauth.provider.rmi.impl.RemoteClientManagementImpl;

/**
 * {@link OAuthProviderRMIActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthProviderRMIActivator implements BundleActivator {

    private ServiceRegistration<Remote> serviceRegistration;
    private ServiceTracker<ClientManagement, ClientManagement> tracker;

    /**
     * Initializes a new {@link OAuthProviderRMIActivator}.
     */
    public OAuthProviderRMIActivator() {
        super();
    }

    @Override
    public synchronized void start(BundleContext context) throws Exception {
        org.slf4j.LoggerFactory.getLogger(OAuthProviderRMIActivator.class).info("starting bundle: \"com.openexchange.oauth.provider.rmi.impl\"");

        tracker = new ServiceTracker<ClientManagement, ClientManagement>(context, ClientManagement.class, null) {

            @SuppressWarnings("synthetic-access")
            @Override
            public ClientManagement addingService(ServiceReference<ClientManagement> reference) {
                ClientManagement service = super.addingService(reference);
                if (service != null) {
                    register(context, service);
                }

                return service;
            }

            @SuppressWarnings("synthetic-access")
            @Override
            public void remove(ServiceReference<ClientManagement> reference) {
                unregister();
                super.remove(reference);
            }
        };

        tracker.open();
    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        org.slf4j.LoggerFactory.getLogger(OAuthProviderRMIActivator.class).info("stopping bundle: \"com.openexchange.oauth.provider.rmi.impl\"");

        unregister();
        tracker.close();
        tracker = null;
    }

    private synchronized void register(BundleContext context, ClientManagement clientManagement) {
        if (serviceRegistration == null) {
            Dictionary<String, Object> props = new Hashtable<>(2);
            props.put("RMIName", RemoteClientManagement.RMI_NAME);
            serviceRegistration = context.registerService(Remote.class, new RemoteClientManagementImpl(clientManagement), props);
        }
    }

    private synchronized void unregister() {
        ServiceRegistration<Remote> serviceRegistration = this.serviceRegistration;
        if (serviceRegistration != null) {
            this.serviceRegistration = null;
            serviceRegistration.unregister();
        }
    }

}
