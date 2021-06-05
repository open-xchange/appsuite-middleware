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

package com.openexchange.configjump.generic;

import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;

/**
 * This customizer handles an appearing Configuration service and activates then this bundles service.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigurationTracker implements ServiceTrackerCustomizer<ConfigurationService, ConfigurationService> {

    private final BundleContext context;
    private final Services services;

    /**
     * Default constructor.
     *
     * @param services
     */
    public ConfigurationTracker(final BundleContext context, final Services services) {
        super();
        this.context = context;
        this.services = services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationService addingService(final ServiceReference<ConfigurationService> reference) {
        try {
            ConfigurationService configuration = context.getService(reference);
            Properties props = configuration.getFile("configjump.properties");
            context.ungetService(reference);
            services.registerService(props);
            return null;
        } catch (Exception e) {
            LoggerFactory.getLogger(ConfigurationTracker.class).error("", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifiedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
        // Nothing to do.
    }
}
