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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.java.Strings;

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
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tablesExist(con, "prg_date_rights", "del_date_rights")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            LOG.info("Starting {}", CalendarExtendDNColumnTaskV2.class.getSimpleName());
            modifyColumnInTable("prg_date_rights", con);
            modifyColumnInTable("del_date_rights", con);
            LOG.info("{} finished.", CalendarExtendDNColumnTaskV2.class.getSimpleName());

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    private static final String SQL_MODIFY = "ALTER TABLE #TABLE# MODIFY dn varchar(" + DESIRED_SIZE + ") collate utf8_unicode_ci default NULL";

    private void modifyColumnInTable(String tableName, Connection con) throws OXException {
        LOG.info("{}: Going to extend size of column `dn` in table `{}`.", CalendarExtendDNColumnTaskV2.class.getSimpleName(), tableName);

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
                        LOG.info("{}: Column {}.{} with size {} is already equal to/greater than {}", CalendarExtendDNColumnTaskV2.class.getSimpleName(), tableName, name, I(size), I(DESIRED_SIZE));
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            throw wrapSQLException(e);
        } finally {
            Databases.closeSQLStuff(rs);
            rs = null;
        }

        // ALTER TABLE...
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(Strings.replaceSequenceWith(SQL_MODIFY, "#TABLE#", tableName));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw wrapSQLException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }

        LOG.info("{}: Size of column `dn` in table `{}` successfully extended.", CalendarExtendDNColumnTaskV2.class.getSimpleName(), tableName);
    }

    private static OXException wrapSQLException(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }

}
