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

package com.openexchange.smtp;

import static com.openexchange.mail.MailInterfaceImpl.mailInterfaceMonitor;
import static com.openexchange.mail.utils.MessageUtility.parseAddressList;
import static com.openexchange.mail.utils.MessageUtility.performLineFolding;
import static java.util.regex.Matcher.quoteReplacement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.i18n.StringHelper;
import com.openexchange.mail.MailConnection;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.TransportMailMessage;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.SendType;
import com.openexchange.mail.transport.dataobjects.InfostoreDocumentMailPart;
import com.openexchange.mail.transport.dataobjects.ReferencedMailPart;
import com.openexchange.mail.transport.dataobjects.TextBodyMailPart;
import com.openexchange.mail.transport.dataobjects.UploadFileMailPart;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.smtp.config.SMTPSessionProperties;
import com.openexchange.smtp.dataobjects.SMTPBodyPart;
import com.openexchange.smtp.dataobjects.SMTPDocumentPart;
import com.openexchange.smtp.dataobjects.SMTPFilePart;
import com.openexchange.smtp.dataobjects.SMTPMailMessage;
import com.openexchange.smtp.dataobjects.SMTPMailPart;
import com.openexchange.smtp.dataobjects.SMTPReferencedPart;
import com.openexchange.smtp.filler.SMTPMessageFiller;
import com.openexchange.tools.mail.ContentType;
import com.sun.mail.smtp.SMTPMessage;

