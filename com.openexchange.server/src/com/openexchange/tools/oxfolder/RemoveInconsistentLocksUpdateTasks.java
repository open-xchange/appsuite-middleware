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

package com.openexchange.tools.oxfolder;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.tasks.objectpermission.ObjectPermissionCreateTableTask;

/**
 * {@link RemoveInconsistentLocksUpdateTasks} removes all file locks which may be hold by any user which doesn't have any permissions to do so anymore.
 *
 * See bug #47929 and #48907
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class RemoveInconsistentLocksUpdateTasks extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link RemoveInconsistentLocksUpdateTasks}.
     */
    public RemoveInconsistentLocksUpdateTasks() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        PreparedStatement stmt = null;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            String SQL = "DELETE l FROM infostore_lock AS l INNER JOIN infostore AS i ON l.cid=i.cid and l.entity=i.id WHERE "
                + "i.folder_id NOT IN (SELECT fuid FROM oxfolder_permissions AS fp WHERE fp.cid=l.cid AND fp.permission_id=l.userid AND owp!=0) AND "
                + "l.entity NOT IN (SELECT object_id FROM object_permission AS op WHERE op.cid=l.cid AND op.permission_id=l.userid AND op.folder_id=i.folder_id AND op.bits=2);";

            stmt = con.prepareStatement(SQL);
            stmt.executeUpdate();

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
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
        return new String[] { ObjectPermissionCreateTableTask.class.getName(), com.openexchange.groupware.update.tasks.FolderPermissionReadAllForUserInfostore.class.getName() };
    }

}
