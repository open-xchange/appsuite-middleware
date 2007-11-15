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
import java.util.Map;

import com.openexchange.sessiond.Session;

/**
 * {@link ManagedUploadFile} - Represents an uploaded file whose lifecycle is
 * managed by either a {@link Session} instance or by a timer task.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface ManagedUploadFile {

	/**
	 * Gets the uploaded file
	 * 
	 * @return The uploaded file
	 */
	public File getFile();

	/**
	 * Gets last access timestamp
	 * 
	 * @return The last access timestamp
	 */
	public long getLastAccess();

	/**
	 * Touches this file's last access timestamp
	 */
	public void touch();

	/**
	 * Removes uploaded file from disk
	 */
	public void delete();

	/**
	 * Starts the timer task in a thread-safe manner. The second and subsequent
	 * calls have no effect.
	 * 
	 * @param id
	 *            The upload file's ID
	 * @param fileMap
	 *            The map reference for future file removals through timer task
	 */
	public void startTimerTask(String id, Map<String, ? extends ManagedUploadFile> fileMap);

	/**
	 * Cancels timer task if already started through
	 * <code>{@link #startTimerTask(String, Map)}</code> method
	 */
	public void cancelTimerTask();

	/**
	 * Checks if this upload file has been previously deleted by timer task
	 * 
	 * @return <code>true</code> if this upload file has been previously
	 *         deleted by timer task; otherwise <code>false</code>
	 */
	public boolean isDeleted();

	/**
	 * Getter for file name
	 * 
	 * @return The file name
	 */
	public String getFileName();

	/**
	 * Setter for file name. Implicitly invokes
	 * <code>{@link UploadEvent#getFileName(String)}</code>.
	 * 
	 * @param fileName
	 *            The file name
	 * @see UploadEvent#getFileName(String)
	 */
	public void setFileName(String fileName);

	/**
	 * Getter for content type
	 * 
	 * @return The content type
	 */
	public String getContentType();

	/**
	 * Setter for content type
	 * 
	 * @param contentType
	 *            The content type
	 */
	public void setContentType(final String contentType);

	/**
	 * Getter for size
	 * 
	 * @return The size
	 */
	public long getSize();

	/**
	 * Setter for size
	 * 
	 * @param size
	 *            The size
	 */
	public void setSize(final long size);
}
