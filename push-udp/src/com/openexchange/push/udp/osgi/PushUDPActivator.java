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

package com.openexchange.push.udp.osgi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.services.ConfigurationServiceHolder;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.push.udp.EventAdminService;
import com.openexchange.push.udp.PushHandler;
import com.openexchange.push.udp.PushInit;
import com.openexchange.server.ServiceHolderListener;
import com.openexchange.server.osgiservice.BundleServiceTracker;

/**
 * OSGi bundle activator for the push udp.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class PushUDPActivator implements BundleActivator {

	private static transient final Log LOG = LogFactory.getLog(PushUDPActivator.class);

	private final List<ServiceTracker> serviceTrackerList = new ArrayList<ServiceTracker>();

	private ServiceRegistration eventHandlerRegistration;

	private ConfigurationServiceHolder csh;

	private ServiceHolderListener<ConfigurationService> configurationListener;

	private ServiceHolderListener<EventAdmin> eventAdminListener;

	private boolean configurationAvailable = false;

	private boolean eventAdminAvailable = false;

	/**
	 * {@inheritDoc}
	 */
	public void start(final BundleContext context) throws Exception {
		LOG.info("starting bundle: com.openexchange.push.udp");

		try {
			csh = ConfigurationServiceHolder.newInstance();
			PushInit.getInstance().setConfigurationServiceHolder(csh);
			/*
			 * Init service tracker check availibility for services
			 */
			serviceTrackerList.add(new ServiceTracker(context, EventAdmin.class.getName(),
					new BundleServiceTracker<EventAdmin>(context, EventAdminService.getInstance(),
							EventAdmin.class)));
			serviceTrackerList.add(new ServiceTracker(context, ConfigurationService.class.getName(),
					new BundleServiceTracker<ConfigurationService>(context, csh, ConfigurationService.class)));
			/*
			 * Open service trackers
			 */
			for (final ServiceTracker tracker : serviceTrackerList) {
				tracker.open();
			}
			/*
			 * Start push udp when configuration service is available
			 */
			configurationListener = new ServiceHolderListener<ConfigurationService>() {

				public void onServiceAvailable(final ConfigurationService service)
						throws AbstractOXException {
					try {
						if (eventAdminAvailable && !PushInit.getInstance().isStarted()) {
							PushInit.getInstance().start();
							addRegisterService(context);
						}
						
						configurationAvailable = true;
					} catch (final AbstractOXException e) {
						LOG.error(e.getLocalizedMessage(), e);
						PushInit.getInstance().stop();
					}
				}

				public void onServiceRelease() {
					configurationAvailable = false;
				}
			};

			/*
			 * Start push udp when event admin service is available
			 */
			eventAdminListener = new ServiceHolderListener<EventAdmin>() {

				public void onServiceAvailable(final EventAdmin service) throws AbstractOXException {
					try {
						if (configurationAvailable && !PushInit.getInstance().isStarted()) {
							PushInit.getInstance().start();
							addRegisterService(context);
						}
						
						eventAdminAvailable = true;
					} catch (final AbstractOXException e) {
						LOG.error(e.getLocalizedMessage(), e);
						PushInit.getInstance().stop();
					}
				}

				public void onServiceRelease() {
					eventAdminAvailable = false;
				}
			};

			csh.addServiceHolderListener(configurationListener);
			EventAdminService.getInstance().addServiceHolderListener(eventAdminListener);
		} catch (final Throwable e) {
			LOG.error("PushUDPActivator: start: ", e);
			// Try to stop what already has been started.
			if (null != PushInit.getInstance()) {
				PushInit.getInstance().stop();
			}
			throw e instanceof Exception ? (Exception) e : new Exception(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(final BundleContext context) throws Exception {
		LOG.info("stopping bundle: com.openexchange.sessiond");

		try {
			
			csh.removeServiceHolderListenerByName(
					configurationListener.getClass().getName());
			csh = null;
			EventAdminService.getInstance().removeServiceHolderListenerByName(
					eventAdminListener.getClass().getName());

			if (PushInit.getInstance().isStarted()) {
				PushInit.getInstance().stop();
			}

			/*
			 * Close service trackers
			 */
			for (final ServiceTracker tracker : serviceTrackerList) {
				tracker.close();
			}
			serviceTrackerList.clear();
		} catch (final Throwable e) {
			LOG.error("PushUDPActivator: start: ", e);
			throw e instanceof Exception ? (Exception) e : new Exception(e);
		}
	}
	
	protected void addRegisterService(final BundleContext context) {
		String[] topics = new String[] { EventConstants.EVENT_TOPIC, "com/openexchange/groupware/*" };
		Hashtable ht = new Hashtable();
		ht.put(EventConstants.EVENT_TOPIC, topics);
		PushHandler pushHandler = new PushHandler();
		eventHandlerRegistration = context.registerService(EventHandler.class.getName(), pushHandler, ht);
	}
}
