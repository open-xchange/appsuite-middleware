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

package com.openexchange.rmi.osgi;

import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rmi.internal.RMIUtility;
import com.openexchange.startup.SignalStartedService;

/**
 * {@link RMIService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RMIActivator extends HousekeepingActivator {

    private Registry registry;

    /**
     * Initializes a new {@link RMIActivator}.
     */
    public RMIActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, SignalStartedService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RMIActivator.class);
        logger.info("Starting bundle com.openexchange.rmi");

        // Create registry instance
        final Registry registry = RMIUtility.createRegistry(getService(ConfigurationService.class));
        this.registry = registry;

        // Start tracker
        track(Remote.class, new RMITrackerCustomizer(registry, context));
        openTrackers();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RMIActivator.class);
        logger.info("Stopping bundle com.openexchange.rmi");
        super.stopBundle();

        // De-register the registry
        Registry registry = this.registry;
        if (null != registry) {
            UnicastRemoteObject.unexportObject(registry, true);
            this.registry = null;
        }
    }

}
