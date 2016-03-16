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
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.pop3.config.IPOP3Properties;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link POP3CapabilityCache} - A cache for CAPA responses from POP3 servers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3CapabilityCache {

    /**
     * The logger.
     */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POP3CapabilityCache.class);

    private static volatile Integer capabiltiesCacheIdleTime;
    private static int capabiltiesCacheIdleTime() {
        Integer tmp = capabiltiesCacheIdleTime;
        if (null == tmp) {
            synchronized (POP3CapabilityCache.class) {
                tmp = capabiltiesCacheIdleTime;
                if (null == tmp) {
                    int defaultValue = 0; // Do not check again
                    ConfigurationService service = POP3ServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.pop3.capabiltiesCacheIdleTime", defaultValue));
                    capabiltiesCacheIdleTime = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    /**
     * The default capabilities providing only mandatory POP3 commands.
     */
    protected static final String DEFAULT_CAPABILITIES = "USER\r\nPASS\r\nSTAT\r\nLIST\r\nRETR\r\nDELE\r\nNOOP\r\nRSET\r\nQUIT";

    private static volatile ConcurrentMap<InetSocketAddress, Future<Capabilities>> MAP;

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
            synchronized (POP3CapabilityCache.class) {
                if (MAP == null) {
                    MAP = new ConcurrentHashMap<InetSocketAddress, Future<Capabilities>>();
                    // TODO: Probably pre-load CAPABILITY and greeting from common POP3 servers like GMail, etc.
                }
            }
        }
    }

    /**
     * Tear-down for this cache.
     */
    public static void tearDown() {
        if (MAP != null) {
            synchronized (POP3CapabilityCache.class) {
                if (MAP != null) {
                    clear();
                    MAP = null;
                }
            }
        }
    }

    /**
     * Clears this cache.
     */
    public static void clear() {
        MAP.clear();
    }

    /**
     * Gets the default capabilities:
     *
     * <pre>
     * USER
     * PASS
     * STATS
     * LIST
     * RETR
     * DELE
     * NOOP
     * RSET
     * QUIT
     * </pre>
     *
     * @return The default capabilities
     */
    public static String getDeaultCapabilities() {
        return DEFAULT_CAPABILITIES;
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
     * @throws OXException If a mail error occurs
     */
    public static String getCapability(final InetAddress inetAddress, final int port, final boolean isSecure, final IPOP3Properties pop3Properties, final String login) throws IOException, OXException {
        return getCapability0(new InetSocketAddress(inetAddress, port), isSecure, pop3Properties, login);
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
     * @throws OXException If a mail error occurs
     */
    public static String getCapability(final InetSocketAddress address, final boolean isSecure, final IPOP3Properties pop3Properties, final String login) throws IOException, OXException {
        return getCapability0(address, isSecure, pop3Properties, login);
    }

    private static final int MAX_TIMEOUT = 5000;

    private static int getMaxTimeout(final IPOP3Properties pop3Properties) {
        return Math.min(pop3Properties.getPOP3Timeout(), MAX_TIMEOUT);
    }

    private static final int MAX_CONNECT_TIMEOUT = 2500;

    private static int getMaxConnectTimeout(final IPOP3Properties pop3Properties) {
        return Math.min(pop3Properties.getPOP3ConnectionTimeout(), MAX_CONNECT_TIMEOUT);
    }

    private static String getCapability0(final InetSocketAddress address, final boolean isSecure, final IPOP3Properties pop3Properties, final String login) throws IOException, OXException {
        int idleTime = capabiltiesCacheIdleTime();
        if (idleTime < 0) {
            // Never cache
            FutureTask<Capabilities> ft = new FutureTask<Capabilities>(new CapabilityCallable(address, isSecure, getMaxConnectTimeout(pop3Properties), getMaxTimeout(pop3Properties)));
            ft.run();
            return getFrom(ft, address, login, true).getCapabilities();
        }

        boolean caller = false;
        ConcurrentMap<InetSocketAddress, Future<Capabilities>> map = MAP;

        Future<Capabilities> f = map.get(address);
        if (null == f) {
            FutureTask<Capabilities> ft = new FutureTask<Capabilities>(new CapabilityCallable(address, isSecure, getMaxConnectTimeout(pop3Properties), getMaxTimeout(pop3Properties)));
            f = map.putIfAbsent(address, ft);
            if (null == f) {
                f = ft;
                ft.run();
                caller = true;
            }
        }

        Capabilities ret = getFrom(f, address, login, caller);
        if (null != ret) {
            if (isElapsed(ret, idleTime)) {
                FutureTask<Capabilities> ft = new FutureTask<Capabilities>(new CapabilityCallable(address, isSecure, getMaxConnectTimeout(pop3Properties), getMaxTimeout(pop3Properties)));
                if (map.replace(address, f, ft)) {
                    f = ft;
                    ft.run();
                } else {
                    f = map.get(address);
                }
                ret = getFrom(f, address, login, caller);
            }

            if (null != ret) {
                return ret.getCapabilities();
            }
        }

        // Not the executing thread which received an exception
        map.remove(address);

        // Create own callable for this thread
        try {
            /*
             * Intended for fast capabilities look-up. Use individual timeout value to ensure fast return from call.
             */
            return new CapabilityCallable(address, isSecure, getMaxConnectTimeout(pop3Properties), getMaxTimeout(pop3Properties)).call().getCapabilities();
        } catch (java.net.SocketTimeoutException e) {
            throw POP3ExceptionCode.CONNECT_ERROR.create(e, address, login);
        }
    }

    private static boolean isElapsed(Capabilities caps, int idleTime) {
        if (idleTime == 0) {
            return false; // never
        }
        // Check if elapsed
        return ((System.currentTimeMillis() - caps.getStamp()) > idleTime);
    }

    private static Capabilities getFrom(final Future<Capabilities> f, final InetSocketAddress address, final String login, final boolean caller) throws IOException, OXException {
        try {
            return f.get();
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw new IOException(e.getMessage());
        } catch (CancellationException e) {
            throw new IOException(e.getMessage());
        } catch (ExecutionException e) {
            if (caller) {
                return handleExecutionException(e, address, login);
            }
            /*
             * Not the executing thread which receives an exception
             */
            return null;
        }
    }

    private static Capabilities handleExecutionException(final ExecutionException e, final InetSocketAddress address, final String login) throws IOException, OXException {
        final Throwable cause = e.getCause();
        if (cause instanceof OXException) {
            throw ((OXException) cause);
        }
        if (cause instanceof java.net.SocketTimeoutException) {
            throw POP3ExceptionCode.CONNECT_ERROR.create(e, address, login);
        }
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

    private static final class CapabilityCallable implements Callable<Capabilities> {

        private final InetSocketAddress key;
        private final boolean isSecure;
        private final int connectionTimeout;
        private final int timeout;

        CapabilityCallable(InetSocketAddress key, boolean isSecure, int connectionTimeout, int timeout) {
            super();
            this.key = key;
            this.isSecure = isSecure;
            this.connectionTimeout = connectionTimeout;
            this.timeout = timeout;
        }

        @Override
        public Capabilities call() throws IOException {
            Socket s = null;
            StringBuilder sb = new StringBuilder(512);
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
                        /*
                         * Throws java.net.SocketTimeoutException if timeout expires before connecting
                         */
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
                        LOG.warn(sb.insert(0, "POP3 CAPA command failed: ").toString());
                        return new Capabilities(DEFAULT_CAPABILITIES);
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
                            LOG.warn("POP3 CAPA command failed: {}", responseCode);
                            return new Capabilities(DEFAULT_CAPABILITIES);
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
                            try {
                                i = in.read();
                            } catch (final java.net.SocketTimeoutException e) {
                                // Orderly failed reading next byte. Seems CAPA response isn't terminated with '.' character.
                                break;
                            }
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
                        if (Character.isDefined(pre)) {
                            LOG.warn("Unexpected CAPA response start: {}", pre);
                        } else {
                            LOG.warn("Invalid unicode character: {}", ((int) pre));
                        }
                        return new Capabilities(DEFAULT_CAPABILITIES);
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
                return new Capabilities(capabilities);
            } catch (final IOException e) {
                LOG.warn("Failed reading capabilities from POP3 server \"{}\". Read so far:{}", key.getHostName(), sb);
                throw e;
            } catch (final RuntimeException e) {
                LOG.warn("Fatally failed reading capabilities from POP3 server \"{}\". Read so far:{}", key.getHostName(), sb);
                final IOException ioException = new IOException(e.getMessage());
                ioException.initCause(e);
                throw ioException;
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (final IOException e) {
                        LOG.error("", e);
                    }
                }
            }
        }
    }

    private static final class Capabilities {

        private final String capabilities;
        private final long stamp;

        Capabilities(String capabilities) {
            super();
            this.capabilities = capabilities;
            this.stamp = System.currentTimeMillis();
        }

        long getStamp() {
            return stamp;
        }

        String getCapabilities() {
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
            Capabilities other = (Capabilities) obj;
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
