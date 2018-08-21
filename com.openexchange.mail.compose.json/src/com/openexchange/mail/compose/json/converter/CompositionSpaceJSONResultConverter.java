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

package com.openexchange.mail.compose.json.converter;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
            @SuppressWarnings("unchecked")
            Collection<CompositionSpace> compositionSpaces = (Collection<CompositionSpace>) resultObject;
            Set<MessageField> optFields = (Set<MessageField>) result.getParameter("columns");
            final JSONArray jArray = new JSONArray(compositionSpaces.size());
            for (CompositionSpace compositionSpace : compositionSpaces) {
                jArray.put(convertCompositionSpace(compositionSpace, optFields));
            }
            result.setResultObject(jArray, "json");
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject convertCompositionSpace(CompositionSpace compositionSpace, Set<MessageField> optFields) throws JSONException {
        final JSONObject json = new JSONObject(16);
        json.put("id", UUIDs.getUnformattedString(compositionSpace.getId()));

        Message message = compositionSpace.getMessage();

        // Addresses
        if (null == optFields || optFields.contains(MessageField.FROM)) {
            json.putOpt("from", convertAddress(message.getFrom()));
        }
        if (null == optFields || optFields.contains(MessageField.SENDER)) {
            json.putOpt("sender", convertAddress(message.getSender()));
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
        jMeta.putOpt("type", meta.getType().name());
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

    private JSONObject convertSecurity(Security security) throws JSONException {
        if (null == security) {
            return null;
        }

        JSONObject jSecurity = new JSONObject(8);
        jSecurity.put("encrypt", security.isEncrypt());
        jSecurity.put("pgpInline", security.isPgpInline());
        jSecurity.put("sign", security.isSign());
        jSecurity.put("language", getNullable(security.getLanguage()));
        jSecurity.put("message", getNullable(security.getMessage()));
        jSecurity.put("pin", getNullable(security.getPin()));
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

}
