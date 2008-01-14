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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;

import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.UUEncodedAttachmentMailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link MailPartHandler} - Looks for a certain mail part by sequence ID
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailPartHandler implements MailMessageHandler {

	private static final class TextMailPart extends MailPart {

		private static final long serialVersionUID = 5622318721711740585L;

		private final String text;

		private transient DataSource dataSource;

		/**
		 * @param text
		 *            The text content
		 */
		public TextMailPart(final String text, final ContentType contentType) {
			super();
			this.text = text;
			setSize(text.length());
			if (contentType.getCharsetParameter() == null) {
				contentType.setCharsetParameter(MailConfig.getDefaultMimeCharset());
			}
			setContentType(contentType);
		}

		private DataSource getDataSource() throws MailException {
			if (null == dataSource) {
				try {
					dataSource = new MessageDataSource(text, getContentType());
				} catch (final UnsupportedEncodingException e) {
					throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
				}
			}
			return dataSource;
		}

		@Override
		public Object getContent() throws MailException {
			return text;
		}

		@Override
		public DataHandler getDataHandler() throws MailException {
			return new DataHandler(getDataSource());
		}

		@Override
		public int getEnclosedCount() throws MailException {
			return NO_ENCLOSED_PARTS;
		}

		@Override
		public MailPart getEnclosedMailPart(final int index) throws MailException {
			return null;
		}

		@Override
		public InputStream getInputStream() throws MailException {
			try {
				return getDataSource().getInputStream();
			} catch (final IOException e) {
				throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
			}
		}

		@Override
		public void prepareForCaching() {
		}

	}

	private final String id;

	private MailPart mailPart;

	/**
	 * Constructor
	 */
	public MailPartHandler(final String id) {
		super();
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleAttachment(com.openexchange.mail.dataobjects.MailContent,
	 *      boolean, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleAttachment(final MailPart part, final boolean isInline, final String baseContentType,
			final String fileName, final String id) throws MailException {
		if (this.id.equals(id)) {
			mailPart = part;
			if (!isInline) {
				checkFilename(mailPart, id, baseContentType);
			}
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleBccRecipient(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleBccRecipient(final InternetAddress[] recipientAddrs) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleCcRecipient(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleCcRecipient(final InternetAddress[] recipientAddrs) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleColorLabel(int)
	 */
	public boolean handleColorLabel(final int colorLabel) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleContentId(java.lang.String)
	 */
	public boolean handleContentId(final String contentId) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleFrom(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleFrom(final InternetAddress[] fromAddrs) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleHeaders(int,
	 *      java.util.Iterator)
	 */
	public boolean handleHeaders(final int size, final Iterator<Entry<String, String>> iter) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleImagePart(com.openexchange.mail.dataobjects.MailContent,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleImagePart(final MailPart part, final String imageCID, final String baseContentType,
			final String id) throws MailException {
		if (this.id.equals(id)) {
			mailPart = part;
			checkFilename(mailPart, id, baseContentType);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineHtml(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlineHtml(final String htmlContent, final ContentType contentType, final long size,
			final String fileName, final String id) throws MailException {
		if (this.id.equals(id)) {
			mailPart = new TextMailPart(htmlContent, contentType);
			mailPart.setContentType(contentType);
			mailPart.setSize(size);
			mailPart.setFileName(fileName);
			mailPart.setSequenceId(id);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleInlinePlainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlinePlainText(final String plainTextContent, final ContentType contentType, final long size,
			final String fileName, final String id) throws MailException {
		if (this.id.equals(id)) {
			mailPart = new TextMailPart(plainTextContent, contentType);
			mailPart.setContentType(contentType);
			mailPart.setSize(size);
			mailPart.setFileName(fileName);
			mailPart.setSequenceId(id);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineUUEncodedAttachment(com.openexchange.tools.mail.UUEncodedPart,
	 *      java.lang.String)
	 */
	public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws MailException {
		if (this.id.equals(id)) {
			mailPart = new UUEncodedAttachmentMailPart(part);
			String ct = MIMEType2ExtMap.getContentType(part.getFileName());
			if (ct == null || ct.length() == 0) {
				ct = MIMETypes.MIME_APPL_OCTET;
			}
			mailPart.setContentType(ct);
			mailPart.setSize(part.getFileSize());
			mailPart.setFileName(part.getFileName());
			mailPart.setSequenceId(id);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineUUEncodedPlainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
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
	public void handleMessageEnd(final MailMessage msg) throws MailException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleMultipart(com.openexchange.mail.dataobjects.MailContent,
	 *      int, java.lang.String)
	 */
	public boolean handleMultipart(final MailPart mp, final int bodyPartCount, final String id) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleNestedMessage(com.openexchange.mail.dataobjects.MailMessage,
	 *      java.lang.String)
	 */
	public boolean handleNestedMessage(final MailMessage nestedMsg, final String id) throws MailException {
		if (this.id.equals(id)) {
			mailPart = nestedMsg;
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handlePriority(int)
	 */
	public boolean handlePriority(final int priority) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleMsgRef(java.lang.String)
	 */
	public boolean handleMsgRef(final String msgRef) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleDispositionNotification(javax.mail.internet.InternetAddress)
	 */
	public boolean handleDispositionNotification(final InternetAddress dispositionNotificationTo, final boolean seen)
			throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleReceivedDate(java.util.Date)
	 */
	public boolean handleReceivedDate(final Date receivedDate) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleSentDate(java.util.Date)
	 */
	public boolean handleSentDate(final Date sentDate) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleSpecialPart(com.openexchange.mail.dataobjects.MailContent,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean handleSpecialPart(final MailPart part, final String baseContentType, final String id)
			throws MailException {
		return handleAttachment(
				part,
				(!Part.ATTACHMENT.equalsIgnoreCase(part.getContentDisposition().getDisposition()) && part.getFileName() == null),
				baseContentType, part.getFileName(), id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleSubject(java.lang.String)
	 */
	public boolean handleSubject(final String subject) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleSystemFlags(int)
	 */
	public boolean handleSystemFlags(final int flags) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleToRecipient(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleToRecipient(final InternetAddress[] recipientAddrs) throws MailException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleUserFlags(java.lang.String[])
	 */
	public boolean handleUserFlags(final String[] userFlags) throws MailException {
		return true;
	}

	/**
	 * Gets the identified mail part or <code>null</code> if none found
	 * matching given sequence ID
	 * 
	 * @return The identified mail part or <code>null</code> if none found
	 *         matching given sequence ID
	 */
	public MailPart getMailPart() {
		return mailPart;
	}

	private static void checkFilename(final MailPart mailPart, final String id, final String baseMimeType) {
		if (mailPart.getFileName() == null) {
			mailPart.setFileName(generateFilename(id, baseMimeType));
		}
	}

}
