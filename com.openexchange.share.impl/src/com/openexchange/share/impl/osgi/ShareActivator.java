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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.impl.osgi;

import java.sql.Connection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.modules.Module;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleHandler;
import com.openexchange.share.groupware.ModuleHandlerProvider;
import com.openexchange.share.groupware.ShareTargetDiff;
import com.openexchange.share.impl.DefaultShareService;
import com.openexchange.share.impl.ShareCryptoServiceImpl;
import com.openexchange.share.impl.groupware.AbstractModuleHandler;
import com.openexchange.share.impl.groupware.FileStorageHandler;
import com.openexchange.share.impl.groupware.FileStorageShareCleanUp;
import com.openexchange.share.impl.groupware.ModuleHandlerProviderImpl;
import com.openexchange.share.impl.notification.DefaultNotificationService;
import com.openexchange.share.impl.notification.mail.MailNotificationHandler;
import com.openexchange.share.notification.ShareNotificationHandler;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ShareActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ShareActivator}.
     */
    public ShareActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, ContextService.class, TemplateService.class,
            ShareStorage.class, ConfigurationService.class, DatabaseService.class, HtmlService.class,
            UserPermissionService.class, UserConfigurationService.class, ContactService.class, ContactUserStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ShareActivator.class);
        logger.info("starting bundle: \"com.openexchange.share.impl\"");
        /*
         * register share crypto service based on underyling crypto service
         */
        final DefaultShareService shareService = new DefaultShareService(this);
        final BundleContext context = this.context;
        track(CryptoService.class, new ServiceTrackerCustomizer<CryptoService, CryptoService>() {

            private volatile ServiceRegistration<ShareCryptoService> cryptoRegistration;
            private volatile ServiceRegistration<ShareService> shareRegistration;
            private volatile ServiceRegistration<EventHandler> cleanUpRegistration;

            @Override
            public CryptoService addingService(ServiceReference<CryptoService> serviceReference) {
                String cryptKey = getService(ConfigurationService.class).getProperty("com.openexchange.share.cryptKey", "erE2e8OhAo71");
                CryptoService service = context.getService(serviceReference);
                ShareCryptoServiceImpl shareCryptoService = new ShareCryptoServiceImpl(service, cryptKey);
                addService(ShareCryptoService.class, shareCryptoService);
                cryptoRegistration = context.registerService(ShareCryptoService.class, shareCryptoService, null);
                shareRegistration = context.registerService(ShareService.class, shareService, null);

                /*
                 * Event handler for deleted files
                 */
                Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
                serviceProperties.put(EventConstants.EVENT_TOPIC, new String[] { FileStorageEventConstants.DELETE_TOPIC });
                cleanUpRegistration = context.registerService(EventHandler.class, new FileStorageShareCleanUp(shareService), serviceProperties);
                return service;
            }

            @Override
            public void modifiedService(ServiceReference<CryptoService> serviceReference, CryptoService service) {
                // nothing to do
            }

            @Override
            public void removedService(ServiceReference<CryptoService> serviceReference, CryptoService service) {
                ServiceRegistration<ShareService> shareRegistration = this.shareRegistration;
                if (null != shareRegistration) {
                    shareRegistration.unregister();
                    this.shareRegistration = null;
                }
                ServiceRegistration<ShareCryptoService> cryptoRegistration = this.cryptoRegistration;
                if (null != cryptoRegistration) {
                    cryptoRegistration.unregister();
                    this.cryptoRegistration = null;
                }
                ServiceRegistration<EventHandler> cleanUpRegistration = this.cleanUpRegistration;
                if (null != cleanUpRegistration) {
                    cleanUpRegistration.unregister();
                    this.cleanUpRegistration = null;
                }

                context.ungetService(serviceReference);
            }
        });

        // Initialize share notification service
        final DefaultNotificationService defaultNotificationService = new DefaultNotificationService();

        // Add in-place handlers
        defaultNotificationService.add(new MailNotificationHandler(this));

        // track additional share notification handlers
        track(ShareNotificationHandler.class, new ServiceTrackerCustomizer<ShareNotificationHandler, ShareNotificationHandler>() {

            @Override
            public ShareNotificationHandler addingService(ServiceReference<ShareNotificationHandler> reference) {
                ShareNotificationHandler handler = context.getService(reference);
                defaultNotificationService.add(handler);
                return handler;
            }

            @Override
            public void modifiedService(ServiceReference<ShareNotificationHandler> reference, ShareNotificationHandler service) {
                // Ignore
            }

            @Override
            public void removedService(ServiceReference<ShareNotificationHandler> reference, ShareNotificationHandler service) {
                defaultNotificationService.remove(service);
                context.ungetService(reference);
            }
        });

        ModuleHandlerProviderImpl moduleHandlerProvider = new ModuleHandlerProviderImpl();
        moduleHandlerProvider.put(new FileStorageHandler(this));
        moduleHandlerProvider.put(newFolderUpdater(Module.CALENDAR));
        moduleHandlerProvider.put(newFolderUpdater(Module.CONTACTS));
        moduleHandlerProvider.put(newFolderUpdater(Module.TASK));
        registerService(ModuleHandlerProvider.class, moduleHandlerProvider);
        registerService(ShareNotificationService.class, defaultNotificationService);

        trackService(ModuleHandlerProvider.class);
        track(ManagementService.class, new ManagementServiceTracker(context, shareService));
        trackService(IDBasedFileAccessFactory.class);
        trackService(FolderService.class);
        trackService(TranslatorFactory.class);
        openTrackers();
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ShareActivator.class);
        logger.info("stopping bundle: \"com.openexchange.share.impl\"");
        super.stopBundle();
    }

    private ModuleHandler newFolderUpdater(final Module module) {
        return new AbstractModuleHandler(this) {
            @Override
            public int getModule() {
                return module.getFolderConstant();
            }

            @Override
            protected String getItemTitle(String folder, String item, Session session) throws OXException {
                throw ShareExceptionCodes.SHARING_ITEMS_NOT_SUPPORTED.create(module.getName());
            }

            @Override
            public void updateObjects(ShareTargetDiff targetDiff, List<InternalRecipient> finalRecipients, Session session, Connection writeCon) throws OXException {
                throw ShareExceptionCodes.SHARING_ITEMS_NOT_SUPPORTED.create(module.getName());
            }
        };
    }

}
