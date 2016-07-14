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

package com.openexchange.pop3.osgi;

import static com.openexchange.pop3.services.POP3ServiceRegistry.getServiceRegistry;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.pop3.Enabled;
import com.openexchange.pop3.POP3Provider;
import com.openexchange.pop3.storage.POP3StorageProvider;
import com.openexchange.pop3.storage.mailaccount.MailAccountPOP3StorageProvider;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link POP3Activator} - The {@link BundleActivator activator} for POP3 bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POP3Activator.class);

    private volatile POP3StorageProviderServiceTrackerCustomizer customizer;
    private volatile MailAccountPOP3StorageProvider builtInProvider;

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
            ContextService.class, TimerService.class, ConfigViewFactory.class, CapabilityService.class
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
    public void startBundle() throws Exception {
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
            track(SessiondService.class);
            trackService(AuditLogService.class);
            openTrackers();
            /*
             * Register
             */
            registerService(PreferencesItemService.class, new Enabled(getService(ConfigViewFactory.class)), null);
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
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
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

}
