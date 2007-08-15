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
import static com.openexchange.groupware.container.mail.parser.MessageUtils.replaceHTMLSimpleQuotesForDisplay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.json.JSONException;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.mail.JSONMessageAttachmentObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.container.mail.parser.MessageDumper;
import com.openexchange.groupware.container.mail.parser.PartMessageHandler;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.AJAXUploadFile;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.MessageHeaders;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.UserSettingMail;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.imap.datasource.ByteArrayDataSource;
import com.openexchange.imap.datasource.MessageDataSource;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.Version;
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
public final class MessageFiller {

	/*
	 * Constants for MIME types
	 */
	private static final String MIME_TEXT = "text/";

	private static final String MIME_MESSAGE_RFC822 = "message/rfc822";

	private static final String MIME_TEXT_VCARD = "text/x-vcard";

	private static final String MIME_TEXT_PLAIN = "text/plain";

	private static final String MIME_TEXT_HTM = "text/htm";

	private static final String MIME_TEXT_HTML = "text/html";

	private static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";

	/*
	 * Constants for Multipart types
	 */
	private static final String MP_ALTERNATIVE = "alternative";

	private static final String MP_RELATED = "related";

	/*
	 * Other string constants
	 */
	private static final String ENC_Q = "Q";

	private static final String STR_UTF8 = "UTF-8";

	private static final String STR_EMPTY = "";

	private static final String STR_CHARSET = "charset";

	private static final String VERSION = "1.0";

	private static final String REPLACE_CS = "#CS#";

	private static final String PAT_TEXT_CT = "text/plain; charset=#CS#";

	private static final String PAT_HTML_CT = "text/html; charset=#CS#";

