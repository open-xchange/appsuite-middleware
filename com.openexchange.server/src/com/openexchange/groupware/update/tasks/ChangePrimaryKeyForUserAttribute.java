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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Tools;

/**
 * {@link ChangePrimaryKeyForUserAttribute} - Changes the PRIMARY KEY of the <code>"user_attribute"</code> table from ("cid", "uuid") to ("cid", "id", "name").
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ChangePrimaryKeyForUserAttribute extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link ChangePrimaryKeyForUserAttribute}.
     */
    public ChangePrimaryKeyForUserAttribute() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.AddUUIDForUserAttributeTable.class.getName(), com.openexchange.groupware.update.tasks.RemoveAliasInUserAttributesTable.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        // Get required service
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

        // Initialize connection
        int contextId = params.getContextId();
        Connection con = dbService.getForUpdateTask(contextId);

        // Start task processing
        boolean restoreAutocommit = false;
        boolean rollback = false;
        boolean modified = false;
        try {
            if (false == Tools.existsPrimaryKey(con, "user_attribute", new String[] {"cid", "uuid"})) {
                // PRIMARY KEY already changed
                return;
            }

            DBUtils.startTransaction(con);
            restoreAutocommit = true;
            rollback = true;

            doPerform(con);
            modified = true;

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            if (restoreAutocommit) {
                autocommit(con);
            }
            if (modified) {
                dbService.backForUpdateTask(contextId, con);
            } else {
                dbService.backForUpdateTaskAfterReading(contextId, con);
            }
        }
    }

    private void doPerform(Connection con) throws SQLException {
        // Ensure all "alias" entries in 'user_attribute' table exist in 'user_alias' one
        ensureAllAliasEntriesExist(con);

        // Now drop them
        dropAllAliasEntries(con);

        // Reset PRIMARY KEY
        Tools.dropPrimaryKey(con, "user_attribute");
        Tools.createPrimaryKey(con, "user_attribute", new String[] {"cid", "id", "name"});

        // Drop the 'uuid' column with next version
    }

    private void dropAllAliasEntries(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_attribute WHERE name=?");
            stmt.setString(1, "alias");
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void ensureAllAliasEntriesExist(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id, value, uuid FROM user_attribute WHERE name=?");
            stmt.setString(1, "alias");
            rs = stmt.executeQuery();

            if (!rs.next()) {
                // No remaining "alias" entries left in 'user_attribute' table
                return;
            }

            class Alias {
                final int contextId;
                final int userId;
                final String alias;
                final byte[] uuid;
                private final int hash;

                Alias(int contextId, int userId, String alias, byte[] uuid) {
                    super();
                    this.contextId = contextId;
                    this.userId = userId;
                    this.alias = alias;
                    this.uuid = uuid;
                    int prime = 31;
                    int result = prime * 1 + contextId;
                    result = prime * result + userId;
                    result = prime * result + ((alias == null) ? 0 : alias.hashCode());
                    this.hash = result;
                }

                @Override
                public int hashCode() {
                    return hash;
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
                    Alias other = (Alias) obj;
                    if (contextId != other.contextId) {
                        return false;
                    }
                    if (userId != other.userId) {
                        return false;
                    }
                    if (alias == null) {
                        if (other.alias != null) {
                            return false;
                        }
                    } else if (!alias.equals(other.alias)) {
                        return false;
                    }
                    return true;
                }
            }

            Set<Alias> aliases = new LinkedHashSet<>();
            do {
                aliases.add(new Alias(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getBytes(4)));
            } while (rs.next());

            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            stmt = con.prepareStatement("INSERT INTO user_alias (cid, user, alias, uuid) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE user=user");
            for (Alias alias : aliases) {
                stmt.setInt(1, alias.contextId);
                stmt.setInt(2, alias.userId);
                stmt.setString(3, alias.alias);
                stmt.setBytes(4, alias.uuid);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
