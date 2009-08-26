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

package com.openexchange.folderstorage.internal.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.folderstorage.internal.Tools;
import com.openexchange.folderstorage.internal.UserizedFolderImpl;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractUserizedFolderAction} - Abstract super class for actions which return one or multiple instances of {@link UserizedFolder}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractUserizedFolderAction extends AbstractAction {

    private TimeZone timeZone;

    /**
     * Initializes a new {@link AbstractUserizedFolderAction}.
     * 
     * @param session The session
     */
    public AbstractUserizedFolderAction(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link AbstractUserizedFolderAction}.
     * 
     * @param user The user
     * @param context The context
     */
    public AbstractUserizedFolderAction(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Initializes a new {@link Create}.
     * 
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public AbstractUserizedFolderAction(final ServerSession session, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(session, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link Create}.
     * 
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public AbstractUserizedFolderAction(final User user, final Context context, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
    }

    /**
     * Gets the time zone.
     * 
     * @return The time zone
     */
    protected TimeZone getTimeZone() {
        if (null == timeZone) {
            timeZone = Tools.getTimeZone(getUser().getTimeZone());
        }
        return timeZone;
    }

    /**
     * Gets the user-sensitive folder for given folder.
     * 
     * @param folder The folder
     * @param ownPermission The user's permission on given folder
     * @param treeId The tree identifier
     * @param all <code>true</code> to add all subfolders; otherwise <code>false</code> to only add subscribed ones
     * @param nullIsPublicAccess <code>true</code> if a <code>null</code> value obtained from {@link Folder#getSubfolderIDs()} hints to
     *            publicly accessible folder; otherwise <code>false</code>
     * @param openedStorages The list of opened storages
     * @return The user-sensitive folder for given folder
     * @throws FolderException If a folder error occurs
     */
    protected UserizedFolder getUserizedFolder(final Folder folder, final Permission ownPermission, final String treeId, final boolean all, final boolean nullIsPublicAccess, final java.util.Collection<FolderStorage> openedStorages) throws FolderException {
        final UserizedFolder userizedFolder = new UserizedFolderImpl(folder);
        userizedFolder.setLocale(getUser().getLocale());
        // Permissions
        userizedFolder.setOwnPermission(ownPermission);
        CalculatePermission.calculateUserPermissions(userizedFolder, getContext());
        // Type
        final boolean isShared;
        if (userizedFolder.getCreatedBy() != getUser().getId() && PrivateType.getInstance().equals(userizedFolder.getType())) {
            userizedFolder.setType(SharedType.getInstance());
            userizedFolder.setSubfolderIDs(new String[0]);
            isShared = true;
        } else {
            isShared = false;
        }
        // Time zone offset and last-modified in UTC
        {
            final Date cd = folder.getCreationDate();
            if (null != cd) {
                userizedFolder.setCreationDate(new Date(addTimeZoneOffset(cd.getTime(), getTimeZone())));
            }
        }
        {
            final Date lm = folder.getLastModified();
            if (null != lm) {
                userizedFolder.setLastModified(new Date(addTimeZoneOffset(lm.getTime(), getTimeZone())));
                userizedFolder.setLastModifiedUTC(new Date(lm.getTime()));
            }
        }
        if (!isShared) {
            hasVisibleSubfolderIDs(folder, treeId, all, userizedFolder, nullIsPublicAccess, openedStorages);
        }
        return userizedFolder;
    }

    private void hasVisibleSubfolderIDs(final Folder folder, final String treeId, final boolean all, final UserizedFolder userizedFolder, final boolean nullIsPublicAccess, final java.util.Collection<FolderStorage> openedStorages) throws FolderException {
        // Subfolders
        final String[] subfolders = folder.getSubfolderIDs();
        final java.util.List<String> visibleSubfolderIds;
        if (null == subfolders) {
            if (nullIsPublicAccess) {
                // A null value hints to a special folder; e.g. a system folder which contains subfolder for all users
                visibleSubfolderIds = new ArrayList<String>(1);
                visibleSubfolderIds.add("dummyId");
            } else {
                // Get appropriate storages and start transaction
                final String folderId = folder.getID();
                final FolderStorage[] ss = folderStorageDiscoverer.getFolderStoragesForParent(treeId, folderId);
                visibleSubfolderIds = new ArrayList<String>(1);
                for (int i = 0; visibleSubfolderIds.size() <= 0 && i < ss.length; i++) {
                    final FolderStorage curStorage = ss[i];
                    boolean alreadyOpened = false;
                    final Iterator<FolderStorage> it = openedStorages.iterator();
                    for (int j = 0; !alreadyOpened && j < openedStorages.size(); j++) {
                        if (it.next().equals(curStorage)) {
                            alreadyOpened = true;
                        }
                    }
                    if (!alreadyOpened) {
                        curStorage.startTransaction(getStorageParameters(), false);
                        openedStorages.add(curStorage);
                    }
                    final SortableId[] visibleIds = curStorage.getSubfolders(treeId, folderId, getStorageParameters());
                    if (visibleIds.length > 0) {
                        /*
                         * Found a storage which offers visible subfolder(s)
                         */
                        visibleSubfolderIds.add(visibleIds[0].getId());
                    }
                }
            }
        } else {
            visibleSubfolderIds = new ArrayList<String>(1);
            /*
             * Check until first visible subfolder found
             */
            for (int i = 0; visibleSubfolderIds.size() <= 0 && i < subfolders.length; i++) {
                final String id = subfolders[i];
                final FolderStorage tmp = getOpenedStorage(id, treeId, openedStorages);
                /*
                 * Get subfolder from appropriate storage
                 */
                final Folder subfolder = tmp.getFolder(treeId, id, getStorageParameters());
                /*
                 * Check for access rights and subscribed status dependent on parameter "all"
                 */
                final Permission subfolderPermission;
                if (null == getSession()) {
                    subfolderPermission = CalculatePermission.calculate(subfolder, getUser(), getContext());
                } else {
                    subfolderPermission = CalculatePermission.calculate(subfolder, getSession());
                }
                if (subfolderPermission.getFolderPermission() > Permission.NO_PERMISSIONS && (all ? true : subfolder.isSubscribed())) {
                    visibleSubfolderIds.add(id);
                }
            }
        }
        userizedFolder.setSubfolderIDs(visibleSubfolderIds.toArray(new String[visibleSubfolderIds.size()]));
    }

    private static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return (date + timeZone.getOffset(date));
    }

}
