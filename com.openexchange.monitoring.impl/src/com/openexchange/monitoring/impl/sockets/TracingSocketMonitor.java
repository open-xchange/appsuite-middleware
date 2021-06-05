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

package com.openexchange.monitoring.impl.sockets;

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
import java.util.Optional;
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
import com.openexchange.log.DedicatedFileLoggerFactory;
import com.openexchange.log.LogConfiguration;
import com.openexchange.monitoring.sockets.SocketMonitor;
import com.openexchange.monitoring.sockets.SocketStatus;
import com.openexchange.monitoring.sockets.failure.ConnectFailure;
import com.openexchange.monitoring.sockets.failure.IOFailure;
import com.openexchange.monitoring.sockets.failure.SocketFailure;
import com.openexchange.monitoring.sockets.failure.TimeoutFailure;
import com.openexchange.net.HostList;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.planetj.math.rabinhash.RabinHashFunction32;

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

            Builder() {
                super();
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
                return new TracingSocketMonitorConfig(numberOfSamplesPerSocket, thresholdMillis, withRequestData, filter, filterPorts, keepIdleThreshold);
            }
        }

        // -------------------------------------------------------------------------------------------------

        private final int numberOfSamplesPerSocket;
        private final int thresholdMillis;
        private final boolean withRequestData;
        private final HostList filter;
        private final int[] filterPorts;
        private final long keepIdleThreshold;

        TracingSocketMonitorConfig(int numberOfSamplesPerSocket, int thresholdMillis, boolean withRequestData, HostList filter, int[] filterPorts, long keepIdleThreshold) {
            super();
            this.numberOfSamplesPerSocket = numberOfSamplesPerSocket;
            this.thresholdMillis = thresholdMillis;
            this.withRequestData = withRequestData;
            this.filter = filter;
            this.filterPorts = filterPorts;
            this.keepIdleThreshold = keepIdleThreshold;
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

    // -----------------------------------------------------------------------------------------------------

    private final TracingSocketMonitorConfig config;
    private final ConcurrentMap<Socket, SocketTrace> socketTraces;
    private final ScheduledTimerTask timerTask;
    private final Optional<Logger> fileLogger;

    /**
     * Initializes a new {@link TracingSocketMonitor}.
     */
    public TracingSocketMonitor(TracingSocketMonitorConfig config, LogConfiguration logConfig, TimerService timerService) {
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

        this.fileLogger = DedicatedFileLoggerFactory.createOrReinitializeLogger(logConfig);
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
            return null == filter || (!filter.isEmpty() && filter.contains(socket.getInetAddress()));
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

            Optional<Logger> fileLogger = this.fileLogger;
            fileLogger.ifPresent((logger) -> logger.error("Failed to connect to socket {}", socket, failure.getE()));
        }
    }

    @Override
    public void connected(Socket socket) throws IOException {
        Optional<Logger> fileLogger = this.fileLogger;
        fileLogger.ifPresent((logger) -> {
            isAllowed(socket);
            logger.info("Opened connection to socket {}", socket);
        });
    }

    @Override
    public void closed(Socket socket) throws IOException {
        if (isAllowed(socket)) {
            SocketTrace trace = optSocketTraceFor(socket);
            if (null != trace && false == trace.isRecordingSamples()) {
                // No samples are recorded, hence drop associated SocketTrace instance
                socketTraces.remove(socket);
            }

            Optional<Logger> fileLogger = this.fileLogger;
            fileLogger.ifPresent((logger) -> logger.info("Closed connection to socket {}", socket));
        }
    }

    // ------------------------------------------------------------------------------

    /**
     * Puts socket-specific log properties.<br>
     * Example:
     * <pre>
     * com.openexchange.monitoring.sockets.166120149.accumulatedWaitMillis=523
     * com.openexchange.monitoring.sockets.166120149.host=imap.domain.tld
     * com.openexchange.monitoring.sockets.166120149.port=143
     * com.openexchange.monitoring.sockets.166120149.status=OK
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
        private final Optional<Logger> fileLogger;

        /**
         * Initializes a new {@link TracingSocketMonitor.SocketTrace}.
         */
        SocketTrace(Socket socket, int numberOfSamplesPerSocket, int thresholdMillis, boolean withRequestData, Optional<Logger> fileLogger) {
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

                Optional<Logger> fileLogger = this.fileLogger;
                fileLogger.ifPresent((logger) -> {
                    if (thresholdExceeded) {
                        logger.warn("Read from socket {} took {}msec", socket, Long.valueOf(duration));
                    } else {
                        logger.info("Read from socket {} took {}msec", socket, Long.valueOf(duration));
                    }
                });
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

                final long d = duration;
                Optional<Logger> fileLogger = this.fileLogger;
                fileLogger.ifPresent((logger) -> {
                    Exception e = failure.getE();
                    logger.error("Failed to read from socket {} after {}msec", socket, Long.valueOf(d), e);
                });
            }
            return set;
        }

        boolean endTracingEof() {
            boolean set = traceMode.compareAndSet(true, false);
            if (set) {
                // Determine duration
                long now = System.currentTimeMillis();
                final long duration = now - traceStart.get();
                boolean thresholdExceeded = duration >= thresholdMillis;

                endTracing0(SocketStatus.EOF, duration, now, thresholdExceeded);

                Optional<Logger> fileLogger = this.fileLogger;
                fileLogger.ifPresent((logger) -> {
                    if (thresholdExceeded) {
                        logger.warn("EOF while reading from socket {} after {}msec", socket, Long.valueOf(duration));
                    } else {
                        logger.info("EOF while reading from socket {} after {}msec", socket, Long.valueOf(duration));
                    }
                });
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
