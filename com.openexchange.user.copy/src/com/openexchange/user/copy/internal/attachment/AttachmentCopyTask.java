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

package com.openexchange.user.copy.internal.attachment;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Streams;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.chronos.ChronosCopyTask;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.contact.ContactCopyTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.tasks.TaskCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link AttachmentCopyTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AttachmentCopyTask implements CopyUserTaskService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentCopyTask.class);

    private static final String SELECT_ATTACHMENTS =
        "SELECT " +
            "id, created_by, creation_date, file_mimetype, " +
            "file_size, filename, attached, " +
            "rtf_flag, comment, file_id " +
        "FROM " +
            "prg_attachment " +
        "WHERE " +
            "cid = ? " +
        "AND " +
            "module = ? " +
        "AND " +
            "attached IN (#IDS#)";

    private static final String INSERT_ATTACHMENTS =
        "INSERT INTO " +
            "prg_attachment " +
            "(cid, id, created_by, creation_date, file_mimetype, " +
            "file_size, filename, attached, module, " +
            "rtf_flag, comment, file_id) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final QuotaFileStorageService qfsf;


    public AttachmentCopyTask(final QuotaFileStorageService qfsf) {
        super();
        this.qfsf = qfsf;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName(),
            ChronosCopyTask.class.getName(),
            ContactCopyTask.class.getName(),
            TaskCopyTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return "attachment";
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public ObjectMapping<?> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Integer srcCtxId = copyTools.getSourceContextId();
        final Integer dstCtxId = copyTools.getDestinationContextId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();

        QuotaFileStorage srcFileStorage = null;
        QuotaFileStorage dstFileStorage = null;
        try {
            srcFileStorage = qfsf.getQuotaFileStorage(srcCtxId.intValue(), Info.general());
            dstFileStorage = qfsf.getQuotaFileStorage(dstCtxId.intValue(), Info.general());
        } catch (OXException e) {
            throw UserCopyExceptionCodes.FILE_STORAGE_PROBLEM.create(e);
        }

        final ObjectMapping<Integer> appointmentMapping = copyTools.checkAndExtractGenericMapping(Event.class.getName());
        final ObjectMapping<Integer> contactMapping = copyTools.checkAndExtractGenericMapping(Contact.class.getName());
        final ObjectMapping<Integer> taskMapping = copyTools.checkAndExtractGenericMapping(Task.class.getName());
        final List<Integer> appointmentIds = new ArrayList<>(appointmentMapping.getSourceKeys());
        final List<Integer> contactIds = new ArrayList<>(contactMapping.getSourceKeys());
        final List<Integer> taskIds = new ArrayList<>(taskMapping.getSourceKeys());
        final List<Attachment> attachments = loadAttachmentsFromDB(srcCon, i(srcCtxId), appointmentIds, contactIds, taskIds);
        copyFiles(attachments, srcFileStorage, dstFileStorage);
        exchangeIds(dstCon, attachments, appointmentMapping, contactMapping, taskMapping, i(dstUsrId), i(dstCtxId));
        writeAttachmentsToDB(dstCon, attachments, dstCtxId.intValue());

        return null;
    }

    void writeAttachmentsToDB(final Connection con, final List<Attachment> attachments, final int cid) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_ATTACHMENTS);
            for (final Attachment attachment : attachments) {
                int i = 1;
                stmt.setInt(i++, cid);
                stmt.setInt(i++, attachment.getId());
                stmt.setInt(i++, attachment.getCreatedBy());
                stmt.setLong(i++, attachment.getCreationDate().getTime());
                stmt.setString(i++, attachment.getFileMIMEType());
                stmt.setLong(i++, attachment.getFilesize());
                stmt.setString(i++, attachment.getFilename());
                stmt.setInt(i++, attachment.getAttachedId());
                stmt.setInt(i++, attachment.getModuleId());
                CopyTools.setIntOrNull(i++, stmt, attachment.getRtfFlag());
                CopyTools.setStringOrNull(i++, stmt, attachment.getComment());
                stmt.setString(i++, attachment.getFileId());

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    void exchangeIds(final Connection con, final List<Attachment> attachments, final ObjectMapping<Integer> appointmentMapping, final ObjectMapping<Integer> contactMapping, final ObjectMapping<Integer> taskMapping, final int uid, final int cid) throws OXException {
        for (final Attachment attachment : attachments) {
            final int oldAttachedId = attachment.getAttachedId();
            final int module = attachment.getModuleId();
            final ObjectMapping<Integer> mapping;
            switch (module) {
                case Types.APPOINTMENT:
                mapping = appointmentMapping;
                break;

                case Types.CONTACT:
                mapping = contactMapping;
                break;

                case Types.TASK:
                mapping = taskMapping;
                break;

                default:
                mapping = null;
                break;
            }

            if (mapping == null) {
                LOG.warn("Unknown module {} for attachment ({}). Skipping ID exchange!", I(module), I(attachment.getId()));
                continue;
            }

            try {
                final int newAttachedId = i(mapping.getDestination(I(oldAttachedId)));
                final int newId = IDGenerator.getId(cid, Types.ATTACHMENT, con);
                attachment.setId(newId);
                attachment.setCreatedBy(uid);
                attachment.setAttachedId(newAttachedId);
            } catch (SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            }

        }
    }

    void copyFiles(final List<Attachment> attachments, final QuotaFileStorage srcFileStorage, final QuotaFileStorage dstFileStorage) throws OXException {
        for (final Attachment attachment : attachments) {
            InputStream is = null;
            try {
                is = srcFileStorage.getFile(attachment.getFileId());
                if (is == null) {
                    LOG.warn("Did not find file for attachment {} ({}).", I(attachment.getId()), attachment.getFileId());
                    continue;
                }

                final String newFileId = dstFileStorage.saveNewFile(is);
                attachment.setFileId(newFileId);
            } catch (OXException e) {
                throw UserCopyExceptionCodes.FILE_STORAGE_PROBLEM.create(e);
            } finally {
                Streams.close(is);
            }
        }
    }

    List<Attachment> loadAttachmentsFromDB(final Connection con, final int cid, final List<Integer> appointmentIds, final List<Integer> contactIds, final List<Integer> taskIds) throws OXException {
        final List<Attachment> attachments = new ArrayList<>();
        attachments.addAll(loadAttachmentsForModule(con, appointmentIds, Types.APPOINTMENT, cid));
        attachments.addAll(loadAttachmentsForModule(con, contactIds, Types.CONTACT, cid));
        attachments.addAll(loadAttachmentsForModule(con, taskIds, Types.TASK, cid));

        return attachments;
    }

    List<Attachment> loadAttachmentsForModule(final Connection con, final List<Integer> ids, final int module, final int cid) throws OXException {
        final List<Attachment> attachments = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        if (!ids.isEmpty()) {
            try {
                final String sql = CopyTools.replaceIdsInQuery("#IDS#", SELECT_ATTACHMENTS, ids);
                stmt = con.prepareStatement(sql);
                stmt.setInt(1, cid);
                stmt.setInt(2, module);

                rs = stmt.executeQuery();
                while (rs.next()) {
                    final Attachment attachment = new Attachment();
                    int i = 1;
                    attachment.setId(rs.getInt(i++));
                    attachment.setCreatedBy(rs.getInt(i++));
                    attachment.setCreationDate(new Date(rs.getLong(i++)));
                    attachment.setFileMIMEType(rs.getString(i++));
                    attachment.setFilesize(rs.getInt(i++));
                    attachment.setFilename(rs.getString(i++));
                    attachment.setAttachedId(rs.getInt(i++));
                    attachment.setModuleId(module);
                    attachment.setRtfFlag(CopyTools.getIntOrNegative(i++, rs));
                    attachment.setComment(rs.getString(i++));
                    attachment.setFileId(rs.getString(i++));

                    attachments.add(attachment);
                }
            } catch (SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        }

        return attachments;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
