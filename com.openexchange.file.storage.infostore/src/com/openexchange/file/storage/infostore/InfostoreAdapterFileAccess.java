package com.openexchange.file.storage.infostore;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.groupware.AbstractOXException;
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

/**
 * {@link InfostoreAdapterFileAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreAdapterFileAccess implements FileStorageFileAccess {

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

    @Override
    public boolean exists(String folder, String id, int version) throws FileStorageException {
        try {
            return infostore.exists( ID(id), version, ctx, user, userConfig);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public InputStream getDocument(String folder, String id, int version) throws FileStorageException {
        try {
            return infostore.getDocument(ID( id ), version, ctx, user, userConfig);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public File getFileMetadata(String folder, String id, int version) throws FileStorageException {
        try {
            DocumentMetadata documentMetadata = infostore.getDocumentMetadata(ID( id ), version, ctx, user, userConfig);
            return new InfostoreFile( documentMetadata ); 
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }
    
    @Override
    public void lock(String folder, String id, long diff) throws FileStorageException {
        try {
            infostore.lock(ID( id ), diff, sessionObj);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws FileStorageException {
        try {
            infostore.removeDocument(FOLDERID(folderId), sequenceNumber, sessionObj);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws FileStorageException {
        int[] infostoreIDs = new int[ids.size()];
        Map<Integer, IDTuple> id2folder = new HashMap<Integer, IDTuple>();
        for(int i = 0; i < infostoreIDs.length; i++) {
            IDTuple tuple = ids.get(i);
            infostoreIDs[i] = ID( tuple.getId() );
            id2folder.put(infostoreIDs[i], tuple);
        }
        
        try {
            int[] conflicted = infostore.removeDocument(infostoreIDs, sequenceNumber, sessionObj);
            
            List<IDTuple> retval = new ArrayList<IDTuple>(conflicted.length);
            for(int id : conflicted) {
                retval.add(id2folder.get(id));
            }
            
            return retval;
            
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public int[] removeVersion(String folder, String id, int[] versions) throws FileStorageException {
        try {
            return infostore.removeVersion(ID(id), versions, sessionObj);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void saveDocument(File document, InputStream data, long sequenceNumber) throws FileStorageException {
        try {
            infostore.saveDocument(new FileMetadata(document), data, sequenceNumber, sessionObj);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void saveDocument(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns) throws FileStorageException {
        try {
            infostore.saveDocument(new FileMetadata(document), data, sequenceNumber, FieldMapping.getMatching(modifiedColumns), sessionObj );
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void saveFileMetadata(File document, long sequenceNumber) {
        try {
            infostore.saveDocumentMetadata(new FileMetadata(document), sequenceNumber, sessionObj);
        } catch (AbstractOXException e) {
            new FileStorageException(e);
        }
    }

    @Override
    public void saveFileMetadata(File document, long sequenceNumber, List<Field> modifiedColumns) throws FileStorageException {
        try {
            infostore.saveDocumentMetadata(new FileMetadata(document), sequenceNumber, FieldMapping.getMatching(modifiedColumns), sessionObj);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void touch(String folder, String id) throws FileStorageException {
        try {
            infostore.touch(ID(id), sessionObj);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void unlock(String folder, String id) throws FileStorageException {
        try {
            infostore.unlock(ID(id), sessionObj);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> columns, boolean ignoreDeleted) throws FileStorageException {
        try {
            Delta<DocumentMetadata> delta = infostore.getDelta(FOLDERID(folderId), updateSince, FieldMapping.getMatching(columns), ignoreDeleted, ctx, user, userConfig);
            return new InfostoreDeltaWrapper(delta);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> columns, Field sort, SortDirection order, boolean ignoreDeleted) throws FileStorageException {
        try {
            Delta<DocumentMetadata> delta = infostore.getDelta(FOLDERID(folderId), updateSince, FieldMapping.getMatching(columns), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), ignoreDeleted, ctx, user, userConfig);
            return new InfostoreDeltaWrapper(delta);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws FileStorageException {
        try {
            TimedResult<DocumentMetadata> documents = infostore.getDocuments(FOLDERID(folderId), ctx, user, userConfig);
            return new InfostoreTimedResult(documents);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> columns) throws FileStorageException {
        try {
            TimedResult<DocumentMetadata> documents = infostore.getDocuments(FOLDERID(folderId), FieldMapping.getMatching(columns), ctx, user, userConfig);
            return new InfostoreTimedResult(documents);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> columns, Field sort, SortDirection order) throws FileStorageException {
        try {
            TimedResult<DocumentMetadata> documents = infostore.getDocuments(FOLDERID(folderId), FieldMapping.getMatching(columns), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), ctx, user, userConfig);
            return new InfostoreTimedResult(documents);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> columns) throws FileStorageException {
        int[] infostoreIDs = IDS(ids);
        TimedResult<DocumentMetadata> documents;
        try {
            documents = infostore.getDocuments(infostoreIDs, FieldMapping.getMatching(columns), ctx, user, userConfig);
            return new InfostoreTimedResult(documents);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        } catch (IllegalAccessException e) {
            throw new FileStorageException(new AbstractOXException(e));
        }
    }

    @Override
    public TimedResult<File> getVersions(String folder, String id) throws FileStorageException {
        try {
            TimedResult<DocumentMetadata> versions = infostore.getVersions(ID(id), ctx, user, userConfig);
            return new InfostoreTimedResult(versions);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public TimedResult<File> getVersions(String folder, String id, List<Field> columns) throws FileStorageException {
        try {
            TimedResult<DocumentMetadata> versions = infostore.getVersions(ID(id), FieldMapping.getMatching(columns), ctx, user, userConfig);
            return new InfostoreTimedResult(versions);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public TimedResult<File> getVersions(String folder, String id, List<Field> columns, Field sort, SortDirection order) throws FileStorageException {
        try {
            TimedResult<DocumentMetadata> versions = infostore.getVersions(ID(id), FieldMapping.getMatching(columns), FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), ctx, user, userConfig);
            return new InfostoreTimedResult(versions);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }
    
    @Override
    public SearchIterator<File> search(String query, List<Field> cols, String folderId, Field sort, SortDirection order, int start, int end) throws FileStorageException {
        int folder = (folderId == null) ? InfostoreSearchEngine.NO_FOLDER : Integer.parseInt(folderId);
        try {
            SearchIterator<DocumentMetadata> iterator = search.search(query, FieldMapping.getMatching(cols), folder, FieldMapping.getMatching(sort), FieldMapping.getSortDirection(order), start, end, ctx, user, userConfig);
            return new InfostoreSearchIterator(iterator);
        } catch (AbstractOXException e) {
            throw new FileStorageException(e);
        }
    }


    @Override
    public void commit() throws TransactionException {
        infostore.commit();
    }

    @Override
    public void finish() throws TransactionException {
        infostore.finish();
    }

    @Override
    public void rollback() throws TransactionException {
        infostore.rollback();
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        infostore.setCommitsTransaction(commits);
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        infostore.setRequestTransactional(transactional);
    }

    @Override
    public void setTransactional(boolean transactional) {
        infostore.setTransactional(transactional);
    }

    @Override
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

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }


}
