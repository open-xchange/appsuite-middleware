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

package com.openexchange.mail.json.writer;

import java.util.Locale;
import java.util.TimeZone;
import javax.mail.Part;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.html.HtmlService;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.text.Enriched2HtmlConverter;
import com.openexchange.mail.text.HtmlProcessing;
import com.openexchange.mail.text.Rtf2HtmlConverter;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link JSONObjectConverter} - Converts a raw JSON mail representation into a user-sensitive JSON mail representation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONObjectConverter {

    private final Session session;

    private final Context ctx;

    private final JSONObject rawJSONMailObject;

    private TimeZone timeZone;

    private final UserSettingMail usm;

    private final DisplayMode displayMode;

    private final MailPath mailPath;

    // private Html2TextConverter converter;

    private final boolean[] modified;

    /**
     * Initializes a new {@link JSONObjectConverter}.
     *
     * @param rawJSONMailObject The raw JSON mail representation to convert
     * @param displayMode The request-specific display mode
     * @param session The session providing needed user information
     * @param usm The (possibly request-specific) mail settings
     * @param ctx The context
     * @throws OXException If initialization fails
     */
    public JSONObjectConverter(final JSONObject rawJSONMailObject, final DisplayMode displayMode, final Session session, final UserSettingMail usm, final Context ctx) throws OXException {
        super();
        this.rawJSONMailObject = rawJSONMailObject;
        // this.accountId = accountId;
        modified = new boolean[1];
        this.session = session;
        this.ctx = ctx;
        this.usm = usm;
        this.displayMode = displayMode;
        {
            final String key = MailJSONField.MSGREF.getKey();
            final String mailPathStr = rawJSONMailObject.optString(key);
            if (null != mailPathStr) {
                this.mailPath = new MailPath(mailPathStr);
            } else {
                this.mailPath = null;
            }
        }
    }

    private TimeZone getTimeZone() throws OXException {
        if (timeZone == null) {
            timeZone = TimeZoneUtils.getTimeZone(UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone());
        }
        return timeZone;
    }

    /**
     * Converts this converter's raw JSON mail representation into a user-sensitive JSON mail representation.
     *
     * @return The user-sensitive JSON mail representation
     * @throws OXException If conversion fails
     */
    public JSONObject raw2Json() throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            /*
             * Mail path
             */
            if (DisplayMode.MODIFYABLE.equals(this.displayMode) && null != mailPath) {
                jsonObject.put(MailJSONField.MSGREF.getKey(), mailPath.toString());
            }
            /*
             * Header stuff
             */
            copyValue(FolderChildFields.FOLDER_ID, rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.UNREAD.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.ACCOUNT_NAME.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.ACCOUNT_ID.getKey(), rawJSONMailObject, jsonObject);
            raw2JsonMail0(rawJSONMailObject, jsonObject);
            return jsonObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void raw2JsonMail0(final JSONObject rawJSONMailObject, final JSONObject jsonObject) throws OXException {
        try {
            copyValue(DataFields.ID, rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.HAS_ATTACHMENTS.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.CONTENT_TYPE.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.SIZE.getKey(), rawJSONMailObject, jsonObject);
            raw2Json0(rawJSONMailObject, jsonObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void raw2Json0(final JSONObject rawJSONMailObject, final JSONObject jsonObject) throws OXException {
        try {
            copyValue(MailJSONField.RECIPIENT_BCC.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.RECIPIENT_CC.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.COLOR_LABEL.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.CID.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.FROM.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.PRIORITY.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.VCARD.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.HEADERS.getKey(), rawJSONMailObject, jsonObject);
            {
                final String key = MailJSONField.RECEIVED_DATE.getKey();
                if (rawJSONMailObject.hasAndNotNull(key)) {
                    final long receivedDateMillis = rawJSONMailObject.getLong(key);
                    jsonObject.put(key, MessageWriter.addUserTimezone(receivedDateMillis, getTimeZone()));
                } else {
                    jsonObject.put(key, JSONObject.NULL);
                }
            }
            {
                final String key = MailJSONField.SENT_DATE.getKey();
                if (rawJSONMailObject.hasAndNotNull(key)) {
                    final long sentDateMillis = rawJSONMailObject.getLong(key);
                    jsonObject.put(key, MessageWriter.addUserTimezone(sentDateMillis, getTimeZone()));
                } else {
                    jsonObject.put(key, JSONObject.NULL);
                }
            }
            copyValue(MailJSONField.SUBJECT.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.FLAGS.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.RECIPIENT_TO.getKey(), rawJSONMailObject, jsonObject);
            copyValue(MailJSONField.USER.getKey(), rawJSONMailObject, jsonObject);
            /*
             * Handle inline and file attachments
             */
            final JSONArray attachmentsArr = new JSONArray();
            /*
             * Handle body (non-file-attachments)
             */
            final JSONArray bodyArr;
            {
                final JSONArray opt = rawJSONMailObject.optJSONArray("body");
                if (null == opt || 0 == opt.length()) {
                    bodyArr = new JSONArray();
                    bodyArr.put(dummyObject());
                } else {
                    bodyArr = opt;
                }
            }
            final int bodyLen = bodyArr.length();

            // final boolean alternative = rawJSONMailObject.optBoolean("alternative");

            /*
             * Find appropriate content
             */
            if (usm.isDisplayHtmlInlineContent()) {
                // HTML
                final JSONObject htmlObject = extractObject(bodyArr, "text/htm");
                if (null == htmlObject) {
                    // No HTML found
                    final JSONObject jo;
                    if (0 == bodyLen) {
                        jo = dummyObject();
                    } else {
                        jo = bodyArr.getJSONObject(0);
                    }
                    handleTextPart(jo, attachmentsArr);
                } else {
                    // HTML part found
                    handleHTMLPart(htmlObject, attachmentsArr);
                }
            } else {
                // Text
                final JSONObject textObject = extractObject(bodyArr, "text/plain", "text/enriched", "text/richtext", "text/rtf");
                if (null == textObject) {
                    // No text part found
                    if (bodyLen == 0) {
                        final JSONObject jo = dummyObject();
                        handleTextPart(jo, attachmentsArr);
                    } else {
                        // HTML part found
                        final JSONObject jo = bodyArr.getJSONObject(0);
                        if (DisplayMode.MODIFYABLE.getMode() <= displayMode.getMode()) {
                            asDisplayText(
                                jo,
                                jo.getString(MailJSONField.CONTENT.getKey()),
                                DisplayMode.DISPLAY.equals(displayMode),
                                attachmentsArr);
                        } else if (DisplayMode.RAW.equals(displayMode)) {
                            /*
                             * As-is
                             */
                            attachmentsArr.put(jo);
                        } else {
                            /*
                             * As-is
                             */
                            attachmentsArr.put(jo);
                        }
                    }
                } else {
                    handleTextPart(textObject, attachmentsArr);
                }
            }
            /*
             * Add attachments
             */
            final JSONArray attachArr = rawJSONMailObject.optJSONArray(MailJSONField.ATTACHMENTS.getKey());
            if (null != attachArr) {
                final int len = attachArr.length();
                for (int i = 0; i < len; i++) {
                    attachmentsArr.put(attachArr.get(i));
                }
            }
            jsonObject.put(MailJSONField.ATTACHMENTS.getKey(), attachmentsArr);

            /*
             * Add nested messages
             */
            final JSONArray nestedMsgs = rawJSONMailObject.optJSONArray(MailJSONField.NESTED_MESSAGES.getKey());
            if (null != nestedMsgs) {
                final JSONArray nestedMessages = new JSONArray();
                final int len = nestedMsgs.length();
                for (int i = 0; i < len; i++) {
                    final JSONObject nestedMsg = nestedMsgs.getJSONObject(i);
                    final JSONObject jo = new JSONObject();
                    raw2JsonMail0(nestedMsg, jo);
                    nestedMessages.put(jo);
                }
                jsonObject.put(MailJSONField.NESTED_MESSAGES.getKey(), nestedMessages);
            }

            /*
             * Modified flag
             */
            if (!jsonObject.has(MailJSONField.MODIFIED.getKey())) {
                jsonObject.put(MailJSONField.MODIFIED.getKey(), modified[0] ? 1 : 0);
            }

        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void handleTextPart(final JSONObject textObject, final JSONArray attachmentsArr) throws OXException, JSONException {
        // Text part found
        if (DisplayMode.MODIFYABLE.getMode() <= displayMode.getMode()) {
            final String displayVersion =
                getHtmlDisplayVersion(
                    new ContentType(textObject.getString(MailJSONField.CONTENT_TYPE.getKey())),
                    textObject.getString(MailJSONField.CONTENT.getKey()));
            asPlainText(textObject, displayVersion, attachmentsArr);
        } else if (DisplayMode.RAW.equals(displayMode)) {
            /*
             * As-is
             */
            attachmentsArr.put(textObject);
        } else {
            /*
             * As-is
             */
            attachmentsArr.put(textObject);
        }
    }

    private void handleHTMLPart(final JSONObject htmlObject, final JSONArray attachmentsArr) throws OXException {
        // HTML part found
        if (DisplayMode.MODIFYABLE.getMode() <= displayMode.getMode()) {
            // Prepare HTML content
            asDisplayHtml(htmlObject, attachmentsArr);
        } else if (DisplayMode.RAW.equals(displayMode)) {
            // As-is
            attachmentsArr.put(htmlObject);
        } else {
            // As-is
            attachmentsArr.put(htmlObject);
        }
    }

    private void asAttachment(final JSONObject bodyObject, final JSONArray attachmentsArr) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            copyValue(MailListField.ID.getKey(), bodyObject, jsonObject);
            copyValue(MailJSONField.CONTENT_TYPE.getKey(), bodyObject, jsonObject);
            copyValue(MailJSONField.DISPOSITION.getKey(), bodyObject, jsonObject);
            copyValue(MailJSONField.SIZE.getKey(), bodyObject, jsonObject);
            copyValue(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), bodyObject, jsonObject);
            jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
            attachmentsArr.put(jsonObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void asPlainText(final JSONObject textObject, final String content, final JSONArray attachmentsArr) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            copyValue(MailListField.ID.getKey(), textObject, jsonObject);
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), MimeTypes.MIME_TEXT_PLAIN);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            attachmentsArr.put(jsonObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void asDisplayHtml(final JSONObject htmlObject, final JSONArray attachmentsArr) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            copyValue(MailListField.ID.getKey(), htmlObject, jsonObject);
            final String content =
                HtmlProcessing.formatHTMLForDisplay(
                    htmlObject.getString(MailJSONField.CONTENT.getKey()),
                    "UTF-8",
                    session,
                    mailPath,
                    usm,
                    modified,
                    displayMode,
                    false);
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), MimeTypes.MIME_TEXT_HTML);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            attachmentsArr.put(jsonObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void asDisplayHtml(final JSONObject htmlObject, final String content, final JSONArray attachmentsArr) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            copyValue(MailListField.ID.getKey(), htmlObject, jsonObject);
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            copyValue(MailJSONField.CONTENT_TYPE.getKey(), htmlObject, jsonObject);
            jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            attachmentsArr.put(jsonObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void asDisplayText(final JSONObject htmlObject, final String htmlContent, final boolean addAttachment, final JSONArray attachmentsArr) throws OXException {
        try {
            final JSONObject jsonObject = new JSONObject();
            copyValue(MailListField.ID.getKey(), htmlObject, jsonObject);
            jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), MimeTypes.MIME_TEXT_PLAIN);
            /*
             * Try to convert the given html to regular text
             */
            final String content;
            {
                final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                final String plainText = htmlService.html2text(htmlContent, true);
                content = HtmlProcessing.formatTextForDisplay(plainText, usm, displayMode);
            }
            jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
            copyValue(MailJSONField.SIZE.getKey(), htmlObject, jsonObject);
            jsonObject.put(MailJSONField.CONTENT.getKey(), content);
            attachmentsArr.put(jsonObject);
            if (addAttachment) {
                /*
                 * Create attachment object for original html content
                 */
                final JSONObject originalVersion = new JSONObject();
                copyValue(MailListField.ID.getKey(), htmlObject, originalVersion);
                jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), MimeTypes.MIME_TEXT_HTML);
                originalVersion.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
                originalVersion.put(MailJSONField.SIZE.getKey(), htmlContent.length());
                originalVersion.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
                final String fileName = htmlObject.optString(MailJSONField.ATTACHMENT_FILE_NAME.getKey());
                if (fileName == null) {
                    originalVersion.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), JSONObject.NULL);
                } else {
                    originalVersion.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), MimeMessageUtility.decodeMultiEncodedHeader(fileName));
                }
                attachmentsArr.put(originalVersion);
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static final Enriched2HtmlConverter ENRCONV = new Enriched2HtmlConverter();

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
                false);
        } else if (baseType.startsWith(MimeTypes.MIME_TEXT_RTF)) {
            return HtmlProcessing.formatHTMLForDisplay(
                Rtf2HtmlConverter.convertRTFToHTML(src),
                contentType.getCharsetParameter(),
                session,
                mailPath,
                usm,
                modified,
                displayMode,
                false);
        }
        return HtmlProcessing.formatTextForDisplay(src, usm, displayMode);
    }

    private static JSONObject extractObject(final JSONArray bodyArr, final String... contentTypes) throws JSONException {
        JSONObject retval = null;
        final int bodyLen = bodyArr.length();
        for (int i = 0; null == retval && i < bodyLen; i++) {
            final JSONObject jsonObject = bodyArr.getJSONObject(i);
            final String ct = jsonObject.optString(MailJSONField.CONTENT_TYPE.getKey());
            if (null != ct && startsWithEither(ct, contentTypes)) {
                retval = jsonObject;
            }
        }
        return retval;
    }

    /**
     * Checks if specified string starts with either of passed prefixes.
     *
     * @param s The string to check
     * @param prefixes The prefixes
     * @return <code>true</code> if specified string starts with either of passed prefixes; otherwise <code>false</code>
     */
    private static boolean startsWithEither(final String s, final String[] prefixes) {
        boolean startsWith = false;
        for (int i = 0; !startsWith && i < prefixes.length; i++) {
            startsWith = s.startsWith(prefixes[i]);
        }
        return startsWith;
    }

    /**
     * Creates a dummy JSON object for an empty plain text mail body.
     *
     * @return A dummy JSON object for an empty plain text mail body
     * @throws JSONException If a JSON error occurs
     */
    private static JSONObject dummyObject() throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(MailJSONField.DISPOSITION.getKey(), "inline");
        jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), "text/plain");
        jsonObject.put(MailJSONField.SIZE.getKey(), 0);
        jsonObject.put(MailJSONField.CONTENT.getKey(), "");
        return jsonObject;
    }

    /**
     * (Shallow) Copies the value associated with given key from source JSON object to destination JSON object if present and not
     * {@link JSONObject#NULL NULL}.
     *
     * @param key The key
     * @param src The source JSON object
     * @param dst The destination JSON object
     * @throws JSONException If a JSON error occurs
     */
    private static void copyValue(final String key, final JSONObject src, final JSONObject dst) throws JSONException {
        if (src.hasAndNotNull(key)) {
            dst.put(key, src.opt(key));
        }
    }

}
