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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.tools.sql.DBUtils;

/**
 * Utility methods for providers that store consumers, tokens and secrets in database.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DatabaseOAuthProvider {

    /**
     * The OAuth validator.
     */
    public static final OAuthValidator VALIDATOR = new SimpleOAuthValidator();

    private static final Object PRESENT = new Object();

    private static final int DEFAULT = 0;

    private final int anyContextId;

    private final OAuthServiceProvider provider;

    private final ConcurrentMap<String, OAuthConsumer> consumers;

    private final ConcurrentMap<OAuthAccessor, Object> tokens;

    /**
     * Initializes a new {@link DatabaseOAuthProvider}.
     * 
     * @throws OXException If initialization fails
     */
    public DatabaseOAuthProvider() throws OXException {
        super();
        consumers = new ConcurrentHashMap<String, OAuthConsumer>(16);
        tokens = new ConcurrentHashMap<OAuthAccessor, Object>(256);
        final DatabaseService databaseService = OAuthProviderServiceLookup.getService(DatabaseService.class);
        // Load arbitrary context identifier
        final int cid = getAnyContextId(databaseService);
        anyContextId = cid;
        // Load provider
        provider = loadServiceProvider(databaseService);
        loadConsumers(databaseService);
    }

    private int getAnyContextId(final DatabaseService databaseService) throws OXException {
        final Connection con = databaseService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid FROM context LIMIT 1");
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(con);
        }
    }

    /**
     * Loads the service provider.
     * 
     * @param databaseService The database service
     * @return The service provider
     * @throws OXException If loading fails
     */
    private OAuthServiceProvider loadServiceProvider(final DatabaseService databaseService) throws OXException {
        final Connection con = databaseService.getReadOnly(anyContextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                con.prepareStatement("SELECT requestTokenUrl, userAuthorizationUrl, accessTokenURL FROM oauthServiceProvider WHERE id = ?");
            stmt.setInt(1, DEFAULT);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw OAuthProviderExceptionCodes.PROVIDER_NOT_FOUND.create(Integer.valueOf(DEFAULT));
            }
            return new OAuthServiceProvider(rs.getString(1), rs.getString(2), rs.getString(3));
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(anyContextId, con);
        }
    }

    /**
     * Loads consumers from database
     * 
     * @param databaseService The database service
     * @throws OXException If loading consumers fails
     */
    private void loadConsumers(final DatabaseService databaseService) throws OXException {
        final Connection con = databaseService.getReadOnly(anyContextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT key, secret, callbackUrl, name FROM oauthServiceProvider WHERE providerId = ?");
            stmt.setInt(1, DEFAULT);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String callbackUrl = rs.getString(3);
                if (rs.wasNull()) {
                    callbackUrl = null;
                }
                final String consumerKey = rs.getString(1);
                final OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey, rs.getString(2), provider);
                consumer.setProperty("name", consumerKey);
                final String name = rs.getString(4);
                if (!rs.wasNull()) {
                    consumer.setProperty("description", name);
                }
                consumers.put(consumerKey, consumer);
            }
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(anyContextId, con);
        }
    }

    /**
     * Gets the consumer for specified OAuth request message.
     * 
     * @param requestMessage The request message
     * @return The associated consumer
     * @throws IOException If an I/O error occurs
     * @throws OAuthProblemException If an OAuth problem occurs
     */
    public OAuthConsumer getConsumer(final OAuthMessage requestMessage) throws IOException, OAuthProblemException {
        final String consumerKey = requestMessage.getConsumerKey();
        final OAuthConsumer consumer = consumers.get(consumerKey);
        if (consumer == null) {
            throw new OAuthProblemException("token_rejected");
        }
        return consumer;
    }

    /**
     * Get the access token and token secret for the given oauth_token.
     */
    public OAuthAccessor getAccessor(final OAuthMessage requestMessage) throws IOException, OAuthProblemException {
        final String consumerToken = requestMessage.getToken();
        OAuthAccessor accessor = null;
        for (final OAuthAccessor a : tokens.keySet()) {
            if (a.requestToken != null) {
                if (a.requestToken.equals(consumerToken)) {
                    accessor = a;
                    break;
                }
            } else if (a.accessToken != null) {
                if (a.accessToken.equals(consumerToken)) {
                    accessor = a;
                    break;
                }
            }
        }

        if (accessor == null) {
            throw new OAuthProblemException("token_expired");
        }

        return accessor;
    }

    /**
     * Set the access token
     */
    public synchronized void markAsAuthorized(final OAuthAccessor accessor, final String userId) throws OAuthException {
        // first remove the accessor from cache
        tokens.remove(accessor);

        accessor.setProperty("user", userId);
        accessor.setProperty("authorized", Boolean.TRUE);
        
        // TODO: Update in database, too

        // update token in local cache
        tokens.put(accessor, PRESENT);
    }

}
