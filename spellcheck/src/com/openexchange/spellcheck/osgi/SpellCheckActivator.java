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

package com.openexchange.spellcheck.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.config.Configuration;
import com.openexchange.server.ServiceHolderListener;
import com.openexchange.server.osgiservice.BundleServiceTracker;
import com.openexchange.spellcheck.SpellCheckException;
import com.openexchange.spellcheck.internal.SpellCheckInit;
import com.openexchange.spellcheck.services.SpellCheckConfigurationService;
import com.openexchange.spellcheck.services.SpellCheckHttpService;

/**
 * {@link SpellCheckActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SpellCheckActivator implements BundleActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SpellCheckActivator.class);

	private static final class SpellCheckListeners {

		private final ServiceHolderListener<Configuration> configListener;

		private final ServiceHolderListener<HttpService> httpListener;

		private final AtomicInteger mode;

		public SpellCheckListeners() {
			super();
			mode = new AtomicInteger();
			configListener = new ServiceHolderListener<Configuration>() {

				public void onServiceAvailable(final Configuration service) throws Exception {
					LOG.info("SpellCheck: Configuration service available.");
					if (mode.compareAndSet(0, 1)) {
						LOG.info("Waiting for HTTP service.");
						return;
					}
					initSpellCheck();
					if (LOG.isInfoEnabled()) {
						LOG.info("Spell check bundle successfully started");
					}
				}

				public void onServiceRelease() throws Exception {

				}
			};
			httpListener = new ServiceHolderListener<HttpService>() {

				public void onServiceAvailable(final HttpService service) throws Exception {
					LOG.info("SpellCheck: HTTP service available.");
					if (mode.compareAndSet(0, 1)) {
						LOG.info("Waiting for configuration service.");
						return;
					}
					initSpellCheck();
					if (LOG.isInfoEnabled()) {
						LOG.info("Spell check bundle successfully started");
					}
				}

				public void onServiceRelease() throws Exception {

				}
			};
		}

		private static void initSpellCheck() throws SpellCheckException {
			SpellCheckInit.getInstance().start();
		}

		public ServiceHolderListener<Configuration> getConfigListener() {
			return configListener;
		}

		public ServiceHolderListener<HttpService> getHttpListener() {
			return httpListener;
		}

	}

	private final List<ServiceTracker> serviceTrackerList;

	private SpellCheckListeners listeners;

	/**
	 * Initializes a new {@link SpellCheckActivator}
	 */
	public SpellCheckActivator() {
		super();
		serviceTrackerList = new ArrayList<ServiceTracker>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		try {
			/*
			 * Add service listener for configuration service
			 */
			serviceTrackerList.add(new ServiceTracker(context, Configuration.class.getName(),
					new BundleServiceTracker<Configuration>(context, SpellCheckConfigurationService.getInstance(),
							Configuration.class)));
			/*
			 * Add service listener for HTTP service
			 */
			serviceTrackerList.add(new ServiceTracker(context, HttpService.class.getName(),
					new BundleServiceTracker<HttpService>(context, SpellCheckHttpService.getInstance(),
							HttpService.class)));
			/*
			 * Open service trackers
			 */
			for (ServiceTracker tracker : serviceTrackerList) {
				tracker.open();
			}
			/*
			 * Create listener collection
			 */
			listeners = new SpellCheckListeners();
			/*
			 * Add listeners to specific service holders
			 */
			SpellCheckConfigurationService.getInstance().addServiceHolderListener(listeners.getConfigListener());
			SpellCheckHttpService.getInstance().addServiceHolderListener(listeners.getHttpListener());
		} catch (final Throwable t) {
			LOG.error("SpellCheckActivator.start: " + t.getLocalizedMessage(), t);
			throw t instanceof Exception ? ((Exception) t) : new Exception(t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		try {
			/*
			 * Remove listeners and reset collection
			 */
			SpellCheckConfigurationService.getInstance().removeServiceHolderListenerByName(
					listeners.configListener.getClass().getName());
			SpellCheckHttpService.getInstance().removeServiceHolderListenerByName(
					listeners.httpListener.getClass().getName());
			listeners = null;
			/*
			 * Close service trackers
			 */
			for (ServiceTracker tracker : serviceTrackerList) {
				tracker.close();
			}
			serviceTrackerList.clear();
			if (LOG.isInfoEnabled()) {
				LOG.info("Spell check bundle successfully stopped");
			}
		} catch (final Throwable t) {
			LOG.error("SpellCheckActivator.stop: " + t.getLocalizedMessage(), t);
			throw t instanceof Exception ? ((Exception) t) : new Exception(t);
		}

	}

}
