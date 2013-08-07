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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderI18nNamesService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.FolderI18nNamesServiceImpl;
import com.openexchange.folderstorage.internal.FolderStorageRegistry;
import com.openexchange.folderstorage.internal.StorageParametersImpl;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractPerformer} - Abstract action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractPerformer {

    private static final Object PRESENT = new Object();

    /**
     * The constant indicating all content types are allowed.
     */
    protected static final List<ContentType> ALL_ALLOWED = Collections.emptyList();

    /**
     * Known working trees.
     */
    protected static final Set<String> KNOWN_TREES = Collections.<String> unmodifiableSet(new HashSet<String>(Arrays.asList(
        FolderStorage.REAL_TREE_ID,
        OutlookFolderStorage.OUTLOOK_TREE_ID)));

    protected final FolderStorageDiscoverer folderStorageDiscoverer;

    protected final ServerSession session;

    protected final User user;

    protected final Context context;

    protected StorageParameters storageParameters;

    private final Map<OXException, Object> warnings;

    protected boolean check4Duplicates;

    /**
     * Initializes a new {@link AbstractPerformer} from given session.
     *
     * @param session The session
     */
    protected AbstractPerformer(final ServerSession session) {
        this(session, FolderStorageRegistry.getInstance());
    }

    /**
     * Initializes a new {@link AbstractPerformer} from given session.
     *
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    protected AbstractPerformer(final ServerSession session, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super();
        this.folderStorageDiscoverer = folderStorageDiscoverer;
        this.session = session;
        // Pre-Initialize session
        final UserPermissionBits userPermissionBits = session.getUserPermissionBits();
        if (null != userPermissionBits) {
            userPermissionBits.isMultipleMailAccounts();
        }
        user = session.getUser();
        context = session.getContext();
        storageParameters = new StorageParametersImpl(session);
        warnings = new ConcurrentHashMap<OXException, Object>(2);
        check4Duplicates = true;
    }

    /**
     * Initializes a new {@link AbstractPerformer} from given user-context-pair.
     *
     * @param user The user
     * @param context The context
     */
    protected AbstractPerformer(final User user, final Context context) {
        this(user, context, FolderStorageRegistry.getInstance());
    }

    /**
     * Initializes a new {@link AbstractPerformer} from given user-context-pair.
     *
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    protected AbstractPerformer(final User user, final Context context, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super();
        this.folderStorageDiscoverer = folderStorageDiscoverer;
        session = null;
        this.user = user;
        this.context = context;
        storageParameters = new StorageParametersImpl(user, context);
        warnings = new ConcurrentHashMap<OXException, Object>(2);
        check4Duplicates = true;
    }

    /**
     * Sets the check4Duplicates flag.
     *
     * @param check4Duplicates The check4Duplicates to set
     */
    public void setCheck4Duplicates(final boolean check4Duplicates) {
        this.check4Duplicates = check4Duplicates;
    }

    /**
     * Checks for duplicate folder through a LIST request.
     *
     * @param name The name to check for
     * @param treeId The tree identifier
     * @param parentId The parent identifier
     * @param openedStorages The list containing already opened folder storages
     * @throws OXException If name look-up fails
     */
    protected void checkForDuplicate(final String name, final String treeId, final String parentId, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        final CheckForDuplicateResult result = getCheckForDuplicateResult(name, treeId, parentId, openedStorages);
        if (null != result) {
            throw result.error;
        }
    }

    /**
     * Checks for duplicate folder through a LIST request.
     *
     * @param name The name to check for
     * @param treeId The tree identifier
     * @param parentId The parent identifier
     * @param openedStorages The list containing already opened folder storages
     * @return The check result or <code>null</code> if no duplicate/conflict found
     * @throws OXException If name look-up fails
     */
    protected CheckForDuplicateResult getCheckForDuplicateResult(final String name, final String treeId, final String parentId, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        if (!check4Duplicates || null == name) {
            return null;
        }
        /*
         * Check for duplicate (if not real tree)
         */
        final Locale locale = storageParameters.getUser().getLocale();
        final String lcName = name.toLowerCase(locale);
        if (!FolderStorage.REAL_TREE_ID.equals(treeId)) {
            for (final UserizedFolder userizedFolder : new ListPerformer(session, null, folderStorageDiscoverer).doList(treeId, parentId, true, true)) {
                final String localizedName = userizedFolder.getLocalizedName(locale);
                if (localizedName.toLowerCase(locale).equals(lcName)) {
                    final FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, parentId);
                    checkOpenedStorage(realStorage, openedStorages);
                    final OXException e = FolderExceptionErrorMessage.EQUAL_NAME.create(name, realStorage.getFolder(FolderStorage.REAL_TREE_ID, parentId, storageParameters).getLocalizedName(locale), treeId);
                    return new CheckForDuplicateResult(userizedFolder.getID(), e);
                }
            }
        }
        /*
         * Check against possible i18n conflicts
         */
        final FolderI18nNamesService namesService = FolderI18nNamesServiceImpl.getInstance();
        final Set<String> i18nNames = namesService.getI18nNamesFor();
        for (final String reservedName : i18nNames) {
            if (reservedName.toLowerCase(locale).equals(lcName)) {
                final OXException e = FolderExceptionErrorMessage.RESERVED_NAME.create(name);
                return new CheckForDuplicateResult(null, e);
            }
        }
        return null;
    }

    private void checkOpenedStorage(final FolderStorage storage, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        for (final FolderStorage openedStorage : openedStorages) {
            if (openedStorage.equals(storage)) {
                return;
            }
        }
        if (storage.startTransaction(storageParameters, true)) {
            openedStorages.add(storage);
        }
    }

    /**
     * Gets the context information for an error message.
     *
     * @return The context information for an error message.
     */
    protected String getContextInfo4Error() {
        final Context context = this.context == null ? session.getContext() : this.context;
        if (null == context) {
            return "";
        }
        final String name = context.getName();
        if (null == name || 0 == name.length()) {
            return String.valueOf(context.getContextId());
        }
        return new com.openexchange.java.StringAllocator(16).append(name).append(" (").append(context.getContextId()).append(')').toString();
    }

    /**
     * Gets the user information for an error message.
     *
     * @return The user information for an error message.
     */
    protected String getUserInfo4Error() {
        final User user = this.user == null ? session.getUser() : this.user;
        if (null == user) {
            return "";
        }
        final String name = user.getDisplayName();
        if (null == name || 0 == name.length()) {
            return String.valueOf(user.getId());
        }
        return new com.openexchange.java.StringAllocator(16).append(name).append(" (").append(user.getId()).append(')').toString();
    }

    /**
     * Gets the folder information for an error message.
     *
     * @return The folder information for an error message.
     */
    protected String getFolderInfo4Error(final Folder folder) {
        if (null == folder) {
            return "";
        }
        final String name = folder.getLocalizedName(user == null ? session.getUser().getLocale() : user.getLocale());
        if (null == name || 0 == name.length()) {
            return folder.getID();
        }
        return new com.openexchange.java.StringAllocator(16).append(name).append(" (").append(folder.getID()).append(')').toString();
    }

    /**
     * Adds a warning to this performer. <br>
     * <b><small>NOTE</small></b>: Category is set to {@link Category#WARNING} if not done, yet.
     *
     * @param warning The warning to add
     */
    protected void addWarning(final OXException warning) {
        warning.addCategory(Category.CATEGORY_WARNING);
        warnings.put(warning, PRESENT);
    }

    /**
     * Gets the number of warnings.
     *
     * @return The number of warnings
     */
    public int getNumOfWarnings() {
        return warnings.size();
    }

    /**
     * Gets the warnings of this performer as an unmodifiable {@link Set set}.
     *
     * @return The warnings as an unmodifiable set
     */
    public Set<OXException> getWarnings() {
        return Collections.unmodifiableSet(warnings.keySet());
    }

    /**
     * Creates a new storage parameter instance.
     *
     * @return A new storage parameter instance.
     */
    protected StorageParameters newStorageParameters() {
        if (null == session) {
            return new StorageParametersImpl(user, context);
        }
        return new StorageParametersImpl(session);
    }

    /**
     * Gets an opened storage for given tree-folder-pair.
     *
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param storageParameters The storage parameters to use
     * @param openedStorages The collection of opened storages
     * @return An opened storage for given tree-folder-pair
     * @throws OXException If a folder error occurs
     */
    protected FolderStorage getOpenedStorage(final String id, final String treeId, final StorageParameters storageParameters, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        for (final FolderStorage ps : openedStorages) {
            if (ps.getFolderType().servesFolderId(id)) {
                // Found an already opened storage which is capable to server given folderId-treeId-pair
                return ps;
            }
        }
        // None opened storage is capable to server given folderId-treeId-pair
        final FolderStorage tmp = folderStorageDiscoverer.getFolderStorage(treeId, id);
        if (null == tmp) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
        }
        // Open storage and add to list of opened storages
        if (tmp.startTransaction(storageParameters, false)) {
            openedStorages.add(tmp);
        }
        return tmp;
    }

    /**
     * Checks if given folder storage is already contained in collection of opened storages. If yes, this method terminates immediately.
     * Otherwise the folder storage is opened according to specified modify flag and is added to specified collection of opened storages.
     *
     * @param checkMe The folder storage to check
     * @param modify <code>true</code> if the storage is supposed to be opened for a modifying operation; otherwise <code>false</code>
     * @param openedStorages The collection of already opened storages
     * @throws OXException If a folder error occurs
     */
    protected void checkOpenedStorage(final FolderStorage checkMe, final boolean modify, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        if (openedStorages.contains(checkMe)) {
            // Passed storage is already opened
            return;
        }
        // Passed storage has not been opened before. Open now and add to collection
        if (checkMe.startTransaction(storageParameters, modify)) {
            openedStorages.add(checkMe);
        }
    }

    /**
     * Gets the context.
     *
     * @return The context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContextId() {
        return context.getContextId();
    }

    /**
     * Sets the storage parameters.
     *
     * @param storageParameters The storage parameters
     */
    public void setStorageParameters(final StorageParameters storageParameters) {
        this.storageParameters = storageParameters;
    }

    /**
     * Gets the storage parameters.
     *
     * @return The storage parameters
     */
    public StorageParameters getStorageParameters() {
        return storageParameters;
    }

    /**
     * Gets the user.
     *
     * @return The user
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUserId() {
        return user.getId();
    }

    /**
     * Gets the session.
     *
     * @return The session
     */
    public ServerSession getSession() {
        return session;
    }

    /**
     * Gets the folder storage discoverer.
     *
     * @return The folder storage discoverer
     */
    public FolderStorageDiscoverer getFolderStorageDiscoverer() {
        return folderStorageDiscoverer;
    }

    /**
     * A check-for-duplicate result.
     */
    protected static final class CheckForDuplicateResult {

        protected final OXException error;
        protected final String optFolderId;

        protected CheckForDuplicateResult(final String optFolderId, final OXException error) {
            super();
            this.optFolderId = optFolderId;
            this.error = error;
        }

    } // End of class CheckForDuplicateResult

}
