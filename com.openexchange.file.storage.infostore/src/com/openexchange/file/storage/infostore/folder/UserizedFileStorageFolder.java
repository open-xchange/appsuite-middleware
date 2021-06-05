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

package com.openexchange.file.storage.infostore.folder;

import static com.openexchange.folderstorage.filestorage.FileStorageUtils.getFileStorageFolderType;
import static com.openexchange.folderstorage.filestorage.FileStorageUtils.getFileStoragePermission;
import static com.openexchange.folderstorage.filestorage.FileStorageUtils.getFileStoragePermissions;
import java.util.Locale;
import com.openexchange.file.storage.CacheAware;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FolderPath;
import com.openexchange.file.storage.OriginAwareFileStorageFolder;
import com.openexchange.file.storage.TypeAware;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.folderstorage.SetterAwareFolder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.i18n.LocaleTools;

/**
 * {@link UserizedFileStorageFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UserizedFileStorageFolder extends DefaultFileStorageFolder implements CacheAware, TypeAware, OriginAwareFileStorageFolder {

    private final UserizedFolder folder;

    private FileStorageFolderType type;
    private Boolean cacheable;

    /**
     * Initializes a new {@link UserizedFileStorageFolder} from the supplied userized folder
     *
     * @param folder The userized folder to construct the file storage folder from
     */
    public UserizedFileStorageFolder(UserizedFolder folder) {
        super();
        this.folder = folder;
        setCreationDate(folder.getCreationDateUTC());
        setDefaultFolder(folder.isDefault());
        setExists(true);
        setId(folder.getID());
        setLastModifiedDate(folder.getLastModifiedUTC());
        String defaultName = folder.getLocalizedName(LocaleTools.DEFAULT_LOCALE, folder.isAltNames());
        // String defaultName = folder.getLocalizedName(folder.getLocale(), folder.isAltNames());
        setName(null != defaultName ? defaultName : folder.getName());
        setParentId(folder.getParentID());
        setPermissions(getFileStoragePermissions(folder.getPermissions()));
        setOwnPermission(getFileStoragePermission(folder.getOwnPermission()));
        setRootFolder(folder.getParentID() == null);
        if (false == SetterAwareFolder.class.isInstance(folder) || ((SetterAwareFolder) folder).containsSubscribed()) {
            setSubscribed(folder.isSubscribed());
        }
        setSubscribedSubfolders(folder.hasSubscribedSubfolders());
        String[] subfolderIDs = folder.getSubfolderIDs();
        setSubfolders(subfolderIDs != null && subfolderIDs.length > 0);
        FolderID folderID = new FolderID(folder.getID());
        setType(getFileStorageFolderType(folder.getType()));
        setCreatedBy(folder.getCreatedBy());
        setModifiedBy(folder.getModifiedBy());
        /*
         * only assume all infostore capabilities if it's really an infostore folder
         */
        if (FileID.INFOSTORE_SERVICE_ID.equals(folderID.getService()) && FileID.INFOSTORE_ACCOUNT_ID.equals(folderID.getAccountId())) {
            setCapabilities(FileStorageFolder.ALL_CAPABILITIES);
        }
    }

    @Override
    public boolean cacheable() {
        return null != cacheable ? cacheable.booleanValue() : folder.isCacheable();
    }

    /**
     * Sets if the folder is cacheable or not. If not specified, the delegate's {@link UserizedFolder#isCacheable()} is consulted.
     *
     * @param cacheable {@link Boolean#TRUE} if the folder should indicate to be cacheable, {@link Boolean#FALSE} if not, or <code>null</code> to decide based on the delegate
     */
    public void setCacheable(Boolean cacheable) {
        this.cacheable = cacheable;
    }

    @Override
    public FileStorageFolderType getType() {
        return type;
    }

    /**
     * Sets the folder type.
     *
     * @param type The type to set
     */
    public void setType(FileStorageFolderType type) {
        this.type = type;
    }

    @Override
    public String getLocalizedName(Locale locale) {
        return folder.getLocalizedName(locale, folder.isAltNames());
    }

    @Override
    public String toString() {
        return "UserizedFileStorageFolder [id=" + id + ", name=" + name + "]";
    }

    @Override
    public FolderPath getOrigin() {
        return folder.getOriginPath() == null ? null : FolderPath.parseFrom(folder.getOriginPath().toString());
    }

    @Override
    public Object getDelegate() {
        return folder;
    }

}
