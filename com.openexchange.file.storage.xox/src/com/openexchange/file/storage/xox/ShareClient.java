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

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.ApiClient;
import com.openexchange.api.client.common.calls.find.FindResponse;
import com.openexchange.api.client.common.calls.find.QueryCall;
import com.openexchange.api.client.common.calls.find.QueryCall.FacetFilter;
import com.openexchange.api.client.common.calls.find.QueryCall.QueryBuilder;
import com.openexchange.api.client.common.calls.folders.DeleteFolderCall;
import com.openexchange.api.client.common.calls.folders.FolderBody;
import com.openexchange.api.client.common.calls.folders.GetFolderCall;
import com.openexchange.api.client.common.calls.folders.ListFoldersCall;
import com.openexchange.api.client.common.calls.folders.RemoteFolder;
import com.openexchange.api.client.common.calls.folders.UpdateCall;
import com.openexchange.api.client.common.calls.infostore.DeleteCall;
import com.openexchange.api.client.common.calls.infostore.DetachCall;
import com.openexchange.api.client.common.calls.infostore.DocumentCall;
import com.openexchange.api.client.common.calls.infostore.DocumentResponse;
import com.openexchange.api.client.common.calls.infostore.GetAllCall;
import com.openexchange.api.client.common.calls.infostore.GetCall;
import com.openexchange.api.client.common.calls.infostore.InfostoreTuple;
import com.openexchange.api.client.common.calls.infostore.ListCall;
import com.openexchange.api.client.common.calls.infostore.LockCall;
import com.openexchange.api.client.common.calls.infostore.MoveCall;
import com.openexchange.api.client.common.calls.infostore.NewCall;
import com.openexchange.api.client.common.calls.infostore.PostCopyCall;
import com.openexchange.api.client.common.calls.infostore.PostUpdateCall;
import com.openexchange.api.client.common.calls.infostore.PutCopyCall;
import com.openexchange.api.client.common.calls.infostore.PutUpdateCall;
import com.openexchange.api.client.common.calls.infostore.UnlockCall;
import com.openexchange.api.client.common.calls.infostore.UpdatesCall;
import com.openexchange.api.client.common.calls.infostore.UpdatesResponse;
import com.openexchange.api.client.common.calls.infostore.VersionsCall;
import com.openexchange.api.client.common.calls.infostore.mapping.DefaultFileMapper;
import com.openexchange.api.client.common.calls.system.WhoamiCall;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.Range;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Strings;
import com.openexchange.quota.AccountQuota;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link ShareClient} a client for accessing remote shares on other OX instances/installations
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ShareClient {

    private final ApiClient ajaxClient;
    private final Session session;
    private final FileStorageAccount account;

    protected static final String SYSTEM_ROOT_FOLDER_ID = "0";
    protected static final String TREE_ID = FolderStorage.REAL_TREE_ID;
    protected static final String MODULE_FILES = "files";
    protected static final String TIMEZONE_UTC = "UTC";
    protected static final String SEARCH_FACET_FOLDER = "folder";
    protected static final String SEARCH_FACET_ACCOUNT = "account";
    protected static final String INFOSTORE = Module.INFOSTORE.getName();
    protected static final String INFOSTORE_ACCOUNT_ID = "com.openexchange.infostore://infostore";

    protected static final List<Field> ALL_FIELDS;
    static {
        ALL_FIELDS = Arrays.asList(Field.values());
    }

    /**
     * Initializes a new {@link ShareClient}.
     *
     * @param session A session
     * @param account The underlying file storage account
     * @param client The underlying {@link ApiClient} to use
     */
    public ShareClient(Session session, FileStorageAccount account, ApiClient client) {
        super();
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.account = account;
        this.ajaxClient = Objects.requireNonNull(client, "client must not be null");
    }

    /**
     * Gets the folderId
     *
     * @param folderId The given ID
     * @return The root folder ID if the given ID is null or empty, the given id otherwise
     */
    protected String getFolderId(String folderId) {
        return Strings.isEmpty(folderId) ? SYSTEM_ROOT_FOLDER_ID : folderId;
    }

    /**
     * Gets the underlying {@link ApiClient}
     *
     * @return The {@link ApiClient}
     */
    protected ApiClient getApiClient() {
        return ajaxClient;
    }

    /**
     * Gets the {@link Session}
     *
     * @return The {@link Session}
     */
    protected Session getSession() {
        return session;
    }

    /**
     * Gets the underlying file storage account
     * 
     * @return The file storage account
     */
    protected FileStorageAccount getAccount() {
        return account;
    }

    /**
     * Pings the remote OX
     *
     * @throws OXException if the ping failed
     */
    public void ping() throws OXException {
        getApiClient().execute(new WhoamiCall());
    }

    /**
     * Internal method to return an array of IDs for the given fields
     *
     * @param fields The fields
     * @return An array of IDs for the given fields
     */
    protected int[] toIdList(List<Field> fields) {
        return fields.stream().mapToInt(f -> f.getNumber()).toArray();
    }

    /**
     * Internal method to create an instance of {@link IDTuple}
     *
     * @param tupleData The data to create the IDTuple from
     * @return The IDTUple, or null if no tuple could be created from the given data
     */
    protected IDTuple toIDTuple(String tupleData) {
        if (tupleData != null && tupleData.contains("/")) {
            int i = tupleData.indexOf("/");
            return new IDTuple(tupleData.substring(0, i), tupleData.substring(i + 1));
        }
        return null;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the folder identified through given identifier
     *
     * @param folderId The identifier
     * @return The corresponding instance of {@link XOXFolder}
     * @throws OXException If either folder does not exist or could not be fetched
     */
    public XOXFolder getFolder(String folderId) throws OXException {
        RemoteFolder remoteFolder = getApiClient().execute(new GetFolderCall(folderId));
        remoteFolder = addEntityInfos(remoteFolder, new XOXEntityInfoLoader(getApiClient()));
        return new XOXFolderConverter(account, session).getStorageFolder(remoteFolder);
    }

    /**
     * Gets the first level subfolders located below the folder whose identifier matches given parameter <code>parentIdentifier</code>.
     *
     * @param parentId The parent identifier
     * @return An array of {@link FileStorageFolder} representing the subfolders
     * @throws OXException If either parent folder does not exist or its subfolders cannot be delivered
     */
    public XOXFolder[] getSubFolders(final String parentId) throws OXException {
        List<RemoteFolder> remoteFolders = getApiClient().execute(new ListFoldersCall(getFolderId(parentId)));
        remoteFolders = addEntityInfos(remoteFolders, new XOXEntityInfoLoader(getApiClient()));
        List<XOXFolder> storageFolders = new XOXFolderConverter(account, session).getStorageFolders(remoteFolders);
        return storageFolders.toArray(new XOXFolder[storageFolders.size()]);
    }

    /**
     * Gets the binary data from a document
     *
     * @param folderId The ID of the folder
     * @param id The ID of the item to get the data from
     * @param version The version to get the data for, or null to get the data for the current version
     * @param eTag The ETag to add to the request
     * @return The {@link DocumentResponse} containing either the data as {@link InputStream} if the document was modified related to the given ETag
     * @throws OXException In case of error
     */
    public DocumentResponse getDocument(String folderId, String id, @Nullable String version, String eTag) throws OXException {
        return getApiClient().execute(new DocumentCall(folderId, id, version, DocumentCall.DELIVERY_METHOD_DOWNLOAD, eTag));
    }

    /**
     * Gets the binary data from a document
     *
     * @param folderId The ID of the folder
     * @param id The ID of the item to get the data from
     * @param version The version to get the data for, or null to get the data for the current version
     * @return The {@link DocumentResponse} containing the data as {@link InputStream}
     * @throws OXException In case of error
     */
    public DocumentResponse getDocument(String folderId, String id, @Nullable String version) throws OXException {
        return getApiClient().execute(new DocumentCall(folderId, id, version, DocumentCall.DELIVERY_METHOD_DOWNLOAD));
    }

    /**
     * Saves a new document
     *
     * @param file The file metadata to set
     * @param data The new content to set
     * @param tryAddVersion whether or not to try adding the file as new version if such a file already exists
     * @return The {@link IDTuple} of the new created file
     * @throws OXException In case of error
     */
    public IDTuple saveNewDocument(File file, InputStream data, boolean tryAddVersion) throws OXException {
        String idTuple = getApiClient().execute(new NewCall(new DefaultFile(file), data, B(tryAddVersion)));
        return toIDTuple(idTuple);
    }

    /**
     * Updates a document meta- and binary-data
     *
     * @param file The file metadata to update
     * @param data The binary data to update
     * @param sequenceNumber The sequence number
     * @param columns The ID of the file's fields to update
     * @return The {@link IDTuple} of the updated file
     * @throws OXException In case of error
     */
    public IDTuple updateDocument(File file, InputStream data, long sequenceNumber, int[] columns) throws OXException {
        String idTuple = getApiClient().execute(new PostUpdateCall(new DefaultFile(file), data, sequenceNumber, columns));
        return toIDTuple(idTuple);
    }

    /**
     * Updates a document's meta data
     *
     * @param id the ID of the item to update
     * @param file The document to update
     * @param sequenceNumber The sequence number
     * @param columns The ID of the file's fields to update
     * @return The {@link IDTuple} of the updated file
     * @throws OXException In case of error
     */
    public IDTuple updateDocument(String id, File file, long sequenceNumber, int[] columns) throws OXException {
        String idTuple = getApiClient().execute(new PutUpdateCall(id, new DefaultFile(file), sequenceNumber, columns));
        return toIDTuple(idTuple);
    }

    /**
     * Updates a document's meta data
     *
     * @param file The document to update
     * @param sequenceNumber The sequence number
     * @param columns The ID of the file's fields to update
     * @return The {@link IDTuple} of the updated file
     * @throws OXException In case of error
     */
    public IDTuple updateDocument(File file, long sequenceNumber, int[] columns) throws OXException {
        String idTuple = getApiClient().execute(new PutUpdateCall(new DefaultFile(file), sequenceNumber, columns));
        return toIDTuple(idTuple);
    }

    /**
     * Copies a document
     *
     * @param id The id of the file to copy
     * @param file The new meta data of the destination item
     * @param columns The columns to set
     * @return The {@link IDTuple} of the copied file
     * @throws OXException In case of error
     */
    public IDTuple copyDocument(String id, File file, int[] columns) throws OXException {
        return copyDocument(id, file, columns, null);
    }

    /**
     * Copies a document
     *
     * @param id The id of the file to copy
     * @param file The new meta data of the destination item
     * @param columns The columns to set
     * @param data The new binary data to apply, or null to not apply any binary data
     * @return The {@link IDTuple} of the copied file
     * @throws OXException In case of error
     */
    public IDTuple copyDocument(String id, File file, int[] columns, @Nullable InputStream data) throws OXException {
        //@formatter:off
        String idTuple = data != null ?
            getApiClient().execute(new PostCopyCall(id, new DefaultFile(file), data, columns)) :
            getApiClient().execute(new PutCopyCall(id, new DefaultFile(file), columns));
        //@formatter:on
        return toIDTuple(idTuple);
    }

    /**
     * Moves a single file to a new folder
     *
     * @param id The ID of the file to move
     * @param destinationFolder The ID of the new destination folder
     * @param timestamp The timestamp
     * @return The new folder/file tuple
     * @throws OXException In case of error
     */
    public IDTuple moveDocument(String id, String destinationFolder, long timestamp) throws OXException {
        String newId = getApiClient().execute(new MoveCall(id, destinationFolder, timestamp));
        return toIDTuple(newId);
    }

    /**
     * Gets given file meta data
     *
     * @param folderId The ID of the folder
     * @param id The ID of the file
     * @param version The version, or null to fetch the current version
     * @return The file
     * @throws OXException In case of error
     */
    public XOXFile getMetaData(String folderId, String id, @Nullable String version) throws OXException {
        DefaultFile file = getApiClient().execute(new GetCall(folderId, id, version));
        return new XOXFile(file);
    }

    /**
     * Gets all documents in the given folder
     *
     * @param folderId The ID of the folder
     * @param fields The fields to get, or null to return all known fields
     * @param sort the field to use for sorting
     * @param order The sort order
     * @return The documents of the given folder
     * @throws OXException In case of error
     */
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        return getDocuments(folderId, fields, sort, order, null);
    }

    /**
     * Gets all documents in the given folder
     *
     * @param folderId The ID of the folder
     * @param fields The fields to get, or null to return all known fields
     * @param sort the field to use for sorting
     * @param order The sort order
     * @param range the range
     * @return The documents of the given folder
     * @throws OXException
     */
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order, Range range) throws OXException {
        final List<Field> fieldsToQuery = fields != null ? fields : ALL_FIELDS;
        boolean requestCreatedFrom = fields == null;
        boolean requestModifiedFrom = fields == null;
        // TODO: Detect 7.10.5 OX to request those fields directly
        if (fieldsToQuery.contains(Field.CREATED_FROM)) {
            fieldsToQuery.remove(Field.CREATED_FROM);
            requestCreatedFrom = true;
        }
        if (fieldsToQuery.contains(Field.MODIFIED_FROM)) {
            fieldsToQuery.remove(Field.MODIFIED_FROM);
            requestModifiedFrom = true;
        }
        //@formatter:off
        List<? extends File> files = getApiClient().execute(
            new GetAllCall(getFolderId(folderId),
                          toIdList(fieldsToQuery),
                          sort != null ? I(sort.getNumber()) : null,
                          order,
                          range != null ? I(range.from) : null,
                          range != null ? I(range.to) : null));
        //@formatter:on
        if (requestCreatedFrom || requestModifiedFrom) {
            XOXEntityInfoLoader loader = new XOXEntityInfoLoader(getApiClient());
            for (File file : files) {
                if (requestCreatedFrom) {
                    EntityInfo info = loader.load(file.getFolderId(), file.getCreatedBy());
                    file.setCreatedFrom(info);
                }
                if (requestModifiedFrom) {
                    EntityInfo info = loader.load(file.getFolderId(), file.getModifiedBy());
                    file.setModifiedFrom(info);
                }
            }
        }
        return new FileTimedResult((List<File>) files);
    }

    /**
     * Lists all given documents
     *
     * @param ids The IDs of the items to fetch
     * @param fields The fields to fetch
     * @return The result
     * @throws OXException In case of error
     */
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        final List<Field> fieldsToQuery = fields != null ? fields : ALL_FIELDS;
        if (ids != null && !ids.isEmpty()) {
            List<InfostoreTuple> filesToQuery = ids.stream().map(t -> new InfostoreTuple(t.getFolder(), t.getId())).collect(Collectors.toList());
            List<? extends File> result = getApiClient().execute(new ListCall(filesToQuery, toIdList(fieldsToQuery)));
            return new FileTimedResult((List<File>) result);
        }

        return new FileTimedResult(Collections.emptyList());
    }

    /**
     * Returns the versions for a given item
     *
     * @param id The ID of the item
     * @param fields The fields to return, or null to return all known fields
     * @param sort The sorting field
     * @param order The sort direction
     * @return A list of versions
     * @throws OXException In case of error
     */
    public TimedResult<File> getVersions(String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        final List<Field> fieldsToQuery = fields != null ? fields : ALL_FIELDS;
        List<? extends File> versions = getApiClient().execute(new VersionsCall(id, toIdList(fieldsToQuery), sort != null ? I(sort.getNumber()) : null, order));
        return new FileTimedResult((List<File>) versions);
    }

    /**
     * Deletes a given set of versions from a file
     * deleteVersions
     *
     * @param id The ID of the file to delete the versions for
     * @param folder The folder ID of the file
     * @param timestamp The timestamp / sequencenumber
     * @param versionsToDelete A list of versions to delta
     * @return The a list of versions which could not get removed
     * @throws OXException
     */
    public String[] deleteVersions(String id, String folder, long timestamp, String[] versionsToDelete) throws OXException {
        int[] versions = Arrays.asList(versionsToDelete).stream().mapToInt(v -> Integer.parseInt(v)).toArray();
        List<Integer> execute = getApiClient().execute(new DetachCall(id, folder, timestamp, versions));
        return execute.stream().map(v -> v.toString()).toArray(String[]::new);
    }

    /**
     * Removed a list of documents
     *
     * @param filesToDelete A list of IDs to removed
     * @param sequenceNumber The sequenceNumber
     * @param hardDelete The hardDelete flag
     * @throws OXException In case of error
     */
    public void removeDocuments(List<IDTuple> filesToDelete, long sequenceNumber, boolean hardDelete) throws OXException {
        if (!filesToDelete.isEmpty()) {
            List<InfostoreTuple> filesToDelete2 = filesToDelete.stream().map(t -> new InfostoreTuple(t.getFolder(), t.getId())).collect(Collectors.toList());
            getApiClient().execute(new DeleteCall(filesToDelete2, sequenceNumber, hardDelete));
        }
    }

    /**
     * Locks an item
     *
     * @param id The id of the item to lock
     * @param diff The amount of time to lock the item as diff related from the current server time
     * @throws OXException In case of error
     */
    public void lock(String id, long diff) throws OXException {
        getApiClient().execute(new LockCall(id, L(diff)));
    }

    /**
     * Unlocks an item
     *
     * @param id The id of the item to lock
     * @throws OXException In case of error
     */
    public void unlock(String id) throws OXException {
        getApiClient().execute(new UnlockCall(id));
    }

    /**
     * Creates a new folder
     *
     * @param folder The ID of the parent folder to create the folder in
     * @param autoRename <code>true</code> to rename the folder (e.g. adding <code>" (1)"</code> appendix) to avoid conflicts; otherwise <code>false</code>
     * @return The ID of the new created folder
     * @throws OXException In case of error
     */
    public String createaFolder(FileStorageFolder folder, boolean autoRename) throws OXException {
        RemoteFolder newRemoteFolder = new XOXFolderConverter(account, session).getFolder(folder);
        FolderBody newFolder = new FolderBody(newRemoteFolder);
        return getApiClient().execute(new com.openexchange.api.client.common.calls.folders.NewCall(newRemoteFolder.getParentID(), newFolder, autoRename));
    }

    /**
     * Moves a folder
     *
     * @param folderId The ID of the folder to move
     * @param newParentId The ID of the parent folder, or null to keep the folder in the current folder
     * @param newName The new name of the folder, or null to keep the name as it is
     * @param timestamp The timestamp
     * @param autoRename <code>true</code> to rename the folder (e.g. adding <code>" (1)"</code> appendix) to avoid conflicts; otherwise <code>false</code>
     * @return The ID of the moved folder
     * @throws OXException In case of error
     */
    public String moveFolder(String folderId, String newParentId, String newName, long timestamp, boolean autoRename) throws OXException {
        RemoteFolder updatedFolder = new RemoteFolder(INFOSTORE);
        updatedFolder.setName(newName);
        updatedFolder.setParentID(newParentId);
        return getApiClient().execute(new UpdateCall(folderId, new FolderBody(updatedFolder), Boolean.FALSE, timestamp, B(autoRename)));
    }

    /**
     * Updates a folders
     *
     * @param folderId The ID of the folder to move
     * @param folder The new folder data
     * @param timestamp The timestamp
     * @param autoRename <code>null</code> if undefined, <code>true</code> to rename the folder (e.g. adding <code>" (1)"</code> appendix) to avoid conflicts; otherwise <code>false</code>
     * @param cascadePermissions <code>null</code> if undefined, <code>true</code> to apply permission changes to all subfolders, <code>false</code>, otherwise
     * @return The ID of the updated folder
     * @throws OXException In case of error
     */
    public String updateFolder(String folderId, FileStorageFolder folder, long timestamp, Boolean autoRename, Boolean cascadePermissions) throws OXException {
        RemoteFolder updatedFolder = new XOXFolderConverter(account, session).getFolder(folder);
        return getApiClient().execute(new UpdateCall(folderId, new FolderBody(updatedFolder), null, timestamp, autoRename, cascadePermissions));
    }

    /**
     * Delete the folder
     *
     * @param folderId The folder ID
     * @param hardDelete Whether to delete permanently or to backup into trash folder
     * @return The folder ID of the deleted folder
     * @throws OXException In case the folder can't be deleted
     */
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        long timestamp = System.currentTimeMillis();
        List<String> notDeleted = getApiClient().execute(new DeleteFolderCall(Collections.singletonList(folderId), TREE_ID, timestamp, null, null, hardDelete, true));
        if (notDeleted.isEmpty()) {
            return folderId;
        }
        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create("Unable to delete folder {}", folderId);
    }

    /**
     * Gets the filestorage / infostore quota
     *
     * @return The {@link AccountQuota} for module "filestorage" and account "infostore"
     * @throws OXException In case of error
     */
    public AccountQuota getInfostoreQuota() throws OXException {
        final String module = "filestorage";
        final String account = INFOSTORE;
        List<AccountQuota> accountQuota = getApiClient().execute(new com.openexchange.api.client.common.calls.quota.GetCall(module, account));
        return accountQuota.get(0);
    }

    /**
     * Gets the changes in a directory since a given time
     *
     * @param folderId The ID of the folder to query for updates
     * @param updateSince The timestamp
     * @param fields The fields to return
     * @param sort The sorting field
     * @param order The sort order
     * @param ignoreDeleted <code>true</code> in order to ignore deleted files, <code>false</code> to include deleted files.
     * @return The changes as delta
     * @throws OXException In case of error
     */
    @SuppressWarnings("unchecked")
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        UpdatesCall.SortOrder sortOrder = null;
        if (sort != null && order != null) {
            sortOrder = order == SortDirection.DESC ? UpdatesCall.SortOrder.DESC : UpdatesCall.SortOrder.ASC;
        }

        //@formatter:off
        UpdatesResponse response = getApiClient().execute(new UpdatesCall(folderId,
            toIdList(fields),
            L(updateSince),
            ignoreDeleted ? new UpdatesCall.UpdateType[] { UpdatesCall.UpdateType.DELETED } : null,
            sort != null ? sort.getName() : null,
            sortOrder,
            null));
        //@formatter:on

        List<? extends File> newFiles = response.getNewFiles();
        List<? extends File> modifiedFiles = response.getModifiedFiles();
        List<? extends File> deletedFiles = response.getDeletedFiles();
        return new FileDelta((List<File>) newFiles, (List<File>) modifiedFiles, (List<File>) deletedFiles, response.getSequenceNumber());
    }

    /**
     * Searches for a given file.
     *
     * @param pattern The search pattern possibly containing wild-cards
     * @param fields Which fields to load
     * @param folderId In which folder to search. Pass ALL_FOLDERS to search in all folders.
     * @param includeSubfolders <code>true</code> to include subfolders, <code>false</code>, otherwise
     * @param sort Which field to sort by. May be <code>null</code>.
     * @param order The order in which to sort
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (exclusive) for the search results. Useful for paging.
     * @return The search results
     * @throws OXException If operation fails
     */
    @SuppressWarnings("unchecked")
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        //Build the query
        //@formatter:off
        final QueryBuilder builder = new QueryBuilder()
            .withStart(start)
            .withSize(end - start)
            .withTimezone(TIMEZONE_UTC)
            .withFacet(SEARCH_FACET_ACCOUNT, INFOSTORE_ACCOUNT_ID)
            .withFacet(SEARCH_FACET_FOLDER, folderId)
            .withFacet("file_name", "file_name:" + pattern,new FacetFilter().setFields(Field.FILENAME.getName()).setQueries(pattern))
            .includeSubfolders(includeSubfolders);
        //@formatter:on

        //Add sorting
        QueryCall.SortOrder sortOrder = null;
        if (sort != null && order != null) {
            sortOrder = order == SortDirection.DESC ? QueryCall.SortOrder.DESC : QueryCall.SortOrder.ASC;
            builder.sortBy(sort.getNumber()).withSortOrder(sortOrder);
        }

        //Search
        FindResponse<DefaultFile> result = getApiClient().execute(new QueryCall<DefaultFile, File.Field>(MODULE_FILES, toIdList(fields), builder.build(), new DefaultFileMapper()));
        List<? extends File> resultFiles = result.getResultObjects();
        return new SearchIteratorAdapter<File>((Iterator<File>) resultFiles.iterator(), resultFiles.size());
    }

    //TODO: intergrate "addEntityInfos" methods into loader directly?

    private List<RemoteFolder> addEntityInfos(Collection<RemoteFolder> remoteFolders, XOXEntityInfoLoader loader) {
        if (null == remoteFolders) {
            return null;
        }
        List<RemoteFolder> enhancedFolders = new ArrayList<RemoteFolder>(remoteFolders.size());
        for (RemoteFolder remoteFolder : remoteFolders) {
            enhancedFolders.add(addEntityInfos(remoteFolder, loader));
        }
        return enhancedFolders;
    }

    private RemoteFolder addEntityInfos(RemoteFolder remoteFolder, XOXEntityInfoLoader loader) {
        if (null == remoteFolder) {
            return null;
        }
        if (null == remoteFolder.getCreatedFrom() && 0 < remoteFolder.getCreatedBy()) {
            remoteFolder.setCreatedFrom(loader.load("", remoteFolder.getCreatedBy())); //TODO: folderId parameter?!
        }
        if (null == remoteFolder.getModifiedFrom() && 0 < remoteFolder.getModifiedBy()) {
            remoteFolder.setModifiedFrom(loader.load("", remoteFolder.getModifiedBy())); //TODO: folderId parameter?!
        }
        if (null != remoteFolder.getPermissions() && 0 < remoteFolder.getPermissions().length) {
            remoteFolder.setPermissions(addEntityInfos(remoteFolder.getPermissions(), loader));
        }
        return remoteFolder;
    }

    private Permission[] addEntityInfos(Permission[] permissions, XOXEntityInfoLoader loader) {
        if (null == permissions || 0 == permissions.length) {
            return permissions;
        }
        Permission[] enhancedPermissions = new Permission[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            enhancedPermissions[i] = addEntityInfos(permissions[i], loader);
        }
        return enhancedPermissions;
    }

    private Permission addEntityInfos(Permission permission, XOXEntityInfoLoader loader) {
        if (null == permission || null != permission.getEntityInfo() || 0 > permission.getEntity() || 0 == permission.getEntity() && false == permission.isGroup()) {
            return permission;
        }
        if (permission.isGroup()) {
            //TODO

            return permission;
        }
        BasicPermission enhancedPermission = new BasicPermission(permission);
        enhancedPermission.setEntityInfo(loader.load("", permission.getEntity()));
        return enhancedPermission;
    }

}
