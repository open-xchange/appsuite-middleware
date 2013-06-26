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

package com.openexchange.groupware.attach.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getStatement;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.activation.FileTypeMap;
import org.apache.commons.logging.Log;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentTimedResult;
import com.openexchange.groupware.attach.util.GetSwitch;
import com.openexchange.groupware.attach.util.SetSwitch;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.log.LogFactory;
import com.openexchange.session.Session;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.SaveFileAction;
import com.openexchange.tools.file.SaveFileWithQuotaAction;
import com.openexchange.tools.file.external.QuotaFileStorageExceptionCodes;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

public class AttachmentBaseImpl extends DBService implements AttachmentBase {

    public static enum FetchMode {
        PREFETCH, CLOSE_LATER, CLOSE_IMMEDIATELY
    }

    private static final FetchMode fetchMode = FetchMode.PREFETCH;

    static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AttachmentBaseImpl.class));

    private static final AttachmentQueryCatalog QUERIES = new AttachmentQueryCatalog();

    private final ThreadLocal<Context> contextHolder = new ThreadLocal<Context>();

    private final ThreadLocal<List<String>> fileIdRemoveList = new ThreadLocal<List<String>>();

    private final TIntObjectMap<List<AttachmentListener>> moduleListeners = new TIntObjectHashMap<List<AttachmentListener>>();

    private final TIntObjectMap<List<AttachmentAuthorization>> moduleAuthorizors = new TIntObjectHashMap<List<AttachmentAuthorization>>();

    public AttachmentBaseImpl() {
        super();
    }

    public AttachmentBaseImpl(final DBProvider provider) {
        super(provider);
    }

    @Override
    public long attachToObject(final AttachmentMetadata attachment, final InputStream data, final Session session, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {

        checkMayAttach(ServerSessionAdapter.valueOf(session, ctx, user, userConfig),
            attachment.getModuleId(), attachment.getFolderId(), attachment.getAttachedId());
//        checkMayAttach(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), ctx, user, userConfig);

        checkCharacters(attachment);

        contextHolder.set(ctx);
        final boolean newAttachment = attachment.getId() == NEW || attachment.getId() == 0;

        initDefaultFields(attachment, ctx, user);
        if (!newAttachment && data != null) {
            final List<String> remove = getFiles(new int[] { attachment.getId() }, ctx);
            fileIdRemoveList.get().addAll(remove);
        }
        String fileId;
        if (data != null) {
            try {
                fileId = saveFile(data, attachment, ctx);
            } catch (final OXException e) {
                if (QuotaFileStorageExceptionCodes.STORE_FULL.getNumber() == e.getCode()) {
                    throw AttachmentExceptionCodes.OVER_LIMIT.create(e);
                }
                throw AttachmentExceptionCodes.SAVE_FAILED.create(e);
            }
        } else {
            if (!newAttachment) {
                fileId = findFileId(attachment.getId(), ctx);
            } else {
                throw AttachmentExceptionCodes.FILE_MISSING.create();
            }
        }
        attachment.setFileId(fileId);
        return save(attachment, newAttachment, session, ctx, user, userConfig);

    }

    @Override
    public long detachFromObject(final int folderId, final int objectId, final int moduleId, final int[] ids, final Session session, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        checkMayDetach(ServerSessionAdapter.valueOf(session, ctx, user, userConfig), moduleId, folderId, objectId);
//        checkMayDetach(folderId, objectId, moduleId, ctx, user, userConfig);

        if (ids.length == 0) {
            return System.currentTimeMillis();
        }

        contextHolder.set(ctx);

        final List<String> files = getFiles(ids, ctx);

        final long ts = removeAttachments(folderId, objectId, moduleId, ids, session, ctx, user, userConfig);

        fileIdRemoveList.get().addAll(files);

        return ts;
    }

    @Override
    public AttachmentMetadata getAttachment(final Session session, final int folderId, final int objectId, final int moduleId, final int id, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        checkMayReadAttachments(ServerSessionAdapter.valueOf(session, ctx, user, userConfig), moduleId, folderId, objectId);
//        checkMayReadAttachments(folderId, objectId, moduleId, ctx, user, userConfig);

        contextHolder.set(ctx);

        return loadAttachment(folderId, id, ctx);
    }

    @Override
    public InputStream getAttachedFile(final Session session, final int folderId, final int objectId, final int moduleId, final int id, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        checkMayReadAttachments(ServerSessionAdapter.valueOf(session, ctx, user, userConfig), moduleId, folderId, objectId);
//        checkMayReadAttachments(folderId, objectId, moduleId, ctx, user, userConfig);
        contextHolder.set(ctx);

        return getFile(id, ctx);
    }

    @Override
    public SortedSet<String> getAttachmentFileStoreLocationsperContext(final Context ctx) throws OXException {
        final SortedSet<String> retval = new TreeSet<String>();
        Connection readCon = null;
        final String selectfileid = "SELECT file_id FROM prg_attachment WHERE file_id is not null AND cid=?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = getReadConnection(ctx);
            stmt = readCon.prepareStatement(selectfileid);
            stmt.setInt(1, ctx.getContextId());
            rs = stmt.executeQuery();

            while (rs.next()) {
                retval.add(rs.getString(1));
            }
        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readCon);
        }
        return retval;
    }

    @Override
    public TimedResult<AttachmentMetadata> getAttachments(final Session session, final int folderId, final int attachedId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return getAttachments(session, folderId, attachedId, moduleId, QUERIES.getFields(), null, ASC, ctx, user, userConfig);
    }

    @Override
    public TimedResult<AttachmentMetadata> getAttachments(final Session session, final int folderId, final int attachedId, final int moduleId, final AttachmentField[] columns, final AttachmentField sort, final int order, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        checkMayReadAttachments(ServerSessionAdapter.valueOf(session, ctx, user, userConfig), moduleId, folderId, attachedId);
//        checkMayReadAttachments(folderId, attachedId, moduleId, ctx, user, userConfig);

        contextHolder.set(ctx);
        final AttachmentField[] cols = addCreationDateAsNeeded(columns);

        final StringBuilder select = new StringBuilder("SELECT ");
        QUERIES.appendColumnList(select, cols);

        select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? ");
        if (sort != null) {
            select.append(" ORDER BY ");
            select.append(sort.getName());
            if (order == DESC) {
                select.append(" DESC");
            } else {
                select.append(" ASC");
            }
        }

        return new AttachmentTimedResult(new AttachmentIterator(
            select.toString(),
            cols,
            ctx,
            folderId,
            fetchMode,
            Integer.valueOf(moduleId),
            Integer.valueOf(attachedId),
            Integer.valueOf(ctx.getContextId())));
    }

    @Override
    public TimedResult<AttachmentMetadata> getAttachments(final Session session, final int folderId, final int attachedId, final int moduleId, final int[] idsToFetch, final AttachmentField[] columns, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        checkMayReadAttachments(ServerSessionAdapter.valueOf(session, ctx, user, userConfig), moduleId, folderId, attachedId);
//        checkMayReadAttachments(folderId, attachedId, moduleId, ctx, user, userConfig);

        contextHolder.set(ctx);

        final AttachmentField[] cols = addCreationDateAsNeeded(columns);

        final StringBuilder select = new StringBuilder("SELECT ");
        QUERIES.appendColumnList(select, cols);

        select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? and id in (");
        select.append(join(idsToFetch));
        select.append(')');

        return new AttachmentTimedResult(new AttachmentIterator(
            select.toString(),
            cols,
            ctx,
            folderId,
            fetchMode,
            Integer.valueOf(moduleId),
            Integer.valueOf(attachedId),
            Integer.valueOf(ctx.getContextId())));
    }

    @Override
    public Delta<AttachmentMetadata> getDelta(final Session session, final int folderId, final int attachedId, final int moduleId, final long ts, final boolean ignoreDeleted, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return getDelta(session, folderId, attachedId, moduleId, ts, ignoreDeleted, QUERIES.getFields(), null, ASC, ctx, user, null);
    }

    @Override
    public Delta<AttachmentMetadata> getDelta(final Session session, final int folderId, final int attachedId, final int moduleId, final long ts, final boolean ignoreDeleted, final AttachmentField[] columns, final AttachmentField sort, final int order, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        checkMayReadAttachments(ServerSessionAdapter.valueOf(session, ctx, user, userConfig), moduleId, folderId, attachedId);
//        checkMayReadAttachments(folderId, attachedId, moduleId, ctx, user, userConfig);

        contextHolder.set(ctx);

        final AttachmentField[] cols = addCreationDateAsNeeded(columns);

        final StringBuilder select = new StringBuilder("SELECT ");
        for (final AttachmentField field : cols) {
            select.append(field.getName());
            select.append(',');
        }
        select.setLength(select.length() - 1);

        select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? and creation_date > ?");

        if (sort != null) {
            select.append(" ORDER BY ");
            select.append(sort.getName());
            if (order == DESC) {
                select.append(" DESC");
            } else {
                select.append(" ASC");
            }
        }

        final SearchIterator<AttachmentMetadata> newIterator = new AttachmentIterator(
            select.toString(),
            cols,
            ctx,
            folderId,
            fetchMode,
            Integer.valueOf(moduleId),
            Integer.valueOf(attachedId),
            Integer.valueOf(ctx.getContextId()),
            Long.valueOf(ts));

        SearchIterator<AttachmentMetadata> deletedIterator = SearchIteratorAdapter.emptyIterator();

        if (!ignoreDeleted) {
            deletedIterator = new AttachmentIterator(
                "SELECT id FROM del_attachment WHERE module = ? and attached = ? and cid = ? and del_date > ?",
                new AttachmentField[] { AttachmentField.ID_LITERAL },
                ctx,
                folderId,
                fetchMode,
                Integer.valueOf(moduleId),
                Integer.valueOf(attachedId),
                Integer.valueOf(ctx.getContextId()),
                Long.valueOf(ts));
        }

        return new DeltaImpl<AttachmentMetadata>(
            newIterator,
            SearchIteratorAdapter.<AttachmentMetadata> emptyIterator(),
            deletedIterator,
            System.currentTimeMillis());
    }

    @Override
    public void registerAttachmentListener(final AttachmentListener listener, final int moduleId) {
        getListeners(moduleId).add(listener);
    }

    @Override
    public void removeAttachmentListener(final AttachmentListener listener, final int moduleId) {
        getListeners(moduleId).remove(listener);
    }

    private AttachmentField[] addCreationDateAsNeeded(final AttachmentField[] columns) {
        for (final AttachmentField attachmentField : columns) {
            if (attachmentField == AttachmentField.CREATION_DATE_LITERAL) {
                return columns;
            }
        }
        int i = 0;
        final AttachmentField[] copy = new AttachmentField[columns.length + 1];
        for (final AttachmentField attachmentField : columns) {
            copy[i++] = attachmentField;
        }
        copy[i] = AttachmentField.CREATION_DATE_LITERAL;
        return copy;
    }

    private long fireAttached(final AttachmentMetadata m, final User user, final UserConfiguration userConfig, final Session session, final Context ctx) throws OXException {
        final FireAttachedEventAction fireAttached = new FireAttachedEventAction();
        fireAttached.setAttachments(Arrays.asList(m));
        fireAttached.setSession(session);
        fireAttached.setContext(ctx);
        fireAttached.setSource(this);
        fireAttached.setUser(user);
        fireAttached.setUserConfiguration(userConfig);
        fireAttached.setProvider(this);
        fireAttached.setAttachmentListeners(getListeners(m.getModuleId()));
        perform(fireAttached, false);
        return fireAttached.getTimestamp();
    }

    private long fireDetached(final List<AttachmentMetadata> deleted, final int module, final User user, final UserConfiguration userConfig, final Session session, final Context ctx) throws OXException {
        final FireDetachedEventAction fireDetached = new FireDetachedEventAction();
        fireDetached.setAttachments(deleted);
        fireDetached.setSession(session);
        fireDetached.setContext(ctx);
        fireDetached.setSource(this);
        fireDetached.setUser(user);
        fireDetached.setUserConfiguration(userConfig);
        fireDetached.setProvider(this);
        fireDetached.setAttachmentListeners(getListeners(module));
        perform(fireDetached, false);
        return fireDetached.getTimestamp();
    }

    @Override
    public void addAuthorization(final AttachmentAuthorization authz, final int moduleId) {
        getAuthorizors(moduleId).add(authz);
    }

    @Override
    public void removeAuthorization(final AttachmentAuthorization authz, final int moduleId) {
        getAuthorizors(moduleId).remove(authz);
    }

    @Override
    public void deleteAll(final Context context) throws OXException {
        try {
            removeFiles(context);
        } catch (final OXException e) {
            throw AttachmentExceptionCodes.FILE_DELETE_FAILED.create(e, I(context.getContextId()));
        }
        try {
            removeDatabaseEntries(context);
        } catch (final SQLException e) {
            LOG.error("SQL Exception: ", e);
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }

    }

    private void removeDatabaseEntries(final Context context) throws OXException, SQLException {
        Connection writeCon = null;
        PreparedStatement stmt = null;
        try {
            writeCon = getWriteConnection(context);
            stmt = writeCon.prepareStatement("DELETE FROM prg_attachment WHERE cid = ?");
            stmt.setInt(1, context.getContextId());
            stmt.executeUpdate();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    LOG.error("Can't close statement", e);
                }
            }
            releaseWriteConnection(context, writeCon);
        }

    }

    private void removeFiles(final Context context) throws OXException, OXException {
        final FileStorage fs = getFileStorage(context);
        for (final String fileId : this.getAttachmentFileStoreLocationsperContext(context)) {
            fs.deleteFile(fileId);
        }
    }

    private List<AttachmentAuthorization> getAuthorizors(final int moduleId) {
        List<AttachmentAuthorization> authorizors = moduleAuthorizors.get(moduleId);
        if (authorizors == null) {
            authorizors = new ArrayList<AttachmentAuthorization>();
            moduleAuthorizors.put(moduleId, authorizors);
        }
        return authorizors;
    }

    // Helper Methods

    private void checkMayAttach(final ServerSession session, final int moduleId, final int folderId, final int objectId) throws OXException {
        for (final AttachmentAuthorization authz : getAuthorizors(moduleId)) {
            authz.checkMayAttach(session, folderId, objectId);
        }
    }

    private void checkMayReadAttachments(final ServerSession session, final int moduleId, final int folderId, final int objectId) throws OXException {
        for (final AttachmentAuthorization authz : getAuthorizors(moduleId)) {
            authz.checkMayReadAttachments(session, folderId, objectId);
        }
    }

    private void checkMayDetach(final ServerSession session, final int moduleId, final int folderId, final int objectId) throws OXException {
        for (final AttachmentAuthorization authz : getAuthorizors(moduleId)) {
            authz.checkMayDetach(session, folderId, objectId);
        }
    }

