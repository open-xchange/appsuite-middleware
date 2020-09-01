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
 *    trademarks of the OX Software GmbH. group of companies.
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
