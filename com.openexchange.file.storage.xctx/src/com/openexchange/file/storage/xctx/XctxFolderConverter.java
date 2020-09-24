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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.infostore.folder.FolderConverter;
import com.openexchange.file.storage.infostore.folder.UserizedFileStorageFolder;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.Permission;
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
        this.entityHelper = new EntityHelper(accountAccess, localSession, guestSession);
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
        //TODO
        folder.setPermissions(tranferForeignPermissions(folder.getPermissions()));
        return folder;
    }
    
    @Override
    public UserizedFileStorageFolder getStorageFolder(UserizedFolder folder) throws OXException {
        /*
         * get storage folder with entities under perspective of remote guest session in foreign context
         */
        UserizedFileStorageFolder storageFolder = super.getStorageFolder(folder);
        /*
         * qualify remote entities for usage in local session in storage account's context  
         */
        storageFolder.setCreatedFrom(entityHelper.mangleRemoteUserEntity(folder.getCreatedFrom(), folder.getCreatedBy()));
        storageFolder.setCreatedBy(0);
        storageFolder.setModifiedFrom(entityHelper.mangleRemoteUserEntity(folder.getModifiedFrom(), folder.getModifiedBy()));
        storageFolder.setModifiedBy(0);
        /*
         * adjust remote guest user id to local session user's id in own permissions
         */
        storageFolder.setOwnPermission(tranferForeignPermission(storageFolder.getOwnPermission()));
        /*
         * adjust remote entities in folder permission
         */
        //TODO entityinfo in FileStoragePermission? or extended interface? 
        storageFolder.setPermissions(tranferForeignPermissions(storageFolder.getPermissions()));
        return storageFolder;
    }

    private List<FileStoragePermission> tranferForeignPermissions(List<FileStoragePermission> foreignPermissions) {
        if (null == foreignPermissions) {
            return null;
        }
        final boolean SKIP_UNKNOWN = true; //TODO: client does not like unknown entities for now

        List<FileStoragePermission> storagePermissions = new ArrayList<FileStoragePermission>(foreignPermissions.size());
        for (FileStoragePermission foreignPermission : foreignPermissions) {
            FileStoragePermission storagePermission = tranferForeignPermission(foreignPermission);
            if (SKIP_UNKNOWN && 0 >= storagePermission.getEntity()) {
                continue;
            }
            storagePermissions.add(storagePermission);
        }
        return storagePermissions;
    }

    private FileStoragePermission tranferForeignPermission(FileStoragePermission foreignPermission) {
        if (null == foreignPermission) {
            return null;
        }
        DefaultFileStoragePermission storagePermission = DefaultFileStoragePermission.newInstance(foreignPermission);
        if (storagePermission.getEntity() == guestSession.getUserId()) {
            storagePermission.setEntity(localSession.getUserId());
        } else {
            storagePermission.setEntity(0);
        }
        return storagePermission;
    }

    private Permission[] tranferForeignPermissions(Permission[] foreignPermissions) {
        if (null == foreignPermissions) {
            return null;
        }
        Permission[] permissions = new Permission[foreignPermissions.length];
        for (int i = 0; i < foreignPermissions.length; i++) {
            permissions[i] = tranferForeignPermission(foreignPermissions[i]);
        }
        return permissions;
    }

    private Permission tranferForeignPermission(Permission foreignPermission) {
        if (null == foreignPermission) {
            return null;
        }
        BasicPermission permission = new BasicPermission(foreignPermission);
        if (permission.getEntity() == localSession.getUserId()) {
            permission.setEntity(guestSession.getUserId());
        }
        return permission;
    }

}
