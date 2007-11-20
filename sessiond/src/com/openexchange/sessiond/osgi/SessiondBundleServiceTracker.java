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

package com.openexchange.sessiond.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.ServiceProxy;
import com.openexchange.server.osgiservice.BundleServiceTracker;
import com.openexchange.sessiond.impl.SessiondInit;

/**
 * {@link SessiondBundleServiceTracker} - Provides access to a bundle service instance
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class SessiondBundleServiceTracker<S> extends BundleServiceTracker<S> {
	
	private static final Log LOG = LogFactory.getLog(SessiondBundleServiceTracker.class);

	/**
	 * Initializes a new bundle service
	 * 
	 * @param context
	 *            The bundle context
	 */
	public SessiondBundleServiceTracker(final BundleContext context, final ServiceProxy<S> proxy, final Class<S> serviceClass) {
		super(context, proxy, serviceClass);
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
				SessiondInit.getInstance().start();
			} catch (AbstractOXException exc) {
				LOG.error(exc);
			}
		}
		
		return super.addingService(reference);
	}
}
