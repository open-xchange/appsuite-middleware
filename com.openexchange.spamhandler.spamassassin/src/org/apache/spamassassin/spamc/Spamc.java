/* <@LICENSE>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </@LICENSE>
 */

package org.apache.spamassassin.spamc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import javax.net.ssl.SSLSocketFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Streams;

/**
 * This class provides a pure Java implementation of the <a
 * href="http://svn.apache.org/repos/asf/spamassassin/trunk/spamd/PROTOCOL"
 * target="_" title="Apache SVN">SpamAssassin Network Protocol</a>. It can be
 * used by mail software to query a <a href="http://spamassassin.apache.org/"
 * title="The Apache SpamAssassin Project" target="_">SpamAssassin <a
 * href="http://spamassassin.apache.org/full/3.1.x/doc/spamd.html"
 * title="daemonized version of spamassassin" target="_">spamd</a> server and
 * check whether a particular message is spam. There are also commands for
 * learning and reporting.
 *
 * @author Nick Radov
 * @see <a href="http://svn.apache.org/viewvc/spamassassin/trunk/spamc/"
 *      title="Apache SVN" target="_">spamc C source code</a>
 * @see <a
 *      href="http://svn.apache.org/repos/asf/james/server/trunk/core-library/src/main/java/org/apache/james/util/SpamAssassinInvoker.java"
 *      title="Apache SVN" target="_">org.apache.james.util.SpamAssassinInvoker</a>
 */
public class Spamc {

    private static class Commands {

        protected final static String CHECK = "CHECK";

        protected final static String SYMBOLS = "SYMBOLS";

        protected final static String REPORT = "REPORT";

        protected final static String REPORT_IFSPAM = "REPORT_IFSPAM";

        protected final static String SKIP = "SKIP";

        protected final static String PING = "PING";

        protected final static String PROCESS = "PROCESS";

        protected final static String TELL = "TELL";

        protected final static String HEADERS = "HEADERS";

    }

    /**
     * The entire response from spamd, including all headers and the
     * processed message. This allows any of the command methods to return
     * the response as a single object.
     */
    public static class SpamdResponse {

        /** The entire spamd response in its raw form */
        private final String rawResponse;

        /** The protocol version used by the spamd server */
        private String protocolVersion;

        /**
         * Response code from the first line of the response. It should equal
         * one of the fields defined in {@link ExitCodes}.
         */
        private int responseCode;

        /**
         * Description of the error, if there was one.
         */
        private String responseMessage;

        /** All of the headers returned */
        private final Map<String, String> headers = new HashMap<>();

        /** Contents of the e-mail message after processing */
        private final String processedMessage;

        /** Expected regular expression pattern for the first line */
        private final Pattern firstLinePattern = Pattern.compile("^SPAMD/\\d+\\.\\d+\\s+\\d+.*");

        /**
         * Construct a response object by parsing the response received from the
         * spamd server.
         *
         * @param response
         *            the raw response received from Spamd
         * @throws IllegalArgumentException
         *             if <code>response</code> is <code>null</code>,
         *             or it doesn't match the expected format
         */
        protected SpamdResponse(final String response) throws IllegalArgumentException {
            super();
            this.rawResponse = response;
            int lineIndex;
            if (response == null || response.length() == 0) {
                throw new IllegalArgumentException("Response not set");
            }
            final String[] lines = response.split("\r\n");
            if (!firstLinePattern.matcher(lines[0]).matches()) {
                throw new IllegalArgumentException("Invalid first response line: " + lines[0]);
            }
            parseFirstLine(lines[0]);
            lineIndex = parseHeaders(lines);
            final StringBuilder processedMessage = new StringBuilder();
            for (; lineIndex < lines.length; lineIndex++) {
                processedMessage.append(lines[lineIndex]).append("\r\n");
            }
            this.processedMessage = processedMessage.toString();
        }

        /**
         * Extract the protocol version, response code, and response message
         * from the first line of the spamd response.
         *
         * @param firstLine
         *            first line of the response
         */
        private final void parseFirstLine(final String firstLine) {
            final String[] words = firstLine.split("\\s", 3);
            this.protocolVersion = words[0].substring(words[0].indexOf('/') + 1);
            this.responseCode = Integer.parseInt(words[1]);
            this.responseMessage = words[2];
        }

        /**
         * Parse out all of the headers and store them for later use
         *
         * @param lines
         *            the spamd response split out into individual lines
         * @return line index after all of the headers (should be the start of
         *         the message body)
         */
        private final int parseHeaders(final String[] lines) {
            // get all of the headers
            int i;
            int colonPosition;
            String name;
            String value;
            for (i = 1; i < lines.length; i++) {
                colonPosition = lines[i].indexOf(':');
                if (colonPosition == -1) {
                    // there are no more headers
                    break;
                }
                name = lines[i].substring(0, colonPosition);
                value = lines[i].substring(colonPosition + 1).trim();
                headers.put(name, value);
            }
            return ++i;
        }

        /**
         * Get the protocol version from the first line of the spamd response.
         *
         * @return protocol version, which should be &quot;1.3&quot; as of
         *         SpamAssassin 3.1.8.
         */
        public String getProtocolVersion() {
            return protocolVersion;
        }

        /**
         * Get the raw, unparsed spamd response including all headers and body
         * content.
         *
         * @return spamd response
         */
        public CharSequence getRawResponse() {
            return rawResponse;
        }

        /**
         * Get the response code from the first line of the response. It should
         * equal one of the fields defined in {@link ExitCodes}.
         *
         * @return response code
         */
        public int getResponseCode() {
            return responseCode;
        }

        /**
         * Get a description of the error, if any.
         *
         * @return response message
         */
        public CharSequence getResponseMessage() {
            return responseMessage;
        }

        /**
         * Get the message after processing by SpamAssassin. Depending on the
         * options set or command issued, it may include a complete report or
         * just the names of the tests that hit. This value may be empty if an
         * error occurred.
         *
         * @return processed message
         */
        public String getProcessedMessage() {
            return processedMessage;
        }

        /**
         * Get all of headers returned by spamd.
         *
         * @see Headers
         * @return collection of headers; for each map entry the key will be the
         *         header name and the value will be the header value
         */
        public Map<String, String> getHeaders() {
            return headers;
        }

        /**
         * Get the value of the {@link Headers#SPAM Spam} header.
         *
         * @return header value
         */
        private String getSpamHeaderValue() {
            return getHeaders().get(Headers.SPAM);
        }

        /**
         * Check whether the processed message is spam.
         *
         * @return <code>true</code> if the Spam header value starts with the
         *         word &quot;True&quot;, otherwise <code>False</code>
         */
        public boolean isSpam() {
            final String spamHeaderValue = getSpamHeaderValue();
            if (spamHeaderValue == null) {
                return false;
            }
            return spamHeaderValue.startsWith("True");
        }

        /**
         * Get the spam score for the processed message.
         *
         * @return spam score
         * @throws NumberFormatException
         *             if spamd returned a response that couldn't be
         *             interpreted as a <code>double</code>
         */
        public double getScore() throws NumberFormatException {
            final String spamHeaderValue = getSpamHeaderValue();
            if (spamHeaderValue == null) {
                return 0d;
            }
            final int semicolonPosition = spamHeaderValue.indexOf(';');
            if (semicolonPosition == -1) {
                return 0d;
            }
            int slashPosition = spamHeaderValue.indexOf('/');
            if (slashPosition == -1) {
                // there is no threshold value
                slashPosition = spamHeaderValue.length();
            }
            final String score = spamHeaderValue.substring(semicolonPosition + 1, slashPosition).trim();
            return Double.parseDouble(score);
        }

        /**
         * Get the threshold score for a message to be considered as spam.
         *
         * @return theshold score
         * @throws NumberFormatException
         *             if spamd returned a response that couldn't be
         *             interpreted as a <code>double</code>
         */
        public double getThreshold() throws NumberFormatException {
            final String spamHeaderValue = getSpamHeaderValue();
            if (spamHeaderValue == null) {
                return 0d;
            }
            final int slashPosition = spamHeaderValue.indexOf('/');
            if (slashPosition == -1) {
                // there is no threshold value
                return 0d;
            }
            final String threshold = spamHeaderValue.substring(slashPosition + 1).trim();
            return Double.parseDouble(threshold);
        }
    }

