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

package com.openexchange.pop3.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import com.openexchange.pop3.config.IPOP3Properties;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link POP3CapabilityCache} - A cache for CAPA responses from POP3 servers.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3CapabilityCache {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3CapabilityCache.class);

    private static ConcurrentMap<InetSocketAddress, Future<Capability>> MAP;

    /**
     * Initializes a new {@link POP3CapabilityCache}.
     */
    private POP3CapabilityCache() {
        super();
    }

    /**
     * Initializes this cache.
     */
    public static void init() {
        if (MAP == null) {
            MAP = new ConcurrentHashMap<InetSocketAddress, Future<Capability>>();
            // TODO: Probably pre-load CAPABILITY and greeting from common POP3 servers like GMail, etc.
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
     * Gets the cached CAPABILITY from POP3 server denoted by specified parameters.<br>
     * Example:
     * 
     * <pre>
     * STLS
     * TOP
     * USER
     * SASL LOGIN
     * UIDL
     * RESP-CODES
     * </pre>
     * 
     * @param inetAddress The POP3 server's internet address
     * @param port The POP3 server's port
     * @param isSecure Whether to establish a secure connection
     * @param pop3Properties The POP3 properties
     * @return The CAPABILITY from POP3 server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static String getCapability(final InetAddress inetAddress, final int port, final boolean isSecure, final IPOP3Properties pop3Properties) throws IOException {
        return getCapability0(new InetSocketAddress(inetAddress, port), isSecure, pop3Properties).getCapability();
    }

    /**
     * Gets the cached CAPABILITY from POP3 server denoted by specified parameters:<br>
     * Example:
     * 
     * <pre>
     * STLS
     * TOP
     * USER
     * SASL LOGIN
     * UIDL
     * RESP-CODES
     * </pre>
     * 
     * @param address The POP3 server's address
     * @param isSecure Whether to establish a secure connection
     * @param pop3Properties The POP3 properties
     * @return The CAPABILITY from POP3 server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static String getCapability(final InetSocketAddress address, final boolean isSecure, final IPOP3Properties pop3Properties) throws IOException {
        return getCapability0(address, isSecure, pop3Properties).getCapability();
    }

    /**
     * Gets the cached CAPABILITY from POP3 server denoted by specified parameters.<br>
     * Example:
     * 
     * <pre>
     * STLS
     * TOP
     * USER
     * SASL LOGIN
     * UIDL
     * RESP-CODES
     * </pre>
     * 
     * @param address The POP3 server's address
     * @param isSecure Whether to establish a secure connection
     * @param connectionTimeout The connection timeout
     * @param timeout The timeout
     * @return The CAPABILITY from POP3 server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static String getCapability(final InetSocketAddress address, final boolean isSecure, final int connectionTimeout, final int timeout) throws IOException {
        Future<Capability> f = MAP.get(address);
        if (null == f) {
            final FutureTask<Capability> ft = new FutureTask<Capability>(new CapabilityCallable(
                address,
                isSecure,
                connectionTimeout,
                timeout));
            f = MAP.putIfAbsent(address, ft);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }
        try {
            return f.get().getCapability();
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw new IOException(e.getMessage());
        } catch (final CancellationException e) {
            throw new IOException(e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw ((IOException) cause);
            }
            if (cause instanceof RuntimeException) {
                throw new IOException(e.getMessage());
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    private static Capability getCapability0(final InetSocketAddress address, final boolean isSecure, final IPOP3Properties pop3Properties) throws IOException {
        Future<Capability> f = MAP.get(address);
        if (null == f) {
            final FutureTask<Capability> ft = new FutureTask<Capability>(new CapabilityCallable(address, isSecure, pop3Properties));
            f = MAP.putIfAbsent(address, ft);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }
        try {
            return f.get();
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw new IOException(e.getMessage());
        } catch (final CancellationException e) {
            throw new IOException(e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw ((IOException) cause);
            }
            if (cause instanceof RuntimeException) {
                throw new IOException(e.getMessage());
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    private static final class CapabilityCallable implements Callable<Capability> {

        private final InetSocketAddress key;

        private final boolean isSecure;

        private final int connectionTimeout;

        private final int timeout;

        public CapabilityCallable(final InetSocketAddress key, final boolean isSecure, final IPOP3Properties pop3Properties) {
            this(key, isSecure, pop3Properties.getPOP3ConnectionTimeout(), pop3Properties.getPOP3Timeout());
        }

        public CapabilityCallable(final InetSocketAddress key, final boolean isSecure, final int connectionTimeout, final int timeout) {
            super();
            this.key = key;
            this.isSecure = isSecure;
            this.connectionTimeout = connectionTimeout;
            this.timeout = timeout;
        }

        public Capability call() throws IOException {
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
                    if (connectionTimeout > 0) {
                        s.connect(key, connectionTimeout);
                    } else {
                        s.connect(key);
                    }
                    if (timeout > 0) {
                        /*
                         * Define timeout for blocking operations
                         */
                        s.setSoTimeout(timeout);
                    }
                } catch (final IOException e) {
                    throw e;
                }
                final InputStream in = s.getInputStream();
                final OutputStream out = s.getOutputStream();
                final StringBuilder sb = new StringBuilder(512);
                /*
                 * Read POP3 server greeting on connect
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
                /* final String greeting = sb.toString(); */
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
                out.write("CAPA\r\n".getBytes());
                out.flush();
                /*
                 * Read CAPABILITY response
                 */
                final String capabilities;
                {
                    final char pre = (char) in.read();
                    if ('-' == pre) {
                        sb.append(pre);
                        eol = false;
                        i = -1;
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
                        throw new IOException("POP3 CAPA command failed: " + sb.toString());
                    } else if ('+' == pre) {
                        sb.append(pre);
                        eol = false;
                        i = -1;
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
                        final String responseCode = sb.toString();
                        if (!responseCode.toUpperCase().startsWith("+OK")) {
                            throw new IOException("POP3 CAPA command failed: " + responseCode);
                        }
                        sb.setLength(0);
                        if (skipLF) {
                            /*
                             * Consume final LF
                             */
                            i = in.read();
                            skipLF = false;
                        }
                        while (true) {
                            i = in.read();
                            if ('.' == i) {
                                final char first = (char) in.read();
                                if ('\n' == first) {
                                    // Read ".LF" sequence
                                    break;
                                } else if ('\r' == first) {
                                    final char sec = (char) in.read();
                                    if ('\n' == sec) {
                                        // Read ".CRLF" sequence
                                        break;
                                    }
                                    sb.append((char) i).append(first).append(sec);
                                } else {
                                    sb.append((char) i).append(first);
                                }
                            } else {
                                sb.append((char) i);
                            }
                        }
                        capabilities = sb.toString();
                    } else {
                        throw new IOException("Unexpected response start: " + pre);
                    }
                }
                /*
                 * Close connection through LOGOUT command
                 */
                out.write("QUIT\r\n".getBytes());
                out.flush();
                /*
                 * Consume until socket closure
                 */
                i = in.read();
                while (i != -1) {
                    i = in.read();
                }
                /*
                 * Create new object
                 */
                return new Capability(capabilities);
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

    private static final class Capability {

        private final String capabilities;

        public Capability(final String capabilities) {
            super();
            this.capabilities = capabilities;
        }

        public String getCapability() {
            return capabilities;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
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
            final Capability other = (Capability) obj;
            if (capabilities == null) {
                if (other.capabilities != null) {
                    return false;
                }
            } else if (!capabilities.equals(other.capabilities)) {
                return false;
            }
            return true;
        }

    }

}
