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

package com.openexchange.oauth.provider.internal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.server.OAuthServlet;
import org.apache.commons.codec.digest.DigestUtils;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.server.ServiceLookup;

/**
 * Utility methods for providers that store consumers, tokens and secrets in database.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DatabaseOAuthProvider implements OAuthProviderService {

    private static final String PROP_AUTHORIZED = "authorized";

    private static final String PROP_USER = "user";

    private static final String PROP_CONTEXT = "context";

    private static final String PROP_DESCRIPTION = "description";

    private static final String PROP_PROVIDER_ID = "providerId";

    private static final String PROP_NAME = "name";

    private static final String PROP_ID = "id";

    private static final Object PRESENT = new Object();

    /*
     * Member section
     */

    private final int anyContextId;

    private final OAuthServiceProvider provider;

    private final ConcurrentMap<String, OAuthConsumer> consumers;

    private final ConcurrentMap<OAuthAccessor, Object> tokens;

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DatabaseOAuthProvider}.
     * 
     * @throws OXException If initialization fails
     */
    public DatabaseOAuthProvider(final ServiceLookup services) throws OXException {
        super();
        this.services = services;
        consumers = new ConcurrentHashMap<String, OAuthConsumer>(16);
        tokens = new ConcurrentHashMap<OAuthAccessor, Object>(256);
        final DatabaseService databaseService = services.getService(DatabaseService.class);
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
                con.prepareStatement("SELECT requestTokenUrl, userAuthorizationUrl, accessTokenURL FROM oauthServiceProvider WHERE id=?");
            stmt.setInt(1, DEFAULT_PROVIDER);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw OAuthProviderExceptionCodes.PROVIDER_NOT_FOUND.create(Integer.valueOf(DEFAULT_PROVIDER));
            }
            return new OAuthServiceProvider(rs.getString(1), rs.getString(2), rs.getString(3));
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(anyContextId, con);
        }
    }

    @Override
    public void loadConsumers() throws OXException {
        loadConsumers(services.getService(DatabaseService.class));
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
            stmt = con.prepareStatement("SELECT key, secret, callbackUrl, name, id FROM oauthConsumer WHERE providerId=?");
            stmt.setInt(1, DEFAULT_PROVIDER);
            rs = stmt.executeQuery();
            consumers.clear();
            while (rs.next()) {
                String callbackUrl = rs.getString(3);
                if (rs.wasNull()) {
                    callbackUrl = null;
                }
                final String consumerKey = rs.getString(1);
                final OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey, rs.getString(2), provider);
                consumer.setProperty(PROP_NAME, consumerKey);
                consumer.setProperty(PROP_ID, Integer.valueOf(rs.getInt(5)));
                consumer.setProperty(PROP_PROVIDER_ID, Integer.valueOf(DEFAULT_PROVIDER));
                final String name = rs.getString(4);
                if (!rs.wasNull()) {
                    consumer.setProperty(PROP_DESCRIPTION, name);
                }
                consumers.put(consumerKey, consumer);
            }
            for (final OAuthConsumer consumer : consumers.values()) {
                /*
                 * Load consumer's properties
                 */
                DBUtils.closeSQLStuff(rs, stmt);
                stmt = con.prepareStatement("SELECT name, value FROM oauthConsumerProperty WHERE id=?");
                final int id = consumer.<Integer> getProperty(PROP_ID).intValue();
                stmt.setInt(1, id);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    final Object value = valueOf(rs.getString(2));
                    if (null != value) {
                        consumer.setProperty(rs.getString(1), value);
                    }
                }
                consumer.setProperty(PROP_NAME, consumer.consumerKey);
                consumer.setProperty(PROP_ID, Integer.valueOf(id));
                /*
                 * Load associated accessors aka tokens
                 */
                DBUtils.closeSQLStuff(rs, stmt);
                stmt =
                    con.prepareStatement("SELECT cid, user, requestToken, accessToken, tokenSecret FROM oauthAccessor WHERE consumerId=? AND providerId=?");
                stmt.setInt(1, id);
                stmt.setInt(2, DEFAULT_PROVIDER);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    final OAuthAccessor accessor = new OAuthAccessor(consumer);
                    accessor.accessToken = stringOf(rs.getString(4));
                    accessor.requestToken = stringOf(rs.getString(3));
                    accessor.tokenSecret = stringOf(rs.getString(5));
                    accessor.setProperty(PROP_CONTEXT, Integer.valueOf(rs.getInt(1)));
                    accessor.setProperty(PROP_USER, Integer.valueOf(rs.getInt(2)));
                    tokens.put(accessor, PRESENT);
                }
                for (final OAuthAccessor accessor : tokens.keySet()) {
                    /*
                     * Load accessors's properties
                     */
                    DBUtils.closeSQLStuff(rs, stmt);
                    stmt =
                        con.prepareStatement("SELECT name, value FROM oauthAccessorProperty WHERE cid=? AND user=? AND consumerId=?");
                    final int contextId = ((Integer) accessor.getProperty(PROP_CONTEXT)).intValue();
                    final int userId = ((Integer) accessor.getProperty(PROP_USER)).intValue();
                    final int consumerId = accessor.consumer.<Integer> getProperty(PROP_ID).intValue();
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, consumerId);
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        final Object value = valueOf(rs.getString(2));
                        if (null != value) {
                            accessor.setProperty(rs.getString(1), value);
                        }
                    }
                    DBUtils.closeSQLStuff(rs, stmt);
                    accessor.setProperty(PROP_CONTEXT, Integer.valueOf(contextId));
                    accessor.setProperty(PROP_USER, Integer.valueOf(userId));
                }
            }
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(anyContextId, con);
        }
    }

    @Override
    public OAuthConsumer getConsumer(final OAuthMessage requestMessage) throws IOException, OAuthProblemException {
        final String consumerKey = requestMessage.getConsumerKey();
        final OAuthConsumer consumer = consumers.get(consumerKey);
        if (consumer == null) {
            throw new OAuthProblemException("token_rejected");
        }
        return consumer;
    }

    @Override
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
        // Check for null
        if (accessor == null) {
            throw new OAuthProblemException("token_expired");
        }
        // Return
        return accessor;
    }

    @Override
    public void markAsAuthorized(final OAuthAccessor accessor, final int userId, final int contextId) throws OXException {
        // Set property
        accessor.setProperty(PROP_AUTHORIZED, Boolean.TRUE);
        // Perform SQL INSERT
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        final Connection con = databaseService.getWritable(anyContextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO oauthAccessorProperty (cid,user,consumerId,name,value) VALUES (?,?,?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, accessor.consumer.<Integer> getProperty(PROP_ID).intValue());
            stmt.setString(4, PROP_AUTHORIZED);
            stmt.setString(5, "true");
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            databaseService.backWritable(anyContextId, con);
        }
        // Update token in local cache
        tokens.put(accessor, PRESENT);
    }

    @Override
    public void generateRequestToken(final OAuthAccessor accessor, final int userId, final int contextId) throws OXException {
        // Generate oauth_token and oauth_secret
        final String consumerKey = accessor.consumer.consumerKey;
        // Generate token and secret based on consumerKey
        // For now use md5 of name + current time as token
        final String tokenData = consumerKey + System.nanoTime();
        final String token = DigestUtils.md5Hex(tokenData);
        // For now use md5 of name + current time + token as secret
        final String secretData = consumerKey + System.nanoTime() + token;
        final String secret = DigestUtils.md5Hex(secretData);
        // Apply request token
        accessor.requestToken = token;
        accessor.tokenSecret = secret;
        accessor.accessToken = null;
        // Add to the local cache
        tokens.put(accessor, PRESENT);
        // Add to database
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        final Connection con = databaseService.getWritable(anyContextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO oauthAccessor (cid,user,consumerId,providerId,requestToken,accessToken,tokenSecret) VALUES (?,?,?,?,?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, accessor.consumer.<Integer> getProperty(PROP_ID).intValue());
            stmt.setInt(4, accessor.consumer.<Integer> getProperty(PROP_PROVIDER_ID).intValue());
            stmt.setString(5, token);
            stmt.setNull(6, Types.VARCHAR);
            stmt.setString(7, secret);
            stmt.executeUpdate();
            /*
             * Properties, too
             */
            DBUtils.closeSQLStuff(stmt);
            stmt = con.prepareStatement("INSERT INTO oauthAccessorProperty (cid,user,consumerId,name,value) VALUES (?,?,?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, accessor.consumer.<Integer> getProperty(PROP_ID).intValue());
            for (final Iterator<Map.Entry<String, Object>> iter = accessor.getProperties(); iter.hasNext();) {
                final Map.Entry<String, Object> entry = iter.next();
                stmt.setString(4, entry.getKey());
                stmt.setString(5, entry.getValue().toString());
                stmt.addBatch();
            }
            stmt.executeBatch();
            // "context"
            stmt.setString(4, PROP_CONTEXT);
            stmt.setString(5, Integer.toString(contextId));
            stmt.addBatch();
            // "user"
            stmt.setString(4, PROP_USER);
            stmt.setString(5, Integer.toString(userId));
            stmt.addBatch();
            // Execute batch
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            databaseService.backWritable(anyContextId, con);
        }
    }

    @Override
    public void generateAccessToken(final OAuthAccessor accessor, final int userId, final int contextId) throws OXException {
        // Generate oauth_token and oauth_secret
        final String consumerKey = accessor.consumer.consumerKey;
        // Generate token and secret based on consumer_key
        // For now use md5 of name + current time as token
        final String tokenData = consumerKey + System.nanoTime();
        final String token = DigestUtils.md5Hex(tokenData);
        // first remove the accessor from cache
        tokens.remove(accessor);
        // Apply access token
        accessor.requestToken = null;
        accessor.accessToken = token;
        // Update token in local cache
        tokens.put(accessor, PRESENT);
        // Update in database
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        final Connection con = databaseService.getWritable(anyContextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE oauthAccessor SET requestToken=?, accessToken=? WHERE cid=? AND user=? AND consumerId=?");
            stmt.setNull(1, Types.VARCHAR);
            stmt.setString(2, token);
            stmt.setInt(3, contextId);
            stmt.setInt(4, userId);
            stmt.setInt(5, accessor.consumer.<Integer> getProperty(PROP_ID).intValue());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            databaseService.backWritable(anyContextId, con);
        }
    }

    public static void handleException(final Exception e, final HttpServletRequest request, final HttpServletResponse response, final boolean sendBody) throws IOException, ServletException {
        final StringBuilder realm = new StringBuilder(32).append((request.isSecure()) ? "https://" : "http://");
        realm.append(request.getLocalName());
        OAuthServlet.handleException(response, e, realm.toString(), sendBody);
    }

    private static Object valueOf(final String value) {
        if (isEmpty(value)) {
            return null;
        }
        /*
         * If it is true, false, or null, return the proper value.
         */
        final String s = value.trim();
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("null")) {
            return null;
        }
        /*
         * If it might be a number, try converting it. We support the 0- and 0x- conventions. If a number cannot be produced, then the value
         * will just be a string. Note that the 0-, 0x-, plus, and implied string conventions are non-standard. A JSON parser is free to
         * accept non-JSON forms as long as it accepts all correct JSON forms.
         */
        final char b = s.charAt(0);
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            if (b == '0') {
                if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                    try {
                        return Integer.valueOf(Integer.parseInt(s.substring(2), 16));
                    } catch (final Exception e) {
                        /* Ignore the error */
                    }
                } else {
                    try {
                        return Integer.valueOf(Integer.parseInt(s, 8));
                    } catch (final Exception e) {
                        /* Ignore the error */
                    }
                }
            }
            try {
                return Integer.valueOf(s);
            } catch (final Exception e) {
                try {
                    return Long.valueOf(s);
                } catch (final Exception f) {
                    try {
                        return Double.valueOf(s);
                    } catch (final Exception g) {
                        return s;
                    }
                }
            }
        }
        return s;
    }

    private static String stringOf(final String value) {
        return isEmpty(value) ? null : value;
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
