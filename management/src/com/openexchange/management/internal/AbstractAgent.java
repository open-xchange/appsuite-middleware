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

package com.openexchange.management.internal;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.monitor.GaugeMonitor;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class AbstractAgent {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AbstractAgent.class);

	private static final class AbstractAgentSocketFactory extends RMISocketFactory implements Serializable {

		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = 8324426326551371658L;

		private final int backlog;

		private final InetAddress bindAddress;

		public AbstractAgentSocketFactory(final int backlog, final String bindAddr) throws UnknownHostException {
			this.backlog = backlog < 1 ? 50 : backlog;
			bindAddress = bindAddr.charAt(0) == '*' ? null : InetAddress.getByName(bindAddr);
		}

		@Override
		public ServerSocket createServerSocket(final int port) throws IOException {
			return new ServerSocket(port, backlog, bindAddress);
		}

		@Override
		public Socket createSocket(final String host, final int port) throws IOException {
			return new Socket(bindAddress == null ? InetAddress.getByName(host) : bindAddress, port);
		}

	}

	private final Lock lockSocketFactory;

	private boolean initialized;

	protected RMISocketFactory rmiSocketFactory;

	protected MBeanServer mbs;

	protected Map<String, Object> environment;

	public AbstractAgent() {
		mbs = ManagementFactory.getPlatformMBeanServer();
		lockSocketFactory = new ReentrantLock();
	}

	/**
	 * Uniquely identify the MBeans and register them with the MBeanServer
	 */
	public void registerMBean(final Object object, final ObjectName objectName) throws Exception {
		if (mbs.isRegistered(objectName)) {
			printTrace(objectName.getCanonicalName() + " already registered");
			return;
		}
		mbs.registerMBean(object, objectName);
		printTrace(object + " registered");
	}

	/**
	 * Uniquely identify the MBeans and register them with the MBeanServer
	 */
	public void registerMBean(final String name, final Object mbean) throws Exception {
		final ObjectName objectName = new ObjectName(name);
		if (mbs.isRegistered(objectName)) {
			printTrace(objectName.getCanonicalName() + " already registered");
			return;
		}
		mbs.registerMBean(mbean, objectName);
		printTrace(name + " registered");
	}

	/**
	 * Unregister an mbean.
	 * 
	 * @param name
	 *            name of the mbean.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void unregisterMBean(final String name) throws Exception {
		unregisterMBean(new ObjectName(name));
	}

	/**
	 * Uniquely identify the MBeans and register them with the MBeanServer
	 */
	public void registerMBean(final ObjectName objectName, final Object mbean) throws Exception {
		if (mbs.isRegistered(objectName)) {
			printTrace(objectName.getCanonicalName() + " already registered");
			return;
		}
		mbs.registerMBean(mbean, objectName);
		printTrace(objectName.getCanonicalName() + " registered");
	}

	/**
	 * Unregister a MBean on the MBeanServer
	 */
	public void unregisterMBean(final ObjectName objectName) throws Exception {
		if (mbs.isRegistered(objectName)) {
			mbs.unregisterMBean(objectName);
			printTrace(objectName.getCanonicalName() + " unregistered");
		}
	}

	protected GaugeMonitor gm;

	/**
	 * add a MBean to observe by the GaugeMonitor
	 * 
	 * @param objectName
	 * @param attributeName
	 * @throws Exception
	 */
	public final void addObservedMBean(final ObjectName objectName, final String attributeName) throws Exception {
		if (gm == null) {
			gm = new GaugeMonitor();
			gm.setGranularityPeriod(5000);
			gm.setDifferenceMode(false);
			gm.setNotifyHigh(false);
			gm.setNotifyLow(true);
			gm.setThresholds(Integer.valueOf(50), Integer.valueOf(5));
			mbs.registerMBean(gm, new ObjectName("Services:type=GaugeMonitor"));
			gm.start();
		}

		gm.addObservedObject(objectName);
		gm.setObservedAttribute(attributeName);
	}

	/**
	 * remove an observed MBean from the GaugeMonitor
	 * 
	 * @param objectName
	 * @throws Exception
	 */
	public final void removeObservedMBean(final ObjectName objectName) throws Exception {
		if (gm != null) {
			gm.removeObservedObject(objectName);
		}
	}

	private final Map<Integer, Registry> registries = new HashMap<Integer, Registry>();

	/**
	 * add a RMI registry
	 * 
	 * @param port
	 *            The port
	 * @param bindAddr
	 *            The bind address or <code>"*"</code> to accept connections
	 *            on any/all local addresses
	 */
	protected final void addRMIRegistry(final int port, final String bindAddr) {
		Registry registry = null;
		try {
			initializeRMIServerSocketFactory(bindAddr);
			registry = LocateRegistry.createRegistry(port, rmiSocketFactory, rmiSocketFactory);
		} catch (final Exception e) {
			printTrace(new StringBuilder(200).append("Can not create a RMI registry on port ").append(port).append(
					" and bind address ").append(bindAddr).append(": ").append(e.getMessage()).toString());
			return;
		}
		registries.put(Integer.valueOf(port), registry);
		printTrace(new StringBuilder(200).append("RMI registry created on port ").append(port).append(
				" and bind address ").append(bindAddr).toString());
	}

	private final void initializeRMIServerSocketFactory(final String bindAddr) throws UnknownHostException {
		if (!initialized) {
			lockSocketFactory.lock();
			try {
				if (null == rmiSocketFactory) {
					rmiSocketFactory = new AbstractAgentSocketFactory(0, bindAddr);
					environment = new HashMap<String, Object>(2);
					environment.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, rmiSocketFactory);
					environment.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, rmiSocketFactory);
					initialized = true;
				}
			} finally {
				lockSocketFactory.unlock();
			}
		}
	}

	/**
	 * remove a RMI registry
	 * 
	 * @param port
	 */
	protected final void removeRMIRegistry(final int port) {
		final Registry registry = registries.get(Integer.valueOf(port));
		try {
			UnicastRemoteObject.unexportObject(registry, true);
			printTrace("Unexport RMI registry on port " + port);
		} catch (final Exception e) {
			printTrace("Can not unexport RMI registry on port " + port);
		}
	}

	/**
	 * list the RMI registries ports
	 * 
	 * @param port
	 */
	public final int[] getRMIRegistryPorts() {
		final int size = registries.size();
		final Iterator<Integer> iter = registries.keySet().iterator();
		final int[] portnumbers = new int[size];
		for (int i = 0; i < size; i++) {
			portnumbers[i] = iter.next().intValue();
		}
		return portnumbers;
	}

	private final Map<JMXServiceURL, JMXConnectorServer> connectors = new HashMap<JMXServiceURL, JMXConnectorServer>();

	/**
	 * Create a JMX connector and starts it
	 * 
	 * @return The assigned JMX URL
	 */
	protected final JMXServiceURL addConnector(final String urlstr) throws Exception {
		final JMXServiceURL url = new JMXServiceURL(urlstr);
		final JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
		cs.start();
		connectors.put(url, cs);
		printTrace("Connector on " + urlstr + " is started");
		return url;
	}

	/**
	 * Remove a JMX connector and stop it
	 */
	protected final void removeConnector(final JMXServiceURL url) {
		final JMXConnectorServer connector = connectors.remove(url);
		if (connector == null) {
			return;
		}
		try {
			connector.stop();
			printTrace("Connector on " + url + " is stopped");
		} catch (final IOException e) {
			LOG.error(e);
			return;
		}
	}

	/**
	 * Remove a Snmp adaptor and start it
	 */
	public final void removeSnmpAdaptor(final int port) {
		if (LOG.isTraceEnabled()) {
			LOG.trace(new StringBuilder("removeSnmpAdaptor() with ").append(port).toString());
		}
	}

	public abstract void run();

	// Utility method: so that the application continues to run
	public static final void waitForEnterPressed() {
		try {
			printTrace("Press return to exit ...");
			System.in.read();
		} catch (final Exception e) {
			LOG.error(e);
		}
	}

	public abstract void stop();

	private static final boolean TRACE = true;

	protected static final void printTrace(final String msg) {
		if (TRACE && LOG.isInfoEnabled()) {
			LOG.info(msg);
		}
	}

	private static final Lock LOCK_FREE_PORT = new ReentrantLock();

	/**
	 * Gets a free port at the time of call to this method. The logic leverages
	 * the built in java.net.ServerSocket implementation which binds a server
	 * socket to a free port when instantiated with a port <code> 0 </code>.
	 * <P>
	 * Note that this method guarantees the availability of the port only at the
	 * time of call. The method does not bind to this port.
	 * <p>
	 * Checking for free port can fail for several reasons which may indicate
	 * potential problems with the system. This method acknowledges the fact and
	 * following is the general contract:
	 * <li> Best effort is made to find a port which can be bound to. All the
	 * exceptional conditions in the due course are considered SEVERE.
	 * <li> If any exceptional condition is experienced, <code> 0 </code> is
	 * returned, indicating that the method failed for some reasons and the
	 * callers should take the corrective action. (The method need not always
	 * throw an exception for this).
	 * <li> Method is synchronized on this class.
	 * 
	 * @return integer depicting the free port number available at this time 0
	 *         otherwise.
	 */
	protected static final int getFreePort() {
		int freePort = 0;
		boolean portFound = false;
		ServerSocket serverSocket = null;
		LOCK_FREE_PORT.lock();
		try {
			try {
				/*
				 * following call normally returns the free port, to which the
				 * ServerSocket is bound.
				 */
				serverSocket = new ServerSocket(0);
				freePort = serverSocket.getLocalPort();
				portFound = true;
			} catch (final Exception e) {
				/*
				 * Squelch the exception
				 */
				LOG.error(e.getLocalizedMessage(), e);
			} finally {
				if (!portFound) {
					freePort = 0;
				}
				try {
					if (serverSocket != null) {
						serverSocket.close();
						if (!serverSocket.isClosed()) {
							throw new Exception("local exception ...");
						}
					}
				} catch (final Exception e) {
					/*
					 * Squelch the exception
					 */
					freePort = 0;
				}
			}
			return freePort;
		} finally {
			LOCK_FREE_PORT.unlock();
		}

	}

}
