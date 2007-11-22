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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class AJPv13ListenerPool {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13ListenerPool.class);

	private static final int LISTENER_POOL_SIZE = AJPv13Config.getAJPListenerPoolSize();

	/**
	 * ...<br>A ConcurrentLinkedQueue is an appropriate choice when many threads will
	 * share access to a common collection.<br>...
	 */
	private static final Queue<AJPv13Listener> LISTENER_QUEUE = new ConcurrentLinkedQueue<AJPv13Listener>();

	private static final AtomicBoolean initialized = new AtomicBoolean();
	
	private static int listenerNum;
	
	private static boolean captureLock;
	
	private static final Lock CREATE_LOCK = new ReentrantLock();
	
	private static final Lock WAIT_LOCK = new ReentrantLock();
	
	private static final Condition RESET_FINISHED = WAIT_LOCK.newCondition();
	
	private AJPv13ListenerPool() {
		super();
	}
	
	/**
	 * Initializes the pool by putting as many listeners as specified through
	 * init-parameter <code>LISTENER_POOL_SIZE</code>
	 * 
	 */
	public static void initPool() {
		AJPv13Server.ajpv13ListenerMonitor.setPoolSize(LISTENER_POOL_SIZE);
		AJPv13Server.ajpv13ListenerMonitor.setNumIdle(LISTENER_POOL_SIZE);
		for (int i = 0; i < LISTENER_POOL_SIZE; i++) {
			final AJPv13Listener l = new AJPv13Listener(++listenerNum, true);
			AJPv13Watcher.addListener(l);
			LISTENER_QUEUE.offer(l);
		}
		initialized.set(true);
		if (LOG.isInfoEnabled()) {
			LOG.info(new StringBuilder(100).append(LISTENER_POOL_SIZE).append(" AJPv13-Listeners created!").toString());
		}
	}
	
	public static void resetPool() {
		WAIT_LOCK.lock();
		try {
			captureLock = true;
			/*
			 * Clear queue
			 */
			while (!LISTENER_QUEUE.isEmpty()) {
				try {
					final AJPv13Listener l = LISTENER_QUEUE.poll();
					if (!l.stopListener()) {
						LOG.error(new StringBuilder(100).append("Listener ").append(l.getListenerName()).append(
								" could NOT be properly stopped."));
					}
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
			RESET_FINISHED.signalAll();
		} finally {
			captureLock = false;
			WAIT_LOCK.unlock();
		}
	}
	
	public static void removeListener(final int num) {
		WAIT_LOCK.lock();
		try {
			captureLock = true;
			final int size = LISTENER_QUEUE.size();
			final Iterator<AJPv13Listener> iter = LISTENER_QUEUE.iterator();
			NextListener: for (int i = 0; i < size; i++) {
				if (iter.next().getListenerNumber() == num) {
					iter.remove();
					break NextListener;
				}
			}
		} finally {
			captureLock = false;
			WAIT_LOCK.unlock();
		}
	}
	
	public static boolean isInitialized() {
		return initialized.get();
	}

	/**
	 * Fetches a listener from pool if available, otherwise a new listener will
	 * be created
	 * 
	 * @return a pooled or new listener
	 */
	public static AJPv13Listener getListener() {
		if (LISTENER_QUEUE.isEmpty()) {
			/*
			 * Empty Queue: All pre-created listeners are running. Create &
			 * return a new listener.
			 */
			return createListener();
		}
		if (captureLock) {
			/*
			 * Either resetPool() or invocation of removeListener(int num) set
			 * the captureLock flag. All Threads which want to obtain a listener
			 * ought to wait for these methods to terminate that is when the
			 * lock WAIT_LOCK is unlocked by either method.
			 */
			WAIT_LOCK.lock();
			try {
				/*
				 * Ensure flag is set to false and either method has finished
				 */
				while (captureLock) {
					RESET_FINISHED.await();
				}
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
			} finally {
				WAIT_LOCK.unlock();
			}
		}
		/*
		 * Return a listener fetched from pool if still available. No lock
		 * around poll() method cause this method is already thread-safe based
		 * on "an efficient wait-free algorithm"
		 */
		final AJPv13Listener retval = LISTENER_QUEUE.poll();
		if (retval == null) {
			return createListener();
		}
		AJPv13Server.ajpv13ListenerMonitor.decrementPoolSize();
		AJPv13Server.ajpv13ListenerMonitor.decrementNumIdle();
		return retval;
	}
	
	/**
	 * 
	 * @return a new <code>AJPv13Listener</code> instance created in a
	 *         thread-safe manner
	 */
	private static final AJPv13Listener createListener() {
		CREATE_LOCK.lock();
		try {
			final AJPv13Listener retval = new AJPv13Listener(++listenerNum);
			AJPv13Watcher.addListener(retval);
			return retval;
		} finally {
			CREATE_LOCK.unlock();
		}
	}

	/**
	 * Puts back the given listener into pool if pool is not full, yet.
	 * 
	 * @param listener
	 * @return <code>true</code> if given listener can be put into pool,
	 *         <code>false</code> otherwise
	 */
	public static boolean putBack(final AJPv13Listener listener) {
		return putBack(listener, false);
	}
	
	/**
	 * Puts back the given listener into pool if pool is not full, yet.
	 * If <code>enforcedPut</code> is <code>true</code> the listener
	 * will be put into a secondary queue.
	 * 
	 * @param listener
	 * @param enforcedPut
	 * @return <code>true</code> if given listener can be put into pool,
	 *         <code>false</code> otherwise
	 */
	public static boolean putBack(final AJPv13Listener listener, final boolean enforcedPut) {
		if (enforcedPut || LISTENER_QUEUE.size() < LISTENER_POOL_SIZE) {
			AJPv13Server.ajpv13ListenerMonitor.incrementPoolSize();
			return LISTENER_QUEUE.offer(listener);
		}
		return false;
	}
	
	public static final int getPoolSize() {
		return LISTENER_QUEUE.size();
	}

}
