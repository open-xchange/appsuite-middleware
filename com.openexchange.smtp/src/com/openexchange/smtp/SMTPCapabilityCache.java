/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.smtp.config.ISMTPProperties;
import com.openexchange.smtp.services.Services;
import com.sun.mail.util.SocketFetcher;

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

        private boolean isNotLastLine(String line) {
            return line != null && line.length() >= 4 && line.charAt(3) == '-';
        }

        @Override
        public Capabilities call() throws IOException {
            Socket s = null;
            com.sun.mail.util.LineInputStream lineInputStream = null;
            try {
                // Establish socket connection
                s = SocketFetcher.getSocket(key.getHostString(), key.getPort(), createSmtpProps(), "mail.smtp", false);

                // Read IMAP server greeting on connect
                final InputStream in = s.getInputStream();
                final OutputStream out = s.getOutputStream();
                final StringBuilder sb = new StringBuilder(512);
                lineInputStream = new com.sun.mail.util.LineInputStream(in);
                {
                    String line;
                    do {
                        line = lineInputStream.readLine();
                        sb.append(line).append('\n');
                    } while (isNotLastLine(line));
                    line = null;
                }
                if (sb.length() > 0) {
                    sb.setLength(0);
                }

                // Request capabilities through EHLO command
                out.write(("EHLO " + domain + "\r\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
                {
                    String line;
                    do {
                        line = lineInputStream.readLine();
                        sb.append(line).append('\n');
                    } while (isNotLastLine(line));
                    line = null;
                }
                String capabilities = sb.toString();

                // Close connection through QUIT command
                out.write("QUIT\r\n".getBytes(StandardCharsets.UTF_8));
                out.flush();

                // Create new Capabilities object
                LOG.debug("Successfully fetched capabilities and greeting from SMTP server \"{}\":{}{}", key.getHostString(), Strings.getLineSeparator(), capabilities);
                return new Capabilities(capabilities);
            } finally {
                if (lineInputStream != null) {
                    Streams.close(lineInputStream);
                }
                Streams.close(s);
            }
        }

        private Properties createSmtpProps() {
            Properties smtpProps = MimeDefaultSession.getDefaultMailProperties();
            {
                int connectionTimeout = smtpProperties.getSmtpConnectionTimeout();
                if (connectionTimeout > 0) {
                    smtpProps.put("mail.smtp.connectiontimeout", Integer.toString(connectionTimeout));
                }
            }
            {
                int timeout = smtpProperties.getSmtpTimeout();
                if (timeout > 0) {
                    smtpProps.put("mail.smtp.timeout", Integer.toString(timeout));
                }
            }
            SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
            final String socketFactoryClass = factoryProvider.getDefault().getClass().getName();
            final String sPort = Integer.toString(key.getPort());
            if (isSecure) {
                smtpProps.put("mail.smtp.socketFactory.class", socketFactoryClass);
                smtpProps.put("mail.smtp.socketFactory.port", sPort);
                smtpProps.put("mail.smtp.socketFactory.fallback", "false");
                applySslProtocols(smtpProps);
                applySslCipherSuites(smtpProps);
            } else {
                smtpProps.put("mail.smtp.starttls.enable", "true");
                smtpProps.put("mail.smtp.socketFactory.port", sPort);
                smtpProps.put("mail.smtp.ssl.socketFactory.class", socketFactoryClass);
                smtpProps.put("mail.smtp.ssl.socketFactory.port", sPort);
                smtpProps.put("mail.smtp.socketFactory.fallback", "false");
                applySslProtocols(smtpProps);
                applySslCipherSuites(smtpProps);
            }
            return smtpProps;
        }

        private void applySslProtocols(Properties imapprops) {
            String sslProtocols = smtpProperties.getSSLProtocols();
            if (Strings.isNotEmpty(sslProtocols)) {
                imapprops.put("mail.smtp.ssl.protocols", sslProtocols);
            }
        }

        private void applySslCipherSuites(Properties imapprops) {
            String sslCipherSuites = smtpProperties.getSSLCipherSuites();
            if (Strings.isNotEmpty(sslCipherSuites)) {
                imapprops.put("mail.smtp.ssl.ciphersuites", sslCipherSuites);
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
