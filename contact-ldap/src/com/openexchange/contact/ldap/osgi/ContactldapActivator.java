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

package com.openexchange.contact.ldap.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.contact.ldap.impl.ContactldapImpl;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.server.ServiceHolderListener;
import com.openexchange.server.osgiservice.BundleServiceTracker;

public class ContactldapActivator implements BundleActivator {

	/**
	 * {@link I18nServiceHolderListener} - Properly registers all I18n services
	 * defined through property <code>"i18n.language.path"</code> when
	 * configuration service is available
	 */
	private static final class ContactldapServiceHolderListener implements ServiceHolderListener<ConfigurationService> {

		private final BundleContext context;

		private final ConfigurationServiceHolder csh;
		
		private ServiceRegistration[] serviceRegistrations;

		public ContactldapServiceHolderListener(final BundleContext context, final ConfigurationServiceHolder csh) {
			super();
			this.context = context;
			this.csh = csh;
		}

		public void onServiceAvailable(final ConfigurationService service) throws Exception {
			unregisterAll();
			serviceRegistrations = initContactldapServices(context, csh.getService());
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
				LOG.info("All ContactLdap services unregistered");
			}
		}
	}

	private static final Log LOG = LogFactory.getLog(ContactldapActivator.class);

	
	private static ServiceRegistration[] initContactldapServices(final BundleContext context, final ConfigurationService config) throws FileNotFoundException {

		final File dir = new File(config.getProperty("contactldap.configuration.path"));
		
		final List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();
		for (final String file : dir.list()) {
			
			
			final ContactInterface conlap = new ContactldapImpl(dir.getAbsolutePath()+"/"+file);
			
			final Properties prop = new Properties();
			prop.put("ldapname", conlap.getLdapServer().getServerName());
			prop.put("ip", conlap.getLdapServer().getServerIP());
			prop.put("context", conlap.getLdapServer().getContext());
			prop.put(ContactInterface.OVERRIDE_FOLDER_ATTRIBUTE, String.valueOf(conlap.getLdapServer().getFolderId()));

			serviceRegistrations.add(context.registerService(ContactInterface.class.getName(), conlap, prop));
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("All ContactLDAP services registered");
		}
		return serviceRegistrations.toArray(new ServiceRegistration[serviceRegistrations.size()]);		
		
	}

	private ConfigurationServiceHolder csh;

	private ContactldapServiceHolderListener listener;

	private final List<ServiceTracker> serviceTrackerList = new ArrayList<ServiceTracker>();

	public void start(final BundleContext context) throws Exception {

		if (LOG.isDebugEnabled())
			LOG.debug("Stopping ContactLdap");

		try {
			csh = ConfigurationServiceHolder.newInstance();

			serviceTrackerList.add(new ServiceTracker(context, ConfigurationService.class.getName(),
					new BundleServiceTracker<ConfigurationService>(context, csh, ConfigurationService.class)));

			for (final ServiceTracker tracker : serviceTrackerList) {
				tracker.open();
			}

			listener = new ContactldapServiceHolderListener(context, csh);
			csh.addServiceHolderListener(listener);

		} catch (final Throwable e) {
			throw e instanceof Exception ? (Exception) e : new Exception(e);
		}

		if (LOG.isDebugEnabled())
			LOG.debug("ContactLdap Started");
	}

	public void stop(final BundleContext context) throws Exception {
		if (LOG.isDebugEnabled())
			LOG.debug("Stopping ContactLdap");

		try {
			csh.removeServiceHolderListenerByName(listener.getClass().getName());
			/*
			 * Unregister through listener
			 */
			if (null != listener) {
				listener.unregisterAll();
				listener = null;
			}
			csh = null;
			/*
			 * Close service trackers
			 */
			for (final ServiceTracker tracker : serviceTrackerList) {
				tracker.close();
			}
			serviceTrackerList.clear();
		} catch (final Throwable e) {
			LOG.error("ContactLdapActivator: stop: ", e);
			throw e instanceof Exception ? (Exception) e : new Exception(e);
		}
		if (LOG.isDebugEnabled())
			LOG.debug("ContactLdap stopped");
	}

}
