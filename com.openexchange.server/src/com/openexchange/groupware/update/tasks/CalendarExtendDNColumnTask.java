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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;

/**
 * {@link CalendarExtendDNColumnTask} - Extends size of <tt>VARCHAR</tt> column <i>dn</i> in both working and backup table of
 * <i>prg_date_rights</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CalendarExtendDNColumnTask implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CalendarExtendDNColumnTask.class));

    /**
     * Desired size for display name taken from ContactsFieldSizeUpdateTask.
     */
    private static final int DESIRED_SIZE = 320;

    @Override
    public int addedWithVersion() {
        return 33;
    }

    @Override
    public int getPriority() {
        /*
         * Modification on database: highest priority.
         */
        return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
    }

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Starting " + CalendarExtendDNColumnTask.class.getSimpleName());
        }
        modifyColumnInTable("prg_date_rights", contextId);
        modifyColumnInTable("del_date_rights", contextId);
        if (LOG.isInfoEnabled()) {
            LOG.info(CalendarExtendDNColumnTask.class.getSimpleName() + " finished.");
        }
    }

    private static final String SQL_MODIFY = "ALTER TABLE #TABLE# MODIFY dn varchar(" + DESIRED_SIZE + ") collate utf8_unicode_ci default NULL";

    private void modifyColumnInTable(final String tableName, final int contextId) throws OXException {
        if (LOG.isInfoEnabled()) {
            LOG.info(CalendarExtendDNColumnTask.class.getSimpleName() + ": Going to extend size of column `dn` in table `" + tableName + "`.");
        }
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            // Check if size needs to be increased
            ResultSet rs = null;
            try {
                final DatabaseMetaData metadata = con.getMetaData();
                rs = metadata.getColumns(null, null, tableName, null);
                final String columnName = "dn";
                while (rs.next()) {
                    final String name = rs.getString("COLUMN_NAME");
                    if (columnName.equals(name)) {
                        // A column whose VARCHAR size shall possibly be changed
                        final int size = rs.getInt("COLUMN_SIZE");
                        if (size >= DESIRED_SIZE) {
                            LOG.info(CalendarExtendDNColumnTask.class.getSimpleName() + ": Column " + tableName + '.' + name + " with size " + size + " is already equal to/greater than " + DESIRED_SIZE);
                            return;
                        }
                    }
                }
            } catch (final SQLException e) {
                throw wrapSQLException(e);
            } finally {
                closeSQLStuff(rs);
                rs = null;
            }
            // ALTER TABLE...
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(SQL_MODIFY.replaceFirst("#TABLE#", tableName));
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw wrapSQLException(e);
            } finally {
                closeSQLStuff(stmt);
            }
        } finally {
            Database.backNoTimeout(contextId, true, con);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(CalendarExtendDNColumnTask.class.getSimpleName() + ": Size of column `dn` in table `" + tableName + "` successfully extended.");
        }
    }

    private static OXException wrapSQLException(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }
}