    /** The current protocol version number */
    private static final String CURRENT_PROTOCOL_VERSION = "1.3";

    /** The protocol version used when sending a command */
    private String protocolVersion = CURRENT_PROTOCOL_VERSION;

    /**
     * Flag to indicate whether input is assumed to be a single
     * BSMTP-formatted message
     */
    private boolean assumeBsmtp = false;

    /** The default port that spamd defaults to listening on */
    private static final int DEFAULT_PORT = 783;

    private final static String DEFAULT_HOST = "localhost";

    /** Host names and/or network addresses of the spamd servers */
    private List<String> hosts;

    /**
     * Flag to indicate whether the order of spamd server IP addresses to
     * try should be randomized for load balancing
     */
    private boolean randomize = false;

    /** TCP port number to connect to the spamd server */
    private int port = DEFAULT_PORT;

    /**
     * Flag to indicate whether Unix sockets should be used instead of
     * standard TCP sockets (currently not implemented
     */
    private boolean useUnixSockets = false;

    /**
     * The path of the Unix socket to use for connecting to the spamd server
     */
    private String unixSocketPath = null;

    /**
     * Flag to mark whether SSL should be used when communicating with the
     * spamd server
     */
    private boolean useSsl = false;

    private static final long DEFAULT_TIMEOUT_SECONDS = 600;

    /**
     * Number of seconds to wait for the spamd server to respond before
     * timing out
     */
    private long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;

    private static final int DEFAULT_CONNECT_RETRIES = 3;

    /**
     * Number of times to retry connecting to the spamd server before
     * failing
     */
    private int connectRetries = DEFAULT_CONNECT_RETRIES;

    /** Flag to indicate whether we should automatically fail over */
    private boolean failover = true;

    private static final int DEFAULT_RETRY_SLEEP_SECONDS = 1;

    /**
     * Number of seconds to sleep before retrying the connection to a spamd
     * server
     */
    private int retrySleepSeconds = DEFAULT_RETRY_SLEEP_SECONDS;

    /** Number of bytes in one kilobyte */
    private static final long BYTES_PER_KB = 1024;

    private static final long DEFAULT_MAX_SIZE_BYTES = 500 * BYTES_PER_KB; // 500KB

    /** Maximum size of a message that can be processed */
    private long maxSize = DEFAULT_MAX_SIZE_BYTES;

    /**
     * Flag to indicate whether the message body should be transmitted using
     * Zlib compression
     */
    private boolean compress = false;

    /** Name of the user for the spamd &quot;User&quot; header */
    private String userName = System.getProperty("user.name");

    /** The optional SSL socket factory to use */
    private final SSLSocketFactory sslSocketFactory;

