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

package com.openexchange.imap.command;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.container.mail.MessageCacheObject;
import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.imap.MessageHeaders;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.tools.mail.ContentType;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.INTERNALDATE;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.RFC822SIZE;
import com.sun.mail.imap.protocol.UID;

/**
 * FetchIMAPCommand - performs a prefetch of messages in given folder with only
 * those fields set that need to be present for display and sorting. A
 * corresponding instance of <code>javax.mail.FetchProfile</code> is going to
 * be generated from given fields.
 * 
 * <p>
 * This method avoids calling JavaMail's fetch() methods which implicitely
 * requests whole message envelope (FETCH 1:* (ENVELOPE INTERNALDATE
 * RFC822.SIZE)) when later working on returned <code>javax.mail.Message</code>
 * objects.
 * 
 * </p>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class FetchIMAPCommand extends AbstractIMAPCommand<Message[]> {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(FetchIMAPCommand.class);

	private static interface SeqNumFetcher {
		public int getNextSeqNum(int messageIndex);
	}

	private static class MsgSeqNumFetcher implements SeqNumFetcher {

		private final Message[] msgs;

		public MsgSeqNumFetcher(final Message[] msgs) {
			this.msgs = msgs;
		}

		public int getNextSeqNum(final int index) {
			return msgs[index].getMessageNumber();
		}
	}

	private static class IntSeqNumFetcher implements SeqNumFetcher {

		private final int[] arr;

		public IntSeqNumFetcher(final int[] arr) {
			this.arr = arr;
		}

		public int getNextSeqNum(final int index) {
			return arr[index];
		}
	}

	private String[] args;

	private final String command;

	private SeqNumFetcher seqNumFetcher;

	private boolean uid;

	private int length;

	private int index;

	private FetchItemHandler[] itemHandlers;

	private final List<MessageCacheObject> retval;

	/**
	 * Constructor
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param arr -
	 *            the source array (either <code>long</code> UIDs,
	 *            <code>int</code> SeqNums or instances of
	 *            <code>Message</code>)
	 * @param fields -
	 *            the demanded fields as defined in {@link JSONMessageObject}
	 * @param sortField -
	 *            the sort field as defined in {@link JSONMessageObject}
	 * @param isSequential -
	 *            whether the source array values are sequential
	 * @throws MessagingException
	 */
	public FetchIMAPCommand(final IMAPFolder imapFolder, final Object arr, final int[] fields, final int sortField,
			final boolean isSequential) throws MessagingException {
		this(imapFolder, arr, getFetchProfile(fields, sortField), isSequential);
	}

	/**
	 * Constructor
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param arr -
	 *            the source array (either <code>long</code> UIDs,
	 *            <code>int</code> SeqNums or instances of
	 *            <code>Message</code>)
	 * @param fp -
	 *            the fetch profile
	 * @param isSequential -
	 *            whether the source array values are sequential
	 * @throws MessagingException
	 */
	public FetchIMAPCommand(final IMAPFolder imapFolder, final Object arr, final FetchProfile fp,
			final boolean isSequential) throws MessagingException {
		super(imapFolder);
		if (null == arr) {
			returnDefaultValue = true;
		} else {
			createArgs(arr, isSequential);
		}
		command = getFetchCommand(imapFolder, fp);
		retval = new ArrayList<MessageCacheObject>(length);
		index = 0;
	}

	/**
	 * Constructor to fetch all messages of given folder
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param fields -
	 *            the demanded fields as defined in {@link JSONMessageObject}
	 * @param sortField -
	 *            the sort field as defined in {@link JSONMessageObject}
	 * @param fetchLen -
	 *            the total message count
	 */
	public FetchIMAPCommand(final IMAPFolder imapFolder, final int[] fields, final int sortField, final int fetchLen) {
		this(imapFolder, getFetchProfile(fields, sortField), fetchLen);
	}

	/**
	 * Constructor to fetch all messages of given folder
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param fp -
	 *            the fetch profile
	 * @param fetchLen -
	 *            the total message count
	 */
	public FetchIMAPCommand(final IMAPFolder imapFolder, final FetchProfile fp, final int fetchLen) {
		super(imapFolder);
		if (0 == fetchLen) {
			returnDefaultValue = true;
		}
		args = AbstractIMAPCommand.ARGS_ALL;
		uid = false;
		length = fetchLen;
		command = getFetchCommand(imapFolder, fp);
		retval = new ArrayList<MessageCacheObject>(length);
		index = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#addLoopCondition()
	 */
	@Override
	protected boolean addLoopCondition() {
		return (index < length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getArgs()
	 */
	@Override
	protected String[] getArgs() {
		return args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getCommand()
	 */
	@Override
	protected String getCommand(final int argsIndex) {
		final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
		if (uid) {
			sb.append("UID ");
		}
		sb.append("FETCH ");
		sb.append(args[argsIndex]);
		sb.append(" (").append(command).append(')');
		return sb.toString();
	}

	private static final MessageCacheObject[] EMPTY_ARR = new MessageCacheObject[0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getDefaultValueOnEmptyFolder()
	 */
	@Override
	protected Message[] getDefaultValue() {
		return EMPTY_ARR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getReturnVal()
	 */
	@Override
	protected Message[] getReturnVal() {
		return retval.toArray(new MessageCacheObject[retval.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#handleLastResponse(com.sun.mail.iap.Response)
	 */
	@Override
	protected void handleLastResponse(final Response lastResponse) throws MessagingException {
		if (!lastResponse.isOK()) {
			throw new MessagingException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
					(uid ? "UID FETCH failed: " : "FETCH failed: ") + lastResponse.getRest()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#handleResponse(com.sun.mail.iap.Response)
	 */
	@Override
	protected void handleResponse(final Response currentReponse) throws MessagingException {
		/*
		 * Response is null or not a FetchResponse
		 */
		if (currentReponse == null) {
			return;
		} else if (!(currentReponse instanceof FetchResponse)) {
			return;
		}
		final FetchResponse f = (FetchResponse) currentReponse;
		final int seqnum;
		if (null != seqNumFetcher) {
			seqnum = seqNumFetcher.getNextSeqNum(index);
			if (seqnum != f.getNumber()) {
				/*
				 * Continue with next response
				 */
				return;
			}
		} else {
			seqnum = f.getNumber();
		}
		final MessageCacheObject msg = new MessageCacheObject(imapFolder.getFullName(), imapFolder.getSeparator(),
				seqnum);
		final int itemCount = f.getItemCount();
		if (itemHandlers == null) {
			itemHandlers = createItemHandlers(itemCount, f);
		}
		boolean error = false;
		try {
			for (int j = 0; j < itemCount; j++) {
				itemHandlers[j].handleItem(f.getItem(j), msg);
			}
		} catch (final MessagingException e) {
			/*
			 * Discard corrupt message
			 */
			final OXMailException me = MailInterfaceImpl.handleMessagingException(e);
			LOG.error(new StringBuilder(100).append("Message #").append(msg.getMessageNumber()).append(" discarded: ")
					.append(me.getMessage()).toString(), me);
			error = true;
		} catch (final OXException e) {
			/*
			 * Discard corrupt message
			 */
			LOG.error(new StringBuilder(100).append("Message #").append(msg.getMessageNumber()).append(" discarded: ")
					.append(e.getMessage()).toString(), e);
			error = true;
		}
		if (!error) {
			retval.add(msg);
		}
		index++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#performHandleResult()
	 */
	@Override
	protected boolean performHandleResult() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#performNotifyResponseHandlers()
	 */
	@Override
	protected boolean performNotifyResponseHandlers() {
		return false;
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

	private static void addFetchItem(final FetchProfile fp, final int field) {
		switch (field) {
		case JSONMessageObject.FIELD_ID:
			fp.add(UIDFolder.FetchProfileItem.UID);
			break;
		case JSONMessageObject.FIELD_ATTACHMENT:
			fp.add(FetchProfile.Item.CONTENT_INFO);
			break;
		case JSONMessageObject.FIELD_FROM:
			fp.add(MessageHeaders.HDR_FROM);
			break;
		case JSONMessageObject.FIELD_TO:
			fp.add(MessageHeaders.HDR_TO);
			break;
		case JSONMessageObject.FIELD_CC:
			fp.add(MessageHeaders.HDR_CC);
			break;
		case JSONMessageObject.FIELD_BCC:
			fp.add(MessageHeaders.HDR_BCC);
			break;
		case JSONMessageObject.FIELD_SUBJECT:
			fp.add(MessageHeaders.HDR_SUBJECT);
			break;
		case JSONMessageObject.FIELD_SIZE:
			fp.add(IMAPFolder.FetchProfileItem.SIZE);
			break;
		case JSONMessageObject.FIELD_SENT_DATE:
			fp.add(MessageHeaders.HDR_DATE);
			break;
		case JSONMessageObject.FIELD_FLAGS:
			if (!fp.contains(FetchProfile.Item.FLAGS)) {
				fp.add(FetchProfile.Item.FLAGS);
			}
			break;
		case JSONMessageObject.FIELD_DISPOSITION_NOTIFICATION_TO:
			fp.add(MessageHeaders.HDR_DISP_NOT_TO);
			break;
		case JSONMessageObject.FIELD_PRIORITY:
			fp.add(MessageHeaders.HDR_X_PRIORITY);
			break;
		case JSONMessageObject.FIELD_COLOR_LABEL:
			if (!fp.contains(FetchProfile.Item.FLAGS)) {
				fp.add(FetchProfile.Item.FLAGS);
			}
			break;
		case JSONMessageObject.FIELD_FLAG_SEEN:
			if (!fp.contains(FetchProfile.Item.FLAGS)) {
				fp.add(FetchProfile.Item.FLAGS);
			}
			break;
		default:
			return;
		}
	}

	private void createArgs(final Object arr, final boolean isSequential) throws MessagingException {
		if (arr instanceof int[]) {
			final int[] seqNums = (int[]) arr;
			uid = false;
			length = seqNums.length;
			args = isSequential ? new String[] { new StringBuilder(64).append(seqNums[0]).append(':').append(
					seqNums[seqNums.length - 1]).toString() } : IMAPNumArgSplitter.splitSeqNumArg(seqNums);
			seqNumFetcher = new IntSeqNumFetcher(seqNums);
		} else if (arr instanceof long[]) {
			final long[] uids = (long[]) arr;
			uid = true;
			length = uids.length;
			args = isSequential ? new String[] { new StringBuilder(64).append(uids[0]).append(':').append(
					uids[uids.length - 1]).toString() } : IMAPNumArgSplitter.splitUIDArg(uids);
			seqNumFetcher = null;
		} else if (arr instanceof Message[]) {
			final Message[] msgs = (Message[]) arr;
			uid = false;
			length = msgs.length;
			args = isSequential ? new String[] { new StringBuilder(64).append(msgs[0].getMessageNumber()).append(':')
					.append(msgs[msgs.length - 1].getMessageNumber()).toString() } : IMAPNumArgSplitter.splitMessageArg(msgs);
			seqNumFetcher = new MsgSeqNumFetcher(msgs);
		} else {
			throw new MessagingException(new StringBuilder("Invalid array type! ").append(arr.getClass().getName())
					.toString());
		}
	}

	private static interface HeaderHandler {
		public void handleHeader(String hdrValue, MessageCacheObject msg) throws MessagingException, OXException;
	}

	private static final MailDateFormat mailDateFormat = new MailDateFormat();

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
			itemHandler.hdrHandlers = new HashMap<String, HeaderHandler>();
			for (final Enumeration e = h.getAllHeaders(); e.hasMoreElements();) {
				final Header hdr = (Header) e.nextElement();
				addHeaderHandlers(itemHandler, hdr);
			}
		}

		public final static void addHeaderHandlers(final FetchItemHandler itemHandler, final Header hdr) {
			if (hdr.getName().equals(MessageHeaders.HDR_FROM)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_FROM, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						try {
							msg.setFrom(InternetAddress.parse(hdrValue, false));
						} catch (final AddressException e) {
							msg.setHeader(MessageHeaders.HDR_FROM, hdrValue);
						}
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_TO)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_TO, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						try {
							msg.setRecipients(RecipientType.TO, InternetAddress.parse(hdrValue, false));
						} catch (final AddressException e) {
							msg.setHeader(MessageHeaders.HDR_TO, hdrValue);
						}
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_CC)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_CC, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						try {
							msg.setRecipients(RecipientType.CC, InternetAddress.parse(hdrValue, false));
						} catch (final AddressException e) {
							msg.setHeader(MessageHeaders.HDR_CC, hdrValue);
						}
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_BCC)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_BCC, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						try {
							msg.setRecipients(RecipientType.BCC, InternetAddress.parse(hdrValue, false));
						} catch (final AddressException e) {
							msg.setHeader(MessageHeaders.HDR_BCC, hdrValue);
						}
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_REPLY_TO)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_REPLY_TO, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						try {
							msg.setReplyTo(InternetAddress.parse(hdrValue, true));
						} catch (final AddressException e) {
							msg.setHeader(MessageHeaders.HDR_REPLY_TO, hdrValue);
						}
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_SUBJECT)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_SUBJECT, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg) {
						try {
							msg.setSubject(MimeUtility.decodeText(hdrValue));
						} catch (final UnsupportedEncodingException e) {
							LOG.error("Unsupported encoding in a message detected and monitored.", e);
							MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
							msg.setSubject(MessageUtils.decodeMultiEncodedHeader(hdrValue));
						}
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_DATE)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_DATE, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						try {
							msg.setSentDate(mailDateFormat.parse(hdrValue));
						} catch (final ParseException e) {
							throw new MessagingException(e.getMessage());
						}
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_X_PRIORITY)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_X_PRIORITY, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						msg.setHeader(MessageHeaders.HDR_X_PRIORITY, hdrValue);
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_MESSAGE_ID)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_MESSAGE_ID, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						msg.setHeader(MessageHeaders.HDR_MESSAGE_ID, hdrValue);
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_IN_REPLY_TO)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_IN_REPLY_TO, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						msg.setHeader(MessageHeaders.HDR_IN_REPLY_TO, hdrValue);
					}
				});
			} else if (hdr.getName().equals(MessageHeaders.HDR_REFERENCES)) {
				itemHandler.hdrHandlers.put(MessageHeaders.HDR_REFERENCES, new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						msg.setHeader(MessageHeaders.HDR_REFERENCES, hdrValue);
					}
				});
			} else {
				itemHandler.hdrHandlers.put(hdr.getName(), new HeaderHandler() {
					public void handleHeader(final String hdrValue, final MessageCacheObject msg)
							throws MessagingException {
						msg.setHeader(hdr.getName(), hdrValue);
					}
				});
			}
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

	/**
	 * Default value for message header 'Content-Type'
	 * 
	 * @value text/plain; charset=us-ascii
	 */
	private static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=us-ascii";

	private static FetchItemHandler[] createItemHandlers(final int itemCount, final FetchResponse f) {
		final FetchItemHandler[] itemHandlers = new FetchItemHandler[itemCount];
		for (int j = 0; j < itemCount; j++) {
			final Item item = f.getItem(j);
			/*
			 * Check for the FLAGS item
			 */
			if (item instanceof Flags) {
				itemHandlers[j] = new FetchItemHandler() {
					@Override
					public void handleItem(final Item item, final MessageCacheObject msg) {
						msg.setFlags((Flags) item);
					}
				};
			} else if (item instanceof ENVELOPE) {
				itemHandlers[j] = new FetchItemHandler() {
					@Override
					public void handleItem(final Item item, final MessageCacheObject msg) throws MessagingException {
						final ENVELOPE env = (ENVELOPE) item;
						msg.setFrom(env.from);
						msg.setRecipients(RecipientType.TO, env.to);
						msg.setRecipients(RecipientType.CC, env.cc);
						msg.setRecipients(RecipientType.BCC, env.bcc);
						msg.setReplyTo(env.replyTo);
						try {
							msg.setSubject(env.subject == null ? "" : MimeUtility.decodeText(env.subject));
						} catch (final UnsupportedEncodingException e) {
							LOG.error("Unsupported encoding in a message detected and monitored.", e);
							MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
							msg.setSubject(MessageUtils.decodeMultiEncodedHeader(env.subject));
						}
						msg.setSentDate(env.date);
					}
				};
			} else if (item instanceof INTERNALDATE) {
				itemHandlers[j] = new FetchItemHandler() {
					@Override
					public void handleItem(final Item item, final MessageCacheObject msg) {
						msg.setReceivedDate(((INTERNALDATE) item).getDate());
					}
				};
			} else if (item instanceof RFC822SIZE) {
				itemHandlers[j] = new FetchItemHandler() {
					@Override
					public void handleItem(final Item item, final MessageCacheObject msg) {
						msg.setSize(((RFC822SIZE) item).size);
					}
				};
			} else if (item instanceof BODYSTRUCTURE) {
				itemHandlers[j] = new FetchItemHandler() {
					@Override
					public void handleItem(final Item item, final MessageCacheObject msg) throws OXException {
						final BODYSTRUCTURE bs = (BODYSTRUCTURE) item;
						msg.setBodystructure(bs);
						final StringBuilder sb = new StringBuilder();
						sb.append(bs.type).append('/').append(bs.subtype);
						if (bs.cParams != null) {
							sb.append(bs.cParams);
						}
						try {
							msg.setContentType(new ContentType(sb.toString()));
						} catch (final OXException e) {
							if (LOG.isWarnEnabled()) {
								LOG.warn(e.getMessage(), e);
							}
							/*
							 * Try with less strict parsing
							 */
							try {
								msg.setContentType(new ContentType(sb.toString(), false));
							} catch (final OXException ie) {
								LOG.error(ie.getMessage(), ie);
								msg.setContentType(new ContentType(DEFAULT_CONTENT_TYPE));
							}
						}
					}
				};
			} else if (item instanceof UID) {
				itemHandlers[j] = new FetchItemHandler() {
					@Override
					public void handleItem(final Item item, final MessageCacheObject msg) {
						msg.setUid(((UID) item).uid);
					}
				};
			} else if (item instanceof RFC822DATA || item instanceof BODY) {
				itemHandlers[j] = new FetchItemHandler() {
					@Override
					public void handleItem(final Item item, final MessageCacheObject msg) throws MessagingException,
							OXException {
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
							HeaderHandler hdrHandler = this.getHdrHandler(hdr.getName());
							if (hdrHandler == null) {
								FetchItemHandler.addHeaderHandlers(this, hdr);
								hdrHandler = this.getHdrHandler(hdr.getName());
								hdrHandler.handleHeader(hdr.getValue(), msg);
							} else {
								hdrHandler.handleHeader(hdr.getValue(), msg);
							}
						}
					}
				};
			}
		}
		return itemHandlers;
	}

	private static final String EnvelopeCmd = "ENVELOPE INTERNALDATE RFC822.SIZE";

	private static String getFetchCommand(final IMAPFolder imapFolder, final FetchProfile fp) {
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

	private static String craftHeaderCmd(final IMAPProtocol p, final String[] hdrs) {
		final StringBuilder sb;
		if (p.isREV1()) {
			sb = new StringBuilder("BODY.PEEK[HEADER.FIELDS (");
		} else {
			sb = new StringBuilder("RFC822.HEADER.LINES (");
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

}
