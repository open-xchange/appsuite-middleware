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

package com.openexchange.groupware.container.mail.filler;

import static com.openexchange.groupware.container.mail.parser.MessageUtils.performLineWrap;
import static com.openexchange.groupware.container.mail.parser.MessageUtils.removeHdrLineBreak;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.json.JSONException;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.mail.JSONMessageAttachmentObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.container.mail.parser.MessageDumper;
import com.openexchange.groupware.container.mail.parser.PartMessageHandler;
import com.openexchange.groupware.imap.ByteArrayDataSource;
import com.openexchange.groupware.imap.IMAPException;
import com.openexchange.groupware.imap.IMAPProperties;
import com.openexchange.groupware.imap.IMAPUtils;
import com.openexchange.groupware.imap.MessageDataSource;
import com.openexchange.groupware.imap.OXMailException;
import com.openexchange.groupware.imap.UserSettingMail;
import com.openexchange.groupware.imap.OXMailException.MailCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.Html2TextConverter;
import com.openexchange.tools.mail.MailTools;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.sun.mail.imap.IMAPFolder;

/**
 * Fills a <code>MimeMessage</code> instance from underlying
 * <code>JSONMessageObject</code> object.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessageFiller {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageFiller.class);

	private static final Html2TextConverter CONVERTER = new Html2TextConverter();

	private static final String DISP_TO = "Disposition-Notification-To";

	private static final String CHARSET = "charset";

	private static final String MIME_VERSION = "MIME-Version";

	private static final String CONTENT_TYPE = "Content-Type";

	private static final String VERSION = "1.0";

	private static final String REPLACE_CS = "#CS#";

	private static final String TEXT_CT = "text/plain; charset=#CS#";

	private static final String HTML_CT = "text/html; charset=#CS#";

	private static final String VCARD_ERROR = "Error while appending user VCard";

	private static final Pattern PATTERN_EMBD_IMG = Pattern.compile("(<img *src=\"cid:)(.+?)(\" */?>)",
			Pattern.CASE_INSENSITIVE);

	private final SessionObject session;

	private final UserSettingMail usm;

	private final Session mailSession;

	private final IMAPFolder destinationFolder;

	private final Message originalMsg;

	private final int linewrap;

	/**
	 * Creates a new instance of <code>MessageFiller</code>
	 * 
	 * @param session -
	 *            the groupware session
	 * @param originalMsg -
	 *            the original message on reply, forward or draft-edit
	 * @param mailSession -
	 *            the mail session
	 * @param destinationFolder -
	 *            the destination folder; may be <code>null</code>
	 */
	public MessageFiller(final SessionObject session, final Message originalMsg, final Session mailSession,
			final IMAPFolder destinationFolder) {
		super();
		this.session = session;
		this.usm = session.getUserConfiguration().getUserSettingMail();
		this.originalMsg = originalMsg;
		this.mailSession = mailSession;
		this.destinationFolder = destinationFolder;
		this.linewrap = session.getUserConfiguration().getUserSettingMail().getAutoLinebreak();
	}

	public void close() {
		// if (origMsgFolder != null && origMsgFolder.isOpen()) {
		// origMsgFolder.close(false);
		// }
	}

	public final void fillMessage(final JSONMessageObject msgObj, final MimeMessage newMimeMessage,
			final UploadEvent uploadEvent, final int sendType) throws IOException, MessagingException, OXException,
			JSONException {
		/*
		 * Set headers
		 */
		setMessageHeaders(newMimeMessage, msgObj);
		/*
		 * Store some flags
		 */
		final boolean hasNestedMessages = (msgObj.getNestedMsgs().size() > 0);
		final boolean hasAttachments = (msgObj.getMsgAttachments().size() > 1);
		/*
		 * A non-inline forward message
		 */
		final boolean isNonInlineForward = (msgObj.getMsgref() != null
				&& (sendType == MailInterfaceImpl.SENDTYPE_FORWARD) && usm.isForwardAsAttachment());
		/*
		 * Initialize primary multipart
		 */
		Multipart primaryMultipart = null;
		/*
		 * Detect if primary multipart is of type multipart/mixed
		 */
		if (hasNestedMessages || hasAttachments || msgObj.isAppendVCard() || isNonInlineForward) {
			primaryMultipart = new MimeMultipart();
		}
		if (msgObj.getMsgref() == null) {
			/*
			 * Sort attachments if a new message should be sent
			 */
			Collections.sort(msgObj.getMsgAttachments(), JSONMessageAttachmentObject.getAttachmentComparator());
		}
		final JSONMessageAttachmentObject mailTextMao = msgObj.getMsgAttachments().get(0);
		/*
		 * Content is expected to be multipart/alternative
		 */
		final boolean sendMultipartAlternative;
		if (msgObj.isDraft()) {
			sendMultipartAlternative = false;
			mailTextMao.setContentType("text/html");
		} else {
			sendMultipartAlternative = ("alternative".equalsIgnoreCase(mailTextMao.getContentType()));
		}
		/*
		 * Html content with embedded images
		 */
		final boolean embeddedImages = (sendMultipartAlternative || (mailTextMao.getContentType().regionMatches(true,
				0, "text/htm", 0, 8)))
				&& hasEmbeddedImages((String) mailTextMao.getContent());
		/*
		 * Compose message
		 */
		if (hasAttachments || sendMultipartAlternative || isNonInlineForward || msgObj.isAppendVCard()
				|| embeddedImages) {
			/*
			 * If any condition is true, we ought to create a multipart/*
			 * message
			 */
			if (sendMultipartAlternative || embeddedImages) {
				final Multipart alternativeMultipart = createMultipartAlternative(mailTextMao, embeddedImages, msgObj);
				if (primaryMultipart == null) {
					primaryMultipart = alternativeMultipart;
				} else {
					final BodyPart bodyPart = new MimeBodyPart();
					bodyPart.setContent(alternativeMultipart);
					primaryMultipart.addBodyPart(bodyPart);
				}
			} else {
				if (primaryMultipart == null) {
					primaryMultipart = new MimeMultipart();
				}
				final String mailText = (String) mailTextMao.getContent();
				/*
				 * Convert html content to regular text if mail text is demanded
				 * to be text/plain
				 */
				if (mailTextMao.getContentType().regionMatches(true, 0, "text/plain", 0, 10)) {
					/*
					 * Convert html content to reguar text. First: Create a body
					 * part for text content
					 */
					final MimeBodyPart text = new MimeBodyPart();
					/*
					 * Define text content
					 */
					text.setText(performLineWrap(CONVERTER.convertWithQuotes(mailText), false, linewrap),
							IMAPProperties.getDefaultMimeCharset());
					text.setHeader(MIME_VERSION, VERSION);
					text.setHeader(CONTENT_TYPE, TEXT_CT.replaceFirst(REPLACE_CS, IMAPProperties
							.getDefaultMimeCharset()));
					/*
					 * Add body part
					 */
					primaryMultipart.addBodyPart(text);
				} else {
					/*
					 * Append html content
					 */
					final MimeBodyPart html = new MimeBodyPart();
					/*
					 * Define html content
					 */
					final String ct = HTML_CT.replaceFirst(REPLACE_CS, IMAPProperties.getDefaultMimeCharset());
					html.setContent(performLineWrap(insertColorQuotes(MailTools.formatHrefLinks(mailText)), true, linewrap), ct);
					html.setHeader(MIME_VERSION, VERSION);
					html.setHeader(CONTENT_TYPE, ct);
					/*
					 * Add body part
					 */
					primaryMultipart.addBodyPart(html);
				}
			}
			final int size = msgObj.getMsgAttachments().size();
			for (int i = 1; i < size; i++) {
				addMessageBodyPart(primaryMultipart, msgObj, msgObj.getMsgAttachments().get(i));
			}
			/*
			 * Append VCard
			 */
			if (msgObj.isAppendVCard()) {
				if (primaryMultipart == null) {
					primaryMultipart = new MimeMultipart();
				}
				try {
					final String userVCard = getUserVCard();
					/*
					 * Create a body part for vcard
					 */
					final MimeBodyPart vcardPart = new MimeBodyPart();
					/*
					 * Define content
					 */
					vcardPart.setDataHandler(new DataHandler(new MessageDataSource(userVCard, "text/vcard")));
					vcardPart.setHeader(MIME_VERSION, VERSION);
					vcardPart.setFileName(MimeUtility.encodeText(new StringBuilder(session.getUserObject()
							.getDisplayName().replaceAll(" +", "")).append(".vcf").toString(), IMAPProperties
							.getDefaultMimeCharset(), "Q"));
					/*
					 * Append body part
					 */
					primaryMultipart.addBodyPart(vcardPart);
				} catch (MessagingException e) {
					LOG.error(VCARD_ERROR, e);
				} catch (DBPoolingException e) {
					LOG.error(VCARD_ERROR, e);
				} catch (IOException e) {
					LOG.error(VCARD_ERROR, e);
				} catch (ConverterException e) {
					LOG.error(VCARD_ERROR, e);
				} catch (OXException e) {
					LOG.error(VCARD_ERROR, e);
				}
			}
			/*
			 * Append original message
			 */
			if (isNonInlineForward) {
				if (primaryMultipart == null) {
					primaryMultipart = new MimeMultipart();
				}
				try {
					/*
					 * Create a body part for original message
					 */
					final MimeBodyPart origMsgPart = new MimeBodyPart();
					origMsgPart.setDataHandler(new DataHandler(originalMsg, "message/rfc822"));
					primaryMultipart.addBodyPart(origMsgPart);
				} catch (MessagingException e) {
					LOG.error("Error while appending original message on forward", e);
				}
			}
		} else {
			/*
			 * Create a non-multipart message
			 */
			if (mailTextMao.getContentType().regionMatches(true, 0, "text/", 0, 5) && mailTextMao.getContent() != null) {
				final boolean isPlainText = (mailTextMao.getContentType().regionMatches(true, 0, "text/plain", 0, 10));
				final ContentType ct = new ContentType(isPlainText ? TEXT_CT.replaceFirst(REPLACE_CS, IMAPProperties
						.getDefaultMimeCharset()) : HTML_CT.replaceFirst(REPLACE_CS, IMAPProperties
						.getDefaultMimeCharset()));
				if (primaryMultipart == null) {
					final String mailText;
					if (isPlainText) {
						/*
						 * Convert html content to reguar text
						 */
						mailText = performLineWrap(CONVERTER.convertWithQuotes((String) mailTextMao.getContent()),
								false, linewrap);
					} else {
						mailText = performLineWrap(insertColorQuotes(MailTools.formatHrefLinks((String) mailTextMao.getContent())), true, linewrap);
					}
					newMimeMessage.setContent(mailText, ct.toString());
					newMimeMessage.setHeader(MIME_VERSION, VERSION);
					newMimeMessage.setHeader(CONTENT_TYPE, ct.toString());
				} else {
					final MimeBodyPart msgBodyPart = new MimeBodyPart();
					msgBodyPart.setContent(mailTextMao.getContent(), ct.toString());
					msgBodyPart.setHeader(MIME_VERSION, VERSION);
					msgBodyPart.setHeader(CONTENT_TYPE, ct.toString());
					primaryMultipart.addBodyPart(msgBodyPart);
				}
			} else {
				if (mailTextMao.getContent() == null) {
					/*
					 * Should not happen: Invalid attachment
					 */
					throw new OXMailException(MailCode.INVALID_ATTACHMENT_ON_SEND, mailTextMao.getPositionInMail());
				}
				/*
				 * Inline element whose content is available
				 */
				Multipart mp = null;
				if (primaryMultipart == null) {
					primaryMultipart = mp = new MimeMultipart();
				} else {
					mp = primaryMultipart;
				}
				MimeBodyPart msgBodyPart = new MimeBodyPart();
				msgBodyPart.setText("", "UTF-8");
				mp.addBodyPart(msgBodyPart);
				msgBodyPart = new MimeBodyPart();
				if (mailTextMao.getContentID() == JSONMessageAttachmentObject.CONTENT_STRING) {
					msgBodyPart.setContent(mailTextMao.getContent(), mailTextMao.getContentType());
				} else if (mailTextMao.getContentID() == JSONMessageAttachmentObject.CONTENT_BYTE_ARRAY) {
					msgBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(mailTextMao.getContentType(),
							mailTextMao.getFileName(), ((byte[]) mailTextMao.getContent()))));
				} else if (mailTextMao.getContentID() == JSONMessageAttachmentObject.CONTENT_INPUT_STREAM) {
					msgBodyPart.setDataHandler(new DataHandler(new MessageDataSource(((InputStream) mailTextMao
							.getContent()), mailTextMao.getContentType(), mailTextMao.getFileName())));
				} else {
					/*
					 * Should not happen: Invalid attachment
					 */
					throw new OXMailException(MailCode.INVALID_ATTACHMENT_ON_SEND, mailTextMao.getPositionInMail());
				}
				mp.addBodyPart(msgBodyPart);
			}
		}
		if (hasNestedMessages) {
			if (primaryMultipart == null) {
				primaryMultipart = new MimeMultipart();
			}
			/*
			 * message/rfc822
			 */
			final int nestedMsgSize = msgObj.getNestedMsgs().size();
			final Iterator<JSONMessageObject> iter = msgObj.getNestedMsgs().iterator();
			for (int i = 0; i < nestedMsgSize; i++) {
				final JSONMessageObject nestedMsgObj = iter.next();
				final MimeMessage nestedMsg = new MimeMessage(mailSession);
				fillMessage(nestedMsgObj, nestedMsg, uploadEvent, sendType);
				final MimeBodyPart msgBodyPart = new MimeBodyPart();
				msgBodyPart.setContent(nestedMsg, "message/rfc822");
				primaryMultipart.addBodyPart(msgBodyPart);
			}
		}
		/*
		 * Finally set multipart
		 */
		if (primaryMultipart != null) {
			newMimeMessage.setContent(primaryMultipart);
		}
	}

	/**
	 * Creates a <code>javax.mail.Multipart</code> instance of MIME type
	 * multipart/alternative. If <code>embeddedImages</code> is
	 * <code>true</code> a sub-multipart of MIME type multipart/related is
	 * going to be appended as the "html" version
	 */
	private final Multipart createMultipartAlternative(final JSONMessageAttachmentObject mailTextMao,
			final boolean embeddedImages, final JSONMessageObject msgObj) throws OXException, MessagingException {
		/*
		 * Create an "alternative" multipart
		 */
		final Multipart alternativeMultipart = new MimeMultipart("alternative");
		final String mailText = (String) mailTextMao.getContent();
		/*
		 * Create a body part for both text and html content
		 */
		final MimeBodyPart text = new MimeBodyPart();
		/*
		 * Define & add text content
		 */
		try {
			text.setText(performLineWrap(CONVERTER.convertWithQuotes(mailText), false, linewrap), IMAPProperties
					.getDefaultMimeCharset());
		} catch (IOException e) {
			throw new OXMailException(MailCode.HTML2TEXT_CONVERTER_ERROR, e, e.getMessage());
		}
		text.setHeader(MIME_VERSION, VERSION);
		text.setHeader(CONTENT_TYPE, TEXT_CT.replaceFirst(REPLACE_CS, IMAPProperties.getDefaultMimeCharset()));
		alternativeMultipart.addBodyPart(text);
		/*
		 * Define html content
		 */
		String htmlCT = HTML_CT.replaceFirst(REPLACE_CS, IMAPProperties.getDefaultMimeCharset());
		final MimeBodyPart html = new MimeBodyPart();
		html.setContent(performLineWrap(insertColorQuotes(MailTools.formatHrefLinks(mailText)), true, linewrap), htmlCT);
		html.setHeader(MIME_VERSION, VERSION);
		html.setHeader(CONTENT_TYPE, htmlCT);
		htmlCT = null;
		/*
		 * Add to newly created "related" or existing superior multipart
		 */
		if (embeddedImages) {
			/*
			 * Create "related" multipart
			 */
			final Multipart relatedMultipart = new MimeMultipart("related");
			relatedMultipart.addBodyPart(html);
			/*
			 * Traverse Content-IDs
			 */
			final List<String> cidList = getContentIDs(mailText);
			final int size = cidList.size();
			final Iterator<String> cidIter = cidList.iterator();
			NextImg: for (int a = 0; a < size; a++) {
				final String cid = cidIter.next();
				/*
				 * Content-ID should match pattern: [filename]@[uid] (e.g.
				 * image001.png@1856FA34AE23C.org)
				 */
				final JSONMessageAttachmentObject imgAttach = getAndRemoveImageAttachment(cid.substring(0, cid
						.indexOf('@')), msgObj.getMsgAttachments());
				if (imgAttach == null) {
					continue NextImg;
				}
				final MimeBodyPart relatedImageBodyPart = new MimeBodyPart();
				final FileDataSource ifds = new FileDataSource(imgAttach.getUniqueDiskFileName());
				/*
				 * Add image to "related" multipart
				 */
				relatedImageBodyPart.setFileName(ifds.getName());
				relatedImageBodyPart.setText(ifds.getName());
				relatedImageBodyPart.setDataHandler(new DataHandler(ifds));
				relatedImageBodyPart.setHeader("Content-ID", new StringBuilder().append('<').append(cid).append('>')
						.toString());
				relatedImageBodyPart.setDisposition(Part.INLINE);
				relatedMultipart.addBodyPart(relatedImageBodyPart);
				final BodyPart altBodyPart = new MimeBodyPart();
				altBodyPart.setContent(relatedMultipart);
				alternativeMultipart.addBodyPart(altBodyPart);
			}
		} else {
			/*
			 * Add html part to superior multipart
			 */
			alternativeMultipart.addBodyPart(html);
		}
		return alternativeMultipart;
	}

	private final void addMessageBodyPart(final Multipart mp, final JSONMessageObject msgObj,
			final JSONMessageAttachmentObject mao) throws MessagingException, OXException, IOException {
		if (mao.getContentType().regionMatches(true, 0, "text/", 0, 5) && mao.getContent() != null) {
			/*
			 * Text
			 */
			final ContentType ct = new ContentType(mao.getContentType());
			if (ct.getParameter(CHARSET) == null) {
				ct.setParameter(CHARSET, IMAPProperties.getDefaultMimeCharset());
			}
			final String contentType = ct.toString();
			final MimeBodyPart msgBodyPart = new MimeBodyPart();
			msgBodyPart.setContent(mao.getContent(), contentType == null ? mao.getContentType() : contentType);
			mp.addBodyPart(msgBodyPart);
		} else {
			final ContentType contentType = new ContentType(mao.getContentType());
			if ("application/octet-stream".equalsIgnoreCase(contentType.getBaseType()) && mao.getFileName() != null) {
				/*
				 * Try to determine MIME type via JAF
				 */
				final String ct = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(mao.getFileName());
				final int pos = ct.indexOf('/');
				contentType.setPrimaryType(ct.substring(0, pos));
				contentType.setSubType(ct.substring(pos + 1));
			}
			if (mao.getContent() != null) {
				/*
				 * Attachment with available content
				 */
				final MimeBodyPart msgBodyPart = new MimeBodyPart();
				if (mao.getContentID() == JSONMessageAttachmentObject.CONTENT_STRING) {
					msgBodyPart.setDataHandler(new DataHandler(new MessageDataSource((String) mao.getContent(),
							contentType.toString())));
					try {
						msgBodyPart.setFileName(MimeUtility.encodeText(mao.getFileName(), IMAPProperties
								.getDefaultMimeCharset(), "Q"));
					} catch (UnsupportedEncodingException e) {
						msgBodyPart.setFileName(mao.getFileName());
					} catch (IMAPException e) {
						msgBodyPart.setFileName(mao.getFileName());
					}
				} else {
					final byte[] bytes = (byte[]) mao.getContent();
					msgBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(contentType.toString(), mao
							.getFileName(), bytes)));
					try {
						msgBodyPart.setFileName(MimeUtility.encodeText(mao.getFileName(), IMAPProperties
								.getDefaultMimeCharset(), "Q"));
					} catch (UnsupportedEncodingException e) {
						msgBodyPart.setFileName(mao.getFileName());
					} catch (IMAPException e) {
						msgBodyPart.setFileName(mao.getFileName());
					}
				}
				mp.addBodyPart(msgBodyPart);
			} else if (mao.getUniqueDiskFileName() != null) {
				/*
				 * Attachment element needs to be read from disk
				 */
				final File uploadedFile = mao.getUniqueDiskFileName();
				final MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setDataHandler(new DataHandler(new FileDataSource(uploadedFile)));
				try {
					messageBodyPart.setFileName(MimeUtility.encodeText(mao.getFileName(), IMAPProperties
							.getDefaultMimeCharset(), "Q"));
				} catch (UnsupportedEncodingException e) {
					messageBodyPart.setFileName(mao.getFileName());
				} catch (IMAPException e) {
					messageBodyPart.setFileName(mao.getFileName());
				}
				messageBodyPart.setDisposition(mao.getDisposition() == null ? Part.ATTACHMENT : mao.getDisposition());
				messageBodyPart.setHeader("Content-Type", mao.getContentType());
				mp.addBodyPart(messageBodyPart);
			} else if (mao.getInfostoreDocumentInputStream() != null) {
				/*
				 * Attachment element needs to be read from infostore
				 */
				final MimeBodyPart messageBodyPart = new MimeBodyPart();
				DataSource dataSource;
				try {
					dataSource = new MessageDataSource(mao.getInfostoreDocumentInputStream(), contentType.toString(),
							mao.getFileName());
				} catch (IOException e) {
					throw new OXMailException(MailCode.INTERNAL_ERROR, e.getMessage());
				}
				messageBodyPart.setDataHandler(new DataHandler(dataSource));
				try {
					messageBodyPart.setFileName(MimeUtility.encodeText(mao.getFileName(), IMAPProperties
							.getDefaultMimeCharset(), "Q"));
				} catch (UnsupportedEncodingException e) {
					messageBodyPart.setFileName(mao.getFileName());
				} catch (IMAPException e) {
					messageBodyPart.setFileName(mao.getFileName());
				}
				messageBodyPart.setDisposition(mao.getDisposition() == null ? Part.ATTACHMENT : mao.getDisposition());
				messageBodyPart.setHeader("Content-Type", mao.getContentType());
				mp.addBodyPart(messageBodyPart);
			} else if (msgObj.getMsgref() != null && mao.getFileName() != null && mao.getPositionInMail() != null) {
				/*
				 * "filename" & "id" is present: a reference to an attachment of
				 * the original mail.
				 */
				final PartMessageHandler msgHandler = new PartMessageHandler(session, mao.getPositionInMail());
				new MessageDumper(session).dumpMessage(originalMsg, msgHandler);
				final Part msgPart = msgHandler.getPart();
				if (msgPart != null) {
					/*
					 * Create new body part from part's data handler
					 */
					final BodyPart messageBodyPart = new MimeBodyPart();
					messageBodyPart.setDataHandler(msgPart.getDataHandler());
					messageBodyPart.setDescription(msgPart.getDescription());
					messageBodyPart.setDisposition(msgPart.getDisposition());
					messageBodyPart.setFileName(msgPart.getFileName());
					/*
					 * Finally, add to primary multipart
					 */
					mp.addBodyPart(messageBodyPart);
				}
			}
		}
	}

	private final void setMessageHeaders(final MimeMessage msg, final JSONMessageObject msgObj)
			throws MessagingException, UnsupportedEncodingException, OXException {
		/*
		 * Set from
		 */
		if (msgObj.getFrom() != null) {
			msg.addFrom(convertToAddressArray(msgObj.getFrom()));
		}
		/*
		 * Set to
		 */
		if (msgObj.getTo() != null) {
			msg.addRecipients(Message.RecipientType.TO, convertToAddressArray(msgObj.getTo()));
		}
		/*
		 * Set cc
		 */
		if (msgObj.getCc() != null) {
			msg.addRecipients(Message.RecipientType.CC, convertToAddressArray(msgObj.getCc()));
		}
		/*
		 * Bcc
		 */
		if (msgObj.getBcc() != null) {
			msg.addRecipients(Message.RecipientType.BCC, convertToAddressArray(msgObj.getBcc()));
		}
		/*
		 * Set subject
		 */
		if (msgObj.getSubject() != null) {
			msg.setSubject(MimeUtility.encodeText(removeHdrLineBreak(msgObj.getSubject()), IMAPProperties
					.getDefaultMimeCharset(), "Q"));
		}
		/*
		 * Set sent date
		 */
		if (msgObj.getSentDate() != null) {
			msg.setSentDate(msgObj.getSentDate());
		}
		/*
		 * Set flags
		 */
		final Flags msgFlags = new Flags();
		if (msgObj.isAnswered()) {
			msgFlags.add(Flags.Flag.ANSWERED);
		}
		if (msgObj.isDeleted()) {
			msgFlags.add(Flags.Flag.DELETED);
		}
		if (msgObj.isDraft()) {
			msgFlags.add(Flags.Flag.DRAFT);
		}
		if (msgObj.isFlagged()) {
			msgFlags.add(Flags.Flag.FLAGGED);
		}
		if (msgObj.isRecent()) {
			msgFlags.add(Flags.Flag.RECENT);
		}
		if (msgObj.isSeen()) {
			msgFlags.add(Flags.Flag.SEEN);
		}
		if (msgObj.isUser()) {
			msgFlags.add(Flags.Flag.USER);
		}
		if (IMAPProperties.isUserFlagsEnabled()
				&& (destinationFolder == null || IMAPUtils.supportsUserDefinedFlags(destinationFolder))) {
			/*
			 * Set user header: Color Label
			 */
			msgFlags.add(JSONMessageObject.getColorLabelStringValue(msgObj.getColorLabel()));
		}
		/*
		 * Finally, apply flags to message
		 */
		msg.setFlags(msgFlags, true);
		/*
		 * Set disposition notification
		 */
		if (msgObj.getDispositionNotification() != null && msgObj.getDispositionNotification().length() > 0) {
			msg.setHeader(DISP_TO, msgObj.getDispositionNotification());
		}
		/*
		 * Set priority
		 */
		msg.setHeader("X-Priority", String.valueOf(msgObj.getPriority()));
		/*
		 * Set mailer TODO: Read in mailer from file
		 */
		msgObj.addHeader("X-Mailer", "Open-Xchange v6.0 Mailer");
		/*
		 * Headers
		 */
		final int size = msgObj.getHeaders().size();
		final Iterator<Map.Entry<String, String>> iter = msgObj.getHeadersIterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, String> entry = iter.next();
			msg.addHeader(entry.getKey(), entry.getValue());
		}
	}

	private final Address[] convertToAddressArray(final List<InternetAddress> list) {
		final Address[] tmp = new Address[list.size()];
		list.toArray(tmp);
		return tmp;
	}

	private final String getUserVCard() throws DBPoolingException, IOException, ConverterException, OXException {
		final User userObj = session.getUserObject();
		final OXContainerConverter converter = new OXContainerConverter(session);
		Connection readCon = null;
		try {
			readCon = DBPool.pickup(session.getContext());
			ContactObject contactObj = null;
			try {
				contactObj = Contacts.getContactById(userObj.getContactId(), userObj.getId(), userObj.getGroups(),
						session.getContext(), session.getUserConfiguration(), readCon);
			} catch (OXException oxExc) {
				throw oxExc;
			} catch (Exception e) {
				throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
			final VersitObject versitObj = converter.convertContact(contactObj, "3.0");
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final VersitDefinition def = Versit.getDefinition("text/vcard");
			final VersitDefinition.Writer w = def.getWriter(os, IMAPProperties.getDefaultMimeCharset());
			def.write(w, versitObj);
			w.flush();
			os.flush();
			return new String(os.toByteArray(), IMAPProperties.getDefaultMimeCharset());
		} finally {
			if (readCon != null) {
				DBPool.closeReaderSilent(session.getContext(), readCon);
				readCon = null;
			}
			converter.close();
		}
	}

	private static final String BLOCKQUOTE_START_TMPL = "<blockquote type=\"cite\" style=\"color:%s; border-color:%s;\">\n";

	private static final String BLOCKQUOTE_END = "</blockquote>\n";

	private static final String HTML_BREAK = "<br>";

	private static final String DEFAULT_COLOR = "#0026ff";

	private static final String STR_HTML_QUOTE = "&gt;";

	private static final String[] COLORS;

	static {
		String[] tmp = null;
		try {
			tmp = IMAPProperties.getQuoteLineColors();
		} catch (OXException e) {
			tmp = new String[] { "#454545" };
		}
		COLORS = tmp;
	}

	/**
	 * <p>
	 * Turns simple quotes ("> " or "&gt; ") into html blockquotes when sending
	 * messages. Thus other mail clients are able to properly display quotes.
	 * </p>
	 * <p>
	 * Thus other mail client which receive html content are able to properly
	 * display html blockquotes
	 * </p>
	 */
	private static final String insertColorQuotes(final String s) {
		final StringBuilder sb = new StringBuilder();
		final String[] lines = s.split("<br/?>");
		int levelBefore = 0;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			int currentLevel = 0;
			int offset = 0;
			if (line.startsWith(STR_HTML_QUOTE)) {
				currentLevel++;
				offset = 4;
				int pos = -1;
				boolean next = true;
				while (next && (pos = line.indexOf(STR_HTML_QUOTE, offset)) > -1) {
					/*
					 * Continue only if next starting position is equal to
					 * offset or if just one whitespace character has been
					 * skipped
					 */
					next = (offset == pos || (pos - offset == 1 && s.charAt(offset) == ' '));
					if (next) {
						currentLevel++;
						offset = (pos + 4);
					}
				}
			}
			if (offset > 0) {
				try {
					offset = offset < line.length() && ' ' == line.charAt(offset) ? offset + 1 : offset;
				} catch (StringIndexOutOfBoundsException e) {
					if (LOG.isTraceEnabled()) {
						LOG.trace(e.getMessage(), e);
					}
				}
				line = line.substring(offset);
			}
			if (levelBefore < currentLevel) {
				for (; levelBefore < currentLevel; levelBefore++) {
					final String color = COLORS != null && COLORS.length > 0 ? (levelBefore >= COLORS.length ? COLORS[COLORS.length - 1]
							: COLORS[levelBefore])
							: DEFAULT_COLOR;
					sb.append(String.format(BLOCKQUOTE_START_TMPL, color, color));
				}
			} else if (levelBefore > currentLevel) {
				for (; levelBefore > currentLevel; levelBefore--) {
					sb.append(BLOCKQUOTE_END);
				}
			}
			sb.append(line).append(HTML_BREAK);
		}
		return sb.toString();
	}

	private static final boolean hasEmbeddedImages(final String htmlContent) {
		return PATTERN_EMBD_IMG.matcher(htmlContent).find();
	}

	private static final List<String> getContentIDs(final String htmlContent) {
		final List<String> retval = new ArrayList<String>();
		final Matcher m = PATTERN_EMBD_IMG.matcher(htmlContent);
		while (m.find()) {
			retval.add(m.group(2));
		}
		return retval;
	}

	private static final JSONMessageAttachmentObject getAndRemoveImageAttachment(final String fileName,
			final List<JSONMessageAttachmentObject> attachList) {
		final int size = attachList.size();
		final Iterator<JSONMessageAttachmentObject> iter = attachList.iterator();
		for (int i = 0; i < size; i++) {
			final JSONMessageAttachmentObject current = iter.next();
			if (fileName.equalsIgnoreCase(current.getFileName())) {
				iter.remove();
				return current;
			}
		}
		return null;
	}
}
