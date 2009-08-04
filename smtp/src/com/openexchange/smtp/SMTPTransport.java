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

package com.openexchange.smtp;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.mime.utils.MIMEMessageUtility.fold;
import static com.openexchange.mail.mime.utils.MIMEMessageUtility.parseAddressList;
import static com.openexchange.mail.text.TextProcessing.performLineFolding;
import static java.util.regex.Matcher.quoteReplacement;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.ISMTPProperties;
import com.openexchange.smtp.config.MailAccountSMTPProperties;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.smtp.config.SMTPSessionProperties;
import com.openexchange.smtp.dataobjects.SMTPMailMessage;
import com.openexchange.smtp.filler.SMTPMessageFiller;
import com.openexchange.smtp.services.SMTPServiceRegistry;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.sun.mail.smtp.SMTPMessage;

/**
 * {@link SMTPTransport} - The SMTP mail transport.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMTPTransport extends MailTransport {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SMTPTransport.class);

    private static final String CHARENC_ISO_8859_1 = "ISO-8859-1";

    private final Queue<Runnable> pendingInvocations;

    private volatile javax.mail.Session smtpSession;

    private final int accountId;

    private final Session session;

    private final Context ctx;

    private final UserSettingMail usm;

    private volatile SMTPConfig smtpConfig;

    protected SMTPTransport() {
        super();
        accountId = MailAccount.DEFAULT_ID;
        smtpSession = null;
        session = null;
        ctx = null;
        usm = null;
        pendingInvocations = new ConcurrentLinkedQueue<Runnable>();
    }

    /**
     * Constructor
     * 
     * @param session The session
     * @throws MailException If initialization fails
     */
    public SMTPTransport(final Session session) throws MailException {
        this(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Constructor
     * 
     * @param session The session
     * @param accountId The account ID
     * @throws MailException If initialization fails
     */
    public SMTPTransport(final Session session, final int accountId) throws MailException {
        super();
        pendingInvocations = new ConcurrentLinkedQueue<Runnable>();
        this.session = session;
        this.accountId = accountId;
        if (session == null) {
            /*
             * Dummy instance
             */
            ctx = null;
            usm = null;
        } else {
            try {
                ctx = ContextStorage.getStorageContext(session.getContextId());
            } catch (final ContextException e) {
                throw new SMTPException(e);
            }
            usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
        }
    }

    private void clearUp() {
        doInvocations();
    }

    @Override
    public void close() {
        clearUp();
    }

    /**
     * Executes all tasks queued for execution
     */
    private void doInvocations() {
        while (!pendingInvocations.isEmpty()) {
            final Runnable task = pendingInvocations.poll();
            if (null != task) {
                task.run();
            }
        }
    }

    /**
     * Executes the given task. This method returns as soon as the task is scheduled, without waiting for it to be executed.
     * 
     * @param task The task to be executed.
     */
    private void invokeLater(final Runnable task) {
        pendingInvocations.offer(task);
    }

    private javax.mail.Session getSMTPSession() throws MailException {
        if (null == smtpSession) {
            synchronized (this) {
                if (null == smtpSession) {
                    final Properties smtpProps = SMTPSessionProperties.getDefaultSessionProperties();
                    final SMTPConfig smtpConfig = getTransportConfig0();
                    /*
                     * Set properties
                     */
                    final ISMTPProperties smtpProperties = smtpConfig.getSMTPProperties();
                    if (smtpProperties.getSmtpLocalhost() != null) {
                        smtpProps.put("mail.smtp.localhost", smtpProperties.getSmtpLocalhost());
                    }
                    if (smtpProperties.getSmtpTimeout() > 0) {
                        smtpProps.put("mail.smtp.timeout", String.valueOf(smtpProperties.getSmtpTimeout()));
                    }
                    if (smtpProperties.getSmtpConnectionTimeout() > 0) {
                        smtpProps.put("mail.smtp.connectiontimeout", String.valueOf(smtpProperties.getSmtpConnectionTimeout()));
                    }
                    smtpProps.put("mail.smtp.auth", smtpProperties.isSmtpAuth() ? "true" : "false");
                    /*
                     * Check if a secure SMTP connection should be established
                     */
                    final String sPort = String.valueOf(smtpConfig.getPort());
                    final String socketFactoryClass = TrustAllSSLSocketFactory.class.getName();
                    if (smtpConfig.isSecure()) {
                        /*
                         * Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected
                         * connection before issuing any login commands.
                         */
                        // smtpProps.put("mail.smtp.starttls.enable", "true");
                        /*
                         * Force use of SSL through specifying the name of the javax.net.SocketFactory interface. This class will be used to
                         * create SMTP sockets.
                         */
                        smtpProps.put("mail.smtp.socketFactory.class", socketFactoryClass);
                        smtpProps.put("mail.smtp.socketFactory.port", sPort);
                        smtpProps.put("mail.smtp.socketFactory.fallback", "false");
                        /*
                         * Specify SSL protocols
                         */
                        smtpProps.put("mail.smtp.ssl.protocols", "SSLv3 TLSv1");
                        smtpProps.put("mail.smtp.ssl", "true");
                        /*
                         * Needed for JavaMail >= 1.4
                         */
                        // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
                    } else {
                        /*
                         * Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected
                         * connection before issuing any login commands.
                         */
                        smtpProps.put("mail.smtp.starttls.enable", "true");
                        /*
                         * Specify the javax.net.ssl.SSLSocketFactory class, this class will be used to create SMTP SSL sockets if TLS
                         * handshake says so.
                         */
                        smtpProps.put("mail.smtp.socketFactory.port", sPort);
                        smtpProps.put("mail.smtp.ssl.socketFactory.class", socketFactoryClass);
                        smtpProps.put("mail.smtp.ssl.socketFactory.port", sPort);
                        smtpProps.put("mail.smtp.socketFactory.fallback", "false");
                        /*
                         * Specify SSL protocols
                         */
                        smtpProps.put("mail.smtp.ssl.protocols", "SSLv3 TLSv1");
                        smtpProps.put("mail.smtp.ssl", "true");
                        /*
                         * Needed for JavaMail >= 1.4
                         */
                        // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
                    }
                    /*
                     * Apply host & port to SMTP session
                     */
                    // smtpProps.put(MIMESessionPropertyNames.PROP_SMTPHOST, smtpConfig.getServer());
                    // smtpProps.put(MIMESessionPropertyNames.PROP_SMTPPORT, sPort);
                    smtpSession = javax.mail.Session.getInstance(smtpProps, null);
                }
            }
        }
        return smtpSession;
    }

    @Override
    public SMTPConfig getTransportConfig() throws MailException {
        return getTransportConfig0();
    }

    private SMTPConfig getTransportConfig0() throws MailException {
        if (smtpConfig == null) {
            synchronized (this) {
                if (smtpConfig == null) {
                    smtpConfig = TransportConfig.getTransportConfig(SMTPConfig.class, new SMTPConfig(), session, accountId);
                    smtpConfig.setTransportProperties(createNewMailProperties());
                }
            }
        }
        return smtpConfig;
    }

    private static final String ACK_TEXT = "Reporting-UA: OPEN-XCHANGE - WebMail\r\nFinal-Recipient: rfc822; #FROM#\r\n" + "Original-Message-ID: #MSG ID#\r\nDisposition: manual-action/MDN-sent-manually; displayed\r\n";

    private static final String CT_TEXT_PLAIN = "text/plain; charset=#CS#";

    private static final String CT_READ_ACK = "message/disposition-notification; name=MDNPart1.txt; charset=UTF-8";

    private static final String CD_READ_ACK = "attachment; filename=MDNPart1.txt";

    private static final String MULTI_SUBTYPE_REPORT = "report; report-type=disposition-notification";

    @Override
    public void sendReceiptAck(final MailMessage srcMail, final String fromAddr) throws MailException {
        try {
            clearUp();
            final InternetAddress dispNotification = srcMail.getDispositionNotification();
            if (dispNotification == null) {
                throw new SMTPException(
                    SMTPException.Code.MISSING_NOTIFICATION_HEADER,
                    MessageHeaders.HDR_DISP_TO,
                    Long.valueOf(srcMail.getMailId()));
            }
            final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession());
            final String userMail = UserStorage.getStorageUser(session.getUserId(), ctx).getMail();
            /*
             * Set from
             */
            final String from;
            if (fromAddr == null) {
                if ((usm.getSendAddr() == null) && (userMail == null)) {
                    throw new SMTPException(SMTPException.Code.NO_SEND_ADDRESS_FOUND);
                }
                from = usm.getSendAddr() == null ? userMail : usm.getSendAddr();
            } else {
                from = fromAddr;
            }
            smtpMessage.addFrom(parseAddressList(from, false));
            /*
             * Set to
             */
            smtpMessage.addRecipients(RecipientType.TO, new Address[] { dispNotification });
            /*
             * Set header
             */
            smtpMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, "3 (normal)");
            /*
             * Subject
             */
            final Locale locale = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale();
            final StringHelper strHelper = new StringHelper(locale);
            smtpMessage.setSubject(strHelper.getString(MailStrings.ACK_SUBJECT));
            /*
             * Sent date in UTC time
             */
            {
                final MailDateFormat mdf = MIMEMessageUtility.getMailDateFormat(session);
                synchronized (mdf) {
                    smtpMessage.setHeader("Date", mdf.format(new Date()));
                }
            }
            /*
             * Set common headers
             */
            final SMTPConfig smtpConfig = getTransportConfig0();
            new SMTPMessageFiller(smtpConfig.getSMTPProperties(), session, ctx, usm).setCommonHeaders(smtpMessage);
            /*
             * Compose body
             */
            final String defaultMimeCS = MailProperties.getInstance().getDefaultMimeCharset();
            final ContentType ct = new ContentType(CT_TEXT_PLAIN.replaceFirst("#CS#", defaultMimeCS));
            final Multipart mixedMultipart = new MimeMultipart(MULTI_SUBTYPE_REPORT);
            /*
             * Define text content
             */
            final Date sentDate = srcMail.getSentDate();
            {
                final MimeBodyPart text = new MimeBodyPart();
                text.setText(
                    performLineFolding(
                        strHelper.getString(MailStrings.ACK_NOTIFICATION_TEXT).replaceFirst(
                            "#DATE#",
                            sentDate == null ? "" : quoteReplacement(DateFormat.getDateInstance(DateFormat.LONG, locale).format(sentDate))).replaceFirst(
                            "#RECIPIENT#",
                            quoteReplacement(from)).replaceFirst("#SUBJECT#", quoteReplacement(srcMail.getSubject())),
                        usm.getAutoLinebreak()),
                    defaultMimeCS);
                text.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, fold(14, ct.toString()));
                mixedMultipart.addBodyPart(text);
            }
            /*
             * Define ack
             */
            ct.setContentType(CT_READ_ACK);
            {
                final MimeBodyPart ack = new MimeBodyPart();
                final String msgId = srcMail.getFirstHeader(MessageHeaders.HDR_MESSAGE_ID);
                ack.setText(strHelper.getString(ACK_TEXT).replaceFirst("#FROM#", quoteReplacement(from)).replaceFirst(
                    "#MSG ID#",
                    quoteReplacement(msgId)), defaultMimeCS);
                ack.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                ack.setHeader(MessageHeaders.HDR_CONTENT_TYPE, fold(14, ct.toString()));
                ack.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, CD_READ_ACK);
                mixedMultipart.addBodyPart(ack);
            }
            /*
             * Set message content
             */
            smtpMessage.setContent(mixedMultipart);
            /*
             * Transport message
             */
            final long start = System.currentTimeMillis();
            final Transport transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
            if (smtpConfig.getSMTPProperties().isSmtpAuth()) {
                transport.connect(
                    smtpConfig.getServer(),
                    smtpConfig.getPort(),
                    smtpConfig.getLogin(),
                    encodePassword(smtpConfig.getPassword()));
            } else {
                transport.connect();
            }
            try {
                smtpMessage.saveChanges();
                transport.sendMessage(smtpMessage, smtpMessage.getAllRecipients());
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            } finally {
                transport.close();
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    @Override
    public MailMessage sendRawMessage(final byte[] asciiBytes, final Address[] allRecipients) throws MailException {
        try {
            clearUp();
            final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession(), new UnsynchronizedByteArrayInputStream(asciiBytes));
            /*
             * Check recipients
             */
            final Address[] recipients = allRecipients == null ? smtpMessage.getAllRecipients() : allRecipients;
            if ((recipients == null) || (recipients.length == 0)) {
                throw new SMTPException(SMTPException.Code.MISSING_RECIPIENTS);
            }
            try {
                final long start = System.currentTimeMillis();
                final Transport transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
                if (getTransportConfig0().getSMTPProperties().isSmtpAuth()) {
                    final SMTPConfig config = getTransportConfig0();
                    transport.connect(config.getServer(), config.getPort(), config.getLogin(), encodePassword(config.getPassword()));
                } else {
                    transport.connect();
                }
                try {
                    smtpMessage.saveChanges();
                    transport.sendMessage(smtpMessage, recipients);
                    mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                } finally {
                    transport.close();
                }
            } catch (final MessagingException e) {
                throw MIMEMailException.handleMessagingException(e);
            }
            return MIMEMessageConverter.convertMessage(smtpMessage);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    @Override
    public MailMessage sendMailMessage(final ComposedMailMessage composedMail, final ComposeType sendType, final Address[] allRecipients) throws MailException {
        try {
            clearUp();
            final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession());
            /*
             * Fill message dependent on send type
             */
            final long startPrep = System.currentTimeMillis();
            final SMTPConfig smtpConfig = getTransportConfig0();
            final SMTPMessageFiller smtpFiller = new SMTPMessageFiller(smtpConfig.getSMTPProperties(), session, ctx, usm);
            composedMail.setFiller(smtpFiller);
            try {
                smtpFiller.fillMail((SMTPMailMessage) composedMail, smtpMessage, sendType);

                /*
                 * Check recipients
                 */
                final Address[] recipients;
                if (allRecipients == null) {
                    if (composedMail.hasRecipients()) {
                        recipients = composedMail.getRecipients();
                    } else {
                        recipients = smtpMessage.getAllRecipients();
                    }
                } else {
                    recipients = allRecipients;
                }

                if ((recipients == null) || (recipients.length == 0)) {
                    throw new SMTPException(SMTPException.Code.MISSING_RECIPIENTS);
                }
                smtpFiller.setSendHeaders(composedMail, smtpMessage);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder(128).append("SMTP mail prepared for transport in ").append(
                        System.currentTimeMillis() - startPrep).append("msec").toString());
                }

                final long start = System.currentTimeMillis();
                final Transport transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
                if (smtpConfig.getSMTPProperties().isSmtpAuth()) {
                    final String encPass = encodePassword(smtpConfig.getPassword());
                    transport.connect(smtpConfig.getServer(), smtpConfig.getPort(), smtpConfig.getLogin(), encPass);
                } else {
                    transport.connect();
                }
                try {
                    smtpMessage.saveChanges();
                    /*
                     * TODO: Do encryption here
                     */
                    transport.sendMessage(smtpMessage, recipients);
                    mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                } finally {
                    transport.close();
                }
            } finally {
                invokeLater(new MailCleanerTask(composedMail));
            }
            return MIMEMessageConverter.convertMessage(smtpMessage);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        } catch (final IOException e) {
            throw new SMTPException(SMTPException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    private String encodePassword(final String password) throws MailException {
        String tmpPass = password;
        if (password != null) {
            try {
                tmpPass = new String(password.getBytes(getTransportConfig0().getSMTPProperties().getSmtpAuthEnc()), CHARENC_ISO_8859_1);
            } catch (final UnsupportedEncodingException e) {
                LOG.error("Unsupported encoding in a message detected and monitored: \"" + e.getMessage() + '"', e);
                mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            }
        }
        return tmpPass;
    }

    @Override
    protected void shutdown() throws MailException {
        SMTPSessionProperties.resetDefaultSessionProperties();
    }

    @Override
    protected void startup() throws MailException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("SMTPTransport.startup()");
        }
    }

    private static final class MailCleanerTask implements Runnable {

        private final ComposedMailMessage composedMail;

        public MailCleanerTask(final ComposedMailMessage composedMail) {
            super();
            this.composedMail = composedMail;
        }

        public void run() {
            composedMail.cleanUp();
        }

    }

    @Override
    public void ping() throws MailException {
        // Connect to SMTP server
        final Transport transport;
        try {
            transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
        } catch (final NoSuchProviderException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
        boolean close = false;
        try {
            final SMTPConfig config = getTransportConfig0();
            if (config.getSMTPProperties().isSmtpAuth()) {
                final String encPass = encodePassword(config.getPassword());
                transport.connect(config.getServer(), config.getPort(), config.getLogin(), encPass);
                close = true;
            } else {
                transport.connect();
                close = true;
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        } finally {
            if (close) {
                try {
                    transport.close();
                } catch (final MessagingException e) {
                    LOG.error("Closing SMTP transport failed.", e);
                }
            }
        }
    }

    @Override
    protected ITransportProperties createNewMailProperties() throws MailException {
        try {
            final MailAccountStorageService storageService = SMTPServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            return new MailAccountSMTPProperties(storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()));
        } catch (final ServiceException e) {
            throw new MailException(e);
        } catch (final MailAccountException e) {
            throw new MailException(e);
        }
    }

}
