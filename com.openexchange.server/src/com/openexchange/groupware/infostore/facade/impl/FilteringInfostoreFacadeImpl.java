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

package com.openexchange.groupware.infostore.facade.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.openexchange.config.admin.HideAdminService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.Quota;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentAndMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.infostore.search.SearchTerm;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link FilteringInfostoreFacadeImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class FilteringInfostoreFacadeImpl implements InfostoreFacade, InfostoreSearchEngine {

    private final InfostoreFacadeImpl delegate;
    private final ServiceLookup services;

    public FilteringInfostoreFacadeImpl(InfostoreFacadeImpl delegate, ServiceLookup services) {
        super();
        this.delegate = delegate;
        this.services = services;
    }

    @Override
    public void startTransaction() throws OXException {
        delegate.startTransaction();
    }

    @Override
    public void commit() throws OXException {
        delegate.commit();
    }

    @Override
    public void rollback() throws OXException {
        delegate.rollback();
    }

    @Override
    public void finish() throws OXException {
        delegate.finish();
    }

    @Override
    public void setTransactional(boolean transactional) {
        delegate.setTransactional(transactional);
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        delegate.setRequestTransactional(transactional);
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        delegate.setCommitsTransaction(commits);
    }

    @Override
    public boolean exists(int id, int version, ServerSession session) throws OXException {
        return delegate.exists(id, version, session);
    }

    @Override
    public boolean exists(int id, int version, Context context) throws OXException {
        return delegate.exists(id, version, context);
    }

    @Override
    public DocumentMetadata getDocumentMetadata(int id, int version, ServerSession session) throws OXException {
        DocumentMetadata documentMetadata = delegate.getDocumentMetadata(id, version, session);
        return removeAdminFromObjectPermissions(session.getContextId(), documentMetadata);
    }

    @Override
    public DocumentMetadata getDocumentMetadata(long folderId, int id, int version, ServerSession session) throws OXException {
        DocumentMetadata documentMetadata = delegate.getDocumentMetadata(folderId, id, version, session);
        return removeAdminFromObjectPermissions(session.getContextId(), documentMetadata);
    }

    @Override
    public DocumentMetadata getDocumentMetadata(int id, int version, Context context) throws OXException {
        DocumentMetadata documentMetadata = getOriginalDocumentMetadata(id, version, context);
        return removeAdminFromObjectPermissions(context.getContextId(), documentMetadata);
    }

    public DocumentMetadata getOriginalDocumentMetadata(int id, int version, Context context) throws OXException {
        return delegate.getDocumentMetadata(id, version, context);
    }

    @Override
    public IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, ServerSession session) throws OXException {
        addAdminPermission(document, session.getContext());
        return delegate.saveDocumentMetadata(document, sequenceNumber, session);
    }

    @Override
    public IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException {
        addAdminPermission(document, session.getContext());
        return delegate.saveDocumentMetadata(document, sequenceNumber, modifiedColumns, session);
    }

    private void addAdminPermission(DocumentMetadata document, Context context) throws OXException {
        if (null == document.getObjectPermissions() || InfostoreFacade.NEW == document.getId()) {
            return; // not needed without 'set' object permissions or for newly created documents
        }
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (null == hideAdminService || false == hideAdminService.showAdmin(context.getContextId())) {
            return; // currently not available or not applicable in this context
        }
        List<ObjectPermission> originalObjectPermissions = delegate.objectPermissionLoader.load(document.getId(), context);
        document.setObjectPermissions(hideAdminService.addAdminToObjectPermissions(context.getContextId(), originalObjectPermissions, document.getObjectPermissions()));
    }

    @Override
    public IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, Context context) throws OXException {
        addAdminPermission(document, context);
        return delegate.saveDocumentMetadata(document, sequenceNumber, modifiedColumns, context);
    }

    @Override
    public InputStream getDocument(int id, int version, ServerSession session) throws OXException {
        return delegate.getDocument(id, version, session);
    }

    @Override
    public DocumentAndMetadata getDocumentAndMetadata(int id, int version, String clientETag, ServerSession session) throws OXException {
        DocumentAndMetadata documentAndMetadata = delegate.getDocumentAndMetadata(id, version, clientETag, session);
        removeAdminFromObjectPermissions(session.getContextId(), documentAndMetadata.getMetadata());
        return documentAndMetadata;
    }

    @Override
    public DocumentAndMetadata getDocumentAndMetadata(long folderId, int id, int version, String clientETag, ServerSession session) throws OXException {
        DocumentAndMetadata documentAndMetadata = delegate.getDocumentAndMetadata(folderId, id, version, clientETag, session);
        removeAdminFromObjectPermissions(session.getContextId(), documentAndMetadata.getMetadata());
        return documentAndMetadata;
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, ServerSession session) throws OXException {
        addAdminPermission(document, session.getContext());
        return delegate.saveDocument(document, data, sequenceNumber, session);
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException {
        addAdminPermission(document, session.getContext());
        return delegate.saveDocument(document, data, sequenceNumber, modifiedColumns, session);
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, boolean ignoreVersion, ServerSession session) throws OXException {
        addAdminPermission(document, session.getContext());
        return delegate.saveDocument(document, data, sequenceNumber, modifiedColumns, ignoreVersion, session);
    }

    @Override
    public IDTuple saveDocumentTryAddVersion(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException {
        addAdminPermission(document, session.getContext());
        return delegate.saveDocumentTryAddVersion(document, data, sequenceNumber, modifiedColumns, session);
    }

    @Override
    public void removeDocument(long folderId, long date, ServerSession session, boolean hardDelete) throws OXException {
        delegate.removeDocument(folderId, date, session, hardDelete);
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long date, ServerSession session, boolean hardDelete) throws OXException {
        return delegate.removeDocument(ids, date, session, hardDelete);
    }

    @Override
    public void removeDocuments(List<IDTuple> ids, Context context) throws OXException {
        delegate.removeDocuments(ids, context);
    }

    @Override
    public IDTuple copyDocument(ServerSession session, IDTuple id, int version, DocumentMetadata metaData, Metadata[] modifiedFields, InputStream newFile, long sequenceNumber, String targetFolderID) throws OXException {
        return delegate.copyDocument(session, id, version, metaData, modifiedFields, newFile, sequenceNumber, targetFolderID);
    }

    @Override
    public List<IDTuple> moveDocuments(ServerSession session, List<IDTuple> ids, long sequenceNumber, String targetFolderID, boolean adjustFilenamesAsNeeded) throws OXException {
        return delegate.moveDocuments(session, ids, sequenceNumber, targetFolderID, adjustFilenamesAsNeeded);
    }

    @Override
    public int[] removeVersion(int id, int[] versionIds, ServerSession session) throws OXException {
        return delegate.removeVersion(id, versionIds, session);
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getDocuments(folderId, session));
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getDocuments(folderId, columns, session));
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, int start, int end, Context context, User user, UserPermissionBits permissionBits) throws OXException {
        return removeAdminFromObjectPermissions(context.getContextId(), delegate.getDocuments(folderId, columns, sort, order, start, end, context, user, permissionBits));
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getDocuments(folderId, columns, sort, order, session));
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getDocuments(folderId, columns, sort, order, start, end, session));
    }

    @Override
    public TimedResult<DocumentMetadata> getUserSharedDocuments(Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getUserSharedDocuments(columns, sort, order, start, end, session));
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(int id, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getVersions(id, session));
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(long folderId, int id, Metadata[] columns, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getVersions(folderId, id, columns, session));
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(long folderId, int id, Metadata[] columns, Metadata sort, int order, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getVersions(folderId, id, columns, sort, order, session));
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(List<IDTuple> ids, Metadata[] columns, ServerSession session) throws IllegalAccessException, OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getDocuments(ids, columns, session));
    }

    @Override
    public Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, boolean ignoreDeleted, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getDelta(folderId, updateSince, columns, ignoreDeleted, session));
    }

    @Override
    public Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, Metadata sort, int order, boolean ignoreDeleted, ServerSession session) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.getDelta(folderId, updateSince, columns, sort, order, ignoreDeleted, session));
    }

    @Override
    public Map<Long, Long> getSequenceNumbers(List<Long> folderIds, boolean versionsOnly, ServerSession session) throws OXException {
        return delegate.getSequenceNumbers(folderIds, versionsOnly, session);
    }

    @Override
    public int countDocuments(long folderId, ServerSession session) throws OXException {
        return delegate.countDocuments(folderId, session);
    }

    @Override
    public long getTotalSize(long folderId, ServerSession session) throws OXException {
        return delegate.getTotalSize(folderId, session);
    }

    @Override
    public boolean hasFolderForeignObjects(long folderId, ServerSession session) throws OXException {
        return delegate.hasFolderForeignObjects(folderId, session);
    }

    @Override
    public boolean isFolderEmpty(long folderId, Context ctx) throws OXException {
        return delegate.isFolderEmpty(folderId, ctx);
    }

    @Override
    public void removeUser(int userId, Context context, Integer destUserID, ServerSession session) throws OXException {
        delegate.removeUser(userId, context, destUserID, session);
    }

    @Override
    public void unlock(int id, ServerSession session) throws OXException {
        delegate.unlock(id, session);
    }

    @Override
    public void lock(int id, long diff, ServerSession session) throws OXException {
        delegate.lock(id, diff, session);
    }

    @Override
    public void touch(int id, ServerSession session) throws OXException {
        delegate.touch(id, session);
    }

    @Override
    public void touch(int id, Context context) throws OXException {
        delegate.touch(id, context);
    }

    @Override
    public void setSessionHolder(SessionHolder sessionHolder) {
        delegate.setSessionHolder(sessionHolder);
    }

    @Override
    public Quota getFileQuota(long folderId, ServerSession session) throws OXException {
        return delegate.getFileQuota(folderId, session);
    }

    @Override
    public Quota getStorageQuota(long folderId, ServerSession session) throws OXException {
        return delegate.getStorageQuota(folderId, session);
    }

    @Override
    public Quota getFileQuota(ServerSession session) throws OXException {
        return delegate.getFileQuota(session);
    }

    @Override
    public Quota getStorageQuota(ServerSession session) throws OXException {
        return delegate.getStorageQuota(session);
    }

    @Override
    public InputStream getDocument(int id, int version, long offset, long length, ServerSession session) throws OXException {
        return delegate.getDocument(id, version, offset, length, session);
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, long offset, ServerSession session) throws OXException {
        addAdminPermission(document, session.getContext());
        return delegate.saveDocument(document, data, sequenceNumber, modifiedColumns, offset, session);
    }

    @Override
    public boolean hasDocumentAccess(int id, AccessPermission permission, User user, Context context) throws OXException {
        return delegate.hasDocumentAccess(id, permission, user, context);
    }

    @Override
    public Map<IDTuple, String> restore(List<IDTuple> toRestore, long destFolderId, ServerSession session) throws OXException {
        return delegate.restore(toRestore, destFolderId, session);
    }

    @Override
    public SearchIterator<DocumentMetadata> search(ServerSession session, String query, int folderId, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.search(session, query, folderId, cols, sortedBy, dir, start, end));
    }

    @Override
    public SearchIterator<DocumentMetadata> search(ServerSession session, String query, int folderId, boolean includeSubfolders, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.search(session, query, folderId, includeSubfolders, cols, sortedBy, dir, start, end));
    }

    @Override
    public SearchIterator<DocumentMetadata> search(ServerSession session, SearchTerm<?> searchTerm, int[] folderIds, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.search(session, searchTerm, folderIds, cols, sortedBy, dir, start, end));
    }

    @Override
    public SearchIterator<DocumentMetadata> search(ServerSession session, SearchTerm<?> searchTerm, int folderId, boolean includeSubfolders, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        return removeAdminFromObjectPermissions(session.getContextId(), delegate.search(session, searchTerm, folderId, includeSubfolders, cols, sortedBy, dir, start, end));
    }

    private DocumentMetadata removeAdminFromObjectPermissions(int contextId, DocumentMetadata documentMetadata) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return documentMetadata;
        }
        List<ObjectPermission> newPermissions = hideAdminService.removeAdminFromObjectPermissions(contextId, documentMetadata.getObjectPermissions());
        documentMetadata.setObjectPermissions(newPermissions);
        return documentMetadata;
    }

    private SearchIterator<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, SearchIterator<DocumentMetadata> searchIterator) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return searchIterator;
        }
        return hideAdminService.removeAdminFromObjectPermissions(contextId, searchIterator);
    }

    private TimedResult<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, TimedResult<DocumentMetadata> documents) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return documents;
        }
        return hideAdminService.removeAdminFromObjectPermissions(contextId, documents);
    }

    private Delta<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, Delta<DocumentMetadata> delta) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return delta;
        }
        return hideAdminService.removeAdminFromObjectPermissions(contextId, delta);
    }

}
