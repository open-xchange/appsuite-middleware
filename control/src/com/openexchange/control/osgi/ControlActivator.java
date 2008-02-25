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

package com.openexchange.control.osgi;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.control.internal.ControlInit;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.management.ManagementService;
import com.openexchange.management.ManagementServiceHolder;
import com.openexchange.server.ServiceHolderListener;
import com.openexchange.server.osgiservice.BundleServiceTracker;

/**
 * {@link ControlActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ControlActivator implements BundleActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ControlActivator.class);

	private final List<ServiceTracker> serviceTrackerList = new ArrayList<ServiceTracker>();

	private ManagementServiceHolder msh;

	private ServiceHolderListener<ManagementService> listener;

	/**
	 * Initializes a new {@link ControlActivator}
	 */
	public ControlActivator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		LOG.info("starting bundle: com.openexchange.control");
		
		try {
			msh = ManagementServiceHolder.newInstance();
			ControlInit.getInstance().setManagementServiceHolder(msh);
			/*
			 * Init service trackers
			 */
			serviceTrackerList.add(new ServiceTracker(context, ManagementService.class.getName(),
					new BundleServiceTracker<ManagementService>(context, msh, ManagementService.class)));
			/*
			 * Open service trackers
			 */
			for (final ServiceTracker tracker : serviceTrackerList) {
				tracker.open();
			}
			/*
			 * Start monitoring when configuration service is available
			 */
			listener = new ServiceHolderListener<ManagementService>() {

				public void onServiceAvailable(final ManagementService service) throws AbstractOXException {
					try {
						if (ControlInit.getInstance().isStarted()) {
							ControlInit.getInstance().stop();
						}
						final ControlInit controlInit = ControlInit.getInstance();
						controlInit.setBundleContext(context);
						controlInit.start();
					} catch (final AbstractOXException e) {
						LOG.error(e.getLocalizedMessage(), e);
						ControlInit.getInstance().stop();
					}
				}

				public void onServiceRelease() {

				}
			};

			msh.addServiceHolderListener(listener);
		} catch (final Throwable t) {
			LOG.error(t.getLocalizedMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		LOG.info("stopping bundle: com.openexchange.control");
		
		try {
			msh.removeServiceHolderListenerByName(listener.getClass().getName());
			if (ControlInit.getInstance().isStarted()) {
				ControlInit.getInstance().stop();
			}
			msh = null;
			/*
			 * Close service trackers
			 */
			for (final ServiceTracker tracker : serviceTrackerList) {
				tracker.close();
			}
			serviceTrackerList.clear();
		} catch (final Throwable t) {
			LOG.error(t.getLocalizedMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}

}
