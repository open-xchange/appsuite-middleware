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

package com.openexchange.mail.compose.json.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.Address;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaceStorageService;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.DefaultAttachment;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentsInfo;
import com.openexchange.mail.compose.Attachment.ContentDisposition;
import com.openexchange.mail.compose.Message.ContentType;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMailComposeAction} - Abstract mail compose action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMailComposeAction implements AJAXActionService {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {

        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMailComposeAction.class);
    }

    /**
     * The service look-up
     */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractMailComposeAction}.
     *
     * @param services The service look-up
     */
    protected AbstractMailComposeAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the composition space service.
     *
     * @return The composition space service
     * @throws OXException If composition space service cannot be returned
     */
    protected CompositionSpaceService getCompositionSpaceService() throws OXException {
        CompositionSpaceService service = services.getService(CompositionSpaceService.class);
        if (null == service) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(CompositionSpaceStorageService.class.getName());
        }
        return service;
    }

    /**
     * Parses specified JSON representation of a message to given <code>MessageDescription</code> instance.
     *
     * @param jMessage The message's JSON representation
     * @param md The <code>MessageDescription</code> instance to parse to
     * @throws JSONException If a JSON error occurs
     */
    protected static void parseJSONMessage(JSONObject jMessage, MessageDescription md) throws JSONException {
        {
            JSONArray jFrom = jMessage.optJSONArray("from");
            if (null != jFrom) {
                JSONArray jAddress = jFrom.optJSONArray(0);
                md.setFrom(toAddress(null == jAddress ? jFrom : jAddress));
            }
        }

        {
            JSONArray jTo = jMessage.optJSONArray("to");
            if (null != jTo) {
                md.setTo(toAddresses(jTo));
            }
        }

        {
            JSONArray jCc = jMessage.optJSONArray("cc");
            if (null != jCc) {
                md.setCc(toAddresses(jCc));
            }
        }

        {
            JSONArray jBcc = jMessage.optJSONArray("bcc");
            if (null != jBcc) {
                md.setBcc(toAddresses(jBcc));
            }
        }

        {
            String subject = jMessage.optString("subject", null);
            if (null != subject) {
                md.setSubject(subject);
            }
        }

        {
            String content = jMessage.optString("content", null);
            if (null != content) {
                md.setContent(content);
            }
        }

        {
            String contentType = jMessage.optString("contentType", null);
            if (null != contentType) {
                md.setContentType(ContentType.contentTypeFor(contentType));
            }
        }

        // Hm... Should we allow manually altering attachment references?
        //        {
        //            JSONArray jAttachments = jMessage.optJSONArray("attachments");
        //            if (null != jAttachments) {
        //                md.setAttachments(toAttachments(jAttachments, compositionSpaceId));
        //            }
        //        }

        // Meta must not be changed by clients

        {
            Object opt = jMessage.optRaw("requestReadReceipt");
            Boolean bool = JSONObject.booleanFor(opt);
            if (null != bool) {
                md.setRequestReadReceipt(bool.booleanValue());
            }
        }

        {
            String priority = jMessage.optString("priority", null);
            if (null != priority) {
                Priority p = Priority.priorityFor(priority);
                md.setPriority(p == null ? Priority.NORMAL : p);
            }
        }

        {
            JSONObject jSecurity = jMessage.optJSONObject("security");
            if (null != jSecurity) {
                md.setSecurity(toSecurity(jSecurity));
            }
        }

        {
            JSONObject jSharedAttachments = jMessage.optJSONObject("sharedAttachments");
            if (null != jSharedAttachments) {
                md.setsharedAttachmentsInfo(toSharedAttachmentsInfo(jSharedAttachments));
            }
        }
    }

    private static List<Address> toAddresses(JSONArray jAddresses) throws JSONException {
        List<Address> addresses = new ArrayList<Address>(jAddresses.length());
        for (Object jAddress : jAddresses) {
            addresses.add(toAddress((JSONArray) jAddress));
        }
        return addresses;
    }

    private static Address toAddress(JSONArray jAddress) throws JSONException {
        return new Address(jAddress.optString(0, null), jAddress.getString(1));
    }

    private static List<Attachment> toAttachments(JSONArray jAttachments, UUID compositionSpaceId) throws JSONException, OXException {
        List<Attachment> attachments = new ArrayList<Attachment>(jAttachments.length());
        for (Object jAttachment : jAttachments) {
            attachments.add(toAttachment((JSONObject) jAttachment, compositionSpaceId));
        }
        return attachments;
    }

    private static Attachment toAttachment(JSONObject jAttachment, UUID compositionSpaceId) throws JSONException, OXException {
        String sId = jAttachment.getString("id");
        UUID uuid = parseAttachmentId(sId);

        DefaultAttachment.Builder attachment = DefaultAttachment.builder(uuid);
        attachment.withCompositionSpaceId(compositionSpaceId);

        {
            String mimeType = jAttachment.optString("mimeType", null);
            if (null != mimeType) {
                attachment.withMimeType(mimeType);
            }
        }

        {
            String disposition = jAttachment.optString("contentDisposition", null);
            if (null != disposition) {
                attachment.withContentDisposition(ContentDisposition.dispositionFor(disposition));
            }
        }

        {
            String name = jAttachment.optString("name", null);
            if (null != name) {
                attachment.withName(name);
            }
        }

        {
            long size = jAttachment.optLong("size", -1L);
            if (size >= 0) {
                attachment.withSize(size);
            }
        }

        {
            String cid = jAttachment.optString("cid", null);
            if (null != cid) {
                attachment.withContentId(cid);
            }
        }

        {
            String origin = jAttachment.optString("origin", null);
            if (null != origin) {
                attachment.withOrigin(AttachmentOrigin.getOriginFor(origin));
            }
        }

        return attachment.build();
    }

    /**
     * Parses given JSON object to shared attachments information
     *
     * @param jSharedAttachments The JSON object to parse
     * @return The parsed shared attachments information
     */
    protected static SharedAttachmentsInfo toSharedAttachmentsInfo(JSONObject jSharedAttachments) {
        SharedAttachmentsInfo.Builder sharedAttachments = SharedAttachmentsInfo.builder();

        sharedAttachments.withEnabled(jSharedAttachments.optBoolean("enabled", false));

        {
            String language = jSharedAttachments.optString("language", null);
            if (null != language) {
                sharedAttachments.withLanguage(LocaleTools.getLocale(language));
            }
        }

        sharedAttachments.withAutoDelete(jSharedAttachments.optBoolean("autodelete", false));

        {
            long expiryDate = jSharedAttachments.optLong("expiryDate", -1L);
            if (expiryDate >= 0) {
                sharedAttachments.withExpiryDate(new Date(expiryDate));
            }
        }

        {
            String password = jSharedAttachments.optString("password", null);
            if (null != password) {
                sharedAttachments.withPassword(password);
            }
        }

        return sharedAttachments.build();
    }

    /**
     * Parses given JSON object to security settings
     *
     * @param jSecurity The JSON object to parse
     * @return The parsed security settings
     */
    protected static Security toSecurity(JSONObject jSecurity) {
        return Security.builder()
            .withEncrypt(jSecurity.optBoolean("encrypt"))
            .withPgpInline(jSecurity.optBoolean("pgpInline"))
            .withSign(jSecurity.optBoolean("sign"))
            .withLanguage(jSecurity.optString("language", null))
            .withMessage(jSecurity.optString("message", null))
            .withPin(jSecurity.optString("pin", null))
            .withMsgRef(jSecurity.optString("msgRef", null))
            .build();
    }

    /**
     * Requires a JSON content in given request's data.
     *
     * @param requestData The request data to read from
     * @return The JSON content
     * @throws OXException If JSON content cannot be returned
     */
    protected JSONValue requireJSONBody(AJAXRequestData requestData) throws OXException {
        Object data = requestData.getData();
        if (null == data) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        JSONValue jBody = requestData.getData(JSONValue.class);
        if (null == jBody) {
            throw AjaxExceptionCodes.INVALID_REQUEST_BODY.create(JSONValue.class, data.getClass());
        }
        return (JSONValue) data;
    }

    /**
     * Gets the referenced mail from given request data.
     *
     * @param requestData The request data to extract from
     * @return The mail path for the referenced mail
     * @throws OXException If mail path for the referenced mail cannot be returned
     */
    protected MailPath requireReferencedMail(AJAXRequestData requestData) throws OXException {
        JSONValue jBody = requireJSONBody(requestData);
        try {
            if (jBody.isObject()) {
                JSONObject jMailPath = jBody.toObject();
                return new MailPath(jMailPath.getString("folderId"), jMailPath.getString("id"));
            }

            JSONObject jMailPath = jBody.toArray().getJSONObject(0);
            return new MailPath(jMailPath.getString("folderId"), jMailPath.getString("id"));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the referenced mails from given request data.
     *
     * @param requestData The request data to extract from
     * @return The mail paths for the referenced mail
     * @throws OXException If mail paths for the referenced mail cannot be returned
     */
    protected List<MailPath> requireReferencedMails(AJAXRequestData requestData) throws OXException {
        JSONValue jBody = requireJSONBody(requestData);
        try {
            if (jBody.isObject()) {
                JSONObject jMailPath = jBody.toObject();
                String folderId = jMailPath.getString("folderId");
                String mailId = jMailPath.getString("id");
                return Collections.singletonList(new MailPath(folderId, mailId));
            }

            JSONArray jMailPaths = jBody.toArray();
            int length = jMailPaths.length();
            List<MailPath> maiLPaths = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                JSONObject jMailPath = jMailPaths.getJSONObject(i);
                maiLPaths.add(new MailPath(jMailPath.getString("folderId"), jMailPath.getString("id")));
            }
            return maiLPaths;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the upload limitations for the maximum allowed size of a single uploaded file and the maximum allowed size of a complete request.
     *
     * @param session The session
     * @return The upload limitations
     */
    protected UploadLimitations getUploadLimitations(ServerSession session) {
        // Determine upload quotas
        long maxSize;
        long maxFileSize;
        {
            UserSettingMail usm = session.getUserSettingMail();
            maxFileSize = usm.getUploadQuotaPerFile();
            if (maxFileSize <= 0) {
                maxFileSize = -1L;
            }
            maxSize = usm.getUploadQuota();
            if (maxSize <= 0) {
                if (maxSize == 0) {
                    maxSize = -1L;
                } else {
                    LoggerHolder.LOG.debug("Upload quota is less than zero. Using global server property \"MAX_UPLOAD_SIZE\" instead.");
                    long globalQuota;
                    try {
                        globalQuota = ServerConfig.getLong(Property.MAX_UPLOAD_SIZE).longValue();
                    } catch (OXException e) {
                        LoggerHolder.LOG.error("", e);
                        globalQuota = 0L;
                    }
                    maxSize = globalQuota <= 0 ? -1L : globalQuota;
                }
            }
        }
        return new UploadLimitations(maxFileSize, maxSize);
    }

    /**
     * Gets a composition space's UUID from specified unformatted string.
     *
     * @param id The composition space identifier as an unformatted string; e.g. <code>067e61623b6f4ae2a1712470b63dff00</code>
     * @return The UUID
     * @throws OXException If passed string in invalid
     */
    protected static UUID parseCompositionSpaceId(String id) throws OXException {
        return CompositionSpaces.parseCompositionSpaceId(id);
    }

    /**
     * Gets an attachment's UUID from specified unformatted string.
     *
     * @param id The attachment identifier as an unformatted string; e.g. <code>067e61623b6f4ae2a1712470b63dff00</code>
     * @return The UUID
     * @throws OXException If passed string in invalid
     */
    protected static UUID parseAttachmentId(String id) throws OXException {
        return CompositionSpaces.parseAttachmentId(id);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            return doPerform(requestData, session);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs given mail compose request.
     *
     * @param requestData The request data
     * @param session The session providing user information
     * @return The AJAX result
     * @throws OXException If performing request fails
     */
    protected abstract AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException;

}
