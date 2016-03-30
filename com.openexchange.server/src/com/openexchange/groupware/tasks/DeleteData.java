/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.sql.DBUtils.startTransaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;

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
        foldStor.selectFolderById(ctx, taskId, getFolderId(), StorageType.ACTIVE);

        if (getOrigTask().getLastModified().after(lastModified)) {
            throw TaskExceptionCode.MODIFIED.create();
        }

        // Check delete permission
        Permission.checkDelete(ctx, user, permissionBits, folder, task);
    }

    public void doDelete() throws OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            startTransaction(con);
            // Try to block simultaneous deleting of tasks by generating a new identifier.
            IDGenerator.getId(ctx, Types.TASK, con);
            TaskLogic.deleteTask(ctx, con, user.getId(), TaskLogic.clone(getOrigTask()), lastModified);
            deleteReminder(con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw TaskExceptionCode.DELETE_FAILED.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    public void doDeleteHard(Session session, int folderId, StorageType type) throws OXException {
        Connection con = DBPool.pickupWriteable(ctx);
        boolean rollback = false;
        try {
            startTransaction(con);
            rollback = true;
            // Try to block simultaneous deleting of tasks by generating a new identifier.
            IDGenerator.getId(ctx, Types.TASK, con);
            TaskLogic.removeTask(session, ctx, con, folderId, taskId, type);
            deleteReminder(con);
            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw TaskExceptionCode.DELETE_FAILED.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    public void sentEvent(Session session) throws OXException {
        new EventClient(session).delete(getOrigTask());
    }

    private void deleteReminder(Connection con) throws OXException {
        Reminder.deleteReminder(ctx, con, task);
    }
}
