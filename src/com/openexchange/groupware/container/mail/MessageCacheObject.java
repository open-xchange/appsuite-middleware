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

package com.openexchange.groupware.container.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.tools.mail.ContentType;

/**
 * <p>
 * MessageCacheObject - a very slim subclass of abstract class
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
public class MessageCacheObject extends Message implements Serializable {

	private static final long serialVersionUID = -5236672658788027516L;

	private static final String HDR_X_PRIORITY = "X-Priority";

	/**
	 * Folder's full name
	 */
	private final String folderFullname;

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

	private String subject;

	private Date date;

	private Date receivedDate;

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

	private final int seqNum;

	private int threadLevel;

	private final Map<String, String> headers;

	public MessageCacheObject(final String folderFullname, final int msgnum) {
		super();
		this.seqNum = msgnum;
		this.folderFullname = folderFullname;
		this.headers = new HashMap<String, String>();
	}

	/**
	 * Constructs a new <code>MessageCacheObject</code> instance from given
	 * <b>PRE-FILLED</b> <code>Message</code> object
	 */
	public MessageCacheObject(final Message msg, final long msgUID) throws MessagingException, OXException {
		super(msg.getFolder(), msg.getMessageNumber());
		this.headers = new HashMap<String, String>();
		this.seqNum = msg.getMessageNumber();
		this.uid = msgUID;
		this.folderFullname = msg.getFolder().getFullName();
		this.from = (InternetAddress[]) msg.getFrom();
		this.to = (InternetAddress[]) msg.getRecipients(RecipientType.TO);
		this.cc = (InternetAddress[]) msg.getRecipients(RecipientType.CC);
		this.bcc = (InternetAddress[]) msg.getRecipients(RecipientType.BCC);
		this.replyTo = msg.getReplyTo() == null ? null : (InternetAddress[]) msg.getReplyTo();
		this.subject = msg.getSubject();
		this.date = msg.getSentDate();
		this.receivedDate = msg.getReceivedDate();
		this.flags = msg.getFlags();
		this.contentType = new ContentType(msg.getContentType());
		this.size = msg.getSize();
		final String[] tmp;
		this.priority = (tmp = msg.getHeader(HDR_X_PRIORITY)) == null || tmp[0] == null || tmp[0].length() == 0 ? JSONMessageObject.PRIORITY_NORMAL
				: parsePriorityStr(tmp[0]);
	}

	private static final int parsePriorityStr(final String priorityHdrArg) {
		final String priorityHdr = priorityHdrArg.trim();
		final int pos = priorityHdr.indexOf(' ');
		try {
			if (pos == -1) {
				return Integer.parseInt(priorityHdr);
			} else {
				return Integer.parseInt(priorityHdr.substring(0, pos));
			}
		} catch (NumberFormatException e) {
			return JSONMessageObject.PRIORITY_NORMAL;
		}
	}

	private static final String HDR_FROM = "From";

	@Override
	public Address[] getFrom() throws MessagingException {
		if (from != null) {
			return from;
		}
		return headers.containsKey(HDR_FROM) ? (from = new InternetAddress[] { new DummyAddress(headers.get(HDR_FROM)) }) : null;
	}

	@Override
	public void setFrom() throws MessagingException {
		throw new MessagingException("Missing from address");
	}

	public void setFrom(final InternetAddress[] from) throws MessagingException {
		this.from = from;
	}

	@Override
	public void setFrom(final Address address) throws MessagingException {
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

	private final InternetAddress[] getRecipientsInternal(final RecipientType type) throws MessagingException {
		if (type.equals(RecipientType.TO)) {
			return to;
		} else if (type.equals(RecipientType.CC)) {
			return cc;
		} else if (type.equals(RecipientType.BCC)) {
			return bcc;
		}
		throw new MessagingException("Unknown recipient type");
	}

	private static final String HDR_TO = "To";

	private static final String HDR_CC = "Cc";

	private static final String HDR_BCC = "Bcc";

	@Override
	public Address[] getRecipients(final RecipientType type) throws MessagingException {
		Address[] retval = getRecipientsInternal(type);
		if (retval == null) {
			if (type.equals(RecipientType.TO)) {
				retval = headers.containsKey(HDR_TO) ? new InternetAddress[] { new DummyAddress(headers.get(HDR_TO)) } : null;
			} else if (type.equals(RecipientType.CC)) {
				retval = headers.containsKey(HDR_CC) ? new InternetAddress[] { new DummyAddress(headers.get(HDR_CC)) } : null;
			} else if (type.equals(RecipientType.BCC)) {
				retval = headers.containsKey(HDR_BCC) ? new InternetAddress[] { new DummyAddress(headers.get(HDR_BCC)) } : null;
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

	private static final String HDR_SUBJECT = "Subject";

	@Override
	public String getSubject() throws MessagingException {
		return subject != null ? subject : headers.containsKey(HDR_SUBJECT) ? MessageUtils
				.decodeMultiEncodedHeader(headers.get(HDR_SUBJECT)) : null;
	}

	@Override
	public void setSubject(final String subject) throws MessagingException {
		this.subject = subject;
	}

	private static final String HDR_DATE = "Date";

	private static final MailDateFormat MDF = new MailDateFormat();

	@Override
	public Date getSentDate() throws MessagingException {
		try {
			return date != null ? date : headers.containsKey(HDR_DATE) ? MDF.parse(headers.get(HDR_SUBJECT)) : null;
		} catch (ParseException e) {
			throw new MessagingException(e.getMessage(), e);
		}
	}

	@Override
	public void setSentDate(final Date date) throws MessagingException {
		this.date = date;
	}

	@Override
	public Date getReceivedDate() throws MessagingException {
		return receivedDate;
	}

	public void setReceivedDate(final Date date) throws MessagingException {
		this.receivedDate = date;
	}

	@Override
	public Flags getFlags() throws MessagingException {
		return (Flags) flags.clone();
	}

	@Override
	public void setFlags(final Flags flag, final boolean set) throws MessagingException {
		throw new MessagingException("Method not supported");

	}

	public void setFlags(final Flags flag) throws MessagingException {
		this.flags = flag;
	}

	@Override
	public Message reply(final boolean replyToAll) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	@Override
	public void saveChanges() throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Message#getMessageNumber()
	 */
	public int getMessageNumber() {
		return seqNum;
	}

	public int getSize() throws MessagingException {
		return size;
	}

	public void setSize(final int size) {
		this.size = size;
	}

	public int getLineCount() throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	private static final String HDR_CONTENT_TYPE = "Content-Type";

	public String getContentType() throws MessagingException {
		return contentType != null ? contentType.toString() : headers.containsKey(HDR_CONTENT_TYPE) ? headers
				.get(HDR_CONTENT_TYPE) : null;
	}

	public boolean isMimeType(final String mimeType) throws MessagingException {
		if (contentType != null) {
			return contentType.isMimeType(mimeType);
		} else if (headers.containsKey(HDR_CONTENT_TYPE)) {
			try {
				return (contentType = new ContentType(headers.get(HDR_CONTENT_TYPE))).isMimeType(mimeType);
			} catch (OXException e) {
				throw new MessagingException(e.getMessage(), e);
			}
		}
		throw new MessagingException(new StringBuilder().append("Header ").append(HDR_CONTENT_TYPE).append(" not set!")
				.toString());
	}

	public void setContentType(final ContentType ct) {
		this.contentType = ct;
	}

	public String getDisposition() throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public void setDisposition(final String disposition) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public String getDescription() throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public void setDescription(final String description) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public String getFileName() throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public void setFileName(final String filename) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public InputStream getInputStream() throws IOException, MessagingException {
		throw new MessagingException("Method not supported");
	}

	public DataHandler getDataHandler() throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public Object getContent() throws IOException, MessagingException {
		throw new MessagingException("Method not supported");
	}

	public void setDataHandler(final DataHandler dh) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public void setContent(final Object obj, final String type) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public void setText(final String text) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public void setContent(final Multipart mp) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public void writeTo(final OutputStream os) throws IOException, MessagingException {
		throw new MessagingException("Method not supported");
	}

	private static final String SPLIT_COMMA = " *, *";

	public String[] getHeader(final String header_name) throws MessagingException {
		if (header_name.equalsIgnoreCase(HDR_X_PRIORITY)) {
			return new String[] { String.valueOf(priority) };
		}
		return headers.containsKey(header_name) ? headers.get(header_name).split(SPLIT_COMMA) : null;
	}

	public void setHeader(final String header_name, final String header_value) throws MessagingException {
		if (header_name.equalsIgnoreCase(HDR_X_PRIORITY)) {
			try {
				priority = parsePriorityStr(header_value);
			} catch (NumberFormatException e) {
				throw new MessagingException(String.format("Unsupported header value: %s", header_value), e);
			}
			return;
		}
		headers.put(header_name, header_value);
	}

	public void addHeader(final String header_name, final String header_value) throws MessagingException {
		if (headers.containsKey(header_name)) {
			headers.put(header_name, new StringBuilder().append(headers.get(header_name)).append(',').append(
					header_value).toString());
			return;
		}
		headers.put(header_name, header_value);
	}

	public void removeHeader(final String header_name) throws MessagingException {
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

	public Enumeration getAllHeaders() throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public Enumeration getMatchingHeaders(final String[] header_names) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public Enumeration getNonMatchingHeaders(final String[] header_names) throws MessagingException {
		throw new MessagingException("Method not supported");
	}

	public String getFolderFullname() {
		return folderFullname;
	}

	private static final String HDR_REPLY_TO = "Reply-To";

	public Address[] getReplyTo() {
		return replyTo != null ? replyTo : headers.containsKey(HDR_REPLY_TO) ? (replyTo = new InternetAddress[] { new DummyAddress(headers
				.get(HDR_REPLY_TO)) }) : null;
	}

	public void setReplyTo(final Address[] addresses) throws MessagingException {
		this.replyTo = (InternetAddress[]) addresses;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
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

	public static class DummyAddress extends InternetAddress {

		private static final long serialVersionUID = -3276144799717449603L;

		private static final String TYPE = "rfc822";

		private final String address;

		public DummyAddress(final String address) {
			this.address = MessageUtils.decodeMultiEncodedHeader(address);
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
			} else {
				return false;
			}
		}

	}

}
