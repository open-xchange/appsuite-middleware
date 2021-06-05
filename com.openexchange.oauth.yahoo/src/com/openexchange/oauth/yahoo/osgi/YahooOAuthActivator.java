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

package com.openexchange.oauth.yahoo.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.oauth.yahoo.YahooOAuthScope;
import com.openexchange.oauth.yahoo.YahooService;
import com.openexchange.oauth.yahoo.access.YahooAccessEventHandler;
import com.openexchange.oauth.yahoo.internal.YahooOAuthServiceMetaData;
import com.openexchange.oauth.yahoo.internal.YahooServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link YahooOAuthActivator}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class YahooOAuthActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(YahooOAuthActivator.class);

    private OAuthService oauthService;
    private YahooOAuthServiceMetaData oAuthMetaData;

    /**
     * Initializes a new {@link YahooOAuthActivator}.
     */
    public YahooOAuthActivator() {
        super();
    }

    /** Gets OAuthService */
    public synchronized OAuthService getOauthService() {
        return oauthService;
    }

    /** Sets OAuthService */
    public synchronized void setOauthService(final OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    /** Gets OAuthServiceMetaDataYahooImpl */
    public synchronized YahooOAuthServiceMetaData getOAuthMetaData() {
        return oAuthMetaData;
    }

    /** Sets OAuthServiceMetaDataYahooImpl */
    public synchronized void setOAuthMetaData(final YahooOAuthServiceMetaData oauthMetaData) {
        this.oAuthMetaData = oauthMetaData;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, OAuthService.class, DeferringURLService.class, ThreadPoolService.class, OAuthScopeRegistry.class, OAuthAccessRegistryService.class, SSLSocketFactoryProvider.class, DispatcherPrefixService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Services.setServices(this);
        oauthService = getService(OAuthService.class);
        oAuthMetaData = new YahooOAuthServiceMetaData(this);
        registerService(OAuthServiceMetaData.class, oAuthMetaData);
        registerService(Reloadable.class, oAuthMetaData);
        LOG.info("OAuthServiceMetaData for Yahoo was started");

        final YahooService yahooService = new YahooServiceImpl(this);
        registerService(YahooService.class, yahooService);
        // Register the delete listener
        registerService(OAuthAccountDeleteListener.class, (OAuthAccountDeleteListener) yahooService);

        // Register the scope
        OAuthScopeRegistry scopeRegistry = getService(OAuthScopeRegistry.class);
        scopeRegistry.registerScopes(oAuthMetaData.getAPI(), YahooOAuthScope.values());

        /*
         * Register event handler
         */
        final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
        registerService(EventHandler.class, new YahooAccessEventHandler(), serviceProperties);

        // Register the update task
        // track(DatabaseService.class, new DatabaseUpdateTaskServiceTracker(context));

        LOG.info("YahooService was started.");
    }

}
