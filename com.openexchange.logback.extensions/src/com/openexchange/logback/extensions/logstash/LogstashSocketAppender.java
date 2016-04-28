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

package com.openexchange.logback.extensions.logstash;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.SocketFactory;
import com.openexchange.exception.OXException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.net.AbstractSocketAppender;
import ch.qos.logback.core.net.DefaultSocketConnector;
import ch.qos.logback.core.net.SocketConnector;
import ch.qos.logback.core.net.SocketConnector.ExceptionHandler;
import ch.qos.logback.core.util.CloseUtil;
import ch.qos.logback.core.util.Duration;

/**
 * {@link LogstashSocketAppender}. Replica class of {@link AbstractSocketAppender}. The method {@link #dispatchEvents()} does the actual
 * work and is rewritten. Instead of sending serialised objects to the stream, a special {@link LogstashEncoder} is used to format all
 * logging events as JSON objects before sending them over the wire.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LogstashSocketAppender extends AppenderBase<ILoggingEvent> implements Runnable, ExceptionHandler, LogstashSocketAppenderMBean {

    /** Atomic reference for MBean registration */
    private static final AtomicReference<LogstashSocketAppender> REF = new AtomicReference<LogstashSocketAppender>();

    private static final float LOAD_FACTOR = 0.67f;

    private int port;
    private String remoteHost;
    private InetAddress address;

    private BlockingQueue<ILoggingEvent> queue;
    private int queueSize;
    private int loadThreshold;
    private float loadFactor;

    private String peerId;
    private Future<?> task;

    private Future<Socket> connectorTask;
    private volatile Socket socket;
    private int reconnectionDelay;
    private Duration eventDelayLimit;
    private int acceptConnectionTimeout;

    private Encoder<ILoggingEvent> encoder;

    private Boolean alwaysPersistEvents;

    /**
     * Get the appender's instance
     * 
     * @return the appender's instance
     */
    public static LogstashSocketAppender getInstance() {
        return REF.get();
    }

    /**
     * Initializes a new {@link LogstashSocketAppender}.
     */
    public LogstashSocketAppender() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.AppenderBase#start()
     */
    public void start() {
        REF.set(this);

        if (isStarted()) {
            return;
        }
        int errorCount = 0;
        if (port <= 0) {
            errorCount++;
            logWarn("No port was configured for appender" + name + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_port");
        }

        if (remoteHost == null) {
            errorCount++;
            logWarn("No remote host was configured for appender" + name + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_host");
        }

        if (queueSize <= 0) {
            logWarn("'queueSize' is not defined in configuration file. Falling back to default value of '2048'");
            queueSize = 2048;
        }

        if (acceptConnectionTimeout <= 0) {
            logWarn("'acceptConnectionTimeout' is not defined in configuration file. Falling back to default value of '5000'");
            acceptConnectionTimeout = 5000;
        }

        setOptionalProperties();

        if (errorCount == 0) {
            try {
                address = InetAddress.getByName(remoteHost);
            } catch (UnknownHostException ex) {
                logError("unknown host: " + remoteHost);
                errorCount++;
            }
        }

        if (errorCount == 0) {
            queue = newBlockingQueue(queueSize);
            peerId = remoteHost + ":" + port;
            task = getContext().getExecutorService().submit(this);
            super.start();
        }
    }

    /**
     * Set the optional properties:
     * <ul>
     * <li>com.openexchange.logback.extensions.logstash.alwaysPersistEvents</li>
     * <li>com.openexchange.logback.extensions.logstash.socketTimeout</li>
     * <li>com.openexchange.logback.extensions.logstash.loadFactor</li>
     * </ul>
     */
    private void setOptionalProperties() {
        alwaysPersistEvents = Boolean.parseBoolean(context.getProperty("com.openexchange.logback.extensions.logstash.alwaysPersistEvents"));
        {
            String value = context.getProperty("com.openexchange.logback.extensions.logstash.loadFactor");
            if (value == null || !(value instanceof String)) {
                logWarn("'com.openexchange.logback.extensions.logstash.loadFactor' property is not defined in the configuration file. Falling back to default value of '" + LOAD_FACTOR + "'");
                loadFactor = LOAD_FACTOR;
            } else {
                try {
                    loadFactor = Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    logWarn("The value of 'com.openexchange.logback.extensions.logstash.loadFactor' is not a parsable float. Falling back to default value of '" + LOAD_FACTOR + "'");
                    loadFactor = LOAD_FACTOR;
                }
            }
            loadThreshold = (int) (loadFactor * queueSize);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.AppenderBase#stop()
     */
    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }
        CloseUtil.closeQuietly(socket);
        task.cancel(true);
        if (connectorTask != null) {
            connectorTask.cancel(true);
        }
        REF.set(null);
        super.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.net.SocketConnector.ExceptionHandler#connectionFailed(ch.qos.logback.core.net.SocketConnector,
     * java.lang.Exception)
     */
    @Override
    public void connectionFailed(SocketConnector connector, Exception ex) {
        handleConnectionException(ex);
    }

    /**
     * Handle connection exceptions
     * 
     * @param ex The exception to handle
     */
    private void handleConnectionException(Exception ex) {
        if (ex instanceof InterruptedException) {
            logError("Connection to " + peerId + " interrupted.", ex);
        } else if (ex instanceof ConnectException) {
            logError("Connection to " + peerId + " refused.", ex);
        } else if (ex instanceof IOException) {
            logError("Connection to " + peerId + " failed.", ex);
        } else if (ex instanceof InterruptedException) {
            logError("Connection to " + peerId + " interupted.", ex);
        } else {
            logError("Connection error to " + peerId + ".", ex);
        }

        try {
            cleanQueueIfNecessary();
        } catch (IOException e) {
            logError("Failed while cleaning queue.", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public final void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SocketConnector connector = createConnector(address, port, 0, reconnectionDelay);

                    connectorTask = activateConnector(connector);
                    if (connectorTask == null) {
                        continue;
                    }

                    socket = waitForConnectorToReturnASocket();
                    if (socket == null) {
                        continue;
                    }

                    dispatchEvents();
                } catch (Exception e) {
                    handleConnectionException(e);
                }
            }
        } catch (Throwable t) {
            logError("LogstashSocketAppender is shutting down.", t);
        }
    }

    /**
     * Dispatch the events to the remote host
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    private void dispatchEvents() throws InterruptedException, IOException {
        try {
            socket.setSoTimeout(acceptConnectionTimeout);
            OutputStream oos = new BufferedOutputStream(socket.getOutputStream());
            encoder.init(oos);
            logInfo("Dispatching events...");
            while (true) {
                ILoggingEvent event = queue.take();
                encoder.doEncode(event);
                oos.flush();
            }
        } catch (IOException ex) {
            handleConnectionException(ex);
        } catch (InterruptedException ex) {
            handleConnectionException(ex);
        } finally {
            CloseUtil.closeQuietly(socket);
            socket = null;
            logError("Connection to " + peerId + " closed.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.AppenderBase#append(java.lang.Object)
     */
    @Override
    protected void append(ILoggingEvent event) {
        if (event == null || !isStarted()) {
            return;
        }
        try {
            final boolean inserted = queue.offer(event, eventDelayLimit.getMilliseconds(), TimeUnit.MILLISECONDS);
            if (!inserted) {
                logError("Dropping event due to timeout limit of [" + eventDelayLimit + "] being exceeded");
            }
        } catch (InterruptedException e) {
            logError("Interrupted while appending event to SocketAppender", e);
        }
    }

    /**
     * Creates a new {@link SocketConnector}.
     * <p>
     * The default implementation creates an instance of {@link DefaultSocketConnector}. A subclass may override to provide a different {@link SocketConnector} implementation.
     * 
     * @param address target remote address
     * @param port target remote port
     * @param initialDelay delay before the first connection attempt
     * @param retryDelay delay before a reconnection attempt
     * @return socket connector
     */
    protected SocketConnector newConnector(InetAddress address, int port, int initialDelay, int retryDelay) {
        return new DefaultSocketConnector(address, port, initialDelay, retryDelay);
    }

    /**
     * Gets the default {@link SocketFactory} for the platform.
     * <p>
     * Subclasses may override to provide a custom socket factory.
     */
    protected SocketFactory getSocketFactory() {
        return SocketFactory.getDefault();
    }

    /**
     * Creates a blocking queue that will be used to hold logging events until they can be delivered to the remote receiver.
     * <p>
     * The default implementation creates a (bounded) {@link ArrayBlockingQueue} for positive queue sizes. Otherwise it creates a {@link SynchronousQueue}.
     * <p>
     * This method is exposed primarily to support instrumentation for unit testing.
     * 
     * @param queueSize size of the queue
     * @return
     */
    BlockingQueue<ILoggingEvent> newBlockingQueue(int queueSize) {
        return queueSize <= 0 ? new SynchronousQueue<ILoggingEvent>() : new ArrayBlockingQueue<ILoggingEvent>(queueSize);
    }

    /**
     * Create a {@link SocketConnector}
     * 
     * @param address The address of the host
     * @param port The port
     * @param initialDelay The initial delay
     * @param retryDelay The retry delay
     * @return The {@link SocketConnector}
     */
    private SocketConnector createConnector(InetAddress address, int port, int initialDelay, int retryDelay) {
        logInfo("Creating socket connector for " + peerId + " ...");
        SocketConnector connector = newConnector(address, port, initialDelay, retryDelay);
        connector.setExceptionHandler(this);
        connector.setSocketFactory(getSocketFactory());
        logInfo("Socket connector for " + peerId + " created.");
        return connector;
    }

    /**
     * Activate the {@SocketConnector} by submitting it to the executor service. If successful, a connector task will be returned.
     * 
     * @param connector The SocketConnector
     * @return A connector task
     * @throws OXException
     */
    private Future<Socket> activateConnector(SocketConnector connector) throws OXException {
        try {
            logInfo("Submitting socket connector to the executor service...");
            Future<Socket> task = getContext().getExecutorService().submit(connector);
            logInfo("Connector task returned.");
            return task;
        } catch (RejectedExecutionException e) {
            throw LogstashSocketAppenderExceptionCodes.ERROR_ACTIVATING_CONNECTOR.create(e.getMessage(), e);
        }
    }

    /**
     * Get a socket from the connector task.
     * 
     * @return A socket
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private Socket waitForConnectorToReturnASocket() throws InterruptedException, ExecutionException {
        logInfo("Trying to connect to " + peerId + "...");
        Socket s = connectorTask.get();
        logInfo("Connection established to " + peerId + ".");
        connectorTask = null;
        return s;
    }

    /**
     * Clean up queue and log if necessary
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    private void cleanQueueIfNecessary() throws IOException {
        final int qSize = queue.size();
        final String message = "Event queue holds " + qSize + " events.";
        if (qSize > loadThreshold) {
            if (alwaysPersistEvents) {
                logInfo(message + " Load threshold of " + loadThreshold + " is reached. Flushing...");
                flushQueue(qSize);
            } else {
                queue.clear();
                logInfo("Event queue is empty.");
            }
        } else {
            logInfo(message + " Not flushing yet. Load threshold of " + loadThreshold + " is not reached.");
        }
    }

    /**
     * Flush queue
     * 
     * @param The amount of elements to flush from the queue
     * @throws IOException
     */
    private void flushQueue(final int qSize) throws IOException {
        // Use the LogstashEncoder to write to a different output stream
        LogstashEncoder enc = new LogstashEncoder();
        enc.init(System.err);
        ILoggingEvent event = null;
        int events = 0;
        while (events < qSize) {
            event = queue.poll();
            enc.doEncode(event);
            events++;
        }
        logInfo("Successfully flushed " + events + " out of " + qSize + " events.");
    }

    /**
     * Write the current timestamp by using the {@link LogstashFormatter.LOGSTASH_TIMEFORMAT}
     * 
     * @return A formatted timestamp
     */
    private String writeCurrentTimestamp() {
        return LogstashFormatter.LOGSTASH_TIMEFORMAT.format(System.currentTimeMillis());
    }

    /**
     * The <b>RemoteHost</b> property takes the name of of the host where a corresponding server is running.
     */
    public void setRemoteHost(String host) {
        remoteHost = host;
    }

    /**
     * Returns value of the <b>RemoteHost</b> property.
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * The <b>Port</b> property takes a positive integer representing the port where the server is waiting for connections.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns value of the <b>Port</b> property.
     */
    public int getPort() {
        return port;
    }

    /**
     * The <b>reconnectionDelay</b> property takes a positive {@link Duration} value representing the time to wait between each failed
     * connection attempt to the server. The default value of this option is to 30 seconds.
     * <p>
     * Setting this option to zero turns off reconnection capability.
     */
    public void setReconnectionDelay(int delay) {
        this.reconnectionDelay = delay;
    }

    /**
     * Returns value of the <b>reconnectionDelay</b> property.
     */
    public int getReconnectionDelay() {
        return reconnectionDelay;
    }

    /**
     * The <b>eventDelayLimit</b> takes a non-negative integer representing the number of milliseconds to allow the appender to block if the
     * underlying BlockingQueue is full. Once this limit is reached, the event is dropped.
     * 
     * @param eventDelayLimit the event delay limit
     */
    public void setEventDelayLimit(Duration eventDelayLimit) {
        this.eventDelayLimit = eventDelayLimit;
    }

    /**
     * Returns the value of the <b>eventDelayLimit</b> property.
     */
    public Duration getEventDelayLimit() {
        return eventDelayLimit;
    }

    /**
     * Sets the timeout that controls how long we'll wait for the remote peer to accept our connection attempt.
     * <p>
     * This property is configurable primarily to support instrumentation for unit testing.
     * 
     * @param acceptConnectionTimeout timeout value in milliseconds
     */
    void setAcceptConnectionTimeout(int acceptConnectionTimeout) {
        this.acceptConnectionTimeout = acceptConnectionTimeout;
    }

    /**
     * Returns the number of elements currently in the blocking queue.
     *
     * @return number of elements currently in the queue.
     */
    public int getNumberOfElementsInQueue() {
        return queue.size();
    }

    /**
     * Returns the value of the <b>queueSize</b> property.
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * Sets the maximum number of entries in the queue. Once the queue is full additional entries will be dropped if in the time given by
     * the <b>eventDelayLimit</b> no space becomes available.
     *
     * @param queueSize the maximum number of entries in the queue
     */
    public void setQueueSize(int queueSize) {
        if (queue != null) {
            throw new IllegalStateException("Queue size must be set before initialization");
        }
        this.queueSize = queueSize;
    }

    /**
     * Gets the encoder
     *
     * @return The encoder
     */
    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    /**
     * Sets the encoder
     *
     * @param encoder The encoder to set
     */
    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    /**
     * Log INFO helper
     * 
     * @param message The message
     * @param t The exception(s)
     */
    private void logInfo(String message, Throwable... t) {
        log(Level.INFO, message, t);
    }

    /**
     * Log WARN helper
     * 
     * @param message The message
     * @param t The exception(s)
     */
    private void logWarn(String message, Throwable... t) {
        log(Level.WARN, message, t);
    }

    /**
     * Log ERROR helper
     * 
     * @param message The message
     * @param t The exception(s)
     */
    private void logError(String message, Throwable... t) {
        log(Level.ERROR, message, t);
    }

    /**
     * Log the specified message and the specified exception(s) stacktrace with the specified level to System.err
     * 
     * @param level The logging level
     * @param message The message
     * @param t The exception(s)
     */
    private void log(Level level, String message, Throwable... t) {
        StringBuilder builder = new StringBuilder();
        builder.append(writeCurrentTimestamp()).append(" ").append(level).append(" in ").append(this.getClass().getCanonicalName()).append(" - ").append(message);
        if (t.length >= 1) {
            builder.append(" Reason:");
        }
        System.err.println(builder.toString());
        for (Throwable th : t) {
            th.printStackTrace();
        }
    }

    @Override
    public int getEventsInQueue() {
        return queue.size();
    }
}
