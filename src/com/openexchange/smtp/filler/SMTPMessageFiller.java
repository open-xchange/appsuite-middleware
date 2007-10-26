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

package com.openexchange.smtp.filler;

import static com.openexchange.mail.utils.MessageUtility.formatHrefLinks;
import static com.openexchange.mail.utils.MessageUtility.performLineFolding;
import static com.openexchange.mail.utils.MessageUtility.replaceHTMLSimpleQuotesForDisplay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.AJAXUploadFile;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MIMEMessageUtility;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.transport.SendType;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.DBPool;
import com.openexchange.server.Version;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.smtp.SMTPException;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.smtp.dataobjects.SMTPMailMessage;
import com.openexchange.tools.mail.Html2TextConverter;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.sun.mail.smtp.SMTPMessage;

/**
 * {@link SMTPMessageFiller} - Fills an instance of {@link SMTPMessage} with
 * headers/contents given through an instance of {@link SMTPMailMessage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SMTPMessageFiller {

	private static final String PARAM_CHARSET = "charset";

	private static final String VERSION_1_0 = "1.0";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SMTPMessageFiller.class);

	private static final String VCARD_ERROR = "Error while appending user VCard";

	/*
	 * Constants for Multipart types
	 */
	private static final String MP_ALTERNATIVE = "alternative";

	private static final String MP_RELATED = "related";

	/*
	 * Patterns for common MIME text types
	 */
	private static final String REPLACE_CS = "#CS#";

	private static final String PAT_TEXT_CT = "text/plain; charset=#CS#";

	private static final String PAT_HTML_CT = "text/html; charset=#CS#";

	/*
	 * Fields
	 */
	private final SessionObject session;

	private final UserSettingMail usm;

	private Set<String> uploadFileIDs;

	private Html2TextConverter converter;

	/**
	 * Constructor
	 * 
	 * @param session
	 *            The session
	 */
	public SMTPMessageFiller(final SessionObject session) {
		super();
		this.session = session;
		usm = session.getUserSettingMail();
	}

	private Html2TextConverter getConverter() {
		if (converter == null) {
			converter = new Html2TextConverter();
		}
		return converter;
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

	/**
	 * Fills given instance of {@link SMTPMessage}
	 * 
	 * @param mail
	 *            The source mail
	 * @param smtpMessage
	 *            The SMTP message to fill
	 * @throws MessagingException
	 * @throws MailException
	 * @throws IOException
	 */
	public void fillMail(final SMTPMailMessage mail, final SMTPMessage smtpMessage) throws MessagingException,
			MailException, IOException {
		fillMail(mail, smtpMessage, null, null);
	}

	/**
	 * Fills given instance of {@link SMTPMessage}
	 * 
	 * @param mail
	 *            The source mail
	 * @param smtpMessage
	 *            The SMTP message to fill
	 * @param sendType
	 *            The send type
	 * @param originalMail
	 *            The referenced mail (on forward/reply)
	 * @throws MessagingException
	 * @throws MailException
	 * @throws IOException
	 */
	public void fillMail(final SMTPMailMessage mail, final SMTPMessage smtpMessage, final SendType sendType,
			final MailMessage originalMail) throws MessagingException, MailException, IOException {
		/*
		 * Set headers
		 */
		setMessageHeaders(mail, smtpMessage);
		/*
		 * Store some flags
		 */
		// TODO: final boolean hasNestedMessages =
		// (msgObj.getNestedMsgs().size() > 0);
		final boolean hasNestedMessages = false;
		final boolean hasAttachments = mail.getEnclosedCount() > 0;
		/*
		 * A non-inline forward message
		 */
		final boolean isNonInlineForward = (mail.getMsgref() != null && (SendType.FORWARD.equals(sendType)) && usm
				.isForwardAsAttachment());
		/*
		 * Initialize primary multipart
		 */
		Multipart primaryMultipart = null;
		/*
		 * Detect if primary multipart is of type multipart/mixed
		 */
		if (hasNestedMessages || hasAttachments || mail.isAppendVCard() || isNonInlineForward) {
			primaryMultipart = new MimeMultipart();
		}
		/*
		 * Content is expected to be multipart/alternative
		 */
		final boolean sendMultipartAlternative;
		if (mail.isDraft()) {
			sendMultipartAlternative = false;
			mail.setContentType("text/html");
		} else {
			sendMultipartAlternative = mail.getContentType().isMimeType("multipart/alternative");
		}
		/*
		 * Html content with embedded images
		 */
		final boolean embeddedImages = (sendMultipartAlternative || (mail.getContentType().isMimeType("text/htm")))
				&& (MIMEMessageUtility.hasEmbeddedImages((String) mail.getContent()) || MIMEMessageUtility
						.hasReferencedLocalImages((String) mail.getContent(), session));
		/*
		 * Compose message
		 */
		if (hasAttachments || sendMultipartAlternative || isNonInlineForward || mail.isAppendVCard() || embeddedImages) {
			/*
			 * If any condition is true, we ought to create a multipart/*
			 * message
			 */
			if (sendMultipartAlternative || embeddedImages) {
				final Multipart alternativeMultipart = createMultipartAlternative(mail, embeddedImages);
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
				/*
				 * Convert html content to regular text if mail text is demanded
				 * to be text/plain
				 */
				if (mail.getContentType().isMimeType("text/plain")) {
					/*
					 * Convert html content to reguar text. First: Create a body
					 * part for text content
					 */
					final MimeBodyPart text = new MimeBodyPart();
					/*
					 * Define text content
					 */
					text.setText(performLineFolding(converter.convertWithQuotes((String) mail.getContent()), false, usm
							.getAutoLinebreak()), SMTPConfig.getDefaultMimeCharset());
					text.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
					text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, PAT_TEXT_CT.replaceFirst(REPLACE_CS, SMTPConfig
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
					final String ct = PAT_HTML_CT.replaceFirst(REPLACE_CS, SMTPConfig.getDefaultMimeCharset());
					html.setContent(performLineFolding(replaceHTMLSimpleQuotesForDisplay(formatHrefLinks((String) mail
							.getContent())), true, usm.getAutoLinebreak()), ct);
					html.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
					html.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct);
					/*
					 * Add body part
					 */
					primaryMultipart.addBodyPart(html);
				}
			}
			final int size = mail.getEnclosedCount();
			for (int i = 0; i < size; i++) {
				addMessageBodyPart(primaryMultipart, mail.getEnclosedMailPart(i), false);
			}
			/*
			 * Append VCard
			 */
			AppendVCard: if (mail.isAppendVCard()) {
				final String fileName = MimeUtility.encodeText(new StringBuilder(session.getUserObject()
						.getDisplayName().replaceAll(" +", "")).append(".vcf").toString(), SMTPConfig
						.getDefaultMimeCharset(), "Q");
				for (int i = 0; i < size; i++) {
					final MailPart part = mail.getEnclosedMailPart(i);
					if (fileName.equalsIgnoreCase(part.getFileName())) {
						/*
						 * VCard already attached in (former draft) message
						 */
						break AppendVCard;
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
					vcardPart.setDataHandler(new DataHandler(new MessageDataSource(userVCard, "text/x-vcard")));
					vcardPart.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
					vcardPart.setFileName(fileName);
					/*
					 * Append body part
					 */
					primaryMultipart.addBodyPart(vcardPart);
				} catch (final SMTPException e) {
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
					final DataSource dataSource;
					{
						final ByteArrayOutputStream out = new ByteArrayOutputStream();
						originalMail.writeTo(out);
						dataSource = new MessageDataSource(out.toByteArray(), "message/rfc822");
					}
					final MimeBodyPart origMsgPart = new MimeBodyPart();
					origMsgPart.setDataHandler(new DataHandler(dataSource));
					primaryMultipart.addBodyPart(origMsgPart);
				} catch (final MessagingException e) {
					LOG.error("Error while appending original message on forward", e);
				}
			}
			/*
			 * Finally set multipart
			 */
			if (primaryMultipart != null) {
				smtpMessage.setContent(primaryMultipart);
			}
			return;
		}
		/*
		 * Create a non-multipart message
		 */
		if (mail.getContentType().isMimeType("text/*")) {
			final boolean isPlainText = mail.getContentType().isMimeType("text/plain");
			if (mail.getContentType().getParameter(PARAM_CHARSET) == null) {
				mail.getContentType().setParameter(PARAM_CHARSET, SMTPConfig.getDefaultMimeCharset());
			}
			if (primaryMultipart == null) {
				final String mailText;
				if (isPlainText) {
					/*
					 * Convert html content to reguar text
					 */
					mailText = performLineFolding(converter.convertWithQuotes((String) mail.getContent()), false, usm
							.getAutoLinebreak());
				} else {
					mailText = performLineFolding(replaceHTMLSimpleQuotesForDisplay(formatHrefLinks((String) mail
							.getContent())), true, usm.getAutoLinebreak());
				}
				smtpMessage.setContent(mailText, mail.getContentType().toString());
				smtpMessage.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
				smtpMessage.setHeader(MessageHeaders.HDR_CONTENT_TYPE, mail.getContentType().toString());
			} else {
				final MimeBodyPart msgBodyPart = new MimeBodyPart();
				msgBodyPart.setContent(mail.getContent(), mail.getContentType().toString());
				msgBodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
				msgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, mail.getContentType().toString());
				primaryMultipart.addBodyPart(msgBodyPart);
			}
		} else {
			Multipart mp = null;
			if (primaryMultipart == null) {
				primaryMultipart = mp = new MimeMultipart();
			} else {
				mp = primaryMultipart;
			}
			final MimeBodyPart msgBodyPart = new MimeBodyPart();
			msgBodyPart.setText("", SMTPConfig.getDefaultMimeCharset());
			msgBodyPart.setDisposition(Part.INLINE);
			mp.addBodyPart(msgBodyPart);
			addMessageBodyPart(mp, mail, true);
		}
		/*
		 * if (hasNestedMessages) { if (primaryMultipart == null) {
		 * primaryMultipart = new MimeMultipart(); }
		 * 
		 * message/rfc822 final int nestedMsgSize =
		 * msgObj.getNestedMsgs().size(); final Iterator<JSONMessageObject>
		 * iter = msgObj.getNestedMsgs().iterator(); for (int i = 0; i <
		 * nestedMsgSize; i++) { final JSONMessageObject nestedMsgObj =
		 * iter.next(); final MimeMessage nestedMsg = new
		 * MimeMessage(mailSession); fillMessage(nestedMsgObj, nestedMsg,
		 * sendType); final MimeBodyPart msgBodyPart = new MimeBodyPart();
		 * msgBodyPart.setContent(nestedMsg, MIME_MESSAGE_RFC822);
		 * primaryMultipart.addBodyPart(msgBodyPart); } }
		 */
		/*
		 * Finally set multipart
		 */
		if (primaryMultipart != null) {
			smtpMessage.setContent(primaryMultipart);
		}
	}

	private String getUserVCard() throws SMTPException {
		final User userObj = session.getUserObject();
		final OXContainerConverter converter = new OXContainerConverter(session);
		Connection readCon = null;
		try {
			try {
				readCon = DBPool.pickup(session.getContext());
				ContactObject contactObj = null;
				try {
					contactObj = Contacts.getContactById(userObj.getContactId(), userObj.getId(), userObj.getGroups(),
							session.getContext(), session.getUserConfiguration(), readCon);
				} catch (final OXException oxExc) {
					throw new SMTPException(oxExc);
				} catch (final Exception e) {
					throw new SMTPException(SMTPException.Code.INTERNAL_ERROR, e, e.getMessage());
				}
				final VersitObject versitObj = converter.convertContact(contactObj, "2.1");
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				final VersitDefinition def = Versit.getDefinition("text/x-vcard");
				final VersitDefinition.Writer w = def.getWriter(os, SMTPConfig.getDefaultMimeCharset());
				def.write(w, versitObj);
				w.flush();
				os.flush();
				return new String(os.toByteArray(), SMTPConfig.getDefaultMimeCharset());
			} finally {
				if (readCon != null) {
					DBPool.closeReaderSilent(session.getContext(), readCon);
					readCon = null;
				}
				converter.close();
			}
		} catch (final ConverterException e) {
			throw new SMTPException(SMTPException.Code.INTERNAL_ERROR, e, e.getMessage());
		} catch (final AbstractOXException e) {
			throw new SMTPException(e);
		} catch (final IOException e) {
			throw new SMTPException(SMTPException.Code.IO_ERROR, e, e.getMessage());
		}
	}

	/**
	 * Creates a <code>javax.mail.Multipart</code> instance of MIME type
	 * multipart/alternative. If <code>embeddedImages</code> is
	 * <code>true</code> a sub-multipart of MIME type multipart/related is
	 * going to be appended as the "html" version
	 */
	private Multipart createMultipartAlternative(final SMTPMailMessage mail, final boolean embeddedImages)
			throws MailException, MessagingException {
		/*
		 * Create an "alternative" multipart
		 */
		final Multipart alternativeMultipart = new MimeMultipart(MP_ALTERNATIVE);
		/*
		 * Create a body part for both text and html content
		 */
		final MimeBodyPart text = new MimeBodyPart();
		final String mailBody = (String) mail.getContent();
		/*
		 * Define & add text content
		 */
		try {
			text.setText(performLineFolding(getConverter().convertWithQuotes(mailBody), false, usm.getAutoLinebreak()),
					SMTPConfig.getDefaultMimeCharset());
		} catch (final IOException e) {
			throw new SMTPException(SMTPException.Code.HTML2TEXT_CONVERTER_ERROR, e, e.getMessage());
		}
		text.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
		text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, PAT_TEXT_CT.replaceFirst(REPLACE_CS, SMTPConfig
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
			relatedMultipart.addBodyPart(createHtmlBodyPart(processReferencedLocalImages(mailBody, relatedMultipart,
					this), usm.getAutoLinebreak()), 0);
			/*
			 * Traverse Content-IDs
			 */
			final List<String> cidList = MIMEMessageUtility.getContentIDs(mailBody);
			NextImg: for (String cid : cidList) {
				/*
				 * Get & remove inline image (to prevent it from be sent twice)
				 */
				final MailPart imgPart = getAndRemoveImageAttachment(cid, mail);
				if (imgPart == null) {
					continue NextImg;
				}
				/*
				 * Create new body part from part's data handler
				 */
				final BodyPart relatedImageBodyPart = new MimeBodyPart();
				relatedImageBodyPart.setDataHandler(imgPart.getDataHandler());
				for (final Iterator<Map.Entry<String, String>> iter = imgPart.getHeadersIterator(); iter.hasNext();) {
					final Map.Entry<String, String> e = iter.next();
					relatedImageBodyPart.setHeader(e.getKey(), e.getValue());
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
			final BodyPart html = createHtmlBodyPart(mailBody, usm.getAutoLinebreak());
			/*
			 * Add html part to superior multipart
			 */
			alternativeMultipart.addBodyPart(html);
		}
		return alternativeMultipart;
	}

	private void addMessageBodyPart(final Multipart mp, final MailPart part, final boolean inline)
			throws MessagingException, MailException {
		final MimeBodyPart messageBodyPart;
		if (part.getContentType().isMimeType("text/*")) {
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setDataHandler(part.getDataHandler());
			messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, part.getContentType().toString());
		} else {
			if (part.getContentType().isMimeType("application/octet-stream") && part.getFileName() != null) {
				/*
				 * Try to determine MIME type
				 */
				final String ct = MIMEType2ExtMap.getContentType(part.getFileName());
				final int pos = ct.indexOf('/');
				part.getContentType().setPrimaryType(ct.substring(0, pos));
				part.getContentType().setSubType(ct.substring(pos + 1));
			}
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setDataHandler(part.getDataHandler());
			messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, part.getContentType().toString());
		}
		/*
		 * Filename
		 */
		if (part.getFileName() != null) {
			try {
				messageBodyPart.setFileName(MimeUtility.encodeText(part.getFileName(), SMTPConfig
						.getDefaultMimeCharset(), "Q"));
			} catch (final UnsupportedEncodingException e) {
				messageBodyPart.setFileName(part.getFileName());
			} catch (final MailConfigException e) {
				messageBodyPart.setFileName(part.getFileName());
			}
		}
		/*
		 * Disposition
		 */
		messageBodyPart.setDisposition(inline ? Part.INLINE : Part.ATTACHMENT);
		/*
		 * Content-ID
		 */
		if (part.getContentId() != null) {
			final String cid = part.getContentId().charAt(0) == '<' ? part.getContentId() : new StringBuilder(part
					.getContentId().length() + 2).append('<').append(part.getContentId()).append('>').toString();
			messageBodyPart.setContentID(cid);
		}
		/*
		 * Add to parental multipart
		 */
		mp.addBodyPart(messageBodyPart);
	}

	private void setMessageHeaders(final SMTPMailMessage mail, final SMTPMessage smtpMessage)
			throws MessagingException, MailConfigException {
		/*
		 * Set from
		 */
		if (mail.containsFrom()) {
			smtpMessage.setFrom(mail.getFrom()[0]);
		}
		/*
		 * Set to
		 */
		if (mail.containsTo()) {
			smtpMessage.setRecipients(RecipientType.TO, mail.getTo());
		}
		/*
		 * Set cc
		 */
		if (mail.containsCc()) {
			smtpMessage.setRecipients(RecipientType.CC, mail.getCc());
		}
		/*
		 * Bcc
		 */
		if (mail.containsBcc()) {
			smtpMessage.setRecipients(RecipientType.BCC, mail.getBcc());
		}
		/*
		 * Set subject
		 */
		if (mail.containsSubject()) {
			smtpMessage.setSubject(mail.getSubject(), SMTPConfig.getDefaultMimeCharset());
		}
		/*
		 * Set sent date
		 */
		if (mail.containsSentDate()) {
			smtpMessage.setSentDate(mail.getSentDate());
		}
		/*
		 * Set flags
		 */
		final Flags msgFlags = new Flags();
		if (mail.isAnswered()) {
			msgFlags.add(Flags.Flag.ANSWERED);
		}
		if (mail.isDeleted()) {
			msgFlags.add(Flags.Flag.DELETED);
		}
		if (mail.isDraft()) {
			msgFlags.add(Flags.Flag.DRAFT);
		}
		if (mail.isFlagged()) {
			msgFlags.add(Flags.Flag.FLAGGED);
		}
		if (mail.isRecent()) {
			msgFlags.add(Flags.Flag.RECENT);
		}
		if (mail.isSeen()) {
			msgFlags.add(Flags.Flag.SEEN);
		}
		if (mail.isUser()) {
			msgFlags.add(Flags.Flag.USER);
		}
		// if (IMAPProperties.isUserFlagsEnabled()
		// && (destinationFolder == null ||
		// session.getCachedUserFlags(destinationFolder, true))) {
		// /*
		// * Set user header: Color Label
		// */
		// msgFlags.add(JSONMessageObject.getColorLabelStringValue(msgObj.getColorLabel()));
		// }
		/*
		 * Finally, apply flags to message
		 */
		smtpMessage.setFlags(msgFlags, true);
		/*
		 * Set disposition notification
		 */
		if (mail.getDispositionNotification() != null) {
			smtpMessage.setHeader(MessageHeaders.HDR_DISP_TO, mail.getDispositionNotification().toString());
		}
		/*
		 * Set priority
		 */
		smtpMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, String.valueOf(mail.getPriority()));
		/*
		 * Set common headers
		 */
		fillCommonHeaders(smtpMessage, session);
		/*
		 * Headers
		 */
		final int size = mail.getHeadersSize();
		final Iterator<Map.Entry<String, String>> iter = mail.getHeadersIterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, String> entry = iter.next();
			smtpMessage.addHeader(entry.getKey(), entry.getValue());
		}
	}

	/*
	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * +++++++++++++++++++++++++ HELPER METHODS +++++++++++++++++++++++++
	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Sets common headers in given SMTP message: <code>X-Mailer</code>,
	 * <code>Organization</code> & <code>ENVELOPE-FROM</code>
	 * 
	 * @param smtpMessage
	 *            The SMTP message
	 * @param session
	 *            The session
	 * @throws MessagingException
	 *             If headers cannot be set
	 * @throws MailConfigException
	 *             If {@link SMTPConfig#isSmtpEnvelopeFrom()} cannot be invoked
	 */
	public static void fillCommonHeaders(final SMTPMessage smtpMessage, final SessionObject session)
			throws MessagingException, MailConfigException {
		/*
		 * Set mailer
		 */
		smtpMessage.setHeader(MessageHeaders.HDR_X_MAILER, "Open-Xchange Mailer v" + Version.VERSION_STRING);
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
				smtpMessage.setHeader(MessageHeaders.HDR_ORGANIZATION, c.getCompany());
			}
		} catch (final Throwable t) {
			LOG.warn("Header \"Organization\" could not be set", t);
		}
		/*
		 * ENVELOPE-FROM
		 */
		if (SMTPConfig.isSmtpEnvelopeFrom()) {
			/*
			 * Set ENVELOPE-FROM in SMTP message to user's primary email address
			 */
			smtpMessage.setEnvelopeFrom(session.getUserObject().getMail());
		}
	}

	private static BodyPart createHtmlBodyPart(final String htmlContent, final int linewrap)
			throws MailConfigException, MessagingException {
		final String htmlCT = PAT_HTML_CT.replaceFirst(REPLACE_CS, SMTPConfig.getDefaultMimeCharset());
		final MimeBodyPart html = new MimeBodyPart();
		html.setContent(performLineFolding(replaceHTMLSimpleQuotesForDisplay(formatHrefLinks(htmlContent)), true,
				linewrap), htmlCT);
		html.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
		html.setHeader(MessageHeaders.HDR_CONTENT_TYPE, htmlCT);
		return html;
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
			final SMTPMessageFiller msgFiller) throws MessagingException {
		final StringBuffer sb = new StringBuffer(htmlContent.length());
		final Matcher m = MIMEMessageUtility.PATTERN_REF_IMG.matcher(htmlContent);
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
			fileName = MimeUtility.encodeText(uploadFile.getFileName(), SMTPConfig.getDefaultMimeCharset(), "Q");
		} catch (final UnsupportedEncodingException e) {
			fileName = uploadFile.getFileName();
		} catch (final MailConfigException e) {
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

	private static MailPart getAndRemoveImageAttachment(final String cid, final SMTPMailMessage mail)
			throws MailException {
		final int size = mail.getEnclosedCount();
		for (int i = 0; i < size; i++) {
			final MailPart enclosedPart = mail.getEnclosedMailPart(i);
			if (enclosedPart.containsContentId() && MIMEMessageUtility.equalsCID(cid, enclosedPart.getContentId())) {
				return mail.removeEnclosedPart(i);
			}
		}
		return null;
	}

}
