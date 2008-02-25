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

package com.openexchange.control.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.management.ManagementAgent;
import com.openexchange.management.ManagementServiceHolder;
import com.openexchange.server.Initialization;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ControlInit implements Initialization {

	private static final AtomicBoolean started = new AtomicBoolean();

	private static final ControlInit singleton = new ControlInit();

	private BundleContext bundleContext = null;

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(ControlInit.class);

	private ManagementServiceHolder msh;

	/**
	 * Prevent instantiation.
	 */
	private ControlInit() {
		super();
	}

	/**
	 * Sets the management service holder
	 * 
	 * @param msh
	 *            The management service holder
	 */
	public void setManagementServiceHolder(final ManagementServiceHolder msh) {
		this.msh = msh;
	}

	/**
	 * @return the singleton instance.
	 */
	public static ControlInit getInstance() {
		return singleton;
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() throws AbstractOXException {
		if (started.get()) {
			LOG.error(ControlInit.class.getName() + " already started");
			return;
		}

		/*
		 * Create Beans and register them
		 */
		final ManagementAgent managementAgent = msh.getService();
		try {
			final GeneralControl generalControlBean = new GeneralControl(bundleContext);
			managementAgent.registerMBean(new ObjectName("com.openexchange.control", "name", "Control"),
					generalControlBean);
		} catch (Exception exc) {
			LOG.error("cannot register mbean", exc);
		} finally {
			msh.ungetService(managementAgent);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("JMX control applied");
		}
		started.set(true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() throws AbstractOXException {
		if (!started.get()) {
			LOG.error(ControlInit.class.getName() + " has not been started");
			return;
		}

		final ManagementAgent managementAgent = msh.getService();
		try {
			managementAgent.unregisterMBean(new ObjectName("com.openexchange.control", "name", "Control"));
		} catch (Exception exc) {
			LOG.error("cannot unregister mbean", exc);
		} finally {
			msh.ungetService(managementAgent);
		}

		removeBundleContext();
		started.set(false);
	}

	/**
	 * @return <code>true</code> if monitoring has been started; otherwise
	 *         <code>false</code>
	 */
	public boolean isStarted() {
		return started.get();
	}

	public void setBundleContext(final BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public void removeBundleContext() {
		bundleContext = null;
	}
}
