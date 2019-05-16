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

package com.openexchange.database.tombstone.cleanup.cleaners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.collect.Iterables;
import com.openexchange.database.Databases;

/**
 * {@link AbstractContextBasedTombstoneTableCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public abstract class AbstractContextBasedTombstoneTableCleaner extends AbstractTombstoneTableCleaner {

    protected Map<Integer, Set<Integer>> gatherDeleteCandidates(Connection connection, long timestamp, String selectStmt) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(selectStmt)) {
            int parameterIndex = 1;
            stmt.setLong(parameterIndex++, timestamp);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                return readItems(resultSet);
            }
        }
    }

    /**
     * Reads the items selected by {@link #gatherDeleteCandidates(Connection, long, String)} and returns the contextId - Set of object IDs mapping
     * 
     * @param resultSet The {@link ResultSet} to read from
     * @return Map<Integer, Set<Integer>> containing the items read by {@link #gatherDeleteCandidates(Connection, long, String)}
     * @throws SQLException
     */
    protected abstract Map<Integer, Set<Integer>> readItems(ResultSet resultSet) throws SQLException;

    /**
     * Generic method to (chunk-wise) delete entries based on the given delete statement.
     * 
     * @param connection The write connection to delete the entries
     * @param deleteCandidatesPerContext Map containing context id to a set of integer mapping with IDs that should be deleted
     * @param deleteStatement The statement that will be used to delete entries
     * @throws SQLException
     */
    protected int deleteContextWise(Connection connection, Map<Integer, Set<Integer>> deleteCandidatesPerContext, String deleteStatement) throws SQLException {
        int deletedRows = 0;
        for (Entry<Integer, Set<Integer>> candidatesPerContext : deleteCandidatesPerContext.entrySet()) {
            Integer contextId = candidatesPerContext.getKey();
            Set<Integer> ids = candidatesPerContext.getValue();

            for (List<Integer> chunk : Iterables.partition(ids, 500)) {
                StringBuilder stringBuilder = new StringBuilder().append(deleteStatement).append(Databases.getPlaceholders(chunk.size()));
                try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, contextId.intValue());
                    for (Integer id : chunk) {
                        stmt.setInt(parameterIndex++, id.intValue());
                    }
                    deletedRows += logExecuteUpdate(stmt);
                }
            }
        }
        return deletedRows;
    }
}
