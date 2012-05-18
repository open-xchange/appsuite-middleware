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

import static com.openexchange.oauth.provider.internal.DBUtils.closeSQLStuff;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.procedure.TIntProcedure;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.OAuthValidator;
import net.oauth.server.OAuthServlet;
import org.apache.commons.codec.digest.DigestUtils;
import com.openexchange.config.ConfigurationService;
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
public class DatabaseOAuthProviderService implements OAuthProviderService {

    private static final Object PRESENT = new Object();

    /*
     * Member section
     */

    private final OAuthValidator validator;

    private final OAuthServiceProvider provider;

    private final ConcurrentMap<String, OAuthConsumer> consumers;

    private final ConcurrentMap<OAuthAccessor, Object> tokens;

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DatabaseOAuthProviderService}.
     * 
     * @throws OXException If initialization fails
     */
    public DatabaseOAuthProviderService(final ServiceLookup services) throws OXException {
        super();
        validator = generateValidator();
        this.services = services;
        consumers = new ConcurrentHashMap<String, OAuthConsumer>(16);
        tokens = new ConcurrentHashMap<OAuthAccessor, Object>(256);
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        // Load provider
        provider = loadServiceProvider(databaseService);
        loadConsumers(databaseService);
    }

    private OAuthValidator generateValidator() {
        final ConfigurationService service = services.getService(ConfigurationService.class);
        final int maxTimestampAgeMsec = service.getIntProperty("com.openexchange.oauth.provider.validator.maxTimestampAgeMsec", 300000);
        final double maxVersion = Double.parseDouble(service.getProperty("com.openexchange.oauth.provider.validator.maxVersion", "1.0").trim());
        return new DatabaseOAuthValidator(maxTimestampAgeMsec, maxVersion);
    }

    private TIntList getContextIds(final DatabaseService databaseService) throws OXException {
        final Connection con = databaseService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid FROM context");
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return new TIntLinkedList();
            }
            final TIntList ret = new TIntLinkedList();
            do {
                ret.add(rs.getInt(1));
            } while (rs.next());
            return ret;
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
        final Connection con = databaseService.getReadOnly();
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
            databaseService.backReadOnly(con);
        }
    }

    /**
     * Gets the <tt>OAuthServiceProvider</tt> instance
     *
     * @return The provider
     */
    public OAuthServiceProvider getProvider() {
        return provider;
    }

    @Override
    public OAuthValidator getValidator() {
        return validator;
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
        Connection con = databaseService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT `key`, `secret`, `callbackUrl`, `name`, `id` FROM oauthConsumer WHERE `providerId`=?");
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
                stmt = con.prepareStatement("SELECT `name`, `value` FROM oauthConsumerProperty WHERE `id`=?");
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
            }
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(con);
        }
        // Load access tokens
        stmt = null;
        rs = null;
        con = null;
        final TIntList contextIds = getContextIds(databaseService);
        try {
            final AtomicReference<OXException> errorRef = new AtomicReference<OXException>();
            final ConcurrentMap<OAuthAccessor, Object> tokens = this.tokens;
            final Object present = DatabaseOAuthProviderService.PRESENT;
            /*
             * TODO: Improve!
             */
            final List<int[]> delete = new LinkedList<int[]>();
            contextIds.forEach(new TIntProcedure() {
                
                @Override
                public boolean execute(final int contextId) {
                    Connection con = null;
                    PreparedStatement ps = null;
                    ResultSet result = null;
                    try {
                        con = databaseService.getReadOnly(contextId);
                        if (!tableExists(con, "oauthAccessor")) {
                            databaseService.backReadOnly(contextId, con);
                            con = databaseService.getWritable(contextId);
                            Statement stmt = null;
                            try {
                                DBUtils.startTransaction(con);
                                stmt = con.createStatement();
                                stmt.execute("CREATE TABLE `oauthAccessor` (" + 
                                		" `cid` int(10) unsigned NOT NULL," + 
                                		" `user` int(10) unsigned NOT NULL," + 
                                		" `consumerId` int(10) unsigned NOT NULL," + 
                                		" `providerId` int(10) unsigned NOT NULL," + 
                                		" `requestToken` varchar(255) DEFAULT NULL," + 
                                		" `accessToken` varchar(255) DEFAULT NULL," + 
                                		" `tokenSecret` varchar(255) NOT NULL," + 
                                		" PRIMARY KEY (`cid`,`user`,`consumerId`)," + 
                                		" KEY `userIndex` (`cid`,`user`)," + 
                                		" KEY `consumerIndex` (`consumerId`,`providerId`)" + 
                                		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
                                stmt.execute("CREATE TABLE `oauthAccessorProperty` (" + 
                                		" `cid` int(10) unsigned NOT NULL," + 
                                		" `user` int(10) unsigned NOT NULL," + 
                                		" `consumerId` int(10) unsigned NOT NULL," + 
                                		" `name` varchar(32) NOT NULL," + 
                                		" `value` varchar(255) NOT NULL," + 
                                		" PRIMARY KEY (`cid`,`user`,`consumerId`,`name`)" + 
                                		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");
                                con.commit();
                            } catch (final SQLException e) {
                                errorRef.set(OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage()));
                                DBUtils.rollback(con);
                                return false;
                            } catch (final Exception e) {
                                errorRef.set(OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
                                DBUtils.rollback(con);
                                return false;
                            } finally {
                                DBUtils.closeSQLStuff(stmt);
                                DBUtils.autocommit(con);
                                databaseService.backWritable(contextId, con);
                                con = null; // Set to null to successfully pass finally block
                            }
                            return true;
                        }
                        // Tables already exist; load them
                        ps = con.prepareStatement("SELECT cid, user, consumerId, requestToken, accessToken, tokenSecret FROM oauthAccessor WHERE providerId=?");
                        ps.setInt(1, DEFAULT_PROVIDER);
                        result = ps.executeQuery();
                        final List<OAuthAccessor> accessors = new LinkedList<OAuthAccessor>();
                        while (result.next()) {
                            final int consumerId = result.getInt(3);
                            final OAuthConsumer consumer = consumerById(consumerId);
                            if (null == consumer) {
                                delete.add(new int[] { result.getInt(1), result.getInt(2), consumerId });
                            } else {
                                final OAuthAccessor accessor = new OAuthAccessor(consumer);
                                accessor.accessToken = stringOf(result.getString(5));
                                accessor.requestToken = stringOf(result.getString(4));
                                accessor.tokenSecret = stringOf(result.getString(6));
                                accessor.setProperty(PROP_CONTEXT, Integer.valueOf(result.getInt(1)));
                                accessor.setProperty(PROP_USER, Integer.valueOf(result.getInt(2)));
                                tokens.put(accessor, present);
                                accessors.add(accessor);
                            }
                        }
                        for (final OAuthAccessor accessor : accessors) {
                            /*
                             * Load accessors's properties
                             */
                            DBUtils.closeSQLStuff(result, ps);
                            ps =
                                con.prepareStatement("SELECT name, value FROM oauthAccessorProperty WHERE cid=? AND user=? AND consumerId=?");
                            final int userId = ((Integer) accessor.getProperty(PROP_USER)).intValue();
                            final int consumerId = accessor.consumer.<Integer> getProperty(PROP_ID).intValue();
                            ps.setInt(1, contextId);
                            ps.setInt(2, userId);
                            ps.setInt(3, consumerId);
                            result = ps.executeQuery();
                            while (result.next()) {
                                final Object value = valueOf(result.getString(2));
                                if (null != value) {
                                    accessor.setProperty(result.getString(1), value);
                                }
                            }
                            DBUtils.closeSQLStuff(result, ps);
                            accessor.setProperty(PROP_CONTEXT, Integer.valueOf(contextId));
                            accessor.setProperty(PROP_USER, Integer.valueOf(userId));
                        }
                        return true;
                    } catch (final OXException e) {
                        errorRef.set(e);
                        return false;
                    } catch (final SQLException e) {
                        errorRef.set(OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage()));
                        return false;
                    } catch (final RuntimeException e) {
                        errorRef.set(OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
                        return false;
                    } finally {
                        DBUtils.closeSQLStuff(result, ps);
                        if (null != con) {
                            databaseService.backReadOnly(contextId, con);
                        }
                    }
                }
            });
            // Check for error
            {
                final OXException err = errorRef.get();
                if (null != err) {
                    throw err;
                }
            }
            // Check delete
            if (!delete.isEmpty()) {
                for (final int[] arr : delete) {
                    final int cid = arr[0];
                    con = databaseService.getWritable(cid);
                    try {
                        stmt = con.prepareStatement("DELETE FROM oauthAccessor WHERE cid=? AND user=? AND consumerId=?");
                        stmt.setInt(1, cid);
                        stmt.setInt(2, arr[1]);
                        stmt.setInt(3, arr[2]);
                        stmt.executeUpdate();
                        DBUtils.closeSQLStuff(stmt);
                        stmt = con.prepareStatement("DELETE FROM oauthAccessorProperty WHERE cid=? AND user=? AND consumerId=?");
                        stmt.setInt(1, cid);
                        stmt.setInt(2, arr[1]);
                        stmt.setInt(3, arr[2]);
                        stmt.executeUpdate();
                    } finally {
                        DBUtils.closeSQLStuff(stmt);
                        databaseService.backWritable(cid, con);
                    }
                }
            }
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Looks-up consumer by identifier.
     * 
     * @param id The consumer identifier
     * @return The consumer or <code>null</code>
     */
    protected OAuthConsumer consumerById(final int id) {
        for (final OAuthConsumer consumer : consumers.values()) {
            if (id == consumer.<Integer> getProperty(PROP_ID).intValue()) {
                return consumer;
            }
        }
        return null;
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
        final Connection con = databaseService.getWritable(contextId);
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
            databaseService.backWritable(contextId, con);
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
        final Connection con = databaseService.getWritable(contextId);
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
            databaseService.backWritable(contextId, con);
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
        final Connection con = databaseService.getWritable(contextId);
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
            databaseService.backWritable(contextId, con);
        }
    }

    public static void handleException(final Exception e, final HttpServletRequest request, final HttpServletResponse response, final boolean sendBody) throws IOException, ServletException {
        final StringBuilder realm = new StringBuilder(32).append((request.isSecure()) ? "https://" : "http://");
        realm.append(request.getLocalName());
        OAuthServlet.handleException(response, e, realm.toString(), sendBody);
    }

    protected static Object valueOf(final String value) {
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

    protected static String stringOf(final String value) {
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

    protected static final boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equalsIgnoreCase(table));
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }

}
