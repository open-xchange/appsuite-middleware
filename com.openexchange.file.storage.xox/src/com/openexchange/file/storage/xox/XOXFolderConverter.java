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
import static com.openexchange.folderstorage.filestorage.FileStorageUtils.getPermissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.api.client.common.calls.folders.ExtendedPermission;
import com.openexchange.api.client.common.calls.folders.ExtendedPermission.Contact;
import com.openexchange.api.client.common.calls.folders.RemoteFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.EntityInfo.Type;
import com.openexchange.groupware.LinkEntityInfo;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Enums;
import com.openexchange.session.Session;
import com.openexchange.share.core.subscription.EntityMangler;

/**
 * {@link XOXFolderConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XOXFolderConverter {

    private final Session localSession;
    private final EntityMangler entityMangler;

    /**
     * Initializes a new {@link XOXFolderConverter}.
     * 
     * @param accountAccess The underlying account access
     */
    public XOXFolderConverter(XOXAccountAccess accountAccess) {
        this(accountAccess.getAccount(), accountAccess.getSession());
    }

    /**
     * Initializes a new {@link XOXFolderConverter}.
     * 
     * @param account The underlying file storage account
     * @param localSession The user's <i>local</i> session associated with the file storage account
     */
    public XOXFolderConverter(FileStorageAccount account, Session localSession) {
        super();
        this.localSession = localSession;
        this.entityMangler = new EntityMangler(account.getFileStorageService().getId(), account.getId());
    }

    /**
     * Converts a list of remote folders into their file storage folder equivalents.
     *
     * @param remoteFolders The remote folders to convert
     * @return The file storage folders
     */
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

    /**
     * Converts a remote folder into its file storage folder equivalent.
     *
     * @param remoteFolder The remote folder to convert
     * @return The file storage folder
     */
    public XOXFolder getStorageFolder(RemoteFolder remoteFolder) {
        if (null == remoteFolder) {
            return null;
        }
        XOXFolder folder = new XOXFolder();
        folder.setCacheable(false); //for now, maybe make configurable?        
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
        if (remoteFolder.containsSubscribed()) {
            folder.setSubscribed(remoteFolder.isSubscribed());
        }
        if (remoteFolder.containsHasSubfolders()) {
            folder.setSubfolders(remoteFolder.hasSubfolders());
        }
        if (remoteFolder.containsSubscribedSubfolders()) {
            folder.setSubscribedSubfolders(remoteFolder.hasSubscribedSubfolders());
        }
        folder.setOwnPermission(getFileStoragePermission(new BasicPermission(localSession.getUserId(), false, remoteFolder.getOwnRights())));
        List<FileStoragePermission> storagePermissions = addExtendedPermissions(getFileStoragePermissions(remoteFolder.getPermissions()), remoteFolder.getExtendedPermissions());
        folder.setPermissions(entityMangler.mangleRemotePermissions(storagePermissions));
        DefaultFileStoragePermission systemPermission = DefaultFileStoragePermission.newInstance(folder.getOwnPermission());
        systemPermission.setSystem(1);
        folder.addPermission(systemPermission);
        folder.setCapabilities(getStorageCapabilities(remoteFolder.getSupportedCapabilities()));
        return folder;
    }

    /**
     * Converts a file storage folder to its remote folder equivalent.
     *
     * @param storageFolder The file storage folder to convert
     * @return The remote folder
     */
    public RemoteFolder getFolder(FileStorageFolder storageFolder) {
        if (null == storageFolder) {
            return null;
        }
        RemoteFolder remoteFolder = new RemoteFolder();
        remoteFolder.setModule(Module.INFOSTORE.getName());
        remoteFolder.setID(storageFolder.getId());
        remoteFolder.setParentID(storageFolder.getParentId());
        EntityInfo remoteCreatedFrom = entityMangler.unmangleLocalEntity(storageFolder.getCreatedFrom());
        remoteFolder.setCreatedFrom(remoteCreatedFrom);
        remoteFolder.setCreatedBy(null != remoteCreatedFrom ? remoteCreatedFrom.getEntity() : -1);
        EntityInfo remoteModifiedFrom = entityMangler.unmangleLocalEntity(storageFolder.getModifiedFrom());
        remoteFolder.setModifiedFrom(remoteModifiedFrom);
        remoteFolder.setModifiedBy(null != remoteModifiedFrom ? remoteModifiedFrom.getEntity() : -1);
        remoteFolder.setName(storageFolder.getName());
        remoteFolder.setMeta(storageFolder.getMeta());
        remoteFolder.setPermissions(entityMangler.unmangleLocalPermissions(getPermissions(storageFolder.getPermissions())));
        return remoteFolder;
    }

    /**
     * Initializes a new remote folder.
     * 
     * @return The initialized remote folder
     */
    public RemoteFolder initFolder() {
        RemoteFolder remoteFolder = new RemoteFolder();
        remoteFolder.setModule(Module.INFOSTORE.getName());
        return remoteFolder;
    }

    private static List<FileStoragePermission> addExtendedPermissions(List<FileStoragePermission> permissions, ExtendedPermission[] extendedPermissions) {
        if (null == permissions) {
            return null;
        }
        List<FileStoragePermission> enhencedPermissions = new ArrayList<FileStoragePermission>(permissions.size());
        for (FileStoragePermission permission : permissions) {
            ExtendedPermission matchingPermission = findMatching(extendedPermissions, permission);
            if (null == matchingPermission) {
                enhencedPermissions.add(permission);
            } else {
                DefaultFileStoragePermission enhancedPermission = DefaultFileStoragePermission.newInstance(permission);
                enhancedPermission.setEntityInfo(getEntityInfo(matchingPermission));
                enhencedPermissions.add(enhancedPermission);
            }
        }
        return enhencedPermissions;
    }

    private static EntityInfo getEntityInfo(ExtendedPermission extendedPermission) {
        if (null == extendedPermission) {
            return null;
        }
        Type type = Enums.parse(EntityInfo.Type.class, extendedPermission.getType(), null);
        Contact contact = extendedPermission.getContact();
        EntityInfo entityInfo;
        if (null == contact) {
            entityInfo = new EntityInfo(extendedPermission.getIdentifier(), extendedPermission.getDisplayName(), null, null, null, null, 
                extendedPermission.getEntity(), null, type);
        } else {
            entityInfo = new EntityInfo(extendedPermission.getIdentifier(), extendedPermission.getDisplayName(), contact.getTitle(), 
                contact.getFirstName(), contact.getLastName(), contact.getEmail1(), extendedPermission.getEntity(), contact.getImage1Url(), type);
        }
        if (Type.ANONYMOUS.equals(type)) {
            entityInfo = new LinkEntityInfo(entityInfo, extendedPermission.getShareUrl(), extendedPermission.getPassword(), extendedPermission.getExpiryDate(), extendedPermission.isInherited());
        }
        return entityInfo;
    }

    private static ExtendedPermission findMatching(ExtendedPermission[] extendedPermissions, FileStoragePermission permission) {
        if (null == extendedPermissions) {
            return null;
        }
        for (ExtendedPermission extendedPermission : extendedPermissions) {
            if (null != permission.getIdentifier() && permission.getIdentifier().equals(extendedPermission.getIdentifier())) {
                return extendedPermission;
            }
            if ((0 < permission.getEntity() || 0 == permission.getEntity() && permission.isGroup()) && permission.getEntity() == extendedPermission.getEntity()) {
                return extendedPermission;
            }
        }
        return null;
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