	private static final String VCARD_ERROR = "Error while appending user VCard";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageFiller.class);

	private static final Pattern PATTERN_EMBD_IMG = Pattern.compile("(<img.*src=\"cid:)([^\"]+)(\"[^/]*/?>)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private final Html2TextConverter converter;

	private final SessionObject session;

	private final UserSettingMail usm;

	private final Session mailSession;

	private final IMAPFolder destinationFolder;

	private final Message originalMsg;

	private final int linewrap;

	private Set<String> uploadFileIDs;

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
		this.usm = session.getUserSettingMail();
		this.originalMsg = originalMsg;
		this.mailSession = mailSession;
		this.destinationFolder = destinationFolder;
		this.linewrap = usm.getAutoLinebreak();
		converter = new Html2TextConverter();
	}

	/**
	 * Deletes referenced local uploaded files from session and disk after
	 * filled instance of <code>{@link Message}</code> is dispatched
	 */
	public void deleteReferencedUploadFiles() {
		if (uploadFileIDs != null) {
			final int size = uploadFileIDs.size();
			final Iterator<String> iter = uploadFileIDs.iterator();
			final StringBuilder sb;
			if (LOG.isInfoEnabled()) {
				sb = new StringBuilder(128);
			} else {
				sb = null;
			}
			for (int i = 0; i < size; i++) {
				final AJAXUploadFile uploadFile = session.removeAJAXUploadFile(iter.next());
				final String fileName = uploadFile.getFile().getName();
				uploadFile.delete();
				if (null != sb) {
					sb.setLength(0);
					LOG.info(sb.append("Upload file \"").append(fileName).append(
							"\" removed from session and deleted from disk"));
				}
			}
			uploadFileIDs.clear();
		}
	}

	public void fillMessage(final JSONMessageObject msgObj, final MimeMessage newMimeMessage,
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
			mailTextMao.setContentType(MIME_TEXT_HTML);
		} else {
			sendMultipartAlternative = (MP_ALTERNATIVE.equalsIgnoreCase(mailTextMao.getContentType()));
		}
		/*
		 * Html content with embedded images
		 */
		final boolean embeddedImages = (sendMultipartAlternative || (mailTextMao.getContentType().regionMatches(true,
				0, MIME_TEXT_HTM, 0, 8)))
				&& (hasEmbeddedImages((String) mailTextMao.getContent()) || hasReferencedLocalImages(
						(String) mailTextMao.getContent(), session));
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
				if (mailTextMao.getContentType().regionMatches(true, 0, MIME_TEXT_PLAIN, 0, 10)) {
					/*
					 * Convert html content to reguar text. First: Create a body
					 * part for text content
					 */
					final MimeBodyPart text = new MimeBodyPart();
					/*
					 * Define text content
					 */
					text.setText(performLineWrap(converter.convertWithQuotes(mailText), false, linewrap),
							IMAPProperties.getDefaultMimeCharset());
					text.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION);
					text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, PAT_TEXT_CT.replaceFirst(REPLACE_CS, IMAPProperties
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
					final String ct = PAT_HTML_CT.replaceFirst(REPLACE_CS, IMAPProperties.getDefaultMimeCharset());
					html.setContent(performLineWrap(insertColorQuotes(MailTools.formatHrefLinks(mailText)), true,
							linewrap), ct);
					html.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION);
					html.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct);
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
			AppendVCard: if (msgObj.isAppendVCard()) {
				final String fileName = MimeUtility.encodeText(new StringBuilder(session.getUserObject()
						.getDisplayName().replaceAll(" +", STR_EMPTY)).append(".vcf").toString(), IMAPProperties
						.getDefaultMimeCharset(), ENC_Q);
				if (msgObj.isDraft()) {
					for (int i = 1; i < size; i++) {
						final JSONMessageAttachmentObject mao = msgObj.getMsgAttachments().get(i);
						if (fileName.equalsIgnoreCase(mao.getFileName())) {
							/*
							 * VCard already attached in draft message
							 */
							break AppendVCard;
						}
					}

				}
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
					vcardPart.setDataHandler(new DataHandler(new MessageDataSource(userVCard, MIME_TEXT_VCARD)));
					vcardPart.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION);
					vcardPart.setFileName(fileName);
					/*
					 * Append body part
					 */
					primaryMultipart.addBodyPart(vcardPart);
				} catch (final MessagingException e) {
					LOG.error(VCARD_ERROR, e);
				} catch (final DBPoolingException e) {
					LOG.error(VCARD_ERROR, e);
				} catch (final IOException e) {
					LOG.error(VCARD_ERROR, e);
				} catch (final ConverterException e) {
					LOG.error(VCARD_ERROR, e);
				} catch (final OXException e) {
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
					origMsgPart.setDataHandler(new DataHandler(originalMsg, MIME_MESSAGE_RFC822));
					primaryMultipart.addBodyPart(origMsgPart);
				} catch (final MessagingException e) {
					LOG.error("Error while appending original message on forward", e);
				}
			}
		} else {
			/*
			 * Create a non-multipart message
			 */
			if (mailTextMao.getContentType().regionMatches(true, 0, MIME_TEXT, 0, 5)
					&& mailTextMao.getContent() != null) {
				final boolean isPlainText = (mailTextMao.getContentType()
						.regionMatches(true, 0, MIME_TEXT_PLAIN, 0, 10));
				final ContentType ct = new ContentType(isPlainText ? PAT_TEXT_CT.replaceFirst(REPLACE_CS,
						IMAPProperties.getDefaultMimeCharset()) : PAT_HTML_CT.replaceFirst(REPLACE_CS, IMAPProperties
						.getDefaultMimeCharset()));
				if (primaryMultipart == null) {
					final String mailText;
					if (isPlainText) {
						/*
						 * Convert html content to reguar text
						 */
						mailText = performLineWrap(converter.convertWithQuotes((String) mailTextMao.getContent()),
								false, linewrap);
					} else {
						mailText = performLineWrap(insertColorQuotes(MailTools.formatHrefLinks((String) mailTextMao
								.getContent())), true, linewrap);
					}
					newMimeMessage.setContent(mailText, ct.toString());
					newMimeMessage.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION);
					newMimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
				} else {
					final MimeBodyPart msgBodyPart = new MimeBodyPart();
					msgBodyPart.setContent(mailTextMao.getContent(), ct.toString());
					msgBodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION);
					msgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
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
				msgBodyPart.setText(STR_EMPTY, STR_UTF8);
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
				msgBodyPart.setContent(nestedMsg, MIME_MESSAGE_RFC822);
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
	private Multipart createMultipartAlternative(final JSONMessageAttachmentObject mailTextMao,
			final boolean embeddedImages, final JSONMessageObject msgObj) throws OXException, MessagingException {
		/*
		 * Create an "alternative" multipart
		 */
		final Multipart alternativeMultipart = new MimeMultipart(MP_ALTERNATIVE);
		final String mailText = (String) mailTextMao.getContent();
		/*
		 * Create a body part for both text and html content
		 */
		final MimeBodyPart text = new MimeBodyPart();
		/*
		 * Define & add text content
		 */
		try {
			text.setText(performLineWrap(converter.convertWithQuotes(mailText), false, linewrap), IMAPProperties
					.getDefaultMimeCharset());
		} catch (final IOException e) {
			throw new OXMailException(MailCode.HTML2TEXT_CONVERTER_ERROR, e, e.getMessage());
		}
		text.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION);
		text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, PAT_TEXT_CT.replaceFirst(REPLACE_CS, IMAPProperties
				.getDefaultMimeCharset()));
		alternativeMultipart.addBodyPart(text);
		/*
		 * Define html content
		 */
		if (embeddedImages) {
			/*
			 * Create "related" multipart
			 */
			final Multipart relatedMultipart = new MimeMultipart(MP_RELATED);
			/*
			 * Process referenced local image files and insert returned html
			 * content as a new body part to first index
			 */
			relatedMultipart.addBodyPart(createHtmlBodyPart(processReferencedLocalImages(mailText, relatedMultipart,
					this), linewrap), 0);
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
				final Part imgPart = getAndRemoveImageAttachment(cid, msgObj, session, originalMsg);
				if (imgPart == null) {
					continue NextImg;
				}
				/*
				 * Create new body part from part's data handler
				 */
				final BodyPart relatedImageBodyPart = new MimeBodyPart();
				relatedImageBodyPart.setDataHandler(imgPart.getDataHandler());
				for (final Enumeration e = imgPart.getAllHeaders(); e.hasMoreElements();) {
					final Header h = (Header) e.nextElement();
					relatedImageBodyPart.setHeader(h.getName(), h.getValue());
				}
				/*
				 * Add image to "related" multipart
				 */
				relatedMultipart.addBodyPart(relatedImageBodyPart);
			}
			/*
			 * Add multipart/related as a body part to superior multipart
			 */
			final BodyPart altBodyPart = new MimeBodyPart();
			altBodyPart.setContent(relatedMultipart);
			alternativeMultipart.addBodyPart(altBodyPart);
		} else {
			final BodyPart html = createHtmlBodyPart(mailText, linewrap);
			/*
			 * Add html part to superior multipart
			 */
			alternativeMultipart.addBodyPart(html);
		}
		return alternativeMultipart;
	}

	private static BodyPart createHtmlBodyPart(final String htmlContent, final int linewrap) throws IMAPException,
			MessagingException {
		final String htmlCT = PAT_HTML_CT.replaceFirst(REPLACE_CS, IMAPProperties.getDefaultMimeCharset());
		final MimeBodyPart html = new MimeBodyPart();
		html.setContent(performLineWrap(insertColorQuotes(MailTools.formatHrefLinks(htmlContent)), true, linewrap),
				htmlCT);
		html.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION);
		html.setHeader(MessageHeaders.HDR_CONTENT_TYPE, htmlCT);
		return html;
	}

	private void addMessageBodyPart(final Multipart mp, final JSONMessageObject msgObj,
			final JSONMessageAttachmentObject mao) throws MessagingException, OXException, IOException {
		if (mao.getContentType().regionMatches(true, 0, MIME_TEXT, 0, 5) && mao.getContent() != null) {
			/*
			 * Text
			 */
			final ContentType ct = new ContentType(mao.getContentType());
			if (ct.getParameter(STR_CHARSET) == null) {
				ct.setParameter(STR_CHARSET, IMAPProperties.getDefaultMimeCharset());
			}
			final String contentType = ct.toString();
			final MimeBodyPart msgBodyPart = new MimeBodyPart();
			msgBodyPart.setContent(mao.getContent(), contentType == null ? mao.getContentType() : contentType);
			mp.addBodyPart(msgBodyPart);
		} else {
			final ContentType contentType = new ContentType(mao.getContentType());
			if (MIME_APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentType.getBaseType()) && mao.getFileName() != null) {
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
								.getDefaultMimeCharset(), ENC_Q));
					} catch (final UnsupportedEncodingException e) {
						LOG.error("Unsupported encoding in a message detected and monitored.", e);
						MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
						msgBodyPart.setFileName(mao.getFileName());
					} catch (final IMAPException e) {
						msgBodyPart.setFileName(mao.getFileName());
					}
				} else {
					final byte[] bytes = (byte[]) mao.getContent();
					msgBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(contentType.toString(), mao
							.getFileName(), bytes)));
					try {
						msgBodyPart.setFileName(MimeUtility.encodeText(mao.getFileName(), IMAPProperties
								.getDefaultMimeCharset(), ENC_Q));
					} catch (final UnsupportedEncodingException e) {
						msgBodyPart.setFileName(mao.getFileName());
					} catch (final IMAPException e) {
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
							.getDefaultMimeCharset(), ENC_Q));
				} catch (final UnsupportedEncodingException e) {
					messageBodyPart.setFileName(mao.getFileName());
				} catch (final IMAPException e) {
					messageBodyPart.setFileName(mao.getFileName());
				}
				messageBodyPart.setDisposition(mao.getDisposition() == null ? Part.ATTACHMENT : mao.getDisposition());
				messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, mao.getContentType());
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
				} catch (final IOException e) {
					throw new OXMailException(MailCode.INTERNAL_ERROR, e.getMessage());
				}
				messageBodyPart.setDataHandler(new DataHandler(dataSource));
				try {
					messageBodyPart.setFileName(MimeUtility.encodeText(mao.getFileName(), IMAPProperties
							.getDefaultMimeCharset(), ENC_Q));
				} catch (final UnsupportedEncodingException e) {
					messageBodyPart.setFileName(mao.getFileName());
				} catch (final IMAPException e) {
					messageBodyPart.setFileName(mao.getFileName());
				}
				messageBodyPart.setDisposition(mao.getDisposition() == null ? Part.ATTACHMENT : mao.getDisposition());
				messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, mao.getContentType());
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

	private void setMessageHeaders(final MimeMessage msg, final JSONMessageObject msgObj) throws MessagingException,
			OXException {
		/*
		 * Set from
		 */
		if (msgObj.getFrom() != null) {
			msg.addFrom(msgObj.getFromAsArray());
		}
		/*
		 * Set to
		 */
		if (msgObj.getTo() != null) {
			msg.addRecipients(Message.RecipientType.TO, msgObj.getToAsArray());
		}
		/*
		 * Set cc
		 */
		if (msgObj.getCc() != null) {
			msg.addRecipients(Message.RecipientType.CC, msgObj.getCcAsArray());
		}
		/*
		 * Bcc
		 */
		if (msgObj.getBcc() != null) {
			msg.addRecipients(Message.RecipientType.BCC, msgObj.getBccAsArray());
		}
		/*
		 * Set subject
		 */
		if (msgObj.getSubject() != null) {
			try {
				msg.setSubject(MimeUtility.encodeText(removeHdrLineBreak(msgObj.getSubject()), IMAPProperties
						.getDefaultMimeCharset(), ENC_Q));
			} catch (final UnsupportedEncodingException e) {
				LOG.error("Unsupported encoding in a message detected and monitored.", e);
				MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
				msg.setSubject(removeHdrLineBreak(msgObj.getSubject()));
			}
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
				&& (destinationFolder == null || session.getCachedUserFlags(destinationFolder, true))) {
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
			msg.setHeader(MessageHeaders.HDR_DISP_TO, msgObj.getDispositionNotification());
		}
		/*
		 * Set priority
		 */
		msg.setHeader(MessageHeaders.HDR_X_PRIORITY, String.valueOf(msgObj.getPriority()));
		/*
		 * Set mailer
		 */
		msgObj.addHeader(MessageHeaders.HDR_X_MAILER, "Open-Xchange Mailer v" + Version.VERSION_STRING);
		/*
		 * Set organization to context-admin's company field setting
		 */
		try {
			/*
			 * Get context's admin contact object
			 */
			final ContactObject c = new RdbContactSQLInterface(session).getObjectById(UserStorage.getInstance(
					session.getContext()).getUser(session.getContext().getMailadmin()).getContactId(),
					FolderObject.SYSTEM_LDAP_FOLDER_ID);
			if (null != c && c.getCompany() != null && c.getCompany().length() > 0) {
				msg.setHeader(MessageHeaders.HDR_ORGANIZATION, c.getCompany());
			}
		} catch (final Throwable t) {
			LOG.warn("Header \"Organization\" could not be set", t);
		}
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

	private String getUserVCard() throws DBPoolingException, IOException, ConverterException, OXException {
		final User userObj = session.getUserObject();
		final OXContainerConverter converter = new OXContainerConverter(session);
		Connection readCon = null;
		try {
			readCon = DBPool.pickup(session.getContext());
			ContactObject contactObj = null;
			try {
				contactObj = Contacts.getContactById(userObj.getContactId(), userObj.getId(), userObj.getGroups(),
						session.getContext(), session.getUserConfiguration(), readCon);
			} catch (final OXException oxExc) {
				throw oxExc;
			} catch (final Exception e) {
				throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
			final VersitObject versitObj = converter.convertContact(contactObj, "2.1");
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final VersitDefinition def = Versit.getDefinition(MIME_TEXT_VCARD);
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

	/**
	 * <p>
	 * Turns simple quotes ("> " or "&amp;gt; ") into html blockquotes when sending
	 * messages. Thus other mail clients are able to properly display quotes.
	 * </p>
	 * <p>
	 * Thus other mail client which receive html content are able to properly
	 * display html blockquotes
	 * </p>
	 */
	private static String insertColorQuotes(final String s) {
		return replaceHTMLSimpleQuotesForDisplay(s);
	}

	// private static final Pattern PATTERN_REF_IMG = Pattern.compile(
	// "(<img[^/]*?)(src=\")([^\"]+)(\"[^/]*?oxfile=\")([^\"]+)(\"[^/]*/?>)",
	// Pattern.CASE_INSENSITIVE
	// | Pattern.DOTALL);

	private static final Pattern PATTERN_REF_IMG = Pattern.compile(
			"(<img[^/>]*?)(src=\")([^\"]+)(id=)([^\"&]+)(?:(&[^\"]+\")|(\"))([^/>]*/?>)", Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);

	/**
	 * Detects if given html content contains references to local image files
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * &lt;img src=&quot;cid:s345asd845@12drg&quot;&gt;
	 * </pre>
	 * 
	 * @param htmlContent
	 *            The html content
	 * @param session
	 *            The user session
	 * @return <code>true</code> if given html content contains references to
	 *         local image files; otherwise <code>false</code>
	 */
	public static boolean hasReferencedLocalImages(final String htmlContent, final SessionObject session) {
		final Matcher m = PATTERN_REF_IMG.matcher(htmlContent);
		while (m.find()) {
			if (session.touchAJAXUploadFile(m.group(5))) {
				return true;
			}
		}
		return false;
	}

	private static final String IMG_PAT = "<img src=\"cid:#1#\">";

	/**
	 * Processes referenced local images, inserts them as inlined html images
	 * and adds their binary data to parental instance of
	 * <code>{@link Multipart}</code>
	 * 
	 * @param htmlContent
	 *            The html content whose &lt;img&gt; tags must be replaced with
	 *            real content ids
	 * @param mp
	 *            The parental instance of <code>{@link Multipart}</code>
	 * @param msgFiller
	 *            The message filler
	 * @return the replaced html content
	 * @throws MessagingException
	 *             If appending as body part fails
	 */
	private static String processReferencedLocalImages(final String htmlContent, final Multipart mp,
			final MessageFiller msgFiller) throws MessagingException {
		final StringBuffer sb = new StringBuffer(htmlContent.length());
		final Matcher m = PATTERN_REF_IMG.matcher(htmlContent);
		if (m.find()) {
			msgFiller.uploadFileIDs = new HashSet<String>();
			final StringBuilder tmp = new StringBuilder(128);
			NextImg: do {
				final String id = m.group(5);
				final AJAXUploadFile uploadFile = msgFiller.session.getAJAXUploadFile(id);
				if (uploadFile == null) {
					if (LOG.isWarnEnabled()) {
						tmp.setLength(0);
						LOG.warn(tmp.append("No upload file found with id \"").append(id).append(
								"\". Referenced image is skipped.").toString());
					}
					continue NextImg;
				}
				final boolean appendBodyPart;
				if (!msgFiller.uploadFileIDs.contains(id)) {
					/*
					 * Remember id to avoid duplicate attachment and for later
					 * cleanup
					 */
					msgFiller.uploadFileIDs.add(id);
					appendBodyPart = true;
				} else {
					appendBodyPart = false;
				}
				/*
				 * Replace image tag
				 */
				m.appendReplacement(sb, IMG_PAT.replaceFirst("#1#", processLocalImage(uploadFile, id, appendBodyPart,
						tmp, mp)));
			} while (m.find());
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Processes a local image and returns its content id
	 * 
	 * @param uploadFile
	 *            The uploaded file
	 * @param id
	 *            uploaded file's ID
	 * @param appendBodyPart
	 * @param tmp
	 *            An instance of {@link StringBuilder}
	 * @param mp
	 *            The parental instance of {@link Multipart}
	 * @return the content id
	 * @throws MessagingException
	 *             If appending as body part fails
	 */
	private static String processLocalImage(final AJAXUploadFile uploadFile, final String id,
			final boolean appendBodyPart, final StringBuilder tmp, final Multipart mp) throws MessagingException {
		/*
		 * Determine filename
		 */
		String fileName;
		try {
			fileName = MimeUtility.encodeText(uploadFile.getFileName(), IMAPProperties.getDefaultMimeCharset(), ENC_Q);
		} catch (final UnsupportedEncodingException e) {
			fileName = uploadFile.getFileName();
		} catch (final IMAPException e) {
			fileName = uploadFile.getFileName();
		}
		/*
		 * ... and cid
		 */
		tmp.setLength(0);
		tmp.append(fileName).append('@').append(id);
		final String cid = tmp.toString();
		if (appendBodyPart) {
			/*
			 * Append body part
			 */
			final MimeBodyPart imgBodyPart = new MimeBodyPart();
			imgBodyPart.setDataHandler(new DataHandler(new FileDataSource(uploadFile.getFile())));
			imgBodyPart.setFileName(fileName);
			tmp.setLength(0);
			imgBodyPart.setContentID(tmp.append('<').append(cid).append('>').toString());
			imgBodyPart.setDisposition(Part.INLINE);
			imgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, uploadFile.getContentType());
			mp.addBodyPart(imgBodyPart);
		}
		return cid;
	}

	/**
	 * Detects if given html content contains inlined images
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * &lt;img src=&quot;cid:s345asd845@12drg&quot;&gt;
	 * </pre>
	 * 
	 * @param htmlContent
	 *            The html content
	 * @return <code>true</code> if given html content contains inlined
	 *         images; otherwise <code>false</code>
	 */
	public static boolean hasEmbeddedImages(final String htmlContent) {
		return PATTERN_EMBD_IMG.matcher(htmlContent).find();
	}

	/**
	 * Gathers all occuring content IDs in html content and returns them as a
	 * list
	 * 
	 * @param htmlContent
	 *            The html content
	 * @return an instance of <code>{@link List}</code> containing all
	 *         occuring content IDs
	 */
	public static List<String> getContentIDs(final String htmlContent) {
		final List<String> retval = new ArrayList<String>();
		final Matcher m = PATTERN_EMBD_IMG.matcher(htmlContent);
		while (m.find()) {
			retval.add(m.group(2));
		}
		return retval;
	}

	private static Part getAndRemoveImageAttachment(final String cid, final JSONMessageObject msgObj,
			final SessionObject session, final Message originalMsg) throws OXException, MessagingException {
		final List<JSONMessageAttachmentObject> attachList = msgObj.getMsgAttachments();
		final int size = attachList.size();
		final Iterator<JSONMessageAttachmentObject> iter = attachList.iterator();
		final MessageDumper dumper = new MessageDumper(session);
		PartMessageHandler msgHandler = null;
		for (int i = 0; i < size; i++) {
			final JSONMessageAttachmentObject mao = iter.next();
			if (mao.getUniqueDiskFileName() != null) {
				LOG.error("Inline image file from disk not supported,yet");
			} else if (msgObj.getMsgref() != null && mao.getFileName() != null && mao.getPositionInMail() != null) {
				dumper.reset();
				dumper.dumpMessage(originalMsg, msgHandler == null ? (msgHandler = new PartMessageHandler(session, mao
						.getPositionInMail())) : msgHandler.reset(mao.getPositionInMail()));
				final Part msgPart = msgHandler.getPart();
				String[] contentId = null;
				if (msgPart != null && (contentId = msgPart.getHeader(MessageHeaders.HDR_CONTENT_ID)) != null
						&& equalsCID(cid, contentId[0])) {
					iter.remove();
					return msgPart;
				}
			}
		}
		return null;
	}

	/**
	 * Compares the given vaslues of message header "Content-ID"
	 * 
	 * @param contentId1Arg
	 *            The first content ID
	 * @param contentId2Arg
	 *            The second content ID
	 * @return <code>true</code> if both are equal; otherwise
	 *         <code>false</code>
	 */
	public static boolean equalsCID(final String contentId1Arg, final String contentId2Arg) {
		if (null != contentId1Arg && null != contentId2Arg) {
			final String contentId1 = contentId1Arg.charAt(0) == '<' ? contentId1Arg.substring(1, contentId1Arg
					.length() - 1) : contentId1Arg;
			final String contentId2 = contentId2Arg.charAt(0) == '<' ? contentId2Arg.substring(1, contentId2Arg
					.length() - 1) : contentId2Arg;
			return contentId1.equals(contentId2);
		}
		return false;
	}
}
