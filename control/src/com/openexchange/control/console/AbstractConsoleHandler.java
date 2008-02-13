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

package com.openexchange.control.console;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.openexchange.control.console.internal.ConsoleException;
import com.openexchange.control.console.internal.ValueObject;
import com.openexchange.control.console.internal.ValuePairObject;
import com.openexchange.control.console.internal.ValueParser;

/**
 * {@link AbstractConsoleHandler}
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * 
 */
public abstract class AbstractConsoleHandler {

	protected String[] defaultParameter = { "-h", "-p" };

	protected String jmxHost = "localhost";

	protected int jmxPort = 9999;

	protected JMXConnector jmxConnector = null;

	protected ObjectName objectName = null;

	protected MBeanServerConnection mBeanServerConnection = null;

	protected ValueParser valueParser;

	protected void init(String args[]) throws ConsoleException {
		init(args, false);
	}
	
	protected void init(String args[], boolean noArgs) throws ConsoleException {
		if (!noArgs && args.length == 0) {
			showHelp();
			exit();
		} else {
			try {
				valueParser = new ValueParser(args, getParameter());
				final ValueObject[] valueObjects = valueParser.getValueObjects();
				for (int a = 0; a < valueObjects.length; a++) {
					if (valueObjects[a].getValue().equals("-help")
							|| valueObjects[a].getValue().equals("--help")) {
						showHelp();
						exit();
					}
				}

				final ValuePairObject[] valuePairObjects = valueParser.getValuePairObjects();
				for (int a = 0; a < valuePairObjects.length; a++) {
					if (valuePairObjects[a].getName().equals("-h")) {
						jmxHost = valuePairObjects[a].getValue();
					} else if (valuePairObjects[a].getName().equals("-p")) {
						jmxPort = Integer.parseInt(valuePairObjects[a].getValue());
					}
				}

				final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"
						+ jmxHost + ":" + jmxPort + "/server");

				// System.out.println("jmxUrl: " + url);
				jmxConnector = JMXConnectorFactory.connect(url, null);

				mBeanServerConnection = jmxConnector.getMBeanServerConnection();

				objectName = ObjectName.getInstance("com.openexchange.control", "name", "Control");
			} catch (Exception exc) {
				throw new ConsoleException(exc);
			}
		}
	}

	protected abstract void showHelp();

	protected abstract void exit();

	protected abstract String[] getParameter();

	protected ObjectName getObjectName() {
		return objectName;
	}

	protected MBeanServerConnection getMBeanServerConnection() {
		return mBeanServerConnection;
	}
	
	protected ValueParser getParser() {
		return valueParser;
	}

	protected void close() throws ConsoleException {
		try {
			if (jmxConnector != null) {
				jmxConnector.close();
			}
		} catch (Exception exc) {
			throw new ConsoleException(exc);
		}
	}
}
