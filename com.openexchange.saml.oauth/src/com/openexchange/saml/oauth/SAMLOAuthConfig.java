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

package com.openexchange.saml.oauth;

import static com.openexchange.java.Autoboxing.I;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.UserAndContext;

/**
 * {@link SAMLOAuthConfig}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class SAMLOAuthConfig {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SAMLOAuthConfig.class);

    private static final String TOKEN_ENDPOINT_PROPERTY = "com.openexchange.saml.oauth.tokenEndpoint";
    private static final String CLIENT_ID_PROPERTY = "com.openexchange.saml.oauth.clientId";
    private static final String CLIENT_SECRET_PROPERTY = "com.openexchange.saml.oauth.clientSecret";
    private static final String SCOPE_PROPERTY = "com.openexchange.saml.oauth.scope";

    private static final Cache<UserAndContext, ImmutableReference<OAuthConfiguration>> CACHE_OAUTH_CONFIGS = CacheBuilder.newBuilder().maximumSize(262144).expireAfterAccess(30, TimeUnit.MINUTES).build();

    /**
     * Clears the cache.
     */
    public static void invalidateCache() {
        CACHE_OAUTH_CONFIGS.invalidateAll();
    }

    /**
     * Gets the OAuth configuration for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param configViewFactory The service to use
     * @return The OAuth configuration or <code>null</code> if OAuth is not configured for that user
     * @throws OXException If OAuth configuration cannot be returned
     */
    public static OAuthConfiguration getConfig(final int userId, final int contextId, final ConfigViewFactory configViewFactory) throws OXException {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        ImmutableReference<OAuthConfiguration> configRef = CACHE_OAUTH_CONFIGS.getIfPresent(key);
        if (null != configRef) {
            return configRef.getValue();
        }

        Callable<ImmutableReference<OAuthConfiguration>> loader = new Callable<ImmutableReference<OAuthConfiguration>>() {

            @Override
            public ImmutableReference<OAuthConfiguration> call() throws Exception {
                return new ImmutableReference<OAuthConfiguration>(doGetConfig(userId, contextId, configViewFactory));
            }
        };

        try {
            configRef = CACHE_OAUTH_CONFIGS.get(key, loader);
            return configRef.getValue();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw cause instanceof OXException ? (OXException) cause : new OXException(cause);
        }
    }

    static OAuthConfiguration doGetConfig(int userId, int contextId, ConfigViewFactory configViewFactory) throws OXException {
        if (null == configViewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = configViewFactory.getView(userId, contextId);

        String endpoint = ConfigViews.getNonEmptyPropertyFrom(TOKEN_ENDPOINT_PROPERTY, view);
        if (null == endpoint) {
            // No end-point configured
            LOG.debug("No token end-point configured for user {} in context {}", I(userId), I(contextId));
            return null;
        }

        String clientId = ConfigViews.getNonEmptyPropertyFrom(CLIENT_ID_PROPERTY, view);
        if (null == clientId) {
            // No client ID configured
            LOG.debug("No client identifier configured for user {} in context {}", I(userId), I(contextId));
        }

        String clientSecret = ConfigViews.getNonEmptyPropertyFrom(CLIENT_SECRET_PROPERTY, view);
        if (null == clientSecret) {
            // No client secret configured
            LOG.debug("No client secret configured for user {} in context {}", I(userId), I(contextId));
        }

        String scope = ConfigViews.getNonEmptyPropertyFrom(SCOPE_PROPERTY, view);
        if (null == clientSecret) {
            // No client secret configured
            LOG.debug("No scope configured for user {} in context {}", I(userId), I(contextId));
        }

        return new OAuthConfiguration(endpoint, clientId, clientSecret, scope);
    }

    /**
     * Checks whether necessary options are specified for given user that are required to let SAML OAuth work.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param configViewFactory The service to use
     * @return <code>true</code> if necessary options are available; otherwise <code>false</code>
     * @throws OXException If checking necessary options fails
     */
    public static boolean isConfigured(int userId, int contextId, ConfigViewFactory configViewFactory) throws OXException{
        OAuthConfiguration config = getConfig(userId, contextId, configViewFactory);
        return null != config;
    }

}
