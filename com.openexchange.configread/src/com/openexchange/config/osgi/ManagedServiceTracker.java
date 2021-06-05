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

package com.openexchange.config.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;


/**
 * {@link ManagedServiceTracker} - Tracks {@link ManagedService} instances and applies certain configuration to them.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public final class ManagedServiceTracker extends ServiceTracker<ManagedService, ManagedService> {

    private final ConfigurationService configService;

    /**
     * Initializes a new {@link ManagedServiceTracker}.
     */
    public ManagedServiceTracker(final BundleContext context, ConfigurationService configService) {
        super(context, ManagedService.class, null);
        this.configService = configService;
    }

    @Override
    public ManagedService addingService(final ServiceReference<ManagedService> reference) {
        boolean serviceObtained = false;
        try {
            if ("org.apache.felix.webconsole.internal.servlet.OsgiManager".equals(reference.getProperty(Constants.SERVICE_PID))) {
                final ManagedService service = super.addingService(reference);
                serviceObtained = true;

                configureWebConsole(service, configService);

                return service;
            }
        } catch (ConfigurationException e) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManagedServiceTracker.class);
            log.warn("Cannot configure Apache Felix Web Console", e);
        } catch (RuntimeException e) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManagedServiceTracker.class);
            log.warn("Cannot configure Apache Felix Web Console", e);
        }
        if (serviceObtained) {
            context.ungetService(reference);
        }
        return null;
    }

    /**
     * Configures the Web Console.
     *
     * @param service The associated managed service
     * @param configService The config service
     * @throws ConfigurationException If configuration fails
     */
    public static void configureWebConsole(final ManagedService service, ConfigurationService configService) throws ConfigurationException {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
        properties.put("manager.root", configService.getProperty("com.openexchange.webconsole.servletPath", "/servlet/console"));
        properties.put("username", configService.getProperty("com.openexchange.webconsole.username", "open-xchange"));
        properties.put("password", configService.getProperty("com.openexchange.webconsole.password", "secret"));
        properties.put("realm", configService.getProperty("com.openexchange.webconsole.realm", "Open-Xchange Management Console"));
        service.updated(properties);
    }

}
