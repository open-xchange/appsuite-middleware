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

package com.openexchange.ajp13;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.ajp13.AJPv13Exception.AJPCode;
import com.openexchange.ajp13.monitoring.AJPv13ListenerMonitor;
import com.openexchange.ajp13.monitoring.AJPv13ServerThreadsMonitor;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.tools.servlet.ServletConfigLoader;

/**
 * {@link AJPv13Server} - The AJP server which accepts incoming socket
 * connections and delegates its processing to a dedicated AJP listener
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJPv13Server implements Runnable {

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13Server.class);

	/**
	 * <p>
	 * The value 0 used in constructor
	 * <code>ServerSocket(int port, int backlog, InetAddress bindAddr)</code>
	 * causes to fall back to default value for backlog
	 * <p>
	 * The <code>backlog</code> argument must be a positive value greater than
	 * 0. If the value passed if equal or less than 0, then the default value
	 * will be assumed
	 */
	private static final int DEFAULT_BACKLOG = 0;

	public static final int AJP13_PORT = AJPv13Config.getAJPPort();

	// member fields
	private ServerSocket serverSocket;

	private Thread[] threadArr;

	private final AtomicBoolean running = new AtomicBoolean();

	private static AJPv13Server instance;

	private static final DecimalFormat DF = new DecimalFormat("0000");

	public static final AJPv13ServerThreadsMonitor ajpv13ServerThreadsMonitor = new AJPv13ServerThreadsMonitor();

	public static final AJPv13ListenerMonitor ajpv13ListenerMonitor = new AJPv13ListenerMonitor();

	public static void startAJPServer() throws AJPv13Exception {
		synchronized (AJPv13Server.class) {
			if (instance == null) {
				instance = new AJPv13Server();
			}
		}
		instance.startServer();
	}

	public static void restartAJPServer() throws AJPv13Exception {
		stopAJPServer();
		startAJPServer();
	}

	public static void stopAJPServer() {
		if (instance == null) {
			return;
		}
		instance.stopServer();
	}

	private AJPv13Server() {
		super();
	}

	private void startServer() throws AJPv13Exception {
		if (running.compareAndSet(false, true)) {
			try {
				serverSocket = new ServerSocket(AJP13_PORT, DEFAULT_BACKLOG, AJPv13Config.getAJPBindAddress());
			} catch (final IOException ex) {
				throw new AJPv13Exception(AJPCode.STARTUP_ERROR, false, ex, Integer.valueOf(AJP13_PORT));
			}
			ServletConfigLoader.initDefaultInstance(AJPv13Config.getServletConfigs());
			initializePools();
			AJPv13Watcher.initializeAJPv13Watcher();
			initializeThreadArray();
			for (int i = 0; i < threadArr.length; i++) {
				threadArr[i].setPriority(Thread.MAX_PRIORITY);
				threadArr[i].start();
			}
			ajpv13ServerThreadsMonitor.setNumActive(threadArr.length);
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("AJPv13Server is already running...");
			}
		}
	}

	private void stopServer() {
		if (running.compareAndSet(true, false)) {
			/*
			 * Stop listeners
			 */
			AJPv13Watcher.stopListeners();
			/*
			 * Reset watcher
			 */
			AJPv13Watcher.resetAJPv13Watcher();
			/*
			 * Reset pools
			 */
			resetPools();
			/*
			 * Reset default servlet config loader
			 */
			ServletConfigLoader.resetDefaultInstance();
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
				} catch (final IOException e) {
					LOG.error(sb.append("AJP server socket bound to port ").append(AJP13_PORT).append(
							" cannot be closed").toString(), e);
					sb.setLength(0);
				}
				serverSocket = null;
			}
			ajpv13ServerThreadsMonitor.setNumActive(0);
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("AJPv13Server is not running and thus does not need to be stopped");
			}
		}
	}

	private final void initializePools() {
		resetPools();
		AJPv13ListenerPool.initPool();
		if (AJPv13Config.useAJPConnectionPool()) {
			AJPv13ConnectionPool.initConnectionPool();
		}
		if (AJPv13Config.useAJPRequestHandlerPool()) {
			AJPv13RequestHandlerPool.initPool();
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("All pools initialized...");
		}
	}

	private final void initializeThreadArray() {
		threadArr = new Thread[AJPv13Config.getAJPServerThreadSize()];
		final StringBuilder sb = new StringBuilder(32);
		for (int i = 0; i < threadArr.length; i++) {
			threadArr[i] = new Thread(this);
			sb.setLength(0);
			threadArr[i].setName(sb.append("AJPServer-").append(DF.format((i + 1))).toString());
		}
	}

	private final void resetPools() {
		if (running.get()) {
			AJPv13ListenerPool.resetPool();
			if (AJPv13Config.useAJPConnectionPool()) {
				AJPv13ConnectionPool.resetConnectionPool();
			}
			if (AJPv13Config.useAJPRequestHandlerPool()) {
				AJPv13RequestHandlerPool.resetPool();
			}
		}
	}

	public final boolean isRunning() {
		return running.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		boolean keepOnRunning = true;
		AcceptSocket: while (keepOnRunning && running.get()) {
			Socket client;
			try {
				client = serverSocket.accept();
				if (Thread.currentThread().isInterrupted()) {
					break AcceptSocket;
				}
				final long start = System.currentTimeMillis();
				client.setTcpNoDelay(true);
				incrementNumberOfOpenAJPSockets();
				AJPv13Listener l = AJPv13ListenerPool.getListener();
				while (!l.startListener(client)) {
					/*
					 * Not possible to start current listener, get next one from
					 * pool and let the current one die...
					 */
					l = AJPv13ListenerPool.getListener();
				}
				final long useTime = System.currentTimeMillis() - start;
				ajpv13ServerThreadsMonitor.addUseTime(useTime);
			} catch (final java.net.SocketException e) {
				/*
				 * Socket closed while being blocked in accept
				 */
				LOG.info("AJPv13Server down");
				keepOnRunning = false;
			} catch (final IOException ex) {
				LOG.error(ex.getMessage(), ex);
				keepOnRunning = false;
			}
		}
	}

	public static void main(final String args[]) {
		try {
			new AJPv13Server().startServer();
		} catch (final AJPv13Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public static int getNumberOfOpenAJPSockets() {
		return MonitoringInfo.getNumberOfConnections(MonitoringInfo.AJP_SOCKET);
	}

	private static void incrementNumberOfOpenAJPSockets() {
		MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.AJP_SOCKET);
	}

	public static void decrementNumberOfOpenAJPSockets() {
		MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.AJP_SOCKET);
	}

}
