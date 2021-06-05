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

package com.openexchange.net.osgi;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.monitoring.sockets.SocketLoggerRegistryService;
import com.openexchange.net.HostList;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceLookup;

/**
 * {@link NetSSLActivator} - The {@link BundleActivator activator} for monitoring bundle.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public final class NetSSLActivator extends HousekeepingActivator {

    private static final AtomicReference<ServiceLookup> REF = new AtomicReference<>();

    /**
     * Sets the service lookup.
     *
     * @param serviceLookup The service lookup or <code>null</code>
     */
    private static void setServiceLookup(final ServiceLookup serviceLookup) {
        REF.set(serviceLookup);
    }

    /**
     * Gets the service lookup.
     *
     * @return The service lookup or <code>null</code>
     */
    public static ServiceLookup getServiceLookup() {
        return REF.get();
    }

    /**
     * Initializes a new {@link NetSSLActivator}
     */
    public NetSSLActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        setServiceLookup(this);
        trackService(SocketLoggerRegistryService.class);
        openTrackers();

        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                HostList.flushCache();
            }
        });
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();
        setServiceLookup(null);
    }
}
