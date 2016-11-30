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

package com.openexchange.groupware.container.mail;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import javax.activation.DataHandler;
import javax.mail.Message.RecipientType;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.datasource.FileDataSource;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.datasource.MimeMessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.version.Version;

/**
 * MailObject
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailObject {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailObject.class);

    private static volatile String staticHostName;

    private static volatile UnknownHostException warnSpam;

    static {
        // Host name initialization
        try {
            staticHostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            staticHostName = "localhost";
            warnSpam = e;
        }
    }

    public static final int DONT_SET = -2;

    private String fromAddr;

    private String[] toAddrs;

    private String[] ccAddrs;

    private String[] bccAddrs;

    private String subject;

    private Object text;

    private String contentType;

    private boolean requestReadReceipt;

    private final Session session;

    private final int objectId;

    private final int folderId;

    private final int module;

    private final String type;

    private Multipart multipart;

    private boolean internalRecipient;

    private String uid;

    private long recurrenceDatePosition;

    private boolean autoGenerated;

    private final Map<String, String> additionalHeaders;

    /**
     * Initializes a new {@link MailObject}
     *
     * @param session The session providing needed user data
     * @param objectId The object ID this message refers to
     * @param folderId The folder ID to which the referred object belongs
     * @param module The module of the referred object
     * @param type The object's notification type
     */
    public MailObject(final Session session, final int objectId, final int folderId, final int module, final String type) {
        super();
        additionalHeaders = new LinkedHashMap<>(4);
        internalRecipient = true;
        this.session = session;
        this.objectId = objectId;
        this.folderId = folderId;
        this.module = module;
        this.type = type;
    }

    private final void validateMailObject() throws OXException {
        if (com.openexchange.java.Strings.isEmpty(fromAddr)) {
            throw MailExceptionCode.MISSING_FIELD.create("From");
        } else if (toAddrs == null || toAddrs.length == 0) {
            throw MailExceptionCode.MISSING_FIELD.create("To");
        } else if (com.openexchange.java.Strings.isEmpty(contentType)) {
            throw MailExceptionCode.MISSING_FIELD.create("Content-Type");
        } else if (subject == null) {
            throw MailExceptionCode.MISSING_FIELD.create("Subject");
        } else if (text == null) {
            throw MailExceptionCode.MISSING_FIELD.create("Text");
        }
    }

    /**
     * Sets given additional header.
     * <p>
     * Note: Header name is required to start with <code>"X-"</code> prefix.
     *
     * @param name The header name
     * @param value The header value
     * @throws IllegalArgumentException If either name/value is <code>null</code> or name does not start with <code>"X-"</code> prefix
     */
    public void setAdditionalHeader(String name, String value) {
        if (null == name) {
            throw new IllegalArgumentException("name is null");
        }
        if (null == value) {
            throw new IllegalArgumentException("value is null");
        }
        if (!name.startsWith("X-")) {
            throw new IllegalArgumentException("name does not start with \"X-\" prefix");
        }

        additionalHeaders.put(name, value);
    }

    /**
     * Sets given additional headers.
     * <p>
     * Note: Header names are required to start with <code>"X-"</code> prefix.
     *
     * @param headers The headers to set
     * @throws IllegalArgumentException If any header's name/value is <code>null</code> or its name does not start with <code>"X-"</code> prefix
     */
    public void setAdditionalHeaders(Map<? extends String, ? extends String> headers) {
        if (null != headers && false == headers.isEmpty()) {
            for (Map.Entry<? extends String, ? extends String> entry : headers.entrySet()) {
                setAdditionalHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Sets whether this mail is transported to an internal recipient or not.
     *
     * @param internalRecipient <code>true</code> for internal recipient; otherwise <code>false</code>
     */
    public void setInternalRecipient(final boolean internalRecipient) {
        this.internalRecipient = internalRecipient;
    }

    /**
     * Adds a file attachment to this mail object.
     *
     * @param contentType The content type (incl. charset parameter)
     * @param file The file to attach
     * @throws OXException If file attachment cannot be added
     */
    public void addFileAttachment(final ContentType contentType, final File file) throws OXException {
        /*
         * Determine proper content type
         */
        final String fileName = file.getName();
        final ContentType ct = new ContentType();
        ct.setContentType(contentType);
        if (ct.startsWith(MimeTypes.MIME_APPL_OCTET) || ct.startsWith(MimeTypes.MIME_MULTIPART_OCTET)) {
            /*
             * Try to determine MIME type
             */
            final String ctStr = MimeType2ExtMap.getContentType(fileName);
            final int pos = ctStr.indexOf('/');
            ct.setPrimaryType(ctStr.substring(0, pos));
            ct.setSubType(ctStr.substring(pos + 1));
        }
        try {
            /*
             * Generate body part
             */
            final MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setDataHandler(new DataHandler(new FileDataSource(file, ct.toString())));
            /*
             * Content-Type
             */
            if (fileName != null && !ct.containsNameParameter()) {
                ct.setNameParameter(fileName);
            }
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
            /*
             * Force base64 encoding to keep data as it is
             */
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
            /*
             * Disposition
             */
            final String disposition = bodyPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
            if (disposition == null) {
                final String disp;
                if (fileName == null) {
                    disp = Part.ATTACHMENT;
                } else {
                    final ContentDisposition contentDisposition = new ContentDisposition(Part.ATTACHMENT);
                    contentDisposition.setFilenameParameter(fileName);
                    disp = MimeMessageUtility.foldContentDisposition(contentDisposition.toString());
                }
                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, disp);
            } else {
                final ContentDisposition contentDisposition = new ContentDisposition(disposition);
                contentDisposition.setDisposition(Part.ATTACHMENT);
                if (fileName != null && !contentDisposition.containsFilenameParameter()) {
                    contentDisposition.setFilenameParameter(fileName);
                }
                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(contentDisposition.toString()));
            }
            /*
             * Add to multipart
             */
            if (multipart == null) {
                multipart = new MimeMultipart("mixed");
            }
            multipart.addBodyPart(bodyPart);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Adds a file attachment to this mail object.
     *
     * @param contentType The content type (incl. charset parameter)
     * @param fileName The attachment's file name
     * @param inputStream The attachment's data as an input stream
     * @throws OXException If file attachment cannot be added
     */
    public void addFileAttachment(final ContentType contentType, final String fileName, final InputStream inputStream) throws OXException {
        /*
         * Determine proper content type
         */
        final ContentType ct = new ContentType();
        ct.setContentType(contentType);
        if ((ct.startsWith(MimeTypes.MIME_APPL_OCTET) || ct.startsWith(MimeTypes.MIME_MULTIPART_OCTET)) && fileName != null) {
            /*
             * Try to determine MIME type
             */
            final String ctStr = MimeType2ExtMap.getContentType(fileName);
            final int pos = ctStr.indexOf('/');
            ct.setPrimaryType(ctStr.substring(0, pos));
            ct.setSubType(ctStr.substring(pos + 1));
        }
        try {
            /*
             * Generate body part
             */
            final MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setDataHandler(new DataHandler(new MessageDataSource(inputStream, getContentType())));
            /*
             * Content-Type
             */
            if (fileName != null && !ct.containsNameParameter()) {
                ct.setNameParameter(fileName);
            }
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
            /*
             * Force base64 encoding to keep data as it is
             */
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
            /*
             * Disposition
             */
            final String disposition = bodyPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
            if (disposition == null) {
                final String disp;
                if (fileName == null) {
                    disp = Part.ATTACHMENT;
                } else {
                    final ContentDisposition contentDisposition = new ContentDisposition(Part.ATTACHMENT);
                    contentDisposition.setFilenameParameter(fileName);
                    disp = MimeMessageUtility.foldContentDisposition(contentDisposition.toString());
                }
                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, disp);
            } else {
                final ContentDisposition contentDisposition = new ContentDisposition(disposition);
                contentDisposition.setDisposition(Part.ATTACHMENT);
                if (fileName != null && !contentDisposition.containsFilenameParameter()) {
                    contentDisposition.setFilenameParameter(fileName);
                }
                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(contentDisposition.toString()));
            }
            /*
             * Add to multipart
             */
            if (multipart == null) {
                multipart = new MimeMultipart("mixed");
            }
            multipart.addBodyPart(bodyPart);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private final static String HEADER_XPRIORITY = "X-Priority";

    private final static String HEADER_DISPNOTTO = "Disposition-Notification-Toy";

    private final static String HEADER_XOXREMINDER = "X-OX-Reminder";

    private final static String VALUE_PRIORITYNROM = "3 (normal)";

    private final static String HEADER_ORGANIZATION = "Organization";

    private final static String HEADER_AUTO_SUBMITTED = "Auto-Submitted";

    private final static String VALUE_AUTO_GENERATED = "auto-generated";

    private final static String HEADER_X_MAILER = "X-Mailer";

    private final static String HEADER_X_OX_MODULE = "X-Open-Xchange-Module";

    private final static String HEADER_X_OX_TYPE = "X-Open-Xchange-Type";

    private final static String HEADER_X_OX_OBJECT = "X-Open-Xchange-Object";

    private final static String HEADER_X_OX_UID = "X-Open-Xchange-UID";

    private final static String HEADER_X_OX_RECURRENCE_DATE = "X-Open-Xchange-RDATE";

    public final void send() throws OXException {
        try {
            validateMailObject();
            final MimeMessage msg = new MimeMessage(MimeDefaultSession.getDefaultSession());
            /*
             * Set from
             */
            InternetAddress[] internetAddrs = parseAddressList(fromAddr, false);
            if (null == internetAddrs || 0 == internetAddrs.length) {
                throw MimeMailException.handleMessagingException(new AddressException("\"From\" cannot be parsed: " + fromAddr));
            }
            msg.setFrom(internetAddrs[0]);
            msg.setReplyTo(internetAddrs);
            /*
             * Set to
             */
            String tmp = Arrays.toString(toAddrs);
            tmp = tmp.substring(1, tmp.length() - 1);
            internetAddrs = parseAddressList(tmp, false);
            msg.setRecipients(RecipientType.TO, internetAddrs);
            /*
             * Set cc
             */
            if (ccAddrs != null && ccAddrs.length > 0) {
                tmp = Arrays.toString(ccAddrs);
                tmp = tmp.substring(1, tmp.length() - 1);
                internetAddrs = parseAddressList(tmp, false);
                msg.setRecipients(RecipientType.CC, internetAddrs);
            }
            /*
             * Set bcc
             */
            if (bccAddrs != null && bccAddrs.length > 0) {
                tmp = Arrays.toString(bccAddrs);
                tmp = tmp.substring(1, tmp.length() - 1);
                internetAddrs = parseAddressList(tmp, false);
                msg.setRecipients(RecipientType.BCC, internetAddrs);
            }
            final ContentType ct = new ContentType(contentType);
            if (!ct.containsCharsetParameter()) {
                /*
                 * Ensure a charset is set
                 */
                ct.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
            }
            /*
             * Set subject
             */
            msg.setSubject(subject, ct.getCharsetParameter());
            /*-
             * Examine message's content type
             *
            if (!"text".equalsIgnoreCase(ct.getPrimaryType())) {
                //throw new OXException(OXException.Code.UNSUPPORTED_MIME_TYPE, ct.toString());
            }
            */
            /*
             * Set content and its type
             */
            final String subType = ct.getSubType();
            if (multipart == null) {
                if ("html".equalsIgnoreCase(subType) || "htm".equalsIgnoreCase(subType)) {
                    msg.setDataHandler(new DataHandler(new MessageDataSource(text.toString(), ct)));
                    // msg.setContent(text, ct.toString());
                } else if ("plain".equalsIgnoreCase(subType) || "enriched".equalsIgnoreCase(subType)) {
                    if (!ct.containsCharsetParameter()) {
                        MessageUtility.setText((String) text, msg);
                        // msg.setText((String) text);
                    } else {
                        MessageUtility.setText((String) text, ct.getCharsetParameter(), msg);
                        // msg.setText((String) text, ct.getCharsetParameter());
                    }
                } else if (ct.startsWith("multipart/")) {
                    MessageUtility.setContent((Multipart) text, msg);
                    // msg.setContent((Multipart) text);
                } else {
                    throw MailExceptionCode.UNSUPPORTED_MIME_TYPE.create(ct.toString());
                }
            } else {
                final MimeBodyPart textPart = new MimeBodyPart();
                if ("html".equalsIgnoreCase(subType) || "htm".equalsIgnoreCase(subType)) {
                    textPart.setDataHandler(new DataHandler(new MessageDataSource(text.toString(), ct)));
                    // textPart.setContent(text, ct.toString());
                } else if ("plain".equalsIgnoreCase(subType) || "enriched".equalsIgnoreCase(subType)) {
                    if (!ct.containsCharsetParameter()) {
                        MessageUtility.setText((String) text, textPart);
                        // textPart.setText((String) text);
                    } else {
                        MessageUtility.setText((String) text, ct.getCharsetParameter(), textPart);
                        // textPart.setText((String) text, ct.getCharsetParameter());
                    }
                } else if (ct.startsWith("multipart/")) {
                    MessageUtility.setContent((Multipart) text, textPart);
                    // textPart.setContent((Multipart) text);
                } else {
                    throw MailExceptionCode.UNSUPPORTED_MIME_TYPE.create(ct.toString());
                }
                textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                multipart.addBodyPart(textPart, 0);
                MessageUtility.setContent(multipart, msg);
                // msg.setContent(multipart);
            }
            /*
             * Disposition notification
             */
            if (requestReadReceipt) {
                msg.setHeader(HEADER_DISPNOTTO, fromAddr);
            }
            /*
             * Additional headers
             */
            for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
                msg.setHeader(header.getKey(), header.getValue());
            }
            /*
             * Set priority
             */
            msg.setHeader(HEADER_XPRIORITY, VALUE_PRIORITYNROM);
            /*
             * Set mailer TODO: Read in mailer from file
             */
            msg.setHeader(HEADER_X_MAILER, "Open-Xchange Mailer v" + Version.getInstance().getVersionString());
            /*
             * Set organization
             */
            try {
                final String organization = ServerServiceRegistry.getInstance().getService(ContactService.class).getOrganization(session);
                if (null != organization && 0 < organization.length()) {
                    msg.setHeader(HEADER_ORGANIZATION, organization);
                }
            } catch (final Exception e) {
                LOG.warn("Header \"Organization\" could not be set", e);
            }
            /*
             * Set ox reference
             */
            if (internalRecipient && folderId != DONT_SET) {
                msg.setHeader(HEADER_XOXREMINDER, new StringBuilder().append(objectId).append(',').append(folderId).append(',').append(
                    module).toString());
            }
            /*
             * Set sent date in UTC time
             */
            if (msg.getSentDate() == null) {
                final MailDateFormat mdf = MimeMessageUtility.getMailDateFormat(session);
                synchronized (mdf) {
                    msg.setHeader("Date", mdf.format(new Date()));
                }
            }
            /*
             * X-Open-Xchange-Module
             */
            if (internalRecipient) {
                msg.setHeader(HEADER_X_OX_MODULE, Types.APPOINTMENT == module ? "Appointments" : "Tasks");
            }
            /*
             * X-Open-Xchange-Type
             */
            if (internalRecipient && type != null) {
                msg.setHeader(HEADER_X_OX_TYPE, type);
            }
            /*
             * X-Open-Xchange-Object
             */
            if (internalRecipient) {
                msg.setHeader(HEADER_X_OX_OBJECT, Integer.toString(objectId));
            }

            if (internalRecipient && uid != null) {
                msg.setHeader(HEADER_X_OX_UID, uid);
            }

            if (internalRecipient && recurrenceDatePosition != 0) {
                msg.setHeader(HEADER_X_OX_RECURRENCE_DATE, String.valueOf(recurrenceDatePosition));
            }

            if (autoGenerated) {
                msg.setHeader(HEADER_AUTO_SUBMITTED, VALUE_AUTO_GENERATED);
            }
            saveChangesSafe(msg);
            /*
             * Finally transport mail
             */
            final MailTransport transport = MailTransport.getInstance(session);
            try {
                transport.sendMailMessage(new ContentAwareComposedMailMessage(msg, session, null), ComposeType.NEW);
            } finally {
                transport.close();
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private MimeMessageDataSource writeTo(final MimeMessage msg, final ByteArrayOutputStream bos) throws IOException, MessagingException {
        try {
            msg.writeTo(bos);
            return null;
        } catch (final javax.activation.UnsupportedDataTypeException e) {
            // Check for "no object DCH for MIME type xxxxx/yyyy"
            if (com.openexchange.java.Strings.toLowerCase(e.getMessage()).indexOf("no object dch") >= 0) {
                // Not able to recover from JAF's "no object DCH for MIME type xxxxx/yyyy" error
                // Perform the alternative transport with custom JAF DataHandler
                LOG.warn(e.getMessage().replaceFirst("[dD][cC][hH]", Matcher.quoteReplacement("javax.activation.DataContentHandler")));
                try {
                    final MimeMessageDataSource dataSource = new MimeMessageDataSource(msg);
                    bos.reset();
                    dataSource.writeTo(bos);
                    return dataSource;
                } catch (final Exception ignore) {
                    // Ignore
                }
            }
            throw e;
        }
    }

    private void saveChangesSafe(final MimeMessage mimeMessage) throws OXException {
        final HostnameService hostnameService = ServerServiceRegistry.getInstance().getService(HostnameService.class);
        String hostName;
        if (null == hostnameService) {
            hostName = getHostName();
        } else {
            hostName = hostnameService.getHostname(session.getUserId(), session.getContextId());
        }
        if (null == hostName) {
            hostName = getHostName();
        }
        MimeMessageUtility.saveChanges(mimeMessage, hostName, false);
    }

    private static String getHostName() {
        final String serverName = LogProperties.getLogProperty(LogProperties.Name.GRIZZLY_SERVER_NAME);
        if (null == serverName) {
            return getStaticHostName();
        }
        return serverName;
    }

    private static String getStaticHostName() {
        final UnknownHostException warning = warnSpam;
        if (warning != null) {
            LOG.error("Can't resolve my own hostname, using 'localhost' instead, which is certainly not what you want!", warning);
        }
        return staticHostName;
    }

    public void addBccAddr(final String addr) {
        bccAddrs = addAddr(addr, bccAddrs);
    }

    public String[] getBccAddrs() {
        return bccAddrs;
    }

    public void setBccAddrs(final String[] bccAddrs) {
        this.bccAddrs = bccAddrs;
    }

    public void addCcAddr(final String addr) {
        ccAddrs = addAddr(addr, ccAddrs);
    }

    public String[] getCcAddrs() {
        return ccAddrs;
    }

    public void setCcAddrs(final String[] ccAddrs) {
        this.ccAddrs = ccAddrs;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getFromAddr() {
        return fromAddr;
    }

    public void setFromAddr(final String fromAddr) {
        this.fromAddr = fromAddr;
    }

    public Object getText() {
        return text;
    }

    public void setText(final Object text) {
        this.text = text;
    }

    public void addToAddr(final String addr) {
        toAddrs = addAddr(addr, toAddrs);
    }

    public String[] getToAddrs() {
        return toAddrs;
    }

    public void setToAddrs(final String[] toAddrs) {
        this.toAddrs = toAddrs;
    }

    public boolean isRequestReadReceipt() {
        return requestReadReceipt;
    }

    public void setRequestReadReceipt(final boolean requestReadReceipt) {
        this.requestReadReceipt = requestReadReceipt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setRecurrenceDatePosition(final long time) {
        this.recurrenceDatePosition = time;
    }

    public long getRecurrenceDatePosition() {
        return recurrenceDatePosition;
    }

    private static final String[] addAddr(final String addr, String[] arr) {
        if (arr == null) {
            return new String[] { addr };
        }
        final String[] tmp = arr;
        arr = new String[tmp.length + 1];
        System.arraycopy(tmp, 0, arr, 0, tmp.length);
        arr[arr.length - 1] = addr;
        return arr;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

}
