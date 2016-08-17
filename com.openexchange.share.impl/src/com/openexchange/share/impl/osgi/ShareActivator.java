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

package com.openexchange.share.impl.osgi;

import java.util.concurrent.ExecutorService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.cluster.timer.ClusterTimerService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.cache.service.FolderCacheInvalidationService;
import com.openexchange.group.GroupService;
import com.openexchange.guest.GuestService;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.passwordmechs.PasswordMechFactory;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaService;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.impl.DefaultShareService;
import com.openexchange.share.impl.SharePasswordMech;
import com.openexchange.share.impl.cleanup.GuestCleaner;
import com.openexchange.share.impl.groupware.ModuleSupportImpl;
import com.openexchange.share.impl.groupware.ShareModuleMapping;
import com.openexchange.share.impl.quota.InviteGuestsQuotaProvider;
import com.openexchange.share.impl.quota.ShareLinksQuotaProvider;
import com.openexchange.templating.TemplateService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
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
        return new Class<?>[] {
            UserService.class, ContextService.class, TemplateService.class, ConfigurationService.class,
            DatabaseService.class, HtmlService.class, UserPermissionService.class, UserConfigurationService.class, ContactService.class,
            ContactUserStorage.class, ThreadPoolService.class, TimerService.class, ExecutorService.class, ConfigViewFactory.class,
            QuotaService.class, FolderCacheInvalidationService.class, ClusterTimerService.class, GuestService.class,
            DispatcherPrefixService.class, CapabilityService.class, GroupService.class, PasswordMechFactory.class };
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

            private volatile ServiceRegistration<ShareService> shareRegistration;

            private volatile ServiceRegistration<EventHandler> cleanUpRegistration;

            @Override
            public CryptoService addingService(ServiceReference<CryptoService> serviceReference) {
                String cryptKey = getService(ConfigurationService.class).getProperty("com.openexchange.share.cryptKey", "erE2e8OhAo71");
                CryptoService service = context.getService(serviceReference);

                PasswordMechFactory passwordMechFactory = getService(PasswordMechFactory.class);
                SharePasswordMech sharePasswordMech = new SharePasswordMech(service, cryptKey);
                passwordMechFactory.register(sharePasswordMech);
                shareRegistration = context.registerService(ShareService.class, shareService, null);
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
                    this.shareRegistration = null;
                    shareRegistration.unregister();
                }
                ServiceRegistration<EventHandler> cleanUpRegistration = this.cleanUpRegistration;
                if (null != cleanUpRegistration) {
                    this.cleanUpRegistration = null;
                    cleanUpRegistration.unregister();
                }

                context.ungetService(serviceReference);
            }
        });

        registerService(ModuleSupport.class, new ModuleSupportImpl(this));
        registerService(QuotaProvider.class, new ShareLinksQuotaProvider(this));
        registerService(QuotaProvider.class, new InviteGuestsQuotaProvider(this));

        trackService(ModuleSupport.class);
        track(ManagementService.class, new ManagementServiceTracker(context, shareService));
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
