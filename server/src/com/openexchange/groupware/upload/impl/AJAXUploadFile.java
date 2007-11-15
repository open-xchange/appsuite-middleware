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

package com.openexchange.groupware.upload.impl;

import java.io.File;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.groupware.upload.ManagedUploadFile;
import com.openexchange.server.ServerTimer;

/**
 * AJAXUploadFile
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AJAXUploadFile implements ManagedUploadFile {

	private final class AJAXUploadFileTimerTask extends TimerTask {

		private final AJAXUploadFile file;

		private final Map<String, ? extends ManagedUploadFile> ajaxUploadFiles;

		private final String id;

		/**
		 * Constructor
		 */
		public AJAXUploadFileTimerTask(final AJAXUploadFile file, final String id,
				final Map<String, ? extends ManagedUploadFile> ajaxUploadFiles) {
			this.file = file;
			this.ajaxUploadFiles = ajaxUploadFiles;
			this.id = id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
		    try {
			if (file != null && !file.isDeleted() && !file.isBlockedForTimer()
					&& ((System.currentTimeMillis() - file.getLastAccess()) >= IDLE_TIME_MILLIS)) {
				ajaxUploadFiles.remove(id);
				final String fileName = file.getFile().getName();
				file.delete();
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(256).append("Upload file \"").append(fileName).append(
							"\" removed from session and deleted from disk through timer task").toString());
				}
				/*
				 * Cancel this task
				 */
				this.cancel();
				ServerTimer.getTimer().purge();
			}
		    } catch (Exception e) {
		        LOG.error(e.getMessage(), e);
		    }
		}

	}

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJAXUploadFile.class);

	private static final long IDLE_TIME_MILLIS = getIdleTimeMillis();

	private static long getIdleTimeMillis() {
		int idleTimeMillis;
		try {
			idleTimeMillis = ServerConfig.getInteger(ServerConfig.Property.MaxUploadIdleTimeMillis);
		} catch (final ConfigurationException e) {
			LOG.error(new StringBuilder(256).append(
					"Max. upload file idle time millis could not be read, using default ").append(300000).append(
					". Error message: ").append(e.getLocalizedMessage()).toString(), e);
			/*
			 * Default
			 */
			idleTimeMillis = 300000;
		}
		return idleTimeMillis;
	}

	private File file;

	private final AtomicLong lastAccess;

	private boolean deleted;

	private TimerTask timerTask;

	private final AtomicBoolean timerTaskStarted = new AtomicBoolean();

	private final Lock timerTaskLock = new ReentrantLock();

	private final AtomicBoolean blockedForTimer;

	private String fileName;

	private String contentType;

	private long size;

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
		lastAccess = new AtomicLong(initialTimestamp);
		blockedForTimer = new AtomicBoolean();
	}

	/**
	 * @return The file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return The last access timestamp
	 */
	public long getLastAccess() {
		return lastAccess.get();
	}

	/**
	 * Touches this file's last access timestamp
	 */
	public void touch() {
		lastAccess.set(System.currentTimeMillis());
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
	 * Starts the timer task in a thread-safe manner. The second and subsequent
	 * calls have no effect.
	 * 
	 * @param id
	 *            The upload file's ID
	 * @param ajaxUploadFiles
	 *            Session's map where the upload file is kept
	 */
	public void startTimerTask(final String id, final Map<String, ? extends ManagedUploadFile> ajaxUploadFiles) {
		if (!timerTaskStarted.get()) {
			timerTaskLock.lock();
			try {
				if (timerTask == null) {
					timerTask = new AJAXUploadFileTimerTask(this, id, ajaxUploadFiles);
					/*
					 * Start timer task
					 */
					ServerTimer.getTimer().schedule(timerTask, 1000/* 1sec */, IDLE_TIME_MILLIS / 5);
					timerTaskStarted.set(true);
				}
			} finally {
				timerTaskLock.unlock();
			}
		}
	}

	/**
	 * Cancels timer task if already started through
	 * <code>{@link #startTimerTask(String, Map)}</code> method
	 */
	public void cancelTimerTask() {
		/*
		 * Prevent this upload file from being deleted by timer task
		 */
		if (timerTaskStarted.get()) {
			blockedForTimer.set(true);
			timerTask.cancel();
			/*
			 * Clean from timer
			 */
			ServerTimer.getTimer().purge();
		}
	}

	/**
	 * Checks if this upload file should be ignored by timer task
	 * 
	 * @return <code>true</code> if this upload file should be ignored by
	 *         timer task; otherwise <code>false</code>
	 */
	private boolean isBlockedForTimer() {
		return blockedForTimer.get();
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

	/**
	 * Getter for file name
	 * 
	 * @return The file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Setter for file name. Implicitely invokes
	 * <code>{@link UploadEvent#getFileName(String)}</code>.
	 * 
	 * @param fileName
	 *            The file name
	 * @see UploadEvent#getFileName(String)
	 */
	public void setFileName(final String fileName) {
		this.fileName = UploadEvent.getFileName(fileName);
	}

	/**
	 * Getter for content type
	 * 
	 * @return The content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Setter for content type
	 * 
	 * @param contentType
	 *            The content type
	 */
	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Getter for size
	 * 
	 * @return The size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Setter for size
	 * 
	 * @param size
	 *            The size
	 */
	public void setSize(final long size) {
		this.size = size;
	}

}
