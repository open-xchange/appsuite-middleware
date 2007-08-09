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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Stack;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MonitorAgent extends AbstractAgent {
	
	/*
	 * Static fields
	 */
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MonitorAgent.class);
	
	private static final int JMX_PORT;
	
	private static final String JMX_BIND_ADDR = ServerConfig.getProperty(Property.JMX_BIND_ADDRESS);
	
	private static MonitorAgent instance;
	
	static {
		int jmxPort;
		try {
			jmxPort = ServerConfig.getInteger(Property.JMX_PORT);
		} catch (final ConfigurationException e) {
			LOG.error(e.getLocalizedMessage(), e);
			/*
			 * Default
			 */
			jmxPort = 9999;
		}
		JMX_PORT = jmxPort;
	}
	
	/*
	 * Member fields
	 */
	private final Stack<ObjectName> objectNames = new Stack<ObjectName>();
	
	private String jmxURL;
	
	private boolean running;

	public static void startMonitorAgent() {
		synchronized (MonitorAgent.class) {
			if (instance == null) {
				instance = new MonitorAgent();
			}
		}
		instance.run();
	}
	
	public static void stopMonitor() {
		if (instance == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("MonitorAgent needs not to be stopped cause it is not running.");
			}
			return;
		}
		instance.stop();
	}

    public static MonitorAgent getInstance() {
    	if (instance == null) {
    		startMonitorAgent();
    	}
        return instance;
    }
	
	public static void registerMBeanGlobal(final ObjectName objName, final Object mbean) {
		if (instance == null) {
			startMonitorAgent();
			try {
				instance.registerMBean(objName, mbean);
			} catch (Exception e) {
				LOG.error("Cannot register pool mbean.", e);
			}
			
//			/*
//			 * startMonitorAgent() has not been invoked
//			 */
//			if (alternativeMBeanServer == null) {
//				createAlternativeMBeanServer();
//			}
//			try {
//				alternativeMBeanServer.registerMBean(mbean, objName);
//			} catch (Exception e) {
//				LOG.error("Can't register pool mbean.", e);
//			}
		} else {
			try {
				instance.registerMBean(objName, mbean);
			} catch (Exception e) {
				LOG.error("Cannot register pool mbean.", e);
			}
		}
	}
	
	/*private static final void createAlternativeMBeanServer() {
		synchronized (MonitorAgent.class) {
			if (alternativeMBeanServer == null) {
				final List servers = MBeanServerFactory.findMBeanServer(null);
				if (servers.size() > 0) {
					alternativeMBeanServer = (MBeanServer) servers.get(0);
	            }
			}
		}
	}*/

	private MonitorAgent() {
		super();
	}

	public static void main(final String argv[]) {
		final MonitorAgent agent = new MonitorAgent();
		agent.run();
		waitForEnterPressed();
		agent.stop();
	}
	
	@Override
	public void run() {
		initializeMBeanServer();
	}
	
	private final void initializeMBeanServer() {
		if (running) {
			if (LOG.isInfoEnabled()) {
				LOG.info("MonitorAgent already running...");
			}
			return;
		}
		try {
			/*
			 * Creates and exports a registry instance on the local host that
			 * accepts requests on the specified port.
			 */
			addRMIRegistry(JMX_PORT, JMX_BIND_ADDR);
			/*
			 * Create a JMX connector and start it
			 */
			final String ip = getIPAddress(JMX_BIND_ADDR.charAt(0) == '*' ? "localhost" : JMX_BIND_ADDR);
			jmxURL = new StringBuilder(100).append("service:jmx:rmi:///jndi/rmi://").append(
					ip == null ? "localhost" : ip).append(':').append(JMX_PORT).append("/server").toString();
			addConnector(jmxURL);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append(
						"\n\n\tUse the JConsole or the MC4J to connect the MBeanServer with this url: ").append(jmxURL)
						.append("\n").toString());
			}
			/*
			 * Create Beans and register them
			 */
			final GeneralMonitor generalMonitorBean = new GeneralMonitor();
			final String[] domainAndName = getDomainAndName(generalMonitorBean.getClass().getName(), false);
			final ObjectInstance objectInstance = mbs.registerMBean(new GeneralMonitor(), new ObjectName(
					domainAndName[0], "name", domainAndName[1]));
			final ObjectName objectName = objectInstance.getObjectName();
			if (objectName != null) {
				objectNames.push(objectName);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("MonitorAgent is running...");
			}
			running  = true;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Returns an array of <code>java.lang.String</code> with a length set to
	 * 2 which contains the domain and name of given class name
	 */
	public static final String[] getDomainAndName(final String className, final boolean defaultDomain) {
		final int pos = className.lastIndexOf('.');
		if (pos == -1 || defaultDomain) {
			return new String[] { MonitorMBean.DEFAULT_DOMAIN, pos == -1 ? className : className.substring(pos + 1) };
		}
		return new String[] { className.substring(0, pos), className.substring(pos + 1) };
	}

	@Override
	public void stop() {
		try {
			while (!objectNames.isEmpty()) {
				unregisterMBean(objectNames.pop());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		removeConnector(jmxURL);
		removeRMIRegistry(9999);
		running = false;
	}
	
	private static final String getIPAddress(final String host) {
		if (host == null) {
			return null;
		}
		try {
			return InetAddress.getByName(host).getHostAddress();
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}
	
	private static final String ERR_REGISTRATION = "MBean registration denied: MonitorAgent is not running";
	
	@Override
	public void registerMBean(final String name, final Object mbean) throws Exception {
		if (!running) {
			throw new Exception(ERR_REGISTRATION);
		}
		final ObjectName objectName = new ObjectName(name);
		super.registerMBean(objectName, mbean);
		objectNames.push(objectName);
	}
	
	@Override
	public void registerMBean(final ObjectName objectName, final Object mbean) throws Exception {
		if (!running) {
			throw new Exception(ERR_REGISTRATION);
		}
		super.registerMBean(objectName, mbean);
		objectNames.push(objectName);
	}
	
	 @Override
	public void unregisterMBean(final String name) throws Exception {
		 if (!running) {
				throw new Exception(ERR_REGISTRATION);
			}
		 final ObjectName objectName = new ObjectName(name);
		 super.unregisterMBean(objectName);
		 objectNames.remove(objectName);
	 }
	 
	 @Override
	public void unregisterMBean(final ObjectName objectName) throws Exception {
		 if (!running) {
				throw new Exception(ERR_REGISTRATION);
			}
		 super.unregisterMBean(objectName);
		 objectNames.remove(objectName);
	 }

}
