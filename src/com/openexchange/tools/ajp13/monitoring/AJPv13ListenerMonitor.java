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

package com.openexchange.tools.ajp13.monitoring;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AJPv13ListenerMonitor implements AJPv13ListenerMonitorMBean {

	private int poolSize;

	private int numActive;

	private int numIdle;

	private int numWaiting;

	private int numProcessing;
	
	private int numRequests;

	private static final int USE_TIME_COUNT = 1000;

	private final long[] avgUseTimeArr;

	private int avgUseTimePointer;

	private long maxUseTime;

	private long minUseTime = Long.MAX_VALUE;

	private final long[] avgProcessingTimeArr;

	private int avgProcessingTimePointer;

	private long maxProcessingTime;

	private long minProcessingTime = Long.MAX_VALUE;

	private final Lock poolSizeLock = new ReentrantLock();

	private final Lock useTimeLock = new ReentrantLock();

	private final Lock processingTimeLock = new ReentrantLock();

	private final Lock numActiveLock = new ReentrantLock();

	private final Lock numIdleLock = new ReentrantLock();

	private final Lock numWaitingLock = new ReentrantLock();

	private final Lock numProcessingLock = new ReentrantLock();
	
	private final Lock numRequestsLock = new ReentrantLock();

	public AJPv13ListenerMonitor() {
		super();
		avgUseTimeArr = new long[USE_TIME_COUNT];
		avgProcessingTimeArr = new long[USE_TIME_COUNT];
	}

	public int getPoolSize() {
		return poolSize;
	}
	
	public void incrementPoolSize() {
		poolSize++;
	}
	
	public void decrementPoolSize() {
		poolSize--;
	}

	public void setPoolSize(final int poolSize) {
		if (poolSizeLock.tryLock()) {
			try {
				this.poolSize = poolSize;
			} finally {
				poolSizeLock.unlock();
			}
		}
	}

	public int getNumActive() {
		return numActive;
	}

	public void incrementNumActive() {
		numActiveLock.lock();
		try {
			numActive++;
		} finally {
			numActiveLock.unlock();
		}
	}

	public void decrementNumActive() {
		numActiveLock.lock();
		try {
			numActive--;
		} finally {
			numActiveLock.unlock();
		}
	}

	public int getNumIdle() {
		return numIdle;
	}

	public void incrementNumIdle() {
		numIdleLock.lock();
		try {
			numIdle++;
		} finally {
			numIdleLock.unlock();
		}
	}

	public void decrementNumIdle() {
		numIdleLock.lock();
		try {
			numIdle--;
		} finally {
			numIdleLock.unlock();
		}
	}

	public void setNumIdle(final int numIdle) {
		numIdleLock.lock();
		try {
			this.numIdle = numIdle;
		} finally {
			numIdleLock.unlock();
		}
	}

	public int getNumWaiting() {
		return numWaiting;
	}

	public void incrementNumWaiting() {
		numWaitingLock.lock();
		try {
			numWaiting++;
		} finally {
			numWaitingLock.unlock();
		}
	}

	public void decrementNumWaiting() {
		numWaitingLock.lock();
		try {
			numWaiting--;
		} finally {
			numWaitingLock.unlock();
		}
	}

	public int getNumProcessing() {
		return numProcessing;
	}

	public void incrementNumProcessing() {
		numProcessingLock.lock();
		try {
			numProcessing++;
		} finally {
			numProcessingLock.unlock();
		}
	}

	public void decrementNumProcessing() {
		numProcessingLock.lock();
		try {
			numProcessing--;
		} finally {
			numProcessingLock.unlock();
		}
	}
	
	public int getNumRequests() {
		return numRequests;
	}
	
	public void incrementNumRequests() {
		numRequestsLock.lock();
		try {
			numRequests++;
		} finally {
			numRequestsLock.unlock();
		}
	}

	public double getAvgUseTime() {
		long duration = 0;
		for (int i = 0; i < avgUseTimeArr.length; i++) {
			duration += avgUseTimeArr[i];
		}
		return (duration / avgUseTimeArr.length);
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

	public void resetMaxUseTime() {
		useTimeLock.lock();
		try {
			this.maxUseTime = 0;
		} finally {
			useTimeLock.unlock();
		}
	}

	/**
	 * Sets the min use time to the minimum of given <code>minUseTime</code>
	 * and existing value
	 */
	public long getMinUseTime() {
		return minUseTime;
	}

	private final void setMinUseTime(final long minUseTime) {
		this.minUseTime = Math.min(minUseTime, this.minUseTime);
	}

	public void resetMinUseTime() {
		useTimeLock.lock();
		try {
			this.minUseTime = Long.MAX_VALUE;
		} finally {
			useTimeLock.unlock();
		}
	}

	public int getNumBrokenConnections() {
		return 0;
	}

	/**
	 * Adds given use time to average use time array and invokes the
	 * setMaxUseTime() and setMinUseTime() methods
	 */
	public void addProcessingTime(final long time) {
		if (processingTimeLock.tryLock()) {
			/*
			 * Add processing time only when lock could be acquired
			 */
			try {
				avgProcessingTimeArr[avgProcessingTimePointer++] = time;
				avgProcessingTimePointer = avgProcessingTimePointer % avgProcessingTimeArr.length;
				setMaxProcessingTime(time);
				setMinProcessingTime(time);
			} finally {
				processingTimeLock.unlock();
			}
		}
	}

	private final void setMaxProcessingTime(final long maxProcessingTime) {
		this.maxProcessingTime = Math.max(this.maxProcessingTime, maxProcessingTime);
	}

	private final void setMinProcessingTime(final long minProcessingTime) {
		this.minProcessingTime = Math.min(this.minProcessingTime, minProcessingTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.ajp13.monitoring.AJPv13ListenerMonitorMBean#getMaxProcessingTime()
	 */
	public long getMaxProcessingTime() {
		return maxProcessingTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.ajp13.monitoring.AJPv13ListenerMonitorMBean#getMinProcessingTime()
	 */
	public long getMinProcessingTime() {
		return minProcessingTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.ajp13.monitoring.AJPv13ListenerMonitorMBean#getAvgProcessingTime()
	 */
	public double getAvgProcessingTime() {
		long duration = 0;
		for (int i = 0; i < avgProcessingTimeArr.length; i++) {
			duration += avgProcessingTimeArr[i];
		}
		return (duration / avgProcessingTimeArr.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.ajp13.monitoring.AJPv13ListenerMonitorMBean#resetMaxProcessingTime()
	 */
	public void resetMaxProcessingTime() {
		this.maxProcessingTime = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.ajp13.monitoring.AJPv13ListenerMonitorMBean#resetMinProcessingTime()
	 */
	public void resetMinProcessingTime() {
		this.minProcessingTime = Long.MAX_VALUE;
	}
}
