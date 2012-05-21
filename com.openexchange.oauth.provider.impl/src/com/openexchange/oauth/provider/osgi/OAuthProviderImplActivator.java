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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.osgi;

import net.oauth.OAuthServiceProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.groupware.OAuth2ProviderCreateTableService;
import com.openexchange.oauth.provider.groupware.OAuth2ProviderCreateTableTask;
import com.openexchange.oauth.provider.groupware.OAuthProviderCreateTableService;
import com.openexchange.oauth.provider.groupware.OAuthProviderCreateTableTask;
import com.openexchange.oauth.provider.groupware.OAuthProviderDeleteListener;
import com.openexchange.oauth.provider.internal.DatabaseOAuthProviderService;
import com.openexchange.oauth.provider.internal.OAuthProviderServiceLookup;
import com.openexchange.oauth.provider.servlets.AccessTokenServlet;
import com.openexchange.oauth.provider.servlets.AccessTokenServlet2;
import com.openexchange.oauth.provider.servlets.AuthorizationServlet;
import com.openexchange.oauth.provider.servlets.AuthorizationServlet2;
import com.openexchange.oauth.provider.servlets.EchoServlet;
import com.openexchange.oauth.provider.servlets.RequestTokenServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.servlet.http.HTTPServletRegistration;

/**
 * {@link OAuthProviderImplActivator} - The activator for OAuth provider implementation bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OAuthProviderImplActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link OAuthProviderImplActivator}.
     */
    public OAuthProviderImplActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        new CheckConfigDBTables(getService(DatabaseService.class)).checkTables();
        OAuthProviderServiceLookup.set(this);
        /*
         * Register OAuth provider service
         */
        final DatabaseOAuthProviderService providerService = new DatabaseOAuthProviderService(this);
        registerService(OAuthProviderService.class, providerService);
        addService(OAuthProviderService.class, providerService);
        /*
         * Service trackers
         */
        final OAuthServiceProvider provider = providerService.getProvider();
        // OAuth v1
        rememberTracker(new HTTPServletRegistration(context, provider.accessTokenURL, new AccessTokenServlet()));
        rememberTracker(new HTTPServletRegistration(context, provider.userAuthorizationURL, new AuthorizationServlet()));
        rememberTracker(new HTTPServletRegistration(context, "/oauth/echo", new EchoServlet()));
        rememberTracker(new HTTPServletRegistration(context, provider.requestTokenURL, new RequestTokenServlet()));
        // OAuth v2
        rememberTracker(new HTTPServletRegistration(context, provider.accessTokenURL+"/v2", new AccessTokenServlet2()));
        rememberTracker(new HTTPServletRegistration(context, provider.userAuthorizationURL+"/v2", new AuthorizationServlet2()));
        openTrackers();
        /*
         * Register update task, create table job and delete listener
         */
        {
            registerService(CreateTableService.class, new OAuthProviderCreateTableService());
            registerService(CreateTableService.class, new OAuth2ProviderCreateTableService());
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new OAuthProviderCreateTableTask(), new OAuth2ProviderCreateTableTask()));
            registerService(DeleteListener.class, new OAuthProviderDeleteListener());
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        OAuthProviderServiceLookup.set(null);
        super.stopBundle();
    }

}
