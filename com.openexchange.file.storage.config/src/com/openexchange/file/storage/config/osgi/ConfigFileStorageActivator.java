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

package com.openexchange.file.storage.config.osgi;

import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.config.ConfigFileStorageAuthenticator;
import com.openexchange.file.storage.config.internal.ConfigFileStorageAccountManagerProvider;
import com.openexchange.file.storage.config.internal.ConfigFileStorageAccountParser;
import com.openexchange.file.storage.config.internal.Services;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ConfigFileStorageActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class ConfigFileStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ConfigFileStorageActivator}.
     */
    public ConfigFileStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, FileStorageServiceRegistry.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigFileStorageActivator.class);
        logger.warn("Absent service: {}", clazz.getName());
        if (ConfigurationService.class.equals(clazz)) {
            dropFileStorageProperties();
        }
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigFileStorageActivator.class);
        logger.info("Re-available service: {}", clazz.getName());
        if (ConfigurationService.class.equals(clazz)) {
            try {
                parseFileStorageProperties(getService(ConfigurationService.class));
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigFileStorageActivator.class);
        try {
            log.info("starting bundle: com.openexchange.file.storage.config");
            Services.setServices(this);
            /*
             * Parse file storage configuration
             */
            parseFileStorageProperties(getService(ConfigurationService.class));
            /*
             * Tracker
             */
            final BundleContext context = this.context;
            final ConcurrentMap<ConfigFileStorageAuthenticator, ConfigFileStorageAuthenticator> authenticators = ConfigFileStorageAccountParser.getInstance().getAuthenticators();
            final ServiceTrackerCustomizer<ConfigFileStorageAuthenticator, ConfigFileStorageAuthenticator> customizer = new ServiceTrackerCustomizer<ConfigFileStorageAuthenticator, ConfigFileStorageAuthenticator>() {

                @Override
                public ConfigFileStorageAuthenticator addingService(final ServiceReference<ConfigFileStorageAuthenticator> reference) {
                    final ConfigFileStorageAuthenticator authenticator = context.getService(reference);
                    if (null == authenticators.putIfAbsent(authenticator, authenticator)) {
                        return authenticator;
                    }
                    context.ungetService(reference);
                    return null;
                }

                @Override
                public void modifiedService(final ServiceReference<ConfigFileStorageAuthenticator> reference, final ConfigFileStorageAuthenticator authenticator) {
                    // Nope
                }

                @Override
                public void removedService(final ServiceReference<ConfigFileStorageAuthenticator> reference, final ConfigFileStorageAuthenticator authenticator) {
                    if (null != authenticator) {
                        try {
                            authenticators.remove(authenticator);
                        } finally {
                            context.ungetService(reference);
                        }
                    }
                }
            };
            track(ConfigFileStorageAuthenticator.class, customizer);
            openTrackers();
            /*
             * Register services
             */
            registerService(FileStorageAccountManagerProvider.class, new ConfigFileStorageAccountManagerProvider(), null);
        } catch (Exception e) {
            log.error("Starting bundle \"com.openexchange.file.storage.config\" failed.", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigFileStorageActivator.class);
        try {
            log.info("stopping bundle: com.openexchange.file.storage.config");
            Services.setServices(null);
            dropFileStorageProperties();
            super.stopBundle();
        } catch (Exception e) {
            log.error("Stopping bundle \"com.openexchange.file.storage.config\" failed.", e);
            throw e;
        }
    }

    private void parseFileStorageProperties(final ConfigurationService configurationService) {
        Properties fsProperties = configurationService.getFile("filestorage.properties");
        ConfigFileStorageAccountParser.getInstance().parse(fsProperties);
    }

    private void dropFileStorageProperties() {
        ConfigFileStorageAccountParser.getInstance().drop();
    }

}
