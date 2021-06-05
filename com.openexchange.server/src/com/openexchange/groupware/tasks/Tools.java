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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.openexchange.database.IncorrectStringSQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.TaskParticipant.Type;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.i18n.LocalizableArgument;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.User;

/**
 * This class contains some tools methods for tasks.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Tools {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tools.class);

    /**
     * Prevent instantiation
     */
    private Tools() {
        super();
    }

    /**
     * Creates a dummy task for inserting into the deleted tables to tell clients that a task has been removed from some folder.
     *
     * @param identifier unique identifier of the task.
     * @param userId user identifier that created the task.
     * @param uid universal identifier for that task
     * @return a dummy task.
     */
    static Task createDummyTask(final int identifier, final int userId, final String uid) {
        final Task retval = new Task();
        retval.setObjectID(identifier);
        retval.setUid(uid);
        retval.setPrivateFlag(false);
        retval.setCreationDate(new Date());
        retval.setLastModified(new Date());
        retval.setCreatedBy(userId);
        retval.setModifiedBy(userId);
        retval.setRecurrenceType(CalendarObject.NO_RECURRENCE);
        retval.setNumberOfAttachments(0);
        return retval;
    }

    /**
     * @param folder the folder object.
     * @return <code>true</code> if the folder is a tasks folder, <code>false</code> otherwise.
     */
    static boolean isFolderTask(final FolderObject folder) {
        return FolderObject.TASK == folder.getModule();
    }

    /**
     * Checks if the folder is a public folder.
     *
     * @param folder folder object.
     * @return <code>true</code> if the folder is a public folder, <code>false</code> otherwise.
     */
    static boolean isFolderPublic(final FolderObject folder) {
        return FolderObject.PUBLIC == folder.getType();
    }

    /**
     * Checks if the folder is a private folder.
     *
     * @param folder folder object.
     * @return <code>true</code> if the folder is a private folder, <code>false</code> otherwise.
     */
    static boolean isFolderPrivate(final FolderObject folder) {
        return FolderObject.PRIVATE == folder.getType();
    }

    /**
     * Checks if the folder is a shared folder.
     *
     * @param folder folder object.
     * @param user requesting user.
     * @return <code>true</code> if the folder is a shared folder, <code>false</code> otherwise.
     */
    static boolean isFolderShared(final FolderObject folder, final User user) {
        return (FolderObject.PRIVATE == folder.getType() && folder.getCreatedBy() != user.getId());
    }

    /**
     * Returns the unique identifier of the users standard tasks folder.
     *
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @return the unique identifier of the users standard tasks folder.
     * @throws OXException if no database connection can be obtained or an error occurs while reading the folder.
     */
    static int getUserTaskStandardFolder(final Context ctx, final int userId) throws OXException {
        return new OXFolderAccess(ctx).getDefaultFolderID(userId, FolderObject.TASK);
    }

    /**
     * Reads a folder.
     *
     * @param ctx Context.
     * @param folderId unique identifier of the folder to read.
     * @return the folder object.
     * @throws OXException if no database connection can be obtained or an error occurs while reading the folder.
     */
    static FolderObject getFolder(final Context ctx, final int folderId) throws OXException {
        return new OXFolderAccess(ctx).getFolderObject(folderId);
    }

    /**
     * Reads a folder.
     *
     * @param ctx Context.
     * @param folderId unique identifier of the folder to read.
     * @return the folder object.
     * @throws OXException if no database connection can be obtained or an error occurs while reading the folder.
     * @throws OXFolderNotFoundException if the folder can not be found.
     */
    static FolderObject getFolder(final Context ctx, final Connection con, final int folderId) throws OXException {
        return new OXFolderAccess(con, ctx).getFolderObject(folderId);
    }

    static void fillStandardFolders(final Context ctx, final Set<InternalParticipant> participants) throws OXException {
        for (final InternalParticipant participant : participants) {
            if (UserParticipant.NO_PFID == participant.getFolderId()) {
                participant.setFolderId(Tools.getUserTaskStandardFolder(ctx, participant.getIdentifier()));
            }
        }
    }

    static void fillStandardFolders(final int cid, final int taskId, final Set<TaskParticipant> participants, final Set<Folder> folders, final boolean privat) {
        final Map<Integer, Folder> folderByUser = new HashMap<Integer, Folder>(folders.size(), 1);
        for (final Folder folder : folders) {
            folderByUser.put(I(folder.getUser()), folder);
        }
        for (final TaskParticipant participant : participants) {
            if (Type.INTERNAL == participant.getType()) {
                final InternalParticipant internal = (InternalParticipant) participant;
                Folder folder = folderByUser.get(I(internal.getIdentifier()));
                if (null == folder) {
                    if (privat) {
                        LOG.error(TaskExceptionCode.PARTICIPANT_FOLDER_INCONSISTENCY.create(I(internal.getIdentifier()), I(taskId), I(cid)).toString());
                    }
                    folder = new Folder(0, internal.getIdentifier());
                }
                internal.setFolderId(folder.getIdentifier());
            }
        }
    }

    static Context getContext(final int contextId) throws OXException {
        return ContextStorage.getStorageContext(contextId);
    }

    static UserConfiguration getUserConfiguration(final Context ctx, final int userId) throws OXException {
        return UserConfigurationStorage.getInstance().getUserConfiguration(userId, ctx);
    }

    static UserPermissionBits getUserPermissionBits(final Context ctx, final int userId) throws OXException {
        return UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, ctx);
    }

    static User getUser(final Context ctx, final int userId) throws OXException {
        return UserStorage.getInstance().getUser(userId, ctx);
    }

    public static OXException parseIncorrectString(IncorrectStringSQLException e) {
        final Mapper<?> mapper = SQL.mapColumn(e.getColumn());

        return createIncorrectStringException(mapper, e);
    }

    public static OXException parseIncorrectStringFromExternalParticipants(IncorrectStringSQLException e) {
        final Mapper<?> mapper = SQL.mapFromExternalParticipantsColumn(e.getColumn());

        Objects.requireNonNull(mapper, "No mapper for column '" + e.getColumn() + "' found. Please check your MySQL configuration for utf8mb4 support (via SHOW VARIABLES) as an insert for mentioned column normally should not fail.");
        return createIncorrectStringException(mapper, e);
    }

    private static OXException createIncorrectStringException(Mapper<?> mapper, IncorrectStringSQLException e) {
        final String incorrectString = e.getIncorrectString();
        OXException incorrectStringException = TaskExceptionCode.INCORRECT_STRING.create(e, incorrectString, new LocalizableArgument(mapper.getDisplayName()));
        incorrectStringException.addProblematic(new OXException.IncorrectString() {

            @Override
            public int getId() {
                return mapper.getId();
            }

            @Override
            public String getIncorrectString() {
                return incorrectString;
            }
        });
        return incorrectStringException;
    }
}
