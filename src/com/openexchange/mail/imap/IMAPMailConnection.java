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

package com.openexchange.mail.imap;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.MessagingException;
import javax.mail.Session;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.IMAPPropertyException;
import com.openexchange.mail.MailConnection;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.sessiond.SessionObject;
import com.sun.mail.imap.IMAPStore;

/**
 * IMAPMailConnection
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPMailConnection extends MailConnection<IMAPFolderStorage, IMAPMessageStorage> implements
		Serializable {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -7510487764376433468L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPMailConnection.class);

	private static final String PROTOCOL_IMAP = "imap";

	private static final String CHARENC_ISO8859 = "ISO-8859-1";

	private final transient SessionObject session;

	private IMAPFolderStorage folderAccess;

	private final transient Lock folderAccessLock = new ReentrantLock();

	private boolean folderAccessInit;

	private IMAPMessageStorage messageAccess;

	private final transient Lock messageAccessLock = new ReentrantLock();

	private boolean messageAccessInit;

	private transient IMAPStore imapStore;

	private transient Session imapSession;

	private boolean connected;

	private boolean decrement;

	private transient Thread usingThread;

	private StackTraceElement[] trace;

	/**
	 * Default constructor
	 */
	public IMAPMailConnection(final SessionObject session) {
		super();
		this.session = session;
		setMailServer("localhost");
		setMailServerPort(143);
		setMailProperties((Properties) System.getProperties().clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#reset()
	 */
	@Override
	protected void reset() {
		super.reset();
		folderAccess = null;
		folderAccessInit = false;
		messageAccess = null;
		messageAccessInit = false;
		imapStore = null;
		imapSession = null;
		connected = false;
		decrement = false;
		trace = null;
		usingThread = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#close()
	 */
	@Override
	public void close() {
		try {
			if (imapStore != null) {
				try {
					imapStore.close();
				} catch (final MessagingException e) {
					LOG.error("Error while closing IMAPStore", e);
				}
			}
		} finally {
			if (decrement) {
				/*
				 * Decrease counters
				 */
				MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(false);
				MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.IMAP);
				decrementCounter();
			}
			/*
			 * Reset
			 */
			reset();
			/* TODO: IMAPConnectionWatcher.removeIMAPConnection(this); */
		}
	}

	private static final String STR_SECURITY_PROVIDER = "ssl.SocketFactory.provider";

	private static final String STR_SECURITY_FACTORY = "com.openexchange.tools.ssl.TrustAllSSLSocketFactory";

	private static final String PROP_MAIL_DEBUG = "mail.debug";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#connectInternal()
	 */
	@Override
	protected void connectInternal() throws IMAPException {
		if (imapStore != null && imapStore.isConnected()) {
			connected = true;
			usingThread = Thread.currentThread();
			trace = usingThread.getStackTrace();
			return;
		}
		try {
			if (IMAPProperties.isImapsEnabled() || IMAPProperties.isSmtpsEnabled()) {
				/*
				 * Needed for JavaMail >= 1.4
				 */
				Security.setProperty(STR_SECURITY_PROVIDER, STR_SECURITY_FACTORY);
			}

			if (imapSession == null) {
				imapSession = Session.getDefaultInstance(getMailProperties(), null);
			}
			/*
			 * Check if debug should be enabled
			 */
			if (Boolean.parseBoolean(imapSession.getProperty(PROP_MAIL_DEBUG))) {
				imapSession.setDebug(true);
				imapSession.setDebugOut(System.err);
			}
			/*
			 * Get store
			 */
			imapStore = (IMAPStore) imapSession.getStore(PROTOCOL_IMAP);
			String tmpPass = getPassword();
			if (getPassword() != null) {
				try {
					tmpPass = new String(getPassword().getBytes(IMAPProperties.getImapAuthEnc()), CHARENC_ISO8859);
				} catch (final UnsupportedEncodingException e) {
					LOG.error(e.getMessage(), e);
				} catch (final IMAPPropertyException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			imapStore.connect(getMailServer(), getMailServerPort(), getLogin(), tmpPass);
			connected = true;
			/*
			 * Increase counter
			 */
			MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(true);
			MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.IMAP);
			incrementCounter();
			/*
			 * Remember to decrement
			 */
			decrement = true;
			usingThread = Thread.currentThread();
			trace = usingThread.getStackTrace();
			/* TODO: IMAPConnectionWatcher.addIMAPConnection(this); */
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, this);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#getFolderStorage()
	 */
	@Override
	public IMAPFolderStorage getFolderStorage() throws IMAPException {
		if (connected) {
			if (!folderAccessInit) {
				folderAccessLock.lock();
				try {
					if (null == folderAccess) {
						folderAccess = new IMAPFolderStorage(imapStore, this, session);
						folderAccessInit = true;
					}
				} finally {
					folderAccessLock.unlock();
				}
			}
			return folderAccess;
		}
		throw new IMAPException(IMAPException.Code.NOT_CONNECTED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#getMessageStorage()
	 */
	@Override
	public IMAPMessageStorage getMessageStorage() throws IMAPException {
		if (connected) {
			if (!messageAccessInit) {
				messageAccessLock.lock();
				try {
					if (null == messageAccess) {
						messageAccess = new IMAPMessageStorage(imapStore, this, session);
						messageAccessInit = true;
					}
				} finally {
					messageAccessLock.unlock();
				}
			}
			return messageAccess;
		}
		throw new IMAPException(IMAPException.Code.NOT_CONNECTED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#isConnected()
	 */
	@Override
	public boolean isConnected() {
		if (!connected) {
			return false;
		}
		return (connected = (imapStore != null && imapStore.isConnected()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#isConnectedUnsafe()
	 */
	@Override
	public boolean isConnectedUnsafe() {
		return connected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#getTrace()
	 */
	@Override
	public String getTrace() {
		final StringBuilder sBuilder = new StringBuilder(512);
		sBuilder.append(toString());
		sBuilder.append("\nEstablished (or fetched from cache) at: ").append('\n');
		/*
		 * Start at index 2
		 */
		for (int i = 2; i < trace.length; i++) {
			sBuilder.append("\tat ").append(trace[i]).append('\n');
		}
		if (null != usingThread && usingThread.isAlive()) {
			sBuilder.append("Current Using Thread: ").append(usingThread.getName()).append('\n');
			final StackTraceElement[] trace = usingThread.getStackTrace();
			for (int i = 0; i < trace.length; i++) {
				if (i > 0) {
					sBuilder.append('\n');
				}
				sBuilder.append("\tat ").append(trace[i]);
			}
		}
		return sBuilder.toString();
	}

}
