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

package com.openexchange.file.storage.xox;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.openexchange.api.client.common.calls.folders.SearchFolderCall;
import com.openexchange.api.client.common.calls.folders.UpdateCall;
import com.openexchange.api.client.common.calls.infostore.AdvancedSearch;
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
import com.openexchange.api.client.common.calls.system.ServerVersionCall;
import com.openexchange.api.client.common.calls.system.WhoamiCall;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.Range;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.ToJsonSearchTermVisitor;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.quota.AccountQuota;
import com.openexchange.session.Session;
import com.openexchange.share.core.subscription.AccountMetadataHelper;
import com.openexchange.version.ServerVersion;

/**
 * {@link ShareClient} a client for accessing remote shares on other OX instances/installations
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ShareClient {

    private static final Logger LOG = LoggerFactory.getLogger(ShareClient.class);

    protected static final String TREE_ID = FolderStorage.REAL_TREE_ID;
    protected static final String MODULE_FILES = "files";
    protected static final String TIMEZONE_UTC = "UTC";
    protected static final String SEARCH_FACET_FOLDER = "folder";
    protected static final String SEARCH_FACET_ACCOUNT = "account";
    protected static final String INFOSTORE = Module.INFOSTORE.getName();
    protected static final String INFOSTORE_ACCOUNT_ID = "com.openexchange.infostore://infostore";
    protected static final List<Field> ALL_FIELDS = Collections.unmodifiableList(Arrays.asList(Field.values()));

    private final ApiClient ajaxClient;
    private final Session session;
    private final FileStorageAccount account;
    private final XOXFolderConverter folderConverter;
    private final XOXFileConverter fileConverter;

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
        EntityHelper entityHelper = new EntityHelper(account, client);
        this.folderConverter = new XOXFolderConverter(entityHelper, session);
        this.fileConverter = new XOXFileConverter(entityHelper);
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
     * Internal method to return an array of IDs for the given fields
     *
     * @param fields The fields
     * @return An array of IDs for the given fields
     */
    protected int[] toIdList(Collection<Field> fields) {
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
     * Pings the remote OX
     *
     * @throws OXException if the ping failed
     */
    public void ping() throws OXException {
        getApiClient().execute(new WhoamiCall());
    }

    /**
     * Get the server version
     *
     * @return The server version or <code>null</code>
     * @throws OXException If the version can't be get
     */
    public ServerVersion getServerVersion() throws OXException {
        return getApiClient().execute(new ServerVersionCall());
    }

    /**
     * Gets the folder identified through given identifier
     *
     * @param folderId The identifier
     * @return The corresponding instance of {@link XOXFolder}
     * @throws OXException If either folder does not exist or could not be fetched
     */
    public XOXFolder getFolder(String folderId) throws OXException {
        RemoteFolder remoteFolder = getApiClient().execute(new GetFolderCall(folderId));
        return folderConverter.getStorageFolder(remoteFolder);
    }

    /**
     * Gets the first level subfolders located below the folder whose identifier matches given parameter <code>parentIdentifier</code>.
     *
     * @param parentId The parent identifier
     * @return An array of {@link FileStorageFolder} representing the subfolders
     * @throws OXException If either parent folder does not exist or its subfolders cannot be delivered
     */
    public XOXFolder[] getSubFolders(final String parentId) throws OXException {
        List<RemoteFolder> remoteFolders = getApiClient().execute(new ListFoldersCall(parentId));
        List<XOXFolder> storageFolders = folderConverter.getStorageFolders(remoteFolders);
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
        String idTuple = getApiClient().execute(new NewCall(fileConverter.getRemoteFile(file), data, B(tryAddVersion)));
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
        String idTuple = getApiClient().execute(new PostUpdateCall(fileConverter.getRemoteFile(file), data, sequenceNumber, columns));
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
        String idTuple = getApiClient().execute(new PutUpdateCall(id, fileConverter.getRemoteFile(file), sequenceNumber, columns));
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
        String idTuple = getApiClient().execute(new PutUpdateCall(fileConverter.getRemoteFile(file), sequenceNumber, columns));
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
            getApiClient().execute(new PostCopyCall(id, fileConverter.getRemoteFile(file), data, columns)) :
            getApiClient().execute(new PutCopyCall(id, fileConverter.getRemoteFile(file), columns));
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
        DefaultFile remoteFile = getApiClient().execute(new GetCall(folderId, id, version));
        return fileConverter.getStorageFile(remoteFile);
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
        //@formatter:off
        List<DefaultFile> remoteFiles = getApiClient().execute(
            new GetAllCall(folderId,
                          toIdList(getFieldsToQuery(fields, Field.SEQUENCE_NUMBER)),
                          sort != null ? I(sort.getNumber()) : null,
                          order,
                          range != null ? I(range.from) : null,
                          range != null ? I(range.to) : null));
        //@formatter:on
        return fileConverter.getStorageTimedResult(remoteFiles, fields);
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
        if (null == ids || ids.isEmpty()) {
            return com.openexchange.groupware.results.Results.emptyTimedResult();
        }
        int[] columns = toIdList(getFieldsToQuery(fields, Field.SEQUENCE_NUMBER));
        List<InfostoreTuple> filesToQuery = ids.stream().map(t -> new InfostoreTuple(t.getFolder(), t.getId())).collect(Collectors.toList());
        List<DefaultFile> remoteFiles = getApiClient().execute(new ListCall(filesToQuery, columns));
        return fileConverter.getStorageTimedResult(remoteFiles, fields);
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
        int[] columns = toIdList(getFieldsToQuery(fields, Field.SEQUENCE_NUMBER));
        List<DefaultFile> remoteVersions = getApiClient().execute(new VersionsCall(id, columns, sort != null ? I(sort.getNumber()) : null, order));
        return fileConverter.getStorageTimedResult(remoteVersions, fields);
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
        RemoteFolder newRemoteFolder = folderConverter.getRemoteFolder(folder);
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
        RemoteFolder updatedFolder = folderConverter.getRemoteFolder(folder);
        /*
         * handle changes subscribed flag internally
         */
        if (updatedFolder.containsSubscribed()) {

            updatedFolder.removeSubscribed();
        }
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
     * @param folderId The ID of the folder to get the quota for
     * @return The {@link AccountQuota} for module "filestorage" and account "infostore"
     * @throws OXException In case of error
     */
    public AccountQuota getInfostoreQuota(String folderId) throws OXException {
        final String module = "filestorage";
        final String account = INFOSTORE;
        List<AccountQuota> accountQuota = getApiClient().execute(new com.openexchange.api.client.common.calls.quota.GetCall(module, account, folderId));
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
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        UpdatesCall.SortOrder sortOrder = null;
        if (sort != null && order != null) {
            sortOrder = order == SortDirection.DESC ? UpdatesCall.SortOrder.DESC : UpdatesCall.SortOrder.ASC;
        }

        //@formatter:off
        UpdatesResponse response = getApiClient().execute(new UpdatesCall(folderId,
            toIdList(getFieldsToQuery(fields)),
            L(updateSince),
            ignoreDeleted ? new UpdatesCall.UpdateType[] { UpdatesCall.UpdateType.DELETED } : null,
            sort != null ? sort.getName() : null,
            sortOrder,
            null));
        //@formatter:on

        return new DeltaImpl<File>(fileConverter.getStorageSearchIterator(response.getNewFiles(), fields), fileConverter.getStorageSearchIterator(response.getModifiedFiles(), fields), fileConverter.getStorageSearchIterator(response.getDeletedFiles(), fields), response.getSequenceNumber());
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
    public List<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
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
        int[] columns = toIdList(getFieldsToQuery(fields, Field.SEQUENCE_NUMBER));
        FindResponse<DefaultFile> result = getApiClient().execute(new QueryCall<DefaultFile, File.Field>(MODULE_FILES, columns, builder.build(), new DefaultFileMapper()));
        return fileConverter.getStorageFiles(result.getResultObjects(), fields);
    }

    /**
     * Performs an advanced search
     *
     * @param folderId The IDs of the folders to search within
     * @param searchTerm The {@link SearchTerm}
     * @param fields The fields to return for the found items
     * @param sort The sorting field, or null if no sorting should be applied
     * @param order The sorting order
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (inclusive) for the search results. Useful for paging.
     * @return A list of found files
     * @throws OXException
     */
    public List<File> advancedSearch(List<String> folderIds, SearchTerm<?> searchTerm, List<Field> fields, Field sort, SortDirection order, int start, int end) throws OXException {
        if (!checkServerVersion(API_LEVEL)) {
            LOG.debug("Cannot perform advanced search. Remote server does not support it in prior versions.");
            //We cannot perform an advanced search against the remote, because it was not available in prior versions
            return Collections.emptyList();
        }

        List<File> ret = new ArrayList<>();
        for (String folderId : folderIds) {
            ret.addAll(advancedSearch(folderId, false, searchTerm, fields, null, null, -1, -1));
        }

        //sort & slice
        ret.sort(order.comparatorBy(sort));
        ret = ret.subList(start, end);

        return ret;
    }

    /**
     * Performs an advanced search
     *
     * @param folderId The ID of the folder to search within
     * @param includeSubfolders Whether or not to search in sub folders
     * @param searchTerm The {@link SearchTerm}
     * @param fields The fields to return for the found items
     * @param sort The sorting field, or null if no sorting should be applied
     * @param order The sorting order
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (inclusive) for the search results. Useful for paging.
     * @return A list of found files
     * @throws OXException
     */
    public List<File> advancedSearch(String folderId, boolean includeSubfolders, SearchTerm<?> searchTerm, List<Field> fields, Field sort, SortDirection order, int start, int end) throws OXException {

        if (!checkServerVersion(API_LEVEL)) {
            LOG.debug("Cannot perform advanced search. Remote server does not support it in prior versions.");
            //We cannot perform an advanced search against the remote, because it was not available in prior versions
            return Collections.emptyList();
        }

        //SearchTerm to JSON
        ToJsonSearchTermVisitor visitor = new ToJsonSearchTermVisitor();
        searchTerm.visit(visitor);
        final JSONObject jsonSearch = visitor.createJSON();

        //Setup query
        AdvancedSearch advancedSearch = new AdvancedSearch(folderId, toIdList(getFieldsToQuery(fields, Field.SEQUENCE_NUMBER)), jsonSearch);
        advancedSearch.setIncludeSubfolders(B(includeSubfolders));

        //Set options
        if (sort != null) {
            advancedSearch.setSortBy(sort.getName());
        }
        if (order != null) {
            advancedSearch.setOrder(order == SortDirection.DESC ? UpdatesCall.SortOrder.DESC : UpdatesCall.SortOrder.ASC);
        }
        if (start >= 0) {
            advancedSearch.setStart(I(start));
        }
        if (end >= 0) {
            advancedSearch.setEnd(I(end));
        }

        //Do the search
        List<DefaultFile> result = getApiClient().execute(advancedSearch);
        return fileConverter.getStorageFiles(result, fields);
    }

    public List<XOXFolder> searchByFolderName(String tree, String id, String module, int[] columns, String query, long date, boolean includeSubfolders, boolean all, int start, int end) throws OXException {
        if (null == columns) {
            return searchByFolderName(tree, id, module, SearchFolderCall.DEFAULT_COLUMNS, query, date, includeSubfolders, all, start, end);
        }
        if (!checkServerVersion(API_LEVEL)) {
            LOG.debug("Cannot perform search by folder name. Remote server does not support it in prior versions.");
            //We cannot perform a search by folder name against the remote, because it was not available in prior versions
            return Collections.emptyList();
        }
        List<RemoteFolder> result = getApiClient().execute(new SearchFolderCall(tree, id, columns, module, query, date, includeSubfolders, all, start, end));
        return folderConverter.getStorageFolders(result);
    }

    /** A static reference since when the federated sharing feature was introduced and thus certain API functionality like the {@link Field#CREATED_FROM} field is available */
    private final static ServerVersion API_LEVEL = new ServerVersion(7, 10, 5, "0");

    /**
     * Checks if the remote server version if equals or higher a given version
     *
     * @param versionToCheck The version to check
     * @return true if the remote server version is equals or higher the given version
     * @throws OXException The
     */
    private final boolean checkServerVersion(ServerVersion versionToCheck) throws OXException {
        String versionString = new AccountMetadataHelper(account, session).getCachedValue("serverVersion", TimeUnit.DAYS.toMillis(1L), String.class, () -> {
            ServerVersion serverVersion = getApiClient().execute(new ServerVersionCall());
            return null != serverVersion ? serverVersion.getVersionString() : null;
        });
        if (null == versionString || versionToCheck.compareTo(ServerVersion.parse(versionString)) > 0) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether or not the connected <b>remote host</b> supports federated Sharing API functionality.
     *
     * This check is done by comparing the remote server version to "7.10.5"
     *
     * @return True, if the remote host's version is equals or higher "7.10.5", false otherwise
     * @throws OXException
     */
    public final boolean supportsFederatedSharing() throws OXException {
       return checkServerVersion(API_LEVEL);
    }

    /**
     * Gets the metadata fields to query from the remote server, based on the fields requested that are actually requested.
     * <p/>
     * Fields that are not supported by the remote server are removed automatically.
     *
     * @param requestedFields The requested fields, or <code>null</code> if undefined
     * @param requiredFields Optional additional fields to include, may contain <code>null</code> elements
     * @return The fields to use when querying the remote server
     */
    private Set<Field> getFieldsToQuery(List<Field> requestedFields, Field... requiredFields) throws OXException {
        Set<Field> fields = new HashSet<Field>(null != requestedFields ? requestedFields : Arrays.asList(Field.values()));
        /*
         * handle fields not supported by the remote server
         */
        if (!checkServerVersion(API_LEVEL)) {
            if (fields.remove(Field.CREATED_FROM)) {
                fields.add(Field.CREATED_BY);
            }
            if (fields.remove(Field.MODIFIED_FROM)) {
                fields.add(Field.CREATED_BY);
            }
        }
        /*
         * add required fields
         */
        if (null != requiredFields && 0 < requiredFields.length) {
            for (Field requiredField : requiredFields) {
                fields.add(requiredField);
            }
        }
        return fields;
    }

}
