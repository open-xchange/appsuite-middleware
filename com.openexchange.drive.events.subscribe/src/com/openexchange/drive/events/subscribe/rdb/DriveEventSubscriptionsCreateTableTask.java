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

package com.openexchange.drive.events.subscribe.rdb;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.tableExists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link DriveEventSubscriptionsCreateTableTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveEventSubscriptionsCreateTableTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link DriveEventSubscriptionsCreateTableTask}.
     */
    public DriveEventSubscriptionsCreateTableTask() {
        super();
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection writeCon = params.getConnection();
        int rollback = 0;
        PreparedStatement stmt = null;
        try {
            writeCon.setAutoCommit(false); // BEGIN
            rollback = 1;
            final String[] tableNames = DriveEventSubscriptionsCreateTableService.getTablesToCreate();
            final String[] createStmts = DriveEventSubscriptionsCreateTableService.getCreateStmts();
            for (int i = 0; i < tableNames.length; i++) {
                try {
                    if (tableExists(writeCon, tableNames[i])) {
                        continue;
                    }
                    stmt = writeCon.prepareStatement(createStmts[i]);
                    stmt.executeUpdate();
                    closeSQLStuff(stmt);
                    stmt = null;
                } catch (SQLException e) {
                    throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                }
            }
            writeCon.commit(); // COMMIT
            rollback = 2;
        } catch (OXException e) {
            throw e;
        } catch (Exception e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(writeCon);
                }
                Databases.autocommit(writeCon);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

}
