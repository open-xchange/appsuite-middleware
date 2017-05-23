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
import com.openexchange.exception.OXException;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Streams;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.calendar.CalendarCopyTask;
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
            CalendarCopyTask.class.getName(),
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
        final Context srcCtx = copyTools.getSourceContext();
        final Context dstCtx = copyTools.getDestinationContext();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();

        QuotaFileStorage srcFileStorage = null;
        QuotaFileStorage dstFileStorage = null;
        try {
            srcFileStorage = qfsf.getQuotaFileStorage(srcCtxId, Info.general());
            dstFileStorage = qfsf.getQuotaFileStorage(dstCtxId, Info.general());
        } catch (final OXException e) {
            throw UserCopyExceptionCodes.FILE_STORAGE_PROBLEM.create(e);
        }

        final ObjectMapping<Integer> appointmentMapping = copyTools.checkAndExtractGenericMapping(Appointment.class.getName());
        final ObjectMapping<Integer> contactMapping = copyTools.checkAndExtractGenericMapping(Contact.class.getName());
        final ObjectMapping<Integer> taskMapping = copyTools.checkAndExtractGenericMapping(Task.class.getName());
        final List<Integer> appointmentIds = new ArrayList<Integer>(appointmentMapping.getSourceKeys());
        final List<Integer> contactIds = new ArrayList<Integer>(contactMapping.getSourceKeys());
        final List<Integer> taskIds = new ArrayList<Integer>(taskMapping.getSourceKeys());
        final List<Attachment> attachments = loadAttachmentsFromDB(srcCon, i(srcCtxId), appointmentIds, contactIds, taskIds);
        copyFiles(attachments, srcFileStorage, dstFileStorage);
        exchangeIds(dstCon, attachments, appointmentMapping, contactMapping, taskMapping, i(dstUsrId), i(dstCtxId));
        writeAttachmentsToDB(dstCon, attachments, dstCtxId);

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
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
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
                LOG.warn("Unknown module {} for attachment ({}). Skipping ID exchange!", module, attachment.getId());
                continue;
            }

            try {
                final int newAttachedId = i(mapping.getDestination(I(oldAttachedId)));
                final int newId = IDGenerator.getId(cid, Types.ATTACHMENT, con);
                attachment.setId(newId);
                attachment.setCreatedBy(uid);
                attachment.setAttachedId(newAttachedId);
            } catch (final SQLException e) {
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
                    LOG.warn("Did not find file for attachment {} ({}).", attachment.getId(), attachment.getFileId());
                    continue;
                }

                final String newFileId = dstFileStorage.saveNewFile(is);
                attachment.setFileId(newFileId);
            } catch (final OXException e) {
                throw UserCopyExceptionCodes.FILE_STORAGE_PROBLEM.create(e);
            } finally {
                Streams.close(is);
            }
        }
    }

    List<Attachment> loadAttachmentsFromDB(final Connection con, final int cid, final List<Integer> appointmentIds, final List<Integer> contactIds, final List<Integer> taskIds) throws OXException {
        final List<Attachment> attachments = new ArrayList<Attachment>();
        attachments.addAll(loadAttachmentsForModule(con, appointmentIds, Types.APPOINTMENT, cid));
        attachments.addAll(loadAttachmentsForModule(con, contactIds, Types.CONTACT, cid));
        attachments.addAll(loadAttachmentsForModule(con, taskIds, Types.TASK, cid));

        return attachments;
    }

    List<Attachment> loadAttachmentsForModule(final Connection con, final List<Integer> ids, final int module, final int cid) throws OXException {
        final List<Attachment> attachments = new ArrayList<Attachment>();

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
            } catch (final SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
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
