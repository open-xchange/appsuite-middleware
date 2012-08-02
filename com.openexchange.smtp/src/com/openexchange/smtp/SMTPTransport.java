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

package com.openexchange.smtp;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.mime.converters.MimeMessageConverter.saveChanges;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import static com.openexchange.mail.text.TextProcessing.performLineFolding;
import static java.util.regex.Matcher.quoteReplacement;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Transport;
import javax.mail.internet.IDNA;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.Subject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Charsets;
import com.openexchange.java.Java7ConcurrentLinkedQueue;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeHeaderNameChecker;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.ISMTPProperties;
import com.openexchange.smtp.config.MailAccountSMTPProperties;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.smtp.config.SMTPSessionProperties;
import com.openexchange.smtp.filler.SMTPMessageFiller;
import com.openexchange.smtp.services.SMTPServiceRegistry;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;
import com.sun.mail.smtp.SMTPMessage;

/**
 * {@link SMTPTransport} - The SMTP mail transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMTPTransport extends MailTransport {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(SMTPTransport.class));

    private static final class SaslSmtpLoginAction implements PrivilegedExceptionAction<Object> {

        private final Transport transport;
        private final String server;
        private final int port;
        private final String login;
        private final String pw;

        protected SaslSmtpLoginAction(final Transport transport, final String server, final int port, final String login, final String pw) {
            super();
            this.transport = transport;
            this.server = server;
            this.port = port;
            this.login = login;
            this.pw = pw;
        }

        @Override
        public Object run() throws MessagingException {
            transport.connect(server, port, login, pw);
            return null;
        }

    }

    // private static final String CHARENC_ISO_8859_1 = "ISO-8859-1";

    private static final String KERBEROS_SESSION_SUBJECT = "kerberosSubject";

    private static volatile String staticHostName;

    private static volatile UnknownHostException warnSpam;

    static {
        try {
            staticHostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            staticHostName = "localhost";
            warnSpam = e;
        }
    }

    private final Queue<Runnable> pendingInvocations;

    private volatile javax.mail.Session smtpSession;

    private final int accountId;

    private final Session session;

    private transient Subject kerberosSubject;

    private final Context ctx;

    private final UserSettingMail usm;

    private volatile SMTPConfig cachedSmtpConfig;

    protected SMTPTransport() {
        super();
        accountId = MailAccount.DEFAULT_ID;
        smtpSession = null;
        session = null;
        ctx = null;
        usm = null;
        pendingInvocations = new Java7ConcurrentLinkedQueue<Runnable>();
    }

    /**
     * Constructor
     *
     * @param session The session
     * @throws OXException If initialization fails
     */
    public SMTPTransport(final Session session) throws OXException {
        this(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Constructor
     *
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If initialization fails
     */
    public SMTPTransport(final Session session, final int accountId) throws OXException {
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
            } catch (final OXException e) {
                throw e;
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

    /**
     * Checks if Kerberos authentication is supposed to be performed.
     * 
     * @return <code>true</code> for Kerberos authentication; otherwise <code>false</code>
     */
    private boolean isKerberosAuth() {
        return MailAccount.DEFAULT_ID == accountId && null != kerberosSubject;
    }

    private static void handlePrivilegedActionException(final PrivilegedActionException e) throws MessagingException, OXException {
        if (null == e) {
            return;
        }
        final Exception cause = e.getException();
        if (null == cause) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e.getCause(), e.getMessage());
        }
        if (cause instanceof MessagingException) {
            throw (MessagingException) cause;
        }
        if (cause instanceof OXException) {
            throw (OXException) cause;
        }
        throw MailExceptionCode.UNEXPECTED_ERROR.create(cause, cause.getMessage());
    }

    private javax.mail.Session getSMTPSession() throws OXException {
        if (null == smtpSession) {
            synchronized (this) {
                if (null == smtpSession) {
                    final Properties smtpProps = SMTPSessionProperties.getDefaultSessionProperties();
                    final SMTPConfig smtpConfig = getTransportConfig0();
                    /*
                     * Set properties
                     */
                    final ISMTPProperties smtpProperties = smtpConfig.getSMTPProperties();
                    /*
                     * Check for Kerberos subject
                     */
                    this.kerberosSubject = (Subject) session.getParameter(KERBEROS_SESSION_SUBJECT);
                    final boolean kerberosAuth = isKerberosAuth();
                    if (kerberosAuth) {
                        smtpProps.put("mail.smtp.auth", "true"); 
                        smtpProps.put("mail.smtp.sasl.enable", "true");
                        smtpProps.put("mail.smtp.sasl.authorizationid", smtpConfig.getLogin());
                        smtpProps.put("mail.smtp.sasl.mechanisms", (kerberosAuth ? "GSSAPI" : "PLAIN"));
                    } else {
                        smtpProps.put("mail.smtp.auth", smtpProperties.isSmtpAuth() ? "true" : "false");
                    }
                    /*
                     * Localhost, & timeouts
                     */
                    final String smtpLocalhost = smtpProperties.getSmtpLocalhost();
                    if (smtpLocalhost != null) {
                        smtpProps.put("mail.smtp.localhost", smtpLocalhost);
                    }
                    if (smtpProperties.getSmtpTimeout() > 0) {
                        smtpProps.put("mail.smtp.timeout", Integer.toString(smtpProperties.getSmtpTimeout()));
                    }
                    if (smtpProperties.getSmtpConnectionTimeout() > 0) {
                        smtpProps.put("mail.smtp.connectiontimeout", Integer.toString(smtpProperties.getSmtpConnectionTimeout()));
                    }
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
                        // smtpProps.put("mail.smtp.ssl.protocols", "SSLv3 TLSv1");
                        // smtpProps.put("mail.smtp.ssl", "true");
                        /*
                         * Needed for JavaMail >= 1.4
                         */
                        // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
                    } else {
                        /*
                         * Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected
                         * connection before issuing any login commands.
                         */
                        String hostName = smtpLocalhost;
                        if (null == hostName) {
                            final HostnameService hostnameService = SMTPServiceRegistry.getServiceRegistry().getService(HostnameService.class);
                            if (null == hostnameService) {
                                hostName = getHostName();
                            } else {
                                hostName = hostnameService.getHostname(session.getUserId(), session.getContextId());
                            }
                            if (null == hostName) {
                                hostName = getHostName();
                            }
                        }
                        try {
                            final InetSocketAddress address = new InetSocketAddress(IDNA.toASCII(smtpConfig.getServer()), smtpConfig.getPort());
                            final Map<String, String> capabilities = SMTPCapabilityCache.getCapabilities(address, smtpConfig.isSecure(), smtpProperties, hostName);
                            if (capabilities.containsKey("STARTTLS")) {
                                smtpProps.put("mail.smtp.starttls.enable", "true");
                            }
                        } catch (final IOException e) {
                            smtpProps.put("mail.smtp.starttls.enable", "true");
                        }
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
                        // smtpProps.put("mail.smtp.ssl", "true");
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
    public SMTPConfig getTransportConfig() throws OXException {
        return getTransportConfig0();
    }

    private SMTPConfig getTransportConfig0() throws OXException {
        if (cachedSmtpConfig == null) {
            synchronized (this) {
                if (cachedSmtpConfig == null) {
                    cachedSmtpConfig = TransportConfig.getTransportConfig(SMTPConfig.class, new SMTPConfig(), session, accountId);
                    cachedSmtpConfig.setTransportProperties(createNewMailProperties());
                }
            }
        }
        return cachedSmtpConfig;
    }

    private static final String ACK_TEXT =
        "Reporting-UA: OPEN-XCHANGE - WebMail\r\nFinal-Recipient: rfc822; #FROM#\r\n" + "Original-Message-ID: #MSG ID#\r\nDisposition: manual-action/MDN-sent-manually; displayed\r\n";

    private static final String CT_TEXT_PLAIN = "text/plain; charset=#CS#";

    private static final String CT_READ_ACK = "message/disposition-notification; name=MDNPart1.txt; charset=UTF-8";

    private static final String CD_READ_ACK = "attachment; filename=MDNPart1.txt";

    private static final String MULTI_SUBTYPE_REPORT = "report; report-type=disposition-notification";

    @Override
    public void sendReceiptAck(final MailMessage srcMail, final String fromAddr) throws OXException {
        SMTPConfig smtpConfig = null;
        try {
            final InternetAddress dispNotification = srcMail.getDispositionNotification();
            if (dispNotification == null) {
                throw SMTPExceptionCode.MISSING_NOTIFICATION_HEADER.create(MessageHeaders.HDR_DISP_TO, Long.valueOf(srcMail.getMailId()));
            }
            final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession());
            final String userMail = UserStorage.getStorageUser(session.getUserId(), ctx).getMail();
            /*
             * Set from
             */
            final String from;
            if (fromAddr == null) {
                if ((usm.getSendAddr() == null) && (userMail == null)) {
                    throw SMTPExceptionCode.NO_SEND_ADDRESS_FOUND.create();
                }
                from = usm.getSendAddr() == null ? userMail : usm.getSendAddr();
            } else {
                from = fromAddr;
            }
            smtpMessage.addFrom(parseAddressList(from, false));
            /*
             * Set to
             */
            final Address[] recipients = new Address[] { dispNotification };
            checkRecipients(recipients);
            smtpMessage.addRecipients(RecipientType.TO, recipients);
            /*
             * Set header
             */
            smtpMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, "3 (normal)");
            smtpMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Medium");
            /*
             * Subject
             */
            final Locale locale = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale();
            final StringHelper strHelper = StringHelper.valueOf(locale);
            smtpMessage.setSubject(strHelper.getString(MailStrings.ACK_SUBJECT));
            /*
             * Sent date in UTC time
             */
            {
                final MailDateFormat mdf = MimeMessageUtility.getMailDateFormat(session);
                synchronized (mdf) {
                    smtpMessage.setHeader("Date", mdf.format(new Date()));
                }
            }
            /*
             * Set common headers
             */
            smtpConfig = getTransportConfig0();
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
                text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                mixedMultipart.addBodyPart(text);
            }
            /*
             * Define ack
             */
            ct.setContentType(CT_READ_ACK);
            {
                final MimeBodyPart ack = new MimeBodyPart();
                final String msgId = srcMail.getFirstHeader(MessageHeaders.HDR_MESSAGE_ID);
                ack.setText(
                    strHelper.getString(ACK_TEXT).replaceFirst("#FROM#", quoteReplacement(from)).replaceFirst(
                        "#MSG ID#",
                        quoteReplacement(msgId)),
                    defaultMimeCS);
                ack.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                ack.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
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
            try {
                final String server = IDNA.toASCII(smtpConfig.getServer());
                final int port = smtpConfig.getPort();
                if (smtpConfig.getSMTPProperties().isSmtpAuth()) {
                    if (isKerberosAuth()) {
                        try {
                            Subject.doAs(kerberosSubject, new SaslSmtpLoginAction(
                                transport,
                                server,
                                port,
                                smtpConfig.getLogin(),
                                encodePassword(smtpConfig.getPassword())));
                        } catch (final PrivilegedActionException e) {
                            handlePrivilegedActionException(e);
                        }
                    } else {
                        transport.connect(server, port, smtpConfig.getLogin(), encodePassword(smtpConfig.getPassword()));
                    }
                } else {
                    transport.connect(server, port, null, null);
                }
            } catch (final javax.mail.AuthenticationFailedException e) {
                throw MimeMailExceptionCode.TRANSPORT_INVALID_CREDENTIALS.create(e, smtpConfig.getServer(), e.getMessage());
            }
            try {
                saveChangesSafe(smtpMessage);
                transport.sendMessage(smtpMessage, smtpMessage.getAllRecipients());
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            } finally {
                transport.close();
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, smtpConfig);
        }
    }

    @Override
    public MailMessage sendRawMessage(final byte[] asciiBytes, final Address[] allRecipients) throws OXException {
        final SMTPConfig smtpConfig = getTransportConfig0();
        try {
            final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession(), MimeHeaderNameChecker.sanitizeHeaderNames(asciiBytes));
            smtpMessage.removeHeader("x-original-headers");
            /*
             * Check recipients
             */
            final Address[] recipients = allRecipients == null ? smtpMessage.getAllRecipients() : allRecipients;
            checkRecipients(recipients);
            try {
                final long start = System.currentTimeMillis();
                final Transport transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
                try {
                    final String server = IDNA.toASCII(smtpConfig.getServer());
                    final int port = smtpConfig.getPort();
                    if (smtpConfig.getSMTPProperties().isSmtpAuth()) {
                        if (isKerberosAuth()) {
                            try {
                                Subject.doAs(kerberosSubject, new SaslSmtpLoginAction(
                                    transport,
                                    server,
                                    port,
                                    smtpConfig.getLogin(),
                                    encodePassword(smtpConfig.getPassword())));
                            } catch (final PrivilegedActionException e) {
                                handlePrivilegedActionException(e);
                            }
                        } else {
                            transport.connect(server, port, smtpConfig.getLogin(), encodePassword(smtpConfig.getPassword()));
                        }
                    } else {
                        transport.connect(server, port, null, null);
                    }
                } catch (final javax.mail.AuthenticationFailedException e) {
                    throw MimeMailExceptionCode.TRANSPORT_INVALID_CREDENTIALS.create(e, smtpConfig.getServer(), e.getMessage());
                }
                try {
                    saveChangesSafe(smtpMessage);
                    transport.sendMessage(smtpMessage, recipients);
                    mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                } finally {
                    transport.close();
                }
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e, smtpConfig);
            }
            return MimeMessageConverter.convertMessage(smtpMessage);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, smtpConfig);
        }
    }

    @Override
    public MailMessage sendMailMessage(final ComposedMailMessage composedMail, final ComposeType sendType, final Address[] allRecipients) throws OXException {
        final SMTPConfig smtpConfig = getTransportConfig0();
        try {
            final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession());
            /*
             * Fill message dependent on send type
             */
            final long startPrep = System.currentTimeMillis();
            final SMTPMessageFiller smtpFiller = new SMTPMessageFiller(smtpConfig.getSMTPProperties(), session, ctx, usm);
            composedMail.setFiller(smtpFiller);
            try {
                smtpFiller.fillMail(composedMail, smtpMessage, sendType);

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
                checkRecipients(recipients);
                smtpFiller.setSendHeaders(composedMail, smtpMessage);
                /*
                 * Drop special "x-original-headers" header
                 */
                smtpMessage.removeHeader("x-original-headers");
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder(128).append("SMTP mail prepared for transport in ").append(
                        System.currentTimeMillis() - startPrep).append("msec").toString());
                }
                final long start = System.currentTimeMillis();
                final Transport transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
                try {
                    final String server = IDNA.toASCII(smtpConfig.getServer());
                    final int port = smtpConfig.getPort();
                    if (smtpConfig.getSMTPProperties().isSmtpAuth()) {
                        if (isKerberosAuth()) {
                            try {
                                Subject.doAs(kerberosSubject, new SaslSmtpLoginAction(
                                    transport,
                                    server,
                                    port,
                                    smtpConfig.getLogin(),
                                    encodePassword(smtpConfig.getPassword())));
                            } catch (final PrivilegedActionException e) {
                                handlePrivilegedActionException(e);
                            }
                        } else {
                            transport.connect(server, port, smtpConfig.getLogin(), encodePassword(smtpConfig.getPassword()));
                        }
                    } else {
                        transport.connect(server, port, null, null);
                    }
                } catch (final javax.mail.AuthenticationFailedException e) {
                    throw MimeMailExceptionCode.TRANSPORT_INVALID_CREDENTIALS.create(e, smtpConfig.getServer(), e.getMessage());
                }
                try {
                    saveChangesSafe(smtpMessage);
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
            return MimeMessageConverter.convertMessage(smtpMessage);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, smtpConfig);
        } catch (final IOException e) {
            throw SMTPExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private String encodePassword(final String password) throws OXException {
        String tmpPass = password;
        if (password != null) {
            try {
                tmpPass = new String(password.getBytes(Charsets.forName(getTransportConfig0().getSMTPProperties().getSmtpAuthEnc())), Charsets.ISO_8859_1);
            } catch (final UnsupportedCharsetException e) {
                LOG.error("Unsupported encoding in a message detected and monitored: \"" + e.getMessage() + '"', e);
                mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            }
        }
        return tmpPass;
    }

    @Override
    protected void shutdown() {
        SMTPSessionProperties.resetDefaultSessionProperties();
        SMTPCapabilityCache.tearDown();
    }

    @Override
    protected void startup() {
        SMTPCapabilityCache.init();
    }

    private static void checkRecipients(final Address[] recipients) throws OXException {
        if ((recipients == null) || (recipients.length == 0)) {
            throw SMTPExceptionCode.MISSING_RECIPIENTS.create();
        }
        final ConfigurationService service = SMTPServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        if (null != service) {
            final Filter filter = service.getFilterFromProperty("com.openexchange.mail.transport.redirectWhitelist");
            if (null != filter) {
                for (final Address address : recipients) {
                    final InternetAddress internetAddress = (InternetAddress) address;
                    if (!filter.accepts(internetAddress.getAddress())) {
                        throw SMTPExceptionCode.RECIPIENT_NOT_ALLOWED.create(internetAddress.toUnicodeString());
                    }
                }
            }
        }
    }

    private static final class MailCleanerTask implements Runnable {

        private final ComposedMailMessage composedMail;

        public MailCleanerTask(final ComposedMailMessage composedMail) {
            super();
            this.composedMail = composedMail;
        }

        @Override
        public void run() {
            composedMail.cleanUp();
        }

    }

    @Override
    public void ping() throws OXException {
        // Connect to SMTP server
        final Transport transport;
        try {
            transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
        } catch (final NoSuchProviderException e) {
            throw MimeMailException.handleMessagingException(e);
        }
        boolean close = false;
        final SMTPConfig config = getTransportConfig0();
        try {
            try {
                final String server = IDNA.toASCII(config.getServer());
                final int port = config.getPort();
                if (config.getSMTPProperties().isSmtpAuth()) {
                    if (isKerberosAuth()) {
                        try {
                            Subject.doAs(kerberosSubject, new SaslSmtpLoginAction(
                                transport,
                                server,
                                port,
                                config.getLogin(),
                                encodePassword(config.getPassword())));
                        } catch (final PrivilegedActionException e) {
                            handlePrivilegedActionException(e);
                        }
                    } else {
                        transport.connect(server, port, config.getLogin(), encodePassword(config.getPassword()));
                    }
                } else {
                    transport.connect(server, port, null, null);
                }
                close = true;
            } catch (final javax.mail.AuthenticationFailedException e) {
                throw MimeMailExceptionCode.TRANSPORT_INVALID_CREDENTIALS.create(e, config.getServer(), e.getMessage());
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, config);
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
    protected ITransportProperties createNewMailProperties() throws OXException {
        try {
            final MailAccountStorageService storageService =
                SMTPServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
            return new MailAccountSMTPProperties(storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()));
        } catch (final OXException e) {
            throw e;
        }
    }

    private void saveChangesSafe(final SMTPMessage smtpMessage) throws OXException {
        try {
            saveChanges(smtpMessage);
            /*
             * Change Message-Id header appropriately
             */
            final String messageId = smtpMessage.getHeader("Message-ID", null);
            if (null != messageId) {
                /*
                 * Somewhat of: <744810669.1.1314981157714.JavaMail.username@host.com>
                 */
                final HostnameService hostnameService = SMTPServiceRegistry.getServiceRegistry().getService(HostnameService.class);
                String hostName;
                if (null == hostnameService) {
                    hostName = getHostName();
                } else {
                    hostName = hostnameService.getHostname(session.getUserId(), session.getContextId());
                }
                if (null == hostName) {
                    hostName = getHostName();
                }
                final int pos = messageId.indexOf('@');
                if (pos > 0 ) {
                    final StringBuilder mid = new StringBuilder(messageId.substring(0, pos + 1)).append(hostName);
                    if (messageId.charAt(0) == '<') {
                        mid.append('>');
                    }
                    smtpMessage.setHeader("Message-ID", mid.toString());
                } else {
                    smtpMessage.setHeader("Message-ID", messageId + hostName);
                }
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static String getHostName() {
        final Props logProperties = LogProperties.optLogProperties();
        if (null == logProperties) {
            return getStaticHostName();
        }
        final String serverName = logProperties.get("com.openexchange.ajp13.serverName");
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

}
