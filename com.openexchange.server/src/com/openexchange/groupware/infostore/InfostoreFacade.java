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

package com.openexchange.groupware.infostore;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tx.TransactionAware;

/**
 * {@link InfostoreFacade} - Access to infostore documents.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Some JavaDoc
 */
@SingletonService
public interface InfostoreFacade extends TransactionAware {

    /** Special Version used if you want to retrieve the latest version of an infostore document */
    static int CURRENT_VERSION = -1;

    /** The identifier marking a new infostore document. */
    static int NEW = -1;

    /** Ascending sort order */
    static final int ASC = 1;

    /** Descending sort order */
    static final int DESC = -1;

    /** Permission for documents; either due to folder or object permissions **/
    static enum AccessPermission {
        READ {
            @Override
            public boolean appliesTo(EffectiveInfostorePermission effectivePermission) {
                return effectivePermission.canReadObject();
            }
        },
        WRITE {
            @Override
            public boolean appliesTo(EffectiveInfostorePermission effectivePermission) {
                return effectivePermission.canWriteObject();
            }
        },
        DELETE {
            @Override
            public boolean appliesTo(EffectiveInfostorePermission effectivePermission) {
                return effectivePermission.canDeleteObject();
            }
        },
        ;

        /**
         * Checks if this permission applies to the passed effective infostore permission.
         *
         * @param effectivePermission The effective permission
         * @return <code>true</code> if the permission applies
         */
        public abstract boolean appliesTo(EffectiveInfostorePermission effectivePermission);
    }

    /**
     * Checks if denoted document exists and the sessions user can read it.
     *
     * @param id The identifier
     * @param version The version
     * @param session The session
     * @return <code>true</code> if it exists and is visible; otherwise <code>false</code>
     * @throws OXException If checking for existence fails
     * @see #CURRENT_VERSION
     */
    boolean exists(int id, int version, ServerSession session) throws OXException;

    /**
     * Checks if denoted document exists.
     *
     * @param id The identifier
     * @param version The version
     * @param context The context
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws OXException If checking for existence fails
     * @see #CURRENT_VERSION
     */
    boolean exists(int id, int version, Context context) throws OXException;

    /**
     * Gets the denoted document's meta data information.
     *
     * @param id The identifier
     * @param version The version
     * @param session The session
     * @return The meta data
     * @throws OXException If operation fails
     * @see #CURRENT_VERSION
     * @deprecated use {@link InfostoreFacade#getDocument(int, int, long, long, ServerSession)} instead
     */
    @Deprecated
    DocumentMetadata getDocumentMetadata(int id, int version, ServerSession session) throws OXException;

    /**
     * Gets the denoted document's meta data information.
     *
     * @param folderId The identifier of the parent folder
     * @param id The identifier
     * @param version The version
     * @param session The session
     * @return The meta data
     * @throws OXException If operation fails
     * @see #CURRENT_VERSION
     */
    DocumentMetadata getDocumentMetadata(long folderId, int id, int version, ServerSession session) throws OXException;

    /**
     * Gets the denoted document's meta data information.<br>
     * <b>This method is only for administrative tasks, no permissions are checked!</b>
     *
     * @param id The identifier
     * @param version The version
     * @param context The context
     * @return The meta data
     * @throws OXException If operation fails
     * @see #CURRENT_VERSION
     */
    DocumentMetadata getDocumentMetadata(int id, int version, Context context) throws OXException;

