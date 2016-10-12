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

package com.openexchange.pop3.connect;

import static com.openexchange.pop3.services.POP3ServiceRegistry.getServiceRegistry;
import static com.openexchange.pop3.util.POP3StorageUtil.parseLoginDelaySeconds;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.idn.IDNA;
import javax.net.ssl.SSLHandshakeException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.log.audit.DefaultAttribute;
import com.openexchange.log.audit.DefaultAttribute.Name;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.MimeSessionPropertyNames;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.pop3.POP3Provider;
import com.openexchange.pop3.config.IPOP3Properties;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.config.POP3Properties;
import com.openexchange.pop3.config.POP3SessionProperties;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.util.POP3CapabilityCache;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Prober;
import com.sun.mail.pop3.POP3Store;

/**
 * {@link POP3StoreConnector} - Connects an instance of {@link POP3Store}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3StoreConnector {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POP3StoreConnector.class);

    private static final PrintStream EMPTY_PRINTER = new PrintStream(new OutputStream() {

        @Override
        public void write(final int b) throws IOException {
            // Do nothing
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            // Do nothing
        }

        @Override
        public void write(final byte[] b) throws IOException {
            // Do nothing
        }

    });

    /**
     * The result after establishing a connection to POP3 server.
     */
    public static final class POP3StoreResult {

        private String capabilities;
        private POP3Store pop3Store;
        private final List<OXException> warnings;

        protected POP3StoreResult(final String capabilities) {
            super();
            this.capabilities = capabilities;
            warnings = new ArrayList<OXException>(2);
        }

        /**
         * Sets the capabilities
         *
         * @param capabilities The capabilities to set
         */
        public void setCapabilities(final String capabilities) {
            this.capabilities = capabilities;
        }

        /**
         * Sets the connected {@link POP3Store} instance.
         *
         * @param pop3Store The connected {@link POP3Store} instance.
         */
        protected void setPop3Store(final POP3Store pop3Store) {
            this.pop3Store = pop3Store;
        }

        /**
         * Adds given warnings.
         *
         * @param warning The warning to add
         */
        protected void addWarning(final OXException warning) {
            warnings.add(warning);
        }

        /**
         * Gets the warnings occurred during establishing a connection to POP3 server.
         *
         * @return The warnings
         */
        public Collection<OXException> getWarnings() {
            return Collections.unmodifiableCollection(warnings);
        }

        /**
         * Gets the connected {@link POP3Store} instance.
         *
         * @return The connected {@link POP3Store} instance.
         */
        public POP3Store getPop3Store() {
            return pop3Store;
        }

        /**
         * Gets the POP3 server's capabilities.
         *
         * @return The POP3 server's capabilities.
         */
        public String getCapabilities() {
            return capabilities;
        }

        /**
         * Checks if this result contains one or more warnings.
         *
         * @return <code>true</code> if this result contains one or more warnings; otherwsie <code>false</code>
         */
        public boolean containsWarnings() {
            return !warnings.isEmpty();
        }

    }

    private static Map<HostAndPort, Long> timedOutServers;

    private static Map<LoginAndPass, Long> failedAuths;

    /**
     * Start-up.
     */
    public static void startUp() {
        timedOutServers = new ConcurrentHashMap<HostAndPort, Long>();
        failedAuths = new ConcurrentHashMap<LoginAndPass, Long>();
    }

    /**
     * Shut-down.
     */
    public static void shutDown() {
        timedOutServers = null;
        failedAuths = null;
    }

    /**
     * Initializes a new {@link POP3StoreConnector}.
     */
    private POP3StoreConnector() {
        super();
    }

    /**
     * Gets a connected instance of {@link POP3Store}.
     *
     * @param pop3Config The POP3 configuration providing credentials and server settings
     * @param pop3Properties Optional additional POP3 properties applied to POP3 session (may be <code>null</code>)
     * @param monitorFailedAuthentication <code>true</code> to monitor failed authentication; otherwise <code>false</code>
     * @param accountId The account identifier
     * @param session The session providing user information
     * @param errorOnMissingUIDL <code>true</code> to throw an error on missing UIDL; otherwise <code>false</code> to ignore
     * @return A connected instance of {@link POP3Store}
     * @throws OXException If establishing a connected instance of {@link POP3Store} fails
     */
    public static POP3StoreResult getPOP3Store(POP3Config pop3Config, Properties pop3Properties, boolean monitorFailedAuthentication, int accountId, Session session, boolean errorOnMissingUIDL, boolean forceSecure) throws OXException {
        try {
            final boolean tmpDownEnabled = (POP3Properties.getInstance().getPOP3TemporaryDown() > 0);
            if (tmpDownEnabled) {
                /*
                 * Check if POP3 server is marked as being (temporary) down since connecting to it failed before
                 */
                checkTemporaryDown(pop3Config);
            }
            /*
             * Check capabilities
             */
            final IPOP3Properties pop3ConfProps = (IPOP3Properties) pop3Config.getMailProperties();
            final String server = pop3Config.getServer();
            final int port = pop3Config.getPort();
            String staticCapabilities;
            try {
                staticCapabilities =
                    POP3CapabilityCache.getCapability(
                        InetAddress.getByName(IDNA.toASCII(server)),
                        port,
                        pop3Config.isSecure(),
                        pop3ConfProps,
                        pop3Config.getLogin());
            } catch (final Exception e) {
                LOG.warn("Couldn't detect capabilities from POP3 server \"{}\" with login \"{}\" (user={}, context={})", server, pop3Config.getLogin(), session.getUserId(), session.getContextId(), e);
                staticCapabilities = POP3CapabilityCache.getDeaultCapabilities();
            }
            /*
             * JavaMail POP3 implementation requires capabilities "UIDL" and "TOP"
             */
            final POP3StoreResult result = new POP3StoreResult(staticCapabilities);
            final String login = pop3Config.getLogin();
            boolean responseCodeAware = staticCapabilities.indexOf("RESP-CODES") >= 0;
            String tmpPass = pop3Config.getPassword();
            if (tmpPass != null) {
                try {
                    tmpPass = new String(tmpPass.getBytes(POP3Properties.getInstance().getPOP3AuthEnc()), com.openexchange.java.Charsets.ISO_8859_1);
                } catch (final UnsupportedEncodingException e) {
                    LOG.error("", e);
                }
            }
            /*
             * Check for already failed authentication
             */
            checkFailedAuths(login, tmpPass);
            /*
             * Get properties
             */
            final Properties pop3Props = POP3SessionProperties.getDefaultSessionProperties();
            if ((null != pop3Properties) && !pop3Properties.isEmpty()) {
                pop3Props.putAll(pop3Properties);
            }
            /*
             * Set timeouts
             */
            final int timeout = pop3ConfProps.getPOP3Timeout();
            if (timeout > 0) {
                pop3Props.put("mail.pop3.timeout", String.valueOf(timeout));
            }
            final int connectionTimeout = pop3ConfProps.getPOP3ConnectionTimeout();
            if (connectionTimeout > 0) {
                pop3Props.put("mail.pop3.connectiontimeout", String.valueOf(connectionTimeout));
            }
            /*
             * Check if a secure POP3 connection should be established.
             *
             * With JavaMail v1.4.3 the JavaMail POP3 provider supports to start in plain text mode and
             * then switching the connection into TLS mode using the STLS command.
             */
            final String sPort = String.valueOf(port);
            final String socketFactoryClass = SSLSocketFactoryProvider.getDefault().getClass().getName();
            String protocols = pop3Config.getPOP3Properties().getSSLProtocols();
            String cipherSuites = pop3Config.getPOP3Properties().getSSLCipherSuites();
            SSLConfigurationService sslConfigService = POP3ServiceRegistry.getServiceRegistry().getService(SSLConfigurationService.class);
            if (pop3Config.isSecure()) {
                pop3Props.put("mail.pop3.socketFactory.class", socketFactoryClass);
                pop3Props.put("mail.pop3.socketFactory.port", sPort);
                pop3Props.put("mail.pop3.socketFactory.fallback", "false");
                /*
                 * Specify SSL protocols
                 */
                if (Strings.isNotEmpty(protocols)) {
                    pop3Props.put("mail.pop3.ssl.protocols", protocols);
                } else {
                    if (null == sslConfigService) {
                        throw ServiceExceptionCode.absentService(SSLConfigurationService.class);
                    }
                    pop3Props.put("mail.pop3.ssl.protocols", Strings.toWhitespaceSeparatedList(sslConfigService.getSupportedProtocols()));
                }
                /*
                 * Specify SSL cipher suites
                 */
                if (Strings.isNotEmpty(cipherSuites)) {
                    pop3Props.put("mail.pop3.ssl.ciphersuites", cipherSuites);
                } else {
                    if (null == sslConfigService) {
                        throw ServiceExceptionCode.absentService(SSLConfigurationService.class);
                    }
                    pop3Props.put("mail.pop3.ssl.ciphersuites", Strings.toWhitespaceSeparatedList(sslConfigService.getSupportedCipherSuites()));
                }
            } else {
                /*
                 * Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection.
                 */
                if (forceSecure && staticCapabilities.indexOf("STLS") < 0) {
                    throw MailExceptionCode.NON_SECURE_DENIED.create(pop3Config.getServer());
                }
                pop3Props.put("mail.pop3.starttls.enable", "true");
                /*
                 * Specify the javax.net.ssl.SSLSocketFactory class, this class will be used to create POP3 SSL sockets if TLS handshake says
                 * so.
                 */
                pop3Props.put("mail.pop3.socketFactory.port", sPort);
                pop3Props.put("mail.pop3.ssl.socketFactory.class", socketFactoryClass);
                pop3Props.put("mail.pop3.ssl.socketFactory.port", sPort);
                pop3Props.put("mail.pop3.socketFactory.fallback", "false");
                /*
                 * Specify SSL protocols
                 */
                if (Strings.isNotEmpty(protocols)) {
                    pop3Props.put("mail.pop3.ssl.protocols", protocols);
                } else {
                    if (null == sslConfigService) {
                        throw ServiceExceptionCode.absentService(SSLConfigurationService.class);
                    }
                    pop3Props.put("mail.pop3.ssl.protocols", Strings.toWhitespaceSeparatedList(sslConfigService.getSupportedProtocols()));
                }
                /*
                 * Specify SSL cipher suites
                 */
                if (Strings.isNotEmpty(cipherSuites)) {
                    pop3Props.put("mail.pop3.ssl.ciphersuites", cipherSuites);
                } else {
                    if (null == sslConfigService) {
                        throw ServiceExceptionCode.absentService(SSLConfigurationService.class);
                    }
                    pop3Props.put("mail.pop3.ssl.ciphersuites", Strings.toWhitespaceSeparatedList(sslConfigService.getSupportedCipherSuites()));
                }
                // pop3Props.put("mail.pop3.ssl.enable", "true");
                /*
                 * Needed for JavaMail >= 1.4
                 */
                // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
            }
            /*
             * Apply properties to POP3 session
             */
            final javax.mail.Session pop3Session = javax.mail.Session.getInstance(pop3Props, null);
            /*
             * Check if debug should be enabled
             */
            if (Boolean.parseBoolean(pop3Session.getProperty(MimeSessionPropertyNames.PROP_MAIL_DEBUG))) {
                pop3Session.setDebug(true);
                pop3Session.setDebugOut(System.out);
            } else {
                pop3Session.setDebug(false);
                pop3Session.setDebugOut(EMPTY_PRINTER);
            }
            /*
             * Get store
             */
            final POP3Store pop3Store = (POP3Store) pop3Session.getStore(POP3Provider.PROTOCOL_POP3.getName());
            /*
             * ... and connect
             */
            String capabilities = staticCapabilities;
            try {
                pop3Store.connect(server, port, login, tmpPass);

                // Log connect
                AuditLogService auditLogService = getServiceRegistry().getOptionalService(AuditLogService.class);
                if (null != auditLogService) {
                    String eventId = MailAccount.DEFAULT_ID == accountId ? "pop3.primary.login" : "pop3.external.login";
                    auditLogService.log(eventId, DefaultAttribute.valueFor(Name.LOGIN, session.getLoginName()), DefaultAttribute.valueFor(Name.IP_ADDRESS, session.getLocalIp()), DefaultAttribute.timestampFor(new Date()), DefaultAttribute.arbitraryFor("pop3.login", login), DefaultAttribute.arbitraryFor("pop3.server", server), DefaultAttribute.arbitraryFor("pop3.port", Integer.toString(port)));
                }

                // Fetch capabilities again
                final Map<String, String> caps = pop3Store.reinitCapabilities();
                if (!caps.isEmpty()) {
                    final StringBuilder sb = new StringBuilder(128);
                    for (final String cap : caps.keySet()) {
                        sb.append(cap).append('\n');
                    }
                    capabilities = sb.toString();
                    result.setCapabilities(capabilities);
                    responseCodeAware = caps.containsKey("RESP-CODES");
                }
            } catch (final AuthenticationFailedException e) {
                if (monitorFailedAuthentication) {
                    /*
                     * Remember failed authentication's credentials (for a short amount of time) to speed-up subsequent connect trials
                     */
                    failedAuths.put(new LoginAndPass(login, tmpPass), Long.valueOf(System.currentTimeMillis()));
                }
                // Fetch capabilities again
                final Map<String, String> caps = pop3Store.reinitCapabilities();
                if (!caps.isEmpty()) {
                    final StringBuilder sb = new StringBuilder(128);
                    for (final String cap : caps.keySet()) {
                        sb.append(cap).append('\n');
                    }
                    capabilities = sb.toString();
                    result.setCapabilities(capabilities);
                    responseCodeAware = caps.containsKey("RESP-CODES");
                }
                if (responseCodeAware && e.getMessage().indexOf("[LOGIN-DELAY]") >= 0) {
                    final int seconds = parseLoginDelaySeconds(capabilities);
                    if (-1 == seconds) {
                        throw POP3ExceptionCode.LOGIN_DELAY.create(e, server, login, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e.getMessage());
                    }
                    throw POP3ExceptionCode.LOGIN_DELAY2.create(e, server, login, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), Integer.valueOf(seconds), e.getMessage());
                }
                throw e;
            } catch (final MessagingException e) {
                final Exception nested = e.getNextException();
                if (nested != null) {
                    if (SSLHandshakeException.class.isInstance(nested)) {
                        throw SSLExceptionCode.UNTRUSTED_CERTIFICATE.create(server);
                    }
                    if (nested instanceof IOException) {
                        throw MimeMailExceptionCode.CONNECT_ERROR.create(e, pop3Config.getServer(), pop3Config.getLogin());
                    } else if (tmpDownEnabled && SocketTimeoutException.class.isInstance(e.getNextException())) {
                        /*
                         * TODO: Re-think if exception's message should be part of condition or just checking if nested exception is an instance of
                         * SocketTimeoutException
                         */
                        /*
                         * Remember a timed-out POP3 server on connect attempt
                         */
                        timedOutServers.put(new HostAndPort(server, port), Long.valueOf(System.currentTimeMillis()));
                    }
                }

                throw e;
            }
            /*
             * Check for needed capabilities
             */
            final boolean hasTop = (capabilities.indexOf("TOP") >= 0);
            final boolean hasUidl = (capabilities.indexOf("UIDL") >= 0);
            if (!hasTop || !hasUidl) {
                final POP3Folder inbox = (POP3Folder) pop3Store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                try {
                    final POP3Prober prober = new POP3Prober(pop3Store, inbox);
                    if (!hasUidl && !prober.probeUIDL()) {
                        /*-
                         * Probe failed.
                         * Avoid fetching UIDs when further working with JavaMail API
                         */
                        if (errorOnMissingUIDL) {
                            throw POP3ExceptionCode.MISSING_REQUIRED_CAPABILITY.create("UIDL",
                                server,
                                login,
                                Integer.valueOf(session.getUserId()),
                                Integer.valueOf(session.getContextId()));
                        }
                        result.addWarning(POP3ExceptionCode.EXPUNGE_MODE_ONLY.create("UIDL",
                            server,
                            login,
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(session.getContextId())));
                    }
                    if (!hasTop && !prober.probeTOP()) {
                        /*-
                         * Probe failed.
                         * Mandatory to further work with JavaMail API
                         */
                        throw POP3ExceptionCode.MISSING_REQUIRED_CAPABILITY.create("TOP",
                            server,
                            login,
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(session.getContextId()));
                    }
                    /*
                     * Check for warnings
                     */
                    final List<Exception> warnings = prober.getWarnings();
                    if (!warnings.isEmpty()) {
                        final org.slf4j.Logger logger = LOG;
                        if (logger.isDebugEnabled()) {
                            for (final Exception warning : warnings) {
                                logger.debug("Exception during probing POP3 server \"{}\".", server, warning);
                            }
                        }
                    }
                } finally {
                    inbox.close(false);
                }
            }
            result.setPop3Store(pop3Store);
            return result;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, pop3Config, session);
        }
    }

    private static void checkFailedAuths(final String login, final String pass) throws AuthenticationFailedException {
        final LoginAndPass key = new LoginAndPass(login, pass);
        final Long range = failedAuths.get(key);
        if (range != null) {
            // TODO: Put time-out to pop3.properties
            if (System.currentTimeMillis() - range.longValue() <= 10000) {
                throw new AuthenticationFailedException("Login failed: authentication failure");
            }
            failedAuths.remove(key);
        }
    }

    private static void checkTemporaryDown(final POP3Config pop3Config) throws OXException {
        final HostAndPort key = new HostAndPort(pop3Config.getServer(), pop3Config.getPort());
        final Long range = timedOutServers.get(key);
        if (range != null) {
            if (System.currentTimeMillis() - range.longValue() <= POP3Properties.getInstance().getPOP3TemporaryDown()) {
                /*
                 * Still treated as being temporary broken
                 */
                throw POP3ExceptionCode.CONNECT_ERROR.create(pop3Config.getServer(), pop3Config.getLogin());
            }
            timedOutServers.remove(key);
        }
    }

    /*-
     * ########################################################################################################
     * ############################################ HELPER CLASSES ############################################
     * ########################################################################################################
     */

    private static final class LoginAndPass {

        private final String login;

        private final String pass;

        private final int hashCode;

        public LoginAndPass(final String login, final String pass) {
            super();
            this.login = login;
            this.pass = pass;
            hashCode = (login.hashCode()) ^ (pass.hashCode());
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LoginAndPass other = (LoginAndPass) obj;
            if (login == null) {
                if (other.login != null) {
                    return false;
                }
            } else if (!login.equals(other.login)) {
                return false;
            }
            if (pass == null) {
                if (other.pass != null) {
                    return false;
                }
            } else if (!pass.equals(other.pass)) {
                return false;
            }
            return true;
        }

    }

    private static final class HostAndPort {

        private final String host;

        private final int port;

        private final int hashCode;

        public HostAndPort(final String host, final int port) {
            super();
            if (port < 0 || port > 0xFFFF) {
                throw new IllegalArgumentException("port out of range:" + port);
            }
            if (host == null) {
                throw new IllegalArgumentException("hostname can't be null");
            }
            this.host = host;
            this.port = port;
            int result = HashCodeUtil.SEED;
            result = HashCodeUtil.hash(result, host);
            result = HashCodeUtil.hash(result, port);
            hashCode = result;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HostAndPort other = (HostAndPort) obj;
            if (host == null) {
                if (other.host != null) {
                    return false;
                }
            } else if (!host.equals(other.host)) {
                return false;
            }
            if (port != other.port) {
                return false;
            }
            return true;
        }
    }

}
