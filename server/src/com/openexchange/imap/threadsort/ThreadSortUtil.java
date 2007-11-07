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

package com.openexchange.imap.threadsort;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Folder;
import javax.mail.MessagingException;

import com.openexchange.mail.mime.ContainerMessage;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

/**
 * ThreadSortUtil
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ThreadSortUtil {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ThreadSortUtil.class);

	/**
	 * Prevent instantiation
	 */
	private ThreadSortUtil() {
		super();
	}

	private static final Pattern PATTERN_THREAD_RESP = Pattern.compile("[0-9]+");

	/**
	 * @return an array of <code>javax.mail.Message</code> objects only filled
	 *         with message's sequence number
	 */
	public static ContainerMessage[] getMessagesFromThreadResponse(final String folderFullname, final char separator,
			final String threadResponse) {
		final Matcher m = PATTERN_THREAD_RESP.matcher(threadResponse);
		if (m.find()) {
			final List<ContainerMessage> tmp = new ArrayList<ContainerMessage>();
			do {
				tmp.add(new ContainerMessage(folderFullname, separator, Integer.parseInt(m.group())));
			} while (m.find());
			return tmp.toArray(new ContainerMessage[tmp.size()]);
		}
		return null;
	}

	/**
	 * @return parsed THREAD response in a structured data type
	 */
	public static List<TreeNode> parseThreadResponse(final String threadResponse) throws MessagingException {
		/*
		 * Now parse the odd THREAD response string.
		 */
		List<TreeNode> pulledUp = null;
		if (threadResponse.indexOf('(') != -1 && threadResponse.indexOf(')') != -1) {
			ThreadParser tp = new ThreadParser();
			try {
				tp.parse(threadResponse.substring(threadResponse.indexOf('('), threadResponse.lastIndexOf(')') + 1));
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
				throw new MessagingException(e.getMessage());
			}
			pulledUp = ThreadParser.pullUpFirst(tp.getParsedList());
			tp = null;
		}
		return pulledUp;
	}

	private static final String PROTOCOL_ERROR_TEMPL = "Server does not support %s command";

	private static final String STR_THREAD = "THREAD";

	/**
	 * @return THREAD response
	 */
	public static String getThreadResponse(final Folder folder, final StringBuilder mdat) throws MessagingException {
		final String data = mdat.toString();
		final IMAPFolder f = (IMAPFolder) folder;
		final Object val = f.doCommand(new IMAPFolder.ProtocolCommand() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
			 */
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				Response[] r = p.command("THREAD REFERENCES UTF-8 " + data, null);
				Response response = r[r.length - 1];
				String retval = null;
				try {
					if (response.isOK()) { // command successful
						for (int i = 0, len = r.length; i < len; i++) {
							if (!(r[i] instanceof IMAPResponse)) {
								continue;
							}
							IMAPResponse ir = (IMAPResponse) r[i];
							if (ir.keyEquals(STR_THREAD)) {
								retval = ir.toString();
							}
							r[i] = null;
						}
					} else {
						throw new ProtocolException(String.format(PROTOCOL_ERROR_TEMPL, STR_THREAD));
					}
				} finally {
					p.notifyResponseHandlers(r);
					p.handleResult(response);
				}
				return retval;
			}
		});
		return (String) val;
	}

}