//    private void checkMayAttach(final int folderId, final int attachedId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
//        for (final AttachmentAuthorization authz : getAuthorizors(moduleId)) {
//            authz.checkMayAttach(folderId, attachedId, user, userConfig, ctx);
//        }
//    }
//
//    private void checkMayReadAttachments(final int folderId, final int objectId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
//        for (final AttachmentAuthorization authz : getAuthorizors(moduleId)) {
//            authz.checkMayReadAttachments(folderId, objectId, user, userConfig, ctx);
//        }
//    }
//
//    private void checkMayDetach(final int folderId, final int objectId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
//        for (final AttachmentAuthorization authz : getAuthorizors(moduleId)) {
//            authz.checkMayDetach(folderId, objectId, user, userConfig, ctx);
//        }
//    }
//
    private List<AttachmentListener> getListeners(final int moduleId) {
        List<AttachmentListener> listener = moduleListeners.get(moduleId);
        if (listener == null) {
            listener = new ArrayList<AttachmentListener>();
            moduleListeners.put(moduleId, listener);
        }
        return listener;
    }

    private void initDefaultFields(final AttachmentMetadata attachment, final Context ctx, final User user) throws OXException {
        attachment.setCreationDate(new Date());
        attachment.setCreatedBy(user.getId());
        if (attachment.getId() == NEW) {
            Connection writeCon = null;
            try {
                writeCon = getWriteConnection(ctx);
                attachment.setId(getId(ctx, writeCon));
            } catch (final SQLException e) {
                throw AttachmentExceptionCodes.GENERATIING_ID_FAILED.create(e);
            } finally {
                releaseWriteConnection(ctx, writeCon);
            }
        }

        if (attachment.getFilename() != null && (attachment.getFileMIMEType() == null || attachment.getFileMIMEType().equals(
            "application/unknown"))) {
            // Try guessing by filename
            final String mimetypes = FileTypeMap.getDefaultFileTypeMap().getContentType(attachment.getFilename());
            attachment.setFileMIMEType(mimetypes);
        }
    }

    private int getId(final Context ctx, final Connection writeCon) throws SQLException {
        if (writeCon.getAutoCommit()) {
            return IDGenerator.getId(ctx, Types.ATTACHMENT);
        }
        return IDGenerator.getId(ctx, Types.ATTACHMENT, writeCon);
    }

    private String saveFile(final InputStream data, final AttachmentMetadata attachment, final Context ctx) throws OXException, OXException {
        final QuotaFileStorage fs = getFileStorage(ctx);
        SaveFileAction action = null;
        final SaveFileWithQuotaAction a = new SaveFileWithQuotaAction();
        a.setIn(data);
        a.setSizeHint(attachment.getFilesize());
        a.setStorage(fs);
        action = a;
        action.perform();
        addUndoable(action);

        attachment.setFilesize(fs.getFileSize(action.getId())); // Definitive!

        return action.getId();
    }

    private List<String> getFiles(final int[] ids, final Context ctx) throws OXException {
        final List<String> files = new ArrayList<String>();
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final StringBuilder selectFileIds = new StringBuilder("SELECT file_id FROM prg_attachment WHERE id in (");
            selectFileIds.append(join(ids));
            selectFileIds.append(") AND cid = ?");

            readCon = getReadConnection(ctx);
            stmt = readCon.prepareStatement(selectFileIds.toString());
            stmt.setInt(1, ctx.getContextId());
            rs = stmt.executeQuery();

            while (rs.next()) {
                files.add(rs.getString(1));
            }
        } catch (final SQLException e) {
            try {
                rollbackDBTransaction();
            } catch (final OXException x2) {
                x2.log(LOG);
            }
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readCon);
        }
        return files;
    }

    private InputStream retrieveFile(final String fileId, final Context ctx) throws OXException {
        try {
            final FileStorage fs = getFileStorage(ctx);
            return fs.getFile(fileId);

        } catch (final OXException e) {
            throw AttachmentExceptionCodes.READ_FAILED.create(e, fileId);
        }
    }

    InputStream getFile(final int id, final Context ctx) throws OXException {
        final String fileId = findFileId(id, ctx);
        return retrieveFile(fileId, ctx);
    }

    private String findFileId(final int id, final Context ctx) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection readCon = null;
        try {
            readCon = getReadConnection(ctx);
            stmt = readCon.prepareStatement(QUERIES.getSelectFileId());

            stmt.setInt(1, id);
            stmt.setInt(2, ctx.getContextId());

            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw AttachmentExceptionCodes.ATTACHMENT_NOT_FOUND.create();
            }
            return rs.getString(1);
        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readCon);
        }
    }

    private long removeAttachments(final int folderId, final int objectId, final int moduleId, final int[] ids, final Session session, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        final TimedResult<AttachmentMetadata> tr = getAttachments(session, folderId, objectId, moduleId, ids, QUERIES.getFields(), ctx, user, userConfig);
        boolean found = false;

        SearchIterator<AttachmentMetadata> iter = null;
        final List<AttachmentMetadata> recreate = new ArrayList<AttachmentMetadata>();
        try {
            iter = tr.results();
            while (iter.hasNext()) {
                found = true;
                AttachmentMetadata att;
                att = iter.next();
                att.setFolderId(folderId);
                recreate.add(att);
            }
        } finally {
            if (null != iter) {
                try {
                    iter.close();
                } catch (final Exception e) {
                    LOG.error("", e);
                }
            }
        }

        if (!found) {
            return System.currentTimeMillis();
        }
        final DeleteAttachmentAction delAction = new DeleteAttachmentAction();
        delAction.setAttachments(recreate);
        delAction.setContext(ctx);
        delAction.setProvider(this);
        delAction.setQueryCatalog(QUERIES);

        perform(delAction, true);

        return this.fireDetached(recreate, moduleId, user, userConfig, session, ctx);

    }

    @Override
    public int[] removeAttachment(final String file_id, final Context ctx) throws OXException {
        final int[] retval = new int[2];
        final long now = System.currentTimeMillis();
        Connection readCon = null;
        Connection writeCon = null;
        PreparedStatement stmt = null;
        StringBuilder rememberDel = null;
        ResultSet rs = null;
        try {
            readCon = getReadConnection(ctx);

            stmt = readCon.prepareStatement("SELECT id, attached, module FROM prg_attachment WHERE cid=? AND file_id=?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setString(2, file_id);

            rs = stmt.executeQuery();

            rememberDel = new StringBuilder("INSERT INTO del_attachment (id, del_date, cid, attached, module) VALUES ");
            boolean found = false;
            if (rs.next()) {
                found = true;
                rememberDel.append('(');
                rememberDel.append(rs.getInt(1));
                rememberDel.append(',');
                rememberDel.append(now);
                rememberDel.append(',');
                rememberDel.append(ctx.getContextId());
                rememberDel.append(',');
                rememberDel.append(rs.getInt(2));
                rememberDel.append(',');
                rememberDel.append(rs.getInt(3));

                rememberDel.append(')');
            }
            if (!found) {
                throw AttachmentExceptionCodes.ATTACHMENT_WITH_FILEID_NOT_FOUND.create(file_id);
            }
        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readCon);
            readCon = null;
        }

        try {

            writeCon = getWriteConnection(ctx);

            stmt = writeCon.prepareStatement(rememberDel.toString());
            retval[0] = stmt.executeUpdate();
            stmt.close();

            stmt = writeCon.prepareStatement("DELETE FROM prg_attachment WHERE cid=? AND file_id=?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setString(2, file_id);
            retval[1] = stmt.executeUpdate();
        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, null);
            releaseWriteConnection(ctx, writeCon);
        }
        return retval;
    }

    @Override
    public int modifyAttachment(final String file_id, final String new_file_id, final String new_comment, final String new_mime, final Context ctx) throws OXException {
        int retval = -1;
        Connection writeCon = null;
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String comment = null;
        try {
            readCon = getReadConnection(ctx);

            stmt = readCon.prepareStatement("SELECT comment FROM prg_attachment WHERE cid=? AND file_id=?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setString(2, file_id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                comment = rs.getString(1);
            }
        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readCon);
            readCon = null;
        }

        if (comment == null) {
            comment = new_comment;
        } else {
            comment = comment.concat(new_comment);
        }

        try {
            writeCon = getWriteConnection(ctx);
            stmt = writeCon.prepareStatement("UPDATE prg_attachment SET file_id=?, file_mimetype=?, comment=? WHERE cid=? AND file_id=?");
            stmt.setString(1, new_file_id);
            stmt.setString(2, new_mime);
            stmt.setString(3, comment);
            stmt.setInt(4, ctx.getContextId());
            stmt.setString(5, file_id);
            retval = stmt.executeUpdate();
        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, null);
            releaseWriteConnection(ctx, readCon);
        }
        return retval;
    }

    private long save(final AttachmentMetadata attachment, final boolean newAttachment, final Session session, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        AbstractAttachmentAction action = null;
        if (newAttachment) {
            final CreateAttachmentAction createAction = new CreateAttachmentAction();
            createAction.setAttachments(Arrays.asList(new AttachmentMetadata[] { attachment }));
            action = createAction;
        } else {
            final AttachmentMetadata oldAttachment = loadAttachment(attachment.getFolderId(), attachment.getId(), ctx);

            final UpdateAttachmentAction updateAction = new UpdateAttachmentAction();
            updateAction.setAttachments(Arrays.asList(attachment));
            updateAction.setOldAttachments(Arrays.asList(oldAttachment));
            action = updateAction;

        }

        action.setProvider(this);
        action.setContext(ctx);
        action.setQueryCatalog(QUERIES);

        perform(action, true);

        if (newAttachment) {
            return fireAttached(attachment, user, userConfig, session, ctx);
        }
        return System.currentTimeMillis();
    }

    private void checkCharacters(final AttachmentMetadata attachment) throws OXException {
        final StringBuilder errors = new StringBuilder();
        boolean invalid = false;
        final GetSwitch get = new GetSwitch(attachment);
        for (final AttachmentField field : AttachmentField.VALUES_ARRAY) {
            final Object value = field.doSwitch(get);
            if (null != value && value instanceof String) {
                final String error = Check.containsInvalidChars((String) value);
                if (null != error) {
                    invalid = true;
                    errors.append(field.getName()).append(' ').append(error).append('\n');
                }
            }
        }
        if (invalid) {
            throw AttachmentExceptionCodes.INVALID_CHARACTERS.create(errors.toString());
        }
    }

    private AttachmentMetadata loadAttachment(final int folderId, final int id, final Context ctx) throws OXException {

        Connection readConnection = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            readConnection = getReadConnection(ctx);
            stmt = readConnection.prepareStatement(QUERIES.getSelectById());
            stmt.setInt(1, id);
            stmt.setInt(2, ctx.getContextId());

            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw AttachmentExceptionCodes.ATTACHMENT_NOT_FOUND.create();
            }
            return getFromResultSet(rs, folderId);

        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readConnection);
        }
    }

    private AttachmentMetadata getFromResultSet(final ResultSet rs, final int folderId) throws SQLException {
        final AttachmentImpl attachment = new AttachmentImpl();
        final SetSwitch set = new SetSwitch(attachment);
        for (final AttachmentField field : QUERIES.getFields()) {
            Object value = rs.getObject(field.getName());
            value = patchValue(value, field);
            set.setValue(value);
            field.doSwitch(set);
        }
        attachment.setFolderId(folderId);
        return attachment;
    }

    private boolean isDateField(final AttachmentField field) {
        return field.equals(AttachmentField.CREATION_DATE_LITERAL);
    }

    private String join(final int[] is) {
        final StringBuilder b = new StringBuilder();
        for (final int i : is) {
            b.append(i);
            b.append(',');
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

    Object patchValue(final Object value, final AttachmentField field) {
        if (value instanceof Long) {
            if (isDateField(field)) {
                return new Date(((Long) value).longValue());
            }
            if (!field.equals(AttachmentField.FILE_SIZE_LITERAL)) {
                return Integer.valueOf(((Long) value).intValue());
            }
        }
        return value;
    }

    @Override
    public void commit() throws OXException {
        if (fileIdRemoveList.get().size() > 0) {
            try {
                final FileStorage fs = getFileStorage(contextHolder.get());
                for (final String fileId : fileIdRemoveList.get()) {
                    fs.deleteFile(fileId);
                }
            } catch (final OXException e) {
                try {
                    rollback();
                } catch (final OXException txe) {
                    e.log(LOG);
                }
                throw AttachmentExceptionCodes.FILE_DELETE_FAILED.create(e, I(contextHolder.get().getContextId()));
            }
        }
    }

    @Override
    public void finish() throws OXException {
        fileIdRemoveList.set(null);
        contextHolder.set(null);
        super.finish();
    }

    @Override
    public void startTransaction() throws OXException {
        fileIdRemoveList.set(new ArrayList<String>());
        contextHolder.set(null);
        super.startTransaction();
    }

    protected QuotaFileStorage getFileStorage(final Context ctx) throws OXException, OXException {
        try {
            return QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
        } catch (final OXException e) {
            throw AttachmentExceptionCodes.FILESTORE_DOWN.create(e);
        }
    }

    public class AttachmentIterator implements SearchIterator<AttachmentMetadata> {

        private final String sql;

        private final AttachmentField[] columns;

        private boolean queried;

        private final Context ctx;

        private Connection readCon;

        private PreparedStatement stmt;

        private ResultSet rs;

        private Exception exception;

        private boolean initNext;

        private boolean hasNext;

        private final Object[] values;

        private final int folderId;

        private final FetchMode mode;

        private SearchIteratorAdapter<AttachmentMetadata> delegate;

        private final List<OXException> warnings;

        public AttachmentIterator(final String sql, final AttachmentField[] columns, final Context ctx, final int folderId, final FetchMode mode, final Object... values) {
            this.warnings = new ArrayList<OXException>(2);
            this.sql = sql;
            this.columns = columns;
            this.ctx = ctx;
            this.values = values;
            this.folderId = folderId;
            this.mode = mode;
        }

        @Override
        public boolean hasNext() throws OXException {
            {
                final SearchIteratorAdapter<AttachmentMetadata> delegate = this.delegate;
                if (delegate != null) {
                    return delegate.hasNext();
                }
            }
            try {
                if (!queried) {
                    queried = true;
                    query();
                    {
                        final Exception exception = this.exception;
                        if (exception != null) {
                            if (exception instanceof OXException) {
                                throw (OXException) exception;
                            }
                            throw AttachmentExceptionCodes.SEARCH_PROBLEM.create(exception);
                        }
                    }
                    {
                        final SearchIteratorAdapter<AttachmentMetadata> delegate = this.delegate;
                        if (delegate != null) {
                            return delegate.hasNext();
                        }
                    }
                    initNext = true;
                }
                if (initNext) {
                    hasNext = null == rs ? false : rs.next();
                }
                initNext = false;
                return hasNext;
            } catch (final SQLException e) {
                this.exception = e;
                return true;
            }
        }

        @Override
        public AttachmentMetadata next() throws OXException {
            {
                final SearchIteratorAdapter<AttachmentMetadata> delegate = this.delegate;
                if (delegate != null) {
                    return delegate.next();
                }
            }
            hasNext();
            {
                final Exception exception = this.exception;
                if (exception != null) {
                    if (exception instanceof OXException) {
                        throw (OXException) exception;
                    }
                    throw AttachmentExceptionCodes.SEARCH_PROBLEM.create(exception);
                }
            }

            final AttachmentMetadata m = nextFromResult(rs);
            initNext = true;
            return m;
        }

        private AttachmentMetadata nextFromResult(final ResultSet rs) throws OXException {
            final AttachmentMetadata m = new AttachmentImpl();
            final SetSwitch set = new SetSwitch(m);

            try {
                for (final AttachmentField column : columns) {
                    Object value;
                    if (column.equals(AttachmentField.FOLDER_ID_LITERAL)) {
                        value = Integer.valueOf(folderId);
                    } else {
                        value = rs.getObject(column.getName());
                    }
                    value = patchValue(value, column);
                    set.setValue(value);
                    column.doSwitch(set);
                }
            } catch (final SQLException e) {
                throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            }
            return m;
        }

        @Override
        public void close() throws OXException {
            if (delegate != null) {
                delegate.close();
                return;
            }
            closeSQLStuff(rs, stmt);
            if (null != readCon) {
                releaseReadConnection(ctx, readCon);
            }
        }

        @Override
        public int size() {
            if (delegate != null) {
                return delegate.size();
            }
            return -1;
        }

        public boolean hasSize() {
            if (delegate != null) {
                return delegate.hasSize();
            }
            return false;
        }

        @Override
        public void addWarning(final OXException warning) {
            warnings.add(warning);
        }

        @Override
        public OXException[] getWarnings() {
            return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
        }

        @Override
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        private void query() {
            try {
                readCon = AttachmentBaseImpl.this.getReadConnection(ctx);
                stmt = readCon.prepareStatement(sql);
                int i = 1;
                for (final Object value : values) {
                    stmt.setObject(i++, value);
                }
                rs = stmt.executeQuery();
                if (mode.equals(FetchMode.CLOSE_LATER)) {
                    return;
                } else if (mode.equals(FetchMode.CLOSE_IMMEDIATELY)) {
                    closeSQLStuff(stmt);
                    releaseReadConnection(ctx, readCon);
                    stmt = null;
                    readCon = null;
                } else if (mode.equals(FetchMode.PREFETCH)) {
                    final List<AttachmentMetadata> values = new LinkedList<AttachmentMetadata>();
                    while (rs.next()) {
                        values.add(nextFromResult(rs));
                    }
                    closeSQLStuff(rs, stmt);
                    releaseReadConnection(ctx, readCon);
                    stmt = null;
                    readCon = null;
                    rs = null;
                    delegate = new SearchIteratorAdapter<AttachmentMetadata>(values.iterator());
                }
            } catch (final SearchIteratorException e) {
                LOG.error(e);
                this.exception = e;
            } catch (final SQLException e) {
                LOG.error(e);
                this.exception = e;
            } catch (final OXException e) {
                LOG.error(e);
                this.exception = e;
            }
        }
    }

    @Override
    public Date getNewestCreationDate(final Context ctx, final int moduleId, final int attachedId) throws OXException {
        return getNewestCreationDates(ctx, moduleId, new int[] { attachedId }).get(I(attachedId));
    }

    @Override
    public Map<Integer, Date> getNewestCreationDates(final Context ctx, final int moduleId, final int[] attachedIds) throws OXException {
        final Connection con = getReadConnection(ctx);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(DBUtils.getIN(QUERIES.getSelectNewestCreationDate(), attachedIds.length) + " GROUP BY attached");
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, moduleId);
            for (final int attachedId : attachedIds) {
                stmt.setInt(pos++, attachedId);
            }
            result = stmt.executeQuery();
            final Map<Integer, Date> retval = new HashMap<Integer, Date>();
            while (result.next()) {
                retval.put(I(result.getInt(1)), new Date(result.getLong(2)));
            }
            return retval;
        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }
    }
}
