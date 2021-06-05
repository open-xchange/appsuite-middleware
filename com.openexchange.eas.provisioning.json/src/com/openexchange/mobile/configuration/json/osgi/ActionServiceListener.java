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

package com.openexchange.mobile.configuration.json.osgi;

import static com.openexchange.mobile.configuration.json.osgi.MobilityProvisioningServiceRegistry.getInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.action.ActionTypes;

/**
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 *
 */
public class ActionServiceListener implements ServiceTrackerCustomizer<ActionService, ActionService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ActionServiceListener.class);

    private final BundleContext context;

    public ActionServiceListener(final BundleContext context) {
        this.context = context;
    }

    @Override
    public ActionService addingService(final ServiceReference<ActionService> serviceReference) {
        final ActionService service = context.getService(serviceReference);
        if (null == service) {
            LOG.warn("Added service is null!", new Throwable());
        }

        {
            final Object identifier = serviceReference.getProperty("action");
            if (null == identifier) {
                LOG.error("Missing identifier in action service: {}", serviceReference.getClass().getName());
                return service;
            }
            if (getInstance().getActionService((ActionTypes) identifier) != null) {
                LOG.error("A action service is already registered for identifier: {}", identifier);
                return service;
            }
            getInstance().putActionService((ActionTypes) identifier, service);
            LOG.info("Action service for identifier '{}' successfully registered", identifier);
        }
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<ActionService> reference, final ActionService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<ActionService> reference, final ActionService service) {
        try {
            final Object identifier = reference.getProperty("action");
            if (null == identifier) {
                LOG.error("Missing identifier in action service: {}", service.getClass().getName());
                return;
            }
            getInstance().removeActionService((ActionTypes) identifier);
            LOG.info("Action service for identifier '{}' successfully unregistered", identifier);
        } finally {
            context.ungetService(reference);
        }
    }

}
