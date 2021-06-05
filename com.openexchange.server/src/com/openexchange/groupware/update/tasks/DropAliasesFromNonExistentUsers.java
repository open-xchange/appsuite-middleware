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

package com.openexchange.groupware.update.tasks;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.UserAndContext;


/**
 * {@link DropAliasesFromNonExistentUsers} - Drops aliases from non-existent users from database.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DropAliasesFromNonExistentUsers extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link DropAliasesFromNonExistentUsers}.
     */
    public DropAliasesFromNonExistentUsers() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            Set<UserAndContext> nonExistentUsers;
            {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = connection.prepareStatement("SELECT user_alias.cid, user_alias.user FROM user_alias LEFT JOIN user ON user.cid=user_alias.cid AND user.id=user_alias.user WHERE user.id IS NULL");
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        nonExistentUsers = new HashSet<>();
                        do {
                            nonExistentUsers.add(UserAndContext.newInstance(rs.getInt(2), rs.getInt(1)));
                        } while (rs.next());
                    } else {
                        nonExistentUsers = Collections.emptySet();
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
            }

            if (nonExistentUsers.isEmpty()) {
                return;
            }

            // Start transaction
            Databases.startTransaction(connection);
            rollback = 1;


            // Remove associated aliases
            UserAliasStorage aliasStorage = ServerServiceRegistry.getInstance().getService(UserAliasStorage.class);
            if (null != aliasStorage) {
                // Use UserAliasStorage instance to remove aliases
                for (UserAndContext userAndContext : nonExistentUsers) {
                    aliasStorage.deleteAliases(connection, userAndContext.getContextId(), userAndContext.getUserId());
                }
            } else {
                // Remove aliases manually
                PreparedStatement stmt = null;
                try {
                    stmt = connection.prepareStatement("DELETE FROM user_alias WHERE cid=? AND user=?");
                    for (UserAndContext userAndContext : nonExistentUsers) {
                        stmt.setInt(1, userAndContext.getContextId());
                        stmt.setInt(2, userAndContext.getUserId());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                } finally {
                    Databases.closeSQLStuff(stmt);
                }

                CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
                if (null != cacheService) {
                    try {
                        Cache cache = cacheService.getCache("UserAlias");
                        List<Serializable> keysToRemove = new ArrayList<>(nonExistentUsers.size());
                        for (UserAndContext userAndContext : nonExistentUsers) {
                            keysToRemove.add(cacheService.newCacheKey(userAndContext.getContextId(), userAndContext.getUserId()));
                        }
                        cache.remove(keysToRemove);
                    } catch (Exception e) {
                        org.slf4j.LoggerFactory.getLogger(DropAliasesFromNonExistentUsers.class).warn("Failed to clean-up \"UserAlias\" cache.", e);
                    }
                }
            }

            // Commit changes
            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                // A transaction has been initialized. Restore auto-commit in any case.
                if (rollback == 1) {
                    // A roll-back needs to be performed, too
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { MigrateUUIDsForUserAliasTable.class.getName() };
    }

}
