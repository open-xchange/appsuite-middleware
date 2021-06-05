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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.groupware.update.SimpleUpdateTask;

/**
 * {@link RemoveUnnecessaryIndexes2} - Second attempt: Removes unnecessary indexes from certain tables (see Bug #21882).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RemoveUnnecessaryIndexes2 extends SimpleUpdateTask {

    public RemoveUnnecessaryIndexes2() {
        super();
    }

    @Override
    protected void perform(final Connection con) throws SQLException {
        {
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                stmt.execute("ALTER TABLE `virtualBackupPermission` DROP INDEX `cid`");
            } catch (Exception e) {
                // Ignore
            } finally {
                closeSQLStuff(null, stmt);
            }
        }

        {
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                stmt.execute("ALTER TABLE `virtualPermission` DROP INDEX `cid`;");
            } catch (Exception e) {
                // Ignore
            } finally {
                closeSQLStuff(null, stmt);
            }
        }

        {
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                stmt.execute("ALTER TABLE `reminder` DROP INDEX `cid`;");
            } catch (Exception e) {
                // Ignore
            } finally {
                closeSQLStuff(null, stmt);
            }
        }
    }

}
