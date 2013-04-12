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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contactcollector.osgi;

import org.apache.commons.logging.Log;
import org.osgi.framework.BundleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.contactcollector.folder.ContactCollectorFolderCreator;
import com.openexchange.contactcollector.internal.ContactCollectorServiceImpl;
import com.openexchange.contactcollector.preferences.ContactCollectEnabled;
import com.openexchange.contactcollector.preferences.ContactCollectFolder;
import com.openexchange.contactcollector.preferences.ContactCollectFolderDeleteDenied;
import com.openexchange.contactcollector.preferences.ContactCollectOnMailAccess;
import com.openexchange.contactcollector.preferences.ContactCollectOnMailTransport;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link BundleActivator Activator} for contact collector.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ContactCollectorActivator extends HousekeepingActivator {

    private volatile ContactCollectorServiceImpl collectorInstance;

    /**
     * Initializes a new {@link ContactCollectorActivator}.
     */
    public ContactCollectorActivator() {
        super();
    }

    @Override
    public <S> boolean addServiceAlt(Class<? extends S> clazz, S service) {
        return super.addServiceAlt(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

    @Override
    public void startBundle() throws Exception {
        {
            final ConfigurationService cService = getService(ConfigurationService.class);
            if (!cService.getBoolProperty("com.openexchange.contactcollector.enabled", true)) {
                final Log log = com.openexchange.log.Log.loggerFor(ContactCollectorActivator.class);
                log.info("Canceled start-up of bundle: com.openexchange.contactcollector. Disabled by configuration setting \"com.openexchange.contactcollector.enabled=false\"");
                registerPreferenceItems();
                return;
            }
        }
        /*
         * Initialize service registry with available services
         */
        final ContactCollectorActivator activator = this;
        final ServiceRegistry serviceRegistry = new ServiceRegistry() {

            @Override
            public <S> S getOptionalService(final Class<? extends S> clazz) {
                return activator.getOptionalService(clazz);
            }

            @Override
            public <S> S getService(final Class<? extends S> clazz) {
                return activator.getService(clazz);
            }

            @Override
            public <S> void addService(final Class<? extends S> clazz, final S service) {
                activator.addServiceAlt(clazz, service);
            }

            @Override
            public void removeService(final Class<?> clazz) {
                activator.removeService(clazz);
            }
        };
        CCServiceRegistry.SERVICE_REGISTRY.set(serviceRegistry);
        /*
         * Initialize service
         */
        final ContactCollectorServiceImpl collectorInstance = new ContactCollectorServiceImpl();
        collectorInstance.start();
        this.collectorInstance = collectorInstance;
        /*
         * Register all
         */
        registerService(LoginHandlerService.class, new ContactCollectorFolderCreator());
        registerService(ContactCollectorService.class, collectorInstance);
        registerPreferenceItems();
    }

    /** Registers PreferencesItemService instances */
    private void registerPreferenceItems() {
        registerService(PreferencesItemService.class, new ContactCollectFolder());
        registerService(PreferencesItemService.class, new ContactCollectEnabled());
        registerService(PreferencesItemService.class, new ContactCollectOnMailAccess());
        registerService(PreferencesItemService.class, new ContactCollectOnMailTransport());
        registerService(PreferencesItemService.class, new ContactCollectFolderDeleteDenied());
    }

    @Override
    public void stopBundle() throws Exception {
        /*
         * Unregister all
         */
        cleanUp();
        /*
         * Stop service
         */
        final ContactCollectorServiceImpl collectorInstance = this.collectorInstance;
        if (null != collectorInstance) {
            collectorInstance.stop();
            this.collectorInstance = null;
        }
        /*
         * Clear service registry
         */
        CCServiceRegistry.SERVICE_REGISTRY.set(null);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ContextService.class, UserService.class, UserConfigurationService.class, ContactService.class,
            ThreadPoolService.class, DatabaseService.class, ConfigurationService.class };
    }

}
