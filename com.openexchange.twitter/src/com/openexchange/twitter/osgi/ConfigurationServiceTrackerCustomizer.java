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

package com.openexchange.twitter.osgi;

import static com.openexchange.twitter.osgi.TwitterServiceRegistry.getServiceRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.twitter.internal.TwitterConfiguration;
import twitter4j.conf.OXConfigurationBase;

/**
 * {@link ConfigurationServiceTrackerCustomizer} - The {@link ServiceTrackerCustomizer customizer} for {@link ConfigurationService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConfigurationServiceTrackerCustomizer implements ServiceTrackerCustomizer<ConfigurationService,ConfigurationService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link ConfigurationServiceTrackerCustomizer}.
     *
     * @param context The bundle context
     */
    public ConfigurationServiceTrackerCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public ConfigurationService addingService(final ServiceReference<ConfigurationService> reference) {
        final ConfigurationService service = context.getService(reference);
        /*
         * Add to registry
         */
        getServiceRegistry().addService(ConfigurationService.class, service);
        /*
         * ... and configure
         */
        final ConfigurationService configurationService = service;
        TwitterConfiguration.getInstance().configure(configurationService);
        OXConfigurationBase.getInstance().parseFrom(configurationService);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
        if (null != service && ConfigurationService.class.isInstance(service)) {
            try {
                /*
                 * Remove from registry
                 */
                getServiceRegistry().removeService(ConfigurationService.class);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
