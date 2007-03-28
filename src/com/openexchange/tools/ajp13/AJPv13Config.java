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
import java.util.Properties;

import com.openexchange.configuration.SystemConfig;

/**
 * AJPv13Config
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJPv13Config {
	
	// Final static fields
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13Config.class);
	
	private static final String AJP_PROP_FILE = "AJPPROPERTIES";
	
	// Static fields
	private static int serverThreadSize = 20;
	
	private static int listenerPoolSize = 20;
	
	private static int listenerReadTimeout = 60000;
	
	private static int maxNumOfSockets = 50;
	
	private static boolean modJK;
	
	private static boolean connectionPool;
	
	private static int connectionPoolSize = 5;
	
	private static boolean requestHandlerPool;
	
	private static int requestHandlerPoolSize = 5;
	
	private static boolean watcherEnabled;
	
	private static boolean watcherPermission;
	
	private static int watcherMaxRunningTime = 300000;
	
	private static int watcherFrequency = 300000;
	
	private static int servletPoolSize = 50;
	
	private static int port = 8009;
	
	private static String jvmRoute;
	
	private static boolean checkMagicBytesStrict;

	private static String servletConfigs;
	
	static {
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
				/*
				 * AJP_PORT
				 */
				port = Integer.parseInt(ajpProperties.getProperty("AJP_PORT", "8009"));
				/*
				 * AJP_SERVER_THREAD_SIZE
				 */
				serverThreadSize = Integer.parseInt(ajpProperties.getProperty("AJP_SERVER_THREAD_SIZE", "20"));
				if (serverThreadSize < 0) {
					/*
					 * At least one server thread should accept opened sockets
					 */
					serverThreadSize = 1;
				}
				/*
				 * AJP_MAX_NUM_OF_SOCKETS
				 */
				maxNumOfSockets = Integer.parseInt(ajpProperties.getProperty("AJP_MAX_NUM_OF_SOCKETS", "50"));
				if (maxNumOfSockets < 0) {
					/*
					 * Use default value on invalid property value
					 */
					maxNumOfSockets = 50;
				}
				/*
				 * AJP_MOD_JK
				 */
				modJK = Boolean.valueOf(ajpProperties.getProperty("AJP_MOD_JK", falseStr));
				/*
				 * AJP_LISTENER_POOL_SIZE
				 */
				listenerPoolSize = Integer.parseInt(ajpProperties.getProperty("AJP_LISTENER_POOL_SIZE", "20"));
				if (listenerPoolSize < 0) {
					listenerPoolSize = 0;
				}
				/*
				 * AJP_LISTENER_READ_TIMEOUT
				 */
				listenerReadTimeout = Integer.parseInt(ajpProperties.getProperty("AJP_LISTENER_READ_TIMEOUT", "60000"));
				if (listenerReadTimeout < 0) {
					listenerReadTimeout = 0;
				}
				/*
				 * AJP_CONNECTION_POOL / AJP_CONNECTION_POOL_SIZE
				 */
				connectionPool = Boolean.valueOf(ajpProperties.getProperty("AJP_CONNECTION_POOL", "true"));
				connectionPoolSize = Integer.parseInt(ajpProperties.getProperty("AJP_CONNECTION_POOL_SIZE", "5"));
				if (connectionPoolSize < 0) {
					connectionPoolSize = 0;
				}
				/*
				 * AJP_REQUEST_HANDLER_POOL / AJP_REQUEST_HANDLER_POOL_SIZE
				 */
				requestHandlerPool = Boolean.valueOf(ajpProperties.getProperty("AJP_REQUEST_HANDLER_POOL", "true"));
				requestHandlerPoolSize = Integer.parseInt(ajpProperties.getProperty("AJP_REQUEST_HANDLER_POOL_SIZE", "5"));
				if (requestHandlerPoolSize < 0) {
					requestHandlerPoolSize = 0;
				}
				/*
				 * AJP_WATCHER_ENABLED
				 */
				watcherEnabled = Boolean.valueOf(ajpProperties.getProperty("AJP_WATCHER_ENABLED", falseStr));
				/*
				 * AJP_WATCHER_PERMISSION
				 */
				watcherPermission = Boolean.valueOf(ajpProperties.getProperty("AJP_WATCHER_PERMISSION", falseStr));
				/*
				 * AJP_WATCHER_MAX_RUNNING_TIME
				 */
				watcherMaxRunningTime = Integer.parseInt(ajpProperties.getProperty("AJP_WATCHER_MAX_RUNNING_TIME", "30000"));
				if (watcherMaxRunningTime < 0) {
					watcherMaxRunningTime = 30000;
				}
				/*
				 * AJP_WATCHER_FREQUENCY
				 */
				watcherFrequency = Integer.parseInt(ajpProperties.getProperty("AJP_WATCHER_FREQUENCY", "30000"));
				if (watcherFrequency < 0) {
					watcherFrequency = 30000;
				}
				/*
				 * SERVLET_POOL_SIZE
				 */
				servletPoolSize = Integer.parseInt(ajpProperties.getProperty("SERVLET_POOL_SIZE", "50"));
				if (servletPoolSize < 0) {
					servletPoolSize = 1;
				}
				/*
				 * AJP_JVM_ROUTE
				 */
				jvmRoute = ajpProperties.getProperty("AJP_JVM_ROUTE");
				if (jvmRoute == null) {
					LOG.error(AJPv13Exception.AJPCode.MISSING_JVM_ROUTE.getMessage());
				}
				/*
				 * AJP_CHECK_MAGIC_BYTES_STRICT
				 */
				checkMagicBytesStrict = Boolean.valueOf(ajpProperties.getProperty("AJP_CHECK_MAGIC_BYTES_STRICT", "true"));
				
				servletConfigs = ajpProperties.getProperty("AJP_SERVLET_CONFIG_DIR");
				if(servletConfigs == null) {
					servletConfigs = "/opt/open-xchange/etc/groupware/servletConfig";
				}
				
				final File servletConfigsFile = new File(servletConfigs);
				if((!servletConfigsFile.exists() || !servletConfigsFile.isDirectory()) && LOG.isWarnEnabled()) {
					LOG.warn(servletConfigsFile+" does not exist or is not a directory");
				}
				
				/*
				 * Log info
				 */
				logInfo();
			} catch (FileNotFoundException e) {
				LOG.error(e.getMessage(), e);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	private static void logInfo() {
		if (LOG.isInfoEnabled()) {
			final StringBuilder logBuilder = new StringBuilder(1000);
			logBuilder.append("\nAJP CONFIGURATION:\n");
			logBuilder.append("\tAJP_PORT=").append(port).append('\n');
			logBuilder.append("\tAJP_SERVER_THREAD_SIZE=").append(serverThreadSize).append('\n');
			logBuilder.append("\tAJP_MAX_NUM_OF_SOCKETS=").append(maxNumOfSockets).append('\n');
			logBuilder.append("\tAJP_MOD_JK=").append(modJK).append('\n');
			logBuilder.append("\tAJP_LISTENER_POOL_SIZE=").append(listenerPoolSize).append('\n');
			logBuilder.append("\tAJP_LISTENER_READ_TIMEOUT=").append(listenerReadTimeout).append('\n');
			logBuilder.append("\tAJP_CONNECTION_POOL=").append(connectionPool).append('\n');
			logBuilder.append("\tAJP_CONNECTION_POOL_SIZE=").append(connectionPoolSize).append('\n');
			logBuilder.append("\tAJP_REQUEST_HANDLER_POOL=").append(requestHandlerPool).append('\n');
			logBuilder.append("\tAJP_REQUEST_HANDLER_POOL_SIZE=").append(requestHandlerPoolSize).append('\n');
			logBuilder.append("\tAJP_WATCHER_ENABLED=").append(watcherEnabled).append('\n');
			logBuilder.append("\tAJP_WATCHER_PERMISSION=").append(watcherPermission).append('\n');
			logBuilder.append("\tAJP_WATCHER_MAX_RUNNING_TIME=").append(watcherMaxRunningTime).append('\n');
			logBuilder.append("\tAJP_WATCHER_FREQUENCY=").append(watcherFrequency).append('\n');
			logBuilder.append("\tSERVLET_POOL_SIZE=").append(servletPoolSize).append('\n');
			logBuilder.append("\tAJP_JVM_ROUTE=").append(jvmRoute).append('\n');
			logBuilder.append("\tAJP_CHECK_MAGIC_BYTES_STRICT=").append(checkMagicBytesStrict).append('\n');
			logBuilder.append("\tAJP_SERVLET_CONFIG_DIR=").append(servletConfigs);
			LOG.info(logBuilder.toString());
		}
	}
	
	private AJPv13Config() {
		super();
	}
	
	public static final int getAJPMaxNumOfSockets() {
		return maxNumOfSockets;
	}
	
	public static final boolean isAJPModJK() {
		return modJK;
	}
	
	public static final int getAJPPort() {
		return port;
	}
	
	public static final int getAJPServerThreadSize() {
		return serverThreadSize;
	}
	
	public static final int getAJPListenerPoolSize() {
		return listenerPoolSize;
	}
	
	public static final int getAJPListenerReadTimeout() {
		return listenerReadTimeout;
	}
	
	public static final boolean useAJPConnectionPool() {
		return connectionPool;
	}
	
	public static final int getAJPConnectionPoolSize() {
		return connectionPoolSize;
	}
	
	public static final boolean useAJPRequestHandlerPool() {
		return requestHandlerPool;
	}
	
	public static final int getAJPRequestHandlerPoolSize() {
		return requestHandlerPoolSize;
	}
	
	public static final boolean getAJPWatcherEnabled() {
		return watcherEnabled;
	}
	
	public static final boolean getAJPWatcherPermission() {
		return watcherPermission;
	}
	
	public static final int getAJPWatcherMaxRunningTime() {
		return watcherMaxRunningTime;
	}
	
	public static final int getAJPWatcherFrequency() {
		return watcherFrequency;
	}
	
	public static final int getServletPoolSize() {
		return servletPoolSize;
	}
	
	public static final String getJvmRoute() {
		return jvmRoute;
	}
	
	public static final boolean getCheckMagicBytesStrict() {
		return checkMagicBytesStrict;
	}
	
	public static final String getServletConfigs(){
		return servletConfigs;
	}
	
}