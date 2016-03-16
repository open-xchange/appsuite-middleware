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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ConfirmTask {

    private static final TaskStorage storage = TaskStorage.getInstance();
    private static final FolderStorage foldStor = FolderStorage.getInstance();
    private static final ParticipantStorage partStor = ParticipantStorage.getInstance();

    private static final int[] CHANGED_ATTRIBUTES = new int[] { Task.LAST_MODIFIED, Task.MODIFIED_BY };

    private final Context ctx;
    private final int taskId;
    private final int userId;
    private final int confirm;
    private final String message;

    private Task origTask;
    private FolderObject folder;
    private Set<TaskParticipant> participants;
    private Set<Folder> folders;
    private Task changedTask;
    private InternalParticipant origParticipant;
    private InternalParticipant changedParticipant;

    /**
     * Default constructor.
     */
    ConfirmTask(final Context ctx, final int taskId, final int userId,
        final int confirm, final String message) {
        super();
        this.ctx = ctx;
        this.taskId = taskId;
        this.userId = userId;
        this.confirm = confirm;
        this.message = message;
    }

    // ===================== API methods =======================================

    /**
     * This method loads all necessary data and prepares the objects for updating
     * the database.
     */
    void prepare() throws OXException {
        // Load full task.
        fillParticipants();
        fillTask();
        // Load participant and set confirmation
        changedParticipant = getOrigParticipant();
        changedParticipant.setConfirm(confirm);
        changedParticipant.setConfirmMessage(message);
        // Prepare changed task attributes.
        changedTask = new Task();
        changedTask.setObjectID(taskId);
        changedTask.setModifiedBy(userId);
        changedTask.setLastModified(new Date());
    }

    /**
     * This method does all the changes in the database in a transaction.
     */
    void doConfirmation() throws OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            con.setAutoCommit(false);
            partStor.updateInternal(ctx, con, taskId, changedParticipant, StorageType.ACTIVE);
            UpdateData.updateTask(ctx, con, changedTask, getOrigTask().getLastModified(), CHANGED_ATTRIBUTES, null, null, null, null);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            autocommit(con);
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    void sentEvent(final Session session) throws OXException {
        final Task orig = getOrigTask();
        if (userId != orig.getCreatedBy() && null == ParticipantStorage.getParticipant(
            ParticipantStorage.extractInternal(getParticipants()),
            orig.getCreatedBy())) {
            // Delegator is not participant and participant changed task. Change parent folder of original task to delegators folder
            // identifier so we are able to use that for participant notification.
            Folder delegatorFolder = FolderStorage.extractFolderOfUser(getFolders(), orig.getCreatedBy());
            if (null != delegatorFolder) {
                orig.setParentFolderID(delegatorFolder.getIdentifier());
            } else {
                // Another user created the task in a shared folder. Normally there can be only one user having the task in its folder who
                // is not participant. And this is the delegator of the task.
                Set<Folder> nonParticipantFolder = FolderStorage.extractNonParticipantFolder(
                    getFolders(),
                    ParticipantStorage.extractInternal(getParticipants()));
                if (nonParticipantFolder.size() > 0) {
                    orig.setParentFolderID(nonParticipantFolder.iterator().next().getIdentifier());
                } else {
                    throw TaskExceptionCode.UNKNOWN_DELEGATOR.create(I(orig.getCreatedBy()));
                }
            }
        }
        final EventClient eventClient = new EventClient(session);
        switch (changedParticipant.getConfirm()) {
        case CalendarObject.ACCEPT:
            eventClient.accept(orig, getFilledChangedTask());
            break;
        case CalendarObject.DECLINE:
            eventClient.declined(orig, getFilledChangedTask());
            break;
        case CalendarObject.TENTATIVE:
            eventClient.tentative(orig, getFilledChangedTask());
            break;
        }
    }

    /**
     * Gives the new last modified attribute of the changed task. This can be
     * only requested after {@link #prepare()} has been called.
     * @return the new last modified of the changed task.
     */
    Date getLastModified() {
        return changedTask.getLastModified();
    }

    // =========================== internal helper methods =====================

    private Task getOrigTask() throws OXException {
        if (null == origTask) {
            origTask = storage.selectTask(ctx, taskId, StorageType.ACTIVE);
        }
        return origTask;
    }

    /**
     * @return the participant getting the confirmation applied.
     */
    private InternalParticipant getOrigParticipant() throws OXException {
        if (null == origParticipant) {
            origParticipant = ParticipantStorage.getParticipant(ParticipantStorage.extractInternal(getParticipants()), userId);
            if (null == origParticipant) {
                throw TaskExceptionCode.PARTICIPANT_NOT_FOUND.create(I(userId), I(taskId));
            }
        }
        return origParticipant;
    }

    /**
     * @return the folder of the confirming participant through that the task is seen.
     */
    private FolderObject getFolder() throws OXException {
        if (null == folder) {
            Folder tmpFolder = FolderStorage.extractFolderOfUser(getFolders(), userId);
            if (null == tmpFolder) {
                if (getFolders().isEmpty()) {
                    throw TaskExceptionCode.MISSING_FOLDER.create(I(taskId));
                }
                tmpFolder = getFolders().iterator().next();
            }
            folder = Tools.getFolder(ctx, tmpFolder.getIdentifier());
        }
        return folder;
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
        Task task = getOrigTask();
        task.setParticipants(TaskLogic.createParticipants(getParticipants()));
        task.setUsers(TaskLogic.createUserParticipants(getParticipants()));
        task.setParentFolderID(getFolder().getObjectID());
        filledTask = true;
    }

    private Set<TaskParticipant> getParticipants() throws OXException {
        if (null == participants) {
            participants = partStor.selectParticipants(ctx, taskId, StorageType.ACTIVE);
        }
        return participants;
    }

    private Set<Folder> getFolders() throws OXException {
        if (null == folders) {
            folders =  foldStor.selectFolder(ctx, taskId, StorageType.ACTIVE);
        }
        return folders;
    }

    private boolean filledChangedTask = false;

    private Task getFilledChangedTask() throws OXException {
        if (!filledChangedTask) {
            final Task oldTask = getOrigTask();
            for (final Mapper mapper : Mapping.MAPPERS) {
                if (!mapper.isSet(changedTask) && mapper.isSet(getOrigTask())) {
                    mapper.set(changedTask, mapper.get(getOrigTask()));
                }
            }
            changedTask.setParticipants(origTask.getParticipants());
            changedTask.setUsers(origTask.getUsers());
            changedTask.setParentFolderID(oldTask.getParentFolderID());
        }
        return changedTask;
    }
}
