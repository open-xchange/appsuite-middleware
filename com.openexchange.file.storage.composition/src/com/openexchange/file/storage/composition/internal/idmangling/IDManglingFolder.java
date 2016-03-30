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

package com.openexchange.file.storage.composition.internal.idmangling;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;
import com.openexchange.file.storage.composition.FolderID;

/**
 * {@link IDManglingFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class IDManglingFolder implements TypeAware {

    /**
     * Create a new {@link FileStorageFolder} instance delegating all regular calls to the supplied folder, but returning the unique ID
     * representations of the folder's own object and the parent folder ID properties based on the underlying service- and account IDs.
     *
     * @param delegate The folder delegate
     * @param serviceID The service ID
     * @param accountID The account ID
     * @return A folder with unique IDs
     */
    public static FileStorageFolder withUniqueID(FileStorageFolder delegate, String serviceID, String accountID) {
        final String id = null != delegate.getId() ? new FolderID(serviceID, accountID, delegate.getId()).toUniqueID() : null;
        final String parentId = null != delegate.getParentId() ? new FolderID(serviceID, accountID, delegate.getParentId()).toUniqueID() : null;
        return new IDManglingFolder(delegate, id, parentId);
    }

    /**
     * Creates {@link FileStorageFolder} instances delegating all regular calls to the supplied folders, but returning the unique ID
     * representations of the folder's own object and the parent folder ID properties based on the underlying service- and account IDs.
     *
     * @param delegates The folder delegates
     * @param serviceID The service ID
     * @param accountID The account ID
     * @return An array of folders with unique IDs
     */
    public static FileStorageFolder[] withUniqueID(FileStorageFolder[] delegates, String serviceID, String accountID) {
        if (null == delegates) {
            return null;
        }
        FileStorageFolder[] idManglingFolders = new IDManglingFolder[delegates.length];
        for (int i = 0; i < idManglingFolders.length; i++) {
            idManglingFolders[i] = withUniqueID(delegates[i], serviceID, accountID);
        }
        return idManglingFolders;
    }

    /**
     * Create a new {@link FileStorageFolder} instance delegating all regular calls to the supplied folder, but returning the relative ID
     * representations of the folder's own object and parent folder ID properties, effectively hiding the service- and account IDs.
     *
     * @param delegate The folder delegate
     * @return A folder with relative IDs
     */
    public static FileStorageFolder withRelativeID(FileStorageFolder delegate) {
        final String id = null != delegate.getId() ? new FolderID(delegate.getId()).getFolderId() : null;
        final String parentId = null != delegate.getParentId() ? new FolderID(delegate.getParentId()).getFolderId() : null;
        return new IDManglingFolder(delegate, id, parentId);
    }

    private final FileStorageFolder delegate;
    private final String id;
    private final String parentId;

    /**
     * Initializes a new {@link IDManglingFolder}.
     *
     * @param delegate The delegate
     */
    IDManglingFolder(FileStorageFolder delegate, String id, String parentId) {
        super();
        this.delegate = delegate;
        this.id = id;
        this.parentId = parentId;
    }

    @Override
    public FileStorageFolderType getType() {
        if (delegate instanceof TypeAware) {
            return ((TypeAware) delegate).getType();
        }
        return FileStorageFolderType.NONE;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<String> getCapabilities() {
        return delegate.getCapabilities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getLocalizedName(Locale locale) {
        return delegate.getLocalizedName(locale);
    }

    @Override
    public FileStoragePermission getOwnPermission() {
        return delegate.getOwnPermission();
    }

    @Override
    public List<FileStoragePermission> getPermissions() {
        return delegate.getPermissions();
    }

    @Override
    public boolean hasSubfolders() {
        return delegate.hasSubfolders();
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        return delegate.hasSubscribedSubfolders();
    }

    @Override
    public boolean isSubscribed() {
        return delegate.isSubscribed();
    }

    @Override
    public Date getCreationDate() {
        return delegate.getCreationDate();
    }

    @Override
    public Date getLastModifiedDate() {
        return delegate.getLastModifiedDate();
    }

    @Override
    public boolean isHoldsFolders() {
        return delegate.isHoldsFolders();
    }

    @Override
    public boolean isHoldsFiles() {
        return delegate.isHoldsFiles();
    }

    @Override
    public boolean isRootFolder() {
        return delegate.isRootFolder();
    }

    @Override
    public boolean isDefaultFolder() {
        return delegate.isDefaultFolder();
    }

    @Override
    public int getFileCount() {
        return delegate.getFileCount();
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Map<String, Object> getMeta() {
        return delegate.getMeta();
    }

    @Override
    public int getCreatedBy() {
        return delegate.getCreatedBy();
    }

    @Override
    public int getModifiedBy() {
        return delegate.getModifiedBy();
    }

    @Override
    public String toString() {
        return "IDManglingFolder [id=" + id + ", delegateId=" + delegate.getId() + ", name=" + delegate.getName() + "]";
    }

}
