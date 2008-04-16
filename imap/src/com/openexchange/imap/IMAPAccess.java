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

import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Properties;

import javax.mail.MessagingException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPSessionProperties;
import com.openexchange.imap.user2acl.User2ACLException;
import com.openexchange.imap.user2acl.User2ACLInit;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPAccess} - Establishes an IMAP access and provides access to
 * storages
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPAccess extends MailAccess<IMAPFolderStorage, IMAPMessageStorage> {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -7510487764376433468L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPAccess.class);

	private static final String CHARENC_ISO8859 = "ISO-8859-1";

	private IMAPFolderStorage folderStorage;

	private IMAPMessageStorage messageStorage;

	private transient MailLogicTools logicTools;

	private transient IMAPStore imapStore;

	private transient javax.mail.Session imapSession;

	private transient IMAPConfig imapConfig;

	private boolean connected;

	private boolean decrement;

	/**
	 * Default constructor
	 */
	public IMAPAccess(final Session session) {
		super(session);
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
	}

	@Override
	protected void releaseResources() {
		if (folderStorage != null) {
			try {
				folderStorage.releaseResources();
			} catch (final MailException e) {
				LOG.error(new StringBuilder("Error while closing IMAP folder storage: ")
						.append(e.getLocalizedMessage()).toString(), e);
			} finally {
				folderStorage = null;
			}
		}
		if (messageStorage != null) {
			try {
				messageStorage.releaseResources();
			} catch (final MailException e) {
				LOG.error(new StringBuilder("Error while closing IMAP message storage: ").append(
						e.getLocalizedMessage()).toString(), e);
			} finally {
				messageStorage = null;

			}
		}
		if (logicTools != null) {
			logicTools = null;
		}
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
				imapStore = null;
			}
		} finally {
			if (decrement) {
				/*
				 * Decrease counters
				 */
				MailServletInterface.mailInterfaceMonitor.changeNumActive(false);
				MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.IMAP);
				decrementCounter();
			}
			/*
			 * Reset
			 */
			reset();
		}
	}

	@Override
	public MailConfig getMailConfig() throws MailException {
		if (null == imapConfig) {
			imapConfig = MailConfig.getConfig(IMAPConfig.class, session);
		}
		return imapConfig;
	}

	/**
	 * Simple getter for config field
	 * 
	 * @return The IMAP config
	 */
	IMAPConfig getIMAPConfig() {
		return imapConfig;
	}

	private static final String PROPERTY_SECURITY_PROVIDER = "ssl.SocketFactory.provider";

	private static final String CLASSNAME_SECURITY_FACTORY = "com.openexchange.tools.ssl.TrustAllSSLSocketFactory";

	@Override
	protected void connectInternal() throws MailException {
		if (imapStore != null && imapStore.isConnected()) {
			connected = true;
			return;
		}
		try {
			final Properties imapProps = IMAPSessionProperties.getDefaultSessionProperties();
			if (null != getMailProperties() && !getMailProperties().isEmpty()) {
				imapProps.putAll(getMailProperties());
			}
			/*
			 * Check if a secure IMAP connection should be established
			 */
			if (getMailConfig().isSecure()) {
				imapProps.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_CLASS, CLASSNAME_SECURITY_FACTORY);
				imapProps.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_PORT, String
						.valueOf(getMailConfig().getPort()));
				imapProps.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_FALLBACK, "false");
				imapProps.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_STARTTLS_ENABLE, "true");
				/*
				 * Needed for JavaMail >= 1.4
				 */
				Security.setProperty(PROPERTY_SECURITY_PROVIDER, CLASSNAME_SECURITY_FACTORY);
			}
			/*
			 * Apply properties to IMAP session
			 */
			imapSession = javax.mail.Session.getInstance(imapProps, null);
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
			imapStore = (IMAPStore) imapSession.getStore(IMAPProvider.PROTOCOL_IMAP.getName());
			String tmpPass = getMailConfig().getPassword();
			if (tmpPass != null) {
				try {
					tmpPass = new String(tmpPass.getBytes(IMAPConfig.getImapAuthEnc()), CHARENC_ISO8859);
				} catch (final UnsupportedEncodingException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			imapStore.connect(getMailConfig().getServer(), getMailConfig().getPort(), getMailConfig().getLogin(),
					tmpPass);
			connected = true;
			/*
			 * Add server's capabilities
			 */
			((IMAPConfig) getMailConfig()).initializeCapabilities(imapStore);
			/*
			 * Increase counter
			 */
			MailServletInterface.mailInterfaceMonitor.changeNumActive(true);
			MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.IMAP);
			incrementCounter();
			/*
			 * Remember to decrement
			 */
			decrement = true;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConfig);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#getFolderStorage()
	 */
	@Override
	public IMAPFolderStorage getFolderStorage() throws MailException {
		connected = (imapStore != null && imapStore.isConnected());
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
		connected = (imapStore != null && imapStore.isConnected());
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
	public MailLogicTools getLogicTools() throws MailException {
		connected = (imapStore != null && imapStore.isConnected());
		if (connected) {
			if (null == logicTools) {
				logicTools = new MailLogicTools(session);
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

	/**
	 * Gets used IMAP session
	 * 
	 * @return The IMAP session
	 */
	public javax.mail.Session getSession() {
		return imapSession;
	}

	@Override
	protected void startup() throws MailException {
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
	protected void shutdown() throws MailException {
		try {
			User2ACLInit.getInstance().stop();
		} catch (final User2ACLException e) {
			throw new MailException(e);
		} catch (final MailException e) {
			throw e;
		} catch (final AbstractOXException e) {
			throw new MailException(e);
		}
		IMAPSessionProperties.resetDefaultSessionProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailConnection#checkMailServerPort()
	 */
	@Override
	protected boolean checkMailServerPort() {
		return true;
	}

}
