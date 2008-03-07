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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.caching.CacheService;
import com.openexchange.charset.AliasCharsetProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configjump.ConfigJumpService;
import com.openexchange.configjump.client.ConfigJump;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.i18n.I18nTools;
import com.openexchange.mail.MailProvider;
import com.openexchange.mail.osgi.MailProviderServiceTracker;
import com.openexchange.mail.osgi.TransportProviderServiceTracker;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.management.ManagementService;
import com.openexchange.server.impl.Starter;
import com.openexchange.server.osgiservice.BundleServiceTracker;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.SessiondConnectorInterface;
import com.openexchange.tools.servlet.http.osgi.HttpServiceImpl;

/**
 * {@link ServerActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ServerActivator extends DeferredActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ServerActivator.class);

	/**
	 * Bundle ID of admin.<br>
	 * TODO: Maybe this should be read by config.ini
	 */
	private static final String BUNDLE_ID_ADMIN = "open_xchange_admin";

	private static final Class<?>[] NEEDED_SERIVICES_ADMIN = { ConfigurationService.class, CacheService.class,
			EventAdmin.class };

	private static final Class<?>[] NEEDED_SERIVICES_SERVER = { ConfigurationService.class, CacheService.class,
			EventAdmin.class, SessiondConnectorInterface.class };

	private final Starter starter = new Starter();

	private Boolean adminBundleInstalled;

	/**
	 * Initializes a new {@link ServerActivator}
	 */
	public ServerActivator() {
		super();
	}

	private final List<ServiceRegistration> registrationList = new ArrayList<ServiceRegistration>();

	private final List<ServiceTracker> serviceTrackerList = new ArrayList<ServiceTracker>();

	/**
	 * The server bundle will not start unless these services are available:
	 * <ul>
	 * <li>{@link ConfigurationService} to properly start up the mail system</li>
	 * <li>{@link CacheService} needed by server in any case</li>
	 * <li>{@link EventAdmin} for a working event system</li>
	 * </ul>
	 */
	@Override
	protected Class<?>[] getNeededServices() {
		if (null == adminBundleInstalled) {
			adminBundleInstalled = Boolean.valueOf(isAdminBundleInstalled(context));
		}
		return adminBundleInstalled.booleanValue() ? NEEDED_SERIVICES_ADMIN : NEEDED_SERIVICES_SERVER;
	}

	@Override
	protected void handleUnavailability(final Class<?> clazz) {
		/*
		 * Never stop the server even if a needed service is absent
		 */
		if (LOG.isWarnEnabled()) {
			LOG.warn("Absent service: " + clazz.getName());
		}
	}

	@Override
	protected void startBundle() throws Exception {
		LOG.info("starting bundle: com.openexchange.server");

		/*
		 * Needed services are available, so add them to registry already
		 */
		{
			final ServerServiceRegistry registry = ServerServiceRegistry.getInstance();
			registry.clearRegistry();
			final Class<?>[] classes = getNeededServices();
			for (int i = 0; i < classes.length; i++) {
				registry.addService(classes[i], getService(classes[i]));
			}
		}
		/*
		 * Add service trackers
		 */
		// I18n service load
		serviceTrackerList
				.add(new ServiceTracker(context, I18nTools.class.getName(), new I18nServiceListener(context)));

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
		 * Start server dependent on whether admin bundle is available or not
		 */
		if (adminBundleInstalled.booleanValue()) {
			// Start up server to only fit admin needs.
			starter.adminStart();
		} else {
			// Management is only needed for groupware.
			serviceTrackerList.add(new ServiceTracker(context, ManagementService.class.getName(),
					new ManagementServiceTracker(context)));
			// TODO:
			/**
			 * <pre>
			 * serviceTrackerList.add(new ServiceTracker(context, MonitorService.class.getName(),
			 * 		new BundleServiceTracker&lt;MonitorService&gt;(context, MonitorService.getInstance(), MonitorService.class)));
			 * </pre>
			 */

			// Authentication is only needed for groupware.
			serviceTrackerList.add(new ServiceTracker(context, AuthenticationService.class.getName(),
				new BundleServiceTracker<AuthenticationService>(context, Authentication.getHolder(),
				AuthenticationService.class)));
			// ConfigJump
			serviceTrackerList.add(new ServiceTracker(context, ConfigJumpService.class.getName(),
			    new BundleServiceTracker<ConfigJumpService>(context, ConfigJump.getHolder(),
		        ConfigJumpService.class)));
			// Start up server the usual way
			starter.start();
		}
		// Open service trackers
		for (final ServiceTracker tracker : serviceTrackerList) {
			tracker.open();
		}
		// Register server's services
		registrationList
				.add(context.registerService(CharsetProvider.class.getName(), new AliasCharsetProvider(), null));
		registrationList.add(context.registerService(HttpService.class.getName(), new HttpServiceImpl(), null));
	}

	@Override
	protected void stopBundle() throws Exception {
		LOG.info("stopping bundle: com.openexchange.server");
        /*
         * Unregister server's services
         */
        for (final ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
        registrationList.clear();
        /*
         * Close service trackers
         */
        for (ServiceTracker tracker : serviceTrackerList) {
            tracker.close();
        }
        serviceTrackerList.clear();
        // Stop all inside the server.
		starter.stop();
		/*
		 * Clear service registry
		 */
		ServerServiceRegistry.getInstance().clearRegistry();
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
	private static boolean isAdminBundleInstalled(final BundleContext context) {
		for (final Bundle bundle : context.getBundles()) {
			if (BUNDLE_ID_ADMIN.equals(bundle.getSymbolicName())) {
				return true;
			}
		}
		return false;
	}

}
