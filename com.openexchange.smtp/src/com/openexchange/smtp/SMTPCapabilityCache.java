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

package com.openexchange.smtp;

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
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.smtp.config.ISMTPProperties;
import com.openexchange.smtp.services.Services;

/**
 * {@link SMTPCapabilityCache} - A cache for CAPABILITY and greeting from SMTP servers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMTPCapabilityCache {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SMTPCapabilityCache.class);

    private static volatile Integer capabiltiesCacheIdleTime;
    private static int capabiltiesCacheIdleTime() {
        Integer tmp = capabiltiesCacheIdleTime;
        if (null == tmp) {
            synchronized (SMTPCapabilityCache.class) {
                tmp = capabiltiesCacheIdleTime;
                if (null == tmp) {
                    int defaultValue = 0; // Do not check again
                    ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.smtp.capabiltiesCacheIdleTime", defaultValue));
                    capabiltiesCacheIdleTime = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile ConcurrentMap<InetSocketAddress, Future<Capabilities>> MAP;

    /**
     * Initializes a new {@link SMTPCapabilityCache}.
     */
    private SMTPCapabilityCache() {
        super();
    }

    /**
     * Initializes this cache.
     */
    public static void init() {
        if (MAP == null) {
            synchronized (SMTPCapabilityCache.class) {
                if (MAP == null) {
                    MAP = new ConcurrentHashMap<InetSocketAddress, Future<Capabilities>>();
                }
            }
        }
    }

    /**
     * Tear-down for this cache.
     */
    public static void tearDown() {
        if (MAP != null) {
            synchronized (SMTPCapabilityCache.class) {
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
        final ConcurrentMap<InetSocketAddress, Future<Capabilities>> map = MAP;
        if (null != map) {
            map.clear();
        }
    }

    /**
     * Gets the SMTP server's capabilities.
     *
     * @param address The address
     * @param isSecure Whether a secure connection should be established
     * @param smtpProperties The SMTP properties
     * @param domain The SMTP server's domain name
     * @return The capabilities
     * @throws IOException If an I/O error occurs
     */
    public static Map<String, String> getCapabilities(final InetSocketAddress address, final boolean isSecure, final ISMTPProperties smtpProperties, final String domain) throws IOException {
        return getCapabilities0(address, isSecure, smtpProperties, domain).getCapabilities();
    }

    private static Capabilities getCapabilities0(final InetSocketAddress address, final boolean isSecure, final ISMTPProperties smtpProperties, final String domain) throws IOException {
        int idleTime = capabiltiesCacheIdleTime();
        if (idleTime < 0 ) {
            FutureTask<Capabilities> ft = new FutureTask<Capabilities>(new CapabilityAndGreetingCallable(address, isSecure, smtpProperties, domain));
            ft.run();
            return getFrom(ft);
        }

        ConcurrentMap<InetSocketAddress, Future<Capabilities>> map = MAP;

        Future<Capabilities> f = map.get(address);
        if (null == f) {
            FutureTask<Capabilities> ft = new FutureTask<Capabilities>(new CapabilityAndGreetingCallable(address, isSecure, smtpProperties, domain));
            f = map.putIfAbsent(address, ft);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }

        Capabilities caps = getFrom(f);
        if (isElapsed(caps, idleTime)) {
            FutureTask<Capabilities> ft = new FutureTask<Capabilities>(new CapabilityAndGreetingCallable(address, isSecure, smtpProperties, domain));
            if (map.replace(address, f, ft)) {
                f = ft;
                ft.run();
            } else {
                f = map.get(address);
            }
            caps = getFrom(f);
        }

        return caps;
    }

    private static boolean isElapsed(Capabilities caps, int idleTime) {
        if (idleTime == 0) {
            return false; // never
        }
        // Check if elapsed
        return ((System.currentTimeMillis() - caps.getStamp()) > idleTime);
    }

    private static Capabilities getFrom(Future<Capabilities> f) throws IOException, Error {
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

    private static final class CapabilityAndGreetingCallable implements Callable<Capabilities> {

        private final InetSocketAddress key;

        private final boolean isSecure;

        private final ISMTPProperties smtpProperties;

        private final String domain;

        public CapabilityAndGreetingCallable(final InetSocketAddress key, final boolean isSecure, final ISMTPProperties smtpProperties, final String domain) {
            super();
            this.domain = domain;
            this.key = key;
            this.isSecure = isSecure;
            this.smtpProperties = smtpProperties;
        }

        @Override
        public Capabilities call() throws IOException {
            Socket s = null;
            try {
                try {
                    if (isSecure) {
                        SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
                        s = factoryProvider.getDefault().createSocket();
                    } else {
                        s = new Socket();
                    }
                    /*
                     * Set connect timeout
                     */
                    final int connectionTimeout = smtpProperties.getSmtpConnectionTimeout();
                    if (connectionTimeout > 0) {
                        s.connect(key, connectionTimeout);
                    } else {
                        s.connect(key);
                    }
                    final int timeout = smtpProperties.getSmtpTimeout();
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
                /*final String greeting = sb.toString();*/
                if (sb.length() > 0) {
                    sb.setLength(0);
                }
                if (skipLF) {
                    /*
                     * Consume final LF
                     */
                    i = in.read();
                    skipLF = false;
                }
                /*
                 * Read CAPABILITY response
                 */
                final String command = "EHLO " + domain;
                while (sb.length() == 0) {
                    /*
                     * Request capabilities through EHLO command
                     */
                    out.write((command + "\r\n").getBytes());
                    out.flush();
                    sb.append((char) in.read());
                    for (int available; (available = in.available()) > 0;) {
                        final byte[] chunk = new byte[available];
                        final int read = in.read(chunk, 0, available);
                        final char[] chars = new char[read];
                        for (int j = 0; j < chars.length; j++) {
                            chars[j] = (char) (chunk[j] & 0xff);
                        }
                        sb.append(chars, 0, read);
                    }
                    if (0 == sb.length()) {
                        LOG.warn("Empty EHLO response for: {}", command);
                    }
                }
                final String capabilities = sb.toString();
                /*
                 * Close connection through QUIT command
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
                 * Create new Capabilities object
                 */
                return new Capabilities(capabilities);
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

        private final Map<String, String> capabilities;
        private final long stamp;

        public Capabilities(final String sCapabilities) {
            super();
            final Map<String, String> capabilities = new HashMap<String, String>(8);
            final String[] strings = Strings.splitByCRLF(sCapabilities);
            for (String cap : strings) {
                if (cap.startsWith("250")) {
                    cap = cap.substring(4); // Swallow "250-" or "250 "
                    capabilities.put(cap.toUpperCase(Locale.US), cap);
                }
            }
            this.capabilities = Collections.unmodifiableMap(capabilities);
            this.stamp = System.currentTimeMillis();
        }

        long getStamp() {
            return stamp;
        }

        Map<String, String> getCapabilities() {
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
            final Capabilities other = (Capabilities) obj;
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
