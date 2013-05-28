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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.html.HtmlService;
import com.openexchange.image.ImageLocation;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenConstants;
import com.openexchange.mail.attachment.AttachmentTokenRegistry;
import com.openexchange.mail.conversion.InlineImageDataSource;
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
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.text.Enriched2HtmlConverter;
import com.openexchange.mail.text.HtmlProcessing;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mail.uuencode.UUEncodedPart;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link JsonMessageHandler} - Generates a JSON message representation considering user-sensitive data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JsonMessageHandler implements MailMessageHandler {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(JsonMessageHandler.class));

    private static final String VIRTUAL = "___VIRTUAL___";

    private static final class PlainTextContent {

        final String id;

        final String contentType;

        final String content;

        public PlainTextContent(final String id, final String contentType, final String content) {
            super();
            this.id = id;
            this.contentType = contentType;
            this.content = content;
        }

    }

    private final Session session;

    private final Context ctx;

    private final LinkedList<String> multiparts;

    private TimeZone timeZone;

    private final UserSettingMail usm;

    private final DisplayMode displayMode;

    private final int accountId;

    private final MailPath mailPath;

    private final JSONObject jsonObject;

    // private Html2TextConverter converter;

    private JSONArray attachmentsArr;

    private JSONArray nestedMsgsArr;

    private boolean isAlternative;

    private String altId;

    private boolean textAppended;

    private boolean textWasEmpty;

    private final boolean[] modified;

    private PlainTextContent plainText;

    private String tokenFolder;

    private String tokenMailId;

    private final boolean token;

    private final int ttlMillis;

    private final boolean embedded;

    private boolean attachHTMLAlternativePart;

    private boolean includePlainText;

    /**
     * Initializes a new {@link JsonMessageHandler}
     *
     * @param accountId The account ID
     * @param mailPath The unique mail path
     * @param displayMode The display mode
     * @param session The session providing needed user data
     * @param usm The mail settings used for preparing message content if <code>displayVersion</code> is set to <code>true</code>; otherwise
     *            it is ignored.
     * @throws OXException If JSON message handler cannot be initialized
     */
    public JsonMessageHandler(final int accountId, final String mailPath, final DisplayMode displayMode, final boolean embedded, final Session session, final UserSettingMail usm, final boolean token, final int ttlMillis) throws OXException {
        super();
        multiparts = new LinkedList<String>();
        this.embedded = embedded;
        attachHTMLAlternativePart = !usm.isSuppressHTMLAlternativePart();
        this.accountId = accountId;
        modified = new boolean[1];
        this.session = session;
        ctx = getContext(session);
        this.usm = usm;
        this.displayMode = displayMode;
        this.mailPath = new MailPath(mailPath);
        jsonObject = new JSONObject();
        this.token = token;
        this.ttlMillis = ttlMillis;
    }

    /**
     * Initializes a new {@link JsonMessageHandler}
     *
     * @param accountId The account ID
     * @param mailPath The unique mail path
     * @param mail The mail message to add JSON fields not set by message parser traversal
     * @param displayMode The display mode
     * @param session The session providing needed user data
     * @param usm The mail settings used for preparing message content if <code>displayVersion</code> is set to <code>true</code>; otherwise
     *            it is ignored.
     * @param token <code>true</code> to add attachment tokens
     * @param ttlMillis The tokens' timeout
     * @throws OXException If JSON message handler cannot be initialized
     */
    public JsonMessageHandler(final int accountId, final MailPath mailPath, final MailMessage mail, final DisplayMode displayMode, final boolean embedded, final Session session, final UserSettingMail usm, final boolean token, final int ttlMillis) throws OXException {
        this(accountId, mailPath, mail, displayMode, embedded, session, usm, getContext(session), token, ttlMillis);
    }

    private static Context getContext(final Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return ContextStorage.getStorageContext(session.getContextId());
    }

    /**
     * Initializes a new {@link JsonMessageHandler} for internal usage
     */
    private JsonMessageHandler(final int accountId, final MailPath mailPath, final MailMessage mail, final DisplayMode displayMode, final boolean embedded, final Session session, final UserSettingMail usm, final Context ctx, final boolean token, final int ttlMillis) throws OXException {
        super();
        multiparts = new LinkedList<String>();
        this.embedded = embedded;
        attachHTMLAlternativePart = !usm.isSuppressHTMLAlternativePart();
        this.ttlMillis = ttlMillis;
        this.token = token;
        this.accountId = accountId;
        modified = new boolean[1];
        this.session = session;
        this.ctx = ctx;
        this.usm = usm;
        this.displayMode = displayMode;
        this.mailPath = mailPath;
        jsonObject = new JSONObject();
        try {
            if (DisplayMode.MODIFYABLE.equals(this.displayMode) && null != mailPath) {
                jsonObject.put(MailJSONField.MSGREF.getKey(), mailPath.toString());
            }
            if (null != mail) {
                /*
                 * Add missing fields
                 */
                final String mailId = mail.getMailId();
                if (mail.containsFolder() && mailId != null) {
                    tokenFolder = prepareFullname(accountId, mail.getFolder());
                    jsonObject.put(FolderChildFields.FOLDER_ID, tokenFolder);
                    tokenMailId = mailId;
                    jsonObject.put(DataFields.ID, mailId);
                }
                jsonObject.put(MailJSONField.UNREAD.getKey(), mail.getUnreadMessages());
                jsonObject.put(
                    MailJSONField.HAS_ATTACHMENTS.getKey(),
                    mail.containsHasAttachment() ? mail.hasAttachment() : mail.getContentType().isMimeType(MimeTypes.MIME_MULTIPART_MIXED));
                jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), mail.getContentType().getBaseType());
                jsonObject.put(MailJSONField.SIZE.getKey(), mail.getSize());
                // jsonObject.put(MailJSONField.THREAD_LEVEL.getKey(), mail.getThreadLevel());
                jsonObject.put(MailJSONField.ACCOUNT_NAME.getKey(), mail.getAccountName());
                jsonObject.put(MailJSONField.ACCOUNT_ID.getKey(), mail.getAccountId());
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sets whether the HTML part of a <i>multipart/alternative</i> content shall be attached.
     *
     * @param attachHTMLAlternativePart Whether the HTML part of a <i>multipart/alternative</i> content shall be attached
     * @return The {@link JsonMessageHandler} with new behavior applied
     */
    public JsonMessageHandler setAttachHTMLAlternativePart(final boolean attachHTMLAlternativePart) {
        this.attachHTMLAlternativePart = attachHTMLAlternativePart;
        return this;
    }

    /**
     * Sets whether to include raw plain-text in generated JSON object.
     *
     * @param includePlainText <code>true</code> to include raw plain-text; otherwise <code>false</code>
     * @return The {@link JsonMessageHandler} with new behavior applied
     */
    public JsonMessageHandler setIncludePlainText(final boolean includePlainText) {
        this.includePlainText = includePlainText;
        return this;
    }

    private JSONArray getAttachmentsArr() throws JSONException {
        if (attachmentsArr == null) {
            attachmentsArr = new JSONArray();
            jsonObject.put(MailJSONField.ATTACHMENTS.getKey(), attachmentsArr);
        }
        return attachmentsArr;
    }

    private JSONArray getNestedMsgsArr() throws JSONException {
        if (nestedMsgsArr == null) {
            nestedMsgsArr = new JSONArray();
            jsonObject.put(MailJSONField.NESTED_MESSAGES.getKey(), nestedMsgsArr);
        }
        return nestedMsgsArr;
    }

    private TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZoneUtils.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone());
        }
        return timeZone;
    }

    /**
     * Sets the time zone.
     *
     * @param timeZone The time zone
     * @return This handler with time zone applied
     */
    public JsonMessageHandler setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    private void addToken(final JSONObject jsonObject, final String attachmentId) {
        if (token && null != tokenFolder && null != tokenMailId) {
            /*
             * Token
             */
            try {
                final AttachmentToken token = new AttachmentToken(ttlMillis <= 0 ? AttachmentTokenConstants.DEFAULT_TIMEOUT : ttlMillis);
                token.setAccessInfo(accountId, session);
                token.setAttachmentInfo(tokenFolder, tokenMailId, attachmentId);
                AttachmentTokenRegistry.getInstance().putToken(token, session);
                final JSONObject attachmentObject = new JSONObject();
                attachmentObject.put("id", token.getId());
                attachmentObject.put("jsessionid", token.getJSessionId());
                jsonObject.put("token", attachmentObject);
            } catch (final Exception e) {
                LOG.warn("Adding attachment token failed.", e);
            }
        }
    }

    @Override
    public boolean handleAttachment(final MailPart part, final boolean isInline, final String baseContentType, final String fileName, final String id) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            /*
             * Sequence ID
             */
            final String attachmentId = part.containsSequenceId() ? part.getSequenceId() : id;
            jsonObject.put(MailListField.ID.getKey(), attachmentId);
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
            /*
             * Add token
             */
            addToken(jsonObject, attachmentId);
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
    private static final Set<HeaderName> COVERED_HEADER_NAMES = new HashSet<HeaderName>(Arrays.asList(new HeaderName[] {
        MessageHeaders.CONTENT_DISPOSITION, MessageHeaders.CONTENT_ID, MessageHeaders.CONTENT_TYPE, MessageHeaders.BCC, MessageHeaders.CC,
        MessageHeaders.DATE, MessageHeaders.DISP_NOT_TO, MessageHeaders.FROM, MessageHeaders.X_PRIORITY, MessageHeaders.SUBJECT,
        MessageHeaders.TO }));

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
        if (isInline && (DisplayMode.RAW.getMode() < displayMode.getMode())) {
            final String mpId = multiparts.peek();
            if (null != mpId && textAppended && id.startsWith(mpId)) {
                try {
                    final JSONArray attachments = getAttachmentsArr();
                    final int len = attachments.length();
                    final String keyContentType = MailJSONField.CONTENT_TYPE.getKey();
                    final String keyContent = MailJSONField.CONTENT.getKey();
                    final String keySize = MailJSONField.SIZE.getKey();
                    boolean b = true;
                    for (int i = len-1; b && i >= 0; i--) {
                        final JSONObject jObject = attachments.getJSONObject(i);
                        if (jObject.getString(keyContentType).startsWith("text/plain")) {
                            final String imageURL;
                            {
                                final InlineImageDataSource imgSource = InlineImageDataSource.getInstance();
                                final ImageLocation imageLocation = new ImageLocation.Builder(fileName).folder(prepareFullname(accountId, mailPath.getFolder())).id(mailPath.getMailID()).build();
                                imageURL = imgSource.generateUrl(imageLocation, session);
                            }
                            final String imgTag = "<img src=\"" + imageURL + "&scaleType=contain&width=800\" alt=\"\" style=\"display: block\" id=\"" + fileName + "\">";
                            final String content = jObject.getString(keyContent);
                            final String newContent = content + imgTag;
                            jObject.put(keyContent, newContent);
                            jObject.put(keySize, newContent.length());
                            b = false;
                        }
                    }
                    return handleAttachment(part, false, baseContentType, fileName, id);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        }
        return handleAttachment(part, isInline, baseContentType, fileName, id);
    }

    @Override
    public boolean handleInlineHtml(final String htmlContent, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        if (textAppended) {
            /*
             * A text part has already been detected as message's body
             */
            if (isAlternative) {
                if (DisplayMode.DISPLAY.equals(displayMode)) {
                    /*
                     * Add HTML alternative part as attachment
                     */
                    if (attachHTMLAlternativePart) {
                        try {
                            final JSONObject attachment = asAttachment(id, contentType.getBaseType(), htmlContent.length(), fileName, null);
                            attachment.put(VIRTUAL, true);
                        } catch (final JSONException e) {
                            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                        }
                    }
                } else if (DisplayMode.RAW.equals(displayMode)) {
                    /*
                     * Return HTML content as-is
                     */
                    asRawContent(id, contentType.getBaseType(), htmlContent);
                } else {
                    /*
                     * Discard
                     */
                    return true;
                }
            } else {
                /*
                 * Add HTML part as attachment
                 */
                asAttachment(id, contentType.getBaseType(), htmlContent.length(), fileName, null);
            }
        } else {
            /*
             * No text part was present before
             */
            if (DisplayMode.MODIFYABLE.getMode() <= displayMode.getMode()) {
                if (usm.isDisplayHtmlInlineContent()) {
                    final JSONObject jsonObject =
                        asDisplayHtml(id, contentType.getBaseType(), htmlContent, contentType.getCharsetParameter());
                    if (includePlainText) {
                        try {
                            /*
                             * Try to convert the given HTML to regular text
                             */
                            final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                            final String plainText = htmlService.html2text(htmlContent, true);
                            jsonObject.put("plain_text", plainText);
                        } catch (final JSONException e) {
                            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                        }
                    }
                } else {
                    asDisplayText(id, contentType.getBaseType(), htmlContent, fileName, DisplayMode.DISPLAY.equals(displayMode));
                }
            } else if (DisplayMode.RAW.equals(displayMode)) {
                /*
                 * Return HTML content as-is
                 */
                asRawContent(id, contentType.getBaseType(), htmlContent);
            } else {
                try {
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put(MailListField.ID.getKey(), id);
                    jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), contentType.getBaseType());
                    jsonObject.put(MailJSONField.SIZE.getKey(), htmlContent.length());
                    jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
                    jsonObject.put(MailJSONField.CONTENT.getKey(), htmlContent);
                    getAttachmentsArr().put(jsonObject);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
            textAppended = true;
        }
        return true;

    }

    private static final Enriched2HtmlConverter ENRCONV = new Enriched2HtmlConverter();

    // private static final RTF2HtmlConverter RTFCONV = new RTF2HtmlConverter();

    @Override
    public boolean handleInlinePlainText(final String plainTextContentArg, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        if (isAlternative && usm.isDisplayHtmlInlineContent() && (DisplayMode.RAW.getMode() < displayMode.getMode()) && contentType.startsWith(MimeTypes.MIME_TEXT_PLAIN)) {
            /*
             * User wants to see message's alternative content
             */
            if (null == plainText) {
                /*
                 * Remember plain-text content
                 */
                plainText =
                    new PlainTextContent(id, contentType.getBaseType(), HtmlProcessing.formatTextForDisplay(
                        plainTextContentArg,
                        usm,
                        displayMode));
            }
            /* textAppended = true; */
            return true;
        }
        try {
            if (contentType.startsWith(MimeTypes.MIME_TEXT_ENRICHED) || contentType.startsWith(MimeTypes.MIME_TEXT_RICHTEXT) || contentType.startsWith(MimeTypes.MIME_TEXT_RTF)) {
                if (textAppended) {
                    if (DisplayMode.DISPLAY.equals(displayMode)) {
                        /*
                         * Add alternative part as attachment
                         */
                        asAttachment(id, contentType.getBaseType(), plainTextContentArg.length(), fileName, null);
                        return true;
                    } else if (DisplayMode.RAW.equals(displayMode)) {
                        /*
                         * Return plain-text content as-is
                         */
                        asRawContent(id, contentType.getBaseType(), plainTextContentArg);
                    }
                    /*
                     * Discard
                     */
                    return true;
                }
                /*
                 * No text part was present before
                 */
                if (DisplayMode.MODIFYABLE.getMode() <= displayMode.getMode()) {
                    final JSONObject textObject;
                    if (usm.isDisplayHtmlInlineContent()) {
                        textObject =
                            asDisplayHtml(
                                id,
                                contentType.getBaseType(),
                                getHtmlDisplayVersion(contentType, plainTextContentArg),
                                contentType.getCharsetParameter());
                    } else {
                        textObject =
                            asDisplayText(
                                id,
                                contentType.getBaseType(),
                                getHtmlDisplayVersion(contentType, plainTextContentArg),
                                fileName,
                                DisplayMode.DISPLAY.equals(displayMode));
                    }
                    if (includePlainText && !textObject.has("plain_text")) {
                        textObject.put("plain_text", plainTextContentArg);
                    }
                } else if (DisplayMode.RAW.equals(displayMode)) {
                    /*
                     * Return plain-text content as-is
                     */
                    asRawContent(id, contentType.getBaseType(), plainTextContentArg);
                } else {
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put(MailListField.ID.getKey(), id);
                    jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
                    jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), contentType.getBaseType());
                    jsonObject.put(MailJSONField.SIZE.getKey(), plainTextContentArg.length());
                    jsonObject.put(MailJSONField.CONTENT.getKey(), plainTextContentArg);
                    if (includePlainText) {
                        jsonObject.put("plain_text", plainTextContentArg);
                    }
                    getAttachmentsArr().put(jsonObject);
                }
                textAppended = true;
                return true;
            }
            /*
             * Just common plain text
             */
            if (textAppended) {
                if (textWasEmpty) {
                    final String content = HtmlProcessing.formatTextForDisplay(plainTextContentArg, usm, displayMode);
                    final JSONObject textObject = asPlainText(id, contentType.getBaseType(), content);
                    if (includePlainText) {
                        textObject.put("plain_text", plainTextContentArg);
                    }
                    textWasEmpty = (null == content || 0 == content.length());
                } else {
                    /*
                     * A plain text message body has already been detected
                     */
                    final String content = HtmlProcessing.formatTextForDisplay(plainTextContentArg, usm, displayMode);
                    final String mpId = multiparts.peek();
                    if (null != mpId && (DisplayMode.RAW.getMode() < displayMode.getMode()) && id.startsWith(mpId)) {
                        final JSONArray attachments = getAttachmentsArr();
                        final int len = attachments.length();
                        final String keyContentType = MailJSONField.CONTENT_TYPE.getKey();
                        final String keyContent = MailJSONField.CONTENT.getKey();
                        final String keySize = MailJSONField.SIZE.getKey();
                        boolean b = true;
                        for (int i = len-1; b && i >= 0; i--) {
                            final JSONObject jObject = attachments.getJSONObject(i);
                            if (jObject.getString(keyContentType).startsWith("text/plain") && jObject.hasAndNotNull(keyContent)) {
                                final String newContent = jObject.getString(keyContent) + content;
                                jObject.put(keyContent, newContent);
                                jObject.put(keySize, newContent.length());
                                if (includePlainText && jObject.has("plain_text")) {
                                    jObject.put("plain_text", jObject.getString("plain_text") + plainTextContentArg);
                                }
                                b = false;
                            }
                        }
                    } else {
                        /*
                         * Append inline text as an attachment, too
                         */
                        final JSONObject textObject =
                            asAttachment(id, contentType.getBaseType(), plainTextContentArg.length(), fileName, content);
                        if (includePlainText) {
                            textObject.put("plain_text", plainTextContentArg);
                        }
                    }
                }
            } else {
                final String content = HtmlProcessing.formatTextForDisplay(plainTextContentArg, usm, displayMode);
                final JSONObject textObject = asPlainText(id, contentType.getBaseType(), content);
                if (includePlainText) {
                    textObject.put("plain_text", plainTextContentArg);
                }
                textAppended = true;
                textWasEmpty = (null == content || 0 == content.length());
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private String getHtmlDisplayVersion(final ContentType contentType, final String src) {
        final String baseType = contentType.getBaseType().toLowerCase(Locale.ENGLISH);
        if (baseType.startsWith(MimeTypes.MIME_TEXT_ENRICHED) || baseType.startsWith(MimeTypes.MIME_TEXT_RICHTEXT)) {
            return HtmlProcessing.formatHTMLForDisplay(
                ENRCONV.convert(src),
                contentType.getCharsetParameter(),
                session,
                mailPath,
                usm,
                modified,
                displayMode,
                embedded);
        }
        // Causes SWING library being loaded...
        // else if (baseType.startsWith(MIMETypes.MIME_TEXT_RTF)) {
        // return HTMLProcessing.formatHTMLForDisplay(
        // RTF2HTMLConverter.convertRTFToHTML(src),
        // contentType.getCharsetParameter(),
        // session,
        // mailPath,
        // usm,
        // modified,
        // displayMode);
        // }
        return HtmlProcessing.formatTextForDisplay(src, usm, displayMode);
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            String contentType = MimeTypes.MIME_APPL_OCTET;
            final String filename = part.getFileName();
            try {
                final Locale locale = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale();
                contentType = MimeType2ExtMap.getContentType(new File(filename.toLowerCase(locale)).getName()).toLowerCase(locale);
            } catch (final Exception e) {
                final Throwable t =
                    new Throwable(
                        new com.openexchange.java.StringAllocator("Unable to fetch content/type for '").append(filename).append("': ").append(e).toString());
                LOG.warn(t.getMessage(), t);
            }
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), contentType);
            jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), filename);
            jsonObject.put(MailJSONField.SIZE.getKey(), part.getFileSize());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
            /*
             * Content-type indicates mime type text/
             */
            if (contentType.startsWith("text/")) {
                /*
                 * Attach link-object with text content
                 */
                jsonObject.put(MailJSONField.CONTENT.getKey(), part.getPart().toString());
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
        if (!textAppended && plainText != null) {
            /*
             * No text present
             */
            asRawContent(plainText.id, plainText.contentType, plainText.content);
        }
        try {
            final String headersKey = MailJSONField.HEADERS.getKey();
            if (!jsonObject.hasAndNotNull(headersKey)) {
                jsonObject.put(headersKey, new JSONObject());
            }
            final String attachKey = MailJSONField.ATTACHMENTS.getKey();
            final String dispKey = MailJSONField.DISPOSITION.getKey();
            if (jsonObject.hasAndNotNull(attachKey)) {
                final JSONArray attachments = jsonObject.getJSONArray(attachKey);
                final int len = attachments.length();
                for (int i = 0; i < len; i++) {
                    final JSONObject attachment = attachments.getJSONObject(i);
                    if (attachment.hasAndNotNull(dispKey) && Part.ATTACHMENT.equalsIgnoreCase(attachment.getString(dispKey))) {
                        if (attachment.hasAndNotNull(VIRTUAL) && attachment.getBoolean(VIRTUAL)) {
                            attachment.remove(VIRTUAL);
                        } else {
                            jsonObject.put(MailJSONField.HAS_ATTACHMENTS.getKey(), true);
                        }
                        if (token && !attachment.hasAndNotNull("token")) {
                            try {
                                addToken(attachment, attachment.getString(MailListField.ID.getKey()));
                            } catch (final Exception e) {
                                // Missing field "id"
                            }
                        }
                    }
                }
            }
        } catch (final JSONException e) {
            LOG.error(e.getMessage(), e);
        }
        /*-
         *
        if (!textAppended) {
            try {
                // Ensure at least one text part is present
                final JSONObject jsonObject = new JSONObject();
                jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), MIMETypes.MIME_TEXT_PLAIN);
                jsonObject.put(MailJSONField.SIZE.getKey(), 0);
                jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
                jsonObject.put(MailJSONField.CONTENT.getKey(), "");
                getAttachmentsArr().put(jsonObject);
            } catch (final JSONException e) {
                throw new OXException(OXException.Code.JSON_ERROR, e, e.getMessage());
            }
        }
         */
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
        multiparts.push(id);
        return true;
    }

    @Override
    public boolean handleMultipartEnd(final MailPart mp, final String id) throws OXException {
        multiparts.pop();
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
                final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128);
                sb.append("Ignoring nested message.").append(
                    "Cannot handle part's content which should be a RFC822 message according to its content type: ");
                sb.append((null == content ? "null" : content.getClass().getSimpleName()));
                LOG.error(sb.toString());
                return true;
            }
            final JsonMessageHandler msgHandler =
                new JsonMessageHandler(accountId, null, null, displayMode, embedded, session, usm, ctx, token, ttlMillis).setTimeZone(timeZone);
            msgHandler.includePlainText = includePlainText;
            msgHandler.attachHTMLAlternativePart = attachHTMLAlternativePart;
            msgHandler.tokenFolder = tokenFolder;
            msgHandler.tokenMailId = tokenMailId;
            new MailMessageParser().parseMailMessage(nestedMail, msgHandler, id);
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
                receivedDate == null ? JSONObject.NULL : Long.valueOf(MessageWriter.addUserTimezone(receivedDate.getTime(), getTimeZone())));
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleSentDate(final Date sentDate) throws OXException {
        try {
            jsonObject.put(
                MailJSONField.SENT_DATE.getKey(),
                sentDate == null ? JSONObject.NULL : Long.valueOf(MessageWriter.addUserTimezone(sentDate.getTime(), getTimeZone())));
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
        final ContentType contentType = part.getContentType();
        if (isVCalendar(baseContentType) && !contentType.containsParameter("method")) {
            /*
             * Check ICal part for a valid METHOD and its presence in Content-Type header
             */
            final ICalParser iCalParser = ServerServiceRegistry.getInstance().getService(ICalParser.class);
            if (iCalParser != null) {
                try {
                    final String method = iCalParser.parseProperty("METHOD", part.getInputStream());
                    if (null != method) {
                        /*
                         * Assume an iTIP response or request
                         */
                        contentType.setParameter("method", method.toUpperCase(Locale.US));
                    }
                } catch (final RuntimeException e) {
                    LOG.warn("A runtime error occurred.", e);
                }
            }
        }
        /*
         * When creating a JSON message object from a message we do not distinguish special parts or image parts from "usual" attachments.
         * Therefore invoke the handleAttachment method. Maybe we need a separate handling in the future for vcards.
         */
        return handleAttachment(part, false, baseContentType, fileName, id);
    }

    private static boolean isVCalendar(final String baseContentType) {
        return "text/calendar".equalsIgnoreCase(baseContentType) || "text/x-vcalendar".equalsIgnoreCase(baseContentType);
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
        if (!jsonObject.has(MailJSONField.MODIFIED.getKey())) {
            try {
                jsonObject.put(MailJSONField.MODIFIED.getKey(), modified[0] ? 1 : 0);
            } catch (final JSONException e) {
                /*
                 * Cannot occur
                 */
                LOG.error(e.getMessage(), e);
            }
        }
        return jsonObject;
    }

    private JSONObject asAttachment(final String id, final String baseContentType, final int len, final String fileName, final String optContent) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), len);
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
            if (null == optContent) {
                jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
            } else {
                jsonObject.put(MailJSONField.CONTENT.getKey(), optContent);
            }
            if (fileName == null) {
                jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), JSONObject.NULL);
            } else {
                jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), MimeMessageUtility.decodeMultiEncodedHeader(fileName));
            }
            /*
             * Add token
             */
            addToken(jsonObject, id);
            getAttachmentsArr().put(jsonObject);
            return jsonObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void asRawContent(final String id, final String baseContentType, final String content) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            getAttachmentsArr().put(jsonObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject asDisplayHtml(final String id, final String baseContentType, final String htmlContent, final String charset) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            final String content = HtmlProcessing.formatHTMLForDisplay(htmlContent, charset, session, mailPath, usm, modified, displayMode, embedded);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            getAttachmentsArr().put(jsonObject);
            return jsonObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject asDisplayText(final String id, final String baseContentType, final String htmlContent, final String fileName, final boolean addAttachment) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), MimeTypes.MIME_TEXT_PLAIN);
            /*
             * Try to convert the given HTML to regular text
             */
            final String content;
            {
                final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                final String plainText = htmlService.html2text(htmlContent, true);
                if (includePlainText) {
                    jsonObject.put("plain_text", plainText);
                }
                content = HtmlProcessing.formatTextForDisplay(plainText, usm, displayMode);
            }
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            getAttachmentsArr().put(jsonObject);
            if (addAttachment) {
                /*
                 * Create attachment object for original HTML content
                 */
                final JSONObject originalVersion = new JSONObject();
                originalVersion.put(MailListField.ID.getKey(), id);
                originalVersion.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
                originalVersion.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
                originalVersion.put(MailJSONField.SIZE.getKey(), htmlContent.length());
                originalVersion.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
                if (fileName == null) {
                    originalVersion.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), JSONObject.NULL);
                } else {
                    originalVersion.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), MimeMessageUtility.decodeMultiEncodedHeader(fileName));
                }
                getAttachmentsArr().put(originalVersion);
            }
            return jsonObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject asPlainText(final String id, final String baseContentType, final String content) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            getAttachmentsArr().put(jsonObject);
            return jsonObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }
}
