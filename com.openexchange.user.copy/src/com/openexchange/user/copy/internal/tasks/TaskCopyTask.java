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

package com.openexchange.user.copy.internal.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.ExternalParticipant;
import com.openexchange.groupware.tasks.Folder;
import com.openexchange.groupware.tasks.FolderStorage;
import com.openexchange.groupware.tasks.InternalParticipant;
import com.openexchange.groupware.tasks.ParticipantStorage;
import com.openexchange.groupware.tasks.StorageType;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskIterator;
import com.openexchange.groupware.tasks.TaskStorage;
import com.openexchange.user.UserService;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.IntegerMapping;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;

/**
 * {@link TaskCopyTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class TaskCopyTask implements CopyUserTaskService {

    private int srcUserId;
    private int dstUserId;
    private Context srcCtx;
    private Context dstCtx;
    private Connection srcCon;
    private Connection dstCon;

    private final UserService service;

    /**
     * Initializes a new {@link TaskCopyTask}.
     */
    public TaskCopyTask(final UserService service) {
        super();
        this.service = service;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] { UserCopyTask.class.getName(), ContextLoadTask.class.getName(), ConnectionFetcherTask.class.getName(), FolderCopyTask.class.getName()
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return Task.class.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public IntegerMapping copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);

        srcCtx = copyTools.getSourceContext();
        dstCtx = copyTools.getDestinationContext();

        srcUserId = copyTools.getSourceUserId();
        dstUserId = copyTools.getDestinationUserId();

        srcCon = copyTools.getSourceConnection();
        dstCon = copyTools.getDestinationConnection();

        final ObjectMapping<FolderObject> folderMapping = copyTools.getFolderMapping();
        final Set<Integer> sourceFolderIds = folderMapping.getSourceKeys();

        final Map<Integer, Task> tasks = loadTasksFromDatabase(srcCon, srcCtx, srcUserId, sourceFolderIds);
        addParticipants(tasks, srcCon, srcCtx, srcUserId);
        exchangeIds(tasks, folderMapping, dstUserId, dstCtx, dstCon);
        writeTasksToDatabase(dstCon, dstCtx, srcCtx, dstUserId, tasks, folderMapping);

        final IntegerMapping mapping = new IntegerMapping();
        for (final int taskId : tasks.keySet()) {
            final Task task = tasks.get(taskId);
            mapping.addMapping(taskId, task.getObjectID());
        }
        return mapping;
    }

    private Map<Integer, Task> loadTasksFromDatabase(final Connection con, final Context srcCtx, final int srcUserId, final Set<Integer> folderIds) throws OXException {
        final Map<Integer, Task> tasks = new HashMap<Integer, Task>();
        try {
            for (final int folderId : folderIds) {
                final TaskIterator taskIterator = TaskStorage.getInstance().list(srcCtx, folderId, 0, -1, 0, Order.NO_ORDER, Task.ALL_COLUMNS, false, srcUserId, false, con);
                try {
                    while (taskIterator.hasNext()) {
                        final Task task = taskIterator.next();
                        tasks.put(task.getObjectID(), task);
                    }
                } finally {
                    if (taskIterator != null) {
                        taskIterator.close();
                    }
                }
            }
        } catch (final OXException e) {
            throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create(e);
        }

        return tasks;
    }

    private void writeTasksToDatabase(final Connection dstCon, final Context dstCtx, final Context srcCtx, final int dstUserId, final Map<Integer, Task> tasks, final ObjectMapping<FolderObject> folderMapping) throws OXException {
        final Set<Folder> newFolders = new HashSet<Folder>();
        for (final int taskId : tasks.keySet()) {
            final Task task = tasks.get(taskId);
            try {
                TaskStorage.getInstance().insertTask(dstCtx, dstCon, task, StorageType.ACTIVE);
                final Set<Folder> source = FolderStorage.getInstance().selectFolder(srcCtx, srcCon, taskId, StorageType.ACTIVE);
                for (int i = 0; i < source.size(); i++) {
                    final Folder newFolder = new Folder(task.getParentFolderID(), dstUserId);
                    newFolders.add(newFolder);
                }
                FolderStorage.getInstance().insertFolder(dstCtx, dstCon, task.getObjectID(), newFolders, StorageType.ACTIVE);
                newFolders.clear();
                final Set<InternalParticipant> internals = new HashSet<InternalParticipant>();
                final Set<ExternalParticipant> externals = new HashSet<ExternalParticipant>();

                final Participant[] participants = task.getParticipants();
                if (participants != null) {
                    for (final Participant p : participants) {
                        if (p.getType() == Participant.USER) {
                            if (p.getIdentifier() == dstUserId) {
                                final UserParticipant tmp = new UserParticipant(p.getIdentifier());
                                tmp.setDisplayName(p.getDisplayName());
                                tmp.setEmailAddress(p.getEmailAddress());
                                final InternalParticipant internal = new InternalParticipant(tmp, null);
                                internals.add(internal);
                            } else {
                                final User user = service.getUser(p.getIdentifier(), srcCtx);
                                final ExternalUserParticipant extParticipant = new ExternalUserParticipant(user.getMail());
                                extParticipant.setDisplayName(user.getDisplayName());
                                extParticipant.setIdentifier(p.getIdentifier());
                                final ExternalParticipant ext = new ExternalParticipant(extParticipant);
                                externals.add(ext);
                            }
                        } else if (p.getType() == Participant.EXTERNAL_USER) {
                            final ExternalUserParticipant extParticipant = new ExternalUserParticipant(p.getEmailAddress());
                            extParticipant.setDisplayName(p.getDisplayName());
                            extParticipant.setIdentifier(p.getIdentifier());
                            final ExternalParticipant ext = new ExternalParticipant(extParticipant);
                            externals.add(ext);
                        } else {
                            continue;
                        }
                    }
                }
                ParticipantStorage.getInstance().insertInternals(dstCtx, dstCon, task.getObjectID(), internals, StorageType.ACTIVE);
                ParticipantStorage.getInstance().insertExternals(dstCtx, dstCon, task.getObjectID(), externals, StorageType.ACTIVE);
            } catch (final OXException e) {
                throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create(e);
            }
        }
    }

    private void addParticipants(final Map<Integer, Task> tasks, final Connection con, final Context ctx, final int userId) throws OXException {
        for (final int taskId : tasks.keySet()) {
            final Task task = tasks.get(taskId);
            convertInternalToExternalParticipant(task);
        }
    }

    private List<Participant> convertInternalToExternalParticipant(final Task task) throws OXException {
        final List<Participant> participants = new ArrayList<Participant>();
        if (task.getParticipants() != null) {
            for (final Participant p : task.getParticipants()) {
                final Participant participant;
                if (p.getType() == Participant.USER) {
                    if (p.getIdentifier() == srcUserId) {
                        participant = p;
                    } else {
                        final ExternalUserParticipant extParticipant = new ExternalUserParticipant(p.getEmailAddress());
                        extParticipant.setDisplayName(p.getDisplayName());
                        extParticipant.setIdentifier(p.getIdentifier());
                        participant = extParticipant;
                    }
                } else if (p.getType() == Participant.EXTERNAL_USER) {
                    final ExternalUserParticipant extParticipant = new ExternalUserParticipant(p.getEmailAddress());
                    extParticipant.setDisplayName(p.getDisplayName());
                    extParticipant.setIdentifier(p.getIdentifier());
                    participant = extParticipant;
                } else {
                    continue;
                }
                participants.add(participant);
            }
        }
        return participants;
    }

    private void exchangeIds(final Map<Integer, Task> tasks, final ObjectMapping<FolderObject> folderMapping, final int userId, final Context ctx, final Connection con) throws OXException {
        final Map<Integer, Task> series = new HashMap<Integer, Task>();
        try {
            for (final int taskId : tasks.keySet()) {
                final int newTaskId = IDGenerator.getId(ctx, com.openexchange.groupware.Types.TASK, con);
                final Task task = tasks.get(taskId);
                task.setObjectID(newTaskId);
                task.setCreatedBy(userId);
                task.setModifiedBy(userId);
                if (task.getRecurrenceID() != -1) {
                    series.put(taskId, task);
                }
                final FolderObject sourceFolder = folderMapping.getSource(task.getParentFolderID());
                int newParentFolderId = task.getParentFolderID();
                if (sourceFolder != null) {
                    final FolderObject destinationFolder = folderMapping.getDestination(sourceFolder);
                    newParentFolderId = destinationFolder.getObjectID();
                }
                task.setParentFolderID(newParentFolderId);
                final Participant[] participants = task.getParticipants();
                if (participants != null) {
                    for (final Participant p : participants) {
                        if (p.getType() == Participant.USER && p.getIdentifier() == srcUserId) {
                            final UserParticipant userParticipant = (UserParticipant) p;
                            userParticipant.setIdentifier(userId);
                            final int personalFolderId = userParticipant.getPersonalFolderId();
                            final FolderObject sourcePersonalFolder = folderMapping.getSource(personalFolderId);
                            if (sourcePersonalFolder != null) {
                                final FolderObject destinationPersonalFolder = folderMapping.getDestination(sourcePersonalFolder);
                                userParticipant.setPersonalFolderId(destinationPersonalFolder.getObjectID());
                            }
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        }
    }

    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
        // Nothing to do
    }

}
