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

import static com.openexchange.groupware.container.mail.parser.MessageUtils.removeHdrLineBreak;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.container.mail.MessageCacheObject;
import com.openexchange.groupware.container.mail.MessageCacheObject.DummyAddress;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.imap.command.IMAPNumArgSplitter;
import com.openexchange.tools.Collections.SmartIntArray;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.UIDSet;

/**
 * IMAPUtils
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class IMAPUtils {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPUtils.class);

	private static final String PROTOCOL_ERROR_TEMPL = "Server does not support %s command";

	private IMAPUtils() {
		super();
	}

	private static final String ROOT_PATTERN = "*";

	/**
	 * Checks if given folder contains subdirectories
	 */
	public static boolean containsSubDir(final Store store, final IMAPFolder imapFolder) throws MessagingException {
		final String ref = new StringBuilder(imapFolder.getFullName()).append(imapFolder.getSeparator()).append('%')
				.toString();
		return containsSubDir(store, ref, ROOT_PATTERN);
	}

	private static boolean containsSubDir(final Store store, final String ref, final String root) {
		final String lref = BASE64MailboxEncoder.encode(ref);
		final String lroot = BASE64MailboxEncoder.encode(root);
		try {
			final IMAPFolder f = (IMAPFolder) store.getDefaultFolder();
			final Boolean val = (Boolean) f.doCommand(new IMAPFolder.ProtocolCommand() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
				 */
				public Object doCommand(IMAPProtocol p) throws ProtocolException {
					Boolean hasChildren = Boolean.FALSE;
					// Issue command
					Argument args = new Argument();
					args.writeString(lref);
					args.writeString(lroot);
					final Response[] r = p.command("LIST", args);
					final Response response = r[r.length - 1];
					try {
						// Grab response
						if (!response.isOK()) {
							/*
							 * Command failed
							 */
							throw new ProtocolException(String.format(PROTOCOL_ERROR_TEMPL, "LIST"));
						}
						NextResponse: for (int i = 0, len = r.length; i < len; i++) {
							if ((r[i] instanceof IMAPResponse)) {
								IMAPResponse ir = (IMAPResponse) r[i];
								if (ir.keyEquals("LIST")) {
									hasChildren = Boolean.TRUE;
									break NextResponse;
								}
							}
							r[i] = null;
						}
					} finally {
						// dispatch remaining untagged responses
						p.notifyResponseHandlers(r);
						p.handleResult(response);
					}
					return hasChildren;
				}
			});
			return val.booleanValue();
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			return false;
		}
	}

	private static final String STR_UID = "UID";

	private static final String TMPL_FETCH_HEADER_REV1 = "FETCH %s (BODY.PEEK[HEADER])";

	private static final String TMPL_FETCH_HEADER_NON_REV1 = "FETCH %s (RFC822.HEADER)";

	private static final Pattern PATTERN_PARSE_HEADER = Pattern
			.compile("(\\S+):\\s(.*)((?:\r?\n(?:\\s(?:.+)))*|(?:$))");

	/**
	 * Call this method if JavaMail's routine fails to load a message's header.
	 * Header is read in a safe manner and filled into given
	 * <tt>javax.mail.Message</tt> instance
	 */
	public static final Map<String, String> loadBrokenHeaders(final Message msg, final boolean uid)
			throws MessagingException, ProtocolException {
		final IMAPFolder fld = (IMAPFolder) msg.getFolder();
		final IMAPProtocol p = fld.getProtocol();
		final String tmpl = p.isREV1() ? TMPL_FETCH_HEADER_REV1 : TMPL_FETCH_HEADER_NON_REV1;
		final String cmd;
		if (uid) {
			cmd = new StringBuilder(50).append(STR_UID).append(' ').append(
					String.format(tmpl, Long.valueOf(fld.getUID(msg)))).toString();
		} else {
			cmd = String.format(tmpl, Integer.valueOf(msg.getMessageNumber()));
		}
		final Map<String, String> retval = new HashMap<String, String>();
		final Response[] r = p.command(cmd, null);
		final Response response = r[r.length - 1];
		try {
			if (response.isOK()) {
				final int len = r.length - 1;
				final StringBuilder valBuilder = new StringBuilder();
				NextResponse: for (int i = 0; i < len; i++) {
					if (r[i] == null) {
						continue NextResponse;
					} else if (!(r[i] instanceof FetchResponse)) {
						continue NextResponse;
					}
					final FetchResponse f = ((FetchResponse) r[i]);
					if (f.getNumber() != msg.getMessageNumber()) {
						continue NextResponse;
					}
					final Matcher m = PATTERN_PARSE_HEADER.matcher(f.toString());
					while (m.find()) {
						valBuilder.append(m.group(2));
						if (m.group(3) != null) {
							valBuilder.append(removeHdrLineBreak(m.group(3)));
						}
						retval.put(m.group(1), valBuilder.toString());
						valBuilder.setLength(0);
					}
					r[i] = null;
				}
			}
		} finally {
			// dispatch remaining untagged responses
			p.notifyResponseHandlers(r);
			p.handleResult(response);
		}
		return null;
	}

	private static final String COMMAND_NOOP = "NOOP";

	/**
	 * Force to send a NOOP command to IMAP server that is explicitely <b>not</b>
	 * handled by JavaMail API. It really does not matter if this command
	 * succeeds or breaks up in a <code>MessagingException</code>. Therefore
	 * neither a return value is defined nor any exception is thrown
	 */
	public static final void forceNoopCommand(final IMAPFolder f) {
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

	private static final String COMMAND_SEARCH = "SEARCH";

	/**
	 * Determines if given folder is marked as read-only when performing a
	 * SELECT command on it.
	 */
	public static boolean isReadOnly(final Folder folder) throws OXException {
		try {
			final IMAPFolder f = (IMAPFolder) folder;
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
			throw new OXMailException(MailCode.FAILED_READ_ONLY_CHECK);
		}
	}

	private static final String COMMAND_SEARCH_UNSEEN = "SEARCH UNSEEN";

	private static final String COMMAND_SORT = "SORT";

	private static final String COMMAND_SORT_REVERSE_DATE_PEFIX = "SORT (REVERSE DATE) UTF-8 ";

	/**
	 * Determines all messages in given folder which have the \UNSEEN flag set
	 * and sorts them to criteria "REVERSE DATE"
	 */
	public static Message[] getNewMessages(final IMAPFolder folder) throws MessagingException {
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
				Response response = r[r.length - 1];
				SmartIntArray tmp = new SmartIntArray(32);
				try {
					if (response.isOK()) {
						for (int i = 0, len = r.length - 1; i < len; i++) {
							if (!(r[i] instanceof IMAPResponse)) {
								continue;
							}
							final IMAPResponse ir = (IMAPResponse) r[i];
							/*
							 * The SEARCH response from the server contains a
							 * listing of message sequence numbers corresponding
							 * to those messages that match the searching
							 * criteria.
							 */
							if (ir.keyEquals(COMMAND_SEARCH)) {
								String num;
								while ((num = ir.readAtomString()) != null) {
									try {
										tmp.append(Integer.parseInt(num));
									} catch (NumberFormatException e) {
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
				final int[] newMsgSeqNums = tmp.toArray();
				tmp = null;
				/*
				 * No new messages found
				 */
				if (newMsgSeqNums.length == 0) {
					return null;
				}
				/*
				 * Sort new messages
				 */
				final String seqNumArg = IMAPNumArgSplitter.splitSeqNumArg(newMsgSeqNums)[0];
				final List<Message> newMsgs = new ArrayList<Message>(newMsgSeqNums.length);
				r = p.command(new StringBuilder(COMMAND_SORT_REVERSE_DATE_PEFIX).append(seqNumArg).toString(), null);
				response = r[r.length - 1];
				try {
					if (response.isOK()) {
						for (int i = 0, len = r.length - 1; i < len; i++) {
							if (!(r[i] instanceof IMAPResponse)) {
								continue;
							}
							IMAPResponse ir = (IMAPResponse) r[i];
							if (ir.keyEquals(COMMAND_SORT)) {
								String num;
								while ((num = ir.readAtomString()) != null) {
									try {
										newMsgs.add(new MessageCacheObject(imapFolder.getFullName(), imapFolder
												.getSeparator(), Integer.parseInt(num)));
									} catch (NumberFormatException e) {
										LOG.error(e.getMessage(), e);
										throw new ProtocolException("Invalid Message Number: " + num);
									} catch (MessagingException e) {
										LOG.error(e.getMessage(), e);
										throw new ProtocolException(e.getMessage());
									}
								}
							}
							r[i] = null;
						}
					}
				} finally {
					p.notifyResponseHandlers(r);
					p.handleResult(response);
				}
				return newMsgs.toArray(new Message[newMsgs.size()]);
			}
		});
		return val;
	}

	/**
	 * @return an array of <code>int</code> representing the sequence numbers
	 *         of unseen messages in selected folder. <code>sort</code>
	 *         determines whether sequence numbers get sorted or not.
	 */
	public static final int[] getNewMsgsSeqNums(final Folder folder, final boolean sort) throws MessagingException {
		if (!(folder instanceof IMAPFolder)) {
			throw new MessagingException("Given folder " + folder.getFullName() + " is not an instance of IMAPFolder");
		}
		final IMAPFolder imapFolder = (IMAPFolder) folder;
		final int[] val = (int[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
			 */
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				Response[] r = p.command("SEARCH UNSEEN", null);
				Response response = r[r.length - 1];
				final List<Integer> retvalList = new ArrayList<Integer>();
				try {
					if (response.isOK()) {
						for (int i = 0, len = r.length - 1; i < len; i++) {
							if (!(r[i] instanceof IMAPResponse)) {
								continue;
							}
							IMAPResponse ir = (IMAPResponse) r[i];
							/*
							 * The SEARCH response from the server contains a
							 * listing of message sequence numbers corresponding
							 * to those messages that match the searching
							 * criteria.
							 */
							if (ir.keyEquals(COMMAND_SEARCH)) {
								String num;
								while ((num = ir.readAtomString()) != null) {
									retvalList.add(Integer.valueOf(num));
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
				final int size = retvalList.size();
				/*
				 * No new messages found
				 */
				if (size == 0) {
					return new int[0];
				}
				if (sort) {
					Collections.sort(retvalList);
				}
				final int[] retval = new int[size];
				for (int i = 0; i < size; i++) {
					retval[i] = retvalList.get(i).intValue();
				}
				return retval;
			}
		});
		return val;
	}

	private static final Pattern PATTERN_PERMANENTFLAGS = Pattern.compile("(\\[PERMANENTFLAGS\\s\\()(.*)(\\)\\]\\s*)");

	private static final Pattern PATTERN_USER_FLAG = Pattern.compile("(?:\\\\\\*|(?:(^|\\s)([^\\\\]\\S+)($|\\s)))");

	/**
	 * Applies the IMAPv4 SELECT command on given mailbox and returns whether
	 * its permanent flags supports user-defined flags or not
	 */
	public static boolean supportsUserDefinedFlags(final Folder mailbox) throws MessagingException {
		if (!(mailbox instanceof IMAPFolder)) {
			throw new MessagingException("Given folder " + mailbox.getFullName() + " is not an instance of IMAPFolder");
		}
		final IMAPFolder imapFolder = (IMAPFolder) mailbox;
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
							if (matcher.matches() && PATTERN_USER_FLAG.matcher(matcher.group(2)).find()) {
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

	/**
	 * @return the next expected UID of given mail folder
	 */
	public static long getUIDNext(final Folder mailbox) throws MessagingException {
		if (!(mailbox instanceof IMAPFolder)) {
			throw new MessagingException("Given folder " + mailbox.getFullName() + " is not an instance of IMAPFolder");
		}
		final IMAPFolder imapFolder = (IMAPFolder) mailbox;
		final Long retval = (Long) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
			 */
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				final String command = "UID FETCH 1:* (UID)";
				final Response[] r = p.command(command, null);
				final Response response = r[r.length - 1];
				String retval = null;
				try {
					if (response.isOK()) {
						if (r.length == 1) {
							/*
							 * Empty folder
							 */
							return Long.valueOf(1L);
						}
						final IMAPResponse ir = (IMAPResponse) r[r.length - 2];
						/*
						 * "* 243 FETCH (UID 338)"
						 */
						final String resp = ir.toString();
						final int pos01 = resp.indexOf("(UID ");
						final int pos02 = resp.lastIndexOf(')');
						retval = resp.substring(pos01 + 5, pos02);
					} else {
						throw new ProtocolException(String.format(PROTOCOL_ERROR_TEMPL, "UID"));
					}
				} finally {
					p.notifyResponseHandlers(r);
					p.handleResult(response);
				}
				return Long.valueOf(retval);
			}
		});
		return retval.longValue() + 1;
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

	/**
	 * An IMAP argument containing at least one of the following characters
	 * should be surrounded with quotes: * % ( ) { } " \ \s
	 */
	private static final Pattern PATTERN_QUOTE_ARG = Pattern.compile("[\\s\\*%\\(\\)\\{\\}\"\\\\]");

	private static final Pattern PATTERN_ESCAPE_ARG = Pattern.compile("[\"\\\\]");

	private final static String TEMPL_CREATE = "CREATE %s";

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
	 * @return encoded and if necessary quoted and escaped mailbox fullname
	 */
	public static final String prepareStringArgument(final String fullname) {
		/*
		 * Ensure to have only ASCII characters
		 */
		final String lfolder = BASE64MailboxEncoder.encode(fullname);
		/*
		 * Determine if quoting (and escaping) has to be done
		 */
		final boolean quote = PATTERN_QUOTE_ARG.matcher(lfolder).find();
		final boolean escape = PATTERN_ESCAPE_ARG.matcher(lfolder).find();
		final StringBuilder sb = new StringBuilder(100);
		if (quote) {
			sb.append('"');
		}
		if (escape) {
			sb.append(lfolder.replaceAll(REPLPAT_QUOTE, REPLACEMENT_QUOTE).replaceAll(REPLPAT_BACKSLASH,
					REPLACEMENT_BACKSLASH));
		} else {
			sb.append(lfolder);
		}
		if (quote) {
			sb.append('"');
		}
		return sb.toString();
	}

	public static final Response[] doCommand(final IMAPStore store, final String command, final String... strings)
			throws MessagingException {
		return doCommand((IMAPFolder) store.getDefaultFolder(), command, strings);
	}

	public static final Response[] doCommand(final IMAPFolder imapFolder, final String command, final String... strings) {
		final IMAPProtocol p = imapFolder.getProtocol();
		final Argument args = new Argument();
		for (int i = 0; i < strings.length; i++) {
			args.writeString(BASE64MailboxEncoder.encode(strings[i]));
		}
		return p.command(command, args);
	}

	public static final void createFolder(final Store store, final String fullname) throws MessagingException,
			ProtocolException {
		final String folderArg = prepareStringArgument(fullname);
		final IMAPProtocol p = ((IMAPFolder) store.getDefaultFolder()).getProtocol();
		final Response[] r = p.command(String.format(TEMPL_CREATE, folderArg), null);
		final Response response = r[r.length - 1];
		try {
			if (response.isOK()) {
				return;
			}
		} finally {
			p.notifyResponseHandlers(r);
			p.handleResult(response);
		}
	}

	public static String getSortCritForIMAPCommand(final int sortField, final boolean descendingDirection)
			throws MessagingException {
		final StringBuilder imapSortCritBuilder = new StringBuilder().append(descendingDirection ? "REVERSE " : "");
		switch (sortField) {
		case JSONMessageObject.FIELD_SENT_DATE:
			/*
			 * Special treatment for DATE
			 */
			if (descendingDirection) {
				imapSortCritBuilder.append("DATE");
			} else {
				imapSortCritBuilder.append("REVERSE DATE");
			}
			break;
		case JSONMessageObject.FIELD_RECEIVED_DATE:
			/*
			 * Special treatment for DATE
			 */
			if (descendingDirection) {
				imapSortCritBuilder.append("DATE");
			} else {
				imapSortCritBuilder.append("REVERSE DATE");
			}
			break;
		case JSONMessageObject.FIELD_FROM:
			imapSortCritBuilder.append("FROM");
			break;
		case JSONMessageObject.FIELD_TO:
			imapSortCritBuilder.append("TO");
			break;
		case JSONMessageObject.FIELD_SUBJECT:
			imapSortCritBuilder.append("SUBJECT");
			break;
		case JSONMessageObject.FIELD_FOLDER:
			imapSortCritBuilder.append("FOLDER");
			break;
		case JSONMessageObject.FIELD_SIZE:
			imapSortCritBuilder.append("SIZE");
			break;
		default:
			throw new MessagingException("Field " + sortField + " NOT supported for IMAP-sided sorting");
		}
		return imapSortCritBuilder.toString();
	}

	/**
	 * @return a <code>java.util.Comparator</code> instance to sort messages
	 *         within application
	 */
	public static Comparator<Message> getComparator(final int sortField, final boolean descendingDirection,
			final Locale locale) {
		return new MailComparator(sortField, descendingDirection, locale);
	}

	/**
	 * @return a <code>java.util.Comparator</code> instance to sort <b>root</b>
	 *         tree nodes according to their messages
	 */
	public static Comparator<TreeNode> getTreeNodeComparator(final int sortField, final boolean descendingDirection,
			final Folder folder, final Locale locale) {
		return new TreeNodeComparator(sortField, descendingDirection, (IMAPFolder) folder, locale);
	}

	/**
	 * Creates an IMAP search term from given search fields and search pattern
	 * for each field
	 * 
	 * @param searchFields -
	 *            the search fields (as defined in <code>MessageObject</code>
	 * @param searchPatterns -
	 *            the search pattern for each field
	 * @param linkWithOR -
	 *            search terms are either logically OR-linked or AND-linked
	 * @return the search term
	 */
	public static SearchTerm getSearchTerm(final int[] searchFields, final String[] searchPatterns,
			final boolean linkWithOR) {
		SearchTerm searchTerm = null;
		for (int i = 0; i < searchFields.length; i++) {
			switch (searchFields[i]) {
			case JSONMessageObject.FIELD_FROM:
				if (searchTerm == null) {
					searchTerm = new FromStringTerm(searchPatterns[i]);
				} else {
					if (linkWithOR) {
						searchTerm = new OrTerm(searchTerm, new FromStringTerm(searchPatterns[i]));
					} else {
						searchTerm = new AndTerm(searchTerm, new FromStringTerm(searchPatterns[i]));
					}
				}
				break;
			case JSONMessageObject.FIELD_TO:
				if (searchTerm == null) {
					searchTerm = new RecipientStringTerm(Message.RecipientType.TO, searchPatterns[i]);
				} else {
					if (linkWithOR) {
						searchTerm = new OrTerm(searchTerm, new RecipientStringTerm(Message.RecipientType.TO,
								searchPatterns[i]));
					} else {
						searchTerm = new AndTerm(searchTerm, new RecipientStringTerm(Message.RecipientType.TO,
								searchPatterns[i]));
					}
				}
				break;
			case JSONMessageObject.FIELD_CC:
				if (searchTerm == null) {
					searchTerm = new RecipientStringTerm(Message.RecipientType.CC, searchPatterns[i]);
				} else {
					if (linkWithOR) {
						searchTerm = new OrTerm(searchTerm, new RecipientStringTerm(Message.RecipientType.CC,
								searchPatterns[i]));
					} else {
						searchTerm = new AndTerm(searchTerm, new RecipientStringTerm(Message.RecipientType.CC,
								searchPatterns[i]));
					}
				}
				break;
			case JSONMessageObject.FIELD_SUBJECT:
				if (searchTerm == null) {
					searchTerm = new SubjectTerm(searchPatterns[i]);
				} else {
					if (linkWithOR) {
						searchTerm = new OrTerm(searchTerm, new SubjectTerm(searchPatterns[i]));
					} else {
						searchTerm = new AndTerm(searchTerm, new SubjectTerm(searchPatterns[i]));
					}
				}
				break;
			default:
				if (searchTerm == null) {
					searchTerm = new BodyTerm(searchPatterns[i]);
				} else {
					if (linkWithOR) {
						searchTerm = new OrTerm(searchTerm, new BodyTerm(searchPatterns[i]));
					} else {
						searchTerm = new AndTerm(searchTerm, new BodyTerm(searchPatterns[i]));
					}
				}
			}
		}
		return searchTerm;
	}

	public static boolean findPatternInField(final int[] searchFields, final String[] searchPatterns,
			final boolean linkWithOR, final Message msg) throws OXException {
		try {
			boolean result = false;
			for (int i = 0; i < searchFields.length; i++) {
				boolean foundInCurrentField = false;
				switch (searchFields[i]) {
				case JSONMessageObject.FIELD_FROM:
					if (msg.getFrom() != null) {
						foundInCurrentField = (IMAPUtils.getAllAddresses(msg.getFrom()).toLowerCase().indexOf(
								searchPatterns[i]) != -1);
					} else {
						foundInCurrentField = false;
					}
					break;
				case JSONMessageObject.FIELD_TO:
					if (msg.getRecipients(Message.RecipientType.TO) != null) {
						foundInCurrentField = (IMAPUtils.getAllAddresses(msg.getRecipients(Message.RecipientType.TO))
								.toLowerCase().indexOf(searchPatterns[i]) != -1);
					} else {
						foundInCurrentField = false;
					}
					break;
				case JSONMessageObject.FIELD_CC:
					if (msg.getRecipients(Message.RecipientType.CC) != null) {
						foundInCurrentField = (IMAPUtils.getAllAddresses(msg.getRecipients(Message.RecipientType.CC))
								.toLowerCase().indexOf(searchPatterns[i]) != -1);
					} else {
						foundInCurrentField = false;
					}
					break;
				case JSONMessageObject.FIELD_SUBJECT:
					final String subject = msg.getSubject();
					if (subject != null) {
						foundInCurrentField = (subject.toLowerCase(Locale.ENGLISH).indexOf(searchPatterns[i]) != -1);
					} else {
						foundInCurrentField = false;
					}
					break;
				default:
					try {
						if (msg.getContent() instanceof String) {
							final String msgText = (String) msg.getContent();
							foundInCurrentField = msgText.toLowerCase(Locale.ENGLISH).indexOf(
									searchPatterns[i].toLowerCase()) > -1;
						} else {
							throw new IMAPException("Unknown Search Field: " + searchFields[i]);
						}
					} catch (final IOException e) {
						throw new OXMailException(MailCode.UNREADBALE_PART_CONTENT, e, Integer.valueOf(msg
								.getMessageNumber()), msg.getFolder().getFullName(), "");
					}
				}
				if (linkWithOR && foundInCurrentField) {
					return true;
				} else if (!linkWithOR && !foundInCurrentField) {
					return false;
				} else {
					result = foundInCurrentField;
				}
			}
			return result;
		} catch (final MessagingException e) {
			throw new IMAPException(e);
		}
	}

	/**
	 * Sorts only messages in specified index range according to specified sort
	 * field and specified sort direction
	 * 
	 * @param folder -
	 *            the mailbox folder in which to sort its messages
	 * @param sortField -
	 *            the sort field
	 * @param descendingDir -
	 *            whether to sort in descending or ascending order
	 * @param fromIndex -
	 *            start index
	 * @param toIndex -
	 *            end index
	 * @return a <code>java.util.List</code> containing the sorted message
	 *         numbers as <code>java.lang.String</code> objects
	 * @throws Exception
	 */
	public static Message[] getServerSortList(final Folder folder, final int sortField, final boolean descendingDir,
			final int fromIndex, final int toIndex) throws Exception {
		StringBuilder sortRange = new StringBuilder();
		for (int i = fromIndex + 1; i <= toIndex; i++) {
			sortRange.append(i).append(',');
		}
		sortRange = sortRange.deleteCharAt(sortRange.length() - 1);
		return getServerSortList(folder, getSortCritForIMAPCommand(sortField, descendingDir), sortRange);
	}

	public static Message[] getServerSortList(final Folder folder, final int sortField, final boolean descendingDir,
			final StringBuilder sortRange) throws MessagingException {
		return getServerSortList(folder, getSortCritForIMAPCommand(sortField, descendingDir), sortRange);
	}

	/**
	 * Sorts all messages according to specified sort field and specified sort
	 * direction
	 * 
	 * @param folder -
	 *            the mailbox folder in which to sort its messages
	 * @param sortField -
	 *            the sort field
	 * @param descendingDir -
	 *            whether to sort in descending or ascending order
	 * @return a <code>java.util.List</code> containing the sorted message
	 *         numbers as <code>java.lang.String</code> objects
	 * @throws Exception
	 */
	public static Message[] getServerSortList(final Folder folder, final int sortField, final boolean descendingDir)
			throws MessagingException {
		final StringBuilder sortRange = new StringBuilder();
		sortRange.append("ALL");
		return getServerSortList(folder, getSortCritForIMAPCommand(sortField, descendingDir), sortRange);
	}

	/**
	 * Get a server-side sorted list
	 */
	@SuppressWarnings("unchecked")
	private static Message[] getServerSortList(final Folder folder, final String scrit, final StringBuilder mdat)
			throws MessagingException {
		final String sortcrit = scrit;
		final String data = mdat.toString();
		final IMAPFolder imapFolder = (IMAPFolder) folder;
		/*
		 * Call the IMAPFolder.doCommand() method with inner class definition of
		 * ProtocolCommand
		 */
		final Object val = imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				Response[] r = p.command("SORT (" + sortcrit + ") UTF-8 " + data, null);
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

	private static final String STR_THREAD = "THREAD";

	/**
	 * This method parses the odd IMAP string representing thread-sorted message
	 * numbers into a tree structure. Every element of this tree structure is
	 * type of <code>com.openexchange.imap.TreeNode</code> which
	 * contains the number of representative message and a
	 * <code>java.util.List</code> containing all child messages.
	 * 
	 * @param folder -
	 *            the affected mailbox folder
	 * @param mdat -
	 *            the sort range
	 * @return a <code>java.util.List</code> of root tree nodes
	 * @throws MessagingException
	 */
	public static List<TreeNode> getThreadList(final Folder folder, final StringBuilder mdat) throws MessagingException {
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
		/*
		 * Now parse the odd THREAD response string.
		 */
		String resp = (String) val;
		List<TreeNode> pulledUp = null;
		if (resp.indexOf('(') != -1 && resp.indexOf(')') != -1) {
			resp = resp.substring(resp.indexOf('('), resp.lastIndexOf(')') + 1);
			ThreadParser tp = new ThreadParser();
			try {
				tp.parse(resp);
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
				throw new MessagingException(e.getMessage());
			}
			pulledUp = ThreadParser.pullUpFirst(tp.getParsedList());
			tp = null;
		}
		return pulledUp;
	}

	/**
	 * @return THREAD response
	 */
	public static final String getThreadResponse(final Folder folder, final StringBuilder mdat)
			throws MessagingException {
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

	/**
	 * @return parsed THREAD response in a structured data type
	 */
	public static final List<TreeNode> parseThreadResponse(final String threadResponse) throws MessagingException {
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

	private static final Pattern PATTERN_THREAD_RESP = Pattern.compile("[0-9]+");

	/**
	 * @return an array of <code>javax.mail.Message</code> objects only filled
	 *         with message's sequence number
	 */
	public static final MessageCacheObject[] getMessagesFromThreadResponse(final String folderFullname,
			final char separator, final String threadResponse) {
		final Matcher m = PATTERN_THREAD_RESP.matcher(threadResponse);
		if (m.find()) {
			final SmartMessageArray retval = new SmartMessageArray();
			do {
				retval.append(new MessageCacheObject(folderFullname, separator, Integer.parseInt(m.group())));
			} while (m.find());
			return retval.toArray();
		}
		return null;
	}

	private static class SmartMessageArray {
		/**
		 * Pointer to keep track of position in the array
		 */
		private int pointer;

		private MessageCacheObject[] array;

		private final int growthSize;

		public SmartMessageArray() {
			this(1024);
		}

		public SmartMessageArray(final int initialSize) {
			this(initialSize, (initialSize / 4));
		}

		public SmartMessageArray(final int initialSize, final int growthSize) {
			this.growthSize = growthSize;
			array = new MessageCacheObject[initialSize];
		}

		public SmartMessageArray append(final MessageCacheObject msg) {
			if (pointer >= array.length) {
				/*
				 * time to grow!
				 */
				final MessageCacheObject[] tmpArray = new MessageCacheObject[array.length + growthSize];
				System.arraycopy(array, 0, tmpArray, 0, array.length);
				array = tmpArray;
			}
			array[pointer++] = msg;
			return this;
		}

		public MessageCacheObject[] toArray() {
			final MessageCacheObject[] trimmedArray = new MessageCacheObject[pointer];
			System.arraycopy(array, 0, trimmedArray, 0, trimmedArray.length);
			return trimmedArray;
		}
	}

	/**
	 * Constructs a list of <code>javax.mail.Message</code> objects from
	 * tree-structured thread-sorted messages of given IMAP folder.
	 * 
	 * @param folder
	 * @param threadList
	 * @return - a <code>java.util.List</code> with thread-sorted messages
	 * @throws MessagingException
	 */
	public static List<ThreadSortMessage> getMessagesFromThreadList(final Folder folder, final List<TreeNode> threadList)
			throws MessagingException {
		final List<ThreadSortMessage> retval = new ArrayList<ThreadSortMessage>();
		final ThreadSortMessage threadSortMsg = new ThreadSortMessage();
		final int threadListSize = threadList.size();
		final Iterator<TreeNode> iter = threadList.iterator();
		for (int i = 0; i < threadListSize; i++) {
			final TreeNode currentNode = iter.next();
			/*
			 * Fetch message according to message's number
			 */
			threadSortMsg.setMsgAndLevel(folder.getMessage(currentNode.msgNum), 0);

			retval.add((ThreadSortMessage) threadSortMsg.clone());
			insertChildMessages(folder, currentNode.getChilds(), retval, 1);
		}
		return retval;
	}

	private static final void insertChildMessages(final Folder folder, final List<TreeNode> threadList,
			final List<ThreadSortMessage> messages, final int level) throws MessagingException {
		final ThreadSortMessage threadSortMsg = new ThreadSortMessage();
		final int threadListSize = threadList.size();
		final Iterator<TreeNode> iter = threadList.iterator();
		for (int i = 0; i < threadListSize; i++) {
			final TreeNode childNode = iter.next();
			threadSortMsg.setMsgAndLevel(folder.getMessage(childNode.msgNum), level);
			messages.add((ThreadSortMessage) threadSortMsg.clone());
			insertChildMessages(folder, childNode.getChilds(), messages, level + 1);
		}
	}

	private final static String TEMPL_UID_STORE_FLAGS = "UID STORE %s %sFLAGS (%s)";

	private static final String ERR_01 = "No matching messages";

	private static final int MAX_IMAP_COMMAND_LENGTH = 16300;

	private static final FetchProfile DEFAULT_FETCH_PROFILE = new FetchProfile();

	private static final FetchProfile UID_FETCH_PROFILE = new FetchProfile();

	static {
		DEFAULT_FETCH_PROFILE.add(FetchProfile.Item.ENVELOPE);
		DEFAULT_FETCH_PROFILE.add(FetchProfile.Item.FLAGS);
		DEFAULT_FETCH_PROFILE.add(FetchProfile.Item.CONTENT_INFO);
		DEFAULT_FETCH_PROFILE.add(UIDFolder.FetchProfileItem.UID);
		DEFAULT_FETCH_PROFILE.add(IMAPFolder.FetchProfileItem.SIZE);
		DEFAULT_FETCH_PROFILE.add(MessageHeaders.HDR_X_PRIORITY);
		UID_FETCH_PROFILE.add(UIDFolder.FetchProfileItem.UID);
	}

	public static final FetchProfile getDefaultFetchProfile() {
		return DEFAULT_FETCH_PROFILE;
	}

	public static final FetchProfile getUIDFetchProfile() {
		return UID_FETCH_PROFILE;
	}

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
	public static final boolean clearAllColorLabels(final IMAPFolder imapFolder, final long[] msgUIDs)
			throws ProtocolException {
		final String[] args = IMAPNumArgSplitter.splitUIDArg(msgUIDs);
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
						&& response.getRest().indexOf("Invalid system flag") != -1) {
					throw new ProtocolException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
							"Invalid System Flag detected"));
				} else {
					throw new ProtocolException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
							"UID STORE not supported"));
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
	public static final boolean setColorLabel(final IMAPFolder imapFolder, final long[] msgUIDs,
			final String colorLabelFlag) throws ProtocolException {
		final String[] args = IMAPNumArgSplitter.splitUIDArg(msgUIDs);
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
						&& response.getRest().indexOf("Invalid system flag") != -1) {
					throw new ProtocolException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
							"Invalid System Flag detected"));
				} else {
					throw new ProtocolException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
							"UID STORE not supported"));
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
	public static final boolean fastExpunge(final IMAPFolder imapFolder) throws ProtocolException {
		final IMAPProtocol p = imapFolder.getProtocol();
		Response[] r = p.command(COMMAND_EXPUNGE, null);
		Response response = r[r.length - 1];
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
	public static final boolean uidExpunge(final IMAPFolder imapFolder, final long[] uids) throws ProtocolException {
		final String[] args = IMAPNumArgSplitter.splitUIDArg(uids);
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

	/**
	 * Turns given array of <code>long</code> into an array of
	 * <code>com.sun.mail.imap.protocol.UIDSet</code> which in turn can be
	 * used for a varieties of <code>IMAPProtocol</code> methods.
	 * 
	 * @param uids -
	 *            the UIDs
	 * @return an array of <code>com.sun.mail.imap.protocol.UIDSet</code>
	 */
	public static final UIDSet[] toUIDSet(final long[] uids) {
		final List<UIDSet> sets = new ArrayList<UIDSet>(uids.length);
		for (int i = 0; i < uids.length; i++) {
			long current = uids[i];
			final UIDSet set = new UIDSet();
			set.start = current;
			/*
			 * Look for contiguous UIDs
			 */
			Inner: for (++i; i < uids.length; i++) {
				final long next = uids[i];
				if (next == current + 1) {
					current = next;
				} else {
					/*
					 * Break in sequence. Need to reexamine this message at the
					 * top of the outer loop, so decrement 'i' to cancel the
					 * outer loop's autoincrement
					 */
					i--;
					break Inner;
				}
			}
			set.end = current;
			sets.add(set);
		}
		if (sets.isEmpty()) {
			return null;
		}
		return sets.toArray(new UIDSet[sets.size()]);
	}

	private static final String getAllAddresses(final Address[] a) {
		final StringBuilder addressBuilder = new StringBuilder();
		if (a == null || a.length == 0) {
			addressBuilder.append("");
		} else {
			appendAddress(addressBuilder, (InternetAddress) a[0]);
			for (int i = 1; i < a.length; i++) {
				addressBuilder.append(", ");
				appendAddress(addressBuilder, (InternetAddress) a[i]);
			}
		}

		return (addressBuilder.toString());
	}

	private static final void appendAddress(final StringBuilder addressBuilder, final InternetAddress ia) {
		final String pers = ia.getPersonal();
		String addr = ia.getAddress();
		/*
		 * some mailers generates '<>' as from. such elements will be handled
		 * as '@unspecified-domain' by javamail. We make it empty ;)
		 */
		if (addr.trim().equals("@unspecified-domain")) {
			addr = "";
		}
		if (pers != null) {
			if (pers.indexOf(',') != -1) {
				addressBuilder.append('\"').append(pers).append('\"').append(" <");
			} else {
				addressBuilder.append(pers).append(" <");
			}
		}
		addressBuilder.append(addr);
		if (pers != null) {
			addressBuilder.append('>');
		}
	}

	private static class TreeNodeComparator implements Comparator<TreeNode> {

		private final MailComparator comp;

		private final IMAPFolder folder;

		public TreeNodeComparator(final int sortCol, final boolean descendingDirection, final IMAPFolder folder,
				final Locale locale) {
			comp = new MailComparator(sortCol, descendingDirection, locale);
			this.folder = folder;
		}

		public int compare(final TreeNode tn1, final TreeNode tn2) {
			try {
				return comp.compare(folder.getMessage(tn1.msgNum), folder.getMessage(tn2.msgNum));
			} catch (final MessagingException e) {
				return 0;
			}
		}
	} // end of class TreeNodeComparator

	private static class MailComparator implements Comparator<Message> {

		private static final String STR_EMPTY = "";

		private final boolean descendingDir;

		private final int sortCol;

		private final Locale locale;

		private static abstract class FieldComparer {

			public final Locale locale;

			public Collator collator;

			public FieldComparer(final Locale locale) {
				this.locale = locale;
			}

			public Collator getCollator() {
				if (collator == null) {
					collator = Collator.getInstance(locale);
					collator.setStrength(Collator.SECONDARY);
				}
				return collator;
			}

			public abstract int compareFields(final Message msg1, final Message msg2) throws MessagingException;
		}

		private final FieldComparer fieldComparer;

		public MailComparator(final boolean descendingDirection, final Locale locale) {
			this(JSONMessageObject.FIELD_SENT_DATE, descendingDirection, locale);
		}

		public MailComparator(final int sortCol, final boolean descendingDirection, final Locale locale) {
			this.sortCol = sortCol;
			this.descendingDir = descendingDirection;
			this.locale = locale;
			fieldComparer = createFieldComparer(this.sortCol, this.locale);
		}

		private static final int compareAddrs(final Address[] addrs1, final Address[] addrs2, final Locale locale,
				final Collator collator) {
			if (isEmptyAddrArray(addrs1) && !isEmptyAddrArray(addrs2)) {
				return -1;
			} else if (!isEmptyAddrArray(addrs1) && isEmptyAddrArray(addrs2)) {
				return 1;
			} else if (isEmptyAddrArray(addrs1) && isEmptyAddrArray(addrs2)) {
				return 0;
			}
			return collator.compare(getCompareStringFromAddress(addrs1[0], locale), getCompareStringFromAddress(
					addrs2[0], locale));
		}

		private static final boolean isEmptyAddrArray(final Address[] addrs) {
			return (addrs == null || addrs.length == 0);
		}

		private static final String getCompareStringFromAddress(final Address addr, final Locale locale) {
			if (addr instanceof InternetAddress) {
				final InternetAddress ia1 = (InternetAddress) addr;
				return ia1.getPersonal() != null && ia1.getPersonal().length() > 0 ? ia1.getPersonal().toLowerCase(
						locale) : ia1.getAddress().toLowerCase(Locale.ENGLISH);
			} else if (addr instanceof DummyAddress) {
				final DummyAddress da1 = (DummyAddress) addr;
				return da1.getAddress().toLowerCase(Locale.ENGLISH);
			} else {
				return STR_EMPTY;
			}
		}

		private static final Integer compareReferences(final Object o1, final Object o2) {
			if (o1 == null && o2 != null) {
				return Integer.valueOf(-1);
			} else if (o1 != null && o2 == null) {
				return Integer.valueOf(1);
			} else if (o1 == null && o2 == null) {
				return Integer.valueOf(0);
			}
			/*
			 * Both references are not null
			 */
			return null;
		}

		private static final FieldComparer createFieldComparer(final int sortCol, final Locale locale) {
			switch (sortCol) {
			case JSONMessageObject.FIELD_SENT_DATE:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						final Date d1 = msg1.getSentDate();
						final Date d2 = msg2.getSentDate();
						final Integer refComp = compareReferences(d1, d2);
						return refComp == null ? (d1.compareTo(d2) * (-1)) : refComp.intValue();
					}
				};
			case JSONMessageObject.FIELD_RECEIVED_DATE:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						final Date d1 = msg1.getReceivedDate();
						final Date d2 = msg2.getReceivedDate();
						final Integer refComp = compareReferences(d1, d2);
						return refComp == null ? (d1.compareTo(d2) * (-1)) : refComp.intValue();
					}
				};
			case JSONMessageObject.FIELD_FROM:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						return compareAddrs(msg1.getFrom(), msg2.getFrom(), this.locale, getCollator());
					}
				};
			case JSONMessageObject.FIELD_TO:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						return compareAddrs(msg1.getRecipients(Message.RecipientType.TO), msg2
								.getRecipients(Message.RecipientType.TO), this.locale, getCollator());
					}
				};
			case JSONMessageObject.FIELD_SUBJECT:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						final String sub1 = msg1.getSubject() == null ? STR_EMPTY : msg1.getSubject();
						final String sub2 = msg2.getSubject() == null ? STR_EMPTY : msg2.getSubject();
						return getCollator().compare(sub1, sub2);
					}
				};
			case JSONMessageObject.FIELD_FLAG_SEEN:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						final boolean isSeen1 = msg1.isSet(Flags.Flag.SEEN);
						final boolean isSeen2 = msg2.isSet(Flags.Flag.SEEN);
						if (isSeen1 && isSeen2) {
							return 0;
						} else if (!isSeen1 && !isSeen2) {
							final boolean isRecent1 = msg1.isSet(Flags.Flag.RECENT);
							final boolean isRecent2 = msg2.isSet(Flags.Flag.RECENT);
							if (isRecent1 && isRecent2) {
								return 0;
							} else if (!isRecent1 && !isRecent2) {
								return 0;
							} else if (isRecent1 && !isRecent2) {
								return 1;
							} else if (!isRecent1 && isRecent2) {
								return -1;
							}
						} else if (isSeen1 && !isSeen2) {
							return 1;
						} else if (!isSeen1 && isSeen2) {
							return -1;
						}
						return 0;
					}
				};
			case JSONMessageObject.FIELD_SIZE:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						return Integer.valueOf(msg1.getSize()).compareTo(Integer.valueOf(msg2.getSize()));
					}
				};
			case JSONMessageObject.FIELD_COLOR_LABEL:
				try {
					if (IMAPProperties.isUserFlagsEnabled()) {
						return new FieldComparer(locale) {
							@Override
							public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
								final Integer cl1 = getColorFlag(msg1.getFlags().getUserFlags());
								final Integer cl2 = getColorFlag(msg2.getFlags().getUserFlags());
								return cl1.compareTo(cl2);
							}
						};
					}
					return new FieldComparer(locale) {
						@Override
						public int compareFields(final Message msg1, final Message msg2) {
							return 0;
						}
					};
				} catch (final IMAPException e) {
					LOG.error(e.getMessage(), e);
				}
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) {
						return 0;
					}
				};
			default:
				throw new UnsupportedOperationException("Unknown sort column value " + sortCol);
			}
		}

		private static final Integer getColorFlag(final String[] userFlags) {
			for (int i = 0; i < userFlags.length; i++) {
				if (userFlags[i].startsWith(JSONMessageObject.COLOR_LABEL_PREFIX)) {
					return Integer.valueOf(userFlags[i].substring(3));
				}
			}
			return Integer.valueOf(JSONMessageObject.COLOR_LABEL_NONE);
		}

		public int compare(final Message msg1, final Message msg2) {
			try {
				int comparedTo = fieldComparer.compareFields(msg1, msg2);
				if (descendingDir) {
					comparedTo *= (-1);
				}
				return comparedTo;
			} catch (final MessagingException e) {
				LOG.error(e.getMessage(), e);
				return 0;
			}
		}
	} // End of class declaration for MailComparator

}
