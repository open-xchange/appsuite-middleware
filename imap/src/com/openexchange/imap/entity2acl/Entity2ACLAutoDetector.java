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

package com.openexchange.imap.entity2acl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.api.MailConfig.BoolCapVal;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link Entity2ACLAutoDetector} - Auto-detects {@link Entity2ACL} implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Entity2ACLAutoDetector {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Entity2ACLAutoDetector.class);

    private static final Object[] EMPTY_ARGS = new Object[0];

    private static final Map<InetAddress, Entity2ACL> map = new ConcurrentHashMap<InetAddress, Entity2ACL>();

    private static final Lock CONTACT_LOCK = new ReentrantLock();

    private static final int BUFSIZE = 512;

    private static final byte[] IMAPCMD_LOGOUT = "A11 LOGOUT\r\n".getBytes();

    private static final byte[] IMAPCMD_CAPABILITY = "A10 CAPABILITY\r\n".getBytes();

    /**
     * Prevent instantiation
     */
    private Entity2ACLAutoDetector() {
        super();
    }

    /**
     * Resets the auto-detector
     */
    static void resetEntity2ACLMappings() {
        map.clear();
    }

    /**
     * Determines the {@link Entity2ACL} implementation dependent on IMAP server's greeting.
     * <p>
     * The IMAP server name can either be a machine name, such as <code>&quot;java.sun.com&quot;</code>, or a textual representation of its
     * IP address.
     * 
     * @param imapServer The IMAP server's address
     * @param imapPort The IMAP server's port
     * @param isSecure <code>true</code> if a secure connection must be established; otherwise <code>false</code>
     * @return the IMAP server's depending {@link Entity2ACL} implementation
     * @throws IOException - if an I/O error occurs
     * @throws Entity2ACLException - if a server greeting could not be mapped to a supported IMAP server
     */
    public static Entity2ACL getEntity2ACLImpl(final InetAddress imapServer, final int imapPort, final boolean isSecure) throws IOException, Entity2ACLException {
        Entity2ACL impl = map.get(imapServer);
        if (impl == null) {
            impl = loadEntity2ACLImpl(imapServer, imapPort, isSecure);
        }
        return impl;
    }

    private static IMAPServer mapInfo2IMAPServer(final String info, final InetAddress inetAddress, final int port, final boolean isSecure) throws IOException, Entity2ACLException {
        final IMAPServer[] imapServers = IMAPServer.values();
        for (int i = 0; i < imapServers.length; i++) {
            if (toLowerCase(info).indexOf(toLowerCase(imapServers[i].getName())) > -1) {
                return imapServers[i];
            }
        }
        /*
         * No known IMAP server found, check if ACLs are disabled anyway. If yes entity2acl is never used and can safely be mapped to
         * default implementation.
         */
        final BoolCapVal supportsACLs = IMAPConfig.isSupportsACLsConfig();
        if (BoolCapVal.FALSE.equals(supportsACLs) || (BoolCapVal.AUTO.equals(supportsACLs) && !checkForACLSupport(
            inetAddress,
            port,
            isSecure))) {
            /*
             * Return fallback implementation
             */
            if (LOG.isWarnEnabled()) {
                final StringBuilder warnBuilder = new StringBuilder(512).append("No IMAP server found ").append(
                    "that corresponds to greeting:\n\"").append(info.replaceAll("\r?\n", "")).append("\" on ").append(
                    address2String(inetAddress)).append(
                    ".\nSince ACLs are disabled (through IMAP configuration) or not supported by IMAP server, \"").append(
                    IMAPServer.CYRUS.getName()).append("\" is used as fallback.");
                LOG.warn(warnBuilder.toString());
            }
            return IMAPServer.CYRUS;
        }
        throw new Entity2ACLException(Entity2ACLException.Code.UNKNOWN_IMAP_SERVER, info);
    }

    private static String toLowerCase(final String str) {
        final char[] buf = new char[str.length()];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = Character.toLowerCase(str.charAt(i));
        }
        return new String(buf);
    }

    private static final Pattern PAT_ACL = Pattern.compile("(^|\\s)(ACL)(\\s+|$)");

    private static boolean checkForACLSupport(final InetAddress inetAddress, final int imapPort, final boolean isSecure) throws IOException, Entity2ACLException {
        CONTACT_LOCK.lock();
        try {
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
                    throw new Entity2ACLException(
                        Entity2ACLException.Code.CREATING_SOCKET_FAILED,
                        e,
                        address2String(inetAddress),
                        e.getMessage());
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
                out.write(IMAPCMD_CAPABILITY);
                out.flush();
                /*
                 * Read CAPABILITY response
                 */
                final StringBuilder sb = new StringBuilder(BUFSIZE);
                boolean nextLine = false;
                NextLoop: do {
                    eol = false;
                    i = in.read();
                    if (i != -1) {
                        /*
                         * Character '*' (whose integer value is 42) indicates an untagged response; meaning subsequent response lines will
                         * follow
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
                final boolean retval = PAT_ACL.matcher(sb.toString()).find();
                /*
                 * Close connection through LOGOUT command
                 */
                out.write(IMAPCMD_LOGOUT);
                out.flush();
                /*
                 * Consume until socket closure
                 */
                i = in.read();
                while (i != -1) {
                    i = in.read();
                }
                return retval;
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (final IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        } finally {
            CONTACT_LOCK.unlock();
        }
    }

    private static Entity2ACL loadEntity2ACLImpl(final InetAddress inetAddress, final int imapPort, final boolean isSecure) throws IOException, Entity2ACLException {
        Entity2ACL entity2Acl = map.get(inetAddress);
        if (entity2Acl != null) {
            return entity2Acl;
        }
        CONTACT_LOCK.lock();
        try {
            entity2Acl = map.get(inetAddress);
            if (entity2Acl != null) {
                return entity2Acl;
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
                    throw new Entity2ACLException(
                        Entity2ACLException.Code.CREATING_SOCKET_FAILED,
                        e,
                        address2String(inetAddress),
                        e.getMessage());
                }
                final InputStream in = s.getInputStream();
                final OutputStream out = s.getOutputStream();
                final StringBuilder sb = new StringBuilder(BUFSIZE);
                /*
                 * Read IMAP server greeting on connect
                 */
                boolean eol = false;
                int i = -1;
                while (!eol && ((i = in.read()) != -1)) {
                    final char c = (char) i;
                    if ((c == '\n') || (c == '\r')) {
                        eol = true;
                    } else {
                        sb.append(c);
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder(256).append("\n\tIMAP server [").append(address2String(inetAddress)).append("] greeting: ").append(
                        sb.toString()));
                }
                /*
                 * Close connection through LOGOUT command
                 */
                out.write(IMAPCMD_LOGOUT);
                out.flush();
                /*
                 * Consume until socket closure
                 */
                i = in.read();
                while (i != -1) {
                    i = in.read();
                }
                /*
                 * Map greeting to a known IMAP server
                 */
                final IMAPServer imapServer = mapInfo2IMAPServer(sb.toString(), inetAddress, imapPort, isSecure);
                try {
                    entity2Acl = Class.forName(imapServer.getImpl()).asSubclass(Entity2ACL.class).newInstance();
                } catch (final InstantiationException e) {
                    throw new Entity2ACLException(Entity2ACLException.Code.INSTANTIATION_FAILED, e, EMPTY_ARGS);
                } catch (final IllegalAccessException e) {
                    throw new Entity2ACLException(Entity2ACLException.Code.INSTANTIATION_FAILED, e, EMPTY_ARGS);
                } catch (final ClassNotFoundException e) {
                    throw new Entity2ACLException(Entity2ACLException.Code.INSTANTIATION_FAILED, e, EMPTY_ARGS);
                }
                map.put(inetAddress, entity2Acl);
                if (LOG.isInfoEnabled()) {
                    LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(address2String(inetAddress)).append(
                        "] greeting successfully mapped to: ").append(imapServer.getName()));
                }
                return entity2Acl;
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (final IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        } finally {
            CONTACT_LOCK.unlock();
        }
    }

    private static String address2String(final InetAddress inetAddress) {
        final String hostname = inetAddress.getHostName();
        if (null == hostname) {
            return inetAddress.getHostAddress();
        }
        return new StringBuilder(hostname).append('/').append(inetAddress.getHostAddress()).toString();
    }
}
