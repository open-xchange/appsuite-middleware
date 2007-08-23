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

import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.openexchange.imap.command.IMAPNumArgSplitter;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

/**
 * {@link IMAPCommandsCollection} - a collection of simple IMAP commands
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPCommandsCollection {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPCommandsCollection.class);

	private static final String PROTOCOL_ERROR_TEMPL = "Server does not support %s command";

	/**
	 * Prevent instantiation
	 */
	private IMAPCommandsCollection() {
		super();
	}

	/**
	 * Sorts given messages according to specified sort field and specified sort
	 * direction
	 * 
	 * @param folder
	 *            The IMAP folder
	 * @param sortCrit
	 *            The IMAP sort criteria
	 * @param toSort
	 *            The messages to sort
	 * @return sorted array of {@link Message} instances
	 * @throws MessagingException
	 */
	public static Message[] getServerSortList(final IMAPFolder folder, final String sortCrit, final Message[] toSort)
			throws MessagingException {
		return getServerSortList(folder, sortCrit, IMAPNumArgSplitter.splitMessageArg(toSort, false));
	}

	private static final String[] RANGE_ALL = { "ALL" };

	/**
	 * Sorts all messages according to specified sort field and specified sort
	 * direction
	 * 
	 * @param folder
	 *            The IMAP folder
	 * @param sortCrit
	 *            The IMAP sort criteria
	 * @return sorted array of {@link Message} instances
	 * @throws MessagingException
	 */
	public static Message[] getServerSortList(final IMAPFolder folder, final String sortCrit) throws MessagingException {
		return getServerSortList(folder, sortCrit, RANGE_ALL);
	}

	/**
	 * Get a server-side sorted list
	 */
	@SuppressWarnings("unchecked")
	public static Message[] getServerSortList(final IMAPFolder imapFolder, final String sortCrit, final String[] mdat)
			throws MessagingException {
		if (mdat == null || mdat.length == 0) {
			throw new MessagingException("IMAP sort failed: Empty message num argument.");
		} else if (mdat.length > 1) {
			throw new MessagingException("IMAP sort failed: Message num argumet too long.");
		}
		/*
		 * Call the IMAPFolder.doCommand() method with inner class definition of
		 * ProtocolCommand
		 */
		final Object val = imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				Response[] r = p.command(new StringBuilder(mdat[0].length() + 16).append("SORT (").append(sortCrit)
						.append(") UTF-8 ").append(mdat[0]).toString(), null);
				Response response = r[r.length - 1];
				List<Message> list = new ArrayList<Message>();
				try {
					if (response.isOK()) {
						for (int i = 0, len = r.length; i < len; i++) {
							if (!(r[i] instanceof IMAPResponse)) {
								continue;
							}
							IMAPResponse ir = (IMAPResponse) r[i];
							if (ir.keyEquals("SORT")) {
								String num;
								while ((num = ir.readAtomString()) != null) {
									try {
										Message msg = imapFolder.getMessage(Integer.parseInt(num));
										list.add(msg);
									} catch (NumberFormatException e) {
										LOG.error(e.getMessage(), e);
										throw new ProtocolException("Invalid Message Number: " + num);
									} catch (MessagingException e) {
										LOG.error(e.getMessage(), e);
										throw new ProtocolException(e.getMessage());
									}
									// list.add(num);
								}
							}
							r[i] = null;
						}
					} else {
						throw new ProtocolException(String.format(PROTOCOL_ERROR_TEMPL, "SORT"));
					}
				} finally {
					p.notifyResponseHandlers(r);
					p.handleResult(response);
				}
				return list;
			}
		});
		final List<Message> l = ((List<Message>) val);
		return l.toArray(new Message[l.size()]);
	}

}
