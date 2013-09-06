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

package com.openexchange.mail.json.parser;

import static com.openexchange.mail.mime.filler.MimeMessageFiller.isCustomOrReplyHeader;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.quotePersonal;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.shouldRetry;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadFileImpl;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Charsets;
import com.openexchange.java.StringAllocator;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.DataMailPart;
import com.openexchange.mail.dataobjects.compose.InfostoreDocumentMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.dataobjects.compose.UploadFileMailPart;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.ManagedMimeMessage;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.ImageMatcher;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.MultipleMailPartHandler;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link MessageParser} - Parses instances of {@link JSONObject} to instances of {@link MailMessage}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - {@link #parseBasics(JSONObject, MailMessage, TimeZone)}
 */
public final class MessageParser {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessageParser.class));

    /**
     * No instantiation
     */
    private MessageParser() {
        super();
    }

    private static final String STR_TRUE = "true";

    private static final String JSON_ARGS = "args";

    private static final String JSON_IDENTIFIER = "identifier";

    /**
     * Completely parses given instance of {@link JSONObject} and given instance of {@link UploadEvent} to a corresponding
     * {@link ComposedMailMessage} object dedicated for being saved as a draft message. Moreover the user's quota limitations are
     * considered.
     *
     * @param jsonObj The JSON object
     * @param uploadEvent The upload event containing the uploaded files to attach
     * @param session The session
     * @param accountId The account ID
     * @param warnings
     * @return A corresponding instance of {@link ComposedMailMessage}
     * @throws OXException If parsing fails
     */
    public static ComposedMailMessage parse4Draft(final JSONObject jsonObj, final UploadEvent uploadEvent, final Session session, final int accountId, final List<OXException> warnings) throws OXException {
        return parse(jsonObj, uploadEvent, session, accountId, null, null, false, warnings)[0];
    }

    /**
     * Completely parses given instance of {@link JSONObject} and given instance of {@link UploadEvent} to corresponding
     * {@link ComposedMailMessage} objects dedicated for being sent. Moreover the user's quota limitations are considered.
     *
     * @param jsonObj The JSON object
     * @param uploadEvent The upload event containing the uploaded files to attach
     * @param session The session
     * @param accountId The account ID
     * @param protocol The server's protocol
     * @param warnings
     * @param hostname The server's host name
     * @return The corresponding instances of {@link ComposedMailMessage}
     * @throws OXException If parsing fails
     */
    public static ComposedMailMessage[] parse4Transport(final JSONObject jsonObj, final UploadEvent uploadEvent, final Session session, final int accountId, final String protocol, final String hostName, final List<OXException> warnings) throws OXException {
        return parse(jsonObj, uploadEvent, session, accountId, protocol, hostName, true, warnings);
    }

    /**
     * Completely parses given instance of {@link JSONObject} and given instance of {@link UploadEvent} to corresponding
     * {@link ComposedMailMessage} objects. Moreover the user's quota limitations are considered.
     *
     * @param jsonObj The JSON object
     * @param uploadEvent The upload event containing the uploaded files to attach
     * @param session The session
     * @param accountId The account ID
     * @param protocol The server's protocol
     * @param hostname The server's host name
     * @param prepare4Transport <code>true</code> to parse with the intention to transport returned mail later on; otherwise
     *            <code>false</code>
     * @param warnings
     * @return The corresponding instances of {@link ComposedMailMessage}
     * @throws OXException If parsing fails
     */
    private static ComposedMailMessage[] parse(final JSONObject jsonObj, final UploadEvent uploadEvent, final Session session, final int accountId, final String protocol, final String hostName, final boolean prepare4Transport, final List<OXException> warnings) throws OXException {
        try {
            final TransportProvider provider = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
            final Context ctx = ContextStorage.getStorageContext(session.getContextId());
            final ComposedMailMessage composedMail = provider.getNewComposedMailMessage(session, ctx);
            composedMail.setAccountId(accountId);
            /*
             * Select appropriate handler
             */
            final IAttachmentHandler attachmentHandler;
            if (prepare4Transport && TransportProperties.getInstance().isPublishOnExceededQuota() && (!TransportProperties.getInstance().isPublishPrimaryAccountOnly() || (MailAccount.DEFAULT_ID == accountId))) {
                attachmentHandler = new PublishAttachmentHandler(session, provider, protocol, hostName);
            } else {
                attachmentHandler = new AbortAttachmentHandler(session);
            }
            /*
             * Parse transport message plus its text body
             */
            parse(composedMail, jsonObj, session, accountId, provider, attachmentHandler, ctx, prepare4Transport);
            if (null != uploadEvent) {
                /*
                 * Uploaded files
                 */
                for (final UploadFile uf : uploadEvent.getUploadFiles()) {
                    if (uf != null) {
                        final UploadFileMailPart mailPart = provider.getNewFilePart(uf);
                        attachmentHandler.addAttachment(mailPart);
                    }
                }
            }
            /*
             * Attached data sources
             */
            if (jsonObj.hasAndNotNull(MailJSONField.DATASOURCES.getKey())) {
                final JSONArray datasourceArray = jsonObj.getJSONArray(MailJSONField.DATASOURCES.getKey());
                final int length = datasourceArray.length();
                if (length > 0) {
                    final ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class);
                    if (conversionService == null) {
                        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConversionService.class.getName());
                    }
                    final Set<Class<?>> types = new HashSet<Class<?>>(4);
                    for (int i = 0; i < length; i++) {
                        final JSONObject dataSourceObject = datasourceArray.getJSONObject(i);
                        if (!dataSourceObject.hasAndNotNull(JSON_IDENTIFIER)) {
                            throw MailExceptionCode.MISSING_PARAM.create(JSON_IDENTIFIER);
                        }
                        final DataSource dataSource = conversionService.getDataSource(dataSourceObject.getString(JSON_IDENTIFIER));
                        if (dataSource == null) {
                            throw DataExceptionCodes.UNKNOWN_DATA_SOURCE.create(dataSourceObject.getString(JSON_IDENTIFIER));
                        }
                        if (!types.isEmpty()) {
                            types.clear();
                        }
                        types.addAll(Arrays.asList(dataSource.getTypes()));
                        final Data<?> data;
                        if (types.contains(InputStream.class)) {
                            data = dataSource.getData(InputStream.class, parseDataSourceArguments(dataSourceObject), session);
                        } else if (types.contains(byte[].class)) {
                            data = dataSource.getData(byte[].class, parseDataSourceArguments(dataSourceObject), session);
                        } else {
                            throw MailExceptionCode.UNSUPPORTED_DATASOURCE.create();
                        }
                        final DataMailPart dataMailPart =
                            provider.getNewDataPart(data.getData(), data.getDataProperties().toMap(), session);
                        attachmentHandler.addAttachment(dataMailPart);
                    }
                }
            }
            /*
             * Attached infostore document IDs
             */
            if (jsonObj.hasAndNotNull(MailJSONField.INFOSTORE_IDS.getKey())) {
                final JSONArray ja = jsonObj.getJSONArray(MailJSONField.INFOSTORE_IDS.getKey());
                final int length = ja.length();
                for (int i = 0; i < length; i++) {
                    final InfostoreDocumentMailPart part = provider.getNewDocumentPart(ja.getString(i), session);
                    attachmentHandler.addAttachment(part);
                }
            }
            /*
             * Fill composed mail
             */
            final ComposedMailMessage[] ret = attachmentHandler.generateComposedMails(composedMail, warnings);
            for (final ComposedMailMessage mail : ret) {
                if (!mail.containsAccountId()) {
                    mail.setAccountId(accountId);
                }
            }
            return ret;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static DataArguments parseDataSourceArguments(final JSONObject json) throws JSONException {
        if (!json.hasAndNotNull(JSON_ARGS)) {
            return DataArguments.EMPTY_ARGS;
        }
        final Object args = json.get(JSON_ARGS);
        if (args instanceof JSONArray) {
            /*
             * Handle as JSON array
             */
            final JSONArray jsonArray = (JSONArray) args;
            final int len = jsonArray.length();
            final DataArguments dataArguments = new DataArguments(len);
            for (int i = 0; i < len; i++) {
                final JSONObject elem = jsonArray.getJSONObject(i);
                if (elem.length() == 1) {
                    final String key = elem.keys().next();
                    dataArguments.put(key, elem.getString(key));
                } else {
                    LOG.warn("Corrupt data argument in JSON object: " + elem.toString());
                }
            }
            return dataArguments;
        }
        /*
         * Expect JSON object
         */
        final JSONObject argsObject = (JSONObject) args;
        final int len = argsObject.length();
        final DataArguments dataArguments = new DataArguments(len);
        for (final Entry<String, Object> entry : argsObject.entrySet()) {
            dataArguments.put(entry.getKey(), entry.getValue().toString());
        }
        return dataArguments;
    }

    private static void parse(final ComposedMailMessage transportMail, final JSONObject jsonObj, final Session session, final int accountId, final TransportProvider provider, final IAttachmentHandler attachmentHandler, final Context ctx, final boolean prepare4Transport) throws OXException {
        parse(
            jsonObj,
            transportMail,
            TimeZoneUtils.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone()),
            provider,
            session,
            accountId,
            attachmentHandler,
            prepare4Transport);
    }

    /**
     * Parses given instance of {@link JSONObject} to given instance of {@link MailMessage}. Moreover the user's quota limitations are
     * considered.
     *
     * @param jsonObj The JSON object (source)
     * @param mail The mail(target), which should be empty
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If parsing fails
     */
    public static void parse(final JSONObject jsonObj, final MailMessage mail, final Session session, final int accountId) throws OXException {
        parse(
            jsonObj,
            mail,
            TimeZoneUtils.getTimeZone(UserStorage.getStorageUser(
                session.getUserId(),
                ContextStorage.getStorageContext(session.getContextId())).getTimeZone()),
            session,
            accountId);
    }

    /**
     * Parses given instance of {@link JSONObject} to given instance of {@link MailMessage}. Moreover the user's quota limitations are
     * considered.
     *
     * @param jsonObj The JSON object (source)
     * @param mail The mail(target), which should be empty
     * @param timeZone The user time zone
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If parsing fails
     */
    public static void parse(final JSONObject jsonObj, final MailMessage mail, final TimeZone timeZone, final Session session, final int accountId) throws OXException {
        parse(
            jsonObj,
            mail,
            timeZone,
            TransportProviderRegistry.getTransportProviderBySession(session, accountId),
            session,
            accountId,
            new AbortAttachmentHandler(session),
            false);
    }

    private static void parse(final JSONObject jsonObj, final MailMessage mail, final TimeZone timeZone, final TransportProvider provider, final Session session, final int accountId, final IAttachmentHandler attachmentHandler, final boolean prepare4Transport) throws OXException {
        try {
            parseBasics(jsonObj, mail, timeZone, prepare4Transport);
            /*
             * Prepare msgref
             */
            prepareMsgRef(session, mail);
            /*
             * Parse attachments
             */
            if (mail instanceof ComposedMailMessage) {
                final ComposedMailMessage transportMail = (ComposedMailMessage) mail;
                if (jsonObj.hasAndNotNull(MailJSONField.ATTACHMENTS.getKey())) {
                    final JSONArray attachmentArray = jsonObj.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
                    /*
                     * Parse body text
                     */
                    final JSONObject tmp = attachmentArray.getJSONObject(0);
                    final String sContent = tmp.getString(MailJSONField.CONTENT.getKey());
                    final TextBodyMailPart part = provider.getNewTextBodyPart(sContent);
                    final String contentType = parseContentType(tmp.getString(MailJSONField.CONTENT_TYPE.getKey()));
                    part.setContentType(contentType);
                    if (contentType.startsWith("text/plain") && tmp.hasAndNotNull("raw") && tmp.getBoolean("raw")) {
                        part.setPlainText(sContent);
                    }
                    transportMail.setContentType(part.getContentType());
                    // Add text part
                    attachmentHandler.setTextPart(part);
                    /*
                     * Parse referenced parts
                     */
                    final int len = attachmentArray.length();
                    if (len > 1) {
                        final Set<String> contentIds = extractContentIds(sContent);
                        parseReferencedParts(provider, session, accountId, transportMail.getMsgref(), attachmentHandler, attachmentArray, contentIds, prepare4Transport);
                    }
                } else {
                    final TextBodyMailPart part = provider.getNewTextBodyPart("");
                    part.setContentType(MimeTypes.MIME_DEFAULT);
                    transportMail.setContentType(part.getContentType());
                    // Add text part
                    attachmentHandler.setTextPart(part);
                }
            }
            /*
             * TODO: Parse nested messages. Currently not used
             */
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static final Pattern PATTERN_ID_ATTRIBUTE = Pattern.compile("id=\"((?:\\\\\\\"|[^\"])+?)\"");

    private static Set<String> extractContentIds(final String htmlContent) {
        final ImageMatcher m = ImageMatcher.matcher(htmlContent);
        if (!m.find()) {
            return Collections.emptySet();
        }
        final Set<String> set = new HashSet<String>(4);
        do {
            final String imageTag = m.group();
            final Matcher tmp = PATTERN_ID_ATTRIBUTE.matcher(imageTag);
            if (tmp.find()) {
                set.add(tmp.group(1));
            }
        } while (m.find());
        return set;
    }

    /**
     * Takes a mail as jsonObj and extracts the values into a given MailMessage object. Handles all basic values that do not need
     * information about the session, like attachments.
     *
     * @param jsonObj
     * @param mail
     * @param timeZone
     * @throws JSONException
     * @throws AddressException
     * @throws OXException
     */
    public static void parseBasics(final JSONObject jsonObj, final MailMessage mail, final TimeZone timeZone) throws JSONException, AddressException, OXException {
        parseBasics(jsonObj, mail, timeZone, false);
    }

    private static void parseBasics(final JSONObject jsonObj, final MailMessage mail, final TimeZone timeZone, final boolean prepare4Transport) throws JSONException, AddressException, OXException {
        /*
         * System flags
         */
        if (jsonObj.hasAndNotNull(MailJSONField.FLAGS.getKey())) {
            mail.setFlags(jsonObj.getInt(MailJSONField.FLAGS.getKey()));
        }
        /*
         * Thread level
         */
        if (jsonObj.hasAndNotNull(MailJSONField.THREAD_LEVEL.getKey())) {
            mail.setThreadLevel(jsonObj.getInt(MailJSONField.THREAD_LEVEL.getKey()));
        }
        /*
         * User flags
         */
        if (jsonObj.hasAndNotNull(MailJSONField.USER.getKey())) {
            final JSONArray arr = jsonObj.getJSONArray(MailJSONField.USER.getKey());
            final int length = arr.length();
            final List<String> l = new ArrayList<String>(length);
            for (int i = 0; i < length; i++) {
                l.add(arr.getString(i));
            }
            mail.addUserFlags(l.toArray(new String[l.size()]));
        }
        /*
         * Parse headers
         */
        if (jsonObj.hasAndNotNull(MailJSONField.HEADERS.getKey())) {
            final JSONObject obj = jsonObj.getJSONObject(MailJSONField.HEADERS.getKey());
            final int size = obj.length();
            final HeaderCollection headers = new HeaderCollection(size);
            final Iterator<String> iter = obj.keys();
            for (int i = 0; i < size; i++) {
                final String key = iter.next();
                if (isCustomOrReplyHeader(key) && !key.equalsIgnoreCase("x-original-headers")) {
                    headers.setHeader(key, obj.getString(key));
                }
            }
            mail.addHeaders(headers);
        }
        /*
         * From Only mandatory if non-draft message
         */
        final String fromKey = MailJSONField.FROM.getKey();
        if (jsonObj.hasAndNotNull(fromKey)) {
            try {
                String value = jsonObj.getString(fromKey);
                final int endPos;
                if ('[' == value.charAt(0) && (endPos = value.indexOf(']', 1)) < value.length()) {
                    value = new com.openexchange.java.StringAllocator(32).append("\"[").append(value.substring(1, endPos)).append("]\"").append(value.substring(endPos+1)).toString();
                }
                mail.addFrom(parseAddressList(value, true, true));
            } catch (final AddressException e) {
                mail.addFrom(parseAddressKey(fromKey, jsonObj, prepare4Transport));
            }
        } else if (prepare4Transport) {
            throw MailExceptionCode.MISSING_FIELD.create(fromKey);
        }
        /*
         * To Only mandatory if non-draft message
         */
        mail.addTo(parseAddressKey(MailJSONField.RECIPIENT_TO.getKey(), jsonObj, prepare4Transport));
        /*
         * Cc
         */
        mail.addCc(parseAddressKey(MailJSONField.RECIPIENT_CC.getKey(), jsonObj, prepare4Transport));
        /*
         * Bcc
         */
        mail.addBcc(parseAddressKey(MailJSONField.RECIPIENT_BCC.getKey(), jsonObj, prepare4Transport));
        /*
         * Optional Reply-To
         */
        {
            final InternetAddress[] addrs = parseAddressKey("reply_to", jsonObj, false);
            if (null != addrs && addrs.length > 0) {
                mail.setHeader("Reply-To", addrs[0].toString());
            }
        }
        /*
         * Disposition notification
         */
        if (jsonObj.hasAndNotNull(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey())) {
            /*
             * Ok, disposition-notification-to is set. Check if its value is a valid email address
             */
            final String dispVal = jsonObj.getString(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey());
            if (STR_TRUE.equalsIgnoreCase(dispVal)) {
                /*
                 * Boolean value "true"
                 */
                mail.setDispositionNotification(mail.getFrom().length > 0 ? mail.getFrom()[0] : null);
            } else {
                final InternetAddress ia = getEmailAddress(dispVal);
                if (ia == null) {
                    /*
                     * Any other value
                     */
                    mail.setDispositionNotification(null);
                } else {
                    /*
                     * Valid email address
                     */
                    mail.setDispositionNotification(ia);
                }
            }
        }
        /*
         * Priority
         */
        if (jsonObj.hasAndNotNull(MailJSONField.PRIORITY.getKey())) {
            mail.setPriority(jsonObj.getInt(MailJSONField.PRIORITY.getKey()));
        }
        /*
         * Color Label
         */
        if (jsonObj.hasAndNotNull(MailJSONField.COLOR_LABEL.getKey())) {
            mail.setColorLabel(jsonObj.getInt(MailJSONField.COLOR_LABEL.getKey()));
        }
        /*
         * VCard
         */
        if (jsonObj.hasAndNotNull(MailJSONField.VCARD.getKey())) {
            mail.setAppendVCard((jsonObj.getInt(MailJSONField.VCARD.getKey()) > 0));
        }
        /*
         * Msg Ref
         */
        if (jsonObj.hasAndNotNull(MailJSONField.MSGREF.getKey())) {
            mail.setMsgref(new MailPath(jsonObj.getString(MailJSONField.MSGREF.getKey())));
        }
        /*
         * Subject, etc.
         */
        if (jsonObj.hasAndNotNull(MailJSONField.SUBJECT.getKey())) {
            mail.setSubject(jsonObj.getString(MailJSONField.SUBJECT.getKey()));
        }
        /*
         * Size
         */
        if (jsonObj.hasAndNotNull(MailJSONField.SIZE.getKey())) {
            mail.setSize(jsonObj.getInt(MailJSONField.SIZE.getKey()));
        }
        /*
         * Sent & received date
         */
        if (jsonObj.hasAndNotNull(MailJSONField.SENT_DATE.getKey())) {
            final Date date = new Date(jsonObj.getLong(MailJSONField.SENT_DATE.getKey()));
            final int offset = timeZone.getOffset(date.getTime());
            mail.setSentDate(new Date(jsonObj.getLong(MailJSONField.SENT_DATE.getKey()) - offset));
        }
        if (jsonObj.hasAndNotNull(MailJSONField.RECEIVED_DATE.getKey())) {
            final Date date = new Date(jsonObj.getLong(MailJSONField.RECEIVED_DATE.getKey()));
            final int offset = timeZone.getOffset(date.getTime());
            mail.setReceivedDate(new Date(jsonObj.getLong(MailJSONField.RECEIVED_DATE.getKey()) - offset));
        }
        /*
         * Drop special "x-original-headers" header
         */
        mail.removeHeader("x-original-headers");
    }

    private static final String ROOT = "0";

    private static final String FILE_PREFIX = "file://";

    private static void parseReferencedParts(final TransportProvider provider, final Session session, final int accountId, final MailPath transportMailMsgref, final IAttachmentHandler attachmentHandler, final JSONArray attachmentArray, final Set<String> contentIds, final boolean prepare4Transport) throws OXException, JSONException {
        final int len = attachmentArray.length();
        /*
         * Group referenced parts by referenced mails' paths
         */
        final Map<String, ReferencedMailPart> groupedReferencedParts =
            groupReferencedParts(provider, session, transportMailMsgref, attachmentArray, contentIds, prepare4Transport);
        /*
         * Iterate attachments array
         */
        MailAccess<?, ?> access = null;
        try {
            ManagedFileManagement management = null;
            NextAttachment: for (int i = 1; i < len; i++) {
                final JSONObject attachment = attachmentArray.getJSONObject(i);
                final String seqId =
                    attachment.hasAndNotNull(MailListField.ID.getKey()) ? attachment.getString(MailListField.ID.getKey()) : null;
                if (null == seqId && attachment.hasAndNotNull(MailJSONField.CONTENT.getKey())) {
                    /*
                     * A direct attachment, as data part
                     */
                    final String contentType = parseContentType(attachment.getString(MailJSONField.CONTENT_TYPE.getKey()));
                    final String charsetName = "UTF-8";
                    final byte[] content;
                    try {
                        /*
                         * UI delivers HTML content in any case. Generate well-formed HTML for further processing dependent on given content
                         * type.
                         */
                        final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                        if (MimeTypes.MIME_TEXT_PLAIN.equals(contentType)) {
                            content = htmlService.html2text(attachment.getString(MailJSONField.CONTENT.getKey()), true).getBytes(charsetName);
                        } else {
                            final String conformHTML =
                                htmlService.getConformHTML(attachment.getString(MailJSONField.CONTENT.getKey()), "ISO-8859-1");
                            content = conformHTML.getBytes(charsetName);
                        }

                    } catch (final UnsupportedEncodingException e) {
                        throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
                    }
                    /*
                     * As data object
                     */
                    final DataProperties properties = new DataProperties();
                    properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contentType);
                    properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(content.length));
                    properties.put(DataProperties.PROPERTY_CHARSET, charsetName);
                    final Data<byte[]> data = new SimpleData<byte[]>(content, properties);
                    final DataMailPart dataMailPart = provider.getNewDataPart(data.getData(), data.getDataProperties().toMap(), session);
                    attachmentHandler.addAttachment(dataMailPart);
                } else if (null != seqId && seqId.startsWith(FILE_PREFIX, 0)) {
                    /*
                     * A file reference
                     */
                    if (null == management) {
                        management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
                    }
                    processReferencedUploadFile(provider, management, seqId, attachmentHandler);
                } else {
                    /*
                     * Prefer MSGREF from attachment if present, otherwise get MSGREF from superior mail
                     */
                    MailPath msgref;
                    final boolean isMail;
                    if (attachment.hasAndNotNull(MailJSONField.MSGREF.getKey())) {
                        msgref = new MailPath(attachment.get(MailJSONField.MSGREF.getKey()).toString());
                        isMail = true;
                    } else {
                        msgref = transportMailMsgref;
                        isMail = false;
                    }
                    if (null == msgref) {
                        /*
                         * Huh...? Not possible to load referenced parts without a referenced mail
                         */
                        continue NextAttachment;
                    }
                    msgref = prepareMsgRef(session, msgref);
                    /*
                     * Decide how to retrieve part
                     */
                    final ReferencedMailPart referencedMailPart;
                    if (isMail || null == seqId || ROOT.equals(seqId)) {
                        /*
                         * The mail itself
                         */
                        if (null == access) {
                            access = MailAccess.getInstance(session, msgref.getAccountId());
                            access.connect();
                        }
                        MailMessage referencedMail =
                            access.getMessageStorage().getMessage(msgref.getFolder(), msgref.getMailID(), false);
                        if (null == referencedMail) {
                            throw MailExceptionCode.REFERENCED_MAIL_NOT_FOUND.create(msgref.getMailID(), msgref.getFolder());
                        }
                        referencedMail.setAccountId(access.getAccountId());
                        referencedMail = ManagedMimeMessage.clone(referencedMail);
                        referencedMailPart = provider.getNewReferencedMail(referencedMail, session);
                    } else {
                        ReferencedMailPart tmp = groupedReferencedParts.get(seqId);
                        if (null != tmp && tmp.containsContentId()) {
                            final String contentId = tmp.getContentId();
                            if (null != contentId && contentIds.contains('<' == contentId.charAt(0) ? contentId.substring(1, contentId.length()-1) : contentId)) {
                                tmp = null;
                            }
                        }
                        referencedMailPart = tmp;
                    }
                    if (null != referencedMailPart) {
                        referencedMailPart.setMsgref(msgref);
                        attachmentHandler.addAttachment(referencedMailPart);
                    }
                }
            }
        } finally {
            if (null != access) {
                access.close(true);
            }
        }
    }

    private static Map<String, ReferencedMailPart> groupReferencedParts(final TransportProvider provider, final Session session, final MailPath parentMsgRef, final JSONArray attachmentArray, final Set<String> contentIds, final boolean prepare4Transport) throws OXException, JSONException {
        if (null == parentMsgRef) {
            return Collections.emptyMap();
        }
        final int len = attachmentArray.length();
        final Map<String, String> groupedSeqIDs = new HashMap<String, String>(len);
        NextAttachment: for (int i = 1; i < len; i++) {
            final JSONObject attachment = attachmentArray.getJSONObject(i);
            final String seqId =
                attachment.hasAndNotNull(MailListField.ID.getKey()) ? attachment.getString(MailListField.ID.getKey()) : null;
            if (seqId == null || seqId.startsWith(FILE_PREFIX, 0)) {
                /*
                 * A file reference
                 */
                continue NextAttachment;
            }
            /*
             * If MSGREF is defined in attachment itself, the MSGREF's mail is meant to be attached and not a nested attachment
             */
            if (!attachment.hasAndNotNull(MailJSONField.MSGREF.getKey())) {
                final Object cid = attachment.opt(MailJSONField.CID.getKey());
                groupedSeqIDs.put(seqId, null == cid ? "" : cid.toString());
            }
        }
        /*
         * Now load them by message reference
         */
        if (groupedSeqIDs.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, ReferencedMailPart> retval = null;
        MailAccess<?, ?> access = null;
        try {
            access = MailAccess.getInstance(session, parentMsgRef.getAccountId());
            access.connect();
            retval = new HashMap<String, ReferencedMailPart>(len);
            handleMultipleRefs(provider, session, parentMsgRef, contentIds, prepare4Transport, groupedSeqIDs, retval, access);
        } catch (final OXException oe) {
            if (null == access || !shouldRetry(oe)) {
                throw oe;
            }
            access = MailAccess.reconnect(access);
            retval = new HashMap<String, ReferencedMailPart>(len);
            handleMultipleRefs(provider, session, parentMsgRef, contentIds, prepare4Transport, groupedSeqIDs, retval, access);
        } finally {
            if (null != access) {
                access.close(true);
            }
        }
        return retval;
    }

    private static void handleMultipleRefs(final TransportProvider provider, final Session session, final MailPath parentMsgRef, final Set<String> contentIds, final boolean prepare4Transport, final Map<String, String> groupedSeqIDs, final Map<String, ReferencedMailPart> retval, final MailAccess<?, ?> access) throws OXException {
        final MailPath pMsgRef = prepareMsgref(parentMsgRef);
        MailMessage referencedMail = access.getMessageStorage().getMessage(pMsgRef.getFolder(), pMsgRef.getMailID(), false);
        if (null == referencedMail) {
            throw MailExceptionCode.REFERENCED_MAIL_NOT_FOUND.create(pMsgRef.getMailID(), pMsgRef.getFolder());
        }
        referencedMail.setAccountId(access.getAccountId());
        referencedMail = ManagedMimeMessage.clone(referencedMail);
        // Get attachments out of referenced mail
        final Set<String> remaining = new HashSet<String>(groupedSeqIDs.keySet());
        final MultipleMailPartHandler handler = new MultipleMailPartHandler(groupedSeqIDs.keySet(), false);
        new MailMessageParser().parseMailMessage(referencedMail, handler);
        for (final Map.Entry<String, MailPart> e : handler.getMailParts().entrySet()) {
            final String seqId = e.getKey();
            retval.put(seqId, provider.getNewReferencedPart(e.getValue(), session));
            remaining.remove(seqId);
        }
        if (prepare4Transport && !remaining.isEmpty()) {
            for (final String seqId : remaining) {
                if (!contentIds.contains(seqId)) {
                    throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(seqId, Long.valueOf(referencedMail.getMailId()), referencedMail.getFolder());
                }
            }
        }
    }

    private static MailPath prepareMsgref(final MailPath msgref) throws OXException {
        final String mailID = msgref.getMailID();
        if (mailID.startsWith("%64%65%66%61")) {
            // Referenced by Unified Mail; e.g. "%64%65%66%61ult0%2FIN%42OX%2F%44r%61%66ts%2F2255"
            return new MailPath(decodeQP(mailID));
        }
        return msgref;
    }

    private static final Pattern DECODE_PATTERN = Pattern.compile("%");
    private static String decodeQP(final String string) {
        try {
            return new String(QuotedPrintableCodec.decodeQuotedPrintable(Charsets.toAsciiBytes(DECODE_PATTERN.matcher(string).replaceAll("="))), com.openexchange.java.Charsets.UTF_8);
        } catch (final DecoderException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void processReferencedUploadFile(final TransportProvider provider, final ManagedFileManagement management, final String seqId, final IAttachmentHandler attachmentHandler) throws OXException {
        /*
         * A file reference
         */
        final ManagedFile managedFile;
        try {
            managedFile = management.getByID(seqId.substring(FILE_PREFIX.length()));
        } catch (final OXException e) {
            LOG.error("No temp file found for ID: " + seqId.substring(FILE_PREFIX.length()), e);
            return;
        }
        // Create wrapping upload file
        final UploadFile wrapper = new UploadFileImpl();
        wrapper.setContentType(managedFile.getContentType());
        wrapper.setFileName(managedFile.getFileName());
        wrapper.setSize(managedFile.getSize());
        wrapper.setTmpFile(managedFile.getFile());
        // Add to quota checker
        attachmentHandler.addAttachment(provider.getNewFilePart(wrapper));
    }

    private static final String CT_ALTERNATIVE = "alternative";

    private static String parseContentType(final String ctStrArg) {
        final String ctStr = toLowerCase(ctStrArg).trim();
        if (ctStr.indexOf(CT_ALTERNATIVE) != -1) {
            return MimeTypes.MIME_MULTIPART_ALTERNATIVE;
        }
        if (MimeTypes.MIME_TEXT_PLAIN.equals(ctStr) || "text".equals(ctStr)) {
            return MimeTypes.MIME_TEXT_PLAIN;
        }
        return MimeTypes.MIME_TEXT_HTML;
    }

    /**
     * Parses "From" field out of passed JSON object.
     *
     * @param jo The JSON object
     * @return The parsed "From" address
     * @throws AddressException If parsing the address fails
     * @throws JSONException If a JSON error occurred
     */
    public static InternetAddress[] getFromField(final JSONObject jo) throws AddressException, JSONException {
        return parseAddressKey(MailJSONField.FROM.getKey(), jo);
    }

    /**
     * Parses address field out of passed JSON object.
     *
     * @param key The key of the address field
     * @param jo The JSON object
     * @return The parsed address(es)
     * @throws JSONException If a JSON error occurred
     * @throws AddressException If parsing an address fails
     */
    public static InternetAddress[] parseAddressKey(final String key, final JSONObject jo) throws JSONException, AddressException {
        return parseAddressKey(key, jo, false);
    }

    private static final InternetAddress[] EMPTY_ADDRS = new InternetAddress[0];

    /**
     * Parses address field out of passed JSON object.
     *
     * @param key The key of the address field
     * @param jo The JSON object
     * @return The parsed address(es)
     * @throws JSONException If a JSON error occurred
     * @throws AddressException If parsing an address fails
     */
    public static InternetAddress[] parseAddressKey(final String key, final JSONObject jo, final boolean failOnError) throws JSONException, AddressException {
        String value = null;
        if (!jo.has(key) || jo.isNull(key) || (value = jo.getString(key)).length() == 0) {
            return EMPTY_ADDRS;
        }
        if (value.charAt(0) == '[') {
            /*
             * Treat as JSON array
             */
            try {
                final JSONArray jsonArr = new JSONArray(value);
                final int length = jsonArr.length();
                if (length == 0) {
                    return EMPTY_ADDRS;
                }
                value = parseAdressArray(jsonArr, length);
            } catch (final JSONException e) {
                LOG.error(e.getMessage(), e);
                /*
                 * Reset
                 */
                value = jo.getString(key);
            }
        }
        return parseAddressList(value, true, true);
    }

    /**
     * Expects the specified JSON array to be an array of arrays. Each inner array conforms to pattern:
     *
     * <pre>
     * [&quot;&lt;personal&gt;&quot;, &quot;&lt;email-address&gt;&quot;]
     * </pre>
     *
     * @param jsonArray The JSON array
     * @return Parsed address list combined in a {@link String} object
     * @throws JSONException If a JSON error occurs
     */
    private static String parseAdressArray(final JSONArray jsonArray, final int length) throws JSONException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(length << 6);
        {
            /*
             * Add first address
             */
            final JSONArray persAndAddr = jsonArray.getJSONArray(0);
            final String personal = persAndAddr.getString(0);
            final boolean hasPersonal = (personal != null && !"null".equals(personal));
            if (hasPersonal) {
                sb.append(quotePersonal(personal)).append(" <");
            }
            sb.append(persAndAddr.getString(1));
            if (hasPersonal) {
                sb.append('>');
            }
        }
        for (int i = 1; i < length; i++) {
            sb.append(", ");
            final JSONArray persAndAddr = jsonArray.getJSONArray(i);
            final String personal = persAndAddr.getString(0);
            final boolean hasPersonal = (personal != null && !"null".equals(personal));
            if (hasPersonal) {
                sb.append(quotePersonal(personal)).append(" <");
            }
            sb.append(persAndAddr.getString(1));
            if (hasPersonal) {
                sb.append('>');
            }
        }
        return sb.toString();
    }

    private static InternetAddress getEmailAddress(final String addrStr) {
        if (addrStr == null || addrStr.length() == 0) {
            return null;
        }
        try {
            return QuotedInternetAddress.parse(addrStr, true)[0];
        } catch (final AddressException e) {
            return null;
        }
    }

    private static void prepareMsgRef(final Session session, final MailMessage mail) throws OXException {
        final MailPath msgref = mail.getMsgref();
        if (null == msgref) {
            // Nothing to do
            return;
        }
        mail.setMsgref(prepareMsgRef(session, msgref));
    }

    private static MailPath prepareMsgRef(final Session session, final MailPath msgref) throws OXException {
        final UnifiedInboxManagement unifiedINBOXManagement =
            ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
        if (null != unifiedINBOXManagement && msgref.getAccountId() == unifiedINBOXManagement.getUnifiedINBOXAccountID(session)) {
            // Something like: INBOX/default6/INBOX
            final String nestedFullname = msgref.getFolder();
            final int pos = nestedFullname.indexOf(MailFolder.DEFAULT_FOLDER_ID);
            if (-1 == pos) {
                // Return unchanged
                return msgref;
            }
            int check = pos + MailFolder.DEFAULT_FOLDER_ID.length();
            while (Character.isDigit(nestedFullname.charAt(check))) {
                check++;
            }
            if (MailProperties.getInstance().getDefaultSeparator() != nestedFullname.charAt(check)) {
                // Unexpected pattern
                return msgref;
            }
            // Create fullname argument from sub-path
            final FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(nestedFullname.substring(pos));
            // Adjust msgref
            return new MailPath(arg.getAccountId(), arg.getFullname(), msgref.getMailID());
        }
        return msgref;
    }

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /** ASCII-wise to lower-case */
    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
