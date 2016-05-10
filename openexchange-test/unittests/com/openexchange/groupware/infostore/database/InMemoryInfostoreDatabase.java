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

package com.openexchange.groupware.infostore.database;

import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.session.ServerSession;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class InMemoryInfostoreDatabase extends DatabaseImpl {

    private final Map<Context, Map<Integer, List<DocumentMetadata>>> data = new HashMap<Context, Map<Integer, List<DocumentMetadata>>>();

    private final Map<Context, List<DocumentMetadata>> changes = new HashMap<Context, List<DocumentMetadata>>();

    private final Map<Context, List<DocumentMetadata>> deletions = new HashMap<Context, List<DocumentMetadata>>();

    private final Map<Context, List<DocumentMetadata>> creations = new HashMap<Context, List<DocumentMetadata>>();

    public void put(final Context ctx, final DocumentMetadata dm) {
        final List<DocumentMetadata> versions = getVersions(ctx, dm.getId());
        assureSize(versions, dm.getVersion());
        versions.set(dm.getVersion(), dm);
    }

    private void assureSize(final List<DocumentMetadata> versions, final int index) {
        while (index >= versions.size()) {
            versions.add(null);
        }
    }

    public int getNextVersionNumber(final Context ctx, final int id) {
        return getVersions(ctx, id).size();
    }

    private List<DocumentMetadata> getVersions(final Context ctx, final int id) {
        final Map<Integer, List<DocumentMetadata>> ctxMap = getCtxMap(ctx);
        if (ctxMap.containsKey(I(id))) {
            return ctxMap.get(I(id));
        }
        final List<DocumentMetadata> dms = new ArrayList<DocumentMetadata>();
        ctxMap.put(I(id), dms);
        return dms;
    }

    private Map<Integer, List<DocumentMetadata>> getCtxMap(final Context ctx) {
        if (data.containsKey(ctx)) {
            return data.get(ctx);
        }
        final Map<Integer, List<DocumentMetadata>> versions = new HashMap<Integer, List<DocumentMetadata>>();
        data.put(ctx, versions);
        return versions;
    }

    @Override
    public boolean exists(final int id, final int version, final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DocumentMetadata getDocumentMetadata(final int id, final int version, final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DocumentMetadata> getAllVersions(final Context ctx, final Metadata[] columns, final String where) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DocumentMetadata> getAllDocuments(final Context ctx, final Metadata[] columns, final String where) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getDocument(final int id, final int version, final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] removeDocument(final String identifier, final Context ctx) {
        for (final List<DocumentMetadata> versions : getCtxMap(ctx).values()) {
            for (final DocumentMetadata metadata : versions) {
                final String location = metadata.getFilestoreLocation();
                if (location != null && location.equals(identifier)) {
                    deletions.get(ctx).add(metadata);
                    return new int[] { 1, 1 };
                }
            }
        }
        return new int[] { 1, 1 };
    }

    @Override
    public int modifyDocument(final String oldidentifier, final String newidentifier, final String description, final String mimetype, final Context ctx) {
        for (final List<DocumentMetadata> versions : getCtxMap(ctx).values()) {
            for (final DocumentMetadata metadata : versions) {
                final String location = metadata.getFilestoreLocation();
                if (location != null && location.equals(oldidentifier)) {
                    metadata.setFilestoreLocation(newidentifier);
                    metadata.setDescription(description);
                    metadata.setFileMIMEType(mimetype);
                    changes.get(ctx).add(metadata);
                    return metadata.getId();
                }
            }
        }
        return -1;
    }

    @Override
    public int[] saveDocumentMetadata(final String identifier, final DocumentMetadata document, final User user, final Context ctx) {
        document.setFilestoreLocation(identifier);
        creations.get(ctx).add(new DocumentMetadataImpl(document));
        return new int[] { 1, 1, 1 };
    }

    @Override
    public SortedSet<String> getDocumentFileStoreLocationsperContext(final Context ctx) {
        final SortedSet<String> locations = new TreeSet<String>();
        for (final List<DocumentMetadata> versions : getCtxMap(ctx).values()) {
            for (final DocumentMetadata metadata : versions) {
                final String location = metadata.getFilestoreLocation();
                if (location != null) {
                    locations.add(location);
                }
            }
        }
        return locations;
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final int id, final Metadata[] columns, final Metadata sort, final int order, final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final int[] ids, final Metadata[] columns, final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int countDocuments(final long folderId, final boolean onlyOwnObjects, final Context ctx, final User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int countDocumentsperContext(final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasFolderForeignObjects(final long folderId, final Context ctx, final User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFolderEmpty(final long folderId, final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeUser(final int id, final Context ctx, final Integer destUser, final ServerSession session, final EntityLockManager locks) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<FileStorage> getFileStorages(final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startTransaction() {
        //IGNORE
    }

    @Override
    public void commit() {
        //IGNORE
    }

    @Override
    public void finish() {
        // IGNORE
    }

    @Override
    public void rollback() {
        // IGNORE
    }

    @Override
    public int getMaxActiveVersion(final int id, final Context context, List<DocumentMetadata> ignoreVersions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DBProvider getProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProvider(final DBProvider provider) {
        //IGNORE
    }

    @Override
    public Connection getReadConnection(final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Connection getWriteConnection(final Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseReadConnection(final Context ctx, final Connection con) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseWriteConnection(final Context ctx, final Connection con) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void commitDBTransaction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollbackDBTransaction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startDBTransaction() {
        // IGNORE
    }

    public void forgetChanges(final Context ctx) {
        changes.put(ctx, new ArrayList<DocumentMetadata>());
    }

    public List<DocumentMetadata> getChanges(final Context ctx) {
        if (!changes.containsKey(ctx)) {
            return new ArrayList<DocumentMetadata>();
        }
        return changes.get(ctx);
    }

    public void forgetDeletions(final Context ctx) {
        deletions.put(ctx, new ArrayList<DocumentMetadata>());
    }

    public List<DocumentMetadata> getDeletions(final Context ctx) {
        return deletions.get(ctx);
    }

    public void forgetCreated(final Context ctx) {
        creations.put(ctx, new ArrayList<DocumentMetadata>());
    }

    public List<DocumentMetadata> getCreated(final Context ctx) {
        return creations.get(ctx);
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // Nothing to do.
    }

    @Override
    public int getDocumentHolderFor(String fileIdentifier, Context ctx) throws OXException {
        for (final List<DocumentMetadata> versions : getCtxMap(ctx).values()) {
            for (final DocumentMetadata metadata : versions) {
                final String location = metadata.getFilestoreLocation();
                if (location != null && location.equals(fileIdentifier)) {
                    return metadata.getCreatedBy();
                }
            }
        }
        return -1;
    }
}
