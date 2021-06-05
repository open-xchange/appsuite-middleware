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

import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.StreamedUpload;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.Attachment.ContentDisposition;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.UploadLimits;
import com.openexchange.mail.compose.json.converter.CompositionSpaceJSONResultConverter;
import com.openexchange.mail.compose.json.util.UploadFileFileIterator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SaveDraftCompositionSpaceAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class SaveDraftCompositionSpaceAction extends AbstractMailComposeAction {

    /**
     * Initializes a new {@link SaveDraftCompositionSpaceAction}.
     *
     * @param services The service look-up
     */
    public SaveDraftCompositionSpaceAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        String sId = requestData.requireParameter("id");
        CompositionSpaceId compositionSpaceId = parseCompositionSpaceId(sId);

        CompositionSpaceService compositionSpaceService = getCompositionSpaceService(compositionSpaceId.getServiceId(), session);

        // Check for optional body data
        Optional<StreamedUploadFileIterator> optionalUploadedAttachments = Optional.empty();
        {
            // Determine upload quotas
            UploadLimits uploadLimits = compositionSpaceService.getAttachmentUploadLimits(compositionSpaceId.getId());

            boolean hasFileUploads = requestData.hasUploads(uploadLimits.getPerAttachmentLimit(), uploadLimits.getPerRequestLimit(), true);
            StreamedUpload upload = null;
            UploadEvent uploadEvent = null;
            try {
                upload = requestData.getStreamedUpload();
            } catch (OXException e) {
                if (!UploadException.UploadCode.FAILED_STREAMED_UPLOAD.equals(e)) {
                    throw e;
                }
                uploadEvent = requestData.getUploadEvent();
            }
            if (null != upload) {
                String disposition = upload.getFormField("contentDisposition");
                if (null == disposition) {
                    disposition = ContentDisposition.ATTACHMENT.getId();
                }

                // Check for JSON data
                JSONObject jMessage = null;
                {
                    String expectedJsonContent = upload.getFormField("JSON");
                    if (Strings.isNotEmpty(expectedJsonContent)) {
                        jMessage = new JSONObject(expectedJsonContent);
                    }
                }

                if (null != jMessage) {
                    MessageDescription md = new MessageDescription();
                    parseJSONMessage(jMessage, md);
                    compositionSpaceService.updateCompositionSpace(compositionSpaceId.getId(), md, getClientToken(requestData));
                }

                if (hasFileUploads) {
                    optionalUploadedAttachments = Optional.of(upload.getUploadFiles());
                }
            } else if (uploadEvent != null) {
                String disposition = uploadEvent.getFormField("contentDisposition");
                if (null == disposition) {
                    disposition = ContentDisposition.ATTACHMENT.getId();
                }

                // Check for JSON data
                JSONObject jMessage = null;
                {
                    String expectedJsonContent = uploadEvent.getFormField("JSON");
                    if (Strings.isNotEmpty(expectedJsonContent)) {
                        jMessage = new JSONObject(expectedJsonContent);
                    }
                }

                if (null != jMessage) {
                    MessageDescription md = new MessageDescription();
                    parseJSONMessage(jMessage, md);
                    compositionSpaceService.updateCompositionSpace(compositionSpaceId.getId(), md, getClientToken(requestData));
                }

                if (hasFileUploads) {
                    optionalUploadedAttachments = Optional.of(new UploadFileFileIterator(uploadEvent.getUploadFiles()));
                }
            }
        }

        MailPath mailPath = compositionSpaceService.saveCompositionSpaceToDraftMail(
            compositionSpaceId.getId(), optionalUploadedAttachments, true, getClientToken(requestData));
        JSONObject jMailPath = CompositionSpaceJSONResultConverter.convertMailPath(mailPath);
        return new AJAXRequestResult(jMailPath, "json").addWarnings(compositionSpaceService.getWarnings());
    }

}
