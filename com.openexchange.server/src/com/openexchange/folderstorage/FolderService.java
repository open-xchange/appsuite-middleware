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

package com.openexchange.folderstorage;

import java.util.Date;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link FolderService} - The folder service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface FolderService {

    /**
     * Gets a map of available content types registered by folder storages.
     *
     * @return A map of available content types registered by folder storages
     */
    Map<Integer, ContentType> getAvailableContentTypes();

    /**
     * Parses a specific content type, either based on the content type's textual representation, or using the content type's module
     * identifier.
     * <p/>
     * If there are multiple suitable candidates among the registered content types, the one with the highest priority is returned.
     *
     * @param value The content type to parse
     * @return The content type, or <code>null</code> if not found
     */
    ContentType parseContentType(String value);

    /**
     * Reinitializes the denoted tree (if necessary).
     *
     * @param treeId The tree identifier
     * @param session The session
     * @throws OXException If re-initialization fails
     */
    void reinitialize(String treeId, Session session) throws OXException;

    /**
     * Checks the consistency of given tree.
     *
     * @param treeId The tree identifier
     * @param user The user
     * @param context The context
     * @throws OXException If check fails
     */
    FolderResponse<Void> checkConsistency(String treeId, User user, Context context) throws OXException;

    /**
     * Checks the consistency of given tree.
     *
     * @param treeId The tree identifier
     * @param session The session
     * @throws OXException If check fails
     */
    FolderResponse<Void> checkConsistency(String treeId, Session session) throws OXException;

    /**
     * Gets the folder identified by given folder identifier and tree identifier.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @return The folder
     * @throws OXException If folder cannot be returned
     */
    UserizedFolder getFolder(String treeId, String folderId, User user, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets the folder identified by given folder identifier and tree identifier.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param session The session
     * @param decorator The optional folder service decorator
     * @return The folder
     * @throws OXException If folder cannot be returned
     */
    UserizedFolder getFolder(String treeId, String folderId, Session session, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets all visible folders located in given tree for given user.
     *
     * @param treeId The tree identifier
     * @param filter The folder filter
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @return All visible folders
     * @throws OXException If folders cannot be returned
     */
    FolderResponse<UserizedFolder[]> getAllVisibleFolders(String treeId, FolderFilter filter, User user, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets all visible folders located in given tree for given session's user.
     *
     * @param treeId The tree identifier
     * @param filter The folder filter
     * @param session The session
     * @param decorator The optional folder service decorator
     * @return All visible folders
     * @throws OXException If folders cannot be returned
     */
    FolderResponse<UserizedFolder[]> getAllVisibleFolders(String treeId, FolderFilter filter, Session session, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets this storage's default folder (private type) for specified user.
     *
     * @param user The user whose default folder shall be returned
     * @param treeId The tree identifier
     * @param contentType The content type or the default folder
     * @param ruser The requesting user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @return The default folder for specified user
     * @throws OXException If the default folder cannot be returned
     */
    UserizedFolder getDefaultFolder(User user, String treeId, ContentType contentType, User ruser, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets this storage's default folder for specified user and type.
     *
     * @param user The user whose default folder shall be returned
     * @param treeId The tree identifier
     * @param contentType The content type or the default folder
     * @param type The type of the folder
     * @param ruser The requesting user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @return The default folder for specified user
     * @throws OXException If the default folder cannot be returned
     */
    UserizedFolder getDefaultFolder(User user, String treeId, ContentType contentType, Type type, User ruser, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets this storage's default folder (private type) for specified user.
     *
     * @param user The user whose default folder shall be returned
     * @param treeId The tree identifier
     * @param contentType The content type or the default folder
     * @param session The session
     * @param decorator The optional folder service decorator
     * @return The default folder for specified user
     * @throws OXException If the default folder cannot be returned
     */
    UserizedFolder getDefaultFolder(User user, String treeId, ContentType contentType, Session session, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets this storage's default folder for specified user.
     *
     * @param user The user whose default folder shall be returned
     * @param treeId The tree identifier
     * @param contentType The content type or the default folder
     * @param type The type of the folder
     * @param session The session
     * @param decorator The optional folder service decorator
     * @return The default folder for specified user
     * @throws OXException If the default folder cannot be returned
     */
    UserizedFolder getDefaultFolder(User user, String treeId, ContentType contentType, Type type, Session session, FolderServiceDecorator decorator) throws OXException;

    /**
     * Subscribes an existing folder from specified source tree to specified (virtual) target tree below given parent.
     * <p>
     * Does only work on virtual trees.
     *
     * @param sourceTreeId The source tree identifier
     * @param folderId The (source) folder identifier
     * @param targetTreeId The target tree identifier
     * @param optTargetParentId The optional target parent identifier
     * @param user The user
     * @param context The context
     * @param session The session
     * @throws OXException If folder cannot be added
     */
    FolderResponse<Void> subscribeFolder(String sourceTreeId, String folderId, String targetTreeId, String optTargetParentId, User user, Context context) throws OXException;

    /**
     * Subscribes an existing folder from specified source tree to specified (virtual) target tree below given parent.
     * <p>
     * Does only work on virtual trees.
     *
     * @param sourceTreeId The source tree identifier
     * @param folderId The (source) folder identifier
     * @param targetTreeId The target tree identifier
     * @param optTargetParentId The optional target parent identifier
     * @param session The session
     * @throws OXException If folder cannot be added
     */
    FolderResponse<Void> subscribeFolder(String sourceTreeId, String folderId, String targetTreeId, String optTargetParentId, Session session) throws OXException;

    /**
     * Unsubscribes the specified folder in given (virtual) tree only.
     * <p>
     * Does only work on virtual trees.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param user The user
     * @param context The context
     * @throws OXException If folder cannot be deleted
     */
    FolderResponse<Void> unsubscribeFolder(String treeId, String folderId, User user, Context context) throws OXException;

    /**
     * Unsubscribes the specified folder in given (virtual) tree only.
     * <p>
     * Does only work on virtual trees.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param session The session
     * @throws OXException If folder cannot be deleted
     */
    FolderResponse<Void> unsubscribeFolder(String treeId, String folderId, Session session) throws OXException;

    /**
     * Gets all visible folders of specified content type and folder type.
     *
     * @param treeId The tree identifier
     * @param contentType The content type
     * @param type The folder type
     * @param all <code>true</code> to deliver all subfolders regardless of their subscribed status; <code>false</code> to deliver
     *            subscribed folders only.
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @return All visible folders of specified content type and folder type.
     * @throws OXException If operation fails
     */
    FolderResponse<UserizedFolder[]> getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final boolean all, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets all visible folders of specified content type and folder type.
     *
     * @param treeId The tree identifier
     * @param contentType The content type
     * @param type The folder type
     * @param all <code>true</code> to deliver all subfolders regardless of their subscribed status; <code>false</code> to deliver
     *            subscribed folders only.
     * @param session The session
     * @param decorator The optional folder service decorator
     * @return All visible folders of specified content type and folder type.
     * @throws OXException If operation fails
     */
    FolderResponse<UserizedFolder[]> getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final boolean all, final Session session, final FolderServiceDecorator decorator) throws OXException;

    FolderResponse<UserizedFolder[]> getUserSharedFolders(final String treeId, final ContentType contentType, final Session session, final FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets the subfolders of specified parent in given tree.
     *
     * @param treeId The tree identifier
     * @param parentId The parent identifier
     * @param all <code>true</code> to deliver all subfolders regardless of their subscribed status; <code>false</code> to deliver
     *            subscribed folders only.
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @return The subfolders
     * @throws OXException If subfolders cannot be returned
     */
    FolderResponse<UserizedFolder[]> getSubfolders(String treeId, String parentId, boolean all, User user, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets the subfolders of specified parent in given tree.
     *
     * @param treeId The tree identifier
     * @param parentId The parent identifier
     * @param all <code>true</code> to deliver all subfolders regardless of their subscribed status; <code>false</code> to deliver
     *            subscribed folders only.
     * @param session The session
     * @param decorator The optional folder service decorator
     * @return The subfolders
     * @throws OXException If subfolders cannot be returned
     */
    FolderResponse<UserizedFolder[]> getSubfolders(String treeId, String parentId, boolean all, Session session, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets the path from given folder to specified tree's root folder.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @return The path from given folder to specified tree's root folder
     * @throws OXException If path cannot be returned
     */
    FolderResponse<UserizedFolder[]> getPath(String treeId, String folderId, User user, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets the path from given folder to specified tree's root folder.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param session The session
     * @param decorator The optional folder service decorator
     * @return The path from given folder to specified tree's root folder
     * @throws OXException If path cannot be returned
     */
    FolderResponse<UserizedFolder[]> getPath(String treeId, String folderId, Session session, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets all new, modified and deleted folders since given time stamp.
     *
     * @param treeId The tree identifier
     * @param timeStamp The time stamp from which to consider changes
     * @param ignoreDeleted <code>true</code> to ignore delete operations; otherwise <code>false</code>
     * @param includeContentTypes The content types to include
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @return All new, modified and deleted folders since given time stamp
     * @throws OXException If path cannot be returned
     */
    FolderResponse<UserizedFolder[][]> getUpdates(String treeId, Date timeStamp, boolean ignoreDeleted, ContentType[] includeContentTypes, User user, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Gets all new, modified and deleted folders since given time stamp.
     *
     * @param treeId The tree identifier
     * @param timeStamp The time stamp from which to consider changes
     * @param ignoreDeleted <code>true</code> to ignore delete operations; otherwise <code>false</code>
     * @param includeContentTypes The content types to include
     * @param session The session
     * @param decorator The optional folder service decorator
     * @return All new, modified and deleted folders since given time stamp
     * @throws OXException If path cannot be returned
     */
    FolderResponse<UserizedFolder[][]> getUpdates(String treeId, Date timeStamp, boolean ignoreDeleted, ContentType[] includeContentTypes, Session session, FolderServiceDecorator decorator) throws OXException;

    /**
     * Deletes the specified folder in given tree.
     * <p>
     * The folder is deleted from all trees and its subfolders as well
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param timeStamp The requestor's last-modified time stamp
     * @param user The user
     * @param context The context
     * @param decorator The folder service decorator or <code>null</code>
     * @throws OXException If folder cannot be deleted
     */
    FolderResponse<Void> deleteFolder(String treeId, String folderId, Date timeStamp, User user, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Deletes the specified folder in given tree.
     * <p>
     * The folder is deleted from all trees and its subfolders as well
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param timeStamp The requestor's last-modified time stamp
     * @param session The session
     * @param decorator The folder service decorator or <code>null</code>
     * @throws OXException If folder cannot be deleted
     */
    FolderResponse<Void> deleteFolder(String treeId, String folderId, Date timeStamp, Session session, FolderServiceDecorator decorator) throws OXException;

    /**
     * Clears the content of specified folder in given tree.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param user The user
     * @param context The context
     * @throws OXException If folder cannot be cleared
     */
    FolderResponse<Void> clearFolder(String treeId, String folderId, User user, Context context) throws OXException;

    /**
     * Clears the content of specified folder in given tree.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param session The session
     * @throws OXException If folder cannot be cleared
     */
    FolderResponse<Void> clearFolder(String treeId, String folderId, Session session) throws OXException;

    /**
     * Updates a folder identified through given folder object.
     *
     * @param folder The folder object containing tree identifier, folder identifier and modified data.
     * @param timeStamp The requestor's last-modified time stamp
     * @param user The user
     * @param context The context
     * @param decorator The folder service decorator or <code>null</code>
     * @throws OXException If update fails
     */
    FolderResponse<Void> updateFolder(Folder folder, Date timeStamp, User user, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Updates a folder identified through given folder object.
     *
     * @param folder The folder object containing tree identifier, folder identifier and modified data.
     * @param timeStamp The requestor's last-modified time stamp
     * @param session The session
     * @param decorator The folder service decorator or <code>null</code>
     * @throws OXException If update fails
     */
    FolderResponse<Void> updateFolder(Folder folder, Date timeStamp, Session session, FolderServiceDecorator decorator) throws OXException;

    /**
     * Creates a new folder described by given folder object.
     *
     * @param folder The folder object containing tree identifier, parent identifier and data.
     * @param user The user
     * @param context The context
     * @param decorator The folder service decorator or <code>null</code>
     * @return The identifier of the newly created folder
     * @throws OXException If creation fails
     */
    FolderResponse<String> createFolder(Folder folder, User user, Context context, FolderServiceDecorator decorator) throws OXException;

    /**
     * Creates a new folder described by given folder object.
     *
     * @param folder The folder object containing tree identifier, parent identifier and data.
     * @param session The session
     * @param decorator The folder service decorator or <code>null</code>
     * @return The identifier of the newly created folder
     * @throws OXException If creation fails
     */
    FolderResponse<String> createFolder(Folder folder, Session session, FolderServiceDecorator decorator) throws OXException;

    // TODO: default folder? all visible folders

}
