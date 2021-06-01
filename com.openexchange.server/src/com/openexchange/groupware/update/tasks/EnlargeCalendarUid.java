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

import static com.openexchange.tools.update.Tools.checkAndModifyColumns;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;

/**
 * {@link EnlargeCalendarUid} - Enlarges the field size of calendar's UID field.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EnlargeCalendarUid extends UpdateTaskAdapter {

    /**
     * Default constructor.
     */
    public EnlargeCalendarUid() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnlargeCalendarUid.class);
        log.info("Performing update task {}", EnlargeCalendarUid.class.getSimpleName());
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tablesExist(con, "prg_dates", "del_dates")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            for (final String table : new String[] { "prg_dates", "del_dates" }) {
                checkAndModifyColumns(con, table, new Column("uid", "VARCHAR(1024)"));
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (Exception e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
        log.info("{} successfully performed.", EnlargeCalendarUid.class.getSimpleName());
    }

}
