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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;

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
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.IMAPUtils;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.TNEFBodyPart;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.UUEncodedMultiPart;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.sun.mail.iap.ProtocolException;

/**
 * MessageDumper
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MessageDumper {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageDumper.class);

	/*
	 * +++++++++++++++++++ MIME TYPES +++++++++++++++++++
	 */
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

	private static final String MIME_APPL_OCTET = "application/octet-stream";

	private static final String MIME_MESSAGE_DELIVERY_STATUS = "message/delivery-status";

	private static final String MIME_MESSAGE_DISP_NOTIFICATION = "message/disposition-notification";

	/*
	 * +++++++++++++++++++ HEADERS +++++++++++++++++++
	 */
	private static final String HEADER_CONTENT_TYPE = "Content-Type";

	/*
	 * +++++++++++++++++++ TNEF CONSTANTS +++++++++++++++++++
	 */
	private static final String TNEF_IPM_CONTACT = "IPM.Contact";

	private static final String TNEF_IPM_MS_READ_RECEIPT = "IPM.Microsoft Mail.Read Receipt";

	private final SessionObject session;

	private final boolean invokePartModifier;

	private boolean stop;

	private boolean multipartDetected;

	private final boolean nestedMsgsOnly;

	/**
	 * Creates a new <code>MessageDumper</code> instance with given session
	 * object. <code>invokePartModifier</code> is set to <code>true</code>.
	 */
	public MessageDumper(final SessionObject session) {
		this(session, true, false);
	}

	/**
	 * Creates a new <code>MessageDumper</code> instance with given session
	 * object. <code>invokePartModifier</code> determines whether underlying
	 * <code>com.openexchange.imap.PartModifier</code>
	 * implementation is going to be called or not.
	 */
	public MessageDumper(final SessionObject session, final boolean invokePartModifier, final boolean nestedMsgsOnly) {
		super();
		this.session = session;
		this.invokePartModifier = invokePartModifier;
		this.nestedMsgsOnly = nestedMsgsOnly;
	}

	/**
	 * Resets this <code>MessageDumper</code> instance for re-usage
	 * 
	 */
	public void reset() {
		stop = false;
		multipartDetected = false;
	}

	public void dumpMessage(final Message msg, final MessageHandler msgHandler) throws OXException, MessagingException {
		dumpMessage(msg, msgHandler, null);
	}

	public void dumpMessage(final Message msg, final MessageHandler msgHandler, final String prefix)
			throws OXException, MessagingException {
		try {
			dumpPart(msg, msgHandler, prefix, 1);
		} catch (final IOException e) {
			throw new OXMailException(MailCode.UNREADBALE_PART_CONTENT, e, Integer.valueOf(msg.getMessageNumber()), msg
					.getFolder().getFullName(), OXFolderManagerImpl.getUserName(session));
		}
		msgHandler.handleMessageEnd(msg);
	}

	private final void dumpPart(final Part partArg, final MessageHandler msgHandler, final String prefix,
			final int partCountArg) throws MessagingException, OXException, IOException {
		if (stop) {
			return;
		}
		/*
		 * Call part modifier
		 */
		final Part part = invokePartModifier ? IMAPProperties.getPartModifierImpl().modifyPart(partArg) : partArg;
		if (part instanceof Message) {
			dumpEnvelope((Message) part, msgHandler);
		}
		/*
		 * Set part infos
		 */
		int partCount = partCountArg;
		final String disposition = part.getDisposition();
		final int size = part.getSize();
		final String filename = MessageUtils.getFileName(part, MessageUtils.getIdentifier(prefix, partCount));
		final ContentType contentType = new ContentType();
		try {
			contentType.setContentType(part.getContentType() == null ? MIME_APPL_OCTET : part.getContentType());
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			/*
			 * Try to determine MIME type from file name
			 */
			if (part.getFileName() != null) {
				contentType.setContentType(new MimetypesFileTypeMap().getContentType(new File(filename).getName())
						.toLowerCase(session.getLocale()));
			}
		}
		/*
		 * Parse part dependent on its MIME type
		 */
		final boolean isInline = ((disposition == null || disposition.equalsIgnoreCase(Part.INLINE)) && part
				.getFileName() == null);
		if (!nestedMsgsOnly && (contentType.isMimeType(MIME_TEXT_PLAIN) || contentType.isMimeType(MIME_TEXT_ENRICHED))) {
			if (isInline) {
				final String content = MessageUtils.readPart(part, contentType);
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
				if (!msgHandler.handleAttachment(part, false, contentType.getBaseType(), filename, MessageUtils
						.getIdentifier(prefix, partCount))) {
					stop = true;
					return;
				}
			}
		} else if (!nestedMsgsOnly && contentType.isMimeType(MIME_TEXT_HTML)) {
			if (isInline) {
				if (!msgHandler.handleInlineHtml(MessageUtils.readPart(part, contentType), contentType.getBaseType(),
						size, filename, MessageUtils.getIdentifier(prefix, partCount))) {
					stop = true;
					return;
				}
			} else {
				if (!msgHandler.handleAttachment(part, false, contentType.getBaseType(), filename, MessageUtils
						.getIdentifier(prefix, partCount))) {
					stop = true;
					return;
				}
			}
		} else if (!nestedMsgsOnly && contentType.isMimeType(MIME_MULTIPART_ALL)) {
			final Multipart mp = (Multipart) part.getContent();
			final int count = mp.getCount();
			final String mpId = MessageUtils.getIdentifier(prefix, partCount);
			if (!msgHandler.handleMultipart(mp, count, mpId)) {
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
			for (int i = 0; i < mp.getCount(); i++) {
				final Part multiPartBodyPart = mp.getBodyPart(i);
				dumpPart(multiPartBodyPart, msgHandler, mpPrefix, i + 1);
			}
		} else if (!nestedMsgsOnly && contentType.isMimeType(MIME_IMAGE_ALL)) {
			if (!msgHandler.handleImagePart(part, ((MimePart) part).getContentID(), contentType.getBaseType(),
					MessageUtils.getIdentifier(prefix, partCount))) {
				stop = true;
				return;
			}
		} else if (contentType.isMimeType(MIME_MESSAGE_RFC822)) {
			final Message nestedMsg = (Message) part.getContent();
			if (!msgHandler.handleNestedMessage(nestedMsg, MessageUtils.getIdentifier(prefix, partCount))) {
				stop = true;
				return;
			}
		} else if (!nestedMsgsOnly && TNEFUtils.isTNEFMimeType(part.getContentType())) {
			try {
				/*
				 * Here go with TNEF encoded messages. First, grab TNEF input
				 * stream.
				 */
				final TNEFInputStream tnefInputStream = new TNEFInputStream(part.getInputStream());
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
						final BodyPart bodyPart = mp.getBodyPart(i);
						dumpPart(bodyPart, msgHandler, prefix, partCount++);
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
						final BodyPart bodyPart = mp.getBodyPart(i);
						/*
						 * Do not increase part count for one vcard
						 */
						dumpPart(bodyPart, msgHandler, prefix, partCount++);
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
					dumpPart(bodyPart, msgHandler, prefix, partCount++);
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
						bodyPart.setHeader(HEADER_CONTENT_TYPE, MIME_TEXT_RTF);
						bodyPart.setSize(content.length());
						dumpPart(bodyPart, msgHandler, prefix, partCount++);
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
						bodyPart.setHeader(HEADER_CONTENT_TYPE, contentTypeStr);
						os.reset();
						attachment.writeTo(os);
						bodyPart.setSize(os.size());
						dumpPart(bodyPart, msgHandler, prefix, partCount++);
					} else {
						/*
						 * Nested message
						 */
						final MimeMessage nestedMessage = TNEFMime.convert(session.getMailSession(), attachment
								.getNestedMessage());
						bodyPart.setDataHandler(new DataHandler(nestedMessage, MIME_MESSAGE_RFC822));
						bodyPart.setHeader(HEADER_CONTENT_TYPE, MIME_MESSAGE_RFC822);
						dumpPart(bodyPart, msgHandler, prefix, partCount++);
					}
				}
			} catch (final IOException tnefExc) {
				if (LOG.isWarnEnabled()) {
					LOG.warn(tnefExc.getLocalizedMessage(), tnefExc);
				}
				if (!msgHandler.handleAttachment(part, isInline, contentType.getBaseType(), filename, MessageUtils
						.getIdentifier(prefix, partCount))) {
					stop = true;
					return;
				}
			}
		} else if (!nestedMsgsOnly
				&& (contentType.isMimeType(MIME_MESSAGE_DELIVERY_STATUS)
						|| contentType.isMimeType(MIME_MESSAGE_DISP_NOTIFICATION)
						|| contentType.isMimeType(MIME_TEXT_RFC822_HDRS) || contentType.isMimeType(MIME_TEXT_ALL_CARD) || contentType
						.isMimeType(MIME_TEXT_ALL_CALENDAR))) {
			if (!msgHandler.handleSpecialPart(part, contentType.getBaseType(), MessageUtils.getIdentifier(prefix,
					partCount))) {
				stop = true;
				return;
			}
		} else if (!nestedMsgsOnly) {
			if (!msgHandler.handleAttachment(part, isInline, contentType.getBaseType(), filename, MessageUtils
					.getIdentifier(prefix, partCount))) {
				stop = true;
				return;
			}
		}
	}

	private static final String STR_CANNOT_LOAD_HEADER = "Cannot load header";

	private final void dumpEnvelope(final Message msg, final MessageHandler msgHandler) throws MessagingException,
			OXException {
		/*
		 * FROM
		 */
		msgHandler.handleFrom((InternetAddress[]) msg.getFrom());
		/*
		 * RECIPIENTS
		 */
		if (msg.getRecipients(Message.RecipientType.TO) != null) {
			msgHandler.handleRecipient(Message.RecipientType.TO, (InternetAddress[]) msg
					.getRecipients(Message.RecipientType.TO));
		}
		if (msg.getRecipients(Message.RecipientType.CC) != null) {
			msgHandler.handleRecipient(Message.RecipientType.CC, (InternetAddress[]) msg
					.getRecipients(Message.RecipientType.CC));
		}
		if (msg.getRecipients(Message.RecipientType.BCC) != null) {
			msgHandler.handleRecipient(Message.RecipientType.BCC, (InternetAddress[]) msg
					.getRecipients(Message.RecipientType.BCC));
		}
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
		final Flags flags = msg.getFlags();
		msgHandler.handleSystemFlags(flags.getSystemFlags());
		msgHandler.handleUserFlags(flags.getUserFlags());
		/*
		 * HEADERS
		 */
		Map<String, String> headerMap = null;
		try {
			headerMap = new HashMap<String, String>();
			for (final Enumeration e = msg.getAllHeaders(); e.hasMoreElements();) {
				final Header h = (Header) e.nextElement();
				headerMap.put(h.getName(), h.getValue());
			}
		} catch (final MessagingException e) {
			if (e.getMessage().indexOf(STR_CANNOT_LOAD_HEADER) != -1) {
				/*
				 * Headers could not be loaded
				 */
				try {
					headerMap = IMAPUtils.loadBrokenHeaders(msg, false);
				} catch (final ProtocolException e1) {
					LOG.error(e.getMessage(), e);
					headerMap = new HashMap<String, String>();
				}
			} else {
				headerMap = new HashMap<String, String>();
			}
		}
		msgHandler.handleHeaders(headerMap);
	}

}
