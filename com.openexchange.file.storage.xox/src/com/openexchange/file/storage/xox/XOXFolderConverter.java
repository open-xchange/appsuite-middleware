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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.xox;

import static com.openexchange.folderstorage.filestorage.FileStorageUtils.getFileStorageFolderType;
import static com.openexchange.folderstorage.filestorage.FileStorageUtils.getFileStoragePermission;
import static com.openexchange.folderstorage.filestorage.FileStorageUtils.getFileStoragePermissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.api.client.common.calls.folders.RemoteFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.share.core.subscription.EntityMangler;

/**
 * {@link XOXFolderConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XOXFolderConverter {

    private final EntityMangler entityMangler;
    private final int userId;

    public XOXFolderConverter(XOXAccountAccess accountAccess) {
        super();
        this.userId = accountAccess.getSession().getUserId();
        this.entityMangler = new EntityMangler(accountAccess.getService().getId(), accountAccess.getAccountId());
    }

    public XOXFolderConverter(int userId, FileStorageAccount account) {
        super();
        this.userId = userId;
        this.entityMangler = new EntityMangler(account.getFileStorageService().getId(), account.getId());
    }

    public List<XOXFolder> getStorageFolders(List<RemoteFolder> remoteFolders) {
        if (null == remoteFolders) {
            return null;
        }
        List<XOXFolder> storageFolders = new ArrayList<XOXFolder>(remoteFolders.size());
        for (RemoteFolder remoteFolder : remoteFolders) {
            storageFolders.add(getStorageFolder(remoteFolder));
        }
        return storageFolders;
    }

    public XOXFolder getStorageFolder(RemoteFolder remoteFolder) {

        XOXFolder folder = new XOXFolder(userId); //TODO empty default c'tor?

        folder.setId(remoteFolder.getID());
        folder.setParentId(remoteFolder.getParentID());
        folder.setName(remoteFolder.getName());
        folder.setCreationDate(remoteFolder.getCreationDate());
        folder.setCreatedFrom(entityMangler.mangleRemoteEntity(remoteFolder.getCreatedFrom()));
        folder.setCreatedBy(0);
        folder.setLastModifiedDate(remoteFolder.getLastModified());
        folder.setModifiedFrom(entityMangler.mangleRemoteEntity(remoteFolder.getModifiedFrom()));
        folder.setModifiedBy(0);
        folder.setType(getFileStorageFolderType(remoteFolder.getType()));
        folder.setMeta(remoteFolder.getMeta());
        folder.setDefaultFolder(remoteFolder.isDefault());
        folder.setFileCount(remoteFolder.getTotal());
        folder.setSubscribed(remoteFolder.isSubscribed());
        folder.setSubfolders(remoteFolder.hasSubfolders());
        folder.setSubscribedSubfolders(remoteFolder.hasSubscribedSubfolders());

        //TODO
        folder.setSubscribed(true);
        folder.setSubscribedSubfolders(remoteFolder.hasSubfolders());

        folder.setOwnPermission(getFileStoragePermission(new BasicPermission(userId, false, remoteFolder.getOwnRights())));
        folder.setPermissions(entityMangler.mangleRemotePermissions(getFileStoragePermissions(remoteFolder.getPermissions())));
        DefaultFileStoragePermission systemPermission = DefaultFileStoragePermission.newInstance(folder.getOwnPermission());
        systemPermission.setSystem(1);
        folder.addPermission(systemPermission);
        folder.setCapabilities(getStorageCapabilities(remoteFolder.getSupportedCapabilities()));
        return folder;
    }

    private static Set<String> getStorageCapabilities(Set<String> capabilities) {
        if (null == capabilities) {
            return null;
        }
        HashSet<String> storageCapabilities = new HashSet<String>(capabilities);
        storageCapabilities.remove(FileStorageFolder.CAPABILITY_SUBSCRIPTION);
        storageCapabilities.remove(FileStorageFolder.CAPABILITY_PERMISSIONS);
        return storageCapabilities;
    }

}
