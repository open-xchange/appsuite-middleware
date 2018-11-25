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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * Creates an initial empty filestore usage entry for every context that currently did not uploaded anything. The new quota counting
 * filestore implementation requires an existing entry for every context and it does not create it if it is missing. The has better
 * performance.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AddInitialFilestoreUsage extends UpdateTaskAdapter {

    public AddInitialFilestoreUsage() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        ProgressState state = params.getProgressState();
        final Connection con = params.getConnection();
        int[] contextIDs = params.getContextsInSameSchema();

        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            state.setTotal(contextIDs.length);
            for (int i = 0; i < contextIDs.length; i++) {
                if (isFilestoreUsageMissing(con, contextIDs[i])) {
                    addInitialFilestoreUsage(con, contextIDs[i]);
                }
                state.setState(i);
            }

            con.commit();
            rollback = 2;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(con);
                }
                autocommit(con);
            }
        }
    }

    private boolean isFilestoreUsageMissing(Connection con, int contextID) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=0");
            stmt.setInt(1, contextID);
            result = stmt.executeQuery();
            return !result.next();
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void addInitialFilestoreUsage(Connection con, int contextID) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO filestore_usage (cid,user,used) VALUES (?,0,0)");
            stmt.setInt(1, contextID);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }
}
