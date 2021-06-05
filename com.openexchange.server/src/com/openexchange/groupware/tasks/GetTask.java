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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.Date;
import java.util.Set;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.attach.impl.AttachmentBaseImpl;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.user.User;

/**
 * This class collects all information for getting tasks. It is also able to
 * check permissions in a fast way.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class GetTask {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetTask.class);

    private final Context ctx;

    /**
     * read only database connection.
     */
    private final Connection con;

    private User user;

    private UserPermissionBits permissionBits;

    private final int folderId;

    private FolderObject folder;

    private final int taskId;

    private Task task;

    private final StorageType type;

    private Set<TaskParticipant> participants;

    private Set<Folder> folderMapping;

    /**
     * The task storage.
     */
    private final TaskStorage storage = TaskStorage.getInstance();

    /**
     * The participant storage.
     */
    private final ParticipantStorage partStor = ParticipantStorage.getInstance();

    /**
     * The folder storage.
     */
    private final FolderStorage foldStor = FolderStorage.getInstance();

    /**
     * Use this constructor if you want permission checks.
     */
    GetTask(final Context ctx, final User user, final UserPermissionBits permissionBits, final int folderId, final int taskId, final StorageType type) {
        this(ctx, null, user, permissionBits, folderId, taskId, type);
    }

    /**
     * Use this constructor if you want permission checks.
     */
    GetTask(final Context ctx, final Connection con, final User user, final UserPermissionBits permissionBits, final int folderId, final int taskId, final StorageType type) {
        super();
        this.ctx = ctx;
        this.con = con;
        this.user = user;
        this.permissionBits = permissionBits;
        this.folderId = folderId;
        this.taskId = taskId;
        this.type = type;
    }

    /**
     * This constructor can be used if permission checks should not be done.
     */
    GetTask(final Context ctx, final int folderId, final int taskId, final StorageType type) {
        this(ctx, null, folderId, taskId, type);
    }

    /**
     * This constructor can be used if permission checks should not be done.
     */
    GetTask(final Context ctx, final Connection con, final int folderId, final int taskId, final StorageType type) {
        super();
        this.ctx = ctx;
        this.con = con;
        this.folderId = folderId;
        this.taskId = taskId;
        this.type = type;
    }

    /**
     * TODO instantiate this class with the normal folder object.
     */
    private FolderObject getFolder() throws OXException {
        if (null == folder) {
            if (null == con) {
                folder = Tools.getFolder(ctx, folderId);
            } else {
                folder = Tools.getFolder(ctx, con, folderId);
            }
        }
        return folder;
    }

    private Task getTask() throws OXException {
        if (null == task) {
            if (null == con) {
                task = storage.selectTask(ctx, taskId, type);
            } else {
                task = storage.selectTask(ctx, con, taskId, type);
            }
        }
        return task;
    }

    private Set<TaskParticipant> getParticipants() throws OXException {
        if (null == participants) {
            if (null == con) {
                participants = partStor.selectParticipants(ctx, taskId, type);
            } else {
                participants = partStor.selectParticipants(ctx, con, taskId, type);
            }
        }
        return participants;
    }

    private Set<Folder> getFolders() throws OXException {
        if (null == folderMapping) {
            if (null == con) {
                folderMapping =  foldStor.selectFolder(ctx, taskId, type);
            } else {
                folderMapping =  foldStor.selectFolder(ctx, con, taskId, type);
            }
        }
        return folderMapping;
    }

    static Task load(final Context ctx, final Connection con, final int folderId, final int taskId, final StorageType type) throws OXException {
        return new GetTask(ctx, con, folderId, taskId, type).load();
    }

    static Task load(final Context ctx, final int folderId, final int taskId, final StorageType type) throws OXException {
        return new GetTask(ctx, folderId, taskId, type).load();
    }

    /**
     * Loads the task without checking permission. Use
     * {@link #checkPermission()} for checking access permissions. Use
     * {@link #fillReminder()} if the reminder for the loading user should be
     * loaded.
     */
    Task load() throws OXException {
        fillParticipants();
        fillTask();
        return getTask();
    }

    Task loadAndCheck() throws OXException {
        checkPermission();
        fillParticipants();
        fillTask();
        fillReminder();
        return getTask();
    }

    void checkPermission() throws OXException {
        if (null == user || null == permissionBits) {
            throw TaskExceptionCode.UNIMPLEMENTED.create();
        }
        if (null == con) {
            Permission.canReadInFolder(ctx, user, permissionBits, getFolder());
        } else {
            Permission.canReadInFolder(ctx, con, user, permissionBits, getFolder());
        }
        final Folder check = FolderStorage.getFolder(getFolders(), folderId);
        if (null == check || (Tools.isFolderShared(getFolder(), user) && getTask().getPrivateFlag())) {
            throw TaskExceptionCode.NO_PERMISSION.create(I(taskId), I(folderId));
        }
    }

    private boolean filledParts = false;

    private void fillParticipants() throws OXException {
        if (filledParts) {
            return;
        }
        if (!Tools.isFolderPublic(getFolder())) {
            Tools.fillStandardFolders(ctx.getContextId(), taskId, getParticipants(), getFolders(), true);
        }
        filledParts = true;
    }

    private boolean filledTask = false;

    private void fillTask() throws OXException {
        if (filledTask) {
            return;
        }
        getTask();
        task.setParticipants(TaskLogic.createParticipants(getParticipants()));
        task.setUsers(TaskLogic.createUserParticipants(getParticipants()));
        task.setParentFolderID(folderId);
        final AttachmentBase attachmentBase;
        if (null == con) {
            attachmentBase = Attachments.getInstance();
        } else {
            attachmentBase = new AttachmentBaseImpl(new SimpleDBProvider(con, null));
        }
        Date lastModifiedOfNewestAttachment = null;
        try {
            lastModifiedOfNewestAttachment = attachmentBase.getNewestCreationDate(ctx, Types.TASK, task.getObjectID());
        } catch (OXException e) {
            LOG.error("", e);
        }
        if (null != lastModifiedOfNewestAttachment) {
            task.setLastModifiedOfNewestAttachment(lastModifiedOfNewestAttachment);
        }
        filledTask = true;
    }

    void fillReminder() throws OXException {
        Reminder.loadReminder(ctx, getUserId(), getTask());
    }

    /* ---------- Convenience methods ---------- */

    private int getUserId() throws OXException {
        if (null == user) {
            throw TaskExceptionCode.UNIMPLEMENTED.create();
        }
        return user.getId();
    }
}
