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

package com.openexchange.imap.ping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;
import com.openexchange.imap.config.IIMAPProperties;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link IMAPCapabilityAndGreetingCache} - A cache for CAPABILITY and greeting from IMAP servers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPCapabilityAndGreetingCache {

    static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(IMAPCapabilityAndGreetingCache.class));

    private static volatile ConcurrentMap<InetSocketAddress, Future<CapabilityAndGreeting>> MAP;

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
            synchronized (IMAPCapabilityAndGreetingCache.class) {
                if (MAP == null) {
                    MAP = new ConcurrentHashMap<InetSocketAddress, Future<CapabilityAndGreeting>>();
                    // TODO: Probably pre-load CAPABILITY and greeting from common IMAP servers like GMail, etc.
                }
            }
        }
    }

    /**
     * Tear-down for this cache.
     */
    public static void tearDown() {
        if (MAP != null) {
            synchronized (IMAPCapabilityAndGreetingCache.class) {
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
     * Gets the cached greeting from IMAP server denoted by specified parameters.
     *
     * @param address The IMAP server's address
     * @param isSecure Whether to establish a secure connection
     * @param imapProperties The IMAP properties
     * @return The greeting from IMAP server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static String getGreeting(final InetSocketAddress address, final boolean isSecure, final IIMAPProperties imapProperties) throws IOException {
        return getCapabilityAndGreeting(address, isSecure, imapProperties).getGreeting();
    }

    /**
     * Gets the cached capabilities from IMAP server denoted by specified parameters.
     *
     * @param address The IMAP server's address
     * @param isSecure Whether to establish a secure connection
     * @param imapProperties The IMAP properties
     * @return The capabilities from IMAP server denoted by specified parameters
     * @throws IOException If an I/O error occurs
     */
    public static Map<String, String> getCapabilities(final InetSocketAddress address, final boolean isSecure, final IIMAPProperties imapProperties) throws IOException {
        return getCapabilityAndGreeting(address, isSecure, imapProperties).getCapability();
    }

    private static CapabilityAndGreeting getCapabilityAndGreeting(final InetSocketAddress address, final boolean isSecure, final IIMAPProperties imapProperties) throws IOException {
        final ConcurrentMap<InetSocketAddress, Future<CapabilityAndGreeting>> map = MAP;
        Future<CapabilityAndGreeting> f = map.get(address);
        if (null == f) {
            final FutureTask<CapabilityAndGreeting> ft =
                new FutureTask<CapabilityAndGreeting>(new CapabilityAndGreetingCallable(address, isSecure, imapProperties));
            f = map.putIfAbsent(address, ft);
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

    private static final class CapabilityAndGreetingCallable implements Callable<CapabilityAndGreeting> {

        private static final Pattern SPLIT = Pattern.compile("\r?\n");

        private final InetSocketAddress key;

        private final boolean isSecure;

        private final IIMAPProperties imapProperties;

        public CapabilityAndGreetingCallable(final InetSocketAddress key, final boolean isSecure, final IIMAPProperties imapProperties) {
            super();
            this.key = key;
            this.isSecure = isSecure;
            this.imapProperties = imapProperties;
        }

        @Override
        public CapabilityAndGreeting call() throws IOException {
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
                    final int connectionTimeout = imapProperties.getImapConnectionTimeout();
                    if (connectionTimeout > 0) {
                        s.connect(key, connectionTimeout);
                    } else {
                        s.connect(key);
                    }
                    final int timeout = imapProperties.getImapTimeout();
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
                    final String[] lines = SPLIT.split(sb.toString());
                    sb.setLength(0);
                    for (final String line : lines) {
                        if (!line.startsWith("A10 ")) {
                            sb.append(' ').append(line);
                        }
                    }
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
                return new CapabilityAndGreeting(capabilities, greeting);
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

    private static final class CapabilityAndGreeting {

        private static final Pattern SPLIT = Pattern.compile(" +");

        private final Map<String, String> capabilities;

        private final String greeting;

        public CapabilityAndGreeting(final String capability, final String greeting) {
            super();
            if (null == capability) {
                capabilities = Collections.emptyMap();
            } else {
                final String[] caps = SPLIT.split(capability);
                final Map<String, String> capabilities = new HashMap<String, String>(caps.length);
                final Locale locale = Locale.ENGLISH;
                for (final String cap : caps) {
                    capabilities.put(cap.toUpperCase(locale), cap);
                }
                this.capabilities = Collections.unmodifiableMap(capabilities);
            }
            this.greeting = greeting;
        }

        public Map<String, String> getCapability() {
            return capabilities;
        }

        public String getGreeting() {
            return greeting;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
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
            if (capabilities == null) {
                if (other.capabilities != null) {
                    return false;
                }
            } else if (!capabilities.equals(other.capabilities)) {
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

    }

}
