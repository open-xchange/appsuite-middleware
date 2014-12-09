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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.concurrent.TimeoutException;
import javax.net.SocketFactory;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.net.AbstractSocketAppender;
import ch.qos.logback.core.net.DefaultSocketConnector;
import ch.qos.logback.core.net.SocketConnector;
import ch.qos.logback.core.net.SocketConnector.ExceptionHandler;
import ch.qos.logback.core.util.CloseUtil;
import ch.qos.logback.core.util.Duration;

/**
 * {@link LogstashSocketAppender}. Replica class of {@link AbstractSocketAppender}. The method {@link #dispatchEvents()} does the actual
 * work and is rewritten. Instead of sending serialized objects to the stream, a special {@link LogstashEncoder} is used to format all
 * logging events as JSON objects before sending them over the wire.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LogstashSocketAppender extends AppenderBase<ILoggingEvent> implements Runnable, ExceptionHandler {

    private static final int SOCKET_TIMEOUT = 60;
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
    private int socketTimeout;
    private int reconnectionDelay;
    private Duration eventDelayLimit;
    private int acceptConnectionTimeout;

    private Encoder<ILoggingEvent> encoder;

    private Boolean alwaysPersistEvents;

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.AppenderBase#start()
     */
    public void start() {
        if (isStarted()) {
            return;
        }
        int errorCount = 0;
        if (port <= 0) {
            errorCount++;
            addError("No port was configured for appender" + name + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_port");
        }

        if (remoteHost == null) {
            errorCount++;
            addError("No remote host was configured for appender" + name + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_host");
        }

        if (queueSize <= 0) {
            addWarn("'queueSize' is not defined in configuration file. Falling back to default value of '2048'");
            queueSize = 2048;
        }

        setOptionalProperties();

        if (errorCount == 0) {
            try {
                address = InetAddress.getByName(remoteHost);
            } catch (UnknownHostException ex) {
                addError("unknown host: " + remoteHost);
                errorCount++;
            }
        }

        if (errorCount == 0) {
            queue = newBlockingQueue(queueSize);
            peerId = "remote peer " + remoteHost + ":" + port + ": ";
            task = getContext().getExecutorService().submit(this);
            super.start();
        }
    }

    private void setOptionalProperties() {
        alwaysPersistEvents = Boolean.parseBoolean(context.getProperty("com.openexchange.logback.extensions.logstash.alwaysPersistEvents"));
        {
            String value = context.getProperty("com.openexchange.logback.extensions.logstash.socketTimeout");
            if (value == null || !(value instanceof String)) {
                addWarn("'com.openexchange.logback.extensions.logstash.socketTimeout' property is not defined in the configuration file. Falling back to default value of '" + SOCKET_TIMEOUT + "'");
                socketTimeout = SOCKET_TIMEOUT;
            } else {
                try {
                    socketTimeout = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    addError("The value of 'com.openexchange.logback.extensions.logstash.socketTimeout' is not a parsable int. Falling back to default value of '" + SOCKET_TIMEOUT + "'");
                    socketTimeout = SOCKET_TIMEOUT;
                }
            }
        }
        {
            String value = context.getProperty("com.openexchange.logback.extensions.logstash.loadFactor");
            if (value == null || !(value instanceof String)) {
                addWarn("'com.openexchange.logback.extensions.logstash.loadFactor' property is not defined in the configuration file. Falling back to default value of '" + LOAD_FACTOR + "'");
                loadFactor = LOAD_FACTOR;
            } else {
                try {
                    loadFactor = Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    addError("The value of 'com.openexchange.logback.extensions.logstash.loadFactor' is not a parsable float. Falling back to default value of '" + LOAD_FACTOR + "'");
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
        if (ex instanceof InterruptedException) {
            addInfo("connector interrupted");
        } else if (ex instanceof ConnectException) {
            addInfo(peerId + "connection refused");
        } else {
            addInfo(peerId + ex);
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
                    e.printStackTrace();
                }
            }
        } catch (Throwable t) {
            System.err.println("LogstashSocketAppender is shutting down. Unexpected error:");
            t.printStackTrace();
        }
    }

    /**
     * Dispatch the events to the remote host
     * 
     * @throws InterruptedException
     */
    private void dispatchEvents() throws InterruptedException {
        try {
            socket.setSoTimeout(acceptConnectionTimeout);
            OutputStream oos = new BufferedOutputStream(socket.getOutputStream());
            encoder.init(oos);
            socket.setSoTimeout(0);
            addInfo(peerId + "connection established");
            int counter = 0;
            while (true) {
                ILoggingEvent event = queue.take();
                encoder.doEncode(event);
                oos.flush();
                if (++counter >= CoreConstants.OOS_RESET_FREQUENCY) {
                    // Failing to reset the object output stream every now and
                    // then creates a serious memory leak.
                    oos.flush();
                    counter = 0;
                }
            }
        } catch (IOException ex) {
            addInfo(peerId + "connection failed: " + ex);
        } finally {
            CloseUtil.closeQuietly(socket);
            socket = null;
            addInfo(peerId + "connection closed");
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
                addInfo("Dropping event due to timeout limit of [" + eventDelayLimit + "] being exceeded");
            }
        } catch (InterruptedException e) {
            addError("Interrupted while appending event to SocketAppender", e);
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

    private SocketConnector createConnector(InetAddress address, int port, int initialDelay, int retryDelay) {
        SocketConnector connector = newConnector(address, port, initialDelay, retryDelay);
        connector.setExceptionHandler(this);
        connector.setSocketFactory(getSocketFactory());
        return connector;
    }

    private Future<Socket> activateConnector(SocketConnector connector) throws RejectedExecutionException, InterruptedException, IOException {
        try {
            return getContext().getExecutorService().submit(connector);
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
            cleanQueueIfNecessary();
            System.err.println(LogstashSocketAppenderExceptionCodes.ERROR_ACTIVATING_CONNECTOR.create(e.getMessage(), e).toString());
            return null;
        }
    }

    private Socket waitForConnectorToReturnASocket() throws InterruptedException, ExecutionException, IOException {
        try {
            Socket s = connectorTask.get(socketTimeout, TimeUnit.SECONDS);
            connectorTask = null;
            return s;
        } catch (TimeoutException e) {
            e.printStackTrace();
            cleanQueueIfNecessary();
            connectorTask = null;
            System.err.println(LogstashSocketAppenderExceptionCodes.TIMEOUT_WHILE_CREATING_SOCKET.create(e).toString());
            return null;
        }
    }

    /**
     * Clean up queue and log if necessary
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    private void cleanQueueIfNecessary() throws InterruptedException, IOException {
        final int qSize = queue.size();
        System.err.print("Event queue holds " + qSize + " events.");
        if (qSize > loadThreshold) {
            if (alwaysPersistEvents) {
                // Use the LogstashEncoder to write to a different output stream
                LogstashEncoder enc = new LogstashEncoder();
                enc.init(System.err);
                ILoggingEvent event = null;
                System.err.println(" Load threshold of " + loadThreshold + " is reached. Flushing...");
                int events = 0;
                while (events < qSize) {
                    event = queue.poll();
                    enc.doEncode(event);
                    events++;
                }
                System.err.println("Successfully flushed " + events + " out of " + qSize + " events.");
            } else {
                queue.clear();
                System.err.println(" Event queue is empty.");
            }
        } else {
            System.err.println(" Not flushing yet. Load threshold of " + loadThreshold + " is not reached.");
        }
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
}
