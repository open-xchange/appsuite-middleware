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
