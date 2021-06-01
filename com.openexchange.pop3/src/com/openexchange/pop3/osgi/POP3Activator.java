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

package com.openexchange.pop3.osgi;

import static com.openexchange.pop3.services.POP3ServiceRegistry.getServiceRegistry;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.charset.CharsetService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.pop3.Enabled;
import com.openexchange.pop3.POP3Provider;
import com.openexchange.pop3.storage.POP3StorageProvider;
import com.openexchange.pop3.storage.mailaccount.MailAccountPOP3StorageProvider;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link POP3Activator} - The {@link BundleActivator activator} for POP3 bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POP3Activator.class);

    private POP3StorageProviderServiceTrackerCustomizer customizer;
    private MailAccountPOP3StorageProvider builtInProvider;

    /**
     * Initializes a new {@link POP3Activator}
     */
    public POP3Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ConfigurationService.class, CacheService.class, UserService.class, MailAccountStorageService.class,
            ContextService.class, TimerService.class, ConfigViewFactory.class, CapabilityService.class, SSLSocketFactoryProvider.class,
            SSLConfigurationService.class, UserAwareSSLConfigurationService.class, ThreadPoolService.class, CharsetService.class
        };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        getServiceRegistry().addService(clazz, getService(clazz));
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                        registry.addService(classes[i], service);
                    }
                }
            }
            final Dictionary<String, String> dictionary = new Hashtable<String, String>(2);
            dictionary.put("protocol", POP3Provider.PROTOCOL_POP3.toString());
            registerService(MailProvider.class, POP3Provider.getInstance(), dictionary);
            /*
             * Signal availability for POP3 provider
             */
            getService(CapabilityService.class).declareCapability("pop3");
            /*
             * Add built-in mail account POP3 storage provider
             */
            final POP3StorageProviderServiceTrackerCustomizer customizer = new POP3StorageProviderServiceTrackerCustomizer(context);
            this.customizer = customizer;
            final MailAccountPOP3StorageProvider builtInProvider = new MailAccountPOP3StorageProvider();
            this.builtInProvider = builtInProvider;
            customizer.addPOP3StorageProvider(builtInProvider);
            /*
             * Service tracker for possible POP3 storage provider
             */
            track(POP3StorageProvider.class, customizer);
            track(SessiondService.class, new RegistryCustomizingServiceTrackerCustomizer<SessiondService>(SessiondService.class, context));
            track(AuditLogService.class, new RegistryCustomizingServiceTrackerCustomizer<AuditLogService>(AuditLogService.class, context));
            openTrackers();
            /*
             * Register
             */
            registerService(PreferencesItemService.class, new Enabled(getService(ConfigViewFactory.class)), null);
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        try {
            // Remove built-in provider
            final POP3StorageProviderServiceTrackerCustomizer customizer = this.customizer;
            if (null != customizer) {
                customizer.removePOP3StorageProvider(builtInProvider);
                builtInProvider = null;
                // Customizer shut-down
                customizer.dropAllRegistrations();
                this.customizer = null;
            }
            CapabilityService capabilityService = getService(CapabilityService.class);
            if (null != capabilityService) {
                capabilityService.undeclareCapability("pop3");
            }
            cleanUp();
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

}
