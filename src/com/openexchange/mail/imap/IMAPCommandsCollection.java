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

import static com.openexchange.mail.imap.IMAPStorageUtils.getFetchProfile;
import static com.openexchange.mail.imap.sort.IMAPSort.getMessageComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.openexchange.imap.OXMailException;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.command.IMAPNumArgSplitter;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailStorageUtils.OrderDirection;
import com.openexchange.tools.Collections.SmartIntArray;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
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

	private static final String ERR_01 = "No matching messages";

	/**
	 * Prevent instantiation
	 */
	private IMAPCommandsCollection() {
		super();
	}

	private static final String COMMAND_NOOP = "NOOP";

	/**
	 * Force to send a NOOP command to IMAP server that is explicitely <b>not</b>
	 * handled by JavaMail API. It really does not matter if this command
	 * succeeds or breaks up in a <code>MessagingException</code>. Therefore
	 * neither a return value is defined nor any exception is thrown
	 */
	public static void forceNoopCommand(final IMAPFolder f) {
		try {
			f.doCommand(new IMAPFolder.ProtocolCommand() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
				 */
				public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
					final Response[] r = protocol.command(COMMAND_NOOP, null);
					/*
					 * Grab last response that should indicate an OK
					 */
					final Response response = r[r.length - 1];
					if (response.isOK()) {
						return Boolean.TRUE;
					}
					return Boolean.FALSE;
				}

			});
		} catch (final MessagingException e) {
			if (LOG.isTraceEnabled()) {
				LOG.trace(e.getMessage(), e);
			}
		}
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

	private static final String COMMAND_SEARCH_UNSEEN = "SEARCH UNSEEN";

	private static final String COMMAND_SEARCH = "SEARCH";

	/**
	 * Determines all messages in given folder which have the \UNSEEN flag set
	 * and sorts them to criteria "REVERSE DATE"
	 */
	public static Message[] getNewMessages(final IMAPFolder folder, final MailListField[] fields,
			final MailListField sortField, final OrderDirection orderDir, final Locale locale)
			throws MessagingException {
		final IMAPFolder imapFolder = folder;
		final Message[] val = (Message[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
			 */
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				Response[] r = p.command(COMMAND_SEARCH_UNSEEN, null);
				/*
				 * Result is something like: * SEARCH 12 20 24
				 */
				int[] newMsgSeqNums = null;
				Response response = r[r.length - 1];
				{
					final SmartIntArray tmp = new SmartIntArray(32);
					try {
						if (response.isOK()) {
							for (int i = 0, len = r.length - 1; i < len; i++) {
								if (!(r[i] instanceof IMAPResponse)) {
									continue;
								}
								final IMAPResponse ir = (IMAPResponse) r[i];
								/*
								 * The SEARCH response from the server contains
								 * a listing of message sequence numbers
								 * corresponding to those messages that match
								 * the searching criteria.
								 */
								if (ir.keyEquals(COMMAND_SEARCH)) {
									String num;
									while ((num = ir.readAtomString()) != null) {
										try {
											tmp.append(Integer.parseInt(num));
										} catch (final NumberFormatException e) {
											continue;
										}
									}
								}
								r[i] = null;
							}
						} else {
							throw new ProtocolException(String.format(PROTOCOL_ERROR_TEMPL, COMMAND_SEARCH));
						}
					} finally {
						p.notifyResponseHandlers(r);
						p.handleResult(response);
					}
					newMsgSeqNums = tmp.toArray();
				}
				/*
				 * No new messages found
				 */
				if (newMsgSeqNums.length == 0) {
					return null;
				}
				/*
				 * Fetch messages and sort them
				 */
				final Message[] newMsgs;
				try {
					newMsgs = new FetchIMAPCommand(folder, newMsgSeqNums, getFetchProfile(fields, sortField), false,
							false).doCommand();
				} catch (final MessagingException e) {
					throw new ProtocolException(e.getLocalizedMessage());
				}
				final List<Message> msgList = Arrays.asList(newMsgs);
				Collections.sort(msgList, getMessageComparator(sortField, orderDir, locale));
				return msgList.toArray(newMsgs);
			}
		});
		return val;
	}

	private static final String COMMAND_EXPUNGE = "EXPUNGE";

	/**
	 * <p>
	 * Performs the <code>EXPUNGE</code> command on whole folder referenced by
	 * <code>imapFolder</code>.
	 * <p>
	 * <b>NOTE</b> folder's message cache is left in an inconsistent state
	 * cause its kept message references are not marked as expunged. Therefore
	 * the folder should be closed afterwards to force message cache update.
	 * 
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @return <code>true</code> if everything went fine; otherwise
	 *         <code>false</code>
	 * @throws ProtocolException -
	 *             if an error occurs in underlying protocol
	 */
	public static boolean fastExpunge(final IMAPFolder imapFolder) throws ProtocolException {
		final IMAPProtocol p = imapFolder.getProtocol();
		final Response[] r = p.command(COMMAND_EXPUNGE, null);
		final Response response = r[r.length - 1];
		try {
			if (response.isOK()) {
				return true;
			} else if (response.isBAD() && response.getRest() != null
					&& response.getRest().indexOf("Invalid system flag") != -1) {
				throw new ProtocolException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
						"Invalid System Flag detected"));
			} else {
				throw new ProtocolException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
						"UID STORE not supported"));
			}
		} finally {
			/*
			 * No invocation of notifyResponseHandlers() to avoid sequential (by
			 * message) folder cache update
			 */
			/* p.notifyResponseHandlers(r); */
			try {
				p.handleResult(response);
			} catch (final CommandFailedException cfe) {
				if (cfe.getMessage().indexOf(ERR_01) != -1) {
					/*
					 * Obviously this folder is empty
					 */
					return true;
				}
				throw cfe;
			}
		}
	}

	/**
	 * Determines if given folder is marked as read-only when performing a
	 * <code>SELECT</code> command on it.
	 * 
	 * @param folder
	 *            The IMAP folder
	 * @return <code>true</code> is IMAP folder is marked as READ-ONLY;
	 *         otherwise <code>false</code>
	 * @throws IMAPException
	 */
	public static boolean isReadOnly(final IMAPFolder f) throws IMAPException {
		try {
			final Boolean val = (Boolean) f.doCommand(new IMAPFolder.ProtocolCommand() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
				 */
				public Object doCommand(IMAPProtocol p) throws ProtocolException {
					/*
					 * Encode the mbox as per RFC2060
					 */
					Argument args = new Argument();
					args.writeString(BASE64MailboxEncoder.encode(f.getFullName()));
					/*
					 * Perform command
					 */
					final Response[] r = p.command("SELECT", args);
					/*
					 * Grab last response that should indicate an OK
					 */
					final Response response = r[r.length - 1];
					Boolean retval = Boolean.FALSE;
					if (response.isOK()) { // command succesful
						retval = Boolean.valueOf(response.toString().indexOf("READ-ONLY") != -1);
					}
					/* p.notifyResponseHandlers(r); */
					p.handleResult(response);
					return retval;
				}
			});
			return val.booleanValue();
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IMAPException(IMAPException.Code.FAILED_READ_ONLY_CHECK);
		}
	}

	private static final String TEMPL_UID_EXPUNGE = "UID EXPUNGE %s";

	/**
	 * <p>
	 * Performs the <code>EXPUNGE</code> command on messages identified
	 * through given <code>uids</code>
	 * <p>
	 * <b>NOTE</b> folder's message cache is left in an inconsistent state
	 * cause its kept message references are not marked as expunged. Therefore
	 * the folder should be closed afterwards to force message cache update.
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param uids -
	 *            the message UIDs
	 * @return <code>true</code> if everything went fine; otherwise
	 *         <code>false</code>
	 * @throws ProtocolException -
	 *             if an error occurs in underlying protocol
	 */
	public static boolean uidExpunge(final IMAPFolder imapFolder, final long[] uids) throws ProtocolException {
		final String[] args = IMAPNumArgSplitter.splitUIDArg(uids, false);
		final IMAPProtocol p = imapFolder.getProtocol();
		Response[] r = null;
		Response response = null;
		Next: for (int i = 0; i < args.length; i++) {
			r = p.command(String.format(TEMPL_UID_EXPUNGE, args[i]), null);
			response = r[r.length - 1];
			try {
				if (response.isOK()) {
					continue Next;
				} else if (response.isBAD() && response.getRest() != null
						&& response.getRest().indexOf("Invalid system flag") != -1) {
					throw new ProtocolException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
							"Invalid System Flag detected"));
				} else {
					throw new ProtocolException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
							"UID STORE not supported"));
				}
			} finally {
				/*
				 * No invocation of notifyResponseHandlers() to avoid sequential
				 * (by message) folder cache update
				 */
				/* p.notifyResponseHandlers(r); */
				try {
					p.handleResult(response);
				} catch (final CommandFailedException cfe) {
					if (cfe.getMessage().indexOf(ERR_01) != -1) {
						/*
						 * Obviously this folder is empty
						 */
						return true;
					}
					throw cfe;
				}
			}
		}
		return true;
	}

}
