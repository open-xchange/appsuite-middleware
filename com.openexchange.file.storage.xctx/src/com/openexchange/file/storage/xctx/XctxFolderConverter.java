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

package com.openexchange.file.storage.xctx;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.infostore.folder.FolderConverter;
import com.openexchange.file.storage.infostore.folder.UserizedFileStorageFolder;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.session.Session;

/**
 * {@link XctxFolderConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFolderConverter extends FolderConverter {
    
    private final Session guestSession;
    private final Session localSession;
    private final EntityHelper entityHelper;

    /**
     * Initializes a new {@link XctxFolderConverter}.
     * 
     * @param accountAccess The parent account access
     * @param localSession The user's <i>local</i> session associated with the file storage account
     * @param guestSession The <i>remote</i> session of the guest user used to access the contents of the foreign context
     */
    public XctxFolderConverter(XctxAccountAccess accountAccess, Session localSession, Session guestSession) {
        super();
        this.guestSession = guestSession;
        this.localSession = localSession;
        this.entityHelper = new EntityHelper(accountAccess);
    }

    @Override
    public UserizedFileStorageFolder getStorageFolder(UserizedFolder folder) throws OXException {
        /*
         * get storage folder with entities under perspective of remote guest session in foreign context
         */
        UserizedFileStorageFolder storageFolder = super.getStorageFolder(folder);
        storageFolder.setCacheable(Boolean.FALSE);
        /*
         * qualify remote entities for usage in local session in storage account's context & erase ambiguous numerical identifiers
         */
        storageFolder.setCreatedFrom(entityHelper.mangleRemoteEntity(folder.getCreatedFrom()));
        storageFolder.setCreatedBy(0);
        storageFolder.setModifiedFrom(entityHelper.mangleRemoteEntity(folder.getModifiedFrom()));
        storageFolder.setModifiedBy(0);
        /*
         * exchange remote guest user id with local session user's id in own permissions
         */
        FileStoragePermission ownStoragePermission = DefaultFileStoragePermission.newInstance(storageFolder.getOwnPermission());
        ownStoragePermission.setEntity(localSession.getUserId());
        ownStoragePermission = entityHelper.addPermissionEntityInfo(localSession, ownStoragePermission);
        storageFolder.setOwnPermission(ownStoragePermission);
        /*
         * enhance & qualify remote entities in folder permissions for usage in local session in storage account's context
         */
        List<FileStoragePermission> permissions = entityHelper.addPermissionEntityInfos(guestSession, storageFolder.getPermissions());
        storageFolder.setPermissions(entityHelper.mangleRemotePermissions(permissions));
        /*
         * insert user's own permission as system permission to ensure folder is considered as visible for the local session user throughout the stack
         */
        DefaultFileStoragePermission systemPermission = DefaultFileStoragePermission.newInstance(ownStoragePermission);
        systemPermission.setSystem(1);
        storageFolder.addPermission(systemPermission);
        return storageFolder;
    }

    @Override
    public Folder getFolder(FileStorageFolder storageFolder) throws OXException {
        /*
         * get folder with entities under perspective of local session in storage account's context
         */
        Folder folder = super.getFolder(storageFolder);
        /*
         * restore previously mangled entities for context of guest session
         */
        EntityInfo remoteCreatedFrom = entityHelper.unmangleLocalEntity(folder.getCreatedFrom());
        folder.setCreatedFrom(remoteCreatedFrom);
        folder.setCreatedBy(null != remoteCreatedFrom ? remoteCreatedFrom.getEntity() : 0);
        EntityInfo remoteModifiedFrom = entityHelper.unmangleLocalEntity(folder.getModifiedFrom());
        folder.setModifiedFrom(remoteModifiedFrom);
        folder.setModifiedBy(null != remoteModifiedFrom ? remoteModifiedFrom.getEntity() : 0);
        /*
         * restore previously adjusted entities in permissions for context of guest session
         */
        folder.setPermissions(entityHelper.unmangleLocalPermissions(folder.getPermissions()));
        return folder;
    }

}
