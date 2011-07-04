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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.infostore.internal.VirtualFolderInfostoreFacade;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.groupware.AbstractOXException;
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
import com.openexchange.tx.TransactionException;

/**
 * {@link InfostoreAdapterFileAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreAdapterFileAccess implements FileStorageFileAccess {

    private static final InfostoreFacade VIRTUAL_INFOSTORE = new VirtualFolderInfostoreFacade();
    private static final Set<Long> VIRTUAL_FOLDERS = new HashSet<Long>() {

        {
            add((long) FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID);
            add((long) FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
            add((long) FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
            add((long) FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
        }
    };

    private InfostoreFacade infostore;
    private InfostoreSearchEngine search;
    private Context ctx;
    private User user;
    private UserConfiguration userConfig;
    private ServerSession sessionObj;
    private FileStorageAccountAccess accountAccess;

    /**
     * Initializes a new {@link InfostoreAdapterFileAccess}.
     * @param session
     * @param infostore2
     */
    public InfostoreAdapterFileAccess(ServerSession session, InfostoreFacade infostore, InfostoreSearchEngine search, FileStorageAccountAccess accountAccess) {
        this.sessionObj = session;

        this.ctx = sessionObj.getContext();
        this.user = sessionObj.getUser();
        this.userConfig = sessionObj.getUserConfiguration();

        this.infostore = infostore;
        this.search = search;
        this.accountAccess = accountAccess;
    }


    public boolean exists(String folderId, String id, int version) throws OXException {
        try {
            return getInfostore(folderId).exists( ID(id), version, ctx, user, userConfig);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public InputStream getDocument(String folderId, String id, int version) throws OXException {
        try {
            return getInfostore(folderId).getDocument(ID( id ), version, ctx, user, userConfig);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public File getFileMetadata(String folderId, String id, int version) throws OXException {
        try {
            DocumentMetadata documentMetadata = getInfostore(folderId).getDocumentMetadata(ID( id ), version, ctx, user, userConfig);
            return new InfostoreFile( documentMetadata );
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public void lock(String folderId, String id, long diff) throws OXException {
        try {
            getInfostore(folderId).lock(ID( id ), diff, sessionObj);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        try {
            getInfostore(folderId).removeDocument(FOLDERID(folderId), sequenceNumber, sessionObj);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        int[] infostoreIDs = new int[ids.size()];
        Map<Integer, IDTuple> id2folder = new HashMap<Integer, IDTuple>();
        for(int i = 0; i < infostoreIDs.length; i++) {
            IDTuple tuple = ids.get(i);
            infostoreIDs[i] = ID( tuple.getId() );
            id2folder.put(infostoreIDs[i], tuple);
        }

        try {
            int[] conflicted = getInfostore(null).removeDocument(infostoreIDs, sequenceNumber, sessionObj);

            List<IDTuple> retval = new ArrayList<IDTuple>(conflicted.length);
            for(int id : conflicted) {
                retval.add(id2folder.get(id));
            }

            return retval;

        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public int[] removeVersion(String folderId, String id, int[] versions) throws OXException {
        try {
            return getInfostore(folderId).removeVersion(ID(id), versions, sessionObj);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public void saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        try {
            getInfostore(file.getFolderId()).saveDocument(new FileMetadata(file), data, sequenceNumber, sessionObj);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public void saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        try {
            getInfostore(file.getFolderId()).saveDocument(new FileMetadata(file), data, sequenceNumber, FieldMapping.getMatching(modifiedFields), sessionObj );
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public void saveFileMetadata(File file, long sequenceNumber) throws OXException {
        try {
            getInfostore(file.getFolderId()).saveDocumentMetadata(new FileMetadata(file), sequenceNumber, sessionObj);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public void saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        try {
            getInfostore(file.getFolderId()).saveDocumentMetadata(new FileMetadata(file), sequenceNumber, FieldMapping.getMatching(modifiedFields), sessionObj);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public void touch(String folderId, String id) throws OXException {
        try {
            getInfostore(folderId).touch(ID(id), sessionObj);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public void unlock(String folderId, String id) throws OXException {
        try {
            getInfostore(folderId).unlock(ID(id), sessionObj);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        try {
            Delta<DocumentMetadata> delta = getInfostore(folderId).getDelta(FOLDERID(folderId), updateSince, FieldMapping.getMatching(fields), ignoreDeleted, ctx, user, userConfig);
            return new InfostoreDeltaWrapper(delta);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        try {
            Delta<DocumentMetadata> delta = getInfostore(folderId).getDelta(FOLDERID(folderId), updateSince, FieldMapping.getMatching(fields), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), ignoreDeleted, ctx, user, userConfig);
            return new InfostoreDeltaWrapper(delta);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public TimedResult<File> getDocuments(String folderId) throws OXException {
        try {
            TimedResult<DocumentMetadata> documents = getInfostore(folderId).getDocuments(FOLDERID(folderId), ctx, user, userConfig);
            return new InfostoreTimedResult(documents);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        try {
            TimedResult<DocumentMetadata> documents = getInfostore(folderId).getDocuments(FOLDERID(folderId), FieldMapping.getMatching(fields), ctx, user, userConfig);
            return new InfostoreTimedResult(documents);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        try {
            TimedResult<DocumentMetadata> documents = getInfostore(folderId).getDocuments(FOLDERID(folderId), FieldMapping.getMatching(fields), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), ctx, user, userConfig);
            return new InfostoreTimedResult(documents);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        int[] infostoreIDs = IDS(ids);
        TimedResult<DocumentMetadata> documents;
        try {
            documents = getInfostore(null).getDocuments(infostoreIDs, FieldMapping.getMatching(fields), ctx, user, userConfig);
            return new InfostoreTimedResult(documents);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        } catch (IllegalAccessException e) {
            throw new OXException(new AbstractOXException(e));
        }
    }


    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        try {
            TimedResult<DocumentMetadata> versions = getInfostore(folderId).getVersions(ID(id), ctx, user, userConfig);
            return new InfostoreTimedResult(versions);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        try {
            TimedResult<DocumentMetadata> versions = getInfostore(folderId).getVersions(ID(id), FieldMapping.getMatching(fields), ctx, user, userConfig);
            return new InfostoreTimedResult(versions);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        try {
            TimedResult<DocumentMetadata> versions = getInfostore(folderId).getVersions(ID(id), FieldMapping.getMatching(fields), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), ctx, user, userConfig);
            return new InfostoreTimedResult(versions);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }


    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        int folder = (folderId == null) ? InfostoreSearchEngine.NO_FOLDER : Integer.parseInt(folderId);
        try {
            SearchIterator<DocumentMetadata> iterator = search.search(pattern, FieldMapping.getMatching(fields), folder, FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), start, end, ctx, user, userConfig);
            return new InfostoreSearchIterator(iterator);
        } catch (AbstractOXException e) {
            throw new OXException(e);
        }
    }



    public void commit() throws TransactionException {
        infostore.commit();
    }


    public void finish() throws TransactionException {
        infostore.finish();
    }


    public void rollback() throws TransactionException {
        infostore.rollback();
    }


    public void setCommitsTransaction(boolean commits) {
        infostore.setCommitsTransaction(commits);
    }


    public void setRequestTransactional(boolean transactional) {
        infostore.setRequestTransactional(transactional);
    }


    public void setTransactional(boolean transactional) {
        infostore.setTransactional(transactional);
    }


    public void startTransaction() throws TransactionException {
        infostore.startTransaction();
    }

    private static int ID(String id) {
        return Integer.parseInt(id);
    }

    private static long FOLDERID(String folderId) {
        return Long.parseLong(folderId);
    }

    private static int[] IDS(List<IDTuple> ids) {
        int[] infostoreIDs = new int[ids.size()];
        for(int i = 0; i < ids.size(); i++) {
           infostoreIDs[i] = ID(ids.get(i).getId());
        }
        return infostoreIDs;
    }


    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }


    public IDTuple copy(IDTuple source, String destFolder, File update, InputStream newFile, List<File.Field> modifiedFields) throws OXException {
        File orig = getFileMetadata(source.getFolder(), source.getId(), CURRENT_VERSION);
        if(newFile == null && orig.getFileName() != null) {
            newFile = getDocument(source.getFolder(), source.getId(), CURRENT_VERSION);
        }
        if(update != null) {
            orig.copyFrom(update, modifiedFields.toArray(new File.Field[modifiedFields.size()]));
        }
        orig.setId(NEW);
        orig.setFolderId(destFolder);

        if(newFile == null) {
            saveFileMetadata(orig, UNDEFINED_SEQUENCE_NUMBER);
        } else {
            saveDocument(orig, newFile, UNDEFINED_SEQUENCE_NUMBER);
        }

        return new IDTuple(destFolder, orig.getId());
    }


    public InfostoreFacade getInfostore(String folderId) {
        if(folderId != null && VIRTUAL_FOLDERS.contains(Long.parseLong(folderId))) {
            return VIRTUAL_INFOSTORE;
        }
        return infostore;
    }


}
