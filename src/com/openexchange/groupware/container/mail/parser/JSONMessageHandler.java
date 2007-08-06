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

package com.openexchange.groupware.container.mail.parser;

import static com.openexchange.api2.MailInterfaceImpl.handleMessagingException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Flags.Flag;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.JSONMessageAttachmentObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.UserSettingMail;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.Enriched2HtmlConverter;
import com.openexchange.tools.mail.Html2TextConverter;
import com.openexchange.tools.mail.MailTools;
import com.openexchange.tools.mail.UUEncodedPart;

/**
 * A <code>MessageHandler</code> that creates a <code>JSONMessageObject</code>
 * instance out of underlying <code>Message</code> object, which is then
 * accessible via the <code>getMessageObject()</code> method
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class JSONMessageHandler implements MessageHandler {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(JSONMessageHandler.class);

	private final SessionObject session;

	private final JSONMessageObject msgObj;

	private final UserSettingMail usm;

	private final String msgUID;

	private boolean isAlternative;

	private String altId;

	private boolean textFound;

	private final boolean createVersionForDisplay;

	private final Html2TextConverter converter;

	public JSONMessageHandler(final SessionObject session, final String msgUID, final boolean createVersionForDisplay) {
		super();
		this.session = session;
		this.usm = session.getUserSettingMail();
		this.msgObj = new JSONMessageObject(usm, TimeZone.getTimeZone(session.getUserObject().getTimeZone()));
		this.msgUID = msgUID;
		this.createVersionForDisplay = createVersionForDisplay;
		converter = new Html2TextConverter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleFrom(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
		msgObj.addFromAddresses(fromAddrs);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleRecipient(javax.mail.Message.RecipientType,
	 *      javax.mail.internet.InternetAddress[])
	 */
	public boolean handleRecipient(final RecipientType recipientType, final InternetAddress[] recipientAddrs)
			throws OXException {
		if (RecipientType.TO.equals(recipientType)) {
			msgObj.addToAddresses(recipientAddrs);
		} else if (RecipientType.CC.equals(recipientType)) {
			msgObj.addCCAddresses(recipientAddrs);
		} else if (RecipientType.BCC.equals(recipientType)) {
			msgObj.addBccAddresses(recipientAddrs);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSubject(java.lang.String)
	 */
	public boolean handleSubject(final String subject) throws OXException {
		msgObj.setSubject(subject);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSentDate(java.util.Date)
	 */
	public boolean handleSentDate(final Date sentDate) throws OXException {
		msgObj.setSentDate(sentDate);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleReceivedDate(java.util.Date)
	 */
	public boolean handleReceivedDate(final Date receivedDate) throws OXException {
		msgObj.setReceivedDate(receivedDate);
		return true;
	}

	private static final String HEADER_X_PRIORITY = "X-Priority";

	private static final String HEADER_X_MSGREF = "X-Msgref";

	private static final String HEADER_X_MAILER = "X-Mailer";

	private static final String HEADER_DISP_NOTIFICATION_TO = "Disposition-Notification-To";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleHeaders(java.util.Map)
	 */
	public boolean handleHeaders(final Map<String, String> headerMap) throws OXException {
		if (headerMap == null) {
			return true;
		}
		final int size = headerMap.size();
		final Iterator<Map.Entry<String, String>> iter = headerMap.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, String> entry = iter.next();
			if (HEADER_DISP_NOTIFICATION_TO.equalsIgnoreCase(entry.getKey())) {
				if (!msgObj.isSeen()) {
					/*
					 * Disposition-Notification: Indicate an expected read ack
					 * if this header is available and if mail has not been
					 * seen, yet
					 */
					msgObj.setDispositionNotification(entry.getValue());
				}
			} else if (HEADER_X_PRIORITY.equalsIgnoreCase(entry.getKey())) {
				/*
				 * Priority
				 */
				final String[] tmp = entry.getValue().split(" +");
				int priority = JSONMessageObject.PRIORITY_NORMAL;
				try {
					priority = Integer.parseInt(tmp[0]);
				} catch (final NumberFormatException nfe) {
					LOG.warn("Strange X-Priority header: " + tmp[0], nfe);
					priority = JSONMessageObject.PRIORITY_NORMAL;
				}
				msgObj.setPriority(priority);
			} else if (HEADER_X_MSGREF.equalsIgnoreCase(entry.getKey())) {
				/*
				 * Message Reference
				 */
				final String[] tmp = entry.getValue().split(" +");
				msgObj.setMsgref(tmp[0]);
			} else if (HEADER_X_MAILER.equalsIgnoreCase(entry.getKey())) {
				msgObj.addHeader(entry.getKey(), entry.getValue());
			} else {
				continue;
				// msgObj.addHeader(entry.getKey(), hdr.getValue());
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSystemFlags(javax.mail.Flags.Flag[])
	 */
	public boolean handleSystemFlags(final Flag[] systemFlags) throws OXException {
		if (systemFlags == null) {
			return true;
		}
		for (int i = 0; i < systemFlags.length; i++) {
			final Flags.Flag f = systemFlags[i];
			if (f == Flags.Flag.ANSWERED) {
				msgObj.setAnswered(true);
			} else if (f == Flags.Flag.DELETED) {
				msgObj.setDeleted(true);
			} else if (f == Flags.Flag.DRAFT) {
				msgObj.setDraft(true);
				/*
				 * Message is marked as draft. Set msgref
				 */
				msgObj.setMsgref(msgUID);
			} else if (f == Flags.Flag.FLAGGED) {
				msgObj.setFlagged(true);
			} else if (f == Flags.Flag.RECENT) {
				msgObj.setRecent(true);
			} else if (f == Flags.Flag.SEEN) {
				msgObj.setSeen(true);
			} else {
				/*
				 * Skip it
				 */
				continue;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleUserFlags(java.lang.String[])
	 */
	public boolean handleUserFlags(final String[] userFlags) throws OXException {
		if (userFlags == null) {
			return true;
		}
		for (int i = 0; i < userFlags.length; i++) {
			/*
			 * Color Label
			 */
			if (userFlags[i].startsWith(JSONMessageObject.COLOR_LABEL_PREFIX)) {
				msgObj.setColorLabel(JSONMessageObject.getColorLabelIntValue(userFlags[i]));
			} else {
				msgObj.addUserFlag(userFlags[i]);
			}
		}
		return true;
	}

	private static final String MIME_TEXT_ENRICHED = "text/enriched";

	private static final Enriched2HtmlConverter ENRCONV = new Enriched2HtmlConverter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlinePlainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlinePlainText(final String plainTextContentArg, final String baseContentType,
			final int size, final String fileName, final String id) throws OXException {
		if (isAlternative && usm.isDisplayHtmlInlineContent() && !textFound) {
			/*
			 * User wants to see message's alternative content
			 */
			textFound = true;
			return true;
		}
		final boolean isEnriched = MIME_TEXT_ENRICHED.equals(baseContentType.toLowerCase(Locale.ENGLISH));
		final String plainTextContent;
		if (isEnriched) {
			plainTextContent = ENRCONV.convert(plainTextContentArg);
		} else {
			plainTextContent = plainTextContentArg;
		}
		final String formattedText = MessageUtils
				.formatContentForDisplay(plainTextContent, isEnriched, session, msgUID);
		final JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject(id);
		mao.setDisposition(Part.INLINE);
		mao.setContentType(baseContentType);
		mao.setContent(formattedText);
		mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
		mao.setSize(formattedText.length());
		msgObj.addMessageAttachment(mao);
		textFound = true;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineUUEncodedPainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final String baseContentType,
			final int size, final String fileName, final String id) throws OXException {
		return handleInlinePlainText(decodedTextContent, baseContentType, size, fileName, id);
	}

	private static final String MIME_APPL_OCTET_STREAM = "application/octet-stream";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineUUEncodedAttachment(com.openexchange.tools.mail.UUEncodedPart,
	 *      java.lang.String)
	 */
	public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
		final JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject(id);
		String contentType = MIME_APPL_OCTET_STREAM;
		final String filename = part.getFileName();
		try {
			contentType = new MimetypesFileTypeMap().getContentType(
					new File(filename.toLowerCase(session.getLocale())).getName()).toLowerCase(session.getLocale());
		} catch (final Exception e) {
			final Throwable t = new Throwable(new StringBuilder("Unable to fetch content/type for '").append(filename)
					.append("': ").append(e).toString());
			LOG.warn(t.getMessage(), t);
		}
		mao.setContentType(contentType);
		mao.setFileName(filename);
		mao.setSize(part.getFileSize());
		mao.setDisposition(Part.ATTACHMENT);
		/*
		 * Content-type indicates mime type text/*
		 */
		if (contentType.startsWith("text/")) {
			/*
			 * Attach link-object with text content
			 */
			mao.setContent(part.getPart().toString());
			mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
		} else {
			/*
			 * Attach link-object.
			 */
			mao.setContent(null);
			mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
		}
		msgObj.addMessageAttachment(mao);
		return true;
	}

	private static final String MIME_TEXT_PLAIN = "TEXT/PLAIN";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineHtml(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlineHtml(final String htmlContent, final String baseContentType, final int size,
			final String fileName, final String id) throws OXException {
		final JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject(id);
		JSONMessageAttachmentObject htmlAttach = null;
		if (isAlternative && usm.isDisplayHtmlInlineContent()) {
			/*
			 * multipart/alternative and user wants to see content's html
			 * version
			 */
			final String content = MessageUtils.formatContentForDisplay(htmlContent, true, session, msgUID);
			mao.setContentType(baseContentType);
			mao.setSize(content.length());
			mao.setDisposition(Part.INLINE);
			mao.setContent(content);
			mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
		} else if (!textFound) {
			if (usm.isDisplayHtmlInlineContent()) {
				/*
				 * Still no text found til here. Insert html content
				 */
				final String content = MessageUtils.formatContentForDisplay(htmlContent, true, session, msgUID);
				mao.setContentType(baseContentType);
				mao.setSize(content.length());
				mao.setDisposition(Part.INLINE);
				mao.setContent(content);
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
			} else {
				/*
				 * Still no text found til here. Insert text-converted html
				 * content
				 */
				mao.setContentType(MIME_TEXT_PLAIN);
				try {
					/*
					 * Try to convert the given html to regular text
					 */
					final String content;
					if (createVersionForDisplay && usm.isUseColorQuote()) {
						content = MailTools.formatHrefLinks(MessageUtils.convertAndKeepQuotes(htmlContent, converter));
					} else {
						final String convertedHtml = converter.convertWithQuotes(htmlContent);
						content = MessageUtils.formatContentForDisplay(convertedHtml, false, session, msgUID);
					}
					mao.setDisposition(Part.INLINE);
					// MessageUtils.getFormattedText(convertedHtml, false,
					// session, msgUID);
					mao.setContent(content);
					mao.setSize(content.length());
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
					if (createVersionForDisplay) {
						/*
						 * Create attachment object for original html content
						 */
						htmlAttach = new JSONMessageAttachmentObject(id);
						htmlAttach.setContentType(baseContentType);
						htmlAttach.setSize(htmlContent.length());
						htmlAttach.setDisposition(Part.ATTACHMENT);
						htmlAttach.setContent(null);
						htmlAttach.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
						htmlAttach.setFileName(fileName);
					}
				} catch (final Exception e) {
					final Throwable t = new Throwable("Unable to parse html2text for message view: " + e.getMessage());
					LOG.error(t.getMessage(), e);
				}
			}
		} else {
			/*
			 * No multipart/alternative or user does not want to see content's
			 * html version or text was already found: Insert as an attachment
			 */
			mao.setContentType(baseContentType);
			mao.setSize(htmlContent.length());
			mao.setDisposition(Part.ATTACHMENT);
			mao.setContent(null);
			mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
			mao.setFileName(fileName);
		}
		msgObj.addMessageAttachment(mao);
		if (htmlAttach != null) {
			msgObj.addMessageAttachment(htmlAttach);
		}
		return true;
	}

	private static final String MIME_PGP_SIGN = "APPLICATION/PGP-SIGNATURE";

	private static final String MIME_TEXT_ALL = "text/*";

	private static final String MIME_TEXT_RTF = "text/rtf";

	private static final String MIME_TEXT_HTM = "text/htm*";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleAttachment(javax.mail.Part,
	 *      boolean, java.lang.String, java.lang.String)
	 */
	public boolean handleAttachment(final Part part, final boolean isInline, final String baseContentType,
			final String fileName, final String id) throws OXException {
		/*
		 * Ignore isInline flag here.
		 */
		final JSONMessageAttachmentObject mao;
		final ContentType contentType;
		try {
			if (Part.INLINE.equalsIgnoreCase(part.getDisposition()) && part.getFileName() == null
					&& MIME_PGP_SIGN.equalsIgnoreCase(baseContentType)) {
				/*
				 * Ignore inline PGP signatures
				 */
				return true;
			}
			/*
			 * Create attachment object
			 */
			mao = new JSONMessageAttachmentObject(id);
			/*
			 * Remember content type to avoid a MessagingException in following
			 * catch-clause
			 */
			try {
				contentType = new ContentType(part.getContentType());
			} catch (final OXMailException e) {
				/*
				 * Invalid content type: Ignore this attachment
				 */
				if (LOG.isWarnEnabled()) {
					LOG.warn(e.getMessage(), e);
				}
				return true;
			}
			/*
			 * Fill attachment object
			 */
			mao.setSize(part.getSize());
			try {
				mao.setFileName(part.getFileName() == null ? fileName : MimeUtility.decodeText(part.getFileName()));
			} catch (final UnsupportedEncodingException e) {
				LOG.error("Unsupported encoding in a message detected and monitored.", e);
				MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
				mao.setFileName(part.getFileName() == null ? fileName : MessageUtils.decodeMultiEncodedHeader(part
						.getFileName()));
			}
			mao.setContentType(contentType.getBaseType());
			mao.setDisposition(Part.ATTACHMENT);
			if (isInline && contentType.isMimeType(MIME_TEXT_ALL)) {
				// TODO: Add rtf2html conversion here!
				if (contentType.isMimeType(MIME_TEXT_RTF)) {
					mao.setContent(null);
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
				} else {
					mao.setContent(MessageUtils.formatContentForDisplay(MessageUtils.readPart(part, contentType),
							contentType.isMimeType(MIME_TEXT_HTM), session, msgUID));
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
				}
			} else {
				mao.setContent(null);
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
			}
			msgObj.addMessageAttachment(mao);
			return true;
		} catch (final MessagingException e) {
			throw handleMessagingException(e, session.getIMAPProperties(), session.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSpecialPart(javax.mail.Part,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean handleSpecialPart(final Part part, final String baseContentType, final String id) throws OXException {
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
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleImagePart(javax.mail.Part,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleImagePart(final Part part, final String imageCID, final String baseContentType, final String id)
			throws OXException {
		/*
		 * When creating a JSON message object from a message we do not
		 * distinguish special parts or image parts from "usual" attachments.
		 * Therefore invoke the handleAttachment method
		 */
		return handleAttachment(part, false, baseContentType, null, id);
	}

	private static final String MIME_MULTIPART_ALT = "multipart/alternative";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleMultipart(javax.mail.Multipart,
	 *      int, java.lang.String)
	 */
	public boolean handleMultipart(final Multipart mp, final int bodyPartCount, final String id) throws OXException {
		/*
		 * Determine if message is of MIME type multipart/alternative
		 */
		if (mp.getContentType().regionMatches(true, 0, MIME_MULTIPART_ALT, 0, 21) && bodyPartCount >= 2) {
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
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleNestedMessage(javax.mail.Message,
	 *      java.lang.String)
	 */
	public boolean handleNestedMessage(final Message nestedMsg, final String id) throws OXException {
		final JSONMessageHandler msgHandler = new JSONMessageHandler(session, msgUID, createVersionForDisplay);
		try {
			new MessageDumper(session).dumpMessage(nestedMsg, msgHandler, id);
		} catch (final MessagingException e) {
			throw handleMessagingException(e, session.getIMAPProperties(), session.getContext());
		}
		msgObj.addNestedMessage(msgHandler.getMessageObject());
		return true;
	}

	/**
	 * @return the JSON message object parsed out of underlying message
	 */
	public JSONMessageObject getMessageObject() {
		return msgObj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final String delim = " | ";
		return new StringBuilder(100).append("isAlternative=").append(isAlternative).append(delim).append("textFound=")
				.append(textFound).append(delim).append("msgUID=").append(msgUID).append(delim).append("msgObject=")
				.append(msgObj.toString()).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleMessageEnd()
	 */
	public void handleMessageEnd(final Message msg) throws OXException {
		/*
		 * Since we obviously touched message's content, mark its corresponding
		 * message object as seen
		 */
		msgObj.setSeen(true);
		if (createVersionForDisplay && msg.getFolder() != null) {
			/*
			 * Try to fill folder information into message object
			 */
			final Folder fld = msg.getFolder();
			try {
				msgObj.setTotal(fld.getMessageCount());
				msgObj.setNew(fld.getNewMessageCount());
				msgObj.setUnread(fld.getUnreadMessageCount());
				msgObj.setDeleted(fld.getDeletedMessageCount());
			} catch (final MessagingException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

}
