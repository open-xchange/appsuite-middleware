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

package com.openexchange.test.osgi;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.openexchange.control.console.StartBundle;
import com.openexchange.control.console.StopBundle;
import com.openexchange.test.JMXInit;

import junit.framework.TestCase;

/**
 * {@link AbstractBundleTest} - Abstract super class for a test class that
 * stops/starts a specific bundle to check behavior on absence.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class AbstractBundleTest extends TestCase {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AbstractBundleTest.class);

	protected static final String PROTOCOL = "http://";

	private WebConversation webConversation;

	protected StartBundle startBundle;

	protected StopBundle stopBundle;

	/**
	 * Initializes a new {@link AbstractBundleTest}
	 * 
	 * @param name
	 *            The test case name
	 */
	protected AbstractBundleTest(final String name) {
		super(name);
	}

	protected String getJMXHost() {
		return JMXInit.getJMXProperty(JMXInit.Property.JMX_HOST.toString());
	}

	protected int getJMXPort() {
		return Integer.parseInt(JMXInit.getJMXProperty(JMXInit.Property.JMX_PORT.toString()));
	}

	protected WebConversation getWebConversation() {
		if (webConversation == null) {
			webConversation = newWebConversation();
		}
		return webConversation;
	}

	/**
	 * Setup the web conversation here so tests are able to create additional if
	 * several users are needed for tests.
	 * 
	 * @return a new web conversation.
	 */
	protected WebConversation newWebConversation() {
		HttpUnitOptions.setDefaultCharacterSet("UTF-8");
		return new WebConversation();
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		stopBundle = new StopBundle(getJMXHost(), getJMXPort());
		startBundle = new StartBundle(getJMXHost(), getJMXPort());
		stopBundle.stop(getBundleName());
		LOG.info("Bundle stopped: " + getBundleName());
	}

	@Override
	public void tearDown() throws Exception {
		startBundle.start(getBundleName());
		LOG.info("Bundle started: " + getBundleName());
		stopBundle = null;
		startBundle = null;
		super.tearDown();
	}

	protected abstract String getBundleName();
}
