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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.oauth.provider.impl.grant;

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.impl.tools.UserizedToken;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.update.Tools;


/**
 * {@link DbGrantStorage}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DbGrantStorage implements OAuthGrantStorage {

    private static final Logger LOG = LoggerFactory.getLogger(DbGrantStorage.class);
    private final ServiceLookup services;

    public DbGrantStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void saveGrant(StoredGrant grant) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getWritable(grant.getContextId());
        PreparedStatement stmt = null;
        try {
            // ensure space
            stmt = con.prepareStatement("SELECT last_modified FROM oauth_grant WHERE client = ? AND cid = ? AND user = ? ORDER BY last_modified ASC");
            stmt.setString(1, grant.getClientId());
            stmt.setInt(2, grant.getContextId());
            stmt.setInt(3, grant.getUserId());
            ResultSet rs = stmt.executeQuery();

            List<Long> lms = new ArrayList<>(MAX_GRANTS_PER_CLIENT);
            while (rs.next()) {
                lms.add(rs.getLong(1));
            }

            Databases.closeSQLStuff(rs, stmt);
            if (lms.size() >= MAX_GRANTS_PER_CLIENT) {
                int index = ((lms.size() - (MAX_GRANTS_PER_CLIENT - 1)) - 1); // Get the most recent last_modified that needs to be deleted (LRU)
                long minLastModified = lms.get(index);
                stmt = con.prepareStatement("DELETE FROM oauth_grant WHERE client = ? AND cid = ? AND user = ? AND last_modified <= ?");
                stmt.setString(1, grant.getClientId());
                stmt.setInt(2, grant.getContextId());
                stmt.setInt(3, grant.getUserId());
                stmt.setLong(4, minLastModified);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
            }

            long now = System.currentTimeMillis();
            stmt = con.prepareStatement("INSERT INTO oauth_grant (cid, user, refresh_token, access_token, client, expiration_date, scopes, creation_date, last_modified) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setInt(1, grant.getContextId());
            stmt.setInt(2, grant.getUserId());
            stmt.setString(3, grant.getRefreshToken().getBaseToken());
            stmt.setString(4, grant.getAccessToken().getBaseToken());
            stmt.setString(5, grant.getClientId());
            stmt.setLong(6, grant.getExpirationDate().getTime());
            stmt.setString(7, grant.getScope().toString());
            stmt.setLong(8, now);
            stmt.setLong(9, now);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
            dbService.backWritable(grant.getContextId(), con);
        }
    }

    @Override
    public void updateGrant(UserizedToken refreshToken, StoredGrant grant) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getWritable(grant.getContextId());
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE oauth_grant SET refresh_token = ?, access_token = ?, expiration_date = ?, scopes = ?, last_modified = ? WHERE refresh_token = ? AND client = ? AND cid = ? AND user = ?");
            stmt.setString(1, grant.getRefreshToken().getBaseToken());
            stmt.setString(2, grant.getAccessToken().getBaseToken());
            stmt.setLong(3, grant.getExpirationDate().getTime());
            stmt.setString(4, grant.getScope().toString());
            stmt.setLong(5, System.currentTimeMillis());
            stmt.setString(6, refreshToken.getBaseToken());
            stmt.setString(7, grant.getClientId());
            stmt.setInt(8, grant.getContextId());
            stmt.setInt(9, grant.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
            dbService.backWritable(grant.getContextId(), con);
        }
    }

    @Override
    public boolean deleteGrantByRefreshToken(UserizedToken refreshToken) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getWritable(refreshToken.getContextId());
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oauth_grant WHERE refresh_token = ? AND cid = ? AND user = ?");
            stmt.setString(1, refreshToken.getBaseToken());
            stmt.setInt(2, refreshToken.getContextId());
            stmt.setInt(3, refreshToken.getUserId());
            int numRows = stmt.executeUpdate();
            return numRows > 0;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
            dbService.backWritable(refreshToken.getContextId(), con);
        }
    }

    @Override
    public boolean deleteGrantByAccessToken(UserizedToken accessToken) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getWritable(accessToken.getContextId());
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oauth_grant WHERE access_token = ? AND cid = ? AND user = ?");
            stmt.setString(1, accessToken.getBaseToken());
            stmt.setInt(2, accessToken.getContextId());
            stmt.setInt(3, accessToken.getUserId());
            int numRows = stmt.executeUpdate();
            return numRows > 0;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
            dbService.backWritable(accessToken.getContextId(), con);
        }
    }

    @Override
    public void deleteGrantsByClientId(String clientId) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Deque<SchemaAndWritePool> schemasAndWritePools = getSchemasAndWritePools(dbService);
        while (!schemasAndWritePools.isEmpty()) {
            SchemaAndWritePool schemaAndWritePool = schemasAndWritePools.removeFirst();
            schemaAndWritePool.incRetryCount();
            Connection con = dbService.get(schemaAndWritePool.getWritePool(), schemaAndWritePool.getSchema());
            PreparedStatement stmt = null;
            try {
                if (Tools.tableExists(con, "oauth_grant")) {
                    stmt = con.prepareStatement("DELETE FROM oauth_grant WHERE client = ?");
                    stmt.setString(1, clientId);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                if (schemaAndWritePool.getRetryCount() >= 3) {
                    schemasAndWritePools.addLast(schemaAndWritePool);
                } else {
                    LOG.error("Could not delete OAuth grants for client {} in database {} after 3 tries", clientId, schemaAndWritePool, e);
                }
            } finally {
                Databases.closeSQLStuff(stmt);
                dbService.back(schemaAndWritePool.getWritePool(), con);
            }
        }
    }

    @Override
    public StoredGrant getGrantByAccessToken(UserizedToken accessToken) throws OXException {
        int contextId = accessToken.getContextId();
        int userId = accessToken.getUserId();
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT client, refresh_token, expiration_date, scopes, creation_date FROM oauth_grant WHERE access_token = ? AND cid = ? AND user = ?");
            stmt.setString(1, accessToken.getBaseToken());
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                StoredGrant grant = new StoredGrant();
                grant.setContextId(contextId);
                grant.setUserId(userId);
                grant.setClientId(rs.getString(1));
                grant.setAccessToken(accessToken);
                grant.setRefreshToken(new UserizedToken(userId, contextId, rs.getString(2)));
                grant.setExpirationDate(new Date(rs.getLong(3)));
                grant.setScope(Scope.parseScope(rs.getString(4)));
                grant.setCreationDate(new Date(rs.getLong(5)));
                return grant;
            }

            return null;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(contextId, con);
        }
    }

    @Override
    public StoredGrant getGrantByRefreshToken(UserizedToken refreshToken) throws OXException {
        int contextId = refreshToken.getContextId();
        int userId = refreshToken.getUserId();
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT client, access_token, expiration_date, scopes, creation_date FROM oauth_grant WHERE refresh_token = ? AND cid = ? AND user = ?");
            stmt.setString(1, refreshToken.getBaseToken());
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                StoredGrant grant = new StoredGrant();
                grant.setContextId(contextId);
                grant.setUserId(userId);
                grant.setClientId(rs.getString(1));
                grant.setAccessToken(new UserizedToken(userId, contextId, rs.getString(2)));
                grant.setRefreshToken(refreshToken);
                grant.setExpirationDate(new Date(rs.getLong(3)));
                grant.setScope(Scope.parseScope(rs.getString(4)));
                grant.setCreationDate(new Date(rs.getLong(5)));
                return grant;
            }

            return null;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(contextId, con);
        }
    }

    @Override
    public int countDistinctGrants(int contextId, int userId) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT COUNT(DISTINCT client) FROM oauth_grant WHERE cid = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(contextId, con);
        }
    }

    @Override
    public List<StoredGrant> getGrantsForUser(int contextId, int userId) throws OXException {
        List<StoredGrant> grants = new LinkedList<>();
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT client, access_token, refresh_token, expiration_date, scopes, creation_date FROM oauth_grant WHERE cid = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                StoredGrant grant = new StoredGrant();
                grant.setContextId(contextId);
                grant.setUserId(userId);
                grant.setClientId(rs.getString(1));
                grant.setAccessToken(new UserizedToken(userId, contextId, rs.getString(2)));
                grant.setRefreshToken(new UserizedToken(userId, contextId, rs.getString(3)));
                grant.setExpirationDate(new Date(rs.getLong(4)));
                grant.setScope(Scope.parseScope(rs.getString(5)));
                grant.setCreationDate(new Date(rs.getLong(6)));
                grants.add(grant);
            }

            return grants;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(contextId, con);
        }
    }

    @Override
    public void deleteGrantsByClientAndUser(String clientId, int contextId, int userId) throws OXException {
        DatabaseService dbService = requireService(DatabaseService.class, services);
        Connection con = dbService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oauth_grant WHERE client = ? AND cid = ? AND user = ?");
            stmt.setString(1, clientId);
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
            dbService.backWritable(contextId, con);
        }
    }

    private static Deque<SchemaAndWritePool> getSchemasAndWritePools(DatabaseService dbService) throws OXException {
        Deque<SchemaAndWritePool> schemasAndWritePools = new LinkedList<>();
        Connection con = dbService.getReadOnly();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT db_schema, write_db_pool_id FROM context_server2db_pool GROUP BY db_schema");
            while (rs.next()) {
                schemasAndWritePools.add(new SchemaAndWritePool(rs.getString(1), rs.getInt(2)));
            }
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(con);
        }

        return schemasAndWritePools;
    }

    private static final class SchemaAndWritePool {

        private final String schema;

        private final int writePool;

        private int retryCount = 0;

        public SchemaAndWritePool(String schema, int writePool) {
            super();
            this.schema = schema;
            this.writePool = writePool;
        }

        public String getSchema() {
            return schema;
        }

        public int getWritePool() {
            return writePool;
        }
        public int getRetryCount() {
            return retryCount;
        }

        public void incRetryCount() {
            ++retryCount;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((schema == null) ? 0 : schema.hashCode());
            result = prime * result + writePool;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SchemaAndWritePool other = (SchemaAndWritePool) obj;
            if (schema == null) {
                if (other.schema != null) {
                    return false;
                }
            } else if (!schema.equals(other.schema)) {
                return false;
            }
            if (writePool != other.writePool) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "{schema=" + schema + ", writePool=" + writePool + "}";
        }

    }

}
