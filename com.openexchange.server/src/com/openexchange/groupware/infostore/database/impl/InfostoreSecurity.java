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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostoreFolderPermission;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.session.ServerSession;

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
