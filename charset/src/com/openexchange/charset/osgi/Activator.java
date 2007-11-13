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

package com.openexchange.charset.osgi;

import java.lang.reflect.Field;
import java.nio.charset.spi.CharsetProvider;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.openexchange.charset.CollectionCharsetProvider;

/**
 * {@link Activator} - Activator for com.openexchange.charset bundle
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class Activator implements BundleActivator, ServiceTrackerCustomizer {

	private final Lock serviceTrackerLock;

	private CollectionCharsetProvider collectionCharsetProvider;

	private BundleContext context;

	private ServiceTracker serviceTracker;

	/**
	 * Default constructor
	 */
	public Activator() {
		super();
		serviceTrackerLock = new ReentrantLock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
	 */
	public Object addingService(final ServiceReference reference) {
		final Object addedService = context.getService(reference);
		if (addedService instanceof CharsetProvider) {
			serviceTrackerLock.lock();
			try {
				collectionCharsetProvider.addCharsetProvider((CharsetProvider) addedService);
			} finally {
				serviceTrackerLock.unlock();
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

	/**
	 * Modifies field <code>java.nio.charset.Charset.extendedProvider</code>
	 * 
	 * @throws NoSuchFieldException
	 *             If field "extendedProvider" does not exist
	 * @throws IllegalAccessException
	 *             If field "extendedProvider" is not accessible
	 */
	private void modifyCharsetExtendedProvider() throws NoSuchFieldException, IllegalAccessException {
		/*
		 * Force initialization of Charset.extendedProvider. Otherwise target
		 * field "extendedProvider" is not initialized.
		 */
		java.nio.charset.Charset.isSupported("X-Unknown-Charset");
		/*
		 * Modify java.nio.charset.Charset class
		 */
		final Field extendedProviderField = java.nio.charset.Charset.class.getDeclaredField("extendedProvider");
		extendedProviderField.setAccessible(true);
		/*
		 * Add previous charset provider
		 */
		collectionCharsetProvider = new CollectionCharsetProvider((CharsetProvider) extendedProviderField.get(null));
		/*
		 * Reinitialize field
		 */
		extendedProviderField.set(null, collectionCharsetProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
	 *      java.lang.Object)
	 */
	public void removedService(final ServiceReference reference, final Object service) {
		if (service instanceof CharsetProvider) {
			serviceTrackerLock.lock();
			try {
				collectionCharsetProvider.removeCharsetProvider((CharsetProvider) service);
			} finally {
				serviceTrackerLock.unlock();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		try {
			modifyCharsetExtendedProvider();
			/*
			 * Initialize a service tracker to track bundle chars providers
			 */
			this.context = context;
			serviceTracker = new ServiceTracker(context, CharsetProvider.class.getName(), this);
			serviceTracker.open();
		} catch (final Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		try {
			serviceTracker.close();
		} catch (final Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
