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
package com.openexchange.admin.jobs;

import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author choeger
 *
 */
public class AdminJob extends FutureTask<Vector> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Mode {
		MOVE_DATABASE,
		MOVE_FILESTORE
	}

	public static final int WAITING = 0;
	public static final int RUNNING = 1;
	public static final int DONE    = 2;
    public static final int FAILED  = 3;

    private Log log = LogFactory.getLog( this.getClass() );
	private int m_destination = -1;
	private int m_context = -1;
	private int m_reason = -1;
	private Integer state = WAITING;
	// Stores a the mode
	private Mode m_mode = null;
	private Callable<Vector> cbobj;
	
	public AdminJob(Callable<Vector> callable) {
		super(callable);
		this.cbobj = callable;
	}

	/**
	 * set's this.state to FAILED, if job did fail
	 */
	private void setState() {
		try {
			Vector retVec = this.get();
			if( retVec.get(0).equals("ERROR") ) {
				synchronized( this.state ) {
					this.state = FAILED;
				}
			}
		} catch (InterruptedException e) {
			log.error(e);
			synchronized( this.state ) {
				this.state = FAILED;
			}
		} catch (ExecutionException e) {
			log.error(e);
			synchronized( this.state ) {
				this.state = FAILED;
			}
		}
	}

	public boolean isDone() {
		setState();
		synchronized( this.state ) {
			return this.state == DONE;
		}
	}
	
	public boolean isFailed() {
		setState();
		synchronized( this.state ) {
			return this.state == FAILED;
		}
	}

	public boolean isRunning() {
		synchronized( this.state ) {
			return this.state == RUNNING;
		}
	}

	public int getPercentDone() {
		if( this.cbobj instanceof I_AdminProgressEnabledJob ) {
			return ((I_AdminProgressEnabledJob)this.cbobj).getPercentDone();
		}
		return 0;
	}
	
	@Override
	public void run() {
		synchronized( this.state ) {
			this.state = RUNNING;
		}
		super.run();
		synchronized( this.state ) {
			this.state = DONE;
		}
	}
	
	public int getDestination() {
		return m_destination;
	}

	public void setDestination(int destination) {
		this.m_destination = destination;
	}

	public int getContext() {
		return m_context;
	}

	public void setContext(int context) {
		m_context = context;
	}

	public Mode getMode() {
		return m_mode;
	}

	public void setMode(Mode mode) {
		m_mode = mode;
	}

	public int getReason() {
		return m_reason;
	}

	public void setReason(int reason) {
		m_reason = reason;
	}

}
