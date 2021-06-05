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
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.WorkingLevel;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link UserAddGuestCreatedByTask}
 *
 * Adds the column 'guestCreatedBy' to the tables 'user' and 'del_user', as well as an appropriate index.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class UserAddGuestCreatedByTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link UserAddGuestCreatedByTask}.
     */
    public UserAddGuestCreatedByTask() {
        super();
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING, WorkingLevel.SCHEMA);
    }

    @Override
    public String[] getDependencies() {
        return new String[] { UserClearDelTablesTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(UserAddGuestCreatedByTask.class);
        log.info("Performing update task {}", UserAddGuestCreatedByTask.class.getSimpleName());

        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            Column guestCreatedByColumn = new Column("guestCreatedBy", "int(10) unsigned NOT NULL DEFAULT 0");
            Tools.checkAndAddColumns(con, "user", guestCreatedByColumn);
            Tools.checkAndAddColumns(con, "del_user", guestCreatedByColumn);
            if (null == Tools.existsIndex(con, "user", new String[] { "cid", "guestCreatedBy"})) {
                Tools.createIndex(con, "user", "guestCreatedByIndex", new String[] { "cid", "guestCreatedBy"}, false);
            }

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

        log.info("{} successfully performed.", UserAddGuestCreatedByTask.class.getSimpleName());
    }

}
