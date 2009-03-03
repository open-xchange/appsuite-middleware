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

package com.openexchange.imap.acl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link ACLExtensionAutoDetector} - Auto-detects IMAP server's ACL extension.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ACLExtensionAutoDetector {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ACLExtensionAutoDetector.class);

    private static final Map<InetAddress, ACLExtension> map = new ConcurrentHashMap<InetAddress, ACLExtension>();

    private static final int BUFSIZE = 512;

    /**
     * Prevent instantiation
     */
    private ACLExtensionAutoDetector() {
        super();
    }

    /**
     * Resets the auto-detector
     */
    static void resetACLExtensionMappings() {
        map.clear();
    }

    /**
     * Determines the ACL extension dependent on IMAP server's capabilities.
     * <p>
     * The IMAP server name can either be a machine name, such as <code>&quot;java.sun.com&quot;</code>, or a textual representation of its
     * IP address.
     * 
     * @param imapServer The IMAP server's address
     * @param imapPort The IMAP server's port
     * @param isSecure <code>true</code> if a secure connection must be established; otherwise <code>false</code>
     * @return The IMAP server's ACL extension.
     * @throws IOException If an I/O error occurs
     */
    public static ACLExtension getACLExtension(final InetAddress imapServer, final int imapPort, final boolean isSecure) throws IOException {
        final ACLExtension cached = map.get(imapServer);
        if (null != cached) {
            return cached;
        }
        putACLExtension(imapServer, imapPort, isSecure);
        return map.get(imapServer);
    }

    private static final Pattern PAT_ACL = Pattern.compile("(^|\\s)(ACL)(\\s+|$)");

    private static final Pattern PAT_RIGHTS = Pattern.compile("(?:^|\\s)(?:RIGHTS=)([a-zA-Z0-9]+)(?:\\s+|$)");

    private static void putACLExtension(final InetAddress inetAddress, final int imapPort, final boolean isSecure) throws IOException {
        synchronized (ACLExtensionAutoDetector.class) {
            if (map.containsKey(inetAddress)) {
                return;
            }
            Socket s = null;
            try {
                try {
                    if (isSecure) {
                        s = TrustAllSSLSocketFactory.getDefault().createSocket();
                    } else {
                        s = new Socket();
                    }
                    /*
                     * Set connect timeout
                     */
                    if (IMAPConfig.getImapConnectionTimeout() > 0) {
                        s.connect(new InetSocketAddress(inetAddress, imapPort), IMAPConfig.getImapConnectionTimeout());
                    } else {
                        s.connect(new InetSocketAddress(inetAddress, imapPort));
                    }
                    if (IMAPConfig.getImapTimeout() > 0) {
                        /*
                         * Define timeout for blocking operations
                         */
                        s.setSoTimeout(IMAPConfig.getImapTimeout());
                    }
                } catch (final IOException e) {
                    throw e;
                    // throw new Entity2ACLException(
                    // Entity2ACLException.Code.CREATING_SOCKET_FAILED,
                    // e,
                    // address2String(inetAddress),
                    // e.getMessage());
                }
                final InputStream in = s.getInputStream();
                final OutputStream out = s.getOutputStream();
                boolean skipLF = false;
                /*
                 * Skip IMAP server greeting on connect
                 */
                boolean eol = false;
                int i = -1;
                while (!eol && ((i = in.read()) != -1)) {
                    final char c = (char) i;
                    if ((c == '\n') || (c == '\r')) {
                        if (c == '\r') {
                            skipLF = true;
                        }
                        eol = true;
                    }
                }
                /*
                 * Request capabilities through CAPABILITY command
                 */
                out.write("A10 CAPABILITY\r\n".getBytes());
                out.flush();
                /*
                 * Read CAPABILITY response
                 */
                final String capabilities;
                {
                    final StringBuilder sb = new StringBuilder(BUFSIZE);
                    boolean nextLine = false;
                    NextLoop: do {
                        eol = false;
                        i = in.read();
                        if (i != -1) {
                            /*
                             * Character '*' (whose integer value is 42) indicates an untagged response; meaning subsequent response lines
                             * will follow
                             */
                            nextLine = (i == 42);
                            do {
                                final char c = (char) i;
                                if ((c == '\n') || (c == '\r')) {
                                    if ((c == '\n') && skipLF) {
                                        // Discard remaining LF
                                        skipLF = false;
                                        nextLine = true;
                                        continue NextLoop;
                                    }
                                    if (c == '\r') {
                                        skipLF = true;
                                    }
                                    eol = true;
                                } else {
                                    sb.append(c);
                                }
                            } while (!eol && ((i = in.read()) != -1));
                        }
                        if (nextLine) {
                            sb.append('\n');
                        }
                    } while (nextLine);
                    capabilities = sb.toString();
                }
                /*
                 * Close connection through LOGOUT command
                 */
                out.write("A11 LOGOUT\r\n".getBytes());
                out.flush();
                /*
                 * Consume until socket closure
                 */
                i = in.read();
                while (i != -1) {
                    i = in.read();
                }
                /*
                 * Examine CAPABILITY response
                 */
                final boolean hasACL = PAT_ACL.matcher(capabilities).find();
                if (!hasACL) {
                    map.put(inetAddress, NoACLExtension.getInstance());
                    if (LOG.isInfoEnabled()) {
                        LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(address2String(inetAddress)).append(
                            "] CAPABILITY response indicates no support of ACL extension."));
                    }
                    return;
                }
                final Matcher m = PAT_RIGHTS.matcher(capabilities);
                if (m.find()) {
                    final String allowedRights = m.group(1);
                    /*
                     * Check if "RIGHTS=" provides any of new characters "k", "x", "t", or "e" as defined in RFC 4314
                     */
                    final boolean containsRFC4314Character = containsRFC4314Character(allowedRights);
                    map.put(inetAddress, containsRFC4314Character ? new RFC4314ACLExtension() : new RFC2086ACLExtension());
                    if (LOG.isInfoEnabled()) {
                        LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(address2String(inetAddress)).append(
                            "] CAPABILITY response indicates support of ACL extension\n\tand specifies \"RIGHTS=").append(allowedRights).append(
                            "\" capability.").append("\n\tACL extension according to ").append(
                            containsRFC4314Character ? "RFC 4314" : "RFC 2086").append(" is going to be used.\n"));
                    }
                    return;
                }
                map.put(inetAddress, new RFC2086ACLExtension());
                if (LOG.isInfoEnabled()) {
                    LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(address2String(inetAddress)).append(
                        "] CAPABILITY response indicates support of ACL extension\n\tbut does not specify \"RIGHTS=\" capability.").append(
                        "\n\tACL extension according to RFC 2086 is going to be used.\n"));
                }
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (final IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    private static final char[] RFC4314_CARACTERS = { 'k', 'x', 't', 'e' };

    private static boolean containsRFC4314Character(final String allowedRights) {
        boolean found = false;
        for (int i = 0; !found && i < RFC4314_CARACTERS.length; i++) {
            found = (allowedRights.indexOf(RFC4314_CARACTERS[i]) >= 0);
        }
        return found;
    }

    private static String address2String(final InetAddress inetAddress) {
        final String hostname = inetAddress.getHostName();
        if (null == hostname) {
            return inetAddress.getHostAddress();
        }
        return new StringBuilder(hostname).append('/').append(inetAddress.getHostAddress()).toString();
    }
}
