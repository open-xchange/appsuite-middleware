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

package com.openexchange.oauth.provider.internal.authcode;

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.Scope;
import com.openexchange.oauth.provider.tools.UserizedToken;
import com.openexchange.server.ServiceLookup;


/**
 * {@link DbAuthorizationCodeProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DbAuthorizationCodeProvider extends AbstractAuthorizationCodeProvider {

    /**
     * Initializes a new {@link DbAuthorizationCodeProvider}.
     */
    public DbAuthorizationCodeProvider(ServiceLookup services) {
        super(services);
    }

    private DatabaseService getDbService() throws OXException {
        return requireService(DatabaseService.class, services);
    }

    @Override
    public String generateAuthorizationCodeFor(String clientId, String redirectURI, Scope scope, int userId, int contextId) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        try {
            return generateAuthorizationCodeFor(clientId, redirectURI, scope, userId, contextId, con);
        } finally {
            dbService.backWritable(contextId, con);
        }
    }

    private String generateAuthorizationCodeFor(String clientId, String redirectURI, Scope scope, int userId, int contextId, Connection con) throws OXException {
        String authCode = new UserizedToken(userId, contextId).getToken();
        long now = System.nanoTime();

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO authCode (code, cid, user, clientId, redirectURI, scope, nanos) VALUES (?, ?, ?, ?, ?, ?, ?)");
            int pos = 1;
            stmt.setString(pos++, authCode);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, clientId);
            stmt.setString(pos++, redirectURI);
            if (null == scope) {
                stmt.setNull(pos++, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(pos++, scope.scopeString());
            }
            stmt.setLong(pos, now);
            stmt.executeUpdate();
            return authCode;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public AuthCodeInfo redeemAuthCode(Client client, String authCode) throws OXException {
        UserizedToken token = new UserizedToken(authCode);
        int contextId = token.getContextId();
        DatabaseService dbService = getDbService();

        AuthCodeInfo authCodeInfo = null;

        Connection con = dbService.getWritable(contextId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            authCodeInfo = redeemAuthCode(client, token, con);

            con.commit();
            rollback = false;

            return authCodeInfo;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            if (null == authCodeInfo) {
                dbService.backWritableAfterReading(contextId, con);
            } else {
                dbService.backWritable(contextId, con);
            }
        }
    }

    private AuthCodeInfo redeemAuthCode(Client client, UserizedToken authCode, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, clientId, redirectURI, scope, nanos FROM authCode WHERE cid = ? AND user = ? AND code = ?");
            stmt.setInt(1, authCode.getContextId());
            stmt.setInt(2, authCode.getUserId());
            stmt.setString(3, authCode.getToken());
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return null;
            }

            // Read values
            int contextId = rs.getInt(1);
            int userId = rs.getInt(2);
            String clientId = rs.getString(3);
            String redirectURI = rs.getString(4);
            String sScope = rs.getString(5);
            long nanos = rs.getLong(6);

            // Delete entry
            if (false == dropAuthorizationCodeFor(authCode, con)) {
                // Redeemed by another thread in the meantime
                return null;
            }

            // Perform check
            AuthCodeInfo authCodeInfo = new AuthCodeInfo(clientId, redirectURI, sScope, userId, contextId, nanos);
            return authCodeInfo;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private boolean dropAuthorizationCodeFor(UserizedToken authCode, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM authCode WHERE code=?");
            stmt.setString(1, authCode.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
