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

package com.openexchange.groupware.container.mail;

import static com.openexchange.mail.mime.utils.MIMEMessageUtility.fold;
import static com.openexchange.mail.mime.utils.MIMEMessageUtility.parseAddressList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.datasource.FileDataSource;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.server.impl.Version;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * MailObject
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailObject {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailObject.class);

    public static final int DONT_SET = -2;

    private String fromAddr;

    private String[] toAddrs;

    private String[] ccAddrs;

    private String[] bccAddrs;

    private String subject;

    private String text;

    private String contentType;

    private boolean requestReadReceipt;

    private final Session session;

    private final int objectId;

    private final int folderId;

    private final int module;

    private final String type;

    private Multipart multipart;

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
        this.session = session;
        this.objectId = objectId;
        this.folderId = folderId;
        this.module = module;
        this.type = type;
    }

    private final void validateMailObject() throws MailException {
        if (fromAddr == null || fromAddr.length() == 0) {
            throw new MailException(MailException.Code.MISSING_FIELD, "From");
        } else if (toAddrs == null || toAddrs.length == 0) {
            throw new MailException(MailException.Code.MISSING_FIELD, "To");
        } else if (contentType == null || contentType.length() == 0) {
            throw new MailException(MailException.Code.MISSING_FIELD, "Content-Type");
        } else if (subject == null) {
            throw new MailException(MailException.Code.MISSING_FIELD, "Subject");
        } else if (text == null) {
            throw new MailException(MailException.Code.MISSING_FIELD, "Text");
        }
    }

    /**
     * Adds a file attachment to this mail object.
     * 
     * @param contentType The content type (incl. charset parameter)
     * @param file The file to attach
     * @throws MailException If file attachment cannot be added
     */
    public void addFileAttachment(final ContentType contentType, final File file) throws MailException {
        /*
         * Determine proper content type
         */
        final String fileName = file.getName();
        final ContentType ct = new ContentType();
        ct.setContentType(contentType);
        if (ct.isMimeType(MIMETypes.MIME_APPL_OCTET)) {
            /*
             * Try to determine MIME type
             */
            final String ctStr = MIMEType2ExtMap.getContentType(fileName);
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
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, fold(14, ct.toString()));
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
                    disp = fold(21, contentDisposition.toString());
                }
                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, disp);
            } else {
                final ContentDisposition contentDisposition = new ContentDisposition(disposition);
                contentDisposition.setDisposition(Part.ATTACHMENT);
                if (fileName != null && !contentDisposition.containsFilenameParameter()) {
                    contentDisposition.setFilenameParameter(fileName);
                }
                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, fold(21, contentDisposition.toString()));
            }
            /*
             * Add to multipart
             */
            if (multipart == null) {
                multipart = new MimeMultipart("mixed");
            }
            multipart.addBodyPart(bodyPart);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    /**
     * Adds a file attachment to this mail object.
     * 
     * @param contentType The content type (incl. charset parameter)
     * @param fileName The attachment's file name
     * @param inputStream The attachment's data as an input stream
     * @throws MailException If file attachment cannot be added
     */
    public void addFileAttachment(final ContentType contentType, final String fileName, final InputStream inputStream) throws MailException {
        /*
         * Determine proper content type
         */
        final ContentType ct = new ContentType();
        ct.setContentType(contentType);
        if (ct.isMimeType(MIMETypes.MIME_APPL_OCTET) && fileName != null) {
            /*
             * Try to determine MIME type
             */
            final String ctStr = MIMEType2ExtMap.getContentType(fileName);
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
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, fold(14, ct.toString()));
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
                    disp = fold(21, contentDisposition.toString());
                }
                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, disp);
            } else {
                final ContentDisposition contentDisposition = new ContentDisposition(disposition);
                contentDisposition.setDisposition(Part.ATTACHMENT);
                if (fileName != null && !contentDisposition.containsFilenameParameter()) {
                    contentDisposition.setFilenameParameter(fileName);
                }
                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, fold(21, contentDisposition.toString()));
            }
            /*
             * Add to multipart
             */
            if (multipart == null) {
                multipart = new MimeMultipart("mixed");
            }
            multipart.addBodyPart(bodyPart);
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    private final static String HEADER_XPRIORITY = "X-Priority";

    private final static String HEADER_DISPNOTTO = "Disposition-Notification-Toy";

    private final static String HEADER_XOXREMINDER = "X-OX-Reminder";

    private final static String VALUE_PRIORITYNROM = "3 (normal)";

    private final static String HEADER_ORGANIZATION = "Organization";

    private final static String HEADER_X_MAILER = "X-Mailer";

    private final static String HEADER_X_OX_MODULE = "X-Open-Xchange-Module";

    private final static String HEADER_X_OX_TYPE = "X-Open-Xchange-Type";

    private final static String HEADER_X_OX_OBJECT = "X-Open-Xchange-Object";

    public final void send() throws MailException {
        try {
            validateMailObject();
            final MimeMessage msg = new MimeMessage(MIMEDefaultSession.getDefaultSession());
            /*
             * Set from
             */
            InternetAddress[] internetAddrs = parseAddressList(fromAddr, false);
            msg.setFrom(internetAddrs[0]);
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
            /*
             * Examine message's content type
             */
            if (!"text".equalsIgnoreCase(ct.getPrimaryType())) {
                throw new MailException(MailException.Code.UNSUPPORTED_MIME_TYPE, ct.toString());
            }
            /*
             * Set content and its type
             */
            if (multipart == null) {
                if ("html".equalsIgnoreCase(ct.getSubType()) || "htm".equalsIgnoreCase(ct.getSubType())) {
                    msg.setContent(text, ct.toString());
                } else if ("plain".equalsIgnoreCase(ct.getSubType()) || "enriched".equalsIgnoreCase(ct.getSubType())) {
                    if (!ct.containsCharsetParameter()) {
                        msg.setText(text);
                    } else {
                        msg.setText(text, ct.getCharsetParameter());
                    }
                } else {
                    throw new MailException(MailException.Code.UNSUPPORTED_MIME_TYPE, ct.toString());
                }
            } else {
                final MimeBodyPart textPart = new MimeBodyPart();
                if ("html".equalsIgnoreCase(ct.getSubType()) || "htm".equalsIgnoreCase(ct.getSubType())) {
                    textPart.setContent(text, ct.toString());
                } else if ("plain".equalsIgnoreCase(ct.getSubType()) || "enriched".equalsIgnoreCase(ct.getSubType())) {
                    if (!ct.containsCharsetParameter()) {
                        textPart.setText(text);
                    } else {
                        textPart.setText(text, ct.getCharsetParameter());
                    }
                } else {
                    throw new MailException(MailException.Code.UNSUPPORTED_MIME_TYPE, ct.toString());
                }
                textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, fold(14, ct.toString()));
                multipart.addBodyPart(textPart, 0);
                msg.setContent(multipart);
            }
            /*
             * Disposition notification
             */
            if (requestReadReceipt) {
                msg.setHeader(HEADER_DISPNOTTO, fromAddr);
            }
            /*
             * Set priority
             */
            msg.setHeader(HEADER_XPRIORITY, VALUE_PRIORITYNROM);
            /*
             * Set mailer TODO: Read in mailer from file
             */
            msg.setHeader(HEADER_X_MAILER, "Open-Xchange Mailer v" + Version.getVersionString());
            /*
             * Set organization
             */
            try {
                final Object org = session.getParameter(MailSessionParameterNames.PARAM_ORGANIZATION_HDR);
                if (null == org) {
                    /*
                     * Get context's admin contact object
                     */
                    final Context ctx = ContextStorage.getStorageContext(session);
                    final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                        ContactInterfaceDiscoveryService.class).newContactInterface(FolderObject.SYSTEM_LDAP_FOLDER_ID, session);
                    final Contact c = contactInterface.getObjectById(
                        UserStorage.getInstance().getUser(ctx.getMailadmin(), ctx).getContactId(),
                        FolderObject.SYSTEM_LDAP_FOLDER_ID);
                    if (null != c && c.getCompany() != null && c.getCompany().length() > 0) {
                        session.setParameter(MailSessionParameterNames.PARAM_ORGANIZATION_HDR, c.getCompany());
                        msg.setHeader(HEADER_ORGANIZATION, c.getCompany());
                    } else {
                        session.setParameter(HEADER_ORGANIZATION, "null");
                    }
                } else if (!"null".equals(org.toString())) {
                    /*
                     * Apply value from session parameter
                     */
                    msg.setHeader(HEADER_ORGANIZATION, org.toString());
                }
            } catch (final Exception e) {
                LOG.warn("Header \"Organization\" could not be set", e);
            }
            /*
             * Set ox reference
             */
            if (folderId != DONT_SET) {
                msg.setHeader(HEADER_XOXREMINDER, new StringBuilder().append(objectId).append(',').append(folderId).append(',').append(
                    module).toString());
            }
            /*
             * Set sent date in UTC time
             */
            if (msg.getSentDate() == null) {
                msg.setSentDate(new Date());
            }
            /*
             * X-Open-Xchange-Module
             */
            msg.setHeader(HEADER_X_OX_MODULE, Types.APPOINTMENT == module ? "Appointments" : "Tasks");
            /*
             * X-Open-Xchange-Type
             */
            if (type != null) {
                msg.setHeader(HEADER_X_OX_TYPE, type);
            }
            /*
             * X-Open-Xchange-Object
             */
            msg.setHeader(HEADER_X_OX_OBJECT, String.valueOf(objectId));
            /*
             * Finally transport mail
             */
            final MailTransport transport = MailTransport.getInstance(session);
            try {
                final UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream();
                msg.writeTo(bos);
                transport.sendRawMessage(bos.toByteArray());
            } finally {
                transport.close();
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
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

    public String getText() {
        return text;
    }

    public void setText(final String text) {
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

}
