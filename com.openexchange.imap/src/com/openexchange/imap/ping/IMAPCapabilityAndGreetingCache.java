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

package com.openexchange.imap.ping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.imap.config.IIMAPProperties;
import com.openexchange.imap.services.Services;
import com.openexchange.java.BoundaryExceededException;
import com.openexchange.java.BoundedStringBuilder;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;

/**
 * {@link IMAPCapabilityAndGreetingCache} - A cache for CAPABILITY and greeting from IMAP servers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPCapabilityAndGreetingCache {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPCapabilityAndGreetingCache.class);

    private static volatile Integer capabiltiesCacheIdleTime;
    private static int capabiltiesCacheIdleTime() {
        Integer tmp = capabiltiesCacheIdleTime;
        if (null == tmp) {
            synchronized (IMAPCapabilityAndGreetingCache.class) {
                tmp = capabiltiesCacheIdleTime;
                if (null == tmp) {
                    int defaultValue = 0; // Do not check again
                    ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.imap.capabiltiesCacheIdleTime", defaultValue));
                    capabiltiesCacheIdleTime = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile ConcurrentMap<String, Future<CapabilityAndGreeting>> MAP;

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
                    MAP = new NonBlockingHashMap<String, Future<CapabilityAndGreeting>>();
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
    public static String getGreeting(final String address, final boolean isSecure, final IIMAPProperties imapProperties) throws IOException {
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
    public static Map<String, String> getCapabilities(final String address, final boolean isSecure, final IIMAPProperties imapProperties) throws IOException {
        return getCapabilityAndGreeting(address, isSecure, imapProperties).getCapability();
    }

    /**
     * Gets the cached capabilities & greeting from IMAP server denoted by specified parameters.
     *
     * @param address The IMAP server's address
     * @param isSecure Whether to establish a secure connection
     * @param imapProperties The IMAP properties
     * @return The capabilities & greeting
     * @throws IOException If an I/O error occurs
     */
    public static CapabilityAndGreeting getCapabilityAndGreeting(String address, boolean isSecure, IIMAPProperties imapProperties) throws IOException {
        int idleTime = capabiltiesCacheIdleTime();
        if (idleTime < 0) {
            // Never cache
            FutureTask<CapabilityAndGreeting> ft = new FutureTask<CapabilityAndGreeting>(new CapabilityAndGreetingCallable(address, isSecure, imapProperties));
            ft.run();
            return getFrom(ft);
        }

        ConcurrentMap<String, Future<CapabilityAndGreeting>> map = MAP;
        if (null == map) {
            init();
            map = MAP;
        }

        String key = new StringBuilder(address).append('-').append(isSecure).toString();
        Future<CapabilityAndGreeting> f = map.get(key);
        if (null == f) {
            FutureTask<CapabilityAndGreeting> ft = new FutureTask<CapabilityAndGreeting>(new CapabilityAndGreetingCallable(address, isSecure, imapProperties));
            f = map.putIfAbsent(key, ft);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }

        CapabilityAndGreeting cag = getFrom(f);
        if (isElapsed(cag, idleTime)) {
            FutureTask<CapabilityAndGreeting> ft = new FutureTask<CapabilityAndGreeting>(new CapabilityAndGreetingCallable(address, isSecure, imapProperties));
            if (map.replace(key, f, ft)) {
                f = ft;
                ft.run();
            } else {
                f = map.get(key);
            }
            cag = getFrom(f);
        }

        return cag;
    }

    private static boolean isElapsed(CapabilityAndGreeting cag, int idleTime) {
        if (idleTime == 0) {
            return false; // never
        }
        // Check if elapsed
        return ((System.currentTimeMillis() - cag.getStamp()) > idleTime);
    }

    private static CapabilityAndGreeting getFrom(Future<CapabilityAndGreeting> f) throws IOException {
        try {
            return f.get();
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw new IOException(e.getMessage());
        } catch (CancellationException e) {
            throw new IOException(e.getMessage());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
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

        private final String serverUrl;
        private final boolean isSecure;
        private final IIMAPProperties imapProperties;

        public CapabilityAndGreetingCallable(final String serverUrl, final boolean isSecure, final IIMAPProperties imapProperties) {
            super();
            this.serverUrl = serverUrl;
            this.isSecure = isSecure;
            this.imapProperties = imapProperties;
        }

        @Override
        public CapabilityAndGreeting call() throws IOException {
            BoundedStringBuilder sb = new BoundedStringBuilder(512, 2048);
            String greeting = null;
            String capabilities = null;

            Socket s = null;
            try {
                // Establish socket connection
                {
                    if (isSecure) {
                        SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
                        s = factoryProvider.getDefault().createSocket();
                    } else {
                        s = new Socket();
                    }

                    // Set connect timeout
                    int connectionTimeout = imapProperties.getImapConnectionTimeout();
                    if (connectionTimeout > 0) {
                        s.connect(toSocketAddress(serverUrl) , connectionTimeout);
                    } else {
                        s.connect(toSocketAddress(serverUrl));
                    }

                    // Set read timeout
                    int timeout = imapProperties.getImapTimeout();
                    if (timeout > 0) {
                        s.setSoTimeout(timeout);
                    }
                }

                // State variables
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();
                boolean skipLF = false;
                boolean eol = false;

                // Read IMAP server greeting on connect
                if (in.available() > 0) {
                    for (int i; !eol && ((i = in.read()) != -1);) {
                        char c = (char) i;
                        switch (c) {
                            case '\r':
                                eol = true;
                                skipLF = true;
                                break;
                            case '\n':
                                eol = true;
                                skipLF = false;
                                break;
                            default:
                                sb.append(c);
                                break;
                        }
                    }
                }
                greeting = sb.toString();
                sb.setLength(0);

                if (skipLF) {
                    // Consume final LF
                    in.read();
                    skipLF = false;
                }

                // Request capabilities through CAPABILITY command
                out.write("A1 CAPABILITY\r\n".getBytes());
                out.flush();

                // Read CAPABILITY response
                {
                    boolean hasNextLine = true;
                    while (hasNextLine) {
                        hasNextLine = false;
                        eol = false;

                        if (skipLF) {
                            // Consume final LF
                            in.read();
                            skipLF = false;
                        }

                        int i = in.read();
                        if (i != -1) {
                            // Character '*' (42) indicates an un-tagged response; meaning subsequent response lines will follow
                            hasNextLine = (i == 42);

                            do {
                                char c = (char) i;
                                switch (c) {
                                    case '\r':
                                        eol = true;
                                        skipLF = true;
                                        break;
                                    case '\n':
                                        eol = true;
                                        skipLF = false;
                                        break;
                                    default:
                                        sb.append(c);
                                        break;
                                }
                            } while (!eol && ((i = in.read()) != -1));

                            if (sb.length() >= 5 && sb.indexOf(" BYE ", 0) >= 0) {
                                // Received "BYE" response
                                sb.insert(0, "Received BYE response from IMAP server: ");
                                throw new IOException(sb.toString());
                            }

                            // Append LF if a next line is expected
                            if (hasNextLine) {
                                sb.append('\n');
                            }

                        }
                    }

                    String[] lines = SPLIT.split(sb.toString());
                    sb.setLength(0);
                    for (String line : lines) {
                        if (line.startsWith("* CAPABILITY ")) {
                            sb.append(line.substring(12));
                        } else if (!line.startsWith("A1 ")) {
                            sb.append(' ').append(line);
                        }
                    }
                    capabilities = sb.toString();
                }

                if (skipLF) {
                    // Consume final LF
                    in.read();
                    skipLF = false;
                }

                // Close connection through LOGOUT command
                out.write("A2 LOGOUT\r\n".getBytes());
                out.flush();

                // Create & return new CapabilityAndGreeting instance
                return new CapabilityAndGreeting(capabilities, greeting);
            } catch (BoundaryExceededException e) {
                if (null == greeting) {
                    // Exceeded while reading greeting
                    throw e;
                }

                if (null == capabilities) {
                    // Exceeded while reading greeting
                    capabilities = sb.toString().trim();
                }
                return new CapabilityAndGreeting(capabilities, greeting);
            } finally {
                if (null != s) {
                    try {
                        s.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
    }

    static InetSocketAddress toSocketAddress(final String serverUrl) {
        if (null == serverUrl) {
            return null;
        }
        final int pos = serverUrl.lastIndexOf(':');
        int port;
        if (pos > 0) {
            try {
                port = Integer.parseInt(serverUrl.substring(pos + 1).trim());
            } catch (final NumberFormatException e) {
                LOG.error("Port cannot be parsed to integer: {}", serverUrl, e);
                port = 143;
            }
        } else {
            port = 143;
        }
        if (pos == -1) {
            return new InetSocketAddress(serverUrl.trim(), port);
        } else {
            return new InetSocketAddress(serverUrl.substring(0, pos).trim(), port);
        }
    }

    /**
     * The capabilities & greeting information for an IMAP server (URL).
     */
    public static final class CapabilityAndGreeting {

        private static final Pattern SPLIT = Pattern.compile(" +");

        private final Map<String, String> capabilities;
        private final String greeting;
        private final long stamp;

        CapabilityAndGreeting(String capability, String greeting) {
            super();
            if (null == capability) {
                capabilities = Collections.emptyMap();
            } else {
                String[] caps = SPLIT.split(capability);
                Map<String, String> capabilities = new LinkedHashMap<String, String>(caps.length);
                for (String cap : caps) {
                    if (!Strings.isEmpty(cap)) {
                        capabilities.put(Strings.toUpperCase(cap), cap);
                    }
                }
                this.capabilities = Collections.unmodifiableMap(capabilities);
            }
            this.greeting = greeting;
            this.stamp = System.currentTimeMillis();
        }

        long getStamp() {
            return stamp;
        }

        /**
         * Gets the capabilities
         *
         * @return The capabilities
         */
        public Map<String, String> getCapability() {
            return capabilities;
        }

        /**
         * Gets the greeting
         *
         * @return The greeting
         */
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
            if (!(obj instanceof CapabilityAndGreeting)) {
                return false;
            }
            CapabilityAndGreeting other = (CapabilityAndGreeting) obj;
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
