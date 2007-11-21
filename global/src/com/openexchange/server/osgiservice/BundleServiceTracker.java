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

package com.openexchange.server.osgiservice;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.openexchange.server.ServiceHolder;

/**
 * {@link BundleServiceTracker} - Tracks a bundle service and fills or empties
 * corresponding {@link ServiceHolder} instance
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class BundleServiceTracker<S> implements ServiceTrackerCustomizer {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(BundleServiceTracker.class);

	protected final BundleContext context;

	protected final ServiceHolder<S> serviceHolder;

	protected final Class<S> serviceClass;

	/**
	 * Initializes a new bundle service tracker
	 * 
	 * @param context
	 *            The bundle context
	 * @param serviceHolder
	 *            The service holder
	 * @param serviceClass
	 *            The service's class (used for dynamic type comparison and
	 *            casts)
	 */
	public BundleServiceTracker(final BundleContext context, final ServiceHolder<S> serviceHolder,
			final Class<S> serviceClass) {
		super();
		this.context = context;
		this.serviceClass = serviceClass;
		this.serviceHolder = serviceHolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
	 */
	public Object addingService(final ServiceReference reference) {
		final Object addedService = context.getService(reference);
		if (serviceClass.isInstance(addedService)) {
			try {
				serviceHolder.setService(serviceClass.cast(addedService));
			} catch (final Exception e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
		return addedService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
	 *      java.lang.Object)
	 */
	public void modifiedService(final ServiceReference reference, final Object service) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
	 *      java.lang.Object)
	 */
	public void removedService(final ServiceReference reference, final Object service) {
		if (serviceClass.isInstance(service)) {
			try {
				serviceHolder.removeService();
			} catch (final Exception e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
		/*
		 * Release service
		 */
		context.ungetService(reference);
	}

}
