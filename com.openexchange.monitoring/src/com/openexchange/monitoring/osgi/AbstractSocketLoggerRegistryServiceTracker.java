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

package com.openexchange.monitoring.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.monitoring.sockets.SocketLoggerRegistryService;
import com.openexchange.monitoring.sockets.exceptions.BlackListException;

/**
 * {@link AbstractSocketLoggerRegistryServiceTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
abstract class AbstractSocketLoggerRegistryServiceTracker implements ServiceTrackerCustomizer<SocketLoggerRegistryService, SocketLoggerRegistryService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSocketLoggerRegistryServiceTracker.class);
    private final BundleContext context;
    private final String loggerName;

    /**
     * Initialises a new {@link SocketLoggerRegistryServiceTracker}.
     */
    protected AbstractSocketLoggerRegistryServiceTracker(BundleContext context, String loggerName) {
        super();
        this.context = context;
        this.loggerName = loggerName;
    }

    @Override
    public SocketLoggerRegistryService addingService(ServiceReference<SocketLoggerRegistryService> reference) {
        try {
            SocketLoggerRegistryService registryService = context.getService(reference);
            onRegistryAppeared(registryService, loggerName);
            return registryService;
        } catch (BlackListException e) {
            LOGGER.error("Failed to register logger '{}'", loggerName, e);
            context.ungetService(reference);
            return null;
        }
    }

    @Override
    public void modifiedService(ServiceReference<SocketLoggerRegistryService> reference, SocketLoggerRegistryService service) {
        // no-op
    }

    @Override
    public void removedService(ServiceReference<SocketLoggerRegistryService> reference, SocketLoggerRegistryService service) {
        onRegistryDisappearing(service, loggerName);
        context.ungetService(reference);
    }

    /**
     * Called when socket logger registry appeared.
     *
     * @param registryService The socket logger registry
     * @param loggerName The name of the logger to handle
     */
    protected abstract void onRegistryAppeared(SocketLoggerRegistryService registryService, String loggerName) throws BlackListException;

    /**
     * Called when socket logger registry is about to disappear.
     *
     * @param registryService The socket logger registry
     * @param loggerName The name of the logger to handle
     */
    protected abstract void onRegistryDisappearing(SocketLoggerRegistryService registryService, String loggerName);
}
