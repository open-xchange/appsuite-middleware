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

package com.openexchange.mail.json.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadFile;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.TransportMailMessage;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.dataobjects.TextBodyMailPart;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.session.Session;

/**
 * {@link MessageParser}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MessageParser {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageParser.class);

	/**
	 * No instantiation
	 */
	private MessageParser() {
		super();
	}

	private static final String STR_TRUE = "true";

	/**
	 * Completely parses given instance of {@link JSONObject} and given instance
	 * of {@link UploadEvent} to a corresponding {@link TransportMailMessage}
	 * object
	 * 
	 * @param jsonObj
	 *            The JSON object
	 * @param uploadEvent
	 *            The upload event containing the uploaded files to attach
	 * @param session
	 *            The session
	 * @return A corresponding instance of {@link TransportMailMessage}
	 * @throws MailException
	 *             If parsing fails
	 */
	public static TransportMailMessage parse(final JSONObject jsonObj, final UploadEvent uploadEvent,
			final Session session) throws MailException {
		try {
			/*
			 * Parse transport message plus its text body
			 */
			final TransportMailMessage transportMail = parse(jsonObj, session);
			{
				/*
				 * Uploaded files
				 */
				final int numOfUploadFiles = uploadEvent.getNumberOfUploadFiles();
				int attachmentCounter = 0;
				int addedAttachments = 0;
				while (addedAttachments < numOfUploadFiles) {
					/*
					 * Get uploaded file by field name: file_0, file_1, ...
					 */
					final UploadFile uf = uploadEvent.getUploadFileByFieldName(getFieldName(attachmentCounter++));
					if (uf != null) {
						transportMail.addEnclosedPart(MailTransport.getNewFilePart(uf));
						addedAttachments++;
					}
				}
			}
			/*
			 * Attached infostore document IDs
			 */
			if (jsonObj.has(MailJSONField.INFOSTORE_IDS.getKey())
					&& !jsonObj.isNull(MailJSONField.INFOSTORE_IDS.getKey())) {
				final JSONArray ja = jsonObj.getJSONArray(MailJSONField.INFOSTORE_IDS.getKey());
				final int length = ja.length();
				for (int i = 0; i < length; i++) {
					transportMail.addEnclosedPart(MailTransport.getNewDocumentPart(ja.getInt(i), session));
				}
			}
			return transportMail;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	private static final String UPLOAD_FILE_ATTACHMENT_PREFIX = "file_";

	private static String getFieldName(final int num) {
		return new StringBuilder(8).append(UPLOAD_FILE_ATTACHMENT_PREFIX).append(num).toString();
	}

	/**
	 * Parses given instance of {@link JSONObject} on send operation and
	 * generates a corresponding instance of {@link TransportMailMessage}
	 * 
	 * @param jsonObj
	 *            The JSON object
	 * @param session
	 *            The session
	 * @return A corresponding instance of {@link TransportMailMessage}
	 * @throws MailException
	 *             If parsing fails
	 */
	public static TransportMailMessage parse(final JSONObject jsonObj, final Session session) throws MailException {
		final TransportMailMessage transportMail = MailTransport.getNewTransportMailMessage();
		parse(jsonObj, transportMail, session);
		return transportMail;
	}

	/**
	 * Parses given instance of {@link JSONObject} to given instance of
	 * {@link MailMessage}
	 * 
	 * @param jsonObj
	 *            The JSON object (source)
	 * @param mail
	 *            The mail(target), which should be empty
	 * @param session
	 *            The session
	 * @throws MailException
	 *             If parsing fails
	 */
	public static void parse(final JSONObject jsonObj, final MailMessage mail, final Session session)
			throws MailException {
		parse(jsonObj, mail, TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(), session.getContext())
				.getTimeZone()));
	}

	/**
	 * Parses given instance of {@link JSONObject} to given instance of
	 * {@link MailMessage}
	 * 
	 * @param jsonObj
	 *            The JSON object (source)
	 * @param mail
	 *            The mail(target), which should be empty
	 * @param timeZone
	 *            The user time zone
	 * @throws MailException
	 *             If parsing fails
	 */
	public static void parse(final JSONObject jsonObj, final MailMessage mail, final TimeZone timeZone)
			throws MailException {
		try {
			/*
			 * System flags
			 */
			if (jsonObj.has(MailJSONField.FLAGS.getKey()) && !jsonObj.isNull(MailJSONField.FLAGS.getKey())) {
				mail.setFlags(jsonObj.getInt(MailJSONField.FLAGS.getKey()));
			}
			/*
			 * Thread level
			 */
			if (jsonObj.has(MailJSONField.THREAD_LEVEL.getKey())
					&& !jsonObj.isNull(MailJSONField.THREAD_LEVEL.getKey())) {
				mail.setThreadLevel(jsonObj.getInt(MailJSONField.THREAD_LEVEL.getKey()));
			}
			/*
			 * User flags
			 */
			if (jsonObj.has(MailJSONField.USER.getKey()) && !jsonObj.isNull(MailJSONField.USER.getKey())) {
				final JSONArray arr = jsonObj.getJSONArray(MailJSONField.USER.getKey());
				final int length = arr.length();
				final List<String> l = new ArrayList<String>(length);
				for (int i = 0; i < length; i++) {
					l.add(arr.getString(i));
				}
				mail.addUserFlags(l.toArray(new String[l.size()]));
			}
			/*
			 * Parse headers
			 */
			if (jsonObj.has(MailJSONField.HEADERS.getKey()) && !jsonObj.isNull(MailJSONField.HEADERS.getKey())) {
				final JSONObject obj = jsonObj.getJSONObject(MailJSONField.HEADERS.getKey());
				final int size = obj.length();
				final Map<String, String> m = new HashMap<String, String>();
				final Iterator<String> iter = obj.keys();
				for (int i = 0; i < size; i++) {
					final String key = iter.next();
					m.put(key, obj.getString(key));
				}
				mail.addHeaders(m);
			}
			/*
			 * From Only mandatory if non-draft message
			 */
			mail.addFrom(parseAddressKey(MailJSONField.FROM.getKey(), jsonObj));
			/*
			 * To Only mandatory if non-draft message
			 */
			mail.addTo(parseAddressKey(MailJSONField.RECIPIENT_TO.getKey(), jsonObj));
			/*
			 * Cc
			 */
			mail.addCc(parseAddressKey(MailJSONField.RECIPIENT_CC.getKey(), jsonObj));
			/*
			 * Bcc
			 */
			mail.addBcc(parseAddressKey(MailJSONField.RECIPIENT_BCC.getKey(), jsonObj));
			/*
			 * Disposition notification
			 */
			if (jsonObj.has(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey())
					&& !jsonObj.isNull(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey())) {
				/*
				 * Ok, disposition-notification-to is set. Check if its value is
				 * a valid email address
				 */
				final String dispVal = jsonObj.getString(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey());
				if (STR_TRUE.equalsIgnoreCase(dispVal)) {
					/*
					 * Boolean value "true"
					 */
					mail.setDispositionNotification(mail.getFrom().length > 0 ? mail.getFrom()[0] : null);
				} else {
					final InternetAddress ia = getEmailAddress(dispVal);
					if (ia == null) {
						/*
						 * Any other value
						 */
						mail.setDispositionNotification(null);
					} else {
						/*
						 * Valid email address
						 */
						mail.setDispositionNotification(ia);
					}
				}
			}
			/*
			 * Priority
			 */
			if (jsonObj.has(MailJSONField.PRIORITY.getKey()) && !jsonObj.isNull(MailJSONField.PRIORITY.getKey())) {
				mail.setPriority(jsonObj.getInt(MailJSONField.PRIORITY.getKey()));
			}
			/*
			 * Color Label
			 */
			if (jsonObj.has(MailJSONField.COLOR_LABEL.getKey()) && !jsonObj.isNull(MailJSONField.COLOR_LABEL.getKey())) {
				mail.setColorLabel(jsonObj.getInt(MailJSONField.COLOR_LABEL.getKey()));
			}
			/*
			 * VCard
			 */
			if (jsonObj.has(MailJSONField.VCARD.getKey()) && !jsonObj.isNull(MailJSONField.VCARD.getKey())) {
				mail.setAppendVCard((jsonObj.getInt(MailJSONField.VCARD.getKey()) > 0));
			}
			/*
			 * Msg Ref
			 */
			if (jsonObj.has(MailJSONField.MSGREF.getKey()) && !jsonObj.isNull(MailJSONField.MSGREF.getKey())) {
				mail.setMsgref(jsonObj.getString(MailJSONField.MSGREF.getKey()));
			}
			/*
			 * Subject, etc.
			 */
			if (jsonObj.has(MailJSONField.SUBJECT.getKey()) && !jsonObj.isNull(MailJSONField.SUBJECT.getKey())) {
				mail.setSubject(jsonObj.getString(MailJSONField.SUBJECT.getKey()));
			}
			/*
			 * Size
			 */
			if (jsonObj.has(MailJSONField.SIZE.getKey())) {
				mail.setSize(jsonObj.getInt(MailJSONField.SIZE.getKey()));
			}
			/*
			 * Sent & received date
			 */
			if (jsonObj.has(MailJSONField.SENT_DATE.getKey()) && !jsonObj.isNull(MailJSONField.SENT_DATE.getKey())) {
				final Date date = new Date(jsonObj.getLong(MailJSONField.SENT_DATE.getKey()));
				final int offset = timeZone.getOffset(date.getTime());
				mail.setSentDate(new Date(jsonObj.getLong(MailJSONField.SENT_DATE.getKey()) - offset));
			}
			if (jsonObj.has(MailJSONField.RECEIVED_DATE.getKey())
					&& !jsonObj.isNull(MailJSONField.RECEIVED_DATE.getKey())) {
				final Date date = new Date(jsonObj.getLong(MailJSONField.RECEIVED_DATE.getKey()));
				final int offset = timeZone.getOffset(date.getTime());
				mail.setReceivedDate(new Date(jsonObj.getLong(MailJSONField.RECEIVED_DATE.getKey()) - offset));
			}
			/*
			 * Parse attachments
			 */
			if (mail instanceof TransportMailMessage) {
				final TransportMailMessage transportMail = (TransportMailMessage) mail;
				if (jsonObj.has(MailJSONField.ATTACHMENTS.getKey())
						&& !jsonObj.isNull(MailJSONField.ATTACHMENTS.getKey())) {
					final JSONArray ja = jsonObj.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
					/*
					 * Parse body text
					 */
					final JSONObject tmp = ja.getJSONObject(0);
					final TextBodyMailPart part = MailTransport.getNewTextBodyPart(tmp.getString(MailJSONField.CONTENT
							.getKey()));
					part.setContentType(parseContentType(tmp.getString(MailJSONField.CONTENT_TYPE.getKey())));
					mail.setContentType(part.getContentType());
					transportMail.setBodyPart(part);
					/*
					 * Parse referenced parts
					 */
					final int len = ja.length();
					if (len > 1 && transportMail.getMsgref() != null) {
						for (int i = 1; i < len; i++) {
							transportMail.addEnclosedPart(MailTransport.getNewReferencedPart(ja.getJSONObject(i)
									.getString(MailListField.ID.getKey())));
						}
					}
				} else {
					final TextBodyMailPart part = MailTransport.getNewTextBodyPart("");
					part.setContentType("text/plain; charset=us-ascii");
					mail.setContentType(part.getContentType());
					transportMail.setBodyPart(part);
				}
			}
			/*
			 * TODO: Parse nested messages. Currently not used by webmail
			 */
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		} catch (final AddressException e) {
			throw MIMEMailException.handleMessagingException(e);
		}
	}

	private static final String CT_ALTERNATIVE = "alternative";

	private static String parseContentType(final String ctStrArg) {
		final String ctStr = ctStrArg.toLowerCase(Locale.ENGLISH).trim();
		if (ctStr.indexOf(CT_ALTERNATIVE) != -1) {
			return MIMETypes.MIME_MULTIPART_ALTERNATIVE;
		} else if (MIMETypes.MIME_TEXT_PLAIN.equals(ctStr)) {
			return MIMETypes.MIME_TEXT_PLAIN;
		}
		return MIMETypes.MIME_TEXT_HTML;
	}

	private static final InternetAddress[] EMPTY_ADDRS = new InternetAddress[0];

	private static InternetAddress[] parseAddressKey(final String key, final JSONObject jo) throws JSONException,
			AddressException {
		String value = null;
		if (!jo.has(key) || jo.isNull(key) || (value = jo.getString(key)).length() == 0) {
			return EMPTY_ADDRS;
		}
		if (value.charAt(0) == '[') {
			/*
			 * Treat as JSON array
			 */
			try {
				final JSONArray jsonArr = new JSONArray(value);
				final int length = jsonArr.length();
				if (length == 0) {
					return EMPTY_ADDRS;
				}
				value = parseAdressArray(jsonArr, length);
			} catch (final JSONException e) {
				LOG.error(e.getLocalizedMessage(), e);
				/*
				 * Reset
				 */
				value = jo.getString(key);
			}
		}
		return MessageUtility.parseAddressList(value, false);
	}

	/**
	 * Expects the specified JSON array to be an array of arrays. Each inner
	 * array conforms to pattern:
	 * 
	 * <pre>
	 * [&quot;&lt;personal&gt;&quot;, &quot;&lt;email-address&gt;&quot;]
	 * </pre>
	 * 
	 * @param jsonArray
	 *            The JSON array
	 * @return Parsed address list combined in a {@link String} object
	 * @throws JSONException
	 *             If a JSON error occurs
	 */
	private static String parseAdressArray(final JSONArray jsonArray, final int length) throws JSONException {
		final StringBuilder sb = new StringBuilder(length * 64);
		{
			/*
			 * Add first address
			 */
			final JSONArray persAndAddr = jsonArray.getJSONArray(0);
			final String personal = persAndAddr.getString(0);
			final boolean hasPersonal = (personal != null && !"null".equals(personal));
			if (hasPersonal) {
				sb.append(MessageUtility.quotePersonal(personal)).append(" <");
			}
			sb.append(persAndAddr.getString(1));
			if (hasPersonal) {
				sb.append('>');
			}
		}
		for (int i = 1; i < length; i++) {
			sb.append(", ");
			final JSONArray persAndAddr = jsonArray.getJSONArray(i);
			final String personal = persAndAddr.getString(0);
			final boolean hasPersonal = (personal != null && !"null".equals(personal));
			if (hasPersonal) {
				sb.append(MessageUtility.quotePersonal(personal)).append(" <");
			}
			sb.append(persAndAddr.getString(1));
			if (hasPersonal) {
				sb.append('>');
			}
		}
		return sb.toString();
	}

	private static InternetAddress getEmailAddress(final String addrStr) {
		if (addrStr == null || addrStr.length() == 0) {
			return null;
		}
		try {
			return InternetAddress.parse(addrStr, true)[0];
		} catch (final AddressException e) {
			return null;
		}
	}
}
