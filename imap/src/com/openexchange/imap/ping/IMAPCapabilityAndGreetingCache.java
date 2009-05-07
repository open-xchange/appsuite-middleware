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

package com.openexchange.imap.ping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link IMAPCapabilityAndGreetingCache} - A cache for CAPABILITY and greeting from IMAP servers.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPCapabilityAndGreetingCache {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPCapabilityAndGreetingCache.class);

    private static Map<InetSocketAddress, CapabilityAndGreeting> MAP;

    /**
     * Initializes a new {@link IMAPCapabilityAndGreetingCache}.
     */
    private IMAPCapabilityAndGreetingCache() {
        super();
    }

    /**
     * Initializes this cache.
     */
    public static void init() {
        if (MAP == null) {
            MAP = new ConcurrentHashMap<InetSocketAddress, CapabilityAndGreeting>();
        }
    }

    /**
     * Tear-down for this cache.
     */
    public static void tearDown() {
        if (MAP != null) {
            clear();
            MAP = null;
        }
    }

    /**
     * Clears this cache.
     */
    public static void clear() {
        MAP.clear();
    }

    /**
     * Gets the cached CAPABILITY from IMAP server denoted by specified parameters.
     * 
     * @param inetAddress The IMAP server's internet address
     * @param imapPort The IMAP server's port
     * @param isSecure Whether to establish a secure connection
     * @return The CAPABILITY from IMAP server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static String getCapability(final InetAddress inetAddress, final int imapPort, final boolean isSecure) throws IOException {
        final InetSocketAddress key = new InetSocketAddress(inetAddress, imapPort);
        final CapabilityAndGreeting cag = MAP.get(key);
        if (null != cag) {
            return cag.getCapability();
        }
        return putCAG(key, isSecure).getCapability();
    }

    /**
     * Gets the cached CAPABILITY from IMAP server denoted by specified parameters.
     * 
     * @param address The IMAP server's address
     * @param isSecure Whether to establish a secure connection
     * @return The CAPABILITY from IMAP server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static String getCapability(final InetSocketAddress address, final boolean isSecure) throws IOException {
        final CapabilityAndGreeting cag = MAP.get(address);
        if (null != cag) {
            return cag.getCapability();
        }
        return putCAG(address, isSecure).getCapability();
    }

    /**
     * Gets the cached greeting from IMAP server denoted by specified parameters.
     * 
     * @param inetAddress The IMAP server's internet address
     * @param imapPort The IMAP server's port
     * @param isSecure Whether to establish a secure connection
     * @return The greeting from IMAP server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static String getGreeting(final InetAddress inetAddress, final int imapPort, final boolean isSecure) throws IOException {
        final InetSocketAddress key = new InetSocketAddress(inetAddress, imapPort);
        final CapabilityAndGreeting cag = MAP.get(key);
        if (null != cag) {
            return cag.getGreeting();
        }
        return putCAG(key, isSecure).getGreeting();
    }

    /**
     * Gets the cached greeting from IMAP server denoted by specified parameters.
     * 
     * @param address The IMAP server's address
     * @param isSecure Whether to establish a secure connection
     * @return The greeting from IMAP server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static String getGreeting(final InetSocketAddress address, final boolean isSecure) throws IOException {
        final CapabilityAndGreeting cag = MAP.get(address);
        if (null != cag) {
            return cag.getGreeting();
        }
        return putCAG(address, isSecure).getGreeting();
    }

    /**
     * Gets the cached CAPABILITY and greeting from IMAP server denoted by specified parameters.
     * 
     * @param inetAddress The IMAP server's internet address
     * @param imapPort The IMAP server's port
     * @param isSecure Whether to establish a secure connection
     * @return The CAPABILITY and greeting from IMAP server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static String[] getCapabilityAndGreeting(final InetAddress inetAddress, final int imapPort, final boolean isSecure) throws IOException {
        final InetSocketAddress key = new InetSocketAddress(inetAddress, imapPort);
        final CapabilityAndGreeting cag = MAP.get(key);
        if (null != cag) {
            return cag.toArray();
        }
        return putCAG(key, isSecure).toArray();
    }

    private static CapabilityAndGreeting putCAG(final InetSocketAddress key, final boolean isSecure) throws IOException {
        synchronized (IMAPCapabilityAndGreetingCache.class) {
            CapabilityAndGreeting cag = MAP.get(key);
            if (null == cag) {
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
                            s.connect(key, IMAPConfig.getImapConnectionTimeout());
                        } else {
                            s.connect(key);
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
                    final StringBuilder sb = new StringBuilder(512);
                    /*
                     * Read IMAP server greeting on connect
                     */
                    boolean skipLF = false;
                    boolean eol = false;
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
                    final String greeting = sb.toString();
                    sb.setLength(0);
                    if (skipLF) {
                        /*
                         * Consume final LF
                         */
                        i = in.read();
                        skipLF = false;
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
                        boolean nextLine = false;
                        NextLoop: do {
                            eol = false;
                            i = in.read();
                            if (i != -1) {
                                /*
                                 * Character '*' (whose integer value is 42) indicates an untagged response; meaning subsequent response
                                 * lines will follow
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
                     * Create new CAG object
                     */
                    cag = new CapabilityAndGreeting(capabilities, greeting);
                    MAP.put(key, cag);
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
            return cag;
        }
    }

    private static final class CapabilityAndGreeting {

        private final String capability;

        private final String greeting;

        public CapabilityAndGreeting(final String capability, final String greeting) {
            super();
            this.capability = capability;
            this.greeting = greeting;
        }

        public String getCapability() {
            return capability;
        }

        public String getGreeting() {
            return greeting;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((capability == null) ? 0 : capability.hashCode());
            result = prime * result + ((greeting == null) ? 0 : greeting.hashCode());
            return result;
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
            final CapabilityAndGreeting other = (CapabilityAndGreeting) obj;
            if (capability == null) {
                if (other.capability != null) {
                    return false;
                }
            } else if (!capability.equals(other.capability)) {
                return false;
            }
            if (greeting == null) {
                if (other.greeting != null) {
                    return false;
                }
            } else if (!greeting.equals(other.greeting)) {
                return false;
            }
            return true;
        }

        public String[] toArray() {
            return new String[] { capability, greeting };
        }
    }

}
