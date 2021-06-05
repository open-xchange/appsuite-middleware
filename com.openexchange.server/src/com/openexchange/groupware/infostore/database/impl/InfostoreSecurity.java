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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostoreFolderPermission;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

public interface InfostoreSecurity {

    /**
     * Gets the effective infostore permissions for a specific document.
     *
     * @param session The user's session
     * @param id The identifier of the document to get the permissions for
     * @return The effective permissions
     */
    EffectiveInfostorePermission getInfostorePermission(ServerSession session, int id) throws OXException;

    /**
     * Gets the effective infostore permissions for a specific document.
     *
     * @param context The context
     * @param user The user
     * @param userPermissions The user permission bits
     * @param id The identifier of the document to get the permissions for
     * @return The effective permissions
     */
    EffectiveInfostorePermission getInfostorePermission(Context context, User user, UserPermissionBits userPermissions, int id) throws OXException;

    /**
     * Gets the effective infostore permissions for a specific document.
     *
     * @param session The user's session
     * @param document The document to get the permissions for
     * @return The effective permissions
     */
    EffectiveInfostorePermission getInfostorePermission(ServerSession session, DocumentMetadata document) throws OXException;

    List<EffectiveInfostorePermission> getInfostorePermissions(List<DocumentMetadata> documents, Context context, User user, UserPermissionBits permissionBits) throws OXException;

    /**
     * Gets the effective infostore permissions for a specific folder.
     *
     * @param session The user's session
     * @param folderId The identifier of the folder to get the permissions for
     * @return The effective permissions
     */
    EffectiveInfostoreFolderPermission getFolderPermission(ServerSession session, long folderId) throws OXException;

    EffectiveInfostoreFolderPermission getFolderPermission(long folderId, Context ctx, User user, UserPermissionBits userPermissions) throws OXException;

    EffectiveInfostoreFolderPermission getFolderPermission(long folderId, Context ctx, User user, UserPermissionBits userPermissions, Connection readConArg) throws OXException;

    <L> L injectInfostorePermissions(int[] ids, Context ctx, User user, UserPermissionBits userPermissions, L list, Injector<L, EffectiveInfostorePermission> injector) throws OXException;

    /**
     * Determines the identifier of the folder owner
     *
     * @param folderId The folder identifier
     * @param ctx The context
     * @return The folder owner identifier or <code>-1</code>
     * @throws OXException If operation fails
     */
    int getFolderOwner(long folderId, Context ctx) throws OXException;

    /**
     * Gets the folder owners for specified documents
     *
     * @param documents The documents
     * @param ctx The associated context
     * @return The folder owners
     * @throws OXException If operation fails
     */
    int[] getFolderOwners(Collection<DocumentMetadata> documents, Context ctx) throws OXException;

    /**
     * Gets the folder owner for specified document
     *
     * @param document The document
     * @param ctx The associated context
     * @return The folder owner
     * @throws OXException If operation fails
     */
    int getFolderOwner(DocumentMetadata document, Context ctx) throws OXException;

}
