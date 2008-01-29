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

package com.openexchange.management.osgi;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.config.Configuration;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.management.ManagementAgent;
import com.openexchange.management.internal.ConfigurationService;
import com.openexchange.management.internal.ManagementAgentImpl;
import com.openexchange.management.internal.ManagementInit;
import com.openexchange.server.ServiceHolderListener;
import com.openexchange.server.osgiservice.BundleServiceTracker;

/**
 * {@link ManagementActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ManagementActivator implements BundleActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ManagementActivator.class);

	private BundleContext context;

	private final List<ServiceTracker> serviceTrackerList = new ArrayList<ServiceTracker>();

	private ServiceRegistration serviceRegistration;

	private ServiceHolderListener<Configuration> listener;

	/**
	 * Initializes a new {@link ManagementActivator}
	 */
	public ManagementActivator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		LOG.info("starting bundle: com.openexchange.management");
		
	    this.context = context;
		try {
			/*
			 * Init service trackers
			 */
			serviceTrackerList.add(new ServiceTracker(context, Configuration.class.getName(),
					new BundleServiceTracker<Configuration>(context, ConfigurationService.getInstance(),
							Configuration.class)));
			/*
			 * Start management when configuration service is available
			 */
			listener = new ServiceHolderListener<Configuration>() {

				public void onServiceAvailable(final Configuration service) throws AbstractOXException {
					try {
					    stopInternal();
						startInternal();
					} catch (final AbstractOXException e) {
						LOG.error(e.getLocalizedMessage(), e);
						ManagementInit.getInstance().stop();
					}
				}

				public void onServiceRelease() {
				}
			};
			ConfigurationService.getInstance().addServiceHolderListener(listener);
            /*
             * Open service trackers
             */
            for (ServiceTracker tracker : serviceTrackerList) {
                tracker.open();
            }
		} catch (final Throwable t) {
			LOG.error(t.getLocalizedMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}

	}

	private void startInternal() throws AbstractOXException {
        ManagementInit.getInstance().start();
        /*
         * Register management service
         */
        serviceRegistration = context.registerService(ManagementAgent.class.getCanonicalName(),
                ManagementAgentImpl.getInstance(), null);
	}

	private void stopInternal() throws AbstractOXException {
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        if (ManagementInit.getInstance().isStarted()) {
            ManagementInit.getInstance().stop();
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		LOG.info("stopping bundle: com.openexchange.management");
		
		try {
            /*
             * Close service trackers
             */
            for (ServiceTracker tracker : serviceTrackerList) {
                tracker.close();
            }
            serviceTrackerList.clear();
			ConfigurationService.getInstance().removeServiceHolderListener(listener.getClass().getName());
			stopInternal();
		} catch (final Throwable t) {
			LOG.error(t.getLocalizedMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}

}
