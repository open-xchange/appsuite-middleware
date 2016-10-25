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

package com.openexchange.smtp;

import static com.openexchange.mail.MailExceptionCode.getSize;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Provider;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.idn.IDNA;
import javax.security.auth.Subject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;
import com.openexchange.java.util.MsisdnCheck;
import com.openexchange.log.LogProperties;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.log.audit.DefaultAttribute;
import com.openexchange.log.audit.DefaultAttribute.Name;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAware;
import com.openexchange.mail.mime.MimeHeaderNameChecker;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.datasource.MimeMessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.MimeSupport;
import com.openexchange.mail.transport.MtaStatusInfo;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mail.transport.listener.Reply;
import com.openexchange.mail.transport.listener.Result;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.ISMTPProperties;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.smtp.config.SMTPSessionProperties;
import com.openexchange.smtp.filler.SMTPMessageFiller;
import com.openexchange.smtp.services.Services;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;
import com.sun.mail.smtp.JavaSMTPTransport;
import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPTransport;

/**
 * {@link SMTPTransport} - The SMTP mail transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
abstract class AbstractSMTPTransport extends MailTransport implements MimeSupport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractSMTPTransport.class);

    /**
     * The SMTP protocol name.
     */
    protected static final String SMTP = SMTPProvider.PROTOCOL_SMTP.getName();

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

    // -------------------------------------------------------------------------------------------------------------------------------

    /** The account identifier */
    protected final int accountId;

    /** The associated context */
    protected final Context ctx;

    /** The associated session or <code>null</code> */
    protected final Session session;

    private final Queue<Runnable> pendingInvocations;
    private volatile javax.mail.Session smtpSession;
    private volatile SMTPConfig cachedSmtpConfig;
    private User user;
    private transient Subject kerberosSubject;

    /**
     * Legacy dummy constructor. Don't use for production!
     */
    protected AbstractSMTPTransport() {
        super();
        accountId = MailAccount.DEFAULT_ID;
        ctx = null;
        session = null;
        pendingInvocations = new ConcurrentLinkedQueue<Runnable>();
    }

    /**
     * Initializes a new {@link AbstractSMTPTransport}.
     *
     * @param contextId The context identifier
     * @throws OXException If initialization fails
     */
    protected AbstractSMTPTransport(int contextId) throws OXException {
        super();
        this.session = null;
        this.ctx = Services.getService(ContextService.class).getContext(contextId);
        this.accountId = MailAccount.DEFAULT_ID;
        pendingInvocations = new ConcurrentLinkedQueue<Runnable>();
    }

    /**
     * Constructor
     *
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If initialization fails
     */
    protected AbstractSMTPTransport(Session session, int accountId) throws OXException {
        super();
        this.session = session;
        this.ctx = Services.getService(ContextService.class).getContext(session.getContextId());
        this.accountId = accountId;
        pendingInvocations = new ConcurrentLinkedQueue<Runnable>();
    }

    // --------------------------------------------- Mandatory overrides ------------------------------------------------------------------

    /**
     * Sets the reply headers in specified MIME message.
     *
     * @param mimeMessage The MIME message to apply reply headers to
     * @param msgref The message reference of the original mail
     * @throws OXException If reply headers cannot be applied
     * @throws MessagingException If reply headers cannot be applied due to a messaging error
     */
    protected abstract void setReplyHeaders(MimeMessage mimeMessage, MailPath msgref) throws OXException, MessagingException;

    /**
     * Creates the appropriate SMTP message filler.
     *
     * @param optMailSettings The optional mail settings to consider
     * @return The SMTP message filler
     * @throws OXException If SMTP message filler cannot be returned
     */
    protected abstract SMTPMessageFiller createSMTPMessageFiller(UserSettingMail optMailSettings) throws OXException;

    /**
     * Creates the appropriate SMTP configuration instance
     *
     * @return The SMTP configuration instance
     * @throws OXException If SMTP configuration instance cannot be returned
     */
    protected abstract SMTPConfig createSMTPConfig() throws OXException;

    // --------------------------------------------- Optional overrides -------------------------------------------------------------------

    /**
     * Handles given messaging error.
     *
     * @param e The messaging error to handle
     * @param config The associated configuration
     * @return The appropriate {@code OXException} instance
     * @throws OXException If appropriate {@code OXException} instance cannot be returned
     */
    protected OXException handleMessagingException(MessagingException e, MailConfig config) throws OXException {
        return MimeMailException.handleMessagingException(e, config, null);
    }

    /**
     * Logs the successful transport of given MIME message.
     *
     * @param smtpMessage The transported MIME message
     * @param smtpConfig The associated configuration
     * @throws OXException If logging fails
     * @throws MessagingException If a messaging error occurs
     */
    protected void logMessageTransport(final MimeMessage smtpMessage, final SMTPConfig smtpConfig) throws OXException, MessagingException {
        if (smtpConfig.getSMTPProperties().isLogTransport()) {
            LOG.info("Sent \"{}\" for login \"{}\" using SMTP server \"{}\" on port {}.", smtpMessage.getMessageID(), smtpConfig.getLogin(), smtpConfig.getServer(), Integer.valueOf(smtpConfig.getPort()));
        }
    }

    /**
     * Gets the JavaMail SMTP session using specified configuration.
     *
     * @return The JavaMail SMTP session
     * @throws OXException If JavaMail SMTP session cannot be returned
     */
    protected javax.mail.Session getSMTPSession() throws OXException {
        return getSMTPSession(getTransportConfig());
    }

    /**
     * Gets the JavaMail SMTP session using specified configuration.
     *
     * @param smtpConfig The configuration to use
     * @return The JavaMail SMTP session
     * @throws OXException If JavaMail SMTP session cannot be returned
     */
    protected javax.mail.Session getSMTPSession(SMTPConfig smtpConfig) throws OXException {
        return getSMTPSession(smtpConfig, accountId > 0 && (smtpConfig.isStartTls() || smtpConfig.isRequireTls() || smtpConfig.getTransportProperties().isEnforceSecureConnection()));
    }

    protected void processAddressHeader(final MimeMessage mimeMessage) throws OXException, MessagingException {
        {
            final String str = mimeMessage.getHeader("From", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setFrom(addresses[0]);
            }
        }
        {
            final String str = mimeMessage.getHeader("Sender", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setSender(addresses[0]);
            }
        }
        {
            final String str = mimeMessage.getHeader("To", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setRecipients(RecipientType.TO, addresses);
            }
        }
        {
            final String str = mimeMessage.getHeader("Cc", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setRecipients(RecipientType.CC, addresses);
            }
        }
        {
            final String str = mimeMessage.getHeader("Bcc", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setRecipients(RecipientType.BCC, addresses);
            }
        }
        {
            final String str = mimeMessage.getHeader("Reply-To", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setReplyTo(addresses);
            }
        }
        {
            final String str = mimeMessage.getHeader("Disposition-Notification-To", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setHeader("Disposition-Notification-To", addresses[0].toString());
            }
        }
    }

    protected boolean checkRecipients(final Address[] recipients) throws OXException {
        if ((recipients == null) || (recipients.length == 0)) {
            throw SMTPExceptionCode.MISSING_RECIPIENTS.create();
        }
        Boolean poisoned = null;
        final ConfigurationService service = Services.getService(ConfigurationService.class);
        if (null != service) {
            final Filter filter = service.getFilterFromProperty("com.openexchange.mail.transport.redirectWhitelist");
            if (null != filter) {
                for (final Address address : recipients) {
                    if (MimeMessageUtility.POISON_ADDRESS == address) {
                        poisoned = Boolean.TRUE;
                    } else {
                        final InternetAddress internetAddress = (InternetAddress) address;
                        if (!filter.accepts(internetAddress.getAddress())) {
                            throw SMTPExceptionCode.RECIPIENT_NOT_ALLOWED.create(internetAddress.toUnicodeString());
                        }
                    }
                }
            }
        }
        if (MailProperties.getInstance().isSupportMsisdnAddresses()) {
            InternetAddress internetAddress;
            for (final Address address : recipients) {
                if (MimeMessageUtility.POISON_ADDRESS == address) {
                    poisoned = Boolean.TRUE;
                } else {
                    internetAddress = (InternetAddress) address;
                    final String sAddress = internetAddress.getAddress();
                    if (MsisdnCheck.checkMsisdn(sAddress)) {
                        if (sAddress.indexOf('/') < 0) {
                            // Detected a MSISDN address that misses "/TYPE=" appendix necessary for the MTA
                            internetAddress.setAddress(sAddress + "/TYPE=PLMN");
                        }
                        try {
                            internetAddress.setPersonal("", "US-ASCII");
                        } catch (final UnsupportedEncodingException e) {
                            // Ignore as personal is cleared
                        }
                    }
                }
            }
        }
        return null == poisoned ? isPoisoned(recipients) : poisoned.booleanValue();
    }

    /*
     * helper methods
     */
    protected javax.mail.Session getSMTPSession(SMTPConfig smtpConfig, boolean forceSecure) throws OXException {
        if (null == smtpSession) {
            synchronized (this) {
                if (null == smtpSession) {
                    final Properties smtpProps = SMTPSessionProperties.getDefaultSessionProperties();
                    smtpProps.put("mail.smtp.class", JavaSMTPTransport.class.getName());
                    smtpProps.put("com.openexchange.mail.maxMailSize", Long.toString(getMaxMailSize()));

                    /*
                     * Set properties
                     */
                    final ISMTPProperties smtpProperties = smtpConfig.getSMTPProperties();
                    /*
                     * Check for Kerberos subject
                     */
                    final boolean kerberosAuth = isKerberosAuth();
                    if (isKerberosAuth()) {
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
                     * Send partial or abort?
                     */
                    if (smtpProperties.isSendPartial()) {
                        smtpProps.put("mail.smtp.sendpartial", "true");
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
                        smtpProps.put("mail.smtp.ssl.protocols", smtpConfig.getSMTPProperties().getSSLProtocols());
                        /*
                         * Specify SSL cipher suites
                         */
                        final String cipherSuites = smtpConfig.getSMTPProperties().getSSLCipherSuites();
                        if (false == Strings.isEmpty(cipherSuites)) {
                            smtpProps.put("mail.smtp.ssl.ciphersuites", cipherSuites);
                        }
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
                        String hostName = getHostName();
                        try {
                            final InetSocketAddress address = new InetSocketAddress(IDNA.toASCII(smtpConfig.getServer()), smtpConfig.getPort());
                            final Map<String, String> capabilities = SMTPCapabilityCache.getCapabilities(address, smtpConfig.isSecure(), smtpProperties, hostName);
                            if (capabilities.containsKey("STARTTLS")) {
                                smtpProps.put("mail.smtp.starttls.enable", "true");
                            } else if (forceSecure) {
                                // No SSL demanded and SMTP server seems not to support TLS
                                throw MailExceptionCode.NON_SECURE_DENIED.create(smtpConfig.getServer());
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
                        smtpProps.put("mail.smtp.ssl.protocols", smtpConfig.getSMTPProperties().getSSLProtocols());
                        /*
                         * Specify SSL cipher suites
                         */
                        final String cipherSuites = smtpConfig.getSMTPProperties().getSSLCipherSuites();
                        if (false == Strings.isEmpty(cipherSuites)) {
                            smtpProps.put("mail.smtp.ssl.ciphersuites", cipherSuites);
                        }
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
                    smtpSession.addProvider(new Provider(Provider.Type.TRANSPORT, "smtp", JavaSMTPTransport.class.getName(), "OX Software GmbH", MailAccess.getVersion()));
                }
            }
        }
        return smtpSession;
    }

    /**
     * Sets the user
     *
     * @param user The user
     */
    protected void setUser(User user) {
        this.user = user;
    }

    /**
     * Sets the Kerberos subject
     *
     * @param kerberosSubject The subject
     */
    protected void setKerberosSubject(Subject kerberosSubject) {
        this.kerberosSubject = kerberosSubject;
    }

    /**
     * Gets the connected SMTP transport.
     *
     * @return The connected SMTP transport
     * @throws OXException If connect attempt fails
     */
    protected com.sun.mail.smtp.SMTPTransport getSmtpTransport() throws OXException {
        SMTPConfig smtpConfig = getTransportConfig();
        javax.mail.Session smtpSession = getSMTPSession(smtpConfig);
        try {
            com.sun.mail.smtp.SMTPTransport transport = (com.sun.mail.smtp.SMTPTransport) smtpSession.getTransport(SMTP);
            connectTransport(transport, smtpConfig);
            return transport;
        } catch (NoSuchProviderException e) {
            throw handleMessagingException(e, smtpConfig);
        }
    }

    /**
     * Connects the given <code>Transport</code> instance.
     *
     * @param transport The instance to connect
     * @param smtpConfig The associated SMTP configuration
     * @throws OXException If connect attempt fails
     */
    protected void connectTransport(Transport transport, SMTPConfig smtpConfig) throws OXException {
        final String server = IDNA.toASCII(smtpConfig.getServer());
        final int port = smtpConfig.getPort();
        final String login = smtpConfig.getLogin();
        try {
            if (smtpConfig.getSMTPProperties().isSmtpAuth()) {
                final String encodedPassword = encodePassword(smtpConfig.getPassword());
                if (isKerberosAuth()) {
                    try {
                        Subject.doAs(kerberosSubject, new SaslSmtpLoginAction(transport, server, port, login, encodedPassword));
                    } catch (final PrivilegedActionException e) {
                        handlePrivilegedActionException(e);
                    }
                } else {
                    transport.connect(server, port, null == login ? "" : login, null == encodedPassword ? "" : encodedPassword);
                }
            } else {
                transport.connect(server, port, null, null);
            }

            Session session = this.session;
            if (session != null) {
                AuditLogService auditLogService = Services.optService(AuditLogService.class);
                if (null != auditLogService) {
                    String eventId = MailAccount.DEFAULT_ID == accountId ? "smtp.primary.login" : "smtp.external.login";
                    auditLogService.log(eventId, DefaultAttribute.valueFor(Name.LOGIN, session.getLoginName()), DefaultAttribute.valueFor(Name.IP_ADDRESS, session.getLocalIp()), DefaultAttribute.timestampFor(new Date()), DefaultAttribute.arbitraryFor("smtp.login", null == login ? "<none>" : login), DefaultAttribute.arbitraryFor("smtp.server", server), DefaultAttribute.arbitraryFor("smtp.port", Integer.toString(port)));
                }
            }
        } catch (javax.mail.AuthenticationFailedException e) {
            throw MimeMailExceptionCode.TRANSPORT_INVALID_CREDENTIALS.create(e, smtpConfig.getServer(), e.getMessage());
        } catch (MessagingException e) {
            if (e.getNextException() instanceof javax.net.ssl.SSLHandshakeException) {
                throw SMTPExceptionCode.SECURE_CONNECTION_NOT_POSSIBLE.create(e.getNextException(), server, login);
            }
            throw handleMessagingException(e, smtpConfig);
        }
    }

    /**
     * Performs {@link MimeMessage#saveChanges() saveChanges()} on specified message with sanitizing for a possibly corrupt/wrong Content-Type header.
     * <p>
     * Aligns <i>Message-Id</i> header to given host name.
     *
     * @param mimeMessage The MIME message
     * @param keepMessageIdIfPresent Whether to keep a possibly available <i>Message-ID</i> header or to generate a new (unique) one
     * @throws OXException If operation fails
     */
    protected void saveChangesSafe(MimeMessage mimeMessage, boolean keepMessageIdIfPresent) throws OXException {
        String hostName = getHostName();
        MimeMessageUtility.saveChanges(mimeMessage, hostName, keepMessageIdIfPresent);
        // Check whether to remove MIME-Version headers from sub-parts
        if (TransportProperties.getInstance().isRemoveMimeVersionInSubParts()) {
            /*-
             *  Note that the MIME-Version header field is required at the top level
             *  of a message.  It is not required for each body part of a multipart
             *  entity.  It is required for the embedded headers of a body of type
             *  "message/rfc822" or "message/partial" if and only if the embedded
             *  message is itself claimed to be MIME-conformant.
             */
            try {
                checkMimeVersionHeader(mimeMessage);
            } catch (final Exception e) {
                LOG.warn("Could not check for proper usage of \"MIME-Version\" header according to RFC2045.", e);
            }
        }
    }

    /**
     * Transports the given MIME message.
     *
     * @param smtpMessage The MIME message to send
     * @param recipients The recipients
     * @param smtpSession The associated SMTP session
     * @param smtpConfig The associated SMTP configuration
     * @return The sent MIME message
     * @throws OXException If transport fails
     */
    protected MimeMessage transport(MimeMessage smtpMessage, Address[] recipients, javax.mail.Session smtpSession, SMTPConfig smtpConfig) throws OXException {
        return transport(smtpMessage, recipients, smtpSession, smtpConfig, null);
    }

    /**
     * Transports the given MIME message.
     *
     * @param smtpMessage The MIME message to send
     * @param recipients The recipients
     * @param smtpSession The associated SMTP session
     * @param smtpConfig The associated SMTP configuration
     * @param mtaInfo The optional MTA info to fill
     * @return The sent MIME message
     * @throws OXException If transport fails
     */
    protected MimeMessage transport(MimeMessage smtpMessage, Address[] recipients, javax.mail.Session smtpSession, SMTPConfig smtpConfig, MtaStatusInfo mtaInfo) throws OXException {
        // Prepare addresses
        prepareAddresses(recipients);

        Transport transport = null;
        try {
            // Grab listener chain instance
            ListenerChain listenerChain = ListenerChain.getInstance();

            // Try to send the message
            MimeMessage messageToSend = smtpMessage;
            Exception exception = null;
            try {
                // Check listener chain
                Result result = listenerChain.onBeforeMessageTransport(messageToSend, session);

                // Examine reply of the listener chain
                {
                    Reply reply = result.getReply();
                    if (Reply.PROCESSED.ordinal() >= reply.ordinal()) {
                        // Check if denied
                        if (Reply.DENY == reply) {
                            throw MailExceptionCode.SEND_DENIED.create();
                        }

                        // Just return the processed message
                        return result.getMimeMessage();
                    }
                }

                // Grab possibly new MIME message
                MimeMessage resultingMimeMessage = result.getMimeMessage();
                if (null != resultingMimeMessage) {
                    messageToSend = resultingMimeMessage;
                }

                // Create the SMTP transport instance
                transport = smtpSession.getTransport(SMTP);

                // Open SMTP connection
                connectTransport(transport, smtpConfig);

                // Register transport listener to fill addresses to status info
                if (null != mtaInfo) {
                    transport.addTransportListener(new AddressAddingTransportListener(mtaInfo));
                }

                // Register transport listener for audit logging
                Session session = this.session;
                if (null != session) {
                    AuditLogService auditLogService = Services.optService(AuditLogService.class);
                    if (null != auditLogService) {
                        String server = IDNA.toASCII(smtpConfig.getServer());
                        int port = smtpConfig.getPort();
                        String login = smtpConfig.getLogin();
                        transport.addTransportListener(new AuditLoggingTransportListener(login, server, port, accountId, session, auditLogService));
                    }
                }

                // Transport
                long start = System.currentTimeMillis();
                doTransport(messageToSend, recipients, transport);
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                logMessageTransport(messageToSend, smtpConfig);
            } catch (OXException e) {
                exception = e;
                throw e;
            } catch (SMTPSendFailedException sendFailed) {
                exception = sendFailed;
                OXException oxe = handleMessagingException(sendFailed, smtpConfig);
                if (null != mtaInfo) {
                    mtaInfo.setReturnCode(sendFailed.getReturnCode());
                    oxe.setArgument("mta_info", mtaInfo);
                }
                Address[] validSentAddresses = sendFailed.getValidSentAddresses();
                if (validSentAddresses != null && validSentAddresses.length > 0) {
                    try {
                        oxe.setArgument("sent_message", MimeMessageConverter.convertMessage(messageToSend));
                    } catch (Exception e) {
                        // Ignore
                        LOG.debug("Failed to convert message after a message was partially sent", e);
                    }
                }
                throw oxe;
            } catch (NoSuchProviderException e) {
                exception = e;
                throw MimeMailException.handleMessagingException(e);
            } catch (MessagingException e) {
                exception = e;
                if (e.getNextException() instanceof javax.activation.UnsupportedDataTypeException) {
                    // Check for "no object DCH for MIME type xxxxx/yyyy"
                    final String message = e.getNextException().getMessage();
                    if (message.toLowerCase().indexOf("no object dch") >= 0) {
                        // Not able to recover from JAF's "no object DCH for MIME type xxxxx/yyyy" error
                        // Perform the alternative transport with custom JAF DataHandler
                        LOG.warn(message.replaceFirst("[dD][cC][hH]", Matcher.quoteReplacement("javax.activation.DataContentHandler")));
                        return transportAlt(messageToSend, recipients, transport, smtpConfig);
                    }
                } else if (e.getNextException() instanceof IOException) {
                    if (e.getNextException().getMessage().equals("Maximum message size is exceeded.")) {
                        throw MailExceptionCode.MAX_MESSAGE_SIZE_EXCEEDED.create(getSize(getMaxMailSize(), 2, false, true));
                    }
                }
                throw handleMessagingException(e, smtpConfig);
            } catch (RuntimeException e) {
                exception = e;
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                listenerChain.onAfterMessageTransport(messageToSend, exception, session);
            }
            return messageToSend;
        } finally {
            closeSafe(transport);
        }
    }

    /**
     * Actually transports given message via specified connected transport to given recipients.
     *
     * @param messageToSend The message to send
     * @param recipients The recipients to send to
     * @param transport The transport to use
     * @throws MessagingException If transport fails
     */
    protected void doTransport(MimeMessage messageToSend, Address[] recipients, Transport transport) throws MessagingException {
        transport.sendMessage(messageToSend, recipients);
    }

    private void closeSafe(Transport transport) {
        if (null != transport) {
            try {
                transport.close();
            } catch (final MessagingException e) {
                LOG.error("Closing SMTP transport failed.", e);
            }
        }
    }

    /*
     * begin interface implementation
     */

    @Override
    public SMTPConfig getTransportConfig() throws OXException {
        SMTPConfig tmp = cachedSmtpConfig;
        if (tmp == null) {
            synchronized (this) {
                tmp = cachedSmtpConfig;
                if (tmp == null) {
                    tmp = createSMTPConfig();
                    cachedSmtpConfig = tmp;
                }
            }
        }
        return tmp;
    }

    @Override
    public void ping() throws OXException {
        // Connect to SMTP server
        Transport transport;
        try {
            SMTPConfig smtpConfig = getTransportConfig();
            transport = getSMTPSession(smtpConfig, smtpConfig.isRequireTls() || smtpConfig.getTransportProperties().isEnforceSecureConnection()).getTransport(SMTP);
        } catch (final NoSuchProviderException e) {
            throw MimeMailException.handleMessagingException(e);
        }

        boolean close = false;
        SMTPConfig config = getTransportConfig();
        try {
            connectTransport(transport, config);
            close = true;
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
    public MailMessage sendMailMessage(ComposedMailMessage composedMail, ComposeType sendType, Address[] allRecipients) throws OXException {
        return sendMailMessage(composedMail, sendType, allRecipients, null);
    }

    @Override
    public MailMessage sendMailMessage(ComposedMailMessage composedMail, ComposeType sendType, Address[] allRecipients, MtaStatusInfo mtaStatusInfo) throws OXException {
        final SMTPConfig smtpConfig = getTransportConfig();
        try {
            /*
             * Message content available?
             */
            MimeMessage mimeMessage = null;
            if (composedMail instanceof ContentAware) {
                try {
                    final Object content = composedMail.getContent();
                    if (content instanceof MimeMessage) {
                        mimeMessage = (MimeMessage) content;
                        mimeMessage.removeHeader("x-original-headers");
                        /*
                         * Check for reply
                         */
                        final MailPath msgref = composedMail.getMsgref();
                        if (ComposeType.REPLY.equals(sendType) && msgref != null) {
                            setReplyHeaders(mimeMessage, msgref);
                        }

                        /*
                         * Set common headers
                         */
                        final SMTPMessageFiller smtpFiller = createSMTPMessageFiller(null);
                        smtpFiller.setAccountId(accountId);
                        smtpFiller.setCommonHeaders(mimeMessage);
                    }
                } catch (final Exception e) {
                    mimeMessage = null;
                }
            }

            /*
             * Fill from scratch
             */
            if (mimeMessage == null) {
                final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession());
                /*
                 * Fill message dependent on send type
                 */
                final SMTPMessageFiller smtpFiller = createSMTPMessageFiller(composedMail.getMailSettings());
                smtpFiller.setAccountId(accountId);
                composedMail.setFiller(smtpFiller);
                try {
                    /*
                     * Check for reply
                     */
                    final MailPath msgref = composedMail.getMsgref();
                    if (ComposeType.REPLY.equals(sendType) && msgref != null) {
                        setReplyHeaders(smtpMessage, msgref);
                    }

                    /*
                     * Fill message
                     */
                    smtpFiller.fillMail(composedMail, smtpMessage, sendType);

                    /*
                     * Check recipients
                     */
                    if (allRecipients == null) {
                        if (composedMail.hasRecipients()) {
                            allRecipients = composedMail.getRecipients();
                        } else {
                            allRecipients = smtpMessage.getAllRecipients();
                        }
                    }

                    smtpFiller.setSendHeaders(composedMail, smtpMessage);

                    /*
                     * Drop special "x-original-headers" header
                     */
                    smtpMessage.removeHeader("x-original-headers");
                    mimeMessage = smtpMessage;
                } finally {
                    invokeLater(new MailCleanerTask(composedMail));
                }
            }

            MimeMessage sentMimeMessage = sendMimeMessage(mimeMessage, allRecipients, mtaStatusInfo);
            return MimeMessageConverter.convertMessage(sentMimeMessage);
        } catch (final MessagingException e) {
            throw handleMessagingException(e, smtpConfig);
        } catch (final IOException e) {
            throw SMTPExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailMessage sendRawMessage(byte[] asciiBytes, SendRawProperties properties) throws OXException {
        final SMTPConfig smtpConfig = getTransportConfig();
        try {
            // Create input stream to read from
            InputStream rfc822IS;
            if (properties.isSanitizeHeaders()) {
                rfc822IS = MimeHeaderNameChecker.sanitizeHeaderNames(asciiBytes);
            } else {
                rfc822IS = new UnsynchronizedByteArrayInputStream(asciiBytes);
            }
            final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession(), rfc822IS);
            InternetAddress sender = properties.getSender();
            if (sender != null) {
                smtpMessage.setEnvelopeFrom(sender.getAddress());
            }

            // Check recipients
            Address[] recipients = properties.getRecipients();
            if (recipients == null) {
                recipients = smtpMessage.getAllRecipients();
            }
            if (properties.isValidateAddressHeaders()) {
                processAddressHeader(smtpMessage);
            }

            // Save changes
            saveChangesSafe(smtpMessage, true);

            // Transport if non-poisoned
            boolean poisoned = checkRecipients(recipients);
            if (!poisoned) {
                transport(smtpMessage, recipients, getSMTPSession(smtpConfig), smtpConfig);
            }

            return MimeMessageConverter.convertMessage(smtpMessage);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, smtpConfig, session);
        }
    }

    @Override
    public void sendRawMessage(InputStream stream, SendRawProperties properties) throws OXException {
        if (properties.isSanitizeHeaders()) {
            // Cannot sanitize stream...
            try {
                sendRawMessage(Streams.stream2bytes(stream), properties);
            } catch (IOException e) {
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
            return;
        }

        SMTPConfig smtpConfig = getTransportConfig();
        try {
            final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession(), stream);
            InternetAddress sender = properties.getSender();
            if (sender != null) {
                smtpMessage.setEnvelopeFrom(sender.getAddress());
            }

            // Check recipients
            Address[] recipients = properties.getRecipients();
            if (recipients == null) {
                recipients = smtpMessage.getAllRecipients();
            }
            if (properties.isValidateAddressHeaders()) {
                processAddressHeader(smtpMessage);
            }

            // Save changes
            saveChangesSafe(smtpMessage, true);

            // Transport if non-poisoned
            boolean poisoned = checkRecipients(recipients);
            if (!poisoned) {
                transport(smtpMessage, recipients, getSMTPSession(smtpConfig), smtpConfig);
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, smtpConfig, session);
        } finally {
            Streams.close(stream);
        }
    }

    @Override
    public MailMessage sendRawMessage(final byte[] asciiBytes, final Address[] allRecipients) throws OXException {
        final SMTPConfig smtpConfig = getTransportConfig();
        final SMTPMessage smtpMessage;
        try {
            smtpMessage = new SMTPMessage(getSMTPSession(), MimeHeaderNameChecker.sanitizeHeaderNames(asciiBytes));
            smtpMessage.removeHeader("x-original-headers");
        } catch (final MessagingException e) {
            throw handleMessagingException(e, smtpConfig);
        }

        MimeMessage sentMimeMessage = sendMimeMessage(smtpMessage, allRecipients, null);
        return MimeMessageConverter.convertMessage(sentMimeMessage);
    }

    @Override
    public void sendRawMessage(InputStream stream, Address[] allRecipients) throws OXException {
        SMTPConfig smtpConfig = getTransportConfig();
        final SMTPMessage smtpMessage;
        try {
            smtpMessage = new SMTPMessage(getSMTPSession(), stream);
            smtpMessage.removeHeader("x-original-headers");
        } catch (final MessagingException e) {
            throw handleMessagingException(e, smtpConfig);
        }

        sendMimeMessage(smtpMessage, allRecipients, null);
    }

    @Override
    public void sendMimeMessage(MimeMessage mimeMessage, Address[] allRecipients) throws OXException {
        sendMimeMessage(mimeMessage, allRecipients, null);
    }

    private MimeMessage sendMimeMessage(MimeMessage mimeMessage, Address[] allRecipients, MtaStatusInfo mtaStatusInfo) throws OXException {
        SMTPConfig smtpConfig = getTransportConfig();
        try {
            // Check recipients
            Address[] recipients = allRecipients == null ? mimeMessage.getAllRecipients() : allRecipients;
            processAddressHeader(mimeMessage);

            // Save changes
            saveChangesSafe(mimeMessage, true);

            // Check if "poisoned"
            boolean poisoned = checkRecipients(recipients);
            if (poisoned) {
                return mimeMessage;
            }

            // Do the transport
            return transport(mimeMessage, recipients, getSMTPSession(smtpConfig), smtpConfig, mtaStatusInfo);
        } catch (MessagingException e) {
            throw handleMessagingException(e, smtpConfig);
        }
    }

    @Override
    public void close() {
        clearUp();
    }

    /*
     * end interface implementation
     */

    private void clearUp() {
        doInvocations();
    }

    /**
     * Executes all tasks queued for execution
     */
    private void doInvocations() {
        for (Runnable task = pendingInvocations.poll(); task != null; task = pendingInvocations.poll()) {
            task.run();
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

    private long getMaxMailSize() throws OXException {
        final ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);

        if (factory != null) {

            final ConfigView view = factory.getView(getUserId(), ctx.getContextId());
            final ConfigProperty<Long> property = view.property("com.openexchange.mail.maxMailSize", Long.class);

            if (property.isDefined()) {
                final Long l = property.get();
                final long maxMailSize = null == l ? -1 : l.longValue();
                if (maxMailSize > 0) {
                    return maxMailSize;
                }
            }
        }

        return -1;
    }

    private int getUserId() {
        int userId;
        if (user == null) {
            userId = ConfigProviderService.NO_USER;
        } else {
            userId = user.getId();
        }

        return userId;
    }

    private MimeMessage transportAlt(final MimeMessage smtpMessage, final Address[] recipients, final Transport transport, final SMTPConfig smtpConfig) throws OXException {
        try {
            final MimeMessageDataSource dataSource = new MimeMessageDataSource(smtpMessage, smtpConfig, smtpConfig.getSession());
            smtpMessage.setDataHandler(new DataHandler(dataSource));
            if (!transport.isConnected()) {
                connectTransport(transport, smtpConfig);
            }
            doTransport(smtpMessage, recipients, transport);
            logMessageTransport(smtpMessage, smtpConfig);
            invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        dataSource.cleanUp();
                    } catch (final Exception e) {
                        // Ignore
                    }
                }
            });
            return smtpMessage;
        } catch (final MessagingException me) {
            throw handleMessagingException(me, smtpConfig);
        }
    }

    private String encodePassword(final String password) throws OXException {
        String tmpPass = password;
        if (tmpPass != null) {
            try {
                tmpPass = new String(password.getBytes(Charsets.forName(getTransportConfig().getSMTPProperties().getSmtpAuthEnc())), Charsets.ISO_8859_1);
            } catch (final UnsupportedCharsetException e) {
                LOG.error("Unsupported encoding in a message detected and monitored", e);
                mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            }
        }
        return tmpPass;
    }

    private void prepareAddresses(final Address[] addresses) {
        final int length = addresses.length;
        final StringBuilder tmp = new StringBuilder(32);
        for (int i = 0; i < length; i++) {
            final InternetAddress address = (InternetAddress) addresses[i];
            final String sAddress = address.getAddress();
            if (MsisdnCheck.checkMsisdn(sAddress)) {
                final int pos = sAddress.indexOf('/');
                if (pos < 0) {
                    tmp.setLength(0);
                    address.setAddress(tmp.append(sAddress).append("/TYPE=PLMN").toString());
                }
            }
        }
    }

    private String getHostName() {
        final HostnameService hostnameService = Services.getService(HostnameService.class);
        String hostName;
        if (null == hostnameService) {
            hostName = getFallbackHostname();
        } else {
            if (null != user && user.isGuest()) {
                hostName = hostnameService.getGuestHostname(getUserId(), ctx.getContextId());
            } else {
                hostName = hostnameService.getHostname(getUserId(), ctx.getContextId());
            }
        }
        if (null == hostName) {
            hostName = getFallbackHostname();
        }
        return hostName;
    }

    private void checkMimeVersionHeader(final MimeMessage mimeMessage) throws MessagingException, IOException {
        final String header = mimeMessage.getHeader("Content-Type", null);
        if (null != header && header.toLowerCase().startsWith("multipart/")) {
            final Multipart multipart = (Multipart) mimeMessage.getContent();
            final int count = multipart.getCount();
            for (int i = 0; i < count; i++) {
                checkMimeVersionHeader(multipart.getBodyPart(i));
            }
        }
    }

    private void checkMimeVersionHeader(final Part part) throws MessagingException, IOException {
        final String[] header = part.getHeader("Content-Type");
        if (null != header && header.length > 0 && null != header[0]) {
            final String cts = header[0].toLowerCase();
            if (cts.startsWith("multipart/")) {
                final Multipart multipart = (Multipart) part.getContent();
                final int count = multipart.getCount();
                for (int i = 0; i < count; i++) {
                    checkMimeVersionHeader(multipart.getBodyPart(i));
                }
            } else if (cts.startsWith("message/rfc822") || cts.startsWith("message/partial")) {
                part.setHeader("MIME-Version", "1.0");
                Object content;
                try {
                    content = part.getContent();
                } catch (Exception e) {
                    content = null;
                }
                if (content instanceof MimeMessage) {
                    checkMimeVersionHeader((MimeMessage) content);
                }
            } else {
                part.removeHeader("MIME-Version");
            }
        } else {
            part.removeHeader("MIME-Version");
        }
    }

    private String getFallbackHostname() {
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

    private static final boolean isPoisoned(final Address[] recipients) {
        if ((recipients == null) || (recipients.length == 0)) {
            return false;
        }
        for (final Address address : recipients) {
            if (MimeMessageUtility.POISON_ADDRESS == address) {
                return true;
            }
        }
        return false;
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

    private static final class AddressAddingTransportListener implements TransportListener {

        private final MtaStatusInfo statusInfo;

        AddressAddingTransportListener(MtaStatusInfo statusInfo) {
            super();
            this.statusInfo = statusInfo;
        }

        @Override
        public void messagePartiallyDelivered(TransportEvent e) {
            fillAddressesFromEvent(e);
        }

        @Override
        public void messageNotDelivered(TransportEvent e) {
            fillAddressesFromEvent(e);

        }

        @Override
        public void messageDelivered(TransportEvent e) {
            fillAddressesFromEvent(e);

        }

        private void fillAddressesFromEvent(TransportEvent e) {
            javax.mail.Address[] arr = e.getInvalidAddresses();
            if (null != arr) {
                statusInfo.getInvalidAddresses().addAll(Arrays.asList(arr));
            }

            arr = e.getValidUnsentAddresses();
            if (null != arr) {
                statusInfo.getUnsentAddresses().addAll(Arrays.asList(arr));
            }

            arr = e.getValidSentAddresses();
            if (null != arr) {
                statusInfo.getSentAddresses().addAll(Arrays.asList(arr));
            }
        }
    }

    private static final class AuditLoggingTransportListener implements TransportListener {

        private final int accountId;
        private final Session session;
        private final AuditLogService auditLogService;
        private final String login;
        private final String server;
        private final int port;

        AuditLoggingTransportListener(String login, String server, int port, int accountId, Session session, AuditLogService auditLogService) {
            super();
            this.login = login;
            this.server = server;
            this.port = port;
            this.accountId = accountId;
            this.session = session;
            this.auditLogService = auditLogService;
        }

        @Override
        public void messagePartiallyDelivered(TransportEvent e) {
            logAddressesFromEvent(e);
        }

        @Override
        public void messageNotDelivered(TransportEvent e) {
            logAddressesFromEvent(e);

        }

        @Override
        public void messageDelivered(TransportEvent e) {
            logAddressesFromEvent(e);

        }

        private void logAddressesFromEvent(TransportEvent e) {
            javax.mail.Address[] arr = e.getInvalidAddresses();
            if (null != arr) {
                for (Address address : arr) {
                    String eventId = MailAccount.DEFAULT_ID == accountId ? "smtp.primary.invalid" : "smtp.external.invalid";
                    auditLogService.log(eventId, DefaultAttribute.valueFor(Name.LOGIN, session.getLoginName()), DefaultAttribute.valueFor(Name.IP_ADDRESS, session.getLocalIp()), DefaultAttribute.timestampFor(new Date()), DefaultAttribute.arbitraryFor("smtp.login", null == login ? "<none>" : login), DefaultAttribute.arbitraryFor("smtp.server", server), DefaultAttribute.arbitraryFor("smtp.port", Integer.toString(port)), DefaultAttribute.arbitraryFor("smtp.invalid", address instanceof InternetAddress ? ((InternetAddress) address).getAddress() : address.toString()));
                }
            }

            arr = e.getValidUnsentAddresses();
            if (null != arr) {
                for (Address address : arr) {
                    String eventId = MailAccount.DEFAULT_ID == accountId ? "smtp.primary.unsent" : "smtp.external.unsent";
                    auditLogService.log(eventId, DefaultAttribute.valueFor(Name.LOGIN, session.getLoginName()), DefaultAttribute.valueFor(Name.IP_ADDRESS, session.getLocalIp()), DefaultAttribute.timestampFor(new Date()), DefaultAttribute.arbitraryFor("smtp.login", null == login ? "<none>" : login), DefaultAttribute.arbitraryFor("smtp.server", server), DefaultAttribute.arbitraryFor("smtp.port", Integer.toString(port)), DefaultAttribute.arbitraryFor("smtp.unsent", address instanceof InternetAddress ? ((InternetAddress) address).getAddress() : address.toString()));
                }
            }

            arr = e.getValidSentAddresses();
            if (null != arr) {
                for (Address address : arr) {
                    String eventId = MailAccount.DEFAULT_ID == accountId ? "smtp.primary.sent" : "smtp.external.sent";
                    auditLogService.log(eventId, DefaultAttribute.valueFor(Name.LOGIN, session.getLoginName()), DefaultAttribute.valueFor(Name.IP_ADDRESS, session.getLocalIp()), DefaultAttribute.timestampFor(new Date()), DefaultAttribute.arbitraryFor("smtp.login", null == login ? "<none>" : login), DefaultAttribute.arbitraryFor("smtp.server", server), DefaultAttribute.arbitraryFor("smtp.port", Integer.toString(port)), DefaultAttribute.arbitraryFor("smtp.sent", address instanceof InternetAddress ? ((InternetAddress) address).getAddress() : address.toString()));
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static final class MailCleanerTask implements Runnable {

        private final ComposedMailMessage composedMail;

        MailCleanerTask(ComposedMailMessage composedMail) {
            super();
            this.composedMail = composedMail;
        }

        @Override
        public void run() {
            composedMail.cleanUp();
        }

    } // End of class MailCleanerTask

    private static final class SaslSmtpLoginAction implements PrivilegedExceptionAction<Object> {

        private final Transport transport;
        private final String server;
        private final int port;
        private final String login;
        private final String pw;

        SaslSmtpLoginAction(Transport transport, String server, int port, String login, String pw) {
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
    } // End of class SaslSmtpLoginAction

}
