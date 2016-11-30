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

import static com.openexchange.groupware.tasks.StorageType.ACTIVE;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;

/**
 * Implements the insertion of a task.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class InsertData {

    private static final Logger LOG = LoggerFactory.getLogger(InsertData.class);

    private static final AtomicReference<TaskQuotaProvider> QUOTA_PROVIDER_REF = new AtomicReference<TaskQuotaProvider>();

    private final Context ctx;
    private final User user;
    private final UserPermissionBits permissionBits;
    private final FolderObject folder;
    private final Task task;

    private Set<TaskParticipant> parts;
    private Set<Folder> folders;

    InsertData(Context ctx, User user, UserPermissionBits permissionBits, FolderObject folder, Task task) {
        super();
        this.ctx = ctx;
        this.user = user;
        this.permissionBits = permissionBits;
        this.folder = folder;
        this.task = task;
    }

    public static void setQuotaProvider(TaskQuotaProvider quotaProvider) {
        QUOTA_PROVIDER_REF.set(quotaProvider);
    }

    void prepare(Session session) throws OXException {
        parts = TaskLogic.createParticipants(ctx, task.getParticipants());
        TaskLogic.checkNewTask(task, user.getId(), permissionBits, parts);

        // Check access rights
        Permission.checkCreate(ctx, user, permissionBits, folder);
        int folderId = folder.getObjectID();
        if (task.getPrivateFlag() && (Tools.isFolderPublic(folder) || Tools.isFolderShared(folder, user))) {
            throw TaskExceptionCode.PRIVATE_FLAG.create(I(folderId));
        }

        // Create folder mappings
        if (Tools.isFolderPublic(folder)) {
            folders = TaskLogic.createFolderMapping(folderId, task.getCreatedBy(), InternalParticipant.EMPTY_INTERNAL);
        } else {
            Tools.fillStandardFolders(ctx, ParticipantStorage.extractInternal(parts));
            int creator = user.getId();
            if (Tools.isFolderShared(folder, user)) {
                creator = folder.getCreator();
            }
            folders = TaskLogic.createFolderMapping(folderId, creator,ParticipantStorage.extractInternal(parts));
        }

        // Check if over quota
        TaskQuotaProvider quotaProvider = QUOTA_PROVIDER_REF.get();
        if (quotaProvider == null) {
            LOG.warn("No TaskQuotaProvider was set, a task will be created without quota check!");
        } else {
            Quota amountQuota = quotaProvider.getAmountQuota(session);
            long limit = amountQuota.getLimit();
            long usage = amountQuota.getUsage();
            if (limit == 0 || (limit > 0 && usage >= limit)) {
                throw QuotaExceptionCodes.QUOTA_EXCEEDED_TASKS.create(usage, limit);
            }
        }
    }

    void doInsert() throws OXException {
        insertTask(ctx, task, parts, folders);
    }

    void createReminder() throws OXException {
        if (task.containsAlarm()) {
            Reminder.createReminder(ctx, task);
        }
    }

    void sentEvent(Session session) throws OXException {
        // Prepare for event
        task.setUsers(TaskLogic.createUserParticipants(parts));
        new EventClient(session).create(task, folder);
    }

    private static final TaskStorage storage = TaskStorage.getInstance();
    private static final ParticipantStorage partStor = ParticipantStorage.getInstance();
    private static final FolderStorage foldStor = FolderStorage.getInstance();

    /**
     * Stores a task with its participants and folders.
     *
     * @param ctx Context.
     * @param task Task to store.
     * @param participants Participants of the task.
     * @param folders Folders the task should appear in.
     * @throws OXException if an error occurs while storing the task.
     */
    static void insertTask(Context ctx, Task task, Set<TaskParticipant> participants, Set<Folder> folders) throws OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            con.setAutoCommit(false);
            final int taskId = IDGenerator.getId(ctx, Types.TASK, con);
            task.setObjectID(taskId);
            storage.insertTask(ctx, con, task, ACTIVE);
            if (participants.size() != 0) {
                partStor.insertParticipants(ctx, con, taskId, participants, ACTIVE);
            }
            foldStor.insertFolder(ctx, con, taskId, folders, ACTIVE);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw TaskExceptionCode.INSERT_FAILED.create(e, e.getMessage());
        } catch (OXException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            DBPool.closeWriterSilent(ctx, con);
        }
    }

}
