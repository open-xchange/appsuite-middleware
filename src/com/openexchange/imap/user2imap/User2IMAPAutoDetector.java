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

package com.openexchange.imap.user2imap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.imap.user2imap.User2IMAP.IMAPServer;
import com.openexchange.imap.user2imap.User2IMAP.User2IMAPException;

/**
 * User2IMAPAutoDetector
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class User2IMAPAutoDetector {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(User2IMAPAutoDetector.class);

	private static final Object[] EMPTY_ARGS = new Object[0];

	private static final Map<InetAddress, User2IMAP> map = new HashMap<InetAddress, User2IMAP>();

	private static final Lock CONTACT_LOCK = new ReentrantLock();

	private static final int BUFSIZE = 512;

	private static final String IMAPCMD_LOGOUT = "A11 LOGOUT\r\n";

	private static final String CHARSET_US_ASCII = "US-ASCII";

	/**
	 * Prevent instanciation
	 */
	private User2IMAPAutoDetector() {
		super();
	}

	/**
	 * Determines the <code>User2IMAP</code> impl dependent on MAP server's
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
	 * @return the IMAP server's depending <code>User2IMAP</code>
	 *         implementation
	 * @throws IOException -
	 *             if an I/O error occurs
	 * @throws User2IMAPException -
	 *             if a server greeting could not be mapped to a supported IMAP
	 *             server
	 */
	public static User2IMAP getUser2IMAPImpl(final String imapServer, final int imapPort) throws IOException,
			User2IMAPException {
		final InetAddress key = InetAddress.getByName(imapServer);
		User2IMAP impl = map.get(key);
		if (impl == null) {
			impl = loadUser2IMAPImpl(key, imapPort);
		}
		return impl;
	}

	private static IMAPServer mapInfo2IMAPServer(final String info) throws User2IMAPException {
		final IMAPServer[] iServers = IMAPServer.values();
		for (int i = 0; i < iServers.length; i++) {
			if (toLowerCase(info).indexOf(toLowerCase(iServers[i].getName())) > -1) {
				return iServers[i];
			}
		}
		throw new User2IMAPException(User2IMAPException.Code.UNKNOWN_IMAP_SERVER, info);
	}

	private static String toLowerCase(final String str) {
		final char[] buf = new char[str.length()];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = Character.toLowerCase(str.charAt(i));
		}
		return new String(buf);
	}

	private static User2IMAP loadUser2IMAPImpl(final InetAddress inetAddress, final int imapPort) throws IOException,
			User2IMAPException {
		CONTACT_LOCK.lock();
		try {
			User2IMAP user2IMAP;
			if ((user2IMAP = map.get(inetAddress)) != null) {
				return user2IMAP;
			}
			Socket s = null;
			InputStreamReader isr = null;
			try {
				s = new Socket(inetAddress, imapPort);
				isr = new InputStreamReader(s.getInputStream(), CHARSET_US_ASCII);
				final StringBuilder sb = new StringBuilder(BUFSIZE);
				final char[] buf = new char[BUFSIZE];
				int bytesRead = -1;
				if ((bytesRead = isr.read(buf, 0, buf.length)) != -1) {
					sb.append(buf, 0, bytesRead);
				}
				final IMAPServer imapServer = mapInfo2IMAPServer(sb.toString());
				try {
					user2IMAP = Class.forName(imapServer.getImpl()).asSubclass(User2IMAP.class).newInstance();
				} catch (final InstantiationException e) {
					throw new User2IMAPException(User2IMAPException.Code.INSTANCIATION_FAILED, e, EMPTY_ARGS);
				} catch (final IllegalAccessException e) {
					throw new User2IMAPException(User2IMAPException.Code.INSTANCIATION_FAILED, e, EMPTY_ARGS);
				} catch (final ClassNotFoundException e) {
					throw new User2IMAPException(User2IMAPException.Code.INSTANCIATION_FAILED, e, EMPTY_ARGS);
				}
				s.getOutputStream().write(IMAPCMD_LOGOUT.getBytes(CHARSET_US_ASCII));
				s.getOutputStream().flush();
				while ((bytesRead = isr.read(buf, 0, buf.length)) != -1) {
					sb.append(buf, 0, bytesRead);
				}
				map.put(inetAddress, user2IMAP);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(inetAddress.toString()).append(
							"] greeting successfully mapped to: ").append(imapServer.getName()));
				}
				return user2IMAP;
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
