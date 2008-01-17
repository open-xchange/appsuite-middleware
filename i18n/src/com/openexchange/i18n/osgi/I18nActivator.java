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

package com.openexchange.i18n.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.config.Configuration;
import com.openexchange.i18n.I18nTools;
import com.openexchange.i18n.impl.I18nConfiguration;
import com.openexchange.i18n.impl.I18nImpl;
import com.openexchange.i18n.impl.ResourceBundleDiscoverer;
import com.openexchange.server.ServiceHolderListener;
import com.openexchange.server.osgiservice.BundleServiceTracker;

public class I18nActivator implements BundleActivator {

	/**
	 * {@link I18nServiceHolderListener} - Properly registers all I18n services
	 * defined through property <code>"i18n.language.path"</code> when
	 * configuration service is available
	 */
	private static final class I18nServiceHolderListener implements ServiceHolderListener<Configuration> {

		private final BundleContext context;

		private ServiceRegistration[] serviceRegistrations;

		public I18nServiceHolderListener(final BundleContext context) {
			super();
			this.context = context;
		}

		public void onServiceAvailable(final Configuration service) throws Exception {
			unregisterAll();
			serviceRegistrations = initI18nServices(context);
		}

		public void onServiceRelease() throws Exception {
		}

		/**
		 * Unregisters all registered I18n services and resets them to
		 * <code>null</code>.
		 */
		public void unregisterAll() {
			if (null == serviceRegistrations) {
				return;
			}
			for (int i = 0; i < serviceRegistrations.length; i++) {
				serviceRegistrations[i].unregister();
				serviceRegistrations[i] = null;
			}
			serviceRegistrations = null;
			if (LOG.isInfoEnabled()) {
				LOG.info("All I18n services unregistered");
			}
		}
	}

	private static final Log LOG = LogFactory.getLog(I18nActivator.class);

	/**
	 * Reads in all I18n services configured through property
	 * <code>"i18n.language.path"</code>, registers them, and returns
	 * corresponding service registrations for future unregistration.
	 * 
	 * @param context
	 *            The current valid bundle context
	 * @return The corresponding service registrations of registered I18n
	 *         services
	 * @throws FileNotFoundException
	 *             If directory referenced by <code>"i18n.language.path"</code>
	 *             does not exist
	 */
	private static ServiceRegistration[] initI18nServices(final BundleContext context) throws FileNotFoundException {

		// File dir = new File("/home/fred/i18n/osgi/");

		final File dir;
		{
			final Configuration conf = I18nConfiguration.getInstance().getService();
			try {
				dir = new File(conf.getProperty("i18n.language.path"));
			} finally {
				I18nConfiguration.getInstance().ungetService(conf);
			}
		}

		final List<ResourceBundle> resourceBundles = new ResourceBundleDiscoverer(dir).getResourceBundles();
		final List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();
		for (final ResourceBundle rc : resourceBundles) {
			final I18nTools i18n = new I18nImpl(rc);

			final Properties prop = new Properties();
			prop.put("language", rc.getLocale());

			serviceRegistrations.add(context.registerService(I18nTools.class.getName(), i18n, prop));
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("All I18n services registered");
		}
		return serviceRegistrations.toArray(new ServiceRegistration[serviceRegistrations.size()]);
	}

	private I18nServiceHolderListener listener;

	private final List<ServiceTracker> serviceTrackerList = new ArrayList<ServiceTracker>();

	public void start(final BundleContext context) throws Exception {

		if (LOG.isDebugEnabled())
			LOG.debug("I18n Starting");

		try {
			serviceTrackerList.add(new ServiceTracker(context, Configuration.class.getName(),
					new BundleServiceTracker<Configuration>(context, I18nConfiguration.getInstance(),
							Configuration.class)));

			for (final ServiceTracker tracker : serviceTrackerList) {
				tracker.open();
			}

			listener = new I18nServiceHolderListener(context);
			I18nConfiguration.getInstance().addServiceHolderListener(listener);

		} catch (final Throwable e) {
			throw e instanceof Exception ? (Exception) e : new Exception(e);
		}

		if (LOG.isDebugEnabled())
			LOG.debug("I18n Started");
	}

	public void stop(final BundleContext context) throws Exception {
		if (LOG.isDebugEnabled())
			LOG.debug("Stopping I18n");

		try {
			I18nConfiguration.getInstance().removeServiceHolderListener(listener.getClass().getName());
			/*
			 * Unregister through listener
			 */
			if (null != listener) {
				listener.unregisterAll();
				listener = null;
			}
			/*
			 * Close service trackers
			 */
			for (ServiceTracker tracker : serviceTrackerList) {
				tracker.close();
			}
			serviceTrackerList.clear();
		} catch (final Throwable e) {
			LOG.error("I18nActivator: stop: ", e);
			throw e instanceof Exception ? (Exception) e : new Exception(e);
		}
		if (LOG.isDebugEnabled())
			LOG.debug("I18n stopped");
	}

}
