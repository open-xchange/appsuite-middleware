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

package com.openexchange.server;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link ServiceHolder}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class ServiceHolder<S> {

	private static final Log LOG = LogFactory.getLog(ServiceHolder.class);

	private final Map<String, ServiceHolderListener<S>> listeners;

	private final AtomicInteger countActive;

	private final AtomicBoolean waiting;

	private S service;

	/**
	 * Default constructor
	 */
	protected ServiceHolder() {
		super();
		countActive = new AtomicInteger();
		waiting = new AtomicBoolean();
		listeners = new ConcurrentHashMap<String, ServiceHolderListener<S>>();
	}

	/**
	 * Add a service holder listener
	 * 
	 * @param listener
	 *            The listener
	 * @throws Exception
	 *             If listener cannot be added
	 */
	public final void addServiceHolderListener(final ServiceHolderListener<S> listener) throws Exception {
		if (listeners.containsKey(listener.getClass())) {
			return;
		}
		listeners.put(listener.getClass().getName(), listener);
		if (null != service) {
			listener.onServiceAvailable(service);
		}
	}

	/**
	 * Removes the listener with given class name
	 * 
	 * @param clazz
	 *            Listener class
	 */
	public final void removeServiceHolderListener(final Class<? extends ServiceHolderListener<S>> clazz) {
		listeners.remove(clazz);
	}

	/**
	 * Sets the service of this service holder
	 * 
	 * @param service
	 *            The service
	 * @throws Exception
	 *             If service cannot be applied
	 */
	public void setService(final S service) throws Exception {
		if (null == this.service) {
			this.service = service;
			notifyListener(true);
		}
	}

	private final void notifyListener(final boolean isAvailable) throws Exception {
		for (final Iterator<ServiceHolderListener<S>> iter = listeners.values().iterator(); iter.hasNext();) {
			if (isAvailable) {
				iter.next().onServiceAvailable(service);
			} else {
				iter.next().onServiceRelease();
			}
		}
	}

	/**
	 * Removes the service from this service holder
	 * 
	 * @throws Exception
	 *             If service cannot be properly removed
	 */
	public void removeService() throws Exception {
		if (null == service) {
			return;
		}
		if (countActive.get() > 0) {
		    // Blocking OSGi framework is not allowed.
		    LOG.error("Service counting for " + this.getClass().getName()
		        + " is not zero: " + countActive.toString());
//			waiting.set(true);
//			synchronized (countActive) {
//				try {
//					while (countActive.get() > 0) {
//						countActive.wait();
//					}
//				} catch (final InterruptedException e) {
//					LOG.error(e.getLocalizedMessage(), e);
//				} finally {
//					waiting.set(false);
//				}
//			}
		}
		this.service = null;
		notifyListener(false);
	}

	/**
	 * Gets the service or <code>null</code> if service is not active, yet<br>
	 * <b>Note:</b> Don't forget to unget the service via
	 * {@link #ungetService()}
	 * 
	 * <pre>
	 * 
	 * ...
	 * final MyService myService = MyService.getInstance();
	 * final Service s = myService.getService();
	 * try {
	 *     // Do something...
	 * } finally {
	 *     myService.ungetService();
	 * }
	 * ...
	 * 
	 * </pre>
	 * 
	 * @return The bundle service instance
	 */
	public S getService() {
		if (null == service) {
			return null;
		}
		countActive.incrementAndGet();
		return service;
	}

	/**
	 * Ungets the bundle service instance
	 */
	public void ungetService() {
		if (countActive.get() == 0) {
			return;
		}
		countActive.decrementAndGet();
		if (waiting.get()) {
			synchronized (countActive) {
				if (waiting.get()) {
					countActive.notifyAll();
				}
			}
		}
	}

}
