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

package com.openexchange.database.tombstone.cleanup.update.cleaners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.database.tombstone.cleanup.cleaners.InfostoreTombstoneCleaner;
import com.openexchange.java.ConcurrentList;

/**
 * {@link InfostoreTombstoneUpdateTaskCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class InfostoreTombstoneUpdateTaskCleaner extends InfostoreTombstoneCleaner {

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT DISTINCT cid FROM del_infostore WHERE last_modified < ?")) {
            int parameterIndex = 1;
            stmt.setLong(parameterIndex++, timestamp);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                List<Integer> deleteCanditates = readContexts(resultSet);
                deleteContextWise(connection, deleteCanditates, timestamp);

                try (Statement createStatement = connection.createStatement()) {
                    createStatement.addBatch("CREATE TABLE del_infostore_new LIKE del_infostore;");
                    createStatement.addBatch("ALTER TABLE del_infostore_new ENGINE = InnoDB;");
                    createStatement.addBatch("INSERT INTO del_infostore_new SELECT * FROM del_infostore WHERE last_modified >= " + timestamp + ";");
                    createStatement.addBatch("RENAME TABLE del_infostore TO del_infostore_old, del_infostore_new TO del_infostore;");
                    createStatement.addBatch("DROP TABLE del_infostore_old");
                    createStatement.executeBatch();
                }
            }
        }
        return Collections.emptyMap();
    }

    protected List<Integer> readContexts(ResultSet resultSet) throws SQLException {
        List<Integer> affectedContextIds = new ConcurrentList<>();
        while (resultSet.next()) {
            int contextId = resultSet.getInt("cid");
            affectedContextIds.add(contextId);
        }
        return affectedContextIds;
    }

    protected void deleteContextWise(Connection connection, List<Integer> deleteCandidatesPerContext, long timestamp) throws SQLException {
        String deleteStmt = "DELETE FROM del_infostore, del_infostore_document USING del_infostore INNER JOIN del_infostore_document ON del_infostore_document.infostore_id = del_infostore.id AND del_infostore_document.cid = del_infostore.cid WHERE del_infostore.last_modified < ? AND del_infostore.cid = ?";

        for (Integer contextId : deleteCandidatesPerContext) {
            if (contextId == null) {
                continue;
            }
            try (PreparedStatement stmt = connection.prepareStatement(deleteStmt)) {
                int parameterIndex = 1;
                stmt.setLong(parameterIndex++, timestamp);
                stmt.setInt(parameterIndex++, contextId.intValue());
                logExecuteUpdate(stmt);
            }
        }
    }
}
