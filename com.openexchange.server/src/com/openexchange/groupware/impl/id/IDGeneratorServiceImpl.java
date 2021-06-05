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

package com.openexchange.groupware.impl.id;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.id.IDExceptionCodes;
import com.openexchange.id.IDGeneratorService;

/**
 * {@link IDGeneratorServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IDGeneratorServiceImpl implements IDGeneratorService {

    /**
     * Initializes a new {@link IDGeneratorServiceImpl}.
     */
    public IDGeneratorServiceImpl() {
        super();
    }

    @Override
    public int getId(final String type, final int contextId) throws OXException {
        return getId(type, contextId, 1);
    }

    @Override
    public int getId(final String type, final int contextId, final int minId) throws OXException {
        /*
         * Get appropriate connection
         */
        final Connection con = getWritableConnection(contextId);
        try {
            /*
             * Try to perform an UPDATE
             */
            final int id = getId(type, contextId, minId, con);
            if (id < 0) {
                /*
                 * Failed
                 */
                throw IDExceptionCodes.ID_GEN_FAILED.create();
            }
            /*
             * Return identifier
             */
            return id;
        } catch (SQLException e) {
            throw IDExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (Exception e) {
            throw IDExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Database.back(contextId, true, con);
        }
    }

    private static Connection getWritableConnection(final int contextId) throws OXException {
        return Database.get(contextId, true);
    }

    private static int getId(final String type, final int contextId, final int minId, final Connection con) throws SQLException {
        /*
         * Start
         */
        int retval = -1;
        int retry = 0;
        boolean tryInsert = true;
        while (retval < 0 && retry++ < 5) {
            /*
             * Try to perform an UPDATE
             */
            int cur;
            int increment;
            do {
                cur = performSelect(type, contextId, con);
                if (cur < 0) {
                    if (tryInsert) {
                        if (performInsert(type, contextId, minId, con)) {
                            return minId;
                        }
                        tryInsert = false;
                    }
                    cur = performSelect(type, contextId, con);
                }
                if (cur < minId) {
                    increment = minId - cur;
                } else {
                    increment = 1;
                }
            } while (!compareAndSet(type, contextId, cur, cur + increment, con) && retry++ < 5);
            retval = cur + increment;
            /*
             * Retry...
             */
        }
        /*
         * Return value
         */
        return retval;
    }

    private static boolean compareAndSet(final String type, final int contextId, final int expected, final int newValue, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE sequenceIds SET id = ? WHERE cid = ? AND type = ? AND id = ?");
            stmt.setInt(1, newValue);
            stmt.setInt(2, contextId);
            stmt.setString(3, type);
            stmt.setInt(4, expected);
            final int result = stmt.executeUpdate();
            return (result > 0);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static int performSelect(final String type, final int contextId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM sequenceIds WHERE cid = ? AND type = ?");
            stmt.setInt(1, contextId);
            stmt.setString(2, type);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static boolean performInsert(final String type, final int contextId, final int firstValue, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO sequenceIds (cid, type, id) VALUES (?, ?, ?)");
            stmt.setInt(1, contextId);
            stmt.setString(2, type);
            stmt.setInt(3, firstValue);
            try {
                final int result = stmt.executeUpdate();
                return (result > 0);
            } catch (SQLException e) {
                return false;
            }
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
