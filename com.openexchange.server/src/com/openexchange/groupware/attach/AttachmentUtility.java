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

package com.openexchange.groupware.attach;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadSizeExceededException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Streams;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AttachmentUtility} - Utility class for attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class AttachmentUtility {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentUtility.class);

    /**
     * Initializes a new {@link AttachmentUtility}.
     */
    private AttachmentUtility() {
        super();
    }

    /**
     * Attaches available upload files to specified entity.
     *
     * @param objectId The object/entity identifier
     * @param module The module identifier; see {@link com.openexchange.groupware.Types}
     * @param folderId The folder identifier
     * @param requestData The AJAX request data
     * @param session The associated session
     * @return The identifiers of the attachments bound to specified entity
     * @throws OXException If attaching upload files fails
     */
    public static List<Integer> attachTo(final int objectId, final int module, final int folderId, final AJAXRequestData requestData, final ServerSession session) throws OXException {
        long maxUploadSize = AttachmentConfig.getMaxUploadSize();
        if (!requestData.hasUploads(-1, maxUploadSize > 0 ? maxUploadSize : -1L)) {
            return Collections.emptyList();
        }
        final UploadEvent upload = requestData.getUploadEvent(-1, maxUploadSize > 0 ? maxUploadSize : -1L);
        if (null == upload) {
            return Collections.emptyList();
        }

        final List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>(4);
        final List<UploadFile> uploadFiles = new ArrayList<UploadFile>(4);

        long sum = 0;
        int index = 0;
        for (final UploadFile uploadFile : upload.getUploadFiles()) {
            final AttachmentMetadata attachment = new AttachmentMetadataImpl(objectId, module, folderId);

            assureSize(index, attachments, uploadFiles);

            attachments.set(index, attachment);
            uploadFiles.set(index, uploadFile);
            sum += uploadFile.getSize();

            checkSize(sum, requestData);

            index++;
        }

        return attach(attachments, uploadFiles, session, session.getContext(), session.getUser(), session.getUserConfiguration());
    }

    private static List<Integer> attach(final List<AttachmentMetadata> attachments, final List<UploadFile> uploadFiles, final ServerSession session, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        initAttachments(attachments, uploadFiles);
        final List<Closeable> closeables = new LinkedList<Closeable>();
        boolean rollback = false;
        try {
            Attachment.ATTACHMENT_BASE.startTransaction();
            rollback = true;

            final Iterator<UploadFile> ufIter = uploadFiles.iterator();
            final List<Integer> ids = new LinkedList<Integer>();
            long timestamp = 0;
            for (final AttachmentMetadata attachment : attachments) {
                final UploadFile uploadFile = ufIter.next();
                attachment.setId(AttachmentBase.NEW);

                final BufferedInputStream data = new BufferedInputStream(new FileInputStream(uploadFile.getTmpFile()), 65536);
                closeables.add(data);

                final long modified = Attachment.ATTACHMENT_BASE.attachToObject(attachment, data, session, ctx, user, userConfig);
                if (modified > timestamp) {
                    timestamp = modified;
                }
                ids.add(Integer.valueOf(attachment.getId()));
            }
            Attachment.ATTACHMENT_BASE.commit();
            rollback = false;
            return ids;
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback();
            }
            finish();

            for (final Closeable closeable : closeables) {
                Streams.close(closeable);
            }
        }
    }

    private static void initAttachments(final List<AttachmentMetadata> attachments, final List<UploadFile> uploads) {
        final List<AttachmentMetadata> attList = new ArrayList<AttachmentMetadata>(attachments);
        final Iterator<UploadFile> ufIter = new ArrayList<UploadFile>(uploads).iterator();

        int index = 0;
        for (final AttachmentMetadata attachment : attList) {
            if (attachment == null) {
                attachments.remove(index);
                ufIter.next();
                uploads.remove(index);
                continue;
            }
            final UploadFile upload = ufIter.next();
            if (upload == null) {
                attachments.remove(index);
                uploads.remove(index);
                continue;
            }
            if (attachment.getFilename() == null || "".equals(attachment.getFilename())) {
                attachment.setFilename(upload.getPreparedFileName());
            }
            if (attachment.getFilesize() <= 0) {
                attachment.setFilesize(upload.getSize());
            }
            if (attachment.getFileMIMEType() == null || "".equals(attachment.getFileMIMEType())) {
                attachment.setFileMIMEType(upload.getContentType());
            }
            index++;
        }
    }

    private static void assureSize(final int index, final List<AttachmentMetadata> attachments, final List<UploadFile> uploadFiles) {
        int enlarge = index - (attachments.size() - 1);
        for (int i = 0; i < enlarge; i++) {
            attachments.add(null);
        }

        enlarge = index - (uploadFiles.size() - 1);
        for (int i = 0; i < enlarge; i++) {
            uploadFiles.add(null);
        }

    }

    private static final String CALLBACK = "callback";

    /**
     * Checks current size of uploaded data against possible quota restrictions.
     *
     * @param size The size
     * @param requestData The associated request data
     * @throws OXException If any quota restrictions are exceeded
     */
    public static void checkSize(final long size, final AJAXRequestData requestData) throws OXException {
        final long maxUploadSize = AttachmentConfig.getMaxUploadSize();
        if (maxUploadSize == 0) {
            return;
        }
        if (size > maxUploadSize) {
            if (!requestData.containsParameter(CALLBACK)) {
                requestData.putParameter(CALLBACK, "error");
            }
            throw UploadSizeExceededException.create(size, maxUploadSize, true);
        }
    }

    /**
     * Performs a roll-back on {@link Attachment#ATTACHMENT_BASE} instance.
     */
    public static void rollback() {
        try {
            Attachment.ATTACHMENT_BASE.rollback();
        } catch (final Exception e) {
            LOG.debug("Rollback failed.", e);
        }
    }

    /**
     * Performs finishing stuff on {@link Attachment#ATTACHMENT_BASE} instance.
     */
    public static void finish() {
        try {
            Attachment.ATTACHMENT_BASE.finish();
        } catch (final Exception e) {
            LOG.debug("Finishing failed.", e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------- //

    private static final class AttachmentMetadataImpl implements AttachmentMetadata {

        private int createdBy;
        private Date creationDate;
        private String fileMIMEType;
        private String filename;
        private long filesize;
        private boolean rtfFlag;
        private int objectId;
        private int moduleId;
        private int id;
        private int folderId;
        private String comment;
        private String fileId;

        /**
         * Initializes a new {@link AttachmentMetadataImpl}.
         */
        AttachmentMetadataImpl() {
            super();
        }

        /**
         * Initializes a new {@link AttachmentMetadataImpl}.
         *
         * @param attachedId The object identifier
         * @param moduleId The module identifier; see {@link com.openexchange.groupware.Types}
         * @param folderId The folder identifier
         */
        AttachmentMetadataImpl(int attachedId, int moduleId, int folderId) {
            super();
            this.objectId = attachedId;
            this.moduleId = moduleId;
            this.folderId = folderId;
        }

        @Override
        public int getCreatedBy() {
            return createdBy;
        }

        @Override
        public void setCreatedBy(final int createdBy) {
            this.createdBy = createdBy;
        }

        @Override
        public Date getCreationDate() {
            return creationDate;
        }

        @Override
        public void setCreationDate(final Date creationDate) {
            this.creationDate = creationDate;
        }

        @Override
        public String getFileMIMEType() {
            return fileMIMEType;
        }

        @Override
        public void setFileMIMEType(final String fileMIMEType) {
            this.fileMIMEType = fileMIMEType;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public void setFilename(final String filename) {
            this.filename = filename;
        }

        @Override
        public long getFilesize() {
            return filesize;
        }

        @Override
        public void setFilesize(final long filesize) {
            this.filesize = filesize;
        }

        @Override
        public int getAttachedId() {
            return objectId;
        }

        @Override
        public void setAttachedId(final int objectId) {
            this.objectId = objectId;
        }

        @Override
        public boolean getRtfFlag() {
            return rtfFlag;
        }

        @Override
        public void setRtfFlag(final boolean rtfFlag) {
            this.rtfFlag = rtfFlag;
        }

        @Override
        public int getModuleId() {
            return moduleId;
        }

        @Override
        public void setModuleId(final int moduleId) {
            this.moduleId = moduleId;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void setId(final int id) {
            this.id = id;
        }

        @Override
        public void setFolderId(final int folderId) {
            this.folderId = folderId;
        }

        @Override
        public int getFolderId() {
            return folderId;
        }

        @Override
        public void setComment(final String comment) {
            this.comment = comment;
        }

        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public void setFileId(final String fileId) {
            this.fileId = fileId;
        }

        @Override
        public String getFileId() {
            return fileId;
        }

    }

}
