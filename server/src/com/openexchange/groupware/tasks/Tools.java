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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.cache.impl.FolderCacheNotEnabledException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.groupware.tasks.TaskParticipant.Type;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;

/**
 * This class contains some tools methods for tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Tools {

    /**
     * Prevent instantiation
     */
    private Tools() {
        super();
    }

    /**
     * @param folder the folder object.
     * @return <code>true</code> if the folder is a tasks folder,
     * <code>false</code> otherwise.
     */
    static boolean isFolderTask(final FolderObject folder) {
        return FolderObject.TASK == folder.getModule();
    }

    /**
     * Checks if the folder is a public folder.
     * @param folder folder object.
     * @return <code>true</code> if the folder is a public folder,
     * <code>false</code> otherwise.
     */
    static boolean isFolderPublic(final FolderObject folder) {
        return FolderObject.PUBLIC == folder.getType();
    }

    /**
     * Checks if the folder is a private folder.
     * @param folder folder object.
     * @return <code>true</code> if the folder is a private folder,
     * <code>false</code> otherwise.
     */
    static boolean isFolderPrivate(final FolderObject folder) {
        return FolderObject.PRIVATE == folder.getType();
    }

    /**
     * Checks if the folder is a shared folder.
     * @param folder folder object.
     * @param user requesting user.
     * @return <code>true</code> if the folder is a shared folder,
     * <code>false</code> otherwise.
     */
    static boolean isFolderShared(final FolderObject folder, final User user) {
        return (FolderObject.PRIVATE == folder.getType()
            && folder.getCreatedBy() != user.getId());
    }

    /**
     * Returns the unique identifier of the users standard tasks folder.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @return the unique identifier of the users standard tasks folder.
     * @throws TaskException if no database connection can be obtained or an
     * error occurs while reading the folder.
     */
    static int getUserTaskStandardFolder(final Context ctx, final int userId)
        throws TaskException {
        int folder;
        try {
            folder = new OXFolderAccess(ctx).getDefaultFolder(userId,
                FolderObject.TASK).getObjectID();
        } catch (OXException e) {
            throw new TaskException(e);
        }
        return folder;
    }

    /**
     * Reads a folder.
     * @param ctx Context.
     * @param folderId unique identifier of the folder to read.
     * @return the folder object.
     * @throws TaskException if no database connection can be obtained or an
     * error occurs while reading the folder.
     */
    static FolderObject getFolder(final Context ctx, final int folderId)
        throws TaskException {
        // TODO Move database connection to folder methods.
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        FolderObject folder = null;
        try {
        	folder = new OXFolderAccess(con, ctx).getFolderObject(folderId);
        } catch (FolderCacheNotEnabledException e) {
            throw new TaskException(e);
        } catch (OXException e) {
            throw new TaskException(e);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
        return folder;
    }

    /**
     * Reads a folder.
     * @param ctx Context.
     * @param folderId unique identifier of the folder to read.
     * @return the folder object.
     * @throws TaskException if no database connection can be obtained or an
     * error occurs while reading the folder.
     * @throws OXFolderNotFoundException if the folder can not be found.
     */
    static FolderObject getFolder(final Context ctx, final Connection con,
        final int folderId) throws TaskException, OXFolderNotFoundException {
        FolderObject folder = null;
        try {
            folder = new OXFolderAccess(con, ctx).getFolderObject(folderId);
        } catch (final FolderCacheNotEnabledException e) {
            throw new TaskException(e);
        } catch (final OXFolderNotFoundException e) {
            throw e;
        } catch (final OXException e) {
            throw new TaskException(e);
        }
        return folder;
    }

    static void fillStandardFolders(final Context ctx,
        final Set<InternalParticipant> participants) throws TaskException {
        for (InternalParticipant participant : participants) {
            if (UserParticipant.NO_PFID == participant.getFolderId()) {
                participant.setFolderId(Tools.getUserTaskStandardFolder(ctx,
                    participant.getIdentifier()));
            }
        }
    }

    static void fillStandardFolders(final Set<TaskParticipant> participants,
        final Set<Folder> folders, final boolean privat) throws TaskException {
        final Map<Integer, Folder> folderByUser = new HashMap<Integer, Folder>(
            folders.size(), 1);
        for (Folder folder : folders) {
            folderByUser.put(Integer.valueOf(folder.getUser()), folder);
        }
        for (TaskParticipant participant : participants) {
            if (Type.INTERNAL == participant.getType()) {
                final InternalParticipant internal = (InternalParticipant)
                    participant;
                final Folder folder = folderByUser.get(Integer.valueOf(internal
                    .getIdentifier()));
                if (null == folder && privat) {
                    throw new TaskException(Code
                        .PARTICIPANT_FOLDER_INCONSISTENCY);
                }
                internal.setFolderId(folder.getIdentifier());
            }
        }
    }

    static Context getContext(final int contextId) throws TaskException {
        try {
            return ContextStorage.getStorageContext(contextId);
        } catch (ContextException e) {
            throw new TaskException(e);
        }
    }

    static UserConfiguration getUserConfiguration(final Session session)
        throws TaskException {
        return getUserConfiguration(getContext(session.getContextId()), session
            .getUserId());
    }

    static UserConfiguration getUserConfiguration(final Context ctx,
        final int userId) throws TaskException {
        try {
            return UserConfigurationStorage.getInstance().getUserConfiguration(
                userId, ctx);
        } catch (UserConfigurationException e) {
            throw new TaskException(e);
        }
    }
    
    static User getUser(final Session session) throws TaskException {
        return getUser(getContext(session.getContextId()), session.getUserId());
    }

    static User getUser(final Context ctx, final int userId)
        throws TaskException {
        try {
            return UserStorage.getInstance().getUser(userId, ctx);
        } catch (LdapException e) {
            throw new TaskException(e);
        }
    }

    /**
     * Converts a task exception into an OX exception.
     * @param exc task exception to convert.
     * @return an OX exception that can be thrown.
     */
    static OXException convert(final TaskException exc) {
        OXException retval;
        switch (exc.getDetail()) {
        case MANDATORY_FIELD:
            retval = new OXMandatoryFieldException(exc);
            break;
        case NOT_FOUND:
            retval = new OXObjectNotFoundException(exc);
            break;
        case PERMISSION:
            retval = new OXPermissionException(exc);
            break;
        case CONFLICT:
            retval = new OXConflictException(exc);
            break;
        case CONCURRENT_MODIFICATION:
            retval = new OXConcurrentModificationException(exc);
            break;
        case OTHER:
        default:
            retval = new OXException(exc);
        }
        return retval;
    }
}
