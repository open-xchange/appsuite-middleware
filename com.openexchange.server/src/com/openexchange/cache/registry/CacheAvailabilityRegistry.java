/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
