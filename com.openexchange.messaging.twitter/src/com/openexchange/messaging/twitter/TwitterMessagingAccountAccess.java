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
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.twitter.services.TwitterMessagingServiceRegistry;
import com.openexchange.messaging.twitter.session.TwitterSessionRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.twitter.Paging;
import com.openexchange.twitter.TwitterAccess;
import com.openexchange.twitter.TwitterAccessToken;
import com.openexchange.twitter.TwitterException;
import com.openexchange.twitter.TwitterService;

/**
 * {@link TwitterMessagingAccountAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingAccountAccess implements MessagingAccountAccess {

    private final MessagingAccount account;

    private final TwitterService twitterService;

    private final TwitterAccess twitterAccess;

    private final Session session;

    private MessagingFolderAccess folderAccess;

    private MessagingMessageAccess messageAccess;

    private boolean connected;

    /**
     * Initializes a new {@link TwitterMessagingAccountAccess}.
     * 
     * @throws MessagingException If initialization fails
     */
    public TwitterMessagingAccountAccess(final MessagingAccount account, final Session session) throws MessagingException {
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
        TwitterAccess tmp = TwitterSessionRegistry.getInstance().getSession(contextId, userId, accountId);
        if (null == tmp) {
            try {
                final Map<String, Object> configuration = account.getConfiguration();
                /*
                 * Get login and password for this account
                 */
                final String login = (String) configuration.get(TwitterConstants.TWITTER_LOGIN);
                final String password = (String) configuration.get(TwitterConstants.TWITTER_PASSWORD);
                /*
                 * Check existence of access token
                 */
                String token = (String) configuration.get(TwitterConstants.TWITTER_TOKEN);
                if (null == token) {
                    /*
                     * Request access token and store in configuration
                     */
                    final TwitterAccessToken accessToken = twitterService.getTwitterAccessToken(login, password);
                    /*
                     * Add to configuration
                     */
                    token = accessToken.getToken();
                    configuration.put(TwitterConstants.TWITTER_TOKEN, token);
                    configuration.put(TwitterConstants.TWITTER_TOKEN_SECRET, accessToken.getTokenSecret());
                    final MessagingAccountManager accountManager = account.getMessagingService().getAccountManager();
                    accountManager.updateAccount(account, session);
                }
                String tokenSecret = (String) configuration.get(TwitterConstants.TWITTER_TOKEN_SECRET);
                final TwitterAccess newTwitterAccess = twitterService.getOAuthTwitterAccess(token, tokenSecret);
                tmp = TwitterSessionRegistry.getInstance().addAccess(contextId, userId, accountId, newTwitterAccess);
                if (null == tmp) {
                    tmp = newTwitterAccess;
                }
            } catch (final TwitterException e) {
                throw new MessagingException(e);
            }
        }
        twitterAccess = tmp;
    }

    public int getAccountId() {
        return account.getId();
    }

    public MessagingFolderAccess getFolderAccess() throws MessagingException {
        if (null == folderAccess) {
            folderAccess = new TwitterMessagingFolderAccess(account, session);
        }
        return folderAccess;
    }

    public MessagingMessageAccess getMessageAccess() throws MessagingException {
        if (null == messageAccess) {
            messageAccess = new TwitterMessagingMessageAccess(twitterAccess, account, session);
        }
        return messageAccess;
    }

    public void close() {
        connected = false;
    }

    public void connect() throws MessagingException {
        connected = true;
    }

    public boolean ping() throws MessagingException {
        try {
            final Paging paging = twitterService.newPaging();
            paging.count(1);
            twitterAccess.getFriendsTimeline(paging);
            return true;
        } catch (final TwitterException e) {
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public MessagingFolder getRootFolder() throws MessagingException {
        return getFolderAccess().getRootFolder();
    }

    public boolean cacheable() {
        return true;
    }

}
