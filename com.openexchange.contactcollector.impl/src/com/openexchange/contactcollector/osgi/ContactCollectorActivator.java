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

package com.openexchange.contactcollector.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.Constants;
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
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.osgi.HousekeepingActivator;
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
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ContextService.class, UserService.class, UserConfigurationService.class, ContactService.class,
            ThreadPoolService.class, DatabaseService.class, ConfigurationService.class };
    }

    @Override
    public void startBundle() throws Exception {
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
        Dictionary<String, Object> props = new Hashtable<String, Object>(2);
        props.put(Constants.SERVICE_RANKING, ContactCollectorServiceImpl.RANKING); //Default

        // Register services
        registerService(LoginHandlerService.class, new ContactCollectorFolderCreator(this));
        registerService(ContactCollectorService.class, collectorInstance, props);
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
    public void stopBundle() throws Exception {
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
