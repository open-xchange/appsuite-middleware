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

import java.util.Properties;

import com.sun.mail.imap.IMAPStore;

/**
 * <p>
 * IMAPConnection Interface for handling imap connections.
 * <p>
 * <b>NOTE:</b> The APIs unique to this class should be considered
 * EXPERIMENTAL. They may be changed in the future in ways that are incompatible
 * with applications using the current APIs.
 * 
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */

public interface IMAPConnection {

	/**
	 * Sets IMAP server and port
	 * 
	 * @param imapServer -
	 *            the IMAP server
	 * @param imapPort -
	 *            the IMAP port
	 */
	public void setImapServer(String imapServer, int imapPort);

	/**
	 * Sets the user name
	 * 
	 * @param imapUsername -
	 *            the user name
	 */
	public void setUsername(String imapUsername);

	/**
	 * Sets the password
	 * 
	 * @param imapPassword -
	 *            the password
	 */
	public void setPassword(String imapPassword);

	/**
	 * Sets mail properties
	 * 
	 * @param imapProperties
	 */
	public void setProperties(Properties imapProperties);

	/**
	 * Establishes this connection
	 * 
	 * @throws javax.mail.NoSuchProviderException
	 * @throws javax.mail.MessagingException
	 */
	public void connect() throws javax.mail.NoSuchProviderException, javax.mail.MessagingException;

	/**
	 * Getter
	 * 
	 * @return an instance of <code>IMAPStore</code> if conencted; otherwise
	 *         <code>null</code>
	 */
	public IMAPStore getIMAPStore();

	/**
	 * Closes the connection
	 * 
	 * @throws javax.mail.MessagingException
	 */
	public void close() throws javax.mail.MessagingException;

	/**
	 * Performs a safe check if it is connected
	 * 
	 * @return <code>true</code> if connected; otherwise <code>false</code>
	 * @throws javax.mail.MessagingException
	 */
	public boolean isConnected() throws javax.mail.MessagingException;

	/**
	 * Performs an <b>unsafe</b> check if it is connected. This is usually
	 * usefull if you want to check for inconnectivity and do not want the
	 * in-deep check of corresponding {@link #isConnected()} method
	 * 
	 * @see #isConnected() to check in a safe manner
	 * 
	 * @return <code>true</code> if connected; otherwise <code>false</code>
	 */
	public boolean isConnectedUnsafe();
	
	/**
	 * @return the trace of this IMAP connection
	 */
	public String getTrace();

}
