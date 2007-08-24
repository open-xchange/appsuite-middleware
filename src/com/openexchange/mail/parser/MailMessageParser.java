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

package com.openexchange.mail.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import net.freeutils.tnef.Attachment;
import net.freeutils.tnef.Attr;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.RawInputStream;
import net.freeutils.tnef.TNEFInputStream;
import net.freeutils.tnef.TNEFUtils;
import net.freeutils.tnef.mime.ContactHandler;
import net.freeutils.tnef.mime.RawDataSource;
import net.freeutils.tnef.mime.ReadReceiptHandler;
import net.freeutils.tnef.mime.TNEFMime;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.IMAPPropertyException;
import com.openexchange.imap.MessageHeaders;
import com.openexchange.imap.TNEFBodyPart;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailContent;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.imap.dataobjects.IMAPMailContent;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.UUEncodedMultiPart;

/**
 * {@link MailMessageParser} - A callback parser to parse instances of
 * {@link MailMessage} by invoking the <code>handleXXX()</code> methods of
 * given {@link MailMessageHandler} object
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailMessageParser {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailMessageParser.class);

	/*
	 * MIME type constants
	 */
	private static final String MIME_APPL_OCTET = "application/octet-stream";

	private static final String MIME_TEXT_PLAIN = "text/plain";

	private static final String MIME_TEXT_ENRICHED = "text/enriched";

	private static final String MIME_TEXT_RTF = "text/rtf";

	private static final String MIME_TEXT_HTML = "text/html";

	private static final String MIME_TEXT_RFC822_HDRS = "text/rfc822-headers";

	private static final String MIME_TEXT_ALL_CARD = "text/*card";

	private static final String MIME_TEXT_ALL_CALENDAR = "text/*calendar";

	private static final String MIME_MULTIPART_ALL = "multipart/*";

	private static final String MIME_IMAGE_ALL = "image/*";

	private static final String MIME_MESSAGE_RFC822 = "message/rfc822";

	private static final String MIME_MESSAGE_DELIVERY_STATUS = "message/delivery-status";

	private static final String MIME_MESSAGE_DISP_NOTIFICATION = "message/disposition-notification";

	/*
	 * +++++++++++++++++++ TNEF CONSTANTS +++++++++++++++++++
	 */
	private static final String TNEF_IPM_CONTACT = "IPM.Contact";

	private static final String TNEF_IPM_MS_READ_RECEIPT = "IPM.Microsoft Mail.Read Receipt";

	/*
	 * String constants
	 */
	private static final String STR_CHARSET = "charset";

	private boolean stop;

	private boolean multipartDetected;

	/**
	 * Constructor
	 */
	public MailMessageParser() {
		super();
	}

	public void parseMailMessage(final MailMessage msg, final MailMessageHandler msgHandler) throws MailException,
			IMAPPropertyException, MessagingException {
		parseMailMessage(msg, msgHandler, null);
	}

	public void parseMailMessage(final MailMessage msg, final MailMessageHandler msgHandler, final String prefix)
			throws MailException, IMAPPropertyException, MessagingException {
		try {
			parseMailContent(msg, msgHandler, prefix, 1);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.UNREADBALE_PART_CONTENT, e, Long.valueOf(msg.getUid()), msg
					.getFolder());
		}
		msgHandler.handleMessageEnd(msg);
	}

	private void parseMailContent(final MailContent mailContent, final MailMessageHandler msgHandler,
			final String prefix, final int partCountArg) throws IMAPPropertyException, MailException, IOException,
			MessagingException {
		if (stop) {
			return;
		}
		if (mailContent instanceof MailMessage) {
			parseEnvelope((MailMessage) mailContent, msgHandler);
		}
		/*
		 * Set part infos
		 */
		int partCount = partCountArg;
		final String disposition = mailContent.containsDisposition() ? mailContent.getDisposition() : null;
		final int size = mailContent.getSize();
		final String filename = MessageUtils.getFileName(mailContent.getFileName(), MessageUtils.getIdentifier(prefix,
				partCount));
		final ContentType contentType;
		try {
			contentType = mailContent.containsContentType() ? mailContent.getContentType() : new ContentType(
					MIME_APPL_OCTET);
		} catch (final OXException e) {
			throw new MailException(e);
		}
		String charset = contentType.getParameter(STR_CHARSET);
		if (null == charset) {
			charset = IMAPProperties.getDefaultMimeCharset();
		}
		/*
		 * Parse part dependent on its MIME type
		 */
		final boolean isInline = ((disposition == null || disposition.equalsIgnoreCase(Part.INLINE)) && mailContent
				.getFileName() == null);
		if (contentType.isMimeType(MIME_TEXT_PLAIN) || contentType.isMimeType(MIME_TEXT_ENRICHED)) {
			if (isInline) {
				final String content = MessageUtils.readStream(mailContent.getInputStream(), charset);
				final UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
				if (uuencodedMP.isUUEncoded()) {
					/*
					 * UUEncoded content detected. Handle normal text.
					 */
					if (!msgHandler.handleInlineUUEncodedPlainText(uuencodedMP.getCleanText(), contentType
							.getBaseType(), uuencodedMP.getCleanText().length(), filename, MessageUtils.getIdentifier(
							prefix, partCount))) {
						stop = true;
						return;
					}
					/*
					 * Now handle uuencoded attachments
					 */
					final int count = uuencodedMP.getCount();
					for (int a = 0; a < count; a++) {
						/*
						 * Increment part count by 1
						 */
						partCount++;
						if (!msgHandler.handleInlineUUEncodedAttachment(uuencodedMP.getBodyPart(a), MessageUtils
								.getIdentifier(prefix, partCount))) {
							stop = true;
							return;
						}
					}
				} else {
					/*
					 * Just non-encoded plain text
					 */
					if (!msgHandler.handleInlinePlainText(content, contentType.getBaseType(), size, filename,
							MessageUtils.getIdentifier(prefix, partCount))) {
						stop = true;
						return;
					}
				}
			} else {
				/*
				 * Non-Inline: Text attachment
				 */
				if (!msgHandler.handleAttachment(mailContent, false, contentType.getBaseType(), filename, MessageUtils
						.getIdentifier(prefix, partCount))) {
					stop = true;
					return;
				}
			}
		} else if (contentType.isMimeType(MIME_TEXT_HTML)) {
			if (isInline) {
				if (!msgHandler.handleInlineHtml(MessageUtils.readStream(mailContent.getInputStream(), charset),
						contentType.getBaseType(), size, filename, MessageUtils.getIdentifier(prefix, partCount))) {
					stop = true;
					return;
				}
			} else {
				if (!msgHandler.handleAttachment(mailContent, false, contentType.getBaseType(), filename, MessageUtils
						.getIdentifier(prefix, partCount))) {
					stop = true;
					return;
				}
			}
		} else if (contentType.isMimeType(MIME_MULTIPART_ALL)) {
			final MailContent multipartMailContent = (MailContent) mailContent.getContent();
			final int count = multipartMailContent.getEnclosedCount();
			if (count == 0) {
				throw new MailException(MailException.Code.INVALID_MULTIPART_CONTENT);
			}
			final String mpId = MessageUtils.getIdentifier(prefix, partCount);
			if (!msgHandler.handleMultipart(multipartMailContent, count, mpId)) {
				stop = true;
				return;
			}
			final String mpPrefix;
			if (!multipartDetected) {
				mpPrefix = prefix;
				multipartDetected = true;
			} else {
				mpPrefix = mpId;
			}
			for (int i = 0; i < count; i++) {
				final MailContent enclosedContent = multipartMailContent.getEnclosedMailContent(i);
				parseMailContent(enclosedContent, msgHandler, mpPrefix, i + 1);
			}
		} else if (contentType.isMimeType(MIME_IMAGE_ALL)) {
			if (!msgHandler.handleImagePart(mailContent, mailContent.getContentId(), contentType.getBaseType(),
					MessageUtils.getIdentifier(prefix, partCount))) {
				stop = true;
				return;
			}
		} else if (contentType.isMimeType(MIME_MESSAGE_RFC822)) {
			final MailMessage nestedMsg = (MailMessage) mailContent.getContent();
			if (!msgHandler.handleNestedMessage(nestedMsg, MessageUtils.getIdentifier(prefix, partCount))) {
				stop = true;
				return;
			}
		} else if (TNEFUtils.isTNEFMimeType(mailContent.getContentType().toString())) {
			try {
				/*
				 * Here go with TNEF encoded messages. Since TNEF library is
				 * based on JavaMail API we are forced to use JavaMail-specific
				 * types regardless of the mail implementation.
				 * 
				 * First, grab TNEF input stream.
				 */
				final TNEFInputStream tnefInputStream = new TNEFInputStream(mailContent.getInputStream());
				/*
				 * Wrapping TNEF message
				 */
				final net.freeutils.tnef.Message message = new net.freeutils.tnef.Message(tnefInputStream);
				/*
				 * Handle special conversion
				 */
				final Attr messageClass = message.getAttribute(Attr.attMessageClass);
				final String messageClassName = messageClass == null ? "" : ((String) messageClass.getValue());
				if (messageClass != null && TNEF_IPM_CONTACT.equalsIgnoreCase(messageClassName)) {
					/*
					 * Convert contact to standard vCard. Resulting Multipart
					 * object consists of only ONE BodyPart object which
					 * encapsulates converted VCard. But for consistency reasons
					 * keep the code structure to iterate over Multipart's child
					 * objects.
					 */
					final Multipart mp = ContactHandler.convert(message);
					final int mpsize = mp.getCount();
					for (int i = 0; i < mpsize; i++) {
						/*
						 * Since TNEF library is based on JavaMail API we use an
						 * instance of IMAPMailContent regardless of the mail
						 * implementation
						 */
						parseMailContent(new IMAPMailContent(mp.getBodyPart(i)), msgHandler, prefix, partCount++);
					}
					/*
					 * Stop to further process tnef attachment
					 */
					return;
				} else if (messageClassName.equalsIgnoreCase(TNEF_IPM_MS_READ_RECEIPT)) {
					/*
					 * Convert read receipt to standard notification. Resulting
					 * Multipart object consists both the human readable text
					 * part and machine readable part.
					 */
					final Multipart mp = ReadReceiptHandler.convert(message);
					final int mpsize = mp.getCount();
					for (int i = 0; i < mpsize; i++) {
						/*
						 * Since TNEF library is based on JavaMail API we use an
						 * instance of IMAPMailContent regardless of the mail
						 * implementation
						 */
						parseMailContent(new IMAPMailContent(mp.getBodyPart(i)), msgHandler, prefix, partCount++);
					}
					/*
					 * Stop to further process tnef attachment
					 */
					return;
				}
				/*
				 * Look for body. Usually the body is the rtf text.
				 */
				final Attr attrBody = Attr.findAttr(message.getAttributes(), Attr.attBody);
				if (attrBody != null) {
					final TNEFBodyPart bodyPart = new TNEFBodyPart();
					bodyPart.setText((String) attrBody.getValue());
					bodyPart.setSize(((String) attrBody.getValue()).length());
					parseMailContent(new IMAPMailContent(bodyPart), msgHandler, prefix, partCount++);
				}
				if (message.getMAPIProps() != null) {
					final RawInputStream ris = (RawInputStream) message.getMAPIProps().getPropValue(
							MAPIProp.PR_RTF_COMPRESSED);
					if (ris != null) {
						final byte[] rtfBody = ris.toByteArray();
						final TNEFBodyPart bodyPart = new TNEFBodyPart();
						final String content = new String(TNEFUtils.decompressRTF(rtfBody), ServerConfig
								.getProperty(Property.DefaultEncoding));
						bodyPart.setText(content);
						bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIME_TEXT_RTF);
						bodyPart.setSize(content.length());
						parseMailContent(new IMAPMailContent(bodyPart), msgHandler, prefix, partCount++);
					}
				}
				/*
				 * Iterate TNEF attachments and nested messages
				 */
				final int s = message.getAttachments().size();
				final Iterator iter = message.getAttachments().iterator();
				final ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
				for (int i = 0; i < s; i++) {
					final Attachment attachment = (Attachment) iter.next();
					final TNEFBodyPart bodyPart = new TNEFBodyPart();
					if (attachment.getNestedMessage() == null) {
						// add TNEF attributes
						bodyPart.setTNEFAttributes(attachment.getAttributes());
						// translate TNEF attributes to Mime
						final String attachFilename = attachment.getFilename();
						if (attachFilename != null) {
							bodyPart.setFileName(attachFilename);
						}
						String contentTypeStr = null;
						if (attachment.getMAPIProps() != null) {
							contentTypeStr = (String) attachment.getMAPIProps().getPropValue(
									MAPIProp.PR_ATTACH_MIME_TAG);
						}
						if (contentTypeStr == null && attachFilename != null) {
							contentTypeStr = MimetypesFileTypeMap.getDefaultFileTypeMap()
									.getContentType(attachFilename);
						}
						if (contentTypeStr == null) {
							contentTypeStr = MIME_APPL_OCTET;
						}
						final DataSource ds = new RawDataSource(attachment.getRawData(), contentTypeStr);
						bodyPart.setDataHandler(new DataHandler(ds));
						bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentTypeStr);
						os.reset();
						attachment.writeTo(os);
						bodyPart.setSize(os.size());
						parseMailContent(new IMAPMailContent(bodyPart), msgHandler, prefix, partCount++);
					} else {
						/*
						 * Nested message
						 */
						final MimeMessage nestedMessage = TNEFMime.convert(Session.getDefaultInstance(IMAPProperties
								.getDefaultJavaMailProperties()), attachment.getNestedMessage());
						bodyPart.setDataHandler(new DataHandler(nestedMessage, MIME_MESSAGE_RFC822));
						bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIME_MESSAGE_RFC822);
						parseMailContent(new IMAPMailContent(bodyPart), msgHandler, prefix, partCount++);
					}
				}
			} catch (final IOException tnefExc) {
				if (LOG.isWarnEnabled()) {
					LOG.warn(tnefExc.getLocalizedMessage(), tnefExc);
				}
				if (!msgHandler.handleAttachment(mailContent, isInline, contentType.getBaseType(), filename,
						MessageUtils.getIdentifier(prefix, partCount))) {
					stop = true;
					return;
				}
			}
		} else if (contentType.isMimeType(MIME_MESSAGE_DELIVERY_STATUS)
				|| contentType.isMimeType(MIME_MESSAGE_DISP_NOTIFICATION)
				|| contentType.isMimeType(MIME_TEXT_RFC822_HDRS) || contentType.isMimeType(MIME_TEXT_ALL_CARD)
				|| contentType.isMimeType(MIME_TEXT_ALL_CALENDAR)) {
			if (!msgHandler.handleSpecialPart(mailContent, contentType.getBaseType(), MessageUtils.getIdentifier(
					prefix, partCount))) {
				stop = true;
				return;
			}
		} else {
			if (!msgHandler.handleAttachment(mailContent, isInline, contentType.getBaseType(), filename, MessageUtils
					.getIdentifier(prefix, partCount))) {
				stop = true;
				return;
			}
		}
	}

	private void parseEnvelope(final MailMessage msg, final MailMessageHandler msgHandler) throws MailException {
		/*
		 * FROM
		 */
		msgHandler.handleFrom(msg.getFrom());
		/*
		 * RECIPIENTS
		 */
		msgHandler.handleToRecipient(msg.getTo());
		msgHandler.handleCcRecipient(msg.getCc());
		msgHandler.handleBccRecipient(msg.getBcc());
		/*
		 * SUBJECT
		 */
		msgHandler.handleSubject(MessageUtils.decodeMultiEncodedHeader(msg.getSubject()));
		/*
		 * SENT DATE
		 */
		if (msg.getSentDate() != null) {
			msgHandler.handleSentDate(msg.getSentDate());
		}
		/*
		 * RECEIVED DATE
		 */
		if (msg.getReceivedDate() != null) {
			msgHandler.handleReceivedDate(msg.getReceivedDate());
		}
		/*
		 * FLAGS
		 */
		msgHandler.handleSystemFlags(msg.getFlags());
		msgHandler.handleUserFlags(msg.getUserFlags());
		/*
		 * COLOR LABEL
		 */
		msgHandler.handleColorLabel(msg.getColorLabel());
		/*
		 * PRIORITY
		 */
		msgHandler.handlePriority(msg.getPriority());
		/*
		 * CONTENT-ID
		 */
		if (msg.containsContentId()) {
			msgHandler.handleContentId(msg.getContentId());
		}
		/*
		 * HEADERS
		 */
		msgHandler.handleHeaders(msg.getHeadersSize(), msg.getHeadersIterator());
	}
}
