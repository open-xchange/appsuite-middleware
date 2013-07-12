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

package com.openexchange.file.storage.infostore;

import static com.openexchange.file.storage.FileStorageUtility.checkUrl;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageRandomFileAccess;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.infostore.internal.VirtualFolderInfostoreFacade;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link InfostoreAdapterFileAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreAdapterFileAccess implements FileStorageRandomFileAccess, FileStorageSequenceNumberProvider {

    private static final InfostoreFacade VIRTUAL_INFOSTORE = new VirtualFolderInfostoreFacade();
    private static final Set<Long> VIRTUAL_FOLDERS;
    static {
        final Set<Long> set = new HashSet<Long>(4);
        set.add(Long.valueOf(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID));
        set.add(Long.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID));
        set.add(Long.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID));
        set.add(Long.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID));
        VIRTUAL_FOLDERS = Collections.unmodifiableSet(set);
    }

    private final InfostoreFacade infostore;
    private final InfostoreSearchEngine search;
    private final Context ctx;
    private final User user;
    private final UserConfiguration userConfig;
    private final ServerSession sessionObj;
    private final FileStorageAccountAccess accountAccess;

    /**
     * Initializes a new {@link InfostoreAdapterFileAccess}.
     *
     * @param session
     * @param infostore2
     */
    public InfostoreAdapterFileAccess(final ServerSession session, final InfostoreFacade infostore, final InfostoreSearchEngine search, final FileStorageAccountAccess accountAccess) {
        super();
        this.sessionObj = session;

        this.ctx = sessionObj.getContext();
        this.user = sessionObj.getUser();
        this.userConfig = sessionObj.getUserConfiguration();

        this.infostore = infostore;
        this.search = search;
        this.accountAccess = accountAccess;
    }

    @Override
    public boolean exists(final String folderId, final String id, final String version) throws OXException {
        try {
            return getInfostore(folderId).exists(ID(id), null == version ? -1 : Integer.parseInt(version), ctx, user, userConfig);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        try {
            return getInfostore(folderId).getDocument(ID(id), null == version ? -1 : Integer.parseInt(version), ctx, user, userConfig);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version, long offset, long length) throws OXException {
        try {
            return getInfostore(folderId).getDocument(
                ID(id), null == version ? -1 : Integer.parseInt(version), offset, length, ctx, user, userConfig);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        try {
            final DocumentMetadata documentMetadata =
                getInfostore(folderId).getDocumentMetadata(ID(id), null == version ? -1 : Integer.parseInt(version), ctx, user, userConfig);
            return new InfostoreFile(documentMetadata);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public void lock(final String folderId, final String id, final long diff) throws OXException {
        try {
            getInfostore(folderId).lock(ID(id), diff, sessionObj);
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, id, folderId);
        }
    }

    @Override
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        getInfostore(folderId).removeDocument(FOLDERID(folderId), sequenceNumber, sessionObj);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        final int[] infostoreIDs = new int[ids.size()];
        final Map<Integer, IDTuple> id2folder = new HashMap<Integer, IDTuple>();
        for (int i = 0; i < infostoreIDs.length; i++) {
            final IDTuple tuple = ids.get(i);
            infostoreIDs[i] = ID(tuple.getId());
            id2folder.put(Integer.valueOf(infostoreIDs[i]), tuple);
        }

        final int[] conflicted = getInfostore(null).removeDocument(infostoreIDs, sequenceNumber, sessionObj);

        final List<IDTuple> retval = new ArrayList<IDTuple>(conflicted.length);
        for (final int id : conflicted) {
            retval.add(id2folder.get(Integer.valueOf(id)));
        }

        return retval;
    }

    @Override
    public String[] removeVersion(final String folderId, final String id, final String[] versions) throws OXException {
        return toStrings(getInfostore(folderId).removeVersion(ID(id), parseInts(versions), sessionObj));
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
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber) throws OXException {
        checkUrl(file);
        getInfostore(file.getFolderId()).saveDocument(new FileMetadata(file), data, sequenceNumber, sessionObj);
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        if (modifiedFields.contains(Field.URL)) {
            checkUrl(file);
        }
        getInfostore(file.getFolderId()).saveDocument(
            new FileMetadata(file),
            data,
            sequenceNumber,
            FieldMapping.getMatching(modifiedFields),
            sessionObj);
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields, final boolean ignoreVersion) throws OXException {
        if (modifiedFields.contains(Field.URL)) {
            checkUrl(file);
        }
        getInfostore(file.getFolderId()).saveDocument(
            new FileMetadata(file),
            data,
            sequenceNumber,
            FieldMapping.getMatching(modifiedFields),
            ignoreVersion,
            sessionObj);
    }

    @Override
    public void saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields, long offset) throws OXException {
        if (modifiedFields.contains(Field.URL)) {
            checkUrl(file);
        }
        getInfostore(file.getFolderId()).saveDocument(
            new FileMetadata(file),
            data,
            sequenceNumber,
            FieldMapping.getMatching(modifiedFields),
            offset,
            sessionObj);
    }

    @Override
    public void saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        checkUrl(file);
        getInfostore(file.getFolderId()).saveDocumentMetadata(new FileMetadata(file), sequenceNumber, sessionObj);
    }

    @Override
    public void saveFileMetadata(final File file, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        if (modifiedFields.contains(Field.URL)) {
            checkUrl(file);
        }
        getInfostore(file.getFolderId()).saveDocumentMetadata(
            new FileMetadata(file),
            sequenceNumber,
            FieldMapping.getMatching(modifiedFields),
            sessionObj);
    }

    @Override
    public void touch(final String folderId, final String id) throws OXException {
        getInfostore(folderId).touch(ID(id), sessionObj);
    }

    @Override
    public void unlock(final String folderId, final String id) throws OXException {
        getInfostore(folderId).unlock(ID(id), sessionObj);
    }

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> fields, final boolean ignoreDeleted) throws OXException {
        final Delta<DocumentMetadata> delta =
            getInfostore(folderId).getDelta(
                FOLDERID(folderId),
                updateSince,
                FieldMapping.getMatching(fields),
                ignoreDeleted,
                ctx,
                user,
                userConfig);
        return new InfostoreDeltaWrapper(delta);
    }

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> fields, final Field sort, final SortDirection order, final boolean ignoreDeleted) throws OXException {
        final Delta<DocumentMetadata> delta =
            getInfostore(folderId).getDelta(
                FOLDERID(folderId),
                updateSince,
                FieldMapping.getMatching(fields),
                FieldMapping.getMatching(sort),
                FieldMapping.getSortDirection(order),
                ignoreDeleted,
                ctx,
                user,
                userConfig);
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
            Map<Long, Long> infostoreNumbers = infostore.getSequenceNumbers(foldersToQuery, true, ctx, user, userConfig);
            for (Map.Entry<Long, Long> entry : infostoreNumbers.entrySet()) {
                sequenceNumbers.put(String.valueOf(entry.getKey().longValue()), entry.getValue());
            }
        }
        return sequenceNumbers;
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        final TimedResult<DocumentMetadata> documents = getInfostore(folderId).getDocuments(FOLDERID(folderId), ctx, user, userConfig);
        return new InfostoreTimedResult(documents);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        final TimedResult<DocumentMetadata> documents =
            getInfostore(folderId).getDocuments(FOLDERID(folderId), FieldMapping.getMatching(fields), ctx, user, userConfig);
        return new InfostoreTimedResult(documents);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        final TimedResult<DocumentMetadata> documents =
            getInfostore(folderId).getDocuments(
                FOLDERID(folderId),
                FieldMapping.getMatching(fields),
                FieldMapping.getMatching(sort),
                FieldMapping.getSortDirection(order),
                ctx,
                user,
                userConfig);
        return new InfostoreTimedResult(documents);
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, final List<Field> fields) throws OXException {
        final int[] infostoreIDs = IDS(ids);
        TimedResult<DocumentMetadata> documents;
        try {
            documents = getInfostore(null).getDocuments(infostoreIDs, FieldMapping.getMatching(fields), ctx, user, userConfig);
            return new InfostoreTimedResult(documents);
        } catch (final IllegalAccessException e) {
            throw new OXException(e);
        }
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id) throws OXException {
        final TimedResult<DocumentMetadata> versions = getInfostore(folderId).getVersions(ID(id), ctx, user, userConfig);
        return new InfostoreTimedResult(versions);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields) throws OXException {
        final TimedResult<DocumentMetadata> versions =
            getInfostore(folderId).getVersions(ID(id), FieldMapping.getMatching(fields), ctx, user, userConfig);
        return new InfostoreTimedResult(versions);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        final TimedResult<DocumentMetadata> versions =
            getInfostore(folderId).getVersions(
                ID(id),
                FieldMapping.getMatching(fields),
                FieldMapping.getMatching(sort),
                FieldMapping.getSortDirection(order),
                ctx,
                user,
                userConfig);
        return new InfostoreTimedResult(versions);
    }

    @Override
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        final int folder = (folderId == null) ? InfostoreSearchEngine.NO_FOLDER : Integer.parseInt(folderId);
        final SearchIterator<DocumentMetadata> iterator =
            search.search(
                pattern,
                FieldMapping.getMatching(fields),
                folder,
                FieldMapping.getMatching(sort),
                FieldMapping.getSortDirection(order),
                start,
                end,
                ctx,
                user,
                userConfig);
        return new InfostoreSearchIterator(iterator);
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

    private static int ID(final String id) {
        return Integer.parseInt(id);
    }

    private static long FOLDERID(final String folderId) {
        return Long.parseLong(folderId);
    }

    private static int[] IDS(final List<IDTuple> ids) {
        final int[] infostoreIDs = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            infostoreIDs[i] = ID(ids.get(i).getId());
        }
        return infostoreIDs;
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    @Override
    public IDTuple copy(final IDTuple source, final String destFolder, final File update, final InputStream newFile, final List<File.Field> modifiedFields) throws OXException {
        final File orig = getFileMetadata(source.getFolder(), source.getId(), CURRENT_VERSION);
        InputStream in = newFile;
        if (in == null && orig.getFileName() != null) {
            in = getDocument(source.getFolder(), source.getId(), CURRENT_VERSION);
        }
        if (update != null) {
            orig.copyFrom(update, modifiedFields.toArray(new File.Field[modifiedFields.size()]));
        }
        orig.setId(NEW);
        orig.setFolderId(destFolder);

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

    public InfostoreFacade getInfostore(final String folderId) {
        if (folderId != null && VIRTUAL_FOLDERS.contains(Long.valueOf(folderId))) {
            return VIRTUAL_INFOSTORE;
        }
        return infostore;
    }

}
