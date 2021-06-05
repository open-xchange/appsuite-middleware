/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.autoconfig.tools;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.autoconfig.sources.Guess.PROP_GENERAL_CONTEXT_ID;
import static com.openexchange.mail.autoconfig.sources.Guess.PROP_GENERAL_USER_ID;
import static com.openexchange.mail.autoconfig.sources.Guess.PROP_SMTP_AUTH_SUPPORTED;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.SocketFetcher;

/**
 * {@link MailValidator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class MailValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailValidator.class);

    private static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    private static final int DEFAULT_TIMEOUT = 10000;

    /**
     * Validates for successful authentication against specified IMAP server.
     *
     * @param host The IMAP host
     * @param port The IMAP port
     * @param connectMode The connect mode to use
     * @param user The login
     * @param pwd The password
     * @param optProperties The properties or <code>null</code>
     * @return <code>true</code> for successful authentication, otherwise <code>false</code> for failed authentication
     */
    public static boolean validateImap(String host, int port, ConnectMode connectMode, String user, String pwd, Map<String, Object> optProperties) {
        Store store = null;
        try {
            ConfigurationService configuration = Services.getService(ConfigurationService.class);
            SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
            String socketFactoryClass = factoryProvider.getDefault().getClass().getName();
            Properties props = MimeDefaultSession.getDefaultMailProperties();
            if (ConnectMode.SSL == connectMode) {
                props.put("mail.imap.socketFactory.class", socketFactoryClass);
            } else if (ConnectMode.STARTTLS == connectMode) {
                props.put("mail.imap.starttls.required", Boolean.TRUE);
                props.put("mail.imap.ssl.trust", "*");
            } else {
                props.put("mail.imap.ssl.socketFactory.class", socketFactoryClass);
                props.put("mail.imap.ssl.socketFactory.port", I(port));
                props.put("mail.imap.starttls.enable", Boolean.TRUE);
                props.put("mail.imap.ssl.trust", "*");
                {
                    String defaultValue = "SSLv3 TLSv1";
                    String sslProtocols = configuration == null ? defaultValue : configuration.getProperty("com.openexchange.imap.ssl.protocols", defaultValue).trim();
                    props.put("mail.imap.ssl.protocols", sslProtocols);
                }
                {
                    String defaultValue = "";
                    String cipherSuites = configuration == null ? defaultValue : configuration.getProperty("com.openexchange.imap.ssl.ciphersuites", defaultValue).trim();
                    if (Strings.isNotEmpty(cipherSuites)) {
                        props.put("mail.imap.ssl.ciphersuites", cipherSuites);
                    }
                }
            }
            props.put("mail.imap.socketFactory.fallback", "false");
            props.put("mail.imap.connectiontimeout", I(DEFAULT_CONNECT_TIMEOUT));
            props.put("mail.imap.timeout", I(DEFAULT_TIMEOUT));
            props.put("mail.imap.socketFactory.port", I(port));
            if (configuration != null) {
                String authenc = configuration.getProperty("com.openexchange.imap.imapAuthEnc", "UTF-8").trim();
                if (Strings.isNotEmpty(authenc)) {
                    props.put("mail.imap.login.encoding", authenc);
                }
            }

            if (optProperties != null) {
                Integer contextId = (Integer) optProperties.get(PROP_GENERAL_CONTEXT_ID);
                Integer userId = (Integer) optProperties.get(PROP_GENERAL_USER_ID);
                if (contextId != null && userId != null) {
                    if (Utils.isPrimaryImapAccount(host, port, userId.intValue(), contextId.intValue())) {
                        props.put("mail.imap.primary", "true");
                    }
                }
            }

            Session session = Session.getInstance(props, null);
            store = session.getStore("imap");
            store.connect(host, port, user, pwd);
            closeSafe(store);
            store = null;
        } catch (AuthenticationFailedException e) {
            return false;
        } catch (MessagingException e) {
            return false;
        } finally {
            closeSafe(store);
        }
        return true;
    }

    /**
     * Validates for successful authentication against specified POP3 server.
     *
     * @param host The POP3 host
     * @param port The POP3 port
     * @param connectMode The connect mode to use
     * @param user The login
     * @param pwd The password
     * @param optProperties The properties or <code>null</code>
     * @return <code>true</code> for successful authentication, otherwise <code>false</code> for failed authentication
     */
    public static boolean validatePop3(String host, int port, ConnectMode connectMode, String user, String pwd, Map<String, Object> optProperties) {
        Store store = null;
        try {
            Properties props = MimeDefaultSession.getDefaultMailProperties();
            SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
            String socketFactoryClass = factoryProvider.getDefault().getClass().getName();
            if (ConnectMode.SSL == connectMode) {
                props.put("mail.pop3.socketFactory.class", socketFactoryClass);
            } else if (ConnectMode.STARTTLS == connectMode) {
                props.put("mail.pop3.starttls.required", Boolean.TRUE);
                props.put("mail.pop3.ssl.trust", "*");
            } else {
                props.put("mail.pop3.ssl.socketFactory.class", socketFactoryClass);
                props.put("mail.pop3.ssl.socketFactory.port", I(port));
                props.put("mail.pop3.starttls.enable", Boolean.TRUE);
                props.put("mail.pop3.ssl.trust", "*");
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String sslProtocols = configuration.getProperty("com.openexchange.pop3.ssl.protocols", "SSLv3 TLSv1").trim();
                    props.put("mail.pop3.ssl.protocols", sslProtocols);
                }
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String cipherSuites = configuration.getProperty("com.openexchange.pop3.ssl.ciphersuites", "").trim();
                    if (Strings.isNotEmpty(cipherSuites)) {
                        props.put("mail.pop3.ssl.ciphersuites", cipherSuites);
                    }
                }
            }
            props.put("mail.pop3.socketFactory.fallback", "false");
            props.put("mail.pop3.socketFactory.port", I(port));
            props.put("mail.pop3.connectiontimeout", I(DEFAULT_CONNECT_TIMEOUT));
            props.put("mail.pop3.timeout", I(DEFAULT_TIMEOUT));
            Session session = Session.getInstance(props, null);
            store = session.getStore("pop3");
            store.connect(host, port, user, pwd);
            closeSafe(store);
            store = null;
        } catch (AuthenticationFailedException e) {
            return false;
        } catch (MessagingException e) {
            return false;
        } finally {
            closeSafe(store);
        }
        return true;
    }

    /**
     * Validates for successful authentication against specified SMTP server.
     *
     * @param host The SMTP host
     * @param port The SMTP port
     * @param connectMode The connect mode to use
     * @param user The login
     * @param pwd The password
     * @return <code>true</code> for successful authentication, otherwise <code>false</code> for failed authentication
     */
    public static boolean validateSmtp(String host, int port, ConnectMode connectModes, String user, String pwd) {
        return validateSmtp(host, port, connectModes, user, pwd, null);
    }

    /**
     * Validates for successful authentication against specified SMTP server.
     *
     * @param host The SMTP host
     * @param port The SMTP port
     * @param connectMode The connect mode to use
     * @param user The login
     * @param pwd The password
     * @param optProperties The optional container for arbitrary properties
     * @return <code>true</code> for successful authentication, otherwise <code>false</code> for failed authentication
     */
    public static boolean validateSmtp(String host, int port, ConnectMode connectMode, String user, String pwd, Map<String, Object> optProperties) {
        Transport transport = null;
        try {
            SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
            String socketFactoryClass = factoryProvider.getDefault().getClass().getName();
            Properties props = MimeDefaultSession.getDefaultMailProperties();
            if (ConnectMode.SSL == connectMode) {
                props.put("mail.smtp.socketFactory.class", socketFactoryClass);
            } else if (ConnectMode.STARTTLS == connectMode) {
                props.put("mail.smtp.starttls.required", Boolean.TRUE);
                props.put("mail.smtp.ssl.trust", "*");
            } else {
                props.put("mail.smtp.ssl.socketFactory.class", socketFactoryClass);
                props.put("mail.smtp.ssl.socketFactory.port", I(port));
                props.put("mail.smtp.starttls.enable", Boolean.TRUE);
                props.put("mail.smtp.ssl.trust", "*");
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String sslProtocols = configuration.getProperty("com.openexchange.smtp.ssl.protocols", "SSLv3 TLSv1").trim();
                    props.put("mail.smtp.ssl.protocols", sslProtocols);
                }
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String cipherSuites = configuration.getProperty("com.openexchange.smtp.ssl.ciphersuites", "").trim();
                    if (Strings.isNotEmpty(cipherSuites)) {
                        props.put("mail.smtp.ssl.ciphersuites", cipherSuites);
                    }
                }
            }
            props.put("mail.smtp.socketFactory.port", I(port));
            //props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.connectiontimeout", I(DEFAULT_CONNECT_TIMEOUT));
            props.put("mail.smtp.timeout", I(DEFAULT_TIMEOUT));
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.auth", "true");
            Session session = Session.getInstance(props, null);
            transport = session.getTransport("smtp");
            transport.connect(host, port, user, pwd);

            if (null != optProperties) {
                final SMTPTransport smtpTransport = (SMTPTransport) transport;
                if (!smtpTransport.supportsExtension("AUTH") && !smtpTransport.supportsExtension("AUTH=LOGIN")) {
                    // No authentication mechanism supported
                    optProperties.put(PROP_SMTP_AUTH_SUPPORTED, Boolean.FALSE);
                }
            }

            closeSafe(transport);
            transport = null;
        } catch (AuthenticationFailedException e) {
            return false;
        } catch (MessagingException e) {
            return false;
        } finally {
            closeSafe(transport);
        }
        return true;
    }

    // ------------------------------------------------------- Connect tests ---------------------------------------------------------

    /**
     * Checks if a (SSL) socket connection can be established to the specified IMAP end-point (host & port)
     *
     * @param host The IMAP host
     * @param port The IMAP port
     * @param secure Whether to create an SSL socket or a plain one.
     * @return <code>true</code> if such a socket could be successfully linked to the given IMAP end-point; otherwise <code>false</code>
     */
    public static boolean tryImapConnect(String host, int port, boolean secure) {
        return tryConnect(host, port, secure, "A11 LOGOUT\r\n", "imap");
    }

    /**
     * Checks if a (SSL) socket connection can be established to the specified SMTP end-point (host & port)
     *
     * @param host The SMTP host
     * @param port The SMTP port
     * @param secure Whether to create an SSL socket or a plain one.
     * @return <code>true</code> if such a socket could be successfully linked to the given SMTP end-point; otherwise <code>false</code>
     */
    public static boolean trySmtpConnect(String host, int port, boolean secure) {
        return tryConnect(host, port, secure, "QUIT\r\n", "smtp");
    }

    /**
     * Checks if a (SSL) socket connection can be established to the specified POP3 end-point (host & port)
     *
     * @param host The POP3 host
     * @param port The POP3 port
     * @param secure Whether to create an SSL socket or a plain one.
     * @return <code>true</code> if such a socket could be successfully linked to the given POP3 end-point; otherwise <code>false</code>
     */
    public static boolean tryPop3Connect(String host, int port, boolean secure) {
        return tryConnect(host, port, secure, "QUIT\r\n", "pop3");
    }

    private static boolean tryConnect(String host, int port, boolean secure, String closePhrase, String name) {
        Socket s = null;
        try {
            // Establish socket connection
            s = SocketFetcher.getSocket(host, port, createProps(name, port, secure), "mail." + name, false);
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            if (null == in || null == out) {
                return false;
            }

            // Read IMAP server greeting on connect
            boolean eol = false;
            boolean skipLF = false;
            int i = -1;
            while (!eol && ((i = in.read()) != -1)) {
                final char c = (char) i;
                if (c == '\r') {
                    eol = true;
                    skipLF = true;
                } else if (c == '\n') {
                    eol = true;
                    skipLF = false;
                }
                // else; Ignore
            }

            // Consume final LF
            if (skipLF && -1 == in.read()) {
                LOGGER.trace("Final LF should have been read but the end of the stream was already reached.");
            }

            // Close
            out.write(closePhrase.getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
        } catch (Exception e) {
            LOGGER.trace("Unable to connect.", e);
            return false;
        } finally {
            Streams.close(s);
        }
        return true;
    }

    private static void closeSafe(AutoCloseable s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception e) {
                // Ignore
                LOGGER.trace("Unable to close resource.", e);
            }
        }
    }

    private static Properties createProps(String name, int port, boolean secure) {
        Properties imapprops = MimeDefaultSession.getDefaultMailProperties();
        {
            int connectionTimeout = DEFAULT_CONNECT_TIMEOUT;
            if (connectionTimeout > 0) {
                imapprops.put("mail." + name + ".connectiontimeout", Integer.toString(connectionTimeout));
            }
        }
        {
            int timeout = DEFAULT_TIMEOUT;
            if (timeout > 0) {
                imapprops.put("mail." + name + ".timeout", Integer.toString(timeout));
            }
        }
        SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
        final String socketFactoryClass = factoryProvider.getDefault().getClass().getName();
        final String sPort = Integer.toString(port);
        if (secure) {
            imapprops.put("mail." + name + ".socketFactory.class", socketFactoryClass);
            imapprops.put("mail." + name + ".socketFactory.port", sPort);
            imapprops.put("mail." + name + ".socketFactory.fallback", "false");
        } else {
            imapprops.put("mail." + name + ".socketFactory.port", sPort);
            imapprops.put("mail." + name + ".ssl.socketFactory.class", socketFactoryClass);
            imapprops.put("mail." + name + ".ssl.socketFactory.port", sPort);
            imapprops.put("mail." + name + ".socketFactory.fallback", "false");
        }
        return imapprops;
    }

}
