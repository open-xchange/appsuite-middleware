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

package com.openexchange.ajax.requesthandler.converters.preview.cache.groupware;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.tableExists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link PreviewCacheCreateDataTableTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PreviewCacheCreateDataTableTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link PreviewCacheCreateDataTableTask}.
     */
    public PreviewCacheCreateDataTableTask() {
        super();
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection writeCon = params.getConnection();
        PreparedStatement stmt = null;
        int rollback = 0;
        try {
            writeCon.setAutoCommit(false); // BEGIN
            rollback = 1;

            final String[] tableNames = PreviewCacheCreateDataTableService.getTablesToCreate();
            final String[] createStmts = PreviewCacheCreateDataTableService.getCreateStmts();
            for (int i = 0; i < tableNames.length; i++) {
                try {
                    if (!tableExists(writeCon, tableNames[i])) {
                        stmt = writeCon.prepareStatement(createStmts[i]);
                        stmt.executeUpdate();
                        Databases.closeSQLStuff(stmt);
                        stmt = null;
                    }
                } catch (SQLException e) {
                    throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                }
            }

            writeCon.commit(); // COMMIT
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(writeCon);
                }
                Databases.autocommit(writeCon);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { PreviewCacheCreateTableTask.class.getName() };
    }

}
