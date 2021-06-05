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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import static com.openexchange.java.Autoboxing.I;
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
            if (false == Databases.tableExists(con, "prg_dates")) {
                return;
            }
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

                logger.info("Fixed appointment {}/{} (cid/id) old count: {} new count: {}", I(row.cid), I(row.id), I(row.count), I(row.realCount));
            }
            repairStmt.executeBatch();

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
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
