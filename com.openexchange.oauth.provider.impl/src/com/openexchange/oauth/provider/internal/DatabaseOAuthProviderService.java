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

import gnu.trove.list.TIntList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthValidator;
import net.oauth.server.OAuthServlet;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.sql.DBUtils;

/**
 * Utility methods for providers that store consumers, tokens and secrets in database.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DatabaseOAuthProviderService extends AbstractOAuthProviderService implements OAuthProviderService {

    protected static final Log LOG = com.openexchange.log.Log.loggerFor(DatabaseOAuthProviderService.class);

    /*
     * Member section
     */

    private final OAuthValidator validator;

    private final ConcurrentMap<String, OAuthConsumer> consumers;

    private final ConcurrentMap<OAuthAccessor, Object> tokens;

    /**
     * Initializes a new {@link DatabaseOAuthProviderService}.
     * 
     * @throws OXException If initialization fails
     */
    public DatabaseOAuthProviderService(final ServiceLookup services) throws OXException {
        super(services);
        validator = generateValidator(services);
        consumers = new ConcurrentHashMap<String, OAuthConsumer>(16);
        tokens = new ConcurrentHashMap<OAuthAccessor, Object>(256);
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        loadConsumers(databaseService, true);
    }

    private OAuthValidator generateValidator(final ServiceLookup services) {
        final ConfigurationService service = services.getService(ConfigurationService.class);
        final int maxTimestampAgeMsec = service.getIntProperty("com.openexchange.oauth.provider.validator.maxTimestampAgeMsec", 300000);
        final double maxVersion =
            Double.parseDouble(service.getProperty("com.openexchange.oauth.provider.validator.maxVersion", "1.0").trim());
        return new DatabaseOAuthValidator(maxTimestampAgeMsec, maxVersion);
    }

    @Override
    public OAuthValidator getValidator() {
        return validator;
    }

    @Override
    public void loadConsumers() throws OXException {
        loadConsumers(services.getService(DatabaseService.class), true);
    }

    /**
     * Loads consumers from database
     * 
     * @param databaseService The database service
     * @throws OXException If loading consumers fails
     */
    private void loadConsumers(final DatabaseService databaseService, final boolean loadAccessors) throws OXException {
        Connection con = databaseService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT `key`, `secret`, `callbackUrl`, `name`, `id` FROM oauthConsumer WHERE `providerId`=?");
            stmt.setInt(1, DEFAULT_PROVIDER);
            rs = stmt.executeQuery();
            consumers.clear();
            final Integer providerId = Integer.valueOf(DEFAULT_PROVIDER);
            while (rs.next()) {
                String callbackUrl = rs.getString(3);
                if (rs.wasNull()) {
                    callbackUrl = null;
                }
                final String consumerKey = rs.getString(1);
                final OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey, rs.getString(2), provider);
                consumer.setProperty(PROP_NAME, consumerKey);
                consumer.setProperty(PROP_ID, Integer.valueOf(rs.getInt(5)));
                consumer.setProperty(PROP_PROVIDER_ID, providerId);
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
        /*
         * Load access tokens
         */
        stmt = null;
        rs = null;
        con = null;
        if (loadAccessors) {
            final TIntList contextIds = getContextIds(databaseService);
            final ConcurrentMap<OAuthAccessor, Object> tokens = this.tokens;
            final Object present = AbstractOAuthProviderService.PRESENT;
            final Runnable loader = new Runnable() {

                @Override
                public void run() {
                    try {
                        final boolean infoEnabled = LOG.isInfoEnabled();
                        final long st = infoEnabled ? System.currentTimeMillis() : 0L;
                        final TIntSet processed = new TIntHashSet(contextIds.size());
                        final AtomicReference<OXException> errorRef = new AtomicReference<OXException>();
                        final List<int[]> delete = new LinkedList<int[]>();
                        contextIds.forEach(new TIntProcedure() {

                            @Override
                            public boolean execute(final int contextId) {
                                if (!processed.add(contextId)) {
                                    return true;
                                }
                                Connection con = null;
                                PreparedStatement ps = null;
                                ResultSet result = null;
                                try {
                                    processed.addAll(databaseService.getContextsInSameSchema(contextId));
                                    con = databaseService.getReadOnly(contextId);
                                    if (!tableExists(con, "oauthAccessor")) {
                                        return true;
                                    }
                                    // Tables already exist; load them
                                    ps =
                                        con.prepareStatement("SELECT cid, user, consumerId, requestToken, accessToken, tokenSecret FROM oauthAccessor WHERE providerId=?");
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
                                    final Set<String> secretPropertyNames = getSecretPropertyNames();
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
                                            if (!result.wasNull()) {
                                                final String name = result.getString(1);
                                                if (secretPropertyNames.contains(name)) {
                                                    accessor.setProperty(name, decrypt(value.toString()));
                                                } else {
                                                    accessor.setProperty(name, value);
                                                }
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
                                final Connection con = databaseService.getWritable(cid);
                                PreparedStatement stmt = null;
                                try {
                                    DBUtils.startTransaction(con);
                                    stmt = con.prepareStatement("DELETE FROM oauthAccessor WHERE cid=? AND user=? AND consumerId=?");
                                    stmt.setInt(1, cid);
                                    stmt.setInt(2, arr[1]);
                                    stmt.setInt(3, arr[2]);
                                    stmt.executeUpdate();
                                    DBUtils.closeSQLStuff(stmt);
                                    stmt =
                                        con.prepareStatement("DELETE FROM oauthAccessorProperty WHERE cid=? AND user=? AND consumerId=?");
                                    stmt.setInt(1, cid);
                                    stmt.setInt(2, arr[1]);
                                    stmt.setInt(3, arr[2]);
                                    stmt.executeUpdate();
                                    con.commit();
                                } catch (final Exception e) {
                                    DBUtils.rollback(con);
                                    LOG.warn("Couldn't delete OAuth accessor.", e);
                                } finally {
                                    DBUtils.closeSQLStuff(stmt);
                                    DBUtils.autocommit(con);
                                    databaseService.backWritable(cid, con);
                                }
                            }
                        }
                        if (infoEnabled) {
                            final long dur = System.currentTimeMillis() - st;
                            LOG.info("DatabaseOAuthProviderService.loadConsumers(): Loading accessors took " + dur + "msec");
                        }
                    } catch (final Exception e) {
                        LOG.warn("Couldn't load OAuth accessors.", e);
                    }
                }
            };
            ThreadPools.getThreadPool().submit(ThreadPools.task(loader));
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
        if (null == consumerKey) {
            final OAuthProblemException exception = new OAuthProblemException("parameter_absent");
            exception.setParameter("oauth_parameters_absent", OAuth.OAUTH_CONSUMER_KEY);
            throw exception;
        }
        final OAuthConsumer consumer = consumers.get(consumerKey);
        if (consumer == null) {
            throw new OAuthProblemException("consumer_key_unknown");
        }
        return consumer;
    }

    @Override
    public OAuthAccessor getAccessor(final OAuthMessage requestMessage) throws IOException, OAuthProblemException {
        final String consumerToken = requestMessage.getToken();
        if (null == consumerToken) {
            throw new OAuthProblemException("parameter_absent");
        }
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
        // Set properties
        accessor.setProperty(PROP_AUTHORIZED, Boolean.TRUE);
        accessor.setProperty(PROP_USER, Integer.valueOf(userId));
        accessor.setProperty(PROP_CONTEXT, Integer.valueOf(contextId));
        // INSERT
        insertOAuthAccessor(accessor, userId, contextId);
        // Update token in local cache
        tokens.put(accessor, PRESENT);
    }

    @Override
    public void generateRequestToken(final OAuthAccessor accessor) throws OXException {
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
        // Add to the local cache (in-memory only for unauthorized request token)
        tokens.put(accessor, PRESENT);
    }

    @Override
    public void generateAccessToken(final OAuthAccessor accessor, final int userId, final int contextId) throws OXException {
        // Generate oauth_token and oauth_secret
        final String consumerKey = accessor.consumer.consumerKey;
        // Generate token and secret based on consumer_key
        // For now use md5 of name + current time as token
        final String tokenData = consumerKey + System.nanoTime();
        final String token = DigestUtils.md5Hex(tokenData);
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

    private void insertOAuthAccessor(final OAuthAccessor accessor, final int userId, final int contextId) throws OXException {
        // Add to database
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            DBUtils.startTransaction(con);
            stmt =
                con.prepareStatement("INSERT INTO oauthAccessor (cid,user,consumerId,providerId,requestToken,accessToken,tokenSecret) VALUES (?,?,?,?,?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, accessor.consumer.<Integer> getProperty(PROP_ID).intValue());
            stmt.setInt(4, accessor.consumer.<Integer> getProperty(PROP_PROVIDER_ID).intValue());
            stmt.setString(5, accessor.requestToken);
            stmt.setNull(6, Types.VARCHAR);
            stmt.setString(7, accessor.tokenSecret);
            stmt.executeUpdate();
            /*
             * Properties, too
             */
            DBUtils.closeSQLStuff(stmt);
            stmt = con.prepareStatement("INSERT INTO oauthAccessorProperty (cid,user,consumerId,name,value) VALUES (?,?,?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, accessor.consumer.<Integer> getProperty(PROP_ID).intValue());
            // Ensure user+context
            accessor.setProperty(PROP_USER, Integer.valueOf(userId));
            accessor.setProperty(PROP_CONTEXT, Integer.valueOf(contextId));
            final Set<String> secretPropertyNames = getSecretPropertyNames();
            for (final Iterator<Map.Entry<String, Object>> iter = accessor.getProperties(); iter.hasNext();) {
                final Map.Entry<String, Object> entry = iter.next();
                final String propName = entry.getKey();
                stmt.setString(4, propName);
                if (secretPropertyNames.contains(propName)) {
                    stmt.setString(5, encrypt(entry.getValue().toString()));
                } else {
                    stmt.setString(5, entry.getValue().toString());
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            DBUtils.rollback(con);
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

}
