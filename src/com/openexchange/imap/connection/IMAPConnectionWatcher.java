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

package com.openexchange.imap.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.MessagingException;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.server.ServerTimer;

/**
 * IMAPConnectionWatcher
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPConnectionWatcher {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPConnectionWatcher.class);

	private static final Map<IMAPConnection, Long> imapCons = new HashMap<IMAPConnection, Long>();

	private static final Lock LOCK = new ReentrantLock();

	/**
	 * Prevent instantiation
	 */
	private IMAPConnectionWatcher() {
		super();
	}

	public static void addIMAPConnection(final IMAPConnection imapCon) {
		LOCK.lock();
		try {
			imapCons.put(imapCon, Long.valueOf(System.currentTimeMillis()));
		} finally {
			LOCK.unlock();
		}
	}

	public static void removeIMAPConnection(final IMAPConnection imapCon) {
		LOCK.lock();
		try {
			imapCons.remove(imapCon);
		} finally {
			LOCK.unlock();
		}
	}

	private static final String INFO_PREFIX = "UNCLOSED IMAP CONNECTION AFTER #N#msec:\n";

	private static final String INFO_PREFIX2 = "CLOSING IMAP CONNECTION BY WATCHER:\n";
	
	private static final String INFO_PREFIX3 = "\n\tDONE";

	private static class IMAPConnectionWatcherTask extends TimerTask {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (!LOG.isInfoEnabled()) {
				/*
				 * Not allowed to log
				 */
				return;
			}
			LOCK.lock();
			try {
				final StringBuilder sb = new StringBuilder(512);
				final Iterator<Entry<IMAPConnection, Long>> iter = imapCons.entrySet().iterator();
				final List<IMAPConnection> exceededCons = new ArrayList<IMAPConnection>();
				{
					final int n = imapCons.size();
					for (int i = 0; i < n; i++) {
						final Entry<IMAPConnection, Long> e = iter.next();
						if ((System.currentTimeMillis() - e.getValue().longValue()) > IMAPProperties
								.getWatcherTime()) {
							sb.setLength(0);
							LOG.info(sb.append(
									INFO_PREFIX
											.replaceFirst("#N#", String.valueOf(IMAPProperties.getWatcherTime())))
									.append(e.getKey().getTrace()).toString());
							exceededCons.add(e.getKey());
						}
					}
				}
				if (!exceededCons.isEmpty()) {
					/*
					 * Remove/Close exceeded connections
					 */
					final boolean closeAllowed = IMAPProperties.isWatcherShallClose();
					{
						final int n = exceededCons.size();
						for (int i = 0; i < n; i++) {
							final IMAPConnection imapCon = exceededCons.get(i);
							boolean remove = true;
							try {
								if (closeAllowed) {
									sb.setLength(0);
									sb.append(INFO_PREFIX2).append(imapCon.toString());
									if (imapCon instanceof DefaultIMAPConnection) {
										MailInterfaceImpl.closeIMAPConnection((DefaultIMAPConnection) imapCon);
										remove = false;
									} else {
										imapCon.close();
									}
									sb.append(INFO_PREFIX3);
									LOG.info(sb.toString());
								}
							} catch (final MessagingException e1) {
								LOG.error(e1.getLocalizedMessage(), e1);
							} finally {
								if (remove) {
									imapCons.remove(imapCon);
								}
							}
						}
					}
				}
			} catch (final IMAPException e) {
				LOG.error(e.getLocalizedMessage(), e);
			} finally {
				LOCK.unlock();
			}
		}
	}

	static {
		try {
			if (IMAPProperties.isWatcherEnabled()) {
				/*
				 * Start task
				 */
				ServerTimer.getTimer().schedule(new IMAPConnectionWatcherTask(), 1000,
						IMAPProperties.getWatcherFrequency());
			}
		} catch (final IMAPException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
