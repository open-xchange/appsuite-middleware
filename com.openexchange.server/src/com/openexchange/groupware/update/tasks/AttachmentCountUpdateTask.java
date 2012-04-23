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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link AttachmentCountUpdateTask}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AttachmentCountUpdateTask extends UpdateTaskAdapter {

    static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AttachmentCountUpdateTask.class));

    /**
     * Finds all appointments, where the field numberOfAttachments does not match the real amount of attachments.
     */
    private static final String SELECT = "SELECT pd.cid, pd.intfield01 AS id, pd.intfield08 AS count, COUNT(pa.id) AS realCount " +
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
        int contextId = params.getContextId();
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);
        Connection con = dbService.getForUpdateTask(contextId);
        PreparedStatement repairStmt = null;
        ResultSet rs = null;
        try {
            con.setAutoCommit(false);

            rs = con.createStatement().executeQuery(SELECT);
            repairStmt = con.prepareStatement(REPAIR);
            while (rs.next()) {
                int cid = rs.getInt("cid");
                int id = rs.getInt("id");
                int count = rs.getInt("count");
                int realCount = rs.getInt("realCount");

                repairStmt.setInt(1, realCount);
                repairStmt.setInt(2, cid);
                repairStmt.setInt(3, id);
                repairStmt.addBatch();

                LOG.info("Fixed appointment " + cid + "/" + id + " (cid/id) old count: " + count + " new count: " + realCount);
            }
            repairStmt.executeBatch();
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            DBUtils.closeSQLStuff(repairStmt);
            DBUtils.closeSQLStuff(rs);
            dbService.backForUpdateTask(contextId, con);
        }

    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND, SCHEMA);
    }

}
