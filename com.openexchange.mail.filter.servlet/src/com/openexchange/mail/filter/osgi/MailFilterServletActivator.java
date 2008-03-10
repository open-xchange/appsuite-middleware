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

package com.openexchange.mail.filter.osgi;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mail.filter.MailFilterService;
import com.openexchange.mail.filter.internal.MailFilterServletInit;
import com.openexchange.server.ServiceHolderListener;
import com.openexchange.server.osgiservice.BundleServiceTracker;

/**
 * OSGi bundle activator for the MailFilterServlet.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class MailFilterServletActivator implements BundleActivator {

	private static transient final Log LOG = LogFactory.getLog(MailFilterServletActivator.class);
	
	private final List<ServiceTracker> serviceTrackerList = new ArrayList<ServiceTracker>();
	
	private ServiceHolderListener<HttpService> httpServiceListener;
	
	private ServiceHolderListener<MailFilterService> mailFilterServiceListener;
	
	private ServiceTracker serviceTracker = null;
	
	private boolean mailFilterServiceAvailable = false;

	private boolean httpServiceAvailable = false;

	/**
	 * {@inheritDoc}
	 */
	public void start(final BundleContext context) throws Exception {
		LOG.info("starting bundle: com.openexchange.mail.filter.servlet");
		/*
		 * Init service tracker check availibility for services
		 */
		serviceTrackerList.add(new ServiceTracker(context, MailFilterService.class.getName(),
				new BundleServiceTracker<MailFilterService>(context, MailFilterServiceHolder.getInstance(),
						MailFilterService.class)));
		serviceTrackerList.add(new ServiceTracker(context, HttpService.class.getName(),
				new BundleServiceTracker<HttpService>(context, MailFilterHttpServiceHolder.getInstance(), HttpService.class)));
		/*
		 * Open service trackers
		 */
		for (final ServiceTracker tracker : serviceTrackerList) {
			tracker.open();
		}

		/*
		 * Start mail filter servlet when mail filter service is available
		 */
		mailFilterServiceListener = new ServiceHolderListener<MailFilterService>() {

			public void onServiceAvailable(final MailFilterService service) throws AbstractOXException {
				MailFilterServletInit mailFilterServletInit = MailFilterServletInit.getInstance();
				try {
					// check if http service is already available
					if (httpServiceAvailable && !mailFilterServletInit.isStarted()) {
						mailFilterServletInit.start();
					}
					mailFilterServiceAvailable = true;
				} catch (final AbstractOXException e) {
					LOG.error(e.getLocalizedMessage(), e);
					mailFilterServletInit.stop();
				}
			}

			public void onServiceRelease() {
				mailFilterServiceAvailable = false;
			}
		};
		
		/*
		 * Start mail filter servlet when http service is available
		 */
		httpServiceListener = new ServiceHolderListener<HttpService>() {

			public void onServiceAvailable(final HttpService service) throws AbstractOXException {
				MailFilterServletInit mailFilterServletInit = MailFilterServletInit.getInstance();
				try {
					// check if mail filter service is already available
					if (mailFilterServiceAvailable && !mailFilterServletInit.isStarted()) {
						mailFilterServletInit.start();
					}
					httpServiceAvailable = true;
				} catch (final AbstractOXException e) {
					LOG.error(e.getLocalizedMessage(), e);
					mailFilterServletInit.stop();
				}
			}

			public void onServiceRelease() {
				httpServiceAvailable = false;
			}
		};

		MailFilterHttpServiceHolder.getInstance().addServiceHolderListener(httpServiceListener);
		MailFilterServiceHolder.getInstance().addServiceHolderListener(mailFilterServiceListener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(final BundleContext context) throws Exception {
		LOG.info("stopping bundle: com.openexchange.mail.filter.servlet");

		try {
			serviceTracker.close();
			MailFilterHttpServiceHolder.getInstance().removeService();
			
			if (MailFilterServletInit.getInstance().isStarted()) {
				MailFilterServletInit.getInstance().stop();
			}
		} catch (final Throwable e) {
			LOG.error("MailFilterServletActivator: stop: ", e);
			throw e instanceof Exception ? (Exception) e : new Exception(e.getMessage(), e);
		} finally {
			serviceTracker.close();
			serviceTracker = null;
		}
	}

}
