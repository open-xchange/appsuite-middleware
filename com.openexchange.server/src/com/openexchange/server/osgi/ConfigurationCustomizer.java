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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.groupware.configuration.ParticipantConfig;
import com.openexchange.groupware.contact.ContactConfig;

/**
 * Configuration service is dynamically pushed into configuration keeping classes.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
final class ConfigurationCustomizer implements ServiceTrackerCustomizer<ConfigurationService, ConfigurationService> {

    private final BundleContext context;

    public ConfigurationCustomizer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
        final ConfigurationService confService = context.getService(reference);
        ServerConfig.getInstance().initialize(confService);
        ParticipantConfig.getInstance().initialize(confService);
        ContactConfig.getInstance().initialize(confService);
        return confService;
    }

    @Override
    public void modifiedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        // ConfigurationService is not referenced in ContactConfig.
        // ConfigurationService is not referenced in ParticipantConfig.
        ServerConfig.getInstance().shutdown();
        context.ungetService(reference);
    }
}
