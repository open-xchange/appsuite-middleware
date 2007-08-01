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

package com.openexchange.tools;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class represents a fast (and optional thread-safe) implementation of a
 * FIFO (<code>first-in-first-out</code>) queue backed by an array of
 * generic objects. This class is only useful if programmer knows the size of
 * the queue in advance.
 * <p>
 * If this queue is created with enabled synchronization mechanism a
 * <code>{@link ReadWriteLock}</code> is used for mutually exclusive access
 * 
 * @see ReadWriteLock
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class FIFOQueue<T> {

	private T[] array;

	private int start, end;

	private boolean full;

	private ReadWriteLock rwLock;

	private Lock r, w;

	private final boolean isSynchronized;

	/**
	 * Constructor which invokes
	 * <code>{@link #FIFOQueue(Class, int, boolean)}</code> with last
	 * <code>boolean</code> parameter (<code>sync</code>) set to
	 * <code>true</code>
	 * 
	 * @param clazz -
	 *            the class whose instances are kept in FIFO queue
	 * @param maxsize -
	 *            the max. size of this queue
	 * @see #FIFOQueue(Class, int, boolean)
	 */
	@SuppressWarnings("unchecked")
	public FIFOQueue(final Class<T> clazz, final int maxsize) {
		this(clazz, maxsize, true);
	}

	/**
	 * Constructor
	 * 
	 * @param clazz -
	 *            the class whose instances are kept in FIFO queue
	 * @param maxsize -
	 *            the max. size of this queue
	 * @param isSynchronized -
	 *            whether this queue is synchronized (mutually exclusive) for
	 *            multiple threads accessing this queue
	 */
	@SuppressWarnings("unchecked")
	public FIFOQueue(final Class<T> clazz, final int maxsize, final boolean isSynchronized) {
		array = (T[]) Array.newInstance(clazz, maxsize);
		start = end = 0;
		full = false;
		this.isSynchronized = isSynchronized;
		if (isSynchronized) {
			rwLock = new ReentrantReadWriteLock();
			r = rwLock.readLock();
			w = rwLock.writeLock();
		}
	}

	private void acquireReadLock() {
		if (isSynchronized) {
			r.lock();
		}
	}

	private void releaseReadLock() {
		if (isSynchronized) {
			r.unlock();
		}
	}

	private void acquireWriteLock() {
		if (isSynchronized) {
			w.lock();
		}
	}

	private void releaseWriteLock() {
		if (isSynchronized) {
			w.unlock();
		}
	}

	/**
	 * Tests if this queue is empty
	 * 
	 * @return <code>true</code> if queue is empty; otherwise
	 *         <code>false</code>
	 */
	public boolean isEmpty() {
		acquireReadLock();
		try {
			return ((start == end) && !full);
		} finally {
			releaseReadLock();
		}
	}

	/**
	 * Tests if this queue is full
	 * 
	 * @return <code>true</code> if queue is full; otherwise
	 *         <code>false</code>
	 */
	public boolean isFull() {
		acquireReadLock();
		try {
			return full;
		} finally {
			releaseReadLock();
		}
	}

	/**
	 * Gets the number of contained objects in queue
	 * 
	 * @return the number of contained objects
	 */
	public int size() {
		acquireReadLock();
		try {
			if (full) {
				return array.length;
			} else if (isEmpty()) {
				return 0;
			} else {
				return start - end;
			}
		} finally {
			releaseReadLock();
		}
	}

	/**
	 * Enqueues given object to queue's tail
	 * 
	 * @param obj -
	 *            the object to enqueue
	 */
	public void enqueue(final T obj) {
		acquireWriteLock();
		try {
			if (!full) {
				array[start = (++start % array.length)] = obj;
			}
			if (start == end) {
				full = true;
			}
		} finally {
			releaseWriteLock();
		}
	}

	/**
	 * Dequeues the first object (head) in queue
	 * 
	 * @return the dequeued object
	 */
	public T dequeue() {
		acquireWriteLock();
		try {
			if (full) {
				full = false;
			} else if (isEmpty()) {
				return null;
			}
			final T retval = array[end = (++end % array.length)];
			/*
			 * Free reference for garbage collector
			 */
			array[end] = null;
			return retval;
		} finally {
			releaseWriteLock();
		}
	}

	/**
	 * Peeks (and does not remove) the first object (head) in queue
	 * 
	 * @return the first object
	 */
	public T get() {
		acquireReadLock();
		try {
			if (isEmpty()) {
				return null;
			}
			final int tmp = end;
			final T retval = array[end = (++end % array.length)];
			/*
			 * Since we do not remove from queue
			 */
			end = tmp;
			return retval;
		} finally {
			releaseReadLock();
		}
	}

}
