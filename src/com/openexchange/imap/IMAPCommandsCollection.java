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

import static com.openexchange.imap.sort.IMAPSort.getMessageComparator;
import static com.openexchange.imap.utils.IMAPStorageUtility.getFetchProfile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.InternetHeaders;

import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.command.IMAPNumArgSplitter;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailStorageUtils.OrderDirection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.tools.Collections.SmartIntArray;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.UID;

/**
 * {@link IMAPCommandsCollection} - a collection of simple IMAP commands
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPCommandsCollection {

	private static final String ERR_UID_STORE_NOT_SUPPORTED = "UID STORE not supported";

	private static final String STR_INVALID_SYSTEM_FLAG = "Invalid system flag";

	private static final String ERR_INVALID_SYSTEM_FLAG_DETECTED = "Invalid System Flag detected";

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

	/**
	 * The reason for this routine is because the javamail library checks for
	 * existence when attempting to alter the subscription on the specified
	 * folder. The problem is that we might be subscribed to a folder that
	 * either no longer exists (deleted by another IMAP client), or that we
	 * simply do not currently have access permissions to (a shared folder that
	 * we are no longer permitted to see.) Either way, we need to be able to
	 * unsubscribe to that folder if so desired. The current javamail routines
	 * will not allow us to do that.
	 * <P>
	 * (Technically this is rather wrong of them. The IMAP spec makes it very
	 * clear that folder subscription should NOT depend upon the existence of
	 * said folder. They even demonstrate a case in which it might indeed be
	 * valid to be subscribed to a folder that does not appear to exist at a
	 * given moment.)
	 * <P>
	 */
	public static void forceSetSubscribed(final Store store, final String folder, final boolean subscribe) {
		final String lfolder = BASE64MailboxEncoder.encode(folder);
		final String cmd = (subscribe ? "SUBSCRIBE" : "UNSUBSCRIBE");
		try {
			final IMAPFolder f = (IMAPFolder) store.getDefaultFolder();
			// Object val =
			f.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
				public Object doCommand(final IMAPProtocol p) {
					final Argument args = new Argument();
					args.writeString(lfolder);
					// Response[] r =
					p.command(cmd, args);
					return null;
				}
			});
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private final static String TEMPL_UID_STORE_FLAGS = "UID STORE %s %sFLAGS (%s)";

	private static final String ALL_COLOR_LABELS = "cl_0 cl_1 cl_2 cl_3 cl_4 cl_5 cl_6 cl_7 cl_8 cl_9 cl_10";

	/**
	 * <p>
	 * Clears all set color label (which are stored as user flags) from messages
	 * which correpond to given UIDs
	 * <p>
	 * All known color labels:
	 * <code>cl_0 cl_1 cl_2 cl_3 cl_4 cl_5 cl_6 cl_7 cl_8 cl_9 cl_10</code>
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param msgUIDs -
	 *            the message UIDs
	 * @return <code>true</code> if everything went fine; otherwise
	 *         <code>false</code>
	 * @throws ProtocolException -
	 *             if an error occurs in underlying protocol
	 */
	public static boolean clearAllColorLabels(final IMAPFolder imapFolder, final long[] msgUIDs)
			throws ProtocolException {
		final String[] args = IMAPNumArgSplitter.splitUIDArg(msgUIDs, false);
		final IMAPProtocol p = imapFolder.getProtocol();
		Response[] r = null;
		Response response = null;
		Next: for (int i = 0; i < args.length; i++) {
			r = p.command(String.format(TEMPL_UID_STORE_FLAGS, args[i], "-", ALL_COLOR_LABELS), null);
			response = r[r.length - 1];
			try {
				if (response.isOK()) {
					continue Next;
				} else if (response.isBAD() && response.getRest() != null
						&& response.getRest().indexOf(STR_INVALID_SYSTEM_FLAG) != -1) {
					throw new ProtocolException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR,
							ERR_INVALID_SYSTEM_FLAG_DETECTED));
				} else {
					throw new ProtocolException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR,
							ERR_UID_STORE_NOT_SUPPORTED));
				}
			} finally {
				p.notifyResponseHandlers(r);
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

	/**
	 * Applies the given color flag as an user flag to the messages
	 * correpsonding to given UIDS
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param msgUIDs -
	 *            the message UIDs
	 * @param colorLabelFlag -
	 *            the color label
	 * @return <code>true</code> if everything went fine; otherwise
	 *         <code>false</code>
	 * @throws ProtocolException -
	 *             if an error occurs in underlying protocol
	 */
	public static boolean setColorLabel(final IMAPFolder imapFolder, final long[] msgUIDs, final String colorLabelFlag)
			throws ProtocolException {
		final String[] args = IMAPNumArgSplitter.splitUIDArg(msgUIDs, false);
		final IMAPProtocol p = imapFolder.getProtocol();
		Response[] r = null;
		Response response = null;
		Next: for (int i = 0; i < args.length; i++) {
			r = p.command(String.format(TEMPL_UID_STORE_FLAGS, args[i], "+", colorLabelFlag), null);
			response = r[r.length - 1];
			try {
				if (response.isOK()) {
					continue Next;
				} else if (response.isBAD() && response.getRest() != null
						&& response.getRest().indexOf(STR_INVALID_SYSTEM_FLAG) != -1) {
					throw new ProtocolException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR,
							ERR_INVALID_SYSTEM_FLAG_DETECTED));
				} else {
					throw new ProtocolException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR,
							ERR_UID_STORE_NOT_SUPPORTED));
				}
			} finally {
				p.notifyResponseHandlers(r);
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
					&& response.getRest().indexOf(STR_INVALID_SYSTEM_FLAG) != -1) {
				throw new ProtocolException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR,
						ERR_INVALID_SYSTEM_FLAG_DETECTED));
			} else {
				throw new ProtocolException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR,
						ERR_UID_STORE_NOT_SUPPORTED));
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

	private static final String FETCH_FLAGS = "FETCH 1:* (FLAGS UID)";

	private static final Pattern PATTERN_UID = Pattern.compile("uid\\s([0-9]+)");

	/**
	 * Gets all messages marked as deleted in given IMAP folder
	 * 
	 * @param imapFolder
	 *            The IMAP folder
	 * @param filter
	 *            The filter whose elements are going to be removed from return
	 *            value
	 * @return All messages marked as deleted in given IMAP folder filtered by
	 *         specified <code>filter</code>
	 * @throws ProtocolException
	 *             If a protocol error occurs
	 */
	public static long[] getDeletedMessages(final IMAPFolder imapFolder, final long[] filter) throws ProtocolException {
		final IMAPProtocol p = imapFolder.getProtocol();
		final Response[] r = p.command(FETCH_FLAGS, null);
		final Response response = r[r.length - 1];
		try {
			if (response.isOK()) {
				final Set<Long> set = new TreeSet<Long>();
				for (int i = 0, len = r.length - 1; i < len; i++) {
					if (!(r[i] instanceof IMAPResponse)) {
						continue;
					}
					final IMAPResponse ir = (IMAPResponse) r[i];
					String rest = null;
					if (ir.keyEquals("FETCH") && (rest = ir.toString().toLowerCase()).indexOf("\\deleted") != -1) {
						final Matcher m = PATTERN_UID.matcher(rest);
						if (m.find()) {
							set.add(Long.valueOf(m.group(1)));
						}
					}
					r[i] = null;
				}
				if (filter != null && filter.length > 0) {
					final Set<Long> tmp = new HashSet<Long>();
					for (int i = 0; i < filter.length; i++) {
						tmp.add(Long.valueOf(filter[i]));
					}
					set.removeAll(tmp);
				}
				final long[] retval = new long[set.size()];
				int i = 0;
				for (final Long l : set) {
					retval[i++] = l.longValue();
				}
				return retval;
			}
			throw new ProtocolException("FETCH command failed");
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
					return new long[0];
				}
				throw cfe;
			}
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
						&& response.getRest().indexOf(STR_INVALID_SYSTEM_FLAG) != -1) {
					throw new ProtocolException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR,
							ERR_INVALID_SYSTEM_FLAG_DETECTED));
				} else {
					throw new ProtocolException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR,
							ERR_UID_STORE_NOT_SUPPORTED));
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

	private static final Pattern PATTERN_PERMANENTFLAGS = Pattern
			.compile("(\\[PERMANENTFLAGS\\s\\()([^\\)\\]]*)(\\)\\]\\s*)");

	private static final Pattern PATTERN_USER_FLAG = Pattern.compile("(?:\\\\\\*|(?:(^|\\s)([^\\\\]\\S+)($|\\s)))");

	/**
	 * Applies the IMAPv4 SELECT command on given folder and returns whether its
	 * permanent flags supports user-defined flags or not
	 * 
	 * @param imapFolder
	 *            The IMAP folder to check
	 * @return <code>true</code> if user flags are supported; otherwise
	 *         <code>false</code>
	 * @throws MessagingException
	 *             If SELECT command fails
	 */
	public static boolean supportsUserDefinedFlags(final IMAPFolder imapFolder) throws MessagingException {
		final Boolean val = (Boolean) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
			 */
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				final String command = new StringBuilder("SELECT ").append(
						prepareStringArgument(imapFolder.getFullName())).toString();
				final Response[] r = p.command(command, null);
				final Response response = r[r.length - 1];
				Boolean retval = Boolean.FALSE;
				try {
					if (response.isOK()) {
						NextResp: for (int i = 0, len = r.length - 1; i < len; i++) {
							if (!(r[i] instanceof IMAPResponse)) {
								continue;
							}
							final Matcher matcher = PATTERN_PERMANENTFLAGS.matcher(((IMAPResponse) r[i]).getRest());
							if (matcher.find() && PATTERN_USER_FLAG.matcher(matcher.group(2)).find()) {
								retval = Boolean.TRUE;
								break NextResp;
							}
							r[i] = null;
						}
					} else {
						return retval;
					}
				} finally {
					p.notifyResponseHandlers(r);
					p.handleResult(response);
				}
				return retval;
			}
		});
		return val.booleanValue();
	}

	private static final String COMMAND_FETCH_OXMARK_RFC = "FETCH *:1 (UID RFC822.HEADER.LINES ("
			+ MessageHeaders.HDR_X_OX_MARKER + "))";

	private static final String COMMAND_FETCH_OXMARK_REV1 = "FETCH *:1 (UID BODY.PEEK[HEADER.FIELDS ("
			+ MessageHeaders.HDR_X_OX_MARKER + ")])";

	private static interface HeaderStream {
		public InputStream getStream(Item fetchItem);
	}

	private static HeaderStream REV1HeaderStream = new HeaderStream() {
		public InputStream getStream(final Item fetchItem) {
			return ((BODY) fetchItem).getByteArrayInputStream();
		}
	};

	private static HeaderStream RFCHeaderStream = new HeaderStream() {
		public InputStream getStream(final Item fetchItem) {
			return ((RFC822DATA) fetchItem).getByteArrayInputStream();
		}
	};

	private static HeaderStream getHeaderStream(final boolean isREV1) {
		if (isREV1) {
			return REV1HeaderStream;
		}
		return RFCHeaderStream;
	}

	/**
	 * Searches the message whose {@link MessageHeaders#HDR_X_OX_MARKER} header
	 * is set to specified marker
	 * 
	 * @param marker
	 *            The marker to lookup
	 * @param imapFolder
	 *            The IMAP folder in which to search the message
	 * @return The matching message's UID or <code>-1</code> if none found
	 * @throws MessagingException
	 */
	public static long findMarker(final String marker, final IMAPFolder imapFolder) throws MessagingException {
		if (marker == null || marker.length() == 0) {
			return -1L;
		}
		final Long retval = (Long) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
			 */
			public Object doCommand(final IMAPProtocol p) throws ProtocolException {
				final Response[] r;
				if (p.isREV1()) {
					r = p.command(COMMAND_FETCH_OXMARK_REV1, null);
				} else {
					r = p.command(COMMAND_FETCH_OXMARK_RFC, null);
				}
				final Response response = r[r.length - 1];
				final Long retval = Long.valueOf(-1L);
				try {
					if (response.isOK()) {
						int index = -1;
						HeaderStream headerStream = null;
						for (int i = 0, len = r.length - 1; i < len; i++) {
							if (!(r[i] instanceof FetchResponse)) {
								continue;
							}
							final FetchResponse fetchResponse = (FetchResponse) r[i];
							if (index == -1) {
								/*
								 * Remember index of header item
								 */
								final int count = fetchResponse.getItemCount();
								for (int k = 0; k < count && index == -1; k++) {
									if (BODY.class.isInstance(fetchResponse.getItem(k))
											|| RFC822DATA.class.isInstance(fetchResponse.getItem(k))) {
										index = k;
									}
								}
								if (index == -1) {
									throw new ProtocolException("No header item found in FETCH response");
								}
							}
							final Enumeration e;
							{
								if (null == headerStream) {
									headerStream = getHeaderStream(p.isREV1());
								}
								final InternetHeaders h = new InternetHeaders();
								h.load(headerStream.getStream(fetchResponse.getItem(index)));
								e = h.getAllHeaders();
							}
							if (e.hasMoreElements() && marker.equals(((Header) e.nextElement()).getValue())) {
								return Long.valueOf(((UID) (index == 0 ? fetchResponse.getItem(1) : fetchResponse
										.getItem(0))).uid);
							}
							r[i] = null;
						}
					}
				} catch (final MessagingException e) {
					final ProtocolException pex = new ProtocolException(e.getLocalizedMessage());
					pex.setStackTrace(e.getStackTrace());
					throw pex;
				} finally {
					// p.notifyResponseHandlers(r);
					p.handleResult(response);
				}
				return retval;
			}
		});
		return retval.longValue();
	}

	private static final String COMMAND_FETCH_ENV_UID = "FETCH *:1 (ENVELOPE UID)";

	/**
	 * Finds corresponding UID of message whose Message-ID header matches given
	 * message ID
	 * 
	 * @param messageId
	 *            The message ID
	 * @param imapFolder
	 *            The IMAP folder
	 * @return The UID of matching message or <code>-1</code> if none found
	 * @throws MessagingException
	 */
	public static long messageId2UID(final String messageId, final IMAPFolder imapFolder) throws MessagingException {
		if (messageId == null || messageId.length() == 0) {
			return -1L;
		}
		final Long retval = (Long) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
			 */
			public Object doCommand(final IMAPProtocol p) throws ProtocolException {
				final Response[] r = p.command(COMMAND_FETCH_ENV_UID, null);
				final Response response = r[r.length - 1];
				final Long retval = Long.valueOf(-1L);
				try {
					if (response.isOK()) {
						int index = -1;
						for (int i = 0, len = r.length - 1; i < len; i++) {
							if (!(r[i] instanceof FetchResponse)) {
								continue;
							}
							final FetchResponse fetchResponse = (FetchResponse) r[i];
							if (index == -1) {
								/*
								 * Remember index of ENVELOPE item
								 */
								final int count = fetchResponse.getItemCount();
								for (int k = 0; k < count && index == -1; k++) {
									if (ENVELOPE.class.isInstance(fetchResponse.getItem(k))) {
										index = k;
									}
								}
								if (index == -1) {
									throw new ProtocolException("No ENVELOPE item found in FETCH response");
								}
							}
							if (messageId.equals(((ENVELOPE) fetchResponse.getItem(index)).messageId)) {
								return Long.valueOf(((UID) (index == 0 ? fetchResponse.getItem(1) : fetchResponse
										.getItem(0))).uid);
							}
							r[i] = null;
						}
					}
				} finally {
					// p.notifyResponseHandlers(r);
					p.handleResult(response);
				}
				return retval;
			}
		});
		return retval.longValue();
	}

	private static final Pattern PATTERN_QUOTE_ARG = Pattern.compile("[\\s\\*%\\(\\)\\{\\}\"\\\\]");

	private static final Pattern PATTERN_ESCAPE_ARG = Pattern.compile("[\"\\\\]");

	private final static String REPLPAT_QUOTE = "\"";

	private final static String REPLACEMENT_QUOTE = "\\\\\\\"";

	private final static String REPLPAT_BACKSLASH = "(\\\\)([^\"])";

	private final static String REPLACEMENT_BACKSLASH = "\\\\\\\\$2";

	/**
	 * First encodes given fullname by using
	 * <code>com.sun.mail.imap.protocol.BASE64MailboxEncoder.encode()</code>
	 * method. Afterwards encoded string is checked if it needs quoting and
	 * escaping of the special characters '"' and '\'.
	 * 
	 * @param fullname
	 *            The folder fullname
	 * @return Prepared fullname ready for being used in raw IMAP commands
	 */
	public static String prepareStringArgument(final String fullname) {
		/*
		 * Ensure to have only ASCII characters
		 */
		final String lfolder = BASE64MailboxEncoder.encode(fullname);
		/*
		 * Determine if quoting (and escaping) has to be done
		 */
		final boolean quote = PATTERN_QUOTE_ARG.matcher(lfolder).find();
		final boolean escape = PATTERN_ESCAPE_ARG.matcher(lfolder).find();
		final StringBuilder sb = new StringBuilder(lfolder.length() + 8);
		if (escape) {
			sb.append(lfolder.replaceAll(REPLPAT_QUOTE, REPLACEMENT_QUOTE).replaceAll(REPLPAT_BACKSLASH,
					REPLACEMENT_BACKSLASH));
		} else {
			sb.append(lfolder);
		}
		if (quote) {
			/*
			 * Surround with quotes
			 */
			sb.insert(0, '"');
			sb.append('"');
		}
		return sb.toString();
	}
}
