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

package com.openexchange.groupware.imap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * IMAPServerInfo
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPServerInfo {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPServerInfo.class);

	private static final Map<InetAddress, String> map = new HashMap<InetAddress, String>();

	private static final Lock CONTACT_LOCK = new ReentrantLock();

	private static final int BUFSIZE = 512;

	private static final String IMAPCMD_LOGOUT = "A11 LOGOUT\r\n";

	private static final String CHARSET_US_ASCII = "US-ASCII";

	/**
	 * Prevent instanciation
	 */
	private IMAPServerInfo() {
		super();
	}

	/**
	 * Determines the IMAP server's greeting, given the IMAP server's name
	 * 
	 * <p>
	 * The IMAP server name can either be a machine name, such as "<code>java.sun.com</code>",
	 * or a textual representation of its IP address.
	 * 
	 * @param imapServer -
	 *            the IMAP server's name
	 * @param imapPort -
	 *            the IMAP server's port
	 * @return the IMAP server's greeting
	 * @throws IOException -
	 *             if an I/O error occurs
	 */
	public static String getIMAPServerInfo(final String imapServer, final int imapPort) throws IOException {
		final InetAddress key = InetAddress.getByName(imapServer);
		String info = map.get(key);
		if (info == null) {
			info = loadIMAPServerInfo(key, imapPort);
		}
		return info;
	}

	private static String loadIMAPServerInfo(final InetAddress inetAddress, final int imapPort) throws IOException {
		CONTACT_LOCK.lock();
		try {
			String info;
			if ((info = map.get(inetAddress)) != null) {
				return info;
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
				info = sb.toString();
				s.getOutputStream().write(IMAPCMD_LOGOUT.getBytes(CHARSET_US_ASCII));
				s.getOutputStream().flush();
				while ((bytesRead = isr.read(buf, 0, buf.length)) != -1) {
					sb.append(buf, 0, bytesRead);
				}
				map.put(inetAddress, info);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(inetAddress.toString()).append(
							"] greeting successfully fetched:\n\t").append(info));
				}
				return info;
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
