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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Flags.Flag;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.JSONMessageAttachmentObject;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.UUEncodedPart;

/**
 * JSONAttachmentMessageHandler
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class JSONAttachmentMessageHandler implements MessageHandler {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(JSONAttachmentMessageHandler.class);

	private final SessionObject session;

	private final String id;

	private JSONMessageAttachmentObject attachment;
	
	private boolean multipartDetected;

	public JSONAttachmentMessageHandler(final SessionObject session, final String id) throws OXException {
		super();
		if (id == null || id.length() == 0) {
			throw new OXMailException(MailCode.MISSING_PARAM, "id");
		}
		this.session = session;
		this.id = id;
		attachment = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleFrom(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
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
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSubject(java.lang.String)
	 */
	public boolean handleSubject(final String subject) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSentDate(java.util.Date)
	 */
	public boolean handleSentDate(final Date sentDate) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleReceivedDate(java.util.Date)
	 */
	public boolean handleReceivedDate(final Date receivedDate) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleHeaders(java.util.Map)
	 */
	public boolean handleHeaders(final Map<String, String> headerMap) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSystemFlags(javax.mail.Flags.Flag[])
	 */
	public boolean handleSystemFlags(final Flag[] systemFlags) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleUserFlags(java.lang.String[])
	 */
	public boolean handleUserFlags(final String[] userFlags) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlinePlainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlinePlainText(final String plainTextContent, final String baseContentType, final int size,
			final String fileName, final String id) throws OXException {
		if (this.id.equals(id)) {
			try {
				attachment = new JSONMessageAttachmentObject(id);
				attachment.setFileName(fileName);
				attachment.setSize(size);
				attachment.setContentType(baseContentType);
				attachment.setDisposition(Part.INLINE);
				attachment.setContent(new ByteArrayInputStream(plainTextContent.getBytes("UTF-8")));
				attachment.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				return false;
			} catch (IOException e) {
				throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineUUEncodedAttachment(com.openexchange.tools.mail.UUEncodedPart,
	 *      java.lang.String)
	 */
	public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
		if (this.id.equals(id)) {
			attachment = new JSONMessageAttachmentObject(id);
			attachment.setSize(part.getFileSize());
			String contentType = "application/octet-stream";
			final String filename = part.getFileName();
			try {
				contentType = new MimetypesFileTypeMap().getContentType(
						new File(filename.toLowerCase(session.getLocale())).getName()).toLowerCase(session.getLocale());
			} catch (Exception e) {
				final Throwable t = new Throwable(new StringBuilder("WARN: Unable to fetch content/type for '").append(
						filename).append("': ").append(e).toString());
				LOG.error(t.getMessage(), t);
			}
			attachment.setFileName(filename);
			attachment.setContentType(contentType);
			attachment.setDisposition(Part.ATTACHMENT);
			attachment.setContent(part.getInputStream());
			attachment.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineHtml(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlineHtml(final String htmlContent, final String baseContentType, final int size,
			final String fileName, final String id) throws OXException {
		if (this.id.equals(id)) {
			try {
				attachment = new JSONMessageAttachmentObject(id);
				attachment.setSize(size);
				attachment.setFileName(fileName);
				attachment.setContentType(baseContentType);
				attachment.setDisposition(Part.INLINE);
				attachment.setContent(new ByteArrayInputStream(htmlContent.getBytes("UTF-8")));
				attachment.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				return false;
			} catch (IOException e) {
				throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleAttachment(javax.mail.Part,
	 *      boolean, java.lang.String, java.lang.String)
	 */
	public boolean handleAttachment(final Part part, final boolean isInline, final String baseContentType,
			final String fileName, final String id) throws OXException {
		if (this.id.equals(id)) {
			try {
				attachment = new JSONMessageAttachmentObject(id);
				attachment.setFileName(part.getFileName() == null ? fileName : part.getFileName());
				attachment.setSize(part.getSize());
				attachment.setContentType(baseContentType);
				attachment.setDisposition(isInline ? Part.INLINE : Part.ATTACHMENT);
				attachment.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				/*
				 * Set content
				 */
				final Object o = part.getContent();
				if (o instanceof String) {
					/*
					 * String object
					 */
					attachment.setContent(new ByteArrayInputStream(((String) o).getBytes("UTF-8")));
				} else if (o instanceof InputStream) {
					/*
					 * InputStream
					 */
					attachment.setContent(o);
				} else {
					/*
					 * Unknown type
					 */
					if (LOG.isWarnEnabled()) {
						LOG.warn(new StringBuilder("Unknown type in message attachment: ").append(o.getClass().getName())
								.toString());
					}
					attachment.setContent(part.getInputStream());
				}
				return false;
			} catch (MessagingException e) {
				throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
			} catch (IOException e) {
				throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSpecialPart(javax.mail.Part,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean handleSpecialPart(final Part part, final String baseContentType, final String id) throws OXException {
		if (this.id.equals(id)) {
			try {
				attachment = new JSONMessageAttachmentObject(id);
				attachment.setSize(part.getSize());
				attachment.setFileName(part.getFileName());
				attachment.setContentType(baseContentType);
				attachment.setDisposition(Part.ATTACHMENT);
				attachment.setContent(part.getInputStream());
				attachment.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				return false;
			} catch (MessagingException e) {
				throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
			} catch (IOException e) {
				throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleImagePart(javax.mail.Part,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleImagePart(final Part part, final String imageCID, final String baseContentType, final String id)
			throws OXException {
		if (this.id.equals(id)) {
			try {
				attachment = new JSONMessageAttachmentObject(id);
				attachment.setSize(part.getSize());
				attachment.setFileName(part.getFileName());
				attachment.setContentType(baseContentType);
				attachment.setDisposition(Part.ATTACHMENT);
				attachment.setContent(part.getInputStream());
				attachment.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				return false;
			} catch (MessagingException e) {
				throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
			} catch (IOException e) {
				throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleMultipart(javax.mail.Multipart,
	 *      int, java.lang.String)
	 */
	public boolean handleMultipart(final Multipart mp, final int bodyPartCount, final String id) throws OXException {
		if (!multipartDetected) {
			multipartDetected = true;
			return true;
		}
		if (this.id.equals(id)) {
			attachment = new JSONMessageAttachmentObject(id);
			attachment.setSize(-1);
			final ContentType ct = new ContentType(mp.getContentType());
			attachment.setContentType(ct.getBaseType());
			attachment.setContent(null);
			attachment.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
			return false;
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
		final JSONAttachmentMessageHandler msgHandler = new JSONAttachmentMessageHandler(session, this.id);
		try {
			new MessageDumper(session).dumpMessage(nestedMsg, msgHandler, id);
			if (msgHandler.getAttachmentObject() != null) {
				this.attachment = msgHandler.getAttachmentObject();
				return false;
			}
			return true;
		} catch (MessagingException e) {
			throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleMessageEnd()
	 */
	public void handleMessageEnd(final Message msg) throws OXException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("handleMessageEnd()");
		}
	}

	/**
	 * @return the attachment identfied through given attachment id
	 */
	public JSONMessageAttachmentObject getAttachmentObject() {
		return this.attachment;
	}
}
