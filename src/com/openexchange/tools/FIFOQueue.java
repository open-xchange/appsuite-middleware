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
 * This class represents a very fast thread-safe implementation of a FIFO
 * (first-in-first-out) queue backed by an array of generic objects. This class
 * is only useful if programmer knows the size of the queue in advance.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class FIFOQueue<T> {

	private T[] array;

	private int start, end;

	private boolean full;

	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

	private final Lock r = rwLock.readLock(), w = rwLock.writeLock();

	@SuppressWarnings("unchecked")
	public FIFOQueue(Class<T> clazz, int maxsize) {
		array = (T[]) Array.newInstance(clazz, maxsize);
		start = end = 0;
		full = false;
	}

	public boolean isEmpty() {
		r.lock();
		try {
			return ((start == end) && !full);
		} finally {
			r.unlock();
		}
	}

	public boolean isFull() {
		r.lock();
		try {
			return full;
		} finally {
			r.unlock();
		}
	}

	public int size() {
		r.lock();
		try {
			if (full) {
				return array.length;
			} else if (isEmpty()) {
				return 0;
			} else {
				return start - end;
			}
		} finally {
			r.unlock();
		}
	}

	public void enqueue(final T obj) {
		w.lock();
		try {
			if (!full) {
				array[start = (++start % array.length)] = obj;
			}
			if (start == end) {
				full = true;
			}
		} finally {
			w.unlock();
		}
	}

	public T dequeue() {
		w.lock();
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
			w.unlock();
		}
	}

	public T get() {
		r.lock();
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
			r.unlock();
		}
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see java.lang.Object#toString()
//	 */
//	public String toString() {
//		final StringBuilder sb = new StringBuilder().append('{');
//		if (isEmpty()) {
//			return sb.append('}').toString();
//		}
//		final int startIndex = full ? end : end + 1;
//		sb.append(array[startIndex].toString());
//		int count = (startIndex + 1) % array.length;
//		while (array[count] != null) {
//			sb.append(',').append(array[count].toString());
//			count = ++count % array.length;
//		}
//		return sb.append('}').toString();
//	}

}
