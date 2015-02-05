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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal.grant;

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.OAuthGrant;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.tools.UserizedToken;
import com.openexchange.server.ServiceLookup;


/**
 * {@link DbGrantStorage}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DbGrantStorage implements OAuthGrantStorage {

    private final ServiceLookup services;

    public DbGrantStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void persistGrant(OAuthGrant grant) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getWritable();
        PreparedStatement select = null;
        PreparedStatement save = null;
        ResultSet rs = null;
        try {
            Databases.startTransaction(con);
            select = con.prepareStatement("SELECT 1 FROM oauth_grant WHERE cid = ? AND uid = ? AND refresh_token = ? AND client = ? FOR UPDATE");
            select.setInt(1, grant.getContextId());
            select.setInt(2, grant.getUserId());
            select.setString(3, grant.getRefreshToken());
            select.setString(4, grant.getClientId());
            rs = select.executeQuery();
            long now = System.currentTimeMillis();
            if (rs.next()) {
                save = con.prepareStatement("UPDATE oauth_grant SET refresh_token = ?, access_token = ?, expiration_date = ?, scopes = ?, last_modified = ? WHERE cid = ? AND uid = ? AND refresh_token = ? AND client = ?");
                save.setString(1, grant.getRefreshToken());
                save.setString(2, grant.getAccessToken());
                save.setLong(3, grant.getExpirationDate().getTime());
                save.setString(4, grant.getScopes().scopeString());
                save.setLong(5, now);
                save.setInt(6, grant.getContextId());
                save.setInt(7, grant.getUserId());
                save.setString(8, grant.getRefreshToken());
                save.setString(9, grant.getClientId());
                save.executeUpdate();
            } else {
                save = con.prepareStatement("INSERT INTO oauth_grant (cid, user, refresh_token, access_token, client, expiration_date, scopes, creation_date, last_modified) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                save.setInt(1, grant.getContextId());
                save.setInt(2, grant.getUserId());
                save.setString(3, grant.getRefreshToken());
                save.setString(4, grant.getAccessToken());
                save.setString(5, grant.getClientId());
                save.setLong(6, grant.getExpirationDate().getTime());
                save.setString(7, grant.getScopes().scopeString());
                save.setLong(8, now);
                save.setLong(9, now);
                save.executeUpdate();
            }

            con.commit();
        } catch (SQLException e) {
            Databases.rollback(con);
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(select);
            Databases.closeSQLStuff(rs);
            Databases.closeSQLStuff(save);
            Databases.autocommit(con);
            dbService.backWritable(con);
        }
    }

    @Override
    public void deleteGrantsForClient(String clientId) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getWritable();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oauth_grant WHERE client = ?");
            stmt.setString(1, clientId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
            dbService.backWritable(con);
        }
    }

    public void deleteGrantsForUserAndClient(int contextId, int userId, String clientId) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getWritable();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oauth_grant WHERE cid = ? AND user = ? AND client = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, clientId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
            dbService.backWritable(con);
        }
    }

    @Override
    public OAuthGrant getGrantByAccessToken(String accessToken) throws OXException {
        UserizedToken userizedToken;
        try {
            userizedToken = new UserizedToken(accessToken);
        } catch (OXException e) {
            if (OAuthProviderExceptionCodes.INVALID_AUTH_CODE.equals(e)) {
                return null;
            }

            throw e;
        }

        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int contextId = userizedToken.getContextId();
            int userId = userizedToken.getUserId();
            stmt = con.prepareStatement("SELECT client, refresh_token, expiration_date, scopes FROM oauth_grant WHERE cid = ? AND user = ? AND access_token = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, accessToken);
            rs = stmt.executeQuery();
            if (rs.next()) {
                OAuthGrantImpl grant = new OAuthGrantImpl(rs.getString(1), contextId, userId);
                grant.setAccessToken(accessToken);
                grant.setRefreshToken(rs.getString(2));
                grant.setExpirationDate(new Date(rs.getLong(3)));
                grant.setScopes(DefaultScopes.parseScope(rs.getString(4)));
                return grant;
            }

            return null;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(con);
        }
    }

    @Override
    public OAuthGrant getGrantByRefreshToken(String refreshToken) throws OXException {
        UserizedToken userizedToken;
        try {
            userizedToken = new UserizedToken(refreshToken);
        } catch (OXException e) {
            if (OAuthProviderExceptionCodes.INVALID_AUTH_CODE.equals(e)) {
                return null;
            }

            throw e;
        }

        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int contextId = userizedToken.getContextId();
            int userId = userizedToken.getUserId();
            stmt = con.prepareStatement("SELECT client, access_token, expiration_date, scopes FROM oauth_grant WHERE cid = ? AND user = ? AND refresh_token = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, refreshToken);
            rs = stmt.executeQuery();
            if (rs.next()) {
                OAuthGrantImpl grant = new OAuthGrantImpl(rs.getString(1), contextId, userId);
                grant.setAccessToken(rs.getString(2));
                grant.setRefreshToken(refreshToken);
                grant.setExpirationDate(new Date(rs.getLong(3)));
                grant.setScopes(DefaultScopes.parseScope(rs.getString(4)));
                return grant;
            }

            return null;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(con);
        }
    }

}
