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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import net.oauth.v2.BaseErrorCode;
import net.oauth.v2.OAuth2;
import net.oauth.v2.OAuth2Accessor;
import net.oauth.v2.OAuth2Client;
import net.oauth.v2.OAuth2Message;
import net.oauth.v2.OAuth2ProblemException;
import net.oauth.v2.OAuth2Validator;
import net.oauth.v2.server.OAuth2Servlet;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.v2.OAuth2ProviderService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DatabaseOAuth2ProviderService} - The database OAuth v2 provider implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseOAuth2ProviderService extends AbstractOAuthProviderService implements OAuth2ProviderService {

    /**
     * The logger constant.
     */
    protected static final Log LOG = com.openexchange.log.Log.loggerFor(DatabaseOAuth2ProviderService.class);

    private final OAuth2Validator validator;

    private final ConcurrentMap<String, OAuth2Client> clients;

    private final ConcurrentMap<OAuth2Accessor, Object> tokens;

    /**
     * Initializes a new {@link DatabaseOAuth2ProviderService}.
     * 
     * @throws OXException If initialization fails
     */
    public DatabaseOAuth2ProviderService(final ServiceLookup services) throws OXException {
        super(services);
        validator = generateValidator(services);
        clients = new ConcurrentHashMap<String, OAuth2Client>(16);
        tokens = new ConcurrentHashMap<OAuth2Accessor, Object>(256);
        loadConsumers(services.getService(DatabaseService.class), true);
    }

    @Override
    public void loadClients() throws OXException {
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
            stmt = con.prepareStatement("SELECT `key`, `secret`, `callbackUrl`, `name`, `id` FROM oauth2Client WHERE `providerId`=?");
            stmt.setInt(1, DEFAULT_PROVIDER);
            rs = stmt.executeQuery();
            clients.clear();
            final Integer providerId = Integer.valueOf(DEFAULT_PROVIDER);
            while (rs.next()) {
                String callbackUrl = rs.getString(3);
                if (rs.wasNull()) {
                    callbackUrl = null;
                }
                final String clientId = rs.getString(1);
                final OAuth2Client client = new OAuth2Client(callbackUrl, clientId, rs.getString(2));
                client.setProperty(PROP_NAME, clientId);
                client.setProperty(PROP_ID, Integer.valueOf(rs.getInt(5)));
                client.setProperty(PROP_PROVIDER_ID, providerId);
                final String name = rs.getString(4);
                if (!rs.wasNull()) {
                    client.setProperty(PROP_DESCRIPTION, name);
                }
                clients.put(clientId, client);
            }
            for (final OAuth2Client client : clients.values()) {
                /*
                 * Load consumer's properties
                 */
                DBUtils.closeSQLStuff(rs, stmt);
                stmt = con.prepareStatement("SELECT `name`, `value` FROM oauth2ClientProperty WHERE `id`=?");
                final int id = client.<Integer> getProperty(PROP_ID).intValue();
                stmt.setInt(1, id);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    final Object value = valueOf(rs.getString(2));
                    if (null != value) {
                        client.setProperty(rs.getString(1), value);
                    }
                }
                client.setProperty(PROP_NAME, client.clientId);
                client.setProperty(PROP_ID, Integer.valueOf(id));
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
            final ConcurrentMap<OAuth2Accessor, Object> tokens = this.tokens;
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
                                    if (!tableExists(con, "oauth2Accessor")) {
                                        return true;
                                    }
                                    // Tables already exist; load them
                                    ps =
                                        con.prepareStatement("SELECT cid, user, clientId, code, refreshToken, accessToken, expiresIn, tokenType, scope, state FROM oauth2Accessor WHERE providerId=?");
                                    ps.setInt(1, DEFAULT_PROVIDER);
                                    result = ps.executeQuery();
                                    final List<OAuth2Accessor> accessors = new LinkedList<OAuth2Accessor>();
                                    while (result.next()) {
                                        final int clientId = result.getInt(3);
                                        final OAuth2Client client = clientById(clientId);
                                        if (null == client) {
                                            delete.add(new int[] { result.getInt(1), result.getInt(2), clientId });
                                        } else {
                                            final OAuth2Accessor accessor = new OAuth2Accessor(client);
                                            accessor.accessToken = stringOf(result.getString(5));
                                            accessor.code = stringOf(result.getString(4));
                                            accessor.expires_in = stringOf(result.getString(7));
                                            accessor.refreshToken = stringOf(result.getString(5));
                                            accessor.scope = stringOf(result.getString(9));
                                            accessor.state = stringOf(result.getString(10));
                                            accessor.tokenType = stringOf(result.getString(8));
                                            accessor.setProperty(PROP_CONTEXT, Integer.valueOf(result.getInt(1)));
                                            accessor.setProperty(PROP_USER, Integer.valueOf(result.getInt(2)));
                                            tokens.put(accessor, present);
                                            accessors.add(accessor);
                                        }
                                    }
                                    final Set<String> secretPropertyNames = getSecretPropertyNames();
                                    for (final OAuth2Accessor accessor : accessors) {
                                        /*
                                         * Load accessors's properties
                                         */
                                        DBUtils.closeSQLStuff(result, ps);
                                        ps =
                                            con.prepareStatement("SELECT name, value FROM oauth2AccessorProperty WHERE cid=? AND user=? AND clientId=?");
                                        final int userId = ((Integer) accessor.getProperty(PROP_USER)).intValue();
                                        final int clientId = accessor.client.<Integer> getProperty(PROP_ID).intValue();
                                        ps.setInt(1, contextId);
                                        ps.setInt(2, userId);
                                        ps.setInt(3, clientId);
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
                                    stmt = con.prepareStatement("DELETE FROM oauth2Accessor WHERE cid=? AND user=? AND clientId=?");
                                    stmt.setInt(1, cid);
                                    stmt.setInt(2, arr[1]);
                                    stmt.setInt(3, arr[2]);
                                    stmt.executeUpdate();
                                    DBUtils.closeSQLStuff(stmt);
                                    stmt = con.prepareStatement("DELETE FROM oauth2AccessorProperty WHERE cid=? AND user=? AND clientId=?");
                                    stmt.setInt(1, cid);
                                    stmt.setInt(2, arr[1]);
                                    stmt.setInt(3, arr[2]);
                                    stmt.executeUpdate();
                                    con.commit();
                                } catch (final Exception e) {
                                    DBUtils.rollback(con);
                                    LOG.warn("Couldn't delete OAuth2 accessor.", e);
                                } finally {
                                    DBUtils.closeSQLStuff(stmt);
                                    DBUtils.autocommit(con);
                                    databaseService.backWritable(cid, con);
                                }
                            }
                        }
                        if (infoEnabled) {
                            final long dur = System.currentTimeMillis() - st;
                            LOG.info("DatabaseOAuth2ProviderService.loadConsumers(): Loading accessors took " + dur + "msec");
                        }
                    } catch (final Exception e) {
                        LOG.warn("Couldn't load OAuth2 accessors.", e);
                    }
                }
            };
            ThreadPools.getThreadPool().submit(ThreadPools.trackableTask(loader));
        }
    }

    /**
     * Looks-up client by identifier.
     * 
     * @param id The client identifier
     * @return The client or <code>null</code>
     */
    protected OAuth2Client clientById(final int id) {
        for (final OAuth2Client client : clients.values()) {
            if (id == client.<Integer> getProperty(PROP_ID).intValue()) {
                return client;
            }
        }
        return null;
    }

    private OAuth2Validator generateValidator(final ServiceLookup services) {
        final ConfigurationService service = services.getService(ConfigurationService.class);
        final int maxTimestampAgeMsec = service.getIntProperty("com.openexchange.oauth.provider.validator.v2.maxTimestampAgeMsec", 300000);
        final double maxVersion =
            Double.parseDouble(service.getProperty("com.openexchange.oauth.provider.validator.v2.maxVersion", "2.0").trim());
        return new DatabaseOAuth2Validator(maxTimestampAgeMsec, maxVersion);
    }

    @Override
    public OAuth2Validator getValidator() {
        return validator;
    }

    @Override
    public OAuth2Client getClient(final OAuth2Message requestMessage) throws IOException, OAuth2ProblemException {
        final String clientId = requestMessage.getClientId();
        if (null == clientId) {
            final OAuth2ProblemException exception = new OAuth2ProblemException("parameter_absent");
            exception.setParameter("oauth_parameters_absent", "client_id");
            throw exception;
        }
        final OAuth2Client client = clients.get(clientId);
        if (client == null) {
            final OAuth2ProblemException problem = new OAuth2ProblemException(BaseErrorCode.INVALID_CLIENT);
            if (requestMessage.getParameter(OAuth2.STATE) != null) {
                problem.setParameter(OAuth2.STATE, requestMessage.getParameter(OAuth2.STATE));
            }
            throw problem;
        }
        return client;
    }

    @Override
    public OAuth2Client getClientFromAuthHeader(final OAuth2Message requestMessage) throws IOException, OAuth2ProblemException {
        OAuth2Client client = null;
        /*
         * Try to load from local cache if not throw exception
         */
        final String authz = requestMessage.getHeader("Authorization");
        if (authz != null) {
            if (authz.substring(0, 5).equals("Basic")) {
                final String userPass = new String(Base64.decodeBase64(authz.substring(6).getBytes()), "UTF-8");
                final int loc = userPass.indexOf(":");
                if (loc == -1) {
                    throw new OAuth2ProblemException(BaseErrorCode.INVALID_CLIENT);
                }
                final String userPassedIn = userPass.substring(0, loc);
                final String user = userPassedIn;
                final String pass = userPass.substring(loc + 1);
                if (user != null && pass != null) {
                    client = clients.get(user);
                }
            }
        }
        if (client == null) {
            throw new OAuth2ProblemException(BaseErrorCode.INVALID_CLIENT);
        }
        return client;
    }

    @Override
    public OAuth2Accessor getAccessorByCode(final OAuth2Message requestMessage) throws IOException, OAuth2ProblemException {
        /*
         * Try to load from local cache if not throw exception
         */
        final String code = requestMessage.getCode();
        if (code == null) {
            final OAuth2ProblemException problem = new OAuth2ProblemException(BaseErrorCode.INVALID_REQUEST);
            throw problem;
        }
        OAuth2Accessor accessor = null;
        for (final OAuth2Accessor a : tokens.keySet()) {
            if (a.code != null) {
                if (a.code.equals(code)) {
                    accessor = a;
                    break;
                }
            }
        }
        if (accessor == null) {
            throw new OAuth2ProblemException(BaseErrorCode.INVALID_REQUEST);
        }
        return accessor;
    }

    @Override
    public OAuth2Accessor getAccessorByRefreshToken(final OAuth2Message requestMessage) throws IOException, OAuth2ProblemException {
        /*
         * Try to load from local cache if not throw exception
         */
        final String refreshToken = requestMessage.getParameter(OAuth2.REFRESH_TOKEN);
        if (refreshToken == null) {
            throw new OAuth2ProblemException(BaseErrorCode.INVALID_REQUEST);
        }
        OAuth2Accessor accessor = null;
        for (final OAuth2Accessor a : tokens.keySet()) {
            if (a.refreshToken != null) {
                if (a.refreshToken.equals(refreshToken)) {
                    accessor = a;
                    break;
                }
            }
        }
        if (accessor == null) {
            throw new OAuth2ProblemException(BaseErrorCode.INVALID_GRANT);
        }
        return accessor;
    }

    @Override
    public void markAsAuthorized(final OAuth2Accessor accessor, final int userId, final int contextId) throws OXException {
        // Set properties
        accessor.setProperty(PROP_AUTHORIZED, Boolean.TRUE);
        accessor.setProperty(PROP_USER, Integer.valueOf(userId));
        accessor.setProperty(PROP_CONTEXT, Integer.valueOf(contextId));
        // INSERT
        insertOAuth2Accessor(accessor, userId, contextId);
        // Update token in local cache
        tokens.put(accessor, PRESENT);
    }

    @Override
    public void generateAccessAndRefreshToken(final OAuth2Accessor accessor, final int userId, final int contextId) throws OXException {
        /*
         * Generate access token and refresh token
         */
        final String client_id = accessor.client.clientId;
        final String redirect_uri = accessor.client.redirectUri;
        /*
         * For now use md5 of client_id + current time as token
         */
        final String accessTokenData = client_id + System.nanoTime();
        final String accessToken = DigestUtils.md5Hex(accessTokenData);
        final String refreshTokenData = redirect_uri + System.nanoTime();
        final String refreshToken = DigestUtils.md5Hex(refreshTokenData);
        accessor.accessToken = accessToken;
        accessor.tokenType = "bearer";
        accessor.refreshToken = refreshToken;
        // Update token in local cache
        tokens.put(accessor, PRESENT);
        // Update in database
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE oauth2Accessor SET refreshToken=?, accessToken=? WHERE cid=? AND user=? AND clientId=?");
            stmt.setString(1, refreshToken);
            stmt.setString(2, accessToken);
            stmt.setInt(3, contextId);
            stmt.setInt(4, userId);
            stmt.setInt(5, accessor.client.<Integer> getProperty(PROP_ID).intValue());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

    @Override
    public void generateCode(final OAuth2Accessor accessor, final int userId, final int contextId) throws OXException {
        /*
         * Generate authorization code
         */
        final String clientId = accessor.client.clientId;
        /*
         * For now use md5 of client_id + current time as token
         */
        final String codeData = clientId + System.nanoTime();
        final String code = DigestUtils.md5Hex(codeData);
        accessor.code = code;
        // Update token in local cache
        tokens.put(accessor, PRESENT);
        // Update in database
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE oauth2Accessor SET code=? WHERE cid=? AND user=? AND clientId=?");
            stmt.setString(1, code);
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            stmt.setInt(4, accessor.client.<Integer> getProperty(PROP_ID).intValue());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }

    }

    public static void handleException(final Exception e, final HttpServletRequest request, final HttpServletResponse response, final boolean sendBodyInJson, final boolean withAuthHeader) throws IOException, ServletException {
        final String realm = null;
        OAuth2Servlet.handleException(request, response, e, realm, sendBodyInJson, withAuthHeader);
    }

    private void insertOAuth2Accessor(final OAuth2Accessor accessor, final int userId, final int contextId) throws OXException {
        // Add to database
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            DBUtils.startTransaction(con);
            stmt =
                con.prepareStatement("INSERT INTO oauth2Accessor (cid,user,clientId,providerId,refreshToken,accessToken,expiresIn,tokenType,scope,state,code) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, accessor.client.<Integer> getProperty(PROP_ID).intValue());
            stmt.setInt(4, accessor.client.<Integer> getProperty(PROP_PROVIDER_ID).intValue());
            stmt.setString(5, accessor.refreshToken);
            stmt.setNull(6, Types.VARCHAR);
            stmt.setString(7, accessor.expires_in);
            stmt.setString(8, accessor.tokenType);
            stmt.setString(9, accessor.scope);
            stmt.setString(10, accessor.state);
            stmt.setString(11, accessor.code);
            stmt.executeUpdate();
            /*
             * Properties, too
             */
            DBUtils.closeSQLStuff(stmt);
            stmt = con.prepareStatement("INSERT INTO oauth2AccessorProperty (cid,user,clientId,name,value) VALUES (?,?,?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, accessor.client.<Integer> getProperty(PROP_ID).intValue());
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
