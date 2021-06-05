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

package com.openexchange.user.copy.internal.tasks;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
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
import com.openexchange.user.User;
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

    @Override
    public String[] getAlreadyCopied() {
        return new String[] { UserCopyTask.class.getName(), ContextLoadTask.class.getName(), ConnectionFetcherTask.class.getName(), FolderCopyTask.class.getName() };
    }

    @Override
    public String getObjectName() {
        return Task.class.getName();
    }

    @Override
    public IntegerMapping copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);

        srcCtx = copyTools.getSourceContext();
        dstCtx = copyTools.getDestinationContext();

        if (null == copyTools.getSourceUserId() || null == copyTools.getDestinationUserId()) {
            throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create();
        }

        srcUserId = copyTools.getSourceUserId().intValue();
        dstUserId = copyTools.getDestinationUserId().intValue();

        srcCon = copyTools.getSourceConnection();
        dstCon = copyTools.getDestinationConnection();

        final ObjectMapping<FolderObject> folderMapping = copyTools.getFolderMapping();
        final Set<Integer> sourceFolderIds = folderMapping.getSourceKeys();

        final Map<Integer, Task> tasks = loadTasksFromDatabase(srcCon, srcCtx, srcUserId, sourceFolderIds);
        addParticipants(tasks);
        exchangeIds(tasks, folderMapping, dstUserId, dstCtx, dstCon);
        writeTasksToDatabase(dstCon, dstCtx, srcCtx, dstUserId, tasks);

        final IntegerMapping mapping = new IntegerMapping();
        for (Iterator<Entry<Integer, Task>> iterator = tasks.entrySet().iterator(); iterator.hasNext();) {
            Entry<Integer, Task> entry = iterator.next();
            mapping.addMapping(entry.getKey(), I(entry.getValue().getObjectID()));
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
                        tasks.put(I(task.getObjectID()), task);
                    }
                } finally {
                    if (taskIterator != null) {
                        taskIterator.close();
                    }
                }
            }
        } catch (OXException e) {
            throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create(e);
        }

        return tasks;
    }

    @SuppressWarnings("deprecation")
    private void writeTasksToDatabase(final Connection dstCon, final Context dstCtx, final Context srcCtx, final int dstUserId, final Map<Integer, Task> tasks) throws OXException {
        final Set<Folder> newFolders = new HashSet<Folder>();
        for (Iterator<Entry<Integer, Task>> iterator = tasks.entrySet().iterator(); iterator.hasNext();) {
            Entry<Integer, Task> entry = iterator.next();
            final Task task = entry.getValue();
            try {
                TaskStorage.getInstance().insertTask(dstCtx, dstCon, task, StorageType.ACTIVE);
                final Set<Folder> source = FolderStorage.getInstance().selectFolder(srcCtx, srcCon, entry.getKey().intValue(), StorageType.ACTIVE);
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
                                internals.add(new InternalParticipant(tmp, null));
                            } else {
                                final User user = service.getUser(p.getIdentifier(), srcCtx);
                                String emailAddress = user.getMail();
                                if (emailAddress != null) {
                                    // EMail address is required for an external participant
                                    final ExternalUserParticipant extParticipant = new ExternalUserParticipant(emailAddress);
                                    extParticipant.setDisplayName(user.getDisplayName());
                                    extParticipant.setIdentifier(p.getIdentifier());
                                    externals.add(new ExternalParticipant(extParticipant));
                                }
                            }
                        } else if (p.getType() == Participant.EXTERNAL_USER) {
                            String emailAddress = p.getEmailAddress();
                            if (emailAddress != null) {
                                // EMail address is required for an external participant
                                final ExternalUserParticipant extParticipant = new ExternalUserParticipant(emailAddress);
                                extParticipant.setDisplayName(p.getDisplayName());
                                extParticipant.setIdentifier(p.getIdentifier());
                                externals.add(new ExternalParticipant(extParticipant));
                            }
                        } else {
                            continue;
                        }
                    }
                }
                ParticipantStorage.getInstance().insertInternals(dstCtx, dstCon, task.getObjectID(), internals, StorageType.ACTIVE);
                ParticipantStorage.getInstance().insertExternals(dstCtx, dstCon, task.getObjectID(), externals, StorageType.ACTIVE);
            } catch (OXException e) {
                if (!OXCalendarExceptionCodes.TASK_UID_ALREDY_EXISTS.equals(e)) {
                    throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create(e);
                }
                // Otherwise continue...
            }
        }
    }

    private void addParticipants(final Map<Integer, Task> tasks) {
        for (Iterator<Entry<Integer, Task>> iterator = tasks.entrySet().iterator(); iterator.hasNext();) {
            convertInternalToExternalParticipant(iterator.next().getValue());
        }
    }

    @SuppressWarnings("deprecation")
    private void convertInternalToExternalParticipant(final Task task) {
        if (task.getParticipants() != null) {
            final List<Participant> participants = new ArrayList<Participant>();
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
            task.setParticipants(participants);
        }
    }

    @SuppressWarnings("deprecation")
    private void exchangeIds(final Map<Integer, Task> tasks, final ObjectMapping<FolderObject> folderMapping, final int userId, final Context ctx, final Connection con) throws OXException {
        final Map<Integer, Task> series = new HashMap<Integer, Task>();
        try {
            for (Iterator<Entry<Integer, Task>> iterator = tasks.entrySet().iterator(); iterator.hasNext();) {
                Entry<Integer, Task> entry = iterator.next();
                final int newTaskId = IDGenerator.getId(ctx, com.openexchange.groupware.Types.TASK, con);
                final Task task = entry.getValue();
                task.setObjectID(newTaskId);
                task.setCreatedBy(userId);
                task.setModifiedBy(userId);
                if (task.getRecurrenceID() != -1) {
                    series.put(entry.getKey(), task);
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
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        }
    }

    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
        // Nothing to do
    }

}
