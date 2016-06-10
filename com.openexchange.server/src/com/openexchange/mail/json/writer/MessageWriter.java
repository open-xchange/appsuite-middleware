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

import static com.openexchange.mail.mime.utils.MimeMessageUtility.decodeMultiEncodedHeader;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.TimeZone;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.Delegatized;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.JsonMessageHandler;
import com.openexchange.mail.parser.handlers.RawJSONMessageHandler;
import com.openexchange.mail.structure.StructureMailMessageParser;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link MessageWriter} - Writes {@link MailMessage} instances as JSON strings
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageWriter {

    // private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageWriter.class);

    /**
     * No instantiation
     */
    private MessageWriter() {
        super();
    }

    /**
     * Writes specified mail's structure as a JSON object.
     * <p>
     * Optionally a prepared version can be returned, this includes following actions:
     * <ol>
     * <li>Header names are inserted to JSON lower-case</li>
     * <li>Mail-safe encoded header values as per RFC 2047 are decoded;<br>
     * e.g.&nbsp;<code><i>To:&nbsp;=?iso-8859-1?q?Keld_J=F8rn?=&nbsp;&lt;keld@xyz.dk&gt;</i></code></li>
     * <li>Address headers are delivered as JSON objects with a <code>"personal"</code> and an <code>"address"</code> field</li>
     * <li>Parameterized headers are delivered as JSON objects with a <code>"type"</code> and a <code>"params"</code> field</li>
     * </ol>
     *
     * @param accountId The mail's account ID
     * @param mail The mail to write
     * @param maxSize The allowed max. size
     * @return The structure as a JSON object
     * @throws OXException If writing structure fails
     */
    public static JSONObject writeStructure(int accountId, MailMessage mail, long maxSize) throws OXException {
        {
            LogProperties.putProperty(LogProperties.Name.MAIL_ACCOUNT_ID, Integer.toString(accountId));
            if (null != mail.getFolder()) {
                LogProperties.putProperty(LogProperties.Name.MAIL_FULL_NAME, mail.getFolder());
            }
            if (null != mail.getMailId()) {
                LogProperties.putProperty(LogProperties.Name.MAIL_MAIL_ID, mail.getMailId());
            }
        }
        MIMEStructureHandler handler = new MIMEStructureHandler(maxSize);
        mail.setAccountId(accountId);
        new StructureMailMessageParser().setParseTNEFParts(true).parseMailMessage(mail, handler);
        return handler.getJSONMailObject();
    }

    /**
     * Writes whole mail as a JSON object.
     *
     * @param accountId The account ID
     * @param mail The mail to write
     * @param displayMode The display mode
     * @param session The session
     * @param settings The user's mail settings used for writing message; if <code>null</code> the settings are going to be fetched from
     *            storage, thus no request-specific preparations will take place.
     * @param warnings A container for possible warnings
     * @return The written JSON object
     * @throws OXException If writing message fails
     */
    public static JSONObject writeMailMessage(final int accountId, final MailMessage mail, final DisplayMode displayMode, final boolean embedded, final Session session, final UserSettingMail settings) throws OXException {
        return writeMailMessage(accountId, mail, displayMode, embedded, session, settings, null, false, -1);
    }

    /**
     * Writes whole mail as a JSON object.
     *
     * @param accountId The account ID
     * @param mail The mail to write
     * @param displayMode The display mode
     * @param session The session
     * @param settings The user's mail settings used for writing message; if <code>null</code> the settings are going to be fetched from
     *            storage, thus no request-specific preparations will take place.
     * @param warnings A container for possible warnings
     * @param tokenTimeout
     * @token <code>true</code> to add attachment tokens
     * @return The written JSON object
     * @throws OXException If writing message fails
     */
    public static JSONObject writeMailMessage(final int accountId, final MailMessage mail, final DisplayMode displayMode, final boolean embedded, final Session session, final UserSettingMail settings, final Collection<OXException> warnings, final boolean token, final int tokenTimeout) throws OXException {
        return writeMailMessage(accountId, mail, displayMode, embedded, session, settings, warnings, token, tokenTimeout, null);
    }

    /**
     * Writes whole mail as a JSON object.
     *
     * @param accountId The account ID
     * @param mail The mail to write
     * @param displayMode The display mode
     * @param session The session
     * @param settings The user's mail settings used for writing message; if <code>null</code> the settings are going to be fetched from
     *            storage, thus no request-specific preparations will take place.
     * @param warnings A container for possible warnings
     * @param tokenTimeout
     * @param mimeFilter The MIME filter
     * @token <code>true</code> to add attachment tokens
     * @return The written JSON object
     * @throws OXException If writing message fails
     */
    public static JSONObject writeMailMessage(final int accountId, final MailMessage mail, final DisplayMode displayMode, final boolean embedded, final Session session, final UserSettingMail settings, final Collection<OXException> warnings, final boolean token, final int tokenTimeout, final MimeFilter mimeFilter) throws OXException {
        return writeMailMessage(accountId, mail, displayMode, embedded, session, settings, warnings, token, tokenTimeout, mimeFilter, null, false, -1);
    }

    /**
     * Writes whole mail as a JSON object by trimming the mail content to the length provided by maxContentSize parameter
     *
     * @param accountId The account ID
     * @param mail The mail to write
     * @param displayMode The display mode
     * @param session The session
     * @param settings The user's mail settings used for writing message; if <code>null</code> the settings are going to be fetched from
     *            storage, thus no request-specific preparations will take place.
     * @param warnings A container for possible warnings
     * @param tokenTimeout
     * @param mimeFilter The MIME filter
     * @param maxContentSize maximum number of bytes that is will be returned for content. '<=0' means unlimited.
     * @token <code>true</code> to add attachment tokens
     * @return The written JSON object
     * @throws OXException If writing message fails
     */
    public static JSONObject writeMailMessage(final int accountId, final MailMessage mail, final DisplayMode displayMode, final boolean embedded, final Session session, final UserSettingMail settings, final Collection<OXException> warnings, final boolean token, final int tokenTimeout, final MimeFilter mimeFilter, final TimeZone optTimeZone, final boolean exactLength, final int maxContentSize) throws OXException {
        final MailPath mailPath;
        final String fullName = mail.getFolder();
        final String mailId = mail.getMailId();
        if (fullName != null && mailId != null) {
            mailPath = new MailPath(accountId, fullName, mailId);
        } else if (mail.getMsgref() != null) {
            mailPath = mail.getMsgref();
        } else {
            mailPath = MailPath.NULL;
        }
        final UserSettingMail usm = null == settings ? UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContextId()) : settings;
        /*
         * Add log properties
         */
        {
            LogProperties.putProperty(LogProperties.Name.MAIL_ACCOUNT_ID, Integer.toString(accountId));
            if (null != fullName) {
                LogProperties.putProperty(LogProperties.Name.MAIL_FULL_NAME, fullName);
            }
            if (null != mailId) {
                LogProperties.putProperty(LogProperties.Name.MAIL_MAIL_ID, mailId);
            }
        }

        try {
            final JsonMessageHandler handler = new JsonMessageHandler(accountId, mailPath, mail, displayMode, embedded, session, usm, token, tokenTimeout, maxContentSize);
            handler.setExactLength(exactLength);
            if (null != optTimeZone) {
                handler.setTimeZone(optTimeZone);
            }
            final MailMessageParser parser = new MailMessageParser().addMimeFilter(mimeFilter);
            parser.parseMailMessage(mail, handler);
            if (null != warnings) {
                final List<OXException> list = parser.getWarnings();
                if (!list.isEmpty()) {
                    warnings.addAll(list);
                }
            }
            final JSONObject jsonObject = handler.getJSONObject();
            if (mail instanceof Delegatized) {
                final int undelegatedAccountId = ((Delegatized) mail).getUndelegatedAccountId();
                if (undelegatedAccountId >= 0) {
                    try {
                        jsonObject.put(FolderChildFields.FOLDER_ID, prepareFullname(undelegatedAccountId, fullName));
                    } catch (final JSONException e) {
                        throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                    }
                }
            }
            if (!mail.isDraft()) {
                return jsonObject;
            }
            /*
             * Ensure "msgref" is present in draft mail
             */
            final String key = MailJSONField.MSGREF.getKey();
            if (!jsonObject.has(key) && null != mailPath) {
                try {
                    jsonObject.put(key, mailPath.toString());
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
            return jsonObject;
        } catch (final OXException e) {
            final Throwable cause = e.getCause();
            if (null != cause && cause.getClass().getName().startsWith("MessageRemoved")) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(cause, mailId, mail.getFolder());
            }
            throw e;
        }
    }

    /**
     * Writes raw mail as a JSON object.
     *
     * @param accountId The account ID
     * @param mail The mail to write
     * @return The written JSON object or <code>null</code> if message's text body parts exceed max. size
     * @throws OXException If writing message fails
     */
    public static JSONObject writeRawMailMessage(final int accountId, final MailMessage mail) throws OXException {
        final MailPath mailPath;
        if (mail.getFolder() != null && mail.getMailId() != null) {
            mailPath = new MailPath(accountId, mail.getFolder(), mail.getMailId());
        } else if (mail.getMsgref() != null) {
            mailPath = mail.getMsgref();
        } else {
            mailPath = MailPath.NULL;
        }
        final RawJSONMessageHandler handler = new RawJSONMessageHandler(accountId, mailPath, mail);
        new MailMessageParser().parseMailMessage(mail, handler);
        return handler.getJSONObject();
    }

    public static interface MailFieldWriter {

        public void writeField(JSONValue jsonContainer, MailMessage mail, int level, boolean withKey, int accountId, int user, int cid, TimeZone optTimeZone) throws OXException;
    }

    private static final class HeaderFieldWriter implements MailFieldWriter {

        private final String headerName;

        HeaderFieldWriter(final String headerName) {
            super();
            this.headerName = headerName;
        }

        @Override
        public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
            final Object value = getHeaderValue(mail);
            if (withKey) {
                if (null != value) {
                    try {
                        jsonContainer.toObject().put(headerName, value);
                    } catch (final JSONException e) {
                        throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                    }
                }
            } else {
                jsonContainer.toArray().put(null == value ? JSONObject.NULL : value);
            }
        }

        private Object getHeaderValue(MailMessage mail) {
            final String[] headerValues = mail.getHeader(headerName);
            if (null == headerValues || 0 == headerValues.length) {
                return null;
            }
            int length = headerValues.length;
            if (1 == length) {
                return headerValues[0];
            }
            JSONArray ja = new JSONArray(length);
            for (String headerValue : headerValues) {
                ja.put(headerValue);
            }
            return ja;
        }

    }

    private static final EnumMap<MailListField, MailFieldWriter> WRITERS = new EnumMap<MailListField, MailFieldWriter>(MailListField.class);

    static {
        WRITERS.put(MailListField.ID, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(DataFields.ID, mail.getMailId());
                    } else {
                        jsonContainer.toArray().put(mail.getMailId());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.FOLDER_ID, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    int accId = accountId;
                    if (mail instanceof Delegatized) {
                        final int undelegatedAccountId = ((Delegatized) mail).getUndelegatedAccountId();
                        if (undelegatedAccountId >= 0) {
                            accId = undelegatedAccountId;
                        }
                    }
                    if (withKey) {
                        jsonContainer.toObject().put(FolderChildFields.FOLDER_ID, prepareFullname(accId, mail.getFolder()));
                    } else {
                        jsonContainer.toArray().put(prepareFullname(accId, mail.getFolder()));
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.ATTACHMENT, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.HAS_ATTACHMENTS.getKey(), mail.hasAttachment());
                    } else {
                        jsonContainer.toArray().put(mail.hasAttachment());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.FROM, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.FROM.getKey(), getAddressesAsArray(mail.getFrom()));
                    } else {
                        jsonContainer.toArray().put(getAddressesAsArray(mail.getFrom()));
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.TO, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.RECIPIENT_TO.getKey(), getAddressesAsArray(mail.getTo()));
                    } else {
                        jsonContainer.toArray().put(getAddressesAsArray(mail.getTo()));
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.CC, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.RECIPIENT_CC.getKey(), getAddressesAsArray(mail.getCc()));
                    } else {
                        jsonContainer.toArray().put(getAddressesAsArray(mail.getCc()));
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.BCC, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.RECIPIENT_BCC.getKey(), getAddressesAsArray(mail.getBcc()));
                    } else {
                        jsonContainer.toArray().put(getAddressesAsArray(mail.getBcc()));
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.SUBJECT, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    String subject = mail.getSubject();
                    if (withKey) {
                        if (subject != null) {
                            subject = decodeMultiEncodedHeader(subject);
                            jsonContainer.toObject().put(MailJSONField.SUBJECT.getKey(), subject.trim());
                        }
                    } else {
                        if (subject == null) {
                            jsonContainer.toArray().put(JSONObject.NULL);
                        } else {
                            subject = decodeMultiEncodedHeader(subject);
                            jsonContainer.toArray().put(subject.trim());
                        }
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.SIZE, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.SIZE.getKey(), mail.getSize());
                    } else {
                        jsonContainer.toArray().put(mail.getSize());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.SENT_DATE, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    TimeZone timeZone = optTimeZone;
                    if (null == timeZone) {
                        timeZone = TimeZoneUtils.getTimeZone(UserStorage.getInstance().getUser(user, cid).getTimeZone());
                    }
                    if (withKey) {
                        if (mail.containsSentDate() && mail.getSentDate() != null) {
                            jsonContainer.toObject().put(
                                MailJSONField.SENT_DATE.getKey(),
                                addUserTimezone(mail.getSentDate().getTime(), timeZone));
                        }
                    } else {
                        if (mail.containsSentDate() && mail.getSentDate() != null) {
                            jsonContainer.toArray().put(addUserTimezone(mail.getSentDate().getTime(), timeZone));
                        } else {
                            jsonContainer.toArray().put(JSONObject.NULL);
                        }
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.RECEIVED_DATE, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    TimeZone timeZone = optTimeZone;
                    if (null == timeZone) {
                        timeZone = TimeZoneUtils.getTimeZone(UserStorage.getInstance().getUser(user, cid).getTimeZone());
                    }
                    if (withKey) {
                        if (mail.containsReceivedDate() && mail.getReceivedDate() != null) {
                            jsonContainer.toObject().put(
                                MailJSONField.RECEIVED_DATE.getKey(),
                                addUserTimezone(mail.getReceivedDate().getTime(), timeZone));
                        }
                    } else {
                        if (mail.containsReceivedDate() && mail.getReceivedDate() != null) {
                            jsonContainer.toArray().put(addUserTimezone(mail.getReceivedDate().getTime(), timeZone));
                        } else {
                            jsonContainer.toArray().put(JSONObject.NULL);
                        }
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.FLAGS, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.FLAGS.getKey(), mail.getFlags());
                    } else {
                        jsonContainer.toArray().put(mail.getFlags());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.THREAD_LEVEL, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.THREAD_LEVEL.getKey(), mail.getThreadLevel());
                    } else {
                        jsonContainer.toArray().put(mail.getThreadLevel());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.DISPOSITION_NOTIFICATION_TO, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    final Object value;
                    if ((mail.containsPrevSeen() ? mail.isPrevSeen() : mail.isSeen())) {
                        value = JSONObject.NULL;
                    } else {
                        value =
                            mail.getDispositionNotification() == null ? JSONObject.NULL : mail.getDispositionNotification().toUnicodeString();
                    }
                    if (withKey) {
                        if (!JSONObject.NULL.equals(value)) {
                            jsonContainer.toObject().put(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(), value);
                        }
                    } else {
                        jsonContainer.toArray().put(value);
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.PRIORITY, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.PRIORITY.getKey(), mail.getPriority());
                    } else {
                        jsonContainer.toArray().put(mail.getPriority());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.MSG_REF, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        if (mail.containsMsgref()) {
                            jsonContainer.toObject().put(MailJSONField.MSGREF.getKey(), mail.getMsgref());
                        }
                    } else {
                        jsonContainer.toArray().put(mail.containsMsgref() ? mail.getMsgref() : JSONObject.NULL);
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.COLOR_LABEL, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    final int colorLabel;
                    if (MailProperties.getInstance().isUserFlagsEnabled() && mail.containsColorLabel()) {
                        colorLabel = mail.getColorLabel();
                    } else {
                        colorLabel = 0;
                    }
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.COLOR_LABEL.getKey(), colorLabel);
                    } else {
                        jsonContainer.toArray().put(colorLabel);
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.TOTAL, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.TOTAL.getKey(), JSONObject.NULL);
                    } else {
                        jsonContainer.toArray().put(JSONObject.NULL);
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.NEW, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.NEW.getKey(), JSONObject.NULL);
                    } else {
                        jsonContainer.toArray().put(JSONObject.NULL);
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.UNREAD, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.UNREAD.getKey(), mail.getUnreadMessages());
                    } else {
                        jsonContainer.toArray().put(mail.getUnreadMessages());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.DELETED, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        jsonContainer.toObject().put(MailJSONField.DELETED.getKey(), JSONObject.NULL);
                    } else {
                        jsonContainer.toArray().put(JSONObject.NULL);
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.ACCOUNT_NAME, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        final JSONObject jsonObject = (JSONObject) jsonContainer;
                        jsonObject.put(MailJSONField.ACCOUNT_NAME.getKey(), mail.getAccountName());
                        jsonObject.put(MailJSONField.ACCOUNT_ID.getKey(), mail.getAccountId());
                    } else {
                        jsonContainer.toArray().put(mail.getAccountName());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS.put(MailListField.ACCOUNT_ID, new MailFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
                try {
                    if (withKey) {
                        final JSONObject jsonObject = (JSONObject) jsonContainer;
                        jsonObject.put(MailJSONField.ACCOUNT_NAME.getKey(), mail.getAccountName());
                        jsonObject.put(MailJSONField.ACCOUNT_ID.getKey(), mail.getAccountId());
                    } else {
                        jsonContainer.toArray().put(mail.getAccountId());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
    }

    private static final MailFieldWriter UNKNOWN = new MailFieldWriter() {

        @Override
        public void writeField(final JSONValue jsonContainer, final MailMessage mail, final int level, final boolean withKey, final int accountId, final int user, final int cid, final TimeZone optTimeZone) throws OXException {
            try {
                if (withKey) {
                    jsonContainer.toObject().put("Unknown column", JSONObject.NULL);
                } else {
                    jsonContainer.toArray().put(JSONObject.NULL);
                }
            } catch (final JSONException e) {
                throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
            }
        }
    };

    /**
     * Generates appropriate field writers for given mail fields
     *
     * @param fields The mail fields to write
     * @return Appropriate field writers as an array of {@link MailFieldWriter}
     */
    public static MailFieldWriter[] getMailFieldWriters(MailListField[] fields) {
        MailFieldWriter[] retval = new MailFieldWriter[fields.length];
        for (int i = 0; i < fields.length; i++) {
            MailFieldWriter mfw = WRITERS.get(fields[i]);
            retval[i] = (mfw == null) ? UNKNOWN : mfw;
        }
        return retval;
    }

    /**
     * Generates the appropriate field writer for given mail field
     *
     * @param field The mail field to write
     * @return Appropriate field writer
     */
    public static MailFieldWriter getMailFieldWriter(MailListField field) {
        MailFieldWriter mfw = WRITERS.get(field);
        return null == mfw ? UNKNOWN : mfw;
    }

    /**
     * Gets writers for specified header names.
     *
     * @param headers The header names
     * @return The writers for specified header names
     */
    public static MailFieldWriter[] getHeaderFieldWriters(String[] headers) {
        if (null == headers) {
            return new MailFieldWriter[0];
        }
        MailFieldWriter[] retval = new MailFieldWriter[headers.length];
        for (int i = 0; i < headers.length; i++) {
            retval[i] = new HeaderFieldWriter(headers[i]);
        }
        return retval;
    }

    /**
     * Gets the writer for specified header name.
     *
     * @param header The header name
     * @return The writer for specified header name
     */
    public static MailFieldWriter getHeaderFieldWriter(String header) {
        return null == header ? null : new HeaderFieldWriter(header);
    }

    /**
     * Adds the user time zone offset to given date time
     *
     * @param time The date time
     * @param timeZone The time zone
     * @return The time with added time zone offset
     */
    public static long addUserTimezone(final long time, final TimeZone timeZone) {
        return (time + timeZone.getOffset(time));
    }

    private static final JSONArray EMPTY_JSON_ARR = new JSONArray(0);

    /**
     * Convert an array of <code>InternetAddress</code> instances into a JSON-Array conforming to:
     *
     * <pre>
     * [[&quot;The Personal&quot;, &quot;someone@somewhere.com&quot;], ...]
     * </pre>
     */
    public static JSONArray getAddressesAsArray(final InternetAddress[] addrs) {
        if (addrs == null || addrs.length == 0) {
            return EMPTY_JSON_ARR;
        }
        final JSONArray jsonArr = new JSONArray(addrs.length);
        for (final InternetAddress address : addrs) {
            jsonArr.put(getAddressAsArray(address));
        }
        return jsonArr;
    }

    /**
     * Convert an <code>InternetAddress</code> instance into a JSON-Array conforming to: ["The Personal", "someone@somewhere.com"]
     */
    private static JSONArray getAddressAsArray(final InternetAddress addr) {
        final JSONArray retval = new JSONArray(2);
        // Personal
        final String personal = addr.getPersonal();
        retval.put(personal == null || personal.length() == 0 ? JSONObject.NULL : preparePersonal(personal));
        // Address
        String address = addr.getAddress();
        retval.put(address == null || address.length() == 0 ? JSONObject.NULL : MimeMessageUtility.prepareAddress(address));

        return retval;
    }

    // private static final Pattern PATTERN_QUOTE = Pattern.compile("[.,:;<>\"]");

    private static String preparePersonal(final String personal) {
        return MimeMessageUtility.quotePhrase(personal, false);
    }
}
