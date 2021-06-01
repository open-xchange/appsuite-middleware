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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.Address;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.Message.ContentType;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentsInfo;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CompositionSpaceJSONResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CompositionSpaceJSONResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link CompositionSpaceJSONResultConverter}.
     */
    public CompositionSpaceJSONResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "compositionSpace";
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
            final Object resultObject = result.getResultObject();
            if (resultObject instanceof CompositionSpace) {
                CompositionSpace compositionSpace = (CompositionSpace) resultObject;
                result.setResultObject(convertCompositionSpace(compositionSpace, null), "json");
                return;
            }
            /*
             * Collection of composition spaces
             */
            @SuppressWarnings("unchecked") Collection<CompositionSpace> compositionSpaces = (Collection<CompositionSpace>) resultObject;
            Set<MessageField> optFields = (Set<MessageField>) result.getParameter("columns");
            final JSONArray jArray = new JSONArray(compositionSpaces.size());
            for (CompositionSpace compositionSpace : compositionSpaces) {
                jArray.put(convertCompositionSpace(compositionSpace, optFields));
            }
            result.setResultObject(jArray, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject convertCompositionSpace(CompositionSpace compositionSpace, Set<MessageField> optFields) throws JSONException {
        final JSONObject json = new JSONObject(16);
        json.put("id", compositionSpace.getId().toString());

        Message message = compositionSpace.getMessage();

        // Mail path
        Optional<MailPath> optionalPath = compositionSpace.getMailPath();
        if (optionalPath.isPresent()) {
            json.putOpt("mailPath", convertMailPath(optionalPath.get()));
        }

        // Addresses
        if (null == optFields || optFields.contains(MessageField.FROM)) {
            json.putOpt("from", convertAddress(message.getFrom()));
        }
        if (null == optFields || optFields.contains(MessageField.SENDER)) {
            json.putOpt("sender", convertAddress(message.getSender()));
        }
        if (null == optFields || optFields.contains(MessageField.REPLY_TO)) {
            json.putOpt("reply_to", convertAddress(message.getReplyTo()));
        }
        if (null == optFields || optFields.contains(MessageField.TO)) {
            json.putOpt("to", convertAddresses(message.getTo()));
        }
        if (null == optFields || optFields.contains(MessageField.CC)) {
            json.putOpt("cc", convertAddresses(message.getCc()));
        }
        if (null == optFields || optFields.contains(MessageField.BCC)) {
            json.putOpt("bcc", convertAddresses(message.getBcc()));
        }

        // Subject
        if (null == optFields || optFields.contains(MessageField.SUBJECT)) {
            json.putOpt("subject", message.getSubject());
        }

        // Content
        if (null == optFields || optFields.contains(MessageField.CONTENT)) {
            json.putOpt("content", message.getContent());
        }
        if (null == optFields || optFields.contains(MessageField.CONTENT_TYPE)) {
            json.putOpt("contentType", convertContentType(message.getContentType()));
        }

        // Attachment identifiers
        if (null == optFields || optFields.contains(MessageField.ATTACHMENTS)) {
            json.putOpt("attachments", convertAttachments(message.getAttachments()));
        }

        // Meta
        if (null == optFields || optFields.contains(MessageField.META)) {
            json.putOpt("meta", convertMeta(message.getMeta()));
        }

        // Custom headers
        if (null == optFields || optFields.contains(MessageField.CUSTOM_HEADERS)) {
            json.putOpt("customHeaders", convertCustomHeders(message.getCustomHeaders()));
        }

        // Read receipt
        if (null == optFields || optFields.contains(MessageField.REQUEST_READ_RECEIPT)) {
            json.put("requestReadReceipt", message.isRequestReadReceipt());
        }

        // Priority
        if (null == optFields || optFields.contains(MessageField.PRIORITY)) {
            json.put("priority", convertPriority(message.getPriority()));
        }

        // Security
        if (null == optFields || optFields.contains(MessageField.SECURITY)) {
            json.putOpt("security", convertSecurity(message.getSecurity()));
        }

        // Shared attachments information
        if (null == optFields || optFields.contains(MessageField.SHARED_ATTACCHMENTS_INFO)) {
            json.putOpt("sharedAttachments", convertSharedAttachments(message.getSharedAttachments()));
        }

        return json;
    }

    private JSONArray convertAddresses(List<Address> addresses) {
        if (null == addresses) {
            return null;
        }

        JSONArray jAddresses = new JSONArray(addresses.size());
        for (Address address : addresses) {
            if (null != address) {
                jAddresses.put(convertAddress(address));
            }
        }
        return jAddresses;
    }

    private JSONArray convertAddress(Address address) {
        if (null == address) {
            return null;
        }

        return new JSONArray(2).put(null == address.getPersonal() ? JSONObject.NULL : address.getPersonal()).put(address.getAddress());
    }

    private JSONArray convertAttachments(List<Attachment> attachments) throws JSONException {
        if (null == attachments) {
            return null;
        }

        JSONArray jAttachments = new JSONArray(attachments.size());
        for (Attachment attachment : attachments) {
            if (null != attachment) {
                jAttachments.put(AttachmentJSONResultConverter.convertAttachment(attachment));
            }
        }
        return jAttachments;
    }

    private JSONObject convertMeta(Meta meta) throws JSONException {
        if (null == meta) {
            return null;
        }

        JSONObject jMeta = new JSONObject(8);
        jMeta.putOpt("type", meta.getType().getId());
        jMeta.putOpt("date", getNullable(meta.getDate()));

        MailPath replyFor = meta.getReplyFor();
        if (null != replyFor) {
            JSONObject jReplyFor = new JSONObject(3);
            jReplyFor.putOpt("originalId", replyFor.getMailID());
            jReplyFor.putOpt("originalFolderId", replyFor.getFolderArgument());
            jMeta.put("replyFor", jReplyFor);
        }

        MailPath editFor = meta.getEditFor();
        if (null != editFor) {
            JSONObject jEditFor = new JSONObject(3);
            jEditFor.putOpt("originalId", editFor.getMailID());
            jEditFor.putOpt("originalFolderId", editFor.getFolderArgument());
            jMeta.put("editFor", jEditFor);
        }

        List<MailPath> forwardsFor = meta.getForwardsFor();
        if (null != forwardsFor) {
            JSONArray jForwardsFor = new JSONArray(forwardsFor.size());
            for (MailPath forwardFor : forwardsFor) {
                JSONObject jForwardFor = new JSONObject(3);
                jForwardFor.putOpt("originalId", forwardFor.getMailID());
                jForwardFor.putOpt("originalFolderId", forwardFor.getFolderArgument());
                jForwardsFor.put(jForwardFor);
            }
            jMeta.putOpt("forwardsFor", jForwardsFor);
        }
        return jMeta;
    }

    private JSONObject convertCustomHeders(Map<String, String> customHeaders) throws JSONException {
        if (null == customHeaders) {
            return null;
        }

        JSONObject jCustomHeaders = new JSONObject(customHeaders.size());
        for (Map.Entry<String, String> customHeader : customHeaders.entrySet()) {
            jCustomHeaders.put(customHeader.getKey(), customHeader.getValue());
        }
        return jCustomHeaders;
    }

    private JSONObject convertSecurity(Security security) throws JSONException {
        if (null == security) {
            return null;
        }

        JSONObject jSecurity = new JSONObject(9);
        jSecurity.put("encrypt", security.isEncrypt());
        jSecurity.put("pgpInline", security.isPgpInline());
        jSecurity.put("sign", security.isSign());
        jSecurity.put("language", getNullable(security.getLanguage()));
        jSecurity.put("message", getNullable(security.getMessage()));
        jSecurity.put("pin", getNullable(security.getPin()));
        jSecurity.put("msgRef", getNullable(security.getMsgRef()));
        return jSecurity;
    }

    private JSONObject convertSharedAttachments(SharedAttachmentsInfo sharedAttachments) throws JSONException {
        if (null == sharedAttachments) {
            return null;
        }

        JSONObject jSharedAttachments = new JSONObject(6);
        jSharedAttachments.put("enabled", sharedAttachments.isEnabled());
        jSharedAttachments.put("language", getNullable(sharedAttachments.getLanguage()));
        jSharedAttachments.put("autodelete", sharedAttachments.isAutoDelete());
        jSharedAttachments.putOpt("expiryDate", getNullable(sharedAttachments.getExpiryDate()));
        jSharedAttachments.put("password", getNullable(sharedAttachments.getPassword()));
        return jSharedAttachments;
    }

    private String convertContentType(ContentType contentType) {
        return null == contentType ? null : contentType.getId();
    }

    private String convertPriority(Priority priority) {
        return (null == priority ? Priority.NORMAL : priority).getId();
    }

    private Long getNullable(Date value) {
        return null == value ? null : Long.valueOf(value.getTime());
    }

    private String getNullable(String value) {
        return null == value ? "" : value;
    }

    private Object getNullable(Object value) {
        return null == value ? JSONObject.NULL : value.toString();
    }

    /**
     * Converts a {@link MailPath} instance to a JSON object of format
     * <code>{"folderId": "default0/INBOX/Drafts", "id": "195"}</code>.
     *
     * @param mailPath The mail path
     * @return The JSON representation
     * @throws JSONException
     */
    public static JSONObject convertMailPath(MailPath mailPath) throws JSONException {
        JSONObject jMailPath = new JSONObject(3);
        jMailPath.putOpt("id", mailPath.getMailID());
        jMailPath.putOpt("folderId", mailPath.getFolderArgument());
        return jMailPath;
    }

}
