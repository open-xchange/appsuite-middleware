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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
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
import com.openexchange.secret.recovery.SecretConsistencyCheck;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.server.osgiservice.Whiteboard;
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
import com.openexchange.userconf.UserConfigurationService;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class DiscoveryActivator implements BundleActivator {

    private OSGiSubscriptionSourceCollector collector;

    private ServiceRegistration<SubscriptionSourceDiscoveryService> discoveryRegistration;

    private ServiceRegistration<SubscriptionExecutionService> executionRegistration;

    private WhiteboardContextService contextService;

    private WhiteboardGenericConfigurationStorageService genconfStorage;

    private Whiteboard whiteboard;

    private ServiceRegistration<FolderUpdaterRegistry> folderUpdaterRegistryRegistration;

    @Override
    public void start(final BundleContext context) throws Exception {
        whiteboard = new Whiteboard(context);
        collector = new OSGiSubscriptionSourceCollector(context);
        contextService = new WhiteboardContextService(context);
        final UserService users = whiteboard.getService(UserService.class);
        final UserConfigurationService userConfigs = whiteboard.getService(UserConfigurationService.class);
        final InfostoreFacade infostore = whiteboard.getService(InfostoreFacade.class);
        final FolderService folders = whiteboard.getService(FolderService.class);

        final Dictionary<String, Object> discoveryDict = new Hashtable<String, Object>();
        discoveryDict.put(Constants.SERVICE_RANKING, Integer.valueOf(256));

        final OSGiSubscriptionSourceDiscoveryCollector discoveryCollector = new OSGiSubscriptionSourceDiscoveryCollector(context);
        discoveryCollector.addSubscriptionSourceDiscoveryService(collector);

        discoveryRegistration =
            context.registerService(SubscriptionSourceDiscoveryService.class, discoveryCollector, discoveryDict);

        final List<FolderUpdaterService<?>> folderUpdaters = new ArrayList<FolderUpdaterService<?>>(5);
        folderUpdaters.add(new StrategyFolderUpdaterService<Contact>(new ContactFolderUpdaterStrategy()));
        folderUpdaters.add(new StrategyFolderUpdaterService<Contact>(new ContactFolderMultipleUpdaterStrategy(), true));
        folderUpdaters.add(new StrategyFolderUpdaterService<CalendarDataObject>(new CalendarFolderUpdaterStrategy()));
        folderUpdaters.add(new StrategyFolderUpdaterService<Task>(new TaskFolderUpdaterStrategy()));
        folderUpdaters.add(new StrategyFolderUpdaterService<DocumentMetadataHolder>(new DocumentMetadataHolderFolderUpdaterStrategy(
            users,
            userConfigs,
            infostore)));

        final SubscriptionExecutionServiceImpl executor = new SubscriptionExecutionServiceImpl(collector, folderUpdaters, contextService);
        executionRegistration = context.registerService(SubscriptionExecutionService.class, executor, null);

        folderUpdaterRegistryRegistration = context.registerService(FolderUpdaterRegistry.class, executor, null);


        final DBProvider provider = whiteboard.getService(DBProvider.class);
        genconfStorage = new WhiteboardGenericConfigurationStorageService(context);
        final SubscriptionSQLStorage storage = new SubscriptionSQLStorage(provider, genconfStorage, discoveryCollector);

        AbstractSubscribeService.STORAGE = storage;

        AbstractSubscribeService.CRYPTO = whiteboard.getService(CryptoService.class);
        AbstractSubscribeService.FOLDERS = folders;

        final SubscriptionUserDeleteListener listener = new SubscriptionUserDeleteListener();
        listener.setStorageService(genconfStorage);
        listener.setDiscoveryService(discoveryCollector);

        context.registerService(DeleteListener.class.getName(), listener, null);

        final SubscriptionSecretHandling secretHandling = new SubscriptionSecretHandling(discoveryCollector);
        context.registerService(SecretConsistencyCheck.class.getName(), secretHandling, null);
        context.registerService(SecretMigrator.class.getName(), secretHandling, null);

    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        whiteboard.close();
        whiteboard = null;
        genconfStorage.close();
        genconfStorage = null;
        collector.close();
        collector = null;
        contextService.close();
        contextService = null;
        discoveryRegistration.unregister();
        discoveryRegistration = null;
        executionRegistration.unregister();
        executionRegistration = null;
        folderUpdaterRegistryRegistration.unregister();
        folderUpdaterRegistryRegistration = null;
    }

}
