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

package com.openexchange.service.messaging.internal.receipt;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.service.messaging.MessagingServiceExceptionCode;
import com.openexchange.service.messaging.internal.Constants;
import com.openexchange.service.messaging.internal.MessageHandlerTracker;
import com.openexchange.service.messaging.internal.MessagingConfig;

/**
 * {@link MessagingServer} - The messaging server.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingServer implements Runnable {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessagingServer.class));

    private static final int BUFFER_LENGTH = Constants.PACKAGE_LENGTH;

    /**
     * The server socket to accept incoming packages.
     */
    private DatagramSocket serverSocket;

    /**
     * The server threads waiting on accept().
     */
    private Thread[] threadArr;

    /**
     * The socket handler processing accepted sockets.
     */
    private final MessagingDatagramHandler socketHandler;

    /**
     * The running flag.
     */
    private final AtomicBoolean running;

    /**
     * The listener port provided by configuration.
     */
    private int listenerPort;

    /**
     * Initializes a new {@link MessagingServer}.
     */
    public MessagingServer(final MessageHandlerTracker handlers) {
        super();
        running = new AtomicBoolean();
        socketHandler = new MessagingDatagramHandler(handlers);
    }

    /**
     * Starts the messaging server parameterized with given configuration.
     *
     * @param config The configuration
     * @throws OXException If start-up fails
     */
    public void startServer(final MessagingConfig config) throws OXException {
        if (running.compareAndSet(false, true)) {
            listenerPort = config.getListenerPort();
            try {
                final InetAddress bindAddress = config.getBindAddress();
                if (null == bindAddress) {
                    serverSocket = new DatagramSocket(listenerPort);
                } else {
                    serverSocket = new DatagramSocket(listenerPort, bindAddress);
                }
                serverSocket.setReceiveBufferSize(Constants.PACKAGE_LENGTH);
            } catch (final IOException e) {
                throw MessagingServiceExceptionCode.BIND_ERROR.create(Integer.valueOf(listenerPort));
            }
            /*
             * Initialize server threads
             */
            {
                final int serverThreads = config.getNumberOfServerThreads();
                threadArr = new Thread[serverThreads <= 0 ? 1 : serverThreads];
            }
            if (threadArr.length > 0) {
                final DecimalFormat DF = new DecimalFormat("0000");
                final CountDownLatch startGate = new CountDownLatch(1);
                final StringBuilder sb = new StringBuilder(32);
                for (int i = 0; i < threadArr.length; i++) {
                    threadArr[i] = new Thread(new GateRunnable(startGate, this, LOG));
                    sb.setLength(0);
                    threadArr[i].setName(sb.append("MessaginServer-").append(DF.format((i + 1))).toString());
                    threadArr[i].setPriority(Thread.MAX_PRIORITY);
                    threadArr[i].start();
                }
                /*
                 * Open gate to start-up all server threads at the same time
                 */
                startGate.countDown();
            }
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Messaging server is already running...");
            }
        }
    }

    /**
     * Stops this messaging server.
     */
    public void stopServer() {
        if (running.compareAndSet(true, false)) {
            /*
             * Stop tasks
             */
            socketHandler.shutDownNow();
            /*
             * Interrupt & destroy threads
             */
            final StringBuilder sb = new StringBuilder(128);
            for (int i = 0; i < threadArr.length; i++) {
                try {
                    threadArr[i].interrupt();
                } catch (final Exception e) {
                    LOG.error(sb.append(threadArr[i].getName()).append(" could NOT be interrupted").toString(), e);
                    sb.setLength(0);
                } finally {
                    threadArr[i] = null;
                }
            }
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (final Exception e) {
                    LOG.error(
                        sb.append("Messaging server socket bound to port ").append(listenerPort).append(" cannot be closed").toString(),
                        e);
                    sb.setLength(0);
                }
                serverSocket = null;
            }
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Messaging server is not running and thus does not need to be stopped");
            }
        }
    }

    /**
     * Checks if this AJP server instance is running
     *
     * @return <code>true</code> if this AJP server instance is running; otherwise <code>false</code>
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Gets the datagram socket
     *
     * @return The datagram
     */
    public DatagramSocket getServerSocket() {
        return serverSocket;
    }

    @Override
    public void run() {
        boolean keepOnRunning = true;
        while (keepOnRunning && running.get()) {
            final DatagramPacket dgram = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
            try {
                /*
                 * Blocks until a datagram is received
                 */
                serverSocket.receive(dgram);
                if (Thread.currentThread().isInterrupted()) {
                    keepOnRunning = false;
                } else {
                    if (dgram.getLength() > 0) {
                        socketHandler.handle(dgram);
                    } else {
                        LOG.warn(new StringBuilder(64).append("Received empty UDP package from ").append(dgram.getAddress()).append(':').append(
                            dgram.getPort()).toString());
                    }
                }
            } catch (final SocketException e) {
                if (running.get()) {
                    LOG.error(e.getMessage(), e);
                }
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private static final class GateRunnable implements Runnable {

        private final transient org.apache.commons.logging.Log logger;

        private final Runnable task;

        private final CountDownLatch latch;

        public GateRunnable(final CountDownLatch latch, final Runnable task, final org.apache.commons.logging.Log logger) {
            super();
            this.task = task;
            this.latch = latch;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                latch.await();
                task.run();
            } catch (final InterruptedException e) {
                logger.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
