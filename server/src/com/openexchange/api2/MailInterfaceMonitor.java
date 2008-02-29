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

package com.openexchange.api2;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.mail.MailAccess;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailInterfaceMonitor implements MailInterfaceMonitorMBean {

	private static final int USE_TIME_COUNT = 1000;

	private final long[] avgUseTimeArr;

	private int avgUseTimePointer;

	private long maxUseTime;

	private long minUseTime = Long.MAX_VALUE;

	private final AtomicInteger numBrokenConnections = new AtomicInteger();

	private final AtomicInteger numTimeoutConnections = new AtomicInteger();

	private final AtomicInteger numSuccessfulLogins = new AtomicInteger();

	private final AtomicInteger numFailedLogins = new AtomicInteger();

	private final Lock useTimeLock = new ReentrantLock();

	private final Map<String, Integer> unsupportedEnc;

	/**
	 * Constructor
	 */
	public MailInterfaceMonitor() {
		super();
		avgUseTimeArr = new long[USE_TIME_COUNT];
		unsupportedEnc = new ConcurrentHashMap<String, Integer>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumActive()
	 */
	public int getNumActive() {
		return MailAccess.getCounter();
	}

	public void changeNumActive(final boolean increment) {
		// Delete this method
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getAvgUseTime()
	 */
	public double getAvgUseTime() {
		long duration = 0;
		for (int i = 0; i < avgUseTimeArr.length; i++) {
			duration += avgUseTimeArr[i];
		}
		return (duration / (double) avgUseTimeArr.length);
	}

	/**
	 * Adds given use time to average use time array and invokes the
	 * setMaxUseTime() and setMinUseTime() methods
	 */
	public void addUseTime(final long time) {
		if (useTimeLock.tryLock()) {
			/*
			 * Add use time only when lock could be acquired
			 */
			try {
				avgUseTimeArr[avgUseTimePointer++] = time;
				avgUseTimePointer = avgUseTimePointer % avgUseTimeArr.length;
				setMaxUseTime(time);
				setMinUseTime(time);
			} finally {
				useTimeLock.unlock();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getMaxUseTime()
	 */
	public long getMaxUseTime() {
		return maxUseTime;
	}

	/**
	 * Sets the max use time to the maximum of given <code>maxUseTime</code>
	 * and existing value
	 */
	private final void setMaxUseTime(final long maxUseTime) {
		this.maxUseTime = Math.max(maxUseTime, this.maxUseTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetMaxUseTime()
	 */
	public void resetMaxUseTime() {
		maxUseTime = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getMinUseTime()
	 */
	public long getMinUseTime() {
		return minUseTime;
	}

	private final void setMinUseTime(final long minUseTime) {
		this.minUseTime = Math.min(minUseTime, this.minUseTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetMinUseTime()
	 */
	public void resetMinUseTime() {
		minUseTime = Long.MAX_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumBrokenConnections()
	 */
	public int getNumBrokenConnections() {
		return numBrokenConnections.get();
	}

	/**
	 * Changes number of broken connections
	 */
	public void changeNumBrokenConnections(final boolean increment) {
		if (increment) {
			numBrokenConnections.incrementAndGet();
		} else {
			numBrokenConnections.decrementAndGet();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumTimeoutConnections()
	 */
	public int getNumTimeoutConnections() {
		return numTimeoutConnections.get();
	}

	/**
	 * Changes number of timed-out connections
	 */
	public void changeNumTimeoutConnections(final boolean increment) {
		if (increment) {
			numTimeoutConnections.incrementAndGet();
		} else {
			numTimeoutConnections.decrementAndGet();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumSuccessfulLogins()
	 */
	public int getNumSuccessfulLogins() {
		return numSuccessfulLogins.get();
	}

	/**
	 * Changes number of successful logins
	 */
	public void changeNumSuccessfulLogins(final boolean increment) {
		if (increment) {
			numSuccessfulLogins.incrementAndGet();
		} else {
			numSuccessfulLogins.decrementAndGet();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumFailedLogins()
	 */
	public int getNumFailedLogins() {
		return numFailedLogins.get();
	}

	/**
	 * Changes number of failes logins
	 */
	public void changeNumFailedLogins(final boolean increment) {
		if (increment) {
			numFailedLogins.incrementAndGet();
		} else {
			numFailedLogins.decrementAndGet();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetNumBrokenConnections()
	 */
	public void resetNumBrokenConnections() {
		numBrokenConnections.set(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetNumTimeoutConnections()
	 */
	public void resetNumTimeoutConnections() {
		numTimeoutConnections.set(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetNumSuccessfulLogins()
	 */
	public void resetNumSuccessfulLogins() {
		numSuccessfulLogins.set(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetNumFailedLogins()
	 */
	public void resetNumFailedLogins() {
		numFailedLogins.set(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getUnsupportedEncodingExceptions()
	 */
	public String getUnsupportedEncodingExceptions() {
		final int size = unsupportedEnc.size();
		if (size == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(100);
		final Iterator<Entry<String, Integer>> iter = unsupportedEnc.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			final Entry<String, Integer> entry = iter.next();
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" times");
		}
		return sb.toString();
	}

	/**
	 * Adds an occurence of an unsupported encoding
	 * 
	 * @param encoding -
	 *            the unsupported encoding
	 */
	public void addUnsupportedEncodingExceptions(final String encoding) {
		final String key = encoding.toLowerCase(Locale.ENGLISH);
		final Integer num = unsupportedEnc.get(key);
		if (null == num) {
			unsupportedEnc.put(key, Integer.valueOf(1));
		} else {
			unsupportedEnc.put(key, Integer.valueOf(num.intValue() + 1));
		}
	}

}
