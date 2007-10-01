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
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.user2acl.User2ACL.IMAPServer;
import com.openexchange.imap.user2acl.User2ACL.User2ACLException;
import com.openexchange.mail.config.MailConfigException;

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

	private static final String CHARSET_US_ASCII = "US-ASCII";

	/**
	 * Prevent instantiation
	 */
	private User2ACLAutoDetector() {
		super();
	}

	/**
	 * Determines the {@link User2ACL} impl dependent on MAP server's
	 * greeting, given the IMAP server's name
	 * 
	 * <p>
	 * The IMAP server name can either be a machine name, such as "<code>java.sun.com</code>",
	 * or a textual representation of its IP address.
	 * 
	 * @param imapServer -
	 *            the IMAP server's name
	 * @param imapPort -
	 *            the IMAP server's port
	 * @return the IMAP server's depending {@link User2ACL}
	 *         implementation
	 * @throws IOException -
	 *             if an I/O error occurs
	 * @throws User2ACLException -
	 *             if a server greeting could not be mapped to a supported IMAP
	 *             server
	 */
	public static User2ACL getUser2ACLImpl(final String imapServer, final int imapPort) throws IOException,
			User2ACLException {
		final InetAddress key = InetAddress.getByName(imapServer);
		User2ACL impl = map.get(key);
		if (impl == null) {
			impl = loadUser2ACLImpl(key, imapPort);
		}
		return impl;
	}

	private static IMAPServer mapInfo2IMAPServer(final String info) throws User2ACLException {
		final IMAPServer[] iServers = IMAPServer.values();
		for (int i = 0; i < iServers.length; i++) {
			if (toLowerCase(info).indexOf(toLowerCase(iServers[i].getName())) > -1) {
				return iServers[i];
			}
		}
		try {
			if (!IMAPConfig.isSupportsACLs()) {
				/*
				 * Return fallback implementation
				 */
				if (LOG.isWarnEnabled()) {
					LOG.warn(new StringBuilder(512)
						.append("No IMAP server found that corresponds to greeting:\r\n\"")
						.append(info.replaceAll("\r?\n", ""))
						.append("\"\r\nSince ACLs are disabled (through IMAP configuration) or not supported by IMAP server, \"")
						.append(IMAPServer.CYRUS.getName()).append("\" is used as fallback."));
				}
				return IMAPServer.CYRUS;
			}
		} catch (final MailConfigException e) {
			throw new User2ACLException(e);
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

	private static User2ACL loadUser2ACLImpl(final InetAddress inetAddress, final int imapPort) throws IOException,
			User2ACLException {
		CONTACT_LOCK.lock();
		try {
			User2ACL user2Acl = map.get(inetAddress);
			if (user2Acl != null) {
				return user2Acl;
			}
			Socket s = null;
			InputStreamReader isr = null;
			try {
				try {
					s = new Socket(inetAddress, imapPort);
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
				final IMAPServer imapServer = mapInfo2IMAPServer(sb.toString());
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

}
