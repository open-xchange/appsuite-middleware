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

package com.openexchange.mail.parser.handlers;

import static com.openexchange.mail.parser.MailMessageParser.generateFilename;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.Enriched2HtmlConverter;
import com.openexchange.tools.mail.Html2TextConverter;
import com.openexchange.tools.mail.UUEncodedPart;

/**
 * {@link JSONMessageHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class JSONMessageHandler implements MailMessageHandler {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(JSONMessageHandler.class);

	/*
	 * Parameter constants
	 */
	private static final String PARAM_CHARSET = "charset";

	private final Session session;

	private TimeZone timeZone;

	private final UserSettingMail usm;

	private final boolean displayVersion;

	private final String mailPath;

	private final JSONObject jsonObject;

	private Html2TextConverter converter;

	private JSONArray attachmentsArr;

	private JSONArray nestedMsgsArr;

	private boolean seen;

	private boolean isAlternative;

	private String altId;

	private boolean textAppended;

	/**
	 * Constructor
	 * 
	 * @param mailPath
	 *            The unique mail path
	 * @param displayVersion
	 *            <code>true</code> to create a version gfor display;
	 *            otherwise <code>false</code>
	 * @param session
	 *            The session
	 */
	public JSONMessageHandler(final String mailPath, final boolean displayVersion, final Session session) {
		super();
		this.session = session;
		usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContext());
		this.displayVersion = displayVersion;
		this.mailPath = mailPath;
		jsonObject = new JSONObject();
	}

	/**
	 * Constructor
	 * 
	 * @param mailPath
	 *            The unique mail path
	 * @param displayVersion
	 *            <code>true</code> to create a version gfor display;
	 *            otherwise <code>false</code>
	 * @param session
	 *            The session
	 */
	public JSONMessageHandler(final MailPath mailPath, final boolean displayVersion, final Session session) {
		super();
		this.session = session;
		usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContext());
		this.displayVersion = displayVersion;
		this.mailPath = mailPath == MailPath.NULL ? "" : mailPath.toString();
		jsonObject = new JSONObject();
	}

	private JSONArray getAttachmentsArr() throws JSONException {
		if (attachmentsArr == null) {
			attachmentsArr = new JSONArray();
			jsonObject.put(MailJSONField.ATTACHMENTS.getKey(), attachmentsArr);
		}
		return attachmentsArr;
	}

	private JSONArray getNestedMsgsArr() throws JSONException {
		if (nestedMsgsArr == null) {
			nestedMsgsArr = new JSONArray();
			jsonObject.put(MailJSONField.NESTED_MESSAGES.getKey(), nestedMsgsArr);
		}
		return nestedMsgsArr;
	}

	private Html2TextConverter getConverter() {
		if (converter == null) {
			converter = new Html2TextConverter();
		}
		return converter;
	}

	private TimeZone getTimeZone() {
		if (timeZone == null) {
			timeZone = TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(), session.getContext())
					.getTimeZone());
		}
		return timeZone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleAttachment(com.openexchange.mail.dataobjects.MailPart,
	 *      boolean, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleAttachment(final MailPart part, final boolean isInline, final String baseContentType,
			final String fileName, final String id) throws MailException {
		if (Part.INLINE.equalsIgnoreCase(part.getDisposition()) && part.getFileName() == null
				&& MIMETypes.MIME_PGP_SIGN.equalsIgnoreCase(baseContentType)) {
			/*
			 * Ignore inline PGP signatures
			 */
			return true;
		}
		try {
			final JSONObject jsonObject = new JSONObject();
			/*
			 * Sequence ID
			 */
			jsonObject.put(MailListField.ID.getKey(), part.containsSequenceId() ? part.getSequenceId() : id);
			/*
			 * Filename
			 */
			if (part.containsFileName() && part.getFileName() != null) {
				try {
					jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), MimeUtility.decodeText(part
							.getFileName()));
				} catch (final UnsupportedEncodingException e) {
					jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), part.getFileName());
				}
			} else {
				final Object val;
				if (isInline) {
					val = JSONObject.NULL;
				} else {
					val = generateFilename(id, baseContentType);
					part.setFileName((String) val);
				}
				jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), val);
			}
			/*
			 * Size
			 */
			if (part.containsSize()) {
				jsonObject.put(MailJSONField.SIZE.getKey(), part.getSize());
			}
			/*
			 * Disposition
			 */
			jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
			/*
			 * Content-ID
			 */
			if (part.containsContentId() && part.getContentId() != null) {
				jsonObject.put(MailJSONField.CID.getKey(), part.getContentId());
			}
			/*
			 * Content-Type
			 */
			jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), part.getContentType().toString());
			/*
			 * Content
			 */
			if (isInline && part.getContentType().isMimeType(MIMETypes.MIME_TEXT_ALL)) {
				// TODO: Add rtf2html conversion here!
				if (part.getContentType().isMimeType(MIMETypes.MIME_TEXT_RTF)) {
					jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
				} else {
					final String charset = part.getContentType().containsParameter(PARAM_CHARSET) ? part
							.getContentType().getParameter(PARAM_CHARSET) : MailConfig.getDefaultMimeCharset();
					jsonObject.put(MailJSONField.CONTENT.getKey(), MessageUtility.formatContentForDisplay(
							MessageUtility.readMailPart(part, charset), part.getContentType().isMimeType(
									MIMETypes.MIME_TEXT_HTM_ALL), session, mailPath, displayVersion));
				}
			} else {
				jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
			}
			getAttachmentsArr().put(jsonObject);
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleBccRecipient(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleBccRecipient(final InternetAddress[] recipientAddrs) throws MailException {
		try {
			jsonObject.put(MailJSONField.RECIPIENT_BCC.getKey(), MessageWriter.getAddressesAsArray(recipientAddrs));
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleCcRecipient(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleCcRecipient(final InternetAddress[] recipientAddrs) throws MailException {
		try {
			jsonObject.put(MailJSONField.RECIPIENT_CC.getKey(), MessageWriter.getAddressesAsArray(recipientAddrs));
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleColorLabel(int)
	 */
	public boolean handleColorLabel(final int colorLabel) throws MailException {
		try {
			jsonObject.put(MailJSONField.COLOR_LABEL.getKey(), colorLabel);
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleContentId(java.lang.String)
	 */
	public boolean handleContentId(final String contentId) throws MailException {
		try {
			jsonObject.put(MailJSONField.CID.getKey(), contentId);
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleFrom(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleFrom(final InternetAddress[] fromAddrs) throws MailException {
		try {
			jsonObject.put(MailJSONField.FROM.getKey(), MessageWriter.getAddressesAsArray(fromAddrs));
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleHeaders(int,
	 *      java.util.Iterator)
	 */
	public boolean handleHeaders(final int size, final Iterator<Entry<String, String>> iter) throws MailException {
		if (size == 0) {
			return true;
		}
		try {
			final JSONObject hdrObject = new JSONObject();
			for (int i = 0; i < size; i++) {
				final Map.Entry<String, String> entry = iter.next();
				if (MessageHeaders.HDR_DISP_NOT_TO.equalsIgnoreCase(entry.getKey())) {
					if (!seen) {
						/*
						 * Disposition-Notification: Indicate an expected read
						 * ack if this header is available and if mail has not
						 * been seen, yet
						 */
						try {
							jsonObject.put(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(), new InternetAddress(
									entry.getValue()).toUnicodeString());
						} catch (final AddressException e) {
							LOG.error(e.getLocalizedMessage(), e);
						}
					}
				} else if (MessageHeaders.HDR_X_PRIORITY.equalsIgnoreCase(entry.getKey())) {
					/*
					 * Priority
					 */
					int priority = MailMessage.PRIORITY_NORMAL;
					if (null != entry.getValue()) {
						final String[] tmp = entry.getValue().split(" +");
						try {
							priority = Integer.parseInt(tmp[0]);
						} catch (final NumberFormatException nfe) {
							if (LOG.isWarnEnabled()) {
								LOG.warn("Strange X-Priority header: " + tmp[0], nfe);
							}
							priority = MailMessage.PRIORITY_NORMAL;
						}
					}
					jsonObject.put(MailJSONField.PRIORITY.getKey(), priority);
				} else if (MessageHeaders.HDR_X_MAILER.equalsIgnoreCase(entry.getKey())) {
					hdrObject.put(entry.getKey(), entry.getValue());
				} else {
					continue;
					// msgObj.addHeader(entry.getKey(), hdr.getValue());
				}
			}
			jsonObject.put(MailJSONField.HEADERS.getKey(), hdrObject);
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleImagePart(com.openexchange.mail.dataobjects.MailPart,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleImagePart(final MailPart part, final String imageCID, final String baseContentType,
			final String id) throws MailException {
		return handleAttachment(part, false, baseContentType, null, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineHtml(java.lang.String,
	 *      com.openexchange.tools.mail.ContentType, long, java.lang.String,
	 *      java.lang.String)
	 */
	public boolean handleInlineHtml(final String htmlContent, final ContentType contentType, final long size,
			final String fileName, final String id) throws MailException {

		if (textAppended) {
			/*
			 * A text part has already been detected as message's body
			 */
			if (isAlternative) {
				if (displayVersion) {
					/*
					 * Add html alternative part as attachment
					 */
					asAttachment(id, contentType.getBaseType(), htmlContent.length(), fileName);
				} else {
					/*
					 * Discard
					 */
					return true;
				}
			} else {
				/*
				 * Add html part as attachment
				 */
				asAttachment(id, contentType.getBaseType(), htmlContent.length(), fileName);
			}
		} else {
			/*
			 * No text part was present before
			 */
			if (usm.isDisplayHtmlInlineContent()) {
				asHtml(id, contentType.getBaseType(), htmlContent);
			} else {
				asText(id, contentType.getBaseType(), htmlContent, fileName);
			}
		}
		return true;

	}

	private static final Enriched2HtmlConverter ENRCONV = new Enriched2HtmlConverter();

	// private static final RTF2HtmlConverter RTFCONV = new RTF2HtmlConverter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleInlinePlainText(java.lang.String,
	 *      com.openexchange.tools.mail.ContentType, long, java.lang.String,
	 *      java.lang.String)
	 */
	public boolean handleInlinePlainText(final String plainTextContentArg, final ContentType contentType,
			final long size, final String fileName, final String id) throws MailException {
		if (isAlternative && usm.isDisplayHtmlInlineContent()) {
			/*
			 * User wants to see message's alternative content
			 */
			/* textAppended = true; */
			return true;
		}
		try {
			final JSONObject jsonObject = new JSONObject();
			jsonObject.put(MailListField.ID.getKey(), id);
			if (contentType.isMimeType(MIMETypes.MIME_TEXT_ENRICHED)
					|| contentType.isMimeType(MIMETypes.MIME_TEXT_RICHTEXT)
					|| contentType.isMimeType(MIMETypes.MIME_TEXT_RTF)) {
				if (textAppended) {
					if (displayVersion) {
						/*
						 * Add alternative part as attachment
						 */
						asAttachment(id, contentType.getBaseType(), getHtmlVersion(contentType, plainTextContentArg)
								.length(), fileName);
						return true;
					}
					/*
					 * Discard
					 */
					return true;
				}
				/*
				 * No text part was present before
				 */
				if (usm.isDisplayHtmlInlineContent()) {
					asHtml(id, contentType.getBaseType(), getHtmlVersion(contentType, plainTextContentArg));
					return true;
				}
				asText(id, contentType.getBaseType(), getHtmlVersion(contentType, plainTextContentArg), fileName);
				return true;
			}
			/*
			 * Just common plain text
			 */
			final String content = MessageUtility.formatContentForDisplay(plainTextContentArg, false, session,
					mailPath, displayVersion);
			jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
			jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), contentType.getBaseType());
			jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
			jsonObject.put(MailJSONField.CONTENT.getKey(), content);
			getAttachmentsArr().put(jsonObject);
			textAppended = true;
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	private String getHtmlVersion(final ContentType contentType, final String src) {
		if (contentType.isMimeType(MIMETypes.MIME_TEXT_ENRICHED)
				|| contentType.isMimeType(MIMETypes.MIME_TEXT_RICHTEXT)) {
			return MessageUtility
					.formatContentForDisplay(ENRCONV.convert(src), true, session, mailPath, displayVersion);
		} else if (contentType.isMimeType(MIMETypes.MIME_TEXT_RTF)) {
			// TODO: return
			// MessageUtils.formatContentForDisplay(RTFCONV.convert2HTML(src),
			// true, session, mailPath,
			// displayVersion);
			return MessageUtility.formatContentForDisplay(src, false, session, mailPath, displayVersion);
		}
		return MessageUtility.formatContentForDisplay(src, false, session, mailPath, displayVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineUUEncodedAttachment(com.openexchange.tools.mail.UUEncodedPart,
	 *      java.lang.String)
	 */
	public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws MailException {
		try {
			final JSONObject jsonObject = new JSONObject();
			jsonObject.put(MailListField.ID.getKey(), id);
			String contentType = MIMETypes.MIME_APPL_OCTET;
			final String filename = part.getFileName();
			try {
				final Locale locale = UserStorage.getStorageUser(session.getUserId(), session.getContext()).getLocale();
				contentType = MIMEType2ExtMap.getContentType(new File(filename.toLowerCase(locale)).getName())
						.toLowerCase(locale);
			} catch (final Exception e) {
				final Throwable t = new Throwable(new StringBuilder("Unable to fetch content/type for '").append(
						filename).append("': ").append(e).toString());
				LOG.warn(t.getMessage(), t);
			}
			jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), contentType);
			jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), filename);
			jsonObject.put(MailJSONField.SIZE.getKey(), part.getFileSize());
			jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
			/*
			 * Content-type indicates mime type text/*
			 */
			if (contentType.startsWith("text/")) {
				/*
				 * Attach link-object with text content
				 */
				jsonObject.put(MailJSONField.CONTENT.getKey(), part.getPart().toString());
			} else {
				/*
				 * Attach link-object.
				 */
				jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
			}
			getAttachmentsArr().put(jsonObject);
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineUUEncodedPlainText(java.lang.String,
	 *      com.openexchange.tools.mail.ContentType, int, java.lang.String,
	 *      java.lang.String)
	 */
	public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType,
			final int size, final String fileName, final String id) throws MailException {
		return handleInlinePlainText(decodedTextContent, contentType, size, fileName, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleMessageEnd(com.openexchange.mail.dataobjects.MailMessage)
	 */
	public void handleMessageEnd(final MailMessage mail) throws MailException {
		/*
		 * Since we obviously touched message's content, mark its corresponding
		 * message object as seen
		 */
		mail.setFlags(mail.getFlags() | MailMessage.FLAG_SEEN);
		// if (displayVersion && mail.getFolder() != null) {
		// /*
		// * TODO: Try to fill folder information into message object
		// */
		// final Folder fld = msg.getFolder();
		// try {
		// msgObj.setTotal(fld.getMessageCount());
		// msgObj.setNew(fld.getNewMessageCount());
		// msgObj.setUnread(fld.getUnreadMessageCount());
		// msgObj.setDeleted(fld.getDeletedMessageCount());
		// } catch (final MessagingException e) {
		// LOG.error(e.getMessage(), e);
		// }
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleMultipart(com.openexchange.mail.dataobjects.MailPart,
	 *      int, java.lang.String)
	 */
	public boolean handleMultipart(final MailPart mp, final int bodyPartCount, final String id) throws MailException {
		/*
		 * Determine if message is of MIME type multipart/alternative
		 */
		if (mp.getContentType().isMimeType(MIMETypes.MIME_MULTIPART_ALTERNATIVE) && bodyPartCount >= 2) {
			isAlternative = true;
			altId = id;
		} else if (null != altId && !id.startsWith(altId)) {
			/*
			 * No more within multipart/alternative since current ID is not
			 * nested below remembered ID
			 */
			isAlternative = false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleNestedMessage(com.openexchange.mail.dataobjects.MailMessage,
	 *      java.lang.String)
	 */
	public boolean handleNestedMessage(final MailMessage nestedMsg, final String id) throws MailException {
		try {
			final JSONMessageHandler msgHandler = new JSONMessageHandler(mailPath, displayVersion, session);
			new MailMessageParser().parseMailMessage(nestedMsg, msgHandler, id);
			getNestedMsgsArr().put(msgHandler.getJSONObject());
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handlePriority(int)
	 */
	public boolean handlePriority(final int priority) throws MailException {
		try {
			jsonObject.put(MailJSONField.PRIORITY.getKey(), priority);
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleMsgRef(java.lang.String)
	 */
	public boolean handleMsgRef(final String msgRef) throws MailException {
		try {
			jsonObject.put(MailJSONField.MSGREF.getKey(), msgRef);
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleDispositionNotification(javax.mail.internet.InternetAddress)
	 */
	public boolean handleDispositionNotification(final InternetAddress dispositionNotificationTo, final boolean seen)
			throws MailException {
		try {
			if (!seen) {
				jsonObject.put(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(), dispositionNotificationTo
						.toUnicodeString());
			}
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleReceivedDate(java.util.Date)
	 */
	public boolean handleReceivedDate(final Date receivedDate) throws MailException {
		try {
			jsonObject.put(MailJSONField.SENT_DATE.getKey(), receivedDate == null ? JSONObject.NULL : Long
					.valueOf(MessageWriter.addUserTimezone(receivedDate.getTime(), getTimeZone())));
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleSentDate(java.util.Date)
	 */
	public boolean handleSentDate(final Date sentDate) throws MailException {
		try {
			jsonObject.put(MailJSONField.RECEIVED_DATE.getKey(), sentDate == null ? JSONObject.NULL : Long
					.valueOf(MessageWriter.addUserTimezone(sentDate.getTime(), getTimeZone())));
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleSpecialPart(com.openexchange.mail.dataobjects.MailPart,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean handleSpecialPart(final MailPart part, final String baseContentType, final String id)
			throws MailException {
		/*
		 * When creating a JSON message object from a message we do not
		 * distinguish special parts or image parts from "usual" attachments.
		 * Therefore invoke the handleAttachment method. Maybe we need a
		 * seperate handling in the future for vcards.
		 */
		return handleAttachment(part, false, baseContentType, null, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleSubject(java.lang.String)
	 */
	public boolean handleSubject(final String subject) throws MailException {
		try {
			jsonObject.put(MailJSONField.SUBJECT.getKey(), subject == null ? JSONObject.NULL : subject);
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleSystemFlags(int)
	 */
	public boolean handleSystemFlags(final int flags) throws MailException {
		try {
			jsonObject.put(MailJSONField.FLAGS.getKey(), flags);
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleToRecipient(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleToRecipient(final InternetAddress[] recipientAddrs) throws MailException {
		try {
			jsonObject.put(MailJSONField.RECIPIENT_TO.getKey(), MessageWriter.getAddressesAsArray(recipientAddrs));
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleUserFlags(java.lang.String[])
	 */
	public boolean handleUserFlags(final String[] userFlags) throws MailException {
		if (userFlags == null) {
			return true;
		}
		try {
			final JSONArray userFlagsArr = new JSONArray();
			for (int i = 0; i < userFlags.length; i++) {
				/*
				 * Color Label
				 */
				if (userFlags[i].startsWith(MailMessage.COLOR_LABEL_PREFIX)) {
					jsonObject.put(MailJSONField.COLOR_LABEL.getKey(), MailMessage.getColorLabelIntValue(userFlags[i]));
				} else {
					userFlagsArr.put(userFlags[i]);
				}
			}
			jsonObject.put(MailJSONField.USER.getKey(), userFlagsArr);
			return true;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	/**
	 * Gets the filled instance of {@link JSONObject}
	 * 
	 * @return The filled instance of {@link JSONObject}
	 */
	public JSONObject getJSONObject() {
		return jsonObject;
	}

	private void asAttachment(final String id, final String baseContentType, final int len, final String fileName)
			throws MailException {
		try {
			final JSONObject jsonObject = new JSONObject();
			jsonObject.put(MailListField.ID.getKey(), id);
			jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
			jsonObject.put(MailJSONField.SIZE.getKey(), len);
			jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
			jsonObject.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
			if (fileName != null) {
				try {
					jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), MimeUtility.decodeText(fileName));
				} catch (final UnsupportedEncodingException e) {
					jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), fileName);
				}
			} else {
				jsonObject.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), JSONObject.NULL);
			}
			getAttachmentsArr().put(jsonObject);

		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	private void asHtml(final String id, final String baseContentType, final String htmlContent) throws MailException {
		try {
			final JSONObject jsonObject = new JSONObject();
			jsonObject.put(MailListField.ID.getKey(), id);
			final String content = MessageUtility.formatContentForDisplay(htmlContent, true, session, mailPath,
					displayVersion);
			jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
			jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
			jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
			jsonObject.put(MailJSONField.CONTENT.getKey(), content);
			getAttachmentsArr().put(jsonObject);
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		}
	}

	private void asText(final String id, final String baseContentType, final String htmlContent, final String fileName)
			throws MailException {
		try {
			final JSONObject jsonObject = new JSONObject();
			jsonObject.put(MailListField.ID.getKey(), id);
			jsonObject.put(MailJSONField.CONTENT_TYPE.getKey(), MIMETypes.MIME_TEXT_PLAIN);
			/*
			 * Try to convert the given html to regular text
			 */
			final String content;
			if (displayVersion && usm.isUseColorQuote()) {
				content = MessageUtility.formatHrefLinks(MessageUtility
						.replaceHTMLSimpleQuotesForDisplay(MessageUtility.convertAndKeepQuotes(htmlContent,
								getConverter())));
			} else {
				final String convertedHtml = getConverter().convertWithQuotes(htmlContent);
				content = MessageUtility.formatContentForDisplay(convertedHtml, false, session, mailPath,
						displayVersion);
			}
			jsonObject.put(MailJSONField.DISPOSITION.getKey(), Part.INLINE);
			jsonObject.put(MailJSONField.SIZE.getKey(), content.length());
			jsonObject.put(MailJSONField.CONTENT.getKey(), content);
			getAttachmentsArr().put(jsonObject);
			if (displayVersion) {
				/*
				 * Create attachment object for original html content
				 */
				final JSONObject originalVersion = new JSONObject();
				originalVersion.put(MailListField.ID.getKey(), id);
				originalVersion.put(MailJSONField.CONTENT_TYPE.getKey(), baseContentType);
				originalVersion.put(MailJSONField.DISPOSITION.getKey(), Part.ATTACHMENT);
				originalVersion.put(MailJSONField.SIZE.getKey(), htmlContent.length());
				originalVersion.put(MailJSONField.CONTENT.getKey(), JSONObject.NULL);
				if (fileName != null) {
					try {
						originalVersion.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), MimeUtility
								.decodeText(fileName));
					} catch (final UnsupportedEncodingException e) {
						originalVersion.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), fileName);
					}
				} else {
					originalVersion.put(MailJSONField.ATTACHMENT_FILE_NAME.getKey(), JSONObject.NULL);
				}
				getAttachmentsArr().put(originalVersion);
			}
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}

	}

}
