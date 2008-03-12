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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.server.ServerTimer;

/**
 * {@link AJPv13Watcher} - A watcher for AJP listeners which keeps track of
 * their run time.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AJPv13Watcher {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13Watcher.class);

	private static Task task;

	private static final Map<Integer, AJPv13Listener> listeners = new HashMap<Integer, AJPv13Listener>();

	private static final Lock LOCK = new ReentrantLock();

	static void initializeAJPv13Watcher() {
		if (AJPv13Config.getAJPWatcherEnabled()) {
			/*
			 * Start task
			 */
			ServerTimer.getTimer().schedule((task = new Task()), 1000, AJPv13Config.getAJPWatcherFrequency());
		}
	}

	static void resetAJPv13Watcher() {
		if (null != task) {
			task.cancel();
			task = null;
			ServerTimer.getTimer().purge();
		}
	}

	public static void addListener(final AJPv13Listener listener) {
		LOCK.lock();
		try {
			if (listeners.containsKey(Integer.valueOf(listener.getListenerNumber()))) {
				return;
			}
			listeners.put(Integer.valueOf(listener.getListenerNumber()), listener);
		} finally {
			LOCK.unlock();
		}
	}

	public static AJPv13Listener removeListener(final int listenerNum) {
		LOCK.lock();
		try {
			if (listeners.containsKey(Integer.valueOf(listenerNum))) {
				return listeners.remove(Integer.valueOf(listenerNum));
			}
			return null;
		} finally {
			LOCK.unlock();
		}
	}

	public static int getNumOfListeners() {
		return listeners.size();
	}

	/**
	 * A thread-safe method to stop all listeners sequentially and clears them
	 * from map
	 * 
	 */
	public static void stopListeners() {
		LOCK.lock();
		try {
			stopAllListeners();
		} finally {
			LOCK.unlock();
		}
	}

	/**
	 * Stops all listeners sequentially and clears them from map
	 * 
	 */
	private static void stopAllListeners() {
		final Iterator<AJPv13Listener> iter = listeners.values().iterator();
		final int size = listeners.size();
		for (int i = 0; i < size; i++) {
			final AJPv13Listener l = iter.next();
			l.stopListener();
		}
		listeners.clear();
	}

	private static class Task extends TimerTask {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			LOCK.lock();
			try {
				int countWaiting = 0;
				int countProcessing = 0;
				int countExceeded = 0;
				final Iterator<AJPv13Listener> iter = listeners.values().iterator();
				final int size = listeners.size();
				for (int i = 0; i < size; i++) {
					final AJPv13Listener l = iter.next();
					if (l.isWaitingOnAJPSocket()) {
						countWaiting++;
					}
					if (l.isProcessing()) {
						/*
						 * At least one listener is currently processing
						 */
						countProcessing++;
						final long currentProcTime = (System.currentTimeMillis() - l.getProcessingStartTime());
						if (currentProcTime > AJPv13Config.getAJPWatcherMaxRunningTime()) {
							if (LOG.isInfoEnabled()) {
								final Throwable t = new Throwable();
								t.setStackTrace(l.getStackTrace());
								LOG.info(new StringBuilder("AJP Listener exceeds max. running time of ").append(
										AJPv13Config.getAJPWatcherMaxRunningTime()).append("msec -> Processing time: ").append(
										currentProcTime).append("msec").toString(), t);
							}
							countExceeded++;
						}
					}
				}
				/*
				 * All threads are listening longer than specified max listener
				 * running time
				 */
				if (AJPv13Config.getAJPWatcherPermission() && countProcessing > 0 && countExceeded == countProcessing) {
					final String delimStr = "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
					LOG.error(new StringBuilder(300).append(delimStr).append(
							"AJP-Watcher's run done: SYSTEM DEADLOCK DETECTED!").append(
							" Going to stop and re-initialize system").append(delimStr).toString());
					/*
					 * Restart AJP Server
					 */
					try {
						AJPv13Server.restartAJPServer();
					} catch (final AJPv13Exception e) {
						LOG.error(e.getMessage(), e);
					}
				} else {
					if (LOG.isTraceEnabled()) {
						final String delimStr = "\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
						LOG.trace(new StringBuilder(300).append(delimStr).append("AJP-Watcher's run done: ").append(
								"    Waiting=").append(countWaiting).append("    Running=").append(countProcessing)
								.append("    Exceeded=").append(countExceeded).append("    Total=").append(size)
								.append(delimStr).toString());
					}
				}
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
			} finally {
				LOCK.unlock();
			}
		}
	}

}
