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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.cache.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;

/**
 * {@link CacheAvailabilityRegistry} - The cache availability registry
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class CacheAvailabilityRegistry {

	private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

	private static volatile CacheAvailabilityRegistry instance;

	/**
	 * Initializes the singleton instance of {@link CacheAvailabilityRegistry}
	 */
	static void initInstance() {
		if (!INITIALIZED.get()) {
			synchronized (INITIALIZED) {
				if (instance == null) {
					instance = new CacheAvailabilityRegistry();
					instance.init();
					INITIALIZED.set(true);
				}
			}
		}
	}

	/**
	 * Releases the singleton instance of {@link CacheAvailabilityRegistry}
	 */
	static void releaseInstance() {
		if (INITIALIZED.get()) {
			synchronized (INITIALIZED) {
				if (instance != null) {
					instance = null;
					INITIALIZED.set(false);
				}
			}
		}
	}

	/**
	 * @return The singleton instance of {@link CacheAvailabilityRegistry}
	 */
	public static CacheAvailabilityRegistry getInstance() {
		return instance;
	}

	private void init() {
		// registerDowngradeListener(new TasksDowngrade());
		/*
		 * TODO: Insert downgrade listeners for user configuration, settings,
		 * quota and attachments
		 */
	}

	/**
	 * The class set to detect duplicate listeners
	 */
	private final Set<Class<? extends CacheAvailabilityListener>> classes;

	/**
	 * The listener list to keep the order
	 */
	private final List<CacheAvailabilityListener> listeners;

	/**
	 * The lock to synchronize accesses
	 */
	private final Lock registryLock;

	private CacheAvailabilityRegistry() {
		super();
		registryLock = new ReentrantLock();
		listeners = new ArrayList<CacheAvailabilityListener>();
		classes = new HashSet<Class<? extends CacheAvailabilityListener>>();
	}

	/**
	 * Notifies the absence of cache service
	 *
	 * @throws OXException
	 *             If an error occurs
	 */
	public void notifyAbsence() throws OXException {
		registryLock.lock();
		try {
			final int size = listeners.size();
			final Iterator<CacheAvailabilityListener> iter = listeners.iterator();
			for (int i = 0; i < size; i++) {
				iter.next().handleAbsence();
			}
		} finally {
			registryLock.unlock();
		}
	}

	/**
	 * Notifies the availability of cache service
	 *
	 * @throws OXException
	 *             If an error occurs
	 */
	public void notifyAvailability() throws OXException {
		registryLock.lock();
		try {
			final int size = listeners.size();
			final Iterator<CacheAvailabilityListener> iter = listeners.iterator();
			for (int i = 0; i < size; i++) {
				iter.next().handleAvailability();
			}
		} finally {
			registryLock.unlock();
		}
	}

	/**
	 * Registers an instance of {@link CacheAvailabilityListener}.<br>
	 * <b>Note</b>: Only one instance of a certain
	 * {@link CacheAvailabilityListener} implementation is added, meaning if you
	 * try to register a certain implementation twice, the latter one is going
	 * to be discarded
	 *
	 * @param listener
	 *            the listener to register
	 * @return <code>true</code> if specified downgrade listener has been
	 *         added to registry; otherwise <code>false</code>
	 */
	public boolean registerListener(final CacheAvailabilityListener listener) {
		registryLock.lock();
		try {
			if (classes.contains(listener.getClass())) {
				return false;
			}
			listeners.add(listener);
			classes.add(listener.getClass());
			return true;
		} finally {
			registryLock.unlock();
		}
	}

	/**
	 * Removes given instance of {@link CacheAvailabilityListener} from this
	 * registry's known listeners.
	 *
	 * @param listener -
	 *            the listener to remove
	 */
	public void unregisterListener(final CacheAvailabilityListener listener) {
		registryLock.lock();
		try {
			final Class<? extends CacheAvailabilityListener> clazz = listener.getClass();
			if (!classes.contains(clazz)) {
				return;
			}
			if (!listeners.remove(listener)) {
				/*
				 * Remove by reference did not work
				 */
				int size = listeners.size();
				for (int i = 0; i < size; i++) {
					if (clazz.equals(listeners.get(i).getClass())) {
						listeners.remove(i);
						// Reset size to leave loop
						size = 0;
					}
				}
			}
			classes.remove(clazz);
		} finally {
			registryLock.unlock();
		}
	}
}
