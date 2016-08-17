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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * {@link CalendarExtendDNColumnTaskV2}
 *
 * Executes CalendarExtendDNColumnTask again, because it's changes were not added to calendar.sql script.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class CalendarExtendDNColumnTaskV2 implements UpdateTaskV2 {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarExtendDNColumnTaskV2.class);

    /**
     * Desired size for display name taken from ContactsFieldSizeUpdateTask.
     *
     * /**
     * Desired size for display name taken from ContactsFieldSizeUpdateTask.
     */
    private static final int DESIRED_SIZE = 320;

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        LOG.info("Starting {}", CalendarExtendDNColumnTaskV2.class.getSimpleName());
        modifyColumnInTable("prg_date_rights", contextId);
        modifyColumnInTable("del_date_rights", contextId);
        LOG.info("{} finished.", CalendarExtendDNColumnTaskV2.class.getSimpleName());
    }

    private static final String SQL_MODIFY = "ALTER TABLE #TABLE# MODIFY dn varchar(" + DESIRED_SIZE + ") collate utf8_unicode_ci default NULL";

    private void modifyColumnInTable(final String tableName, final int contextId) throws OXException {
        LOG.info("{}: Going to extend size of column `dn` in table `{}`.", CalendarExtendDNColumnTaskV2.class.getSimpleName(), tableName);
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
                            LOG.info("{}: Column {}.{} with size {} is already equal to/greater than {}", CalendarExtendDNColumnTaskV2.class.getSimpleName(), tableName, name, size, DESIRED_SIZE);
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
        LOG.info("{}: Size of column `dn` in table `{}` successfully extended.", CalendarExtendDNColumnTaskV2.class.getSimpleName(), tableName);
    }

    private static OXException wrapSQLException(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }

}
