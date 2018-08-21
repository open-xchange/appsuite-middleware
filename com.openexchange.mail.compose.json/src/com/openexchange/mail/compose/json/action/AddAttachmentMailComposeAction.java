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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose.json.action;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.upload.StreamedUpload;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.Attachment.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AddAttachmentMailComposeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AddAttachmentMailComposeAction extends AbstractMailComposeAction {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AddAttachmentMailComposeAction.class);
    }

    /**
     * Initializes a new {@link AddAttachmentMailComposeAction}.
     * @param services
     */
    public AddAttachmentMailComposeAction(ServiceLookup services) {
        super(services);
    }

    private boolean hasUploads(long maxFileSize, long maxSize, AJAXRequestData request) throws OXException {
        return request.hasUploads(maxFileSize, maxSize, true);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        // Require composition space identifier
        String sId = requestData.requireParameter("id");
        UUID uuid = parseCompositionSpaceId(sId);

        // Load composition space
        CompositionSpaceService compositionSpaceService = getCompositionSpaceService();

        // Determine upload quotas
        UploadLimitations uploadLimitations = getUploadLimitations(session);
        long maxSize = uploadLimitations.maxUploadSize;
        long maxFileSize = uploadLimitations.maxUploadFileSize;

        boolean hasFileUploads = hasUploads(maxFileSize, maxSize, requestData);

        StreamedUpload upload = requestData.getStreamedUpload();
        if (null == upload) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        String disposition = upload.getFormField("contentDisposition");
        if (null == disposition) {
            disposition = ContentDisposition.ATTACHMENT.getId();
        }

        if (hasFileUploads) {
            // File upload available...
            List<Attachment> newAttachments = compositionSpaceService.addAttachmentToCompositionSpace(uuid, upload.getUploadFiles(), disposition, session);
            if (newAttachments.size() == 1) {
                return new AJAXRequestResult(newAttachments.get(0), "compositionSpaceAttachment");
            }
            return new AJAXRequestResult(newAttachments, "compositionSpaceAttachment");
        }

        // No file uploads available... Expect a "JSON" form field
        JSONObject jAttachment;
        {
            String expectedJsonContent = upload.getFormField("JSON");
            if (Strings.isEmpty(expectedJsonContent)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("JSON");
            }

            jAttachment = new JSONObject(expectedJsonContent);
        }

        // Determine origin
        String origin = jAttachment.optString("origin", null);
        if (Strings.isEmpty(origin)) {
            // No origin given
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("origin");
        }

        if ("drive".equals(origin)) {
            IDBasedFileAccessFactory fileAccessFactory = services.getOptionalService(IDBasedFileAccessFactory.class);
            if (null == fileAccessFactory) {
                throw ServiceExceptionCode.absentService(IDBasedFileAccessFactory.class);
            }

            IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
            String id = jAttachment.getString("id");
            String version = jAttachment.optString("version", FileStorageFileAccess.CURRENT_VERSION);

            AttachmentDescription attachment = new AttachmentDescription();
            attachment.setCompositionSpaceId(uuid);
            attachment.setContentDisposition(Attachment.ContentDisposition.dispositionFor(disposition));
            InputStream attachmentData = parseDriveAttachment(attachment, id, version, fileAccess);
            try {
                Attachment newAttachment = compositionSpaceService.addAttachmentToCompositionSpace(uuid, attachment, attachmentData, session);
                return new AJAXRequestResult(newAttachment, "compositionSpaceAttachment");
            } finally {
                Streams.close(attachmentData);
            }
        }

        if ("contacts".equals(origin)) {
            String contactId = jAttachment.getString("id");
            String folderId = jAttachment.getString("folderId");
            Attachment newAttachment = compositionSpaceService.addContactVCardToCompositionSpace(uuid, contactId, folderId, session);
            return new AJAXRequestResult(newAttachment, "compositionSpaceAttachment");
        }

        // Unknown origin
        throw AjaxExceptionCodes.INVALID_PARAMETER.create("origin");
    }

    private InputStream parseDriveAttachment(AttachmentDescription attachment, String id, String version, IDBasedFileAccess fileAccess) throws OXException {
        FileID fileID = new FileID(id);
        if (fileAccess.supports(fileID.getService(), fileID.getAccountId(), FileStorageCapability.EFFICIENT_RETRIEVAL)) {
            Document document = fileAccess.getDocumentAndMetadata(id, version);
            if (null != document) {
                try {
                    ContentType contentType = new ContentType(document.getMimeType());
                    attachment.setMimeType(contentType.toString());
                    if (Attachment.ContentDisposition.INLINE == attachment.getContentDisposition() && contentType.startsWith("image/")) {
                        // Set a Content-Id for inline image, too
                        attachment.setContentId(UUIDs.getUnformattedStringFromRandom() + "@Open-Xchange");
                    }
                    attachment.setName(document.getName());
                    attachment.setOrigin(AttachmentOrigin.DRIVE);
                    InputStream data = document.getData();
                    document = null;
                    return data;
                } finally {
                    if (null != document && !document.isRepetitive()) {
                        Streams.close(document.getData());
                    }
                }
            }
        }

        File metadata = fileAccess.getFileMetadata(id, version);
        ContentType contentType = new ContentType(metadata.getFileMIMEType());
        attachment.setMimeType(contentType.toString());
        if (Attachment.ContentDisposition.INLINE == attachment.getContentDisposition() && contentType.startsWith("image/")) {
            // Set a Content-Id for inline image, too
            attachment.setContentId(UUIDs.getUnformattedStringFromRandom() + "@Open-Xchange");
        }
        attachment.setName(metadata.getFileName());
        attachment.setOrigin(AttachmentOrigin.DRIVE);
        return fileAccess.getDocument(id, version);
    }

}
