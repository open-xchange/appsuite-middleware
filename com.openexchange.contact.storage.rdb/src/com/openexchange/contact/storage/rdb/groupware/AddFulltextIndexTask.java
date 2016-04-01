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

package com.openexchange.contact.storage.rdb.groupware;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.contact.storage.rdb.internal.RdbServiceLookup;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.contact.storage.rdb.search.FulltextAutocompleteAdapter;
import com.openexchange.contact.storage.rdb.sql.Table;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
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
 * <code>"com.openexchange.contact.fulltextIndexFields"</code> setting; creates it if missing &  dropping obsolete ones.
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
        DatabaseService dbService = RdbServiceLookup.getService(DatabaseService.class);

        // Prepare indexed columns
        ContactField[] fields = FulltextAutocompleteAdapter.fulltextIndexFields();
        String[] columns = new String[fields.length];
        for (int i = fields.length; i-- > 0;) {
            columns[i] = Mappers.CONTACT.get(fields[i]).getColumnLabel();
        }

        // Create index unless it already exists
        int contextID = params.getContextId();
        Connection connection = dbService.getForUpdateTask(contextID);
        try {
            connection.setAutoCommit(false);

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
                    if (!Strings.isEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("innodb_ft_max_token_size", connection);
                    if (!Strings.isEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("innodb_ft_server_stopword_table", connection);
                    if (!Strings.isEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("innodb_ft_user_stopword_table", connection);
                    if (!Strings.isEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("innodb_ft_enable_stopword", connection);
                    if (!Strings.isEmpty(tmp)) {
                        updates.add(tmp);
                    }
                    tmp = getConfigOption("ngram_token_size", connection);
                    if (!Strings.isEmpty(tmp)) {
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
        } catch (SQLException e) {
            rollback(connection);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(connection);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(connection);
            Database.backNoTimeout(contextID, true, connection);
        }
    }

    private boolean addFulltextIndex(String name, ContactField[] fields, Connection connection) throws SQLException, OXException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            return stmt.execute(new StringBuilder("CREATE FULLTEXT INDEX `").append(name).append("` ON ").append(Table.CONTACTS).append(" (").append(Mappers.CONTACT.getColumns(fields)).append(");").toString());
        } finally {
            closeSQLStuff(stmt);
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
            closeSQLStuff(rs, stmt);
        }
    }

}