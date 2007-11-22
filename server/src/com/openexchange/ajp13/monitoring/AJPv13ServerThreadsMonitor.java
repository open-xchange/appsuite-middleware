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

package com.openexchange.ajp13.monitoring;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.ajp13.AJPv13Exception;
import com.openexchange.ajp13.AJPv13Server;



public class AJPv13ServerThreadsMonitor implements AJPv13ServerThreadsMonitorMBean {

	private final AtomicInteger numActive = new AtomicInteger();
	
	private final AtomicInteger numIdle = new AtomicInteger();
	
	private static final int USE_TIME_COUNT = 1000;
	
	private final long[] avgUseTimeArr;
	
	private int avgUseTimePointer;
	
	private long maxUseTime;
	
	private long minUseTime = Long.MAX_VALUE; 
	
	private final Lock useTimeLock = new ReentrantLock();
	
	public AJPv13ServerThreadsMonitor() {
		super();
		avgUseTimeArr = new long[USE_TIME_COUNT];
	}
	
	public int getPoolSize() {
		return 0;
	}

	public int getNumActive() {
		return numActive.get();
	}
	
	public void setNumActive(final int numActive) {
		this.numActive.set(numActive);
	}

	public int getNumIdle() {
		return numIdle.get();
	}
	
	public void setNumIdle(final int numIdle) {
		this.numIdle.set(numIdle);
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

	public void stopAndRestartAJPServer() throws AJPv13Exception {
		AJPv13Server.stopAJPServer();
		AJPv13Server.startAJPServer();
	}

}
