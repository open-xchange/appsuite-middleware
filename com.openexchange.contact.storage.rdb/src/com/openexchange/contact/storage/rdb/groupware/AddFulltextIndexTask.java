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

package com.openexchange.contact.storage.rdb.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.contact.storage.rdb.search.FulltextAutocompleteAdapter;
import com.openexchange.contact.storage.rdb.sql.Table;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Strings;
import com.openexchange.tools.update.Tools;
import com.planetj.math.rabinhash.RabinHashFunction32;

/**
 * {@link AddFulltextIndexTask} - Checks existence of an appropriate FULLTEXT index for fields configured via
 * <code>"com.openexchange.contact.fulltextIndexFields"</code> setting; creates it if missing & dropping obsolete ones.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class AddFulltextIndexTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link AddFulltextIndexTask}.
     */
    public AddFulltextIndexTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        // Prepare indexed columns
        ContactField[] fields = FulltextAutocompleteAdapter.fulltextIndexFields();
        String[] columns = new String[fields.length];
        for (int i = fields.length; i-- > 0;) {
            columns[i] = Mappers.CONTACT.get(fields[i]).getColumnLabel();
        }

        // Create index unless it already exists
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            // Generate expected index name (dependent on configured fields)
            String expectedName;
            {
                // Consider sorted column names
                List<String> updates = new ArrayList<String>(Arrays.asList(columns));
                Collections.sort(updates);

                /*-
                 * Also consider any of FULLTEXT-affecting MySQL configuration options:
                 *  - innodb_ft_min_token_size
                 *  - innodb_ft_max_token_size
                 *  - innodb_ft_server_stopword_table
                 *  - innodb_ft_user_stopword_table
                 *  - innodb_ft_enable_stopword
                 *  - ngram_token_size
                 */
                {
                    String tmp = getConfigOption("innodb_ft_min_token_size", connection);
                    if (Strings.isNotEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("innodb_ft_max_token_size", connection);
                    if (Strings.isNotEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("innodb_ft_server_stopword_table", connection);
                    if (Strings.isNotEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("innodb_ft_user_stopword_table", connection);
                    if (Strings.isNotEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("innodb_ft_enable_stopword", connection);
                    if (Strings.isNotEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("ngram_token_size", connection);
                    if (Strings.isNotEmpty(tmp)) {
                        updates.add(tmp);
                    }
                }

                int hash = RabinHashFunction32.DEFAULT_HASH_FUNCTION.hash(updates.toString());
                expectedName = new StringBuilder("autocomplete").append(hash).toString();
            }

            if (false == expectedName.equals(Tools.existsIndex(connection, Table.CONTACTS.getName(), columns))) {
                // Drop possible obsolete FULLTEXT index
                String[] indexNames = Tools.listIndexes(connection, Table.CONTACTS.getName());
                for (String indexName : indexNames) {
                    if (null != indexName && indexName.startsWith("autocomplete")) {
                        Tools.dropIndex(connection, Table.CONTACTS.getName(), indexName);
                    }
                }

                // Create new one
                addFulltextIndex(expectedName, fields, connection);
            }

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
        }
    }

    private boolean addFulltextIndex(String name, ContactField[] fields, Connection connection) throws SQLException, OXException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            return stmt.execute(new StringBuilder("CREATE FULLTEXT INDEX `").append(name).append("` ON ").append(Table.CONTACTS).append(" (").append(Mappers.CONTACT.getColumns(fields)).append(");").toString());
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
