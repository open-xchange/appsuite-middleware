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

package com.openexchange.imap;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.MessagingException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.imap.config.GlobalIMAPConfig;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPSessionProperties;
import com.openexchange.imap.spam.SpamHandler;
import com.openexchange.imap.user2acl.User2ACLInit;
import com.openexchange.imap.user2acl.User2ACL.User2ACLException;
import com.openexchange.mail.MailConnection;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailInterfaceImpl;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPConnection} - Establishes an IMAP connection and provides access
 * to storages
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPConnection extends MailConnection<IMAPFolderStorage, IMAPMessageStorage, IMAPLogicTools>
		implements Serializable {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -7510487764376433468L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPConnection.class);

	private static final String PROTOCOL_IMAP = "imap";

	private static final String CHARENC_ISO8859 = "ISO-8859-1";

	private IMAPFolderStorage folderStorage;

	private IMAPMessageStorage messageStorage;

	private transient IMAPLogicTools logicTools;

	private transient IMAPStore imapStore;

	private transient javax.mail.Session imapSession;

	private boolean connected;

	private boolean decrement;

	private transient Thread usingThread;

	private StackTraceElement[] trace;

	private transient IMAPConfig imapConfig;

	/**
	 * Default constructor
	 */
	public IMAPConnection(final Session session) {
		super(session);
		setMailServer("localhost");
		setMailServerPort(143);
		setMailProperties((Properties) System.getProperties().clone());
	}

	private void reset() {
		super.resetFields();
		folderStorage = null;
		messageStorage = null;
		logicTools = null;
		imapStore = null;
		imapSession = null;
		connected = false;
		decrement = false;
		trace = null;
		usingThread = null;
		imapConfig = null;
	}

	@Override
	protected void releaseResources() {
		if (folderStorage != null) {
			try {
				folderStorage.releaseResources();
			} catch (final IMAPException e) {
				LOG.error(e.getLocalizedMessage(), e);
			} finally {
				folderStorage = null;
			}
		}
		if (messageStorage != null) {
			try {
				messageStorage.releaseResources();
			} catch (final IMAPException e) {
				LOG.error(e.getLocalizedMessage(), e);
			} finally {
				messageStorage = null;

			}
		}
		if (logicTools != null) {
			try {
				logicTools.releaseResources();
			} catch (final IMAPException e) {
				LOG.error(e.getLocalizedMessage(), e);
			} finally {
				logicTools = null;
			}
		}
	}

	@Override
	protected void initMailConfig(final Session session) throws MailException {
		if (imapConfig == null) {
			imapConfig = IMAPConfig.getImapConfig(session);
		}
	}

	@Override
	public MailConfig getMailConfig() throws MailException {
		return imapConfig;
	}

	@Override
	protected void closeInternal() {
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
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#connectInternal()
	 */
	@Override
	protected void connectInternal() throws MailException {
		if (imapStore != null && imapStore.isConnected()) {
			connected = true;
			usingThread = Thread.currentThread();
			trace = usingThread.getStackTrace();
			return;
		}
		try {
			final Properties imapProps = IMAPSessionProperties.getDefaultSessionProperties();
			if (null != getMailProperties() && !getMailProperties().isEmpty()) {
				imapProps.putAll(getMailProperties());
			}
			if (imapSession == null) {
				imapSession = javax.mail.Session.getInstance(imapProps, null);
			}
			/*
			 * Check if debug should be enabled
			 */
			if (Boolean.parseBoolean(imapSession.getProperty(MIMESessionPropertyNames.PROP_MAIL_DEBUG))) {
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
					tmpPass = new String(getPassword().getBytes(IMAPConfig.getImapAuthEnc()), CHARENC_ISO8859);
				} catch (final UnsupportedEncodingException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			imapStore.connect(getMailServer(), getMailServerPort(), getLogin(), tmpPass);
			connected = true;
			/*
			 * Check server's capabilities
			 */
			imapConfig.initializeCapabilities(imapStore);
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
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#getFolderStorage()
	 */
	@Override
	public IMAPFolderStorage getFolderStorage() throws MailException {
		if (connected) {
			if (null == folderStorage) {
				folderStorage = new IMAPFolderStorage(imapStore, this, session);
			}
			return folderStorage;
		}
		throw new IMAPException(IMAPException.Code.NOT_CONNECTED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#getMessageStorage()
	 */
	@Override
	public IMAPMessageStorage getMessageStorage() throws MailException {
		if (connected) {
			if (null == messageStorage) {
				messageStorage = new IMAPMessageStorage(imapStore, this, session);
			}
			return messageStorage;
		}
		throw new IMAPException(IMAPException.Code.NOT_CONNECTED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#getLogicTools()
	 */
	@Override
	public IMAPLogicTools getLogicTools() throws MailException {
		if (connected) {
			if (null == logicTools) {
				logicTools = new IMAPLogicTools(imapStore, this, session);
			}
			return logicTools;
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
		sBuilder.append("\nIMAP connection established (or fetched from cache) at: ").append('\n');
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#getMailPermissionClass()
	 */
	@Override
	public String getMailPermissionClassInternal() {
		return ACLPermission.class.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#getMailPermissionClass()
	 */
	@Override
	public String getGlobalMailConfigClassInternal() {
		return GlobalIMAPConfig.class.getName();
	}

	/**
	 * Gets used IMAP session
	 * 
	 * @return The IMAP session
	 */
	public javax.mail.Session getSession() {
		return imapSession;
	}

	@Override
	protected void startupInternal() throws MailException {
		try {
			User2ACLInit.getInstance().start();
		} catch (final User2ACLException e) {
			throw new MailException(e);
		} catch (final MailException e) {
			throw e;
		} catch (final AbstractOXException e) {
			throw new MailException(e);
		}
	}
	
	@Override
	protected void shutdownInternal() throws MailException {
		try {
			User2ACLInit.getInstance().stop();
			SpamHandler.releaseInstance();
		} catch (final User2ACLException e) {
			throw new MailException(e);
		} catch (final MailException e) {
			throw e;
		} catch (final AbstractOXException e) {
			throw new MailException(e);
		}
	}
}
