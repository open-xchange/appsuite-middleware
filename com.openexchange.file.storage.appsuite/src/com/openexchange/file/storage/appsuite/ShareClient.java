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

package com.openexchange.file.storage.appsuite;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.ApiClient;
import com.openexchange.api.client.common.calls.folders.GetFolderCall;
import com.openexchange.api.client.common.calls.folders.ListFoldersCall;
import com.openexchange.api.client.common.calls.folders.RemoteFolder;
import com.openexchange.api.client.common.calls.infostore.DeleteCall;
import com.openexchange.api.client.common.calls.infostore.DeleteCall.DeleteTuple;
import com.openexchange.api.client.common.calls.infostore.DocumentCall;
import com.openexchange.api.client.common.calls.infostore.GetAllCall;
import com.openexchange.api.client.common.calls.infostore.GetCall;
import com.openexchange.api.client.common.calls.infostore.LockCall;
import com.openexchange.api.client.common.calls.infostore.MoveCall;
import com.openexchange.api.client.common.calls.infostore.NewCall;
import com.openexchange.api.client.common.calls.infostore.PostCopyCall;
import com.openexchange.api.client.common.calls.infostore.PostUpdateCall;
import com.openexchange.api.client.common.calls.infostore.PutCopyCall;
import com.openexchange.api.client.common.calls.infostore.PutUpdateCall;
import com.openexchange.api.client.common.calls.infostore.UnlockCall;
import com.openexchange.api.client.common.calls.infostore.VersionsCall;
import com.openexchange.api.client.common.calls.system.WhoamiCall;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link ShareClient} a client for accessing remote shared on other Appsuite instances
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ShareClient {

    private final ApiClient ajaxClient;
    private final Session session;

    private static final String USER_INFOSTORE_FOLDER = "10";
    private static final List<Field> ALL_FIELDS;

    static {
        ALL_FIELDS = Arrays.asList(Field.values());
    }

    /**
     * Initializes a new {@link ShareClient}.
     *
     * @param session A session
     * @param client The underlying {@link ApiClient} to use
     */
    public ShareClient(Session session, ApiClient client) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.ajaxClient = Objects.requireNonNull(client, "client must not be null");
    }

    private String getFolderId(String folderId) {
        return Strings.isEmpty(folderId) ? USER_INFOSTORE_FOLDER : folderId;
    }

    /**
     * Pings the remote OX
     *
     * @throws OXException if the ping failed
     */
    public void ping() throws OXException {
        ajaxClient.execute(new WhoamiCall());
    }

    /**
     * Internal method to return an array of IDs for the given fields
     *
     * @param fields The fields
     * @return An array of IDs for the given fields
     */
    private int[] toIdList(List<Field> fields) {
        return fields.stream().mapToInt(f -> f.getNumber()).toArray();
    }

    /**
     * Internal method to create an instance of {@link IDTubple}
     *
     * @param tupleData The data to create the IDTuple from
     * @return The IDTUple, or null if no tuple could be created from the given data
     */
    private IDTuple toIDTuple(String tupleData) {
        if (tupleData != null && tupleData.contains("/")) {
            int i = tupleData.indexOf("/");
            return new IDTuple(tupleData.substring(0, i), tupleData.substring(i + 1));
        }
        return null;
    }

    /**
     * Gets the folder identified through given identifier
     *
     * @param folderId The identifier
     * @return The corresponding instance of {@link AppsuiteFolder}
     * @throws OXException If either folder does not exist or could not be fetched
     */
    public AppsuiteFolder getFolder(String folderId) throws OXException {
        RemoteFolder remoteFolder = ajaxClient.execute(new GetFolderCall(folderId));
        final int userId = session.getUserId();
        return new AppsuiteFolder(userId, remoteFolder);
    }

    /**
     * Gets the first level subfolders located below the folder whose identifier matches given parameter <code>parentIdentifier</code>.
     *
     * @param parentId The parent identifier
     * @return An array of {@link FileStorageFolder} representing the subfolders
     * @throws OXException If either parent folder does not exist or its subfolders cannot be delivered
     */
    public AppsuiteFolder[] getSubFolders(String parentId) throws OXException {
        List<RemoteFolder> folders = ajaxClient.execute(new ListFoldersCall(getFolderId(parentId)));
        final int userId = session.getUserId();
        List<AppsuiteFolder> ret = folders.stream().map(f -> new AppsuiteFolder(userId, f)).collect(Collectors.toList());
        return ret.toArray(new AppsuiteFolder[ret.size()]);
    }

    /**
     * Gets the binary data from a document
     *
     * @param folderId The ID of the folder
     * @param id The ID of the item to get the data from
     * @param version The version to get the data for, or null to get the data for the current version
     * @return The binary data as {@link InputStream}
     * @throws OXException
     */
    public InputStream getDocument(String folderId, String id, @Nullable String version) throws OXException {
        return ajaxClient.execute(new DocumentCall(folderId, id, version));
    }

    /**
     * Saves a new document
     *
     * @param file The file metadata to set
     * @param data The new content to set
     * @param tryAddVersion whether or not to try adding the file as new version if such a file already exists
     * @return The {@link IDTuple} of the new created file
     * @throws OXException
     */
    public IDTuple saveNewDocument(File file, InputStream data, boolean tryAddVersion) throws OXException {
        String idTuple = ajaxClient.execute(new NewCall(new DefaultFile(file), data, B(tryAddVersion)));
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
     * @throws OXException
     */
    public IDTuple updateDocument(File file, InputStream data, long sequenceNumber, int[] columns) throws OXException {
        String idTuple = ajaxClient.execute(new PostUpdateCall(new DefaultFile(file), data, sequenceNumber, columns));
        return toIDTuple(idTuple);
    }

    /**
     * Updates a document's meta data
     *
     * @param file The document to update
     * @param sequenceNumber The sequence number
     * @param columns The ID of the file's fields to update
     * @return The {@link IDTuple} of the updated file
     * @throws OXException
     */
    public IDTuple updateDocument(File file, long sequenceNumber, int[] columns) throws OXException {
        String idTuple = ajaxClient.execute(new PutUpdateCall(new DefaultFile(file), sequenceNumber, columns));
        return toIDTuple(idTuple);
    }

    /**
     * Copies a document
     *
     * @param id The id of the file to copy
     * @param file The new meta data of the destination item
     * @param columns The columns to set
     * @return The {@link IDTuple} of the copied file
     * @throws OXException
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
     * @throws OXException
     */
    public IDTuple copyDocument(String id, File file, int[] columns, @Nullable InputStream data) throws OXException {
        //@formatter:off
        String idTuple = data != null ?
            ajaxClient.execute(new PostCopyCall(id, new DefaultFile(file), data, columns)) :
            ajaxClient.execute(new PutCopyCall(id, new DefaultFile(file), columns));
        //@formatter:on
        return toIDTuple(idTuple);
    }

    /**
     * Moves a single file to a new folder
     *
     * @param id The ID of the file to move
     * @param destinationFolder The ID of the new destination folder
     * @param timestamp The timestamp
     * @return
     * @throws OXException
     */
    public IDTuple moveDocument(String id, String destinationFolder, long timestamp) throws OXException {
        String newId = ajaxClient.execute(new MoveCall(id, destinationFolder, timestamp));
        return toIDTuple(newId);
    }

    /**
     * Gets given file meta data
     *
     * @param folderId The ID of the folder
     * @param id The ID of the file
     * @param version The version, or null to fetch the current version
     * @return The file
     * @throws OXException
     */
    public AppsuiteFile getMetaData(String folderId, String id, @Nullable String version) throws OXException {
        DefaultFile file = ajaxClient.execute(new GetCall(folderId, id, version));
        return new AppsuiteFile(file);
    }

    /**
     * Gets all documents in the given folder
     *
     * @param folderId The ID of the folder
     * @param fields The fields to get, or null to return all known fields
     * @param sort the field to use for sorting
     * @param order The sort order
     * @return The documents of the given folder
     * @throws OXException
     */
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {

        //TODO: refine
        //Folders in the first layer are not accessible for file listing (infostore?action=all)
        //if(folderId.equals("44")) {
        //    return new FileTimedResult(Collections.emptyList());
        //}

        final List<Field> fieldsToQuery = fields != null ? fields : ALL_FIELDS;
        //@formatter:off
        List<? extends File> files = ajaxClient.execute(
            new GetAllCall(getFolderId(folderId),
                          toIdList(fieldsToQuery),
                          sort != null ? I(sort.getNumber()) : null,
                          order));
        //@formatter:on
        return new FileTimedResult((List<File>) files);
    }

    /**
     * Returns the versions for a given item
     *
     * @param id The ID of the item
     * @param fields The fields to return, or null to return all known fields
     * @param sort The sorting field
     * @param order The sort direction
     * @return A list of versions
     * @throws OXException
     */
    public TimedResult<File> getVersions(String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        final List<Field> fieldsToQuery = fields != null ? fields : ALL_FIELDS;
        //@formatter:off
        List<? extends File> versions = ajaxClient.execute(
            new VersionsCall(id,
                             toIdList(fieldsToQuery),
                             sort != null ? I(sort.getNumber()) : null,
                             order));
        //@formatter:on
        return new FileTimedResult((List<File>) versions);
    }

    /**
     * Removed a list of documents
     *
     * @param filesToDelete A list of IDs to removed
     * @param sequenceNumber The sequenceNumber
     * @param hardDelete The hardDelete flag
     * @throws OXException
     */
    public void removeDocuments(List<IDTuple> filesToDelete, long sequenceNumber, boolean hardDelete) throws OXException {
        if (filesToDelete.isEmpty()) {
            List<DeleteTuple> filesToDelete2 = filesToDelete.stream().map(t -> new DeleteCall.DeleteTuple(t.getFolder(), t.getId())).collect(Collectors.toList());
            ajaxClient.execute(new DeleteCall(filesToDelete2, sequenceNumber, hardDelete));
        }
    }

    /**
     * Locks an item
     *
     * @param id The id of the item to lock
     * @param diff The amount of time to lock the item as diff related from the current server time
     * @throws OXException
     */
    public void lock(String id, long diff) throws OXException {
        ajaxClient.execute(new LockCall(id, L(diff)));
    }

    /**
     * Unlocks an item
     *
     * @param id The id of the item to lock
     * @throws OXException
     */
    public void Unlock(String id) throws OXException {
        ajaxClient.execute(new UnlockCall(id));
    }
}
