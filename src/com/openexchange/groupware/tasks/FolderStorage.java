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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;

/**
 * Interface to different SQL implementations for storing task folder mapping.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class FolderStorage {

    /**
     * Singleton attribute.
     */
    private static final FolderStorage SINGLETON = new RdbFolderStorage();

    /**
     * Default constructor.
     */
    protected FolderStorage() {
        super();
    }

    /**
     * @return the singleton implementation.
     */
    public static FolderStorage getInstance() {
        return SINGLETON;
    }

    /**
     * Inserts task folder mappings.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param folders folder mappings to insert.
     * @param type storage type of folder mappings.
     * @throws TaskException if an exception occurs.
     */
    abstract void insertFolder(Context ctx, Connection con, int taskId,
        Set<Folder> folders, StorageType type) throws TaskException;

    /**
     * Inserts a task folder mapping.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param folder folder mapping to insert.
     * @param type storage type of folder mappings.
     * @throws TaskException if an exception occurs.
     */
    void insertFolder(final Context ctx, final Connection con, final int taskId,
        final Folder folder, final StorageType type) throws TaskException {
        final Set<Folder> folders = new HashSet<Folder>();
        folders.add(folder);
        insertFolder(ctx, con, taskId, folders, type);
    }

    /**
     * Reads the folder mappings of a task.
     * @param ctx Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task.
     * @param type storage type of the folder mappings.
     * @return the folder objects.
     * @throws TaskException if an exception occurs.
     */
    abstract Set<Folder> selectFolder(Context ctx, Connection con, int taskId,
        StorageType type) throws TaskException;

    /**
     * Reads the folder mappings of a task.
     * @param ctx Context.
     * @param taskId unique identifier of the task.
     * @param type storage type of the folder mappings.
     * @return the folder objects.
     * @throws TaskException if an exception occurs.
     */
    Set<Folder> selectFolder(final Context ctx, final int taskId,
        final StorageType type) throws TaskException {
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            return selectFolder(ctx, con, taskId, type);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Selects a task folder mapping for a user.
     * @param ctx Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task.
     * @param userId unique identifier of the user.
     * @param type storage type of folder mapping that should be selected.
     * @return the folder or <code>null</code> if no folder could be found
     * especially for the user. This can occur for public folders.
     * @throws TaskException if an exception occurs.
     */
    abstract Folder selectFolderByUser(Context ctx, Connection con, int taskId,
        int userId, StorageType type) throws TaskException;

    /**
     * Reads a task folder mapping.
     * @param ctx Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task.
     * @param folderId unique identifier of the folder.
     * @param type storage type of the folder mapping.
     * @return the folder object or <code>null</code> if no folder could be
     * found.
     * @throws TaskException if the folder isn't found or an error occurs.
     */
    abstract Folder selectFolderById(Context ctx, Connection con, int taskId,
        int folderId, StorageType type) throws TaskException;

    Folder selectFolderById(final Context ctx, final int taskId,
        final int folderId, final StorageType type) throws TaskException {
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            return selectFolderById(ctx, con, taskId, folderId, type);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Deletes task folder mappings.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param folderIds array of folder identifier to delete for the task.
     * @param type storage type of folder mapping that should be deleted.
     * @throws TaskException if an exception occurs or if not all folder can be
     * deleted.
     */
    abstract void deleteFolder(Context ctx, Connection con, int taskId,
        int[] folderIds, StorageType type) throws TaskException;

    /**
     * Deletes a task folder mapping.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param folderId folder identifier of the to delete mapping.
     * @param type storage type of folder mapping that should be deleted.
     * @throws TaskException if an exception occurs.
     */
    void deleteFolder(final Context ctx, final Connection con, final int taskId,
        final int folderId, final StorageType type) throws TaskException {
        deleteFolder(ctx, con, taskId, new int[] { folderId }, type);
    }

    /**
     * Deletes task folder mappings.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param folders set of folder to delete for the task.
     * @param type storage type of folder mapping that should be deleted.
     * @throws TaskException if an exception occurs.
     */
    void deleteFolder(final Context ctx, final Connection con, final int taskId,
        final Set<Folder> folders, final StorageType type)
        throws TaskException {
        if (0 == folders.size()) {
            return;
        }
        final int[] folderIds = new int[folders.size()];
        final Iterator<Folder> iter = folders.iterator();
        for (int i = 0; i < folderIds.length; i++) {
            folderIds[i] = iter.next().getIdentifier();
        }
        deleteFolder(ctx, con, taskId, folderIds, type);
    }

    /**
     * Selects all task folder mappings for a user.
     * @param ctx Context.
     * @param readCon readable database connection.
     * @param userId unique identifier of the user.
     * @param type storage type of the folder mapping to select.
     * @return an 2-dimensional int array. the first dimension represents all
     * found entries while the second contains folder and task identifier.
     * @throws TaskException if an exception occurs.
     */
    abstract int[][] searchFolderByUser(Context ctx, Connection readCon,
        int userId, StorageType type) throws TaskException;

    /**
     * @param folders Set of task folder mappings.
     * @param userId unique identifier of a user.
     * @return the folder mapping for the user.
     */
    static Folder extractFolderOfUser(final Set<Folder> folders,
        final int userId) {
        Folder retval = null;
        for (Folder folder : folders) {
            if (folder.getUser() == userId) {
                retval = folder;
                break;
            }
        }
        return retval;
    }

    static Folder getFolder(final Set<Folder> folders, final int folderId) {
        Folder retval = null;
        for (Folder folder : folders) {
            if (folder.getIdentifier() == folderId) {
                retval = folder;
                break;
            }
        }
        return retval;
    }

}
