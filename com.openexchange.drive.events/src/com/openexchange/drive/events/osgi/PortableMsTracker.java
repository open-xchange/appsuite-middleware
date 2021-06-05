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

package com.openexchange.drive.events.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.drive.events.internal.DriveEventServiceImpl;
import com.openexchange.drive.events.ms.MsDriveEventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.ms.PortableMsService;

/**
 * {@link PortableMsTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class PortableMsTracker implements ServiceTrackerCustomizer<PortableMsService, PortableMsService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PortableMsTracker.class);

    private final DriveEventServiceImpl service;
    private final BundleContext context;
    private MsDriveEventHandler eventHandler;

    /**
     * Initializes a new {@link PortableMsTracker}.
     * 
     * @param service
     */
    PortableMsTracker(BundleContext context, DriveEventServiceImpl service) {
        this.context = context;
        this.service = service;
    }

    @Override
    public synchronized PortableMsService addingService(ServiceReference<PortableMsService> reference) {
        PortableMsService messagingService = context.getService(reference);
        MsDriveEventHandler.setMsService(messagingService);
        LOG.debug("Initializing messaging service drive event handler");
        try {
            this.eventHandler = new MsDriveEventHandler(service);
        } catch (OXException e) {
            throw new IllegalStateException(e.getMessage(), new BundleException(e.getMessage(), BundleException.ACTIVATOR_ERROR, e));
        }
        return messagingService;
    }

    @Override
    public void modifiedService(ServiceReference<PortableMsService> reference, PortableMsService service) {
        // Ignored
    }

    @Override
    public synchronized void removedService(ServiceReference<PortableMsService> reference, PortableMsService service) {
        LOG.debug("Stopping messaging service cache event handler");
        MsDriveEventHandler eventHandler = this.eventHandler;
        if (null != eventHandler) {
            eventHandler.stop();
            this.eventHandler = null;
        }
        MsDriveEventHandler.setMsService(null);
    }
}
