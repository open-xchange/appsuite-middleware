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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.mail.mime.utils.MIMEMessageUtility.decodeMultiEncodedHeader;
import static com.openexchange.mail.parser.MailMessageParser.generateFilename;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.text.Enriched2HtmlConverter;
import com.openexchange.mail.text.HTMLProcessing;
import com.openexchange.mail.text.RTF2HTMLConverter;
import com.openexchange.mail.text.parser.HTMLParser;
import com.openexchange.mail.text.parser.handler.HTML2TextHandler;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mail.uuencode.UUEncodedPart;
import com.openexchange.session.Session;

/**
 * {@link JSONMessageHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONMessageHandler implements MailMessageHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(JSONMessageHandler.class);

    private final Session session;

    private final Context ctx;

    private TimeZone timeZone;

    private final UserSettingMail usm;

    private final DisplayMode displayMode;

    private final int accountId;

    private final MailPath mailPath;

    private final JSONObject jsonObject;

    // private Html2TextConverter converter;

    private HTML2TextHandler html2textHandler;

    private JSONArray attachmentsArr;

    private JSONArray nestedMsgsArr;

    private boolean isAlternative;

    private String altId;

    private boolean textAppended;

    private final boolean[] modified;

    /**
     * Initializes a new {@link JSONMessageHandler}
     * 
     * @param accountId The account ID
     * @param mailPath The unique mail path
     * @param displayMode The display mode
     * @param session The session providing needed user data
     * @param usm The mail settings used for preparing message content if <code>displayVersion</code> is set to <code>true</code>; otherwise
     *            it is ignored.
     * @throws MailException If JSON message handler cannot be initialized
     */
    public JSONMessageHandler(final int accountId, final String mailPath, final DisplayMode displayMode, final Session session, final UserSettingMail usm) throws MailException {
        super();
        this.accountId = accountId;
        modified = new boolean[1];
        this.session = session;
        ctx = getContext(session);
        this.usm = usm;
        this.displayMode = displayMode;
        this.mailPath = new MailPath(mailPath);
        jsonObject = new JSONObject();
    }

    /**
     * Initializes a new {@link JSONMessageHandler}
     * 
     * @param accountId The account ID
     * @param mailPath The unique mail path
     * @param mail The mail message to add JSON fields not set by message parser traversal
     * @param displayMode The display mode
     * @param session The session providing needed user data
     * @param usm The mail settings used for preparing message content if <code>displayVersion</code> is set to <code>true</code>; otherwise
     *            it is ignored.
     * @throws MailException If JSON message handler cannot be initialized
     */
    public JSONMessageHandler(final int accountId, final MailPath mailPath, final MailMessage mail, final DisplayMode displayMode, final Session session, final UserSettingMail usm) throws MailException {
        this(accountId, mailPath, mail, displayMode, session, usm, getContext(session));
    }

    private static Context getContext(final Session session) throws MailException {
        try {
            return ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new MailException(e);
        }
    }

    /**
     * Initializes a new {@link JSONMessageHandler} for internal usage
     */
    private JSONMessageHandler(final int accountId, final MailPath mailPath, final MailMessage mail, final DisplayMode displayMode, final Session session, final UserSettingMail usm, final Context ctx) throws MailException {
        super();
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
                if (mail.containsFolder() && mail.getMailId() != null) {
                    jsonObject.put(FolderChildFields.FOLDER_ID, prepareFullname(accountId, mail.getFolder()));
                    jsonObject.put(DataFields.ID, mail.getMailId());
                }
                jsonObject.put(MailJSONField.UNREAD.getKey(), mail.getUnreadMessages());
                jsonObject.put(
                    MailJSONField.HAS_ATTACHMENTS.getKey(),
                    mail.containsHasAttachment() ? mail.hasAttachment() : mail.getContentType().isMimeType(MIMETypes.MIME_MULTIPART_MIXED));
                jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), mail.getContentType().getBaseType());
                jsonObject.put(MailJSONField.SIZE.getKey(), mail.getSize());
                // jsonObject.put(MailJSONField.THREAD_LEVEL.getKey(), mail.getThreadLevel());
                jsonObject.put(MailJSONField.ACCOUNT_NAME.getKey(), mail.getAccountName());
            }
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
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

    private HTML2TextHandler getHandler() {
        if (html2textHandler == null) {
            html2textHandler = new HTML2TextHandler(4096, true);
            /*
             * Add debugging information
             */
            if (jsonObject.hasAndNotNull(FolderChildFields.FOLDER_ID)) {
                html2textHandler.setMailFolderPath(prepareMailFolderParam(jsonObject.optString(FolderChildFields.FOLDER_ID)).getFullname());
            }
            if (jsonObject.hasAndNotNull(DataFields.ID)) {
                html2textHandler.setMailId(jsonObject.optLong(DataFields.ID));
            }
            html2textHandler.setContextId(session.getContextId());
            html2textHandler.setUserId(session.getUserId());
        }
        return html2textHandler;
    }

    private TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone());
        }
        return timeZone;
    }

    public boolean handleAttachment(final MailPart part, final boolean isInline, final String baseContentType, final String fileName, final String id) throws MailException {
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
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleBccRecipient(final InternetAddress[] recipientAddrs) throws MailException {
        try {
            jsonObject.put(MailJSONField.RECIPIENT_BCC.getKey(), MessageWriter.getAddressesAsArray(recipientAddrs));
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
        return true;
    }

    public boolean handleCcRecipient(final InternetAddress[] recipientAddrs) throws MailException {
        try {
            jsonObject.put(MailJSONField.RECIPIENT_CC.getKey(), MessageWriter.getAddressesAsArray(recipientAddrs));
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
        return true;
    }

    public boolean handleColorLabel(final int colorLabel) throws MailException {
        try {
            jsonObject.put(MailJSONField.COLOR_LABEL.getKey(), colorLabel);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
        return true;
    }

    public boolean handleContentId(final String contentId) throws MailException {
        try {
            jsonObject.put(MailJSONField.CID.getKey(), contentId);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
        return true;
    }

    public boolean handleFrom(final InternetAddress[] fromAddrs) throws MailException {
        try {
            jsonObject.put(MailJSONField.FROM.getKey(), MessageWriter.getAddressesAsArray(fromAddrs));
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
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

    public boolean handleHeaders(final int size, final Iterator<Entry<String, String>> iter) throws MailException {
        if (size == 0) {
            return true;
        }
        try {
            final JSONObject hdrObject = new JSONObject();
            for (int i = 0; i < size; i++) {
                final Map.Entry<String, String> entry = iter.next();
                if (MessageHeaders.HDR_DISP_NOT_TO.equalsIgnoreCase(entry.getKey())) {
                    /*
                     * This special header is handled through handleDispositionNotification()
                     */
                    continue;
                } else if (MessageHeaders.HDR_X_PRIORITY.equalsIgnoreCase(entry.getKey())) {
                    /*
                     * Priority
                     */
                    int priority = MailMessage.PRIORITY_NORMAL;
                    if (null != entry.getValue()) {
                        final String[] tmp = entry.getValue().split(" +");
                        try {
                            priority = Integer.parseInt(tmp[0]);
                        } catch (final NumberFormatException nfe) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("Strange X-Priority header: " + tmp[0]);
                            }
                            priority = MailMessage.PRIORITY_NORMAL;
                        }
                    }
                    jsonObject.put(MailJSONField.PRIORITY.getKey(), priority);
                } else if (MessageHeaders.HDR_X_MAILER.equalsIgnoreCase(entry.getKey())) {
                    hdrObject.put(entry.getKey(), entry.getValue());
                } else if (MessageHeaders.HDR_X_OX_VCARD.equalsIgnoreCase(entry.getKey())) {
                    jsonObject.put(MailJSONField.VCARD.getKey(), true);
                } else if (MessageHeaders.HDR_X_OX_NOTIFICATION.equalsIgnoreCase(entry.getKey())) {
                    jsonObject.put(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(), entry.getValue());
                } else {
                    if (!COVERED_HEADER_NAMES.contains(HeaderName.valueOf(entry.getKey()))) {
                        hdrObject.put(entry.getKey(), decodeMultiEncodedHeader(entry.getValue()));
                    }
                }
            }
            jsonObject.put(MailJSONField.HEADERS.getKey(), hdrObject);
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleImagePart(final MailPart part, final String imageCID, final String baseContentType, final boolean isInline, final String fileName, final String id) throws MailException {
        return handleAttachment(part, isInline, baseContentType, fileName, id);
    }

    public boolean handleInlineHtml(final String htmlContent, final ContentType contentType, final long size, final String fileName, final String id) throws MailException {
        if (textAppended) {
            /*
             * A text part has already been detected as message's body
             */
            if (isAlternative) {
                if (DisplayMode.DISPLAY.equals(displayMode)) {
                    /*
                     * Add HTML alternative part as attachment
                     */
                    asAttachment(id, contentType.getBaseType(), htmlContent.length(), fileName);
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
                asAttachment(id, contentType.getBaseType(), htmlContent.length(), fileName);
            }
        } else {
            /*
             * No text part was present before
             */
            if (DisplayMode.MODIFYABLE.getMode() <= displayMode.getMode()) {
                if (usm.isDisplayHtmlInlineContent()) {
                    asDisplayHtml(id, contentType.getBaseType(), htmlContent, contentType.getCharsetParameter());
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
                    throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
                }
            }
            textAppended = true;
        }
        return true;

    }

    private static final Enriched2HtmlConverter ENRCONV = new Enriched2HtmlConverter();

    // private static final RTF2HtmlConverter RTFCONV = new RTF2HtmlConverter();

    public boolean handleInlinePlainText(final String plainTextContentArg, final ContentType contentType, final long size, final String fileName, final String id) throws MailException {
        if (isAlternative && usm.isDisplayHtmlInlineContent() && (DisplayMode.RAW.getMode() < displayMode.getMode()) && contentType.isMimeType(MIMETypes.MIME_TEXT_PLAIN)) {
            /*
             * User wants to see message's alternative content
             */
            /* textAppended = true; */
            return true;
        }
        try {
            if (contentType.isMimeType(MIMETypes.MIME_TEXT_ENRICHED) || contentType.isMimeType(MIMETypes.MIME_TEXT_RICHTEXT) || contentType.isMimeType(MIMETypes.MIME_TEXT_RTF)) {
                if (textAppended) {
                    if (DisplayMode.DISPLAY.equals(displayMode)) {
                        /*
                         * Add alternative part as attachment
                         */
                        asAttachment(id, contentType.getBaseType(), plainTextContentArg.length(), fileName);
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
                    if (usm.isDisplayHtmlInlineContent()) {
                        asDisplayHtml(
                            id,
                            contentType.getBaseType(),
                            getHtmlDisplayVersion(contentType, plainTextContentArg),
                            contentType.getCharsetParameter());
                    } else {
                        asDisplayText(
                            id,
                            contentType.getBaseType(),
                            getHtmlDisplayVersion(contentType, plainTextContentArg),
                            fileName,
                            DisplayMode.DISPLAY.equals(displayMode));
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
                    getAttachmentsArr().put(jsonObject);
                }
                textAppended = true;
                return true;
            }
            /*
             * Just usual plain text
             */
            asPlainText(id, contentType.getBaseType(), HTMLProcessing.formatTextForDisplay(plainTextContentArg, usm, displayMode));
            if (textAppended) {
                /*
                 * A plain text message body has already been detected; append inline text as an attachment, too
                 */
                asAttachment(id, contentType.getBaseType(), plainTextContentArg.length(), fileName);
            } else {
                textAppended = true;
            }
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private String getHtmlDisplayVersion(final ContentType contentType, final String src) {
        if (contentType.isMimeType(MIMETypes.MIME_TEXT_ENRICHED) || contentType.isMimeType(MIMETypes.MIME_TEXT_RICHTEXT)) {
            return HTMLProcessing.formatHTMLForDisplay(
                ENRCONV.convert(src),
                contentType.getCharsetParameter(),
                session,
                mailPath,
                usm,
                modified,
                displayMode);
        } else if (contentType.isMimeType(MIMETypes.MIME_TEXT_RTF)) {
            return HTMLProcessing.formatHTMLForDisplay(
                RTF2HTMLConverter.convertRTFToHTML(src),
                contentType.getCharsetParameter(),
                session,
                mailPath,
                usm,
                modified,
                displayMode);
        }
        return HTMLProcessing.formatTextForDisplay(src, usm, displayMode);
    }

    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws MailException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            String contentType = MIMETypes.MIME_APPL_OCTET;
            final String filename = part.getFileName();
            try {
                final Locale locale = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale();
                contentType = MIMEType2ExtMap.getContentType(new File(filename.toLowerCase(locale)).getName()).toLowerCase(locale);
            } catch (final Exception e) {
                final Throwable t = new Throwable(
                    new StringBuilder("Unable to fetch content/type for '").append(filename).append("': ").append(e).toString());
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
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType, final int size, final String fileName, final String id) throws MailException {
        return handleInlinePlainText(decodedTextContent, contentType, size, fileName, id);
    }

    public void handleMessageEnd(final MailMessage mail) throws MailException {
        /*
         * Since we obviously touched message's content, mark its corresponding message object as seen
         */
        mail.setFlags(mail.getFlags() | MailMessage.FLAG_SEEN);
        if (!textAppended) {
            try {
                /*
                 * Ensure at least one text part is present
                 */
                final JSONObject jsonObject = new JSONObject();
                jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), MIMETypes.MIME_TEXT_PLAIN);
                jsonObject.put(MailJSONField.SIZE.getKey(), 0);
                jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
                jsonObject.put(MailJSONField.CONTENT.getKey(), "");
                getAttachmentsArr().put(jsonObject);
            } catch (final JSONException e) {
                throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
            }
        }
    }

    public boolean handleMultipart(final MailPart mp, final int bodyPartCount, final String id) throws MailException {
        /*
         * Determine if message is of MIME type multipart/alternative
         */
        if (mp.getContentType().isMimeType(MIMETypes.MIME_MULTIPART_ALTERNATIVE) && bodyPartCount >= 2) {
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

    public boolean handleNestedMessage(final MailPart mailPart, final String id) throws MailException {
        try {
            final Object content = mailPart.getContent();
            final MailMessage nestedMail;
            if (content instanceof MailMessage) {
                nestedMail = (MailMessage) content;
            } else if (content instanceof InputStream) {
                try {
                    nestedMail = MIMEMessageConverter.convertMessage(new MimeMessage(
                        MIMEDefaultSession.getDefaultSession(),
                        (InputStream) content));
                } catch (final MessagingException e) {
                    throw MIMEMailException.handleMessagingException(e);
                }
            } else {
                LOG.error("Ignoring nested message. Cannot handle part's content which should be a RFC822 message according to its content type: " + (null == content ? "null" : content.getClass().getSimpleName()));
                return true;
            }
            final JSONMessageHandler msgHandler = new JSONMessageHandler(accountId, null, null, displayMode, session, usm, ctx);
            new MailMessageParser().parseMailMessage(nestedMail, msgHandler, id);
            final JSONObject nestedObject = msgHandler.getJSONObject();
            /*
             * Sequence ID
             */
            nestedObject.put(MailListField.ID.getKey(), mailPart.containsSequenceId() ? mailPart.getSequenceId() : id);
            /*
             * Filename (if present)
             */
            if (mailPart.containsFileName() && mailPart.getFileName() != null) {
                nestedObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), mailPart.getFileName());
            }
            getNestedMsgsArr().put(nestedObject);
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handlePriority(final int priority) throws MailException {
        try {
            jsonObject.put(MailJSONField.PRIORITY.getKey(), priority);
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleMsgRef(final String msgRef) throws MailException {
        try {
            jsonObject.put(MailJSONField.MSGREF.getKey(), msgRef);
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleDispositionNotification(final InternetAddress dispositionNotificationTo, final boolean seen) throws MailException {
        try {
            if (!seen) {
                jsonObject.put(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(), dispositionNotificationTo.toUnicodeString());
            }
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleReceivedDate(final Date receivedDate) throws MailException {
        try {
            jsonObject.put(
                MailJSONField.RECEIVED_DATE.getKey(),
                receivedDate == null ? JSONObject.NULL : Long.valueOf(MessageWriter.addUserTimezone(receivedDate.getTime(), getTimeZone())));
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleSentDate(final Date sentDate) throws MailException {
        try {
            jsonObject.put(
                MailJSONField.SENT_DATE.getKey(),
                sentDate == null ? JSONObject.NULL : Long.valueOf(MessageWriter.addUserTimezone(sentDate.getTime(), getTimeZone())));
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleSpecialPart(final MailPart part, final String baseContentType, final String fileName, final String id) throws MailException {
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
                throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
            }
        }
         */
        /*
         * When creating a JSON message object from a message we do not distinguish special parts or image parts from "usual" attachments.
         * Therefore invoke the handleAttachment method. Maybe we need a separate handling in the future for vcards.
         */
        return handleAttachment(part, false, baseContentType, fileName, id);
    }

    public boolean handleSubject(final String subject) throws MailException {
        try {
            jsonObject.put(MailJSONField.SUBJECT.getKey(), subject == null ? JSONObject.NULL : subject.trim());
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleSystemFlags(final int flags) throws MailException {
        try {
            if (jsonObject.hasAndNotNull(MailJSONField.FLAGS.getKey())) {
                final int prevFlags = jsonObject.getInt(MailJSONField.FLAGS.getKey());
                jsonObject.put(MailJSONField.FLAGS.getKey(), prevFlags | flags);
            } else {
                jsonObject.put(MailJSONField.FLAGS.getKey(), flags);
            }
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleToRecipient(final InternetAddress[] recipientAddrs) throws MailException {
        try {
            jsonObject.put(MailJSONField.RECIPIENT_TO.getKey(), MessageWriter.getAddressesAsArray(recipientAddrs));
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleUserFlags(final String[] userFlags) throws MailException {
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
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
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

    private void asAttachment(final String id, final String baseContentType, final int len, final String fileName) throws MailException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), len);
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
            jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
            if (fileName == null) {
                jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), JSONObject.NULL);
            } else {
                jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), MIMEMessageUtility.decodeMultiEncodedHeader(fileName));
            }
            getAttachmentsArr().put(jsonObject);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private void asRawContent(final String id, final String baseContentType, final String content) throws MailException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            getAttachmentsArr().put(jsonObject);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private void asDisplayHtml(final String id, final String baseContentType, final String htmlContent, final String charset) throws MailException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            final String content = HTMLProcessing.formatHTMLForDisplay(htmlContent, charset, session, mailPath, usm, modified, displayMode);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            getAttachmentsArr().put(jsonObject);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private void asDisplayText(final String id, final String baseContentType, final String htmlContent, final String fileName, final boolean addAttachment) throws MailException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), MIMETypes.MIME_TEXT_PLAIN);
            /*
             * Try to convert the given html to regular text
             */
            HTMLParser.parse(HTMLProcessing.getConformHTML(htmlContent, (String) null), getHandler().reset());
            final String content = HTMLProcessing.formatTextForDisplay(getHandler().getText(), usm, displayMode);
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            getAttachmentsArr().put(jsonObject);
            if (addAttachment) {
                /*
                 * Create attachment object for original html content
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
                    originalVersion.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), MIMEMessageUtility.decodeMultiEncodedHeader(fileName));
                }
                getAttachmentsArr().put(originalVersion);
            }
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private void asPlainText(final String id, final String baseContentType, final String content) throws MailException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(MailListField.ID.getKey(), id);
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            getAttachmentsArr().put(jsonObject);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }
}