/**
 * {@link SMTPTransport}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SMTPTransport extends MailTransport {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SMTPTransport.class);

	private static final String CHARENC_ISO_8859_1 = "ISO-8859-1";

	private static final String PROTOCOL_SMTP = "smtp";

	private final Session smtpSession;

	private final MailConnection mailConnection;

	private final SessionObject session;

	private final UserSettingMail usm;

	private MailConfig transportConfig;

	public SMTPTransport() {
		super();
		smtpSession = null;
		mailConnection = null;
		session = null;
		usm = null;
	}

	/**
	 * Constructor
	 * 
	 * @param session
	 *            The session
	 * @param mailConnection
	 *            The mail connection (to access folders/messages)
	 * @throws MailException
	 *             If initialization fails
	 */
	public SMTPTransport(final SessionObject session, final MailConnection mailConnection) throws MailException {
		super();
		this.mailConnection = mailConnection;
		final Properties smtpProps = SMTPSessionProperties.getDefaultSessionProperties();
		final MailConfig transportConfig = getTransportConfig(session);
		if (transportConfig.getError() != null) {
			throw new SMTPException(transportConfig.getError());
		}
		smtpProps.put(MIMESessionPropertyNames.PROP_SMTPHOST, transportConfig.getServer());
		smtpProps.put(MIMESessionPropertyNames.PROP_SMTPPORT, String.valueOf(transportConfig.getPort()));
		this.smtpSession = Session.getInstance(smtpProps, null);
		this.session = session;
		usm = session.getUserSettingMail();
	}

	private void checkMailConnection() throws MailException {
		if (!mailConnection.isConnected()) {
			mailConnection.connect();
		}
	}

	@Override
	public MailConfig getTransportConfig(final SessionObject session) throws MailException {
		if (transportConfig == null) {
			transportConfig = SMTPConfig.getSmtpConfig(session);
		}
		return transportConfig;
	}

	private static final String ACK_TEXT = "Reporting-UA: OPEN-XCHANGE - WebMail\r\nFinal-Recipient: rfc822; #FROM#\r\n"
			+ "Original-Message-ID: #MSG ID#\r\nDisposition: manual-action/MDN-sent-manually; displayed\r\n";

	@Override
	public void sendReceiptAck(final String fullname, final long msgUID, final String fromAddr) throws MailException {
		try {
			checkMailConnection();
			final MailMessage mail = mailConnection.getMessageStorage().getMessage(fullname, msgUID);
			final String dispNotification = mail.getHeader(MessageHeaders.HDR_DISP_TO);
			if (dispNotification == null || dispNotification.length() == 0) {
				throw new SMTPException(SMTPException.Code.MISSING_NOTIFICATION_HEADER, MessageHeaders.HDR_DISP_TO,
						Long.valueOf(msgUID));
			}
			final SMTPMessage smtpMessage = new SMTPMessage(smtpSession);
			/*
			 * Set from
			 */
			final String from;
			if (fromAddr != null) {
				from = fromAddr;
			} else if (usm.getSendAddr() == null && session.getUserObject().getMail() == null) {
				throw new SMTPException(SMTPException.Code.NO_SEND_ADDRESS_FOUND);
			} else {
				from = usm.getSendAddr() == null ? session.getUserObject().getMail() : usm.getSendAddr();
			}
			smtpMessage.addFrom(parseAddressList(from, false));
			/*
			 * Set to
			 */
			smtpMessage.addRecipients(RecipientType.TO, InternetAddress.parse(dispNotification, true));
			/*
			 * Set header
			 */
			smtpMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, "3 (normal)");
			/*
			 * Subject
			 */
			final StringHelper strHelper = new StringHelper(session.getLocale());
			smtpMessage.setSubject(strHelper.getString(MailStrings.ACK_SUBJECT));
			/*
			 * Sent date
			 */
			final Date date = new Date();
			final int offset = TimeZone.getTimeZone(session.getUserObject().getTimeZone()).getOffset(date.getTime());
			smtpMessage.setSentDate(new Date(System.currentTimeMillis() - offset));
			/*
			 * Set common headers
			 */
			SMTPMessageFiller.fillCommonHeaders(smtpMessage, session);
			/*
			 * Compose body
			 */
			final ContentType ct = new ContentType("text/plain; charset=UTF-8");
			final Multipart mixedMultipart = new MimeMultipart("report; report-type=disposition-notification");
			/*
			 * Define text content
			 */
			final Date sentDate = mail.getSentDate();
			final MimeBodyPart text = new MimeBodyPart();
			text.setText(performLineFolding(strHelper.getString(MailStrings.ACK_NOTIFICATION_TEXT).replaceFirst(
					"#DATE#",
					sentDate == null ? "" : quoteReplacement(DateFormat.getDateInstance(DateFormat.LONG,
							session.getLocale()).format(sentDate))).replaceFirst("#RECIPIENT#", quoteReplacement(from))
					.replaceFirst("#SUBJECT#", quoteReplacement(mail.getSubject())), false, usm.getAutoLinebreak()),
					MailConfig.getDefaultMimeCharset());
			text.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
			text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
			mixedMultipart.addBodyPart(text);
			/*
			 * Define ack
			 */
			ct.setContentType("message/disposition-notification; name=MDNPart1.txt; charset=UTF-8");
			final MimeBodyPart ack = new MimeBodyPart();
			final String msgId = mail.getHeader(MessageHeaders.HDR_MESSAGE_ID);
			ack.setText(strHelper.getString(ACK_TEXT).replaceFirst("#FROM#", quoteReplacement(fromAddr)).replaceFirst(
					"#MSG ID#", quoteReplacement(msgId)), MailConfig.getDefaultMimeCharset());
			ack.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
			ack.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
			ack.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, "attachment; filename=MDNPart1.txt");
			mixedMultipart.addBodyPart(ack);
			/*
			 * Set message content
			 */
			smtpMessage.setContent(mixedMultipart);
			/*
			 * Transport message
			 */
			final long start = System.currentTimeMillis();
			final Transport transport = smtpSession.getTransport(PROTOCOL_SMTP);
			if (SMTPConfig.isSmtpAuth()) {
				final MailConfig config = getTransportConfig(session);
				transport.connect(config.getServer(), config.getPort(), config.getLogin(), encodePassword(config
						.getPassword()));
			} else {
				transport.connect();
			}
			try {
				smtpMessage.saveChanges();
				transport.sendMessage(smtpMessage, smtpMessage.getAllRecipients());
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			} finally {
				transport.close();
			}
		} catch (final MessagingException e) {
			throw SMTPException.handleMessagingException(e, mailConnection);
		}
	}

	@Override
	public MailPath sendRawMessage(final byte[] asciiBytes) throws MailException {
		try {
			final SMTPMessage smtpMessage = new SMTPMessage(Session.getInstance(SMTPSessionProperties
					.getDefaultSessionProperties(), null), new ByteArrayInputStream(asciiBytes));
			/*
			 * Check recipients
			 */
			final Address[] allRecipients = smtpMessage.getAllRecipients();
			if (allRecipients == null || allRecipients.length == 0) {
				throw new SMTPException(SMTPException.Code.MISSING_RECIPIENTS);
			}
			try {
				final long start = System.currentTimeMillis();
				final Transport transport = smtpSession.getTransport(PROTOCOL_SMTP);
				if (SMTPConfig.isSmtpAuth()) {
					final MailConfig config = getTransportConfig(session);
					transport.connect(config.getServer(), config.getPort(), config.getLogin(), encodePassword(config
							.getPassword()));
				} else {
					transport.connect();
				}
				try {
					smtpMessage.saveChanges();
					transport.sendMessage(smtpMessage, allRecipients);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				} finally {
					transport.close();
				}
			} catch (final MessagingException e) {
				throw SMTPException.handleMessagingException(e, mailConnection);
			}
			if (usm.isNoCopyIntoStandardSentFolder()) {
				/*
				 * No copy in sent folder
				 */
				return MailPath.NULL;
			}
			/*
			 * Append message to folder "SENT"
			 */
			final long startTransport = System.currentTimeMillis();
			final String sentFullname = mailConnection.getFolderStorage().getSentFolder();
			long uid = -1L;
			try {
				uid = mailConnection.getMessageStorage().appendMessages(sentFullname,
						new MailMessage[] { MIMEMessageConverter.convertMessage(smtpMessage) })[0];
			} catch (final SMTPException e) {
				throw e;
			} catch (final MailException e) {
				throw new SMTPException(SMTPException.Code.COPY_TO_SENT_FOLDER_FAILED, e, new Object[0]);
			}
			final MailPath retval = new MailPath(sentFullname, uid);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(128).append("Mail copy (").append(retval.toString())
						.append(") appended in ").append(System.currentTimeMillis() - startTransport).append("msec")
						.toString());
			}
			return retval;
		} catch (final MessagingException e) {
			throw SMTPException.handleMessagingException(e, mailConnection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.transport.MailTransport#sendMailMessage(com.openexchange.mail.dataobjects.MailMessage,
	 *      com.openexchange.mail.transport.SendType)
	 */
	@Override
	public MailPath sendMailMessage(final TransportMailMessage transportMail, final SendType sendType)
			throws MailException {
		try {
			long startTransport = System.currentTimeMillis();
			final SMTPMessage smtpMessage = new SMTPMessage(smtpSession);
			if ((transportMail.getFlags() & MailMessage.FLAG_DRAFT) == MailMessage.FLAG_DRAFT) {
				/*
				 * A draft message
				 */
				checkMailConnection();
				/*
				 * Fill message
				 */
				final SMTPMessageFiller filler = new SMTPMessageFiller(session);
				filler.fillMail((SMTPMailMessage) transportMail, smtpMessage);
				smtpMessage.setFlag(Flags.Flag.DRAFT, true);
				smtpMessage.saveChanges();
				/*
				 * Append message to draft folder
				 */
				final String draftFullname = mailConnection.getFolderStorage().getDraftsFolder();
				final long uid = mailConnection.getMessageStorage().appendMessages(draftFullname,
						new MailMessage[] { MIMEMessageConverter.convertMessage(smtpMessage) })[0];
				filler.deleteReferencedUploadFiles();
				/*
				 * Check for draft-edit operation: Delete old version
				 */
				if (transportMail.getMsgref() != null) {
					final MailPath mailPath = new MailPath(transportMail.getMsgref());
					if (mailConnection.getMessageStorage().getMessage(mailPath.getFolder(), mailPath.getUid())
							.isDraft()) {
						mailConnection.getMessageStorage().deleteMessages(mailPath.getFolder(),
								new long[] { mailPath.getUid() }, true);
					}
					transportMail.setMsgref(null);
				}
				return new MailPath(draftFullname, uid);
			}
			/*
			 * Send operation
			 */
			boolean markAsAnswered = false;
			final SMTPMessageFiller msgFiller;
			final MailPath mailPath;
			final List<String> tempIds = new ArrayList<String>(5);
			{
				final MailMessage referencedMail;
				if (transportMail.getMsgref() != null) {
					mailPath = new MailPath(transportMail.getMsgref());
					referencedMail = mailConnection.getMessageStorage().getMessage(mailPath.getFolder(),
							mailPath.getUid());
					loadReferencedParts((SMTPMailMessage) transportMail, tempIds, referencedMail);
					if (SendType.REPLY.equals(sendType)) {
						checkMailConnection();
						setReplyHeaders(referencedMail, smtpMessage);
						/*
						 * Remember to set \ANSWERED flag in referenced message
						 */
						markAsAnswered = true;
					}
				} else {
					mailPath = null;
					referencedMail = null;
				}
				/*
				 * Fill message dependent on send type
				 */
				msgFiller = new SMTPMessageFiller(session);
				if (SendType.FORWARD.equals(sendType) && usm.isForwardAsAttachment()) {
					msgFiller.fillMail((SMTPMailMessage) transportMail, smtpMessage, sendType, referencedMail);
				} else {
					msgFiller.fillMail((SMTPMailMessage) transportMail, smtpMessage);
				}
			}
			/*
			 * Check recipients
			 */
			final Address[] allRecipients = smtpMessage.getAllRecipients();
			if (allRecipients == null || allRecipients.length == 0) {
				throw new SMTPException(SMTPException.Code.MISSING_RECIPIENTS);
			}
			setSendHeaders((SMTPMailMessage) transportMail, smtpMessage);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(128).append("SMTP mail prepared for transport in ").append(
						System.currentTimeMillis() - startTransport).append("msec").toString());
			}
			try {
				final long start = System.currentTimeMillis();
				final Transport transport = smtpSession.getTransport(PROTOCOL_SMTP);
				if (SMTPConfig.isSmtpAuth()) {
					final MailConfig config = getTransportConfig(session);
					transport.connect(config.getServer(), config.getPort(), config.getLogin(), encodePassword(config
							.getPassword()));
				} else {
					transport.connect();
				}
				try {
					smtpMessage.saveChanges();
					transport.sendMessage(smtpMessage, allRecipients);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				} finally {
					transport.close();
				}
			} catch (final MessagingException e) {
				throw SMTPException.handleMessagingException(e, mailConnection);
			}
			if (markAsAnswered) {
				mailConnection.getMessageStorage().updateMessageFlags(mailPath.getFolder(),
						new long[] { mailPath.getUid() }, MailMessage.FLAG_ANSWERED, true);
			}
			if (usm.isNoCopyIntoStandardSentFolder()) {
				/*
				 * No copy in sent folder
				 */
				msgFiller.deleteReferencedUploadFiles();
				for (String id : tempIds) {
					session.removeAJAXUploadFile(id);
				}
				return MailPath.NULL;
			}
			/*
			 * Append message to folder "SENT"
			 */
			startTransport = System.currentTimeMillis();
			final String sentFullname = mailConnection.getFolderStorage().getSentFolder();
			long uid = -1L;
			try {
				uid = mailConnection.getMessageStorage().appendMessages(sentFullname,
						new MailMessage[] { MIMEMessageConverter.convertMessage(smtpMessage) })[0];
			} catch (final SMTPException e) {
				throw e;
			} catch (final MailException e) {
				throw new SMTPException(SMTPException.Code.COPY_TO_SENT_FOLDER_FAILED, e, new Object[0]);
			}
			msgFiller.deleteReferencedUploadFiles();
			for (String id : tempIds) {
				session.removeAJAXUploadFile(id);
			}
			final MailPath retval = new MailPath(sentFullname, uid);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(128).append("Mail copy (").append(retval.toString())
						.append(") appended in ").append(System.currentTimeMillis() - startTransport).append("msec")
						.toString());
			}
			return retval;
		} catch (final MessagingException e) {
			throw SMTPException.handleMessagingException(e, mailConnection);
		} catch (final IOException e) {
			throw new SMTPException(SMTPException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	private void loadReferencedParts(final SMTPMailMessage mail, final List<String> tempIds,
			final MailMessage referencedMail) throws MailException, SMTPException {
		/*
		 * Load referenced parts
		 */
		final MailMessageParser parser = new MailMessageParser();
		final int count = mail.getEnclosedCount();
		for (int i = 0; i < count; i++) {
			final SMTPMailPart smtpMailPart = (SMTPMailPart) mail.getEnclosedMailPart(i);
			if (SMTPMailPart.SMTPPartType.REFERENCE.equals(smtpMailPart.getType())) {
				final String id = ((SMTPReferencedPart) smtpMailPart).loadReferencedPart(parser, referencedMail,
						session);
				if (id != null) {
					tempIds.add(id);
				}
			}
		}
	}

	/**
	 * Sets the appropriate headers before message's transport:
	 * <code>Reply-To</code>, <code>Date</code>, and <code>Subject</code>
	 * 
	 * @param mail
	 *            The source mail
	 * @param newSMTPMsg
	 *            The SMTP message
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private void setSendHeaders(final SMTPMailMessage mail, final SMTPMessage newSMTPMsg) throws AddressException,
			MessagingException {
		/*
		 * Set the Reply-To header for future replies to this new message
		 */
		final InternetAddress[] ia;
		if (usm.getReplyToAddr() == null) {
			ia = mail.getFrom();
		} else {
			ia = parseAddressList(usm.getReplyToAddr(), false);
		}
		newSMTPMsg.setReplyTo(ia);
		/*
		 * Set sent date if not done, yet
		 */
		if (newSMTPMsg.getSentDate() == null) {
			newSMTPMsg.setSentDate(new Date());
		}
		/*
		 * Set default subject if none set
		 */
		final String subject;
		if ((subject = newSMTPMsg.getSubject()) == null || subject.length() == 0) {
			newSMTPMsg.setSubject(new StringHelper(session.getLocale()).getString(MailStrings.DEFAULT_SUBJECT));
		}
	}

	/**
	 * Sets the appropriate headers <code>In-Reply-To</code> and
	 * <code>References</code>
	 * 
	 * @param referencedMail
	 *            The referenced mail
	 * @param smtpMessage
	 *            The SMTP message
	 * @throws MessagingException
	 *             If setting the reply headers fails
	 */
	private void setReplyHeaders(final MailMessage referencedMail, final SMTPMessage smtpMessage)
			throws MessagingException {
		/*
		 * A reply! Set appropriate message headers
		 */
		final String pMsgId = referencedMail.getHeader(MessageHeaders.HDR_MESSAGE_ID);
		if (pMsgId != null) {
			smtpMessage.setHeader(MessageHeaders.HDR_IN_REPLY_TO, pMsgId);
		}
		/*
		 * Set References header field
		 */
		final String pReferences = referencedMail.getHeader(MessageHeaders.HDR_REFERENCES);
		final String pInReplyTo = referencedMail.getHeader(MessageHeaders.HDR_IN_REPLY_TO);
		final StringBuilder refBuilder = new StringBuilder();
		if (pReferences != null) {
			/*
			 * The "References:" field will contain the contents of the parent's
			 * "References:" field (if any) followed by the contents of the
			 * parent's "Message-ID:" field (if any).
			 */
			refBuilder.append(pReferences);
		} else if (pInReplyTo != null) {
			/*
			 * If the parent message does not contain a "References:" field but
			 * does have an "In-Reply-To:" field containing a single message
			 * identifier, then the "References:" field will contain the
			 * contents of the parent's "In-Reply-To:" field followed by the
			 * contents of the parent's "Message-ID:" field (if any).
			 */
			refBuilder.append(pInReplyTo);
		}
		if (pMsgId != null) {
			if (refBuilder.length() > 0) {
				refBuilder.append(' ');
			}
			refBuilder.append(pMsgId);
		}
		if (refBuilder.length() > 0) {
			/*
			 * If the parent has none of the "References:", "In-Reply-To:", or
			 * "Message-ID:" fields, then the new message will have no
			 * "References:" field.
			 */
			smtpMessage.setHeader(MessageHeaders.HDR_REFERENCES, refBuilder.toString());
		}
	}

	private static String encodePassword(final String password) {
		String tmpPass = password;
		if (password != null) {
			try {
				tmpPass = new String(password.getBytes(SMTPConfig.getSmtpAuthEnc()), CHARENC_ISO_8859_1);
			} catch (final UnsupportedEncodingException e) {
				LOG.error("Unsupported encoding in a message detected and monitored.", e);
				mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			} catch (final MailConfigException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return tmpPass;
	}

	@Override
	protected TransportMailMessage getNewTransportMailMessageInternal() throws MailException {
		return new SMTPMailMessage();
	}

	@Override
	protected UploadFileMailPart getNewFilePartInternal(final UploadFile uploadFile) throws MailException {
		return new SMTPFilePart(uploadFile);
	}

	@Override
	protected InfostoreDocumentMailPart getNewDocumentPartInternal(final int documentId, final SessionObject session)
			throws MailException {
		return new SMTPDocumentPart(documentId, session);
	}

	@Override
	protected ReferencedMailPart getNewReferencedPartInternal(final MailPart referencedPart, final SessionObject session)
			throws MailException {
		return new SMTPReferencedPart(referencedPart, session);
	}

	@Override
	protected ReferencedMailPart getNewReferencedPartInternal(final String sequenceId) throws MailException {
		return new SMTPReferencedPart(sequenceId);
	}

	@Override
	protected TextBodyMailPart getNewTextBodyPartInternal(final String textBody) throws MailException {
		return new SMTPBodyPart(textBody);
	}
}
