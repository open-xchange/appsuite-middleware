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

package com.openexchange.mail.json.parser;

import static com.openexchange.mail.mime.utils.MIMEMessageUtility.parseAddressList;
import static com.openexchange.mail.mime.utils.MIMEMessageUtility.quotePersonal;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataException;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataSource;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadFile;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.DataMailPart;
import com.openexchange.mail.dataobjects.compose.InfostoreDocumentMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.MultipleMailPartHandler;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link MessageParser} - Parses instances of {@link JSONObject} to instances of {@link MailMessage}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageParser {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MessageParser.class);

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
     * {@link ComposedMailMessage} object. Moreover the user's quota limitations are considered.
     * 
     * @param jsonObj The JSON object
     * @param uploadEvent The upload event containing the uploaded files to attach
     * @param session The session
     * @return A corresponding instance of {@link ComposedMailMessage}
     * @throws MailException If parsing fails
     */
    public static ComposedMailMessage parse(final JSONObject jsonObj, final UploadEvent uploadEvent, final Session session) throws MailException {
        try {
            final TransportProvider provider = TransportProviderRegistry.getTransportProviderBySession(session);
            final QuotaChecker quotaChecker = new QuotaChecker(session);
            /*
             * Parse transport message plus its text body
             */
            final ComposedMailMessage composedMail = parse(jsonObj, session, provider, quotaChecker);
            if (null != uploadEvent) {
                /*
                 * Uploaded files
                 */
                final int numOfUploadFiles = uploadEvent.getNumberOfUploadFiles();
                int attachmentCounter = 0;
                int addedAttachments = 0;
                while (addedAttachments < numOfUploadFiles) {
                    /*
                     * Get uploaded file by field name: file_0, file_1, ...
                     */
                    final UploadFile uf = uploadEvent.getUploadFileByFieldName(getFieldName(attachmentCounter++));
                    if (uf != null) {
                        quotaChecker.addConsumed(uf.getSize(), uf.getFileName());
                        composedMail.addEnclosedPart(provider.getNewFilePart(uf));
                        addedAttachments++;
                    }
                }
            }
            /*
             * Attached data sources
             */
            if (jsonObj.hasAndNotNull(MailJSONField.DATASOURCES.getKey())) {
                final ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class);
                if (conversionService == null) {
                    throw new MailException(new ServiceException(
                        ServiceException.Code.SERVICE_UNAVAILABLE,
                        ConversionService.class.getName()));
                }
                final JSONArray datasourceArray = jsonObj.getJSONArray(MailJSONField.DATASOURCES.getKey());
                final int length = datasourceArray.length();
                final Set<Class<?>> types = new HashSet<Class<?>>(4);
                for (int i = 0; i < length; i++) {
                    final JSONObject dataSourceObject = datasourceArray.getJSONObject(i);
                    if (!dataSourceObject.hasAndNotNull(JSON_IDENTIFIER)) {
                        throw new MailException(MailException.Code.MISSING_PARAM, JSON_IDENTIFIER);
                    }
                    final DataSource dataSource = conversionService.getDataSource(dataSourceObject.getString(JSON_IDENTIFIER));
                    if (dataSource == null) {
                        throw new MailException(DataExceptionCodes.UNKNOWN_DATA_SOURCE.create(dataSourceObject.getString(JSON_IDENTIFIER)));
                    }
                    if (!types.isEmpty()) {
                        types.clear();
                    }
                    types.addAll(Arrays.asList(dataSource.getTypes()));
                    final Data<?> data;
                    if (types.contains(InputStream.class)) {
                        try {
                            data = dataSource.getData(InputStream.class, parseDataSourceArguments(dataSourceObject), session);
                        } catch (final DataException e) {
                            throw new MailException(e);
                        }
                    } else if (types.contains(byte[].class)) {
                        try {
                            data = dataSource.getData(byte[].class, parseDataSourceArguments(dataSourceObject), session);
                        } catch (final DataException e) {
                            throw new MailException(e);
                        }
                    } else {
                        throw new MailException(MailException.Code.UNSUPPORTED_DATASOURCE);
                    }
                    final DataMailPart dataMailPart = provider.getNewDataPart(data.getData(), data.getDataProperties().toMap(), session);
                    quotaChecker.addConsumed(dataMailPart.getSize(), dataMailPart.getFileName());
                    composedMail.addEnclosedPart(dataMailPart);
                }
            }
            /*
             * Attached infostore document IDs
             */
            if (jsonObj.hasAndNotNull(MailJSONField.INFOSTORE_IDS.getKey())) {
                final JSONArray ja = jsonObj.getJSONArray(MailJSONField.INFOSTORE_IDS.getKey());
                final int length = ja.length();
                for (int i = 0; i < length; i++) {
                    final InfostoreDocumentMailPart part = provider.getNewDocumentPart(ja.getInt(i), session);
                    quotaChecker.addConsumed(part.getSize(), part.getFileName());
                    composedMail.addEnclosedPart(provider.getNewDocumentPart(ja.getInt(i), session));
                }
            }
            return composedMail;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private static DataArguments parseDataSourceArguments(final JSONObject json) throws JSONException {
        if (!json.hasAndNotNull(JSON_ARGS)) {
            return DataArguments.EMPTY_ARGS;
        }
        final JSONArray jsonArray = json.getJSONArray(JSON_ARGS);
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

    private static final String UPLOAD_FILE_ATTACHMENT_PREFIX = "file_";

    private static String getFieldName(final int num) {
        return new StringBuilder(8).append(UPLOAD_FILE_ATTACHMENT_PREFIX).append(num).toString();
    }

    /**
     * Parses given instance of {@link JSONObject} on send operation and generates a corresponding instance of {@link ComposedMailMessage}.
     * Moreover the user's quota limitations are considered.
     * 
     * @param jsonObj The JSON object
     * @param session The session
     * @return A corresponding instance of {@link ComposedMailMessage}
     * @throws MailException If parsing fails
     */
    public static ComposedMailMessage parse(final JSONObject jsonObj, final Session session) throws MailException {
        return parse(jsonObj, session, null, new QuotaChecker(session));
    }

    private static ComposedMailMessage parse(final JSONObject jsonObj, final Session session, final TransportProvider provider, final QuotaChecker quotaChecker) throws MailException {
        final TransportProvider tp = provider == null ? TransportProviderRegistry.getTransportProviderBySession(session) : provider;
        final ComposedMailMessage transportMail;
        try {
            final Context ctx = ContextStorage.getStorageContext(session.getContextId());
            transportMail = tp.getNewComposedMailMessage(session, ctx);
            parse(
                jsonObj,
                transportMail,
                TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone()),
                tp,
                session,
                quotaChecker);
        } catch (final ContextException e) {
            throw new MailException(e);
        }
        return transportMail;
    }

    /**
     * Parses given instance of {@link JSONObject} to given instance of {@link MailMessage}. Moreover the user's quota limitations are
     * considered.
     * 
     * @param jsonObj The JSON object (source)
     * @param mail The mail(target), which should be empty
     * @param session The session
     * @throws MailException If parsing fails
     */
    public static void parse(final JSONObject jsonObj, final MailMessage mail, final Session session) throws MailException {
        try {
            parse(jsonObj, mail, TimeZone.getTimeZone(UserStorage.getStorageUser(
                session.getUserId(),
                ContextStorage.getStorageContext(session.getContextId())).getTimeZone()), session);
        } catch (final ContextException e) {
            throw new MailException(e);
        }
    }

    /**
     * Parses given instance of {@link JSONObject} to given instance of {@link MailMessage}. Moreover the user's quota limitations are
     * considered.
     * 
     * @param jsonObj The JSON object (source)
     * @param mail The mail(target), which should be empty
     * @param timeZone The user time zone
     * @param session The session
     * @throws MailException If parsing fails
     */
    public static void parse(final JSONObject jsonObj, final MailMessage mail, final TimeZone timeZone, final Session session) throws MailException {
        parse(jsonObj, mail, timeZone, TransportProviderRegistry.getTransportProviderBySession(session), session, new QuotaChecker(session));
    }

    private static void parse(final JSONObject jsonObj, final MailMessage mail, final TimeZone timeZone, final TransportProvider provider, final Session session, final QuotaChecker quotaChecker) throws MailException {
        try {
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
                    headers.addHeader(key, obj.getString(key));
                }
                mail.addHeaders(headers);
            }
            /*
             * From Only mandatory if non-draft message
             */
            mail.addFrom(parseAddressKey(MailJSONField.FROM.getKey(), jsonObj));
            /*
             * To Only mandatory if non-draft message
             */
            mail.addTo(parseAddressKey(MailJSONField.RECIPIENT_TO.getKey(), jsonObj));
            /*
             * Cc
             */
            mail.addCc(parseAddressKey(MailJSONField.RECIPIENT_CC.getKey(), jsonObj));
            /*
             * Bcc
             */
            mail.addBcc(parseAddressKey(MailJSONField.RECIPIENT_BCC.getKey(), jsonObj));
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
                    final TextBodyMailPart part = provider.getNewTextBodyPart(tmp.getString(MailJSONField.CONTENT.getKey()));
                    part.setContentType(parseContentType(tmp.getString(MailJSONField.CONTENT_TYPE.getKey())));
                    mail.setContentType(part.getContentType());
                    transportMail.setBodyPart(part);
                    /*
                     * Parse referenced parts
                     */
                    parseReferencedParts(provider, session, transportMail, quotaChecker, attachmentArray);
                } else {
                    final TextBodyMailPart part = provider.getNewTextBodyPart("");
                    part.setContentType(MIMETypes.MIME_DEFAULT);
                    mail.setContentType(part.getContentType());
                    transportMail.setBodyPart(part);
                }
            }
            /*
             * TODO: Parse nested messages. Currently not used
             */
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        } catch (final AddressException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    private static final String ROOT = "0";

    private static final String FILE_PREFIX = "file://";

    private static void parseReferencedParts(final TransportProvider provider, final Session session, final ComposedMailMessage transportMail, final QuotaChecker quotaChecker, final JSONArray attachmentArray) throws MailException, JSONException {
        final int len = attachmentArray.length();
        if (len <= 1) {
            /*
             * We start at index 1 to skip the body part, so no attachment available
             */
            return;
        }
        /*
         * Group referenced parts by referenced mails' paths
         */
        final Map<String, ReferencedMailPart> groupedReferencedParts = groupReferencedParts(
            provider,
            session,
            transportMail.getMsgref(),
            attachmentArray);
        /*
         * Iterate attachments array
         */
        MailAccess<?, ?> access = null;
        try {
            ManagedFileManagement management = null;
            NextAttachment: for (int i = 1; i < len; i++) {
                final JSONObject attachment = attachmentArray.getJSONObject(i);
                final String seqId = attachment.hasAndNotNull(MailListField.ID.getKey()) ? attachment.getString(MailListField.ID.getKey()) : null;
                if (seqId != null && seqId.startsWith(FILE_PREFIX, 0)) {
                    /*
                     * A file reference
                     */
                    if (null == management) {
                        management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
                    }
                    processReferencedUploadFile(provider, management, transportMail, seqId, quotaChecker);
                } else {
                    /*
                     * Prefer MSGREF from attachment if present, otherwise get MSGREF from superior mail
                     */
                    final MailPath msgref;
                    final boolean isMail;
                    if (attachment.hasAndNotNull(MailJSONField.MSGREF.getKey())) {
                        msgref = new MailPath(attachment.get(MailJSONField.MSGREF.getKey()).toString());
                        isMail = true;
                    } else {
                        msgref = transportMail.getMsgref();
                        isMail = false;
                    }
                    if (null == msgref) {
                        /*
                         * Huh...? Not possible to load referenced parts without a referenced mail
                         */
                        continue NextAttachment;
                    }
                    /*
                     * Decide how to retrieve part
                     */
                    final ReferencedMailPart referencedMailPart;
                    if (isMail || null == seqId || ROOT.equals(seqId)) {
                        /*
                         * The mail itself
                         */
                        if (null == access) {
                            access = MailAccess.getInstance(session);
                            access.connect();
                        }
                        final MailMessage referencedMail = access.getMessageStorage().getMessage(msgref.getFolder(), msgref.getUid(), false);
                        quotaChecker.addConsumed(referencedMail.getSize(), referencedMail.getFileName());
                        referencedMailPart = provider.getNewReferencedMail(referencedMail, session);
                    } else {
                        referencedMailPart = groupedReferencedParts.get(seqId);
                        quotaChecker.addConsumed(referencedMailPart.getSize(), referencedMailPart.getFileName());
                    }
                    referencedMailPart.setMsgref(msgref);
                    transportMail.addEnclosedPart(referencedMailPart);
                }
            }
        } finally {
            if (null != access) {
                access.close(true);
            }
        }
    }

    private static Map<String, ReferencedMailPart> groupReferencedParts(final TransportProvider provider, final Session session, final MailPath parentMsgRef, final JSONArray attachmentArray) throws MailException, JSONException {
        if (null == parentMsgRef) {
            return Collections.emptyMap();
        }
        final int len = attachmentArray.length();
        final Set<String> groupedSeqIDs = new HashSet<String>(len);
        NextAttachment: for (int i = 1; i < len; i++) {
            final JSONObject attachment = attachmentArray.getJSONObject(i);
            final String seqId = attachment.hasAndNotNull(MailListField.ID.getKey()) ? attachment.getString(MailListField.ID.getKey()) : null;
            if (seqId != null && seqId.startsWith(FILE_PREFIX, 0)) {
                /*
                 * A file reference
                 */
                continue NextAttachment;
            }
            /*
             * If MSGREF is defined in attachment itself, the MSGREF's mail is meant to be attached and not a nested attachment
             */
            if (!attachment.hasAndNotNull(MailJSONField.MSGREF.getKey())) {
                groupedSeqIDs.add(seqId);
            }
        }
        /*
         * Now load them by message reference
         */
        if (groupedSeqIDs.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, ReferencedMailPart> retval = new HashMap<String, ReferencedMailPart>(len);
        final MailAccess<?, ?> access = MailAccess.getInstance(session);
        access.connect();
        try {
            final MailMessage referencedMail = access.getMessageStorage().getMessage(parentMsgRef.getFolder(), parentMsgRef.getUid(), false);
            // Get attachments out of referenced mail
            final MultipleMailPartHandler handler = new MultipleMailPartHandler(groupedSeqIDs, true);
            new MailMessageParser().parseMailMessage(referencedMail, handler);
            final Set<Map.Entry<String, MailPart>> results = handler.getMailParts().entrySet();
            for (final Map.Entry<String, MailPart> e : results) {
                retval.put(e.getKey(), provider.getNewReferencedPart(e.getValue(), session));
            }
        } finally {
            access.close(true);
        }
        return retval;
    }

    private static void processReferencedUploadFile(final TransportProvider provider, final ManagedFileManagement management, final ComposedMailMessage transportMail, final String seqId, final QuotaChecker quotaChecker) throws MailException {
        /*
         * A file reference
         */
        final ManagedFile managedFile;
        try {
            managedFile = management.getByID(seqId.substring(FILE_PREFIX.length()));
        } catch (ManagedFileException e) {
            LOG.error("No temp file found for ID: " + seqId.substring(FILE_PREFIX.length()), e);
            return;
        }
        // Add to quota checker
        quotaChecker.addConsumed(managedFile.getSize(), managedFile.getFileName());
        // Create wrapping upload file
        final UploadFile wrapper = new UploadFile();
        wrapper.setContentType(managedFile.getContentType());
        wrapper.setFileName(managedFile.getFileName());
        wrapper.setSize(managedFile.getSize());
        wrapper.setTmpFile(managedFile.getFile());
        transportMail.addEnclosedPart(provider.getNewFilePart(wrapper));
    }

    private static final String CT_ALTERNATIVE = "alternative";

    private static String parseContentType(final String ctStrArg) {
        final String ctStr = ctStrArg.toLowerCase(Locale.ENGLISH).trim();
        if (ctStr.indexOf(CT_ALTERNATIVE) != -1) {
            return MIMETypes.MIME_MULTIPART_ALTERNATIVE;
        }
        if (MIMETypes.MIME_TEXT_PLAIN.equals(ctStr)) {
            return MIMETypes.MIME_TEXT_PLAIN;
        }
        return MIMETypes.MIME_TEXT_HTML;
    }

    private static final InternetAddress[] EMPTY_ADDRS = new InternetAddress[0];

    private static InternetAddress[] parseAddressKey(final String key, final JSONObject jo) throws JSONException, AddressException {
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
        return parseAddressList(value, false);
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
        final StringBuilder sb = new StringBuilder(length << 6);
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
            return InternetAddress.parse(addrStr, true)[0];
        } catch (final AddressException e) {
            return null;
        }
    }

    private static final class QuotaChecker {

        private static final org.apache.commons.logging.Log LOG1 = org.apache.commons.logging.LogFactory.getLog(QuotaChecker.class);

        private final boolean doAction;

        private final long uploadQuota;

        private final long uploadQuotaPerFile;

        private long consumed;

        public QuotaChecker(final Session session) throws MailException {
            super();
            try {
                final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(
                    session.getUserId(),
                    session.getContextId());
                if (usm.getUploadQuota() >= 0) {
                    this.uploadQuota = usm.getUploadQuota();
                } else {
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug("Upload quota is less than zero. Using global server property \"MAX_UPLOAD_SIZE\" instead.");
                    }
                    long tmp;
                    try {
                        tmp = ServerConfig.getInteger(ServerConfig.Property.MAX_UPLOAD_SIZE);
                    } catch (final ConfigurationException e) {
                        LOG1.error(e.getMessage(), e);
                        tmp = 0;
                    }
                    this.uploadQuota = tmp;
                }
                this.uploadQuotaPerFile = usm.getUploadQuotaPerFile();
                doAction = ((uploadQuotaPerFile > 0) || (uploadQuota > 0));
            } catch (final UserConfigurationException e) {
                throw new MailException(e);
            }
        }

        public void addConsumed(final long size, final String fileName) throws MailException {
            if (!doAction) {
                return;
            }
            if (size <= 0 && LOG1.isDebugEnabled()) {
                LOG1.debug(new StringBuilder("Missing size: ").append(size).toString(), new Throwable());
            }
            if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
                throw new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED_FOR_FILE, UploadUtility.getSize(
                    uploadQuotaPerFile,
                    2,
                    false,
                    true), null == fileName ? "" : fileName, UploadUtility.getSize(size, 2, false, true));
            }
            /*
             * Add current file size
             */
            consumed += size;
            if (uploadQuota > 0 && consumed > uploadQuota) {
                throw new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED, UploadUtility.getSize(uploadQuota, 2, false, true));
            }
        }

    } // End of quota checker class

}
