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

package com.openexchange.mail.compose.json.action;

import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.openexchange.mail.compose.Attachment.ContentDisposition;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.AttachmentResult;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.ContentId;
import com.openexchange.mail.compose.UploadLimits;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tx.TransactionAwares;


/**
 * {@link AddAttachmentMailComposeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AddAttachmentMailComposeAction extends AbstractMailComposeAction {

    /**
     * Initializes a new {@link AddAttachmentMailComposeAction}.
     *
     * @param services The service look-up
     */
    public AddAttachmentMailComposeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        // Require composition space identifier
        String sId = requestData.requireParameter("id");
        CompositionSpaceId compositionSpaceId = parseCompositionSpaceId(sId);

        // Load composition space
        CompositionSpaceService compositionSpaceService = getCompositionSpaceService(compositionSpaceId.getServiceId(), session);

        // Determine upload quotas
        UploadLimits uploadLimits = compositionSpaceService.getAttachmentUploadLimits(compositionSpaceId.getId());
        boolean hasFileUploads = hasUploads(uploadLimits, requestData);

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
            AttachmentResult attachmentResult = compositionSpaceService.addAttachmentToCompositionSpace(
                compositionSpaceId.getId(), upload.getUploadFiles(), disposition, getClientToken(requestData));
            return new AJAXRequestResult(attachmentResult, "compositionSpaceAttachment").addWarnings(compositionSpaceService.getWarnings());
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
            try {
                String id = jAttachment.getString("id");
                String version = jAttachment.optString("version", FileStorageFileAccess.CURRENT_VERSION);
                AttachmentDescription attachment = new AttachmentDescription();
                attachment.setCompositionSpaceId(compositionSpaceId.getId());
                attachment.setContentDisposition(Attachment.ContentDisposition.dispositionFor(disposition));
                InputStream attachmentData = parseDriveAttachment(attachment, id, version, fileAccess);
                try {
                    AttachmentResult attachmentResult = compositionSpaceService.addAttachmentToCompositionSpace(compositionSpaceId.getId(), attachment, attachmentData, getClientToken(requestData));
                    return new AJAXRequestResult(attachmentResult, "compositionSpaceAttachment").addWarnings(compositionSpaceService.getWarnings());
                } finally {
                    Streams.close(attachmentData);
                }
            } finally {
                TransactionAwares.finishSafe(fileAccess);
            }
        }

        if ("contacts".equals(origin)) {
            String contactId = jAttachment.getString("id");
            String folderId = jAttachment.getString("folderId");
            AttachmentResult attachmentResult = compositionSpaceService.addContactVCardToCompositionSpace(
                compositionSpaceId.getId(), contactId, folderId, getClientToken(requestData));
            return new AJAXRequestResult(attachmentResult, "compositionSpaceAttachment").addWarnings(compositionSpaceService.getWarnings());
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
                    attachment.setMimeType(contentType.getBaseType());
                    if (Attachment.ContentDisposition.INLINE == attachment.getContentDisposition() && contentType.startsWith("image/")) {
                        // Set a Content-Id for inline image, too
                        attachment.setContentId(ContentId.valueOf(UUIDs.getUnformattedStringFromRandom() + "@Open-Xchange"));
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
        attachment.setMimeType(contentType.getBaseType());
        if (Attachment.ContentDisposition.INLINE == attachment.getContentDisposition() && contentType.startsWith("image/")) {
            // Set a Content-Id for inline image, too
            attachment.setContentId(ContentId.valueOf(UUIDs.getUnformattedStringFromRandom() + "@Open-Xchange"));
        }
        attachment.setName(metadata.getFileName());
        attachment.setOrigin(AttachmentOrigin.DRIVE);
        return fileAccess.getDocument(id, version);
    }

}
