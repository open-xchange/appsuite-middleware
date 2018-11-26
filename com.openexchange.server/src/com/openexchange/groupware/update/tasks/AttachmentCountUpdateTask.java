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

import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link AttachmentCountUpdateTask}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AttachmentCountUpdateTask extends UpdateTaskAdapter {

    /**
     * Finds all appointments, where the field numberOfAttachments does not match the real amount of attachments.
     */
    // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
    private static final String SELECT = "SELECT pd.cid, pd.intfield01 AS id, MIN(pd.intfield08) AS count, COUNT(pa.id) AS realCount " +
            "FROM prg_dates pd LEFT JOIN prg_attachment pa " +
            "ON pd.cid = pa.cid AND pd.intfield01 = pa.attached AND pa.module=1 " +
            "GROUP BY pd.cid,pd.intfield01 " +
            "HAVING count!=realCount";

    private static final String REPAIR = "UPDATE prg_dates SET intfield08 = ? WHERE cid = ? AND intfield01 = ?";

    @Override
    public String[] getDependencies() {
        return new String[]{};
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        PreparedStatement repairStmt = null;
        Statement stmt = null;
        ResultSet rs = null;
        int rollback = 0;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SELECT);
            if (false == rs.next()) {
                // No such appointments having field 'numberOfAttachments' not equal to the real number of attachments
                return;
            }

            class Row {

                final int cid;
                final int id;
                final int count;
                final int realCount;

                Row(ResultSet rs) throws SQLException {
                    super();
                    cid = rs.getInt("cid");
                    id = rs.getInt("id");
                    count = rs.getInt("count");
                    realCount = rs.getInt("realCount");
                }
            }

            List<Row> rows = new LinkedList<>();
            do {
                rows.add(new Row(rs));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AttachmentCountUpdateTask.class);

            con.setAutoCommit(false);
            rollback = 1;

            repairStmt = con.prepareStatement(REPAIR);
            for (Row row : rows) {
                repairStmt.setInt(1, row.realCount);
                repairStmt.setInt(2, row.cid);
                repairStmt.setInt(3, row.id);
                repairStmt.addBatch();

                logger.info("Fixed appointment {}/{} (cid/id) old count: {} new count: {}", row.cid, row.id, row.count, row.realCount);
            }
            repairStmt.executeBatch();

            con.commit();
            rollback = 2;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(repairStmt);
            Databases.closeSQLStuff(rs, stmt);
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }

    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND, SCHEMA);
    }

}
