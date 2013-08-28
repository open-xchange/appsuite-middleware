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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.filestorage;

import java.util.List;
import com.openexchange.file.storage.CacheAware;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.type.FileStorageType;
import com.openexchange.folderstorage.type.SystemType;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link FileStorageFolderImpl} - A file storage folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageFolderImpl extends AbstractFolder {

    private static final long serialVersionUID = 6445442372690458946L;

    /**
     * <code>"10"</code>
     */
    private static final String INFOSTORE_USER = Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);

    /**
     * <code>"15"</code>
     */
    private static final String INFOSTORE_PUBLIC = Integer.toString(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);

    private boolean cacheable;

    /**
     * Initializes an empty {@link FileStorageFolderImpl}.
     */
    public FileStorageFolderImpl() {
        super();
    }

    /**
     * The private folder identifier.
     */
    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    /**
     * Initializes a new {@link FileStorageFolderImpl} from given messaging folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     *
     * @param fsFolder The underlying file storage folder
     */
    public FileStorageFolderImpl(final FileStorageFolder fsFolder) {
        super();
        id = fsFolder.getId();
        name = fsFolder.getName();
        if (fsFolder.isRootFolder()) {
            parent = PRIVATE_FOLDER_ID;
        } else {
            String parentId = null;
            if (fsFolder instanceof TypeAware) {
                final FileStorageFolderType folderType = ((TypeAware) fsFolder).getType();
                if (FileStorageFolderType.HOME_DIRECTORY.equals(folderType)) {
                    parentId = INFOSTORE_USER;
                } else if (FileStorageFolderType.PUBLIC_FOLDER.equals(folderType)) {
                    parentId = INFOSTORE_PUBLIC;
                }
            }
            parent = null != parentId ? parentId : fsFolder.getParentId();
        }
        {
            final List<FileStoragePermission> fsPermissions = fsFolder.getPermissions();
            final int size = fsPermissions.size();
            permissions = new Permission[size];
            for (int i = 0; i < size; i++) {
                permissions[i] = new FileStoragePermissionImpl(fsPermissions.get(i));
            }
        }
        type = SystemType.getInstance();
        subscribed = fsFolder.isSubscribed();
        subscribedSubfolders = fsFolder.hasSubscribedSubfolders();
        // capabilities = parseCaps(messagingFolder.getCapabilities());
        deefault = fsFolder.isDefaultFolder();
        total = fsFolder.getFileCount();
        defaultType = deefault ? FileStorageContentType.getInstance().getModule() : 0;
        if (fsFolder instanceof CacheAware) {
            cacheable = !fsFolder.isDefaultFolder() && ((CacheAware) fsFolder).cacheable();
        } else {
            cacheable = !fsFolder.isDefaultFolder();
        }
        meta = fsFolder.getMeta();
        supportedCapabilities = fsFolder.getCapabilities();
    }

    @Override
    public Object clone() {
        final FileStorageFolderImpl clone = (FileStorageFolderImpl) super.clone();
        clone.cacheable = cacheable;
        return clone;
    }

    @Override
    public boolean isCacheable() {
        return cacheable;
    }

    @Override
    public ContentType getContentType() {
        return FileStorageContentType.getInstance();
    }

    @Override
    public Type getType() {
        return FileStorageType.getInstance();
    }

    @Override
    public void setContentType(final ContentType contentType) {
        // Nothing to do
    }

    @Override
    public void setType(final Type type) {
        // Nothing to do
    }

    @Override
    public boolean isGlobalID() {
        return false;
    }

}
