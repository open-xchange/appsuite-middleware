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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.search.impl.SearchEngineImpl;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Strings;
import com.openexchange.tools.update.Tools;
import com.planetj.math.rabinhash.RabinHashFunction32;

/**
 * {@link InfostoreDocumentAddFulltextIndexUpdateTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class InfostoreDocumentAddFulltextIndexUpdateTask extends UpdateTaskAdapter {

    private static final String TABLE = "infostore_document";

    @Override
    public void perform(PerformParameters params) throws OXException {
        String[] fields = SearchEngineImpl.getSearchFields();

        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            String expectedIndexName;
            List<String> updates = new ArrayList<String>(Arrays.asList(fields));
            {
                String tmp = getConfigOption("innodb_ft_min_token_size", con);
                if (Strings.isNotEmpty(tmp)) {
                    updates.add(tmp);
                }
                tmp = getConfigOption("innodb_ft_max_token_size", con);
                if (Strings.isNotEmpty(tmp)) {
                    updates.add(tmp);
                }
                tmp = getConfigOption("innodb_ft_server_stopword_table", con);
                if (Strings.isNotEmpty(tmp)) {
                    updates.add(tmp);
                }
                tmp = getConfigOption("innodb_ft_user_stopword_table", con);
                if (Strings.isNotEmpty(tmp)) {
                    updates.add(tmp);
                }
                tmp = getConfigOption("innodb_ft_enable_stopword", con);
                if (Strings.isNotEmpty(tmp)) {
                    updates.add(tmp);
                }
                tmp = getConfigOption("ngram_token_size", con);
                if (Strings.isNotEmpty(tmp)) {
                    updates.add(tmp);
                }
            }
            int hash = RabinHashFunction32.DEFAULT_HASH_FUNCTION.hash(updates.toString());
            expectedIndexName = new StringBuilder("fulltextSearch_").append(hash).toString();

            if (false == expectedIndexName.equals(Tools.existsIndex(con, TABLE, fields))) {
                String[] indexes = Tools.listIndexes(con, TABLE);
                for (String indexName : indexes) {
                    if (Strings.isNotEmpty(indexName) && indexName.startsWith("fulltextSearch_")) {
                        Tools.dropIndex(con, TABLE, indexName);
                    }
                }

                addFulltextIndex(expectedIndexName, fields, con);
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }

    }

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.AddMediaFieldsForInfostoreDocumentTableV3.class.getName() };
    }

    private boolean addFulltextIndex(String name, String[] fields, Connection connection) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            StringBuilder sb = new StringBuilder("CREATE FULLTEXT INDEX `").append(name).append("` ON ").append(TABLE).append(" (");
            for (String field : fields) {
                sb.append(field).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(");");
            return stmt.execute(sb.toString());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private String getConfigOption(String name, Connection connection) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SHOW VARIABLES LIKE ?");
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(2) : null;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
