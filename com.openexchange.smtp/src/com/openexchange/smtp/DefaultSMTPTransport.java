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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import static com.openexchange.mail.text.TextProcessing.performLineFolding;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.Subject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.MailAccountSMTPProperties;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.smtp.filler.SMTPMessageFiller;
import com.openexchange.smtp.services.Services;
import com.openexchange.user.UserService;
import com.sun.mail.smtp.SMTPMessage;

/**
 * {@link DefaultSMTPTransport} - The SMTP mail transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultSMTPTransport extends AbstractSMTPTransport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSMTPTransport.class);

    private static final String ACK_TEXT =
        "Reporting-UA: OPEN-XCHANGE - WebMail\r\nFinal-Recipient: rfc822; #FROM#\r\n" + "Original-Message-ID: #MSG ID#\r\nDisposition: manual-action/MDN-sent-manually; displayed\r\n";

    private static final String CT_TEXT_PLAIN = "text/plain; charset=#CS#";

    private static final String CT_READ_ACK = "message/disposition-notification; name=MDNPart1.txt; charset=UTF-8";

    private static final String CD_READ_ACK = "attachment; filename=MDNPart1.txt";

    private static final String MULTI_SUBTYPE_REPORT = "report; report-type=disposition-notification";

    private static final String KERBEROS_SESSION_SUBJECT = "kerberosSubject";

    private final UserSettingMail usm;

    protected DefaultSMTPTransport() {
        super();
        usm = null;
    }

    /**
     * Constructor
     *
     * @param session The session
     * @throws OXException If initialization fails
     */
    public DefaultSMTPTransport(final Session session) throws OXException {
        this(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Constructor
     *
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If initialization fails
     */
    public DefaultSMTPTransport(final Session session, final int accountId) throws OXException {
        super(session, accountId);
        this.usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
        setUser(Services.getService(UserService.class).getUser(session.getUserId(), ctx));
        setKerberosSubject((Subject) session.getParameter(KERBEROS_SESSION_SUBJECT));
    }

    @Override
    protected void setReplyHeaders(MimeMessage mimeMessage, MailPath msgref) throws OXException, MessagingException {
        MailAccess<?, ?> access = null;
        try {
            access = MailAccess.getInstance(session, msgref.getAccountId());
            access.connect();
            MimeMessageFiller.setReplyHeaders(access.getMessageStorage().getMessage(msgref.getFolder(), msgref.getMailID(), false), mimeMessage);
        } finally {
            if (null != access) {
                access.close(true);
            }
        }
    }

    @Override
    protected SMTPMessageFiller createSMTPMessageFiller(UserSettingMail optMailSettings) throws OXException {
        return new SMTPMessageFiller(getTransportConfig().getSMTPProperties(), session, ctx, null == optMailSettings ? usm : optMailSettings);
    }

    @Override
    protected SMTPConfig createSMTPConfig() throws OXException {
        SMTPConfig tmp = TransportConfig.getTransportConfig(new SMTPConfig(), session, accountId);
        tmp.setTransportProperties(createNewMailProperties());
        tmp.setSession(session);
        return tmp;
    }

    @Override
    protected OXException handleMessagingException(MessagingException e, MailConfig config) throws OXException {
        return MimeMailException.handleMessagingException(e, config, session);
    }

    @Override
    protected void logMessageTransport(final MimeMessage smtpMessage, final SMTPConfig smtpConfig) throws OXException, MessagingException {
        if (getTransportConfig().getSMTPProperties().isLogTransport()) {
            LogProperties.putSessionProperties(session);
            LOG.info("Sent \"{}\" for login \"{}\" using SMTP server \"{}\" on port {}.", smtpMessage.getMessageID(), smtpConfig.getLogin(), smtpConfig.getServer(), Integer.valueOf(smtpConfig.getPort()));
        }
    }

    @Override
    public void sendReceiptAck(final MailMessage srcMail, final String fromAddr) throws OXException {
        if (null == srcMail) {
            return;
        }
        SMTPConfig smtpConfig = null;
        try {
            InternetAddress dispNotification = srcMail.getDispositionNotification();
            if (dispNotification == null) {
                InternetAddress[] from = srcMail.getFrom();
                if (from != null && from.length > 0) {
                    dispNotification = from[0];
                }

                if (null == dispNotification) {
                    throw SMTPExceptionCode.MISSING_NOTIFICATION_HEADER.create(MessageHeaders.HDR_DISP_TO, Long.valueOf(srcMail.getMailId()));
                }
            }
            final SMTPMessage smtpMessage = new SMTPMessage(getSMTPSession());
            final String userMail = UserStorage.getInstance().getUser(session.getUserId(), ctx).getMail();
            /*
             * Set from
             */
            final String from;
            if (fromAddr == null) {
                if ((usm.getSendAddr() == null) && (userMail == null)) {
                    throw SMTPExceptionCode.NO_SEND_ADDRESS_FOUND.create();
                }
                from = usm.getSendAddr() == null ? userMail : usm.getSendAddr();
            } else {
                from = fromAddr;
            }
            smtpMessage.addFrom(parseAddressList(from, false));
            /*
             * Set to
             */
            final Address[] recipients = new Address[] { dispNotification };
            processAddressHeader(smtpMessage);
            checkRecipients(recipients);
            smtpMessage.addRecipients(RecipientType.TO, recipients);
            /*
             * Set header
             */
            smtpMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, "3 (normal)");
            smtpMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Medium");
            /*
             * Subject
             */
            final Locale locale = UserStorage.getInstance().getUser(session.getUserId(), ctx).getLocale();
            final StringHelper strHelper = StringHelper.valueOf(locale);
            smtpMessage.setSubject(strHelper.getString(MailStrings.ACK_SUBJECT));
            /*
             * Sent date in UTC time
             */
            {
                final MailDateFormat mdf = MimeMessageUtility.getMailDateFormat(session);
                synchronized (mdf) {
                    smtpMessage.setHeader("Date", mdf.format(new Date()));
                }
            }
            /*
             * Set common headers
             */
            smtpConfig = getTransportConfig();
            new SMTPMessageFiller(smtpConfig.getSMTPProperties(), session, ctx, usm).setAccountId(accountId).setCommonHeaders(smtpMessage);
            /*
             * Compose body
             */
            final String defaultMimeCS = MailProperties.getInstance().getDefaultMimeCharset();
            final ContentType ct = new ContentType(CT_TEXT_PLAIN.replaceFirst("#CS#", defaultMimeCS));
            final Multipart mixedMultipart = new MimeMultipart(MULTI_SUBTYPE_REPORT);
            /*
             * Define text content
             */
            final Date sentDate = srcMail.getSentDate();
            {
                final MimeBodyPart text = new MimeBodyPart();
                final String txt = performLineFolding(
                    strHelper.getString(MailStrings.ACK_NOTIFICATION_TEXT)
                    .replaceFirst("#DATE#", sentDate == null ? "" : quoteReplacement(DateFormat.getDateInstance(DateFormat.LONG, locale).format(sentDate)))
                    .replaceFirst("#RECIPIENT#", quoteReplacement(from)).replaceFirst("#SUBJECT#", quoteReplacement(srcMail.getSubject())), usm.getAutoLinebreak());
                MessageUtility.setText(txt, defaultMimeCS, text);
                // text.setText(txt,defaultMimeCS);
                text.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                mixedMultipart.addBodyPart(text);
            }
            /*
             * Define ack
             */
            ct.setContentType(CT_READ_ACK);
            {
                final MimeBodyPart ack = new MimeBodyPart();
                final String msgId = srcMail.getFirstHeader(MessageHeaders.HDR_MESSAGE_ID);
                final String txt = strHelper.getString(ACK_TEXT).replaceFirst("#FROM#", quoteReplacement(from)).replaceFirst(
                    "#MSG ID#",
                    quoteReplacement(msgId));
                MessageUtility.setText(txt, defaultMimeCS, ack);
                // ack.setText(txt,defaultMimeCS);
                ack.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                ack.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                ack.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, CD_READ_ACK);
                mixedMultipart.addBodyPart(ack);
            }
            /*
             * Set message content
             */
            MessageUtility.setContent(mixedMultipart, smtpMessage);
            // smtpMessage.setContent(mixedMultipart);
            /*
             * Transport message
             */
            final long start = System.currentTimeMillis();
            final Transport transport = getSMTPSession().getTransport(SMTP);
            try {
                connectTransport(transport, smtpConfig);
                saveChangesSafe(smtpMessage, true);
                transport(smtpMessage, smtpMessage.getAllRecipients(), transport, smtpConfig);
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            } catch (final javax.mail.AuthenticationFailedException e) {
                throw MimeMailExceptionCode.TRANSPORT_INVALID_CREDENTIALS.create(e, smtpConfig.getServer(), e.getMessage());
            } finally {
                transport.close();
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, smtpConfig, session);
        }
    }

    @Override
    protected ITransportProperties createNewMailProperties() throws OXException {
        MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
        int contextId = session.getContextId();
        int userId = session.getUserId();
        if (storageService.existsMailAccount(accountId, userId, contextId)) {
            return new MailAccountSMTPProperties(storageService.getMailAccount(accountId, userId, contextId));
        }

        // Fall-back...
        return new MailAccountSMTPProperties(accountId);
    }

    private static String quoteReplacement(final String str) {
        return com.openexchange.java.Strings.isEmpty(str) ? "" : quoteReplacement0(str);
    }

    private static String quoteReplacement0(final String s) {
        if ((s.indexOf('\\') < 0) && (s.indexOf('$') < 0)) {
            return s;
        }
        final int length = s.length();
        final StringBuilder sb = new StringBuilder(length << 1);
        for (int i = 0; i < length; i++) {
            final char c = s.charAt(i);
            if (c == '\\') {
                sb.append('\\');
                sb.append('\\');
            } else if (c == '$') {
                sb.append('\\');
                sb.append('$');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
