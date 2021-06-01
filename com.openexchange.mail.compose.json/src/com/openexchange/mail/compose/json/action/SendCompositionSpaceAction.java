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

import java.util.ArrayList;
import java.util.List;
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
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.UploadLimits;
import com.openexchange.mail.compose.json.converter.CompositionSpaceJSONResultConverter;
import com.openexchange.mail.compose.json.util.UploadFileFileIterator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SendCompositionSpaceAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class SendCompositionSpaceAction extends AbstractMailComposeAction {

    /**
     * Initializes a new {@link SendCompositionSpaceAction}.
     *
     * @param services The service look-up
     */
    public SendCompositionSpaceAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        String sId = requestData.requireParameter("id");
        CompositionSpaceId compositionSpaceId = parseCompositionSpaceId(sId);

        CompositionSpaceService compositionSpaceService = getCompositionSpaceService(compositionSpaceId.getServiceId(), session);
        final ClientToken clientToken = getClientToken(requestData);

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
                    compositionSpaceService.updateCompositionSpace(compositionSpaceId.getId(), md, clientToken);
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
                    compositionSpaceService.updateCompositionSpace(compositionSpaceId.getId(), md, clientToken);
                }

                if (hasFileUploads) {
                    optionalUploadedAttachments = Optional.of(new UploadFileFileIterator(uploadEvent.getUploadFiles()));
                }
            }
        }

        List<OXException> warnings = new ArrayList<OXException>(4);
        MailPath mailPath = compositionSpaceService.transportCompositionSpace(compositionSpaceId.getId(), optionalUploadedAttachments, session.getUserSettingMail(), requestData, warnings, true, clientToken);

        AJAXRequestResult result;
        if (null == mailPath) {
            result = new AJAXRequestResult(new JSONObject(2).put("success", true), "json");
        } else {
            JSONObject jMailPath = CompositionSpaceJSONResultConverter.convertMailPath(mailPath);
            result =  new AJAXRequestResult(jMailPath, "json");
        }
        result.addWarnings(warnings);
        result.addWarnings(compositionSpaceService.getWarnings());
        return result;
    }

}
