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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;

/**
 * AJPv13Config
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13Config implements Initialization {

	// Final static fields
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13Config.class);

	private static final String AJP_PROP_FILE = "AJPPROPERTIES";

	private static AJPv13Config instance = new AJPv13Config();

	private static AtomicBoolean started = new AtomicBoolean();

	public static AJPv13Config getInstance() {
		return instance;
	}

	// fields
	private AtomicBoolean initialized = new AtomicBoolean();

	private int serverThreadSize = 20;

	private int listenerPoolSize = 20;

	private int listenerReadTimeout = 60000;

	private int maxNumOfSockets = 50;

	private boolean modJK;

	private boolean connectionPool;

	private int connectionPoolSize = 5;

	private boolean requestHandlerPool;

	private int requestHandlerPoolSize = 5;

	private boolean watcherEnabled;

	private boolean watcherPermission;

	private int watcherMaxRunningTime = 300000;

	private int watcherFrequency = 300000;

	private int servletPoolSize = 50;

	private int port = 8009;

	private String jvmRoute;

	private boolean checkMagicBytesStrict;

	private String servletConfigs;

	private InetAddress ajpBindAddr;

	public void start() throws AbstractOXException {
		if (started.get()) {
			LOG.error(this.getClass().getName() + " already started");
			return;
		}
		init();
		started.set(true);
	}

	public void stop() throws AbstractOXException {
		if (!started.get()) {
			LOG.error(this.getClass().getName() + " cannot be stopped since it has no been started before");
			return;
		}
		reset();
		started.set(false);
	}

	private void reset() {
		if (!initialized.get()) {
			return;
		}
		serverThreadSize = 20;
		listenerPoolSize = 20;
		listenerReadTimeout = 60000;
		maxNumOfSockets = 50;
		modJK = false;
		connectionPool = false;
		connectionPoolSize = 5;
		requestHandlerPool = false;
		requestHandlerPoolSize = 5;
		watcherEnabled = false;
		watcherPermission = false;
		watcherMaxRunningTime = 300000;
		watcherFrequency = 300000;
		servletPoolSize = 50;
		port = 8009;
		jvmRoute = null;
		checkMagicBytesStrict = false;
		servletConfigs = null;
		ajpBindAddr = null;
		/*
		 * Switch flag
		 */
		initialized.set(false);
	}

	private void init() throws AJPv13Exception {
		if (initialized.get()) {
			return;
		}
		final Properties ajpProperties = new Properties();
		final String ajpPropFile = SystemConfig.getProperty(AJP_PROP_FILE);
		if (ajpPropFile != null) {
			try {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(new File(ajpPropFile));
					ajpProperties.load(fis);
				} finally {
					if (fis != null) {
						fis.close();
					}
				}
				final String falseStr = "false";
				final String trueStr = "true";
				/*
				 * AJP_PORT
				 */
				port = Integer.parseInt(ajpProperties.getProperty("AJP_PORT", "8009").trim());
				/*
				 * AJP_SERVER_THREAD_SIZE
				 */
				serverThreadSize = Integer.parseInt(ajpProperties.getProperty("AJP_SERVER_THREAD_SIZE", "20").trim());
				if (serverThreadSize < 0) {
					/*
					 * At least one server thread should accept opened sockets
					 */
					serverThreadSize = 1;
				}
				/*
				 * AJP_MAX_NUM_OF_SOCKETS
				 */
				maxNumOfSockets = Integer.parseInt(ajpProperties.getProperty("AJP_MAX_NUM_OF_SOCKETS", "50").trim());
				if (maxNumOfSockets < 0) {
					/*
					 * Use default value on invalid property value
					 */
					maxNumOfSockets = 50;
				}
				/*
				 * AJP_MOD_JK
				 */
				modJK = trueStr.regionMatches(true, 0, ajpProperties.getProperty("AJP_MOD_JK", falseStr).trim(), 0, 4);
				/*
				 * AJP_LISTENER_POOL_SIZE
				 */
				listenerPoolSize = Integer.parseInt(ajpProperties.getProperty("AJP_LISTENER_POOL_SIZE", "20").trim());
				if (listenerPoolSize < 0) {
					listenerPoolSize = 0;
				}
				/*
				 * AJP_LISTENER_READ_TIMEOUT
				 */
				listenerReadTimeout = Integer.parseInt(ajpProperties.getProperty("AJP_LISTENER_READ_TIMEOUT", "60000")
						.trim());
				if (listenerReadTimeout < 0) {
					listenerReadTimeout = 0;
				}
				/*
				 * AJP_CONNECTION_POOL / AJP_CONNECTION_POOL_SIZE
				 */
				connectionPool = trueStr.regionMatches(true, 0, ajpProperties.getProperty("AJP_CONNECTION_POOL",
						trueStr).trim(), 0, 4);
				connectionPoolSize = Integer
						.parseInt(ajpProperties.getProperty("AJP_CONNECTION_POOL_SIZE", "5").trim());
				if (connectionPoolSize < 0) {
					connectionPoolSize = 0;
				}
				/*
				 * AJP_REQUEST_HANDLER_POOL / AJP_REQUEST_HANDLER_POOL_SIZE
				 */
				requestHandlerPool = trueStr.regionMatches(true, 0, ajpProperties.getProperty(
						"AJP_REQUEST_HANDLER_POOL", trueStr).trim(), 0, 4);
				requestHandlerPoolSize = Integer.parseInt(ajpProperties.getProperty("AJP_REQUEST_HANDLER_POOL_SIZE",
						"5").trim());
				if (requestHandlerPoolSize < 0) {
					requestHandlerPoolSize = 0;
				}
				/*
				 * AJP_WATCHER_ENABLED
				 */
				watcherEnabled = trueStr.regionMatches(true, 0, ajpProperties.getProperty("AJP_WATCHER_ENABLED",
						falseStr).trim(), 0, 4);
				/*
				 * AJP_WATCHER_PERMISSION
				 */
				watcherPermission = trueStr.regionMatches(true, 0, ajpProperties.getProperty("AJP_WATCHER_PERMISSION",
						falseStr).trim(), 0, 4);
				/*
				 * AJP_WATCHER_MAX_RUNNING_TIME
				 */
				watcherMaxRunningTime = Integer.parseInt(ajpProperties.getProperty("AJP_WATCHER_MAX_RUNNING_TIME",
						"30000").trim());
				if (watcherMaxRunningTime < 0) {
					watcherMaxRunningTime = 30000;
				}
				/*
				 * AJP_WATCHER_FREQUENCY
				 */
				watcherFrequency = Integer.parseInt(ajpProperties.getProperty("AJP_WATCHER_FREQUENCY", "30000").trim());
				if (watcherFrequency < 0) {
					watcherFrequency = 30000;
				}
				/*
				 * SERVLET_POOL_SIZE
				 */
				servletPoolSize = Integer.parseInt(ajpProperties.getProperty("SERVLET_POOL_SIZE", "50").trim());
				if (servletPoolSize < 0) {
					servletPoolSize = 1;
				}
				/*
				 * AJP_JVM_ROUTE
				 */
				jvmRoute = ajpProperties.getProperty("AJP_JVM_ROUTE");
				if (jvmRoute == null) {
					LOG.error(AJPv13Exception.AJPCode.MISSING_JVM_ROUTE.getMessage());
				} else {
					jvmRoute = jvmRoute.trim();
				}
				/*
				 * AJP_CHECK_MAGIC_BYTES_STRICT
				 */
				checkMagicBytesStrict = trueStr.regionMatches(true, 0, ajpProperties.getProperty(
						"AJP_CHECK_MAGIC_BYTES_STRICT", trueStr).trim(), 0, 4);
				/*
				 * AJP_SERVLET_CONFIG_DIR
				 */
				servletConfigs = ajpProperties.getProperty("AJP_SERVLET_CONFIG_DIR");
				if (servletConfigs == null || "null".equalsIgnoreCase((servletConfigs = servletConfigs.trim()))) {
					servletConfigs = SystemConfig.getProperty("CONFIGPATH") + "/servletConfig";
				}
				final File servletConfigsFile = new File(servletConfigs);
				if ((!servletConfigsFile.exists() || !servletConfigsFile.isDirectory()) && LOG.isWarnEnabled()) {
					LOG.warn(servletConfigsFile + " does not exist or is not a directory");
				}
				/*
				 * AJP_BIND_ADDR
				 */
				final String bindAddr = ajpProperties.getProperty("AJP_BIND_ADDR", "localhost").trim();
				ajpBindAddr = bindAddr.charAt(0) == '*' ? null : InetAddress.getByName(ajpProperties.getProperty(
						"AJP_BIND_ADDR", "localhost"));
				/*
				 * Switch flag
				 */
				initialized.set(true);
				/*
				 * Log info
				 */
				logInfo();
			} catch (final FileNotFoundException e) {
				throw new AJPv13Exception(AJPv13Exception.AJPCode.FILE_NOT_FOUND, e, ajpPropFile);
			} catch (final IOException e) {
				throw new AJPv13Exception(AJPv13Exception.AJPCode.IO_ERROR, e, e.getLocalizedMessage());
			}
		}
	}

	private static void logInfo() {
		if (LOG.isInfoEnabled()) {
			final StringBuilder logBuilder = new StringBuilder(1000);
			logBuilder.append("\nAJP CONFIGURATION:\n");
			logBuilder.append("\tAJP_PORT=").append(instance.port).append('\n');
			logBuilder.append("\tAJP_SERVER_THREAD_SIZE=").append(instance.serverThreadSize).append('\n');
			logBuilder.append("\tAJP_MAX_NUM_OF_SOCKETS=").append(instance.maxNumOfSockets).append('\n');
			logBuilder.append("\tAJP_MOD_JK=").append(instance.modJK).append('\n');
			logBuilder.append("\tAJP_LISTENER_POOL_SIZE=").append(instance.listenerPoolSize).append('\n');
			logBuilder.append("\tAJP_LISTENER_READ_TIMEOUT=").append(instance.listenerReadTimeout).append('\n');
			logBuilder.append("\tAJP_CONNECTION_POOL=").append(instance.connectionPool).append('\n');
			logBuilder.append("\tAJP_CONNECTION_POOL_SIZE=").append(instance.connectionPoolSize).append('\n');
			logBuilder.append("\tAJP_REQUEST_HANDLER_POOL=").append(instance.requestHandlerPool).append('\n');
			logBuilder.append("\tAJP_REQUEST_HANDLER_POOL_SIZE=").append(instance.requestHandlerPoolSize).append('\n');
			logBuilder.append("\tAJP_WATCHER_ENABLED=").append(instance.watcherEnabled).append('\n');
			logBuilder.append("\tAJP_WATCHER_PERMISSION=").append(instance.watcherPermission).append('\n');
			logBuilder.append("\tAJP_WATCHER_MAX_RUNNING_TIME=").append(instance.watcherMaxRunningTime).append('\n');
			logBuilder.append("\tAJP_WATCHER_FREQUENCY=").append(instance.watcherFrequency).append('\n');
			logBuilder.append("\tSERVLET_POOL_SIZE=").append(instance.servletPoolSize).append('\n');
			logBuilder.append("\tAJP_JVM_ROUTE=").append(instance.jvmRoute).append('\n');
			logBuilder.append("\tAJP_CHECK_MAGIC_BYTES_STRICT=").append(instance.checkMagicBytesStrict).append('\n');
			logBuilder.append("\tAJP_SERVLET_CONFIG_DIR=").append(instance.servletConfigs).append('\n');
			logBuilder.append("\tAJP_BIND_ADDR=").append(
					instance.ajpBindAddr == null ? "* (all interfaces)" : instance.ajpBindAddr.toString());
			LOG.info(logBuilder.toString());
		}
	}

	private AJPv13Config() {
		super();
	}

	public static int getAJPMaxNumOfSockets() {
		return instance.maxNumOfSockets;
	}

	public static boolean isAJPModJK() {
		return instance.modJK;
	}

	public static int getAJPPort() {
		return instance.port;
	}

	public static int getAJPServerThreadSize() {
		return instance.serverThreadSize;
	}

	public static int getAJPListenerPoolSize() {
		return instance.listenerPoolSize;
	}

	public static int getAJPListenerReadTimeout() {
		return instance.listenerReadTimeout;
	}

	public static boolean useAJPConnectionPool() {
		return instance.connectionPool;
	}

	public static int getAJPConnectionPoolSize() {
		return instance.connectionPoolSize;
	}

	public static boolean useAJPRequestHandlerPool() {
		return instance.requestHandlerPool;
	}

	public static int getAJPRequestHandlerPoolSize() {
		return instance.requestHandlerPoolSize;
	}

	public static boolean getAJPWatcherEnabled() {
		return instance.watcherEnabled;
	}

	public static boolean getAJPWatcherPermission() {
		return instance.watcherPermission;
	}

	public static int getAJPWatcherMaxRunningTime() {
		return instance.watcherMaxRunningTime;
	}

	public static int getAJPWatcherFrequency() {
		return instance.watcherFrequency;
	}

	public static int getServletPoolSize() {
		return instance.servletPoolSize;
	}

	public static String getJvmRoute() {
		return instance.jvmRoute;
	}

	public static boolean getCheckMagicBytesStrict() {
		return instance.checkMagicBytesStrict;
	}

	public static String getServletConfigs() {
		return instance.servletConfigs;
	}

	/**
	 * @return an instance if <code>java.net.InetAddress</code> if property
	 *         AJP_BIND_ADDR is different to "*"; <code>null</code> otherwise
	 */
	public static InetAddress getAJPBindAddress() {
		return instance.ajpBindAddr;
	}

}