    /**
     * Create a new object.
     */
    public Spamc(SSLSocketFactory sslSocketFactory) {
        super();
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * Set a single spamd destination host.
     *
     * @param host
     *            host name or IP address
     */
    public void setHost(final String host) {
        if (this.hosts == null) {
            this.hosts = new ArrayList<>();
        } else {
            this.hosts.clear();
        }
        this.hosts.add(host);
    }

    /**
     * Set the list of spamd hosts.
     *
     * @param hosts
     *            collection of {@link java.lang.String}} objects
     *            containing host names and/or IP addresses
     */
    public void setHosts(final List<String> hosts) {
        this.hosts = hosts;
    }

    /**
     * Set the list of spamd hosts.
     *
     * @param hosts
     *            array containing host names and/or IP addresses
     */
    public void setHosts(final String[] hosts) {
        if (this.hosts == null) {
            this.hosts = new ArrayList<>();
        } else {
            this.hosts.clear();
        }
        if (hosts != null) {
            for (int i = 0; i < hosts.length; i++) {
                this.hosts.add(hosts[i]);
            }
        }
    }

    /**
     * Get the current list of spamd destination hosts.
     *
     * @return collection of {@link java.lang.String} objects containing
     *         host names and/or IP addresses
     */
    public List<String> getHosts() {
        return hosts;
    }

    /**
     * Set whether the order of spamd destination hosts should be randomized
     * for load balancing.
     *
     * @see RandomizeOption
     * @param randomize
     *            <code>true</code> for load balancing or
     *            <code>false</code> to try hosts in the order
     *            specified
     */
    public void setRandomize(final boolean randomize) {
        this.randomize = randomize;
    }

    /**
     * Get whether the order of spamd destination hosts should be randomized
     * for load balancing.
     *
     * @return <code>true</code> for load balancing or <code>false</code>
     *         to try hosts in the order specified
     */
    public boolean getRandomize() {
        return randomize;
    }

    private static final int MIN_TCP_PORT = 0;

    private static final int MAX_TCP_PORT = 65535;

    /**
     * Set the TCP port to use when connecting to the spamd server (defaults
     * to <code>783</code>).
     *
     * @param port
     *            TCP port number
     * @throws IllegalArgumentException
     *             if <code>port</code> is less than <code>0</code>
     *             or greater than <code>65535</code>
     */
    public void setPort(final int port) throws IllegalArgumentException {
        if ((port < MIN_TCP_PORT) || (port > MAX_TCP_PORT)) {
            throw new IllegalArgumentException("Invalid port number: " + port);
        }
        this.port = port;
    }

    /**
     * Get the TCP port to use when connecting to the spamd server (defaults
     * to <code>783</code>).
     */
    public int getPort() {
        return port;
    }

    /**
     * Set whether to use Unix sockets instead of TCP sockets when
     * connecting to the spamd server.
     *
     * @param useUnixSockets
     *            <code>true</code> to use Unix sockets,
     *            <code>false</code> (default) to use TCP sockets
     * @throws IllegalArgumentException
     *             if <code>useUnixSockets</code> is <code>true</code>
     *             <i>(that feature is not yet implemented)</i>
     */
    public void setUseUnixSockets(final boolean useUnixSockets) {
        if (useUnixSockets) {
            // TODO: implement support for Unix sockets
            throw new IllegalArgumentException("Unix socket support is not implemented");
        }
        this.useUnixSockets = useUnixSockets;
    }

    /**
     * Set whether to assume the message is in BSMTP format.
     *
     * @param bsmtp
     *            <code>true</code> to assume BSMTP format,
     *            <code>false</code> (default) to assume standard <a
     *            href="ftp://ftp.rfc-editor.org/in-notes/rfc822.txt"
     *            target="_" title="Standard for the Format of ARPA
     *            Internet Text Messages">RFC 822</a> format
     * @throws IllegalArgumentException
     *             if <code>bsmtp</code> is <code>true</code>
     *             <i>(that feature is not yet implemented)</i>
     */
    public void setAssumeBSMTP(final boolean bsmtp) {
        if (bsmtp) {
            throw new IllegalArgumentException("BSMTP support is not implemented");
        }
        this.assumeBsmtp = bsmtp;
    }

    public boolean getAssumeBSMTP() {
        return this.assumeBsmtp;
    }

    public boolean getUseUnixSockets() {
        return this.useUnixSockets;
    }

    public void setUnixSocketPath(final String path) {
        this.unixSocketPath = path;
    }

    public String getUnixSocketPath() {
        return this.unixSocketPath;
    }

    public void setConnectRetries(final int connectRetries) {
        this.connectRetries = connectRetries;
    }

    public int getConnectRetries() {
        return connectRetries;
    }

    public void setRetrySleep(final int sleepSeconds) {
        this.retrySleepSeconds = sleepSeconds;
    }

    public int getRetrySleep() {
        return this.retrySleepSeconds;
    }

    public void setMaxSize(final long bytes) {
        this.maxSize = bytes;
    }

    public long getMaxSize() {
        return this.maxSize;
    }

    public void setFailover(final boolean failover) {
        this.failover = failover;
    }

    public boolean getFailover() {
        return failover;
    }

    /**
     * Set whether to use SSL when connecting to the spamd server.
     *
     * @param useSsl
     *            <code>true</code> to use SSL, <code>false</code>
     *            (default) to send the network traffic in the clear
     */
    public void setUseSSL(final boolean useSsl) {
        this.useSsl = useSsl;
    }

    public boolean getUseSSL() {
        return useSsl;
    }

    /**
     * Set the amount of time to wait for the spamd server to respond.
     *
     * @param timeoutSeconds
     *            timeout in seconds.
     */
    public void setTimeout(final long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Get the amount of time to wait for the spamd server to respond.
     *
     * @return timeout in seconds
     */
    public long getTimeout() {
        return timeoutSeconds;
    }

    /**
     * Get the user name that will be sent to the spamd server.
     *
     * @see Headers#USER
     * @param userName
     *            user name
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    /**
     * Get the user name that will be sent to the spamd server.
     *
     * @return user name (defaults to the current user)
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Fields for use with the {@link Spamc#setProtocolVersion(String)}
     * method.
     */
    public static class ProtocolVersions {

        /** Version 1.0 */
        public static final String V1_0 = "1.0";

        /** Version 1.1 */
        public static final String V1_1 = "1.1";

        /** Version 1.2 */
        public static final String V1_2 = "1.2";

        /** Version 1.3 */
        public static final String V1_3 = "1.3";

        /** Version 1.4 */
        public static final String V1_4 = "1.4";
    }

    /** All supported protocol versions */
    private final static List<String> VALID_PROTOCOL_VERSIONS = new ArrayList<>(6);
    static {
        VALID_PROTOCOL_VERSIONS.add(ProtocolVersions.V1_0);
        VALID_PROTOCOL_VERSIONS.add(ProtocolVersions.V1_1);
        VALID_PROTOCOL_VERSIONS.add(ProtocolVersions.V1_2);
        VALID_PROTOCOL_VERSIONS.add(ProtocolVersions.V1_3);
        // TODO: Add version 1.4
    }

    /**
     * Check whether the current protocol version is the same or newer as
     * some other protocol version.
     *
     * @see #getProtocolVersion()
     * @param protocolVersion
     *            protocol version to check, which should be a field
     *            from {@link ProtocolVersions}
     * @return <code>true</code> if the current protocol version is the
     *         same or higher than <code>protocolVersion</code>
     * @throws IllegalArgumentException
     *             if <code>protocolVersion</code> is
     *             <code>null</code> or not a valid protocol version
     * @throws IllegalStateException
     *             if the current protocol version is not valid
     */
    private boolean isSameOrNewerProtocolVersion(final String protocolVersion) throws IllegalArgumentException {
        if (protocolVersion == null) {
            throw new IllegalArgumentException("Protocol version not set");
        }
        final int currentIndex = VALID_PROTOCOL_VERSIONS.indexOf(getProtocolVersion());
        if (currentIndex == -1) {
            throw new IllegalStateException("Invalid current protocol version:" + getProtocolVersion());
        }
        final int checkIndex = VALID_PROTOCOL_VERSIONS.indexOf(protocolVersion);
        if (checkIndex == -1) {
            throw new IllegalArgumentException("Invalid protocol version: " + protocolVersion);
        }
        return currentIndex >= checkIndex;
    }

    /**
     * Get the name of the current class, not including the package name.
     * This method should be removed once Java 1.4 compatibility is no
     * longer needed and the {@link Class#getSimpleName()} method can be
     * used instead.
     *
     * @return name of the current class
     */
    private static String getSimpleClassName() {
        final String className = Spamc.class.getName();
        final String packageName = Spamc.class.getPackage().getName();
        return className.substring(packageName.length() + 1);
    }

    /**
     * Set the current protocol version. As a side effect, compression will
     * be automatically disabled if it is not supported for the new version.
     *
     * @see #setCompress(boolean)
     * @param protocolVersion
     *            protocol version, which should be a field from
     *            {@link ProtocolVersions}
     * @throws IllegalArgumentException
     *             if the protocol version is not supported
     */
    public void setProtocolVersion(final String protocolVersion) throws IllegalArgumentException {
        if ((protocolVersion == null) || (protocolVersion.length() == 0)) {
            throw new IllegalArgumentException("Protocol version not set");
        }
        if (!VALID_PROTOCOL_VERSIONS.contains(protocolVersion)) {
            throw new IllegalArgumentException("Invalid protocol version: " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        if (!isSameOrNewerProtocolVersion(ProtocolVersions.V1_4)) {
            setCompress(false);
        }
    }

    /**
     * Get the protocol version.
     *
     * @return protocol version, which should be a field from
     *         {@link ProtocolVersions}
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Set whether to compress the message body with Zlib. The default is
     * <code>false</code>.
     *
     * @param compress
     * @throws IllegalArgumentException
     *             if <code>compress</code> is <code>true</code> and
     *             the protocol version is less than &quot;1.4&quot;
     */
    public void setCompress(final boolean compress) throws IllegalArgumentException {
        if (compress && !isSameOrNewerProtocolVersion(ProtocolVersions.V1_4)) {
            throw new IllegalArgumentException("Compression is not supported for protocol version " + getProtocolVersion());
        }
        this.compress = compress;
    }

    /**
     * Get whether message bodies will be compressed using Zlib.
     *
     * @return <code>true</code> if message bodies will be compressed,
     *         otherwise <code>false</code>
     */
    public boolean getCompress() {
        return compress;
    }

    /**
     * Container class for fields of header names and values.
     */
    private static class Headers {

        private static final String MESSAGE_CLASS = "Message-class";

        private static final String MESSAGE_CLASS_SPAM = "spam";

        private static final String MESSAGE_CLASS_HAM = "ham";

        private static final String SET = "Set";

        private static final String REMOVE = "Remove";

        private static final String SET_REMOVE_LOCAL = "local";

        private static final String SET_REMOVE_REMOTE = "remote";

        // optional headers
        private static final String CONTENT_LENGTH = "Content-length";

        private static final String SPAM = "Spam";

        private static final String USER = "User";

        private static final String COMPRESS = "Compress";

        private static final String ZLIB = "zlib";
    }

    /**
     * Send a command to the spamd server with no message.
     *
     * @param command
     *            command to send, which should be a field from
     *            {@link Commands}
     * @return spamd server response
     * @throws IOException
     * @throws UnknownHostException
     */
    private SpamdResponse sendCommand(final String command) throws IOException, UnknownHostException {
        return sendCommand(command, (String) null);
    }

    /**
     * Send a command to the spamd server with a message to be checked,
     * learned, and/or reported.
     *
     * @param command
     *            command to send, which should be a field from
     *            {@link Commands}
     * @param message
     *            message in RFC 822 or BSMTP format
     * @see #getAssumeBSMTP()
     * @return spamd server response
     * @throws IOException
     * @throws UnknownHostException
     */
    private SpamdResponse sendCommand(final String command, final String message) throws IllegalArgumentException, UnknownHostException, IOException {
        if (message == null) {
            throw new IllegalArgumentException("Message contents not set");
        }
        return sendCommand(command, (Map<String, String>) null, message);
    }

    /**
     * Construct the query that will be sent to the spamd server.
     *
     * @param command
     *            command to send, which should be a field from
     *            {@link Commands}
     * @param headers
     *            optional headers to send (the mandatory headers will
     *            be added automatically)
     * @param message
     * @return the query
     */
    private String constructQuery(final String command, final Map<String, String> headers, final String message) {
        final StringBuilder query = new StringBuilder();
        query.append(command);
        query.append(' ');
        query.append("SPAMC/");
        query.append(getProtocolVersion());
        query.append("\r\n");

        // create a new Map to store all the headers the calling code passed in,
        // plus more we will add later
        final Map<String, String> newHeaders = new HashMap<>();
        if (headers != null) {
            newHeaders.putAll(headers);
        }

        // set the User header
        if ((getUserName() != null) && (getUserName().length() > 0)) {
            newHeaders.put(Headers.USER, getUserName());
        }

        // set the Compress header
        if (getCompress()) {
            newHeaders.put(Headers.COMPRESS, Headers.ZLIB);
        }

        // set the Content-length header
        newHeaders.put(Headers.CONTENT_LENGTH, Long.toString(message != null ? message.getBytes().length : 0));

        // append all of the headers to the query;
        final Iterator<Map.Entry<String, String>> iterator = newHeaders.entrySet().iterator();
        Map.Entry<String, String> entry;
        while (iterator.hasNext()) {
            entry = iterator.next();
            query.append(entry.getKey());
            query.append(": ");
            query.append(entry.getValue());
            query.append("\r\n");
        }

        long contentLength;
        if (message != null && message.length() > 0) {
            query.append("\r\n");
            if (getCompress()) {
                // TODO: complete and test this feature
                final Deflater compresser = new Deflater();
                compresser.setInput(message.getBytes());
                compresser.finish();
                final byte[] compressedMessage = new byte[message.length()];
                contentLength = compresser.deflate(compressedMessage);
                // query.append(compressedMessage, 0, contentLength);
                throw new IllegalStateException("Compression is not supported");
            }

            contentLength = message.getBytes().length;
            query.append(message);
        } else {
            contentLength = 0;
        }

        return query.toString();
    }

    private SpamdResponse sendCommand(final String command, final Map<String, String> headers, final String message) throws UnknownHostException, IOException {
        final String query = constructQuery(command, headers, message);
        final String queryResponse = getQueryResponse(query);
        if (isEmpty(queryResponse)) {
            throw new IllegalArgumentException("Received no response from spamc for query:\n\t" + query);
        }
        return new SpamdResponse(queryResponse);
    }

    /**
     * Translate the list of host names into a list of IP addresses. Each
     * host may have multiple addresses. If the randomization option is
     * turned on then the array entries will be rearranged in pseudo-random
     * order.
     *
     * @see #getRandomize()
     * @return list of {@link java.let.InetAddress} objects
     * @throws IllegalStateException
     *             if there is not at least one host set
     */
    private List<InetAddress> getAllHostAddresses() throws IllegalStateException, UnknownHostException {
        final List<InetAddress> addresses = new ArrayList<>();
        if ((getHosts() == null) || (getHosts().isEmpty())) {
            setHost(DEFAULT_HOST);
        }
        String host;
        // build up a list of host names in case we have to report an error
        final StringBuilder commaSeparatedHosts = new StringBuilder();
        for (int i = 0; i < getHosts().size(); i++) {
            host = getHosts().get(i);
            if (i > 0) {
                commaSeparatedHosts.append(',');
            }
            commaSeparatedHosts.append(host);
            try {
                addresses.addAll(Arrays.asList(InetAddress.getAllByName(host)));
            } catch (final UnknownHostException e) {
                System.err.println(InetAddress.class.getName() + ".getAllByName(" + host + ") failed");
            }
        }
        if (addresses.isEmpty()) {
            throw new UnknownHostException("could not resolve any hosts (" + commaSeparatedHosts + "): no such host");
        }
        if (getRandomize()) {
            Collections.shuffle(addresses);
        }
        return addresses;
    }

    /** Number of milliseconds in one second */
    private static final int MILLIS_PER_SECOND = 1000;

    private static final String SSL_SOCKET_FACTORY_CLASS_NAME = "SSLSocketFactory";

    /**
     * Set up a connection to a spamd server. It iterates through the list
     * of available addresses and retries until it either succeeds, or
     * reaches the limit or retries.
     *
     * @return connection to a spamd server
     * @throws IOException
     * @throws UnknownHostException
     */
    private Socket setupTransport() throws IOException, UnknownHostException {
        if (getUseUnixSockets()) {
            // TODO: implement Unix socket support
            throw new IllegalStateException("Unix sockets are not supported");
        }

        Socket socket = null;
        try {
            IOException lastException = null;
            final List<InetAddress> addresses = getAllHostAddresses();
            if (addresses.isEmpty()) {
                throw new IllegalStateException("No destination address");
            }
            int retryCount = 0;
            int addressIndex = 0;
            InetAddress address;
            InetSocketAddress inetSocketAddress;
            do {
                address = addresses.get(addressIndex);
                Socket attempt = null;
                try {
                    inetSocketAddress = new InetSocketAddress(address, getPort());
                    if (getUseSSL() && null != sslSocketFactory) {
                        attempt = sslSocketFactory.createSocket();
                    } else {
                        attempt = new Socket();
                    }
                    attempt.connect(inetSocketAddress, (int) getTimeout() * MILLIS_PER_SECOND);
                    attempt.setSoTimeout((int) getTimeout() * MILLIS_PER_SECOND);
                    socket = attempt;
                    attempt = null;
                } catch (final IOException e) {
                    lastException = e;
                    retryCount++;
                    System.err.println(Socket.class.getName() + ".connect(SocketAddress, int) to spamd at " + address + " failed, retrying (#" + retryCount + " of " + getConnectRetries() + ")");
                    addressIndex++;
                    if (addressIndex >= addresses.size()) {
                        // wrap around to the first IP address again
                        addressIndex = 0;
                    }
                    try {
                        Thread.sleep(getTimeout() * MILLIS_PER_SECOND);
                    } catch (final InterruptedException e1) {
                        // this should not occur, but if it does there is
                        // nothing we can do about it
                        // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    Streams.close(attempt);
                }
            } while (getFailover() && (socket == null) && (retryCount < addresses.size()) && (retryCount < getConnectRetries()));

            if (socket == null) {
                System.err.println("connection attempt to spamd aborted after " + getConnectRetries() + " retries");
                throw lastException;
            }

            if (getUseSSL() && null == sslSocketFactory) {
                // TODO: test this
                if (sslPackage == null) {
                    throw new IllegalStateException("SSL is not available");
                }
                // negotiate an SSL connection
                Socket existingSocket = socket;
                socket = null;
                try {
                    // load the class dynamically, since it may not be available in some JVMs
                    final Class sslSocketFactory = Class.forName(SSL_PACKAGE_NAME + "." + SSL_SOCKET_FACTORY_CLASS_NAME);
                    final Method getDefault = sslSocketFactory.getMethod("getDefault", (Class) null);
                    final Object defaultSocketFactory = getDefault.invoke(null, (Object) null);
                    final Class[] parameterTypes = new Class[] { Socket.class, String.class, int.class, boolean.class };
                    final Method createSocket = sslSocketFactory.getMethod("createSocket", parameterTypes);
                    final Object[] args = new Object[] { existingSocket, existingSocket.getInetAddress().getHostName(), Integer.valueOf(existingSocket.getPort()), Boolean.TRUE };
                    socket = (Socket) createSocket.invoke(defaultSocketFactory, args);
                    existingSocket = null;
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Class " + SSL_PACKAGE_NAME + "." + SSL_SOCKET_FACTORY_CLASS_NAME + " could not be loaded", e);
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                } finally {
                    Streams.close(existingSocket);
                }
            }
            Socket retval = socket;
            socket = null;
            return retval;
        } finally {
            Streams.close(socket);
        }
    }

    /**
     * Get the response to a query from a spamd server.
     *
     * @param query
     *            complete query, including all headers and possibly a
     *            message
     * @return raw response from the server
     * @throws UnknownHostException
     * @throws IOException
     * @throws IllegalStateException
     */
    public String getQueryResponse(final String query) throws UnknownHostException, IOException, IllegalStateException {
        final Socket socket = setupTransport();
        OutputStream out = null;
        BufferedReader in = null;

        final StringBuilder response = new StringBuilder();

        try {
            out = socket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out.write(query.getBytes(StandardCharsets.UTF_8));
            out.flush();
            socket.shutdownOutput();
            String s;
            while ((s = in.readLine()) != null) {
                response.append(s).append("\r\n");
            }
            return response.toString();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * Just check if the passed message is spam or not.
     *
     * @param message
     * @return
     */
    public SpamdResponse check(final String message) throws UnknownHostException, IOException {
        return sendCommand(Commands.CHECK, message);
    }

    /**
     * Check if message is spam or not, and return score plus list of
     * symbols hit
     *
     * @param message
     * @return
     */
    public SpamdResponse symbols(final String message) throws UnknownHostException, IOException {
        return sendCommand(Commands.SYMBOLS, message);
    }

    /**
     * Check if message is spam or not, and return score plus report.
     *
     * @param message
     * @return
     */
    public SpamdResponse report(final String message) throws UnknownHostException, IOException {
        return sendCommand(Commands.REPORT, message);
    }

    /**
     * Check if message is spam or not, and return score plus report if the
     * message is spam.
     *
     * @param message
     * @return
     */
    public SpamdResponse reportIfSpam(final String message) throws UnknownHostException, IOException {
        return sendCommand(Commands.REPORT_IFSPAM, message);
    }

    /**
     * Ignore this message -- client opened connection then changed its
     * mind. (This method should not generally be used, but is included for
     * completeness.)
     *
     * @param message
     * @return
     */
    public SpamdResponse skip(final String message) throws UnknownHostException, IOException {
        return sendCommand(Commands.SKIP, message);
    }

    /**
     *
     * @return
     * @throws UnknownHostException
     * @throws IOException
     */
    public SpamdResponse ping() throws UnknownHostException, IOException {
        return sendCommand(Commands.PING);
    }

    public SpamdResponse process(final String message) throws UnknownHostException, IOException {
        return sendCommand(Commands.PROCESS, message);
    }

    /**
     * Tell what type of we are to process and what should be done with that
     * message. This includes setting or removing a local or a remote
     * database (learning, reporting, forgetting, revoking).
     *
     * @param message
     *            message contents
     * @param spam
     *            if <code>true</code> &quot;<code>Message-class: spam</code>&quot;,
     *            <code>false</code> if it is ham
     * @param setLocal
     *            if <code>true</code>, set the &quot;Set:
     *            local&quot; header to learn the message in the local
     *            database
     * @param setRemote
     *            if <code>true</code>, set the &quot;Set:
     *            remote&quot; header to learn the message in remote
     *            databases
     * @param removeLocal
     * @param removeRemote
     * @return
     * @throws IllegalArgumentException
     *             if both <code>setLocal</code> and
     *             <code>removeLocal</code> are <code>true</code>,
     *             or if both <code>setRemote</code> and
     *             <code>removeRemote</code> are <code>true</code>
     */
    public SpamdResponse tell(final String message, final boolean spam, final boolean setLocal, final boolean setRemote, final boolean removeLocal, final boolean removeRemote) throws IllegalArgumentException, IOException {
        if (setLocal && removeLocal) {
            throw new IllegalArgumentException("Can't both set and remove local");
        }
        if (setRemote && removeRemote) {
            throw new IllegalArgumentException("Can't both set and remove remote");
        }

        final Map<String, String> headers = new HashMap<>();
        if (spam) {
            headers.put(Headers.MESSAGE_CLASS, Headers.MESSAGE_CLASS_SPAM);
        } else {
            headers.put(Headers.MESSAGE_CLASS, Headers.MESSAGE_CLASS_HAM);
        }
        final StringBuilder setValue = new StringBuilder();
        final StringBuilder removeValue = new StringBuilder();
        if (setLocal) {
            setValue.append(Headers.SET_REMOVE_LOCAL);
        } else if (removeLocal) {
            removeValue.append(Headers.SET_REMOVE_LOCAL);
        }
        if (setRemote) {
            if (setValue.length() > 0) {
                setValue.append(", ");
            }
            setValue.append(Headers.SET_REMOVE_REMOTE);
        } else if (removeRemote) {
            if (removeValue.length() > 0) {
                removeValue.append(", ");
            }
            removeValue.append(Headers.SET_REMOVE_REMOTE);
        }
        headers.put(Headers.SET, setValue.toString());
        headers.put(Headers.REMOVE, removeValue.toString());
        return sendCommand(Commands.TELL, headers, message);
    }

    public SpamdResponse headers(final String message) throws UnknownHostException, IOException {
        return sendCommand(Commands.HEADERS, message);
    }

    /**
     * Command line utility for checking whether a message is spam. It takes
     * the same arguments as the standard <code>spamc</code> program.
     * However, some arguments are currently not supported.
     *
     * @param args
     *            command-line arguments; for a full description see
     *            {@link #printUsage()}
     */
    public static void main(final String[] args) {
        SpamdResponse response = null;
        long flags = SafeFallbackOption.FLAG;
        String message = null;

        try {
            // TODO: remove this debugging code
            final File spamFile = new File(File.separator + File.separator + "ax5" + File.separator + "apps" + File.separator + "Apache" + File.separator + "Mail-SpamAssassin-3.1.8" + File.separator + "sample-spam.txt");
            System.setIn(new FileInputStream(spamFile));
            // end debugging code

            File configFile = null;
            for (int i = 0; i < args.length; i++) {
                if ("-F".equals(args[i]) || "--config".equals(args[i])) {
                    if ((i + 1) >= (args.length - 1)) {
                        throw new NoArgumentException(i, args[i].length() - 1, args[i]);
                    }
                    configFile = new File(args[i + 1]);
                }
            }
            final String[] combinedArgs = Spamc.combineArgs(configFile, args);
            final Spamc spamc = new Spamc(null);
            flags = Spamc.readArgs(spamc, combinedArgs);

            // read the message from stdin
            final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            final StringBuilder sb = new StringBuilder(System.in.available());
            while (in.ready()) {
                sb.append(in.readLine()).append("\r\n");
            }
            message = sb.toString();

            // figure out which command to execute
            if ((flags & CheckOption.FLAG) > 0) {
                response = spamc.check(message);
                System.out.println(response.getScore() + "/" + response.getThreshold());
            } else if ((flags & TestsOption.FLAG) > 0) {
                response = spamc.symbols(message);
                System.out.println(response.getProcessedMessage());
            } else if ((flags & FullOption.FLAG) > 0) {
                response = spamc.report(message);
                System.out.println(response.getProcessedMessage());
            } else if ((flags & FullSpamOption.FLAG) > 0) {
                response = spamc.reportIfSpam(message);
                if (response.isSpam()) {
                    System.out.println(response.getProcessedMessage());
                }
            } else if ((flags & KeepAliveOption.FLAG) > 0) {
                response = spamc.ping();
            } else if ((flags & HeadersOption.FLAG) > 0) {
                response = spamc.headers(message);
                final Iterator<String> iterator = response.getHeaders().keySet().iterator();
                String key;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    System.out.println(key + ": " + response.getHeaders().get(key));
                }
            } else if ((flags & LearnTypeOption.FLAG) > 0) {
                boolean setLocal = false;
                boolean setRemote = false;
                boolean removeLocal = false;
                boolean removeRemote = false;
                boolean spam = false;
                if (LearnTypeOption.SPAM.equals(Spamc.getLearnType())) {
                    spam = true;
                    setLocal = true;
                    setRemote = true;
                } else if (LearnTypeOption.HAM.equals(Spamc.getLearnType())) {
                    setLocal = true;
                    setRemote = true;
                } else if (LearnTypeOption.FORGET.equals(Spamc.getLearnType())) {
                    removeLocal = true;
                    removeRemote = true;
                } else {
                    throw new IllegalStateException("Invalid learn type: " + Spamc.getLearnType());
                }

                response = spamc.tell(message, spam, setLocal, setRemote, removeLocal, removeRemote);
                System.out.println(response.getProcessedMessage());
            } else {
                // default to normal processing
                response = spamc.process(message);
                System.out.println(response.getProcessedMessage());
            }
            int status;
            if (Spamc.useExitCode.get()) {
                if (response.isSpam()) {
                    status = ExitCodes.EX_ISSPAM;
                } else {
                    status = ExitCodes.EX_NOTSPAM;
                }
            } else {
                // use the exit code returned by spamd
                status = response.getResponseCode();
            }
            System.exit(status);
        } catch (final UsageException e) {
            System.err.println("invalid usage");
            System.err.println(e.getMessage());
        } catch (final UnknownHostException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final ConfigurationException e) {
            e.printStackTrace();
        } finally {
            if ((response == null) && ((flags & SafeFallbackOption.FLAG) > 0) && (message != null) && ((flags & CheckOption.FLAG) == 0)) {
                // we couldn't get a response from the spamd server and the safe
                // fallback option is enabled so just output the original
                // message
                System.out.println(message);
            }
        }
    }

    private static final String CONFIG_FILE_NAME = "spamc.conf";

    private static class ConfigurationException extends Exception {

        protected ConfigurationException(final String message) {
            super(message);
        }

        public static int getExitCode() {
            return ExitCodes.EX_CONFIG;
        }
    }

    /**
     * Combine the arguments passed on the command line with those included
     * in the configuration file (if it exists).
     *
     * @param userConfig
     * @param args
     * @return
     * @throws ConfigurationException
     */
    private static String[] combineArgs(final File userConfig, final String[] args) throws ConfigurationException {
        final List<String> combined = new ArrayList<>();
        File configFile;
        boolean userDefinedConfigFile;
        if (userConfig == null) {
            userDefinedConfigFile = false;
            configFile = new File(CONFIG_FILE_NAME);
        } else {
            userDefinedConfigFile = true;
            configFile = userConfig;
        }

        // read the arguments in from the file first
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(configFile));
            String line;
            String tokens[];
            while (reader.ready()) {
                line = reader.readLine();
                if(line == null) {
                    break;
                }
                if (line.length() > 0 && (line.charAt(0) == '#' || line.charAt(0) == '\r' || line.charAt(0) == '\n')) {
                    continue;
                }
                tokens = line.split("\\s");
                for (int i = 0; i < tokens.length; i++) {
                    combined.add(tokens[i]);
                }
            }
        } catch (final IOException e) {
            if (userDefinedConfigFile) {
                throw new ConfigurationException("Failed to open config file: " + configFile.getPath());
            }
        } finally {
            Streams.close(reader);
        }

        // arguments entered at the command line are processed next
        for (int i = 0; i < args.length; i++) {
            combined.add(args[i]);
        }

        final String[] combinedArray = new String[combined.size()];
        // we can't use the toArray() method because that returns the wrong data
        // type
        for (int i = 0; i < combined.size(); i++) {
            combinedArray[i] = combined.get(i);
        }
        return combinedArray;
    }

    /**
     * SSL support may not be included for some JVMs so we will try to load
     * it dynamically
     */
    private static final String SSL_PACKAGE_NAME = "javax.net.ssl";

    private static Package sslPackage;
    static {
        try {
            // try to load the package
            Class.forName(SSL_PACKAGE_NAME + ".SSLContext");
            sslPackage = Package.getPackage(SSL_PACKAGE_NAME);
        } catch (final ClassNotFoundException e) {
            // SSL is not available
        }
    }

    static void printVersion() {
        System.out.print("SpamAssassin Client version " + Spamc.class.getPackage().getImplementationVersion() + "\n");
        if (sslPackage != null) {
            System.out.print("  compiled with SSL support (" + sslPackage.getImplementationVersion() + ")\n");
        }
    }

    static void printUsage() {
        printVersion();
        System.out.print("\n" + "Usage: " + Spamc.getSimpleClassName() + " [options] [" + "-e" + " command [args]] < message\n" + "\n" + "Options:\n" + "  " + "-d" + ", " + "--dest" + " host[,host2]\n" + "                      Specify one or more hosts to connect to.\n" + "                      [default: " + DEFAULT_HOST + "]\n" + "  " + "-H" + " , " + "--randomize" + "    Randomize IP addresses for the looked-up\n" + "                      hostname.\n" + "  " + "-p" + ", " + "--port" + " port     Specify port for connection to spamd.\n" + "                      [default: " + DEFAULT_PORT + "]\n" + "  " + "-S" + ", " + "--ssl" + "           Use SSL to talk to spamd.\n" + "  " + "-U" + ", " + "--socket" + " path   Connect to spamd via UNIX domain sockets.\n" + "  " + "-F" + ", " + "--config" + " path   Use this configuration file.\n" + "  " + "-t" + ", " + "--timeout" + " timeout\n" + "                      Timeout in seconds for communications to\n" + "                      spamd. [default: " + DEFAULT_TIMEOUT_SECONDS + "]\n" + "  " + "--connect-retries" + " retries\n" + "                      Try connecting to spamd this many times\n" + "                      [default: " + DEFAULT_CONNECT_RETRIES + "]\n" + "  " + "--retry-sleep" + " sleep Sleep for this time between attempts to\n" + "                      connect to spamd, in seconds [default: " + DEFAULT_RETRY_SLEEP_SECONDS + "]\n" + "  " + "-s" + ", " + "--max-size" + " size Specify maximum message size, in bytes.\n" + "                      [default: " + (DEFAULT_MAX_SIZE_BYTES / BYTES_PER_KB) + "k]\n" + "  " + "-u" + ", " + "--username" + " username\n" + "                      User for spamd to process this message under.\n" + "                      [default: current user]\n" + "  " + "-L" + ", " + "--learntype" + " learntype\n" + "                      Learn message as " + LearnTypeOption.SPAM + ", " + LearnTypeOption.HAM + " or " + LearnTypeOption.FORGET + " to\n" + "                      forget or unlearn the message.\n" + "  " + "-C" + ", " + "--reporttype" + " reporttype\n" + "                      Report message to collaborative filtering\n" + "                      databases.  Report type should be '" + ReportTypeOption.REPORT + "' for\n" + "                      spam or '" + ReportTypeOption.REVOKE + "' for ham.\n" + "  " + "-B" + ", " + "--bsmtp" + "         Assume input is a single BSMTP-formatted\n" + "                      message.\n" + "  " + "-c" + ", " + "--check" + "         Just print the summary line and set an exit\n" + "                      code.\n" + "  " + "-y" + ", " + "--tests" + "         Just print the names of the tests hit.\n" + "  " + "-r" + ", " + "--full-spam" + "     Print full report for messages identified as\n" + "                      spam.\n" + "  " + "-R" + ", " + "--full" + "          Print full report for all messages.\n" + "  " + "--headers" + "           Rewrite only the message headers.\n" + "  " + "-E" + ", " + "--exitcode" + "      Filter as normal, and set an exit code.\n" + "  " + "-x" + ", " + "--no-safe-fallback" + "\n" + "                      Don't fallback safely.\n" + "  " + "-l" + ", " + "--log-to-stderr" + " Log errors and warnings to stderr.\n" + "  " + "-e" + ", " + "--pipe-to" + " command [args]\n" + "                      Pipe the output to the given command instead\n" + "                      of stdout. This must be the last option.\n" + "  " + "-h" + ", " + "--help" + "          Print this help message and exit.\n" + "  " + "-V" + ", " + "--version" + "       Print spamc version and exit.\n" + "  " + "-K" + "                  Keepalive check of spamd.\n" + "  " + "-z" + "                  Compress mail message sent to spamd.\n" + "  " + "-f" + "                  (Now default, ignored.)\n" + "\n");

    }

    private static abstract class AbstractOption {

        private final String shortName;
        private final String longName;
        private final boolean hasArgument;

        protected AbstractOption(final String longName, final boolean hasArgument) {
            this(null, longName, hasArgument);
        }

        protected AbstractOption(final String shortName, final String longName, final boolean hasArgument) {
            super();
            this.longName = longName;
            this.hasArgument = hasArgument;
            this.shortName = shortName;
        }

        protected String getShortName() {
            return shortName;
        }

        protected String getLongName() {
            return longName;
        }

        protected boolean hasArgument() {
            return hasArgument;
        }

        protected long apply(final Spamc spamc, final long flags) throws UsageException {
            // this method will be overridden where necessary by subclasses
            return flags;
        }

        protected long apply(final String argument, final Spamc spamc, final long flags) throws UsageException {
            if (this.hasArgument() && ((argument == null) || (argument.trim().length() == 0))) {
                throw new NoArgumentException();
            }
            return flags;
        }

        /**
         * Check whether we should continue running after processing this
         * option. For some options, like those that print usage and version
         * information, we need to immediately stop.
         *
         * @return <code>true</code>
         */
        protected boolean keepRunning() {
            return true;
        }
    }

    private static class DestinationOption extends AbstractOption {

        DestinationOption() {
            super("-d", "--dest", true);
        }

        @Override
        protected long apply(final String hosts, final Spamc spamc, final long flags) throws UsageException {
            final long newFlags = super.apply(hosts, spamc, flags);
            final String[] splitHosts = hosts.split(",");
            spamc.setHosts(splitHosts);
            return newFlags;
        }
    }

    private static class RandomizeOption extends AbstractOption {

        RandomizeOption() {
            super("-H", "--randomize", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) throws UsageException {
            final long newFlags = super.apply(spamc, flags);
            spamc.setRandomize(true);
            return newFlags;
        }
    }

    private static class PortOption extends AbstractOption {

        PortOption() {
            super("-p", "--port", true);
        }

        @Override
        protected long apply(final String port, final Spamc spamc, final long flags) throws UsageException {
            final long newFlags = super.apply(port, spamc, flags);
            spamc.setPort(Integer.parseInt(port));
            return newFlags;
        }
    }

    private static class SSLOption extends AbstractOption {

        // option flags, copied from libspamc.h
        private final static long FLAG = 1 << 27;

        SSLOption() {
            super("-S", "--ssl", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            spamc.setUseSSL(true);
            return flags | FLAG;
        }
    }

    private static class SocketOption extends AbstractOption {

        SocketOption() {
            super("-U", "--socket", true);
        }

        @Override
        protected long apply(final String path, final Spamc spamc, final long flags) throws UsageException {
            super.apply(path, spamc, flags);
            spamc.setUseUnixSockets(true);
            spamc.setUnixSocketPath(path);
            return flags;
        }
    }

    private static class ConfigOption extends AbstractOption {

        ConfigOption() {
            super("-F", "--config", true);
        }
    }

    private static class TimeoutOption extends AbstractOption {

        TimeoutOption() {
            super("-t", "--timeout", true);
        }

        @Override
        protected long apply(final String timeout, final Spamc spamc, final long flags) throws UsageException {
            super.apply(timeout, spamc, flags);
            spamc.setTimeout(Integer.parseInt(timeout));
            return flags;
        }
    }

    private static class ConnectRetriesOption extends AbstractOption {

        ConnectRetriesOption() {
            super("--connect-retries", true);
        }

        @Override
        protected long apply(final String retries, final Spamc spamc, final long flags) throws UsageException {
            super.apply(retries, spamc, flags);
            spamc.setConnectRetries(Integer.parseInt(retries));
            return flags;
        }
    }

    private static class RetrySleepOption extends AbstractOption {

        RetrySleepOption() {
            super("--retry-sleep", true);
        }

        @Override
        protected long apply(final String sleep, final Spamc spamc, final long flags) throws UsageException {
            super.apply(sleep, spamc, flags);
            spamc.setRetrySleep(Integer.parseInt(sleep));
            return flags;
        }
    }

    private static class MaxSizeOption extends AbstractOption {

        MaxSizeOption() {
            super("-s", "--max-size", true);
        }

        @Override
        protected long apply(final String size, final Spamc spamc, final long flags) throws UsageException {
            super.apply(size, spamc, flags);
            spamc.setMaxSize(Long.parseLong(size));
            return flags;
        }
    }

    private static class UsernameOption extends AbstractOption {

        UsernameOption() {
            super("-u", "--username", true);
        }

        @Override
        protected long apply(final String userName, final Spamc spamc, final long flags) throws UsageException {
            super.apply(userName, spamc, flags);
            spamc.setUserName(userName);
            return flags;
        }
    }

    private static class LearnTypeOption extends AbstractOption {

        private static final String FORGET = "forget";

        private static final String HAM = "ham";

        private static final String SPAM = "spam";

        final static long FLAG = 1 << 21;

        LearnTypeOption() {
            super("-L", "--learntype", true);
        }

        @Override
        protected long apply(final String learntype, final Spamc spamc, final long flags) throws UsageException {
            final long newFlags = super.apply(learntype, spamc, flags);
            if (!LearnTypeOption.SPAM.equals(learntype) && !LearnTypeOption.HAM.equals(learntype) && !LearnTypeOption.FORGET.equals(learntype)) {
                throw new UsageException("Please specifiy a legal learn type");
            }
            Spamc.setLearnType(learntype);
            return newFlags | FLAG;
        }
    }

    private static class ReportTypeOption extends AbstractOption {

        private static final String REPORT = "report";

        private static final String REVOKE = "revoke";

        final static long FLAG = 1 << 20;

        ReportTypeOption() {
            super("-C", "--reporttype", true);
        }

        @Override
        protected long apply(final String reportType, final Spamc spamc, final long flags) throws UsageException {
            final long newFlags = super.apply(reportType, spamc, flags);
            if (!ReportTypeOption.REPORT.equals(reportType) && !ReportTypeOption.REVOKE.equals(reportType)) {
                throw new UsageException("Please specifiy a legal report type");
            }
            Spamc.setReportType(reportType);
            return newFlags | FLAG;
        }
    }

    private static class BSMTPOption extends AbstractOption {

        private static final long FLAG = 1;

        BSMTPOption() {
            super("-B", "--bsmtp", false);
        }

        protected void apply(final Spamc spamc) {
            spamc.setAssumeBSMTP(true);
        }

        protected long getFlag() {
            return FLAG;
        }
    }

    private static class CheckOption extends AbstractOption {

        final static long FLAG = 1 << 29;

        CheckOption() {
            super("-c", "--check", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            Spamc.useExitCode.set(true);
            return flags | FLAG;
        }
    }

    private static class TestsOption extends AbstractOption {

        private final static long FLAG = 1 << 24;

        TestsOption() {
            super("-y", "--tests", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            return flags | FLAG;
        }
    }

    private static class FullSpamOption extends AbstractOption {

        final static long FLAG = 1 << 25;

        FullSpamOption() {
            super("-r", "--full-spam", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            return flags | FLAG;
        }
    }

    private static class FullOption extends AbstractOption {

        final static long FLAG = 1 << 26;

        FullOption() {
            super("-R", "--full", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            return flags | FLAG;
        }
    }

    private static class HeadersOption extends AbstractOption {

        final static long FLAG = 1 << 15;

        HeadersOption() {
            super("--headers", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            return flags | FLAG;
        }
    }

    static final AtomicBoolean useExitCode = new AtomicBoolean(false);

    private static class ExitCodeOption extends AbstractOption {

        ExitCodeOption() {
            super("-E", "--exitcode", false);
        }

        protected void apply(final Spamc spamc) {
            Spamc.useExitCode.set(true);
        }
    }

    private static class NoSafeFallbackOption extends AbstractOption {

        NoSafeFallbackOption() {
            super("-x", "--no-safe-fallback", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            // turn off the safe fallback flag
            return flags & ~SafeFallbackOption.FLAG;
        }
    }

    private static class LogToStderrOption extends AbstractOption {

        final static long FLAG = 1 << 22;

        LogToStderrOption() {
            super("-l", "--log-to-stderr", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            return flags | FLAG;
        }
    }

    private static class PipeToOption extends AbstractOption {

        PipeToOption() {
            super("-e", "--pipe-to", true);
        }
    }

    private static class VersionOption extends AbstractOption {

        VersionOption() {
            super("-V", "--version", false);
        }

        protected void apply(final Spamc ignored) {
            Spamc.printVersion();
        }

        /**
         * Stop running after printing version information.
         *
         * @return <code>false</code>
         */
        @Override
        protected boolean keepRunning() {
            return false;
        }
    }

    private static class HelpOption extends AbstractOption {

        protected HelpOption() {
            super("-h", "--help", false);
        }

        protected void apply(final Spamc ignored) {
            Spamc.printUsage();
        }

        /**
         * Stop running after printing usage information.
         *
         * @return <code>false</code>
         */
        @Override
        protected boolean keepRunning() {
            return false;
        }
    }

    private static class CompressOption extends AbstractOption {

        final static long FLAG = 1 << 16;

        CompressOption() {
            super("-z", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            spamc.setCompress(true);
            return flags | FLAG;
        }
    }

    private static class SafeFallbackOption extends AbstractOption {

        final static long FLAG = 1 << 28;

        SafeFallbackOption() {
            super("-f", false);
        }
    }

    private static class KeepAliveOption extends AbstractOption {

        final static long FLAG = 1 << 19;

        KeepAliveOption() {
            super("-K", false);
        }

        @Override
        protected long apply(final Spamc spamc, final long flags) {
            return flags | FLAG;
        }
    }

    private static class SpamcOptions {

        final Map<String, AbstractOption> shortArgumentsMap;
        final Map<String, AbstractOption> longArgumentsMap;

        SpamcOptions(Map<String, AbstractOption> shortArgumentsMap, Map<String, AbstractOption> longArgumentsMap) {
            super();
            this.shortArgumentsMap = ImmutableMap.copyOf(shortArgumentsMap);
            this.longArgumentsMap = ImmutableMap.copyOf(longArgumentsMap);
        }
    }

    private static SpamcOptions createOptions() {
        Collection<AbstractOption> options = new ArrayList<AbstractOption>(32);
        options.add(new DestinationOption());
        options.add(new RandomizeOption());
        options.add(new PortOption());
        options.add(new SSLOption());
        options.add(new SocketOption());
        options.add(new ConfigOption());
        options.add(new TimeoutOption());
        options.add(new ConnectRetriesOption());
        options.add(new RetrySleepOption());
        options.add(new MaxSizeOption());
        options.add(new UsernameOption());
        options.add(new LearnTypeOption());
        options.add(new ReportTypeOption());
        options.add(new BSMTPOption());
        options.add(new CheckOption());
        options.add(new TestsOption());
        options.add(new FullSpamOption());
        options.add(new FullOption());
        options.add(new HeadersOption());
        options.add(new ExitCodeOption());
        options.add(new NoSafeFallbackOption());
        options.add(new LogToStderrOption());
        options.add(new PipeToOption());
        options.add(new HelpOption());
        options.add(new VersionOption());
        options.add(new CompressOption());
        options.add(new KeepAliveOption());

        // store all short and long argument names as map keys so that
        // we will have a fast index to them when parsing the command
        // line
        Map<String, AbstractOption> shortArgumentsMap = new HashMap<>(options.size());
        Map<String, AbstractOption> longArgumentsMap = new HashMap<>(options.size());
        for (AbstractOption option : options) {
            if (option.getShortName() != null) {
                shortArgumentsMap.put(option.getShortName(), option);
            }
            if (option.getLongName() != null) {
                longArgumentsMap.put(option.getLongName(), option);
            }
        }

        return new SpamcOptions(shortArgumentsMap, longArgumentsMap);
    }

    private static class UsageException extends Exception {

        private static final long serialVersionUID = -2852631284561061184L;

        protected UsageException() {
            super();
        }

        protected UsageException(final String message) {
            super(message);
        }
    }

    private static class ArgumentUsageException extends UsageException {

        private static final long serialVersionUID = 3473869403464966828L;

        private int argumentIndex = -1;
        private int charIndex = -1;

        protected ArgumentUsageException() {
            super();
        }

        protected ArgumentUsageException(final int argumentIndex, final int charIndex) {
            super();
            this.argumentIndex = argumentIndex;
            this.charIndex = charIndex;
        }

        protected String getMessagePrefix() {
            return "Error in argument " + (argumentIndex + 1) + ", char " + (charIndex + 1) + ": ";
        }

        @Override
        public String getMessage() {
            return getMessagePrefix();
        }

    }

    private static class OptionNotFoundException extends ArgumentUsageException {

        private static final long serialVersionUID = -4806428789728756802L;

        private String option;

        protected OptionNotFoundException(final int argumentIndex, final int charIndex, final String option) {
            super(argumentIndex, charIndex);
            this.option = option;
            // trim off any leading "-" characters
            while (this.option.charAt(0) == '-') {
                this.option = this.option.substring(1);
            }
        }

        @Override
        public String getMessage() {
            return getMessagePrefix() + "option not found " + option;
        }
    }

    private static class NoArgumentException extends ArgumentUsageException {

        private static final long serialVersionUID = -8957133343189214627L;

        private String option;

        protected NoArgumentException() {
            super();
        }

        protected NoArgumentException(final int argumentIndex, final int charIndex, final String option) {
            super(argumentIndex, charIndex);
            this.option = option;
            // trim off any leading "-" characters
            while (this.option.charAt(0) == '-') {
                this.option = this.option.substring(1);
            }
        }

        @Override
        public String getMessage() {
            return getMessagePrefix() + "no argument for option " + option;
        }
    }

    /**
     * Values from <code>sysexits.h</code>, plus some additional values
     * defined for SpamAsassin. One of these should be included in the spamd
     * response.
     *
     * @see SpamdResponse#getResponseCode()
     */
    public static class ExitCodes {
        // sysexits.h exit codes

        /** successful termination */
        public static final int EX_OK = 0;

        /** base value for error messages */
        public static final int EX__BASE = 64;

        /** command line usage error */
        public static final int EX_USAGE = 64;

        /** data format error */
        public static final int EX_DATAERR = 65;

        /** cannot open input */
        public static final int EX_NOINPUT = 66;

        /** addressee unknown */
        public static final int EX_NOUSER = 67;

        /** host name unknown */
        public static final int EX_NOHOST = 68;

        /** service unavailable */
        public static final int EX_UNAVAILABLE = 69;

        /** internal software error */
        public static final int EX_SOFTWARE = 70;

        /** system error (e.g., can't fork) */
        public static final int EX_OSERR = 71;

        /** critical OS file missing */
        public static final int EX_OSFILE = 72;

        /** can't create (user) output file */
        public static final int EX_CANTCREAT = 73;

        /** input/output error */
        public static final int EX_IOERR = 74;

        /** temp failure; user is invited to retry */
        public static final int EX_TEMPFAIL = 75;

        /** remote error in protocol */
        public static final int EX_PROTOCOL = 76;

        /** permission denied */
        public static final int EX_NOPERM = 77;

        /** configuration error */
        public static final int EX_CONFIG = 78;

        // SpamAssassin-specific exit codes

        public static final int EX_NOTSPAM = 0;

        public static final int EX_ISSPAM = 1;

        public static final int EX_TOOBIG = 866;
    }

    private static final AtomicReference<String> learnType = new AtomicReference<String>();

    static void setLearnType(final String learnType) {
        Spamc.learnType.set(learnType);
    }

    private static String getLearnType() {
        return Spamc.learnType.get();
    }

    private static final AtomicReference<String> reportType = new AtomicReference<String>();

    static void setReportType(final String reportType) {
        Spamc.reportType.set(reportType);
    }

    private static String getReportType() {
        return Spamc.reportType.get();
    }

    private static long readArgs(final Spamc spamc, final String[] args) throws UsageException {
        // default to safe fallback
        long flags = SafeFallbackOption.FLAG;
        SpamcOptions spamcOptions = createOptions();
        Map<String, AbstractOption> shortArgumentsMap = spamcOptions.shortArgumentsMap;
        Map<String, AbstractOption> longArgumentsMap = spamcOptions.longArgumentsMap;

        String optionalArg = null;
        AbstractOption option;
        for (int i = 0; i < args.length; i++) {
            if ((i + 1) < args.length) {
                // get the next argument, which could be the option for this argument
                optionalArg = args[i + 1];
            } else {
                optionalArg = null;
            }

            option = null;
            if (shortArgumentsMap.containsKey(args[i])) {
                option = shortArgumentsMap.get(args[i]);
            } else if (longArgumentsMap.containsKey(args[i])) {
                option = longArgumentsMap.get(args[i]);
            }
            if (option == null) {
                throw new OptionNotFoundException(i, args[i].length(), args[i]);
            }

            if (option.hasArgument()) {
                if (optionalArg == null) {
                    throw new NoArgumentException(i, args[i].length(), args[i]);
                }
                flags = option.apply(optionalArg, spamc, flags);
                // skip the next argument
                i++;
            } else {
                flags = option.apply(spamc, flags);
            }
            if (!option.keepRunning()) {
                // stop processing any more arguments
                return flags;
            }
        }

        // learning action has to block some parameters
        if ((flags & LearnTypeOption.FLAG) > 0) {
            if ((flags & CheckOption.FLAG) > 0) {
                throw new UsageException("Learning excludes check only");
            }
            if ((flags & KeepAliveOption.FLAG) > 0) {
                throw new UsageException("Learning excludes ping");
            }
            if ((flags & FullSpamOption.FLAG) > 0) {
                throw new UsageException("Learning excludes report if spam");
            }
            if ((flags & FullOption.FLAG) > 0) {
                throw new UsageException("Learning excludes report");
            }
            if ((flags & TestsOption.FLAG) > 0) {
                throw new UsageException("Learning excludes symbols");
            }
            if ((flags & ReportTypeOption.FLAG) > 0) {
                throw new UsageException("Learning excludes reporting to collaborative filtering databases");
            }
        }

        return flags;
    }

    private static boolean isEmpty(final String str) {
        if (null == str) {
            return true;
        }
        final char[] chars = str.toCharArray();
        boolean empty = true;
        for (int i = 0; empty && i < chars.length; i++) {
            empty = isWhitespace(chars[i]);
        }
        return empty;
    }

    /**
     * High speed test for whitespace! Faster than the java one (from some testing).
     *
     * @return <code>true</code> if the indicated character is whitespace; otherwise <code>false</code>
     */
    private static boolean isWhitespace(final char c) {
        switch (c) {
            case 9:  //'unicode: 0009
            case 10: //'unicode: 000A'
            case 11: //'unicode: 000B'
            case 12: //'unicode: 000C'
            case 13: //'unicode: 000D'
            case 28: //'unicode: 001C'
            case 29: //'unicode: 001D'
            case 30: //'unicode: 001E'
            case 31: //'unicode: 001F'
            case ' ': // Space
                //case Character.SPACE_SEPARATOR:
                //case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return true;
            default:
                return false;
        }
    }

}
