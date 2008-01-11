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

package com.openexchange.imap.user2acl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.user2acl.User2ACL.User2ACLException;
import com.openexchange.imap.user2acl.User2ACLInit.IMAPServer;
import com.openexchange.mail.config.MailConfig.BoolCapVal;

/**
 * {@link User2ACLAutoDetector}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class User2ACLAutoDetector {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(User2ACLAutoDetector.class);

	private static final Object[] EMPTY_ARGS = new Object[0];

	private static final Map<InetAddress, User2ACL> map = new HashMap<InetAddress, User2ACL>();

	private static final Lock CONTACT_LOCK = new ReentrantLock();

	private static final int BUFSIZE = 512;

	private static final String IMAPCMD_LOGOUT = "A11 LOGOUT\r\n";

	private static final String IMAPCMD_CAPABILITY = "A10 CAPABILITY\r\n";

	private static final String CHARSET_US_ASCII = "US-ASCII";

	/**
	 * Prevent instantiation
	 */
	private User2ACLAutoDetector() {
		super();
	}

	/**
	 * Resets the user2acl auto-detector
	 */
	static void resetUser2ACLMappings() {
		map.clear();
	}

	/**
	 * Determines the {@link User2ACL} impl dependent on MAP server's greeting,
	 * given the IMAP server's name
	 * 
	 * <p>
	 * The IMAP server name can either be a machine name, such as "<code>java.sun.com</code>",
	 * or a textual representation of its IP address.
	 * 
	 * @param imapServer -
	 *            the IMAP server's name
	 * @param imapPort -
	 *            the IMAP server's port
	 * @return the IMAP server's depending {@link User2ACL} implementation
	 * @throws IOException -
	 *             if an I/O error occurs
	 * @throws User2ACLException -
	 *             if a server greeting could not be mapped to a supported IMAP
	 *             server
	 */
	static User2ACL getUser2ACLImpl(final String imapServer, final int imapPort) throws IOException, User2ACLException {
		final InetAddress key = InetAddress.getByName(imapServer);
		User2ACL impl = map.get(key);
		if (impl == null) {
			impl = loadUser2ACLImpl(key, imapPort);
		}
		return impl;
	}

	private static IMAPServer mapInfo2IMAPServer(final String info, final InetAddress inetAddress, final int port)
			throws IOException, User2ACLException {
		final IMAPServer[] iServers = IMAPServer.values();
		for (int i = 0; i < iServers.length; i++) {
			if (toLowerCase(info).indexOf(toLowerCase(iServers[i].getName())) > -1) {
				return iServers[i];
			}
		}
		/*
		 * No known IMAP server found, check if ACLs are disabled anyway. If yes
		 * user2acl is never used and can safely be mapped to default
		 * implementation.
		 */
		final BoolCapVal supportsACLs = IMAPConfig.isSupportsACLsConfig();
		if (BoolCapVal.FALSE.equals(supportsACLs)
				|| (BoolCapVal.AUTO.equals(supportsACLs) && !checkForACLSupport(inetAddress, port))) {
			/*
			 * Return fallback implementation
			 */
			if (LOG.isWarnEnabled()) {
				LOG
						.warn(new StringBuilder(512)
								.append("No IMAP server found that corresponds to greeting:\n\"")
								.append(info.replaceAll("\r?\n", ""))
								.append("\" on ")
								.append(inetAddress.toString())
								.append(
										".\nSince ACLs are disabled (through IMAP configuration) or not supported by IMAP server, \"")
								.append(IMAPServer.CYRUS.getName()).append("\" is used as fallback."));
			}
			return IMAPServer.CYRUS;
		}
		throw new User2ACLException(User2ACLException.Code.UNKNOWN_IMAP_SERVER, info);
	}

	private static String toLowerCase(final String str) {
		final char[] buf = new char[str.length()];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = Character.toLowerCase(str.charAt(i));
		}
		return new String(buf);
	}

	private static final Pattern PAT_ACL = Pattern.compile("(^|\\s)(ACL)(\\s+|$)");

	private static boolean checkForACLSupport(final InetAddress inetAddress, final int imapPort) throws IOException,
			User2ACLException {
		CONTACT_LOCK.lock();
		try {
			Socket s = null;
			InputStreamReader isr = null;
			try {
				try {
					s = TimedSocket.getSocket(inetAddress, imapPort, IMAPConfig.getImapConnectionTimeout());
					/*
					 * Define timeout for blocking operations
					 */
					s.setSoTimeout(IMAPConfig.getImapTimeout());
				} catch (final IOException e) {
					throw new User2ACLException(User2ACLException.Code.CREATING_SOCKET_FAILED, e, inetAddress
							.toString(), e.getLocalizedMessage());
				}
				isr = new InputStreamReader(s.getInputStream(), CHARSET_US_ASCII);
				final StringBuilder sb = new StringBuilder(BUFSIZE);
				final char[] buf = new char[BUFSIZE];
				int bytesRead = -1;
				if ((bytesRead = isr.read(buf, 0, buf.length)) != -1) {
					sb.append(buf, 0, bytesRead);
				}
				s.getOutputStream().write(IMAPCMD_CAPABILITY.getBytes(CHARSET_US_ASCII));
				s.getOutputStream().flush();
				sb.setLength(0);
				if ((bytesRead = isr.read(buf, 0, buf.length)) != -1) {
					sb.append(buf, 0, bytesRead);
				}
				final boolean retval = PAT_ACL.matcher(sb.toString()).find();
				s.getOutputStream().write(IMAPCMD_LOGOUT.getBytes(CHARSET_US_ASCII));
				s.getOutputStream().flush();
				sb.setLength(0);
				while ((bytesRead = isr.read(buf, 0, buf.length)) != -1) {
					sb.append(buf, 0, bytesRead);
				}
				return retval;
			} finally {
				if (isr != null) {
					try {
						isr.close();
					} catch (final IOException e) {
						LOG.error(e.getLocalizedMessage(), e);
					}
				}
				if (s != null) {
					try {
						s.close();
					} catch (final IOException e) {
						LOG.error(e.getLocalizedMessage(), e);
					}
				}
			}
		} finally {
			CONTACT_LOCK.unlock();
		}
	}

	private static User2ACL loadUser2ACLImpl(final InetAddress inetAddress, final int imapPort) throws IOException,
			User2ACLException {
		User2ACL user2Acl = map.get(inetAddress);
		if (user2Acl != null) {
			return user2Acl;
		}
		CONTACT_LOCK.lock();
		try {
			Socket s = null;
			InputStreamReader isr = null;
			try {
				try {
					s = TimedSocket.getSocket(inetAddress, imapPort, IMAPConfig.getImapConnectionTimeout());
					/*
					 * Define timeout for blocking operations
					 */
					s.setSoTimeout(IMAPConfig.getImapTimeout());
				} catch (final IOException e) {
					throw new User2ACLException(User2ACLException.Code.CREATING_SOCKET_FAILED, e, inetAddress
							.toString(), e.getLocalizedMessage());
				}
				isr = new InputStreamReader(s.getInputStream(), CHARSET_US_ASCII);
				final StringBuilder sb = new StringBuilder(BUFSIZE);
				final char[] buf = new char[BUFSIZE];
				int bytesRead = -1;
				if ((bytesRead = isr.read(buf, 0, buf.length)) != -1) {
					sb.append(buf, 0, bytesRead);
				}
				final IMAPServer imapServer = mapInfo2IMAPServer(sb.toString(), inetAddress, imapPort);
				try {
					user2Acl = Class.forName(imapServer.getImpl()).asSubclass(User2ACL.class).newInstance();
				} catch (final InstantiationException e) {
					throw new User2ACLException(User2ACLException.Code.INSTANTIATION_FAILED, e, EMPTY_ARGS);
				} catch (final IllegalAccessException e) {
					throw new User2ACLException(User2ACLException.Code.INSTANTIATION_FAILED, e, EMPTY_ARGS);
				} catch (final ClassNotFoundException e) {
					throw new User2ACLException(User2ACLException.Code.INSTANTIATION_FAILED, e, EMPTY_ARGS);
				}
				s.getOutputStream().write(IMAPCMD_LOGOUT.getBytes(CHARSET_US_ASCII));
				s.getOutputStream().flush();
				while ((bytesRead = isr.read(buf, 0, buf.length)) != -1) {
					sb.append(buf, 0, bytesRead);
				}
				map.put(inetAddress, user2Acl);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(inetAddress.toString()).append(
							"] greeting successfully mapped to: ").append(imapServer.getName()));
				}
				return user2Acl;
			} finally {
				if (isr != null) {
					try {
						isr.close();
					} catch (final IOException e) {
						LOG.error(e.getLocalizedMessage(), e);
					}
				}
				if (s != null) {
					try {
						s.close();
					} catch (final IOException e) {
						LOG.error(e.getLocalizedMessage(), e);
					}
				}
			}
		} finally {
			CONTACT_LOCK.unlock();
		}
	}

	/**
	 * {@link TimedSocket} - This class offers a timeout feature on socket
	 * connections. A maximum length of time allowed for a connection can be
	 * specified, along with a host and port.
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class TimedSocket {

		/**
		 * Polling delay for socket checks (in milliseconds)
		 */
		private static final int POLL_DELAY = 100;

		/**
		 * Attempts to connect to a service at the specified address and port,
		 * for a specified maximum amount of time.
		 * 
		 * @param addr
		 *            Address of host
		 * @param port
		 *            Port of service
		 * @param delay
		 *            Delay in milliseconds
		 * @return The established client socket
		 * @throws InterruptedIOException
		 *             If socket connect times out
		 * @throws IOException
		 *             If an I/O error occurs
		 */
		public static Socket getSocket(final InetAddress addr, final int port, final int delay)
				throws InterruptedIOException, IOException {
			/*
			 * Create a new socket thread, and start it running
			 */
			final SocketThread st = new SocketThread(addr, port);
			st.start();
			int timer = 0;
			Socket sock = null;
			/*
			 * Frequently checking for established socket until delay is
			 * reached.
			 */
			for (;;) {
				/*
				 * Check if a connection is established
				 */
				if (st.isConnected()) {
					sock = st.getSocket();
					break;
				}
				/*
				 * Check if an error occurred
				 */
				if (st.isError()) {
					throw (st.getException());
				}
				/*
				 * Sleep for a short period of time
				 */
				try {
					Thread.sleep(POLL_DELAY);
				} catch (final InterruptedException ie) {
					LOG.warn(ie.getLocalizedMessage(), ie);
				}
				/*
				 * Increment timer
				 */
				timer += POLL_DELAY;
				/*
				 * Check to see if time limit exceeded
				 */
				if (timer > delay) {
					/*
					 * Can't connect to server
					 */
					throw new InterruptedIOException("Could not connect for " + delay + " milliseconds");
				}
			}
			/*
			 * Return established socket
			 */
			return sock;
		}

		/**
		 * Attempts to connect to a service at the specified address and port,
		 * for a specified maximum amount of time.
		 * 
		 * @param host
		 *            Address of host
		 * @param port
		 *            Port of service
		 * @param delay
		 *            Delay in milliseconds
		 * @return The established client socket
		 * @throws InterruptedIOException
		 *             If socket connect times out
		 * @throws IOException
		 *             If an I/O error occurs
		 */
		public static Socket getSocket(final String host, final int port, final int delay)
				throws InterruptedIOException, IOException {
			/*
			 * Convert host into an InetAddress, and call getSocket method
			 */
			return getSocket(InetAddress.getByName(host), port, delay);
		}
	}

	/**
	 * {@link SocketThread} - Establishes a socket
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class SocketThread extends Thread {

		/**
		 * Socket connection to remote host
		 */
		volatile private Socket m_connection = null;

		/**
		 * Host name to connect to
		 */
		private String m_host = null;

		/**
		 * Internet Address to connect to
		 */
		private InetAddress m_inet = null;

		/**
		 * Port number to connect to
		 */
		private int m_port = 0;

		/**
		 * Exception in the event a connection error occurs
		 */
		private IOException m_exception = null;

		/**
		 * Initializes a new {@link SocketThread}
		 * 
		 * @param host
		 *            The host name
		 * @param port
		 *            The port
		 */
		public SocketThread(final String host, final int port) {
			m_host = host;
			m_port = port;
		}

		/**
		 * Initializes a new {@link SocketThread}
		 * 
		 * @param inetAddr
		 *            The internet address
		 * @param port
		 *            The port
		 */
		public SocketThread(final InetAddress inetAddr, final int port) {
			m_inet = inetAddr;
			m_port = port;
		}

		@Override
		public void run() {
			/*
			 * Socket used for establishing a connection
			 */
			Socket sock = null;
			try {
				/*
				 * Was a string or an internet address specified?
				 */
				if (m_host != null) {
					/*
					 * Connect to a remote host - BLOCKING I/O
					 */
					sock = new Socket(m_host, m_port);
				} else {
					/*
					 * Connect to a remote host - BLOCKING I/O
					 */
					sock = new Socket(m_inet, m_port);
				}
			} catch (final IOException ioe) {
				m_exception = ioe;
				return;
			}
			/*
			 * If socket constructor returned without error, then connection
			 * finished
			 */
			m_connection = sock;
		}

		public boolean isConnected() {
			return (m_connection != null);
		}

		public boolean isError() {
			return (m_exception != null);
		}

		public Socket getSocket() {
			return m_connection;
		}

		public IOException getException() {
			return m_exception;
		}
	}
}
