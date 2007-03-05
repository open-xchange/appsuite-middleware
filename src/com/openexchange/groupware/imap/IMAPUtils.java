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

import static com.openexchange.groupware.container.mail.parser.MessageUtils.removeHdrLineBreak;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.Flags.Flag;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
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
import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.groupware.imap.OXMailException.MailCode;
import com.openexchange.tools.mail.ContentType;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.INTERNALDATE;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.RFC822SIZE;
import com.sun.mail.imap.protocol.UID;

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

	private static MailDateFormat mailDateFormat = new MailDateFormat();

	/*
	 * From, To, Cc, Bcc, ReplyTo, Subject and Date.
	 */
	private static final String HDR_FROM = "From";

	private static final String HDR_TO = "To";

	private static final String HDR_CC = "Cc";

	private static final String HDR_BCC = "Bcc";

	private static final String HDR_DATE = "Date";

	private static final String HDR_REPLY_TO = "Reply-To";

	private static final String HDR_SUBJECT = "Subject";

	private static final String HDR_MESSAGE_ID = "Message-ID";

	private static final String HDR_IN_REPLY_TO = "In-Reply-To";

	private static final String HDR_REFERENCES = "References";

	private static final String HDR_X_PRIORITY = "X-Priority";

	private static final String HDR_DISP_NOT_TO = "Disposition-Notification-To";

	private static abstract class FetchItemHandler {

		private Map<String, HeaderHandler> hdrHandlers;

		public FetchItemHandler() {
			super();
		}

		public final Map<String, HeaderHandler> getHdrHandlers() {
			return hdrHandlers;
		}

		public final HeaderHandler getHdrHandler(final String headerName) {
			return hdrHandlers.get(headerName);
		}

		public final int getHeadersSize() {
			return hdrHandlers.size();
		}

		public final boolean containsHeaderHandlers() {
			return this.hdrHandlers != null;
		}

		public final static void createHeaderHandlers(final FetchItemHandler itemHandler, final InternetHeaders h) {
			final Map<String, HeaderHandler> tmp = new HashMap<String, HeaderHandler>();
			for (final Enumeration e = h.getAllHeaders(); e.hasMoreElements();) {
				final Header hdr = (Header) e.nextElement();
				if (hdr.getName().equals(HDR_FROM)) {
					tmp.put(HDR_FROM, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							try {
								msg.setFrom(InternetAddress.parse(hdrValue, false));
							} catch (AddressException e) {
								msg.setHeader(HDR_FROM, hdrValue);
							}
						}
					});
				} else if (hdr.getName().equals(HDR_TO)) {
					tmp.put(HDR_TO, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							try {
								msg.setRecipients(RecipientType.TO, InternetAddress.parse(hdrValue, false));
							} catch (AddressException e) {
								msg.setHeader(HDR_TO, hdrValue);
							}
						}
					});
				} else if (hdr.getName().equals(HDR_CC)) {
					tmp.put(HDR_CC, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							try {
								msg.setRecipients(RecipientType.CC, InternetAddress.parse(hdrValue, false));
							} catch (AddressException e) {
								msg.setHeader(HDR_CC, hdrValue);
							}
						}
					});
				} else if (hdr.getName().equals(HDR_BCC)) {
					tmp.put(HDR_BCC, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							try {
								msg.setRecipients(RecipientType.BCC, InternetAddress.parse(hdrValue, false));
							} catch (AddressException e) {
								msg.setHeader(HDR_BCC, hdrValue);
							}
						}
					});
				} else if (hdr.getName().equals(HDR_REPLY_TO)) {
					tmp.put(HDR_REPLY_TO, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							try {
								msg.setReplyTo(InternetAddress.parse(hdrValue, true));
							} catch (AddressException e) {
								msg.setHeader(HDR_REPLY_TO, hdrValue);
							}
						}
					});
				} else if (hdr.getName().equals(HDR_SUBJECT)) {
					tmp.put(HDR_SUBJECT, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							msg.setSubject(MessageUtils.decodeMultiEncodedHeader(hdrValue));
						}
					});
				} else if (hdr.getName().equals(HDR_DATE)) {
					tmp.put(HDR_DATE, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							try {
								msg.setSentDate(mailDateFormat.parse(hdrValue));
							} catch (ParseException e) {
								throw new MessagingException(e.getMessage());
							}
						}
					});
				} else if (hdr.getName().equals(HDR_X_PRIORITY)) {
					tmp.put(HDR_X_PRIORITY, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							msg.setHeader(HDR_X_PRIORITY, hdrValue);
						}
					});
				} else if (hdr.getName().equals(HDR_MESSAGE_ID)) {
					tmp.put(HDR_MESSAGE_ID, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							msg.setHeader(HDR_MESSAGE_ID, hdrValue);
						}
					});
				} else if (hdr.getName().equals(HDR_IN_REPLY_TO)) {
					tmp.put(HDR_IN_REPLY_TO, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							msg.setHeader(HDR_IN_REPLY_TO, hdrValue);
						}
					});
				} else if (hdr.getName().equals(HDR_REFERENCES)) {
					tmp.put(HDR_REFERENCES, new HeaderHandler() {
						public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException,
								OXException {
							msg.setHeader(HDR_REFERENCES, hdrValue);
						}
					});
				}
			}
			itemHandler.hdrHandlers = tmp;
		}

		/**
		 * Handles given <code>com.sun.mail.imap.protocol.Item</code> instance
		 * and applies it to given
		 * <code>com.openexchange.groupware.container.mail.MessageCacheObject</code>
		 * instance
		 */
		public abstract void handleItem(final Item item, final MessageCacheObject msg) throws MessagingException,
				OXException;
	}

	private static interface HeaderHandler {
		public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException, OXException;
	}

	private IMAPUtils() {
		super();
	}

	private static final String STRING_HAS_CHILDREN = "\\HasChildren";

	/**
	 * @return <code>true</code> if given folder indicates to hold subfolder,
	 *         <code>false</code> otherwise
	 */
	public final static boolean hasSubfolders(final IMAPFolder imapFolder) throws MessagingException {
		boolean containsSubDir;
		if ((imapFolder.getType() & javax.mail.Folder.HOLDS_FOLDERS) == 0) {
			containsSubDir = false;
		} else {
			containsSubDir = false;
			final String[] attributes = imapFolder.getAttributes();
			boolean next = true;
			for (int j = 0; j < attributes.length && next; j++) {
				if (STRING_HAS_CHILDREN.equalsIgnoreCase(attributes[j])) {
					containsSubDir = true;
					next = false;
				}
			}
		}
		return containsSubDir;
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
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return false;
		}
	}

	private static final String STR_UID = "UID";

	private static final String TMPL_FETCH_HEADER_REV1 = "FETCH %s (BODY.PEEK[HEADER])";

	private static final String TMPL_FETCH_HEADER_NON_REV1 = "FETCH %s (RFC822.HEADER)";

	private static final Pattern PATTERN_PARSE_HEADER = Pattern
			.compile("(\\S+):\\s(.*)((?:\r?\n(?:\\s(?:.+)))*|(?:$))");

	public static final Map<String, String> loadBrokenHeaders(final Message msg, final boolean uid)
			throws MessagingException, ProtocolException {
		final IMAPFolder fld = (IMAPFolder) msg.getFolder();
		final IMAPProtocol p = fld.getProtocol();
		final String tmpl = p.isREV1() ? TMPL_FETCH_HEADER_REV1 : TMPL_FETCH_HEADER_NON_REV1;
		final String cmd;
		if (uid) {
			cmd = new StringBuilder(50).append(STR_UID).append(' ').append(String.format(tmpl, fld.getUID(msg)))
					.toString();
		} else {
			cmd = String.format(tmpl, msg.getMessageNumber());
		}
		final Map<String, String> retval = new HashMap<String, String>();
		final Response[] r = p.command(cmd, null);
		final Response response = r[r.length - 1];
		try {
			if (response.isOK()) {
				final StringBuilder valBuilder = new StringBuilder();
				NextResponse: for (int i = 0; i < r.length - 1; i++) {
					if (r[i] == null || !(r[i] instanceof FetchResponse)) {
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
				}
			}
		} finally {
			// dispatch remaining untagged responses
			p.notifyResponseHandlers(r);
			p.handleResult(response);
		}
		return null;
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
					Boolean retval = Boolean.valueOf(false);
					if (response.isOK()) { // command succesful
						retval = Boolean.valueOf(response.toString().indexOf("READ-ONLY") != -1);
					}
					p.notifyResponseHandlers(r);
					p.handleResult(response);
					return retval;
				}
			});
			return val.booleanValue();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new OXMailException(MailCode.FAILED_READ_ONLY_CHECK);
		}
	}

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
				Response[] r = p.command("SEARCH UNSEEN", null);
				/*
				 * Result is something like: * SEARCH 12 20 24
				 */
				Response response = r[r.length - 1];
				int index = 0;
				StringBuilder newMsgsBuffer = new StringBuilder();
				Message[] newMsgs = null;
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
								boolean first = true;
								String num;
								while ((num = ir.readAtomString()) != null) {
									if (first) {
										newMsgsBuffer.append(num);
										first = false;
									} else {
										newMsgsBuffer.append(',').append(num);
									}
									index++;
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
				/*
				 * No new messages found
				 */
				if (index == 0) {
					return null;
				}
				/*
				 * Sort new messages
				 */
				newMsgs = new Message[index];
				index = 0;
				r = p.command("SORT (REVERSE DATE) UTF-8 " + newMsgsBuffer.toString(), null);
				response = r[r.length - 1];
				try {
					if (response.isOK()) {
						for (int i = 0, len = r.length - 1; i < len; i++) {
							if (!(r[i] instanceof IMAPResponse)) {
								continue;
							}
							IMAPResponse ir = (IMAPResponse) r[i];
							if (ir.keyEquals("SORT")) {
								String num;
								while ((num = ir.readAtomString()) != null) {
									try {
										newMsgs[index++] = new MessageCacheObject(imapFolder.getFullName(), imapFolder
												.getSeparator(), Integer.parseInt(num));
										// newMsgs[index++] =
										// imapFolder.getMessage(Integer.parseInt(num));
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
				return newMsgs;
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
				List<Integer> retvalList = new ArrayList<Integer>();
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
				int[] retval = new int[size];
				for (int i = 0; i < size; i++) {
					retval[i] = retvalList.get(i).intValue();
				}
				return retval;
			}
		});
		return val;
	}

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
							final IMAPResponse ir = (IMAPResponse) r[i];
							// String key = ir.getKey();
							final String rest = ir.getRest();
							if (rest.indexOf("PERMANENTFLAGS") != -1) {
								/*
								 * [PERMANENTFLAGS (\Answered \Flagged \Draft
								 * \Deleted \Seen NonJunk attach \*)]
								 */
								final int pos01 = rest.indexOf('(');
								final int pos02 = rest.lastIndexOf(')');
								if (pos02 == (pos01 + 1)) {
									/*
									 * Empty. Assume all is supported.
									 */
									retval = Boolean.TRUE;
									break NextResp;
								}
								final String[] flags = rest.substring(pos01 + 1, pos02).split(" +");
								for (int j = 0; j < flags.length; j++) {
									final String flag = flags[j];
									if ("\\*".equals(flag) || flag.charAt(0) != '\\') {
										retval = Boolean.TRUE;
										break NextResp;
									}
								}
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
				public Object doCommand(IMAPProtocol p) throws ProtocolException {
					final Argument args = new Argument();
					args.writeString(lfolder);
					// Response[] r =
					p.command(cmd, args);
					return null;
				}
			});
		} catch (Exception e) {
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
					final String subject;
					if (msg instanceof MessageCacheObject) {
						subject = MessageUtils.decodeMultiEncodedHeader(((MessageCacheObject) msg).getSubject());
					} else {
						subject = MessageUtils.decodeMultiEncodedHeader(((MimeMessage) msg).getHeader("Subject", null));
					}
					if (subject != null) {
						foundInCurrentField = (subject.toLowerCase().indexOf(searchPatterns[i]) != -1);
					} else {
						foundInCurrentField = false;
					}
					break;
				default:
					try {
						if (msg.getContent() instanceof String) {
							final String msgText = (String) msg.getContent();
							foundInCurrentField = msgText.toLowerCase().indexOf(searchPatterns[i].toLowerCase()) > -1;
						} else {
							throw new IMAPException("Unknown Search Field: " + searchFields[i]);
						}
					} catch (IOException e) {
						throw new OXMailException(MailCode.UNREADBALE_PART_CONTENT, e, msg.getMessageNumber(), msg
								.getFolder().getFullName(), "");
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
		} catch (MessagingException e) {
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
	private static Message[] getServerSortList(final Folder folder, final String scrit, StringBuilder mdat)
			throws MessagingException {
		final String sortcrit = scrit;
		final String data = mdat.toString();
		final IMAPFolder imapFolder = (IMAPFolder) folder;
		/*
		 * Call the IMAPFolder.doCommand() method with inner class definition of
		 * ProtocolCommand
		 */
		Object val = imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				Response[] r = p.command("SORT (" + sortcrit + ") UTF-8 " + data, null);
				Response response = r[r.length - 1];
				List<Message> list = new ArrayList<Message>();
				try {
					if (response.isOK()) {
						for (int i = 0, len = r.length; i < len; i++) {
							if (!(r[i] instanceof IMAPResponse))
								continue;
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
		List<Message> l = ((List<Message>) val);
		return l.toArray(new Message[l.size()]);
	}

	private static final String STR_THREAD = "THREAD";

	/**
	 * This method parses the odd IMAP string representing thread-sorted message
	 * numbers into a tree structure. Every element of this tree structure is
	 * type of <code>com.openexchange.groupware.imap.TreeNode</code> which
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
			} catch (Exception e) {
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
			} catch (Exception e) {
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

	private static final String TEMPL_UID_FETCH = "UID FETCH %s (%s)";

	private static final String TEMPL_UID_COPY = "UID COPY %s %s";

	/**
	 * Copies all Messages identified through given <code>uids</code> from
	 * <code>imapFolder</code> to <code>destFolderName</code>.
	 * 
	 * @return an array of <code>long</code> containing the UIDs of copied
	 *         messages in destination folder
	 */
	public static final long[] copyUID(final IMAPFolder imapFolder, final long[] uids, final String destFolderName,
			final boolean isSequential) throws MessagingException {
		if (uids == null || uids.length == 0) {
			return new long[0];
		}
		/*
		 * Copy messages
		 */
		final String nameArg = prepareStringArgument(destFolderName);
		final String[] uidsArr;
		if (isSequential) {
			final StringBuilder tmp = new StringBuilder(100);
			tmp.append(uids[0]).append(':').append(uids[uids.length - 1]);
			uidsArr = new String[] { tmp.toString() };
		} else {
			uidsArr = getUIDs(uids);
		}
		/*
		 * Perform copy
		 */
		final long[] retval = new long[uids.length];
		Arrays.fill(retval, -1);
		imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
			 */
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				Response[] r = null;
				Response response = null;
				for (int k = 0; k < uidsArr.length; k++) {
					r = p.command(String.format(TEMPL_UID_COPY, uidsArr[k], nameArg), null);
					response = r[r.length - 1];
					try {
						if (!response.isOK()) {
							throw new ProtocolException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
									"COPY not supported"));
						}
						NextResponse: for (int index = 0; index < r.length && index < uids.length; index++) {
							if (!(r[index] instanceof IMAPResponse)) {
								continue NextResponse;
							}
							final IMAPResponse ir = (IMAPResponse) r[index];
							UIDCopyResponse copyResp = null;
							int atomCount = 0;
							String next = null;
							while ((next = ir.readAtom()) != null) {
								switch (++atomCount) {
								case 1:
									if (!"[COPYUID".equals(next)) {
										continue NextResponse;
									}
									copyResp = new UIDCopyResponse();
									break;
								case 3:
									copyResp.src = next;
									break;
								case 4:
									copyResp.dest = next;
									break;
								default:
									break;
								}
							}
							if (copyResp == null) {
								continue;
							}
							copyResp.fillResponse(uids, retval);
						}
					} finally {
						p.notifyResponseHandlers(r);
						p.handleResult(response);
					}
				}
				return Boolean.TRUE;
			}
		});
		return retval;
	}

	private static class UIDCopyResponse {
		private String src;

		private String dest;

		public String getDest() {
			return dest;
		}

		public void setDest(final String dest) {
			this.dest = dest;
		}

		public String getSrc() {
			return src;
		}

		public void setSrc(final String src) {
			this.src = src;
		}

		public void fillResponse(final long[] uids, final long[] retval) {
			final long[] srcArr = toIntArray(src);
			final long[] destArr = toIntArray(dest);
			for (int in = 0; in < srcArr.length; in++) {
				final long currentUID = srcArr[in];
				int index = 0;
				Inner: for (; index < uids.length; index++) {
					if (uids[index] == currentUID) {
						break Inner;
					}
				}
				retval[index] = destArr[in];
			}
		}

		private static final long[] toIntArray(final String s) {
			final SmartLongArray arr = new SmartLongArray();
			final String[] sa = s.replaceFirst("\\]", "").split(" *, *");
			Next: for (int i = 0; i < sa.length; i++) {
				final int pos;
				if ((pos = sa[i].indexOf(':')) == -1) {
					arr.append(Integer.parseInt(sa[i]));
					continue Next;
				}
				final int endUID = Integer.parseInt(sa[i].substring(pos + 1));
				for (int j = Integer.parseInt(sa[i].substring(0, pos)); j <= endUID; j++) {
					arr.append(j);
				}
			}
			return arr.toArray();
		}
	}

	private static class SmartLongArray {
		/**
		 * Pointer to keep track of position in the array
		 */
		private int pointer;

		private long[] array;

		private final int growthSize;

		public SmartLongArray() {
			this(1024);
		}

		public SmartLongArray(final int initialSize) {
			this(initialSize, (initialSize / 4));
		}

		public SmartLongArray(final int initialSize, final int growthSize) {
			this.growthSize = growthSize;
			array = new long[initialSize];
		}

		public SmartLongArray append(final int i) {
			if (pointer >= array.length) {
				/*
				 * time to grow!
				 */
				final long[] tmpArray = new long[array.length + growthSize];
				System.arraycopy(array, 0, tmpArray, 0, array.length);
				array = tmpArray;
			}
			array[pointer++] = i;
			return this;
		}

		public long[] toArray() {
			final long[] trimmedArray = new long[pointer];
			System.arraycopy(array, 0, trimmedArray, 0, trimmedArray.length);
			return trimmedArray;
		}
	}

	private final static String TEMPL_UID_STORE_FLAGS = "UID STORE %s %sFLAGS (%s)";

	public static final void setSystemFlags(final IMAPFolder imapFolder, final long[] uids, final boolean isSequential,
			final Flags flags, final boolean enable) throws MessagingException {
		final Flag[] systemFlags;
		if (flags == null || (systemFlags = flags.getSystemFlags()).length == 0) {
			return;
		}
		final String[] uidsArr;
		if (isSequential) {
			final StringBuilder tmp = new StringBuilder(100);
			tmp.append(uids[0]).append(':').append(uids[uids.length - 1]);
			uidsArr = new String[] { tmp.toString() };
		} else {
			uidsArr = getUIDs(uids);
		}
		final StringBuilder flagBuilder = new StringBuilder(200);
		flagBuilder.append(getFlagString(systemFlags[0]));
		for (int i = 1; i < systemFlags.length; i++) {
			flagBuilder.append(' ').append(getFlagString(systemFlags[i]));
		}
		imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
			 */
			public Object doCommand(IMAPProtocol p) throws ProtocolException {
				Response[] r = null;
				Response response = null;
				Next: for (int k = 0; k < uidsArr.length; k++) {
					r = p.command(String.format(TEMPL_UID_STORE_FLAGS, uidsArr[k], (enable ? "+" : "-"), flagBuilder
							.toString()), null);
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
						p.handleResult(response);
					}
				}
				return Boolean.TRUE;
			}
		});
	}

	/*
	 * \Answered \Flagged \Draft \Deleted \Seen $MDNSent NonJunk \*
	 */

	public static final String FLAG_ANSWERED = "\\Answered";

	public static final String FLAG_DELETED = "\\Deleted";

	public static final String FLAG_DRAFT = "\\Draft";

	public static final String FLAG_FLAGGED = "\\Flagged";

	public static final String FLAG_RECENT = "\\Recent";

	public static final String FLAG_SEEN = "\\Seen";

	public static final String FLAG_USER = "\\User";

	private static final String getFlagString(final Flag systemFlag) throws MessagingException {
		if (systemFlag.equals(Flags.Flag.ANSWERED)) {
			return FLAG_ANSWERED;
		} else if (systemFlag.equals(Flags.Flag.DELETED)) {
			return FLAG_DELETED;
		} else if (systemFlag.equals(Flags.Flag.DRAFT)) {
			return FLAG_DRAFT;
		} else if (systemFlag.equals(Flags.Flag.FLAGGED)) {
			return FLAG_FLAGGED;
		} else if (systemFlag.equals(Flags.Flag.RECENT)) {
			return FLAG_RECENT;
		} else if (systemFlag.equals(Flags.Flag.SEEN)) {
			return FLAG_SEEN;
		} else if (systemFlag.equals(Flags.Flag.USER)) {
			return FLAG_USER;
		}
		throw new MessagingException("Unknown System Flag");
	}

	/**
	 * @return corresponding sequence numbers of given messages' UIDs
	 */
	public static final int[] getSequenceNumbers(final IMAPFolder imapFolder, final long[] uids,
			final boolean isSequential) throws OXMailException, ProtocolException {
		if (uids == null || uids.length == 0) {
			return new int[0];
		}
		final IMAPProtocol p = imapFolder.getProtocol();
		final String[] uidsArr;
		if (isSequential) {
			final StringBuilder tmp = new StringBuilder(100);
			tmp.append(uids[0]).append(':').append(uids[uids.length - 1]);
			uidsArr = new String[] { tmp.toString() };
		} else {
			uidsArr = getUIDs(uids);
		}
		/*
		 * Fetch sequence number s to uids
		 */
		final SmartIntArray seqNums = new SmartIntArray(uids.length);
		int uidIndex = 0;
		Response[] r = null;
		Response response = null;
		for (int k = 0; k < uidsArr.length; k++) {
			r = p.command(String.format(TEMPL_UID_FETCH, uidsArr[k], "UID"), null);
			response = r[r.length - 1];
			try {
				if (!response.isOK()) {
					throw new OXMailException(MailCode.PROTOCOL_ERROR, "UID FETCH not supported");
				}
				NextResponse: for (int index = 0; index < r.length && index < uids.length; index++) {
					final long correspondingUID = uids[uidIndex];
					if (!(r[index] instanceof FetchResponse)) {
						continue;
					}
					final FetchResponse f = (FetchResponse) r[index];
					/*
					 * Check if response's uid matches corresponding uid
					 */
					long currentUID = -1;
					final int itemCount = f.getItemCount();
					for (int j = 0; j < itemCount; j++) {
						final Item item = f.getItem(j);
						if (item instanceof UID) {
							currentUID = ((UID) item).uid;
						}
					}
					if (correspondingUID != currentUID) {
						continue NextResponse;
					}
					seqNums.append(f.getNumber());
					uidIndex++;
				}
			} finally {
				p.notifyResponseHandlers(r);
				p.handleResult(response);
			}
		}
		return seqNums.toArray();
	}

	/**
	 * <p>
	 * Prefetch of given messages in given folder with only those fields set
	 * that need to be present for display and sorting. A corresponding instance
	 * of <code>javax.mail.FetchProfile</code> is going to be generated from
	 * given fields.
	 * </p>
	 * <p>
	 * This method avoids calling JavaMail's fetch() methods which implicitely
	 * requests whole message envelope (FETCH 1:* (ENVELOPE INTERNALDATE
	 * RFC822.SIZE)) when later working on returned
	 * <code>javax.mail.Message</code> objects.
	 * 
	 * </p>
	 * 
	 * @param isSequential
	 */
	public static Message[] fetchMessages(final IMAPFolder imapFolder, final Message[] msgs, final int[] fields,
			final int sortField, final boolean isSequential) throws ProtocolException, MessagingException, OXException {
		return fetchMessages(imapFolder, msgs, getFetchProfile(fields, sortField), isSequential);
	}

	private static final String TEMPL_FETCH = "FETCH %s (%s)";

	/**
	 * <p>
	 * Prefetch of given messages in given folder with only those fields set
	 * that need to be present for display and sorting.
	 * </p>
	 * <p>
	 * This method avoids calling JavaMail's fetch() methods which implicitely
	 * requests whole message envelope (FETCH 1:* (ENVELOPE INTERNALDATE
	 * RFC822.SIZE)) when later working on returned
	 * <code>javax.mail.Message</code> objects.
	 * </p>
	 * 
	 * @param isSequential
	 */
	public static Message[] fetchMessages(final IMAPFolder imapFolder, final Message[] msgs, final FetchProfile fp,
			final boolean isSequential) throws ProtocolException, MessagingException, OXException {
		if (msgs == null || msgs.length == 0) {
			return new MessageCacheObject[0];
		}
		final IMAPProtocol p = imapFolder.getProtocol();
		final String command = getFetchCommand(imapFolder, fp);
		final String[] messageNumArr;
		if (isSequential) {
			final StringBuilder tmp = new StringBuilder(100);
			tmp.append(msgs[0].getMessageNumber()).append(':').append(msgs[msgs.length - 1].getMessageNumber());
			messageNumArr = new String[] { tmp.toString() };
		} else {
			messageNumArr = getMessageSeqNums(msgs);
		}
		final List<MessageCacheObject> retval = new ArrayList<MessageCacheObject>(msgs.length);
		int messageIndex = 0;
		Response[] r = null;
		Response response = null;
		for (int k = 0; k < messageNumArr.length; k++) {
			r = p.command(String.format(TEMPL_FETCH, messageNumArr[k], command), null);
			response = r[r.length - 1];
			try {
				if (!response.isOK()) {
					throw new OXMailException(MailCode.PROTOCOL_ERROR, "FETCH not supported");
				}
				FetchItemHandler[] itemHandlers = null;
				NextResponse: for (int index = 0; index < r.length && messageIndex < msgs.length; index++) {
					final Response currentReponse = r[index];
					/*
					 * Response is null or not a FetchResponse
					 */
					if (currentReponse == null || !(currentReponse instanceof FetchResponse)) {
						continue;
					}
					final FetchResponse f = (FetchResponse) currentReponse;
					final int seqnum = msgs[messageIndex].getMessageNumber();
					/*
					 * Skip, ff it does not match our sequence number
					 */
					if (f.getNumber() != seqnum) {
						continue;
					}
					final MessageCacheObject msg = new MessageCacheObject(imapFolder.getFullName(), imapFolder
							.getSeparator(), seqnum);
					final int itemCount = f.getItemCount();
					if (itemHandlers == null) {
						itemHandlers = createItemHandlers(itemCount, f);
					}
					/*
					 * Item handlers already initialized
					 */
					for (int j = 0; j < itemCount; j++) {
						itemHandlers[j].handleItem(f.getItem(j), msg);
					}
					retval.add(msg);
					messageIndex++;
				}
			} finally {
				p.notifyResponseHandlers(r);
				p.handleResult(response);
			}
		}
		return retval.toArray(new MessageCacheObject[retval.size()]);
	}

	/**
	 * Performs a normal <code>FETCH</code> or an <code>UID FETCH</code>
	 * IMAP command dependent on given parameter <code>arr</code>.
	 */
	public static Message[] fetchMessages(final IMAPFolder imapFolder, final Object arr, final int[] fields,
			final int sortField, final boolean isSequential) throws ProtocolException, MessagingException, OXException {
		return fetchMessages(imapFolder, arr, getFetchProfile(fields, sortField), isSequential);
	}

	/**
	 * Performs a normal <code>FETCH</code> or an <code>UID FETCH</code>
	 * IMAP command dependent on given parameter <code>arr</code>.
	 */
	public static Message[] fetchMessages(final IMAPFolder imapFolder, final Object arr, final FetchProfile fp,
			final boolean isSequential) throws ProtocolException, MessagingException, OXException {
		if (!arr.getClass().isArray()) {
			throw new MessagingException(new StringBuilder("Invalid parameter. Object is not an array! ").append(
					arr.getClass().getName()).toString());
		}
		final int fetchLength;
		final String[] fetchArr;
		final boolean uid;
		if (arr instanceof int[]) {
			final int[] seqNums = (int[]) arr;
			uid = false;
			fetchLength = seqNums.length;
			fetchArr = isSequential ? new String[] { new StringBuilder(100).append(seqNums[0]).append(':').append(
					seqNums[seqNums.length - 1]).toString() } : getSequenceNumbers(seqNums);
		} else if (arr instanceof long[]) {
			final long[] uids = (long[]) arr;
			uid = true;
			fetchLength = uids.length;
			fetchArr = isSequential ? new String[] { new StringBuilder(100).append(uids[0]).append(':').append(
					uids[uids.length - 1]).toString() } : getUIDs(uids);
		} else if (arr instanceof Message[]) {
			final Message[] msgs = (Message[]) arr;
			uid = false;
			fetchLength = msgs.length;
			fetchArr = isSequential ? new String[] { new StringBuilder(100).append(msgs[0].getMessageNumber()).append(
					':').append(msgs[msgs.length - 1].getMessageNumber()).toString() } : getMessageSeqNums(msgs);
		} else {
			throw new MessagingException(new StringBuilder("Invalid array type! ").append(arr.getClass().getName())
					.toString());
		}

		return fetchMessages(imapFolder, fetchLength, fetchArr, fp, uid);
	}

	private static final Message[] fetchMessages(final IMAPFolder imapFolder, final int fetchLength,
			final String[] fetchArr, final FetchProfile fp, final boolean uid) throws OXException, MessagingException,
			ProtocolException {
		final IMAPProtocol p = imapFolder.getProtocol();
		final String command = getFetchCommand(imapFolder, fp);
		final List<MessageCacheObject> retval = new ArrayList<MessageCacheObject>(fetchLength);
		int messageIndex = 0;
		Response[] r = null;
		Response response = null;
		for (int k = 0; k < fetchArr.length; k++) {
			r = p.command(String.format(uid ? TEMPL_UID_FETCH : TEMPL_FETCH, fetchArr[k], command), null);
			response = r[r.length - 1];
			try {
				if (!response.isOK()) {
					throw new OXMailException(MailCode.PROTOCOL_ERROR, "FETCH not supported");
				}
				FetchItemHandler[] itemHandlers = null;
				NextResponse: for (int index = 0; index < r.length && messageIndex < fetchLength; index++) {
					final Response currentReponse = r[index];
					/*
					 * Response is null or not a FetchResponse
					 */
					if (currentReponse == null || !(currentReponse instanceof FetchResponse)) {
						continue;
					}
					final FetchResponse f = (FetchResponse) currentReponse;
					final MessageCacheObject msg = new MessageCacheObject(imapFolder.getFullName(), imapFolder
							.getSeparator(), f.getNumber());
					final int itemCount = f.getItemCount();
					if (itemHandlers == null) {
						itemHandlers = createItemHandlers(itemCount, f);
					}
					/*
					 * Item handlers already initialized
					 */
					for (int j = 0; j < itemCount; j++) {
						itemHandlers[j].handleItem(f.getItem(j), msg);
					}
					retval.add(msg);
					messageIndex++;
				}
			} finally {
				p.notifyResponseHandlers(r);
				p.handleResult(response);
			}
		}
		return retval.toArray(new MessageCacheObject[retval.size()]);
	}

	private static final FetchItemHandler[] createItemHandlers(final int itemCount, final FetchResponse f) {
		FetchItemHandler[] itemHandlers = new FetchItemHandler[itemCount];
		for (int j = 0; j < itemCount; j++) {
			final Item item = f.getItem(j);
			/*
			 * Check for the FLAGS item
			 */
			if (item instanceof Flags) {
				itemHandlers[j] = new FetchItemHandler() {
					public void handleItem(Item item, MessageCacheObject msg) throws MessagingException, OXException {
						msg.setFlags((Flags) item);
					}
				};
			} else if (item instanceof ENVELOPE) {
				itemHandlers[j] = new FetchItemHandler() {
					public void handleItem(Item item, MessageCacheObject msg) throws MessagingException, OXException {
						final ENVELOPE env = (ENVELOPE) item;
						msg.setFrom(env.from);
						msg.setRecipients(RecipientType.TO, env.to);
						msg.setRecipients(RecipientType.CC, env.cc);
						msg.setRecipients(RecipientType.BCC, env.bcc);
						msg.setReplyTo(env.replyTo);
						msg.setSubject(MessageUtils.decodeMultiEncodedHeader(env.subject));
						msg.setSentDate(env.date);
					}
				};
			} else if (item instanceof INTERNALDATE) {
				itemHandlers[j] = new FetchItemHandler() {
					public void handleItem(Item item, MessageCacheObject msg) throws MessagingException, OXException {
						msg.setReceivedDate(((INTERNALDATE) item).getDate());
					}
				};
			} else if (item instanceof RFC822SIZE) {
				itemHandlers[j] = new FetchItemHandler() {
					public void handleItem(Item item, MessageCacheObject msg) throws MessagingException, OXException {
						msg.setSize(((RFC822SIZE) item).size);
					}
				};
			} else if (item instanceof BODYSTRUCTURE) {
				itemHandlers[j] = new FetchItemHandler() {
					public void handleItem(Item item, MessageCacheObject msg) throws MessagingException, OXException {
						final BODYSTRUCTURE bs = (BODYSTRUCTURE) item;
						final StringBuilder sb = new StringBuilder();
						sb.append(bs.type).append('/').append(bs.subtype);
						if (bs.cParams != null) {
							sb.append(bs.cParams);
						}
						msg.setContentType(new ContentType(sb.toString()));
					}
				};
			} else if (item instanceof UID) {
				itemHandlers[j] = new FetchItemHandler() {
					public void handleItem(Item item, MessageCacheObject msg) throws MessagingException, OXException {
						msg.setUid(((UID) item).uid);
					}
				};
			} else if (item instanceof RFC822DATA || item instanceof BODY) {
				itemHandlers[j] = new FetchItemHandler() {
					@Override
					public void handleItem(Item item, MessageCacheObject msg) throws MessagingException, OXException {
						final InputStream headerStream;
						if (item instanceof RFC822DATA) {
							/*
							 * IMAP4
							 */
							headerStream = ((RFC822DATA) item).getByteArrayInputStream();
						} else {
							/*
							 * IMAP4rev1
							 */
							headerStream = ((BODY) item).getByteArrayInputStream();
						}
						final InternetHeaders h = new InternetHeaders();
						h.load(headerStream);
						if (!this.containsHeaderHandlers()) {
							FetchItemHandler.createHeaderHandlers(this, h);
						}
						for (final Enumeration e = h.getAllHeaders(); e.hasMoreElements();) {
							final Header hdr = (Header) e.nextElement();
							final HeaderHandler hdrHandler = this.getHdrHandler(hdr.getName());
							if (hdrHandler != null) {
								hdrHandler.handleHeader(hdr.getValue(), msg);
							}
						}
					}
				};
			}
		}
		return itemHandlers;
	}

	private static final int MAX_IMAP_COMMAND_LENGTH = 16300;

	private static final String[] getSequenceNumbers(final int[] seqNums) {
		/*
		 * Command MUST NOT exceed the maximum command length of the imap
		 * server, which is 16384 bytes
		 */
		if (seqNums == null || seqNums.length == 0) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(seqNums[0]);
		for (int i = 1; i < seqNums.length; i++) {
			sb.append(',').append(seqNums[i]);
		}
		/*
		 * Split into fitting blocks
		 */
		return splitCommaSeparatedList(sb.toString(), MAX_IMAP_COMMAND_LENGTH);
	}

	private static final String[] getUIDs(final long[] uids) {
		/*
		 * Command MUST NOT exceed the maximum command length of the imap
		 * server, which is 16384 bytes
		 */
		if (uids == null || uids.length == 0) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(uids[0]);
		for (int i = 1; i < uids.length; i++) {
			sb.append(',').append(uids[i]);
		}
		/*
		 * Split into fitting blocks
		 */
		return splitCommaSeparatedList(sb.toString(), MAX_IMAP_COMMAND_LENGTH);
	}

	private static final String[] getMessageSeqNums(final Message[] msgs) {
		/*
		 * Command MUST NOT exceed the maximum command length of the imap
		 * server, which is 16384 bytes
		 */
		if (msgs == null || msgs.length == 0) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(msgs[0].getMessageNumber());
		for (int i = 1; i < msgs.length; i++) {
			sb.append(',').append(msgs[i].getMessageNumber());
		}
		/*
		 * Split into fitting blocks
		 */
		return splitCommaSeparatedList(sb.toString(), MAX_IMAP_COMMAND_LENGTH);
	}

	/**
	 * Splits a comma-separated string arrays with max. <code>blockSize</code>
	 * length
	 */
	private static final String[] splitCommaSeparatedList(final String csw, final int blockSize) {
		final List<String> tmp = new ArrayList<String>();
		String s = csw;
		while (s.length() > blockSize) {
			int pos = blockSize - 1;
			while (s.charAt(pos) != ',') {
				pos--;
			}
			final String substr = s.substring(0, pos);
			tmp.add(substr);
			try {
				s = s.substring(pos + 1);
			} catch (StringIndexOutOfBoundsException e) {
				s = "";
			}
		}
		if (s.length() > 0) {
			tmp.add(s);
		}
		return tmp.toArray(new String[tmp.size()]);
	}

	private static final String EnvelopeCmd = "ENVELOPE INTERNALDATE RFC822.SIZE";

	private static final String getFetchCommand(final IMAPFolder imapFolder, final FetchProfile fp) {
		final StringBuilder command = new StringBuilder();
		boolean allHeaders = false;
		final boolean envelope = (fp.contains(FetchProfile.Item.ENVELOPE));
		if (envelope) {
			command.append(EnvelopeCmd);
		} else {
			command.append("INTERNALDATE");
		}
		if (fp.contains(FetchProfile.Item.FLAGS)) {
			command.append(" FLAGS");
		}
		if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
			command.append(" BODYSTRUCTURE");
		}
		if (fp.contains(UIDFolder.FetchProfileItem.UID)) {
			command.append(" UID");
		}
		if (fp.contains(IMAPFolder.FetchProfileItem.HEADERS)) {
			allHeaders = true;
			if (imapFolder.getProtocol().isREV1()) {
				command.append(" BODY.PEEK[HEADER]");
			} else {
				command.append(" RFC822.HEADER");
			}
		}
		if (!envelope && fp.contains(IMAPFolder.FetchProfileItem.SIZE)) {
			command.append(" RFC822.SIZE");
		}
		/*
		 * If we're not fetching all headers, fetch individual headers
		 */
		if (!allHeaders) {
			final String[] hdrs = fp.getHeaderNames();
			if (hdrs.length > 0) {
				command.append(' ');
				command.append(craftHeaderCmd(imapFolder.getProtocol(), hdrs));
			}
		}
		return command.toString();
	}

	private static final String craftHeaderCmd(final IMAPProtocol p, final String[] hdrs) {
		StringBuffer sb;
		if (p.isREV1()) {
			sb = new StringBuffer("BODY.PEEK[HEADER.FIELDS (");
		} else {
			sb = new StringBuffer("RFC822.HEADER.LINES (");
		}
		sb.append(hdrs[0]);
		for (int i = 1; i < hdrs.length; i++) {
			sb.append(' ');
			sb.append(hdrs[i]);
		}
		if (p.isREV1()) {
			sb.append(")]");
		} else {
			sb.append(')');
		}
		return sb.toString();
	}

	private static final FetchProfile DEFAULT_FETCH_PROFILE = new FetchProfile();

	static {
		DEFAULT_FETCH_PROFILE.add(FetchProfile.Item.ENVELOPE);
		DEFAULT_FETCH_PROFILE.add(FetchProfile.Item.FLAGS);
		DEFAULT_FETCH_PROFILE.add(FetchProfile.Item.CONTENT_INFO);
		DEFAULT_FETCH_PROFILE.add(UIDFolder.FetchProfileItem.UID);
		DEFAULT_FETCH_PROFILE.add(IMAPFolder.FetchProfileItem.SIZE);
		DEFAULT_FETCH_PROFILE.add(HDR_X_PRIORITY);
	}

	public static FetchProfile getDefaultFetchProfile() {
		return DEFAULT_FETCH_PROFILE;
	}

	public static FetchProfile getFetchProfile(final int[] fields, final int sortField) {
		final FetchProfile retval = new FetchProfile();
		final Set<Integer> trimmedFields = new HashSet<Integer>();
		for (int i = 0; i < fields.length; i++) {
			trimmedFields.add(Integer.valueOf(fields[i]));
		}
		if (sortField > -1) {
			trimmedFields.add(Integer.valueOf(sortField));
		}
		final int size = trimmedFields.size();
		final Iterator<Integer> iter = trimmedFields.iterator();
		for (int i = 0; i < size; i++) {
			addFetchItem(retval, iter.next().intValue());
		}
		return retval;
	}

	private static final void addFetchItem(final FetchProfile fp, final int field) {
		switch (field) {
		case JSONMessageObject.FIELD_ID:
			fp.add(UIDFolder.FetchProfileItem.UID);
			break;
		case JSONMessageObject.FIELD_ATTACHMENT:
			fp.add(FetchProfile.Item.CONTENT_INFO);
			break;
		case JSONMessageObject.FIELD_FROM:
			fp.add(HDR_FROM);
			break;
		case JSONMessageObject.FIELD_TO:
			fp.add(HDR_TO);
			break;
		case JSONMessageObject.FIELD_CC:
			fp.add(HDR_CC);
			break;
		case JSONMessageObject.FIELD_BCC:
			fp.add(HDR_BCC);
			break;
		case JSONMessageObject.FIELD_SUBJECT:
			fp.add(HDR_SUBJECT);
			break;
		case JSONMessageObject.FIELD_SIZE:
			fp.add(IMAPFolder.FetchProfileItem.SIZE);
			break;
		case JSONMessageObject.FIELD_SENT_DATE:
			fp.add(HDR_DATE);
			break;
		case JSONMessageObject.FIELD_FLAGS:
			fp.add(FetchProfile.Item.FLAGS);
			break;
		case JSONMessageObject.FIELD_DISPOSITION_NOTIFICATION_TO:
			fp.add(HDR_DISP_NOT_TO);
			break;
		case JSONMessageObject.FIELD_PRIORITY:
			fp.add(HDR_X_PRIORITY);
			break;
		case JSONMessageObject.FIELD_COLOR_LABEL:
			fp.add(FetchProfile.Item.FLAGS);
			break;
		default:
			return;
		}
	}

	public static String getAllAddresses(final Address[] a) {
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

		public TreeNodeComparator(int sortCol, boolean descendingDirection, IMAPFolder folder, final Locale locale) {
			comp = new MailComparator(sortCol, descendingDirection, locale);
			this.folder = folder;
		}

		public int compare(final TreeNode tn1, final TreeNode tn2) {
			try {
				return comp.compare(folder.getMessage(tn1.msgNum), folder.getMessage(tn2.msgNum));
			} catch (MessagingException e) {
				return 0;
			}
		}
	} // end of class TreeNodeComparator

	private static class MailComparator implements Comparator<Message> {

		private final boolean descendingDir;

		private final int sortCol;

		private final Locale locale;

		private static abstract class FieldComparer {

			public final Locale locale;

			public FieldComparer(final Locale locale) {
				this.locale = locale;
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

		private static final int compareAddrs(final Address[] addrs1, final Address[] addrs2, final Locale locale) {
			if (isEmptyAddrArray(addrs1) && !isEmptyAddrArray(addrs2)) {
				return -1;
			} else if (!isEmptyAddrArray(addrs1) && isEmptyAddrArray(addrs2)) {
				return 1;
			} else if (isEmptyAddrArray(addrs1) && isEmptyAddrArray(addrs2)) {
				return 0;
			}
			return getCompareStringFromAddress(addrs1[0], locale).compareTo(
					getCompareStringFromAddress(addrs2[0], locale));
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
				return "";
			}
		}

		private static final FieldComparer createFieldComparer(final int sortCol, final Locale locale) {
			switch (sortCol) {
			case JSONMessageObject.FIELD_SENT_DATE:
				return new FieldComparer(locale) {
					public int compareFields(Message msg1, Message msg2) throws MessagingException {
						final Date d1 = msg1.getSentDate();
						final Date d2 = msg2.getSentDate();
						return (d1.compareTo(d2) * (-1));
					}
				};
			case JSONMessageObject.FIELD_RECEIVED_DATE:
				return new FieldComparer(locale) {
					public int compareFields(Message msg1, Message msg2) throws MessagingException {
						final Date d1 = msg1.getReceivedDate();
						final Date d2 = msg2.getReceivedDate();
						return (d1.compareTo(d2) * (-1));
					}
				};
			case JSONMessageObject.FIELD_FROM:
				return new FieldComparer(locale) {
					public int compareFields(Message msg1, Message msg2) throws MessagingException {
						return compareAddrs(msg1.getFrom(), msg2.getFrom(), this.locale);
					}
				};
			case JSONMessageObject.FIELD_TO:
				return new FieldComparer(locale) {
					public int compareFields(Message msg1, Message msg2) throws MessagingException {
						return compareAddrs(msg1.getRecipients(Message.RecipientType.TO), msg2
								.getRecipients(Message.RecipientType.TO), this.locale);
					}
				};
			case JSONMessageObject.FIELD_SUBJECT:
				return new FieldComparer(locale) {
					public int compareFields(Message msg1, Message msg2) throws MessagingException {
						final String sub1 = MessageUtils.decodeMultiEncodedHeader(msg1.getSubject()).toLowerCase(
								this.locale);
						final String sub2 = MessageUtils.decodeMultiEncodedHeader(msg2.getSubject()).toLowerCase(
								this.locale);
						return (sub1 == null ? "" : sub1).compareTo((sub2 == null ? "" : sub2));
					}
				};
			case JSONMessageObject.FIELD_FLAG_SEEN:
				return new FieldComparer(locale) {
					public int compareFields(Message msg1, Message msg2) throws MessagingException {
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
					public int compareFields(Message msg1, Message msg2) throws MessagingException {
						return Integer.valueOf(msg1.getSize()).compareTo(Integer.valueOf(msg2.getSize()));
					}
				};
			case JSONMessageObject.FIELD_COLOR_LABEL:
				try {
					if (IMAPProperties.isUserFlagsEnabled()) {
						return new FieldComparer(locale) {
							public int compareFields(Message msg1, Message msg2) throws MessagingException {
								final Integer cl1 = getColorFlag(msg1.getFlags().getUserFlags());
								final Integer cl2 = getColorFlag(msg2.getFlags().getUserFlags());
								return cl1.compareTo(cl2);
							}
						};
					} else {
						return new FieldComparer(locale) {
							public int compareFields(Message msg1, Message msg2) throws MessagingException {
								return 0;
							}
						};
					}
				} catch (IMAPException e) {
					LOG.error(e.getMessage(), e);
					return new FieldComparer(locale) {
						public int compareFields(Message msg1, Message msg2) throws MessagingException {
							return 0;
						}
					};
				}
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
			} catch (MessagingException e) {
				LOG.error(e.getMessage(), e);
				return 0;
			}
		}
	} // End of class declaration for MailComparator

	private static class SmartIntArray {
		/**
		 * Pointer to keep track of position in the array
		 */
		private int pointer;

		private int[] array;

		private final int growthSize;

		public SmartIntArray() {
			this(1024);
		}

		public SmartIntArray(final int initialSize) {
			this(initialSize, (initialSize / 4));
		}

		public SmartIntArray(final int initialSize, final int growthSize) {
			this.growthSize = growthSize;
			array = new int[initialSize];
		}

		public SmartIntArray append(final int i) {
			if (pointer >= array.length) {
				/*
				 * time to grow!
				 */
				final int[] tmpArray = new int[array.length + growthSize];
				System.arraycopy(array, 0, tmpArray, 0, array.length);
				array = tmpArray;
			}
			array[pointer++] = i;
			return this;
		}

		public int[] toArray() {
			final int[] trimmedArray = new int[pointer];
			System.arraycopy(array, 0, trimmedArray, 0, trimmedArray.length);
			return trimmedArray;
		}
	}

}
