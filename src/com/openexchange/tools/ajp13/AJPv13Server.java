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

package com.openexchange.tools.ajp13;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.openexchange.monitoring.MonitorAgent;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.tools.ajp13.AJPv13Exception.AJPCode;
import com.openexchange.tools.ajp13.monitoring.AJPv13ListenerMonitor;
import com.openexchange.tools.ajp13.monitoring.AJPv13ServerThreadsMonitor;
import com.openexchange.tools.servlet.ServletConfigLoader;
import com.openexchange.tools.servlet.ServletConfigWrapper;
import com.openexchange.tools.servlet.ServletContextWrapper;
import com.openexchange.tools.servlet.http.HttpServletManager;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
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

	private boolean running;

	public static final ServletConfigLoader SERVLET_CONFIGS = new ServletConfigLoader();

	private static AJPv13Server instance;

	private static final DecimalFormat DF = new DecimalFormat("0000");

	public static final AJPv13ServerThreadsMonitor ajpv13ServerThreadsMonitor;

	public static final AJPv13ListenerMonitor ajpv13ListenerMonitor;

	static {
		ajpv13ServerThreadsMonitor = new AJPv13ServerThreadsMonitor();
		ajpv13ListenerMonitor = new AJPv13ListenerMonitor();
		try {
			/*
			 * Register server threads monitor
			 */
			String[] sa = MonitorAgent.getDomainAndName(ajpv13ServerThreadsMonitor.getClass().getName(), true);
			MonitorAgent.registerMBeanGlobal(new ObjectName(sa[0], "name", sa[1]), ajpv13ServerThreadsMonitor);
			/*
			 * Register listener monitor
			 */
			sa = MonitorAgent.getDomainAndName(ajpv13ListenerMonitor.getClass().getName(), true);
			MonitorAgent.registerMBeanGlobal(new ObjectName(sa[0], "name", sa[1]), ajpv13ListenerMonitor);
		} catch (final MalformedObjectNameException e) {
			LOG.error(e.getMessage(), e);
		} catch (final NullPointerException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public static void startAJPServer() throws AJPv13Exception {
		/*
		 * Create Servlet Config and servlet-specific Servlet Context
		 */
		final ServletConfigWrapper servletConfig = new ServletConfigWrapper();
		final ServletContextWrapper servletContext = new ServletContextWrapper(servletConfig);
		servletConfig.setServletContextWrapper(servletContext);
		SERVLET_CONFIGS.setDefaultConfig(servletConfig);
		SERVLET_CONFIGS.setDefaultContext(servletContext);
		SERVLET_CONFIGS.setDirectory(new File(AJPv13Config.getServletConfigs()));
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

	private AJPv13Server() throws AJPv13Exception {
		super();
		try {
			serverSocket = new ServerSocket(AJP13_PORT, DEFAULT_BACKLOG, AJPv13Config.getAJPBindAddress());
		} catch (final IOException ex) {
			throw new AJPv13Exception(AJPCode.STARTUP_ERROR, false, ex, Integer.valueOf(AJP13_PORT));
		}
	}

	private void startServer() {
		if (running) {
			if (LOG.isInfoEnabled()) {
				LOG.info("AJPv13Server is already running...");
			}
			return;
		}
		initializePools();
		initializeThreadArray();
		for (int i = 0; i < threadArr.length; i++) {
			threadArr[i].setPriority(Thread.MAX_PRIORITY);
			threadArr[i].start();
		}
		ajpv13ServerThreadsMonitor.setNumActive(threadArr.length);
		running = true;
	}

	private void stopServer() {
		if (!running && LOG.isInfoEnabled()) {
			LOG.info("AJPv13Server is not running and thus does not need to be stopped");
		}
		/*
		 * Stop listeners
		 */
		AJPv13Watcher.stopListeners();
		/*
		 * Reset pools
		 */
		resetPools();
		/*
		 * Interrupt & destroy threads
		 */
		final StringBuilder sb = new StringBuilder(100);
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
		ajpv13ServerThreadsMonitor.setNumActive(0);
		running = false;
	}

	private final void initializePools() {
		resetPools();
		HttpServletManager.createServlets();
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
		for (int i = 0; i < threadArr.length; i++) {
			threadArr[i] = new Thread(this);
			threadArr[i].setName(new StringBuilder(25).append("AJPServer-").append(DF.format((i + 1))).toString());
		}
	}

	private final void resetPools() {
		if (running) {
			HttpServletManager.clearServletPool();
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
		return running;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		AcceptSocket: while (true) {
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
					l = null;
					l = AJPv13ListenerPool.getListener();
				}
				final long useTime = System.currentTimeMillis() - start;
				ajpv13ServerThreadsMonitor.addUseTime(useTime);
			} catch (final IOException ex) {
				LOG.error(ex.getMessage(), ex);
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
