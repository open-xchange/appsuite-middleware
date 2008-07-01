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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import com.openexchange.management.ManagementException;
import com.openexchange.management.ManagementService;

/**
 * {@link ManagementAgentImpl} - A JMX agent implementation
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagementAgentImpl extends AbstractAgent implements ManagementService {

	private static final ManagementAgentImpl instance = new ManagementAgentImpl();

	/*
	 * Member fields
	 */
	private int jmxPort;

	private String jmxBindAddr;

	private String jmxLogin;

	private String jmxPassword;

	private final Stack<ObjectName> objectNames = new Stack<ObjectName>();

	private JMXServiceURL jmxURL;

	private final AtomicBoolean running = new AtomicBoolean();

	/**
	 * Gets the singleton instance
	 * 
	 * @return The singleton instance
	 */
	public static ManagementAgentImpl getInstance() {
		return instance;
	}

	private ManagementAgentImpl() {
		super();
	}

	public static void main(final String argv[]) {
		final ManagementAgentImpl agent = new ManagementAgentImpl();
		agent.run();
		waitForEnterPressed();
		agent.stop();
	}

	@Override
	public void run() {
		initializeMBeanServer();
	}

	private void initializeMBeanServer() {
		if (running.get()) {
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
			addRMIRegistry(jmxPort, jmxBindAddr, jmxLogin, jmxPassword);
			/*
			 * Create a JMX connector and start it
			 */
			final String ip = getIPAddress(jmxBindAddr.charAt(0) == '*' ? "localhost" : jmxBindAddr);
			final String jmxURLStr = new StringBuilder(128).append("service:jmx:rmi:///jndi/rmi://").append(
					ip == null ? "localhost" : ip).append(':').append(jmxPort).append("/server").toString();
			jmxURL = addConnector(jmxURLStr);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(128).append(
						"\n\n\tUse JConsole or the MC4J to connect to MBeanServer with this url: ").append(jmxURL)
						.append("\n").toString());
			}
			running.set(true);
		} catch (final ManagementException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void stop() {
		if (!running.get()) {
			return;
		}
		try {
			while (!objectNames.isEmpty()) {
				unregisterMBean(objectNames.pop());
			}
		} catch (final ManagementException e) {
			LOG.error(e.getMessage(), e);
		}
		removeConnector(jmxURL);
		running.set(false);
	}

	private static String getIPAddress(final String host) {
		if (host == null) {
			return null;
		}
		try {
			return InetAddress.getByName(host).getHostAddress();
		} catch (final UnknownHostException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public void registerMBean(final Object object, final ObjectName objectName) throws ManagementException {
		if (!running.get()) {
			throw new ManagementException(ManagementException.Code.NOT_RUNNING);
		}
		super.registerMBean(object, objectName);
		objectNames.push(objectName);
	}

	@Override
	public void registerMBean(final String name, final Object mbean) throws ManagementException {
		if (!running.get()) {
			throw new ManagementException(ManagementException.Code.NOT_RUNNING);
		}
		final ObjectName objectName;
		try {
			objectName = new ObjectName(name);
		} catch (MalformedObjectNameException e) {
			throw new ManagementException(ManagementException.Code.MALFORMED_OBJECT_NAME, e, name);
		}
		super.registerMBean(objectName, mbean);
		objectNames.push(objectName);
	}

	@Override
	public void registerMBean(final ObjectName objectName, final Object mbean) throws ManagementException {
		if (!running.get()) {
			throw new ManagementException(ManagementException.Code.NOT_RUNNING);
		}
		super.registerMBean(objectName, mbean);
		objectNames.push(objectName);
	}

	@Override
	public void unregisterMBean(final String name) throws ManagementException {
		if (!running.get()) {
			throw new ManagementException(ManagementException.Code.NOT_RUNNING);
		}
		final ObjectName objectName;
		try {
			objectName = new ObjectName(name);
		} catch (MalformedObjectNameException e) {
			throw new ManagementException(ManagementException.Code.MALFORMED_OBJECT_NAME, e, name);
		}
		super.unregisterMBean(objectName);
		objectNames.remove(objectName);
	}

	@Override
	public void unregisterMBean(final ObjectName objectName) throws ManagementException {
		if (!running.get()) {
			throw new ManagementException(ManagementException.Code.NOT_RUNNING);
		}
		super.unregisterMBean(objectName);
		objectNames.remove(objectName);
	}

	/**
	 * @param jmxPort
	 *            the jmxPort to set
	 */
	public void setJmxPort(final int jmxPort) {
		this.jmxPort = jmxPort;
	}

	/**
	 * @param jmxBindAddr
	 *            the jmxBindAddr to set
	 */
	public void setJmxBindAddr(final String jmxBindAddr) {
		this.jmxBindAddr = jmxBindAddr;
	}

	/**
	 * Sets the jmxLogin
	 * 
	 * @param jmxLogin
	 *            the jmxLogin to set
	 */
	public void setJmxLogin(final String jmxLogin) {
		this.jmxLogin = jmxLogin;
	}

	/**
	 * Sets the jmxPassword
	 * 
	 * @param jmxPassword
	 *            the jmxPassword to set
	 */
	public void setJmxPassword(final String jmxPassword) {
		this.jmxPassword = jmxPassword;
	}

}
