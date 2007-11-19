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

package com.openexchange.server.osgi;

import com.openexchange.sessiond.impl.SessiondConnectorInterface;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

import com.openexchange.charset.AliasCharsetProvider;
import com.openexchange.server.Starter;
import com.openexchange.tools.servlet.http.osgi.HttpServiceImpl;
import org.osgi.util.tracker.ServiceTracker;

/**
 * OSGi bundle activator for the server.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Activator implements BundleActivator {

	private final Starter starter = new Starter();

	private final CharsetProvider charsetProvider = new AliasCharsetProvider();

	private final HttpService httpService = new HttpServiceImpl();

	private final List<ServiceRegistration> registrationList = new ArrayList<ServiceRegistration>();

	/**
	 * Bundle ID of admin.<br>
	 * TODO: Maybe this should be read by config.ini
	 */
	private static final String BUNDLE_ID_ADMIN = "open_xchange_admin";

    private ServiceTracker sessiondServiceTracker = null;

	/**
	 * {@inheritDoc}
	 */
	public void start(final BundleContext context) throws Exception {
		try {
			if (isAdminBundleInstalled(context)) {
				/*
				 * Start up server to only fit admin needs
				 */
				starter.adminStart();
			} else {
				/*
				 * Start up server the usual way
				 */
				starter.start();
			}
			/*
			 * Register server's services
			 */
			registrationList.add(context.registerService(CharsetProvider.class.getName(), charsetProvider, null));
			registrationList.add(context.registerService(HttpService.class.getName(), httpService, null));
            /*
             * Create service tracker
             */
            SessiondService sessiondTracker = new SessiondService(context);
            sessiondServiceTracker = new ServiceTracker(context, SessiondConnectorInterface.class.getName(), sessiondTracker);
		} catch (final Exception e) {
			// Try to stop what already has been started.
			starter.stop();
			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(final BundleContext context) throws Exception {
		try {
            sessiondServiceTracker.close();
			starter.stop();
		} finally {
			/*
			 * Unregister server's services
			 */
			for (ServiceRegistration registration : registrationList) {
				registration.unregister();
			}
		}
	}

	/**
	 * Determines if admin bundle is installed by iterating bundle context's
	 * bundles whose status is set to {@link Bundle#INSTALLED} or
	 * {@link Bundle#ACTIVE} and whose symbolic name equals
	 * {@value #BUNDLE_ID_ADMIN}.
	 * 
	 * @param context
	 *            The bundle context
	 * @return <code>true</code> if admin bundle is installed; otherwise
	 *         <code>false</code>
	 */
	private static final boolean isAdminBundleInstalled(final BundleContext context) {
		final Bundle[] bundles = context.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() == Bundle.INSTALLED || bundles[i].getState() == Bundle.ACTIVE)
					&& BUNDLE_ID_ADMIN.equals(bundles[i].getSymbolicName())) {
				return true;
			}
		}
		return false;
	}
}
