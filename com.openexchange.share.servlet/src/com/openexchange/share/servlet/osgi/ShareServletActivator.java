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

package com.openexchange.share.servlet.osgi;

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.guest.GuestService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.passwordchange.BasicPasswordChangeService;
import com.openexchange.passwordmechs.PasswordMechFactory;
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
            ShareService.class, UserService.class, ContextService.class, SessiondService.class, PasswordMechFactory.class,
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

        // Dependently registers Servlets
        {
            Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + HttpService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + DispatcherPrefixService.class.getName() + "))");
            ServletRegisterer registerer = new ServletRegisterer(shareHandlerRegistry, context);
            track(filter, registerer);
        }
        {
            byte[] hashSalt = getService(ConfigurationService.class).getProperty("com.openexchange.cookie.hash.salt", "replaceMe1234567890").getBytes();
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
