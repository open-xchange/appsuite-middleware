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



package com.openexchange.tools.ajp13;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.server.ServerTimer;

/**
 * AJPv13Watcher
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class AJPv13Watcher {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13Watcher.class);

	private static final boolean ENABLED = AJPv13Config.getAJPWatcherEnabled();

	private static final boolean PERMISSION_ENABLED = AJPv13Config.getAJPWatcherPermission();

	private static final int MAX_LISTENER_RUNNING_TIME = AJPv13Config.getAJPWatcherMaxRunningTime();

	private static final int WATCHER_FREQUENCY = AJPv13Config.getAJPWatcherFrequency();

	private static Map<Integer, AJPv13Listener> listeners = new HashMap<Integer, AJPv13Listener>();

	private static final Lock LOCK = new ReentrantLock();

	public static final void addListener(final AJPv13Listener listener) {
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

	public static final AJPv13Listener removeListener(final int listenerNum) {
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

	public static final int getNumOfListeners() {
		return listeners.size();
	}

	/**
	 * A thread-safe method to stop all listeners sequentially and clears them
	 * from map
	 * 
	 */
	public static final void stopListeners() {
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
	private static final void stopAllListeners() {
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
				int countListeningThreads = 0;
				int countProcessingThreads = 0;
				int countExceededThreads = 0;
				final Iterator<AJPv13Listener> iter = listeners.values().iterator();
				final int size = listeners.size();
				for (int i = 0; i < size; i++) {
					final AJPv13Listener l = iter.next();
					if (l.isListening()) {
						countListeningThreads++;
					}
					if (l.isProcessing()) {
						/*
						 * At least one listener is currently processing
						 */
						countProcessingThreads++;
						if ((System.currentTimeMillis() - l.getLastAccessTime()) > MAX_LISTENER_RUNNING_TIME) {
							countExceededThreads++;
						}
					}
				}
				/*
				 * All threads are listening longer than specified max listener
				 * running time
				 */
				if (PERMISSION_ENABLED && countProcessingThreads > 0 && countExceededThreads == countProcessingThreads) {
					final String delimStr = "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
					LOG.error(new StringBuilder(300).append(delimStr).append(
							"AJP-Watcher's run done: SYSTEM DEADLOCK DETECTED!").append(
							" Going to stop and re-initialize system").append(delimStr).toString());
					/*
					 * Restart AJP Server
					 */
					try {
						AJPv13Server.restartAJPServer();
					} catch (AJPv13Exception e) {
						LOG.error(e.getMessage(), e);
					}
				} else {
					if (LOG.isInfoEnabled()) {
						final String delimStr = "\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
						LOG.info(new StringBuilder(300).append(delimStr).append("AJP-Watcher's run done: ").append(
								"    Listening=").append(countListeningThreads).append("    Running=").append(
								countProcessingThreads).append("    Exceeded=").append(countExceededThreads).append(
								"    Total=").append(size).append(delimStr).toString());
					}
				}
			} finally {
				LOCK.unlock();
			}
		}
	}

	static {
		if (ENABLED) {
			/*
			 * Start task
			 */
			ServerTimer.getTimer().schedule(new Task(), 1000, WATCHER_FREQUENCY);
		}
	}
}
