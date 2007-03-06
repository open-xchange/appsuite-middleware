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



package com.openexchange.monitoring;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.monitor.GaugeMonitor;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.jmx.snmp.daemon.SnmpAdaptorServer;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public abstract class AbstractAgent {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AbstractAgent.class);

	protected MBeanServer mbs;

	public AbstractAgent() {
		mbs = ManagementFactory.getPlatformMBeanServer();
	}

	/**
	 * Uniquely identify the MBeans and register them with the MBeanServer
	 */
	public void registerMBean(final String name, final Object mbean) throws Exception {
		final ObjectName objectName = new ObjectName(name);
		mbs.registerMBean(mbean, objectName);
		printTrace(name + " registered");
	}

	/**
     * Unregister an mbean.
     * @param name name of the mbean.
	 * @throws Exception if an error occurs.
	 */
    public void unregisterMBean(final String name) throws Exception {
        unregisterMBean(new ObjectName(name));
    }

	/**
	 * Uniquely identify the MBeans and register them with the MBeanServer
	 */
	public void registerMBean(final ObjectName objectName, final Object mbean) throws Exception {
		mbs.registerMBean(mbean, objectName);
		printTrace(objectName.getCanonicalName() + " registered");
	}

	/**
	 * Unregister a MBean on the MBeanServer
	 */
	public void unregisterMBean(final ObjectName objectName) throws Exception {
		mbs.unregisterMBean(objectName);
		printTrace(objectName.getCanonicalName() + " unregistered");
	}

	protected GaugeMonitor gm;

	/**
	 * add a MBean to observe by the GaugeMonitor
	 * 
	 * @param objectName
	 * @param attributeName
	 * @throws Exception
	 */
	public void addObservedMBean(final ObjectName objectName, final String attributeName) throws Exception {
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
	public void removeObservedMBean(final ObjectName objectName) throws Exception {
		if (gm != null) {
			gm.removeObservedObject(objectName);
		}
	}

	private final Map<Integer,Registry> registries = new HashMap<Integer,Registry>();

	/**
	 * add a RMI registry
	 * 
	 * @param port
	 */
	protected void addRMIRegistry(final int port) {
		Registry registry = null;
		try {
			registry = LocateRegistry.createRegistry(port);
		} catch (Exception e) {
			printTrace("Can not create a RMI registry on port " + port);
			return;
		}
		registries.put(Integer.valueOf(port), registry);
		printTrace("RMI registry created on port " + port);
	}

	/**
	 * remove a RMI registry
	 * 
	 * @param port
	 */
	protected void removeRMIRegistry(final int port) {
		final Registry registry = registries.get(Integer.valueOf(port));
		try {
			UnicastRemoteObject.unexportObject(registry, true);
			printTrace("Unexport RMI registry on port " + port);
		} catch (Exception e) {
			printTrace("Can not unexport RMI registry on port " + port);
		}
	}

	/**
	 * list the RMI registries ports
	 * 
	 * @param port
	 */
	public int[] getRMIRegistryPorts() {
		final int size = registries.size();
		final Iterator<Integer> iter = registries.keySet().iterator();
		int[] portnumbers = new int[size];
		for (int i = 0; i < size; i++) {
			portnumbers[i] = iter.next().intValue();
		}
		return portnumbers;
	}

	private final Map<String,JMXConnectorServer> connectors = new HashMap<String,JMXConnectorServer>();

	/**
	 * Create a JMX connector and start it
	 */
	protected void addConnector(final String urlstr) throws Exception {
		final JMXServiceURL url = new JMXServiceURL(urlstr);
		final JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
		cs.start();
		connectors.put(urlstr, cs);
		printTrace("Connector on " + urlstr + " is started");
	}

	/**
	 * Remove a JMX connector and stop it
	 */
	protected void removeConnector(final String urlstr) {
		final JMXConnectorServer connector = connectors.remove(urlstr);
		if (connector == null) {
			return;
		}
		try {
			connector.stop();
			printTrace("Connector on " + urlstr + " is stopped");
		} catch (IOException e) {
			LOG.error(e);
			return;
		}
	}

	// /**
	// * Create a Http adaptor and start it
	// */
	// public void addHttpAdaptor(int port)
	// throws Exception {
	// // Register and start the HTML adaptor
	// HtmlAdaptorServer adapter = new HtmlAdaptorServer();
	// ObjectName adapterName = new ObjectName(
	// "Adaptors:type=HTML,port=" + port);
	// adapter.setPort(port);
	// mbs.registerMBean(adapter, adapterName);
	// adapter.start();
	// printtrace("HttpAdaptor on port " + port);
	// }

	/**
	 * Create a Snmp adaptor and start it
	 */
	public void addSnmpAdaptor(final int port) throws Exception {
		// Register and start the SNMP adaptor
		final SnmpAdaptorServer adapter = new SnmpAdaptorServer();
		final ObjectName adapterName = new ObjectName("Adaptors:type=SNMP,port=" + port);
		adapter.setPort(port);
		mbs.registerMBean(adapter, adapterName);
		adapter.start();
		printTrace("SnmpAdaptor on port " + port);
	}

	/**
	 * Remove a Snmp adaptor and start it
	 */
	public void removeSnmpAdaptor(final int port) {
		if (LOG.isTraceEnabled()) {
			LOG.trace(new StringBuilder("removeSnmpAdaptor() with ").append(port).toString());
		}
	}

	public abstract void run();

	// Utility method: so that the application continues to run
	public static void waitForEnterPressed() {
		try {
			printTrace("Press return to exit ...");
			System.in.read();
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	public abstract void stop();

	private static boolean TRACE = true;

	protected static void printTrace(final String msg) {
		if (TRACE && LOG.isInfoEnabled()) {
			LOG.info(msg);
		}
	}

}
