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

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.decodeMultiEncodedHeader;
import static com.openexchange.mail.parser.MailMessageParser.generateFilename;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.html.HtmlSanitizeResult;
import com.openexchange.html.HtmlService;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenConstants;
import com.openexchange.mail.attachment.AttachmentTokenService;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.conversion.InlineImageDataSource;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.ContentProvider;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JsonMessageHandler.class);

    private static final String CONTENT = MailJSONField.CONTENT.getKey();
    private static final String DISPOSITION = MailJSONField.DISPOSITION.getKey();
    private static final String SIZE = MailJSONField.SIZE.getKey();
    private static final String CONTENT_TYPE = MailJSONField.CONTENT_TYPE.getKey();
    private static final String ID = MailListField.ID.getKey();
    private static final String PRIORITY = MailJSONField.PRIORITY.getKey();
    private static final String NESTED_MESSAGES = MailJSONField.NESTED_MESSAGES.getKey();
    private static final String ATTACHMENTS = MailJSONField.ATTACHMENTS.getKey();
    private static final String ACCOUNT_ID = MailJSONField.ACCOUNT_ID.getKey();
    private static final String ACCOUNT_NAME = MailJSONField.ACCOUNT_NAME.getKey();
    private static final String HAS_ATTACHMENTS = MailJSONField.HAS_ATTACHMENTS.getKey();
    private static final String HAS_REAL_ATTACHMENTS = "real_attachment";
    private static final String UNREAD = MailJSONField.UNREAD.getKey();
    private static final String ATTACHMENT_FILE_NAME = MailJSONField.ATTACHMENT_FILE_NAME.getKey();
    private static final String FROM = MailJSONField.FROM.getKey();
    private static final String CID = MailJSONField.CID.getKey();
    private static final String COLOR_LABEL = MailJSONField.COLOR_LABEL.getKey();
    private static final String RECIPIENT_CC = MailJSONField.RECIPIENT_CC.getKey();
    private static final String RECIPIENT_BCC = MailJSONField.RECIPIENT_BCC.getKey();
    private static final String HEADERS = MailJSONField.HEADERS.getKey();
    private static final String ORIGINAL_ID = MailJSONField.ORIGINAL_ID.getKey();
    private static final String ORIGINAL_FOLDER_ID = MailJSONField.ORIGINAL_FOLDER_ID.getKey();

    private static final String TRUNCATED = MailJSONField.TRUNCATED.getKey();

    private static final String VIRTUAL = "___VIRTUAL___";
    private static final String MULTIPART_ID = "___MP-ID___";

