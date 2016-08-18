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
import com.openexchange.oauth.API;
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
                                Set<OAuthScope> scopes = scopeRegistry.getAvailableScopes(API.TWITTER);
                                oAuthAccount = oAuthService.createAccount("com.openexchange.oauth.twitter", arguments, userId, contextId, scopes);
                                /*
                                 * Write to configuration
                                 */
                                configuration.put(TwitterConstants.TWITTER_OAUTH_ACCOUNT, Integer.valueOf(oAuthAccount.getId()));
                                final MessagingAccountManager accountManager = account.getMessagingService().getAccountManager();
                                accountManager.updateAccount(account, session);
                            } else {
                                oAuthAccount = oAuthService.getAccount(oAuthAccountId.intValue(), session, userId, contextId);
                            }
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        oAuthAccount = oAuthService.getAccount(oAuthAccountId.intValue(), session, userId, contextId);
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
            } catch (final OXException e) {
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
        } catch (final OXException e) {
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
        } catch (final OXException e) {
            return false;
        }
    }

}
