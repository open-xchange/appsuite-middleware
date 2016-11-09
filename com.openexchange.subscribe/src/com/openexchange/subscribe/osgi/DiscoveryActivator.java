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


package com.openexchange.subscribe.osgi;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.Constants;
import com.openexchange.context.osgi.WhiteboardContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.datatypes.genericonf.storage.osgi.tools.WhiteboardGenericConfigurationStorageService;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.database.SubscriptionUserDeleteListener;
import com.openexchange.subscribe.helpers.DocumentMetadataHolder;
import com.openexchange.subscribe.internal.CalendarFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.ContactFolderMultipleUpdaterStrategy;
import com.openexchange.subscribe.internal.ContactFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.DocumentMetadataHolderFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.StrategyFolderUpdaterService;
import com.openexchange.subscribe.internal.SubscriptionExecutionServiceImpl;
import com.openexchange.subscribe.internal.TaskFolderUpdaterStrategy;
import com.openexchange.subscribe.secret.SubscriptionSecretHandling;
import com.openexchange.subscribe.sql.SubscriptionSQLStorage;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class DiscoveryActivator extends HousekeepingActivator {

    private volatile OSGiSubscriptionSourceCollector collector;
    private volatile WhiteboardContextService contextService;
    private volatile WhiteboardGenericConfigurationStorageService genconfStorage;

    @Override
    public void startBundle() throws Exception {
        final OSGiSubscriptionSourceCollector collector = new OSGiSubscriptionSourceCollector(context);
        this.collector = collector;
        final WhiteboardContextService contextService = new WhiteboardContextService(context);
        this.contextService = contextService;
        final UserService users = getService(UserService.class);
        final UserPermissionService userPermissions = getService(UserPermissionService.class);
        final InfostoreFacade infostore = getService(InfostoreFacade.class);
        final FolderService folders = getService(FolderService.class);

        final Dictionary<String, Object> discoveryDict = new Hashtable<String, Object>();
        discoveryDict.put(Constants.SERVICE_RANKING, Integer.valueOf(256));

        final OSGiSubscriptionSourceDiscoveryCollector discoveryCollector = new OSGiSubscriptionSourceDiscoveryCollector(context);
        discoveryCollector.addSubscriptionSourceDiscoveryService(collector);
        AutoUpdateActivator.setCollector(discoveryCollector);
        registerService(SubscriptionSourceDiscoveryService.class, discoveryCollector, discoveryDict);

        final List<FolderUpdaterService<?>> folderUpdaters = new ArrayList<FolderUpdaterService<?>>(5);
        folderUpdaters.add(new StrategyFolderUpdaterService<Contact>(new ContactFolderUpdaterStrategy()));
        folderUpdaters.add(new StrategyFolderUpdaterService<Contact>(new ContactFolderMultipleUpdaterStrategy(), true));
        folderUpdaters.add(new StrategyFolderUpdaterService<CalendarDataObject>(new CalendarFolderUpdaterStrategy()));
        folderUpdaters.add(new StrategyFolderUpdaterService<Task>(new TaskFolderUpdaterStrategy()));
        folderUpdaters.add(new StrategyFolderUpdaterService<DocumentMetadataHolder>(new DocumentMetadataHolderFolderUpdaterStrategy(
            users,
            userPermissions,
            infostore)));

        final SubscriptionExecutionServiceImpl executor = new SubscriptionExecutionServiceImpl(collector, folderUpdaters, contextService);
        registerService(SubscriptionExecutionService.class, executor);
        registerService(FolderUpdaterRegistry.class, executor);

        AutoUpdateActivator.setExecutor(executor);

        final DBProvider provider = getService(DBProvider.class);
        final WhiteboardGenericConfigurationStorageService genconfStorage = new WhiteboardGenericConfigurationStorageService(context);
        this.genconfStorage = genconfStorage;
        final SubscriptionSQLStorage storage = new SubscriptionSQLStorage(provider, genconfStorage, discoveryCollector);

        AbstractSubscribeService.STORAGE.set(storage);

        AbstractSubscribeService.ENCRYPTION_FACTORY.set(getService(SecretEncryptionFactoryService.class));
        AbstractSubscribeService.CRYPTO_SERVICE.set(getService(CryptoService.class));
        AbstractSubscribeService.FOLDERS.set(folders);
        AbstractSubscribeService.USER_PERMISSIONS.set(userPermissions);

        final SubscriptionUserDeleteListener listener = new SubscriptionUserDeleteListener();
        listener.setStorageService(genconfStorage);
        listener.setDiscoveryService(discoveryCollector);

        registerService(DeleteListener.class, listener);

        final SubscriptionSecretHandling secretHandling = new SubscriptionSecretHandling(discoveryCollector);
        registerService(EncryptedItemDetectorService.class, secretHandling);
        registerService(EncryptedItemCleanUpService.class, secretHandling);
        registerService(SecretMigrator.class, secretHandling);
    }

    @Override
    public void stopBundle() throws Exception {
        final WhiteboardGenericConfigurationStorageService genconfStorage = this.genconfStorage;
        if (null != genconfStorage) {
            genconfStorage.close();
            this.genconfStorage = null;
        }
        final OSGiSubscriptionSourceCollector collector = this.collector;
        if (null != collector) {
            collector.close();
            this.collector = null;
        }
        final WhiteboardContextService contextService = this.contextService;
        if (null != contextService) {
            contextService.close();
            this.contextService = null;
        }
        unregisterServices();
        AbstractSubscribeService.STORAGE.set(null);
        AbstractSubscribeService.ENCRYPTION_FACTORY.set(null);
        AbstractSubscribeService.CRYPTO_SERVICE.set(null);
        AbstractSubscribeService.FOLDERS.set(null);
        AbstractSubscribeService.USER_PERMISSIONS.set(null);
        AutoUpdateActivator.setCollector(null);
        AutoUpdateActivator.setExecutor(null);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { UserService.class, UserPermissionService.class, InfostoreFacade.class, FolderService.class, DBProvider.class, SecretEncryptionFactoryService.class, CryptoService.class };
    }

}
