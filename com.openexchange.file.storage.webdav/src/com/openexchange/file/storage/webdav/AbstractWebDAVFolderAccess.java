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

package com.openexchange.file.storage.webdav;

import static com.openexchange.file.storage.webdav.WebDAVUtils.extractQuotaBytes;
import static com.openexchange.file.storage.webdav.WebDAVUtils.find;
import static com.openexchange.file.storage.webdav.WebDAVUtils.matches;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.webdav.client.PropertyName.DAV_QUOTA_AVAILABLE_BYTES;
import static com.openexchange.webdav.client.PropertyName.DAV_QUOTA_USED_BYTES;
import static com.openexchange.webdav.client.WebDAVClient.DEPTH_0;
import static com.openexchange.webdav.client.WebDAVClient.DEPTH_1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.http.HttpStatus;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAutoRenameFoldersAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.NameBuilder;
import com.openexchange.file.storage.PathKnowingFileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.UserCreatedFileStorageFolderAccess;
import com.openexchange.webdav.client.PropertyName;
import com.openexchange.webdav.client.WebDAVClient;
import com.openexchange.webdav.client.WebDAVClientException;
import com.openexchange.webdav.client.WebDAVResource;

/**
 * {@link AbstractWebDAVFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.10.4
 */
public abstract class AbstractWebDAVFolderAccess extends AbstractWebDAVAccess implements FileStorageFolderAccess, PathKnowingFileStorageFolderAccess, FileStorageAutoRenameFoldersAccess, UserCreatedFileStorageFolderAccess {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractWebDAVFolderAccess.class);

    /** The default property names to query when retrieving WebDAV collections */
    protected static Set<QName> DEFAULT_PROPERTY_NAMES = com.openexchange.tools.arrays.Collections.unmodifiableSet(
        PropertyName.DAV_CREATIONDATE,
        PropertyName.DAV_DISPLAYNAME,
        PropertyName.DAV_GETCONTENTLANGUAGE,
        PropertyName.DAV_GETCONTENTLENGTH,
        PropertyName.DAV_GETCONTENTTYPE,
        PropertyName.DAV_GETETAG,
        PropertyName.DAV_GETLASTMODIFIED,
        PropertyName.DAV_LOCKDISCOVERY,
        PropertyName.DAV_RESOURCETYPE,
        PropertyName.DAV_SOURCE,
        PropertyName.DAV_SUPPORTEDLOCK
    );

    /**
     * Initializes a new {@link AbstractWebDAVFolderAccess}.
     *
     * @param webdavClient The WebDAV client to use
     * @param accountAccess A WebDAV account access reference
     * @throws {@link OXException} in case the account is not properly configured
     */
    protected AbstractWebDAVFolderAccess(WebDAVClient webdavClient, AbstractWebDAVAccountAccess accountAccess) throws OXException {
        super(webdavClient, accountAccess);
    }

    @Override
    public WebDAVFolder getRootFolder() {
        WebDAVFolder rootFolder = new WebDAVFolder(session.getUserId());
        rootFolder.setRootFolder(true);
        rootFolder.setId(FileStorageFolder.ROOT_FULLNAME);
        rootFolder.setHoldsFiles(true);
        rootFolder.setHoldsFolders(true);
        rootFolder.setType(FileStorageFolderType.HOME_DIRECTORY);
        rootFolder.setParentId(null);
        rootFolder.setName(account.getDisplayName());
        rootFolder.setSubfolders(true);
        rootFolder.setSubscribedSubfolders(true);
        return rootFolder;
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        if (isRoot(folderId)) {
            return true;
        }
        WebDAVPath path = getWebDAVPath(folderId);
        try {
            return client.exists(path.toString(), null);
        } catch (WebDAVClientException e) {
            throw asOXException(e, folderId);
        }
    }

    @Override
    public WebDAVFolder getFolder(String folderId) throws OXException {
        WebDAVPath path = getWebDAVPath(folderId);
        WebDAVResource resource;
        try {
            resource = find(client.propFind(path.toString(), DEPTH_0, getPropertiesToQuery(), null), path);
        } catch (WebDAVClientException e) {
            throw asOXException(e, folderId);
        }
        if (null == resource) {
            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId, account.getId(), account.getFileStorageService().getId(), I(session.getUserId()), I(session.getContextId()));
        }
        return getWebDAVFolder(resource);
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        WebDAVPath path = getWebDAVPath(parentIdentifier);
        List<WebDAVResource> resources;
        try {
            resources = client.propFind(path.toString(), DEPTH_1, getPropertiesToQuery(), null);
        } catch (WebDAVClientException e) {
            throw asOXException(e, parentIdentifier);
        }
        List<FileStorageFolder> subfolders = new ArrayList<FileStorageFolder>();
        for (WebDAVResource resource : resources) {
            if (resource.isCollection() && false == matches(resource.getHref(), path)) {
                subfolders.add(getWebDAVFolder(resource));
            }
        }
        return subfolders.toArray(new FileStorageFolder[subfolders.size()]);
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        return createFolder(toCreate, true);
    }

    @Override
    public String createFolder(FileStorageFolder toCreate, boolean autoRename) throws OXException{
        WebDAVPath path = getWebDAVPath(toCreate.getParentId()).append(toCreate.getName(), true);
        Map<String, String> headers = Collections.singletonMap("If-None-Match", "*");
        NameBuilder nameBuilder = null;
        boolean created = false;
        while (!created) {
            try {
                client.mkCol(path.toString(), headers);
                created = true;
            }
            catch(WebDAVClientException e) {
                if (autoRename && e.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
                    //Collection does already exist. Choose another name
                    if (nameBuilder == null) {
                         nameBuilder = NameBuilder.nameBuilderFor(path.getName());
                    }
                    nameBuilder.advance();
                    path = getWebDAVPath(toCreate.getParentId()).append(nameBuilder.toString(),true);
                }
                else {
                    throw e;
                }
            }
        }
        return getFolderId(path);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // Neither support for subscription nor permissions
        return identifier;
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        return moveFolder(folderId, newParentId, newName, true);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName, boolean autoRename) throws OXException {
        if (isRoot(folderId)) {
            throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(account.getFileStorageService().getId());
        }
        WebDAVPath path = getWebDAVPath(folderId);
        String name = null != newName ? newName : path.getName();
        WebDAVPath targetPath = getWebDAVPath(newParentId).append(name, true);
        Map<String, String> headers = Collections.singletonMap("Overwrite", "F");
        NameBuilder nameBuilder = null;
        boolean moved = false;
        while (!moved) {
            try {
                client.move(path.toString(), targetPath.toString(), headers);
                moved = true;
            }
            catch(WebDAVClientException e) {
                if (e.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
                    if (autoRename) {
                        //Collection does already exist. Choose another name
                        if (nameBuilder == null) {
                             nameBuilder = NameBuilder.nameBuilderFor(name);
                        }
                        nameBuilder.advance();
                        targetPath = getWebDAVPath(newParentId).append(nameBuilder.toString(), true);
                    }
                    else {
                        throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(e, targetPath.getName(), targetPath.getParent().getName());
                    }
                }
                else {
                    throw e;
                }
            }
        }
        return getFolderId(targetPath);
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        if (isRoot(folderId)) {
            throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(account.getFileStorageService().getId());
        }
        WebDAVPath path = getWebDAVPath(folderId);
        WebDAVPath targetPath = path.getParent().append(newName, true);
        try {
            Map<String, String> headers = Collections.singletonMap("Overwrite", "F");
            client.move(path.toString(), targetPath.toString(), headers);
        } catch (WebDAVClientException e) {
            if (e.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
                throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(e, newName, folderId);
            }
            throw e;
        }
        return getFolderId(targetPath);
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        WebDAVPath path = getWebDAVPath(folderId);
        try {
            client.delete(path.toString(), null);
        } catch (WebDAVClientException e) {
            throw asOXException(e, folderId);
        }
        return folderId;
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, false);
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        getAccountAccess().getFileAccess().removeDocument(folderId, FileStorageFileAccess.DISTANT_FUTURE, hardDelete);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>();
        WebDAVFolder folder = getFolder(folderId);
        folders.add(folder);
        while (false == isRoot(folder.getId())) {
            folder = getFolder(folder.getParentId());
            folders.add(folder);
        }
        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    @Override
    public String[] getPathIds2DefaultFolder(String folderId) throws OXException {
        List<String> foldersIds = new ArrayList<String>();
        String id = folderId;
        foldersIds.add(id);
        while (false == isRoot(id)) {
            WebDAVPath parentPath = getWebDAVPath(id).getParent();
            id = getFolderId(parentPath);
            foldersIds.add(id);
        }
        return foldersIds.toArray(new String[foldersIds.size()]);
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        WebDAVPath path = getWebDAVPath(folderId);
        Set<QName> props = new HashSet<QName>();
        props.add(DAV_QUOTA_AVAILABLE_BYTES);
        props.add(DAV_QUOTA_USED_BYTES);
        WebDAVResource resource;
        try {
            resource = find(client.propFind(path.toString(), DEPTH_0, props, null), path);
        } catch (WebDAVClientException e) {
            throw asOXException(e, folderId);
        }
        if (null == resource) {
            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId, account.getId(), account.getFileStorageService().getId(), I(session.getUserId()), I(session.getContextId()));
        }
        long used = extractQuotaBytes(resource.getProperty(DAV_QUOTA_USED_BYTES));
        long available = extractQuotaBytes(resource.getProperty(DAV_QUOTA_AVAILABLE_BYTES));
        long limit = Quota.UNLIMITED != available && Quota.UNLIMITED != used ? used + available : Quota.UNLIMITED;
        return new Quota(limit, used, Type.STORAGE);
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        if (null == types) {
            return null;
        }
        Quota[] quotas = new Quota[types.length];
        for (int i = 0; i < types.length; i++) {
            switch (types[i]) {
                case FILE:
                    quotas[i] = getFileQuota(folder);
                    break;
                case STORAGE:
                    quotas[i] = getStorageQuota(folder);
                    break;
                default:
                    throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create("Quota " + types[i]);
            }
        }
        return quotas;
    }

    /**
     * Gets the qualified property names to retrieve when doing PROPFIND queries for resources from the WebDAV server.
     * <p/>
     * Defaults to the qualified names defined in {@link #DEFAULT_PROPERTY_NAMES}, override if applicable.
     *
     * @return The property names to query
     */
    protected Set<QName> getPropertiesToQuery() {
        return DEFAULT_PROPERTY_NAMES;
    }

    /**
     * Constructs a file storage folder from the supplied WebDAV collection.
     * <p/>
     * By default, all common properties are taken over if set. Override if applicable.
     *
     * @param collection The WebDAV collection to create the file storage folder from
     * @return The file storage folder
     */
    protected WebDAVFolder getWebDAVFolder(WebDAVResource collection) {
        if (false == collection.isCollection()) {
            throw new UnsupportedOperationException();
        }
        WebDAVPath path = new WebDAVPath(collection.getHref());
        String folderId = getFolderId(path);
        if (isRoot(folderId)) {
            return getRootFolder();
        }
        WebDAVFolder folder = new WebDAVFolder(session.getUserId());
        folder.setHoldsFiles(true);
        folder.setHoldsFolders(true);
        folder.setSubfolders(true);
        folder.setSubscribedSubfolders(true);
        folder.setId(folderId);
        folder.setParentId(isRoot(folderId) ? null : getFolderId(path.getParent()));
        folder.setName(path.getName());
        folder.setLastModifiedDate(collection.getModifiedDate());
        folder.setCreationDate(collection.getCreationDate());
        return folder;
    }

    /**
     * Gets an appropriate file storage exception for the supplied WebDAV client exception that occurred during communication with the
     * remote WebDAV server when accessing a specific folder.
     *
     * @param e The {@link WebDAVClientException} to get the {@link OXException} for
     * @param folderId The actual folder identifier
     * @return The exception to re-throw
     */
    protected OXException asOXException(WebDAVClientException e, String folderId) {
        switch (e.getStatusCode()) {
            case HttpStatus.SC_NOT_FOUND:
                return FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                    folderId, account.getId(), account.getFileStorageService().getId(), I(session.getUserId()), I(session.getContextId()));
            default:
                return super.asOXException(e);
        }
    }

}
