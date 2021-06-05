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

package com.openexchange.file.storage.infostore.internal;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentAndMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.Results;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

public class VirtualFolderInfostoreFacade implements InfostoreFacade {

    @Override
    public int countDocuments(final long folderId, ServerSession session) {
        return 0;
    }

    @Override
    public long getTotalSize(long folderId, ServerSession session) throws OXException {
        return 0;
    }

    @Override
    public boolean exists(final int id, final int version, ServerSession session) {
        return false;
    }

    @Override
    public Delta<DocumentMetadata> getDelta(final long folderId, final long updateSince, final Metadata[] columns, final boolean ignoreDeleted, ServerSession session) throws OXException {
        return Results.emptyDelta();
    }

    @Override
    public Delta<DocumentMetadata> getDelta(final long folderId, final long updateSince, final Metadata[] columns, final Metadata sort, final int order, final boolean ignoreDeleted, ServerSession session) throws OXException {
        return Results.emptyDelta();
    }

    @Override
    public Map<Long, Long> getSequenceNumbers(List<Long> folderIds, boolean versionsOnly, ServerSession session) throws OXException {
        Map<Long, Long> sequenceNumbers = new HashMap<Long, Long>(folderIds.size());
        for (Long folderID : folderIds) {
            sequenceNumbers.put(folderID, Long.valueOf(0));
        }
        return sequenceNumbers;
    }

    @Override
    public InputStream getDocument(final int id, final int version, ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public DocumentAndMetadata getDocumentAndMetadata(int id, int version, String clientETag, ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public DocumentAndMetadata getDocumentAndMetadata(long folderId, int id, int version, String clientETag, ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public InputStream getDocument(int id, int version, long offset, long length, ServerSession sessions) throws OXException {
        throw virtualFolder();
    }

    @Override
    public DocumentMetadata getDocumentMetadata(final int id, final int version, ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public DocumentMetadata getDocumentMetadata(long folderId, int id, int version, ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public DocumentMetadata getDocumentMetadata(int id, int version, Context context) throws OXException {
        throw virtualFolder();
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, ServerSession session) {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, final Metadata[] columns, ServerSession session) {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, final Metadata[] columns, final Metadata sort, final int order, ServerSession session) {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final List<IDTuple> ids, final Metadata[] columns, ServerSession session) {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<DocumentMetadata> getUserSharedDocuments(Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final int id, ServerSession session) {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final long folderId, final int id, final Metadata[] columns, ServerSession session) {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final long folderId, final int id, final Metadata[] columns, final Metadata sort, final int order, ServerSession session) {
        return Results.emptyTimedResult();
    }

    @Override
    public boolean hasFolderForeignObjects(final long folderId, ServerSession session) {
        return false;
    }

    @Override
    public boolean isFolderEmpty(final long folderId, final Context ctx) {
        return true;
    }

    @Override
    public void lock(final int id, final long diff, final ServerSession session) throws OXException {
        virtualFolder();
    }

    @Override
    public void removeDocument(final long folderId, final long date, final ServerSession session, boolean hardDelete) throws OXException {
        virtualFolder();
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long date, final ServerSession session, boolean hardDelete) {
        return ids;
    }

    @Override
    public IDTuple copyDocument(ServerSession session, IDTuple id, int version, DocumentMetadata update, Metadata[] modifiedFields, InputStream newFile, long sequenceNumber, String targetFolderID) throws OXException {
        throw virtualFolder();
    }

    @Override
    public List<IDTuple> moveDocuments(ServerSession session, List<IDTuple> ids, long sequenceNumber, String targetFolderID, boolean adjustFilenamesAsNeeded) throws OXException {
        return ids;
    }

    @Override
    public void removeDocuments(List<IDTuple> ids, Context context) throws OXException {
        virtualFolder();
    }

    @Override
    public void removeUser(final int userId, final Context context, final Integer destUser, final ServerSession session) {
        // Nothing to do.
    }

    @Override
    public int[] removeVersion(final int id, final int[] versionIds, final ServerSession session) {
        return versionIds;
    }

    @Override
    public IDTuple saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public IDTuple saveDocumentTryAddVersion(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final Metadata[] modifiedColumns, final ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public IDTuple saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final Metadata[] modifiedColumns, final ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, long offset, ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public IDTuple saveDocumentMetadata(final DocumentMetadata document, final long sequenceNumber, final ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, Context context) throws OXException {
        throw virtualFolder();
    }

    @Override
    public IDTuple saveDocumentMetadata(final DocumentMetadata document, final long sequenceNumber, final Metadata[] modifiedColumns, final ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public IDTuple saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final Metadata[] modifiedColumns, final boolean ignoreVersion, final ServerSession session) throws OXException {
        throw virtualFolder();
    }

    @Override
    public void unlock(final int id, final ServerSession session) {
        // Nothing to do.
    }

    @Override
    public void commit() {
        // Nothing to do.
    }

    @Override
    public void finish() {
        // Nothing to do.
    }

    @Override
    public void rollback() {
        // Nothing to to.
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // Nothing to to.
    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        // Nothing to to.
    }

    @Override
    public void setTransactional(final boolean transactional) {
        // Nothing to to.
    }

    @Override
    public void startTransaction() {
        // Nothing to to.
    }

    private OXException virtualFolder() throws OXException{
        throw InfostoreExceptionCodes.NO_DOCUMENTS_IN_VIRTUAL_FOLDER.create();
    }

    @Override
    public void touch(final int id, final ServerSession session) throws OXException {
        virtualFolder();
    }

    @Override
    public void setSessionHolder(final SessionHolder sessionHolder) {
        // Nothing to do.
    }

    @Override
    public Quota getFileQuota(ServerSession session) throws OXException {
        return Quota.getUnlimitedQuota(Type.FILE);
    }

    @Override
    public Quota getStorageQuota(ServerSession session) throws OXException {
        return Quota.getUnlimitedQuota(Type.STORAGE);
    }

    @Override
    public Quota getFileQuota(long folderId, ServerSession session) throws OXException {
        return Quota.getUnlimitedQuota(Type.FILE);
    }

    @Override
    public Quota getStorageQuota(long folderId, ServerSession session) throws OXException {
        return Quota.getUnlimitedQuota(Type.STORAGE);
    }

    @Override
    public boolean exists(int id, int version, Context context) throws OXException {
        return false;
    }

    @Override
    public boolean hasDocumentAccess(int id, AccessPermission permission, User user, Context context) throws OXException {
        return false;
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, int start, int end, Context context, User user, UserPermissionBits permissionBits) throws OXException {
        return Results.emptyTimedResult();
    }

    @Override
    public void touch(int id, Context context) throws OXException {
        //
    }

    @Override
    public Map<IDTuple, String> restore(List<IDTuple> toRestore, long destFolderId, ServerSession session) throws OXException {
        LinkedHashMap<IDTuple, String> ret = new LinkedHashMap<IDTuple, String>(toRestore.size());
        for (IDTuple t : toRestore) {
            ret.put(t, String.valueOf(destFolderId));
        }
        return ret;
    }
}
