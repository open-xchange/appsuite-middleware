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

package com.openexchange.mail.compose.json.converter;

import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentResult;
import com.openexchange.mail.compose.CompositionSpaceInfo;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AttachmentJSONResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentJSONResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link AttachmentJSONResultConverter}.
     */
    public AttachmentJSONResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "compositionSpaceAttachment";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        try {
            AttachmentResult attachmentResult = (AttachmentResult) result.getResultObject();
            List<? extends Attachment> attachments = attachmentResult.getAttachments();

            int numberOfAttachments = attachments.size();
            if (numberOfAttachments <= 0) {
                // No attachments at all
                JSONObject jResponse = new JSONObject(2);
                jResponse.put("attachments", JSONArray.EMPTY_ARRAY);
                jResponse.put("compositionSpace", convertCompositionSpaceInfo(attachmentResult.getCompositionSpaceInfo()));
                result.setResultObject(jResponse, "json");
                return;
            }

            if (numberOfAttachments == 1) {
                // A single attachment
                JSONObject jResponse = new JSONObject(2);
                jResponse.put("attachments", new JSONArray(1).put(convertAttachment(attachments.get(0))));
                jResponse.put("compositionSpace", convertCompositionSpaceInfo(attachmentResult.getCompositionSpaceInfo()));
                result.setResultObject(jResponse, "json");
                return;
            }

            // Collection of attachments
            JSONObject jResponse = new JSONObject(2);
            {
                JSONArray jAttachments = new JSONArray(numberOfAttachments);
                for (Attachment attachment : attachments) {
                    jAttachments.put(convertAttachment(attachment));
                }
                jResponse.put("attachments", jAttachments);
            }
            jResponse.put("compositionSpace", convertCompositionSpaceInfo(attachmentResult.getCompositionSpaceInfo()));
            result.setResultObject(jResponse, "json");
            return;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Converts specified attachment to its JSON representation.
     *
     * @param attachment The attachment
     * @return The JSON representation
     * @throws JSONException If conversion fails
     */
    public static JSONObject convertAttachment(Attachment attachment) throws JSONException {
        if (null == attachment) {
            return null;
        }

        JSONObject jAttachment = new JSONObject(8);
        jAttachment.putOpt("id", UUIDs.getUnformattedString(attachment.getId()));
        jAttachment.putOpt("name", attachment.getName());
        jAttachment.put("size", attachment.getSize());
        jAttachment.putOpt("mimeType", attachment.getMimeType());
        jAttachment.putOpt("cid", attachment.getContentId());
        jAttachment.putOpt("contentDisposition", attachment.getContentDisposition());
        jAttachment.putOpt("origin", attachment.getOrigin());
        return jAttachment;
    }

    /**
     * Converts specified composition space information to its JSON representation.
     *
     * @param compositionSpaceInfo The composition space information
     * @return The JSON representation
     * @throws JSONException If conversion fails
     */
    public static JSONObject convertCompositionSpaceInfo(CompositionSpaceInfo compositionSpaceInfo) throws JSONException {
        if (null == compositionSpaceInfo) {
            return null;
        }

        JSONObject json = new JSONObject(2);
        json.putOpt("id", compositionSpaceInfo.getId().toString());
        Optional<MailPath> optionalPath = compositionSpaceInfo.getMailPath();
        if (optionalPath.isPresent()) {
            json.putOpt("mailPath", CompositionSpaceJSONResultConverter.convertMailPath(optionalPath.get()));
        }
        return json;
    }

}
