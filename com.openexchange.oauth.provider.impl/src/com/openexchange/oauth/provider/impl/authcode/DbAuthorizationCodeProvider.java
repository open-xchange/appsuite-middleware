/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.oauth.provider.impl.authcode;

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.impl.tools.UserizedToken;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.server.ServiceLookup;


/**
 * {@link DbAuthorizationCodeProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DbAuthorizationCodeProvider extends AbstractAuthorizationCodeProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DbAuthorizationCodeProvider}.
     */
    public DbAuthorizationCodeProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    private DatabaseService getDbService() throws OXException {
        return requireService(DatabaseService.class, services);
    }

    @Override
    public void put(AuthCodeInfo authCodeInfo) throws OXException {
        DatabaseService dbService = getDbService();
        int contextId = authCodeInfo.getContextId();
        Connection con = dbService.getWritable(contextId);
        try {
            put(authCodeInfo, con);
        } finally {
            dbService.backWritable(contextId, con);
        }
    }

    private void put(AuthCodeInfo authCodeInfo, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO authCode (code, cid, user, clientId, redirectURI, scope, nanos) VALUES (?, ?, ?, ?, ?, ?, ?)");
            int pos = 1;
            stmt.setString(pos++, authCodeInfo.getAuthCode());
            stmt.setInt(pos++, authCodeInfo.getContextId());
            stmt.setInt(pos++, authCodeInfo.getUserId());
            stmt.setString(pos++, authCodeInfo.getClientId());
            stmt.setString(pos++, authCodeInfo.getRedirectURI());
            Scope scope = authCodeInfo.getScope();
            if (null == scope) {
                stmt.setNull(pos++, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(pos++, scope.toString());
            }
            stmt.setLong(pos, authCodeInfo.getTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public AuthCodeInfo remove(String authCode) throws OXException {
        DatabaseService dbService = getDbService();
        AuthCodeInfo authCodeInfo = null;
        UserizedToken parsedCode = UserizedToken.parse(authCode);
        Connection con = dbService.getWritable(parsedCode.getContextId());
        int rollback = 0;
        try {
            Databases.startTransaction(con);
            rollback = 1;

            authCodeInfo = redeemAuthCode(parsedCode.getContextId(), parsedCode.getUserId(), authCode, con);

            con.commit();
            rollback = 2;

            return authCodeInfo;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            if (null == authCodeInfo) {
                dbService.backWritableAfterReading(parsedCode.getContextId(), con);
            } else {
                dbService.backWritable(parsedCode.getContextId(), con);
            }
        }
    }

    private AuthCodeInfo redeemAuthCode(int contextId, int userId, String authCode, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT clientId, redirectURI, scope, nanos FROM authCode WHERE cid = ? AND user = ? AND code = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, authCode);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return null;
            }

            // Read values
            String clientId = rs.getString(1);
            String redirectURI = rs.getString(2);
            String scopeStr = rs.getString(3);
            long timestamp = rs.getLong(4);

            // Delete entry
            if (false == dropAuthorizationCodeFor(contextId, userId, authCode, con)) {
                // Redeemed by another thread in the meantime
                return null;
            }

            // Perform check
            AuthCodeInfo authCodeInfo = new AuthCodeInfo(authCode, clientId, redirectURI, Scope.parseScope(scopeStr), userId, contextId, timestamp);
            return authCodeInfo;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    protected String generateAuthCode(int userId, int contextId) {
        String token = UserizedToken.generate(userId, contextId).getToken();
        return token;
    }

    private boolean dropAuthorizationCodeFor(int contextId, int userId, String authCode, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM authCode WHERE code=? AND cid=? AND user=?");
            stmt.setString(1, authCode);
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
