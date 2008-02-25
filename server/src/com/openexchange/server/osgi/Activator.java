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

import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.authentication.Authentication;
import com.openexchange.authentication.service.AuthenticationService;
import com.openexchange.charset.AliasCharsetProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.configjump.ConfigJumpInterface;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.i18n.I18nTools;
import com.openexchange.mail.MailProvider;
import com.openexchange.mail.osgi.MailProviderServiceTracker;
import com.openexchange.mail.osgi.TransportProviderServiceTracker;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.management.ManagementAgent;
import com.openexchange.monitoring.MonitorInterface;
import com.openexchange.server.ServiceHolderListener;
import com.openexchange.server.impl.Starter;
import com.openexchange.server.osgiservice.BundleServiceTracker;
import com.openexchange.server.services.ConfigJumpService;
import com.openexchange.server.services.EventAdminService;
import com.openexchange.server.services.MonitorService;
import com.openexchange.server.services.SessiondService;
import com.openexchange.sessiond.SessiondConnectorInterface;
import com.openexchange.tools.servlet.http.osgi.HttpServiceImpl;

/**
 * OSGi bundle activator for the server.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Activator implements BundleActivator, EventHandler {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(Activator.class);

	private final Starter starter = new Starter();

	private final CharsetProvider charsetProvider = new AliasCharsetProvider();

	private final HttpService httpService = new HttpServiceImpl();

	private ServiceRegistration eventServiceRegistration = null;

	private final List<ServiceRegistration> registrationList = new ArrayList<ServiceRegistration>();

	private final List<ServiceTracker> serviceTrackerList = new ArrayList<ServiceTracker>();

	private ConfigurationServiceHolder configurationServiceHolder;

	private ServiceHolderListener<ConfigurationService> listener;

	/**
	 * Bundle ID of admin.<br>
	 * TODO: Maybe this should be read by config.ini
	 */
	private static final String BUNDLE_ID_ADMIN = "open_xchange_admin";

	/**
	 * {@inheritDoc}
	 */
	public void start(final BundleContext context) throws Exception {
		LOG.info("starting bundle: com.openexchange.server");

		try {
			// Configuration service is always needed.
			configurationServiceHolder = ConfigurationServiceHolder.newInstance();
			serviceTrackerList.add(new ServiceTracker(context, ConfigurationService.class.getName(),
					new BundleServiceTracker<ConfigurationService>(context, configurationServiceHolder, ConfigurationService.class)));

			// TODO: Remove when mail is bundled
			configurationServiceHolder.addServiceHolderListener((listener = new ServiceHolderListener<ConfigurationService>() {

				public void onServiceAvailable(final ConfigurationService service) throws Exception {
					/**
					 * Mail initialization
					 */
					com.openexchange.mail.MailInitialization.getInstance().setConfigurationServiceHolder(
							configurationServiceHolder);
					com.openexchange.mail.MailInitialization.getInstance().start();
					/**
					 * Transport initialization
					 */
					com.openexchange.mail.transport.TransportInitialization.getInstance()
							.setConfigurationServiceHolder(configurationServiceHolder);
					com.openexchange.mail.transport.TransportInitialization.getInstance().start();

				}

				public void onServiceRelease() throws Exception {
					// TODO Auto-generated method stub

				}
			}));

			// event service is always needed.
			serviceTrackerList.add(new ServiceTracker(context, EventAdmin.class.getName(),
					new BundleServiceTracker<EventAdmin>(context, EventAdminService.getInstance(), EventAdmin.class)));

			// I18n service load
			serviceTrackerList.add(new ServiceTracker(context, I18nTools.class.getName(), new I18nServiceListener(
					context)));

			// Mail provider service tracker
			serviceTrackerList.add(new ServiceTracker(context, MailProvider.class.getName(),
					new MailProviderServiceTracker(context)));

			// Transport provider service tracker
			serviceTrackerList.add(new ServiceTracker(context, TransportProvider.class.getName(),
					new TransportProviderServiceTracker(context)));

			// contacts
			serviceTrackerList.add(new ServiceTracker(context, ContactInterface.class.getName(),
					new ContactServiceListener(context)));

			/*
			 * Start server
			 */
			if (isAdminBundleInstalled(context)) {
				// Start up server to only fit admin needs.
				starter.adminStart();
			} else {
				// SessionD is only needed for groupware.
				serviceTrackerList.add(new ServiceTracker(context, SessiondConnectorInterface.class.getName(),
						new BundleServiceTracker<SessiondConnectorInterface>(context, SessiondService.getInstance(),
								SessiondConnectorInterface.class)));
				// ConfigJump is only needed for groupware.
				serviceTrackerList.add(new ServiceTracker(context, ConfigJumpInterface.class.getName(),
						new BundleServiceTracker<ConfigJumpInterface>(context, ConfigJumpService.getInstance(),
								ConfigJumpInterface.class)));
				// Management is only needed for groupware.
				serviceTrackerList.add(new ServiceTracker(context, ManagementAgent.class.getName(),
						new ManagementServiceTracker(context)));
				serviceTrackerList.add(new ServiceTracker(context, MonitorInterface.class.getName(),
						new BundleServiceTracker<MonitorInterface>(context, MonitorService.getInstance(),
								MonitorInterface.class)));
				// Authentication is only needed for groupware.
				serviceTrackerList.add(new ServiceTracker(context, Authentication.class.getName(),
						new BundleServiceTracker<Authentication>(context, AuthenticationService.getInstance(),
								Authentication.class)));
				// Start up server the usual way
				starter.start();
			}
			// Open service trackers
			for (ServiceTracker tracker : serviceTrackerList) {
				tracker.open();
			}
			/**
			 * In future:
			 * 
			 * <pre>
			 * final ServiceProxyListener l = new ServerStarterServiceListener(starter, isAdminBundleInstalled(context));
			 * ConfigurationService.getInstance().addServiceProxyListener(l);
			 * </pre>
			 */
			// Register server's services
			registrationList.add(context.registerService(CharsetProvider.class.getName(), charsetProvider, null));
			registrationList.add(context.registerService(HttpService.class.getName(), httpService, null));
		} catch (final Throwable t) {
			LOG.error("Server Activator: start: ", t);
			// Try to stop what already has been started.
			starter.stop();
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(final BundleContext context) throws Exception {
		LOG.info("stopping bundle: com.openexchange.server");

		try {
			try {
				starter.stop();
			} finally {
				/*
				 * Unregister server's services
				 */
				for (ServiceRegistration registration : registrationList) {
					registration.unregister();
				}
				registrationList.clear();

				/*
				 * Close configuration service holder
				 */
				configurationServiceHolder.removeServiceHolderListenerByRef(listener);
				listener = null;
				com.openexchange.mail.MailInitialization.getInstance().setConfigurationServiceHolder(null);
				com.openexchange.mail.transport.TransportInitialization.getInstance().setConfigurationServiceHolder(
						null);
				configurationServiceHolder = null;

				/*
				 * Close service trackers
				 */
				for (ServiceTracker tracker : serviceTrackerList) {
					tracker.close();
				}
				serviceTrackerList.clear();
			}
		} catch (final Throwable t) {
			LOG.error("Server Activator: stop: ", t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}

	/**
	 * Determines if admin bundle is installed by iterating context's bundles
	 * whose status is set to {@link Bundle#INSTALLED} or {@link Bundle#ACTIVE}
	 * and whose symbolic name equals {@value #BUNDLE_ID_ADMIN}.
	 * 
	 * @param context
	 *            The bundle context
	 * @return <code>true</code> if admin bundle is installed; otherwise
	 *         <code>false</code>
	 */
	private static final boolean isAdminBundleInstalled(final BundleContext context) {
		for (final Bundle bundle : context.getBundles()) {
			if (BUNDLE_ID_ADMIN.equals(bundle.getSymbolicName())) {
				return true;
			}
		}
		return false;
	}

	public void handleEvent(final Event event) {
		System.out.println("Event detected");

	}
}
