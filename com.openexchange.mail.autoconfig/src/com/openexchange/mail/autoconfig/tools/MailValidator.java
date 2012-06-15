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

package com.openexchange.mail.autoconfig.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link MailValidator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class MailValidator {

    public static boolean validateImap(String host, int port, String user, String pwd) {
        try {
            String socketFactoryClass = TrustAllSSLSocketFactory.class.getName();
            Properties props = new Properties();
            if (port == 993) {
                props.put("mail.imap.socketFactory.class", socketFactoryClass);
            } else if (port == 143) {
                props.put("mail.imap.ssl.socketFactory.class", socketFactoryClass);
                props.put("mail.imap.ssl.protocols", "SSLv3 TLSv1");
                props.put("mail.imap.ssl.socketFactory.port", port);
            } else {
                return false;
            }
            props.put("mail.imap.socketFactory.fallback", "false");
            props.put("mail.imap.connectiontimeout", 100);
            props.put("mail.imap.timeout", 100);
            props.put("mail.imap.socketFactory.port", port);
            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imap");
            store.connect(host, port, user, pwd);
            store.close();
        } catch (AuthenticationFailedException e) {
            return false;
        } catch (MessagingException e) {
            return false;
        }
        return true;
    }

    public static boolean validatePop3(String host, int port, String user, String pwd) {
        try {
            Properties props = new Properties();
            String socketFactoryClass = TrustAllSSLSocketFactory.class.getName();
            if (port == 995) {
                props.put("mail.pop3.socketFactory.class", socketFactoryClass);
            } else if (port == 110) {
                props.put("mail.pop3.ssl.socketFactory.class", socketFactoryClass);
                props.put("mail.pop3.ssl.socketFactory.port", port);
                props.put("mail.pop3.ssl.protocols", "SSLv3 TLSv1");
            } else {
                return false;
            }
            props.put("mail.pop3.socketFactory.fallback", "false");
            props.put("mail.pop3.socketFactory.port", port);
            props.put("mail.pop3.connectiontimeout", 1000);
            props.put("mail.pop3.timeout", 1000);
            Session session = Session.getInstance(props, null);
            Store store = session.getStore("pop3");
            store.connect(host, port, user, pwd);
            store.close();
        } catch (AuthenticationFailedException e) {
            return false;
        } catch (MessagingException e) {
            return false;
        }
        return true;
    }

    public static boolean validateSmtp(String host, int port, String user, String pwd) {
        try {
            String socketFactoryClass = TrustAllSSLSocketFactory.class.getName();
            Properties props = new Properties();
            if (port == 465) {
                props.put("mail.smtp.socketFactory.class", socketFactoryClass);
            } else if (port == 25) {
                props.put("mail.smtp.ssl.socketFactory.class", socketFactoryClass);
                props.put("mail.smtp.ssl.socketFactory.port", port);
                props.put("mail.smtp.ssl.protocols", "SSLv3 TLSv1");
            } else {
                return false;
            }
            props.put("mail.smtp.socketFactory.port", port);
            //props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.connectiontimeout", 1000);
            props.put("mail.smtp.timeout", 1000);
            props.put("mail.smtp.socketFactory.fallback", "false");
            Session session = Session.getInstance(props, null);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, port, user, pwd);
            transport.close();
        } catch (AuthenticationFailedException e) {
            return false;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean checkForImap(String host, int port) throws IOException {
        Socket s = null;
        String greeting = null;
        try {
            if (port == 993) {
                s = TrustAllSSLSocketFactory.getDefault().createSocket();
            } else {
                s = new Socket();
            }
            /*
             * Set connect timeout
             */
            s.connect(new InetSocketAddress(host, port), 1000);
            s.setSoTimeout(1000);
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
            if (s != null) {
                s.close();
            }
        }
        return greeting != null;
    }

    public static boolean checkForSmtp(String host, int port) throws IOException {
        Socket s = null;
        String greeting = null;
        try {
            if (port == 465) {
                s = TrustAllSSLSocketFactory.getDefault().createSocket();
            } else {
                s = new Socket();
            }
            /*
             * Set connect timeout
             */
            s.connect(new InetSocketAddress(host, port), 1000);
            s.setSoTimeout(1000);
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
            s.close();
        }
        return greeting != null;
    }

    public static boolean checkForPop3(String host, int port) throws IOException {
        Socket s = null;
        String greeting = null;
        try {
            if (port == 995) {
                s = TrustAllSSLSocketFactory.getDefault().createSocket();
            } else {
                s = new Socket();
            }
            /*
             * Set connect timeout
             */
            s.connect(new InetSocketAddress(host, port), 1000);
            s.setSoTimeout(1000);
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
            s.close();
        }
        return greeting != null;
    }
}
