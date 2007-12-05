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

package com.openexchange.mail.mime;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailInterfaceImpl;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.utils.MessageUtility;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * <p>
 * {@link ContainerMessage} - a lightweight subclass of abstract class
 * <code>javax.mail.Message</code> which just holds common-used header
 * information about a rfc822 message and can be used for caching since
 * interface <code>java.io.Serializable</code> is implemented.
 * </p>
 * <p>
 * In contrast to other subclasses of abstract class
 * <code>javax.mail.Message</code> this class does not force to fill message's
 * envelope. It only fills given header fields by additional setter methods.
 * </p>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContainerMessage extends Message implements Serializable {

	private static final String ERR_METHOD_NOT_SUPPORTED = "Method not supported";

	private static final long serialVersionUID = -5236672658788027516L;

	private static transient final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ContainerMessage.class);

	/**
	 * Folder's full name
	 */
	private String folderFullname;

	private char separator;

	/**
	 * Message's UID
	 */
	private long uid;

	/*
	 * ENVELOPE: Implementations should include the following attributes: From,
	 * To, Cc, Bcc, ReplyTo, Subject and Date.
	 */
	private InternetAddress[] from;

	private InternetAddress[] to;

	private InternetAddress[] cc;

	private InternetAddress[] bcc;

	private InternetAddress[] replyTo;

	private String inReplyTo;

	private String subject;

	private Date date;

	private Date receivedDate;

	private String messageId;

	/**
	 * FLAS
	 */
	private Flags flags;

	/**
	 * CONTENT_INFO
	 */
	private ContentType contentType;

	/**
	 * SIZE
	 */
	private int size = -1;

	/**
	 * X-PRIORITY
	 */
	private int priority;

	private int seqNum;

	private int threadLevel;

	private final Map<String, String> headers;

	private boolean expunged;

	private boolean hasAttachment;
	
	private BODYSTRUCTURE bodystructure;

	private ContainerMessage() {
		super();
		this.headers = new HashMap<String, String>();
	}

	public ContainerMessage(final String folderFullname, final char separator, final int msgnum) {
		this();
		this.seqNum = msgnum;
		this.folderFullname = folderFullname;
		this.separator = separator;
	}

	/**
	 * Constructs a new <code>MessageCacheObject</code> instance from given
	 * <b>PRE-FILLED</b> <code>Message</code> object
	 */
	public ContainerMessage(final MimeMessage msg, final long msgUID) throws MessagingException, MailException {
		super(msg.getFolder(), msg.getMessageNumber());
		this.headers = new HashMap<String, String>();
		this.seqNum = msg.getMessageNumber();
		this.uid = msgUID;
		this.folderFullname = msg.getFolder().getFullName();
		this.separator = msg.getFolder().getSeparator();
		if (msg.isExpunged()) {
			expunged = true;
		} else {
			this.from = (InternetAddress[]) msg.getFrom();
			this.to = (InternetAddress[]) msg.getRecipients(RecipientType.TO);
			this.cc = (InternetAddress[]) msg.getRecipients(RecipientType.CC);
			this.bcc = (InternetAddress[]) msg.getRecipients(RecipientType.BCC);
			this.replyTo = msg.getReplyTo() == null ? null : (InternetAddress[]) msg.getReplyTo();
			this.inReplyTo = msg.getHeader(MessageHeaders.HDR_IN_REPLY_TO, ",");
			this.messageId = msg.getHeader(MessageHeaders.HDR_MESSAGE_ID, null);
			this.subject = msg.getSubject();
			this.date = msg.getSentDate();
			this.receivedDate = msg.getReceivedDate();
			this.flags = msg.getFlags();
			this.contentType = new ContentType(msg.getContentType());
			this.size = msg.getSize();
			final String[] tmp;
			this.priority = (tmp = msg.getHeader(MessageHeaders.HDR_X_PRIORITY)) == null || tmp[0] == null
					|| tmp[0].length() == 0 ? MailMessage.PRIORITY_NORMAL : parsePriorityStr(tmp[0]);
		}
	}

	private static int parsePriorityStr(final String priorityHdrArg) {
		final String priorityHdr = priorityHdrArg.trim();
		final int pos = priorityHdr.indexOf(' ');
		try {
			if (pos == -1) {
				return Integer.parseInt(priorityHdr);
			}
			return Integer.parseInt(priorityHdr.substring(0, pos));
		} catch (final NumberFormatException e) {
			return MailMessage.PRIORITY_NORMAL;
		}
	}

	@Override
	public Address[] getFrom() {
		if (from != null) {
			return from;
		}
		return headers.containsKey(MessageHeaders.HDR_FROM) ? (from = new InternetAddress[] { new DummyAddress(headers
				.get(MessageHeaders.HDR_FROM)) }) : null;
	}

	@Override
	public void setFrom() throws MessagingException {
		throw new MessagingException("Missing from address");
	}

	public void setFrom(final InternetAddress[] from) {
		this.from = from;
	}

	@Override
	public void setFrom(final Address address) {
		this.from = new InternetAddress[] { ((InternetAddress) address) };
	}

	@Override
	public void addFrom(final Address[] addresses) throws MessagingException {
		if (addresses == null) {
			throw new MessagingException("Missing from address");
		}
		if (this.from == null) {
			this.from = new InternetAddress[addresses.length];
			for (int i = 0; i < addresses.length; i++) {
				from[i] = (InternetAddress) addresses[i];
			}
			return;
		}
		final InternetAddress[] tmp = from;
		from = new InternetAddress[tmp.length + addresses.length];
		int c = 0;
		for (int i = 0; i < tmp.length; i++) {
			from[c++] = tmp[i];
		}
		for (int i = 0; i < addresses.length; i++) {
			from[c++] = (InternetAddress) addresses[i];
		}
	}

	private InternetAddress[] getRecipientsInternal(final RecipientType type) throws MessagingException {
		if (type.equals(RecipientType.TO)) {
			return to;
		} else if (type.equals(RecipientType.CC)) {
			return cc;
		} else if (type.equals(RecipientType.BCC)) {
			return bcc;
		}
		throw new MessagingException("Unknown recipient type");
	}

	@Override
	public Address[] getRecipients(final RecipientType type) throws MessagingException {
		Address[] retval = getRecipientsInternal(type);
		if (retval == null) {
			if (type.equals(RecipientType.TO)) {
				retval = headers.containsKey(MessageHeaders.HDR_TO) ? new InternetAddress[] { new DummyAddress(headers
						.get(MessageHeaders.HDR_TO)) } : null;
			} else if (type.equals(RecipientType.CC)) {
				retval = headers.containsKey(MessageHeaders.HDR_CC) ? new InternetAddress[] { new DummyAddress(headers
						.get(MessageHeaders.HDR_CC)) } : null;
			} else if (type.equals(RecipientType.BCC)) {
				retval = headers.containsKey(MessageHeaders.HDR_BCC) ? new InternetAddress[] { new DummyAddress(headers
						.get(MessageHeaders.HDR_BCC)) } : null;
			} else {
				throw new MessagingException("Unknown recipient type!");
			}
			setRecipients(type, retval);
		}
		return retval;
	}

	@Override
	public void setRecipients(final RecipientType type, final Address[] addresses) throws MessagingException {
		if (type.equals(RecipientType.TO)) {
			to = (InternetAddress[]) addresses;
		} else if (type.equals(RecipientType.CC)) {
			cc = (InternetAddress[]) addresses;
		} else if (type.equals(RecipientType.BCC)) {
			bcc = (InternetAddress[]) addresses;
		} else {
			throw new MessagingException("Unknown recipient type");
		}
	}

	@Override
	public void addRecipients(final RecipientType type, final Address[] addresses) throws MessagingException {
		if (addresses == null) {
			throw new MessagingException("Missing from address");
		}
		InternetAddress[] member = getRecipientsInternal(type);
		if (member == null) {
			member = new InternetAddress[addresses.length];
			for (int i = 0; i < addresses.length; i++) {
				member[i] = (InternetAddress) addresses[i];
			}
			return;
		}
		final InternetAddress[] tmp = member;
		member = new InternetAddress[tmp.length + addresses.length];
		int c = 0;
		for (int i = 0; i < tmp.length; i++) {
			member[c++] = tmp[i];
		}
		for (int i = 0; i < addresses.length; i++) {
			member[c++] = (InternetAddress) addresses[i];
		}
	}

	@Override
	public String getSubject() {
		try {
			return subject != null ? subject : headers.containsKey(MessageHeaders.HDR_SUBJECT) ? MimeUtility
					.decodeText(headers.get(MessageHeaders.HDR_SUBJECT)) : null;
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Unsupported encoding in a message detected and monitored.", e);
			MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			return headers.containsKey(MessageHeaders.HDR_SUBJECT) ? MessageUtility.decodeMultiEncodedHeader(headers
					.get(MessageHeaders.HDR_SUBJECT)) : null;
		}
	}

	@Override
	public void setSubject(final String subject) {
		this.subject = subject;
	}

	@Override
	public boolean isExpunged() {
		return expunged;
	}

	@Override
	public void setExpunged(final boolean expunged) {
		this.expunged = expunged;
	}

	public boolean hasAttachment() {
		return hasAttachment;
	}

	public void setHasAttachment(final boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
	}
	
	private static final MailDateFormat MDF = new MailDateFormat();

	@Override
	public Date getSentDate() throws MessagingException {
		try {
			return date != null ? date : headers.containsKey(MessageHeaders.HDR_DATE) ? MDF.parse(headers
					.get(MessageHeaders.HDR_SUBJECT)) : null;
		} catch (final ParseException e) {
			throw new MessagingException(e.getMessage(), e);
		}
	}

	@Override
	public void setSentDate(final Date date) {
		this.date = date;
	}

	@Override
	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(final Date date) {
		this.receivedDate = date;
	}

	@Override
	public Flags getFlags() {
		return (Flags) flags.clone();
	}

	@Override
	public void setFlags(final Flags flag, final boolean set) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);

	}

	public void setFlags(final Flags flag) {
		this.flags = flag;
	}

	@Override
	public Message reply(final boolean replyToAll) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	@Override
	public void saveChanges() throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Message#getMessageNumber()
	 */
	@Override
	public int getMessageNumber() {
		return seqNum;
	}

	public int getSize() {
		return size;
	}

	public void setSize(final int size) {
		this.size = size;
	}

	public int getLineCount() throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public String getContentType() {
		return contentType != null ? contentType.toString()
				: headers.containsKey(MessageHeaders.HDR_CONTENT_TYPE) ? headers.get(MessageHeaders.HDR_CONTENT_TYPE)
						: null;
	}

	public boolean isMimeType(final String mimeType) throws MessagingException {
		if (contentType != null) {
			return contentType.isMimeType(mimeType);
		} else if (headers.containsKey(MessageHeaders.HDR_CONTENT_TYPE)) {
			try {
				return (contentType = new ContentType(headers.get(MessageHeaders.HDR_CONTENT_TYPE)))
						.isMimeType(mimeType);
			} catch (final MailException e) {
				throw new MessagingException(e.getMessage(), e);
			}
		}
		throw new MessagingException(new StringBuilder().append("Header ").append(MessageHeaders.HDR_CONTENT_TYPE)
				.append(" not set!").toString());
	}

	public void setContentType(final ContentType ct) {
		this.contentType = ct;
	}

	public String getDisposition() throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public void setDisposition(final String disposition) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public String getDescription() throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public void setDescription(final String description) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public String getFileName() throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public void setFileName(final String filename) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public InputStream getInputStream() throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public DataHandler getDataHandler() throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public Object getContent() throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public void setDataHandler(final DataHandler dh) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public void setContent(final Object obj, final String type) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public void setText(final String text) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public void setContent(final Multipart mp) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public void writeTo(final OutputStream os) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	private static final String SPLIT_COMMA = " *, *";

	public String[] getHeader(final String header_name) {
		if (header_name.equalsIgnoreCase(MessageHeaders.HDR_X_PRIORITY)) {
			return new String[] { String.valueOf(priority) };
		} else if (header_name.equalsIgnoreCase(MessageHeaders.HDR_MESSAGE_ID)) {
			return messageId != null ? messageId.split(SPLIT_COMMA) : headers
					.containsKey(MessageHeaders.HDR_MESSAGE_ID) ? (messageId = headers
					.get(MessageHeaders.HDR_MESSAGE_ID)).split(SPLIT_COMMA) : null;
		}
		return headers.containsKey(header_name) ? headers.get(header_name).split(SPLIT_COMMA) : null;
	}

	public void setHeader(final String header_name, final String header_value) throws MessagingException {
		if (header_name.equalsIgnoreCase(MessageHeaders.HDR_X_PRIORITY)) {
			try {
				priority = parsePriorityStr(header_value);
			} catch (final NumberFormatException e) {
				throw new MessagingException(String.format("Unsupported header value: %s", header_value), e);
			}
			return;
		}
		headers.put(header_name, header_value);
	}

	public void addHeader(final String header_name, final String header_value) {
		if (headers.containsKey(header_name)) {
			headers.put(header_name, new StringBuilder().append(headers.get(header_name)).append(',').append(
					header_value).toString());
			return;
		}
		headers.put(header_name, header_value);
	}

	public void removeHeader(final String header_name) {
		headers.remove(header_name);
	}

	public Iterator<String> getHeaderNames() {
		return headers.keySet().iterator();
	}

	public Iterator<Map.Entry<String, String>> getHeaders() {
		return headers.entrySet().iterator();
	}

	public int getNumfHeaders() {
		return headers.size();
	}

	public Enumeration<?> getAllHeaders() throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public Enumeration<?> getMatchingHeaders(final String[] header_names) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public Enumeration<?> getNonMatchingHeaders(final String[] header_names) throws MessagingException {
		throw new MessagingException(ERR_METHOD_NOT_SUPPORTED);
	}

	public String getFolderFullname() {
		return folderFullname;
	}

	@Override
	public Address[] getReplyTo() {
		return replyTo != null ? replyTo
				: headers.containsKey(MessageHeaders.HDR_REPLY_TO) ? (replyTo = new InternetAddress[] { new DummyAddress(
						headers.get(MessageHeaders.HDR_REPLY_TO)) })
						: null;
	}

	@Override
	public void setReplyTo(final Address[] addresses) {
		this.replyTo = (InternetAddress[]) addresses;
	}

	public String getInReplyTo() {
		return inReplyTo != null ? inReplyTo
				: headers.containsKey(MessageHeaders.HDR_IN_REPLY_TO) ? (inReplyTo = MessageHeaders.HDR_IN_REPLY_TO)
						: null;
	}

	public void setInReplyTo(final String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public String getMessageId() {
		return messageId != null ? messageId
				: headers.containsKey(MessageHeaders.HDR_MESSAGE_ID) ? (messageId = headers
						.get(MessageHeaders.HDR_MESSAGE_ID)) : null;
	}

	public void setMessageId(final String messageId) {
		this.messageId = messageId;
		headers.put(MessageHeaders.HDR_MESSAGE_ID, messageId);
	}

	public int getThreadLevel() {
		return threadLevel;
	}

	public void setThreadLevel(final int threadLevel) {
		this.threadLevel = threadLevel;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(final long uid) {
		this.uid = uid;
	}

	public char getSeparator() {
		return separator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("uid=").append(uid);
		sb.append(" folder=").append(folderFullname);
		sb.append(" seqnum=").append(seqNum);
		sb.append(" subject=").append(subject);
		return sb.toString();
	}

	/**
	 * @return this message's IMAP folder
	 */
	public Folder getFolder(final Store store) throws MessagingException {
		return store.getFolder(folderFullname);
	}

	/**
	 * {@link DummyAddress} - extends {@link InternetAddress}
	 *
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 *
	 */
	public static final class DummyAddress extends InternetAddress {

		private static final long serialVersionUID = -3276144799717449603L;

		private static final String TYPE = "rfc822";

		private final String address;
		
		private final int hashCode;

		public DummyAddress(final String address) {
			this.address = MessageUtility.decodeMultiEncodedHeader(address);
			hashCode = address.toLowerCase().hashCode();
		}

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public String toString() {
			return address;
		}

		@Override
		public String getAddress() {
			return address;
		}

		@Override
		public String getPersonal() {
			return null;
		}

		@Override
		public boolean equals(final Object address) {
			if (address instanceof InternetAddress) {
				final InternetAddress ia = (InternetAddress) address;
				return this.address.equalsIgnoreCase(ia.getAddress());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	/**
	 * Gets the bodystructure
	 *
	 * @return the bodystructure
	 */
	public BODYSTRUCTURE getBodystructure() {
		return bodystructure;
	}

	/**
	 * Sets the bodystructure
	 *
	 * @param bodystructure the bodystructure to set
	 */
	public void setBodystructure(BODYSTRUCTURE bodystructure) {
		this.bodystructure = bodystructure;
	}
}
