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

package com.openexchange.file.storage.infostore.internal;

import static com.openexchange.file.storage.FileStorageUtility.checkUrl;
import static com.openexchange.file.storage.infostore.internal.FieldMapping.getMatching;
import static com.openexchange.file.storage.infostore.internal.FieldMapping.getSortDirection;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAdvancedSearchFileAccess;
import com.openexchange.file.storage.FileStorageEfficientRetrieval;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageExtendedMetadata;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageLockedFileAccess;
import com.openexchange.file.storage.FileStorageMultiMove;
import com.openexchange.file.storage.FileStoragePersistentIDs;
import com.openexchange.file.storage.FileStorageRandomFileAccess;
import com.openexchange.file.storage.FileStorageRangeFileAccess;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.ObjectPermissionAware;
import com.openexchange.file.storage.Range;
import com.openexchange.file.storage.infostore.FileMetadata;
import com.openexchange.file.storage.infostore.InfostoreFile;
import com.openexchange.file.storage.infostore.InfostoreSearchIterator;
import com.openexchange.file.storage.infostore.ToInfostoreTermVisitor;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentAndMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link InfostoreAdapterFileAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreAdapterFileAccess extends InfostoreAccess implements FileStorageRandomFileAccess, FileStorageSequenceNumberProvider, FileStorageAdvancedSearchFileAccess, FileStoragePersistentIDs, FileStorageVersionedFileAccess, FileStorageLockedFileAccess, FileStorageEfficientRetrieval, ObjectPermissionAware, FileStorageRangeFileAccess, FileStorageExtendedMetadata, FileStorageMultiMove {

    private final InfostoreSearchEngine search;
    private final Context ctx;
    private final User user;
    private final ServerSession session;
    private final FileStorageAccountAccess accountAccess;
    private final int hash;

    /**
     * Initializes a new {@link InfostoreAdapterFileAccess}.
     *
     * @param session
     * @param infostore2
     */
    public InfostoreAdapterFileAccess(final ServerSession session, final InfostoreFacade infostore, final InfostoreSearchEngine search, final FileStorageAccountAccess accountAccess) {
        super(infostore);
        this.session = session;

        this.ctx = session.getContext();
        this.user = session.getUser();

        this.search = search;
        this.accountAccess = accountAccess;

        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountAccess == null) ? 0 : accountAccess.getAccountId().hashCode());
        result = prime * result + ((ctx == null) ? 0 : ctx.getContextId());
        result = prime * result + ((user == null) ? 0 : user.getId());
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InfostoreAdapterFileAccess)) {
            return false;
        }
        InfostoreAdapterFileAccess other = (InfostoreAdapterFileAccess) obj;
        if (accountAccess == null) {
            if (other.accountAccess != null) {
                return false;
            }
        } else if (!accountAccess.getAccountId().equals(other.accountAccess.getAccountId())) {
            return false;
        }
        if (ctx == null) {
            if (other.ctx != null) {
                return false;
            }
        } else if (ctx.getContextId() != other.ctx.getContextId()) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (user.getId() != other.user.getId()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean exists(final String folderId, final String id, final String version) throws OXException {
        try {
            return getInfostore(folderId).exists(ID(id), null == version ? -1 : Integer.parseInt(version), session);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        try {
            return getInfostore(folderId).getDocument(ID(id), null == version ? -1 : Integer.parseInt(version), session);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version, long offset, long length) throws OXException {
        try {
            return getInfostore(folderId).getDocument(ID(id), null == version ? -1 : Integer.parseInt(version), offset, length, session);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public File getFileMetadata(String folderId, String id, String version) throws OXException {
        try {
            DocumentMetadata metadata;
            if (null == folderId) {
                metadata = getInfostore(folderId).getDocumentMetadata(ID(id), null == version ? -1 : Integer.parseInt(version), session);
            } else {
                metadata = getInfostore(folderId).getDocumentMetadata(FOLDERID(folderId), ID(id), null == version ? -1 : Integer.parseInt(version), session);
                if (0 < metadata.getFolderId() && false == folderId.equals(Long.toString(metadata.getFolderId()))) {
                    throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
                }
            }
            return new InfostoreFile(metadata);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String id, String version) throws OXException {
        return getDocumentAndMetadata(folderId, id, version, null);
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String id, String version, String clientETag) throws OXException {
        try {
            DocumentAndMetadata document;
            if (null == folderId) {
                document = getInfostore(folderId).getDocumentAndMetadata(ID(id), null == version ? -1 : ID(version), clientETag, session);
            } else {
                document = getInfostore(folderId).getDocumentAndMetadata(FOLDERID(folderId), ID(id), null == version ? -1 : ID(version), clientETag, session);
                long documentFolderId = null != document.getMetadata() ? document.getMetadata().getFolderId() : 0;
                if (0 < documentFolderId && false == folderId.equals(String.valueOf(documentFolderId))) {
                    throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
                }
            }
            return new InfostoreDocument(document);
        } catch (NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public void lock(final String folderId, final String id, final long diff) throws OXException {
        try {
            getInfostore(folderId).lock(ID(id), diff, session);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        getInfostore(folderId).removeDocument(FOLDERID(folderId), sequenceNumber, session);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber, boolean hardDelete) throws OXException {
        final int[] infostoreIDs = new int[ids.size()];
        final Map<Integer, IDTuple> id2folder = new HashMap<Integer, IDTuple>(ids.size());
        for (int i = 0; i < infostoreIDs.length; i++) {
            final IDTuple tuple = ids.get(i);
            infostoreIDs[i] = ID(tuple.getId());
            id2folder.put(Integer.valueOf(infostoreIDs[i]), tuple);
        }
        InfostoreFacade infostore = getInfostore(null);
        List<IDTuple> conflicted = null;
        if (hardDelete) {
            /*
             * perform hard-deletion independently of file's parent folders
             */
            conflicted = infostore.removeDocument(ids, sequenceNumber, session);
        } else {
            /*
             * check for presence of trash folder
             */
            String trashFolderID = getTrashFolderID();
            if (null == trashFolderID) {
                /*
                 * perform hard-deletion instead
                 */
                conflicted = infostore.removeDocument(ids, sequenceNumber, session);
            } else {
                /*
                 * distinguish between files already in or below trash folder
                 */
                FileStorageFolderAccess folderAccess = getAccountAccess().getFolderAccess();
                String rootFolderID = folderAccess.getRootFolder().getId();
                List<IDTuple> filesToDelete = new ArrayList<IDTuple>();
                List<IDTuple> filesToMove = new ArrayList<IDTuple>();
                Map<String, FileStorageFolder> knownFolders = new HashMap<String, FileStorageFolder>();
                for (IDTuple tuple : ids) {
                    String folderID = tuple.getFolder();
                    while (null != folderID && false == trashFolderID.equals(folderID) && false == rootFolderID.equals(folderID)) {
                        FileStorageFolder folder = knownFolders.get(folderID);
                        if (null == folder) {
                            folder = folderAccess.getFolder(folderID);
                            knownFolders.put(folderID, folder);
                        }
                        folderID = folder.getParentId();
                    }
                    if (trashFolderID.equals(folderID)) {
                        filesToDelete.add(tuple);
                    } else {
                        filesToMove.add(tuple);
                    }
                }
                /*
                 * hard-delete already deleted files
                 */
                if (0 < filesToDelete.size()) {
                    conflicted = infostore.removeDocument(filesToDelete, sequenceNumber, session);
                }
                /*
                 * move other files to trash folder
                 */
                if (0 < filesToMove.size()) {
                    List<IDTuple> conflicted2 = infostore.moveDocuments(session, filesToMove, sequenceNumber, trashFolderID, true);
                    if (null == conflicted || 0 == conflicted.size()) {
                        conflicted = conflicted2;
                    } else if (null != conflicted2 && 0 < conflicted2.size()) {
                        List<IDTuple> temp = new ArrayList<IDTuple>(conflicted.size() + conflicted2.size());
                        temp.addAll(conflicted);
                        temp.addAll(conflicted2);
                        conflicted = temp;
                    }
                }
            }
        }

        return conflicted;
    }

    @Override
    public String[] removeVersion(final String folderId, final String id, final String[] versions) throws OXException {
        return toStrings(getInfostore(folderId).removeVersion(ID(id), parseInts(versions), session));
    }

    private static int[] parseInts(final String[] sa) {
        if (null == sa) {
            return null;
        }
        final int[] ret = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            final String version = sa[i];
            ret[i] = null == version ? -1 : Integer.parseInt(version);
        }
        return ret;
    }

    private static String[] toStrings(final int[] ia) {
        if (null == ia) {
            return null;
        }
        final String[] ret = new String[ia.length];
        for (int i = 0; i < ia.length; i++) {
            final int iVersion = ia[i];
            ret[i] = iVersion < 0 ? null : Integer.toString(iVersion);
        }
        return ret;
    }

    @Override
    public IDTuple saveDocument(final File file, final InputStream data, final long sequenceNumber) throws OXException {
        checkUrl(file);
        return getInfostore(file.getFolderId()).saveDocument(new FileMetadata(file), data, sequenceNumber, session);
    }

    @Override
    public IDTuple saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        if (modifiedFields.contains(Field.URL)) {
            checkUrl(file);
        }
        return getInfostore(file.getFolderId()).saveDocument(new FileMetadata(file), data, sequenceNumber, FieldMapping.getMatching(modifiedFields), session);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields, long offset) throws OXException {
        if (modifiedFields.contains(Field.URL)) {
            checkUrl(file);
        }
        return getInfostore(file.getFolderId()).saveDocument(new FileMetadata(file), data, sequenceNumber, FieldMapping.getMatching(modifiedFields), offset, session);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields, boolean ignoreVersion) throws OXException {
        if (modifiedFields.contains(Field.URL)) {
            checkUrl(file);
        }
        return getInfostore(file.getFolderId()).saveDocument(new FileMetadata(file), data, sequenceNumber, FieldMapping.getMatching(modifiedFields), ignoreVersion, session);
    }

    @Override
    public IDTuple saveDocumentTryAddVersion(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if (modifiedFields.contains(Field.URL)) {
            checkUrl(file);
        }
        return getInfostore(file.getFolderId()).saveDocumentTryAddVersion(new FileMetadata(file), data, sequenceNumber, FieldMapping.getMatching(modifiedFields), session);
    }

    @Override
    public IDTuple saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        checkUrl(file);
        return getInfostore(file.getFolderId()).saveDocumentMetadata(new FileMetadata(file), sequenceNumber, session);
    }

    @Override
    public IDTuple saveFileMetadata(final File file, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        if (modifiedFields.contains(Field.URL)) {
            checkUrl(file);
        }
        return getInfostore(file.getFolderId()).saveDocumentMetadata(new FileMetadata(file), sequenceNumber, FieldMapping.getMatching(modifiedFields), session);
    }

    @Override
    public void touch(final String folderId, final String id) throws OXException {
        getInfostore(folderId).touch(ID(id), session);
    }

    @Override
    public void unlock(final String folderId, final String id) throws OXException {
        getInfostore(folderId).unlock(ID(id), session);
    }

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> fields, final boolean ignoreDeleted) throws OXException {
        final Delta<DocumentMetadata> delta = getInfostore(folderId).getDelta(FOLDERID(folderId), updateSince, FieldMapping.getMatching(fields), ignoreDeleted, session);
        return new InfostoreDeltaWrapper(delta);
    }

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> fields, final Field sort, final SortDirection order, final boolean ignoreDeleted) throws OXException {
        final Delta<DocumentMetadata> delta = getInfostore(folderId).getDelta(FOLDERID(folderId), updateSince, FieldMapping.getMatching(fields), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), ignoreDeleted, session);
        return new InfostoreDeltaWrapper(delta);
    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        /*
         * filter virtual folders
         */
        Map<String, Long> sequenceNumbers = new HashMap<String, Long>(folderIds.size());
        List<Long> foldersToQuery = new ArrayList<Long>(folderIds.size());
        for (String folderId : folderIds) {
            Long id = Long.valueOf(folderId);
            if (VIRTUAL_FOLDERS.contains(id)) {
                sequenceNumbers.put(folderId, Long.valueOf(0L));
            } else {
                foldersToQuery.add(id);
            }
        }
        /*
         * query infostore for non-virtual ones
         */
        if (0 < foldersToQuery.size()) {
            Map<Long, Long> infostoreNumbers = infostore.getSequenceNumbers(foldersToQuery, true, session);
            for (Map.Entry<Long, Long> entry : infostoreNumbers.entrySet()) {
                sequenceNumbers.put(String.valueOf(entry.getKey().longValue()), entry.getValue());
            }
        }
        return sequenceNumbers;
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        final TimedResult<DocumentMetadata> documents = getInfostore(folderId).getDocuments(FOLDERID(folderId), session);
        return new InfostoreTimedResult(documents);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        final TimedResult<DocumentMetadata> documents = getInfostore(folderId).getDocuments(FOLDERID(folderId), FieldMapping.getMatching(fields), session);
        return new InfostoreTimedResult(documents);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        final TimedResult<DocumentMetadata> documents = getInfostore(folderId).getDocuments(FOLDERID(folderId), FieldMapping.getMatching(fields), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), session);
        return new InfostoreTimedResult(documents);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order, Range range) throws OXException {
        if (null == range) {
            return getDocuments(folderId, fields, sort, order);
        }

        TimedResult<DocumentMetadata> documents = getInfostore(folderId).getDocuments(FOLDERID(folderId), FieldMapping.getMatching(fields), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), range.from, range.to, session);
        return new InfostoreTimedResult(documents);
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, final List<Field> fields) throws OXException {
        TimedResult<DocumentMetadata> documents;
        try {
            documents = getInfostore(null).getDocuments(ids, FieldMapping.getMatching(fields), session);
            return new InfostoreTimedResult(documents);
        } catch (final IllegalAccessException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public SearchIterator<File> getUserSharedDocuments(List<Field> fields, Field sort, SortDirection order) throws OXException {
        TimedResult<DocumentMetadata> documents = getInfostore(null).getUserSharedDocuments(getMatching(fields), getMatching(sort), getSortDirection(order), -1, -1, session);
        return new InfostoreTimedResult(documents).results();
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id) throws OXException {
        final TimedResult<DocumentMetadata> versions = getInfostore(folderId).getVersions(ID(id), session);
        return new InfostoreTimedResult(versions);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields) throws OXException {
        final TimedResult<DocumentMetadata> versions = getInfostore(folderId).getVersions(ID(id), FieldMapping.getMatching(fields), session);
        return new InfostoreTimedResult(versions);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        final TimedResult<DocumentMetadata> versions = getInfostore(folderId).getVersions(ID(id), FieldMapping.getMatching(fields), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), session);
        return new InfostoreTimedResult(versions);
    }

    @Override
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, boolean includeSubfolders, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        int folder = (folderId == null) ? InfostoreSearchEngine.NO_FOLDER : Integer.parseInt(folderId);
        return new InfostoreSearchIterator(search.search(session, pattern, folder, includeSubfolders, getMatching(fields), getMatching(sort), getSortDirection(order), start, end));
    }

    @Override
    public SearchIterator<File> search(List<String> folderIds, SearchTerm<?> searchTerm, List<Field> fields, Field sort, SortDirection order, int start, int end) throws OXException {
        TIntList fids = new TIntArrayList(null == folderIds ? 0 : folderIds.size());
        if (null != folderIds) {
            for (final String folderId : folderIds) {
                try {
                    fids.add(Integer.parseInt(folderId));
                } catch (final NumberFormatException e) {
                    throw FileStorageExceptionCodes.INVALID_FOLDER_IDENTIFIER.create(e, folderId);
                }
            }
        }

        ToInfostoreTermVisitor visitor = new ToInfostoreTermVisitor();
        searchTerm.visit(visitor);
        return new InfostoreSearchIterator(search.search(session, visitor.getInfostoreTerm(), fids.toArray(), getMatching(fields), getMatching(sort), getSortDirection(order), start, end));
    }

    @Override
    public SearchIterator<File> search(String folderId, boolean includeSubfolders, SearchTerm<?> searchTerm, List<Field> fields, Field sort, SortDirection order, int start, int end) throws OXException {
        int fid;
        try {
            fid = Integer.parseInt(folderId);
        } catch (NumberFormatException e) {
            throw FileStorageExceptionCodes.INVALID_FOLDER_IDENTIFIER.create(e, folderId);
        }

        ToInfostoreTermVisitor visitor = new ToInfostoreTermVisitor();
        searchTerm.visit(visitor);
        return new InfostoreSearchIterator(search.search(session, visitor.getInfostoreTerm(), fid, includeSubfolders, getMatching(fields), getMatching(sort), getSortDirection(order), start, end));
    }

    @Override
    public void commit() throws OXException {
        infostore.commit();
    }

    @Override
    public void finish() throws OXException {
        infostore.finish();
    }

    @Override
    public void rollback() throws OXException {
        infostore.rollback();
    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        infostore.setCommitsTransaction(commits);
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        infostore.setRequestTransactional(transactional);
    }

    @Override
    public void setTransactional(final boolean transactional) {
        infostore.setTransactional(transactional);
    }

    @Override
    public void startTransaction() throws OXException {
        infostore.startTransaction();
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    @Override
    public IDTuple copy(final IDTuple source, String version, final String destFolder, final File update, final InputStream newFile, final List<File.Field> modifiedFields) throws OXException {
        final File orig = getFileMetadata(source.getFolder(), source.getId(), version);
        InputStream in = newFile;
        if (in == null && orig.getFileName() != null) {
            in = getDocument(source.getFolder(), source.getId(), version);
        }
        if (update != null) {
            orig.copyFrom(update, modifiedFields.toArray(new File.Field[modifiedFields.size()]));
            /*
             * remove creation date of original file so that the current time will be assigned during creation
             */
            if (false == modifiedFields.contains(File.Field.CREATED)) {
                orig.setCreated(null);
            }
        }
        orig.setId(NEW);
        orig.setFolderId(destFolder);
        orig.setObjectPermissions(null);

        if (in == null) {
            saveFileMetadata(orig, UNDEFINED_SEQUENCE_NUMBER);
        } else {
            saveDocument(orig, in, UNDEFINED_SEQUENCE_NUMBER);
        }

        return new IDTuple(destFolder, orig.getId());
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<Field> modifiedFields) throws OXException {
        /*
         * use saveFileMetadata method with adjusted folder; the file ID is sufficient to identify the source
         */
        update.setFolderId(destFolder);
        update.setId(source.getId());
        this.saveFileMetadata(update, sequenceNumber, modifiedFields);
        return new IDTuple(update.getFolderId(), update.getId());
    }

    @Override
    public List<IDTuple> move(List<IDTuple> sources, String destFolder, long sequenceNumber, boolean adjustFilenamesAsNeeded) throws OXException {
        int size;
        if (null == sources || (size = sources.size()) <= 0) {
            return Collections.emptyList();
        }

        // Check if all denoted files are located in the same folder
        boolean sameFolder = true;
        for (int i = size; sameFolder && i-- > 1;) {
            sameFolder = sources.get(i).getFolder().equals(sources.get(i - 1).getFolder());
        }

        // All in the same folder...
        if (sameFolder) {
            // ... yes
            return getInfostore(sources.get(0).getFolder()).moveDocuments(session, sources, sequenceNumber, destFolder, adjustFilenamesAsNeeded);
        }

        // ... no, different folders. Split by folder identifiers,
        Map<String, List<IDTuple>> folder2ids = new LinkedHashMap<String, List<IDTuple>>(size);
        for (IDTuple idTuple : sources) {
            String folder = idTuple.getFolder();
            List<IDTuple> ids = folder2ids.get(folder);
            if (null == ids) {
                ids = new ArrayList<IDTuple>();
                folder2ids.put(folder, ids);
            }
            ids.add(idTuple);
        }

        List<IDTuple> retval = new ArrayList<IDTuple>(size);
        for (Map.Entry<String, List<IDTuple>> filesInFolder : folder2ids.entrySet()) {
            retval.addAll(getInfostore(filesInFolder.getKey()).moveDocuments(session, filesInFolder.getValue(), sequenceNumber, destFolder, adjustFilenamesAsNeeded));
        }
        return retval;
    }

    @Override
    public List<Field> getSupportedFields() {
        // all supported
        return Arrays.asList(File.Field.values());
    }

    /**
     * Gets the ID of the trash folder.
     *
     * @return The trash folder ID, or <code>null</code> if not found
     * @throws OXException
     */
    private String getTrashFolderID() throws OXException {
        FileStorageFolderAccess folderAccess = getAccountAccess().getFolderAccess();
        try {
            FileStorageFolder trashFolder = folderAccess.getTrashFolder();
            if (null != trashFolder) {
                return trashFolder.getId();
            }
        } catch (OXException e) {
            if (false == FileStorageExceptionCodes.NO_SUCH_FOLDER.equals(e)) {
                throw e;
            }
        }
        return null;
    }

}
