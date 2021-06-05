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
package com.openexchange.report.appsuite.storage;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.user.UserExceptionCode;

/**
 * The {@link ContextLoader} class is used to load data from the database, that is needed
 * by the report creation functions.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.2
 */
public class ContextLoader {

    /**
     * Initializes a new {@link ContextLoader}.
     */
    private ContextLoader() {
        super();
    }

    /**
     * Loads all context identifiers that are in the same schema, as the given context identifier. The given context identifier
     * will be also returned in the list.
     *
     * @param cid The context identifier
     * @return A list with all context identifiers in the same schema
     * @throws OXException If listing cannot be returned
     */
    public static List<Integer> getAllContextIdsInSameSchema(int cid) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getReadOnly();
        try {
            return getAllContextIdsInSameSchema(cid, con);
        } finally {
            dbService.backReadOnly(con);
        }
    }

    private static List<Integer> getAllContextIdsInSameSchema(int cid, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet sqlResult = null;
        try {
            stmt = con.prepareStatement("SELECT t2.cid FROM context_server2db_pool AS t1 JOIN context_server2db_pool AS t2 ON t1.db_schema=t2.db_schema WHERE t1.cid=?");
            stmt.setInt(1, cid);
            sqlResult = stmt.executeQuery();
            if (!sqlResult.next()) {
                return Collections.emptyList();
            }

            List<Integer> result = new ArrayList<>();
            do {
                result.add(I(sqlResult.getInt(1)));
            } while (sqlResult.next());
            return result;
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(sqlResult, stmt);
        }
    }

}
