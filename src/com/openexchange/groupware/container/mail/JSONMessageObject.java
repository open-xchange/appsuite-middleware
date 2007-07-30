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

import static com.openexchange.groupware.container.mail.parser.MessageUtils.parseAddressList;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.UserSettingMail;
import com.openexchange.imap.OXMailException.MailCode;

/**
 * JSONMessageObject
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class JSONMessageObject {

	private static final String STR_TRUE = "true";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(JSONMessageObject.class);

	/*
	 * Constant strings for JSON
	 */
	public static final String JSON_HEADERS = "headers";

	public static final String JSON_NESTED_MESSAGES = "nested_msgs";

	public static final String JSON_ID = "id";

	public static final String JSON_ATTACHMENT_FILE_NAME = "filename";

	public static final String JSON_ATTACHMENT_UNIQUE_DISK_FILE_NAME = "disk";

	public static final String JSON_FLAGS = "flags";

	public static final String JSON_VALUE = "value";

	public static final String JSON_THREAD_LEVEL = "level";

	public static final String JSON_FROM = "from";

	public static final String JSON_RECIPIENT_TO = "to";

	public static final String JSON_RECIPIENT_CC = "cc";

	public static final String JSON_RECIPIENT_BCC = "bcc";

	public static final String JSON_SUBJECT = "subject";

	public static final String JSON_SENT_DATE = "sent_date";

	public static final String JSON_RECEIVED_DATE = "received_date";

	public static final String JSON_SIZE = "size";

	public static final String JSON_CONTENT_TYPE = "content_type";

	public static final String JSON_CONTENT = "content";

	public static final String JSON_ATTACHMENTS = "attachments";

	public static final String JSON_HAS_ATTACHMENTS = "attachment";

	public static final String JSON_DISPOSITION = "disp";

	public static final String JSON_USER = "user";

	public static final String JSON_DISPOSITION_NOTIFICATION_TO = "disp_notification_to";

	public static final String JSON_PRIORITY = "priority";

	public static final String JSON_MSGREF = "msgref";

	public static final String JSON_COLOR_LABEL = CommonFields.COLORLABEL;

	public static final String JSON_INFOSTORE_IDS = "infostore_ids";

	public static final String JSON_VCARD = "vcard";
	
	public static final String JSON_TOTAL = "total";

	public static final String JSON_NEW = "new";

	public static final String JSON_UNREAD = "unread";

	public static final String JSON_DELETED = "deleted";

	/*
	 * Constant integers for mail fields
	 */

	public static final int FIELD_ID = 600;

	public static final int FIELD_FOLDER_ID = 601;

	public static final int FIELD_ATTACHMENT = 602;

	public static final int FIELD_FROM = 603;

	public static final int FIELD_TO = 604;

	public static final int FIELD_CC = 605;

	public static final int FIELD_BCC = 606;

	public static final int FIELD_SUBJECT = 607;

	public static final int FIELD_SIZE = 608;

	public static final int FIELD_SENT_DATE = 609;

	public static final int FIELD_RECEIVED_DATE = 610;

	public static final int FIELD_FLAGS = 611;

	public static final int FIELD_THREAD_LEVEL = 612;

	public static final int FIELD_DISPOSITION_NOTIFICATION_TO = 613;

	public static final int FIELD_PRIORITY = 614;

	public static final int FIELD_MSG_REF = 615;

	public static final int FIELD_COLOR_LABEL = CommonObject.COLOR_LABEL;

	public static final int FIELD_FOLDER = 650;

	public static final int FIELD_FLAG_SEEN = 651;
	
	public static final int FIELD_TOTAL = FolderObject.TOTAL;

	public static final int FIELD_NEW = FolderObject.NEW;

	public static final int FIELD_UNREAD = FolderObject.UNREAD;

	public static final int FIELD_DELETED = FolderObject.DELETED;

	/*
	 * Constant integers for bit-encoded flag set.
	 */

	/**
	 * This message has been answered. This flag is set by clients to indicate
	 * that this message has been answered to.
	 * 
	 * @value 1
	 */
	public static final int BIT_ANSWERED = 1;

	/**
	 * This message is marked deleted. Clients set this flag to mark a message
	 * as deleted. The expunge operation on a folder removes all messages in
	 * that folder that are marked for deletion.
	 * 
	 * @value 2
	 */
	public static final int BIT_DELETED = 2;

	/**
	 * This message is a draft. This flag is set by clients to indicate that the
	 * message is a draft message.
	 * 
	 * @value 4
	 */
	public static final int BIT_DRAFT = 4;

	/**
	 * This message is flagged. No semantic is defined for this flag. Clients
	 * alter this flag.
	 * 
	 * @value 8
	 */
	public static final int BIT_FLAGGED = 8;

	/**
	 * This message is recent. Folder implementations set this flag to indicate
	 * that this message is new to this folder, that is, it has arrived since
	 * the last time this folder was opened.
	 * 
	 * @value 16
	 * 
	 */
	public static final int BIT_RECENT = 16;

	/**
	 * This message is seen. This flag is implicitly set by the implementation
	 * when the this Message's content is returned to the client in some form.
	 * 
	 * @value 32
	 */
	public static final int BIT_SEEN = 32;

	/**
	 * A special flag that indicates that this folder supports user defined
	 * flags
	 * 
	 * @value 64
	 */
	public static final int BIT_USER = 64;
	
	/**
	 * Virtal Spam flags
	 * 
	 * @value 128
	 */
	public static final int BIT_SPAM = 128;

	/*
	 * ------------------- Priority ------------------------------
	 */
	public static final int PRIORITY_HIGHEST = 1;

	public static final int PRIORITY_HIGH = 2;

	public static final int PRIORITY_NORMAL = 3;

	public static final int PRIORITY_LOW = 4;

	public static final int PRIORITY_LOWEST = 5;

	/*
	 * ------------------- Color Label ------------------------------
	 */
	public static final String COLOR_LABEL_PREFIX = "cl_";

	public static final int COLOR_LABEL_NONE = 0;

	public static int getColorLabelIntValue(final String cl) throws OXException {
		if (!cl.startsWith(COLOR_LABEL_PREFIX)) {
			throw new OXMailException(MailCode.UNKNOWN_COLOR_LABEL, cl);
		}
		return Integer.parseInt(cl.substring(3));
	}

	public static String getColorLabelStringValue(final int cl) {
		return new StringBuilder(COLOR_LABEL_PREFIX).append(cl).toString();
	}

	/*
	 * ------------------- USER FLAG CONSTANT(S) ------------------------------
	 */
	public static final String USER_FLAG_ATTACHMENT = "attach";

	public static final String USER_FLAG_NO_ATTACHMENT = "noattach";

	public static final String USER_FLAG_MSGREF = "msgref";

	/*
	 * ------------------- Members ------------------------------
	 */

	private List<JSONMessageObject> nestedMsgs;

	private List<JSONMessageAttachmentObject> msgAttachments;

	private int threadLevel;

	private int flags;

	private final Set<InternetAddress> from;

	private final Set<InternetAddress> to;

	private final Set<InternetAddress> cc;

	private final Set<InternetAddress> bcc;

	private String subject;

	private int size = -1;

	private Date sentDate;

	private Date receivedDate;

	private String dispositionNotification;

	private final UserSettingMail userSettingMail;

	private final TimeZone userTimeZone;

	private int priority = PRIORITY_NORMAL;

	private int colorLabel = COLOR_LABEL_NONE;

	private String msgref;

	private boolean appendVCard;

	private Map<String, String> headers;
	
	private boolean messageCountInfoSet;
	
	private int total;
	
	private int newi;
	
	private int unread;
	
	private int deleted;

	/**
	 * Something like { "x-mailer": "foo", "x-spam": "bar" }
	 */
	private List<String> userFlags;

	public JSONMessageObject(UserSettingMail userSettingMail, TimeZone userTimeZone) {
		super();
		nestedMsgs = new ArrayList<JSONMessageObject>();
		msgAttachments = new ArrayList<JSONMessageAttachmentObject>();
		from = new HashSet<InternetAddress>();
		to = new HashSet<InternetAddress>();
		cc = new HashSet<InternetAddress>();
		bcc = new HashSet<InternetAddress>();
		userFlags = new ArrayList<String>();
		this.userSettingMail = userSettingMail;
		this.userTimeZone = userTimeZone;
		this.headers = new HashMap<String, String>();
	}

	public void addNestedMessage(final JSONMessageObject mo) {
		nestedMsgs.add(mo);
	}

	public JSONMessageObject removeMessageContentObject() {
		return nestedMsgs.remove(nestedMsgs.size() - 1);
	}

	public JSONMessageObject removeMessageContentObject(final int index) {
		return nestedMsgs.remove(index);
	}

	public void clearMessageContentObjects() {
		nestedMsgs.clear();
	}

	public List<JSONMessageObject> getNestedMsgs() {
		return nestedMsgs;
	}

	public void setNestedMsgs(final List<JSONMessageObject> nestedMsgs) {
		this.nestedMsgs = nestedMsgs;
	}

	public List<JSONMessageAttachmentObject> getMsgAttachments() {
		return msgAttachments;
	}

	public void addMessageAttachment(final JSONMessageAttachmentObject mao) {
		msgAttachments.add(mao);
	}

	public void setMsgAttachments(final List<JSONMessageAttachmentObject> msgAttachments) {
		this.msgAttachments = msgAttachments;
	}

	public boolean isAnswered() {
		return ((flags & BIT_ANSWERED) == BIT_ANSWERED);
	}

	public boolean isDeleted() {
		return ((flags & BIT_DELETED) == BIT_DELETED);
	}

	public boolean isDraft() {
		return ((flags & BIT_DRAFT) == BIT_DRAFT);
	}

	public boolean isFlagged() {
		return ((flags & BIT_FLAGGED) == BIT_FLAGGED);
	}

	public boolean isRecent() {
		return ((flags & BIT_RECENT) == BIT_RECENT);
	}

	public boolean isSeen() {
		return ((flags & BIT_SEEN) == BIT_SEEN);
	}

	public boolean isUser() {
		return ((flags & BIT_USER) == BIT_USER);
	}

	public void setAnswered(final boolean answered) {
		flags = answered ? (flags | BIT_ANSWERED) : (isAnswered() ? flags ^ BIT_ANSWERED : flags);
	}

	public void setDeleted(final boolean deleted) {
		flags = deleted ? (flags | BIT_DELETED) : (isDeleted() ? flags ^ BIT_DELETED : flags);
	}

	public void setDraft(final boolean draft) {
		flags = draft ? (flags | BIT_DRAFT) : (isDraft() ? flags ^ BIT_DRAFT : flags);
	}

	public void setFlagged(final boolean flagged) {
		flags = flagged ? (flags | BIT_FLAGGED) : (isFlagged() ? flags ^ BIT_FLAGGED : flags);
	}

	public void setRecent(final boolean recent) {
		flags = recent ? (flags | BIT_RECENT) : (isRecent() ? flags ^ BIT_RECENT : flags);
	}

	public void setSeen(final boolean seen) {
		flags = seen ? (flags | BIT_SEEN) : (isSeen() ? flags ^ BIT_SEEN : flags);
	}

	public void setUser(final boolean user) {
		flags = user ? (flags | BIT_USER) : (isUser() ? flags ^ BIT_USER : flags);
	}

	public int getThreadLevel() {
		return threadLevel;
	}

	public void setThreadLevel(final int threadLevel) {
		this.threadLevel = threadLevel;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(final int flags) {
		this.flags = flags;
	}

	public Collection<InternetAddress> getBcc() {
		return bcc;
	}
	
	public InternetAddress[] getBccAsArray() {
		return bcc.toArray(new InternetAddress[bcc.size()]);
	}

	public void addBccAddress(final String addr) throws AddressException {
		bcc.add(new InternetAddress(addr, false));
	}

	public void addBccAddresses(final InternetAddress[] addrs) {
		if (addrs != null) {
			bcc.addAll(Arrays.asList(addrs));
		}
	}

	public void setBcc(final Collection<InternetAddress> bcc) {
		this.bcc.clear();
		this.bcc.addAll(bcc);
	}

	public Collection<InternetAddress> getCc() {
		return cc;
	}
	
	public InternetAddress[] getCcAsArray() {
		return cc.toArray(new InternetAddress[cc.size()]);
	}

	public void addCCAddress(final String addr) throws AddressException {
		cc.add(new InternetAddress(addr, false));
	}

	public void addCCAddresses(final InternetAddress[] addrs) {
		if (addrs != null) {
			cc.addAll(Arrays.asList(addrs));
		}
	}

	public void setCc(final Collection<InternetAddress> cc) {
		this.cc.clear();
		this.cc.addAll(cc);
	}

	public Collection<InternetAddress> getFrom() {
		return from;
	}
	
	public InternetAddress[] getFromAsArray() {
		return from.toArray(new InternetAddress[from.size()]);
	}

	public void addFromAddress(final String addr) throws AddressException {
		from.add(new InternetAddress(addr, false));
	}

	public void addFromAddresses(final InternetAddress[] addrs) {
		if (addrs != null) {
			from.addAll(Arrays.asList(addrs));
		}
	}

	public void setFrom(final Collection<InternetAddress> from) {
		this.from.clear();
		this.from.addAll(from);
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(final Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public String getDispositionNotification() {
		return dispositionNotification;
	}

	public void setDispositionNotification(final String dispositionNotification) {
		this.dispositionNotification = dispositionNotification;
	}

	public UserSettingMail getUserSettingMail() {
		return userSettingMail;
	}

	public TimeZone getUserTimeZone() {
		return userTimeZone;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(final Date sentDate) {
		this.sentDate = sentDate;
	}

	public int getSize() {
		return size;
	}

	public void setSize(final int size) {
		this.size = size;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public Collection<InternetAddress> getTo() {
		return to;
	}
	
	public InternetAddress[] getToAsArray() {
		return to.toArray(new InternetAddress[to.size()]);
	}

	public void addToAddress(final String addr) throws AddressException {
		to.add(new InternetAddress(addr, false));
	}

	public void addToAddresses(final InternetAddress[] addrs) {
		if (addrs != null) {
			to.addAll(Arrays.asList(addrs));
		}
	}

	public void setTo(final Collection<InternetAddress> to) {
		this.to.clear();
		this.to.addAll(to);
	}

	public List<String> getUserFlags() {
		return userFlags;
	}

	public void addUserFlag(final String flag) {
		userFlags.add(flag);
	}

	public void setUserFlags(final List<String> user) {
		this.userFlags = user;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void addHeader(final String name, final String value) {
		headers.put(name, value);
	}

	public void setHeaders(final Map<String, String> headers) {
		this.headers = headers;
	}

	public Iterator<String> getHeaderNames() {
		return headers.keySet().iterator();
	}

	public Iterator<Map.Entry<String, String>> getHeadersIterator() {
		return headers.entrySet().iterator();
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(final int priority) {
		this.priority = priority;
	}

	public int getColorLabel() {
		return colorLabel;
	}

	public void setColorLabel(final int colorLabel) {
		this.colorLabel = colorLabel;
	}

	public String getMsgref() {
		return msgref;
	}

	public void setMsgref(final String msgref) {
		this.msgref = msgref;
	}

	public int getDeleted() {
		return deleted;
	}

	public void setDeleted(final int deleted) {
		messageCountInfoSet = true;
		this.deleted = deleted;
	}

	public int getNew() {
		return newi;
	}

	public void setNew(final int newi) {
		messageCountInfoSet = true;
		this.newi = newi;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(final int total) {
		messageCountInfoSet = true;
		this.total = total;
	}

	public int getUnread() {
		return unread;
	}

	public void setUnread(final int unread) {
		messageCountInfoSet = true;
		this.unread = unread;
	}

	public boolean isMessageCountInfoSet() {
		return messageCountInfoSet;
	}

	/*public void setMessageCountInfoSet(final boolean messageCountInfoSet) {
		this.messageCountInfoSet = messageCountInfoSet;
	}*/

	public boolean isAppendVCard() {
		return appendVCard;
	}

	public void setAppendVCard(final boolean appendVCard) {
		this.appendVCard = appendVCard;
	}

	public JSONObject getJSONObject() throws JSONException {
		final JSONObject retval = new JSONObject();
		retval.put(JSON_FROM, getAddressesAsArray(this.from));
		retval.put(JSON_RECIPIENT_TO, getAddressesAsArray(this.to));
		retval.put(JSON_RECIPIENT_CC, getAddressesAsArray(this.cc));
		retval.put(JSON_RECIPIENT_BCC, getAddressesAsArray(this.bcc));
		retval.put(JSON_SUBJECT, subject);
		retval.put(JSON_SIZE, size);
		retval.put(JSON_SENT_DATE, (sentDate == null ? JSONObject.NULL : Long.valueOf(addUserTimezone(sentDate.getTime(),
				userTimeZone))));
		retval.put(JSON_RECEIVED_DATE, (receivedDate == null ? JSONObject.NULL : Long.valueOf(addUserTimezone(
				receivedDate.getTime(), userTimeZone))));
		retval.put(JSON_FLAGS, flags);
		retval.put(JSON_THREAD_LEVEL, threadLevel);
		retval.put(JSON_USER, getUserFieldsAsObject(userFlags));
		retval.put(JSON_HEADERS, getHeadersAsObject(headers));
		retval.put(JSON_DISPOSITION_NOTIFICATION_TO, dispositionNotification == null ? JSONObject.NULL
				: dispositionNotification);
		retval.put(JSON_PRIORITY, priority);
		try {
			if (IMAPProperties.isUserFlagsEnabled()) {
				retval.put(JSON_COLOR_LABEL, colorLabel);
			} else {
				retval.put(JSON_COLOR_LABEL, COLOR_LABEL_NONE);
			}
		} catch (OXException e) {
			retval.put(JSON_COLOR_LABEL, COLOR_LABEL_NONE);
		}
		if (messageCountInfoSet) {
			retval.put(JSON_TOTAL, total);
			retval.put(JSON_NEW, newi);
			retval.put(JSON_UNREAD, unread);
			retval.put(JSON_DELETED, deleted);
		}
		retval.putOpt(JSON_ATTACHMENTS, getAttachmentsAsArray(msgAttachments));
		retval.putOpt(JSON_NESTED_MESSAGES, getNestedMsgsAsArray(nestedMsgs));
		return retval;
	}

	public void writeAsJSONObjectIntoJSONWriter(final JSONWriter jw) throws JSONException {
		jw.object();
		jw.key(JSON_FROM);
		jw.value(getAddressesAsArray(this.from));
		jw.key(JSON_RECIPIENT_TO);
		jw.value(getAddressesAsArray(this.to));
		jw.key(JSON_RECIPIENT_CC);
		jw.value(getAddressesAsArray(this.cc));
		jw.key(JSON_RECIPIENT_BCC);
		jw.value(getAddressesAsArray(this.bcc));
		jw.key(JSON_SUBJECT);
		jw.value(subject);
		jw.key(JSON_SIZE);
		jw.value(size);
		jw.key(JSON_SENT_DATE);
		jw.value(sentDate == null ? JSONObject.NULL : Long.valueOf(addUserTimezone(sentDate.getTime(), userTimeZone)));
		jw.key(JSON_RECEIVED_DATE);
		jw.value(receivedDate == null ? JSONObject.NULL : Long.valueOf(addUserTimezone(receivedDate.getTime(), userTimeZone)));
		jw.key(JSON_FLAGS);
		jw.value(flags);
		jw.key(JSON_THREAD_LEVEL);
		jw.value(threadLevel);
		jw.key(JSON_USER);
		jw.value(getUserFieldsAsObject(userFlags));
		jw.key(JSON_HEADERS);
		jw.value(getHeadersAsObject(headers));
		jw.key(JSON_DISPOSITION_NOTIFICATION_TO);
		jw.value(dispositionNotification == null ? JSONObject.NULL : dispositionNotification);
		jw.key(JSON_PRIORITY);
		jw.value(priority);
		jw.key(JSON_COLOR_LABEL);
		try {
			if (IMAPProperties.isUserFlagsEnabled()) {
				jw.value(colorLabel);
			} else {
				jw.value(COLOR_LABEL_NONE);
			}
		} catch (OXException e) {
			jw.value(COLOR_LABEL_NONE);
		}
		if (messageCountInfoSet) {
			jw.key(JSON_TOTAL);
			jw.value(total);
			jw.key(JSON_NEW);
			jw.value(newi);
			jw.key(JSON_UNREAD);
			jw.value(unread);
			jw.key(JSON_DELETED);
			jw.value(deleted);
		}
		if (msgref != null) {
			jw.key(JSON_MSGREF);
			jw.value(msgref);
		}
		jw.key(JSON_ATTACHMENTS);
		jw.value(getAttachmentsAsArray(msgAttachments));
		jw.key(JSON_NESTED_MESSAGES);
		jw.value(getNestedMsgsAsArray(nestedMsgs));
		jw.endObject();
	}

	public static final long addUserTimezone(final long time, final TimeZone timeZone) {
		return (time + timeZone.getOffset(time));
	}

	/**
	 * Convert an array of <code>InternetAddress</code> instances into a
	 * JSON-Array comforming to: [["The Personal", "someone@somewhere.com"],
	 * ...]
	 */
	public static final JSONArray getAddressesAsArray(final Collection<InternetAddress> addrs) {
		final JSONArray jsonArr = new JSONArray();
		if (addrs != null) {
			final int size = addrs.size();
			final Iterator<InternetAddress> iter = addrs.iterator();
			for (int i = 0; i < size; i++) {
				jsonArr.put(getAddressAsArray(iter.next()));
			}
		}
		return jsonArr;
	}

	/**
	 * Convert an <code>InternetAddress</code> instance into a JSON-Array
	 * comforming to: ["The Personal", "someone@somewhere.com"]
	 */
	private static final JSONArray getAddressAsArray(final InternetAddress addr) {
		final JSONArray retval = new JSONArray();
		retval.put(addr.getPersonal() == null || addr.getPersonal().length() == 0 ? JSONObject.NULL
				: preparePersonal(addr.getPersonal()));
		retval.put(addr.getAddress() == null || addr.getAddress().length() == 0 ? JSONObject.NULL : prepareAddress(addr
				.getAddress()));
		return retval;
	}

	private static final Pattern PATTERN_QUOTE = Pattern.compile("[.,:;<>\"]");

	private static final String preparePersonal(final String personal) {
		if (PATTERN_QUOTE.matcher(personal).find()) {
			/*
			 * Surround with double-quotes
			 */
			String pp;
			try {
				pp = MimeUtility.decodeText(personal);
			} catch (UnsupportedEncodingException e) {
				LOG.error("Unsupported encoding in a message detected and monitored.", e);
				MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
				pp = personal;
			}
			return new StringBuilder().append('"').append(pp.replaceAll("\"", "\\\\\\\"")).append('"').toString();
		}
		return personal;
	}
	
	private static final String DUMMY_DOMAIN = "@unspecified-domain";
	
	private static final String prepareAddress(final String address) {
		try {
			final String decoded = MimeUtility.decodeText(address);
			if (decoded.endsWith(DUMMY_DOMAIN)) {
				return decoded.substring(0, decoded.indexOf('@'));
			}
			return decoded;
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Unsupported encoding in a message detected and monitored.", e);
			MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			return MessageUtils.decodeMultiEncodedHeader(address);
		}
	}

	private static final JSONArray getUserFieldsAsObject(final List<String> user) {
		final JSONArray jsonArr = new JSONArray();
		final int size = user.size();
		final Iterator<String> iter = user.iterator();
		for (int i = 0; i < size; i++) {
			jsonArr.put(iter.next());
		}
		return jsonArr;
	}

	private static final JSONObject getHeadersAsObject(final Map<String, String> headers) throws JSONException {
		final JSONObject tmpJO = new JSONObject();
		final int size = headers.size();
		final Iterator<Map.Entry<String, String>> iter = headers.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, String> entry = iter.next();
			tmpJO.putOpt(entry.getKey(), entry.getValue());
		}
		return tmpJO;
	}

	private static final JSONArray getAttachmentsAsArray(final List<JSONMessageAttachmentObject> msgAttachments)
			throws JSONException {
		final JSONArray jsonArr = new JSONArray();
		final int size = msgAttachments.size();
		final Iterator<JSONMessageAttachmentObject> attachmentIter = msgAttachments.iterator();
		for (int i = 0; i < size; i++) {
			final JSONMessageAttachmentObject mao = attachmentIter.next();
			jsonArr.put(mao.getJSONObject());
		}
		return jsonArr;
	}

	private static final JSONArray getNestedMsgsAsArray(final List<JSONMessageObject> nestedMsgs) throws JSONException {
		final JSONArray jsonArr = new JSONArray();
		final int size = nestedMsgs.size();
		final Iterator<JSONMessageObject> iter = nestedMsgs.iterator();
		for (int i = 0; i < size; i++) {
			final JSONMessageObject mo = iter.next();
			jsonArr.put(mo.getJSONObject());
		}
		return jsonArr;
	}

	public JSONMessageObject parseJSONObject(final JSONObject jo) throws OXException {
		return parseJSONObject(jo, 0);
	}

	private JSONMessageObject parseJSONObject(final JSONObject jo, final int partLevel) throws OXException {
		try {
			/*
			 * System flags
			 */
			if (jo.has(JSON_FLAGS) && !jo.isNull(JSON_FLAGS)) {
				setFlags(jo.getInt(JSON_FLAGS));
			}
			/*
			 * Thread level
			 */
			if (jo.has(JSON_THREAD_LEVEL) && !jo.isNull(JSON_THREAD_LEVEL)) {
				threadLevel = jo.getInt(JSON_THREAD_LEVEL);
			}
			/*
			 * User flags
			 */
			if (jo.has(JSON_USER) && !jo.isNull(JSON_USER)) {
				final JSONArray arr = jo.getJSONArray(JSON_USER);
				final int length = arr.length();
				final List<String> l = new ArrayList<String>(length);
				for (int i = 0; i < length; i++) {
					l.add(arr.getString(i));
				}
				setUserFlags(l);
			}
			/*
			 * Parse headers
			 */
			if (jo.has(JSON_HEADERS) && !jo.isNull(JSON_HEADERS)) {
				final JSONObject obj = jo.getJSONObject(JSON_HEADERS);
				final int size = obj.length();
				final Map<String, String> m = new HashMap<String, String>();
				final Iterator iter = obj.keys();
				for (int i = 0; i < size; i++) {
					final String key = (String) iter.next();
					m.put(key, obj.getString(key));
				}
				setHeaders(m);
			}
			/*
			 * From Only mandatory if non-draft message
			 */
			setFrom(parseAddressKey(JSONMessageObject.JSON_FROM, jo));
			/*
			 * To Only mandatory if non-draft message
			 */
			setTo(parseAddressKey(JSONMessageObject.JSON_RECIPIENT_TO, jo));
			/*
			 * Cc
			 */
			setCc(parseAddressKey(JSONMessageObject.JSON_RECIPIENT_CC, jo));
			/*
			 * Bcc
			 */
			setBcc(parseAddressKey(JSONMessageObject.JSON_RECIPIENT_BCC, jo));
			/*
			 * Disposition notification
			 */
			if (jo.has(JSON_DISPOSITION_NOTIFICATION_TO) && !jo.isNull(JSON_DISPOSITION_NOTIFICATION_TO)) {
				/*
				 * Ok, disposition-notification-to is set. Check if its value is
				 * a valid email address
				 */
				final String dispVal = jo.getString(JSON_DISPOSITION_NOTIFICATION_TO);
				if (STR_TRUE.equalsIgnoreCase(dispVal)) {
					/*
					 * Boolean value "true"
					 */
					setDispositionNotification(from.size() > 0 ? from.iterator().next().getAddress() : null);
				} else if (isValidEmailAddress(dispVal)) {
					/*
					 * Valid email address
					 */
					setDispositionNotification(dispVal);
				} else {
					/*
					 * Any other value
					 */
					setDispositionNotification(null);
				}
			}
			/*
			 * Priority
			 */
			if (jo.has(JSON_PRIORITY) && !jo.isNull(JSON_PRIORITY)) {
				setPriority(jo.getInt(JSON_PRIORITY));
			}
			/*
			 * Color Label
			 */
			if (jo.has(JSON_COLOR_LABEL) && !jo.isNull(JSON_COLOR_LABEL)) {
				setColorLabel(jo.getInt(JSON_COLOR_LABEL));
			}
			/*
			 * VCard
			 */
			if (jo.has(JSON_VCARD) && !jo.isNull(JSON_VCARD)) {
				setAppendVCard((jo.getInt(JSON_VCARD) > 0));
			}
			/*
			 * Msg Ref
			 */
			if (jo.has(JSON_MSGREF) && !jo.isNull(JSON_MSGREF)) {
				setMsgref(jo.getString(JSON_MSGREF));
			}
			/*
			 * Subject, etc.
			 */
			if (jo.has(JSON_SUBJECT) && !jo.isNull(JSON_SUBJECT)) {
				setSubject(jo.getString(JSON_SUBJECT));
			}
			if (jo.has(JSON_SIZE)) {
				setSize(jo.getInt(JSON_SIZE));
			}
			if (jo.has(JSON_SENT_DATE) && !jo.isNull(JSON_SENT_DATE)) {
				final Date date = new Date(jo.getLong(JSON_SENT_DATE));
				final int offset = userTimeZone.getOffset(date.getTime());
				setSentDate(new Date(jo.getLong(JSON_SENT_DATE) - offset));
			}
			if (jo.has(JSON_RECEIVED_DATE) && !jo.isNull(JSON_RECEIVED_DATE)) {
				final Date date = new Date(jo.getLong(JSON_RECEIVED_DATE));
				final int offset = userTimeZone.getOffset(date.getTime());
				setSentDate(new Date(jo.getLong(JSON_RECEIVED_DATE) - offset));
			}
			if (jo.has(JSON_USER) && !jo.isNull(JSON_USER)) {
				final JSONArray tmpJA = jo.getJSONArray(JSON_USER);
				final List<String> l = new ArrayList<String>();
				final int size = tmpJA.length();
				for (int i = 0; i < size; i++) {
					l.add(tmpJA.getString(i));
				}
				setUserFlags(l);
			}
			/*
			 * Parse attachments
			 */
			final List<JSONMessageAttachmentObject> attachmentList = new ArrayList<JSONMessageAttachmentObject>();
			if (jo.has(JSON_ATTACHMENTS) && !jo.isNull(JSON_ATTACHMENTS)) {
				final JSONArray arr = jo.getJSONArray(JSONMessageObject.JSON_ATTACHMENTS);
				final int length = arr.length();
				for (int i = 0; i < length; i++) {
					attachmentList.add(new JSONMessageAttachmentObject().parseJSONObject(arr.getJSONObject(i),
							partLevel, i + 1));
				}
			} else {
				final JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject();
				mao.setContentType("text/plain");
				mao.setContent("");
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
				mao.setPositionInMail(MessageUtils.getIdentifier(new int[] { partLevel, 1 }));
				attachmentList.add(mao);
			}
			setMsgAttachments(attachmentList);
			/*
			 * Parse nested messages
			 */
			if (jo.has(JSON_NESTED_MESSAGES) && !jo.isNull(JSON_NESTED_MESSAGES)) {
				final List<JSONMessageObject> msgObjs = new ArrayList<JSONMessageObject>();
				final JSONArray arr = jo.getJSONArray(JSONMessageObject.JSON_NESTED_MESSAGES);
				final int length = arr.length();
				for (int i = 0; i < length; i++) {
					msgObjs.add(new JSONMessageObject(this.userSettingMail, this.userTimeZone).parseJSONObject(arr
							.getJSONObject(i), partLevel + 1));
				}
				setNestedMsgs(msgObjs);
			}
			return this;
		} catch (JSONException e) {
			throw new OXMailException(MailCode.JSON_ERROR, e, e.getMessage());
		} catch (AddressException e) {
			throw MailInterfaceImpl.handleMessagingException(e);
		}
	}

	private final List<InternetAddress> parseAddressKey(final String key, final JSONObject jo) throws JSONException,
			AddressException {
		final List<InternetAddress> retList = new ArrayList<InternetAddress>();
		String value = null;
		if (!jo.has(key) || jo.isNull(key) || (value = jo.getString(key)).length() == 0) {
			return retList;
		}
		retList.addAll(Arrays.asList(parseAddressList(value, false)));
		return retList;
	}

	private static final boolean isValidEmailAddress(final String addrStr) {
		if (addrStr == null || addrStr.length() == 0) {
			return false;
		}
		try {
			new InternetAddress(addrStr).validate();
			return true;
		} catch (AddressException e) {
			return false;
		}
	}

	public static final String getPriorityString(final int priority) {
		final StringBuilder priorityBuilder = new StringBuilder(10).append(priority);
		switch (priority) {
		case PRIORITY_HIGHEST:
			return priorityBuilder.append(" (highest)").toString();
		case PRIORITY_HIGH:
			return priorityBuilder.append(" (high)").toString();
		case PRIORITY_NORMAL:
			return priorityBuilder.append(" (normal)").toString();
		case PRIORITY_LOW:
			return priorityBuilder.append(" (low)").toString();
		case PRIORITY_LOWEST:
			return priorityBuilder.append(" (lowest)").toString();
		default:
			return priorityBuilder.toString();
		}
	}

}
