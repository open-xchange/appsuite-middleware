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

package com.openexchange.config.admin;

import java.util.Collections;
import java.util.List;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.iterator.SearchIterator;

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
     * @throws OXException
     */
    SearchIterator<Contact> removeAdminFromContacts(int contextId, @Nullable SearchIterator<Contact> searchIterator) throws OXException;

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param groups Array of {@link Group}s that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     * @throws OXException
     */
    Group[] removeAdminFromGroupMemberList(int contextId, @Nullable Group[] groups) throws OXException;

    /**
     * Adds the the administrator entity to the group members if she was previously available. If the original object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * Should be used when a {@link Group} is going to be updated.
     *
     * @param contextId The context identifier
     * @param originalGroupMember The persisted members of the group
     * @param updatedGroupMember The new list of members that should be persisted.
     * @return int[] array containing the administrator if previously removed
     * @throws OXException
     */
    int[] addAdminToGroupMemberList(int contextId, @Nullable int[] originalGroupMember, @Nullable int[] updatedGroupMember) throws OXException;

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param users Array of {@link User}s that should be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     * @throws OXException
     */
    User[] removeAdminFromUsers(int contextId, @Nullable User[] users) throws OXException;

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param userIds Array of user identifiers that should be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     * @throws OXException
     */
    int[] removeAdminFromUserIds(int contextId, @Nullable int[] userIds) throws OXException;

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param folders Array of {@link UserizedFolder}s that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     * @throws OXException
     */
    UserizedFolder[] removeAdminFromFolderPermissions(int contextId, @Nullable UserizedFolder[] folders) throws OXException;

    /**
     * Adds the administrator permission to the given permissions array if previously available. If the original object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param originalPermissions The previously persisted folder permissions to identify if the permission has been set before
     * @param updatedPermissions The current permissions of the folder
     * @return A {@link Permission} array where the administrator is contained (if previously set)
     * @throws OXException
     */
    Permission[] addAdminToFolderPermissions(int contextId, @Nullable Permission[] originalPermissions, @Nullable Permission[] updatedPermissions) throws OXException;

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param list A list of {@link ObjectPermission}s that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     * @throws OXException
     */
    List<ObjectPermission> removeAdminFromObjectPermissions(int contextId, @Nullable List<ObjectPermission> objectPermissions) throws OXException;

    /**
     * Removes the administrator from given object and returns the result in an new object. If the provided object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param documents {@link TimedResult} containing {@link DocumentMetadata} that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     * @throws OXException
     */
    TimedResult<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable TimedResult<DocumentMetadata> documents) throws OXException;

    /**
     * Removes the administrator from the permissions of the given {@link DocumentMetadata}
     *
     * @param contextId The context identifier
     * @param documents {@link SearchIterator} containing {@link DocumentMetadata} that might be filtered
     * @return A new object that does not contain the administrator. If the provided one was <code>null</code>, empty or the feature is disabled the original object will be returned
     * @throws OXException
     */
    SearchIterator<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable SearchIterator<DocumentMetadata> searchIterator) throws OXException;

    /**
     * Adds the administrator permission to the given list of {@link ObjectPermission} if previously available. If the original object is <code>null</code>, empty or the feature is disabled the original object will be returned.
     *
     * @param contextId The context identifier
     * @param originalPermissions The previously persisted {@link ObjectPermission}s to identify if the administrator had the permission before
     * @param updatedPermissions The permissions to persist
     * @return A new List of {@link ObjectPermission}s where the administrator is contained (if previously set)
     * @throws OXException
     */
    List<ObjectPermission> addAdminToObjectPermissions(int contextId, @Nullable List<ObjectPermission> originalPermissions, @Nullable List<ObjectPermission> updatedPermissions) throws OXException;

    /**
     * Returns if the administrator should be available in responses.
     *
     * @param contextId The context id to check for.
     * @return boolean <code>true</code> if the administrator should be shown; otherwise <code>false</code>
     */
    boolean showAdmin(int contextId);

}
