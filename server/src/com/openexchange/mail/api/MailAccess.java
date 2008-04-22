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

package com.openexchange.mail.api;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.caching.CacheException;
import com.openexchange.mail.MailAccessWatcher;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.cache.MailAccessCache;
import com.openexchange.session.Session;

/**
 * {@link MailAccess} - Handles connecting to the mailing system while using an
 * internal cache for connected access objects (see {@link MailAccessCache}).
 * <p>
 * Moreover it provides access to either message storage, folder storage and
 * logic tools.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailAccess<F extends MailFolderStorage, M extends MailMessageStorage> implements Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -2580495494392812083L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailAccess.class);

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private static final transient Lock LOCK_CON = new ReentrantLock();

	private static final transient Condition LOCK_CON_CONDITION = LOCK_CON.newCondition();

	protected final transient Session session;

	private Properties mailProperties;

	private transient Thread usingThread;

	private StackTraceElement[] trace;

	/**
	 * Friendly instantiation
	 */
	protected MailAccess(final Session session) {
		super();
		this.session = session;
	}

	/**
	 * Resets this access' settings
	 */
	protected final void resetFields() {
		mailProperties = null;
		usingThread = null;
		trace = null;
	}

	/**
	 * Triggers all implementation-specific startup actions
	 * 
	 * @param className
	 *            The mail access class name
	 * @throws MailException
	 *             If implementation-specific startup fails
	 */
	static void startupImpl(final Class<? extends MailAccess<?, ?>> clazz) throws MailException {
		createNewMailAccess(clazz, null).startup();
	}

	/**
	 * Triggers all implementation-specific shutdown actions
	 * 
	 * @param className
	 *            The mail access class name
	 * @throws MailException
	 *             If implementation-specific shutdown fails
	 */
	static void shutdownImpl(final Class<? extends MailAccess<?, ?>> clazz) throws MailException {
		createNewMailAccess(clazz, null).shutdown();
	}

	private static final Class<?>[] CONSTRUCTOR_ARGS = new Class[] { Session.class };

	/**
	 * Gets the proper instance of {@link MailAccess} parameterized with given
	 * session
	 * 
	 * @param session
	 *            The session
	 * @return A proper instance of {@link MailAccess}
	 * @throws MailException
	 *             If instantiation fails or a caching error occurs
	 */
	public static final MailAccess<?, ?> getInstance(final Session session) throws MailException {
		try {
			if (MailAccessCache.getInstance().containsMailAccess(session)) {
				final MailAccess<?, ?> mailAccess = MailAccessCache.getInstance().removeMailAccess(session);
				if (mailAccess != null) {
					/*
					 * Apply new thread's trace information
					 */
					mailAccess.applyNewThread();
					MailAccessWatcher.addMailAccess(mailAccess);
					return mailAccess;
				}
			}
		} catch (final CacheException e1) {
			/*
			 * Fetching from cache failed
			 */
			LOG.error(e1.getLocalizedMessage(), e1);
		}
		/*
		 * No cached connection available, check if a new one may be established
		 */
		if ((MailConfig.getMaxNumOfConnections() > 0) && (COUNTER.get() > MailConfig.getMaxNumOfConnections())) {
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
				if (MailAccessCache.getInstance().containsMailAccess(session)) {
					final MailAccess<?, ?> mailAccess = MailAccessCache.getInstance().removeMailAccess(session);
					if (mailAccess != null) {
						/*
						 * Apply new thread's trace information
						 */
						mailAccess.applyNewThread();
						MailAccessWatcher.addMailAccess(mailAccess);
						return mailAccess;
					}
				}
			} catch (final InterruptedException e) {
				LOG.error(e.getMessage(), e);
				throw new MailException(MailException.Code.INTERRUPT_ERROR, e, new Object[0]);
			} catch (final CacheException e1) {
				/*
				 * Fetching from cache failed
				 */
				LOG.error(e1.getLocalizedMessage(), e1);
			} finally {
				LOCK_CON.unlock();
			}
		}
		/*
		 * Create a new mail connection through user's mail provider
		 */
		return createNewMailAccess(MailProviderRegistry.getMailProviderBySession(session).getMailAccessClass(), session);
	}

	/**
	 * Creates a new mail access instance by class name
	 * 
	 * @param clazz
	 *            The mail access class
	 * @param session
	 *            The session providing needed user data
	 * @return Newly created mail connection instance
	 * @throws MailException
	 *             If mail connection creation fails
	 */
	private static final MailAccess<?, ?> createNewMailAccess(final Class<? extends MailAccess<?, ?>> clazz,
			final Session session) throws MailException {
		try {
			return clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance(new Object[] { session });
		} catch (final SecurityException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final NoSuchMethodException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalArgumentException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InstantiationException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalAccessException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InvocationTargetException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		}
	}

	/**
	 * @return the global access counter
	 */
	public static final int getCounter() {
		return COUNTER.get();
	}

	/**
	 * Increments the global access counter
	 */
	protected static final void incrementCounter() {
		COUNTER.incrementAndGet();
	}

	/**
	 * Decrements the global access counter
	 */
	protected static final void decrementCounter() {
		COUNTER.decrementAndGet();
	}

	/**
	 * Gets the optional properties used on connect.
	 * 
	 * @return the mailProperties
	 */
	public final Properties getMailProperties() {
		return mailProperties;
	}

	/**
	 * Sets optional properties used on connect. Herewith additional properties
	 * can be applied and checked later on.
	 * 
	 * @param mailProperties
	 *            The properties
	 */
	public final void setMailProperties(final Properties mailProperties) {
		this.mailProperties = mailProperties;
	}

	/**
	 * Checks if all necessary fields are set in this access object
	 * <p>
	 * This routine is implicitly invoked by {@link #connect()}
	 * 
	 * @throws MailException
	 *             If a necessary field is missing
	 * @see #connect()
	 */
	protected final void checkFieldsBeforeConnect(final MailConfig mailConfig) throws MailException {

		/*
		 * Properties are implementation specific and therefore are created
		 * within connectInternal()
		 */
		if (mailConfig.getServer() == null) {
			throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "mail server");
		} else if (checkMailServerPort() && (mailConfig.getPort() <= 0)) {
			throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "mail server port");
		} else if (mailConfig.getLogin() == null) {
			throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "login");
		} else if (mailConfig.getPassword() == null) {
			throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "password");
		}
	}

	/**
	 * Opens this access. May be invoked on an already opened access.
	 * 
	 * @throws MailException
	 *             If the connection could not be established for various
	 *             reasons
	 * @throws IllegalArgumentException
	 *             If specified mail configuration is <code>null</code>
	 */
	public final void connect() throws MailException {
		applyNewThread();
		if (isConnected()) {
			getFolderStorage().checkDefaultFolders();
			return;
		}
		checkFieldsBeforeConnect(getMailConfig());
		connectInternal();
		getFolderStorage().checkDefaultFolders();
		MailAccessWatcher.addMailAccess(this);
	}

	/**
	 * Internal connect method to establish a mail connection
	 * 
	 * @param mailConfig
	 *            The mail configuration providing connect and login data
	 * @throws MailException
	 *             If connection could not be established
	 */
	protected abstract void connectInternal() throws MailException;

	/**
	 * Closes this access
	 * <p>
	 * An already closed connection is not going to be put into cache and is
	 * treated as a no-op.
	 * 
	 * @param put2CacheArg
	 *            <code>true</code> to try to put this mail connection into
	 *            cache; otherwise <code>false</code>
	 */
	public final void close(final boolean put2CacheArg) {
		if (!isConnectedUnsafe()) {
			return;
		}
		boolean put2Cache = put2CacheArg;
		try {
			try {
				/*
				 * Release all used, non-cachable resources
				 */
				releaseResources();
			} catch (final Throwable t) {
				/*
				 * Dropping
				 */
				LOG.error("Resources could not be properly released. Dropping mail connection for safety reasons", t);
				put2Cache = false;
			}
			// resetFields();
			try {
				/*
				 * Cache connection if desired/possible anymore
				 */
				if (put2Cache && MailAccessCache.getInstance().putMailAccess(session, this)) {
					/*
					 * Successfully cached: signal & return
					 */
					signalAvailableConnection();
					return;
				}
			} catch (final CacheException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
			/*
			 * Close mail connection
			 */
			closeInternal();
			signalAvailableConnection();
		} finally {
			/*
			 * Remove from watcher no matter if cached or closed
			 */
			MailAccessWatcher.removeMailAccess(this);
		}
	}

	/**
	 * Gets the trace of the thread that lastly obtained this access.
	 * <p>
	 * This is useful to detect certain threads which uses an access for a long
	 * time
	 * 
	 * @return the trace of the thread that lastly obtained this access
	 */
	public final String getTrace() {
		final StringBuilder sBuilder = new StringBuilder(512);
		sBuilder.append(toString());
		sBuilder.append("\nIMAP connection established (or fetched from cache) at: ").append('\n');
		/*
		 * Start at index 3
		 */
		for (int i = 3; i < trace.length; i++) {
			sBuilder.append("\tat ").append(trace[i]).append('\n');
		}
		if ((null != usingThread) && usingThread.isAlive()) {
			sBuilder.append("Current Using Thread: ").append(usingThread.getName()).append('\n');
			final StackTraceElement[] trace = usingThread.getStackTrace();
			sBuilder.append("\tat ").append(trace[0]);
			for (int i = 1; i < trace.length; i++) {
				sBuilder.append('\n').append("\tat ").append(trace[i]);
			}
		}
		return sBuilder.toString();
	}

	/**
	 * Returns the mail configuration appropriate for current user. It provides
	 * needed connection and login informations.
	 * 
	 * @return The mail configuration
	 */
	public abstract MailConfig getMailConfig() throws MailException;

	/**
	 * Signals an available connection
	 */
	private void signalAvailableConnection() {
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
	}

	/**
	 * Apply new thread's trace informations
	 */
	private final void applyNewThread() {
		usingThread = Thread.currentThread();
		trace = usingThread.getStackTrace();
	}

	/**
	 * Defines if mail server port has to be present in provided mail
	 * configuration before establishing any connection.
	 * 
	 * @return <code>true</code> if mail server port has to be set before
	 *         establishing any connection; otherwise <code>false</code>
	 */
	protected abstract boolean checkMailServerPort();

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
	public abstract F getFolderStorage() throws MailException;

	/**
	 * Gets the appropriate {@link MailMessageStorage} implementation that
	 * provides necessary message-related operations/methods
	 * 
	 * @return The appropriate {@link MailMessageStorage} implementation
	 * @throws MailException
	 *             If connection is not established
	 */
	public abstract M getMessageStorage() throws MailException;

	/**
	 * Gets the appropriate {@link MailLogicTools} implementation that provides
	 * operations/methods to create a reply/forward message from a referenced
	 * message.
	 * 
	 * @return The appropriate {@link MailLogicTools} implementation
	 * @throws MailException
	 *             If connection is not established
	 */
	public abstract MailLogicTools getLogicTools() throws MailException;

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
	 * Triggers all necessary startup actions
	 * 
	 * @throws MailException
	 *             If startup actions fail
	 */
	protected abstract void startup() throws MailException;

	/**
	 * Triggers all necessary shutdown actions
	 * 
	 * @throws MailException
	 *             If shutdown actions fail
	 */
	protected abstract void shutdown() throws MailException;
}
