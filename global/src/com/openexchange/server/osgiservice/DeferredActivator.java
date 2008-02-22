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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link DeferredActivator} - Supports the deferred starting of a bundle which
 * highly depends on other services.
 * <p>
 * The needed services are specified through providing their classes by
 * {@link #getNeededServices()}.
 * <p>
 * When all needed services are available, the {@link #startBundle()} method is
 * invoked. For each absent service the {@link #handleUnavailability(Class)}
 * method is triggered to let the programmer decide which actions are further
 * taken.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class DeferredActivator implements BundleActivator {

	private final class DeferredServiceTrackerCustomizer implements ServiceTrackerCustomizer {

		private final Class<?> clazz;

		private final BundleContext context;

		private final int index;

		public DeferredServiceTrackerCustomizer(final Class<?> clazz, final int index, final BundleContext context) {
			super();
			this.context = context;
			this.clazz = clazz;
			this.index = index;
		}

		public Object addingService(final ServiceReference reference) {
			final Object addedService = context.getService(reference);
			if (clazz.isInstance(addedService)) {
				/*
				 * Signal availability
				 */
				signalAvailability(index);
			}
			return addedService;
		}

		public void modifiedService(final ServiceReference reference, final Object service) {
		}

		public void removedService(final ServiceReference reference, final Object service) {
			if (clazz.isInstance(service)) {
				/*
				 * Signal unavailability
				 */
				signalUnavailability(index);
				handleUnavailability(clazz);
			}
		}
	}

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(DeferredActivator.class);

	protected final List<Boolean> availability;

	/**
	 * The execution context of the bundle
	 */
	protected BundleContext context;

	protected final Map<Class<?>, ServiceTracker> serviceTrackers;

	/**
	 * Initializes a new {@link DeferredActivator}
	 */
	public DeferredActivator() {
		super();
		serviceTrackers = new ConcurrentHashMap<Class<?>, ServiceTracker>(getNeededServices().length);
		availability = new ArrayList<Boolean>(getNeededServices().length);
	}

	/**
	 * Gets the classes of the service which need to be available to start this
	 * activator
	 * 
	 * @return The array of {@link Class} instances of needed services
	 */
	protected abstract Class<?>[] getNeededServices();

	/**
	 * Handles the (possibly temporary) unavailability of a needed service. The
	 * specific activator may decide which actions are further done dependent on
	 * given service's class.
	 * <p>
	 * On the one hand, if the service in question is not needed to further keep
	 * on running, it may be discarded. On the other hand, if the service is
	 * absolutely needed; the {@link #stopBundle()} method can be invoked.
	 * 
	 * 
	 * @param clazz
	 *            The service's class
	 */
	protected abstract void handleUnavailability(final Class<?> clazz);

	/**
	 * Initializes this deferred activator's members
	 */
	private final void init() {
		final Class<?>[] classes = getNeededServices();
		for (int i = 0; i < classes.length; i++) {
			if (serviceTrackers.containsKey(classes[i])) {
				LOG.error("Duplicate service tracker for class/interface: " + classes[i].getName());
				continue;
			}
			final ServiceTracker tracker = new ServiceTracker(context, classes[i].getName(),
					new DeferredServiceTrackerCustomizer(classes[i], i, context));
			serviceTrackers.put(classes[i], tracker);
			tracker.open();
		}
	}

	/**
	 * Resets this deferred activator's members
	 */
	private final void reset() {
		for (final Iterator<ServiceTracker> iter = serviceTrackers.values().iterator(); iter.hasNext();) {
			iter.next().close();
			iter.remove();
		}
		final int size = availability.size();
		for (int i = 0; i < size; i++) {
			availability.set(i, Boolean.FALSE);
		}
	}

	/**
	 * Signals availability of the class whose index in array provided by
	 * {@link #getNeededServices()} is equal to given <code>index</code>
	 * argument.
	 * <p>
	 * If all needed services are available, then the {@link #startBundle()}
	 * method is invoked.
	 * 
	 * @param index
	 *            The class' index
	 */
	private final void signalAvailability(final int index) {
		availability.add(index, Boolean.TRUE);
		for (final Boolean b : availability) {
			if (b == Boolean.FALSE) {
				return;
			}
		}
		/*
		 * Start bundle
		 */
		try {
			startBundle();
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
		}
	}

	/**
	 * Only marks the class whose index in array provided by
	 * {@link #getNeededServices()} is equal to given <code>index</code>
	 * argument as absent.
	 * 
	 * @param index
	 *            The class' index
	 */
	private final void signalUnavailability(final int index) {
		availability.add(index, Boolean.FALSE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public final void start(final BundleContext context) throws Exception {
		try {
			this.context = context;
			init();
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t.getMessage(), t);
		}
	}

	/**
	 * Called when this bundle is started so the framework can perform the
	 * bundle-specific activities necessary to start this bundle. This method
	 * can be used to register services or to allocate any resources that this
	 * bundle needs.
	 * 
	 * This method must complete and return to its caller in a timely manner.
	 * 
	 * @throws Exception
	 *             If this method throws an exception, this bundle is marked as
	 *             stopped and the Framework will remove this bundle's
	 *             listeners, unregister all services registered by this bundle,
	 *             and release all services used by this bundle.
	 */
	protected abstract void startBundle() throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public final void stop(final BundleContext context) throws Exception {
		try {
			reset();
			stopBundle();
			this.context = null;
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t.getMessage(), t);
		}
	}

	/**
	 * Called when this bundle is stopped so the framework can perform the
	 * bundle-specific activities necessary to stop the bundle. In general, this
	 * method should undo the work that the BundleActivator.start method
	 * started. There should be no active threads that were started by this
	 * bundle when this bundle returns. A stopped bundle must not call any
	 * Framework objects.
	 * 
	 * This method must complete and return to its caller in a timely manner.
	 * 
	 * @throws Exception
	 *             If this method throws an exception, the bundle is still
	 *             marked as stopped, and the Framework will remove the bundle's
	 *             listeners, unregister all services registered by the bundle,
	 *             and release all services used by the bundle.
	 */
	protected abstract void stopBundle() throws Exception;

}
