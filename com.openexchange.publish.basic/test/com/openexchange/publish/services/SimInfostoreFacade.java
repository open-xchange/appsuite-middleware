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

package com.openexchange.publish.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.SaveResult;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentAndMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tx.TransactionException;


/**
 * {@link SimInfostoreFacade}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimInfostoreFacade implements InfostoreFacade {

    private final Map<Integer, byte[]> files = new HashMap<Integer, byte[]>();

    @Override
    public int countDocuments(long folderId, ServerSession session) throws OXException {
        // Nothing to do
        return 0;
    }

    @Override
    public long getTotalSize(long folderId, ServerSession session) throws OXException {
        return 0;
    }

    @Override
    public boolean exists(int id, int version, ServerSession session) throws OXException {
        // Nothing to do
        return false;
    }

    @Override
    public Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, boolean ignoreDeleted, ServerSession session) throws OXException, SearchIteratorException, OXException {
        // Nothing to do
        return null;
    }

    @Override
    public Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, Metadata sort, int order, boolean ignoreDeleted, ServerSession session) throws OXException, SearchIteratorException {
        // Nothing to do
        return null;
    }

    @Override
    public InputStream getDocument(int id, int version, ServerSession session) throws OXException {
        return new ByteArrayInputStream(files.get(id));
    }

    @Override
    public DocumentMetadata getDocumentMetadata(int id, int version, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(List<IDTuple> ids, Metadata[] columns, ServerSession session) throws IllegalAccessException, OXException {
        // Nothing to do
        return null;
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(int id, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(int id, Metadata[] columns, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(int id, Metadata[] columns, Metadata sort, int order, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public boolean hasFolderForeignObjects(long folderId, ServerSession session) throws OXException {
        // Nothing to do
        return false;
    }

    @Override
    public boolean isFolderEmpty(long folderId, Context ctx) throws OXException {
        // Nothing to do
        return false;
    }

    @Override
    public void lock(int id, long diff, ServerSession session) throws OXException {
        // Nothing to do
    }

    @Override
    public void removeDocument(long folderId, long date, ServerSession session) throws OXException {
        // Nothing to do
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long date, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public void removeUser(int userId, Context context, final Integer destUser, ServerSession session) throws OXException {
        // Nothing to do
    }

    @Override
    public int[] removeVersion(int id, int[] versionIds, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public SaveResult saveDocumentTryAddVersion(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, boolean ignoreVersion, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public void setSessionHolder(SessionHolder sessionHolder) {
        // Nothing to do
    }

    @Override
    public void touch(int id, ServerSession session) throws OXException {
        // Nothing to do
    }

    @Override
    public void unlock(int id, ServerSession session) throws OXException {
        // Nothing to do
    }

    @Override
    public void commit() throws TransactionException {
        // Nothing to do
    }

    @Override
    public void finish() throws TransactionException {
        // Nothing to do
    }

    @Override
    public void rollback() throws TransactionException {
        // Nothing to do
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        // Nothing to do
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        // Nothing to do
    }

    @Override
    public void setTransactional(boolean transactional) {
        // Nothing to do
    }

    @Override
    public void startTransaction() throws TransactionException {
        // Nothing to do
    }

    public void simulateDocument(int cid, int folder, int id, String string, byte[] bytes) {
        files.put(id, bytes);
    }

    @Override
    public Map<Long, Long> getSequenceNumbers(List<Long> folderIds, boolean versionsOnly, ServerSession session) throws OXException {
        // Nothing to do
        return null;
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
    public InputStream getDocument(int id, int version, long offset, long length, ServerSession session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, long offset, ServerSession session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public List<IDTuple> moveDocuments(ServerSession session, List<IDTuple> ids, long sequenceNumber, String targetFolderID, boolean adjustFilenamesAsNeeded) throws OXException {
        return ids;
    }

    @Override
    public DocumentMetadata getDocumentMetadata(int id, int version, Context context) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, Context context) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeDocuments(List<IDTuple> ids, Context context) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public DocumentAndMetadata getDocumentAndMetadata(int id, int version, String clientETag, ServerSession session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedResult<DocumentMetadata> getUserSharedDocuments(Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException {
        // TODO Auto-generated method stub
        return null;
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
        return null;
    }

    @Override
    public DocumentMetadata getDocumentMetadata(long folderId, int id, int version, ServerSession session) throws OXException {
        return null;
    }

    @Override
    public DocumentAndMetadata getDocumentAndMetadata(long folderId, int id, int version, String clientETag, ServerSession session) throws OXException {
        return null;
    }

    @Override
    public void touch(int id, Context context) throws OXException {
        //
    }

}
