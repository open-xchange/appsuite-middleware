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

package com.openexchange.snippet.mime;

import static com.openexchange.snippet.mime.Services.getService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.snippet.ReferenceType;
import com.openexchange.snippet.SnippetExceptionCodes;

/**
 * {@link MimeSnippetFileAccess}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class MimeSnippetFileAccess {

    private static final int FS_TYPE = ReferenceType.FILE_STORAGE.getType();

    private static DatabaseService getDatabaseService() {
        return getService(DatabaseService.class);
    }

    /**
     * Retrieves a list of snippets' file references for specified contest
     *
     * @param contextId The context identifier
     * @return A list of file references
     * @throws OXException If file references cannot be returned
     */
    public static List<String> getFiles(int contextId) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final StringBuilder sql;
            sql = new StringBuilder("SELECT refId FROM snippet WHERE cid=? AND refType=").append(FS_TYPE);
            stmt = con.prepareStatement(sql.toString());
            int pos = 0;
            stmt.setInt(++pos, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            do {
                result.add(rs.getString(1));
            } while (rs.next());

            return result;
        } catch (SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }


}
