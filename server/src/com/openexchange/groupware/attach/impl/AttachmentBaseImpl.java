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

package com.openexchange.groupware.attach.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.activation.MimetypesFileTypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentException;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentTimedResult;
import com.openexchange.groupware.attach.util.GetSwitch;
import com.openexchange.groupware.attach.util.SetSwitch;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.SaveFileAction;
import com.openexchange.tools.file.SaveFileWithQuotaAction;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tx.TransactionException;

public class AttachmentBaseImpl extends DBService implements AttachmentBase {

    public static enum FetchMode {
        PREFETCH, CLOSE_LATER, CLOSE_IMMEDIATELY
    }

    private static final FetchMode fetchMode = FetchMode.PREFETCH;

    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(AttachmentBaseImpl.class));

    private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(AttachmentBaseImpl.class, LOG);

    private static final AttachmentQueryCatalog QUERIES = new AttachmentQueryCatalog();

    private final ThreadLocal<Context> contextHolder = new ThreadLocal<Context>();

    private final ThreadLocal<List<String>> fileIdRemoveList = new ThreadLocal<List<String>>();

    private final Map<Integer, List<AttachmentListener>> moduleListeners = new HashMap<Integer, List<AttachmentListener>>();

    private final Map<Integer, List<AttachmentAuthorization>> moduleAuthorizors = new HashMap<Integer, List<AttachmentAuthorization>>();

    public AttachmentBaseImpl() {
        super();
    }

    public AttachmentBaseImpl(final DBProvider provider) {
        super(provider);
    }

    public long attachToObject(final AttachmentMetadata attachment, final InputStream data, Session session, final Context ctx, final User user, final UserConfiguration userConfig) throws AttachmentException {

        checkMayAttach(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), ctx, user, userConfig);

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
            } catch (OXException e) {
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

    public long detachFromObject(final int folderId, final int objectId, final int moduleId, final int[] ids, Session session, final Context ctx, final User user, final UserConfiguration userConfig) throws AttachmentException {
        checkMayDetach(folderId, objectId, moduleId, ctx, user, userConfig);

        if (ids.length == 0) {
            return System.currentTimeMillis();
        }

        contextHolder.set(ctx);

        final List<String> files = getFiles(ids, ctx);

        final long ts = removeAttachments(folderId, objectId, moduleId, ids, session, ctx, user, userConfig);

        fileIdRemoveList.get().addAll(files);

        return ts;
    }

    public AttachmentMetadata getAttachment(final int folderId, final int objectId, final int moduleId, final int id, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        checkMayReadAttachments(folderId, objectId, moduleId, ctx, user, userConfig);

        contextHolder.set(ctx);

        return loadAttachment(folderId, id, ctx);
    }

    public InputStream getAttachedFile(final int folderId, final int objectId, final int moduleId, final int id, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        checkMayReadAttachments(folderId, objectId, moduleId, ctx, user, userConfig);
        contextHolder.set(ctx);

        return getFile(id, ctx);
    }

    public SortedSet<String> getAttachmentFileStoreLocationsperContext(final Context ctx) throws AttachmentException {
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
        } catch (final DBPoolingException e) {
            throw new AttachmentException(e);
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readCon);
        }
        return retval;
    }

    public TimedResult<AttachmentMetadata> getAttachments(final int folderId, final int attachedId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return getAttachments(folderId, attachedId, moduleId, QUERIES.getFields(), null, ASC, ctx, user, userConfig);
    }

    public TimedResult<AttachmentMetadata> getAttachments(final int folderId, final int attachedId, final int moduleId, AttachmentField[] columns, final AttachmentField sort, final int order, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {

        checkMayReadAttachments(folderId, attachedId, moduleId, ctx, user, userConfig);

        contextHolder.set(ctx);
        columns = addCreationDateAsNeeded(columns);

        final StringBuilder select = new StringBuilder("SELECT ");
        QUERIES.appendColumnList(select, columns);

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
            columns,
            ctx,
            folderId,
            fetchMode,
            Integer.valueOf(moduleId),
            Integer.valueOf(attachedId),
            Integer.valueOf(ctx.getContextId())));
    }

    public TimedResult<AttachmentMetadata> getAttachments(final int folderId, final int attachedId, final int moduleId, final int[] idsToFetch, AttachmentField[] columns, final Context ctx, final User user, final UserConfiguration userConfig) throws AttachmentException {
        checkMayReadAttachments(folderId, attachedId, moduleId, ctx, user, userConfig);

        contextHolder.set(ctx);

        columns = addCreationDateAsNeeded(columns);

        final StringBuilder select = new StringBuilder("SELECT ");
        QUERIES.appendColumnList(select, columns);

        select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? and id in (");
        select.append(join(idsToFetch));
        select.append(')');

        return new AttachmentTimedResult(new AttachmentIterator(
            select.toString(),
            columns,
            ctx,
            folderId,
            fetchMode,
            Integer.valueOf(moduleId),
            Integer.valueOf(attachedId),
            Integer.valueOf(ctx.getContextId())));
    }

    public Delta<AttachmentMetadata> getDelta(final int folderId, final int attachedId, final int moduleId, final long ts, final boolean ignoreDeleted, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return getDelta(folderId, attachedId, moduleId, ts, ignoreDeleted, QUERIES.getFields(), null, ASC, ctx, user, null);
    }

    public Delta<AttachmentMetadata> getDelta(final int folderId, final int attachedId, final int moduleId, final long ts, final boolean ignoreDeleted, AttachmentField[] columns, final AttachmentField sort, final int order, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        checkMayReadAttachments(folderId, attachedId, moduleId, ctx, user, userConfig);

        contextHolder.set(ctx);

        columns = addCreationDateAsNeeded(columns);

        final StringBuilder select = new StringBuilder("SELECT ");
        for (final AttachmentField field : columns) {
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
            columns,
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

    public void registerAttachmentListener(final AttachmentListener listener, final int moduleId) {
        getListeners(moduleId).add(listener);
    }

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

    private long fireAttached(final AttachmentMetadata m, final User user, final UserConfiguration userConfig, Session session, final Context ctx) throws AttachmentException {
        final FireAttachedEventAction fireAttached = new FireAttachedEventAction();
        fireAttached.setAttachments(Arrays.asList(m));
        fireAttached.setSession(session);
        fireAttached.setContext(ctx);
        fireAttached.setSource(this);
        fireAttached.setUser(user);
        fireAttached.setUserConfiguration(userConfig);
        fireAttached.setProvider(this);
        fireAttached.setAttachmentListeners(getListeners(m.getModuleId()));
        try {
            perform(fireAttached, false);
            return fireAttached.getTimestamp();
        } catch (final AbstractOXException e) {
            throw new AttachmentException(e);
        }

    }

    private long fireDetached(final List<AttachmentMetadata> deleted, final int module, final User user, final UserConfiguration userConfig, Session session, final Context ctx) throws AttachmentException {
        final FireDetachedEventAction fireDetached = new FireDetachedEventAction();
        fireDetached.setAttachments(deleted);
        fireDetached.setSession(session);
        fireDetached.setContext(ctx);
        fireDetached.setSource(this);
        fireDetached.setUser(user);
        fireDetached.setUserConfiguration(userConfig);
        fireDetached.setProvider(this);
        fireDetached.setAttachmentListeners(getListeners(module));
        try {
            perform(fireDetached, false);
            return fireDetached.getTimestamp();
        } catch (final AbstractOXException e) {
            throw new AttachmentException(e);
        }
    }

    public void addAuthorization(final AttachmentAuthorization authz, final int moduleId) {
        getAuthorizors(moduleId).add(authz);
    }

    public void removeAuthorization(final AttachmentAuthorization authz, final int moduleId) {
        getAuthorizors(moduleId).remove(authz);
    }

    public void deleteAll(final Context context) throws AttachmentException {
        try {
            removeFiles(context);
        } catch (OXException e) {
            throw AttachmentExceptionCodes.FILE_DELETE_FAILED.create(e, I(context.getContextId()));
        }
        try {
            removeDatabaseEntries(context);
        } catch (final SQLException e) {
            LOG.error("SQL Exception: ", e);
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (DBPoolingException e) {
            throw new AttachmentException(e);
        }

    }

    private void removeDatabaseEntries(final Context context) throws DBPoolingException, SQLException {
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

    private void removeFiles(final Context context) throws OXException, AttachmentException {
        final FileStorage fs = getFileStorage(context);
        for (final String fileId : this.getAttachmentFileStoreLocationsperContext(context)) {
            fs.deleteFile(fileId);
        }
    }

    private List<AttachmentAuthorization> getAuthorizors(final int moduleId) {
        final Integer key = Integer.valueOf(moduleId);
        List<AttachmentAuthorization> authorizors = moduleAuthorizors.get(key);
        if (authorizors == null) {
            authorizors = new ArrayList<AttachmentAuthorization>();
            moduleAuthorizors.put(key, authorizors);
        }
        return authorizors;
    }

    // Helper Methods

    private void checkMayAttach(final int folderId, final int attachedId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws AttachmentException {
        for (final AttachmentAuthorization authz : getAuthorizors(moduleId)) {
            try {
                authz.checkMayAttach(folderId, attachedId, user, userConfig, ctx);
            } catch (OXException e) {
                throw new AttachmentException(e);
            }
        }
    }

    private void checkMayReadAttachments(final int folderId, final int objectId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws AttachmentException {
        for (final AttachmentAuthorization authz : getAuthorizors(moduleId)) {
            try {
                authz.checkMayReadAttachments(folderId, objectId, user, userConfig, ctx);
            } catch (OXException e) {
                throw new AttachmentException(e);
            }
        }
    }

    private void checkMayDetach(final int folderId, final int objectId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws AttachmentException {
        for (final AttachmentAuthorization authz : getAuthorizors(moduleId)) {
            try {
                authz.checkMayDetach(folderId, objectId, user, userConfig, ctx);
            } catch (OXException e) {
                throw new AttachmentException(e);
            }
        }
    }

    private List<AttachmentListener> getListeners(final int moduleId) {
        final Integer key = Integer.valueOf(moduleId);
        List<AttachmentListener> listener = moduleListeners.get(key);
        if (listener == null) {
            listener = new ArrayList<AttachmentListener>();
            moduleListeners.put(key, listener);
        }
        return listener;
    }

    private void initDefaultFields(final AttachmentMetadata attachment, final Context ctx, final User user) throws AttachmentException {
        attachment.setCreationDate(new Date());
        attachment.setCreatedBy(user.getId());
        if (attachment.getId() == NEW) {
            Connection writeCon = null;
            try {
                writeCon = getWriteConnection(ctx);
                attachment.setId(getId(ctx, writeCon));
            } catch (final SQLException e) {
                throw AttachmentExceptionCodes.GENERATIING_ID_FAILED.create(e);
            } catch (DBPoolingException e) {
                throw new AttachmentException(e);
            } finally {
                releaseWriteConnection(ctx, writeCon);
            }
        }

        if (attachment.getFilename() != null && (attachment.getFileMIMEType() == null || attachment.getFileMIMEType().equals(
            "application/unknown"))) {
            // Try guessing by filename
            final String mimetypes = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(attachment.getFilename());
            attachment.setFileMIMEType(mimetypes);
        }
    }

    private int getId(final Context ctx, final Connection writeCon) throws SQLException {
        if (writeCon.getAutoCommit()) {
            return IDGenerator.getId(ctx, Types.ATTACHMENT);
        }
        return IDGenerator.getId(ctx, Types.ATTACHMENT, writeCon);
    }

    private String saveFile(final InputStream data, final AttachmentMetadata attachment, final Context ctx) throws OXException, AttachmentException {
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

    private List<String> getFiles(final int[] ids, final Context ctx) throws AttachmentException {
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
            } catch (final DBPoolingException x2) {
                LL.log(x2);
            }
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (DBPoolingException e) {
            throw new AttachmentException(e);
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readCon);
        }
        return files;
    }

    private InputStream retrieveFile(final String fileId, final Context ctx) throws AttachmentException {
        try {
            final FileStorage fs = getFileStorage(ctx);
            return fs.getFile(fileId);

        } catch (final OXException e) {
            throw AttachmentExceptionCodes.READ_FAILED.create(e, fileId);
        }
    }

    InputStream getFile(final int id, final Context ctx) throws AttachmentException {
        final String fileId = findFileId(id, ctx);
        return retrieveFile(fileId, ctx);
    }

    private String findFileId(final int id, final Context ctx) throws AttachmentException {
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
        } catch (DBPoolingException e) {
            throw new AttachmentException(e);
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readCon);
        }
    }

    private long removeAttachments(final int folderId, final int objectId, final int moduleId, final int[] ids, Session session, final Context ctx, final User user, final UserConfiguration userConfig) throws AttachmentException {
        final TimedResult<AttachmentMetadata> tr = getAttachments(folderId, objectId, moduleId, ids, QUERIES.getFields(), ctx, user, userConfig);
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
        } catch (AbstractOXException e) {
            throw new AttachmentException(e);
        } finally {
            try {
                iter.close();
            } catch (final AbstractOXException e) {
                LOG.error("", e);
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

        try {
            perform(delAction, true);
        } catch (final AbstractOXException e1) {
            throw new AttachmentException(e1);
        }

        return this.fireDetached(recreate, moduleId, user, userConfig, session, ctx);

    }

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
        } catch (DBPoolingException e) {
            throw new AttachmentException(e);
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
        } catch (DBPoolingException e) {
            throw new AttachmentException(e);
        } finally {
            close(stmt, null);
            releaseWriteConnection(ctx, writeCon);
        }
        return retval;
    }

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
        } catch (DBPoolingException e) {
            throw new AttachmentException(e);
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
        } catch (DBPoolingException e) {
            throw new AttachmentException(e);
        } finally {
            close(stmt, null);
            releaseWriteConnection(ctx, readCon);
        }
        return retval;
    }

    private long save(final AttachmentMetadata attachment, final boolean newAttachment, Session session, final Context ctx, final User user, final UserConfiguration userConfig) throws AttachmentException {
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

        try {
            perform(action, true);
        } catch (final AbstractOXException e) {
            throw new AttachmentException(e);
        }

        if (newAttachment) {
            return fireAttached(attachment, user, userConfig, session, ctx);
        }
        return System.currentTimeMillis();
    }

    private void checkCharacters(final AttachmentMetadata attachment) throws AttachmentException {
        final StringBuilder errors = new StringBuilder();
        boolean invalid = false;
        final GetSwitch get = new GetSwitch(attachment);
        for (final AttachmentField field : AttachmentField.VALUES_ARRAY) {
            final Object value = field.doSwitch(get);
            if (null != value && value instanceof String) {
                final String error = Check.containsInvalidChars((String) value);
                if (null != error) {
                    invalid = true;
                    errors.append(field.getName()).append(" ").append(error).append("\n");
                }
            }
        }
        if (invalid) {
            throw AttachmentExceptionCodes.INVALID_CHARACTERS.create(errors.toString());
        }
    }

    private AttachmentMetadata loadAttachment(final int folderId, final int id, final Context ctx) throws AttachmentException {

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
        } catch (DBPoolingException e) {
            throw new AttachmentException(e);
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

    Object patchValue(Object value, final AttachmentField field) {
        if (value instanceof Long) {
            if (isDateField(field)) {
                value = new Date(((Long) value).longValue());
            } else if (!field.equals(AttachmentField.FILE_SIZE_LITERAL)) {
                value = Integer.valueOf(((Long) value).intValue());
            }
        }
        return value;
    }

    @Override
    public void commit() throws TransactionException {
        if (fileIdRemoveList.get().size() > 0) {
            try {
                final FileStorage fs = getFileStorage(contextHolder.get());
                for (final String fileId : fileIdRemoveList.get()) {
                    fs.deleteFile(fileId);
                }
            } catch (OXException e) {
                try {
                    rollback();
                } catch (final OXException txe) {
                    LL.log(e);
                }
                throw new TransactionException(AttachmentExceptionCodes.FILE_DELETE_FAILED.create(e, I(contextHolder.get().getContextId())));
            } catch (AttachmentException e) {
                throw new TransactionException(e);
            }
        }
    }

    @Override
    public void finish() throws TransactionException {
        fileIdRemoveList.set(null);
        contextHolder.set(null);
        super.finish();
    }

    @Override
    public void startTransaction() throws TransactionException {
        fileIdRemoveList.set(new ArrayList<String>());
        contextHolder.set(null);
        super.startTransaction();
    }

    protected QuotaFileStorage getFileStorage(final Context ctx) throws OXException, AttachmentException {
        try {
            return QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
        } catch (FilestoreException e) {
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

        private final List<AbstractOXException> warnings;

        public AttachmentIterator(final String sql, final AttachmentField[] columns, final Context ctx, final int folderId, final FetchMode mode, final Object... values) {
            this.warnings = new ArrayList<AbstractOXException>(2);
            this.sql = sql;
            this.columns = columns;
            this.ctx = ctx;
            this.values = values;
            this.folderId = folderId;
            this.mode = mode;
        }

        public boolean hasNext() {
            if (delegate != null) {
                return delegate.hasNext();
            }
            try {
                if (!queried) {
                    queried = true;
                    query();
                    if (delegate != null) {
                        return delegate.hasNext();
                    }
                    initNext = true;
                }
                if (initNext) {
                    hasNext = rs.next();
                }
                initNext = false;
                return hasNext;
            } catch (SQLException e) {
                this.exception = e;
                return true;
            }
        }

        public AttachmentMetadata next() throws SearchIteratorException {
            if (delegate != null) {
                return delegate.next();
            }
            hasNext();
            if (exception != null) {
                if (exception instanceof AbstractOXException) {
                    throw new SearchIteratorException((AbstractOXException) exception);
                }
                throw new SearchIteratorException(AttachmentExceptionCodes.SEARCH_PROBLEM.create(exception));
            }

            final AttachmentMetadata m = nextFromResult(rs);
            initNext = true;
            return m;
        }

        private AttachmentMetadata nextFromResult(final ResultSet rs) throws SearchIteratorException {
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
                throw new SearchIteratorException(AttachmentExceptionCodes.SQL_PROBLEM.create(e, e.getMessage()));
            }
            return m;
        }

        public void close() {
            if (delegate != null) {
                delegate.close();
                return;
            }
            closeSQLStuff(rs, stmt);
            if (null != readCon) {
                releaseReadConnection(ctx, readCon);
            }
        }

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

        public void addWarning(final AbstractOXException warning) {
            warnings.add(warning);
        }

        public AbstractOXException[] getWarnings() {
            return warnings.isEmpty() ? null : warnings.toArray(new AbstractOXException[warnings.size()]);
        }

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
                    final List<Object> values = new ArrayList<Object>();
                    while (rs.next()) {
                        values.add(nextFromResult(rs));
                    }
                    closeSQLStuff(rs, stmt);
                    releaseReadConnection(ctx, readCon);
                    stmt = null;
                    readCon = null;
                    rs = null;
                    delegate = new SearchIteratorAdapter(values.iterator());
                }
            } catch (SearchIteratorException e) {
                LOG.error(e);
                this.exception = e;
            } catch (SQLException e) {
                LOG.error(e);
                this.exception = e;
            } catch (DBPoolingException e) {
                LOG.error(e);
                this.exception = e;
            }
        }
    }

    public Date getNewestCreationDate(final Context ctx, final int moduleId, final int attachedId) throws AttachmentException {
        return getNewestCreationDates(ctx, moduleId, new int[] { attachedId }).get(I(attachedId));
    }

    public Map<Integer, Date> getNewestCreationDates(final Context ctx, final int moduleId, final int[] attachedIds) throws AttachmentException {
        final Connection con;
        try {
            con = getReadConnection(ctx);
        } catch (final DBPoolingException e) {
            throw new AttachmentException(e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        final Map<Integer, Date> retval = new HashMap<Integer, Date>();
        try {
            stmt = con.prepareStatement(DBUtils.getIN(QUERIES.getSelectNewestCreationDate(), attachedIds.length) + " GROUP BY attached");
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, moduleId);
            for (final int attachedId : attachedIds) {
                stmt.setInt(pos++, attachedId);
            }
            result = stmt.executeQuery();
            while (result.next()) {
                retval.put(I(result.getInt(1)), new Date(result.getLong(2)));
            }
        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }
        return retval;
    }
}
