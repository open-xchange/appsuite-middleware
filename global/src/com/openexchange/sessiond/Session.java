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

package com.openexchange.sessiond;

import java.util.Date;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.upload.ManagedUploadFile;

/**
 * {@link Session}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface Session {
	
	/**
	 * Gets the context
	 * 
	 * @return The context
	 */
	public Context getContext();

	/**
	 * Gets the local IP address
	 * 
	 * @return The local IP address
	 */
	public String getLocalIp();

	/**
	 * Gets the login name
	 * 
	 * @return The login name
	 */
	public String getLoginName();

	/**
	 * Gets the parameter bound to specified name or <code>null</code> if no
	 * such parameter is present
	 * 
	 * @param name
	 *            The parameter name
	 * @return The parameter or <code>null</code>
	 */
	public Object getParameter(String name);

	/**
	 * Gets the password
	 * 
	 * @return The password
	 */
	public String getPassword();
	
	/**
	 * Gets the random token
	 * 
	 * @return The random token
	 */
	public String getRandomToken();

	/**
	 * Gets the secret
	 * 
	 * @return
	 */
	public String getSecret();

	/**
	 * Gets the session ID
	 * 
	 * @return The session ID
	 */
	public String getSessionID();

	/**
	 * Gets the uploaded file associated with given ID and set its last access
	 * timestamp to current time in milliseconds.
	 * 
	 * @param id
	 *            The id
	 * @return The uploaded file associated with given ID or <code>null</code>
	 *         if none found
	 */
	public ManagedUploadFile getUploadedFile(String id);

	/**
	 * Gets the user ID
	 * 
	 * @return The user ID
	 */
	public int getUserId();

	/**
	 * Gets the user login
	 * 
	 * @return The user login
	 */
	public String getUserlogin();

	/**
	 * Puts the uploaded file with ID as key and starts timer
	 * 
	 * @param id
	 *            The ID (must not be <code>null</code>)
	 * @param uploadFile
	 *            The upload file (must not be <code>null</code>)
	 */
	public void putUploadedFile(String id, ManagedUploadFile uploadFile);

	/**
	 * Removes the uploaded file associated with given ID and stops timer task
	 * 
	 * @param id
	 *            The ID
	 * @return The removed uploaded file or <code>null</code> if none removed
	 */
	public ManagedUploadFile removeUploadedFile(String id);

	/**
	 * Removes a formerly uploaded file from session <b>without</b> stopping
	 * timer task. This method is usually invoked by the timer task itself.
	 * 
	 * @param id
	 *            The uploaded file's ID
	 */
	public void removeUploadedFileOnly(String id);

	/**
	 * Sets the parameter. Any existing parameters bound to specified name are
	 * replaced with given value.
	 * 
	 * @param name
	 *            The parameter name
	 * @param value
	 *            The parameter value
	 */
	public void setParameter(String name, Object value);

	/**
	 * Touches the uploaded file associated with given ID; meaning to set its
	 * last access timestamp to current time millis
	 * 
	 * @param id
	 *            The id
	 * @return <code>true</code> if a matching upload file has been found and
	 *         successfully touched; otherwise <code>false</code>
	 */
	public boolean touchUploadedFile(String id);
	
	public void removeRandomToken();
}
