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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.Constants;
import org.osgi.service.http.HttpService;
import com.openexchange.ajax.osgi.AbstractServletActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareService;
import com.openexchange.share.servlet.handler.RedirectingShareHandler;
import com.openexchange.share.servlet.handler.ShareHandler;
import com.openexchange.share.servlet.internal.ShareLoginConfiguration;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.share.servlet.internal.ShareServlet;
import com.openexchange.user.UserService;

/**
 * {@link ShareServletActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareServletActivator extends AbstractServletActivator {

    private static final String ALIAS = "/ajax/share";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ShareService.class, UserService.class, ContextService.class, DispatcherPrefixService.class,
            HttpService.class, SessiondService.class, ShareCryptoService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(ShareServletActivator.class).info("starting bundle: \"com.openexchange.share.servlet\"");
        ShareServiceLookup.set(this);
        /*
         * track share handlers
         */
        RankingAwareNearRegistryServiceTracker<ShareHandler> shareHandlerRegistry = new RankingAwareNearRegistryServiceTracker<ShareHandler>(context, ShareHandler.class);
        rememberTracker(shareHandlerRegistry);
        openTrackers();
        /*
         * register default handlers
         */
        {
            ShareLoginConfiguration loginConfig = new ShareLoginConfiguration(getService(ConfigurationService.class));
            RedirectingShareHandler.setShareLoginConfiguration(loginConfig);
            ShareHandler handler = new RedirectingShareHandler();
            Dictionary<String, Object> props = new Hashtable<String, Object>(2);
            props.put(Constants.SERVICE_RANKING, Integer.valueOf(handler.getRanking()));
            registerService(ShareHandler.class, handler);
        }
        /*
         * register servlet
         */
        super.registerServlet(ALIAS, new ShareServlet(shareHandlerRegistry), getService(HttpService.class));
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(ShareServletActivator.class).info("stopping bundle: \"com.openexchange.share.servlet\"");
        RedirectingShareHandler.setShareLoginConfiguration(null);
        ShareServiceLookup.set(null);
        super.stopBundle();
    }

}
