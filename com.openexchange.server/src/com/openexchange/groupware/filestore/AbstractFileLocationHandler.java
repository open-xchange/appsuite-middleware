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

package com.openexchange.groupware.filestore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.database.Databases;

/**
 * {@link AbstractFileLocationHandler} - The abstract file location handler.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public abstract class AbstractFileLocationHandler implements FileLocationHandler {

    /** The limitation for SQL IN values. */
    protected static final int IN_LIMIT = Databases.IN_LIMIT;

    /**
     * Initializes a new {@link AbstractFileLocationHandler}.
     */
    protected AbstractFileLocationHandler() {
        super();
    }

    /**
     * Generates an SQL IN string with specified number of placeholders.
     *
     * @param size The number of placeholders
     * @return The SQL IN string
     */
    protected static String getSqlInStringFor(int size) {
        StringBuilder sb = new StringBuilder(size << 1);
        sb.append("(?");

        for (int i = size-1; i-- > 0;) {
            sb.append(", ?");
        }
        sb.append(')');

        return sb.toString();
    }

    /**
     * Updates the file locations using given statements.
     *
     * @param prevFileName2newFileName The file name mappings
     * @param contextId The context identifier
     * @param selectStmt The SELECT statement; e.g. <code>"SELECT file_id FROM prg_attachment WHERE cid=? AND file_id IN "</code>
     * @param updateStmt The UPDATE statement; e.g. <code>"UPDATE prg_attachment SET file_id = ? WHERE cid = ? AND file_id = ?"</code>
     * @param con The connection to use
     * @throws SQLException If an SQL error occurs
     */
    protected static void updateFileLocationsUsing(Map<String, String> prevFileName2newFileName, int contextId, String selectStmt, String updateStmt, Connection con) throws SQLException {
        int size = prevFileName2newFileName.size();
        if (size <= 0) {
            return;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Iterator<String> allPrevFileNames = prevFileName2newFileName.keySet().iterator();
            for (int i = 0; i < size; i += IN_LIMIT) {
                int chunkSizeSize = i + IN_LIMIT > size ? size - i : IN_LIMIT;

                List<String> chunkPrevFileNames = new ArrayList<String>(chunkSizeSize);
                for (int j = chunkSizeSize; j-- > 0;) {
                    chunkPrevFileNames.add(allPrevFileNames.next());
                }

                stmt = con.prepareStatement(selectStmt + getSqlInStringFor(chunkSizeSize));
                // Fill values
                int pos = 0;
                stmt.setInt(++pos, contextId);
                {
                    Iterator<String> chunkIter = chunkPrevFileNames.iterator();
                    for (int j = chunkSizeSize; j-- > 0;) {
                        stmt.setString(++pos, chunkIter.next());
                    }
                }

                // Query for existent file names (if any)
                rs = stmt.executeQuery();
                if (rs.next()) {
                    // Collect existent file names
                    List<String> existent = chunkPrevFileNames;
                    existent.clear();
                    chunkPrevFileNames = null;
                    do {
                        existent.add(rs.getString(1));
                    } while (rs.next());
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;

                    stmt = con.prepareStatement(updateStmt);
                    for (String prevFileName : existent) {
                        String newFileName = prevFileName2newFileName.get(prevFileName);
                        stmt.setString(1, newFileName);
                        stmt.setInt(2, contextId);
                        stmt.setString(3, prevFileName);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
                Databases.closeSQLStuff(rs, stmt);
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
