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

package com.openexchange.chronos.storage.rdb.legacy;

import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.tools.arrays.Collections.put;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.storage.AttachmentStorage;
import com.openexchange.chronos.storage.rdb.RdbStorage;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentMetadataFactory;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link RdbAttachmentStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAttachmentStorage extends RdbStorage implements AttachmentStorage {

    private static final int MODULE_ID = com.openexchange.groupware.Types.APPOINTMENT;
    private static final AttachmentMetadataFactory METADATA_FACTORY = new AttachmentMetadataFactory();

    /**
     * Initializes a new {@link RdbAttachmentStorage}.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbAttachmentStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
    }

    @Override
    public List<Attachment> loadAttachments(String objectID) throws OXException {
        return loadAttachments(new String[] { objectID }).get(objectID);
    }

    @Override
    public Map<String, List<Attachment>> loadAttachments(String[] objectIDs) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAttachments(connection, context.getContextId(), objectIDs);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Set<String> hasAttachments(String[] eventIds) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectHasAttachments(connection, context.getContextId(), eventIds);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void deleteAttachments(Session session, String folderID, String eventID) throws OXException {
        ServerSession serverSession = checkSession(session);
        AttachmentBase attachmentBase = initAttachmentBase();
        serverSession.setParameter(AttachmentStorage.class.getName(), Boolean.TRUE);
        try {
            attachmentBase.startTransaction();
            List<Integer> attachmentIDs = new ArrayList<Integer>();
            TimedResult<AttachmentMetadata> timedResult = attachmentBase.getAttachments(
                session, asInt(folderID), asInt(eventID), MODULE_ID, new AttachmentField[] { AttachmentField.ID_LITERAL }, null, 0,
                context, serverSession.getUser(), serverSession.getUserConfiguration());
            SearchIterator<AttachmentMetadata> iterator = null;
            try {
                iterator = timedResult.results();
                while (iterator.hasNext()) {
                    AttachmentMetadata metadata = iterator.next();
                    if (metadata != null) {
                        attachmentIDs.add(I(metadata.getId()));
                    }
                }
            } finally {
                SearchIterators.close(iterator);
            }
            if (0 < attachmentIDs.size()) {
                attachmentBase.detachFromObject(asInt(folderID), asInt(eventID), MODULE_ID, I2i(attachmentIDs),
                    session, context, serverSession.getUser(), serverSession.getUserConfiguration());
            }
            attachmentBase.commit();
        } finally {
            serverSession.setParameter(AttachmentStorage.class.getName(), null);
            attachmentBase.finish();
        }
    }

    @Override
    public void deleteAttachments(Session session, String folderID, String eventID, List<Attachment> attachments) throws OXException {
        if (null == attachments || 0 == attachments.size()) {
            return;
        }
        ServerSession serverSession = checkSession(session);
        AttachmentBase attachmentBase = initAttachmentBase();
        serverSession.setParameter(AttachmentStorage.class.getName(), Boolean.TRUE);
        try {
            attachmentBase.startTransaction();
            attachmentBase.detachFromObject(asInt(folderID), asInt(eventID), MODULE_ID, getAttachmentIds(attachments),
                session, context, serverSession.getUser(), serverSession.getUserConfiguration());
            attachmentBase.commit();
        } finally {
            serverSession.setParameter(AttachmentStorage.class.getName(), null);
            attachmentBase.finish();
        }
    }

    @Override
    public void deleteAttachments(Session session, Map<String, Map<String, List<Attachment>>> attachmentsByEventPerFolderId) throws OXException {
        if (null == attachmentsByEventPerFolderId || 0 == attachmentsByEventPerFolderId.size()) {
            return;
        }
        ServerSession serverSession = checkSession(session);
        AttachmentBase attachmentBase = initAttachmentBase();
        serverSession.setParameter(AttachmentStorage.class.getName(), Boolean.TRUE);
        try {
            attachmentBase.startTransaction();
            for (Map.Entry<String, Map<String, List<Attachment>>> entry : attachmentsByEventPerFolderId.entrySet()) {
                int folderId = asInt(entry.getKey());
                for (Map.Entry<String, List<Attachment>> attachmentsByEvent : entry.getValue().entrySet()) {
                    attachmentBase.detachFromObject(folderId, asInt(attachmentsByEvent.getKey()), MODULE_ID, getAttachmentIds(attachmentsByEvent.getValue()),
                        session, context, serverSession.getUser(), serverSession.getUserConfiguration());
                }
            }
            attachmentBase.commit();
        } finally {
            serverSession.setParameter(AttachmentStorage.class.getName(), null);
            attachmentBase.finish();
        }
    }

    @Override
    public void insertAttachments(Session session, String folderID, String eventID, List<Attachment> attachments) throws OXException {
        if (null == attachments || 0 == attachments.size()) {
            return;
        }
        ServerSession serverSession = checkSession(session);
        AttachmentBase attachmentBase = initAttachmentBase();
        serverSession.setParameter(AttachmentStorage.class.getName(), Boolean.TRUE);
        try {
            attachmentBase.startTransaction();
            /*
             * store new binary attachments
             */
            for (Attachment attachment : filterBinary(attachments)) {
                AttachmentMetadata metadata = getMetadata(attachment, asInt(folderID), asInt(eventID));
                InputStream inputStream = null;
                try {
                    inputStream = attachment.getData().getStream();
                    attachmentBase.attachToObject(metadata, inputStream, session, context, serverSession.getUser(), serverSession.getUserConfiguration());
                } finally {
                    Streams.close(inputStream);
                }
            }
            /*
             * copy over referenced managed attachments
             */
            for (Attachment attachment : filterManaged(attachments)) {
                AttachmentMetadata metadata = getMetadata(attachment, asInt(folderID), asInt(eventID));
                metadata.setId(AttachmentBase.NEW);
                InputStream inputStream = null;
                try {
                    inputStream = loadAttachmentData(attachment.getManagedId());
                    attachmentBase.attachToObject(metadata, inputStream, session, context, serverSession.getUser(), serverSession.getUserConfiguration());
                } finally {
                    Streams.close(inputStream);
                }
            }
            attachmentBase.commit();
        } finally {
            serverSession.setParameter(AttachmentStorage.class.getName(), null);
            attachmentBase.finish();
        }
    }

    @Override
    public InputStream loadAttachmentData(int attachmentID) throws OXException {
        String fileID;
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            fileID = selectFileID(connection, context.getContextId(), attachmentID);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
        if (null == fileID) {
            throw AttachmentExceptionCodes.ATTACHMENT_NOT_FOUND.create();
        }
        return getFileStorage().getFile(fileID);
    }

    @Override
    public String resolveAttachmentId(int managedId) throws OXException {
        String eventId;
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            eventId = selectEventID(connection, context.getContextId(), managedId);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
        if (null == eventId) {
            throw AttachmentExceptionCodes.ATTACHMENT_NOT_FOUND.create();
        }
        return eventId;
    }

    private QuotaFileStorage getFileStorage() throws OXException, OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        if (null == storageService) {
            throw AttachmentExceptionCodes.FILESTORE_DOWN.create();
        }
        return storageService.getQuotaFileStorage(context.getContextId(), Info.general());
    }

    private ServerSession checkSession(Session session) throws OXException {
        if (null == session || session.getContextId() != context.getContextId()) {
            throw new UnsupportedOperationException();
        }
        return ServerSessionAdapter.valueOf(session);
    }

    private AttachmentBase initAttachmentBase() {
        return Attachments.getInstance(dbProvider, true);
    }

    private static Map<String, List<Attachment>> selectAttachments(Connection connection, int contextID, String[] objectIDs) throws SQLException {
        Map<String, List<Attachment>> attachmentsById = new HashMap<String, List<Attachment>>();
        String sql = new StringBuilder()
            .append("SELECT attached,id,file_mimetype,file_size,filename,file_id,creation_date FROM prg_attachment ")
            .append("WHERE cid=? AND attached IN (").append(getParameters(objectIDs.length)).append(") AND module=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (String objectID : objectIDs) {
                stmt.setInt(parameterIndex++, asInt(objectID));
            }
            stmt.setInt(parameterIndex++, MODULE_ID);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    Attachment attachment = new Attachment();
                    attachment.setManagedId(resultSet.getInt("id"));
                    attachment.setFormatType(resultSet.getString("file_mimetype"));
                    attachment.setSize(resultSet.getLong("file_size"));
                    attachment.setFilename(resultSet.getString("filename"));
                    //                    attachment.setContentId(resultSet.getString("file_id"));
                    attachment.setCreated(new Date(resultSet.getLong("creation_date")));
                    put(attachmentsById, asString(resultSet.getInt("attached")), attachment);
                }
            }
        }
        return attachmentsById;
    }

    private static Set<String> selectHasAttachments(Connection connection, int contextID, String[] eventIds) throws SQLException {
        if (null == eventIds || 0 == eventIds.length) {
            return Collections.emptySet();
        }
        Set<String> eventIdsWithAttachment = new HashSet<String>();
        String sql = new StringBuilder()
            .append("SELECT DISTINCT(attached) FROM prg_attachment ")
            .append("WHERE cid=? AND attached").append(Databases.getPlaceholders(eventIds.length)).append(';')
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (String id : eventIds) {
                stmt.setInt(parameterIndex++, asInt(id));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    eventIdsWithAttachment.add(String.valueOf(resultSet.getInt("attached")));
                }
            }
        }
        return eventIdsWithAttachment;
    }

    private static String selectFileID(Connection connection, int contextID, int attachmentID) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT file_id FROM prg_attachment WHERE cid=? AND id=? AND module=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, attachmentID);
            stmt.setInt(3, MODULE_ID);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        }
    }

    private static String selectEventID(Connection connection, int contextID, int attachmentID) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT attached FROM prg_attachment WHERE cid=? AND id=? AND module=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, attachmentID);
            stmt.setInt(3, MODULE_ID);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                return resultSet.next() ? String.valueOf(resultSet.getInt(1)) : null;
            }
        }
    }

    private static List<Attachment> filterBinary(List<Attachment> attachments) {
        List<Attachment> binaryAttachments = new ArrayList<Attachment>();
        for (Attachment attachment : attachments) {
            if (null != attachment.getData()) {
                binaryAttachments.add(attachment);
            }
        }
        return binaryAttachments;
    }

    private static List<Attachment> filterManaged(List<Attachment> attachments) {
        List<Attachment> managedAttachments = new ArrayList<Attachment>();
        for (Attachment attachment : attachments) {
            if (0 < attachment.getManagedId()) {
                managedAttachments.add(attachment);
            }
        }
        return managedAttachments;
    }

    private static AttachmentMetadata getMetadata(Attachment attachment, int folderID, int eventID) {
        AttachmentMetadata metadata = METADATA_FACTORY.newAttachmentMetadata();
        metadata.setModuleId(MODULE_ID);
        metadata.setId(attachment.getManagedId());
        metadata.setFolderId(folderID);
        metadata.setAttachedId(eventID);
        if (null != attachment.getFormatType()) {
            metadata.setFileMIMEType(attachment.getFormatType());
        } else if (null != attachment.getData()) {
            metadata.setFileMIMEType(attachment.getData().getContentType());
        }
        if (null != attachment.getFilename()) {
            metadata.setFilename(attachment.getFilename());
        } else if (null != attachment.getData()) {
            metadata.setFilename(attachment.getData().getName());
        }
        if (0 < attachment.getSize()) {
            metadata.setFilesize(attachment.getSize());
        } else if (null != attachment.getData()) {
            metadata.setFilesize(attachment.getData().getLength());
        }
        return metadata;
    }

    private static int[] getAttachmentIds(List<Attachment> attachments) {
        List<Integer> attachmentIDs = new ArrayList<Integer>(attachments.size());
        for (Attachment attachment : attachments) {
            attachmentIDs.add(I(attachment.getManagedId()));
        }
        return I2i(attachmentIDs);
    }

}
