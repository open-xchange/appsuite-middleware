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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.tools.sql.DBUtils;

/**
 * Removes the duplicate recurrence date positions from appointment exceptions.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AppointmentExceptionRemoveDuplicateDatePosition implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AppointmentExceptionRemoveDuplicateDatePosition.class));

    public AppointmentExceptionRemoveDuplicateDatePosition() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 22;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(final Schema schema, final int contextId)
        throws OXException {
        LOG.info("Performing update task to remove duplicate date recurrence position from appointment change exceptions on schema " + schema.getSchema());
        final Connection con = Database.get(contextId, true);
        Statement st = null;
        try {
            con.setAutoCommit(false);
            st = con.createStatement();
            st.executeUpdate("UPDATE prg_dates SET field08=SUBSTR(field08,1,LOCATE(',', field08)-1) WHERE intfield01!=intfield02 AND field08 LIKE '%,%';");
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            closeSQLStuff(null, st);
            if (con != null) {
                Database.back(contextId, true, con);
            }
        }
        LOG.info("Update task to remove duplicate date recurrence position from appointment change exceptions performed.");
    }
}