//    private static final int DEFAULT_MAX_NESTED_MESSAGES_LEVELS = 10;

    private static final class PlainTextContent {

        final String id;
        final String contentType;
        final String content;

        PlainTextContent(final String id, final String contentType, final String content) {
            super();
            this.id = id;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder(256);
            builder.append("PlainTextContent [");
            if (id != null) {
                builder.append("id=").append(id).append(", ");
            }
            if (contentType != null) {
                builder.append("contentType=").append(contentType).append(", ");
            }
            if (content != null) {
                builder.append("content=").append(content);
            }
            builder.append("]");
            return builder.toString();
        }
    } // End of class PlainTextContent

    private static final class MultipartInfo {

        final String mpId;
        final ContentType contentType;

        MultipartInfo(final String mpId, final ContentType contentType) {
            super();
            this.mpId = mpId;
            this.contentType = contentType;
        }

        boolean isSubType(final String subtype) {
            return null != contentType && contentType.startsWith("multipart/" + subtype);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder(256);
            builder.append("MultipartInfo [");
            if (mpId != null) {
                builder.append("mpId=").append(mpId).append(", ");
            }
            if (contentType != null) {
                builder.append("contentType=").append(contentType);
            }
            builder.append("]");
            return builder.toString();
        }

    } // End of class MultipartInfo

    private final Session session;
    private final Context ctx;
    private final LinkedList<MultipartInfo> multiparts;
    private TimeZone timeZone;
    private final UserSettingMail usm;
    private final DisplayMode displayMode;
    private final int accountId;
    private final MailPath mailPath;
    private final MailPath originalMailPath;
    private final JSONObject jsonObject;
    private AttachmentListing attachmentListing;
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
    private boolean exactLength;
    private final int maxContentSize;
    private int currentNestingLevel = 0;
    private final int maxNestedMessageLevels;
    private String initialiserSequenceId;

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
        this(accountId, new MailPath(mailPath), null, displayMode, embedded, session, usm, getContext(session), token, ttlMillis, -1, -1);
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
        this(accountId, mailPath, mail, displayMode, embedded, session, usm, getContext(session), token, ttlMillis, -1, -1);
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
     * @param maxContentSize maximum number of bytes that is will be returned for content. '<=0' means unlimited.
     * @param maxNestedMessageLevels The number of levels in which deep-parsing of nested messages takes place; otherwise only ID information is set; '<=0' falls back to default value (10)
     * @throws OXException If JSON message handler cannot be initialized
     */
    public JsonMessageHandler(int accountId, MailPath mailPath, MailMessage mail, DisplayMode displayMode, boolean embedded, Session session, UserSettingMail usm, boolean token, int ttlMillis, int maxContentSize, int maxNestedMessageLevels) throws OXException {
        this(accountId, mailPath, mail, displayMode, embedded, session, usm, getContext(session), token, ttlMillis, maxContentSize, maxNestedMessageLevels);
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
    private JsonMessageHandler(int accountId, MailPath mailPath, MailMessage mail, DisplayMode displayMode, boolean embedded, Session session, UserSettingMail usm, Context ctx, boolean token, int ttlMillis, int maxContentSize, int maxNestedMessageLevels) throws OXException {
        super();
        this.multiparts = new LinkedList<MultipartInfo>();
        this.embedded = DisplayMode.DOCUMENT.equals(displayMode) ? false : embedded;
        this.attachHTMLAlternativePart = !usm.isSuppressHTMLAlternativePart();
        this.ttlMillis = ttlMillis;
        this.token = token;
        this.accountId = accountId;
        this.modified = new boolean[1];
        this.session = session;
        this.ctx = ctx;
        this.usm = usm;
        this.displayMode = displayMode;
        this.mailPath = mailPath;
        this.maxContentSize = maxContentSize;
        this.jsonObject = new JSONObject(32);
        this.maxNestedMessageLevels = 1; //maxNestedMessageLevels <= 0 ? DEFAULT_MAX_NESTED_MESSAGES_LEVELS : maxNestedMessageLevels;
        try {
            if (DisplayMode.MODIFYABLE.equals(this.displayMode) && null != mailPath) {
                jsonObject.put(MailJSONField.MSGREF.getKey(), mailPath.toString());
            }
            MailPath originalMailPath = null;
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
                final int unreadMessages = mail.getUnreadMessages();
                if (unreadMessages >= 0) {
                    jsonObject.put(UNREAD, unreadMessages);
                }
                if (mail.containsHasAttachment()) {
                    // jsonObject.put(HAS_ATTACHMENTS, mail.containsHasAttachment() ? mail.hasAttachment() : mail.getContentType().isMimeType(MimeTypes.MIME_MULTIPART_MIXED));
                    // See bug 42695 & 42862
                    jsonObject.put(HAS_ATTACHMENTS, mail.hasAttachment());
                }
                jsonObject.put(CONTENT_TYPE, mail.getContentType().getBaseType());
                jsonObject.put(SIZE, mail.getSize());
                jsonObject.put(ACCOUNT_NAME, mail.getAccountName());
                jsonObject.put(ACCOUNT_ID, mail.getAccountId());
                this.initialiserSequenceId = mail.getSequenceId();


                String originalId = null;
                if (mail.containsOriginalId()) {
                    originalId = mail.getOriginalId();
                    if (null != originalId) {
                        jsonObject.put(ORIGINAL_ID, originalId);
                    }
                }
                String originalFolder = null;
                if (mail.containsOriginalFolder()) {
                    originalFolder = mail.getOriginalFolder();
                    if (null != originalFolder) {
                        jsonObject.put(ORIGINAL_FOLDER_ID, prepareFullname(accountId, originalFolder));
                    }
                }
                if (null != originalId && null != originalFolder) {
                    originalMailPath = new MailPath(null == mailPath ? mail.getAccountId() : mailPath.getAccountId(), originalFolder, originalId);
                }
            }
            this.originalMailPath = originalMailPath;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sets whether to set the exact length of mail parts.
     *
     * @param exactLength <code>true</code> to set the exact length of mail parts; otherwise use mail system's size estimation
     * @return This {@link JsonMessageHandler} with new behavior applied
     */
    public JsonMessageHandler setExactLength(final boolean exactLength) {
        this.exactLength = exactLength;
        return this;
    }

    /**
     * Sets whether the HTML part of a <i>multipart/alternative</i> content shall be attached.
     *
     * @param attachHTMLAlternativePart Whether the HTML part of a <i>multipart/alternative</i> content shall be attached
     * @return This {@link JsonMessageHandler} with new behavior applied
     */
    public JsonMessageHandler setAttachHTMLAlternativePart(final boolean attachHTMLAlternativePart) {
        this.attachHTMLAlternativePart = attachHTMLAlternativePart;
        return this;
    }

    /**
     * Sets whether to include raw plain-text in generated JSON object.
     *
     * @param includePlainText <code>true</code> to include raw plain-text; otherwise <code>false</code>
     * @return This {@link JsonMessageHandler} with new behavior applied
     */
    public JsonMessageHandler setIncludePlainText(final boolean includePlainText) {
        this.includePlainText = includePlainText;
        return this;
    }

    public String getInitialiserSequenceId() {
        return initialiserSequenceId;
    }

    public void setInitialiserSequenceId(String initialiserSequenceId) {
        this.initialiserSequenceId = initialiserSequenceId;
    }

    private AttachmentListing getAttachmentListing() {
        if (attachmentListing == null) {
            attachmentListing = new AttachmentListing();
        }
        return attachmentListing;
    }

    private JSONArray getNestedMsgsArr() throws JSONException {
        if (nestedMsgsArr == null) {
            nestedMsgsArr = new JSONArray();
            jsonObject.put(NESTED_MESSAGES, nestedMsgsArr);
        }
        return nestedMsgsArr;
    }

    private TimeZone getTimeZone() throws OXException {
        if (timeZone == null) {
            if (session instanceof ServerSession) {
                timeZone = TimeZoneUtils.getTimeZone(((ServerSession) session).getUser().getTimeZone());
            } else {
                timeZone = TimeZoneUtils.getTimeZone(UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone());
            }
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
                AttachmentTokenService service = ServerServiceRegistry.getInstance().getService(AttachmentTokenService.class, true);
                service.putToken(token, session);
                final JSONObject attachmentObject = new JSONObject(2);
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
        if (isInline && isAlternative && null != altId && id.startsWith(altId) && baseContentType.startsWith("text/xml")) {
            // Ignore
            return true;
        }

        // Handle attachment
        return handleAttachment0(part, isInline, null, baseContentType, fileName, id, false);
    }

    private boolean handleAttachment0(MailPart part, boolean isInline, String disposition, String baseContentType, String fileName, String id, boolean handleAsInlineImage) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject(8);
            /*
             * Sequence ID
             */
            final String attachmentId = part.containsSequenceId() ? part.getSequenceId() : id;
            jsonObject.put(ID, attachmentId);
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
                jsonObject.put(ATTACHMENT_FILE_NAME, val);
            } else {
                jsonObject.put(ATTACHMENT_FILE_NAME, fileName);
            }
            /*
             * Size
             */
            boolean checkSize = true;
            if (exactLength) {
                try {
                    jsonObject.put(SIZE, Streams.countInputStream(part.getInputStream()));
                    checkSize = false;
                } catch (final Exception e) {
                    // Failed counting part's content
                }
            }
            if (checkSize && part.containsSize()) {
                jsonObject.put(SIZE, part.getSize());
            }
            /*
             * Disposition
             */
            jsonObject.put(DISPOSITION, null == disposition ? Part.ATTACHMENT : disposition);
            /*
             * Content-ID
             */
            if (part.containsContentId()) {
                final String contentId = part.getContentId();
                if (contentId != null) {
                    jsonObject.put(CID, contentId);
                }
            }
            /*
             * Content-Type
             */
            {
                ContentType clone = new ContentType();
                clone.setContentType(part.getContentType());
                clone.removeNameParameter();
                jsonObject.put(CONTENT_TYPE, clone.toString());
            }
            /*
             * Content
             */
            jsonObject.put(CONTENT, JSONObject.NULL);
            /*
             * Add token
             */
            addToken(jsonObject, attachmentId);
            /*
             * Add attachment
             */
            if (handleAsInlineImage) {
                getAttachmentListing().addRemainder(jsonObject);
            } else {
                getAttachmentListing().add(jsonObject);
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleBccRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        try {
            jsonObject.put(RECIPIENT_BCC, MessageWriter.getAddressesAsArray(recipientAddrs));
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleCcRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        try {
            jsonObject.put(RECIPIENT_CC, MessageWriter.getAddressesAsArray(recipientAddrs));
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleColorLabel(final int colorLabel) throws OXException {
        try {
            jsonObject.put(COLOR_LABEL, colorLabel);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleContentId(final String contentId) throws OXException {
        try {
            jsonObject.put(CID, contentId);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
        try {
            jsonObject.put(FROM, MessageWriter.getAddressesAsArray(fromAddrs));
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    /**
     * These headers are covered by fields of {@link MailMessage}
     */
    private static final Set<HeaderName> COVERED_HEADER_NAMES = ImmutableSet.of(
        MessageHeaders.CONTENT_DISPOSITION, MessageHeaders.CONTENT_ID, MessageHeaders.CONTENT_TYPE, MessageHeaders.BCC, MessageHeaders.CC,
        MessageHeaders.DATE, MessageHeaders.DISP_NOT_TO, MessageHeaders.FROM, MessageHeaders.X_PRIORITY, MessageHeaders.SUBJECT,
        MessageHeaders.TO);

    @Override
    public boolean handleHeaders(final int size, final Iterator<Entry<String, String>> iter) throws OXException {
        if (size == 0) {
            return true;
        }
        try {
            final JSONObject hdrObject = new JSONObject(size);
            for (int i = size; i-- > 0;) {
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
                        jsonObject.put(PRIORITY, priority);
                    }
                } else if (MessageHeaders.HDR_X_PRIORITY.equalsIgnoreCase(headerName)) {
                    if (!jsonObject.has(PRIORITY)) {
                        /*
                         * Priority
                         */
                        int priority = MailMessage.PRIORITY_NORMAL;
                        if (null != entry.getValue()) {
                            priority = MimeMessageConverter.parsePriority(entry.getValue());
                        }
                        jsonObject.put(PRIORITY, priority);
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
            jsonObject.put(HEADERS, hdrObject);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleImagePart(final MailPart part, final String imageCID, final String baseContentType, final boolean isInline, final String fileName, final String id) throws OXException {
        // Check for inline image
        boolean considerAsInline = isInline || (!part.getContentDisposition().isAttachment() && part.containsHeader("Content-Id"));

        // Handle it...
        if (considerAsInline && (DisplayMode.MODIFYABLE.getMode() < displayMode.getMode())) {
            final MultipartInfo mpInfo = multiparts.peek();
            if (null != mpInfo && textAppended && id.startsWith(mpInfo.mpId) && mpInfo.isSubType("mixed")) {
                try {
                    List<JSONObject> attachments = getAttachmentListing().getAttachments();
                    int len = attachments.size();
                    String keyContentType = CONTENT_TYPE;
                    String keyContent = CONTENT;
                    String keySize = SIZE;
                    MailPath mailPath = this.mailPath;

                    boolean b = true;
                    for (int i = len; b && i-- > 0;) {
                        JSONObject jAttachment = attachments.get(i);
                        if (jAttachment.getString(keyContentType).startsWith("text/plain") && null != mailPath) {
                            try {
                                final String imageURL;
                                {
                                    final InlineImageDataSource imgSource = InlineImageDataSource.getInstance();
                                    final ImageLocation imageLocation = new ImageLocation.Builder(fileName).folder(prepareFullname(accountId, mailPath.getFolder())).id(mailPath.getMailID()).build();
                                    imageURL = imgSource.generateUrl(imageLocation, session);
                                }
                                final String imgTag = "<img src=\"" + imageURL + "&scaleType=contain&width=800\" alt=\"\" style=\"display: block\" id=\"" + fileName + "\">";
                                final String content = jAttachment.getString(keyContent);
                                final String newContent = content + imgTag;
                                jAttachment.put(keyContent, newContent);
                                jAttachment.put(keySize, newContent.length());
                                b = false;
                            } catch (final Exception e) {
                                LOG.error("Error while inlining image part.", e);
                            }
                        }
                    }

                    if (b) { // No suitable text/plain
                        try {
                            for (int i = len; b && i-- > 0;) {
                                JSONObject jAttachment = attachments.get(i);
                                // Is HTML and in same multipart
                                if (jAttachment.optString(CONTENT_TYPE, "").startsWith("text/htm") && mpInfo.mpId.equals(jAttachment.optString(MULTIPART_ID, null))) {
                                    String content = jAttachment.optString(CONTENT, "null");
                                    if (!"null".equals(content) && null != mailPath) {
                                        try {
                                            // Append to first one
                                            final String imageURL;
                                            {
                                                final InlineImageDataSource imgSource = InlineImageDataSource.getInstance();
                                                final ImageLocation imageLocation = new ImageLocation.Builder(fileName).folder(prepareFullname(accountId, mailPath.getFolder())).id(mailPath.getMailID()).build();
                                                imageURL = imgSource.generateUrl(imageLocation, session);
                                            }
                                            final String imgTag = "<img src=\"" + imageURL + "&scaleType=contain&width=800\" alt=\"\" style=\"display: block\" id=\"" + fileName + "\">";
                                            content = new StringBuilder(content).append(imgTag).toString();
                                            jAttachment.put(CONTENT, content);
                                            b = false;
                                        } catch (final Exception e) {
                                            LOG.error("Error while inlining image part.", e);
                                        }
                                    }
                                }
                            }
                        } catch (final RuntimeException e) {
                            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                        }
                    }

                    return handleAttachment0(part, considerAsInline, considerAsInline ? Part.INLINE : Part.ATTACHMENT, baseContentType, fileName, id, considerAsInline);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        }
        return handleAttachment0(part, considerAsInline, considerAsInline ? Part.INLINE : Part.ATTACHMENT, baseContentType, fileName, id, considerAsInline);
    }

    @Override
    public boolean handleInlineHtml(final ContentProvider contentProvider, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        String htmlContent = contentProvider.getContent();
        if (textAppended) {
            /*
             * A text part has already been detected as message's body
             */
            MailPath mailPath = this.mailPath;
            if (isAlternative) {
                if (DisplayMode.DISPLAY.isIncluded(displayMode)) {
                    /*
                     * Check if previously appended text part was empty
                     */
                    if (textWasEmpty) {
                        if (usm.isDisplayHtmlInlineContent()) {
                            JSONObject jsonObject = asDisplayHtml(id, contentType.getBaseType(), htmlContent, contentType.getCharsetParameter());
                            if (includePlainText) {
                                try {
                                    String plainText = html2text(htmlContent);
                                    jsonObject.put("plain_text", plainText);
                                } catch (JSONException e) {
                                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                                }
                            }
                        } else {
                            try {
                                asDisplayText(id, contentType.getBaseType(), htmlContent, fileName, false);
                                getAttachmentListing().removeFirst();
                            } catch (RuntimeException e) {
                                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                            }
                        }
                    }
                    /*
                     * Check if nested in same multipart
                     */
                    try {
                        MultipartInfo mpInfo = multiparts.peek();
                        List<JSONObject> attachments = getAttachmentListing().getAttachments();
                        int length = attachments.size();
                        for (int i = length; i-- > 0;) {
                            JSONObject jAttachment = attachments.get(i);
                            // Is HTML and in same multipart
                            if (jAttachment.optString(CONTENT_TYPE, "").startsWith("text/htm") && null != mpInfo && mpInfo.mpId.equals(jAttachment.optString(MULTIPART_ID, null)) && mpInfo.isSubType("mixed")) {
                                String content = jAttachment.optString(CONTENT, "null");
                                if (!"null".equals(content) && null != mailPath) {
                                    // Append to first one
                                    HtmlSanitizeResult sanitizeResult = HtmlProcessing.formatHTMLForDisplay(htmlContent, contentType.getCharsetParameter(), session, mailPath, originalMailPath, usm, modified, displayMode, embedded, maxContentSize);
                                    content = new StringBuilder(content).append(sanitizeResult.getContent()).toString();
                                    jAttachment.put(CONTENT, content);
                                    return true;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                    }
                    /*
                     * Add HTML alternative part as attachment
                     */
                    if (attachHTMLAlternativePart) {
                        try {
                            JSONObject attachment = asAttachment(id, contentType.getBaseType(), htmlContent.length(), fileName, null);
                            attachment.put(VIRTUAL, true);
                        } catch (final JSONException e) {
                            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                        }
                    }
                } else if (DisplayMode.RAW.equals(displayMode)) {
                    /*
                     * Return HTML content as-is
                     */
                    asRawContent(id, contentType.getBaseType(), new HtmlSanitizeResult(htmlContent));
                } else {
                    /*
                     * Discard
                     */
                    return true;
                }
            } else {
                try {
                    MultipartInfo mpInfo = multiparts.peek();
                    List<JSONObject> attachments = getAttachmentListing().getAttachments();
                    int length = attachments.size();
                    for (int i = length; i-- > 0;) {
                        JSONObject jAttachment = attachments.get(i);
                        // Is HTML and in same multipart
                        if (jAttachment.optString(CONTENT_TYPE, "").startsWith("text/htm") && null != mpInfo && mpInfo.mpId.equals(jAttachment.optString(MULTIPART_ID, null)) && mpInfo.isSubType("mixed")) {
                            String content = jAttachment.optString(CONTENT, "null");
                            if (!"null".equals(content) && null != mailPath) {
                                // Append to first one
                                HtmlSanitizeResult sanitizeResult = HtmlProcessing.formatHTMLForDisplay(htmlContent, contentType.getCharsetParameter(), session, mailPath, originalMailPath, usm, modified, displayMode, embedded, maxContentSize);
                                content = new StringBuilder(content).append(sanitizeResult.getContent()).toString();
                                jAttachment.put(CONTENT, content);
                                return true;
                            }
                        }
                    }
                } catch (JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
                /*
                 * Add HTML part as attachment
                 */
                try {
                    JSONObject attachment = asAttachment(id, contentType.getBaseType(), htmlContent.length(), fileName, null);
                    attachment.put(VIRTUAL, true);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        } else {
            /*
             * No text part was present before
             */
            if (DisplayMode.MODIFYABLE.getMode() <= displayMode.getMode()) {
                if (usm.isDisplayHtmlInlineContent()) {
                    /*
                     * Check if HTML is empty or has an empty body section
                     */
                    if ((com.openexchange.java.Strings.isEmpty(htmlContent) || (htmlContent.length() < 1024 && hasNoImage(htmlContent) && isEmpty(html2text(htmlContent)))) && plainText != null) {
                        /*
                         * No text present
                         */
                        asRawContent(plainText.id, plainText.contentType, new HtmlSanitizeResult(plainText.content));
                    } else {
                        JSONObject jsonObject = asDisplayHtml(id, contentType.getBaseType(), htmlContent, contentType.getCharsetParameter());
                        if (includePlainText) {
                            try {
                                /*
                                 * Try to convert the given HTML to regular text
                                 */
                                String plainText = html2text(htmlContent);
                                jsonObject.put("plain_text", plainText);
                            } catch (JSONException e) {
                                throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                            }
                        }
                    }
                } else {
                    asDisplayText(id, contentType.getBaseType(), htmlContent, fileName, DisplayMode.DISPLAY.isIncluded(displayMode));
                }
            } else if (DisplayMode.RAW.equals(displayMode)) {
                /*
                 * Return HTML content as-is
                 */
                asRawContent(id, contentType.getBaseType(), new HtmlSanitizeResult(htmlContent));
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(6);
                    jsonObject.put(ID, id);
                    jsonObject.put(CONTENT_TYPE, contentType.getBaseType());
                    jsonObject.put(SIZE, htmlContent.length());
                    jsonObject.put(DISPOSITION, Part.INLINE);
                    jsonObject.put(CONTENT, htmlContent);
                    getAttachmentListing().add(jsonObject);
                } catch (JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
            textAppended = true;
        }
        return true;
    }

    private static final Enriched2HtmlConverter ENRCONV = new Enriched2HtmlConverter();

    @Override
    public boolean handleInlinePlainText(final String plainTextContentArg, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        String identifier = id;
        if (isAlternative && usm.isDisplayHtmlInlineContent() && (DisplayMode.RAW.getMode() < displayMode.getMode()) && contentType.startsWith(MimeTypes.MIME_TEXT_PLAIN)) {
            /*
             * User wants to see message's alternative content
             */
            if (null == plainText) {
                /*
                 * Remember plain-text content
                 */
                HtmlSanitizeResult sanitizeResult = HtmlProcessing.formatTextForDisplay(plainTextContentArg, usm, displayMode, maxContentSize);
                plainText = new PlainTextContent(identifier, contentType.getBaseType(), sanitizeResult.getContent());
            }
            return true;
        }
        try {
            /*
             * Adjust DI if virtually inserted; e.g. MimeForward
             */
            if (isVirtual(contentType)) {
                identifier = "0";
            }
            if (contentType.startsWith(MimeTypes.MIME_TEXT_ENRICHED) || contentType.startsWith(MimeTypes.MIME_TEXT_RICHTEXT) || contentType.startsWith(MimeTypes.MIME_TEXT_RTF)) {
                if (textAppended) {
                    if (DisplayMode.DISPLAY.isIncluded(displayMode)) {
                        /*
                         * Add alternative part as attachment
                         */
                        try {
                            JSONObject attachment = asAttachment(identifier, contentType.getBaseType(), plainTextContentArg.length(), fileName, null);
                            attachment.put(VIRTUAL, true);
                        } catch (final JSONException e) {
                            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                        }
                        return true;
                    } else if (DisplayMode.RAW.equals(displayMode)) {
                        /*
                         * Return plain-text content as-is
                         */
                        asRawContent(identifier, contentType.getBaseType(), new HtmlSanitizeResult(plainTextContentArg));
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
                        textObject = asDisplayHtml(identifier, contentType.getBaseType(), getHtmlDisplayVersion(contentType, plainTextContentArg), contentType.getCharsetParameter());
                    } else {
                        textObject = asDisplayText(identifier, contentType.getBaseType(), getHtmlDisplayVersion(contentType, plainTextContentArg), fileName, DisplayMode.DISPLAY.isIncluded(displayMode));
                    }
                    if (includePlainText && !textObject.has("plain_text")) {
                        textObject.put("plain_text", plainTextContentArg);
                    }
                } else if (DisplayMode.RAW.equals(displayMode)) {
                    /*
                     * Return plain-text content as-is
                     */
                    asRawContent(identifier, contentType.getBaseType(), new HtmlSanitizeResult(plainTextContentArg));
                } else {
                    final JSONObject jsonObject = new JSONObject(6);
                    jsonObject.put(ID, identifier);
                    jsonObject.put(DISPOSITION, Part.INLINE);
                    jsonObject.put(CONTENT_TYPE, contentType.getBaseType());
                    jsonObject.put(SIZE, plainTextContentArg.length());
                    jsonObject.put(CONTENT, plainTextContentArg);
                    if (includePlainText) {
                        jsonObject.put("plain_text", plainTextContentArg);
                    }
                    getAttachmentListing().add(jsonObject);
                }
                textAppended = true;
                return true;
            }
            /*
             * Just common plain text
             */
            if (textAppended) {
                if (textWasEmpty) {
                    final HtmlSanitizeResult content = HtmlProcessing.formatTextForDisplay(plainTextContentArg, usm, displayMode, maxContentSize);
                    final JSONObject textObject = asPlainText(identifier, contentType.getBaseType(), content);
                    if (includePlainText) {
                        textObject.put("plain_text", plainTextContentArg);
                    }
                    textWasEmpty = (null == content.getContent() || 0 == content.getContent().length());
                } else {
                    if (usm.isDisplayHtmlInlineContent()) {
                        // Assume HTML content has been appended before
                        if (DisplayMode.DISPLAY.isIncluded(displayMode)) {
                            /*
                             * Add alternative part as attachment
                             */
                            if (null != contentType.getParameter("realfilename") && plainTextContentArg.length() > 0) {
                                try {
                                    JSONObject attachment = asAttachment(identifier, contentType.getBaseType(), plainTextContentArg.length(), fileName, null);
                                    attachment.put(VIRTUAL, true);
                                } catch (final JSONException e) {
                                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                                }
                                return true;
                            }
                        } else if (DisplayMode.RAW.equals(displayMode)) {
                            /*
                             * Return plain-text content as-is
                             */
                            asRawContent(identifier, contentType.getBaseType(), new HtmlSanitizeResult(plainTextContentArg));
                            return true;
                        }
                    }

                    // A plain text message body has already been detected
                    final HtmlSanitizeResult sanitizeResult = HtmlProcessing.formatTextForDisplay(plainTextContentArg, usm, displayMode, maxContentSize);
                    final MultipartInfo mpInfo = multiparts.peek();
                    if (null != mpInfo && (DisplayMode.RAW.getMode() < displayMode.getMode()) && identifier.startsWith(mpInfo.mpId) && mpInfo.isSubType("mixed")) {
                        List<JSONObject> attachments = getAttachmentListing().getAttachments();
                        int len = attachments.size();
                        String keyContentType = CONTENT_TYPE;
                        String keyContent = CONTENT;
                        String keySize = SIZE;
                        boolean b = true;
                        for (int i = len - 1; b && i >= 0; i--) {
                            JSONObject jObject = attachments.get(i);
                            if (jObject.getString(keyContentType).startsWith("text/plain") && jObject.hasAndNotNull(keyContent)) {
                                String newContent = jObject.getString(keyContent) + sanitizeResult.getContent();
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
                        JSONObject textObject = asAttachment(identifier, contentType.getBaseType(), plainTextContentArg.length(), fileName, new HtmlSanitizeResult(sanitizeResult.getContent()));
                        textObject.put(VIRTUAL, true);
                        if (includePlainText) {
                            textObject.put("plain_text", plainTextContentArg);
                        }
                    }
                }
            } else {
                HtmlSanitizeResult sanitizeResult = HtmlProcessing.formatTextForDisplay(plainTextContentArg, usm, displayMode, maxContentSize);
                final JSONObject textObject = asPlainText(identifier, contentType.getBaseType(), sanitizeResult);
                if (includePlainText) {
                    textObject.put("plain_text", plainTextContentArg);
                }
                textAppended = true;
                textWasEmpty = (null == sanitizeResult.getContent() || 0 == sanitizeResult.getContent().length());
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
                originalMailPath,
                usm,
                modified,
                displayMode,
                embedded);
        }
        return HtmlProcessing.formatTextForDisplay(src, usm, displayMode);
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject(8);
            jsonObject.put(ID, id);
            String contentType = MimeTypes.MIME_APPL_OCTET;
            final String filename = part.getFileName();
            try {
                final Locale locale = UserStorage.getInstance().getUser(session.getUserId(), ctx).getLocale();
                contentType = MimeType2ExtMap.getContentType(new File(filename.toLowerCase(locale)).getName()).toLowerCase(locale);
            } catch (final Exception e) {
                final Throwable t =
                    new Throwable(
                        new StringBuilder("Unable to fetch content/type for '").append(filename).append("': ").append(e).toString());
                LOG.warn("", t);
            }
            jsonObject.put(CONTENT_TYPE, contentType);
            jsonObject.put(ATTACHMENT_FILE_NAME, filename);
            jsonObject.put(SIZE, part.getFileSize());
            jsonObject.put(DISPOSITION, Part.ATTACHMENT);
            /*
             * Content-type indicates mime type text/
             */
            if (contentType.startsWith("text/")) {
                /*
                 * Attach link-object with text content
                 */
                jsonObject.put(CONTENT, part.getPart().toString());
            } else {
                /*
                 * Attach link-object.
                 */
                jsonObject.put(CONTENT, JSONObject.NULL);
            }
            getAttachmentListing().add(jsonObject);
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
        // Since we obviously touched message's content, mark its corresponding message object as seen
        mail.setFlags(mail.getFlags() | MailMessage.FLAG_SEEN);

        // Check if we did not append any text so far
        if (!textAppended && plainText != null) {
            /*
             * Append the plain text...
             */
            asRawContent(plainText.id, plainText.contentType, new HtmlSanitizeResult(plainText.content), true);

        }

        // Attachments
        {
            AttachmentListing attachmentListing = this.attachmentListing;
            if (null != attachmentListing) {
                try {
                    jsonObject.put(ATTACHMENTS, attachmentListing.toJsonArray());
                } catch (JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        }

        try {
            String headersKey = HEADERS;
            if (!jsonObject.hasAndNotNull(headersKey)) {
                jsonObject.put(headersKey, new JSONObject(1));
            }

            String attachKey = ATTACHMENTS;
            String dispKey = DISPOSITION;
            if (jsonObject.hasAndNotNull(attachKey)) {
                JSONArray jAttachments = jsonObject.getJSONArray(attachKey);
                int len = jAttachments.length();
                for (int i = 0; i < len; i++) {
                    JSONObject jAttachment = jAttachments.getJSONObject(i);
                    if (this.initialiserSequenceId != null) {
                        jAttachment.put(ID, this.initialiserSequenceId + "." + jAttachment.getString(ID));
                    }
                    jAttachment.remove(MULTIPART_ID);
                    if (jAttachment.hasAndNotNull(dispKey) && Part.ATTACHMENT.equalsIgnoreCase(jAttachment.getString(dispKey))) {
                        if (jAttachment.hasAndNotNull(VIRTUAL) && jAttachment.getBoolean(VIRTUAL)) {
                            jAttachment.remove(VIRTUAL);
                        } else {
                            if (jsonObject.has(HAS_ATTACHMENTS)) {
                                // Do not overwrite existing "has-attachment" information in a mail's JSON representation
                                // See bug 42695 & 42862
                                jsonObject.put(HAS_REAL_ATTACHMENTS, true);
                            } else {
                                jsonObject.put(HAS_ATTACHMENTS, true);
                            }
                        }
                        if (token && !jAttachment.hasAndNotNull("token")) {
                            try {
                                addToken(jAttachment, jAttachment.getString(ID));
                            } catch (final Exception e) {
                                // Missing field "id"
                            }
                        }
                    }
                }
            }
        } catch (final JSONException e) {
            LOG.error("", e);
        }
    }

    private boolean isVirtual(ContentType contentType) {
        return "virtual".equals(contentType.getParameter("nature"));
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
        multiparts.push(new MultipartInfo(id, mp.getContentType()));
        return true;
    }

    @Override
    public boolean handleMultipartEnd(final MailPart mp, final String id) throws OXException {
        if (null != altId && altId.equals(id)) {
            // Leaving multipart/alternative part
            altId = null;
            isAlternative = false;
        }
        multiparts.pop();
        return true;
    }

    @Override
    public boolean handleNestedMessage(final MailPart mailPart, final String id) throws OXException {

        String nestedMessageFullId = "";
        try {
            JSONObject nestedObject;
            if (currentNestingLevel < maxNestedMessageLevels) {
                // Get nested message from part
                MailMessage nestedMail = MailMessageParser.getMessageContentFrom(mailPart);
                if (null == nestedMail) {
                    LOG.warn("Ignoring nested message. Cannot handle part's content which should be a RFC822 message according to its content type.");
                    return true;
                }

                // Generate a dedicated JsonMessageHandler instance to parse the nested message
                JsonMessageHandler msgHandler = new JsonMessageHandler(accountId, null, null, displayMode, embedded, session, usm, ctx, token, ttlMillis, maxContentSize, maxNestedMessageLevels);
                msgHandler.setTimeZone(timeZone);
                msgHandler.includePlainText = includePlainText;
                msgHandler.attachHTMLAlternativePart = attachHTMLAlternativePart;
                msgHandler.tokenFolder = tokenFolder;
                msgHandler.tokenMailId = tokenMailId;
                msgHandler.exactLength = exactLength;
                // msgHandler.originalMailPath = originalMailPath;
                msgHandler.currentNestingLevel = currentNestingLevel + 1;
                if (this.initialiserSequenceId != null) {
                    nestedMessageFullId = this.initialiserSequenceId + "." + id;
                    msgHandler.setInitialiserSequenceId(initialiserSequenceId);
                }
                new MailMessageParser().parseMailMessage(nestedMail, msgHandler, id);
                nestedObject = msgHandler.getJSONObject();
                if (nestedMessageFullId.length() != 0) {
                    nestedObject.put(ID, nestedMessageFullId);
                }
            } else {
                // Only basic information
                nestedObject = new JSONObject(3);

            }
            /*
             * Sequence ID
             */
            if (!nestedObject.has(ID) && this.initialiserSequenceId == null) {
                nestedObject.put(ID, mailPart.containsSequenceId() ? mailPart.getSequenceId() : id);
            } else if (this.initialiserSequenceId != null && this.initialiserSequenceId.length() != 0) {
                nestedObject.put(ID, this.initialiserSequenceId + "." + id);
            }
            /*
             * Filename (if present)
             */
            if (mailPart.containsFileName()) {
                final String name = mailPart.getFileName();
                if (null != name) {
                    nestedObject.put(ATTACHMENT_FILE_NAME, name);
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
            jsonObject.put(PRIORITY, priority);
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
            Object value = receivedDate == null ? JSONObject.NULL : Long.valueOf(MessageWriter.addUserTimezone(receivedDate.getTime(), getTimeZone()));
            jsonObject.put(MailJSONField.RECEIVED_DATE.getKey(), value);
            if (false == MailProperties.getInstance().isPreferSentDate()) {
                jsonObject.put(MailJSONField.DATE.getKey(), value);
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleSentDate(final Date sentDate) throws OXException {
        try {
            Object value = sentDate == null ? JSONObject.NULL : Long.valueOf(MessageWriter.addUserTimezone(sentDate.getTime(), getTimeZone()));
            jsonObject.put(MailJSONField.SENT_DATE.getKey(), value);
            if (MailProperties.getInstance().isPreferSentDate()) {
                jsonObject.put(MailJSONField.DATE.getKey(), value);
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleSpecialPart(final MailPart part, final String baseContentType, final String fileName, final String id) throws OXException {
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
        return handleAttachment0(part, false, null, baseContentType, fileName, id, false);
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
            final JSONArray userFlagsArr = new JSONArray(userFlags.length);
            for (final String userFlag : userFlags) {
                if (MailMessage.isColorLabel(userFlag)) {
                    jsonObject.put(COLOR_LABEL, MailMessage.getColorLabelIntValue(userFlag));
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
                LOG.error("", e);
            }
        }
        return jsonObject;
    }

    private JSONObject asAttachment(final String id, final String baseContentType, final int len, final String fileName, final HtmlSanitizeResult sanitizeResult) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject(8);
            jsonObject.put(ID, id);
            jsonObject.put(CONTENT_TYPE, baseContentType);
            jsonObject.put(SIZE, len);
            jsonObject.put(DISPOSITION, Part.ATTACHMENT);
            if ((null != sanitizeResult) && (sanitizeResult.getContent() != null)) {
                jsonObject.put(CONTENT, sanitizeResult.getContent());
                jsonObject.put(TRUNCATED, sanitizeResult.isTruncated());
            } else {
                jsonObject.put(CONTENT, JSONObject.NULL);
            }
            if (fileName == null) {
                jsonObject.put(ATTACHMENT_FILE_NAME, JSONObject.NULL);
            } else {
                jsonObject.put(ATTACHMENT_FILE_NAME, MimeMessageUtility.decodeMultiEncodedHeader(fileName));
            }
            /*
             * Add token
             */
            addToken(jsonObject, id);
            getAttachmentListing().add(jsonObject);
            return jsonObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void asRawContent(String id, String baseContentType, HtmlSanitizeResult sanitizeResult) throws OXException {
        asRawContent(id, baseContentType, sanitizeResult, false);
    }

    private void asRawContent(String id, String baseContentType, HtmlSanitizeResult sanitizeResult, boolean asFirstAttachment) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject(6);
            jsonObject.put(ID, id);
            jsonObject.put(CONTENT_TYPE, baseContentType);
            jsonObject.put(SIZE, sanitizeResult.getContent().length());
            jsonObject.put(DISPOSITION, Part.INLINE);
            jsonObject.put(TRUNCATED, sanitizeResult.isTruncated());
            jsonObject.put(CONTENT, sanitizeResult.getContent());
            final MultipartInfo mpInfo = multiparts.peek();
            jsonObject.put(MULTIPART_ID, null == mpInfo ? JSONObject.NULL : mpInfo.mpId);

            if (asFirstAttachment) {
                getAttachmentListing().addFirst(jsonObject);
            } else {
                getAttachmentListing().add(jsonObject);
            }

        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject asDisplayHtml(final String id, final String baseContentType, final String htmlContent, final String charset) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject(6);
            jsonObject.put(ID, id);
            HtmlSanitizeResult sanitizeResult = HtmlProcessing.formatHTMLForDisplay(htmlContent, charset, session, mailPath, originalMailPath, usm, modified, displayMode, embedded, maxContentSize);
            final String content = sanitizeResult.getContent();

            jsonObject.put(TRUNCATED, sanitizeResult.isTruncated());
            jsonObject.put(CONTENT_TYPE, baseContentType);
            jsonObject.put(SIZE, content.length());
            jsonObject.put(DISPOSITION, Part.INLINE);
            jsonObject.put(CONTENT, content);
            final MultipartInfo mpInfo = multiparts.peek();
            jsonObject.put(MULTIPART_ID, null == mpInfo ? JSONObject.NULL : mpInfo.mpId);
            getAttachmentListing().add(jsonObject);
            return jsonObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject asDisplayText(final String id, final String baseContentType, final String htmlContent, final String fileName, final boolean addAttachment) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject(6);
            jsonObject.put(ID, id);
            jsonObject.put(CONTENT_TYPE, MimeTypes.MIME_TEXT_PLAIN);
            /*
             * Try to convert the given HTML to regular text
             */
            final String plainText = html2text(htmlContent);
            if (includePlainText) {
                jsonObject.put("plain_text", plainText);
            }
            HtmlSanitizeResult sanitizeResult = HtmlProcessing.formatTextForDisplay(plainText, usm, displayMode, maxContentSize);
            final String content = sanitizeResult.getContent();
            jsonObject.put(TRUNCATED, sanitizeResult.isTruncated());
            jsonObject.put(DISPOSITION, Part.INLINE);
            jsonObject.put(SIZE, content.length());
            jsonObject.put(CONTENT, content);
            final MultipartInfo mpInfo = multiparts.peek();
            jsonObject.put(MULTIPART_ID, null == mpInfo ? JSONObject.NULL : mpInfo.mpId);
            getAttachmentListing().add(jsonObject);
            if (addAttachment) {
                /*
                 * Create attachment object for original HTML content
                 */
                final JSONObject originalVersion = new JSONObject(6);
                originalVersion.put(ID, id);
                originalVersion.put(CONTENT_TYPE, baseContentType);
                originalVersion.put(DISPOSITION, Part.ATTACHMENT);
                originalVersion.put(SIZE, htmlContent.length());
                originalVersion.put(CONTENT, JSONObject.NULL);
                originalVersion.put(VIRTUAL, true);
                if (fileName == null) {
                    originalVersion.put(ATTACHMENT_FILE_NAME, JSONObject.NULL);
                } else {
                    originalVersion.put(ATTACHMENT_FILE_NAME, MimeMessageUtility.decodeMultiEncodedHeader(fileName));
                }
                getAttachmentListing().add(originalVersion);
            }
            return jsonObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject asPlainText(final String id, final String baseContentType, final HtmlSanitizeResult sanitizeResult) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject(6);
            jsonObject.put(ID, id);
            jsonObject.put(DISPOSITION, Part.INLINE);
            jsonObject.put(CONTENT_TYPE, baseContentType);
            jsonObject.put(SIZE, sanitizeResult.getContent().length());
            jsonObject.put(CONTENT, sanitizeResult.getContent());
            jsonObject.put(TRUNCATED, sanitizeResult.isTruncated());
            getAttachmentListing().add(jsonObject);
            return jsonObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private String html2text(final String htmlContent) {
        final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
        return null == htmlService ? null : htmlService.html2text(htmlContent, true);
    }

    private boolean hasNoImage(final String htmlContent) {
        return null == htmlContent || (com.openexchange.java.Strings.toLowerCase(htmlContent).indexOf("<img ") < 0);
    }

    // --------------------------------------------------------------------------------------------------------------------------

    private static class AttachmentListing {

        private List<JSONObject> attachments;
        private List<JSONObject> remainder;

        /**
         * Initializes a new {@link JsonMessageHandler.AttachmentListing}.
         */
        AttachmentListing() {
            super();
        }

        List<JSONObject> getAttachments() {
            return null == attachments ? Collections.<JSONObject> emptyList() : attachments;
        }

        void add(JSONObject jAttachment) {
            List<JSONObject> attachments = this.attachments;
            if (null == attachments) {
                attachments = new ArrayList<JSONObject>(6);
                this.attachments = attachments;
            }
            attachments.add(jAttachment);
        }

        void addFirst(JSONObject jAttachment) {
            List<JSONObject> attachments = this.attachments;
            if (null == attachments) {
                attachments = new ArrayList<JSONObject>(6);
                this.attachments = attachments;
            }
            attachments.add(0, jAttachment);
        }

        void removeFirst() {
            List<JSONObject> attachments = this.attachments;
            if (null != attachments) {
                attachments.remove(0);
            }
        }

        void addRemainder(JSONObject jAttachment) {
            List<JSONObject> remainder = this.remainder;
            if (null == remainder) {
                remainder = new ArrayList<JSONObject>(4);
                this.remainder = remainder;
            }
            remainder.add(jAttachment);
        }

        JSONArray toJsonArray() {
            List<JSONObject> attachments = this.attachments;
            List<JSONObject> remainder = this.remainder;

            JSONArray jArray = new JSONArray((null == attachments ? 0 : attachments.size()) + (null == remainder ? 0 : remainder.size()));

            if (null != attachments) {
                for (JSONObject jAttachment : attachments) {
                    jArray.put(jAttachment);
                }
            }

            if (null != remainder) {
                for (JSONObject jAttachment : remainder) {
                    jArray.put(jAttachment);
                }
            }

            return jArray;
        }
    }

}
