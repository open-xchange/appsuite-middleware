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

package com.openexchange.mail.watcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.mail.MailConnection;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.server.ServerTimer;

/**
 * {@link MailConnectionWatcher} - Keeps track of established mail connections
 * and allows a forced close if connection time exceeds allowed time
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailConnectionWatcher {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailConnectionWatcher.class);

	private static final ConcurrentMap<MailConnection, Long> mailConnections = new ConcurrentHashMap<MailConnection, Long>();

	private static final Lock LOCK = new ReentrantLock();

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static WatcherTask watcherTask;

	/**
	 * Initializes and starts mail connection watcher if not done, yet
	 */
	public static void init() {
		if (!initialized.get()) {
			LOCK.lock();
			try {
				if (initialized.get()) {
					return;
				}
				if (MailConfig.isWatcherEnabled()) {
					/*
					 * Start task
					 */
					watcherTask = new WatcherTask();
					ServerTimer.getTimer().schedule(watcherTask, 1000, MailConfig.getWatcherFrequency());
					initialized.set(true);
					if (LOG.isInfoEnabled()) {
						LOG.info("Mail connection watcher successfully established and ready for tracing");
					}
				}
			} finally {
				LOCK.unlock();
			}
		}
	}

	/**
	 * Stops mail connection watcher if currently running
	 */
	public static void stop() {
		if (initialized.get()) {
			LOCK.lock();
			try {
				if (!initialized.get()) {
					return;
				}
				if (MailConfig.isWatcherEnabled()) {
					watcherTask.cancel();
					ServerTimer.getTimer().purge();
					mailConnections.clear();
					initialized.set(false);
					if (LOG.isInfoEnabled()) {
						LOG.info("Mail connection watcher successfully stopped");
					}
				}
			} finally {
				LOCK.unlock();
			}
		}
	}

	/**
	 * Prevent instantiation
	 */
	private MailConnectionWatcher() {
		super();
	}

	/**
	 * Adds specified mail connection to this watcher's tracing if not already
	 * added before
	 * <p>
	 * Watcher is established if not running, yet
	 * 
	 * @param mailConnection
	 *            The mail connection to add
	 */
	public static void addMailConnection(final MailConnection mailConnection) {
		if (!initialized.get()) {
			LOG.error("Mail connection watcher is not running. Aborting addMailConnection()");
			return;
		}
		if (!mailConnections.containsKey(mailConnection)) {
			mailConnections.put(mailConnection, Long.valueOf(System.currentTimeMillis()));
		}
	}

	/**
	 * Removes specified mail connection from this watcher's tracing
	 * 
	 * @param mailConnection
	 *            The mail connection to remove
	 */
	public static void removeMailConnection(final MailConnection mailConnection) {
		if (!initialized.get()) {
			LOG.error("Mail connection watcher is not running. Aborting removeMailConnection()");
			return;
		}
		mailConnections.remove(mailConnection);
	}

	private static final String INFO_PREFIX = "UNCLOSED MAIL CONNECTION AFTER #N#msec:\n";

	private static final String INFO_PREFIX2 = "CLOSING MAIL CONNECTION BY WATCHER:\n";

	private static final String INFO_PREFIX3 = "\n\tDONE";

	private static class WatcherTask extends TimerTask {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final StringBuilder sb = new StringBuilder(512);
			final List<MailConnection> exceededCons = new ArrayList<MailConnection>();
			for (final Iterator<Entry<MailConnection, Long>> iter = mailConnections.entrySet().iterator(); iter
					.hasNext();) {
				final Entry<MailConnection, Long> e = iter.next();
				if (!e.getKey().isConnectedUnsafe()) {
					/*
					 * Remove closed connection from watcher
					 */
					iter.remove();
				} else {
					if ((System.currentTimeMillis() - e.getValue().longValue()) > MailConfig.getWatcherTime()) {
						sb.setLength(0);
						LOG.info(sb
								.append(INFO_PREFIX.replaceFirst("#N#", String.valueOf(MailConfig.getWatcherTime())))
								.append(e.getKey().getTrace()).toString());
						exceededCons.add(e.getKey());
					}
				}
			}
			if (!exceededCons.isEmpty()) {
				/*
				 * Remove/Close exceeded connections
				 */
				final int n = exceededCons.size();
				for (int i = 0; i < n; i++) {
					final MailConnection mailConnection = exceededCons.get(i);
					try {
						if (MailConfig.isWatcherShallClose()) {
							sb.setLength(0);
							sb.append(INFO_PREFIX2).append(mailConnection.toString());
							mailConnection.close(false);
							sb.append(INFO_PREFIX3);
							LOG.info(sb.toString());
						}
					} finally {
						mailConnections.remove(mailConnection);
					}
				}
			}
		}
	}

}
