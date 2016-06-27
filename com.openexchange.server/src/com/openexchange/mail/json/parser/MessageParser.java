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

package com.openexchange.mail.json.parser;

import static com.openexchange.java.Strings.toLowerCase;
import static com.openexchange.mail.mime.filler.MimeMessageFiller.isCustomOrReplyHeader;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
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
import com.openexchange.java.HTMLDetector;
import com.openexchange.java.Strings;
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
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
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
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link MessageParser} - Parses instances of {@link JSONObject} to instances of {@link MailMessage}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - {@link #parseBasics(JSONObject, MailMessage, TimeZone)}
 */
public final class MessageParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageParser.class);

    private static final String CONTENT_TYPE = MailJSONField.CONTENT_TYPE.getKey();
    private static final String CONTENT = MailJSONField.CONTENT.getKey();
    private static final String ATTACHMENTS = MailJSONField.ATTACHMENTS.getKey();
    private static final String ATTACHMENT_FILE_NAME = MailJSONField.ATTACHMENT_FILE_NAME.getKey();

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
     * @param jMail The JSON mail representation
     * @param uploadEvent The upload event containing the uploaded files to attach
     * @param session The session
     * @param accountId The account identifier
     * @param warnings A list to add possible warnings to
     * @return A corresponding instance of {@link ComposedMailMessage}
     * @throws OXException If parsing fails
     */
    public static ComposedMailMessage parse4Draft(JSONObject jMail, UploadEvent uploadEvent, Session session, int accountId, List<OXException> warnings) throws OXException {
        return parse(jMail, uploadEvent, session, accountId, null, null, false, warnings)[0];
    }

    /**
     * Completely parses given instance of {@link JSONObject} and given instance of {@link UploadEvent} to corresponding
     * {@link ComposedMailMessage} objects dedicated for being sent. Moreover the user's quota limitations are considered.
     *
     * @param jMail The JSON mail representation
     * @param uploadEvent The upload event containing the uploaded files to attach
     * @param session The session
     * @param accountId The account identifier
     * @param protocol The server's protocol
     * @param warnings A list to add possible warnings to
     * @param hostname The server's host name
     * @return The corresponding instances of {@link ComposedMailMessage}
     * @throws OXException If parsing fails
     */
    public static ComposedMailMessage[] parse4Transport(JSONObject jMail, UploadEvent uploadEvent, Session session, int accountId, String protocol, String hostName, List<OXException> warnings) throws OXException {
        return parse(jMail, uploadEvent, session, accountId, protocol, hostName, true, warnings);
    }

    /**
     * Completely parses given instance of {@link JSONObject} and given instance of {@link UploadEvent} to corresponding
     * {@link ComposedMailMessage} objects. Moreover the user's quota limitations are considered.
     *
     * @param jMail The JSON mail representation
     * @param uploadEvent The upload event containing the uploaded files to attach
     * @param session The session
     * @param accountId The account identifier
     * @param protocol The server's protocol
     * @param prepare4Transport <code>true</code> to parse with the intention to transport returned mail later on; otherwise <code>false</code>
     * @param warnings
     * @param hostname The server's host name
     * @param monitor The monitor
     * @return The corresponding instances of {@link ComposedMailMessage}
     * @throws OXException If parsing fails
     */
    private static ComposedMailMessage[] parse(JSONObject jMail, UploadEvent uploadEvent, Session session, int accountId, String protocol, String hostName, boolean prepare4Transport, List<OXException> warnings) throws OXException {
        try {
            TransportProvider provider = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
            Context ctx = ContextStorage.getStorageContext(session.getContextId());
            ComposedMailMessage composedMail = provider.getNewComposedMailMessage(session, ctx);
            composedMail.setAccountId(accountId);

            // Select appropriate handler
            IAttachmentHandler attachmentHandler;
            if (prepare4Transport && TransportProperties.getInstance().isPublishOnExceededQuota() && (!TransportProperties.getInstance().isPublishPrimaryAccountOnly() || (MailAccount.DEFAULT_ID == accountId))) {
                attachmentHandler = new PublishAttachmentHandler(session, provider, protocol, hostName);
            } else {
                attachmentHandler = new AbortAttachmentHandler(session);
            }

            // Parse transport message plus its text body
            parse(composedMail, jMail, session, accountId, provider, attachmentHandler, ctx, prepare4Transport);

            // Check for file attachments; either uploaded or referenced ones
            if (null != uploadEvent) {
                for (UploadFile uf : uploadEvent.getUploadFiles()) {
                    if (uf != null) {
                        attachmentHandler.addAttachment(provider.getNewFilePart(uf));
                    }
                }
            }

            // Attach data sources
            if (jMail.hasAndNotNull(MailJSONField.DATASOURCES.getKey())) {
                JSONArray datasourceArray = jMail.getJSONArray(MailJSONField.DATASOURCES.getKey());
                int length = datasourceArray.length();
                if (length > 0) {
                    // Check max. allowed Drive attachments
                    int max = MailProperties.getInstance().getMaxDriveAttachments();
                    if (max > 0 && length > max) {
                        throw MailExceptionCode.MAX_DRIVE_ATTACHMENTS_EXCEEDED.create(Integer.toString(max));
                    }
                    // Proceed
                    ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class);
                    if (conversionService == null) {
                        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConversionService.class.getName());
                    }
                    Set<Class<?>> types = new HashSet<Class<?>>(4);
                    for (int i = 0; i < length; i++) {
                        JSONObject dataSourceObject = datasourceArray.getJSONObject(i);
                        if (!dataSourceObject.hasAndNotNull(JSON_IDENTIFIER)) {
                            throw MailExceptionCode.MISSING_PARAM.create(JSON_IDENTIFIER);
                        }
                        DataSource dataSource = conversionService.getDataSource(dataSourceObject.getString(JSON_IDENTIFIER));
                        if (dataSource == null) {
                            throw DataExceptionCodes.UNKNOWN_DATA_SOURCE.create(dataSourceObject.getString(JSON_IDENTIFIER));
                        }
                        if (!types.isEmpty()) {
                            types.clear();
                        }
                        types.addAll(Arrays.asList(dataSource.getTypes()));
                        Data<?> data;
                        if (types.contains(InputStream.class)) {
                            data = dataSource.getData(InputStream.class, parseDataSourceArguments(dataSourceObject), session);
                        } else if (types.contains(byte[].class)) {
                            data = dataSource.getData(byte[].class, parseDataSourceArguments(dataSourceObject), session);
                        } else {
                            throw MailExceptionCode.UNSUPPORTED_DATASOURCE.create();
                        }
                        DataMailPart dataMailPart = provider.getNewDataPart(data.getData(), data.getDataProperties().toMap(), session);
                        attachmentHandler.addAttachment(dataMailPart);
                    }
                }
            }

            // Attach Drive documents
            if (jMail.hasAndNotNull(MailJSONField.INFOSTORE_IDS.getKey())) {
                JSONArray ja = jMail.getJSONArray(MailJSONField.INFOSTORE_IDS.getKey());
                int length = ja.length();
                if (length > 0) {
                    // Check max. allowed Drive attachments
                    int max = MailProperties.getInstance().getMaxDriveAttachments();
                    if (max > 0 && length > max) {
                        throw MailExceptionCode.MAX_DRIVE_ATTACHMENTS_EXCEEDED.create(Integer.toString(max));
                    }
                    for (int i = 0; i < length; i++) {
                        attachmentHandler.addAttachment(provider.getNewDocumentPart(ja.getString(i), session));
                    }
                }
            }

            // Fill composed mail
            ComposedMailMessage[] ret = attachmentHandler.generateComposedMails(composedMail, warnings);
            for (ComposedMailMessage mail : ret) {
                if (!mail.containsAccountId()) {
                    mail.setAccountId(accountId);
                }
            }
            return ret;
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static DataArguments parseDataSourceArguments(JSONObject json) throws JSONException {
        if (!json.hasAndNotNull(JSON_ARGS)) {
            return DataArguments.EMPTY_ARGS;
        }
        Object args = json.get(JSON_ARGS);
        if (args instanceof JSONArray) {
            /*
             * Handle as JSON array
             */
            JSONArray jsonArray = (JSONArray) args;
            int len = jsonArray.length();
            DataArguments dataArguments = new DataArguments(len);
            for (int i = 0; i < len; i++) {
                JSONObject elem = jsonArray.getJSONObject(i);
                if (elem.length() == 1) {
                    String key = elem.keys().next();
                    dataArguments.put(key, elem.getString(key));
                } else {
                    LOG.warn("Corrupt data argument in JSON object: {}", elem);
                }
            }
            return dataArguments;
        }
        /*
         * Expect JSON object
         */
        JSONObject argsObject = (JSONObject) args;
        int len = argsObject.length();
        DataArguments dataArguments = new DataArguments(len);
        for (Entry<String, Object> entry : argsObject.entrySet()) {
            dataArguments.put(entry.getKey(), entry.getValue().toString());
        }
        return dataArguments;
    }

    private static void parse(ComposedMailMessage transportMail, JSONObject jsonObj, Session session, int accountId, TransportProvider provider, IAttachmentHandler attachmentHandler, Context ctx, boolean prepare4Transport) throws OXException {
        TimeZone timeZone = TimeZoneUtils.getTimeZone(UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone());
        parse(jsonObj, transportMail, timeZone, provider, session, accountId, attachmentHandler, prepare4Transport);
    }

    /**
     * Parses given instance of {@link JSONObject} to given instance of {@link MailMessage}. Moreover the user's quota limitations are considered.
     *
     * @param jsonObj The JSON object (source)
     * @param mail The mail(target), which should be empty
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If parsing fails
     */
    public static void parse(JSONObject jsonObj, MailMessage mail, Session session, int accountId) throws OXException {
        Context ctx = ContextStorage.getStorageContext(session.getContextId());
        TimeZone timeZone = TimeZoneUtils.getTimeZone(UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone());
        parse(jsonObj, mail, timeZone, session, accountId);
    }

    /**
     * Parses given instance of {@link JSONObject} to given instance of {@link MailMessage}. Moreover the user's quota limitations are considered.
     *
     * @param jsonObj The JSON object (source)
     * @param mail The mail(target), which should be empty
     * @param timeZone The user time zone
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If parsing fails
     */
    public static void parse(JSONObject jsonObj, MailMessage mail, TimeZone timeZone, Session session, int accountId) throws OXException {
        parse(jsonObj, mail, timeZone, TransportProviderRegistry.getTransportProviderBySession(session, accountId), session, accountId, new AbortAttachmentHandler(session), false);
    }

    private static void parse(JSONObject jsonObj, MailMessage mail, TimeZone timeZone, TransportProvider provider, Session session, int accountId, IAttachmentHandler attachmentHandler, boolean prepare4Transport) throws OXException {
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
                ComposedMailMessage transportMail = (ComposedMailMessage) mail;
                JSONArray attachmentArray = jsonObj.optJSONArray(ATTACHMENTS);
                if (null != attachmentArray) {
                    /*
                     * Parse body text (the first array element)
                     */
                    String sContent;
                    {
                        JSONObject jTextBody = attachmentArray.getJSONObject(0);
                        sContent = jTextBody.getString(CONTENT);
                        TextBodyMailPart part = provider.getNewTextBodyPart(sContent);
                        String contentType;
                        {
                            String sContentType = jTextBody.optString(CONTENT_TYPE, null);
                            if (null == sContentType) {
                                sContentType = HTMLDetector.containsHTMLTags(sContent, true) ? "text/plain" : "text/html";
                            }
                            contentType = parseContentType(sContentType);
                        }
                        part.setContentType(contentType);
                        if (contentType.startsWith("text/plain") && jTextBody.optBoolean("raw", false)) {
                            part.setPlainText(sContent);
                        }
                        transportMail.setContentType(part.getContentType());
                        // Add text part
                        attachmentHandler.setTextPart(part);
                    }
                    /*
                     * Parse referenced parts
                     */
                    if (attachmentArray.length() > 1) {
                        Set<String> contentIds = extractContentIds(sContent);
                        MailPath transportMailMsgref = transportMail.getMsgref();
                        parseReferencedParts(provider, session, accountId, transportMailMsgref, attachmentHandler, attachmentArray, contentIds, prepare4Transport);
                    }
                } else {
                    TextBodyMailPart part = provider.getNewTextBodyPart("");
                    part.setContentType(MimeTypes.MIME_DEFAULT);
                    transportMail.setContentType(part.getContentType());
                    // Add text part
                    attachmentHandler.setTextPart(part);
                }
            }
            /*
             * TODO: Parse nested messages. Currently not used
             */
        } catch (JSONException e) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        } catch (AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static final Pattern PATTERN_ID_ATTRIBUTE = Pattern.compile("id=\"((?:\\\\\\\"|[^\"])+?)\"");

    private static Set<String> extractContentIds(String htmlContent) {
        ImageMatcher m = ImageMatcher.matcher(htmlContent);
        if (!m.find()) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<String>(4);
        do {
            String imageTag = m.group();
            Matcher tmp = PATTERN_ID_ATTRIBUTE.matcher(imageTag);
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
    public static void parseBasics(JSONObject jsonObj, MailMessage mail, TimeZone timeZone) throws JSONException, AddressException, OXException {
        parseBasics(jsonObj, mail, timeZone, false);
    }

    private static void parseBasics(JSONObject jsonObj, MailMessage mail, TimeZone timeZone, boolean prepare4Transport) throws JSONException, AddressException, OXException {
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
            JSONArray arr = jsonObj.getJSONArray(MailJSONField.USER.getKey());
            int length = arr.length();
            List<String> l = new ArrayList<String>(length);
            for (int i = 0; i < length; i++) {
                l.add(arr.getString(i));
            }
            mail.addUserFlags(l.toArray(new String[l.size()]));
        }
        /*
         * Parse headers
         */
        if (jsonObj.hasAndNotNull(MailJSONField.HEADERS.getKey())) {
            JSONObject jHeaders = jsonObj.getJSONObject(MailJSONField.HEADERS.getKey());
            int size = jHeaders.length();
            HeaderCollection headers = new HeaderCollection(size);
            Iterator<String> iter = jHeaders.keys();
            for (int i = size; i-- > 0;) {
                String key = iter.next();
                if (isCustomOrReplyHeader(key) && !key.equalsIgnoreCase("x-original-headers")) {
                    headers.setHeader(key, jHeaders.getString(key));
                }
            }
            mail.addHeaders(headers);
        }
        /*
         * From Only mandatory if non-draft message
         */
        String fromKey = MailJSONField.FROM.getKey();
        if (jsonObj.hasAndNotNull(fromKey)) {
            try {
                String value = jsonObj.getString(fromKey);
                int endPos;
                if ('[' == value.charAt(0) && (endPos = value.indexOf(']', 1)) < value.length()) {
                    value = new StringBuilder(32).append("\"[").append(value.substring(1, endPos)).append("]\"").append(value.substring(endPos+1)).toString();
                }
                mail.addFrom(parseAddressList(value, true, true));
            } catch (AddressException e) {
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
            InternetAddress[] addrs = parseAddressKey("reply_to", jsonObj, false);
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
            String dispVal = jsonObj.getString(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey());
            if (STR_TRUE.equalsIgnoreCase(dispVal)) {
                /*
                 * Boolean value "true"
                 */
                InternetAddress[] from = mail.getFrom();
                mail.setDispositionNotification(from.length > 0 ? from[0] : null);
            } else {
                InternetAddress ia = getEmailAddress(dispVal);
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
            long date = jsonObj.getLong(MailJSONField.SENT_DATE.getKey());
            int offset = timeZone.getOffset(date);
            mail.setSentDate(new Date(jsonObj.getLong(MailJSONField.SENT_DATE.getKey()) - offset));
        }
        if (jsonObj.hasAndNotNull(MailJSONField.RECEIVED_DATE.getKey())) {
            long date = jsonObj.getLong(MailJSONField.RECEIVED_DATE.getKey());
            int offset = timeZone.getOffset(date);
            mail.setReceivedDate(new Date(jsonObj.getLong(MailJSONField.RECEIVED_DATE.getKey()) - offset));
        }
        /*
         * Drop special "x-original-headers" header
         */
        mail.removeHeader("x-original-headers");
    }

    private static final String ROOT = "0";

    private static final String FILE_PREFIX = "file://";

    private static void parseReferencedParts(TransportProvider provider, Session session, int accountId, MailPath transportMailMsgref, IAttachmentHandler attachmentHandler, JSONArray jAttachments, Set<String> contentIds, boolean prepare4Transport) throws OXException, JSONException {
        int len = jAttachments.length();
        /*
         * Group referenced parts by referenced mails' paths
         */
        Map<String, ReferencedMailPart> groupedReferencedParts = groupReferencedParts(provider, session, transportMailMsgref, jAttachments, contentIds, prepare4Transport);
        /*
         * Iterate attachments array
         */
        MailAccess<?, ?> access = null;
        try {
            ManagedFileManagement management = null;
            NextAttachment: for (int i = 1; i < len; i++) {
                JSONObject jAttachment = jAttachments.getJSONObject(i);
                String seqId = jAttachment.optString(MailListField.ID.getKey(), null);
                if (null == seqId && jAttachment.hasAndNotNull(CONTENT)) {
                    /*
                     * A direct attachment, as data part
                     */
                    String contentType = parseContentType(jAttachment.getString(CONTENT_TYPE));
                    String charsetName = "UTF-8";
                    byte[] content;
                    try {
                        /*
                         * UI provides HTML content in any case. Generate well-formed HTML for further processing dependent on given content
                         * type.
                         */
                        HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                        if (MimeTypes.MIME_TEXT_PLAIN.equals(contentType)) {
                            if (jAttachment.optBoolean("raw", false)) {
                                content = jAttachment.getString(CONTENT).getBytes(charsetName);
                            } else {
                                content = htmlService.html2text(jAttachment.getString(CONTENT), true).getBytes(charsetName);
                            }
                        } else {
                            String conformHTML = htmlService.getConformHTML(jAttachment.getString(CONTENT), "ISO-8859-1");
                            content = conformHTML.getBytes(charsetName);
                        }

                    } catch (UnsupportedEncodingException e) {
                        throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
                    }
                    /*
                     * As data object
                     */
                    DataProperties properties = new DataProperties();
                    properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contentType);
                    properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(content.length));
                    properties.put(DataProperties.PROPERTY_CHARSET, charsetName);
                    {
                        String fileName = jAttachment.optString(ATTACHMENT_FILE_NAME, null);
                        if (!Strings.isEmpty(fileName)) {
                            properties.put(DataProperties.PROPERTY_NAME, fileName);
                        }
                    }
                    Data<byte[]> data = new SimpleData<byte[]>(content, properties);
                    DataMailPart dataMailPart = provider.getNewDataPart(data.getData(), data.getDataProperties().toMap(), session);
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
                    boolean isMail;
                    String msgrefKey = MailJSONField.MSGREF.getKey();
                    if (jAttachment.hasAndNotNull(msgrefKey)) {
                        msgref = new MailPath(jAttachment.get(msgrefKey).toString());
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
                    ReferencedMailPart referencedMailPart;
                    if (isMail || ROOT.equals(seqId)) {
                        /*
                         * The mail itself
                         */
                        if (null == access) {
                            access = MailAccess.getInstance(session, msgref.getAccountId());
                            access.connect();
                        }
                        MailMessage referencedMail = access.getMessageStorage().getMessage(msgref.getFolder(), msgref.getMailID(), false);
                        if (null == referencedMail) {
                            throw MailExceptionCode.REFERENCED_MAIL_NOT_FOUND.create(msgref.getMailID(), msgref.getFolder());
                        }
                        referencedMail.setAccountId(access.getAccountId());
                        referencedMail = ManagedMimeMessage.clone(referencedMail);
                        referencedMailPart = provider.getNewReferencedMail(referencedMail, session);
                    } else {
                        referencedMailPart = null == seqId ? null : groupedReferencedParts.get(seqId);
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

    private static Map<String, ReferencedMailPart> groupReferencedParts(TransportProvider provider, Session session, MailPath parentMsgRef, JSONArray jAttachments, Set<String> contentIds, boolean prepare4Transport) throws OXException, JSONException {
        if (null == parentMsgRef) {
            return Collections.emptyMap();
        }
        int len = jAttachments.length();
        Map<String, String> groupedSeqIDs = new HashMap<String, String>(len);
        NextAttachment: for (int i = 1; i < len; i++) {
            JSONObject jAttachment = jAttachments.getJSONObject(i);
            String seqId = jAttachment.hasAndNotNull(MailListField.ID.getKey()) ? jAttachment.getString(MailListField.ID.getKey()) : null;
            if (seqId == null || seqId.startsWith(FILE_PREFIX, 0)) {
                /*
                 * A file reference
                 */
                continue NextAttachment;
            }
            /*
             * If MSGREF is defined in attachment itself, the MSGREF's mail is meant to be attached and not a nested attachment
             */
            if (!jAttachment.hasAndNotNull(MailJSONField.MSGREF.getKey())) {
                Object cid = jAttachment.opt(MailJSONField.CID.getKey());
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
        } catch (OXException oe) {
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

    private static void handleMultipleRefs(TransportProvider provider, Session session, MailPath parentMsgRef, Set<String> contentIds, boolean prepare4Transport, Map<String, String> groupedSeqIDs, Map<String, ReferencedMailPart> retval, MailAccess<?, ?> access) throws OXException {
        MailPath pMsgRef = /*prepareMsgref(*/parentMsgRef/*)*/;
        MailMessage referencedMail = access.getMessageStorage().getMessage(pMsgRef.getFolder(), pMsgRef.getMailID(), false);
        if (null == referencedMail) {
            throw MailExceptionCode.REFERENCED_MAIL_NOT_FOUND.create(pMsgRef.getMailID(), pMsgRef.getFolder());
        }
        referencedMail.setAccountId(access.getAccountId());
        referencedMail = ManagedMimeMessage.clone(referencedMail);
        // Get attachments out of referenced mail
        Set<String> remaining = new HashSet<String>(groupedSeqIDs.keySet());
        MultipleMailPartHandler handler = new MultipleMailPartHandler(groupedSeqIDs.keySet(), false);
        new MailMessageParser().parseMailMessage(referencedMail, handler);
        for (Map.Entry<String, MailPart> e : handler.getMailParts().entrySet()) {
            String seqId = e.getKey();
            retval.put(seqId, provider.getNewReferencedPart(e.getValue(), session));
            remaining.remove(seqId);
        }
        if (prepare4Transport && !remaining.isEmpty()) {
            for (String seqId : remaining) {
                if (!contentIds.contains(seqId)) {
                    throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(seqId, Long.valueOf(referencedMail.getMailId()), referencedMail.getFolder());
                }
            }
        }
    }

    private static MailPath prepareMsgref(MailPath msgref) throws OXException {
        String mailID = msgref.getMailID();
        if (mailID.startsWith("%64%65%66%61")) {
            // Referenced by Unified Mail; e.g. "%64%65%66%61ult0%2FIN%42OX%2F%44r%61%66ts%2F2255"
            return new MailPath(decodeQP(mailID));
        }
        return msgref;
    }

    private static final Pattern DECODE_PATTERN = Pattern.compile("%");
    private static String decodeQP(String string) {
        try {
            return new String(QuotedPrintableCodec.decodeQuotedPrintable(Charsets.toAsciiBytes(DECODE_PATTERN.matcher(string).replaceAll("="))), com.openexchange.java.Charsets.UTF_8);
        } catch (DecoderException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void processReferencedUploadFile(TransportProvider provider, ManagedFileManagement management, String seqId, IAttachmentHandler attachmentHandler) throws OXException {
        /*
         * A file reference
         */
        ManagedFile managedFile;
        try {
            managedFile = management.getByID(seqId.substring(FILE_PREFIX.length()));
        } catch (OXException e) {
            LOG.error("No temp file found for ID: {}", seqId.substring(FILE_PREFIX.length()), e);
            return;
        }
        // Create wrapping upload file
        UploadFile wrapper = new UploadFileImpl();
        wrapper.setContentType(managedFile.getContentType());
        wrapper.setFileName(managedFile.getFileName());
        wrapper.setSize(managedFile.getSize());
        wrapper.setTmpFile(managedFile.getFile());
        // Add to quota checker
        attachmentHandler.addAttachment(provider.getNewFilePart(wrapper));
    }

    private static final String CT_ALTERNATIVE = "alternative";

    private static String parseContentType(String ctStrArg) {
        String ctStr = toLowerCase(ctStrArg).trim();
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
    public static InternetAddress[] getFromField(JSONObject jo) throws AddressException, JSONException {
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
    public static InternetAddress[] parseAddressKey(String key, JSONObject jo) throws JSONException, AddressException {
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
    public static InternetAddress[] parseAddressKey(String key, JSONObject jo, boolean failOnError) throws JSONException, AddressException {
        if (!jo.has(key) || jo.isNull(key)) {
            return EMPTY_ADDRS;
        }

        JSONArray jAddresses = jo.optJSONArray(key);
        if (null == jAddresses) {
            return parseAddressList(jo.getString(key), true, failOnError);
        }

        // Treat as JSON array
        try {
            int length = jAddresses.length();
            if (length == 0) {
                return EMPTY_ADDRS;
            }
            return parseAdressArray(jAddresses, length, ParseMode.AS_IS);
        } catch (JSONException e) {
            LOG.error("", e);
            /*
             * Reset
             */
            return parseAddressList(jo.getString(key), true, failOnError);
        }
    }

    /**
     * Expects the specified JSON array to be an array of arrays. Each inner array conforms to pattern:
     *
     * <pre>
     * [&quot;&lt;personal&gt;&quot;, &quot;&lt;email-address&gt;&quot;]
     * </pre>
     *
     * @param jAddresses The JSON array of addresses
     * @param length The length of the passed JSON array
     * @param strict boolean flag to enable/disable strict RFC822 parsing
     * @return Parsed address list combined in a {@link String} object
     * @throws JSONException If a JSON error occurs
     * @throws AddressException If constructing an address fails
     */
    public static InternetAddress[] parseAdressArray(JSONArray jAddresses, int length, boolean strict) throws JSONException, AddressException {
        return parseAdressArray(jAddresses, length, strict ? ParseMode.STRICT : ParseMode.LENIENT);
    }

    /** The parse mode */
    public static enum ParseMode {
        /**
         * Enforces strict RFC822 syntax during parsing
         */
        STRICT,
        /**
         * Enforces no strict RFC822 syntax during parsing, but does fail for completely syntactically wrong addresses; e.g. containing illegal characters
         */
        LENIENT,
        /**
         * Simply passes address string as-is.
         */
        AS_IS;
    }

    /**
     * Expects the specified JSON array to be an array of arrays. Each inner array conforms to pattern:
     *
     * <pre>
     * [&quot;&lt;personal&gt;&quot;, &quot;&lt;email-address&gt;&quot;]
     * </pre>
     *
     * @param jAddresses The JSON array of addresses
     * @param length The length of the passed JSON array
     * @param parseMode The parse mode to apply
     * @return Parsed address list combined in a {@link String} object
     * @throws JSONException If a JSON error occurs
     * @throws AddressException If constructing an address fails
     */
    public static InternetAddress[] parseAdressArray(JSONArray jAddresses, int length, ParseMode parseMode) throws JSONException, AddressException {
        boolean strictOrLenient = (ParseMode.STRICT == parseMode) || (ParseMode.LENIENT == parseMode);
        List<InternetAddress> addresses = new ArrayList<InternetAddress>(length);
        for (int i = 0, k = length; k-- > 0; i++) {
            JSONArray persAndAddr = jAddresses.getJSONArray(i);
            int pLen = persAndAddr.length();
            if (pLen != 0) {
                if (1 == pLen) {
                    if (strictOrLenient) {
                        addresses.add(new QuotedInternetAddress(persAndAddr.getString(0), ParseMode.STRICT == parseMode));
                    } else {
                        // Use 'QuotedInternetAddress(String, String, String)' constructor for no parsing, but accepting literals as-is
                        try {
                            addresses.add(new QuotedInternetAddress(persAndAddr.getString(0), null, "UTF-8"));
                        } catch (UnsupportedEncodingException x) {
                            // Cannot occur
                        }
                    }
                } else {
                    String personal = persAndAddr.optString(0, null);
                    boolean hasPersonal = (personal != null && !"null".equals(personal));
                    if (hasPersonal) {
                        if (strictOrLenient) {
                            QuotedInternetAddress addr = new QuotedInternetAddress(persAndAddr.getString(1), ParseMode.STRICT == parseMode);
                            try {
                                addr.setPersonal(personal, "UTF-8");
                            } catch (UnsupportedEncodingException x) {
                                // Cannot occur
                            }
                            addresses.add(addr);
                        } else {
                            // Use 'QuotedInternetAddress(String, String, String)' constructor for no parsing, but accepting literals as-is
                            try {
                                addresses.add(new QuotedInternetAddress(persAndAddr.getString(1), personal, "UTF-8"));
                            } catch (UnsupportedEncodingException x) {
                                // Cannot occur
                            }
                        }
                    } else {
                        if (strictOrLenient) {
                            addresses.add(new QuotedInternetAddress(persAndAddr.getString(1), ParseMode.STRICT == parseMode));
                        } else {
                            // Use 'QuotedInternetAddress(String, String, String)' constructor for no parsing, but accepting literals as-is
                            try {
                                addresses.add(new QuotedInternetAddress(persAndAddr.getString(1), null, "UTF-8"));
                            } catch (UnsupportedEncodingException x) {
                                // Cannot occur
                            }
                        }
                    }
                }
            }
        }
        return addresses.toArray(new InternetAddress[addresses.size()]);
    }

    private static InternetAddress getEmailAddress(String addrStr) {
        if (com.openexchange.java.Strings.isEmpty(addrStr)) {
            return null;
        }
        try {
            return new QuotedInternetAddress(addrStr, false);
        } catch (AddressException e) {
            return null;
        }
    }

    private static void prepareMsgRef(Session session, MailMessage mail) throws OXException {
        MailPath msgref = mail.getMsgref();
        if (null == msgref) {
            // Nothing to do
            return;
        }
        mail.setMsgref(prepareMsgRef(session, msgref));
    }

    private static MailPath prepareMsgRef(Session session, MailPath msgref) throws OXException {
        UnifiedInboxManagement unifiedINBOXManagement = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
        if (null != unifiedINBOXManagement && msgref.getAccountId() == unifiedINBOXManagement.getUnifiedINBOXAccountID(session)) {
            // Something like: INBOX/default6/INBOX
            String nestedFullname = msgref.getFolder();
            int pos = nestedFullname.indexOf(MailFolder.DEFAULT_FOLDER_ID);
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
            FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(nestedFullname.substring(pos));
            // Adjust msgref
            return new MailPath(arg.getAccountId(), arg.getFullname(), msgref.getMailID());
        }
        return msgref;
    }

}
