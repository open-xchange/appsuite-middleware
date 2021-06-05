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

package com.openexchange.groupware.tasks;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import com.openexchange.database.RetryingTransactionClosure;
import com.openexchange.database.SQLClosure;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * Process of deleting a task.
 * TODO a lot of stuff needs to be migrated from {@link TaskLogic} class.
 * TODO Switch to only delete the participant from task
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class DeleteData {

//    private static final TaskStorage storage = TaskStorage.getInstance();
    private static final FolderStorage foldStor = FolderStorage.getInstance();

    private final Context ctx;
    private final User user;
    private final UserPermissionBits permissionBits;
    private final FolderObject folder;
    private final int taskId;
    private final Date lastModified;

    private Task task;

    public DeleteData(Context ctx, User user, UserPermissionBits permissionBits, FolderObject folder, int taskId, Date lastModified) {
        super();
        this.ctx = ctx;
        this.user = user;
        this.permissionBits = permissionBits;
        this.folder = folder;
        this.taskId = taskId;
        this.lastModified = lastModified;
    }

    private int getFolderId() {
        return folder.getObjectID();
    }

    private Task getOrigTask() throws OXException {
        if (null == task) {
            // Load task with participants.
            task = GetTask.load(ctx, getFolderId(), taskId, StorageType.ACTIVE);
        }
        return task;
    }

    public void prepare() throws OXException {
        // Check if folder is correct.
        Folder selectedFolder = foldStor.selectFolderById(ctx, taskId, getFolderId(), StorageType.ACTIVE);
        if (null == selectedFolder) {
            // Either no such folder or specified task does not reside in given folder
            throw TaskExceptionCode.NO_DELETE_PERMISSION.create();
        }

        if (getOrigTask().getLastModified().after(lastModified)) {
            throw TaskExceptionCode.MODIFIED.create();
        }

        // Check delete permission
        Permission.checkDelete(ctx, user, permissionBits, folder, task);
    }

    public void doDelete() throws OXException {
        delete(c -> {
            TaskLogic.deleteTask(ctx, c, user.getId(), TaskLogic.clone(getOrigTask()), lastModified);
            return null;
        });
    }

    public void doDeleteHard(Session session, int folderId, StorageType type) throws OXException {
        delete(c -> {
            TaskLogic.removeTask(session, ctx, c, folderId, taskId, type);
            return null;
        });
    }

    private void delete(SQLClosure<Void> deleteClosure) throws OXException {
        Connection con = DBPool.pickupWriteable(ctx);
        int rollback = 0;
        try {
            startTransaction(con);
            rollback = 1;
            RetryingTransactionClosure.execute(deleteClosure, 3, con);
            RetryingTransactionClosure.execute(c -> {
                Reminder.deleteReminder(ctx, c, task);
                return null;
            }, 3, con);
            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw TaskExceptionCode.DELETE_FAILED.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(con);
                }
                autocommit(con);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    public void sentEvent(Session session) throws OXException {
        new EventClient(session).delete(getOrigTask());
    }
}
