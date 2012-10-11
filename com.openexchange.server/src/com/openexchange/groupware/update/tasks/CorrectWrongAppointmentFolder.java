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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;

/**
 * This update task fixes broken folder identifier in the appointment
 * participants table that are cause by bug 12595.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CorrectWrongAppointmentFolder implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CorrectWrongAppointmentFolder.class));

    public CorrectWrongAppointmentFolder() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addedWithVersion() {
        return 25;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(final Schema schema, final int contextId)
        throws OXException {
        final String find = "SELECT pd.cid,pd.object_id,pd.member_uid "
            + "FROM prg_dates_members pd JOIN oxfolder_tree f "
            + "ON pd.cid=f.cid AND pd.pfid=f.fuid"
            + " WHERE f.created_from!=pd.member_uid AND f.module=2 AND f.type=1 AND f.default_flag=1";
        final Connection con = Database.get(contextId, true);
        Statement stmt = null;
        ResultSet result = null;
        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            result = stmt.executeQuery(find);
            while (result.next()) {
                int pos = 1;
                final int cid = result.getInt(pos++);
                final int appId = result.getInt(pos++);
                final int member = result.getInt(pos++);
                final int folderId = getPrivateFolder(con, cid, member);
                if (-1 == folderId) {
                    LOG.info("Unable to correct folder of participant "
                        + member + " for appointment " + appId + " in context "
                        + cid + ".");
                } else {
                    LOG.info("Setting folder to " + folderId + " of participant "
                        + member + " for appointment " + appId + " in context "
                        + cid + ".");
                    correctFolder(con, cid, appId, member, folderId);
                }
            }
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    private void correctFolder(final Connection con, final int cid,
        final int appId, final int member, final int folderId) {
        final String sql = "UPDATE prg_dates_members SET pfid=? "
            + "WHERE cid=? AND object_id=? AND member_uid=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, appId);
            stmt.setInt(pos++, member);
            stmt.execute();
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private int getPrivateFolder(final Connection con, final int cid, final int member) {
        final String sql = "SELECT fuid FROM oxfolder_tree WHERE cid=? "
            + "AND created_from=? AND type=? AND module=? AND default_flag=?";
        PreparedStatement stmt = null;
        ResultSet result = null;
        int folderId = -1;
        try {
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, member);
            stmt.setInt(pos++, FolderObject.PRIVATE);
            stmt.setInt(pos++, FolderObject.CALENDAR);
            stmt.setInt(pos++, 1);
            result = stmt.executeQuery();
            if (result.next()) {
                folderId = result.getInt(1);
            }
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return folderId;
    }
}
