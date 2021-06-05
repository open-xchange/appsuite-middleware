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

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.tools.update.Tools.columnExists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.groupware.update.SimpleUpdateTask;

/**
 * {@link VirtualFolderAddSortNumTask} - Add "sortNum" column to virtual folder table.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolderAddSortNumTask extends SimpleUpdateTask {

    public VirtualFolderAddSortNumTask() {
        super();
    }

    @Override
    protected void perform(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            if (!columnExists(con, "virtualTree", "sortNum")) {
                stmt =
                    con.prepareStatement("ALTER TABLE virtualTree ADD COLUMN sortNum INT4 UNSIGNED DEFAULT NULL");
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
            }
            if (!columnExists(con, "virtualBackupTree", "sortNum")) {
                stmt =
                    con.prepareStatement("ALTER TABLE virtualBackupTree ADD COLUMN sortNum INT4 UNSIGNED DEFAULT NULL");
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
            }
        } finally {
            closeSQLStuff(stmt);
        }
    }

}
