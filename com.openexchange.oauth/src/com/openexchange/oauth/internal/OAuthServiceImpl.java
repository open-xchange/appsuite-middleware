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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.oauth.internal;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.Command;
import com.openexchange.sql.grammar.INSERT;


/**
 * An {@link OAuthService} Implementation using the RDB for storage and Scribe OAuth library for the OAuth interaction.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OAuthServiceImpl implements OAuthService {

    private static final Log LOG = LogFactory.getLog(OAuthServiceImpl.class);
    
    private final OAuthServiceMetaDataRegistry registry;

    private final DBProvider provider;

    private final IDGeneratorService idGenerator;

    private ContextService contexts;

    /**
     * Initializes a new {@link OAuthServiceImpl}.
     * 
     * @param provider
     * @param simIDGenerator
     */
    public OAuthServiceImpl(final DBProvider provider, final IDGeneratorService idGenerator, final OAuthServiceMetaDataRegistry registry, ContextService contexts) {
        super();
        this.registry = registry;
        this.provider = provider;
        this.idGenerator = idGenerator;
        this.contexts = contexts;
    }

    public OAuthServiceMetaDataRegistry getMetaDataRegistry() {
        return registry;
    }

    public List<OAuthAccount> getAccounts(final int user, final int contextId) throws OAuthException {
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, displayName, accessToken, accessSecret, serviceId FROM oauthAccounts WHERE cid = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<OAuthAccount> accounts = new ArrayList<OAuthAccount>(8);
            do {
                final DefaultOAuthAccount account = new DefaultOAuthAccount();
                account.setId(rs.getInt(1));
                account.setDisplayName(rs.getString(2));
                account.setToken(rs.getString(3));
                account.setSecret(rs.getString(4));
                account.setMetaData(registry.getService(rs.getString(5)));
                accounts.add(account);
            } while (rs.next());
            return accounts;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    public List<OAuthAccount> getAccounts(final String serviceMetaData, final int user, final int contextId) throws OAuthException {
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, displayName, accessToken, accessSecret FROM oauthAccounts WHERE cid = ? AND user = ? AND serviceId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            stmt.setString(3, serviceMetaData);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<OAuthAccount> accounts = new ArrayList<OAuthAccount>(8);
            do {
                final DefaultOAuthAccount account = new DefaultOAuthAccount();
                account.setId(rs.getInt(1));
                account.setDisplayName(rs.getString(2));
                account.setToken(rs.getString(3));
                account.setSecret(rs.getString(4));
                account.setMetaData(registry.getService(serviceMetaData));
                accounts.add(account);
            } while (rs.next());
            return accounts;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    public OAuthInteraction initOAuth(final String serviceMetaData) throws OAuthException {
        /*
         * TODO: Fix this stuff
         */
        final OAuthServiceMetaData metaData = registry.getService(serviceMetaData);
        final String apiKey = metaData.getAPIKey();
        final String apiSecret = metaData.getAPISecret();
        final String authorizationURL = metaData.getAuthorizationURL(new OAuthToken() {
            
            public String getToken() {
                return apiKey;
            }
            
            public String getSecret() {
                return apiSecret;
            }
        });
        /*
         * Return out-of-band interaction
         */
        return new OAuthInteraction() {
            
            public OAuthToken getRequestToken() {
                // TODO Auto-generated method stub
                return null;
            }
            
            public OAuthInteractionType getInteractionType() {
                return OAuthInteractionType.OUT_OF_BAND;
            }
            
            public String getAuthorizationURL() {
                return authorizationURL;
            }
        };
    }

    public OAuthAccount createAccount(String serviceMetaData, OAuthInteractionType type, Map<String, Object> arguments, int user, int contextId) throws OAuthException {
        try {
            DefaultOAuthAccount account = new DefaultOAuthAccount();
            
            OAuthServiceMetaData service = registry.getService(serviceMetaData);
            account.setMetaData(service);

            Object displayName = arguments.get(OAuthConstants.ARGUMENT_DISPLAY_NAME);
            if(displayName == null) {
                displayName = service.getDisplayName();
            }
            account.setDisplayName(displayName.toString());
            account.setId(idGenerator.getId(OAuthConstants.TYPE_ACCOUNT, contextId));
            
            obtainToken(type, arguments, account);
            
            ArrayList<Object> values = new ArrayList<Object>(SQLStructure.OAUTH_COLUMN.values().length);
            INSERT insert = SQLStructure.INSERT_ACCOUNT(account, contextId, user, values);
            
            executeUpdate(contextId, insert, values);
            
            return account;
        } catch (OAuthException x) {
            throw x;
        } catch (AbstractOXException x) {
            throw new OAuthException(x);
        }
    }


    private void executeUpdate(int contextId, Command command, List<Object> values) throws OAuthException {
        Connection writeCon = null;
        Context ctx = null;
        try {
            ctx = contexts.getContext(contextId);
            writeCon = provider.getWriteConnection(ctx);
            new StatementBuilder().executeStatement(writeCon, command, values);
        } catch (DBPoolingException e) {
            throw new OAuthException(e);
        } catch (SQLException e) {
            LOG.error(e);
            throw OAuthExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } catch (ContextException e) {
            throw new OAuthException(e);
        } finally {
            if(writeCon != null) {
                provider.releaseWriteConnection(ctx, writeCon);
            }
        }
    }

    public void deleteAccount(final int accountId, final int user, final int contextId) throws OAuthException {
        final Context context = getContext(contextId);
        final Connection con = getConnection(false, context);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oauthAccounts WHERE cid = ? AND user = ? and id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            stmt.setInt(3, accountId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    public void updateAccount(final int accountId, final Map<String, Object> arguments, final int user, final int contextId) throws OAuthException {
        final List<Setter> list = setterFrom(arguments);
        if (list.isEmpty()) {
            /*
             * Nothing to update
             */
            return;
        }
        final Context context = getContext(contextId);
        final Connection con = getConnection(false, context);
        PreparedStatement stmt = null;
        try {
            final StringBuilder stmtBuilder = new StringBuilder(128).append("UPDATE oauthAccounts SET ");
            final int size = list.size();
            list.get(0).appendTo(stmtBuilder);
            for (int i = 1; i < size; i++) {
                stmtBuilder.append(", ");
                list.get(i).appendTo(stmtBuilder);
            }
            stmt = con.prepareStatement(stmtBuilder.append(" WHERE cid = ? AND user = ? and id = ?").toString());
            int pos = 1;
            for (final Setter setter : list) {
                pos = setter.set(pos, stmt);
            }
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, user);
            stmt.setInt(pos, accountId);
            final int rows = stmt.executeUpdate();
            if (rows <= 0) {
                throw OAuthExceptionCodes.ACCOUNT_NOT_FOUND.create(
                    Integer.valueOf(accountId),
                    Integer.valueOf(user),
                    Integer.valueOf(contextId));
            }
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    public OAuthAccount getAccount(final int accountId, final int user, final int contextId) throws OAuthException {
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT displayName, accessToken, accessSecret, serviceId FROM oauthAccounts WHERE cid = ? AND user = ? and id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            stmt.setInt(3, accountId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw OAuthExceptionCodes.ACCOUNT_NOT_FOUND.create(
                    Integer.valueOf(accountId),
                    Integer.valueOf(user),
                    Integer.valueOf(contextId));
            }
            final DefaultOAuthAccount account = new DefaultOAuthAccount();
            account.setId(accountId);
            account.setDisplayName(rs.getString(1));
            account.setToken(rs.getString(2));
            account.setSecret(rs.getString(3));
            account.setMetaData(registry.getService(rs.getString(4)));
            return account;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    // OAuth

    protected void obtainToken(final OAuthInteractionType type, final Map<String, Object> arguments, final DefaultOAuthAccount account) {

    }

    // Helper Methods

    private Connection getConnection(final boolean readOnly, final Context context) throws OAuthException {
        try {
            return readOnly ? provider.getReadConnection(context) : provider.getWriteConnection(context);
        } catch (final DBPoolingException e) {
            throw new OAuthException(e);
        }
    }

    private Context getContext(final int contextId) throws OAuthException {
        try {
            return contexts.getContext(contextId);
        } catch (final ContextException e) {
            throw new OAuthException(e);
        }
    }

    private static interface Setter {

        void appendTo(StringBuilder stmtBuilder);

        int set(int pos, PreparedStatement stmt) throws SQLException;
    }

    private static List<Setter> setterFrom(final Map<String, Object> arguments) {
        final List<Setter> ret = new ArrayList<Setter>(4);
        /*
         * Check for display name
         */
        final String displayName = (String) arguments.get(OAuthConstants.ARGUMENT_DISPLAY_NAME);
        if (null != displayName) {
            ret.add(new Setter() {

                public int set(final int pos, final PreparedStatement stmt) throws SQLException {
                    stmt.setString(pos, displayName);
                    return pos + 1;
                }

                public void appendTo(final StringBuilder stmtBuilder) {
                    stmtBuilder.append("displayName = ?");
                }
            });
        }
        /*
         * Check for request token
         */
        final OAuthToken token = (OAuthToken) arguments.get(OAuthConstants.ARGUMENT_REQUEST_TOKEN);
        if (null != token) {
            final String sToken = token.getToken();
            final String secret = token.getSecret();
            ret.add(new Setter() {

                public int set(final int pos, final PreparedStatement stmt) throws SQLException {
                    stmt.setString(pos, sToken);
                    stmt.setString(pos + 1, secret);
                    return pos + 2;
                }

                public void appendTo(final StringBuilder stmtBuilder) {
                    stmtBuilder.append("accessToken = ?, accessSecret = ?");
                }
            });
        }
        /*
         * Other arguments?
         */
        return ret;
    }

}
