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

package com.openexchange.pop3.connect;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.pop3.POP3Exception;
import com.openexchange.pop3.POP3Provider;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.config.POP3Properties;
import com.openexchange.pop3.config.POP3SessionProperties;
import com.openexchange.session.Session;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;
import com.sun.mail.pop3.POP3Store;

/**
 * {@link POP3StoreConnector} - Connects an instance of {@link POP3Store}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3StoreConnector {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3StoreConnector.class);

    private static final Map<HostAndPort, Long> timedOutServers = new ConcurrentHashMap<HostAndPort, Long>();

    private static final Map<LoginAndPass, Long> failedAuths = new ConcurrentHashMap<LoginAndPass, Long>();

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
     * @param session The session providing user information
     * @return A connected instance of {@link POP3Store}
     * @throws MailException If establishing a connected instance of {@link POP3Store} fails
     */
    public static POP3Store getPOP3Store(final POP3Config pop3Config, final Properties pop3Properties, final boolean monitorFailedAuthentication, final Session session) throws MailException {
        try {
            final boolean tmpDownEnabled = (POP3Properties.getInstance().getPOP3TemporaryDown() > 0);
            if (tmpDownEnabled) {
                /*
                 * Check if POP3 server is marked as being (temporary) down since connecting to it failed before
                 */
                checkTemporaryDown(pop3Config);
            }
            String tmpPass = pop3Config.getPassword();
            if (tmpPass != null) {
                try {
                    tmpPass = new String(tmpPass.getBytes(POP3Properties.getInstance().getPOP3AuthEnc()), "ISO-8859-1");
                } catch (final UnsupportedEncodingException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            /*
             * Check for already failed authentication
             */
            final String login = pop3Config.getLogin();
            checkFailedAuths(login, tmpPass);
            /*
             * Get properties
             */
            final Properties pop3Props = POP3SessionProperties.getDefaultSessionProperties();
            if ((null != pop3Properties) && !pop3Properties.isEmpty()) {
                pop3Props.putAll(pop3Properties);
            }
            /*-
             * Check if a secure POP3 connection should be established.
             * 
             * Unfortunately the JavaMail POP3 provider doesn't support to start in plain text mode and
             * switch the connection into TLS mode using the STLS command. Possibly the server will accept
             * connecting to the secure POP3 port in TLS mode.
             */
            final int port = pop3Config.getPort();
            if (pop3Config.isSecure()) {
                pop3Props.put("mail.pop3.socketFactory.class", TrustAllSSLSocketFactory.class.getName());
                pop3Props.put("mail.pop3.socketFactory.port", String.valueOf(port));
                pop3Props.put("mail.pop3.socketFactory.fallback", "false");
                pop3Props.put("mail.pop3.starttls.enable", "true");
                /*
                 * Needed for JavaMail >= 1.4
                 */
                Security.setProperty("ssl.SocketFactory.provider", TrustAllSSLSocketFactory.class.getName());
            }
            /*
             * Apply properties to POP3 session
             */
            final javax.mail.Session pop3Session = javax.mail.Session.getInstance(pop3Props, null);
            /*
             * Check if debug should be enabled
             */
            if (Boolean.parseBoolean(pop3Session.getProperty(MIMESessionPropertyNames.PROP_MAIL_DEBUG))) {
                pop3Session.setDebug(true);
                pop3Session.setDebugOut(System.out);
            }
            /*
             * Get store
             */
            final POP3Store pop3Store = (POP3Store) pop3Session.getStore(POP3Provider.PROTOCOL_POP3.getName());
            /*
             * ... and connect
             */
            final String server = pop3Config.getServer();
            try {
                pop3Store.connect(server, port, login, tmpPass);
            } catch (final AuthenticationFailedException e) {
                if (monitorFailedAuthentication) {
                    /*
                     * Remember failed authentication's credentials (for a short amount of time) to speed-up subsequent connect trials
                     */
                    failedAuths.put(new LoginAndPass(login, tmpPass), Long.valueOf(System.currentTimeMillis()));
                }
                throw e;
            } catch (final MessagingException e) {
                /*
                 * TODO: Re-think if exception's message should be part of condition or just checking if nested exception is an instance of
                 * SocketTimeoutException
                 */
                if (tmpDownEnabled && SocketTimeoutException.class.isInstance(e.getNextException()) && ((SocketTimeoutException) e.getNextException()).getMessage().toLowerCase(
                    Locale.ENGLISH).indexOf("connect timed out") != -1) {
                    /*
                     * Remember a timed-out POP3 server on connect attempt
                     */
                    timedOutServers.put(new HostAndPort(server, port), Long.valueOf(System.currentTimeMillis()));
                }
                throw e;
            }
            return pop3Store;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config, session);
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

    private static void checkTemporaryDown(final POP3Config pop3Config) throws POP3Exception {
        final HostAndPort key = new HostAndPort(pop3Config.getServer(), pop3Config.getPort());
        final Long range = timedOutServers.get(key);
        if (range != null) {
            if (System.currentTimeMillis() - range.longValue() <= POP3Properties.getInstance().getPOP3TemporaryDown()) {
                /*
                 * Still treated as being temporary broken
                 */
                throw new POP3Exception(POP3Exception.Code.CONNECT_ERROR, pop3Config.getServer(), pop3Config.getLogin());
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
            hashCode = (host.toLowerCase(Locale.ENGLISH).hashCode()) ^ port;
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
