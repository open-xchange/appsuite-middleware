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

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.utils.MessageUtility.parseAddressList;
import static com.openexchange.mail.utils.MessageUtility.performLineFolding;
import static java.util.regex.Matcher.quoteReplacement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.smtp.config.SMTPSessionProperties;
import com.openexchange.smtp.dataobjects.SMTPMailMessage;
import com.openexchange.smtp.filler.SMTPMessageFiller;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
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

	private javax.mail.Session smtpSession;

	private final Session session;

	private final Context ctx;

	private final UserSettingMail usm;

	private SMTPConfig smtpConfig;

	private SMTPMessageFiller smtpFiller;

	private List<String> tempIds;

	public SMTPTransport() {
		super();
		smtpSession = null;
		session = null;
		ctx = null;
		usm = null;
	}

	private static final String PROPERTY_SECURITY_PROVIDER = "ssl.SocketFactory.provider";

	private static final String CLASSNAME_SECURITY_FACTORY = "com.openexchange.tools.ssl.TrustAllSSLSocketFactory";

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
	public SMTPTransport(final Session session) throws MailException {
		super();
		this.session = session;
		if (session == null) {
			/*
			 * Dummy instance
			 */
			this.ctx = null;
			usm = null;
		} else {
			try {
				this.ctx = ContextStorage.getStorageContext(session.getContextId());
			} catch (final ContextException e) {
				throw new SMTPException(e);
			}
			usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
		}
	}

	private void clearUp() {
		if (smtpFiller != null) {
			smtpFiller.deleteReferencedUploadFiles();
			smtpFiller = null;
		}
		if (tempIds != null) {
			for (final String id : tempIds) {
				session.removeUploadedFile(id);
			}
			tempIds = null;
		}
	}

	@Override
	public void close() {
		clearUp();
	}

	private javax.mail.Session getSMTPSession() throws MailException {
		if (null == smtpSession) {
			final Properties smtpProps = SMTPSessionProperties.getDefaultSessionProperties();
			final SMTPConfig smtpConfig = getTransportConfig();
			/*
			 * Check if a secure SMTP connection should be established
			 */
			if (smtpConfig.isSecure()) {
				smtpProps.put(MIMESessionPropertyNames.PROP_MAIL_SMTP_SOCKET_FACTORY_CLASS, CLASSNAME_SECURITY_FACTORY);
				smtpProps.put(MIMESessionPropertyNames.PROP_MAIL_SMTP_SOCKET_FACTORY_PORT, String.valueOf(smtpConfig
						.getPort()));
				smtpProps.put(MIMESessionPropertyNames.PROP_MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false");
				smtpProps.put(MIMESessionPropertyNames.PROP_MAIL_SMTP_STARTTLS_ENABLE, "true");
				/*
				 * Needed for JavaMail >= 1.4
				 */
				Security.setProperty(PROPERTY_SECURITY_PROVIDER, CLASSNAME_SECURITY_FACTORY);
			}
			/*
			 * Apply host & port to SMTP session
			 */
			smtpProps.put(MIMESessionPropertyNames.PROP_SMTPHOST, smtpConfig.getServer());
			smtpProps.put(MIMESessionPropertyNames.PROP_SMTPPORT, String.valueOf(smtpConfig.getPort()));
			smtpSession = javax.mail.Session.getInstance(smtpProps, null);
		}
		return smtpSession;
	}

	private SMTPConfig getTransportConfig() throws MailException {
		if (smtpConfig == null) {
			smtpConfig = TransportConfig.getTransportConfig(SMTPConfig.class, session);
		}
		return smtpConfig;
	}

	private static final String ACK_TEXT = "Reporting-UA: OPEN-XCHANGE - WebMail\r\nFinal-Recipient: rfc822; #FROM#\r\n"
			+ "Original-Message-ID: #MSG ID#\r\nDisposition: manual-action/MDN-sent-manually; displayed\r\n";

	@Override
	public void sendReceiptAck(final MailMessage srcMail, final String fromAddr) throws MailException {
		try {
			clearUp();
			final String dispNotification = srcMail.getHeader(MessageHeaders.HDR_DISP_TO);
			if (dispNotification == null || dispNotification.length() == 0) {
				throw new SMTPException(SMTPException.Code.MISSING_NOTIFICATION_HEADER, MessageHeaders.HDR_DISP_TO,
						Long.valueOf(srcMail.getMailId()));
			}
			final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession());
			final User u = UserStorage.getStorageUser(session.getUserId(), ctx);
			/*
			 * Set from
			 */
			final String from;
			if (fromAddr != null) {
				from = fromAddr;
			} else if (usm.getSendAddr() == null && u.getMail() == null) {
				throw new SMTPException(SMTPException.Code.NO_SEND_ADDRESS_FOUND);
			} else {
				from = usm.getSendAddr() == null ? u.getMail() : usm.getSendAddr();
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
			final Locale locale = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale();
			final StringHelper strHelper = new StringHelper(locale);
			smtpMessage.setSubject(strHelper.getString(MailStrings.ACK_SUBJECT));
			/*
			 * Sent date in UTC time
			 */
			smtpMessage.setSentDate(new Date());
			/*
			 * Set common headers
			 */
			new SMTPMessageFiller(session, ctx, usm).setCommonHeaders(smtpMessage);
			/*
			 * Compose body
			 */
			final ContentType ct = new ContentType("text/plain; charset=UTF-8");
			final Multipart mixedMultipart = new MimeMultipart("report; report-type=disposition-notification");
			/*
			 * Define text content
			 */
			final Date sentDate = srcMail.getSentDate();
			if (sentDate != null) {
				final int offset = TimeZone.getTimeZone(u.getTimeZone()).getOffset(sentDate.getTime());
				sentDate.setTime(sentDate.getTime() + offset);
			}
			final MimeBodyPart text = new MimeBodyPart();
			text.setText(performLineFolding(strHelper.getString(MailStrings.ACK_NOTIFICATION_TEXT).replaceFirst(
					"#DATE#",
					sentDate == null ? "" : quoteReplacement(DateFormat.getDateInstance(DateFormat.LONG, locale)
							.format(sentDate))).replaceFirst("#RECIPIENT#", quoteReplacement(from)).replaceFirst(
					"#SUBJECT#", quoteReplacement(srcMail.getSubject())), false, usm.getAutoLinebreak()), MailConfig
					.getDefaultMimeCharset());
			text.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
			text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
			mixedMultipart.addBodyPart(text);
			/*
			 * Define ack
			 */
			ct.setContentType("message/disposition-notification; name=MDNPart1.txt; charset=UTF-8");
			final MimeBodyPart ack = new MimeBodyPart();
			final String msgId = srcMail.getHeader(MessageHeaders.HDR_MESSAGE_ID);
			ack.setText(strHelper.getString(ACK_TEXT).replaceFirst("#FROM#", quoteReplacement(from)).replaceFirst(
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
			final Transport transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
			if (SMTPConfig.isSmtpAuth()) {
				final SMTPConfig config = getTransportConfig();
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
			throw SMTPException.handleMessagingException(e);
		}
	}

	@Override
	public MailMessage sendRawMessage(final byte[] asciiBytes) throws MailException {
		try {
			clearUp();
			final SMTPMessage smtpMessage = new SMTPMessage(javax.mail.Session.getInstance(SMTPSessionProperties
					.getDefaultSessionProperties(), null), new UnsynchronizedByteArrayInputStream(asciiBytes));
			/*
			 * Check recipients
			 */
			final Address[] allRecipients = smtpMessage.getAllRecipients();
			if (allRecipients == null || allRecipients.length == 0) {
				throw new SMTPException(SMTPException.Code.MISSING_RECIPIENTS);
			}
			try {
				final long start = System.currentTimeMillis();
				final Transport transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
				if (SMTPConfig.isSmtpAuth()) {
					final SMTPConfig config = getTransportConfig();
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
				throw SMTPException.handleMessagingException(e);
			}
			return MIMEMessageConverter.convertMessage(smtpMessage);
		} catch (final MessagingException e) {
			throw SMTPException.handleMessagingException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.transport.MailTransport#sendMailMessage(com.openexchange.mail.dataobjects.MailMessage,
	 *      com.openexchange.mail.transport.SendType)
	 */
	@Override
	public MailMessage sendMailMessage(final ComposedMailMessage composedMail, final ComposeType sendType)
			throws MailException {
		try {
			clearUp();
			final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession());
			/*
			 * Fill message dependent on send type
			 */
			final long startPrep = System.currentTimeMillis();
			smtpFiller = new SMTPMessageFiller(session, ctx, usm);
			if (composedMail.getReferencedMailsSize() == 1) {
				tempIds = ReferencedMailPart.loadReferencedParts(composedMail, session);
			}
			if (ComposeType.FORWARD.equals(sendType)
					&& (usm.isForwardAsAttachment() || composedMail.getReferencedMailsSize() > 1)) {
				smtpFiller.fillMail((SMTPMailMessage) composedMail, smtpMessage, sendType, composedMail
						.getReferencedMails());
			} else {
				smtpFiller.fillMail((SMTPMailMessage) composedMail, smtpMessage, sendType);
			}
			/*
			 * Check recipients
			 */
			final Address[] allRecipients = smtpMessage.getAllRecipients();
			if (allRecipients == null || allRecipients.length == 0) {
				throw new SMTPException(SMTPException.Code.MISSING_RECIPIENTS);
			}
			smtpFiller.setSendHeaders(composedMail, smtpMessage);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(128).append("SMTP mail prepared for transport in ").append(
						System.currentTimeMillis() - startPrep).append("msec").toString());
			}
			try {
				final long start = System.currentTimeMillis();
				final Transport transport = getSMTPSession().getTransport(SMTPProvider.PROTOCOL_SMTP.getName());
				if (SMTPConfig.isSmtpAuth()) {
					final SMTPConfig config = getTransportConfig();
					transport.connect(config.getServer(), config.getPort(), config.getLogin(), encodePassword(config
							.getPassword()));
				} else {
					transport.connect();
				}
				try {
					smtpMessage.saveChanges();
					/*
					 * TODO: Do encryption here
					 */
					transport.sendMessage(smtpMessage, allRecipients);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				} finally {
					transport.close();
				}
			} catch (final MessagingException e) {
				throw SMTPException.handleMessagingException(e);
			}
			return MIMEMessageConverter.convertMessage(smtpMessage);
		} catch (final MessagingException e) {
			throw SMTPException.handleMessagingException(e);
		} catch (final IOException e) {
			throw new SMTPException(SMTPException.Code.IO_ERROR, e, e.getLocalizedMessage());
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
			}
		}
		return tmpPass;
	}

	@Override
	protected void shutdown() throws MailException {
		SMTPSessionProperties.resetDefaultSessionProperties();
	}

	@Override
	protected void startup() throws MailException {
	}
}
