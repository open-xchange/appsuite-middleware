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

package com.openexchange.jslob.config.osgi;

import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.jslob.config.ConfigJSlobService;
import com.openexchange.jslob.shared.SharedJSlobService;

/**
 * {@link SharedJSlobServiceTracker}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SharedJSlobServiceTracker implements ServiceTrackerCustomizer<SharedJSlobService, SharedJSlobService> {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SharedJSlobServiceTracker.class);

    private final BundleContext context;

    private final ConfigJSlobService jslobService;

    /**
     * Initializes a new {@link SharedJSlobServiceTracker}.
     */
    public SharedJSlobServiceTracker(BundleContext context, ConfigJSlobService jslobService) {
        super();
        this.context = context;
        this.jslobService = jslobService;
    }

    @Override
    public SharedJSlobService addingService(ServiceReference<SharedJSlobService> reference) {
        SharedJSlobService service = context.getService(reference);
        try {
            jslobService.setShared(service.getId(), service);
            EventAdmin eventAdmin = jslobService.getServices().getOptionalService(EventAdmin.class);
            if (null != eventAdmin) {
                Map<String, Object> properties = new HashMap<String, Object>(1);
                properties.put("service", service);
                Event event = new Event(SharedJSlobService.EVENT_ADDED, properties);
                eventAdmin.postEvent(event);
            }
        } catch (RuntimeException e) {
            LOG.error("", e);
        }
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<SharedJSlobService> reference, SharedJSlobService service) {
        // nothing to do
    }

    @Override
    public void removedService(ServiceReference<SharedJSlobService> reference, SharedJSlobService service) {
        try {
            jslobService.setShared(service.getId(), null);
            EventAdmin eventAdmin = jslobService.getServices().getOptionalService(EventAdmin.class);
            if (null != eventAdmin) {
                Map<String, Object> properties = new HashMap<String, Object>(1);
                properties.put("service", service);
                Event event = new Event(SharedJSlobService.EVENT_REMOVED, properties);
                eventAdmin.postEvent(event);
            }
        } catch (RuntimeException e) {
            LOG.error("", e);
        }
        context.ungetService(reference);
    }

}