    /**
     * Saves given document meta data.
     * <br>
     * <b>Note</b>: No <tt>modifiedColumns</tt> means all columns.
     *
     * @param document The meta data of the document
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param session The session
     * @throws OXException If save operation fails
     */
    IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, ServerSession session) throws OXException;

    /**
     * Saves given document meta data
     *
     * @param document The meta data of the document
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param modifiedColumns The columns to modify
     * @param session The session
     * @throws OXException If save operation fails
     */
    IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException;

    /**
     * Saves given document meta data. This is currently only meant for updating existing documents. Trying to create a new one will throw
     * an exception!<br>
     * <b>This method is only for administrative tasks, no permissions are checked!</b>
     *
     * @param document The meta data of the document
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param modifiedColumns The columns to modify; <code>null</code> means all columns.
     * @param context The context
     * @throws OXException If save operation fails
     */
    IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, Context context) throws OXException;

    /**
     * Gets the document's binary content.
     *
     * @param id The identifier
     * @param version The version
     * @param session The associated session
     * @return The document's binary content
     * @throws OXException If retrieving binary content fails
     * @see #CURRENT_VERSION
     */
    InputStream getDocument(int id, int version, ServerSession session) throws OXException;

    /**
     * Gets a doucment's stream including associated metadata, depending on the supplied client E-Tag, i.e. the document data is only
     * included in the result in case the client has a stale E-Tag.
     *
     * @param id The identifier of the document to retrieve
     * @param version The version of the document to retrieve
     * @param clientETag The client E-Tag to compare the current E-Tag to
     * @param session The session
     * @return The document metadata, including the document's input stream in case the client E-Tag is outdated
     * @deprecated use {@link InfostoreFacade#getDocumentAndMetadata(int, int, int, String, ServerSession) instead
     */
    @Deprecated
    DocumentAndMetadata getDocumentAndMetadata(int id, int version, String clientETag, ServerSession session) throws OXException;

    /**
     * Gets a doucment's stream including associated metadata, depending on the supplied client E-Tag, i.e. the document data is only
     * included in the result in case the client has a stale E-Tag.
     *
     * @param folderId The identifier of the parent folder
     * @param id The identifier of the document to retrieve
     * @param version The version of the document to retrieve
     * @param clientETag The client E-Tag to compare the current E-Tag to
     * @param session The session
     * @return The document metadata, including the document's input stream in case the client E-Tag is outdated
     */
    DocumentAndMetadata getDocumentAndMetadata(long folderId, int id, int version, String clientETag, ServerSession session) throws OXException;

    /**
     * Saves given document meta data and binary content (if not <code>null</code>).
     *
     * @param document The document meta data
     * @param data The optional binary content or <code>null</code>
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param session The session
     * @throws OXException If save operation fails
     */
    IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, ServerSession session) throws OXException;

    /**
     * Saves given document meta data and binary content (if not <code>null</code>).
     *
     * @param document The document meta data
     * @param data The optional binary content or <code>null</code>
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param modifiedColumns The columns to modify
     * @param session The session
     * @throws OXException If save operation fails
     */
    IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException;

    /**
     * Saves given document meta data and binary content (if not <code>null</code>).
     *
     * @param document The document meta data
     * @param data The optional binary content or <code>null</code>
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param modifiedColumns The columns to modify
     * @param ignoreVersion Whether the version shall <b>NOT</b> be updated
     * @param session The session
     * @throws OXException If save operation fails
     */
    IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, boolean ignoreVersion, ServerSession session) throws OXException;

    /**
     * Removes all documents contained in specified folder.
     *
     * @param folderId The identifier of the folder to clear
     * @param date The client's time stamp
     * @param session The session
     * @throws OXException If remove operation fails
     */
    void removeDocument(long folderId, long date, ServerSession session) throws OXException;

    /**
     * Removes denoted documents.
     *
     * @param ids The identifiers of the documents to remove
     * @param date The client's time stamp
     * @param session The session
     * @return The identifiers of those documents that could <b>not</b> be deleted successfully
     * @throws OXException If remove operation fails
     */
    List<IDTuple> removeDocument(List<IDTuple> ids, long date, ServerSession session) throws OXException;

    /**
     * Removes denoted documents.<br>
     * <b>This method is only for administrative tasks, no permissions are checked!</b>
     *
     * @param ids The identifiers of the documents to remove
     * @param context The context
     * @throws OXException If remove operation fails
     */
    void removeDocuments(List<IDTuple> ids, Context context) throws OXException;

    /**
     * Moves denoted documents to another folder. Colliding filenames in the target folder may be renamed automatically.
     *
     * @param session The session
     * @param ids The identifiers of the documents to remove
     * @param sequenceNumber The sequence number to catch concurrent modifications, i.e. the client's most recent time stamp
     * @param targetFolderID The target folder ID.
     * @param adjustFilenamesAsNeeded <code>true</code> to adjust filenames in target folder automatically, <code>false</code>, otherwise
     * @return The identifiers of those documents that could <b>not</b> be moved successfully
     * @throws OXException If remove operation fails
     */
    List<IDTuple> moveDocuments(ServerSession session, List<IDTuple> ids, long sequenceNumber, String targetFolderID, boolean adjustFilenamesAsNeeded) throws OXException;

    /**
     * Removes denoted versions.
     *
     * @param id The document identifier
     * @param versionIds The identifiers of the versions to remove
     * @param session The session
     * @return The identifiers of those versions that could <b>not</b> be deleted successfully
     * @throws OXException If remove operation fails
     */
    int[] removeVersion(int id, int[] versionIds, ServerSession session) throws OXException;

    /**
     * Gets the folder's documents.
     *
     * @param folderId The folder identifier
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The folder's documents
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getDocuments(long folderId, ServerSession session) throws OXException;

    /**
     * Gets the folder's documents.
     *
     * @param folderId The folder identifier
     * @param columns The columns to set in returned documents
     * @param session The associated session
     * @return The folder's documents
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, ServerSession session) throws OXException;

    /**
     * Gets the documents in a specific folder as seen by a user.
     * <p/>
     * <b>This method is only for administrative tasks, no permissions are checked!</b>
     *
     * @param folderId The folder identifier
     * @param columns The columns to set in returned documents
     * @param sort The sort-by field
     * @param order The order; see {@link #ASC} or {@link #DESC}
     * @param start The start index (inclusive)
     * @param end The end index (exclusive)
     * @param context The context
     * @param user The user
     * @param permissionBits The user permission bits
     * @return The documents
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, int start, int end, Context context, User user, UserPermissionBits permissionBits) throws OXException;

    /**
     * Gets the sorted folder's documents.
     *
     * @param folderId The folder identifier
     * @param columns The columns to set in returned documents
     * @param sort The sort-by field
     * @param order The order; see {@link #ASC} or {@link #DESC}
     * @param session The associated session
     * @return The folder's documents
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, ServerSession session) throws OXException;

    /**
     * Gets the sorted folder's documents.
     *
     * @param folderId The folder identifier
     * @param columns The columns to set in returned documents
     * @param sort The sort-by field
     * @param order The order; see {@link #ASC} or {@link #DESC}
     * @param start The start index (inclusive)
     * @param end The end index (exclusive)
     * @param session The associated session
     * @return The folder's documents
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException;

    /**
     * Gets all documents that are considered as "shared" by the user, i.e. those documents of the user that have been shared to at least
     * one other entity.
     *
     * @param columns The columns to set in returned documents
     * @param sort The sort-by field
     * @param order The order; see {@link #ASC} or {@link #DESC}
     * @param start The start index (inclusive), or <code>-1</code> to start at the beginning
     * @param end The end index (exclusive), or <code>-1</code> for no limitation
     * @param session The associated session
     * @return The folder's documents
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getUserSharedDocuments(Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException;

    /**
     * Gets the document's versions.
     *
     * @param id The document identifier
     * @param session The associated session
     * @return The document's version
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getVersions(int id, ServerSession session) throws OXException;

    /**
     * Gets the document's versions.
     *
     * @param id The document identifier
     * @param columns The columns to set in returned documents
     * @param session The associated session
     * @return The document's versions
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getVersions(int id, Metadata[] columns, ServerSession session) throws OXException;

    /**
     * Gets the document's versions.
     *
     * @param id The document identifier
     * @param columns The columns to set in returned documents
     * @param sort The sort-by field
     * @param order The order; see {@link #ASC} or {@link #DESC}
     * @param session The associated session
     * @return The document's versions
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getVersions(int id, Metadata[] columns, Metadata sort, int order, ServerSession session) throws OXException;

    /**
     * Gets the specified documents.
     *
     * @param ids The identifiers
     * @param columns The columns to set in returned documents
     * @param session The associated session
     * @return The documents
     * @throws OXException If retrieval fails
     */
    TimedResult<DocumentMetadata> getDocuments(List<IDTuple> ids, Metadata[] columns, ServerSession session) throws IllegalAccessException, OXException;

    /**
     * Gets the folder's updated & deleted documents.
     *
     * @param folderId The folder identifier
     * @param updateSince The time stamp to consider
     * @param columns The columns to set in returned documents
     * @param ignoreDeleted Whether to ignore deleted ones
     * @param session The associated session
     * @return The matching changed/deleted documents
     * @throws OXException If retrieval fails
     */
    Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, boolean ignoreDeleted, ServerSession session) throws OXException;

    /**
     * Gets the folder's updated & deleted documents.
     *
     * @param folderId The folder identifier
     * @param updateSince The time stamp to consider
     * @param columns The columns to set in returned documents
     * @param sort The sort-by field
     * @param order The order; see {@link #ASC} or {@link #DESC}
     * @param ignoreDeleted Whether to ignore deleted ones
     * @param session The associated session
     * @return The matching changed/deleted documents
     * @throws OXException If retrieval fails
     */
    Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, Metadata sort, int order, boolean ignoreDeleted, ServerSession session) throws OXException;

    /**
     * Gets the sequence numbers for the contents of the supplied folders to quickly determine which folders contain changes. An updated
     * sequence number in a folder indicates a change, for example a new, modified or deleted file.
     *
     * @param folderIds A list of folder IDs to get the sequence numbers for
     * @param versionsOnly <code>true</code> to only take documents with at least one version into account, <code>false</code>, otherwise
     * @param session The associated session
     * @return A map holding the resulting sequence numbers to each requested folder ID
     * @throws OXException
     */
    Map<Long, Long> getSequenceNumbers(List<Long> folderIds, boolean versionsOnly, ServerSession session) throws OXException;

    /**
     * Gets the number of documents in given folder.
     *
     * @param folderId The folder identifier
     * @param session The associated session
     * @return The number of documents
     * @throws OXException If operation fails
     */
    int countDocuments(long folderId, ServerSession session) throws OXException;

    /**
     * Signals if denoted folder contains documents not owned by specified user.
     *
     * @param folderId The folder identifier
     * @param session The associated session
     * @return <code>true</code> if folder contains documents not owned by specified user; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean hasFolderForeignObjects(long folderId, ServerSession session) throws OXException;

    /**
     * Checks if denoted folder is empty.
     *
     * @param folderId The folder identifier
     * @param ctx The context
     * @return <code>true</code> if empty; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean isFolderEmpty(long folderId, Context ctx) throws OXException;

    /**
     * Performs necessary clean-up operations if specified user has been deleted.
     * 
     * Moves all shared files to the user specified by <code>destUserID</code>. If <code>destUserID</code> set to null the context admin will be used instead.
     * If set to 0 or below all shared files will be deleted instead.
     * 
     * @param userId The user identifier
     * @param context The context
     * @param destUserID The user id the public files will be assigned to.
     * @param session The session
     * @throws OXException If clean-up fails
     */
    void removeUser(int userId, Context context, Integer destUserID, ServerSession session) throws OXException;

    /**
     * Unlocks specified document.
     *
     * @param id The document identifier
     * @param session The session
     * @throws OXException If operation fails
     */
    void unlock(int id, ServerSession session) throws OXException;

    /**
     * Locks specified document.
     *
     * @param id The document identifier
     * @param session The session
     * @throws OXException If operation fails
     */
    void lock(int id, long diff, ServerSession session) throws OXException;

    /**
     * Touches specified document.
     *
     * @param id The document identifier
     * @param session The session
     * @throws OXException If operation fails
     */
    void touch(int id, ServerSession session) throws OXException;

    /**
     * Touches specified document.<br>
     * <b>This method is only for administrative tasks, no permissions are checked!</b>
     *
     * @param id The document identifier
     * @param context The context
     * @throws OXException If operation fails
     */
    void touch(int id, Context context) throws OXException;

    /**
     * Sets this facade's session holder.
     *
     * @param sessionHolder The session holder
     */
    void setSessionHolder(SessionHolder sessionHolder);

    /**
     * Gets the quota restrictions and current usage of {@link Type#FILE} for the supplied session.
     *
     * @param session The session
     * @return The quota of {@link Type#FILE}, or quota with {@link Quota#UNLIMITED} limit if not set
     * @throws OXException
     */
    Quota getFileQuota(ServerSession session) throws OXException;

    /**
     * Gets the quota restrictions and current usage of {@link Type#STORAGE} for the supplied session.
     *
     * @param session The session
     * @return The quota of {@link Type#STORAGE}, or quota with {@link Quota#UNLIMITED} limit if not set
     * @throws OXException
     */
    Quota getStorageQuota(ServerSession session) throws OXException;

    /**
     * Loads (part of) a document's content.
     *
     * @param id The ID of the document
     * @param version The version of the document. Pass {@link FileStorageFileAccess#CURRENT_VERSION} for the current version.
     * @param offset The start offset in bytes to read from the document, or <code>0</code> to start from the beginning
     * @param length The number of bytes to read from the document, or <code>-1</code> to read the stream until the end
     * @param session The associated session
     * @return An input stream for the content
     * @throws OXException
     */
    InputStream getDocument(int id, int version, long offset, long length, ServerSession session) throws OXException;

    /**
     * Save file metadata and content. Since the actual version is modified, the version number is not increased.
     *
     * @param document The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @param modifiedColumns The fields to save. All other fields will be ignored
     * @param offset The start offset in bytes where to append the data to the document, must be equal to the actual document's length
     * @param session The session
     * @throws OXException If operation fails
     */
    IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, long offset, ServerSession session) throws OXException;

    /**
     * Checks whether a user has a certain access permission for a certain file.
     *
     * @param id The ID of the document
     * @param permission The permission
     * @param user The user
     * @param context The context
     * @throws OXException If operation fails
     */
    boolean hasDocumentAccess(int id, AccessPermission permission, User user, Context context) throws OXException;

}
