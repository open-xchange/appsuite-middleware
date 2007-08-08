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

package com.openexchange.groupware.upload;

import java.io.File;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AJAXUploadFile
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AJAXUploadFile {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJAXUploadFile.class);

	private File file;

	private long lastAccess;

	private boolean deleted;

	private TimerTask timerTask;

	private final Lock lock = new ReentrantLock();

	private boolean blockedForTimer;

	/**
	 * Constructor
	 * 
	 * @param file
	 *            The file
	 * @param initialTimestamp
	 *            The file's timestamp
	 */
	public AJAXUploadFile(final File file, final long initialTimestamp) {
		this.file = file;
		this.lastAccess = initialTimestamp;
	}

	/**
	 * @return The file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return The timestamp
	 */
	public long getLastAccess() {
		return lastAccess;
	}

	/**
	 * Touches this file's last access timestamp
	 */
	public void touch() {
		lastAccess = System.currentTimeMillis();
	}

	/**
	 * Removes uploaded file from disk
	 */
	public void delete() {
		try {
			if (!file.delete()) {
				LOG.warn(new StringBuilder(128).append("Uploaded file \"").append(file.getName()).append(
						"\" could not be deleted").toString());
			}
		} catch (final Throwable t) {
			LOG.error(new StringBuilder(128).append("Uploaded file \"").append(file.getName()).append(
					"\" could not be deleted").toString(), t);
		} finally {
			file = null;
			deleted = true;
		}
	}

	/**
	 * Setter for time task
	 * 
	 * @param timerTask
	 *            The time task
	 */
	public void setTimerTask(final TimerTask timerTask) {
		this.timerTask = timerTask;
	}

	/**
	 * Getter for timer task
	 * 
	 * @return The time task
	 */
	public TimerTask getTimerTask() {
		return timerTask;
	}

	/**
	 * Checks if this upload file should be ignored by timer task
	 * 
	 * @return <code>true</code> if this upload file should be ignored by
	 *         timer task; otherwise <code>false</code>
	 */
	public boolean isBlockedForTimer() {
		lock.lock();
		try {
			return blockedForTimer;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Blocks this upload file for timer task; meaning it is ignored when timer
	 * runs
	 * 
	 * @param blockedForTimer
	 *            <code>true</code> to blocks this upload file for timer task;
	 *            otherwise <code>false</code>
	 */
	public void setBlockedForTimer(final boolean blockedForTimer) {
		lock.lock();
		try {
			this.blockedForTimer = blockedForTimer;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Checks if this upload file has been previously deleted by timer task
	 * 
	 * @return <code>true</code> if this upload file has been previously
	 *         deleted by timer task; otherwise <code>false</code>
	 */
	public boolean isDeleted() {
		return deleted;
	}

}
