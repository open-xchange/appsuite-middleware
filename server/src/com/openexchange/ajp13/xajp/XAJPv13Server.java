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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajp13.xajp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xsocket.DataConverter;
import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.INonBlockingConnection;
import org.xsocket.connection.IServer;
import org.xsocket.connection.IServerListener;
import org.xsocket.connection.Server;
import org.xsocket.connection.IConnection.FlushMode;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.xajp.executor.XAJPv13ThreadPoolExecutor;
import com.openexchange.tools.servlet.ServletConfigLoader;

/**
 * {@link XAJPv13Server} - The AJP server based <a href="http://xsocket.sourceforge.net/">xSocket</a> library. See tutorial <a
 * href="http://xsocket.sourceforge.net/core/tutorial/V2/TutorialCore.htm">here</a>
 * <p>
 * Following properties are known:<br>
 * <table style="text-align: center;" border="1" cellpadding="2" cellspacing="0">
 * <tbody>
 * <tr align="center">
 * <th style="text-align: left;">system property</th> <th style="text-align: left;">type</th> <th style="text-align: left;">description</th>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top">org.xsocket.connection.server.workerpoolSize</td>
 * <td style="text-align: left;">int</td>
 * <td style="text-align: left;">the size of the default (FixedThread) workerpool. This workerpool will be used by the server if no custom
 * workerpool is set. Default is 40.</td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top"></td>
 * <td style="text-align: left;"></td>
 * <td style="text-align: left;"></td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top">org.xsocket.connection.sendFlushTimeoutMillis</td>
 * <td style="text-align: left;">int</td>
 * <td style="text-align: left;">The flush (write) timeout. This timeout will be ignored by using FlushMode.ASYNC. Default is 60000 millis.</td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top"></td>
 * <td style="text-align: left;"></td>
 * <td style="text-align: left;"></td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top">org.xsocket.connection.dispatcher.initialCount</td>
 * <td style="text-align: left;">int</td>
 * <td style="text-align: left;">amount of dispatcher (NIO selectors) which will be used. By default 'number cpu + 1' dispatchers are used.</td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top">org.xsocket.connection.dispatcher.maxHandles</td>
 * <td style="text-align: left;">int</td>
 * <td style="text-align: left;">the maximum number of channels which will be attached to a dispatcher instance. If necessary, an additional
 * dispatcher will be started automatically by xSocket. By default the number of channels is unlimited.</td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top">org.xsocket.connection.dispatcher.detachHandleOnNoOps</td>
 * <td style="text-align: left;">boolean</td>
 * <td style="text-align: left;">By setting true the channel will be detached, if no (NIO SelectionKey) operation is set. The channel will
 * automatically reattached. Default is false.</td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top"></td>
 * <td style="text-align: left;"></td>
 * <td style="text-align: left;"></td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top">org.xsocket.connection.client.readbuffer.defaultMaxReadBufferThreshold
 * <p>
 * org.xsocket.connection.server.readbuffer.defaultMaxReadBufferThreshold
 * </p>
 * </td>
 * <td style="text-align: left;">int</td>
 * <td style="text-align: left;">Sets the default maxReadBuffer threshold. The default value of this property is unlimited.</td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top">org.xsocket.connection.client.readbuffer.usedirect
 * <p>
 * org.xsocket.connection.server.readbuffer.usedirect
 * </p>
 * </td>
 * <td style="text-align: left;">boolean</td>
 * <td style="text-align: left;">By setting true direct allocated buffer will be used to read incoming socket data. Default is false.</td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top">org.xsocket.connection.client.readbuffer.preallocation.size
 * <p>
 * org.xsocket.connection.server.readbuffer.preallocation.size
 * </p>
 * </td>
 * <td style="text-align: left;">int</td>
 * <td style="text-align: left;">The preallocation size in bytes. Preallocated buffer will be used to read the incoming socket data. Unused
 * preallocated buffer of the read operation will be recycled. Default is 65536.</td>
 * </tr>
 * <tr>
 * <td style="text-align: left;" valign="top">org.xsocket.connection.client.readbuffer.preallocated.minSize
 * <p>
 * org.xsocket.connection.server.readbuffer.preallocated.minSize
 * </p>
 * </td>
 * <td style="text-align: left;">int</td>
 * <td style="text-align: left;">The minimal preallocated size in bytes. If the preallocated buffer size falls below the minSize, new buffer
 * will be preallocated. Default is 64.</td>
 * </tr>
 * </tbody>
 * </table>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class XAJPv13Server {

    private static volatile XAJPv13Server instance;

    /**
     * Gets the instance of {@link XAJPv13Server}.
     * 
     * @return The instance of {@link XAJPv13Server}.
     * @throws AJPv13Exception If server instance cannot be obtained
     */
    public static XAJPv13Server getInstance() throws AJPv13Exception {
        XAJPv13Server tmp = instance;
        if (null == tmp) {
            synchronized (XAJPv13Server.class) {
                tmp = instance;
                if (null == tmp) {
                    try {
                        tmp = instance = new XAJPv13Server();
                    } catch (final UnknownHostException e) {
                        throw new AJPv13Exception(AJPv13Exception.AJPCode.IO_ERROR, false, e, e.getMessage());
                    } catch (final IOException e) {
                        throw new AJPv13Exception(AJPv13Exception.AJPCode.IO_ERROR, false, e, e.getMessage());
                    }
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the instance of {@link XAJPv13Server}.n
     */
    public static void releaseInstance() {
        XAJPv13Server tmp = instance;
        if (null != tmp) {
            synchronized (XAJPv13Server.class) {
                tmp = instance;
                if (null != tmp) {
                    instance = null;
                }
            }
        }
    }

    private final IServer server;

    private XAJPv13Server() throws UnknownHostException, IOException {

        // //////////////////////
        // uncomment following code for using the first visit throttling filter
        // FirstVisitThrottlingFilter firstVisitFilter = new FirstVisitThrottlingFilter(5);
        // HandlerChain chain = new HandlerChain();
        // chain.addLast(firstVisitFilter);
        // chain.addLast(hdl);
        // hdl = chain;

        processSysProps();

        final InetAddress addr = AJPv13Config.getAJPBindAddress();
        if (null == addr) {
            server = new Server(AJPv13Config.getAJPPort(), new XAJPv13ProtocolHandler(false));
        } else {
            server = new Server(addr, AJPv13Config.getAJPPort(), new XAJPv13ProtocolHandler(false));
        }

        /*-
         * Setting the flush-mode to ASYNC can improve the performance. However, some strong restriction exits by using the ASYNC
         * flush-mode:
         * 
         * By setting the flush mode to ASYNC (default is SYNC) the data will be transferred to the underlying OS-internal socket send
         * buffer in an asynchronous way. By setting the flush mode to ASYNC the worker thread will not be synchronized with the
         * xSocket-internal I/O-Thread. Please take care by setting flush mode to ASYNC. If you access the buffer after writing it, race
         * conditions will occur.
         */
        // server.setFlushMode(FlushMode.ASYNC); // performance improvement
        // server.setWriteTransferRate(64);
        /*-
         * The worker pool is used to perform the handler's call back methods such as onData or onConnect. After reading the data from the
         * socket, the xSocket-internal Dispatcher starts a pooled worker thread to perform the proper call back method.
         */
        server.setWorkerpool(new XAJPv13ThreadPoolExecutor(10L, TimeUnit.SECONDS));
        // server.setWorkerpool(new XAJPv13ThreadPoolExecutor(40));

        /*
         * Activate xSocket logging (for namespace org.xsocket.connection)
         */
        final Logger logger = Logger.getLogger("org.xsocket.connection");
        logger.setLevel(Level.WARNING);

        final ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.WARNING);
        logger.addHandler(ch);

    }

    /**
     * Starts this AJP server instance.
     * 
     * @throws AJPv13Exception If start-up fails
     */
    public void start() throws AJPv13Exception {
        try {
            start(server, 60);
            // TODO: ConnectionUtils.registerMBean(server);
            ServletConfigLoader.initDefaultInstance(AJPv13Config.getServletConfigs());
        } catch (final SocketTimeoutException e) {
            throw new AJPv13Exception(AJPv13Exception.AJPCode.IO_ERROR, false, e, e.getMessage());
        }
    }

    /**
     * Gets the local address.
     * 
     * @return The local address
     */
    public InetAddress getLocalAddress() {
        return server.getLocalAddress();
    }

    /**
     * Closes this AJP server instance.
     * 
     * @throws AJPv13Exception If shut-down fails
     */
    public void close() throws AJPv13Exception {
        ServletConfigLoader.resetDefaultInstance();
        if (server != null) {
            try {
                server.close();
            } catch (final IOException e) {
                throw new AJPv13Exception(AJPv13Exception.AJPCode.IO_ERROR, false, e, e.getMessage());
            }
        }
    }

    private static final class FirstVisitThrottlingFilter implements IConnectHandler {

        private final Set<String> knownIps;

        private final int writeRate;

        public FirstVisitThrottlingFilter(final int writeRate) {
            super();
            knownIps = new HashSet<String>();
            this.writeRate = writeRate;
        }

        public boolean onConnect(final INonBlockingConnection connection) throws IOException {
            final String ipAddress = connection.getRemoteAddress().getHostAddress();
            if (!knownIps.contains(ipAddress)) {
                knownIps.add(ipAddress);
                connection.setFlushmode(FlushMode.ASYNC);
                connection.setWriteTransferRate(writeRate);
            }

            return false; // false -> successor element in handler chain will be called (true -> chain processing will be terminated)
        }
    }

    /**
     * Checks values for xSocket system properties:
     * <ul>
     * <li><tt>"org.xsocket.connection.server.readbuffer.usedirect"</tt></li>
     * <li><tt>"org.xsocket.connection.dispatcher.initialCount"</tt></li>
     * </ul>
     */
    private static void processSysProps() {
        final Properties sysprops = System.getProperties();
        sysprops.put("org.xsocket.connection.server.readbuffer.usedirect", "true");
        /*-
         * The Dispatcher (I/O thread) is responsible to perform the socket read & write I/O operations and to delegate the call back
         * handling. By default number of CPUs + 1 dispatchers will be created. A connection is bound to one dispatcher during the total
         * lifetime.
         */
        sysprops.put("org.xsocket.connection.dispatcher.initialCount", String.valueOf(Math.max(
            (Runtime.getRuntime().availableProcessors() + 1),
            /* AJPv13Config.getAJPServerThreadSize() */20)));
    }

    /**
     * Starts the given server within a dedicated thread. This method blocks until the server is open.
     * 
     * @param server The server to start
     * @param timeoutSec The maximum time to wait
     * @throws SocketTimeoutException If the timeout has been reached
     */
    private static void start(final IServer server, final int timeoutSec) throws SocketTimeoutException {

        final CountDownLatch startedSignal = new CountDownLatch(1);

        // create and add startup listener
        final IServerListener startupListener = new IServerListener() {

            public void onInit() {
                startedSignal.countDown();
            }

            public void onDestroy() {
                // Nothing to do
            }
        };
        server.addListener(startupListener);

        // start server within a dedicated thread
        final Thread t = new Thread(server);
        t.setName("xServer");
        t.start();

        // wait until server has been started (onInit has been called)
        boolean isStarted = false;
        try {
            isStarted = startedSignal.await(timeoutSec, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            throw new RuntimeException(new StringBuilder(128).append("Start signal did not occur. ").append(e.toString()).toString(), e);
        }

        // timeout occurred?
        if (!isStarted) {
            throw new SocketTimeoutException(new StringBuilder(128).append("Start timeout (").append(
                DataConverter.toFormatedDuration((long) timeoutSec * 1000)).append(')').toString());
        }

        // update thread name
        t.setName("AJPServer@" + server.getLocalPort());

        // remove the startup listener
        server.removeListener(startupListener);
    }
}
