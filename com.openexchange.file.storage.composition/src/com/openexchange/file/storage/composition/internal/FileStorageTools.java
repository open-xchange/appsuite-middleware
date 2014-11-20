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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.file.storage.composition.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAdvancedSearchFileAccess;
import com.openexchange.file.storage.FileStorageETagProvider;
import com.openexchange.file.storage.FileStorageEfficientRetrieval;
import com.openexchange.file.storage.FileStorageEventHelper.EventProperty;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageIgnorableVersionFileAccess;
import com.openexchange.file.storage.FileStorageLockedFileAccess;
import com.openexchange.file.storage.FileStoragePersistentIDs;
import com.openexchange.file.storage.FileStorageRandomFileAccess;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.ObjectPermissionAware;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.composition.FileStorageCapability;
import com.openexchange.log.LogProperties;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link FileStorageTools}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileStorageTools {

    /**
     * Gets a value indicating whether a specific account supports one or more capabilities.
     *
     * @param fileAccess The file access reference to check the capability for
     * @param capability The capability to check
     * @return <code>true</code> if the capability is supported, <code>false</code>, otherwise
     */
    public static boolean supports(FileStorageFileAccess fileAccess, FileStorageCapability capability) throws OXException {
        switch (capability) {
        case FILE_VERSIONS:
            return FileStorageVersionedFileAccess.class.isInstance(fileAccess);
        case FOLDER_ETAGS:
            return FileStorageETagProvider.class.isInstance(fileAccess);
        case IGNORABLE_VERSION:
            return FileStorageIgnorableVersionFileAccess.class.isInstance(fileAccess);
        case PERSISTENT_IDS:
            return FileStoragePersistentIDs.class.isInstance(fileAccess);
        case RANDOM_FILE_ACCESS:
            return FileStorageRandomFileAccess.class.isInstance(fileAccess);
        case RECURSIVE_FOLDER_ETAGS:
            return FileStorageETagProvider.class.isInstance(fileAccess) && ((FileStorageETagProvider) fileAccess).isRecursive();
        case SEARCH_BY_TERM:
            return FileStorageAdvancedSearchFileAccess.class.isInstance(fileAccess);
        case SEQUENCE_NUMBERS:
            return FileStorageSequenceNumberProvider.class.isInstance(fileAccess);
        case THUMBNAIL_IMAGES:
            return ThumbnailAware.class.isInstance(fileAccess);
        case EFFICIENT_RETRIEVAL:
            return FileStorageEfficientRetrieval.class.isInstance(fileAccess);
        case LOCKS:
            return FileStorageLockedFileAccess.class.isInstance(fileAccess);
        case OBJECT_PERMISSIONS:
            return ObjectPermissionAware.class.isInstance(fileAccess);
        default:
            org.slf4j.LoggerFactory.getLogger(FileStorageTools.class).warn("Unknown capability: {}", capability);
            return false;
        }
    }

    /**
     * Adds the fields {@link Field#ID}, {@link Field#FOLDER_ID} and {@link Field#LAST_MODIFIED} to the field list if not already
     * contained.
     *
     * @param columns The fields to add the ID colums to
     * @return A new list holding the fields as well as the field columns, or the list itself if all already contained
     */
    public static List<File.Field> addIDColumns(List<File.Field> columns) {
        final boolean hasID = columns.contains(File.Field.ID);
        final boolean hasFolder = columns.contains(File.Field.FOLDER_ID);
        final boolean hasLastModified = columns.contains(File.Field.LAST_MODIFIED);

        if (hasID && hasFolder && hasLastModified) {
            return columns;
        }

        columns = new ArrayList<File.Field>(columns);

        if (!hasID) {
            columns.add(File.Field.ID);
        }

        if (!hasFolder) {
            columns.add(File.Field.FOLDER_ID);
        }

        if (!hasLastModified) {
            columns.add(File.Field.LAST_MODIFIED);
        }

        return columns;
    }

    /**
     * Checks that length of the supplied search pattern is allowed according to the <code>com.openexchange.MinimumSearchCharacters</code>
     * configuration property, throwing an appropriate exception if validation fails.
     *
     * @param pattern The pattern to check
     * @throws OXException If validation fails
     */
    public static void checkPatternLength(final String pattern) throws OXException {
        final ConfigurationService configurationService = Services.optService(ConfigurationService.class);
        final int minimumSearchCharacters = null == configurationService ? 0 : configurationService.getIntProperty("com.openexchange.MinimumSearchCharacters", 0);
        if (minimumSearchCharacters <= 0) {
            return;
        }
        if (null != pattern && 0 != pattern.length() && com.openexchange.java.SearchStrings.lengthWithoutWildcards(pattern) < minimumSearchCharacters) {
            throw FileStorageExceptionCodes.PATTERN_NEEDS_MORE_CHARACTERS.create(I(minimumSearchCharacters));
        }
    }

    /**
     * Processes the list of supplied ID tuples to ensure that each entry has an assigned folder ID.
     *
     * @param access The file access to query if folder IDs are missing
     * @param idTuples The ID tuples to process
     * @return The ID tuples, with each entry holding its full file- and folder-ID information
     * @throws OXException
     */
    //TODO: This is weird. The client already sends fileID:folderID pairs, though they get stripped for the infostore currently
    //      when generating the corresponding com.openexchange.file.storage.composition.FileID.
    public static List<IDTuple> ensureFolderIDs(FileStorageFileAccess access, List<IDTuple> idTuples) throws OXException {
        if (null == idTuples || 0 == idTuples.size()) {
            return idTuples;
        }
        List<IDTuple> incompleteTuples = new ArrayList<FileStorageFileAccess.IDTuple>();
        for (IDTuple tuple : idTuples) {
            if (null == tuple.getFolder()) {
                incompleteTuples.add(tuple);
            }
        }
        if (0 < incompleteTuples.size()) {
            SearchIterator<File> searchIterator = null;
            try {
                searchIterator = access.getDocuments(incompleteTuples, Arrays.asList(Field.ID, Field.FOLDER_ID)).results();
                for (int i = 0; i < incompleteTuples.size() && searchIterator.hasNext(); i++) {
                    File file = searchIterator.next();
                    incompleteTuples.get(i).setFolder(file.getFolderId());
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
        }
        return idTuples;
    }

    /**
     * Creates an event property holding the current request's remote address by utilizing the thread-local log properties.
     *
     * @return An <code>remoteAddress</code> event property, or <code>null</code> if not available
     */
    public static EventProperty extractRemoteAddress() {
        Object serverName = LogProperties.get(LogProperties.Name.GRIZZLY_REMOTE_ADDRESS);
        if (null != serverName) {
            return new EventProperty("remoteAddress", serverName.toString());
        }
        return null;
    }

}
