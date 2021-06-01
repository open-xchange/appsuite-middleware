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

package com.openexchange.share.impl.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.cache.service.FolderCacheInvalidationService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.guest.GuestService;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.ShareService;
import com.openexchange.share.core.ModuleAdjuster;
import com.openexchange.share.core.ModuleHandler;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.spi.FolderHandlerModuleExtension;
import com.openexchange.share.impl.DefaultShareService;
import com.openexchange.share.impl.SharePasswordMech;
import com.openexchange.share.impl.ShareRMIServiceImpl;
import com.openexchange.share.impl.cleanup.GuestCleaner;
import com.openexchange.share.impl.groupware.FileStorageHandler;
import com.openexchange.share.impl.groupware.MailModuleAdjuster;
import com.openexchange.share.impl.groupware.ModuleExtensionRegistry;
import com.openexchange.share.impl.groupware.ModuleSupportImpl;
import com.openexchange.share.impl.groupware.ShareModuleMapping;
import com.openexchange.share.impl.quota.InviteGuestsQuotaProvider;
import com.openexchange.share.impl.quota.ShareLinksQuotaProvider;
import com.openexchange.share.impl.subscription.ContextInternalSubscriptionProvider;
import com.openexchange.share.impl.subscription.ShareSubscriptionRegistryImpl;
import com.openexchange.share.impl.xctx.XctxSessionCache;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.share.subscription.ShareSubscriptionRegistry;
import com.openexchange.share.subscription.XctxSessionManager;
import com.openexchange.templating.TemplateService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.oxfolder.property.FolderSubscriptionHelper;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ShareActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareActivator extends HousekeepingActivator {

    private volatile GuestCleaner guestCleaner;

    /**
     * Initializes a new {@link ShareActivator}.
     */
    public ShareActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { //@formatter:off
            UserService.class, ContextService.class, TemplateService.class, ConfigurationService.class, DatabaseService.class,
            HtmlService.class, UserPermissionService.class, UserConfigurationService.class, ContactService.class, ContactUserStorage.class,
            ThreadPoolService.class, TimerService.class, ExecutorService.class, ConfigViewFactory.class, QuotaService.class,
            FolderCacheInvalidationService.class, GuestService.class, DispatcherPrefixService.class,  CapabilityService.class,
            GroupService.class, PasswordMechRegistry.class, UserAliasStorage.class, SessiondService.class, FolderSubscriptionHelper.class,
            DatabaseCleanUpService.class }; //@formatter:on
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ShareActivator.class);
        logger.info("starting bundle: \"com.openexchange.share.impl\"");
        /*
         * register share crypto service based on underyling crypto service
         */
        GuestCleaner guestCleaner = new GuestCleaner(this);
        this.guestCleaner = guestCleaner;
        final DefaultShareService shareService = new DefaultShareService(this, guestCleaner);
        final BundleContext context = this.context;
        track(CryptoService.class, new ServiceTrackerCustomizer<CryptoService, CryptoService>() {

            private ServiceRegistration<ShareService> shareRegistration;

            @Override
            public synchronized CryptoService addingService(ServiceReference<CryptoService> serviceReference) {
                String cryptKey = getService(ConfigurationService.class).getProperty("com.openexchange.share.cryptKey", "erE2e8OhAo71");
                CryptoService service = context.getService(serviceReference);

                SharePasswordMech sharePasswordMech = new SharePasswordMech(service, cryptKey);
                context.registerService(PasswordMech.class, sharePasswordMech, null);
                shareRegistration = context.registerService(ShareService.class, shareService, null);
                return service;
            }

            @Override
            public void modifiedService(ServiceReference<CryptoService> serviceReference, CryptoService service) {
                // nothing to do
            }

            @Override
            public synchronized void removedService(ServiceReference<CryptoService> serviceReference, CryptoService service) {
                ServiceRegistration<ShareService> shareRegistration = this.shareRegistration;
                if (null != shareRegistration) {
                    this.shareRegistration = null;
                    shareRegistration.unregister();
                }

                context.ungetService(serviceReference);
            }
        });

        AccessibleModulesExtensionTracker accessibleModulesTracker = new AccessibleModulesExtensionTracker(context);
        rememberTracker(accessibleModulesTracker);
        /*
         * track module handlers and -adjusters & register default implementations
         */
        ServiceSet<ModuleHandler> moduleHandlers = new ServiceSet<ModuleHandler>();
        ModuleExtensionRegistry<ModuleHandler> handlerRegistry = new ModuleExtensionRegistry<ModuleHandler>(moduleHandlers);
        track(ModuleHandler.class, moduleHandlers);
        ServiceSet<ModuleAdjuster> moduleAdjusters = new ServiceSet<ModuleAdjuster>();
        ModuleExtensionRegistry<ModuleAdjuster> adjusterRegistry = new ModuleExtensionRegistry<ModuleAdjuster>(moduleAdjusters);
        track(ModuleAdjuster.class, moduleAdjusters);
        ServiceSet<FolderHandlerModuleExtension> folderModuleHandlers = new ServiceSet<FolderHandlerModuleExtension>();
        ModuleExtensionRegistry<FolderHandlerModuleExtension> folderHandlerRegistry = new ModuleExtensionRegistry<FolderHandlerModuleExtension>(folderModuleHandlers);
        track(FolderHandlerModuleExtension.class, folderModuleHandlers);
        registerService(ModuleHandler.class, new FileStorageHandler(this));
        registerService(ModuleAdjuster.class, new MailModuleAdjuster(this));

        registerService(ModuleSupport.class, new ModuleSupportImpl(this, folderHandlerRegistry, accessibleModulesTracker, handlerRegistry, adjusterRegistry));
        registerService(QuotaProvider.class, new ShareLinksQuotaProvider(this));
        registerService(QuotaProvider.class, new InviteGuestsQuotaProvider(this));
        {
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put("RMI_NAME", ShareRMIServiceImpl.RMI_NAME);
            registerService(Remote.class, new ShareRMIServiceImpl(shareService), serviceProperties);
        }

        ShareSubscriptionRegistryImpl linkService = new ShareSubscriptionRegistryImpl(context);
        rememberTracker(linkService);
        registerService(ShareSubscriptionRegistry.class, linkService);
        ShareSubscriptionRegistryImpl shareSubscriptionRegistry = new ShareSubscriptionRegistryImpl(context);
        rememberTracker(shareSubscriptionRegistry);
        registerService(ShareSubscriptionRegistry.class, shareSubscriptionRegistry);
        registerService(ShareSubscriptionProvider.class, new ContextInternalSubscriptionProvider(this));
        XctxSessionCache xctxSessionCache = new XctxSessionCache(this);
        registerService(EventHandler.class, xctxSessionCache, singletonDictionary(EventConstants.EVENT_TOPIC, new String[] {
            SessiondEventConstants.TOPIC_REMOVE_SESSION, SessiondEventConstants.TOPIC_REMOVE_CONTAINER }));
        registerService(XctxSessionManager.class, xctxSessionCache);

        trackService(ModuleSupport.class);
        trackService(IDBasedFileAccessFactory.class);
        trackService(FolderService.class);
        trackService(TranslatorFactory.class);
        openTrackers();

        //initialize share module mapping
        ShareModuleMapping.init(getService(ConfigurationService.class));
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ShareActivator.class);
        logger.info("stopping bundle: \"com.openexchange.share.impl\"");
        /*
         * stop any running guest cleanup activities
         */
        GuestCleaner guestCleaner = this.guestCleaner;
        if (null != guestCleaner) {
            guestCleaner.stop();
            this.guestCleaner = null;
        }
        super.stopBundle();
    }

}
