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

package com.openexchange.mail.autoconfig.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Service;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.ssl.SSLSocketFactoryProvider;
import com.sun.mail.smtp.SMTPTransport;

/**
 * {@link MailValidator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class MailValidator {

    private static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    private static final int DEFAULT_TIMEOUT = 10000;

    /**
     * Validates for successful authentication against specified IMAP server.
     *
     * @param host The IMAP host
     * @param port The IMAP port
     * @param secure Whether to establish a secure connection
     * @param requireTls Whether STARTTLS is required
     * @param isOAuth <code>true</code> to perform XOAUTH2 authentication mechanism; otherwise <code>false</code>
     * @param user The login
     * @param pwd The password
     * @return <code>true</code> for successful authentication, otherwise <code>false</code> for failed authentication
     */
    public static boolean validateImap(String host, int port, boolean secure, boolean requireTls, boolean isOAuth, String user, String pwd) {
        Store store = null;
        try {
            String socketFactoryClass = SSLSocketFactoryProvider.getDefault().getClass().getName();
            Properties props = new Properties();
            if (secure) {
                props.put("mail.imap.socketFactory.class", socketFactoryClass);
            } else if (requireTls) {
                props.put("mail.imap.starttls.required", true);
                props.put("mail.imap.ssl.trust", "*");
            } else {
                props.put("mail.imap.ssl.socketFactory.class", socketFactoryClass);
                props.put("mail.imap.ssl.socketFactory.port", port);
                props.put("mail.imap.starttls.enable", true);
                props.put("mail.imap.ssl.trust", "*");
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String sslProtocols = configuration.getProperty("com.openexchange.imap.ssl.protocols", "SSLv3 TLSv1").trim();
                    props.put("mail.imap.ssl.protocols", sslProtocols);
                }
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String cipherSuites = configuration.getProperty("com.openexchange.imap.ssl.ciphersuites", "").trim();
                    if (!Strings.isEmpty(cipherSuites)) {
                        props.put("mail.imap.ssl.ciphersuites", cipherSuites);
                    }
                }
            }
            props.put("mail.imap.socketFactory.fallback", "false");
            props.put("mail.imap.connectiontimeout", DEFAULT_CONNECT_TIMEOUT);
            props.put("mail.imap.timeout", DEFAULT_TIMEOUT);
            props.put("mail.imap.socketFactory.port", port);
            if (isOAuth) {
                props.put("mail.imap.auth.mechanisms", "XOAUTH2");
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
     * @param secure Whether to establish a secure connection
     * @param requireTls Whether STARTTLS is required
     * @param isOAuth <code>true</code> to perform XOAUTH2 authentication mechanism; otherwise <code>false</code>
     * @param user The login
     * @param pwd The password
     * @return <code>true</code> for successful authentication, otherwise <code>false</code> for failed authentication
     */
    public static boolean validatePop3(String host, int port, boolean secure, boolean requireTls, boolean isOAuth, String user, String pwd) {
        Store store = null;
        try {
            Properties props = new Properties();
            String socketFactoryClass = SSLSocketFactoryProvider.getDefault().getClass().getName();
            if (secure) {
                props.put("mail.pop3.socketFactory.class", socketFactoryClass);
            } else if (requireTls) {
                props.put("mail.pop3.starttls.required", true);
                props.put("mail.pop3.ssl.trust", "*");
            } else {
                props.put("mail.pop3.ssl.socketFactory.class", socketFactoryClass);
                props.put("mail.pop3.ssl.socketFactory.port", port);
                props.put("mail.pop3.starttls.enable", true);
                props.put("mail.pop3.ssl.trust", "*");
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String sslProtocols = configuration.getProperty("com.openexchange.pop3.ssl.protocols", "SSLv3 TLSv1").trim();
                    props.put("mail.pop3.ssl.protocols", sslProtocols);
                }
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String cipherSuites = configuration.getProperty("com.openexchange.pop3.ssl.ciphersuites", "").trim();
                    if (!Strings.isEmpty(cipherSuites)) {
                        props.put("mail.pop3.ssl.ciphersuites", cipherSuites);
                    }
                }
            }
            props.put("mail.pop3.socketFactory.fallback", "false");
            props.put("mail.pop3.socketFactory.port", port);
            props.put("mail.pop3.connectiontimeout", DEFAULT_CONNECT_TIMEOUT);
            props.put("mail.pop3.timeout", DEFAULT_TIMEOUT);
            if (isOAuth) {
                props.put("mail.pop3.auth.mechanisms", "XOAUTH2");
             }
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
     * @param secure Whether to establish a secure connection
     * @param requireTls Whether STARTTLS is required
     * @param isOAuth <code>true</code> to perform XOAUTH2 authentication mechanism; otherwise <code>false</code>
     * @param user The login
     * @param pwd The password
     * @return <code>true</code> for successful authentication, otherwise <code>false</code> for failed authentication
     */
    public static boolean validateSmtp(String host, int port, boolean secure, boolean requireTls, boolean isOAuth, String user, String pwd) {
        return validateSmtp(host, port, secure, requireTls, isOAuth, user, pwd, null);
    }

    /**
     * Validates for successful authentication against specified SMTP server.
     *
     * @param host The SMTP host
     * @param port The SMTP port
     * @param secure Whether to establish a secure connection
     * @param requireTls Whether STARTTLS is required
     * @param isOAuth <code>true</code> to perform XOAUTH2 authentication mechanism; otherwise <code>false</code>
     * @param user The login
     * @param pwd The password
     * @param optProperties The optional container for arbitrary properties
     * @return <code>true</code> for successful authentication, otherwise <code>false</code> for failed authentication
     */
    public static boolean validateSmtp(String host, int port, boolean secure, boolean requireTls, boolean isOAuth, String user, String pwd, Map<String, Object> optProperties) {
        Transport transport = null;
        try {
            String socketFactoryClass = SSLSocketFactoryProvider.getDefault().getClass().getName();
            Properties props = new Properties();
            if (secure) {
                props.put("mail.smtp.socketFactory.class", socketFactoryClass);
            } else if (requireTls) {
                props.put("mail.smtp.starttls.required", true);
                props.put("mail.smtp.ssl.trust", "*");
            } else {
                props.put("mail.smtp.ssl.socketFactory.class", socketFactoryClass);
                props.put("mail.smtp.ssl.socketFactory.port", port);
                props.put("mail.smtp.starttls.enable", true);
                props.put("mail.smtp.ssl.trust", "*");
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String sslProtocols = configuration.getProperty("com.openexchange.smtp.ssl.protocols", "SSLv3 TLSv1").trim();
                    props.put("mail.smtp.ssl.protocols", sslProtocols);
                }
                {
                    final ConfigurationService configuration = Services.getService(ConfigurationService.class);
                    final String cipherSuites = configuration.getProperty("com.openexchange.smtp.ssl.ciphersuites", "").trim();
                    if (!Strings.isEmpty(cipherSuites)) {
                        props.put("mail.smtp.ssl.ciphersuites", cipherSuites);
                    }
                }
            }
            props.put("mail.smtp.socketFactory.port", port);
            //props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.connectiontimeout", DEFAULT_CONNECT_TIMEOUT);
            props.put("mail.smtp.timeout", DEFAULT_TIMEOUT);
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.auth", "true");
            if (isOAuth) {
               props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            }
            Session session = Session.getInstance(props, null);
            transport = session.getTransport("smtp");
            transport.connect(host, port, user, pwd);

            if (null != optProperties) {
                final SMTPTransport smtpTransport = (SMTPTransport) transport;
                if (!smtpTransport.supportsExtension("AUTH") && !smtpTransport.supportsExtension("AUTH=LOGIN")) {
                    // No authentication mechanism supported
                    optProperties.put("smtp.auth-supported", Boolean.FALSE);
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

    public static boolean checkForImap(String host, int port, boolean secure) throws IOException {
        Socket s = null;
        String greeting = null;
        try {
            if (secure) {
                s = SSLSocketFactoryProvider.getDefault().createSocket();
            } else {
                s = new Socket();
            }

            /*
             * Set connect timeout
             */
            s.connect(new InetSocketAddress(host, port), DEFAULT_CONNECT_TIMEOUT);
            s.setSoTimeout(DEFAULT_TIMEOUT);
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            StringBuilder sb = new StringBuilder(512);
            /*
             * Read IMAP server greeting on connect
             */
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
                } else {
                    sb.append(c);
                }
            }
            greeting = sb.toString();

            if (skipLF) {
                /*
                 * Consume final LF
                 */
                i = in.read();
                skipLF = false;
            }

            out.write("A11 LOGOUT\r\n".getBytes());
            out.flush();
        } catch (Exception e) {
            return false;
        } finally {
            closeSafe(s);
        }
        return greeting != null;
    }

    public static boolean checkForSmtp(String host, int port, boolean secure) throws IOException {
        Socket s = null;
        String greeting = null;
        try {
            if (secure) {
                s = SSLSocketFactoryProvider.getDefault().createSocket();
            } else {
                s = new Socket();
            }
            /*
             * Set connect timeout
             */
            s.connect(new InetSocketAddress(host, port), DEFAULT_CONNECT_TIMEOUT);
            s.setSoTimeout(DEFAULT_TIMEOUT);
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            StringBuilder sb = new StringBuilder(512);
            /*
             * Read IMAP server greeting on connect
             */
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
                } else {
                    sb.append(c);
                }
            }
            greeting = sb.toString();

            if (skipLF) {
                /*
                 * Consume final LF
                 */
                i = in.read();
                skipLF = false;
            }

            out.write("QUIT\r\n".getBytes());
            out.flush();
        } catch (Exception e) {
            return false;
        } finally {
            closeSafe(s);
        }
        return greeting != null;
    }

    public static boolean checkForPop3(String host, int port, boolean secure) throws IOException {
        Socket s = null;
        String greeting = null;
        try {
            if (secure) {
                s = SSLSocketFactoryProvider.getDefault().createSocket();
            } else {
                s = new Socket();
            }
            /*
             * Set connect timeout
             */
            s.connect(new InetSocketAddress(host, port), DEFAULT_CONNECT_TIMEOUT);
            s.setSoTimeout(DEFAULT_TIMEOUT);
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            StringBuilder sb = new StringBuilder(512);
            /*
             * Read IMAP server greeting on connect
             */
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
                } else {
                    sb.append(c);
                }
            }
            greeting = sb.toString();

            if (skipLF) {
                /*
                 * Consume final LF
                 */
                i = in.read();
                skipLF = false;
            }

            out.write("QUIT\r\n".getBytes());
            out.flush();
        } catch (Exception e) {
            return false;
        } finally {
            closeSafe(s);
        }
        return greeting != null;
    }

    private static void closeSafe(Socket s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private static void closeSafe(final Service service) {
        if (null != service) {
            try {
                service.close();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }
}
