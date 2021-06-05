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

package com.openexchange.conversion.engine.osgi;

import static com.openexchange.conversion.engine.internal.ConversionEngineRegistry.getInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.engine.internal.ConversionEngineRegistry;

/**
 * {@link DataHandlerTracker} - The service tracker customizer for {@link DataHandler}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DataHandlerTracker implements ServiceTrackerCustomizer<DataHandler, DataHandler> {

    private static final String PROP_IDENTIFIER = "identifier";

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(DataHandlerTracker.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link DataHandlerTracker}
     *
     * @param context The bundle context
     */
    public DataHandlerTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public DataHandler addingService(final ServiceReference<DataHandler> reference) {
        final DataHandler addedService = context.getService(reference);
        if (null == addedService) {
            LOG.warn("Added service is null!", new Throwable());
            context.ungetService(reference);
            return null;
        }
        final String identifier = (String) reference.getProperty(PROP_IDENTIFIER);
        if (null == identifier) {
            LOG.error("Missing identifier in data handler: {}", addedService.getClass().getName());
            context.ungetService(reference);
            return null;
        }
        final ConversionEngineRegistry registry = getInstance();
        synchronized (registry) {
            if (registry.getDataHandler(identifier) != null) {
                context.ungetService(reference);
                return null;
            }
            registry.putDataHandler(identifier, addedService);
            LOG.info("Data handler for identifier '{}' successfully registered", identifier);
        }
        return addedService;
    }

    @Override
    public void modifiedService(final ServiceReference<DataHandler> reference, final DataHandler service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<DataHandler> reference, final DataHandler service) {
        if (null == service) {
            return;
        }
        try {
            final String identifier = (String) reference.getProperty(PROP_IDENTIFIER);
            if (null == identifier) {
                LOG.error("Missing identifier in data handler: {}", service.getClass().getName());
                return;
            }
            getInstance().removeDataHandler(identifier);
            LOG.info("Data handler for identifier '{}' successfully unregistered", identifier);
        } finally {
            context.ungetService(reference);
        }
    }

}
