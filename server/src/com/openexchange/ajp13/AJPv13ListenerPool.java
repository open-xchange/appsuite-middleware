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

package com.openexchange.ajp13;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.openexchange.tools.NonBlockingRWLock;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AJPv13ListenerPool {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13ListenerPool.class);

	private static final int LISTENER_POOL_SIZE = AJPv13Config.getAJPListenerPoolSize();

	/**
	 * ...<br>
	 * A ConcurrentLinkedQueue is an appropriate choice when many threads will
	 * share access to a common collection.<br>
	 * ...
	 */
	private static final Queue<AJPv13Listener> LISTENER_QUEUE = new ConcurrentLinkedQueue<AJPv13Listener>();

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static final AtomicInteger listenerNum = new AtomicInteger();

	private static final NonBlockingRWLock RW_LOCK = new NonBlockingRWLock();

	private AJPv13ListenerPool() {
		super();
	}

	/**
	 * Initializes the pool by putting as many listeners as specified through
	 * init-parameter <code>LISTENER_POOL_SIZE</code>
	 * 
	 */
	public static void initPool() {
		if (!initialized.get()) {
			synchronized (initialized) {
				if (!initialized.get()) {
					AJPv13Server.ajpv13ListenerMonitor.setPoolSize(LISTENER_POOL_SIZE);
					AJPv13Server.ajpv13ListenerMonitor.setNumIdle(LISTENER_POOL_SIZE);
					for (int i = 0; i < LISTENER_POOL_SIZE; i++) {
						final AJPv13Listener l = new AJPv13Listener(listenerNum.incrementAndGet(), true);
						AJPv13Watcher.addListener(l);
						LISTENER_QUEUE.offer(l);
					}
					initialized.set(true);
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append(LISTENER_POOL_SIZE).append(" AJPv13-Listeners created!")
								.toString());
					}
				}
			}
		}
	}

	/**
	 * Resets the pool
	 */
	public static void resetPool() {
		RW_LOCK.acquireWrite();
		try {
			/*
			 * Clear queue one-by-one
			 */
			while (!LISTENER_QUEUE.isEmpty()) {
				try {
					final AJPv13Listener l = LISTENER_QUEUE.poll();
					if (!l.stopListener()) {
						LOG.error(new StringBuilder(128).append("Listener ").append(l.getListenerName()).append(
								" could NOT be properly stopped."));
					}
				} catch (final Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
			initialized.set(false);
		} finally {
			RW_LOCK.releaseWrite();
		}
	}

	/**
	 * Removes the listener from pool whose number equals specified number
	 * 
	 * @param num
	 *            The number of the listener to remove
	 */
	public static void removeListener(final int num) {
		RW_LOCK.acquireWrite();
		try {
			Next: for (final Iterator<AJPv13Listener> iter = LISTENER_QUEUE.iterator(); iter.hasNext();) {
				if (iter.next().getListenerNumber() == num) {
					iter.remove();
					break Next;
				}
			}
		} finally {
			RW_LOCK.releaseWrite();
		}
	}

	public static boolean isInitialized() {
		return initialized.get();
	}

	/**
	 * Fetches a listener from pool if available, otherwise a new listener is
	 * created
	 * 
	 * @return A pooled or newly created listener
	 */
	public static AJPv13Listener getListener() {
		AJPv13Listener retval = null;
		boolean decrement = true;
		int state;
		do {
			state = RW_LOCK.acquireRead();
			retval = LISTENER_QUEUE.poll();
			if (retval == null) {
				/*
				 * All pre-created listeners are running. Create & return a new
				 * listener.
				 */
				retval = createListener();
				decrement = false;
			}

		} while (!RW_LOCK.releaseRead(state));
		if (decrement) {
			AJPv13Server.ajpv13ListenerMonitor.decrementPoolSize();
			AJPv13Server.ajpv13ListenerMonitor.decrementNumIdle();
		}
		return retval;
	}

	/**
	 * Gets a newly created AJP listener
	 * 
	 * @return a new <code>AJPv13Listener</code> instance created in a
	 *         thread-safe manner
	 */
	private static AJPv13Listener createListener() {
		final AJPv13Listener retval = new AJPv13Listener(listenerNum.incrementAndGet());
		AJPv13Watcher.addListener(retval);
		return retval;
	}

	/**
	 * Puts back the given listener into pool if pool is not full, yet.
	 * 
	 * @param listener
	 *            The AJP listener which shall be put back into pool
	 * @return <code>true</code> if given listener can be put into pool,
	 *         <code>false</code> otherwise
	 */
	public static boolean putBack(final AJPv13Listener listener) {
		return putBack(listener, false);
	}

	/**
	 * Puts back the given listener into pool if pool is not full, yet. If
	 * <code>enforcedPut</code> is <code>true</code> the listener is going
	 * to be put in any case.
	 * 
	 * @param listener
	 *            The AJP listener which shall be put back into pool
	 * @param enforcedPut
	 *            <code>true</code> to enforce a put even though pool's size
	 *            is exceeded; otherwise <code>false</code>
	 * @return <code>true</code> if given listener can be put into pool,
	 *         <code>false</code> otherwise
	 */
	public static boolean putBack(final AJPv13Listener listener, final boolean enforcedPut) {
		RW_LOCK.acquireWrite();
		try {
			if (enforcedPut || (LISTENER_QUEUE.size() < LISTENER_POOL_SIZE)) {
				AJPv13Server.ajpv13ListenerMonitor.incrementPoolSize();
				AJPv13Server.ajpv13ListenerMonitor.incrementNumIdle();
				return LISTENER_QUEUE.offer(listener);
			}
			return false;
		} finally {
			RW_LOCK.releaseWrite();
		}
	}

	/**
	 * Gets the current pool size
	 * 
	 * @return The current pool size
	 */
	public static int getPoolSize() {
		return LISTENER_QUEUE.size();
	}

}
