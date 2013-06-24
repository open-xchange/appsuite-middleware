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

package com.openexchange.file.storage;

import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.meta.FileComparator;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tx.TransactionAware;

/**
 * A {@link FileStorageFileAccess} provides access to files in a file hierarchy.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface FileStorageFileAccess extends TransactionAware {

    /**
     * A tuple for a folder and file identifier.
     */
    public static class IDTuple {

        private String folder;

        private String id;

        public IDTuple() {
            super();
        }

        public IDTuple(final String folder, final String id) {
            this();
            this.folder = folder;
            this.id = id;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(final String folder) {
            this.folder = folder;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((folder == null) ? 0 : folder.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IDTuple other = (IDTuple) obj;
            if (folder == null) {
                if (other.folder != null) {
                    return false;
                }
            } else if (!folder.equals(other.folder)) {
                return false;
            }
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }

    }

    /**
     * A version number pointing at the current version of a file
     */
    public static final String CURRENT_VERSION = null;

    /**
     * An ID value denoting a newly created file, or a file that should be created
     */
    public static final String NEW = null;

    /**
     * A folderId indicating all folders. Useful for searching.
     */
    public static final String ALL_FOLDERS = null;

    /**
     * An undefined last modified value
     */
    public static final long UNDEFINED_SEQUENCE_NUMBER = -1;

    /**
     * A sequence number that can be considered larger than all others
     */
    public static final long DISTANT_FUTURE = Long.MAX_VALUE;

    /**
     * A sequence number that can be considered smaller than all others
     */
    public static final long DISTANT_PAST = Long.MIN_VALUE;

    /**
     * Indicates a range that is not defined. Useful for search.
     */
    public static final int NOT_SET = -11;

    /**
     * Denotes a {@link SortDirection}
     */
    public static enum SortDirection {
        /**
         * Sort in ascending order
         */
        ASC,
        /**
         * Sort in descending order
         */
        DESC;

        /**
         * The default SortDirection
         */
        public static final SortDirection DEFAULT = ASC;

        public Comparator<File> comparatorBy(final File.Field by) {
            final FileComparator fileComparator = new FileComparator(by);
            switch (this) {
            case ASC:
                return fileComparator;
            case DESC:
                return new InverseComparator(fileComparator);
            }
            return null;
        }

        public Comparator<File> comparatorBy(final File.Field by, final Comparator<File> comparator) {
            final FileComparator fileComparator = new FileComparator(by, comparator);
            switch (this) {
            case ASC:
                return fileComparator;
            case DESC:
                return new InverseComparator(fileComparator);
            }
            return null;
        }

        public void sort(final List<File> collection, final File.Field by) {
            Collections.sort(collection, comparatorBy(by));
        }

        public void sort(final List<File> collection, final File.Field by, final Comparator<File> comparator) {
            Collections.sort(collection, comparatorBy(by, comparator));
        }

        private static final class InverseComparator implements Comparator<File> {

            private Comparator<File> delegate = null;

            public InverseComparator(final Comparator<File> delegate) {
                this.delegate = delegate;
            }

            @Override
            public int compare(final File o1, final File o2) {
                return -delegate.compare(o1, o2);
            }
        }

        public static SortDirection get(final String name) {
            if (name == null) {
                return DEFAULT;
            }
            for (final SortDirection dir : values()) {
                if (dir.name().equalsIgnoreCase(name)) {
                    return dir;
                }
            }
            return null;
        }

    }

    /**
     * Find out whether the file with a given ID exists or not.
     *
     * @param folderId The folder identifier
     * @param id The ID to check for
     * @param version The version to check for
     * @return true when the file exists and is readable, false otherwise.
     * @throws OXException If operation fails
     */
    boolean exists(String folderId, String id, String version) throws OXException;

    /**
     * Load the metadata about a file
     *
     * @param folderId The folder identifier
     * @param id The id of the file
     * @param version The version number of the file. May pass in CURRENT_VERSION to load the current version
     * @return The File Metadata
     * @throws OXException If operation fails
     */
    File getFileMetadata(String folderId, String id, String version) throws OXException;

    /**
     * Save the file metadata.
     *
     * @param file The metadata to save
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass UNDEFINED_SEQUENCE_NUMBER for new files or
     *            DISTANT_FUTURE to circumvent the check
     * @throws OXException If operation fails
     */
    void saveFileMetadata(File file, long sequenceNumber) throws OXException; // No modifiedColumns means all columns

    /**
     * Save the file metadata.
     *
     * @param file The metadata to save
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass UNDEFINED_SEQUENCE_NUMBER for new files or
     *            DISTANT_FUTURE to circumvent the check
     * @param modifiedFields The fields to save. All other fields will be ignored
     * @throws OXException If operation fails
     */
    void saveFileMetadata(File file, long sequenceNumber, List<File.Field> modifiedFields) throws OXException;

    /**
     * Copy a file from a given source to a given destination. Changes to the metadata can be applied and a new file attachment
     * may be uploaded as well.
     *
     * @param source The file to copy
     * @param dest Where to copy the file to. This is a folder id.
     * @param update Which other changes to apply to the copy. May be null, if no changes are to be applied.
     * @param newFile A new file to be attached to the copy. May be null, if no new file data should be attached to the file.
     * @param The fields to use from the update. May be null if the update is null.
     * @return The new folderId and id
     * @throws OXException If operation fails
     */
    IDTuple copy(IDTuple source, String destFolder, File update, InputStream newFile, List<File.Field> modifiedFields) throws OXException;

    /**
     * Move a file from a given source to a given destination. Changes to the metadata can be applied and a new file attachment
     * may be uploaded as well.
     *
     * @param source The file to move
     * @param dest Where to move the file to. This is a folder id.
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass UNDEFINED_SEQUENCE_NUMBER for new files or
     *                       DISTANT_FUTURE to circumvent the check
     * @param update Which other changes to apply to the copy. May be null, if no changes are to be applied.
     * @param The fields to use from the update. May be null if the update is null.
     * @return The new folderId and id
     * @throws OXException If operation fails
     */
    IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException;

    /**
     * Load the file content
     *
     * @param folderId The folder identifier
     * @param id The id of the file
     * @param version The version of the file. Pass in CURRENT_VERSION for the current version of the file.
     * @return
     * @throws OXException If operation fails
     */
    InputStream getDocument(String folderId, String id, String version) throws OXException;

    /**
     * Save the file metadata and binary content.
     *
     * @param file The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass UNDEFINED_SEQUENCE_NUMBER for new files or
     *            DISTANT_FUTURE to circumvent the check
     * @throws OXException If operation fails
     */
    void saveDocument(File file, InputStream data, long sequenceNumber) throws OXException;

    /**
     * Save the file metadata and binary content.
     *
     * @param file The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @param modifiedFields The fields to save. All other fields will be ignored
     * @throws OXException If operation fails
     */
    void saveDocument(File file, InputStream data, long sequenceNumber, List<File.Field> modifiedFields) throws OXException;

    /**
     * Remove all files in the given folder.
     *
     * @param folderId The folder to clear
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @throws OXException If operation fails
     */
    void removeDocument(String folderId, long sequenceNumber) throws OXException;

    /**
     * Removes the files with the given identifiers from the folder. Documents identifiers that could not be removed due to an
     * edit-delete conflict are returned.
     *
     * @param ids The identifiers
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @return
     * @throws OXException If operation fails
     */
    List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException;

    /**
     * Remove a certain version of a file
     *
     * @param folderId The folder identifier
     * @param id The file id whose version is to be removed
     * @param versions The versions to be remvoed. The versions that couldn't be removed are returned again.
     * @return
     * @throws OXException If operation fails
     */
    String[] removeVersion(String folderId, String id, String[] versions) throws OXException;

    /**
     * Unlocks a given file.
     *
     * @param folderId The folder identifier
     * @param id The file to unlock
     * @throws OXException If operation fails
     */
    void unlock(String folderId, String id) throws OXException;

    /**
     * Locks a given file for the given duration (in milliseconds)
     *
     * @param folderId The folder identifier
     * @param id The file to lock
     * @param diff The duration in milliseconds
     * @throws OXException If operation fails
     */
    void lock(String folderId, String id, long diff) throws OXException;

    /**
     * Updates a files sequence number
     *
     * @param folderId The folder identifier
     * @param id The file whose sequence number should be updated
     * @throws OXException If operation fails
     */
    void touch(String folderId, String id) throws OXException;

    /**
     * List a folders content
     *
     * @param folderId The folder whose contents to list
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getDocuments(String folderId) throws OXException;

    /**
     * List a folders content loading only the fields given
     *
     * @param folderId The folder whose contents to list
     * @param fields The fields to load
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getDocuments(String folderId, List<File.Field> fields) throws OXException;

    /**
     * List a folders content loading only the fields given and sorting by a certain field either ascending or descending.
     *
     * @param folderId The folder whose contents to list
     * @param fields The fields to load
     * @param sort The field to sort by
     * @param order The sorting direction
     * @return
     * @throws OXException If operation fails
     */
    TimedResult<File> getDocuments(String folderId, List<File.Field> fields, File.Field sort, SortDirection order) throws OXException;

    /**
     * List all versions of a file
     *
     * @param folderId The folder identifier
     * @param id The file's identifier
     * @return All versions of a file
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String folderId, String id) throws OXException;

    /**
     * List all versions of a file loading the given fields
     *
     * @param folderId The folder identifier
     * @param id The file's identifier
     * @param fields The fields to load
     * @return All versions of a file with given fields loaded
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String folderId, String id, List<File.Field> fields) throws OXException;

    /**
     * List all versions of a file loading the given fields sorted according to the given field in a given order
     *
     * @param folderId The folder identifier
     * @param id The file's identifier
     * @param fields The fields to load
     * @return All sorted versions of a file with given fields loaded
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String folderId, String id, List<File.Field> fields, File.Field sort, SortDirection order) throws OXException;

    /**
     * Load the file metadata with the given identifiers.
     *
     * @param ids The identifiers
     * @param fields The fields to load
     * @return The file metadatas
     * @throws OXException If operation fails
     */
    TimedResult<File> getDocuments(List<IDTuple> ids, List<File.Field> fields) throws OXException;

    /**
     * Get changes in a given folder since a certain sequence number
     *
     * @param folderId The folder to examine
     * @param updateSince The sequence number to check against
     * @param fields The fields to load
     * @param ignoreDeleted Whether to check for file deletion as well.
     * @return
     * @throws OXException If operation fails
     */
    Delta<File> getDelta(String folderId, long updateSince, List<File.Field> fields, boolean ignoreDeleted) throws OXException;

    /**
     * Get changes in a given folder since a certain sequence number
     *
     * @param folderId The folder to examine
     * @param updateSince The sequence number to check against
     * @param fields The fields to load
     * @param sort The field to sort by
     * @param order The sorting direction
     * @param ignoreDeleted
     * @return
     * @throws OXException If operation fails
     */
    Delta<File> getDelta(String folderId, long updateSince, List<File.Field> fields, File.Field sort, SortDirection order, boolean ignoreDeleted) throws OXException;

    /**
     * Search for a given file.
     *
     * @param pattern The search pattern possibly containing wild-cards
     * @param fields Which fields to load
     * @param folderId In which folder to search. Pass ALL_FOLDERS to search in all folders.
     * @param sort Which field to sort by. May be null.
     * @param order The order in which to sort
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (exclusive) for the search results. Useful for paging.
     * @return
     * @throws OXException If operation fails
     */
    SearchIterator<File> search(String pattern, List<File.Field> fields, String folderId, File.Field sort, SortDirection order, int start, int end) throws OXException;

    /**
     * Retrieve the parent account access.
     */
    FileStorageAccountAccess getAccountAccess();

}
