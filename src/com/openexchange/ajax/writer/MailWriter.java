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

package com.openexchange.ajax.writer;

import static com.openexchange.groupware.container.mail.JSONMessageObject.getAddressesAsArray;

import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.MailFolderObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.container.mail.MessageCacheObject;
import com.openexchange.groupware.container.mail.parser.JSONMessageHandler;
import com.openexchange.groupware.container.mail.parser.MessageDumper;
import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.ThreadSortMessage;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.sessiond.SessionObject;
import com.sun.mail.imap.IMAPFolder;

/**
 * MailWriter
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailWriter extends DataWriter {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailWriter.class);
	
	private static final String MIME_MULTIPART_MIXED = "multipart/mixed";

	public static interface MailFieldWriter {
		public void writeField(JSONWriter jsonwriter, Message msg, int level, boolean withKey) throws OXException,
				JSONException, MessagingException;
	}

	private final SessionObject session;

	private final TimeZone userTimeZone;

	public MailWriter(JSONWriter jw, SessionObject session) {
		jsonwriter = jw;
		this.session = session;
		userTimeZone = TimeZone.getTimeZone(session.getUserObject().getTimeZone());
	}

	public final void writeJSONMessageObject(final JSONMessageObject msgObj) throws JSONException {
		msgObj.writeAsJSONObjectIntoJSONWriter(jsonwriter);
	}
	
	public final void writeMessageAsJSONObject(final Message msg) throws OXException {
		writeMessageAsJSONObject(msg, true);
	}
	
	private static final String ERR_BROKEN_BODYSTRUCTURE = "unable to load bodystructure";

	public final void writeMessageAsJSONObject(final Message msg, final boolean createVersionForDisplay) throws OXException {
		try {
			final JSONMessageHandler msgHandler = new JSONMessageHandler(session, MessageUtils
					.getMessageUniqueIdentifier(msg), createVersionForDisplay);
			new MessageDumper(session).dumpMessage(msg, msgHandler);
			final JSONMessageObject msgObj = msgHandler.getMessageObject();
			msgObj.writeAsJSONObjectIntoJSONWriter(jsonwriter);
		} catch (MessagingException e) {
			if (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_BROKEN_BODYSTRUCTURE) != -1 && LOG.isWarnEnabled()) {
				LOG.warn(e.getMessage(), e);
			} else {
				throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
			}
		} catch (JSONException e) {
			throw new OXMailException(MailCode.JSON_ERROR, e, e.getMessage());
		}
	}

	public final void writeThreadSortMessageAsArray(final int[] fields, final ThreadSortMessage threadSortMsg)
			throws OXException {
		try {
			try {
				jsonwriter.array();
				for (int i = 0; i < fields.length; i++) {
					writeThreadSortMessageFieldAsJSONValue(fields[i], threadSortMsg, false);
				}
			} finally {
				jsonwriter.endArray();
			}
		} catch (JSONException e) {
			throw new OXMailException(MailCode.JSON_ERROR, e, e.getMessage());
		}
	}

	public final void writeThreadSortMessageFieldAsJSONValue(final int field, final ThreadSortMessage threadSortMsg,
			final boolean withKey) throws OXException {
		writeMessageFieldAsJSONValue(field, threadSortMsg.getMsg(), threadSortMsg.getThreadLevel(), withKey);
	}

	public final void writeMessageAsArray(final int[] fields, final Message msg) throws OXException {
		try {
			try {
				jsonwriter.array();
				for (int i = 0; i < fields.length; i++) {
					writeMessageFieldAsJSONValue(fields[i], msg, false);
				}
			} finally {
				jsonwriter.endArray();
			}
		} catch (JSONException e) {
			throw new OXMailException(MailCode.JSON_ERROR, e, e.getMessage());
		}
	}

	public final MailFieldWriter[] getMailFieldWriters(final int[] fields) {
		MailFieldWriter[] retval = new MailFieldWriter[fields.length];
		for (int i = 0; i < retval.length; i++) {
			Fields: switch (fields[i]) {
			case JSONMessageObject.FIELD_ID:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(DataFields.ID);
						}
						/*
						 * Assume that message's folder is open!
						 */
						if (msg instanceof MessageCacheObject) {
							final MessageCacheObject msgco = (MessageCacheObject) msg;
							final String msgUIDStr = new StringBuilder(msgco.getFolderFullname()).append(
									Mail.SEPERATOR).append(msgco.getUid()).toString();
							jsonwriter.value(msgUIDStr == null ? JSONObject.NULL : msgUIDStr);
						} else {
							final String msgUIDStr = new StringBuilder(msg.getFolder().getFullName()).append(
									Mail.SEPERATOR).append(((IMAPFolder) (msg.getFolder())).getUID(msg)).toString();
							jsonwriter.value(msgUIDStr == null ? JSONObject.NULL : msgUIDStr);
						}
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_FOLDER_ID:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderChildFields.FOLDER_ID);
						}
						final String folder;
						if (msg instanceof MessageCacheObject) {
							final MessageCacheObject msgco = (MessageCacheObject) msg;
							folder = MailFolderObject.prepareFullname(msgco.getFolderFullname(), msgco.getSeparator());
						} else {
							folder = MailFolderObject.prepareFullname(msg.getFolder().getFullName(), msg.getFolder()
									.getSeparator());
						}
						jsonwriter.value(folder);
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_ATTACHMENT:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_HAS_ATTACHMENTS);
						}
						jsonwriter.value(msg.isMimeType(MIME_MULTIPART_MIXED));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_FROM:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_FROM);
						}
						jsonwriter.value(getFromAddr(msg));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_TO:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_RECIPIENT_TO);
						}
						jsonwriter.value(getRecipientAddresses(msg, MimeMessage.RecipientType.TO));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_CC:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_RECIPIENT_CC);
						}
						jsonwriter.value(getRecipientAddresses(msg, MimeMessage.RecipientType.CC));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_BCC:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_RECIPIENT_BCC);
						}
						jsonwriter.value(getRecipientAddresses(msg, MimeMessage.RecipientType.BCC));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_SUBJECT:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_SUBJECT);
						}
						jsonwriter.value(MessageUtils.decodeMultiEncodedHeader(msg.getSubject()));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_SIZE:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_SIZE);
						}
						jsonwriter.value(msg.getSize());
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_SENT_DATE:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_SENT_DATE);
						}
						jsonwriter.value(JSONMessageObject.addUserTimezone(msg.getSentDate().getTime(), userTimeZone));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_RECEIVED_DATE:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_RECEIVED_DATE);
						}
						jsonwriter.value(JSONMessageObject.addUserTimezone(msg.getReceivedDate().getTime(),
								userTimeZone));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_FLAGS:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_FLAGS);
						}
						jsonwriter.value(getFlags(msg));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_THREAD_LEVEL:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_THREAD_LEVEL);
						}
						jsonwriter.value(level);
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_DISPOSITION_NOTIFICATION_TO:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_DISPOSITION_NOTIFICATION_TO);
						}
						jsonwriter.value(getMessageHeader("Disposition-Notification-To", msg));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_PRIORITY:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_PRIORITY);
						}
						final String val = getSingleMessageHeader("X-Priority", msg);
						jsonwriter.value(val == null ? JSONObject.NULL : Integer.valueOf(val.split(" +")[0]));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_MSG_REF:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_MSGREF);
						}
						final String msgref = getSingleMessageHeader("X-Msgref", msg);
						jsonwriter.value(msgref == null ? JSONObject.NULL : Long.valueOf(msgref.split(" +")[0]));
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_COLOR_LABEL:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException, OXException {
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_COLOR_LABEL);
						}
						if (IMAPProperties.isUserFlagsEnabled()) {
							final String[] userFlags = msg.getFlags().getUserFlags();
							for (int a = 0; a < userFlags.length; a++) {
								if (userFlags[a].startsWith(JSONMessageObject.COLOR_LABEL_PREFIX)) {
									jsonwriter.value(JSONMessageObject.getColorLabelIntValue(userFlags[a]));
									return;
								}
							}
						}
						jsonwriter.value(JSONMessageObject.COLOR_LABEL_NONE);
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_TOTAL:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException, OXException {
						if (msg.getFolder() == null) {
							/*
							 * Skip
							 */
							return;
						}
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_TOTAL);
						}
						jsonwriter.value(msg.getFolder().getMessageCount());
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_NEW:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException, OXException {
						if (msg.getFolder() == null) {
							/*
							 * Skip
							 */
							return;
						}
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_NEW);
						}
						jsonwriter.value(msg.getFolder().getNewMessageCount());
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_UNREAD:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException, OXException {
						if (msg.getFolder() == null) {
							/*
							 * Skip
							 */
							return;
						}
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_UNREAD);
						}
						jsonwriter.value(msg.getFolder().getUnreadMessageCount());
					}
				};
				break Fields;
			case JSONMessageObject.FIELD_DELETED:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException, MessagingException, OXException {
						if (msg.getFolder() == null) {
							/*
							 * Skip
							 */
							return;
						}
						if (withKey) {
							jsonwriter.key(JSONMessageObject.JSON_DELETED);
						}
						jsonwriter.value(msg.getFolder().getDeletedMessageCount());
					}
				};
				break Fields;
			default:
				retval[i] = new MailFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final Message msg, final int level,
							final boolean withKey) throws JSONException {
						if (withKey) {
							jsonwriter.key("Unknown column");
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
			}
		}
		return retval;
	}

	public final void writeMessageFieldAsJSONValue(final int field, final Message msg, final boolean withKey)
			throws OXException {
		writeMessageFieldAsJSONValue(field, msg, 0, withKey);
	}

	private final void writeMessageFieldAsJSONValue(final int field, final Message msg, final int threadLevel,
			final boolean withKey) throws OXException {
		try {
			getMailFieldWriters(new int[] { field })[0].writeField(jsonwriter, msg, threadLevel, withKey);
		} catch (JSONException e) {
			throw new OXMailException(MailCode.JSON_ERROR, e, e.getMessage());
		} catch (MessagingException e) {
			throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
		}
	}

	private final String getMessageHeader(final String headerName, final Message msg) throws MessagingException {
		final String[] values = msg.getHeader(headerName);
		if (values == null || values.length == 0) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(values[0]);
		for (int i = 1; i < values.length; i++) {
			sb.append(", ").append(values[i]);
		}
		return sb.toString();
	}

	private final String getSingleMessageHeader(final String headerName, final Message msg) throws MessagingException {
		final String[] values = msg.getHeader(headerName);
		if (values == null || values.length == 0) {
			return null;
		}
		return values[0];
	}
	
	private static final JSONArray EMPTY_JSON_ARR = new JSONArray();

	private final JSONArray getFromAddr(final Message msg) throws MessagingException {
		final Address[] adrs = msg.getFrom();
		if (null == adrs) {
			return EMPTY_JSON_ARR;
		}
		return getAddressesAsArray(Arrays.asList((InternetAddress[]) adrs));
	}

	private final JSONArray getRecipientAddresses(final Message msg, final javax.mail.Message.RecipientType type)
			throws MessagingException {
		final Address[] adrs = msg.getRecipients(type);
		if (null == adrs) {
			return EMPTY_JSON_ARR;
		}
		return getAddressesAsArray(Arrays.asList((InternetAddress[]) adrs));
	}

	private final int getFlags(final Message msg) throws MessagingException {
		int flags = 0;
		if (msg.isSet(Flags.Flag.ANSWERED)) {
			flags |= JSONMessageObject.BIT_ANSWERED;
		}
		if (msg.isSet(Flags.Flag.DELETED)) {
			flags |= JSONMessageObject.BIT_DELETED;
		}
		if (msg.isSet(Flags.Flag.DRAFT)) {
			flags |= JSONMessageObject.BIT_DRAFT;
		}
		if (msg.isSet(Flags.Flag.FLAGGED)) {
			flags |= JSONMessageObject.BIT_FLAGGED;
		}
		if (msg.isSet(Flags.Flag.RECENT)) {
			flags |= JSONMessageObject.BIT_RECENT;
		}
		if (msg.isSet(Flags.Flag.SEEN)) {
			flags |= JSONMessageObject.BIT_SEEN;
		}
		if (msg.isSet(Flags.Flag.USER)) {
			flags |= JSONMessageObject.BIT_USER;
		}
		return flags;
	}

}
