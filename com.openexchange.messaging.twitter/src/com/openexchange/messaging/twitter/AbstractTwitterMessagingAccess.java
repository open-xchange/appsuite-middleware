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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import java.util.Map;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.twitter.services.TwitterMessagingServiceRegistry;
import com.openexchange.messaging.twitter.session.TwitterAccessRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.twitter.Paging;
import com.openexchange.twitter.TwitterAccess;
import com.openexchange.twitter.TwitterAccessToken;
import com.openexchange.twitter.TwitterException;
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

    protected boolean connected;

    /**
     * Initializes a new {@link AbstractTwitterMessagingAccess}.
     * 
     * @throws MessagingException If initialization fails
     */
    protected AbstractTwitterMessagingAccess(final MessagingAccount account, final Session session) throws MessagingException {
        super();
        this.session = session;
        this.account = account;
        try {
            twitterService = TwitterMessagingServiceRegistry.getServiceRegistry().getService(TwitterService.class, true);
        } catch (final ServiceException e) {
            throw new MessagingException(e);
        }
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
                 * Check existence of access token in current configuration
                 */
                final String token = (String) configuration.get(TwitterConstants.TWITTER_TOKEN);
                final String tokenSecret = (String) configuration.get(TwitterConstants.TWITTER_TOKEN_SECRET);
                if ((null == token || null == tokenSecret) || !testOAuthTwitterAccess((newTwitterAccess =
                    twitterService.getOAuthTwitterAccess(token, tokenSecret)))) {
                    /*
                     * Request new access token and store in configuration
                     */
                    newTwitterAccess = newAccessToken();
                }
                /*
                 * Add twitter access to registry
                 */
                tmp = TwitterAccessRegistry.getInstance().addAccess(contextId, userId, accountId, newTwitterAccess);
                if (null == tmp) {
                    tmp = newTwitterAccess;
                }
            } catch (final TwitterException e) {
                throw new MessagingException(e);
            }
        }
        twitterAccess = tmp;
    }

    private TwitterAccess newAccessToken() throws MessagingException, TwitterException {
        final Map<String, Object> configuration = account.getConfiguration();
        final String login = (String) configuration.get(TwitterConstants.TWITTER_LOGIN);
        final String password = (String) configuration.get(TwitterConstants.TWITTER_PASSWORD);
        /*
         * Request access token and store in configuration
         */
        final TwitterAccessToken accessToken = twitterService.getTwitterAccessToken(login, password);
        /*
         * Add to configuration & update
         */
        final String token = accessToken.getToken();
        final String tokenSecret = accessToken.getTokenSecret();
        configuration.put(TwitterConstants.TWITTER_TOKEN, token);
        configuration.put(TwitterConstants.TWITTER_TOKEN_SECRET, tokenSecret);
        final MessagingAccountManager accountManager = account.getMessagingService().getAccountManager();
        accountManager.updateAccount(account, session);
        /*
         * Obtain OAuth twitter access
         */
        final TwitterAccess newTwitterAccess = twitterService.getOAuthTwitterAccess(token, tokenSecret);
        /*
         * Test it
         */
        testOAuthTwitterAccess(newTwitterAccess);
        /*
         * ... and return
         */
        return newTwitterAccess;
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
        } catch (final TwitterException e) {
            final org.apache.commons.logging.Log logger = org.apache.commons.logging.LogFactory.getLog(AbstractTwitterMessagingAccess.class);
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
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
        } catch (final TwitterException e) {
            return false;
        }
    }

}
