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

package com.openexchange.cache.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMISocketFactory;
import java.util.Properties;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.jcs.auxiliary.remote.server.RemoteCacheServerAttributes;

public class RemoteCacheServerStartup {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RemoteCacheServerStartup.class);

	private static final int DEFAULT_REGISTRY_PORT = 1101;

	private static int DEFAULT_RMI_SOCKET_FACTORY_TIMEOUT_MS = 10000;

	private static String serviceName;

	private static RemoteCacheServerStartup instance;

	/** The single instance of the RemoteCacheServer object. */
	private static RemoteCacheServer remoteCacheServer;

	private boolean running;

	public static void startup() {
		synchronized (RemoteCacheServerStartup.class) {
			if (instance == null) {
				instance = new RemoteCacheServerStartup();
			}
		}
		instance.startJSCRemoteCacheServer();
	}

	private RemoteCacheServerStartup() {
		super();
	}

	private void startJSCRemoteCacheServer() {
		if (running) {
			return;
		}
		/*
		 * Starts the registry and then tries to bind to it. <p> Gets the port
		 * from a props file. Uses the local host name for the rgistry host.
		 * Tries to start the registry, ignoreing failure. Starts the server.
		 */
		final String remoteCacheConfigFile = System.getProperty("remote.cache.ccf");
		Properties props = null;
		/*
		 * Set the registry port
		 */
		int registryPort = DEFAULT_REGISTRY_PORT;
		try {
			props = new Properties();
			FileInputStream fis = null;
			try {
				props.load((fis = new FileInputStream(new File(remoteCacheConfigFile))));
			} finally {
				if (fis != null) {
					fis.close();
					fis = null;
				}
			}
			// final Properties props =
			// PropertyLoader.loadProperties(remoteCacheConfigFile);
			final String portS = props.getProperty("registry.port", String.valueOf(DEFAULT_REGISTRY_PORT));

			try {
				registryPort = Integer.parseInt(portS);
			} catch (NumberFormatException e) {
				LOG.error("Problem converting port to an int.", e);
			}
		} catch (Exception e) {
			LOG.error("Problem loading props.", e);
		} catch (Throwable t) {
			LOG.error("Problem loading props.", t);
		}
		// we will always use the local machine for the registry
		String registryHost;
		try {
			registryHost = InetAddress.getLocalHost().getHostAddress();

			if (LOG.isDebugEnabled()) {
				LOG.debug("registryHost = [" + registryHost + ']');
			}

			if (("localhost".equals(registryHost) || "127.0.0.1".equals(registryHost)) && LOG.isWarnEnabled()) {
				LOG.warn("The local address [" + registryHost
						+ "] is INVALID.  Other machines must be able to use the address to reach this server.");
			}

			try {
				LocateRegistry.createRegistry(registryPort);
			} catch (RemoteException e) {
				LOG.error("Problem creating registry.  It may already be started. " + e.getMessage());
			} catch (Throwable t) {
				LOG.error("Problem creating registry.", t);
			}
			
			try {
				startupRemoteCacheServer(registryHost, registryPort, props, remoteCacheConfigFile);
			} catch (IOException e1) {
				LOG.error("Problem during Remote Cache Server startup " + e1.getMessage(), e1);
			}

		} catch (UnknownHostException e) {
			LOG.error("Could not get local address to use for the registry!", e);
		}
	}

	/**
	 * Starts up the remote cache server on this JVM, and binds it to the
	 * registry on the given host and port. A remote cache is either a local
	 * cache or a cluster cache
	 * 
	 * @param host
	 * @param port
	 * @param propFile
	 * @throws IOException
	 */
	private final void startupRemoteCacheServer(final String host, final int port, final Properties props, final String propFile)
			throws IOException {
		if (remoteCacheServer != null) {
			return;
		}

		try {
			// TODO make configurable.
			// use this socket factory to add a timeout.
			RMISocketFactory.setSocketFactory(new RMISocketFactory() {
				@Override
				public Socket createSocket(String host, int port) throws IOException {
					final Socket socket = new Socket(host, port);
					socket.setSoTimeout(DEFAULT_RMI_SOCKET_FACTORY_TIMEOUT_MS);
					socket.setSoLinger(false, 0);
					return socket;
				}

				@Override
				public ServerSocket createServerSocket(int port) throws IOException {
					return new ServerSocket(port);
				}
			});
		} catch (Exception e) {
			LOG.error("Problem setting custom RMI Socket Factory.", e);
		}

		// TODO: make automatic
		final RemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();
		rcsa.setConfigFileName(propFile);

		final String servicePortStr = props.getProperty(IRemoteCacheConstants.REMOTE_CACHE_SERVICE_PORT);
		int servicePort = -1;
		try {
			servicePort = Integer.parseInt(servicePortStr);

			rcsa.setServicePort(servicePort);
			LOG.debug("Remote cache service uses port number " + servicePort + '.');
		} catch (NumberFormatException ignore) {
			LOG.debug("Remote cache service port property " + IRemoteCacheConstants.REMOTE_CACHE_SERVICE_PORT
					+ " not specified.  An anonymous port will be used.");
		}

		String lccStr = props.getProperty(IRemoteCacheConstants.REMOTE_LOCAL_CLUSTER_CONSISTENCY);
		if (lccStr == null) {
			lccStr = "true";
		}
		final boolean lcc = Boolean.parseBoolean(lccStr);
		rcsa.setLocalClusterConsistency(lcc);

		String acgStr = props.getProperty(IRemoteCacheConstants.REMOTE_ALLOW_CLUSTER_GET);
		if (acgStr == null) {
			acgStr = "true";
		}
		final boolean acg = Boolean.parseBoolean(acgStr);
		rcsa.setAllowClusterGet(acg);

		// CREATE SERVER
		remoteCacheServer = new RemoteCacheServer(rcsa);
		// Register the RemoteCacheServer remote object in the registry.
		serviceName = props.getProperty(IRemoteCacheConstants.REMOTE_CACHE_SERVICE_NAME,
				IRemoteCacheConstants.REMOTE_CACHE_SERVICE_VAL).trim();

		if (LOG.isInfoEnabled()) {
			LOG.info("Binding server to " + host + ':' + port + " with the name " + serviceName);
		}
		try {
			Naming.rebind(new StringBuilder().append("//").append(host).append(':').append(port).append('/').append(
					serviceName).toString(), remoteCacheServer);
		} catch (MalformedURLException ex) {
			// impossible case.
			throw new IllegalArgumentException(ex.getMessage() + "; host=" + host + ", port=" + port, ex);
		}

	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(final String[] args) throws Exception {
		if (System.getProperty("remote.cache.ccf") == null) {
			throw new Exception("Missing property \"remote.cache.ccf\" in system properties");
		} else if (System.getProperty("cache.ccf") == null) {
			throw new Exception("Missing property \"cache.ccf\" in system properties");
		}
		startup();
	}

}
