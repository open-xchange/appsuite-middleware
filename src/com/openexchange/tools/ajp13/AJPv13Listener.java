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

package com.openexchange.tools.ajp13;

import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.HttpServletResponseWrapper;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AJPv13Listener implements Runnable {

	// private final String excPrefix;

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13Listener.class);

	private Socket client;

	private AJPv13ListenerThread listenerThread;

	private boolean listenerStarted;

	private AJPv13Connection ajpCon;

	private boolean processing;

	private long processingStart;

	private boolean waitingOnAJPSocket;

	private boolean pooled;

	private final int num;

	private final Lock listenerLock = new ReentrantLock();

	private final transient Condition resumeRunning = listenerLock.newCondition();

	private static int numRunning;

	private static final Lock COUNT_LOCK = new ReentrantLock();

	private static final DecimalFormat DF = new DecimalFormat("00000");

	/**
	 * Constructs a new <code>AJPv13Listener</code> instance with given
	 * <code>num</code> argument
	 * 
	 * @param num
	 */
	public AJPv13Listener(final int num) {
		this(num, false);
	}

	/**
	 * Constructs a new <code>AJPv13Listener</code> instance with given
	 * <code>num</code> argument. <code>pooled</code> determines whether
	 * this listener is initially put into pool
	 */
	public AJPv13Listener(final int num, final boolean pooled) {
		this.num = num;
		processing = false;
		waitingOnAJPSocket = false;
		this.pooled = pooled;
		listenerThread = new AJPv13ListenerThread(this);
		listenerThread.setName(new StringBuilder("AJPListener-").append(DF.format(this.num)).toString());
	}

	/**
	 * Starts the listener
	 */
	public boolean startListener(final Socket client) {
		/*
		 * Assign a newly accepted client socket
		 */
		this.client = client;
		if (listenerStarted) {
			if (!listenerThread.isAlive() || listenerThread.isDead()) {
				/*
				 * Listener has died or has been interrupted before
				 */
				this.client = null;
				return false;
			}
			/*
			 * Listener has already been started and is waiting to resume to
			 * work
			 */
			listenerLock.lock();
			try {
				resumeRunning.signal();
			} finally {
				listenerLock.unlock();
			}
			AJPv13Server.ajpv13ListenerMonitor.incrementNumActive();
			return true;
		}
		/*
		 * Listener gets started the first time
		 */
		listenerThread.start();
		listenerStarted = true;
		AJPv13Server.ajpv13ListenerMonitor.incrementNumActive();
		return true;
	}

	/**
	 * Stops the listener by interrupting its worker, marks it as dead and
	 * removes listener from pool (if pooled)
	 * <p>
	 * <b>NOTE: </b>This could lead to an unpredicted behaviour in overall
	 * system. Please use with care
	 * </p>
	 */
	public boolean stopListener() {
		terminateAndClose();
		if (listenerThread == null) {
			return true;
		}
		if (pooled) {
			AJPv13ListenerPool.removeListener(num);
		}
		try {
			listenerThread.interrupt();
			return true;
		} catch (final Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(e.getMessage(), e);
			}
			return false;
		} finally {
			listenerThread = null;
			listenerThread = new AJPv13ListenerThread(this);
			listenerThread.setName(new StringBuilder("AJPListener-").append(DF.format(this.num)).toString());
			listenerStarted = false;
		}
	}

	private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

	/**
	 * @return the stack trace of this listener's running thread
	 */
	public StackTraceElement[] getStackTrace() {
		if (listenerThread == null || !listenerThread.isAlive() || listenerThread.isDead()) {
			return EMPTY_STACK_TRACE;
		}
		return listenerThread.getStackTrace();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		boolean keepOnRunning = true;
		changeNumberOfRunningAJPListeners(true);
		while (keepOnRunning && client != null) {
			final long start = System.currentTimeMillis();
			/*
			 * Assign a connection to this listener
			 */
			if (AJPv13Config.useAJPConnectionPool()) {
				/*
				 * If Connection Pool should be used
				 */
				ajpCon = AJPv13ConnectionPool.getAJPv13Connection(this);
			} else {
				/*
				 * Otherwise
				 */
				ajpCon = new AJPv13Connection(this);
			}
			try {
				client.setKeepAlive(true);
				waitingOnAJPSocket = true;
				/*
				 * Keep on processing underlying stream's data as long as
				 * accepted client socket is alive and its input is not shut
				 * down
				 */
				while (client != null && !client.isInputShutdown()) {
					try {
						ajpCon.processRequest();
						ajpCon.createResponse();
						if (!ajpCon.getAjpRequestHandler().isEndResponseSent()) {
							/*
							 * Just for safety reason to ensure END_RESPONSE
							 * package is going to be sent.
							 */
							writeEndResponse(client, false);
						}
					} catch (final UploadServletException e) {
						LOG.error(e.getMessage(), e);
						/*
						 * Send call back
						 */
						writeSendHeaders(client, (HttpServletResponseWrapper) e.getRes());
						writeSendBody(client, e.getData().getBytes("UTF-8"));
						closeAndKeepAlive();
					} catch (final ServletException e) {
						LOG.error(e.getMessage(), e);
						closeAndKeepAlive();
					} catch (final AJPv13Exception e) {
						if (e.keepAlive()) {
						    LOG.error(e.getMessage(), e);
							closeAndKeepAlive();
						} else {
							/*
							 * Leave outer while loop since connection shall be
							 * closed
							 */
							throw e;
						}
					} catch (final IOException e) {
						/*
						 * Obviously a socket communication error occurred
						 */
						throw new AJPv13SocketClosedException(AJPv13Exception.AJPCode.IO_ERROR, e, e
								.getLocalizedMessage());
					} catch (final Throwable e) {
						/*
						 * Catch Throwable to catch every Exception, even
						 * RuntimeExceptions
						 */
						final AbstractOXException logMe;
						if (e instanceof AbstractOXException) {
							logMe = (AbstractOXException) e;
						} else {
							logMe = new AJPv13Exception(e);
						}
						LOG.error(logMe.getMessage(), logMe);
						closeAndKeepAlive();
					}
					ajpCon.resetConnection(false);
					AJPv13Server.ajpv13ListenerMonitor.decrementNumProcessing();
					AJPv13Server.ajpv13ListenerMonitor.addProcessingTime(System.currentTimeMillis() - processingStart);
					AJPv13Server.ajpv13ListenerMonitor.incrementNumRequests();
					processing = false;
					client.getOutputStream().flush();
				}
			} catch (final AJPv13SocketClosedException e) {
				/*
				 * Just as debug
				 */
				if (LOG.isDebugEnabled()) {
					LOG.debug(e.getMessage(), e);
				}
			} catch (final AJPv13Exception e) {
				LOG.error(e.getMessage(), e);
			} catch (final Throwable e) {
				/*
				 * Catch Throwable to catch every Exception, even
				 * RuntimeExceptions
				 */
				final AJPv13Exception wrapper = new AJPv13Exception(e);
				LOG.error(wrapper.getMessage(), wrapper);
			} finally {
				terminateAndClose();
				waitingOnAJPSocket = false;
				if (processing) {
					AJPv13Server.ajpv13ListenerMonitor.decrementNumProcessing();
					AJPv13Server.ajpv13ListenerMonitor.addProcessingTime(System.currentTimeMillis() - processingStart);
					AJPv13Server.ajpv13ListenerMonitor.incrementNumRequests();
					processing = false;
				}
				AJPv13Server.decrementNumberOfOpenAJPSockets();
				AJPv13Server.ajpv13ListenerMonitor.decrementNumActive();
			}
			/*
			 * Put back listener into pool. Use an enforced put if mod_jk is
			 * enabled.
			 */
			if (AJPv13ListenerPool.putBack(this, AJPv13Config.isAJPModJK())) {
				/*
				 * Listener could be successfully put into pool, so put him
				 * asleep
				 */
				AJPv13Server.ajpv13ListenerMonitor.incrementNumIdle();
				pooled = true;
				listenerLock.lock();
				try {
					final long duration = System.currentTimeMillis() - start;
					AJPv13Server.ajpv13ListenerMonitor.addUseTime(duration);
					resumeRunning.await();
					if (this.listenerThread.isDead()) {
						keepOnRunning = false;
					}
					pooled = false;
				} catch (final InterruptedException e) {
					LOG.error(e.getMessage(), e);
					keepOnRunning = false;
				} finally {
					listenerLock.unlock();
				}
			} else {
				/*
				 * Listener could NOT be put back. Leave run() method and let
				 * this listener die since he finished working on assigned
				 * socket
				 */
				final long duration = System.currentTimeMillis() - start;
				AJPv13Server.ajpv13ListenerMonitor.addUseTime(duration);
				keepOnRunning = false;
			}
		}
		changeNumberOfRunningAJPListeners(false);
		AJPv13Watcher.removeListener(num);
	}

	private void closeAndKeepAlive() throws AJPv13Exception, IOException {
		/*
		 * Send END_RESPONSE package
		 */
		writeEndResponse(client, false);
		ajpCon.getAjpRequestHandler().setEndResponseSent(true);
	}

	/**
	 * Writes connection-terminating ajp END_RESPONSE package to web server,
	 * closes the ajp connection and accepted client socket as well.
	 */
	private void terminateAndClose() {
		try {
			/*
			 * Release AJP connection
			 */
			if (ajpCon != null) {
				ajpCon.removeListener();
				if (AJPv13Config.useAJPConnectionPool()) {
					AJPv13ConnectionPool.putBackAJPv13Connection(ajpCon);
				}
				ajpCon = null;
			}
		} catch (final Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(e.getMessage(), e);
			}
		}
		try {
			/*
			 * Terminate ajp cycle and close socket
			 */
			if (client != null) {
				if (!client.isClosed()) {
					writeEndResponse(client, true);
					client.close();
				}
				client = null;
			}
		} catch (final Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(e.getMessage(), e);
			}
		}
	}

	private static void writeEndResponse(final Socket client, final boolean closeConnection) throws AJPv13Exception,
			IOException {
		client.getOutputStream().write(AJPv13Response.getEndResponseBytes(closeConnection));
		client.getOutputStream().flush();
	}

	private static void writeSendHeaders(final Socket client, final HttpServletResponseWrapper resp)
			throws AJPv13Exception, IOException {
		client.getOutputStream().write(AJPv13Response.getSendHeadersBytes(resp));
		client.getOutputStream().flush();
	}

	private static void writeSendBody(final Socket client, final byte[] data) throws AJPv13Exception, IOException {
		client.getOutputStream().write(AJPv13Response.getSendBodyChunkBytes(data));
		client.getOutputStream().flush();
	}

	/**
	 * @return listener name
	 */
	public String getListenerName() {
		return listenerThread.getName();
	}

	/**
	 * @return listener's last timestamp when processing started
	 */
	public long getProcessingStartTime() {
		return processingStart;
	}

	/**
	 * @return listener's number
	 */
	public int getListenerNumber() {
		return num;
	}

	/**
	 * @return <code>true</code> if listener is currently processing,
	 *         otherwise <code>false</code>
	 */
	public boolean isProcessing() {
		return processing;
	}

	/**
	 * Sets this listener's processing flag
	 */
	public void markProcessing() {
		processing = true;
		waitingOnAJPSocket = false;
		processingStart = System.currentTimeMillis();
		AJPv13Server.ajpv13ListenerMonitor.incrementNumProcessing();
	}

	/**
	 * Mark this listener as non-processing
	 */
	public void markNonProcessing() {
		if (processing) {
			processing = false;
			waitingOnAJPSocket = true;
			AJPv13Server.ajpv13ListenerMonitor.decrementNumProcessing();
		}
	}

	/**
	 * @return <code>true</code> if listener is currently listening to client
	 *         socket's input stream, otherwise <code>false</code>
	 */
	public boolean isWaitingOnAJPSocket() {
		return waitingOnAJPSocket;
	}

	/**
	 * @return this listener's accepted client socket
	 */
	public Socket getSocket() {
		return client;
	}

	public boolean isListenerStarted() {
		return listenerStarted;
	}

	public boolean isPooled() {
		return pooled;
	}

	public static void changeNumberOfRunningAJPListeners(final boolean increment) {
		COUNT_LOCK.lock();
		try {
			numRunning += increment ? 1 : -1;
		} finally {
			COUNT_LOCK.unlock();
		}
	}

	public static int getNumberOfRunningAJPListeners() {
		return numRunning;
	}

}
