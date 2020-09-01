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
    
    /**
     * Initializes a new {@link XctxFolderConverter}.
     * 
     * @param localSession The user's <i>local</i> session associated with the file storage account
     * @param guestSession The <i>remote</i> session of the guest user used to access the contents of the foreign context
     */
    public XctxFolderConverter(Session localSession, Session guestSession) {
        super();
        this.guestSession = guestSession;
        this.localSession = localSession;
    }

    @Override
    public Folder getFolder(FileStorageFolder storageFolder) throws OXException {
        Folder folder = super.getFolder(storageFolder);
        folder.setCreatedBy(0);
        folder.setModifiedBy(0);
        folder.setPermissions(tranferForeignPermissions(folder.getPermissions()));
        return folder;
    }

    @Override
    public UserizedFileStorageFolder getStorageFolder(UserizedFolder folder) throws OXException {
        UserizedFileStorageFolder storageFolder = super.getStorageFolder(folder);
        storageFolder.setCreatedBy(0);
        storageFolder.setModifiedBy(0);
        storageFolder.setPermissions(tranferForeignPermissions(storageFolder.getPermissions()));
        storageFolder.setOwnPermission(tranferForeignPermission(storageFolder.getOwnPermission()));
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
