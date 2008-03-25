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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.groupware.infostore.database;

import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.Undoable;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.ldap.User;
import com.openexchange.api2.OXException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;

import java.util.*;
import java.io.InputStream;
import java.sql.Connection;


/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class InMemoryInfostoreDatabase extends DatabaseImpl {

    private Map<Context,Map<Integer, List<DocumentMetadata>>> data = new HashMap<Context, Map<Integer, List<DocumentMetadata>>>();

    public void put(Context ctx, DocumentMetadata dm) {
        List<DocumentMetadata> versions = getVersions(ctx,dm.getId());
        assureSize(versions, dm.getVersion());
        versions.set(dm.getVersion(), dm);
    }

    private void assureSize(List<DocumentMetadata> versions, int index) {
        while(index >= versions.size()) {
            versions.add(null);
        }
    }

    public int getNextVersionNumber(Context ctx, int id) {
        return getVersions(ctx, id).size();
    }

    private List<DocumentMetadata> getVersions(Context ctx, int id) {
        Map<Integer, List<DocumentMetadata>> ctxMap = getCtxMap(ctx);
        if(ctxMap.containsKey(id)) {
            return ctxMap.get(id);
        }
        List<DocumentMetadata> dms = new ArrayList<DocumentMetadata>();
        ctxMap.put(id, dms);
        return dms;
    }

    private Map<Integer, List<DocumentMetadata>> getCtxMap(Context ctx) {
        if(data.containsKey(ctx)) {
            return data.get(ctx);
        }
        Map<Integer, List<DocumentMetadata>> versions = new HashMap<Integer, List<DocumentMetadata>>();
        data.put(ctx, versions);
        return versions;
    }

    public boolean exists(int id, int version, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public DocumentMetadata getDocumentMetadata(int id, int version, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public List<DocumentMetadata> getAllVersions(Context ctx, Metadata[] columns, String where) throws OXException {
        throw new UnsupportedOperationException();
    }

    public List<DocumentMetadata> getAllDocuments(Context ctx, Metadata[] columns, String where) throws OXException {
        throw new UnsupportedOperationException();
    }

    public InputStream getDocument(int id, int version, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public Set<Integer> removeDocuments(Set<Integer> ids, long date, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public int[] removeDocument(String identifier, Context ctx) throws OXException {
        throw new UnsupportedOperationException();
    }

    public int[] removeDelDocument(String identifier, Context ctx) throws OXException {
        throw new UnsupportedOperationException();
    }

    public int modifyDocument(String oldidentifier, String newidentifier, String description, String mimetype, Context ctx) throws OXException {
        throw new UnsupportedOperationException();
    }

    public int modifyDelDocument(String oldidentifier, String newidentifier, String description, String mimetype, Context ctx) throws OXException {
        throw new UnsupportedOperationException();
    }

    public int[] saveDocumentMetadata(String identifier, DocumentMetadata document, User user, Context ctx) throws OXException {
        throw new UnsupportedOperationException();
    }

    public List<Integer> removeVersion(int id, int[] versionId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public TimedResult getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, boolean onlyOwnObjects, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public SortedSet<String> getDocumentFileStoreLocationsperContext(Context ctx) throws OXException {
       SortedSet<String> locations = new TreeSet<String>();
        for(List<DocumentMetadata> versions : getCtxMap(ctx).values()) {
            for(DocumentMetadata metadata : versions) {
                String location = metadata.getFilestoreLocation();
                if(location != null) {
                    locations.add(location);
                }
            }
        }
        return locations;
    }

    public SortedSet<String> getDelDocumentFileStoreLocationsperContext(Context ctx) throws OXException {
        throw new UnsupportedOperationException();
    }

    public TimedResult getVersions(int id, Metadata[] columns, Metadata sort, int order, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public TimedResult getDocuments(int[] ids, Metadata[] columns, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public Delta getDelta(long folderId, long updateSince, Metadata[] columns, Metadata sort, int order, boolean onlyOwnObjects, boolean ignoreDeleted, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public int countDocuments(long folderId, boolean onlyOwnObjects, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public int countDocumentsperContext(Context ctx) throws OXException {
        throw new UnsupportedOperationException();
    }

    public boolean hasFolderForeignObjects(long folderId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public boolean isFolderEmpty(long folderId, Context ctx) throws OXException {
        throw new UnsupportedOperationException();
    }

    public void removeUser(int id, Context ctx, ServerSession session, EntityLockManager locks) throws OXException {
        throw new UnsupportedOperationException();
    }

    protected FileStorage getFileStorage(Context ctx) throws FileStorageException, FilestoreException {
        throw new UnsupportedOperationException();
    }

    public void startTransaction() throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public void commit() throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public void finish() throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public void rollback() throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public int getMaxActiveVersion(int id, Context context) throws OXException {
        throw new UnsupportedOperationException();
    }

    public DBProvider getProvider() {
        throw new UnsupportedOperationException();
    }

    public void setProvider(DBProvider provider) {
        //IGNORE
    }

    public Connection getReadConnection(Context ctx) throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public Connection getWriteConnection(Context ctx) throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public void releaseReadConnection(Context ctx, Connection con) {
        throw new UnsupportedOperationException();
    }

    public void releaseWriteConnection(Context ctx, Connection con) {
        throw new UnsupportedOperationException();
    }

    public void commitDBTransaction() throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public void commitDBTransaction(Undoable undo) throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public void rollbackDBTransaction() throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public void startDBTransaction() throws TransactionException {
        throw new UnsupportedOperationException();
        }
}
