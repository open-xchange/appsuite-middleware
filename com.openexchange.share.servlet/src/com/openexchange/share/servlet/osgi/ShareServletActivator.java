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

package com.openexchange.share.servlet.osgi;

import java.nio.charset.StandardCharsets;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.upgrade.SegmentedUpdateService;
import com.openexchange.guest.GuestService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.passwordchange.BasicPasswordChangeService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.servlet.handler.ShareHandler;
import com.openexchange.share.servlet.handler.WebUIShareHandler;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.user.UserService;

/**
 * {@link ShareServletActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareServletActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ShareService.class, UserService.class, ContextService.class, SessiondService.class, PasswordMechRegistry.class,
            ConfigurationService.class, ModuleSupport.class, GuestService.class, TranslatorFactory.class, BasicPasswordChangeService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(ShareServletActivator.class).info("starting bundle: \"com.openexchange.share.servlet\"");
        ShareServiceLookup.set(this);

        // Track share handlers
        RankingAwareNearRegistryServiceTracker<ShareHandler> shareHandlerRegistry = new RankingAwareNearRegistryServiceTracker<ShareHandler>(context, ShareHandler.class);
        rememberTracker(shareHandlerRegistry);
        trackService(HostnameService.class);
        trackService(DatabaseService.class);
        trackService(ShareNotificationService.class);
        trackService(SegmentedUpdateService.class);

        // Dependently registers Servlets
        {
            Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + HttpService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + DispatcherPrefixService.class.getName() + "))");
            ServletRegisterer registerer = new ServletRegisterer(shareHandlerRegistry, this, context);
            track(filter, registerer);
            registerService(Reloadable.class, registerer);
        }
        {
            byte[] hashSalt = getService(ConfigurationService.class).getProperty("com.openexchange.cookie.hash.salt", "replaceMe1234567890").getBytes(StandardCharsets.ISO_8859_1);
            Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + HttpService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + DispatcherPrefixService.class.getName() + "))");
            PasswordResetServletRegisterer registerer = new PasswordResetServletRegisterer(context, hashSalt);
            track(filter, registerer);
        }
        {
            Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + HttpService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + DispatcherPrefixService.class.getName() + "))");
            RedeemLoginLocationTokenServletRegisterer registerer = new RedeemLoginLocationTokenServletRegisterer(context);
            track(filter, registerer);
        }

        // Open trackers
        openTrackers();


        // Register default handlers
        {
            ShareHandler handler = new WebUIShareHandler();
            registerService(ShareHandler.class, handler, handler.getRanking());
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(ShareServletActivator.class).info("stopping bundle: \"com.openexchange.share.servlet\"");
        ShareServiceLookup.set(null);
        super.stopBundle();
    }

}
