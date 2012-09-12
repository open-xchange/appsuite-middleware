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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.zmal.transport;

import static com.openexchange.mail.mime.converters.MimeMessageConverter.saveChanges;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import static com.openexchange.mail.text.TextProcessing.performLineFolding;
import static java.util.regex.Matcher.quoteReplacement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Charsets;
import com.openexchange.java.Java7ConcurrentLinkedQueue;
import com.openexchange.java.Streams;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeHeaderNameChecker;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.openexchange.zmal.Services;
import com.openexchange.zmal.ZmalAccess;
import com.openexchange.zmal.ZmalException;
import com.openexchange.zmal.ZmalSoapPerformer;
import com.openexchange.zmal.config.ZmalConfig;
import com.openexchange.zmal.transport.config.ZTransConfig;
import com.openexchange.zmal.transport.filler.ZTransMessageFiller;
import com.sun.mail.smtp.SMTPMessage;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.zclient.ZEmailAddress;
import com.zimbra.cs.zclient.ZMailbox;
import com.zimbra.cs.zclient.ZMailbox.ZOutgoingMessage;

/**
 * {@link ZTransTransport} - The SMTP mail transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZTransTransport extends MailTransport {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ZTransTransport.class));

    private static volatile String staticHostName;

    private static volatile UnknownHostException warnSpam;

    static {
        try {
            staticHostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            staticHostName = "localhost";
            warnSpam = e;
        }
    }

    private final Queue<Runnable> pendingInvocations;
    private final int accountId;
    private final Session session;
    private final Context ctx;
    private final UserSettingMail usm;
    private volatile ZTransConfig cachedZmalConfig;

    protected ZTransTransport() {
        super();
        accountId = MailAccount.DEFAULT_ID;
        session = null;
        ctx = null;
        usm = null;
        pendingInvocations = new Java7ConcurrentLinkedQueue<Runnable>();
    }

    /**
     * Constructor
     *
     * @param session The session
     * @throws OXException If initialization fails
     */
    public ZTransTransport(final Session session) throws OXException {
        this(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Constructor
     *
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If initialization fails
     */
    public ZTransTransport(final Session session, final int accountId) throws OXException {
        super();
        pendingInvocations = new ConcurrentLinkedQueue<Runnable>();
        this.session = session;
        this.accountId = accountId;
        if (session == null) {
            /*
             * Dummy instance
             */
            ctx = null;
            usm = null;
        } else {
            try {
                ctx = ContextStorage.getStorageContext(session.getContextId());
            } catch (final OXException e) {
                throw e;
            }
            usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
        }
    }

    private ZMailbox.Options newOptions(final ZmalConfig zmalConfig, final ZmalSoapPerformer performer) {
        final ZMailbox.Options options = new ZMailbox.Options(performer.getAuthToken(), performer.getUrl());
        options.setRequestProtocol(performer.isUseJson() ? SoapProtocol.SoapJS : SoapProtocol.Soap11);
        options.setResponseProtocol(performer.isUseJson() ? SoapProtocol.SoapJS : SoapProtocol.Soap11);
        final int timeout = zmalConfig.getZmalProperties().getZmalTimeout();
        if (timeout > 0) {
            options.setTimeout(timeout);
        }
        options.setUserAgent("Open-Xchange Http Client", "v6.22");
        return options;
    }

    private void clearUp() {
        doInvocations();
    }

    @Override
    public void close() {
        clearUp();
    }

    /**
     * Executes all tasks queued for execution
     */
    private void doInvocations() {
        while (!pendingInvocations.isEmpty()) {
            final Runnable task = pendingInvocations.poll();
            if (null != task) {
                task.run();
            }
        }
    }

    /**
     * Executes the given task. This method returns as soon as the task is scheduled, without waiting for it to be executed.
     *
     * @param task The task to be executed.
     */
    private void invokeLater(final Runnable task) {
        pendingInvocations.offer(task);
    }

    @Override
    public TransportConfig getTransportConfig() throws OXException {
        return getTransportConfig0();
    }

    private ZTransConfig getTransportConfig0() throws OXException {
        ZTransConfig tmp = cachedZmalConfig;
        if (tmp == null) {
            synchronized (this) {
                tmp = cachedZmalConfig;
                if (tmp == null) {
                    final ZmalConfig zmalConfig = MailConfig.getConfig(new ZmalConfig(accountId), session, accountId);
                    tmp = new ZTransConfig(zmalConfig);
                    cachedZmalConfig = tmp;
                }
            }
        }
        return tmp;
    }

    private static final String ACK_TEXT =
        "Reporting-UA: OPEN-XCHANGE - WebMail\r\nFinal-Recipient: rfc822; #FROM#\r\n" + "Original-Message-ID: #MSG ID#\r\nDisposition: manual-action/MDN-sent-manually; displayed\r\n";

    private static final String CT_TEXT_PLAIN = "text/plain; charset=#CS#";

    private static final String CT_READ_ACK = "message/disposition-notification; name=MDNPart1.txt; charset=UTF-8";

    private static final String CD_READ_ACK = "attachment; filename=MDNPart1.txt";

    private static final String MULTI_SUBTYPE_REPORT = "report; report-type=disposition-notification";

    @Override
    public void sendReceiptAck(final MailMessage srcMail, final String fromAddr) throws OXException {
        ZTransConfig smtpConfig = null;
        try {
            final InternetAddress dispNotification = srcMail.getDispositionNotification();
            if (dispNotification == null) {
                throw ZTransExceptionCode.MISSING_NOTIFICATION_HEADER.create(MessageHeaders.HDR_DISP_TO, Long.valueOf(srcMail.getMailId()));
            }
            final SMTPMessage smtpMessage = new SMTPMessage(MimeDefaultSession.getDefaultSession());
            final String userMail = UserStorage.getStorageUser(session.getUserId(), ctx).getMail();
            /*
             * Set from
             */
            final String from;
            if (fromAddr == null) {
                if ((usm.getSendAddr() == null) && (userMail == null)) {
                    throw ZTransExceptionCode.NO_SEND_ADDRESS_FOUND.create();
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
            final Locale locale = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale();
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
            new ZTransMessageFiller(session, ctx, usm).setCommonHeaders(smtpMessage);
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
                text.setText(
                    performLineFolding(
                        strHelper.getString(MailStrings.ACK_NOTIFICATION_TEXT).replaceFirst(
                            "#DATE#",
                            sentDate == null ? "" : quoteReplacement(DateFormat.getDateInstance(DateFormat.LONG, locale).format(sentDate))).replaceFirst(
                            "#RECIPIENT#",
                            quoteReplacement(from)).replaceFirst("#SUBJECT#", quoteReplacement(srcMail.getSubject())),
                        usm.getAutoLinebreak()),
                    defaultMimeCS);
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
                ack.setText(
                    strHelper.getString(ACK_TEXT).replaceFirst("#FROM#", quoteReplacement(from)).replaceFirst(
                        "#MSG ID#",
                        quoteReplacement(msgId)),
                    defaultMimeCS);
                ack.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                ack.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                ack.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, CD_READ_ACK);
                mixedMultipart.addBodyPart(ack);
            }
            /*
             * Set message content
             */
            smtpMessage.setContent(mixedMultipart);
            saveChangesSafe(smtpMessage);
            /*
             * Transport message
             */
            ZmalAccess zmalAccess = null;
            try {
                zmalAccess = new ZmalAccess(session, accountId);
                zmalAccess.connect(false);
                smtpConfig = new ZTransConfig(zmalAccess.getZmalConfig());
                final ZMailbox mailbox = new ZMailbox(newOptions(zmalAccess.getZmalConfig(), zmalAccess.getPerformer()));
                mailbox.sendMessage(convert(smtpMessage), null, false);
            } finally {
                if (null != zmalAccess) {
                    zmalAccess.close(false);
                }
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, smtpConfig);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailMessage sendRawMessage(final byte[] asciiBytes, final Address[] allRecipients) throws OXException {
        ZTransConfig smtpConfig = null;
        try {
            final SMTPMessage smtpMessage = new SMTPMessage(MimeDefaultSession.getDefaultSession(), MimeHeaderNameChecker.sanitizeHeaderNames(asciiBytes));
            smtpMessage.removeHeader("x-original-headers");
            /*
             * Check recipients
             */
            final Address[] recipients = allRecipients == null ? smtpMessage.getAllRecipients() : allRecipients;
            checkRecipients(recipients);
            saveChangesSafe(smtpMessage);
            /*
             * Transport message
             */
            ZmalAccess zmalAccess = null;
            try {
                zmalAccess = new ZmalAccess(session, accountId);
                zmalAccess.connect(false);
                smtpConfig = new ZTransConfig(zmalAccess.getZmalConfig());
                final ZMailbox mailbox = new ZMailbox(newOptions(zmalAccess.getZmalConfig(), zmalAccess.getPerformer()));
                mailbox.sendMessage(convert(smtpMessage), null, false);
            } finally {
                if (null != zmalAccess) {
                    zmalAccess.close(false);
                }
            }
            return MimeMessageConverter.convertMessage(smtpMessage);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, smtpConfig);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailMessage sendMailMessage(final ComposedMailMessage composedMail, final ComposeType sendType, final Address[] allRecipients) throws OXException {
        ZTransConfig smtpConfig = null;
        try {
            final SMTPMessage smtpMessage = new SMTPMessage(MimeDefaultSession.getDefaultSession());
            /*
             * Fill message dependent on send type
             */
            final long startPrep = System.currentTimeMillis();
            final ZTransMessageFiller smtpFiller = new ZTransMessageFiller(session, ctx, usm);
            composedMail.setFiller(smtpFiller);
            try {
                smtpFiller.fillMail(composedMail, smtpMessage, sendType);
                /*
                 * Check recipients
                 */
                final Address[] recipients;
                if (allRecipients == null) {
                    if (composedMail.hasRecipients()) {
                        recipients = composedMail.getRecipients();
                    } else {
                        recipients = smtpMessage.getAllRecipients();
                    }
                } else {
                    recipients = allRecipients;
                }
                checkRecipients(recipients);
                smtpFiller.setSendHeaders(composedMail, smtpMessage);
                /*
                 * Drop special "x-original-headers" header
                 */
                smtpMessage.removeHeader("x-original-headers");
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder(128).append("SMTP mail prepared for transport in ").append(
                        System.currentTimeMillis() - startPrep).append("msec").toString());
                }
                saveChangesSafe(smtpMessage);
                /*
                 * Transport message
                 */
                ZmalAccess zmalAccess = null;
                try {
                    zmalAccess = new ZmalAccess(session, accountId);
                    zmalAccess.connect(false);
                    smtpConfig = new ZTransConfig(zmalAccess.getZmalConfig());
                    final ZMailbox mailbox = new ZMailbox(newOptions(zmalAccess.getZmalConfig(), zmalAccess.getPerformer()));
                    mailbox.sendMessage(convert(smtpMessage), null, false);
                } finally {
                    if (null != zmalAccess) {
                        zmalAccess.close(false);
                    }
                }
            } finally {
                invokeLater(new MailCleanerTask(composedMail));
            }
            return MimeMessageConverter.convertMessage(smtpMessage);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, smtpConfig);
        } catch (final IOException e) {
            throw ZTransExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    protected void shutdown() {
        // Nope
    }

    @Override
    protected void startup() {
        // Nope
    }

    private static void checkRecipients(final Address[] recipients) throws OXException {
        if ((recipients == null) || (recipients.length == 0)) {
            throw ZTransExceptionCode.MISSING_RECIPIENTS.create();
        }
        final ConfigurationService service = Services.getService(ConfigurationService.class);
        if (null != service) {
            final Filter filter = service.getFilterFromProperty("com.openexchange.mail.transport.redirectWhitelist");
            if (null != filter) {
                for (final Address address : recipients) {
                    final InternetAddress internetAddress = (InternetAddress) address;
                    if (!filter.accepts(internetAddress.getAddress())) {
                        throw ZTransExceptionCode.RECIPIENT_NOT_ALLOWED.create(internetAddress.toUnicodeString());
                    }
                }
            }
        }
    }

    private static final class MailCleanerTask implements Runnable {

        private final ComposedMailMessage composedMail;

        public MailCleanerTask(final ComposedMailMessage composedMail) {
            super();
            this.composedMail = composedMail;
        }

        @Override
        public void run() {
            composedMail.cleanUp();
        }

    }

    @Override
    public void ping() throws OXException {
        ZmalAccess zmalAccess = null;
        try {
            zmalAccess = new ZmalAccess(session, accountId);
            zmalAccess.ping();
        } finally {
            if (null != zmalAccess) {
                zmalAccess.close(false);
            }
        }
    }

    @Override
    protected ITransportProperties createNewMailProperties() throws OXException {
        return null;
    }

    private void saveChangesSafe(final SMTPMessage smtpMessage) throws OXException {
        try {
            saveChanges(smtpMessage);
            /*
             * Change Message-Id header appropriately
             */
            final String messageId = smtpMessage.getHeader("Message-ID", null);
            if (null != messageId) {
                /*
                 * Somewhat of: <744810669.1.1314981157714.JavaMail.username@host.com>
                 */
                final HostnameService hostnameService = Services.getService(HostnameService.class);
                String hostName;
                if (null == hostnameService) {
                    hostName = getHostName();
                } else {
                    hostName = hostnameService.getHostname(session.getUserId(), session.getContextId());
                }
                if (null == hostName) {
                    hostName = getHostName();
                }
                final int pos = messageId.indexOf('@');
                if (pos > 0 ) {
                    final StringBuilder mid = new StringBuilder(messageId.substring(0, pos + 1)).append(hostName);
                    if (messageId.charAt(0) == '<') {
                        mid.append('>');
                    }
                    smtpMessage.setHeader("Message-ID", mid.toString());
                } else {
                    smtpMessage.setHeader("Message-ID", messageId + hostName);
                }
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static String getHostName() {
        final Props logProperties = LogProperties.optLogProperties();
        if (null == logProperties) {
            return getStaticHostName();
        }
        final String serverName = logProperties.get("com.openexchange.ajp13.serverName");
        if (null == serverName) {
            return getStaticHostName();
        }
        return serverName;
    }

    private static String getStaticHostName() {
        final UnknownHostException warning = warnSpam;
        if (warning != null) {
            LOG.error("Can't resolve my own hostname, using 'localhost' instead, which is certainly not what you want!", warning);
        }
        return staticHostName;
    }

    private static ZOutgoingMessage convert(final SMTPMessage message) throws OXException {
        try {
            final ZOutgoingMessage msg = new ZOutgoingMessage();
            // Addresses
            final List<ZEmailAddress> addresses = new ArrayList<ZEmailAddress>();
            Address[] addrs = message.getFrom();
            if (null != addrs) {
                for (final Address address : addrs) {
                    final InternetAddress addr = (InternetAddress) address;
                    addresses.add(new ZEmailAddress(addr.getAddress(), null, addr.getPersonal(), ZEmailAddress.EMAIL_TYPE_FROM));
                }
            }
            addrs = message.getRecipients(RecipientType.TO);
            if (null != addrs) {
                for (final Address address : addrs) {
                    final InternetAddress addr = (InternetAddress) address;
                    addresses.add(new ZEmailAddress(addr.getAddress(), null, addr.getPersonal(), ZEmailAddress.EMAIL_TYPE_TO));
                }
            }
            addrs = message.getRecipients(RecipientType.CC);
            if (null != addrs) {
                for (final Address address : addrs) {
                    final InternetAddress addr = (InternetAddress) address;
                    addresses.add(new ZEmailAddress(addr.getAddress(), null, addr.getPersonal(), ZEmailAddress.EMAIL_TYPE_CC));
                }
            }
            addrs = message.getRecipients(RecipientType.BCC);
            if (null != addrs) {
                for (final Address address : addrs) {
                    final InternetAddress addr = (InternetAddress) address;
                    addresses.add(new ZEmailAddress(addr.getAddress(), null, addr.getPersonal(), ZEmailAddress.EMAIL_TYPE_BCC));
                }
            }
            msg.setAddresses(addresses);
            // Subject
            msg.setSubject(message.getSubject());
            // In-Reply-To
            {
                final String s = message.getHeader("In-Reply-To", null);
                if (!isEmpty(s)) {
                    msg.setInReplyTo(s);
                }
            }
            // Priority
            final String importance = message.getHeader(MessageHeaders.HDR_IMPORTANCE, null);
            if (null == importance) {
                String priorityStr = message.getHeader(MessageHeaders.HDR_X_PRIORITY, null);
                if (null != priorityStr) {
                    msg.setPriority(priorityStr);
                }
            } else {
                msg.setPriority(importance);
            }
            // Assemble body
            msg.setMessagePart(toMessagePart(message));
            return msg;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static ZOutgoingMessage.MessagePart toMessagePart(final Part part) throws OXException {
        if (null == part) {
            return null;
        }
        try {
            final ContentType contentType;
            {
                final String[] s = part.getHeader("Content-Type");
                contentType = isEmpty(s) ? ContentType.DEFAULT_CONTENT_TYPE : new ContentType(s[0]);
            }
            final ZOutgoingMessage.MessagePart retval;
            if (contentType.startsWith("multipart/")) {
                final Multipart multipart = (Multipart) part.getContent();
                final int count = multipart.getCount();
                final ZOutgoingMessage.MessagePart[] subparts = new ZOutgoingMessage.MessagePart[count];
                for (int i = 0; i < count; i++) {
                    subparts[i] = toMessagePart(multipart.getBodyPart(i));
                }
                final String subType = contentType.getSubType();
                retval = new ZOutgoingMessage.MessagePart("multipart/" + (isEmpty(subType) ? "mixed" : subType), subparts);
            } else {
                // Plain part
                final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(4096);
                part.writeTo(out);
                if (contentType.startsWith("text/")) {
                    final String cs = contentType.getCharsetParameter();
                    final Charset charset = isEmpty(cs) ? Charsets.ISO_8859_1 : Charsets.forName(cs);
                    retval = new ZOutgoingMessage.MessagePart(contentType.toString(), new String(out.toByteArray(), charset));
                } else {
                    retval = new ZOutgoingMessage.MessagePart(contentType.toString(), new String(out.toByteArray(), Charsets.ISO_8859_1));
                }
            }
            return retval;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static boolean isEmpty(final String[] arr) {
        if (null == arr) {
            return true;
        }
        final int len = arr.length;
        boolean isEmpty = true;
        for (int i = 0; isEmpty && i < len; i++) {
            isEmpty = isEmpty(arr[i]);
        }
        return isEmpty;
    }

}
