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

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.cache.OXCachingException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.cache.MailConnectionCache;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.watcher.MailConnectionWatcher;
import com.openexchange.sessiond.SessionObject;

/**
 * {@link MailConnection}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailConnection<T extends MailFolderStorage, E extends MailMessageStorage, L extends MailLogicTools> {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailConnection.class);

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private static final Lock LOCK_INIT = new ReentrantLock();

	private static final Lock LOCK_CON = new ReentrantLock();

	private static final Condition LOCK_CON_CONDITION = LOCK_CON.newCondition();

	private static Class<? extends MailConnection> clazz;

	private static final AtomicBoolean initialized = new AtomicBoolean();

	protected final SessionObject session;

	private Properties mailProperties;

	private String mailServer;

	private int mailServerPort = -1;

	private String login;

	private String password;

	/**
	 * Friendly instantiation
	 */
	protected MailConnection(final SessionObject session) {
		super();
		this.session = session;
	}

	/**
	 * Resets this connection's settings
	 */
	protected final void resetFields() {
		mailProperties = null;
		mailServer = null;
		mailServerPort = -1;
		login = null;
		password = null;
	}

	/**
	 * Initializes the mail connection
	 * 
	 * @throws MailException
	 *             If implementing class cannot be found
	 */
	public static final void init() throws MailException {
		if (!initialized.get()) {
			LOCK_INIT.lock();
			try {
				if (clazz == null) {
					final String className = SystemConfig.getProperty(SystemConfig.Property.MailProtocol);
					try {
						if (className == null) {
							/*
							 * Fallback
							 */
							if (LOG.isWarnEnabled()) {
								LOG.warn("Using fallback \"com.openexchange.imap.IMAPConnection\"");
							}
							clazz = Class.forName("com.openexchange.imap.IMAPConnection").asSubclass(
									MailConnection.class);
							initialized.set(true);
							return;
						}
						clazz = Class.forName(className).asSubclass(MailConnection.class);
					} catch (final ClassNotFoundException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					}
					initialized.set(true);
				}
			} finally {
				LOCK_INIT.unlock();
			}
		}
	}

	private static final Class[] CONSTRUCTOR_ARGS = new Class[] { SessionObject.class };

	/**
	 * Gets the proper instance of {@link MailConnection} parameterized with
	 * given session
	 * 
	 * @param session
	 *            The session
	 * @return A proper instance of {@link MailConnection}
	 * @throws MailException
	 *             If instantiation fails or a caching error occurs
	 */
	public static final MailConnection getInstance(final SessionObject session) throws MailException {
		if (!initialized.get()) {
			init();
		}
		try {
			if (MailConnectionCache.getInstance().containsMailConnection(session)) {
				final MailConnection mailConnection = MailConnectionCache.getInstance().removeMailConnection(session);
				if (mailConnection != null) {
					/*
					 * Apply new thread's trace information
					 */
					mailConnection.initMailConfig(session);
					mailConnection.connectInternal();
					return mailConnection;
				}
			}
		} catch (final OXCachingException e1) {
			/*
			 * Fetching from cache failed
			 */
			LOG.error(e1.getLocalizedMessage(), e1);
		}
		/*
		 * No cached connection available, check if a new one may be established
		 */
		if (MailConfig.getMaxNumOfConnections() > 0 && COUNTER.get() > MailConfig.getMaxNumOfConnections()) {
			LOCK_CON.lock();
			try {
				while (COUNTER.get() > MailConfig.getMaxNumOfConnections()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Too many mail connections currently established. Going asleep.");
					}
					LOCK_CON_CONDITION.await();
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("Woke up & mail connection(s) may again be established");
				}
				/*
				 * Try to fetch from cache again
				 */
				if (MailConnectionCache.getInstance().containsMailConnection(session)) {
					final MailConnection mailConnection = MailConnectionCache.getInstance().removeMailConnection(
							session);
					if (mailConnection != null) {
						/*
						 * Apply new thread's trace information
						 */
						mailConnection.initMailConfig(session);
						mailConnection.connectInternal();
						return mailConnection;
					}
				}
			} catch (final InterruptedException e) {
				LOG.error(e.getMessage(), e);
				throw new MailException(MailException.Code.INTERRUPT_ERROR, e, new Object[0]);
			} catch (final OXCachingException e1) {
				/*
				 * Fetching from cache failed
				 */
				LOG.error(e1.getLocalizedMessage(), e1);
			} finally {
				LOCK_CON.unlock();
			}
		}
		/*
		 * Create a new mail connection
		 */
		try {
			final MailConnection mailConnection = clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance(
					new Object[] { session });
			mailConnection.initMailConfig(session);
			return mailConnection;
		} catch (SecurityException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (NoSuchMethodException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (IllegalArgumentException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (InstantiationException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (IllegalAccessException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (InvocationTargetException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		}
	}

	/**
	 * Create a dummy instance to access member methods
	 * 
	 * @return A dummy instance
	 * @throws MailException
	 */
	private static MailConnection getInstanceInternal() throws MailException {
		/*
		 * Create a new mail connection
		 */
		try {
			return clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance(new Object[] { null });
		} catch (SecurityException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (NoSuchMethodException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (IllegalArgumentException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (InstantiationException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (IllegalAccessException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (InvocationTargetException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		}
	}

	/**
	 * Gets the class name of {@link MailPermission} implementation
	 * 
	 * @return The class name of {@link MailPermission} implementation
	 */
	public static String getMailPermissionClass() {
		try {
			return MailConnection.getInstanceInternal().getMailPermissionClassInternal();
		} catch (final MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
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
		/*
		 * Properties are implementation specific and therefore are created
		 * within connectInternal()
		 */
		if (getMailServer() == null) {
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
	 * Opens this connection. May be invoked on an already opened connection to
	 * apply new thread information to mail connection watcher.
	 * 
	 * @throws MailException
	 *             If the connection could not be established for variuos
	 *             reasons
	 */
	public final void connect() throws MailException {
		checkFieldsBeforeConnect();
		connectInternal();
		MailConnectionWatcher.addMailConnection(this);
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
	 * <p>
	 * An already closed connection is not going to be put into cache and is
	 * treated as a no-op.
	 * 
	 * @param put2Cache
	 *            <code>true</code> to try to put this mail connection into
	 *            cache; otherwise <code>false</code>
	 */
	public final void close(final boolean put2Cache) {
		if (!isConnectedUnsafe()) {
			return;
		}
		/*
		 * Release all used, non-cachable resources
		 */
		releaseResources();
		// resetFields();
		try {
			/*
			 * Cache connection if desired
			 */
			if (put2Cache && MailConnectionCache.getInstance().putMailConnection(session, this)) {
				if (MailConfig.getMaxNumOfConnections() > 0) {
					LOCK_CON.lock();
					try {
						LOCK_CON_CONDITION.signalAll();
						if (LOG.isDebugEnabled()) {
							LOG.debug("Sending signal to possible waiting threads");
						}
					} finally {
						LOCK_CON.unlock();
					}
				}
				return;
			}
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final MailConfigException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		/*
		 * Close mail connection
		 */
		closeInternal();
		MailConnectionWatcher.removeMailConnection(this);
		try {
			if (MailConfig.getMaxNumOfConnections() > 0) {
				LOCK_CON.lock();
				try {
					LOCK_CON_CONDITION.signalAll();
					if (LOG.isDebugEnabled()) {
						LOG.debug("Sending signal to possible waiting threads");
					}
				} finally {
					LOCK_CON.unlock();
				}
			}
		} catch (final MailConfigException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Initializes the user-specific mail configuration. Initialization of mail
	 * configuration should happend only one time; meaning any subsequent
	 * invocations should be treated as a no-op.
	 * <p>
	 * For example use the lazy creation pattern:
	 * 
	 * <pre>
	 * ...
	 * if (this.config == null) {
	 *     this.config = new MyMailConfig(session);
	 * }
	 * ...
	 * </pre>
	 * 
	 * @param session
	 *            The session providing needed user data
	 * @throws MailException
	 *             If mail configuration cannot be initialized
	 */
	protected abstract void initMailConfig(SessionObject session) throws MailException;

	/**
	 * Gets the user-specific mail configuration with properly set login and
	 * password
	 * 
	 * @return User-specific mail configuration
	 * @throws MailException
	 *             If mail configuration cannot be determined
	 */
	public abstract MailConfig getMailConfig() throws MailException;

	/**
	 * Releases all used resources prior to caching or closing a connection
	 */
	protected abstract void releaseResources();

	/**
	 * Internal close method to drop a mail connection
	 */
	protected abstract void closeInternal();

	/**
	 * Gets the appropriate {@link MailFolderStorage} implementation that is
	 * considered as the main entry point to a user's mailbox
	 * 
	 * @return The appropriate {@link MailFolderStorage} implementation
	 * @throws MailException
	 *             If connection is not established
	 */
	public abstract T getFolderStorage() throws MailException;

	/**
	 * Gets the appropriate {@link MailMessageStorage} implementation that
	 * provides necessary message-related operations/methods
	 * 
	 * @return The appropriate {@link MailMessageStorage} implementation
	 * @throws MailException
	 *             If connection is not established
	 */
	public abstract E getMessageStorage() throws MailException;

	/**
	 * Gets the appropriate {@link MailLogicTools} implementation that provides
	 * common mailbox-related operations/methods
	 * 
	 * @return The appropriate {@link MailLogicTools} implementation
	 * @throws MailException
	 *             If connection is not established
	 */
	public abstract L getLogicTools() throws MailException;

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

	/**
	 * Gets the name of {@link MailPermission} implementation
	 * 
	 * @return The name of {@link MailPermission} implementation
	 */
	protected abstract String getMailPermissionClassInternal();

}
