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

package com.openexchange.filestore.impl.groupware;

import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.impl.osgi.Services;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.IntReference;

/**
 * Creates an initial empty "filestore_usage" entry for users.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AddInitialUserFilestoreUsage extends UpdateTaskAdapter {

    public AddInitialUserFilestoreUsage() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddUserColumnToFilestoreUsageTable.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        ProgressState state = params.getProgressState();

        DatabaseService dbService = Services.requireService(DatabaseService.class);
        Connection con = dbService.getForUpdateTask(contextId);
        boolean rollback = false;
        try {
            IntReference count = new IntReference();
            Map<Integer, List<Integer>> users = loadUsersInSchema(contextId, dbService, count, con);

            con.setAutoCommit(false);
            rollback = true;

            state.setTotal(count.getValue());
            int i = 0;
            for (Map.Entry<Integer, List<Integer>> entry : users.entrySet()) {
                int currentContextId = entry.getKey().intValue();
                for (Integer userId : entry.getValue()) {
                    if (isFilestoreUsageMissing(con, currentContextId, userId.intValue())) {
                        addInitialFilestoreUsage(con, currentContextId, userId.intValue());
                    }
                    state.setState(i++);
                }
            }

            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
               Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backForUpdateTask(contextId, con);
        }
    }

    private Map<Integer, List<Integer>> loadUsersInSchema(int contextId, DatabaseService dbService, IntReference count, Connection con) throws OXException, SQLException {
        int[] contextIds = dbService.getContextsInSameSchema(contextId);

        int c = 0;
        Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>(contextIds.length);
        for (int cid : contextIds) {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement("SELECT id FROM user WHERE cid=? AND filestore_id > 0");
                stmt.setInt(1, cid);
                result = stmt.executeQuery();

                List<Integer> users = new LinkedList<Integer>();
                while (result.next()) {
                    users.add(Integer.valueOf(result.getInt(1)));
                    c++;
                }

                map.put(Integer.valueOf(cid), users);
            } finally {
                Databases.closeSQLStuff(result, stmt);
            }
        }

        count.setValue(c);
        return map;
    }

    private boolean isFilestoreUsageMissing(Connection con, int contextId, int userId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            result = stmt.executeQuery();
            return !result.next();
        } finally {
            Databases.closeSQLStuff(result, stmt);
        }
    }

    private void addInitialFilestoreUsage(Connection con, int contextId, int userId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO filestore_usage (cid,user,used) VALUES (?,?,0)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }
}
