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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.MessagingException;
import javax.mail.Session;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.monitoring.MonitoringInfo;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * DefaultIMAPConnection Interface for handling the imap connections. NOTE: The
 * APIs unique to this class should be considered EXPERIMENTAL. They may be
 * changed in the future in ways that are incompatible with applications using
 * the current APIs.
 * 
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class DefaultIMAPConnection implements IMAPConnection, Serializable {

	private static final long serialVersionUID = 6925486716045103344L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(DefaultIMAPConnection.class);

	private static final String PROTOCOL_IMAP = "imap";

	private static final String CHARENC_ISO8859 = "ISO-8859-1";

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private String imapServer;

	private int imapPort;

	private String imapUsername;

	private String imapPassword;

	private Properties imapProperties;

	private transient Session imapSession;

	private transient IMAPStore imapStore;

	private boolean expunge;

	private transient IMAPFolder imapFolder;

	private int holdsMessages = -1;

	private boolean connected;

	private boolean decrement;

	private StackTraceElement[] trace;

	private Thread usingThread;

	public DefaultIMAPConnection() {
		super();
		imapServer = "localhost";
		imapPort = 143;
		imapProperties = (Properties) System.getProperties().clone();
	}

	private void reset() {
		imapServer = null;
		imapPort = 0;
		imapUsername = null;
		imapPassword = null;
		imapProperties = null;
		imapSession = null;
		imapStore = null;
		expunge = false;
		imapFolder = null;
		holdsMessages = -1;
		connected = false;
		decrement = false;
		trace = null;
		usingThread = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#setImapServer(java.lang.String,
	 *      int)
	 */
	public void setImapServer(final String imapServer, final int imapPort) {
		this.imapServer = imapServer;
		this.imapPort = imapPort;
		/*
		 * if (imapPort != -1) imapProperties.put("mail.imap.port",
		 * String.valueOf(imapPort));
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#setUsername(java.lang.String)
	 */
	public void setUsername(final String imapUsername) {
		this.imapUsername = imapUsername;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#setPassword(java.lang.String)
	 */
	public void setPassword(final String imapPassword) {
		this.imapPassword = imapPassword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#setProperties(java.util.Properties)
	 */
	public void setProperties(final Properties imapProperties) {
		this.imapProperties = imapProperties;
	}

	private static final String STR_SECURITY_PROVIDER = "ssl.SocketFactory.provider";

	private static final String STR_SECURITY_FACTORY = "com.openexchange.tools.ssl.TrustAllSSLSocketFactory";

	private static final String PROP_MAIL_DEBUG = "mail.debug";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#connect()
	 */
	public void connect() throws javax.mail.NoSuchProviderException, javax.mail.MessagingException {
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
		} catch (final IMAPException e1) {
			throw new MessagingException(e1.getMessage(), e1);
		}
		if (imapSession == null) {
			imapSession = Session.getDefaultInstance(imapProperties, null);
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
		String tmpPass = imapPassword;
		if (imapPassword != null) {
			try {
				tmpPass = new String(imapPassword.getBytes(IMAPProperties.getImapAuthEnc()), CHARENC_ISO8859);
			} catch (final UnsupportedEncodingException e) {
				LOG.error(e.getMessage(), e);
			} catch (final IMAPException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		imapStore.connect(imapServer, imapPort, imapUsername, tmpPass);
		connected = true;
		/*
		 * Increase counter
		 */
		MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(true);
		MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.IMAP);
		COUNTER.incrementAndGet();
		/*
		 * Remember to decrement
		 */
		decrement = true;
		usingThread = Thread.currentThread();
		trace = usingThread.getStackTrace();
		IMAPConnectionWatcher.addIMAPConnection(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#getIMAPStore()
	 */
	public IMAPStore getIMAPStore() {
		if (connected) {
			return imapStore;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#close()
	 */
	public void close() throws javax.mail.MessagingException {
		try {
			try {
				if (imapFolder != null) {
					try {
						imapFolder.close(false);
						MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(false);
					} catch (final IllegalStateException e) {
						LOG.warn("Invoked close() on a closed folder", e);
					}
				}
			} catch (final Throwable t) {
				LOG.warn("IMAP folder could not be closed", t);
			} finally {
				if (imapStore != null) {
					try {
						imapStore.close();
					} catch (final MessagingException e) {
						LOG.error("Error while closing IMAPStore", e);
					}
				}
			}
		} finally {
			if (decrement) {
				/*
				 * Decrease counters
				 */
				MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(false);
				MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.IMAP);
				COUNTER.decrementAndGet();
			}
			/*
			 * Reset
			 */
			reset();
			IMAPConnectionWatcher.removeIMAPConnection(this);
		}
	}

	public Session getSession() {
		return imapSession;
	}

	public boolean isExpunge() {
		return expunge;
	}

	public void setExpunge(final boolean expunge) {
		this.expunge = expunge;
	}

	public IMAPFolder getImapFolder() {
		return imapFolder;
	}

	public void setImapFolder(final IMAPFolder imapFolder) {
		this.imapFolder = imapFolder;
		if (null == imapFolder) {
			this.holdsMessages = -1;
			this.expunge = false;
		}
	}

	public void resetImapFolder() {
		this.imapFolder = null;
		this.holdsMessages = -1;
		this.expunge = false;
	}

	public boolean isHoldsMessages() throws MessagingException {
		if (holdsMessages == -1) {
			holdsMessages = (imapFolder.getType() & IMAPFolder.HOLDS_MESSAGES) == 0 ? 0 : 1;
		}
		return (holdsMessages > 0);
	}

	public void resetHoldsMessages() {
		this.holdsMessages = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#isConnected()
	 */
	public boolean isConnected() {
		if (!connected) {
			return false;
		}
		return (connected = (imapStore != null && imapStore.isConnected()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#isConnectedUnsafe()
	 */
	public boolean isConnectedUnsafe() {
		return connected;
	}

	/**
	 * Getter for manually counted open IMAP connections
	 * 
	 * @return Manually counted open IMAP connections
	 */
	public static int getCounter() {
		return COUNTER.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.IMAPConnection#getTrace()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sBuilder = new StringBuilder(512);
		if (!isConnected()) {
			sBuilder.append("IMAPConnection NOT connected");
		} else {
			sBuilder.append("IMAPConnection connected");
		}
		sBuilder.append(" | IMAP Server=").append(imapServer).append(':').append(imapPort);
		sBuilder.append(" | User=").append(imapUsername);
		if (null != imapFolder) {
			sBuilder.append(" | IMAP Folder=").append(imapFolder.getFullName());
		}
		return sBuilder.toString();
	}

}
