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

package com.openexchange.mail;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * MailConnection
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailConnection<T extends MailFolderStorage, E extends MailMessageStorage> {

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private Properties mailProperties;

	private String mailServer;

	private int mailServerPort = -1;

	private String login;

	private String password;

	/**
	 * Prevent instantiation
	 */
	protected MailConnection() {
		super();
	}

	/**
	 * Resets this connection's settings
	 */
	protected void reset() {
		mailProperties = null;
		mailServer = null;
		mailServerPort = -1;
		login = null;
		password = null;
	}

	public static final MailConnection getInstance() {

		return null;
	}

	/**
	 * @return the global connection counter
	 */
	public static final int getCounter() {
		return COUNTER.get();
	}

	/**
	 * Increments the global connection counter
	 */
	protected static final void incrementCounter() {
		COUNTER.incrementAndGet();
	}

	/**
	 * Decrements the global connection counter
	 */
	protected static final void decrementCounter() {
		COUNTER.decrementAndGet();
	}

	/**
	 * Gets the login
	 * 
	 * @return the login
	 */
	public final String getLogin() {
		return login;
	}

	/**
	 * Sets the login
	 * 
	 * @param login
	 *            the login to set
	 */
	public final void setLogin(final String login) {
		this.login = login;
	}

	/**
	 * Gets the mailProperties
	 * 
	 * @return the mailProperties
	 */
	public final Properties getMailProperties() {
		return mailProperties;
	}

	/**
	 * Sets the mailProperties
	 * 
	 * @param mailProperties
	 *            the mailProperties to set
	 */
	public final void setMailProperties(final Properties mailProperties) {
		this.mailProperties = mailProperties;
	}

	/**
	 * Gets the mailServer
	 * 
	 * @return the mailServer
	 */
	public final String getMailServer() {
		return mailServer;
	}

	/**
	 * Sets the mailServer
	 * 
	 * @param mailServer
	 *            the mailServer to set
	 */
	public final void setMailServer(final String mailServer) {
		this.mailServer = mailServer;
	}

	/**
	 * Gets the mailServerPort
	 * 
	 * @return the mailServerPort
	 */
	public final int getMailServerPort() {
		return mailServerPort;
	}

	/**
	 * Sets the mailServerPort
	 * 
	 * @param mailServerPort
	 *            the mailServerPort to set
	 */
	public final void setMailServerPort(final int mailServerPort) {
		this.mailServerPort = mailServerPort;
	}

	/**
	 * Gets the password
	 * 
	 * @return the password
	 */
	public final String getPassword() {
		return password;
	}

	/**
	 * Sets the password
	 * 
	 * @param password
	 *            the password to set
	 */
	public final void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Checks if all necessary fields are set in this connection object
	 * <p>
	 * This routine is implicitely invoked by {@link #connect()}
	 * 
	 * @throws MailException
	 *             If a necessary field is missing
	 * @see #connect()
	 */
	protected final void checkFieldsBeforeConnect() throws MailException {
		if (getMailProperties() == null) {
			throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "mail properties");
		} else if (getMailServer() == null) {
			throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "mail server");
		} else if (getMailServerPort() == -1) {
			throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "mail server port");
		} else if (getLogin() == null) {
			throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "login");
		} else if (getPassword() == null) {
			throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "password");
		}
	}

	/**
	 * Opens this connection
	 * 
	 * @throws MailException
	 *             If the connection could not be established for variuos
	 *             reasons
	 */
	public final void connect() throws MailException {
		checkFieldsBeforeConnect();
		connectInternal();
	}

	/**
	 * Internal connect method to establish a mail connection
	 * 
	 * @throws MailException
	 *             If connection could not be established
	 */
	protected abstract void connectInternal() throws MailException;

	/**
	 * Closes this connection
	 */
	public abstract void close();

	/**
	 * Gets the appropiate {@link MailFolderStorage} implementation that is
	 * considered as the main entry point to a user's mailbox
	 * @return The appropiate {@link MailFolderStorage} implementation
	 * @throws MailException
	 *             If connection is not established, yet
	 */
	public abstract T getFolderStorage() throws MailException;

	/**
	 * Gets the appropiate {@link MailMessageStorage} implementation that
	 * provides necessary message-related operations/methods
	 * 
	 * @return The appropiate {@link MailMessageStorage} implementation
	 * @throws MailException
	 *             If connection is not established, yet
	 */
	public abstract E getMessageStorage() throws MailException;

	/**
	 * Checks if this connection is currently connected
	 * 
	 * @return <code>true</code> if connected; otherwise <code>false</code>
	 * @see #isConnectedUnsafe()
	 */
	public abstract boolean isConnected();

	/**
	 * Checks if this connection is currently connected in an unsafe, but faster
	 * manner than {@link #isConnected()}
	 * 
	 * @return <code>true</code> if connected; otherwise <code>false</code>
	 * @see #isConnected()
	 */
	public abstract boolean isConnectedUnsafe();

	/**
	 * Gets the trace of the thread that lastly obtained this connection.
	 * <p>
	 * This is useful to detect certain threads which uses a connection for a
	 * long time
	 * 
	 * @return the trace of the thread that lastly obtained this connection
	 */
	public abstract String getTrace();

}
