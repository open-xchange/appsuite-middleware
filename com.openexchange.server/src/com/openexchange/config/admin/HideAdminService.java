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

package com.openexchange.config.admin;

import java.util.Collections;
import java.util.List;
import com.openexchange.annotation.Nullable;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.User;

/**
 * {@link HideAdminService}
 *
 * This service might remove the administrator from given {@link Collections}. Methods should only be called if the administrator has not explicitly been requested (i. e. 'all' requests)
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
@SingletonService
public interface HideAdminService {

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param searchIterator {@link SearchIterator} with current search results
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     */
    SearchIterator<Contact> removeAdminFromContacts(int contextId, @Nullable SearchIterator<Contact> searchIterator);

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param groups Array of {@link Group}s that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     */
    Group[] removeAdminFromGroupMemberList(int contextId, @Nullable Group[] groups);

    /**
     * Adds the the administrator entity to the group members if she was previously available. If the original object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * Should be used when a {@link Group} is going to be updated.
     *
     * @param contextId The context identifier
     * @param originalGroupMember The persisted members of the group
     * @param updatedGroupMember The new list of members that should be persisted.
     * @return int[] array containing the administrator if previously removed
     */
    int[] addAdminToGroupMemberList(int contextId, @Nullable int[] originalGroupMember, @Nullable int[] updatedGroupMember);

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param users Array of {@link User}s that should be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     */
    User[] removeAdminFromUsers(int contextId, @Nullable User[] users);

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param userIds Array of user identifiers that should be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     */
    int[] removeAdminFromUserIds(int contextId, @Nullable int[] userIds);

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param folders Array of {@link UserizedFolder}s that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     */
    UserizedFolder[] removeAdminFromFolderPermissions(int contextId, @Nullable UserizedFolder[] folders);

    /**
     * Adds the administrator permission to the given permissions array if previously available. If the original object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param originalPermissions The previously persisted folder permissions to identify if the permission has been set before
     * @param updatedPermissions The current permissions of the folder
     * @return A {@link Permission} array where the administrator is contained (if previously set)
     */
    Permission[] addAdminToFolderPermissions(int contextId, @Nullable Permission[] originalPermissions, @Nullable Permission[] updatedPermissions);

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param list A list of {@link ObjectPermission}s that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     */
    List<ObjectPermission> removeAdminFromObjectPermissions(int contextId, @Nullable List<ObjectPermission> objectPermissions);

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param documents {@link TimedResult} containing {@link DocumentMetadata} that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     */
    TimedResult<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable TimedResult<DocumentMetadata> documents);

    /**
     * Removes the administrator from the permissions of the given {@link DocumentMetadata}
     *
     * @param contextId The context identifier
     * @param documents {@link SearchIterator} containing {@link DocumentMetadata} that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     */
    SearchIterator<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable SearchIterator<DocumentMetadata> searchIterator);

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param delta {@link Delta} containing {@link DocumentMetadata} that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     */
    Delta<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable Delta<DocumentMetadata> delta);

    /**
     * Adds the administrator permission to the given list of {@link ObjectPermission} if previously available. If the original object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param originalPermissions The previously persisted {@link ObjectPermission}s to identify if the administrator had the permission before
     * @param updatedPermissions The permissions to persist
     * @return A new List of {@link ObjectPermission}s where the administrator is contained (if previously set)
     */
    List<ObjectPermission> addAdminToObjectPermissions(int contextId, @Nullable List<ObjectPermission> originalPermissions, @Nullable List<ObjectPermission> updatedPermissions);

    /**
     * Returns if the administrator should be available in responses.
     *
     * @param contextId The context id to check for.
     * @return boolean <code>true</code> if the administrator should be shown; otherwise <code>false</code>
     */
    boolean showAdmin(int contextId);

}
