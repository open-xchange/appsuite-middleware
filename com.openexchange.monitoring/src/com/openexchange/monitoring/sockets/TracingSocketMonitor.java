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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.monitoring.sockets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.MDC;
import com.google.common.base.Preconditions;
import com.openexchange.java.Strings;
import com.openexchange.logback.extensions.ExtendedPatternLayoutEncoder;
import com.openexchange.net.HostList;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.planetj.math.rabinhash.RabinHashFunction32;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;

/**
 * {@link TracingSocketMonitor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class TracingSocketMonitor implements SocketMonitor {

    /** The configuration for socket tracing */
    public static final class TracingSocketMonitorConfig {

        /**
         * Creates a new builder instance.
         *
         * @return The new builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /** The builder for an instance of <code>TracingSocketMonitorConfig</code> */
        public static class Builder {

            private int numberOfSamplesPerSocket;
            private int thresholdMillis;
            private boolean withRequestData;
            private HostList filter;
            private int[] filterPorts;
            private long keepIdleThreshold;

            private boolean enableDedicatedLogging;
            private String logLevel;
            private String loggingFileLocation;
            private int loggingFileLimit;
            private int loggingFileCount;
            private String loggingFileLayoutPattern;

            Builder() {
                super();
                loggingFileLayoutPattern = "%date{\"yyyy-MM-dd'T'HH:mm:ss,SSSZ\"} %-5level [%thread]%n%sanitisedMessage%n%lmdc%exception{full}";
            }

            public Builder setEnableDedicatedLogging(boolean enableDedicatedLogging) {
                this.enableDedicatedLogging = enableDedicatedLogging;
                return this;
            }

            public Builder setLogLevel(String logLevel) {
                this.logLevel = logLevel;
                return this;
            }

            public Builder setLoggingFileCount(int loggingFileCount) {
                this.loggingFileCount = loggingFileCount;
                return this;
            }

            public Builder setLoggingFileLayoutPattern(String loggingFileLayoutPattern) {
                this.loggingFileLayoutPattern = loggingFileLayoutPattern;
                return this;
            }

            public Builder setLoggingFileLimit(int loggingFileLimit) {
                this.loggingFileLimit = loggingFileLimit;
                return this;
            }

            public Builder setLoggingFileLocation(String loggingFileLocation) {
                this.loggingFileLocation = loggingFileLocation;
                return this;
            }

            public Builder setNumberOfSamplesPerSocket(int numberOfSamplesPerSocket) {
                this.numberOfSamplesPerSocket = numberOfSamplesPerSocket;
                return this;
            }

            public Builder setThresholdMillis(int thresholdMillis) {
                this.thresholdMillis = thresholdMillis;
                return this;
            }

            public Builder setWithRequestData(boolean withRequestData) {
                this.withRequestData = withRequestData;
                return this;
            }

            public Builder setFilter(HostList filter) {
                this.filter = filter;
                return this;
            }

            public Builder setFilterPorts(int[] filterPorts) {
                this.filterPorts = filterPorts;
                return this;
            }

            public Builder setKeepIdleThreshold(long keepIdleThreshold) {
                this.keepIdleThreshold = keepIdleThreshold;
                return this;
            }

            public TracingSocketMonitorConfig build() {
                return new TracingSocketMonitorConfig(numberOfSamplesPerSocket, thresholdMillis, withRequestData, filter, filterPorts, keepIdleThreshold, enableDedicatedLogging, logLevel, loggingFileLocation, loggingFileLimit, loggingFileCount, loggingFileLayoutPattern);
            }
        }

        // -------------------------------------------------------------------------------------------------

        private final int numberOfSamplesPerSocket;
        private final int thresholdMillis;
        private final boolean withRequestData;
        private final HostList filter;
        private final int[] filterPorts;
        private final long keepIdleThreshold;
        private final boolean enableDedicatedLogging;
        private final String loggingFileLocation;
        private final int loggingFileLimit;
        private final int loggingFileCount;
        private final String loggingFileLayoutPattern;
        private final String logLevel;

        TracingSocketMonitorConfig(int numberOfSamplesPerSocket, int thresholdMillis, boolean withRequestData, HostList filter, int[] filterPorts, long keepIdleThreshold, boolean enableDedicatedLogging, String logLevel, String loggingFileLocation, int loggingFileLimit, int loggingFileCount, String loggingFileLayoutPattern) {
            super();
            this.numberOfSamplesPerSocket = numberOfSamplesPerSocket;
            this.thresholdMillis = thresholdMillis;
            this.withRequestData = withRequestData;
            this.filter = filter;
            this.filterPorts = filterPorts;
            this.keepIdleThreshold = keepIdleThreshold;
            this.enableDedicatedLogging = enableDedicatedLogging;
            this.logLevel = logLevel;
            this.loggingFileLocation = loggingFileLocation;
            this.loggingFileLimit = loggingFileLimit;
            this.loggingFileCount = loggingFileCount;
            this.loggingFileLayoutPattern = loggingFileLayoutPattern;
        }

        public boolean isEnableDedicatedLogging() {
            return enableDedicatedLogging;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public int getLoggingFileCount() {
            return loggingFileCount;
        }

        public String getLoggingFileLayoutPattern() {
            return loggingFileLayoutPattern;
        }

        public int getLoggingFileLimit() {
            return loggingFileLimit;
        }

        public String getLoggingFileLocation() {
            return loggingFileLocation;
        }

        public int getNumberOfSamplesPerSocket() {
            return numberOfSamplesPerSocket;
        }

        public int getThresholdMillis() {
            return thresholdMillis;
        }

        public boolean isWithRequestData() {
            return withRequestData;
        }

        public HostList getFilter() {
            return filter;
        }

        public int[] getFilterPorts() {
            return filterPorts;
        }

        public long getKeepIdleThreshold() {
            return keepIdleThreshold;
        }
    }

    static final Logger STD_LOGGER = org.slf4j.LoggerFactory.getLogger(TracingSocketMonitor.class);

    private static ch.qos.logback.classic.Logger staticFileLogger;
    private static RollingFileAppender<ILoggingEvent> staticRollingFileAppender;

    private static org.slf4j.Logger createOrReinitializeLogger(TracingSocketMonitorConfig config) {
        if (false == config.isEnableDedicatedLogging()) {
            return null;
        }

        // Check if a dedicated file location is specified
        String fileLocation = config.getLoggingFileLocation();
        if (Strings.isEmpty(fileLocation)) {
            STD_LOGGER.warn("File location for dedicated logging is empty. Disabling logging...");
            return null;
        }

        synchronized (TracingSocketMonitor.class) {
            ch.qos.logback.classic.Logger logbackLogger = staticFileLogger;
            if (null == logbackLogger) {
                logbackLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("SOCKET-MONITORING");
                staticFileLogger = logbackLogger;
            } else {
                logbackLogger.detachAndStopAllAppenders();
            }

            RollingFileAppender<ILoggingEvent> rollingFileAppender = staticRollingFileAppender;
            if (null != rollingFileAppender) {
                rollingFileAppender.stop();
                rollingFileAppender = null;
            }

            // Grab logger context from standard logger
            ch.qos.logback.classic.Logger templateLogger = (ch.qos.logback.classic.Logger) STD_LOGGER;
            LoggerContext context = templateLogger.getLoggerContext();

            String filePattern = fileLocation;

            ExtendedPatternLayoutEncoder encoder = new ExtendedPatternLayoutEncoder();
            encoder.setContext(context);
            encoder.setPattern("%date{\"yyyy-MM-dd'T'HH:mm:ss,SSSZ\"} %-5level [%thread] %class.%method\\(%class{0}.java:%line\\)%n%sanitisedMessage%n%lmdc%exception{full}");

            SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
            triggeringPolicy.setContext(context);
            triggeringPolicy.setMaxFileSize(FileSize.valueOf(Integer.toString(config.getLoggingFileLimit())));

            FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
            rollingPolicy.setContext(context);
            rollingPolicy.setFileNamePattern(filePattern + ".%i");
            rollingPolicy.setMinIndex(1);
            rollingPolicy.setMaxIndex(config.getLoggingFileCount());

            rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
            staticRollingFileAppender = rollingFileAppender;
            rollingFileAppender.setAppend(true);
            rollingFileAppender.setContext(context);
            rollingFileAppender.setEncoder(encoder);
            rollingFileAppender.setFile(filePattern);
            rollingFileAppender.setName("SocketMonitorAppender");
            rollingFileAppender.setPrudent(false);
            rollingFileAppender.setRollingPolicy(rollingPolicy);
            rollingFileAppender.setTriggeringPolicy(triggeringPolicy);

            rollingPolicy.setParent(rollingFileAppender);

            encoder.start();
            triggeringPolicy.start();
            rollingPolicy.start();
            rollingFileAppender.start();

            List<ch.qos.logback.core.status.Status> statuses = context.getStatusManager().getCopyOfStatusList();
            if (null != statuses && false == statuses.isEmpty()) {
                for (ch.qos.logback.core.status.Status status : statuses) {
                    if (rollingFileAppender.equals(status.getOrigin()) && (status instanceof ch.qos.logback.core.status.ErrorStatus)) {
                        ch.qos.logback.core.status.ErrorStatus errorStatus = (ch.qos.logback.core.status.ErrorStatus) status;
                        Throwable throwable = errorStatus.getThrowable();
                        if (null == throwable) {
                            class FastThrowable extends Throwable {

                                private static final long serialVersionUID = -1177996474876999361L;

                                FastThrowable(String msg) {
                                    super(msg);
                                }

                                @Override
                                public synchronized Throwable fillInStackTrace() {
                                    return this;
                                }
                            }
                            throwable = new FastThrowable(errorStatus.getMessage());
                        }

                        STD_LOGGER.warn("Illegal logging configuration. Reason: '{}'. Disabling logging...", throwable.getMessage());
                        return null;
                    }
                }
            }

            logbackLogger.addAppender(rollingFileAppender);
            {
                ch.qos.logback.classic.Level l;
                String level = config.getLogLevel();
                if (Strings.isEmpty(level)) {
                    l = ch.qos.logback.classic.Level.ERROR;
                } else {
                    level = Strings.asciiLowerCase(level.trim());
                    if ("all".equals(level)) {
                        l = ch.qos.logback.classic.Level.ALL;
                    } else if ("error".equals(level)) {
                        l = ch.qos.logback.classic.Level.ERROR;
                    } else if ("warn".equals(level) || "warning".equals(level)) {
                        l = ch.qos.logback.classic.Level.WARN;
                    } else if ("info".equals(level)) {
                        l = ch.qos.logback.classic.Level.INFO;
                    } else if ("debug".equals(level)) {
                        l = ch.qos.logback.classic.Level.DEBUG;
                    } else if ("trace".equals(level)) {
                        l = ch.qos.logback.classic.Level.TRACE;
                    } else {
                        l = ch.qos.logback.classic.Level.ERROR;
                    }
                }
                logbackLogger.setLevel(l);
            }
            logbackLogger.setAdditive(false);

            return logbackLogger;
        }
    }

    // -----------------------------------------------------------------------------------------------------

    private final TracingSocketMonitorConfig config;
    private final ConcurrentMap<Socket, SocketTrace> socketTraces;
    private final ScheduledTimerTask timerTask;
    private final org.slf4j.Logger fileLogger;

    /**
     * Initializes a new {@link TracingSocketMonitor}.
     */
    public TracingSocketMonitor(TracingSocketMonitorConfig config, TimerService timerService) {
        super();
        this.config = config;

        final ConcurrentMap<Socket, SocketTrace> socketTraces = new ConcurrentHashMap<>(256, 0.9F, 1);
        this.socketTraces = socketTraces;

        long keepIdleThreshold = config.getKeepIdleThreshold();
        if (keepIdleThreshold < 300000L) {
            keepIdleThreshold = 300000L;
        }

        final long thrsh = keepIdleThreshold;
        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    long tombstone = System.currentTimeMillis() - thrsh;
                    for (Iterator<SocketTrace> iter = socketTraces.values().iterator(); iter.hasNext();) {
                        if (iter.next().lastAccessed() < tombstone) {
                            iter.remove();
                        }
                    }
                } catch (Exception e) {
                    // Failed run...
                    STD_LOGGER.error("", e);
                }
            }
        };
        timerTask = timerService.scheduleAtFixedRate(task, 60000L, 60000L);

        this.fileLogger = createOrReinitializeLogger(config);
    }

    /**
     * Gets the configuration
     *
     * @return The configuration
     */
    public TracingSocketMonitorConfig getConfig() {
        return config;
    }

    /**
     * Stops this monitor.
     */
    public void stop() {
        timerTask.cancel();
    }

    private boolean isAllowed(Socket socket) {
        int[] filterPorts = config.getFilterPorts();
        if (null == filterPorts || Arrays.binarySearch(filterPorts, socket.getPort()) >= 0) {
            HostList filter = config.getFilter();
            return null == filter || filter.contains(socket.getInetAddress());
        }

        return false;
    }

    private SocketTrace getSocketTraceFor(Socket socket) {
        SocketTrace socketTrace = socketTraces.get(socket);
        if (null == socketTrace) {
            SocketTrace newSocketTrace = new SocketTrace(socket, config.getNumberOfSamplesPerSocket(), config.getThresholdMillis(), config.isWithRequestData(), fileLogger);
            socketTrace = socketTraces.putIfAbsent(socket, newSocketTrace);
            if (null == socketTrace) {
                socketTrace = newSocketTrace;
            }
        } else {
            socketTrace.touch();
        }
        return socketTrace;
    }

    private SocketTrace optSocketTraceFor(Socket socket) {
        SocketTrace socketTrace = socketTraces.get(socket);
        if (null != socketTrace) {
            socketTrace.touch();
        }
        return socketTrace;
    }

    /**
     * Gets all currently available socket traces.
     *
     * @return The socket traces
     */
    public List<SocketTrace> getSocketTraces() {
        int size = socketTraces.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        List<SocketTrace> traces = new ArrayList<>(size);
        for (SocketTrace socketTrace : socketTraces.values()) {
            traces.add(socketTrace);
        }
        return traces;
    }

    @Override
    public void write(Socket socket, int data) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = getSocketTraceFor(socket);
            trace.markTracing(new byte[] { (byte) data }, 0, 1);
        }
    }

    @Override
    public void write(Socket socket, byte[] data, int off, int len) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = getSocketTraceFor(socket);
            trace.markTracing(data, off, len);
        }
    }

    @Override
    public void read(Socket socket, int data) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = getSocketTraceFor(socket);
            trace.endTracing();
        }
    }

    @Override
    public void read(Socket socket, byte[] data, int off, int len) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = getSocketTraceFor(socket);
            trace.endTracing();
        }
    }

    @Override
    public void readTimedOut(Socket socket, TimeoutFailure failure) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = getSocketTraceFor(socket);
            trace.endTracing(failure);
        }
    }

    @Override
    public void readError(Socket socket, IOFailure failure) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = getSocketTraceFor(socket);
            trace.endTracing(failure);
        }
    }

    @Override
    public void eof(Socket socket) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = optSocketTraceFor(socket);
            if (null != trace) {
                trace.endTracingEof();
            }
        }
    }

    @Override
    public void connectError(Socket socket, ConnectFailure failure) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = optSocketTraceFor(socket);
            if (null != trace && false == trace.isRecordingSamples()) {
                socketTraces.remove(socket);
            }

            org.slf4j.Logger fileLogger = this.fileLogger;
            if (null != fileLogger) {
                fileLogger.error("Failed to connect to socket {}", socket, failure.getE());
            }
        }
    }

    @Override
    public void connected(Socket socket) throws IOException {
        org.slf4j.Logger fileLogger = this.fileLogger;
        if (null != fileLogger && isAllowed(socket)) {
            fileLogger.info("Opened connection to socket {}", socket);
        }
    }

    @Override
    public void closed(Socket socket) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = optSocketTraceFor(socket);
            if (null != trace && false == trace.isRecordingSamples()) {
                // No samples are recorded, hence drop associated SocketTrace instance
                socketTraces.remove(socket);
            }

            org.slf4j.Logger fileLogger = this.fileLogger;
            if (null != fileLogger) {
                fileLogger.info("Closed connection to socket {}", socket);
            }
        }
    }

    // ------------------------------------------------------------------------------

    /**
     * Puts socket-specific log properties.<br>
     * Example:
     * <pre>
     *  com.openexchange.monitoring.sockets.166120149.accumulatedWaitMillis=523
     *  com.openexchange.monitoring.sockets.166120149.host=imap.domain.tld
     *  com.openexchange.monitoring.sockets.166120149.port=143
     *  com.openexchange.monitoring.sockets.166120149.status=OK
     * </pre>
     *
     * @param name The property name
     * @param value The property value
     */
    static void putLogProperties(Socket socket, long duration, SocketStatus socketStatus) {
        // Build log property prefix for specified socket
        InetAddress address = socket.getInetAddress();
        String host = address.getHostName();
        if (Strings.isEmpty(host)) {
            host = address.getHostAddress();
        }
        int port = socket.getPort();

        StringBuilder keyBuilder = new StringBuilder(76);
        {
            // Calculate hash from <host> + ":" + <port> + "-" + <hashcode>
            keyBuilder.append(host).append(':').append(port).append('-').append(socket.hashCode());
            int hash = Math.abs(RabinHashFunction32.DEFAULT_HASH_FUNCTION.hash(keyBuilder.toString()));
            // Build key prefix
            keyBuilder.setLength(0);
            keyBuilder.append("com.openexchange.monitoring.sockets.").append(hash).append('.');
        }
        int resetLen = keyBuilder.length();

        // Host
        {
            String key = keyBuilder.append("host").toString();
            MDC.put(key, host);
        }

        // Port
        keyBuilder.setLength(resetLen);
        {
            String key = keyBuilder.append("port").toString();
            MDC.put(key, Integer.toString(port));
        }

        // Duration
        keyBuilder.setLength(resetLen);
        {
            String key = keyBuilder.append("accumulatedWaitMillis").toString();
            String sDuration = MDC.get(key);
            if (null == sDuration) {
                MDC.put(key, Long.toString(duration));
            } else {
                long prevDur = parseLong(sDuration);
                MDC.put(key, Long.toString(prevDur > 0 ? prevDur + duration : duration));
            }
        }

        // Status
        keyBuilder.setLength(resetLen);
        {
            String key = keyBuilder.append("status").toString();
            MDC.put(key, socketStatus.getId());
        }
    }

    /**
     * A trace for a socket.
     */
    public static final class SocketTrace {

        private static final Samples EMPTY_SAMPLES = new Samples(1) {

            @Override
            public void push(Sample sample) {
                // Nothing
            }

            @Override
            public long getAverageDuration() {
                return 0;
            }

            @Override
            public long getAverageTimeout() {
                return 0;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public java.util.Iterator<Sample> iterator() {
                return Collections.emptyIterator();
            }
        };

        private final AtomicBoolean traceMode;
        private final AtomicLong traceStart;
        private final ByteArrayOutputStream buffer;
        private final Samples samples;
        private final Socket socket;
        private final int thresholdMillis;
        private final AtomicLong lastAccessed;
        private final org.slf4j.Logger fileLogger;

        /**
         * Initializes a new {@link TracingSocketMonitor.SocketTrace}.
         */
        SocketTrace(Socket socket, int numberOfSamplesPerSocket, int thresholdMillis, boolean withRequestData, org.slf4j.Logger fileLogger) {
            super();
            this.socket = socket;
            this.thresholdMillis = thresholdMillis;
            traceMode = new AtomicBoolean(false);
            traceStart = new AtomicLong();
            samples = numberOfSamplesPerSocket > 0 ? new Samples(numberOfSamplesPerSocket) : null;
            buffer = withRequestData ? new ByteArrayOutputStream(2048) : null;
            lastAccessed = new AtomicLong(System.currentTimeMillis());
            this.fileLogger = fileLogger;
        }

        boolean isRecordingSamples() {
            return null != samples;
        }

        long lastAccessed() {
            return lastAccessed.get();
        }

        void touch() {
            lastAccessed.set(System.currentTimeMillis());
        }

        boolean markTracing(byte[] data, int off, int len) {
            boolean set = traceMode.compareAndSet(false, true);
            if (set) {
                if (null != buffer) {
                    buffer.reset();
                }
                traceStart.set(System.currentTimeMillis());
            }
            if (null != buffer) {
                buffer.write(data, off, len);
            }
            return set;
        }

        boolean endTracing() {
            boolean set = traceMode.compareAndSet(true, false);
            if (set) {
                // Determine duration and status
                SocketStatus status = SocketStatus.OK;
                long now = System.currentTimeMillis();
                long duration = now - traceStart.get();
                boolean thresholdExceeded = duration >= thresholdMillis;

                endTracing0(status, duration, now, thresholdExceeded);

                org.slf4j.Logger fileLogger = this.fileLogger;
                if (null != fileLogger) {
                    if (thresholdExceeded) {
                        fileLogger.warn("Read from socket {} took {}msec", socket, Long.valueOf(duration));
                    } else {
                        fileLogger.info("Read from socket {} took {}msec", socket, Long.valueOf(duration));
                    }
                }
            }
            return set;
        }

        boolean endTracing(SocketFailure<? extends Exception> failure) {
            boolean set = traceMode.compareAndSet(true, false);
            if (set) {
                // Determine duration and status
                SocketStatus status = failure.getStatus();
                long duration = failure.getMillis();
                long now = System.currentTimeMillis();
                if (duration < 0) {
                    duration = now - traceStart.get();
                }
                boolean thresholdExceeded = duration >= thresholdMillis;

                endTracing0(status, duration, now, thresholdExceeded);

                org.slf4j.Logger fileLogger = this.fileLogger;
                if (null != fileLogger) {
                    Exception e = failure.getE();
                    fileLogger.error("Failed to read from socket {} after {}msec", socket, Long.valueOf(duration), e);
                }
            }
            return set;
        }

        boolean endTracingEof() {
            boolean set = traceMode.compareAndSet(true, false);
            if (set) {
                // Determine duration
                long now = System.currentTimeMillis();
                long duration = now - traceStart.get();
                boolean thresholdExceeded = duration >= thresholdMillis;

                endTracing0(SocketStatus.EOF, duration, now, thresholdExceeded);

                org.slf4j.Logger fileLogger = this.fileLogger;
                if (null != fileLogger) {
                    if (thresholdExceeded) {
                        fileLogger.warn("EOF while reading from socket {} after {}msec", socket, Long.valueOf(duration));
                    } else {
                        fileLogger.info("EOF while reading from socket {} after {}msec", socket, Long.valueOf(duration));
                    }
                }
            }
            return set;
        }

        private void endTracing0(SocketStatus status, long duration, long now, boolean thresholdExceeded) {
            // Put to log properties
            putLogProperties(socket, duration, status);

            // Put to samples if threshold is satisfied
            if (thresholdExceeded && null != samples) {
                samples.push(new Sample(now, duration, null == buffer ? null : buffer.toByteArray(), status));
            }
        }

        /**
         * Gets the traced socket
         *
         * @return The socket
         */
        public Socket getSocket() {
            return socket;
        }

        /**
         * Gets the recorded samples
         *
         * @return The samples
         */
        public Samples getSamples() {
            return null == samples ? EMPTY_SAMPLES : samples;
        }
    }

    /**
     * A recorded sample providing:
     * <ul>
     * <li>The duration from first written byte(s) to first received bytes</li>
     * <li>Whether a read timeout occurred</li>
     * <li>Optionally: The request data</li>
     * </ul>
     */
    public static final class Sample {

        private final long timeStamp;
        final long durationMillis;
        private final byte[] request;
        private final SocketStatus status;

        Sample(long timeStamp, long durationMillis, byte[] request, SocketStatus status) {
            super();
            this.timeStamp = timeStamp;
            this.durationMillis = durationMillis;
            this.request = request;
            this.status = status;
        }

        /**
         * Gets the time stamp when this sample has been created
         *
         * @return The time stamp; in milliseconds, between the current time and midnight, January 1, 1970 UTC.
         */
        public long getTimeStamp() {
            return timeStamp;
        }

        /**
         * Checks if associated read attempt encountered a timeout
         *
         * @return <code>true</code> if timed out; otherwise <code>false</code>
         */
        public boolean isTimedOut() {
            return SocketStatus.TIMED_OUT == status;
        }

        /**
         * Checks if associated read attempt encountered an EOF
         *
         * @return <code>true</code> if EOF; otherwise <code>false</code>
         */
        public boolean isEof() {
            return SocketStatus.EOF == status;
        }

        /**
         * Checks if associated read attempt encountered an error
         *
         * @return <code>true</code> if EOF; otherwise <code>false</code>
         */
        public boolean isError() {
            return SocketStatus.READ_ERROR == status;
        }

        /**
         * Gets the status
         *
         * @return The status
         */
        public SocketStatus getStatus() {
            return status;
        }

        /**
         * Gets the duration in milliseconds
         *
         * @return The duration in milliseconds
         */
        public long getDurationMillis() {
            return durationMillis;
        }

        /**
         * Gets the request data (if enabled)
         *
         * @return The request data or <code>null</code> if not enabled
         */
        public byte[] getRequest() {
            return request;
        }
    }

    /**
     * A collection of samples recorded for a single socket.
     */
    public static class Samples implements Iterable<Sample> {

        private final Deque<Sample> list = new ConcurrentLinkedDeque<>();
        private final int maxEntries;
        private final ReentrantLock lock = new ReentrantLock();

        Samples(int maxEntries) {
            super();
            Preconditions.checkArgument(maxEntries > 0, "maxEntries must be greater than zero");
            this.maxEntries = maxEntries;
        }

        public void push(final Sample sample) {
            Preconditions.checkNotNull(sample, "Sample must not be null");
            lock.lock();
            try {
                list.push(sample);
                if (list.size() > maxEntries) {
                    list.removeLast();
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * Gets the average duration
         *
         * @return The average duration or <code>0</code> (zero) if there are no samples (yet)
         */
        public long getAverageDuration() {
            BigInteger avg = null;
            int numSamples = 0;
            for (Sample sample : list) {
                BigInteger dur = BigInteger.valueOf(sample.durationMillis);
                avg = null == avg ? dur : avg.add(dur);
                numSamples++;
            }
            return avg == null ? 0 : avg.divide(BigInteger.valueOf(numSamples)).longValue();
        }

        /**
         * Gets the average timeout
         *
         * @return The average timeout or <code>0</code> (zero) if there are no timed-out samples (yet)
         */
        public long getAverageTimeout() {
            BigInteger avg = null;
            int numSamples = 0;
            for (Sample sample : list) {
                if (sample.isTimedOut()) {
                    BigInteger dur = BigInteger.valueOf(sample.durationMillis);
                    avg = null == avg ? dur : avg.add(dur);
                    numSamples++;
                }
            }
            return avg == null ? 0 : avg.divide(BigInteger.valueOf(numSamples)).longValue();
        }

        /**
         * Gets the number of recorded samples.
         *
         * @return The number of samples
         */
        public int size() {
            return list.size();
        }

        /**
         * Gets an {@link Iterator} for currently recorded samples
         *
         * @return The samples' iterator
         */
        @Override
        public Iterator<Sample> iterator() {
            return list.iterator();
        }
    }

    private static long parseLong(String s) throws NumberFormatException {
        if (s == null) {
            return -1L;
        }

        int len = s.length();
        if (len <= 0) {
            return -1;
        }

        int i = 0;
        boolean negative = false;
        long limit = -Long.MAX_VALUE;
        char firstChar = s.charAt(0);
        if (firstChar < '0') { // Possible leading "+" or "-"
            if (firstChar == '-') {
                negative = true;
                limit = Long.MIN_VALUE;
            } else if (firstChar != '+') {
                return -1;
            }

            if (len == 1) {
                return -1;
            }
            i++;
        }

        long result = 0;
        int radix = 10;
        long multmin = limit / radix;
        int digit;
        while (i < len) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            if (result < multmin) {
                return -1;
            }
            result *= radix;
            if (result < limit + digit) {
                return -1;
            }
            result -= digit;
        }
        return negative ? result : -result;
    }

    private static int digit(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            default:
                return -1;
        }
    }

}
