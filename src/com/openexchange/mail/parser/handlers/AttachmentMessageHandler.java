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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.internet.InternetAddress;

import com.openexchange.api2.OXException;
import com.openexchange.imap.datasource.MessageDataSource;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailContent;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.UUEncodedPart;

/**
 * AttachmentMessageHandler
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AttachmentMessageHandler implements MailMessageHandler {

	private static final String MIME_APPL_OCTET = "application/octet-stream";

	private static final class TextMailContent extends MailContent {

		private final String text;

		/**
		 * @param text
		 *            The text content
		 */
		public TextMailContent(final String text) {
			super();
			this.text = text;
		}

		@Override
		public Object getContent() throws MailException {
			return text;
		}

		@Override
		public DataHandler getDataHandler() throws MailException {
			return null;
		}

		@Override
		public int getEnclosedCount() throws MailException {
			return 0;
		}

		@Override
		public MailContent getEnclosedMailContent(final int index) throws MailException {
			return null;
		}

		@Override
		public InputStream getInputStream() throws MailException {
			return null;
		}

		@Override
		public void prepareForCaching() throws MailException {
		}

	}

	private static final class UUEncAttachMailContent extends MailContent {

		private final UUEncodedPart part;

		/**
		 * @param part
		 *            The uuencoded part
		 */
		public UUEncAttachMailContent(final UUEncodedPart part) {
			super();
			this.part = part;
		}

		@Override
		public Object getContent() throws MailException {
			return null;
		}

		@Override
		public DataHandler getDataHandler() throws MailException {
			try {
				final ContentType contentType;
				if (!containsContentType()) {
					contentType = getContentType();
				} else {
					String ct = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(part.getFileName());
					if (ct == null || ct.length() == 0) {
						ct = MIME_APPL_OCTET;
					}
					contentType = new ContentType(ct);
				}
				return new DataHandler(new MessageDataSource(part.getInputStream(), contentType.toString()));
			} catch (final OXException e) {
				throw new MailException(e);
			} catch (final IOException e) {
				throw new MailException(MailException.Code.IO_ERROR, e, new Object[0]);
			}
		}

		@Override
		public int getEnclosedCount() throws MailException {
			return 0;
		}

		@Override
		public MailContent getEnclosedMailContent(final int index) throws MailException {
			return null;
		}

		@Override
		public InputStream getInputStream() throws MailException {
			return part.getInputStream();
		}

		@Override
		public void prepareForCaching() throws MailException {
		}

	}

	private final String id;

	private MailContent attachment;

	/**
	 * Constructor
	 */
	public AttachmentMessageHandler(final String id) {
		super();
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.parser.MailMessageHandler#handleAttachment(com.openexchange.mail.dataobjects.MailContent,
	 *      boolean, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleAttachment(final MailContent part, final boolean isInline, final String baseContentType,
			final String fileName, final String id) throws MailException {
		if (this.id.equals(id)) {
			attachment = part;
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
	public boolean handleImagePart(final MailContent part, final String imageCID, final String baseContentType,
			final String id) throws MailException {
		if (this.id.equals(id)) {
			attachment = part;
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
	public boolean handleInlineHtml(final String htmlContent, final String baseContentType, final int size,
			final String fileName, final String id) throws MailException {
		if (this.id.equals(id)) {
			attachment = new TextMailContent(htmlContent);
			try {
				attachment.setContentType(baseContentType);
			} catch (final OXException e) {
				throw new MailException(e);
			}
			attachment.setSize(size);
			attachment.setFileName(fileName);
			attachment.setSequenceId(id);
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
	public boolean handleInlinePlainText(final String plainTextContent, final String baseContentType, final int size,
			final String fileName, final String id) throws MailException {
		if (this.id.equals(id)) {
			attachment = new TextMailContent(plainTextContent);
			try {
				attachment.setContentType(baseContentType);
			} catch (final OXException e) {
				throw new MailException(e);
			}
			attachment.setSize(size);
			attachment.setFileName(fileName);
			attachment.setSequenceId(id);
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
			attachment = new UUEncAttachMailContent(part);
			String ct = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(part.getFileName());
			if (ct == null || ct.length() == 0) {
				ct = MIME_APPL_OCTET;
			}
			try {
				attachment.setContentType(ct);
			} catch (final OXException e) {
				throw new MailException(e);
			}
			attachment.setSize(part.getFileSize());
			attachment.setFileName(part.getFileName());
			attachment.setSequenceId(id);
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
	public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final String baseContentType,
			final int size, final String fileName, final String id) throws MailException {
		return handleInlinePlainText(decodedTextContent, baseContentType, size, fileName, id);
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
	public boolean handleMultipart(final MailContent mp, final int bodyPartCount, final String id) throws MailException {
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
			attachment = nestedMsg;
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
	public boolean handleSpecialPart(final MailContent part, final String baseContentType, final String id)
			throws MailException {
		return true;
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

	public MailContent getAttachment() {
		return attachment;
	}
}
