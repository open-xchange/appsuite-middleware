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

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FolderPath;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link OXFolderManager} - Offers routines for folder creation, update and deletion.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class OXFolderManager {

    /**
     * Gets an appropriate instance of {@link OXFolderManager}.
     *
     * @param session The session
     * @return An appropriate instance of {@link OXFolderManager}.
     * @throws OXException If an appropriate instance of {@link OXFolderManager} cannot be generated
     */
    public static final OXFolderManager getInstance(final Session session) throws OXException {
        return new OXFolderManagerImpl(session);
    }

    /**
     * Gets an appropriate instance of {@link OXFolderManager}.
     *
     * @param session The session
     * @param oxfolderAccess An instance of {@link OXFolderAccess} to use; may be <code>null</code>
     * @return An appropriate instance of {@link OXFolderManager}
     * @throws OXException If an appropriate instance of {@link OXFolderManager} cannot be generated
     */
    public static final OXFolderManager getInstance(final Session session, final OXFolderAccess oxfolderAccess) throws OXException {
        return new OXFolderManagerImpl(session, oxfolderAccess);
    }

    /**
     * Gets an appropriate instance of {@link OXFolderManager}.
     *
     * @param session The session
     * @param readCon A connection with read capability; pass <code>null</code> to fetch from pool
     * @param writeCon A connection with write capability; pass <code>null</code> to fetch from pool
     * @return An appropriate instance of {@link OXFolderManager}
     * @throws OXException If an appropriate instance of {@link OXFolderManager} cannot be generated
     */
    public static final OXFolderManager getInstance(final Session session, final Connection readCon, final Connection writeCon) throws OXException {
        return new OXFolderManagerImpl(session, readCon, writeCon);
    }

    /**
     * Gets an appropriate instance of {@link OXFolderManager}.
     *
     * @param session The session
     * @param handlers Possible handlers to invoke on update
     * @param readCon A connection with read capability; pass <code>null</code> to fetch from pool
     * @param writeCon A connection with write capability; pass <code>null</code> to fetch from pool
     * @return An appropriate instance of {@link OXFolderManager}
     * @throws OXException If an appropriate instance of {@link OXFolderManager} cannot be generated
     */
    public static final OXFolderManager getInstance(Session session, Collection<UpdatedFolderHandler> handlers, Connection readCon, Connection writeCon) throws OXException {
        return new OXFolderManagerImpl(session, handlers, readCon, writeCon);
    }

    /**
     * Gets an appropriate instance of {@link OXFolderManager}.
     *
     * @param session The session
     * @param oxfolderAccess An instance of {@link OXFolderAccess} to use; may be <code>null</code>
     * @param readCon A connection with read capability; pass <code>null</code> to fetch from pool
     * @param writeCon A connection with write capability; pass <code>null</code> to fetch from pool
     * @return An appropriate instance of {@link OXFolderManager}
     * @throws OXException If an appropriate instance of {@link OXFolderManager} cannot be generated
     */
    public static final OXFolderManager getInstance(final Session session, final OXFolderAccess oxfolderAccess, final Connection readCon, final Connection writeCon) throws OXException {
        return new OXFolderManagerImpl(session, oxfolderAccess, null, readCon, writeCon);
    }

    /**
     * Gets the warnings
     *
     * @return The warnings
     */
    public abstract List<OXException> getWarnings();

    /**
     * Creates a folder filled with values from given folder object. <b>NOTE:</b> given instance of <tt>FolderObject</tt> is going to be
     * completely filled from storage. Thus it does not matter if you further work on this routine's return value or with parameter value.
     *
     * @return An instance of <tt>FolderObject</tt> representing newly created folder
     */
    public abstract FolderObject createFolder(FolderObject fo, boolean checkPermissions, long createTime) throws OXException;

    /**
     * Updates an existing folder according to changes contained in given folder object. <b>NOTE:</b> given instance of
     * <tt>FolderObject</tt> is going to be completely filled from storage. Thus it does not matter if you further work on this routine's
     * return value or with parameter value.
     * <p>
     * Possible operations here: rename, move and/or permissions update: When a rename should be performed, given folder object should
     * contain field 'folder name', so that invocation of <tt>FolderObject.containsFolderName()</tt> returns <tt>true</tt>. If a move should
     * be done, routine <tt>FolderObject.containsParentFolderID()</tt> should return <tt>true</tt>. Last, but not least, if an update of
     * folder's permissions should be done, routine <tt>FolderObject.containsPermissions()</tt> should return <tt>true</tt>. Changed
     * permissions are not handed down to subfolders implicitly.
     * </p>
     *
     * @param fo The folder to update
     * @param checkPermissions <code>true</code> to check permissions, <code>false</code>, otherwise
     * @param lastModified The last-modified time stamp which is written into database; usually {@link System#currentTimeMillis()}
     * @return An instance of <tt>FolderObject</tt> representing modified folder
     */
    public FolderObject updateFolder(FolderObject fo, boolean checkPermissions, long lastModified) throws OXException {
        return updateFolder(fo, checkPermissions, false, lastModified);
    }

    /**
     * Updates an existing folder according to changes contained in given folder object. <b>NOTE:</b> given instance of
     * <tt>FolderObject</tt> is going to be completely filled from storage. Thus it does not matter if you further work on this routine's
     * return value or with parameter value.
     * <p>
     * Possible operations here: rename, move and/or permissions update: When a rename should be performed, given folder object should
     * contain field 'folder name', so that invocation of <tt>FolderObject.containsFolderName()</tt> returns <tt>true</tt>. If a move should
     * be done, routine <tt>FolderObject.containsParentFolderID()</tt> should return <tt>true</tt>. Last, but not least, if an update of
     * folder's permissions should be done, routine <tt>FolderObject.containsPermissions()</tt> should return <tt>true</tt>.
     * </p>
     *
     * @param fo The folder to update
     * @param checkPermissions <code>true</code> to check permissions, <code>false</code>, otherwise
     * @param handDown <code>true</code> if permissions are supposed to be handed down to subfolders, <code>false</code>, otherwise
     * @param lastModified The last-modified time stamp which is written into database; usually {@link System#currentTimeMillis()}
     * @return An instance of <tt>FolderObject</tt> representing modified folder
     */
    public abstract FolderObject updateFolder(FolderObject fo, boolean checkPermissions, boolean handDown, long lastModified) throws OXException;

    /**
     * Deletes a folder identified by given folder object. This operation causes a recursive traversal of all folder's subfolders to check
     * if user can delete them, too. Furthermore user's permission on contained objects are checked as well. <b>NOTE:</b> given instance of
     * <tt>FolderObject</tt> is going to be completely filled from storage. Thus it does not matter if you further work on this routine's
     * return value or with parameter value.
     * <p>
     * Calling this method has the same effect as invoking {@link #deleteFolder(FolderObject, boolean, long, boolean)} with
     * <code>hardDelete</code> set to <code>false</code>, i.e. if a trash folder is available, and the folder is not yet located
     * below that trash folder, it is backed up, otherwise it is deleted permanently.
     *
     * @param fo The folder object at least containing the ID of the folder that shall be deleted
     * @param checkPermissions Whether permissions shall be checked or not
     * @param lastModified The last-modified time stamp which is written into database; usually {@link System#currentTimeMillis()}.
     * @return An instance of <tt>FolderObject</tt> representing deleted folder
     */
    public FolderObject deleteFolder(FolderObject fo, boolean checkPermissions, long lastModified) throws OXException {
        return deleteFolder(fo, checkPermissions, lastModified, false);
    }

    /**
     * Deletes a folder identified by given folder object. This operation causes a recursive traversal of all folder's subfolders to check
     * if user can delete them, too. Furthermore user's permission on contained objects are checked as well. <b>NOTE:</b> given instance of
     * <tt>FolderObject</tt> is going to be completely filled from storage. Thus it does not matter if you further work on this routine's
     * return value or with parameter value.
     * <p>
     * If <code>hardDelete</code> is not set, the storage supports a trash folder, and the folder is not yet located below that trash
     * folder, it is backed up (including the subfolder tree), otherwise it is deleted permanently.
     * <p>
     * While another backup folder with the same name already exists below default trash folder, an increasing serial number is appended to
     * folder name until its name is unique inside default trash folder's subfolders. E.g.: If folder "DeleteMe" already exists below
     * default trash folder, the next name would be "DeleteMe (2)". If again a folder "DeleteMe (2)" already exists below default trash
     * folder, the next name would be "DeleteMe (3)", and so no.
     *
     * @param fo The folder object at least containing the ID of the folder that shall be deleted
     * @param checkPermissions Whether permissions shall be checked or not
     * @param lastModified The last-modified time stamp which is written into database; usually {@link System#currentTimeMillis()}.
     * @param hardDelete <code>true</code> to permanently delete the folder, <code>false</code> to move the folder to the default
     *                   trash folder if possible
     * @return An instance of <tt>FolderObject</tt> representing deleted folder
     */
    public FolderObject deleteFolder(FolderObject fo, boolean checkPermissions, long lastModified, boolean hardDelete) throws OXException {
        return deleteFolder(fo, checkPermissions, lastModified, hardDelete, null);
    }

    /**
     * Deletes a folder identified by given folder object. This operation causes a recursive traversal of all folder's subfolders to check
     * if user can delete them, too. Furthermore user's permission on contained objects are checked as well. <b>NOTE:</b> given instance of
     * <tt>FolderObject</tt> is going to be completely filled from storage. Thus it does not matter if you further work on this routine's
     * return value or with parameter value.
     * <p>
     * If <code>hardDelete</code> is not set, the storage supports a trash folder, and the folder is not yet located below that trash
     * folder, it is backed up (including the subfolder tree), otherwise it is deleted permanently.
     * <p>
     * While another backup folder with the same name already exists below default trash folder, an increasing serial number is appended to
     * folder name until its name is unique inside default trash folder's subfolders. E.g.: If folder "DeleteMe" already exists below
     * default trash folder, the next name would be "DeleteMe (2)". If again a folder "DeleteMe (2)" already exists below default trash
     * folder, the next name would be "DeleteMe (3)", and so no.
     *
     * @param fo The folder object at least containing the ID of the folder that shall be deleted
     * @param checkPermissions Whether permissions shall be checked or not
     * @param lastModified The last-modified time stamp which is written into database; usually {@link System#currentTimeMillis()}.
     * @param hardDelete <code>true</code> to permanently delete the folder, <code>false</code> to move the folder to the default
     *                   trash folder if possible
     * @param originPath
     * @return An instance of <tt>FolderObject</tt> representing deleted folder
     */
    public abstract FolderObject deleteFolder(FolderObject fo, boolean checkPermissions, long lastModified, boolean hardDelete, FolderPath originPath) throws OXException;

    /**
     * Deletes the validated folder.
     *
     * @param folderID The folder ID
     * @param lastModified The last-modified time stamp
     * @param type The folder type
     * @throws OXException If deletion fails
     */
    public abstract void deleteValidatedFolder(final int folderID, final long lastModified, final int type, final boolean hardDelete) throws OXException;

    /**
     * Clears a folder's content so that all items located in given folder are going to be deleted. <b>NOTE:</b> the returned instance of
     * <tt>FolderObject</tt> is the parameter object itself. Thus it does not matter if you further work on this routine's return value or
     * with parameter value.
     *
     * @return The cleaned instance of <tt>FolderObject</tt>
     */
    public abstract FolderObject clearFolder(FolderObject fo, boolean checkPermissions, long lastModified) throws OXException;

    /**
     * This routine is called through AJAX' folder tests!
     */
    public abstract void cleanUpTestFolders(int[] fuids, Context ctx);

    /**
     * Removes all file locks within this folder for the given users.
     *
     * @param folder The folder object at least containing the ID of the folder.
     * @param userIds The ids of the users holding the locks
     * @throws OXException
     */
    public abstract void cleanLocksForFolder(FolderObject folder, int userIds[]) throws OXException;

}
