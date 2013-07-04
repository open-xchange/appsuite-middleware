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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.internal.performers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.folderstorage.internal.UserizedFolderImpl;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractUserizedFolderPerformer} - Abstract super class for actions which return one or multiple instances of
 * {@link UserizedFolder}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractUserizedFolderPerformer extends AbstractPerformer {

    private static final String DUMMY_ID = "dummyId";

    private final FolderServiceDecorator decorator;

    private volatile TimeZone timeZone;

    private volatile Locale locale;

    private volatile java.util.List<ContentType> allowedContentTypes;

    /**
     * Initializes a new {@link AbstractUserizedFolderPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     */
    public AbstractUserizedFolderPerformer(final ServerSession session, final FolderServiceDecorator decorator) {
        super(session);
        this.decorator = decorator;
        storageParameters.setDecorator(decorator);
    }

    /**
     * Initializes a new {@link AbstractUserizedFolderPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     */
    public AbstractUserizedFolderPerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context);
        this.decorator = decorator;
        storageParameters.setDecorator(decorator);
    }

    /**
     * Initializes a new {@link AbstractUserizedFolderPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public AbstractUserizedFolderPerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(session, folderStorageDiscoverer);
        this.decorator = decorator;
        storageParameters.setDecorator(decorator);
    }

    /**
     * Initializes a new {@link AbstractUserizedFolderPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public AbstractUserizedFolderPerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
        this.decorator = decorator;
        storageParameters.setDecorator(decorator);
    }

    /**
     * Those content type identifiers which are capable to accept folder names containing parenthesis characters.
     */
    protected static final Set<String> PARENTHESIS_CAPABLE = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        CalendarContentType.getInstance().toString(),
        TaskContentType.getInstance().toString(),
        ContactContentType.getInstance().toString(),
        InfostoreContentType.getInstance().toString(),
        FileStorageContentType.getInstance().toString())));

    /**
     * Gets the optional folder service decorator.
     *
     * @return The folder service decorator or <code>null</code>
     */
    protected final FolderServiceDecorator getDecorator() {
        return decorator;
    }

    /**
     * Gets the time zone.
     * <p>
     * If a {@link FolderServiceDecorator decorator} was set and its {@link FolderServiceDecorator#getTimeZone() getTimeZone()} method
     * returns a non-<code>null</code> value, then decorator's time zone is returned; otherwise user's time zone is returned.
     *
     * @return The time zone
     */
    protected final TimeZone getTimeZone() {
        TimeZone tmp = timeZone;
        if (null == tmp) {
            synchronized (this) {
                tmp = timeZone;
                if (null == tmp) {
                    final TimeZone tz = null == decorator ? null : decorator.getTimeZone();
                    timeZone = tmp = (tz == null ? TimeZoneUtils.getTimeZone(getUser().getTimeZone()) : tz);
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the locale.
     * <p>
     * If a {@link FolderServiceDecorator decorator} was set and its {@link FolderServiceDecorator#getLocale() getLocale()} method returns a
     * non-<code>null</code> value, then decorator's locale is returned; otherwise user's locale is returned.
     *
     * @return The locale
     */
    protected final Locale getLocale() {
        Locale tmp = locale;
        if (null == tmp) {
            synchronized (this) {
                tmp = locale;
                if (null == tmp) {
                    final Locale l = null == decorator ? null : decorator.getLocale();
                    locale = tmp = l == null ? getUser().getLocale() : l;
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the allowed content types.
     *
     * @return The allowed content types
     */
    protected final java.util.List<ContentType> getAllowedContentTypes() {
        java.util.List<ContentType> tmp = allowedContentTypes;
        if (null == tmp) {
            synchronized (this) {
                tmp = allowedContentTypes;
                if (null == tmp) {
                    tmp = null == decorator ? ALL_ALLOWED : decorator.getAllowedContentTypes();
                    allowedContentTypes = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the named property from decorator (if not <code>null</code>).
     *
     * @param name The property name
     * @return The property's value or <code>null</code>
     */
    @SuppressWarnings("unchecked")
    protected final <V> V getDecoratorProperty(final String name) {
        if (null == name || null == decorator) {
            return null;
        }
        final Object val = decorator.getProperty(name);
        return null == val ? null : (V) val;
    }

    /**
     * Gets the named property's string value from decorator (if not <code>null</code>).
     *
     * @param name The property name
     * @return The property's string value or <code>null</code>
     */
    protected final String getDecoratorStringProperty(final String name) {
        if (null == name || null == decorator) {
            return null;
        }
        final Object val = decorator.getProperty(name);
        return null == val ? null : val.toString();
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
     * @param storageParameters The storage parameters to use
     * @param openedStorages The list of opened storages
     * @return The user-sensitive folder for given folder
     * @throws OXException If a folder error occurs
     */
    protected UserizedFolder getUserizedFolder(final Folder folder, final Permission ownPermission, final String treeId, final boolean all, final boolean nullIsPublicAccess, final StorageParameters storageParameters, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        return getUserizedFolder(folder, ownPermission, treeId, all, nullIsPublicAccess, storageParameters, openedStorages, false);
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
     * @param storageParameters The storage parameters to use
     * @param openedStorages The list of opened storages
     * @return The user-sensitive folder for given folder
     * @throws OXException If a folder error occurs
     */
    protected UserizedFolder getUserizedFolder(final Folder folder, final Permission ownPermission, final String treeId, final boolean all, final boolean nullIsPublicAccess, final StorageParameters storageParameters, final java.util.Collection<FolderStorage> openedStorages, final boolean checkOnly) throws OXException {
        Folder f = folder;
        final UserizedFolder userizedFolder;
        /*-
         * Type
         *
         * Create user-sensitive folder dependent on shared flag
         */
        final boolean isShared;
        {
            final int createdBy = f.getCreatedBy();
            final Type type = f.getType();
            if (SharedType.getInstance().equals(type)) {
                userizedFolder = new UserizedFolderImpl(f, storageParameters.getSession(), storageParameters.getUser(), storageParameters.getContext());
                userizedFolder.setDefault(false);
                isShared = true;
            } else if ((createdBy >= 0) && (createdBy != getUserId()) && PrivateType.getInstance().equals(type)) {
                /*
                 * Prepare
                 */
                final FolderStorage curStorage = folderStorageDiscoverer.getFolderStorage(treeId, f.getID());
                boolean alreadyOpened = false;
                final Iterator<FolderStorage> it = openedStorages.iterator();
                for (int j = 0; !alreadyOpened && j < openedStorages.size(); j++) {
                    if (it.next().equals(curStorage)) {
                        alreadyOpened = true;
                    }
                }
                if (!alreadyOpened && curStorage.startTransaction(storageParameters, false)) {
                    openedStorages.add(curStorage);
                }
                f = curStorage.prepareFolder(treeId, f, storageParameters);
                userizedFolder = new UserizedFolderImpl(f, storageParameters.getSession(), storageParameters.getUser(), storageParameters.getContext());
                userizedFolder.setDefault(false);
                userizedFolder.setType(SharedType.getInstance());
                isShared = true;
            } else {
                userizedFolder = new UserizedFolderImpl(f, storageParameters.getSession(), storageParameters.getUser(), storageParameters.getContext());
                isShared = false;
            }
        }
        /*
         * Set locale
         */
        userizedFolder.setLocale(getLocale());
        /*
         * Permissions
         */
        userizedFolder.setOwnPermission(ownPermission);
        CalculatePermission.calculateUserPermissions(userizedFolder, getContext());
        /*
         * Check parent
         */
        if (userizedFolder.getID().startsWith(FolderObject.SHARED_PREFIX)) {
            userizedFolder.setParentID(FolderStorage.SHARED_ID);
        }
        /*
         * Time zone offset and last-modified in UTC
         */
        {
            final Date cd = f.getCreationDate();
            if (null != cd) {
                final long time = cd.getTime();
                userizedFolder.setCreationDate(new Date(addTimeZoneOffset(time, getTimeZone())));
                userizedFolder.setCreationDateUTC(new Date(time));
            }
        }
        {
            final Date lm = f.getLastModified();
            if (null != lm) {
                final long time = lm.getTime();
                userizedFolder.setLastModified(new Date(addTimeZoneOffset(time, getTimeZone())));
                userizedFolder.setLastModifiedUTC(new Date(time));
            }
        }
        if (!checkOnly) {
            if (isShared) {
                /*
                 * Subfolders already calculated for user
                 */
                final String[] visibleSubfolders = f.getSubfolderIDs();
                if (null == visibleSubfolders) {
                    userizedFolder.setSubfolderIDs(nullIsPublicAccess ? new String[] { DUMMY_ID } : new String[0]);
                } else {
                    userizedFolder.setSubfolderIDs(visibleSubfolders);
                }
            } else {
                /*
                 * Compute user-visible subfolders
                 */
                hasVisibleSubfolderIDs(f, treeId, all, userizedFolder, nullIsPublicAccess, storageParameters, openedStorages);
            }
        }
        return userizedFolder;
    }

    private void hasVisibleSubfolderIDs(final Folder folder, final String treeId, final boolean all, final UserizedFolder userizedFolder, final boolean nullIsPublicAccess, final StorageParameters storageParameters, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        /*
         * Subfolders
         */
        final String[] subfolders = folder.getSubfolderIDs();
        String dummyId = null;
        if (null == subfolders) {
            if (nullIsPublicAccess) {
                /*
                 * A null value hints to a special folder; e.g. a system folder which contains subfolder for all users
                 */
                dummyId = DUMMY_ID;
            } else {
                /*
                 * Get appropriate storages and start transaction
                 */
                final String folderId = folder.getID();
                final FolderStorage[] ss = folderStorageDiscoverer.getFolderStoragesForParent(treeId, folderId);
                for (int i = 0; (null == dummyId) && i < ss.length; i++) {
                    final FolderStorage curStorage = ss[i];
                    boolean alreadyOpened = false;
                    final Iterator<FolderStorage> it = openedStorages.iterator();
                    for (int j = 0; !alreadyOpened && j < openedStorages.size(); j++) {
                        if (it.next().equals(curStorage)) {
                            alreadyOpened = true;
                        }
                    }
                    if (!alreadyOpened && curStorage.startTransaction(storageParameters, false)) {
                        openedStorages.add(curStorage);
                    }
                    final SortableId[] visibleIds = curStorage.getSubfolders(treeId, folderId, storageParameters);
                    if (visibleIds.length > 0) {
                        /*
                         * Found a storage which offers visible subfolder(s)
                         */
                        for (int j = 0; (null == dummyId) && j < visibleIds.length; j++) {
                            final String id = visibleIds[0].getId();
                            final Folder subfolder = curStorage.getFolder(treeId, id, storageParameters);
                            if (all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) {
                                final Permission p = CalculatePermission.calculate(subfolder, session, getAllowedContentTypes());
                                if (p.isVisible()) {
                                    dummyId = id;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            final int length = subfolders.length;
            if (length > 0) {
                /*
                 * Check until a visible subfolder is found in case of a global folder
                 */
                if (folder.isGlobalID()) {
                    for (int i = 0; (null == dummyId) && i < length; i++) {
                        final String id = subfolders[i];
                        final FolderStorage tmp = getOpenedStorage(id, treeId, storageParameters, openedStorages);
                        /*
                         * Get subfolder from appropriate storage
                         */
                        final Folder subfolder = tmp.getFolder(treeId, id, storageParameters);
                        if ((all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) && CalculatePermission.isVisible(subfolder, getUser(), getContext(), getAllowedContentTypes())) {
                            dummyId = id;
                        }
                    }
                } else if (all || folder.hasSubscribedSubfolders()) { // User-only folder
                    dummyId = DUMMY_ID;
                }
            }
        }
        userizedFolder.setSubfolderIDs((null == dummyId) ? new String[0] : new String[] { dummyId });
    }

    private static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return (date + timeZone.getOffset(date));
    }

}
