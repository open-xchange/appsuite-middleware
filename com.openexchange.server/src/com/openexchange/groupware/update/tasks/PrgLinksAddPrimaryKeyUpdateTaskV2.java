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

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;


/**
 * {@link PrgLinksAddPrimaryKeyUpdateTaskV2} - (Partly) re-executes <code>PrgLinksAddPrimaryKeyUpdateTask</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PrgLinksAddPrimaryKeyUpdateTaskV2 extends PrgLinksAddPrimaryKeyUpdateTask {

    /**
     * Initializes a new {@link PrgLinksAddPrimaryKeyUpdateTaskV2}.
     */
    public PrgLinksAddPrimaryKeyUpdateTaskV2() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            // Changes "uuid BINARY(16) DEFAULT NULL" to "uuid BINARY(16) NOT NULL" (if not yet performed)
            Column column = new Column("uuid", "BINARY(16) NOT NULL");
            Tools.checkAndModifyColumns(con, "prg_links", column);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { PrgLinksAddPrimaryKeyUpdateTask.class.getName() };
    }

}
