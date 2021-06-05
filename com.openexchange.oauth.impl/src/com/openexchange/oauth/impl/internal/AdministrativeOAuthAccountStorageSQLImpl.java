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

package com.openexchange.oauth.impl.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.oauth.AdministrativeOAuthAccount;
import com.openexchange.oauth.AdministrativeOAuthAccountStorage;
import com.openexchange.oauth.DefaultAdministrativeOAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AdministrativeOAuthAccountStorageSQLImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class AdministrativeOAuthAccountStorageSQLImpl extends OAuthAccountStorageSQLImpl implements AdministrativeOAuthAccountStorage {

    private static final String SELECT_CONTEXT = "SELECT cid,user,id,serviceId FROM oauthAccounts WHERE cid=?;";
    private static final String SELECT_USER = "SELECT cid,user,id,serviceId FROM oauthAccounts WHERE cid=? AND user=?;";
    private static final String SELECT_CONTEXT_PROVIDER = "SELECT cid,user,id,serviceId FROM oauthAccounts WHERE cid=? AND serviceId=?;";
    private static final String SELECT_USER_PROVIDER = "SELECT cid,user,id,serviceId FROM oauthAccounts WHERE cid=? AND user=? AND serviceId=?;";

    /**
     * Initializes a new {@link AdministrativeOAuthAccountStorageSQLImpl}.
     *
     * @param provider The database provider
     * @param idGenerator The id generator service
     * @param registry The service metadata registry
     * @param contextService The context service
     */
    public AdministrativeOAuthAccountStorageSQLImpl(DBProvider provider, IDGeneratorService idGenerator, OAuthServiceMetaDataRegistry registry, ContextService contextService) {
        super(provider, idGenerator, registry, contextService);
    }

    @Override
    public List<AdministrativeOAuthAccount> listAccounts(int contextId) throws OXException {
        Context context = getContext(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        Connection connection = getConnection(true, context);
        try {
            stmt = connection.prepareStatement(SELECT_CONTEXT);
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            List<AdministrativeOAuthAccount> accounts = new LinkedList<>();
            do {
                DefaultAdministrativeOAuthAccount account = new DefaultAdministrativeOAuthAccount(rs.getInt(1), rs.getInt(2), rs.getString(4));
                account.setId(rs.getInt(3));
                accounts.add(account);
            } while (rs.next());
            return accounts;
        } catch (SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseReadConnection(context, connection);
        }
    }

    @Override
    public List<AdministrativeOAuthAccount> listAccounts(int contextId, int userId) throws OXException {
        Context context = getContext(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        Connection connection = getConnection(true, context);
        try {
            stmt = connection.prepareStatement(SELECT_USER);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            List<AdministrativeOAuthAccount> accounts = new LinkedList<>();
            do {
                DefaultAdministrativeOAuthAccount account = new DefaultAdministrativeOAuthAccount(rs.getInt(1), rs.getInt(2), rs.getString(4));
                account.setId(rs.getInt(3));
                accounts.add(account);
            } while (rs.next());
            return accounts;
        } catch (SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseReadConnection(context, connection);
        }
    }

    @Override
    public List<AdministrativeOAuthAccount> listAccounts(int contextId, String providerId) throws OXException {
        Context context = getContext(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        Connection connection = getConnection(true, context);
        try {
            stmt = connection.prepareStatement(SELECT_CONTEXT_PROVIDER);
            stmt.setInt(1, contextId);
            stmt.setString(2, providerId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            List<AdministrativeOAuthAccount> accounts = new LinkedList<>();
            do {
                DefaultAdministrativeOAuthAccount account = new DefaultAdministrativeOAuthAccount(rs.getInt(1), rs.getInt(2), rs.getString(4));
                account.setId(rs.getInt(3));
                accounts.add(account);
            } while (rs.next());
            return accounts;
        } catch (SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseReadConnection(context, connection);
        }
    }

    @Override
    public List<AdministrativeOAuthAccount> listAccounts(int contextId, int userId, String providerId) throws OXException {
        Context context = getContext(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        Connection connection = getConnection(true, context);
        try {
            stmt = connection.prepareStatement(SELECT_USER_PROVIDER);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, providerId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            List<AdministrativeOAuthAccount> accounts = new LinkedList<>();
            do {
                DefaultAdministrativeOAuthAccount account = new DefaultAdministrativeOAuthAccount(rs.getInt(1), rs.getInt(2), rs.getString(4));
                account.setId(rs.getInt(3));
                accounts.add(account);
            } while (rs.next());
            return accounts;
        } catch (SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseReadConnection(context, connection);
        }
    }

    @Override
    public boolean deleteAccount(int contextId, int userId, int accountId) throws OXException {
        return deleteAccount(ServerSessionAdapter.valueOf(userId, contextId), accountId);
    }

    @Override
    public boolean deleteAccount(int contextId, int userId, int accountId, Connection connection) throws OXException {
        try {
            DeleteListenerRegistry deleteListenerRegistry = DeleteListenerRegistry.getInstance();
            Map<String, Object> properties = new HashMap<>(2);
            // Hint to not update the scopes since it's an OAuth account deletion
            // This hint has to be passed via the delete listener
            properties.put(OAuthConstants.SESSION_PARAM_UPDATE_SCOPES, Boolean.FALSE);

            deleteListenerRegistry.triggerOnBeforeDeletion(accountId, properties, userId, contextId, connection);
            boolean deleted = deleteAccount(ServerSessionAdapter.valueOf(userId, contextId), accountId, connection);
            deleteListenerRegistry.triggerOnAfterDeletion(accountId, properties, userId, contextId, connection);
            return deleted;
        } catch (SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }
}
