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
