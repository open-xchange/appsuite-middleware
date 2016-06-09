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

package com.openexchange.file.storage.composition;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.Range;
import com.openexchange.file.storage.TryAddVersionAware;
import com.openexchange.file.storage.WarningsAware;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tx.TransactionAware;


/**
 * {@link IDBasedFileAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface IDBasedFileAccess extends TransactionAware, WarningsAware, TryAddVersionAware {

    /**
     * Gets a value indicating whether a specific account supports one or more capabilities.
     *
     * @param serviceID The service identifier for the account to query the capabilities for
     * @param accountID The account identifier for the account to query the capabilities for
     * @param capabilities The capabilities to check
     * @return <code>true</code> if all capabilities are supported, <code>false</code>, otherwise
     * @throws OXException If operation fails
     */
    boolean supports(String serviceID, String accountID, FileStorageCapability...capabilities) throws OXException;

    /**
     * Gets a value indicating whether a file with the given identifier and version exists or not.
     *
     * @param id The ID to check for
     * @param version The version to check for
     * @return <code>true</code> if the file exists and is readable, <code>false</code>, otherwise.
     * @throws OXException If operation fails
     */
    boolean exists(String id, String version) throws OXException;

    /**
     * Loads metadata for specific file version.
     *
     * @param id The id of the file
     * @param version The version number of the file. May pass in CURRENT_VERSION to load the current version
     * @return The file metadata
     * @throws OXException If operation fails
     */
    File getFileMetadata(String id, String version) throws OXException;

    /**
     * Saves metadata for a file.
     *
     * @param document The metadata to save
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass UNDEFINED_SEQUENCE_NUMBER for new files or DISTANT_FUTURE to circumvent the check
     * @return The (fully qualified) unique identifier of the saved file
     * @throws OXException If operation fails
     */
    String saveFileMetadata(File document, long sequenceNumber) throws OXException ; // No modifiedColumns means all columns

    /**
     * Saves metadata for a file.
     *
     * @param document The metadata to save
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass UNDEFINED_SEQUENCE_NUMBER for new files or DISTANT_FUTURE to circumvent the check
     * @param modifiedColumns The fields to save. All other fields will be ignored
     * @return The (fully qualified) unique identifier of the saved file
     * @throws OXException If operation fails
     */
    String saveFileMetadata(File document, long sequenceNumber, List<File.Field> modifiedColumns) throws OXException ;

    /**
     * Saves metadata for a file.
     *
     * @param document The metadata to save
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass UNDEFINED_SEQUENCE_NUMBER for new files or DISTANT_FUTURE to circumvent the check
     * @param modifiedColumns The fields to save. All other fields will be ignored
     * @param ignoreWarnings <code>true</code> to force a file update even if warnings regarding potential data loss are detected, <code>false</code>, otherwise
     * @param tryAddVersion <code>true</code> to add a new version if a file with given name already exists and file versions are supported
     * @return The (fully qualified) unique identifier of the saved file
     * @throws OXException If operation fails
     */
    String saveFileMetadata(File document, long sequenceNumber, List<File.Field> modifiedColumns, boolean ignoreWarnings, boolean tryAddVersion) throws OXException;

    /**
     * Copies a file from the source to the destination.
     *
     * @param sourceId The file to copy
     * @param version The source version to copy, or {@link FileStorageFileAccess#CURRENT_VERSION} to use the current one
     * @param destFolderId The folder to copy into
     * @param update Optional updates to the copy. May be null
     * @param newData Optional new binary data. May be null
     * @param The fields to use from the update.
     * @return The (fully qualified) unique identifier of the copied file
     * @throws OXException If operation fails
     */
    String copy(String sourceId, String version, String destFolderId, File update, InputStream newData, List<File.Field> modifiedFields) throws OXException;

    /**
     * Moves denoted files to specified destination folder.
     *
     * @param sourceIds The file identifiers
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass UNDEFINED_SEQUENCE_NUMBER for new files or DISTANT_FUTURE to circumvent the check
     * @param destFolderId The identifier of the destination folder
     * @param adjustFilenamesAsNeeded <code>true</code> to adjust filenames in target folder automatically, <code>false</code>, otherwise
     * @return The identifiers of those files that could <b>not</b> be moved successfully
     * @throws OXException If move fails
     */
    List<String> move(List<String> sourceIds, long sequenceNumber, String destFolderId, boolean adjustFilenamesAsNeeded) throws OXException;

    /**
     * Loads the documents content
     *
     * @param id The id of the document
     * @param version The version of the document. Pass in CURRENT_VERSION for the current version of the document.
     * @return The content
     * @throws OXException If operation fails
     */
    InputStream getDocument(String id, String version) throws OXException;

    /**
     * Loads (part of) a document's content.
     * <p/>
     * <b>Note:</b> Only available if the underlying account supports the {@link FileStorageCapability#RANDOM_FILE_ACCESS} capability.
     *
     * @param id The ID of the document
     * @param version The version of the document. Pass {@link FileStorageFileAccess#CURRENT_VERSION} for the current version.
     * @param offset The start offset in bytes to read from the document, or <code>0</code> to start from the beginning
     * @param length The number of bytes to read from the document, or <code>-1</code> to read the stream until the end
     * @return An input stream for the content
     * @throws OXException If operation fails
     */
    InputStream getDocument(String id, String version, long offset, long length) throws OXException;

    /**
     * (Optionally) Loads the thumbnail content
     *
     * @param folderId The folder identifier
     * @param id The id of the file which thumbnail shall be returned
     * @param version The version of the file. Pass in CURRENT_VERSION for the current version of the file.
     * @return The thumbnail stream or <code>null</code> if not supported
     * @throws OXException If operation fails
     */
    InputStream optThumbnailStream(String id, String version) throws OXException;

    /**
     * Tries to load the documents content and associated metadata. Returns null if the underlying implementation cannot satisfy this call.
     *
     * <b>Note:</b> Only available if the underlying account supports the {@link FileStorageCapability#EFFICIENT_RETRIEVAL} capability.
     *
     * @param id The id of the document
     * @param version The version of the document. Pass in CURRENT_VERSION for the current version of the document.
     * @return An InputStream Source and metadata or null if the underlying filestore does not implement this feature
     * @throws OXException
     */
    Document getDocumentAndMetadata(String id, String version) throws OXException;

    /**
     * Tries to load the documents content and associated metadata. Only retrieves
     * the document if the given eTag does not match, otherwise returns a document instance with the
     * etag set and no input stream. Returns null if the underlying implementation cannot satisfy this call
     *
     * <b>Note:</b> Only available if the underlying account supports the {@link FileStorageCapability#EFFICIENT_RETRIEVAL} capability.
     *
     * @param id The id of the document
     * @param version The version of the document. Pass in CURRENT_VERSION for the current version of the document.
     * @param clientEtag The eTag supplied by the client, only fill in the input stream if the
     *                     client etag does NOT match the current etag of the document. Still fill in the
     *                     Document#getEtag field.
     * @return An InputStream Source and metadata or null if the underlying filestore does not implement this feature
     * @throws OXException
     */
    Document getDocumentAndMetadata(String id, String version, String clientEtag) throws OXException;

    /**
     * Saves file metadata and binary content.
     * <p/>
     * <b>Notes:</b>
     * <ul>
     * <li>To update an existing file, the file's identifier must be set accordingly</li>
     * <li>For new files, the file ID should be set to {@link FileStorageFileAccess#NEW} (i.e. <code>null</code>)</li>
     * <li>Warnings regarding potential data loss are available via {@link #getWarnings()}</li>
     * </ul>
     *
     * @param document The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass
     *        {@link FileStorageFileAccess#UNDEFINED_SEQUENCE_NUMBER} for new files or
     *        {@link FileStorageFileAccess#DISTANT_FUTURE} to circumvent the check
     * @return The (fully qualified) unique identifier of the saved file
     * @throws OXException If operation fails
     */
    String saveDocument(File document, InputStream data, long sequenceNumber) throws OXException ;

    /**
     * Saves file metadata and binary content.
     * <p/>
     * <b>Notes:</b>
     * <ul>
     * <li>To update an existing file, the file's identifier must be set accordingly</li>
     * <li>For new files, the file ID should be set to {@link FileStorageFileAccess#NEW} (i.e. <code>null</code>)</li>
     * <li>Warnings regarding potential data loss are available via {@link #getWarnings()}</li>
     * </ul>
     *
     * @param document The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass
     *        {@link FileStorageFileAccess#UNDEFINED_SEQUENCE_NUMBER} for new files or
     *        {@link FileStorageFileAccess#DISTANT_FUTURE} to circumvent the check
     * @param modifiedColumns The fields to save. All other fields will be ignored
     * @return The (fully qualified) unique identifier of the saved file
     * @throws OXException If operation fails
     */
    String saveDocument(File document, InputStream data, long sequenceNumber, List<File.Field> modifiedColumns) throws OXException ;

    /**
     * Saves file metadata and binary content, optionally without creating a new version.
     * <p/>
     * <b>Notes:</b>
     * <ul>
     * <li>To update an existing file, the file's identifier must be set accordingly</li>
     * <li>For new files, the file ID should be set to {@link FileStorageFileAccess#NEW} (i.e. <code>null</code>)</li>
     * <li>Setting <code>ignoreVersion</code> to <code>true</code> requires the {@link FileStorageCapability#IGNORABLE_VERSION}</li>
     * <li><code>ignoreVersion</code> is only considered during update operations, not for new files
     * <li>Warnings regarding potential data loss are available via {@link #getWarnings()}</li>
     * </ul>
     *
     * @param document The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass
     *        {@link FileStorageFileAccess#UNDEFINED_SEQUENCE_NUMBER} for new files or
     *        {@link FileStorageFileAccess#DISTANT_FUTURE} to circumvent the check
     * @param modifiedColumns The fields to save. All other fields will be ignored
     * @param ignoreVersion Whether a new version is supposed to be set if binary content is available; or <code>true</code> to keep version as is
     * @return The (fully qualified) unique identifier of the saved file
     * @throws OXException If operation fails
     */
    String saveDocument(File document, InputStream data, long sequenceNumber, List<File.Field> modifiedColumns, boolean ignoreVersion) throws OXException;

    /**
     * Saves file metadata and binary content, optionally without creating a new version.
     * <p/>
     * <b>Notes:</b>
     * <ul>
     * <li>To update an existing file, the file's identifier must be set accordingly</li>
     * <li>For new files, the file ID should be set to {@link FileStorageFileAccess#NEW} (i.e. <code>null</code>)</li>
     * <li>Setting <code>ignoreVersion</code> to <code>true</code> requires the {@link FileStorageCapability#IGNORABLE_VERSION}</li>
     * <li><code>ignoreVersion</code> is only considered during update operations, not for new files
     * <li>Warnings regarding potential data loss are available via {@link #getWarnings()}</li>
     * </ul>
     *
     * @param document The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass
     *            {@link FileStorageFileAccess#UNDEFINED_SEQUENCE_NUMBER} for new files or
     *            {@link FileStorageFileAccess#DISTANT_FUTURE} to circumvent the check
     * @param modifiedColumns The fields to save. All other fields will be ignored
     * @param ignoreVersion Whether a new version is supposed to be set if binary content is available; or <code>true</code> to keep version as is
     * @param ignoreWarnings <code>true</code> to force a file update even if warnings regarding potential data loss are detected, <code>false</code>, otherwise
     * @param tryAddVersion <code>true</code> to add a new version if a file with given name already exists and file versions are supported
     * @return The (fully qualified) unique identifier of the saved file
     * @throws OXException If operation fails
     */
    String saveDocument(File document, InputStream data, long sequenceNumber, List<File.Field> modifiedColumns, boolean ignoreVersion, boolean ignoreWarnings, boolean tryAddVersion) throws OXException;

    /**
     * Save file metadata and content. Since the actual version is modified, the version number is not increased.
     * <p/>
     * <b>Note:</b> Only available if the underlying account supports the {@link FileStorageCapability#RANDOM_FILE_ACCESS} capability.
     *
     * @param document The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @param modifiedColumns The fields to save. All other fields will be ignored
     * @param offset The start offset in bytes where to append the data to the document, must be equal to the actual document's length
     * @return The (fully qualified) unique identifier of the saved file
     * @throws OXException If operation fails
     */
    String saveDocument(File document, InputStream data, long sequenceNumber, List<File.Field> modifiedColumns, long offset) throws OXException;

    /**
     * Removes all documents in the given folder.
     *
     * @param folderId The folder to clear
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass
     *        <code>FileStorageFileAccess#DISTANT_FUTURE</code> to circumvent the check
     * @throws OXException If operation fails
     */
    void removeDocument(String folderId, long sequenceNumber) throws OXException;

    /**
     * Removes the files with the given identifiers from the folder. Documents identifiers that could not be removed due to an
     * edit-delete conflict are returned.
     * <p>
     * Calling this method should have the same effect as invoking {@link #removeDocument(List, long, boolean)} with
     * <code>hardDelete</code> set to <code>false</code>, i.e. if the storage supports a trash folder, and a document is not yet located
     * below that trash folder, it is backed up, otherwise it is deleted permanently.
     *
     * @param ids The identifiers
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @return The IDs of documents that could not be deleted due to an edit-delete conflict
     * @throws OXException If operation fails
     */
    List<String> removeDocument(List<String> ids, long sequenceNumber) throws OXException;

    /**
     * Removes the documents with the given IDs from the folder. Documents' identifiers that could not be removed due to an edit-delete
     * conflict are returned.
     * <p>
     * If <code>hardDelete</code> is <code>false</code>, the storage supports a trash folder, and a document is not yet located below
     * that trash folder, it is backed up, otherwise it is deleted permanently.
     *
     * @param ids The identifiers
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @param hardDelete <code>true</code> to permanently remove the documents, <code>false</code> to move the documents to the default
     *                   trash folder of the storage if possible
     * @return The IDs of documents that could not be deleted due to an edit-delete conflict
     * @throws OXException If operation fails
     */
    List<String> removeDocument(List<String> ids, long sequenceNumber, boolean hardDelete) throws OXException;

    /**
     * Remove a certain version of a file
     * @param id The file id whose version is to be removed
     * @param versions The versions to be remvoed. The versions that couldn't be removed are returned again.
     * @return The IDs of versions that could not be deleted due to an edit-delete conflict
     * @throws OXException If operation fails
     */
    String[] removeVersion(String id, String[] versions) throws OXException;

    /**
     * Unlocks a given file.
     *
     * @param id The file to unlock
     * @throws OXException If operation fails
     */
    void unlock(String id) throws OXException;

    /**
     * Locks a given file for the given duration (in milliseconds)
     * @param id The file to lock
     * @param diff The duration in milliseconds
     * @throws OXException If operation fails
     */
    void lock(String id, long diff) throws OXException;

    /**
     * Updates a files sequence number
     * @param id The file whose sequence number should be updated
     * @throws OXException If operation fails
     */
    void touch(String id) throws OXException;

    /**
     * List a folders content
     * @param folderId The folder whose contents to list
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getDocuments(String folderId) throws OXException;

    /**
     * List a folders content loading only the columns given
     * @param folderId The folder whose contents to list
     * @param columns The fields to load
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getDocuments(String folderId, List<File.Field> columns) throws OXException;

    /**
     * List a folders content loading only the columns given and sorting by a certain field either ascendingly or descendingly
     * @param folderId The folder whose contents to list
     * @param columns The columns to load
     * @param sort The field to sort by
     * @param order The sorting direction
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getDocuments(String folderId, List<File.Field> columns, File.Field sort, SortDirection order) throws OXException;

    /**
     * Lists a folders content loading only the fields given and sorting by a certain field either ascending or descending.
     *
     * @param folderId The folder whose contents to list
     * @param fields The fields to load
     * @param sort The field to sort by
     * @param order The sorting direction
     * @param range The optional range
     * @return The documents
     * @throws OXException If operation fails
     */
    TimedResult<File> getDocuments(String folderId, List<File.Field> fields, File.Field sort, SortDirection order, Range range) throws OXException;

    /**
     * Gets all documents that are considered as "shared" by the user, i.e. those documents of the user that have been shared to at least
     * one other entity.
     * <p/><b>Note:</b>
     * Only available if at least one underlying account supports the {@link FileStorageCapability#OBJECT_PERMISSIONS} capability.
     *
     * @param fields The fields to load, or <code>null</code> to load all fields
     * @param sort The field to sort by, or <code>null</code> for no specific sort order
     * @param order The sorting direction, or <code>null</code> for no specific sort order
     * @return The shared documents of all accounts, or an empty iterator if there are none, or no storage supports object permissions
     * @throws OXException If operation fails
     */
    SearchIterator<File> getUserSharedDocuments(List<Field> fields, Field sort, SortDirection order) throws OXException;

    /**
     * List all versions of a document
     * @param id The documents id
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String id) throws OXException;

    /**
     * List all versions of a document loading the given columns
     * @param id The documents id
     * @param columns The columns to load
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String id, List<File.Field> columns) throws OXException;

    /**
     * List all versions of a document loading the given columns sorted according to the given field in a given order
     * @param id The documents id
     * @param columns The columns to load
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String id, List<File.Field> columns, File.Field sort, SortDirection order) throws OXException;

    /**
     * Load the document metadata with the given identifiers
     * @param ids The identifiers
     * @param columns The fields to load
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getDocuments(List<String> ids, List<File.Field> columns) throws OXException;

    /**
     * Get changes in a given folder since a certain sequence number
     * @param folderId The folder to examine
     * @param updateSince The sequence number to check against
     * @param columns The columns to load
     * @param ignoreDeleted Whether to check for file deletion as well.
     * @return
     * @throws OXException If operation fails
     */
    Delta<File> getDelta(String folderId, long updateSince, List<File.Field> columns, boolean ignoreDeleted) throws OXException;

    /**
     * Get changes in a given folder since a certain sequence number
     * @param folderId The folder to examine
     * @param updateSince The sequence number to check against
     * @param columns The columns to load
     * @param sort The field to sort by
     * @param order The sorting direction
     * @param ignoreDeleted
     * @return
     * @throws OXException If operation fails
     */
    Delta<File> getDelta(String folderId, long updateSince, List<File.Field> columns, File.Field sort, SortDirection order, boolean ignoreDeleted) throws OXException;

    /**
     * Search for a given file.
     *
     * @param query The search query
     * @param cols Which fields to load
     * @param folderId In which folder to search. Pass ALL_FOLDERS to search in all folders.
     * @param sort Which field to sort by. May be <code>null</code>.
     * @param order The order in which to sort
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (exclusive) for the search results. Useful for paging.
     * @return
     * @throws OXException If operation fails
     */
    SearchIterator<File> search(String query, List<File.Field> cols, String folderId, File.Field sort, SortDirection order, int start, int end) throws OXException;

    /**
     * Search for a given file.
     *
     * @param query The search query
     * @param cols Which fields to load
     * @param folderId In which folder to search. Pass ALL_FOLDERS to search in all folders.
     * @param includeSubfolders <code>true</code> to include subfolders, <code>false</code>, otherwise
     * @param sort Which field to sort by. May be <code>null</code>.
     * @param order The order in which to sort
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (exclusive) for the search results. Useful for paging.
     * @return
     * @throws OXException If operation fails
     */
    SearchIterator<File> search(String query, List<File.Field> cols, String folderId, boolean includeSubfolders, File.Field sort, SortDirection order, int start, int end) throws OXException;

    /**
     * Search for a given file.
     *
     * @param folderIds The optional folder identifiers
     * @param searchTerm The search term
     * @param fields The fields to load
     * @param sort Which field to sort by. May be <code>null</code>.
     * @param order The order in which to sort
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (exclusive) for the search results. Useful for paging.
     *
     * @return
     * @throws OXException If operation fails
     */
    SearchIterator<File> search(List<String> folderIds, SearchTerm<?> searchTerm, List<Field> fields, File.Field sort, SortDirection order, int start, int end) throws OXException;

    /**
     * Search for a given file.
     *
     * @param folderId The identifier of the folder to search in
     * @param includeSubfolders <code>true</code> to include subfolders, <code>false</code>, otherwise
     * @param searchTerm The search term
     * @param fields The fields to load
     * @param sort Which field to sort by. May be <code>null</code>.
     * @param order The order in which to sort
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (exclusive) for the search results. Useful for paging.
     * @return The found files
     * @throws OXException If operation fails
     */
    SearchIterator<File> search(String folderId, boolean includeSubfolders, SearchTerm<?> searchTerm, List<Field> fields, File.Field sort, SortDirection order, int start, int end) throws OXException;

    /**
     * Gets the sequence numbers for the contents of the supplied folders to quickly determine which folders contain changes. An updated
     * sequence number in a folder indicates a change, for example a new, modified or deleted file.
     * <p/>
     * <b>Note:</b> Only available if the underlying account supports the {@link FileStorageCapability#SEQUENCE_NUMBERS} capability.
     *
     * @param folderIds A list of folder IDs to get the sequence numbers for
     * @return A map holding the resulting sequence numbers mapped to the corresponding folder ID. Not all folders may be present in
     *         the result.
     * @throws OXException
     */
    Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException;

    /**
     * Gets the ETags for the supplied folders to quickly determine which folders contain changes. An updated ETag in a folder indicates a
     * change, for example a new, modified or deleted file. If {@link FileStorageCapability#RECURSIVE_FOLDER_ETAGS} is supported, an
     * updated ETag may also indicate a change in one of the folder's subfolders.
     * <p/>
     * <b>Note:</b> Only available if the underlying account supports the {@link FileStorageCapability#FOLDER_ETAGS} capability.
     *
     * @param folderIds A list of folder IDs to get the ETags for
     * @return A map holding the resulting ETags to each requested folder ID
     * @throws OXException
     */
    Map<String, String> getETags(List<String> folderIds) throws OXException;

}
