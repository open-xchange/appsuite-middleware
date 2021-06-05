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

package com.openexchange.contactcollector.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import org.osgi.framework.BundleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
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
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link BundleActivator Activator} for contact collector.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ContactCollectorActivator extends HousekeepingActivator {

    private ContactCollectorServiceImpl collectorInstance;

    /**
     * Initializes a new {@link ContactCollectorActivator}.
     */
    public ContactCollectorActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ContextService.class, UserService.class, UserConfigurationService.class, ContactService.class,
            ThreadPoolService.class, DatabaseService.class, ConfigurationService.class, UserPermissionService.class,
            ConfigViewFactory.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        // Check if disabled by configuration
        {
            final ConfigurationService cService = getService(ConfigurationService.class);
            if (!cService.getBoolProperty("com.openexchange.contactcollector.enabled", true)) {
                final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContactCollectorActivator.class);
                log.info("Canceled start-up of bundle: com.openexchange.contactcollector. Disabled by configuration setting \"com.openexchange.contactcollector.enabled=false\"");
                registerPreferenceItems();
                return;
            }
        }

        // Track other services
        trackService(ObjectUseCountService.class);
        trackService(UserAliasStorage.class);
        openTrackers();

        // Initialize service
        final ContactCollectorServiceImpl collectorInstance = new ContactCollectorServiceImpl(this);
        collectorInstance.start();
        this.collectorInstance = collectorInstance;
         //Default

        // Register services
        registerService(LoginHandlerService.class, new ContactCollectorFolderCreator(this));
        registerService(ContactCollectorService.class, collectorInstance, withRanking(ContactCollectorServiceImpl.RANKING));
        registerPreferenceItems();
    }

    /** Registers PreferencesItemService instances */
    private void registerPreferenceItems() {
        registerService(PreferencesItemService.class, new ContactCollectFolder());
        registerService(PreferencesItemService.class, new ContactCollectEnabled());
        registerService(PreferencesItemService.class, new ContactCollectOnMailAccess());
        registerService(PreferencesItemService.class, new ContactCollectOnMailTransport());
        registerService(PreferencesItemService.class, new ContactCollectFolderDeleteDenied(this));
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        /*
         * Stop service
         */
        ContactCollectorServiceImpl collectorInstance = this.collectorInstance;
        if (null != collectorInstance) {
            collectorInstance.stop();
            this.collectorInstance = null;
        }

        /*
         * Unregister all
         */
        super.stopBundle();
    }

}
