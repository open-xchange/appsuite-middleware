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

package com.openexchange.messaging.twitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.twitter.osgi.Services;
import com.openexchange.messaging.twitter.session.TwitterAccessRegistry;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.secret.SecretService;
import com.openexchange.session.Session;
import com.openexchange.twitter.Paging;
import com.openexchange.twitter.TwitterAccess;
import com.openexchange.twitter.TwitterService;

/**
 * {@link AbstractTwitterMessagingAccess} - Generic access to twitter.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractTwitterMessagingAccess {

    protected final MessagingAccount account;
    protected final TwitterService twitterService;
    protected final TwitterAccess twitterAccess;
    protected final Session session;
    protected final String secret;
    protected boolean connected;

    /**
     * Initializes a new {@link AbstractTwitterMessagingAccess}.
     *
     * @throws OXException If initialization fails
     */
    protected AbstractTwitterMessagingAccess(final MessagingAccount account, final Session session) throws OXException {
        super();
        this.session = session;
        this.account = account;
        twitterService = Services.getService(TwitterService.class);
        secret = Services.getService(SecretService.class).getSecret(session);
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        final int accountId = account.getId();
        TwitterAccess tmp = TwitterAccessRegistry.getInstance().getAccess(contextId, userId, accountId);
        if (null == tmp) {
            try {
                final Map<String, Object> configuration = account.getConfiguration();
                /*
                 * The OAuth twitter access
                 */
                TwitterAccess newTwitterAccess;
                /*
                 * Get associated Twitter OAuth account
                 */
                final OAuthAccount oAuthAccount;
                {
                    final OAuthService oAuthService = Services.getService(OAuthService.class);
                    /*
                     * Check presence of TwitterConstants.TWITTER_OAUTH_ACCOUNT
                     */
                    Integer oAuthAccountId = (Integer) configuration.get(TwitterConstants.TWITTER_OAUTH_ACCOUNT);
                    if (null == oAuthAccountId) {
                        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
                        if (null == lock) {
                            lock = Session.EMPTY_LOCK;
                        }
                        lock.lock();
                        try {
                            oAuthAccountId = (Integer) configuration.get(TwitterConstants.TWITTER_OAUTH_ACCOUNT);
                            if (null == oAuthAccountId) {
                                final String token = (String) configuration.get(TwitterConstants.TWITTER_TOKEN);
                                final String tokenSecret = (String) configuration.get(TwitterConstants.TWITTER_TOKEN_SECRET);
                                if ((null == token || null == tokenSecret)) {
                                    throw TwitterMessagingExceptionCodes.INVALID_ACCOUNT.create(new Object[0]);
                                }
                                final Map<String, Object> arguments = new HashMap<String, Object>(3);
                                arguments.put(OAuthConstants.ARGUMENT_DISPLAY_NAME, account.getDisplayName());
                                arguments.put(OAuthConstants.ARGUMENT_TOKEN, token);
                                arguments.put(OAuthConstants.ARGUMENT_SECRET, tokenSecret);
                                arguments.put(OAuthConstants.ARGUMENT_SESSION, session);
                                OAuthScopeRegistry scopeRegistry = Services.getService(OAuthScopeRegistry.class);
                                Set<OAuthScope> scopes = scopeRegistry.getAvailableScopes(KnownApi.TWITTER);
                                oAuthAccount = oAuthService.createAccount(session, "com.openexchange.oauth.twitter", scopes, arguments);
                                /*
                                 * Write to configuration
                                 */
                                configuration.put(TwitterConstants.TWITTER_OAUTH_ACCOUNT, Integer.valueOf(oAuthAccount.getId()));
                                final MessagingAccountManager accountManager = account.getMessagingService().getAccountManager();
                                accountManager.updateAccount(account, session);
                            } else {
                                oAuthAccount = oAuthService.getAccount(session, oAuthAccountId.intValue());
                            }
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        oAuthAccount = oAuthService.getAccount(session, oAuthAccountId.intValue());
                    }
                }
                newTwitterAccess = twitterService.getOAuthTwitterAccess(oAuthAccount.getToken(), oAuthAccount.getSecret());
                /*
                 * Add twitter access to registry
                 */
                tmp = TwitterAccessRegistry.getInstance().addAccess(contextId, userId, accountId, newTwitterAccess);
                if (null == tmp) {
                    tmp = newTwitterAccess;
                    testOAuthTwitterAccess(tmp);
                }
            } catch (OXException e) {
                throw e;
            }
        }
        twitterAccess = tmp;
    }

    private boolean testOAuthTwitterAccess(final TwitterAccess newTwitterAccess) {
        try {
            /*
             * Test twitter access
             */
            final Paging paging = twitterService.newPaging();
            paging.setCount(1);
            newTwitterAccess.getFriendsTimeline(paging);
            return true;
        } catch (OXException e) {
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractTwitterMessagingAccess.class);
            logger.debug("", e);
            return false;
        }
    }

    /**
     * Pings twitter access.
     *
     * @return <code>true</code> if ping was successful; otherwise <code>false</code>
     */
    public boolean ping() {
        try {
            final Paging paging = twitterService.newPaging();
            paging.count(1);
            twitterAccess.getFriendsTimeline(paging);
            return true;
        } catch (OXException e) {
            return false;
        }
    }

}
