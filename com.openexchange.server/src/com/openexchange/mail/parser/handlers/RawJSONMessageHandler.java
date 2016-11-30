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

package com.openexchange.mail.parser.handlers;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.decodeMultiEncodedHeader;
import static com.openexchange.mail.parser.MailMessageParser.generateFilename;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.parser.ContentProvider;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link RawJSONMessageHandler} - Generates a raw JSON message representation without any user-sensitive data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RawJSONMessageHandler implements MailMessageHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RawJSONMessageHandler.class);

    /**
     * The max. allowed body size of 16 KB.
     */
    private static final int MAX_BODY_SIZE = 0x4000;

    private final int accountId;

    private final MailPath mailPath;

    private JSONObject jsonObject;

    private long totalConsumedBodySize;

    private JSONArray bodyArr;

    private JSONArray attachmentsArr;

    private JSONArray nestedMsgsArr;

    private boolean bodyAdded;

    private boolean textWasEmpty;

    private boolean isAlternative;

    private String altId;

    /**
     * Initializes a new {@link RawJSONMessageHandler}.
     *
     * @param accountId The account ID
     * @param mailPath The unique mail path
     * @throws OXException If JSON message handler cannot be initialized
     */
    public RawJSONMessageHandler(final int accountId, final String mailPath) throws OXException {
        super();
        this.accountId = accountId;
        this.mailPath = new MailPath(mailPath);
        jsonObject = new JSONObject();
    }

    /**
     * Initializes a new {@link RawJSONMessageHandler}.
     *
     * @param accountId The account ID
     * @param mailPath The unique mail path
     * @param mail The mail message to add JSON fields not set by message parser traversal
     * @throws OXException If JSON message handler cannot be initialized
     */
    public RawJSONMessageHandler(final int accountId, final MailPath mailPath, final MailMessage mail) throws OXException {
        super();
        this.accountId = accountId;
        this.mailPath = mailPath;
        jsonObject = new JSONObject();
        try {
            if (null != mail) {
                /*
                 * Add missing fields
                 */
                if (mail.containsFolder() && mail.getMailId() != null) {
                    jsonObject.put(FolderChildFields.FOLDER_ID, prepareFullname(accountId, mail.getFolder()));
                    jsonObject.put(DataFields.ID, mail.getMailId());
                }
                jsonObject.put(
                    MailJSONField.HAS_ATTACHMENTS.getKey(),
                    mail.containsHasAttachment() ? mail.hasAttachment() : mail.getContentType().isMimeType(MimeTypes.MIME_MULTIPART_MIXED));
                jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), mail.getContentType().getBaseType());
                jsonObject.put(MailJSONField.SIZE.getKey(), mail.getSize());
                // jsonObject.put(MailJSONField.THREAD_LEVEL.getKey(), mail.getThreadLevel());
                jsonObject.put(MailJSONField.ACCOUNT_NAME.getKey(), mail.getAccountName());
                jsonObject.put(MailJSONField.ACCOUNT_ID.getKey(), mail.getAccountId());
                if (mail.containsOriginalId()) {
                    String originalId = mail.getOriginalId();
                    if (null != originalId) {
                        jsonObject.put(MailJSONField.ORIGINAL_ID.getKey(), originalId);
                    }
                }
                if (mail.containsOriginalFolder()) {
                    String originalFolder = mail.getOriginalFolder();
                    if (null != originalFolder) {
                        jsonObject.put(MailJSONField.ORIGINAL_FOLDER_ID.getKey(), originalFolder);
                    }
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private long addConsumedBodySize(final long consumedBodySize) {
        this.totalConsumedBodySize += consumedBodySize;
        return totalConsumedBodySize;
    }

    private void discardJSONObject() {
        bodyArr = null;
        attachmentsArr = null;
        nestedMsgsArr = null;
        jsonObject = null;
    }

    private JSONArray getAttachmentsArr() throws JSONException {
        if (attachmentsArr == null) {
            attachmentsArr = new JSONArray();
            jsonObject.put(MailJSONField.ATTACHMENTS.getKey(), attachmentsArr);
        }
        return attachmentsArr;
    }

    private JSONArray getBodyArr() throws JSONException {
        if (bodyArr == null) {
            bodyArr = new JSONArray();
            jsonObject.put("body", bodyArr);
        }
        return bodyArr;
    }

    private JSONArray getNestedMsgsArr() throws JSONException {
        if (nestedMsgsArr == null) {
            nestedMsgsArr = new JSONArray();
            jsonObject.put(MailJSONField.NESTED_MESSAGES.getKey(), nestedMsgsArr);
        }
        return nestedMsgsArr;
    }

    @Override
    public boolean handleMultipartEnd(final MailPart mp, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleAttachment(final MailPart part, final boolean isInline, final String baseContentType, final String fileName, final String id) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            /*
             * Sequence ID
             */
            jsonObject.put(MailListField.ID.getKey(), part.containsSequenceId() ? part.getSequenceId() : id);
            /*
             * Filename
             */
            if (fileName == null) {
                final Object val;
                if (isInline) {
                    val = JSONObject.NULL;
                } else {
                    val = generateFilename(id, baseContentType);
                    part.setFileName((String) val);
                }
                jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), val);
            } else {
                jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), fileName);
            }
            /*
             * Size
             */
            if (part.containsSize()) {
                jsonObject.put(MailJSONField.SIZE.getKey(), part.getSize());
            }
            /*
             * Disposition
             */
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
            /*
             * Content-ID
             */
            if (part.containsContentId() && part.getContentId() != null) {
                jsonObject.put(MailJSONField.CID.getKey(), part.getContentId());
            }
            /*
             * Content-Type
             */
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), part.getContentType().toString());
            /*
             * Content
             */
            jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
            // if (isInline &&
            // part.getContentType().isMimeType(MIMETypes.MIME_TEXT_ALL)) {
            // // TODO: Add rtf2html conversion here!
            // if (part.getContentType().isMimeType(MIMETypes.MIME_TEXT_RTF)) {
            // jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
            // } else {
            // final String charset =
            // part.getContentType().containsCharsetParameter() ?
            // part.getContentType()
            // .getCharsetParameter() : MailConfig.getDefaultMimeCharset();
            // jsonObject.put(MailJSONField.CONTENT.getKey(),
            // MessageUtility.formatContentForDisplay(
            // MessageUtility.readMailPart(part, charset),
            // part.getContentType().isMimeType(
            // MIMETypes.MIME_TEXT_HTM_ALL), session, mailPath,
            // displayVersion));
            // }
            // } else {
            // jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
            // }
            getAttachmentsArr().put(jsonObject);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleBccRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        try {
            jsonObject.put(MailJSONField.RECIPIENT_BCC.getKey(), MessageWriter.getAddressesAsArray(recipientAddrs));
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleCcRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        try {
            jsonObject.put(MailJSONField.RECIPIENT_CC.getKey(), MessageWriter.getAddressesAsArray(recipientAddrs));
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleColorLabel(final int colorLabel) throws OXException {
        try {
            jsonObject.put(MailJSONField.COLOR_LABEL.getKey(), colorLabel);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleContentId(final String contentId) throws OXException {
        try {
            jsonObject.put(MailJSONField.CID.getKey(), contentId);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
        try {
            jsonObject.put(MailJSONField.FROM.getKey(), MessageWriter.getAddressesAsArray(fromAddrs));
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    /**
     * These headers are covered by fields of {@link MailMessage}
     */
    private static final Set<HeaderName> COVERED_HEADER_NAMES =
        ImmutableSet.of(
            MessageHeaders.CONTENT_DISPOSITION, MessageHeaders.CONTENT_ID, MessageHeaders.CONTENT_TYPE, MessageHeaders.BCC,
            MessageHeaders.CC, MessageHeaders.DATE, MessageHeaders.DISP_NOT_TO, MessageHeaders.FROM, MessageHeaders.X_PRIORITY,
            MessageHeaders.SUBJECT, MessageHeaders.TO);

    @Override
    public boolean handleHeaders(final int size, final Iterator<Entry<String, String>> iter) throws OXException {
        if (size == 0) {
            return true;
        }
        try {
            final JSONObject hdrObject = new JSONObject();
            for (int i = 0; i < size; i++) {
                final Map.Entry<String, String> entry = iter.next();
                final String headerName = entry.getKey();
                if (MessageHeaders.HDR_DISP_NOT_TO.equalsIgnoreCase(headerName)) {
                    /*
                     * This special header is handled through handleDispositionNotification()
                     */
                    continue;
                } else if (MessageHeaders.HDR_IMPORTANCE.equalsIgnoreCase(headerName)) {
                    /*
                     * Priority
                     */
                    int priority = MailMessage.PRIORITY_NORMAL;
                    if (null != entry.getValue()) {
                        priority = MimeMessageConverter.parseImportance(entry.getValue());
                        jsonObject.put(MailJSONField.PRIORITY.getKey(), priority);
                    }
                } else if (MessageHeaders.HDR_X_PRIORITY.equalsIgnoreCase(headerName)) {
                    if (!jsonObject.has(MailJSONField.PRIORITY.getKey())) {
                        /*
                         * Priority
                         */
                        int priority = MailMessage.PRIORITY_NORMAL;
                        if (null != entry.getValue()) {
                            priority = MimeMessageConverter.parsePriority(entry.getValue());
                        }
                        jsonObject.put(MailJSONField.PRIORITY.getKey(), priority);
                    }
                } else if (MessageHeaders.HDR_X_MAILER.equalsIgnoreCase(headerName)) {
                    hdrObject.put(headerName, entry.getValue());
                } else if (MessageHeaders.HDR_X_OX_VCARD.equalsIgnoreCase(headerName)) {
                    jsonObject.put(MailJSONField.VCARD.getKey(), true);
                } else if (MessageHeaders.HDR_X_OX_NOTIFICATION.equalsIgnoreCase(headerName)) {
                    jsonObject.put(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(), entry.getValue());
                } else {
                    if (!COVERED_HEADER_NAMES.contains(HeaderName.valueOf(headerName))) {
                        if (hdrObject.has(headerName)) {
                            final Object previous = hdrObject.get(headerName);
                            if (previous instanceof JSONArray) {
                                final JSONArray ja = (JSONArray) previous;
                                ja.put(decodeMultiEncodedHeader(entry.getValue()));
                            } else {
                                final JSONArray ja = new JSONArray();
                                ja.put(previous);
                                ja.put(decodeMultiEncodedHeader(entry.getValue()));
                                hdrObject.put(headerName, ja);
                            }
                        } else {
                            hdrObject.put(headerName, decodeMultiEncodedHeader(entry.getValue()));
                        }
                    }
                }
            }
            jsonObject.put(MailJSONField.HEADERS.getKey(), hdrObject);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleImagePart(final MailPart part, final String imageCID, final String baseContentType, final boolean isInline, final String fileName, final String id) throws OXException {
        return handleAttachment(part, isInline, baseContentType, fileName, id);
    }

    @Override
    public boolean handleInlineHtml(final ContentProvider contentProvider, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        /*
         * Append HTML content as-is
         */
        String htmlContent = contentProvider.getContent();
        if (bodyAdded) {
            if (isAlternative) {
                if (containsContent("text/htm")) {
                    asAttachment(id, contentType.getBaseType(), size, fileName, null);
                } else {
                    /*
                     * Check size
                     */
                    if (addConsumedBodySize(htmlContent.length()) > MAX_BODY_SIZE) {
                        discardJSONObject();
                        return false;
                    }
                    asRawContent(id, contentType.getBaseType(), htmlContent, fileName);
                }
            } else {
                asAttachment(id, contentType.getBaseType(), size, fileName, null);
            }
        } else {
            /*
             * Check size
             */
            if (addConsumedBodySize(htmlContent.length()) > MAX_BODY_SIZE) {
                discardJSONObject();
                return false;
            }
            asRawContent(id, contentType.getBaseType(), htmlContent, fileName);
            bodyAdded = true;
        }
        return true;
    }

    @Override
    public boolean handleInlinePlainText(final String plainTextContentArg, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        /*
         * Append plain-text content as-is
         */
        if (bodyAdded) {
            if (isAlternative) {
                if (containsContent(contentType.getBaseType())) {
                    if (textWasEmpty) {
                        /*
                         * Check size
                         */
                        if (addConsumedBodySize(null == plainTextContentArg ? 0 : plainTextContentArg.length()) > MAX_BODY_SIZE) {
                            discardJSONObject();
                            return false;
                        }
                        replaceEmptyContent(id, contentType.getBaseType(), plainTextContentArg);
                        textWasEmpty = (null == plainTextContentArg || 0 == plainTextContentArg.length());
                    } else {
                        asAttachment(id, contentType.getBaseType(), size, fileName, null);
                    }
                } else {
                    /*
                     * Check size
                     */
                    if (addConsumedBodySize(null == plainTextContentArg ? 0 : plainTextContentArg.length()) > MAX_BODY_SIZE) {
                        discardJSONObject();
                        return false;
                    }
                    asRawContent(id, contentType.getBaseType(), plainTextContentArg, fileName);
                    textWasEmpty = (null == plainTextContentArg || 0 == plainTextContentArg.length());
                }
            } else {
                if (textWasEmpty) {
                    /*
                     * Check size
                     */
                    if (addConsumedBodySize(null == plainTextContentArg ? 0 : plainTextContentArg.length()) > MAX_BODY_SIZE) {
                        discardJSONObject();
                        return false;
                    }
                    replaceEmptyContent(id, contentType.getBaseType(), plainTextContentArg);
                    textWasEmpty = (null == plainTextContentArg || 0 == plainTextContentArg.length());
                } else {
                    asAttachment(id, contentType.getBaseType(), size, fileName, plainTextContentArg);
                }
            }
        } else {
            /*
             * Check size
             */
            if (addConsumedBodySize(null == plainTextContentArg ? 0 : plainTextContentArg.length()) > MAX_BODY_SIZE) {
                discardJSONObject();
                return false;
            }
            asRawContent(id, contentType.getBaseType(), plainTextContentArg, fileName);
            textWasEmpty = (null == plainTextContentArg || 0 == plainTextContentArg.length());
            bodyAdded = true;
        }
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            String contentType = MimeTypes.MIME_APPL_OCTET;
            final String filename = part.getFileName();
            try {
                final Locale locale = Locale.ENGLISH;
                contentType = MimeType2ExtMap.getContentType(new File(filename.toLowerCase(locale)).getName()).toLowerCase(locale);
            } catch (final Exception e) {
                final Throwable t =
                    new Throwable(
                        new StringBuilder("Unable to fetch content/type for '").append(filename).append("': ").append(e).toString());
                LOG.warn("", t);
            }
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), contentType);
            jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), filename);
            jsonObject.put(MailJSONField.SIZE.getKey(), part.getFileSize());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
            /*
             * Content-type indicates mime type text/
             */
            if (contentType.startsWith("text/")) {
                final String content = part.getPart().toString();
                /*
                 * Check size
                 */
                if (addConsumedBodySize(content.length()) > MAX_BODY_SIZE) {
                    discardJSONObject();
                    return false;
                }
                /*
                 * Attach link-object with text content
                 */
                jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            } else {
                /*
                 * Attach link-object.
                 */
                jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
            }
            getAttachmentsArr().put(jsonObject);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType, final int size, final String fileName, final String id) throws OXException {
        return handleInlinePlainText(decodedTextContent, contentType, size, fileName, id);
    }

    @Override
    public void handleMessageEnd(final MailMessage mail) throws OXException {
        /*
         * Since we obviously touched message's content, mark its corresponding message object as seen
         */
        mail.setFlags(mail.getFlags() | MailMessage.FLAG_SEEN);
        if (null != jsonObject) {
            try {
                jsonObject.put("alternative", isAlternative);
                if (null != mailPath) {
                    jsonObject.put(MailJSONField.MSGREF.getKey(), mailPath.toString());
                }
            } catch (final JSONException e) {
                // Cannot occur
                LOG.error("", e);
            }
        }
    }

    @Override
    public boolean handleMultipart(final MailPart mp, final int bodyPartCount, final String id) throws OXException {
        /*
         * Determine if message is of MIME type multipart/alternative
         */
        if (mp.getContentType().startsWith(MimeTypes.MIME_MULTIPART_ALTERNATIVE) && bodyPartCount >= 2) {
            isAlternative = true;
            altId = id;
        } else if (null != altId && !id.startsWith(altId)) {
            /*
             * No more within multipart/alternative since current ID is not nested below remembered ID
             */
            isAlternative = false;
        }
        return true;
    }

    @Override
    public boolean handleNestedMessage(final MailPart mailPart, final String id) throws OXException {
        try {
            final Object content = mailPart.getContent();
            final MailMessage nestedMail;
            if (content instanceof MailMessage) {
                nestedMail = (MailMessage) content;
            } else if (content instanceof InputStream) {
                try {
                    nestedMail =
                        MimeMessageConverter.convertMessage(new MimeMessage(MimeDefaultSession.getDefaultSession(), (InputStream) content));
                } catch (final MessagingException e) {
                    throw MimeMailException.handleMessagingException(e);
                }
            } else {
                final StringBuilder sb = new StringBuilder(128);
                sb.append("Ignoring nested message.").append(
                    "Cannot handle part's content which should be a RFC822 message according to its content type: ");
                sb.append((null == content ? "null" : content.getClass().getSimpleName()));
                LOG.error(sb.toString());
                return true;
            }
            final RawJSONMessageHandler msgHandler = new RawJSONMessageHandler(accountId, null, null);
            msgHandler.totalConsumedBodySize = totalConsumedBodySize;
            new MailMessageParser().parseMailMessage(nestedMail, msgHandler, id);
            if ((totalConsumedBodySize = msgHandler.totalConsumedBodySize) > MAX_BODY_SIZE) {
                discardJSONObject();
                return false;
            }
            final JSONObject nestedObject = msgHandler.getJSONObject();
            /*
             * Sequence ID
             */
            nestedObject.put(MailListField.ID.getKey(), mailPart.containsSequenceId() ? mailPart.getSequenceId() : id);
            /*
             * Filename (if present)
             */
            if (mailPart.containsFileName()) {
                final String name = mailPart.getFileName();
                if (null != name) {
                    nestedObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), name);
                }
            }
            getNestedMsgsArr().put(nestedObject);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handlePriority(final int priority) throws OXException {
        try {
            jsonObject.put(MailJSONField.PRIORITY.getKey(), priority);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleMsgRef(final String msgRef) throws OXException {
        try {
            jsonObject.put(MailJSONField.MSGREF.getKey(), msgRef);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleDispositionNotification(final InternetAddress dispositionNotificationTo, final boolean seen) throws OXException {
        try {
            if (!seen) {
                jsonObject.put(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(), dispositionNotificationTo.toUnicodeString());
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleReceivedDate(final Date receivedDate) throws OXException {
        try {
            jsonObject.put(
                MailJSONField.RECEIVED_DATE.getKey(),
                receivedDate == null ? JSONObject.NULL : Long.valueOf(receivedDate.getTime()));
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleSentDate(final Date sentDate) throws OXException {
        try {
            jsonObject.put(MailJSONField.SENT_DATE.getKey(), sentDate == null ? JSONObject.NULL : Long.valueOf(sentDate.getTime()));
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleSpecialPart(final MailPart part, final String baseContentType, final String fileName, final String id) throws OXException {
        /*-
         *
        if (false && !textAppended && part.getContentType().isMimeType(MIMETypes.MIME_TEXT_ALL)) {
            String charset = part.getContentType().getCharsetParameter();
            if (null == charset) {
                charset = CharsetDetector.detectCharset(part.getInputStream());
            }
            try {
                return handleInlinePlainText(
                    MessageUtility.readMailPart(part, charset),
                    part.getContentType(),
                    part.getSize(),
                    fileName,
                    id);
            } catch (final IOException e) {
                throw new OXException(OXException.Code.IO_ERROR, e, e.getMessage());
            }
        }
         */
        /*
         * When creating a JSON message object from a message we do not distinguish special parts or image parts from "usual" attachments.
         * Therefore invoke the handleAttachment method. Maybe we need a separate handling in the future for vcards.
         */
        return handleAttachment(part, false, baseContentType, fileName, id);
    }

    @Override
    public boolean handleSubject(final String subject) throws OXException {
        try {
            jsonObject.put(MailJSONField.SUBJECT.getKey(), subject == null ? JSONObject.NULL : subject.trim());
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleSystemFlags(final int flags) throws OXException {
        try {
            if (jsonObject.hasAndNotNull(MailJSONField.FLAGS.getKey())) {
                final int prevFlags = jsonObject.getInt(MailJSONField.FLAGS.getKey());
                jsonObject.put(MailJSONField.FLAGS.getKey(), prevFlags | flags);
            } else {
                jsonObject.put(MailJSONField.FLAGS.getKey(), flags);
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleToRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        try {
            jsonObject.put(MailJSONField.RECIPIENT_TO.getKey(), MessageWriter.getAddressesAsArray(recipientAddrs));
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleUserFlags(final String[] userFlags) throws OXException {
        if (userFlags == null) {
            return true;
        }
        try {
            final JSONArray userFlagsArr = new JSONArray();
            for (final String userFlag : userFlags) {
                if (MailMessage.isColorLabel(userFlag)) {
                    jsonObject.put(MailJSONField.COLOR_LABEL.getKey(), MailMessage.getColorLabelIntValue(userFlag));
                } else if (MailMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                    if (jsonObject.hasAndNotNull(MailJSONField.FLAGS.getKey())) {
                        final int flags = jsonObject.getInt(MailJSONField.FLAGS.getKey());
                        jsonObject.put(MailJSONField.FLAGS.getKey(), flags | MailMessage.FLAG_FORWARDED);
                    } else {
                        jsonObject.put(MailJSONField.FLAGS.getKey(), MailMessage.FLAG_FORWARDED);
                    }
                } else if (MailMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                    if (jsonObject.hasAndNotNull(MailJSONField.FLAGS.getKey())) {
                        final int flags = jsonObject.getInt(MailJSONField.FLAGS.getKey());
                        jsonObject.put(MailJSONField.FLAGS.getKey(), flags | MailMessage.FLAG_READ_ACK);
                    } else {
                        jsonObject.put(MailJSONField.FLAGS.getKey(), MailMessage.FLAG_READ_ACK);
                    }
                } else {
                    userFlagsArr.put(userFlag);
                }
            }
            jsonObject.put(MailJSONField.USER.getKey(), userFlagsArr);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the filled instance of {@link JSONObject}
     *
     * @return The filled instance of {@link JSONObject}
     */
    public JSONObject getJSONObject() {
        return jsonObject;
    }

    /**
     * Gets mail path.
     *
     * @return The mail path
     */
    public MailPath getMailPath() {
        return mailPath;
    }

    private boolean containsContent(final String baseContentType) throws OXException {
        try {
            final JSONArray bodyArr = getBodyArr();
            final int len = bodyArr.length();
            for (int i = 0; i < len; i++) {
                final String ct = bodyArr.getJSONObject(i).optString(MailJSONField.CONTENT_TYPE.getKey());
                if (null != ct && startsWith(ct, baseContentType)) {
                    return true;
                }
            }
            return false;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static String toLowerCase(final String s) {
        final int length = s.length();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(Character.toLowerCase(s.charAt(i)));
        }
        return sb.toString();
    }

    private static boolean startsWith(final String s, final String prefix) {
        return toLowerCase(s).startsWith(toLowerCase(prefix), 0);
    }

    private void asRawContent(final String id, final String baseContentType, final String content, final String filename) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject(8);
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), filename);
            getBodyArr().put(jsonObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void asAttachment(final String id, final String baseContentType, final long size, final String fileName, final String optContent) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject(8);
            /*
             * Sequence ID
             */
            jsonObject.put(MailListField.ID.getKey(), id);
            /*
             * Filename
             */
            if (fileName == null) {
                jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), JSONObject.NULL);
            } else {
                jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), fileName);
            }
            /*
             * Size
             */
            jsonObject.put(MailJSONField.SIZE.getKey(), size);
            /*
             * Disposition
             */
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
            /*
             * Content-Type
             */
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            /*
             * Content
             */
            if (null == optContent) {
                jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
            } else {
                jsonObject.put(MailJSONField.CONTENT.getKey(), optContent);
            }
            getAttachmentsArr().put(jsonObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void replaceEmptyContent(final String id, final String baseContentType, final String content) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);

            final JSONArray bodyArr = getBodyArr();
            final int len = bodyArr.length();
            int i = 0;
            while (i < len && !baseContentType.equals(bodyArr.getJSONObject(i).get(MailJSONField.CONTENT_TYPE.getKey()))) {
                i++;
            }

            bodyArr.put(i, jsonObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
