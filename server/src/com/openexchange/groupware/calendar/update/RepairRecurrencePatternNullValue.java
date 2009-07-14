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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.calendar.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class RepairRecurrencePatternNullValue implements UpdateTask {

    private static final String UPDATE_PRG_DATES = "UPDATE prg_dates SET field06 = ? WHERE field06 = ?";

    private static final String UPDATE_DEL_DATES = "UPDATE del_dates SET field06 = ? WHERE field06 = ?";

    public int addedWithVersion() {
        return 58;
    }

    public int getPriority() {
        return UpdateTask.UpdateTaskPriority.NORMAL.priority;
    }

    public void perform(Schema schema, int contextId) throws AbstractOXException {
        Connection writeCon = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;

        try {
            writeCon = Database.getNoTimeout(contextId, true);
            writeCon.setAutoCommit(false);

            stmt1 = writeCon.prepareStatement(UPDATE_PRG_DATES);
            stmt1.setNull(1, Types.VARCHAR);
            stmt1.setString(2, "null");

            stmt2 = writeCon.prepareStatement(UPDATE_DEL_DATES);
            stmt2.setNull(1, Types.VARCHAR);
            stmt2.setString(2, "null");

            stmt1.execute();
            stmt2.execute();
            
            writeCon.commit();
        } catch (SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.UPDATE_EXCEPTION, e.getMessage());
        } finally {
            try {
                closeSQLStuff(contextId, writeCon, stmt1, stmt2);
            } catch (SQLException e) {
                throw new OXCalendarException(OXCalendarException.Code.UPDATE_EXCEPTION, e.getMessage());
            }
        }

    }
    
    private void closeSQLStuff(int contextId, Connection con, PreparedStatement stmt1, PreparedStatement stmt2) throws SQLException {
        SQLException sqle = null;
        
        if (con != null) {
            try {
                con.rollback();
                con.setAutoCommit(true);
            } catch (SQLException e) {
                sqle = e;
            } finally {
                Database.backNoTimeout(contextId, true, con);
            }
        }
        
        if (stmt1 != null) {
            try {
                stmt1.close();
            } catch (SQLException e) {
                sqle = e;
            }
        }
        
        if (stmt2 != null) {
            try {
                stmt2.close();
            } catch (SQLException e) {
                sqle = e;
            }
        }
        
        if (sqle != null) {
            throw sqle;
        }
    }

}
