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

package com.openexchange.mail.json.writer;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;

import java.io.UnsupportedEncodingException;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.JSONMessageHandler;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.session.Session;

/**
 * {@link MessageWriter} - Writes {@link MailMessage} instances as JSON strings
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MessageWriter {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageWriter.class);

	/**
	 * No instantiation
	 */
	private MessageWriter() {
		super();
	}

	/**
	 * Writes whole mail as a JSON object
	 * 
	 * @param mail
	 *            The mail to write
	 * @param displayVersion
	 *            <code>true</code> to create a version for displaying
	 *            purpose; otherwise <code>false</code>
	 * @param session
	 *            The session
	 * @return The written JSON object
	 * @throws MailException
	 */
	public static JSONObject writeMailMessage(final MailMessage mail, final boolean displayVersion,
			final Session session) throws MailException {
		final MailPath mailPath;
		if (mail.getFolder() != null && mail.getMailId() != 0) {
			mailPath = new MailPath(mail.getFolder(), mail.getMailId());
		} else if (mail.getMsgref() != null) {
			mailPath = new MailPath(mail.getMsgref());
		} else {
			mailPath = MailPath.NULL;
		}
		final JSONMessageHandler handler = new JSONMessageHandler(mailPath, displayVersion, session);
		new MailMessageParser().parseMailMessage(mail, handler);
		final JSONObject jObject = handler.getJSONObject();
		try {
			/*
			 * Add missing fields
			 */
			if (mail.containsFolder() && mail.getMailId() > 0) {
				jObject.put(FolderChildFields.FOLDER_ID, prepareFullname(mail.getFolder(), mail.getSeparator()));
				jObject.put(DataFields.ID, mail.getMailId());
			}
			jObject.put(MailJSONField.UNREAD.getKey(), mail.getUnreadMessages());
			jObject.put(MailJSONField.HAS_ATTACHMENTS.getKey(), mail.getContentType().isMimeType(
					MIMETypes.MIME_MULTIPART_MIXED));
			jObject.put(MailJSONField.CONTENT_TYPE.getKey(), mail.getContentType().getBaseType());
			jObject.put(MailJSONField.SIZE.getKey(), mail.getSize());
			jObject.put(MailJSONField.THREAD_LEVEL.getKey(), mail.getThreadLevel());
		} catch (final JSONException e) {
			LOG.error(e.getMessage(), e);
		}
		return jObject;
	}

	public static interface MailFieldWriter {
		public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey) throws MailException;
	}

	/**
	 * Generates appropriate field writers for given mail fields
	 * 
	 * @param fields
	 *            The mail fields to write
	 * @param session
	 *            The session
	 * @return Appropriate field writers as an array of {@link MailFieldWriter}
	 */
	public static MailFieldWriter[] getMailFieldWriter(final MailListField[] fields, final Session session) {
		final MailFieldWriter[] retval = new MailFieldWriter[fields.length];
		for (int i = 0; i < fields.length; i++) {
			Fields: switch (fields[i]) {
			case ID:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(DataFields.ID, mail.getMailId());
							} else {
								((JSONArray) jsonContainer).put(mail.getMailId());
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case FOLDER_ID:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(FolderChildFields.FOLDER_ID, prepareFullname(mail
										.getFolder(), mail.getSeparator()));
							} else {
								((JSONArray) jsonContainer).put(prepareFullname(mail.getFolder(), mail.getSeparator()));
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case ATTACHMENT:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.HAS_ATTACHMENTS.getKey(), mail
										.hasAttachment());
							} else {
								((JSONArray) jsonContainer).put(mail.hasAttachment());
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case FROM:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.FROM.getKey(), getAddressesAsArray(mail
										.getFrom()));
							} else {
								((JSONArray) jsonContainer).put(getAddressesAsArray(mail.getFrom()));
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case TO:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.RECIPIENT_TO.getKey(),
										getAddressesAsArray(mail.getTo()));
							} else {
								((JSONArray) jsonContainer).put(getAddressesAsArray(mail.getTo()));
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case CC:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.RECIPIENT_CC.getKey(),
										getAddressesAsArray(mail.getCc()));
							} else {
								((JSONArray) jsonContainer).put(getAddressesAsArray(mail.getCc()));
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case BCC:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.RECIPIENT_BCC.getKey(),
										getAddressesAsArray(mail.getBcc()));
							} else {
								((JSONArray) jsonContainer).put(getAddressesAsArray(mail.getBcc()));
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case SUBJECT:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.SUBJECT.getKey(), MessageUtility
										.decodeMultiEncodedHeader(mail.getSubject()));
							} else {
								((JSONArray) jsonContainer).put(MessageUtility.decodeMultiEncodedHeader(mail
										.getSubject()));
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case SIZE:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.SIZE.getKey(), mail.getSize());
							} else {
								((JSONArray) jsonContainer).put(mail.getSize());
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case SENT_DATE:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								if (mail.containsSentDate() && mail.getSentDate() != null) {
									((JSONObject) jsonContainer).put(MailJSONField.SENT_DATE.getKey(), addUserTimezone(
											mail.getSentDate().getTime(), TimeZone.getTimeZone(UserStorage
													.getStorageUser(session.getUserId(),
															ContextStorage.getStorageContext(session.getContextId()))
													.getTimeZone())));
								} else {
									((JSONObject) jsonContainer).put(MailJSONField.SENT_DATE.getKey(), JSONObject.NULL);
								}
							} else {
								if (mail.containsSentDate() && mail.getSentDate() != null) {
									((JSONArray) jsonContainer).put(addUserTimezone(mail.getSentDate().getTime(),
											TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(),
													ContextStorage.getStorageContext(session.getContextId()))
													.getTimeZone())));
								} else {
									((JSONArray) jsonContainer).put(JSONObject.NULL);
								}
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						} catch (final ContextException e) {
							throw new MailException(e);
						}
					}
				};
				break Fields;
			case RECEIVED_DATE:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								if (mail.containsReceivedDate() && mail.getReceivedDate() != null) {
									((JSONObject) jsonContainer).put(MailJSONField.RECEIVED_DATE.getKey(),
											addUserTimezone(mail.getReceivedDate().getTime(), TimeZone
													.getTimeZone(UserStorage.getStorageUser(session.getUserId(),
															ContextStorage.getStorageContext(session.getContextId()))
															.getTimeZone())));
								} else {
									((JSONObject) jsonContainer).put(MailJSONField.RECEIVED_DATE.getKey(),
											JSONObject.NULL);
								}
							} else {
								if (mail.containsReceivedDate() && mail.getReceivedDate() != null) {
									((JSONArray) jsonContainer).put(addUserTimezone(mail.getReceivedDate().getTime(),
											TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(),
													ContextStorage.getStorageContext(session.getContextId()))
													.getTimeZone())));
								} else {
									((JSONArray) jsonContainer).put(JSONObject.NULL);
								}
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						} catch (final ContextException e) {
							throw new MailException(e);
						}
					}
				};
				break Fields;
			case FLAGS:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.FLAGS.getKey(), mail.getFlags());
							} else {
								((JSONArray) jsonContainer).put(mail.getFlags());
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case THREAD_LEVEL:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.THREAD_LEVEL.getKey(), mail
										.getThreadLevel());
							} else {
								((JSONArray) jsonContainer).put(mail.getThreadLevel());
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case DISPOSITION_NOTIFICATION_TO:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							final Object value;
							if ((mail.containsPrevSeen() ? mail.isPrevSeen() : mail.isSeen())) {
								value = mail.getDispositionNotification() == null ? JSONObject.NULL : mail
										.getDispositionNotification().toUnicodeString();
							} else {
								value = JSONObject.NULL;
							}
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(),
										value);
							} else {
								((JSONArray) jsonContainer).put(value);
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case PRIORITY:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.PRIORITY.getKey(), mail.getPriority());
							} else {
								((JSONArray) jsonContainer).put(mail.getPriority());
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case MSG_REF:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.MSGREF.getKey(),
										mail.containsMsgref() ? mail.getMsgref() : JSONObject.NULL);
							} else {
								((JSONArray) jsonContainer).put(mail.containsMsgref() ? mail.getMsgref()
										: JSONObject.NULL);
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case COLOR_LABEL:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							final int colorLabel;
							if (MailConfig.isUserFlagsEnabled() && mail.containsColorLabel()) {
								colorLabel = mail.getColorLabel();
							} else {
								colorLabel = 0;
							}
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.COLOR_LABEL.getKey(), colorLabel);
							} else {
								((JSONArray) jsonContainer).put(colorLabel);
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case TOTAL:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							/*
							 * TODO: Total, New, Unread, and Deleted count
							 */
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.TOTAL.getKey(), JSONObject.NULL);
							} else {
								((JSONArray) jsonContainer).put(JSONObject.NULL);
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case NEW:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							/*
							 * TODO: Total, New, Unread, and Deleted count
							 */
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.NEW.getKey(), JSONObject.NULL);
							} else {
								((JSONArray) jsonContainer).put(JSONObject.NULL);
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case UNREAD:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							/*
							 * TODO: Total, New, Unread, and Deleted count
							 */
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.UNREAD.getKey(), mail
										.getUnreadMessages());
							} else {
								((JSONArray) jsonContainer).put(mail.getUnreadMessages());
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			case DELETED:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							/*
							 * TODO: Total, New, Unread, and Deleted count
							 */
							if (withKey) {
								((JSONObject) jsonContainer).put(MailJSONField.DELETED.getKey(), JSONObject.NULL);
							} else {
								((JSONArray) jsonContainer).put(JSONObject.NULL);
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break Fields;
			default:
				retval[i] = new MailFieldWriter() {
					public void writeField(Object jsonContainer, MailMessage mail, int level, boolean withKey)
							throws MailException {
						try {
							/*
							 * TODO: Total, New, Unread, and Deleted count
							 */
							if (withKey) {
								((JSONObject) jsonContainer).put("Unknown column", JSONObject.NULL);
							} else {
								((JSONArray) jsonContainer).put(JSONObject.NULL);
							}
						} catch (final JSONException e) {
							throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
			}
		}
		return retval;
	}

	/**
	 * Adds the user time zone offset to given date time
	 * 
	 * @param time
	 *            The date time
	 * @param timeZone
	 *            The time zone
	 * @return The time with added time zone offset
	 */
	public static long addUserTimezone(final long time, final TimeZone timeZone) {
		return (time + timeZone.getOffset(time));
	}

	private static final JSONArray EMPTY_JSON_ARR = new JSONArray();

	/**
	 * Convert an array of <code>InternetAddress</code> instances into a
	 * JSON-Array conforming to:
	 * 
	 * <pre>
	 * [[&quot;The Personal&quot;, &quot;someone@somewhere.com&quot;], ...]
	 * </pre>
	 */
	public static JSONArray getAddressesAsArray(final InternetAddress[] addrs) {
		if (addrs == null || addrs.length == 0) {
			return EMPTY_JSON_ARR;
		}
		final JSONArray jsonArr = new JSONArray();
		for (InternetAddress address : addrs) {
			jsonArr.put(getAddressAsArray(address));
		}
		return jsonArr;
	}

	/**
	 * Convert an <code>InternetAddress</code> instance into a JSON-Array
	 * comforming to: ["The Personal", "someone@somewhere.com"]
	 */
	private static JSONArray getAddressAsArray(final InternetAddress addr) {
		final JSONArray retval = new JSONArray();
		retval.put(addr.getPersonal() == null || addr.getPersonal().length() == 0 ? JSONObject.NULL
				: preparePersonal(addr.getPersonal()));
		retval.put(addr.getAddress() == null || addr.getAddress().length() == 0 ? JSONObject.NULL : prepareAddress(addr
				.getAddress()));
		return retval;
	}

	private static final Pattern PATTERN_QUOTE = Pattern.compile("[.,:;<>\"]");

	private static String preparePersonal(final String personal) {
		if (PATTERN_QUOTE.matcher(personal).find()) {
			/*
			 * Surround with double-quotes
			 */
			final String pp = MessageUtility.decodeMultiEncodedHeader(personal);
			return new StringBuilder(pp.length()).append('"').append(pp.replaceAll("\"", "\\\\\\\"")).append('"')
					.toString();
		}
		return MessageUtility.decodeMultiEncodedHeader(personal);
	}

	private static final String DUMMY_DOMAIN = "@unspecified-domain";

	private static String prepareAddress(final String address) {
		try {
			final String decoded = MimeUtility.decodeText(address);
			if (decoded.endsWith(DUMMY_DOMAIN)) {
				return decoded.substring(0, decoded.indexOf('@'));
			}
			return decoded;
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Unsupported encoding in a message detected and monitored.", e);
			mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			return MessageUtility.decodeMultiEncodedHeader(address);
		}
	}

}
