/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
            parseFileStorageProperties(getService(ConfigurationService.class));
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
        } catch (final Exception e) {
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
            cleanUp();
            dropFileStorageProperties();
        } catch (final Exception e) {
            log.error("Stopping bundle \"com.openexchange.file.storage.config\" failed.", e);
            throw e;
        }
    }

    private void parseFileStorageProperties(final ConfigurationService configurationService) {
        final Properties fsProperties = configurationService.getFile("filestorage.properties");
        ConfigFileStorageAccountParser.getInstance().parse(fsProperties);
    }

    private void dropFileStorageProperties() {
        ConfigFileStorageAccountParser.getInstance().drop();
    }

}
