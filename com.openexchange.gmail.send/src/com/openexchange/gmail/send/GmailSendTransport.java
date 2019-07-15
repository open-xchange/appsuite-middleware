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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.gmail.send;

import static com.openexchange.gmail.send.GmailAccess.accessFor;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import static com.openexchange.mail.text.TextProcessing.performLineFolding;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
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
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.Messages.Send;
import com.google.api.services.gmail.model.Message;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.gmail.send.config.GmailSendConfig;
import com.openexchange.gmail.send.config.GmailSendSessionProperties;
import com.openexchange.gmail.send.config.MailAccountGmailSendProperties;
import com.openexchange.gmail.send.filler.GmailSendMessageFiller;
import com.openexchange.gmail.send.http.RetryingHttpRequestInitializer;
import com.openexchange.gmail.send.services.Services;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;
import com.openexchange.java.util.MsisdnCheck;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAware;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeHeaderNameChecker;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mail.transport.listener.Reply;
import com.openexchange.mail.transport.listener.Result;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mailaccount.Account;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.oauth.API;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link GmailSendTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class GmailSendTransport extends MailTransport {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GmailSendTransport.class);

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

    private final int accountId;
    private final Context ctx;
    private final User user;
    private final Session session;
    private final UserSettingMail usm;
    private final Queue<Runnable> pendingInvocations;
    private volatile GmailSendConfig cachedMailSendConfig;
    private volatile javax.mail.Session javaMailSession;

    /**
     * Initializes a new {@link GmailSendTransport}.
     */
    public GmailSendTransport() {
        super();
        accountId = MailAccount.DEFAULT_ID;
        ctx = null;
        user = null;
        session = null;
        usm = null;
        pendingInvocations = new ConcurrentLinkedQueue<Runnable>();
    }

    /**
     * Constructor
     *
     * @param session The session
     * @throws OXException If initialization fails
     */
    public GmailSendTransport(Session session) throws OXException {
        this(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Constructor
     *
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If initialization fails
     */
    public GmailSendTransport(Session session, int accountId) throws OXException {
        super();
        this.session = session;
        this.ctx = Services.getService(ContextService.class).getContext(session.getContextId());
        this.user = Services.getService(UserService.class).getUser(session.getUserId(), ctx);
        this.accountId = accountId;
        usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
        pendingInvocations = new ConcurrentLinkedQueue<Runnable>();
    }

    @Override
    protected boolean supports(AuthType authType) {
        return AuthType.OAUTH == authType;
    }

    private Account requireAccount(GmailSendConfig gmailSendConfig) throws OXException {
        Account account = gmailSendConfig.getAccount();
        if (null != account) {
            return account;
        }

        Session session = this.session;
        if (null == session) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create("Session not set");
        }

        MailAccountStorageService service = Services.optService(MailAccountStorageService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(MailAccountStorageService.class);
        }

        return service.getTransportAccount(accountId, session.getUserId(), session.getContextId());
    }

    /**
     * Handles given messaging error.
     *
     * @param e The messaging error to handle
     * @param config The associated configuration
     * @return The appropriate {@code OXException} instance
     */
    private OXException handleMessagingException(MessagingException e, MailConfig config) {
        return MimeMailException.handleMessagingException(e, config, null);
    }

    /**
     * Logs the successful transport of given MIME message.
     *
     * @param message The returned message model
     * @param gmailSendMessage The transported MIME message
     * @param gmailSendConfig The associated configuration
     * @throws MessagingException If a messaging error occurs
     */
    private void logMessageTransport(Message message, final MimeMessage gmailSendMessage, final GmailSendConfig gmailSendConfig) throws MessagingException {
        if (gmailSendConfig.getGmailSendProperties().isLogTransport()) {
            LOG.info("Sent \"{}\" for login \"{}\" using Gmail Send API.", message == null ? gmailSendMessage.getMessageID() : message.getId(), gmailSendConfig.getLogin());
        }
    }

    /**
     * Connects to Gmail.
     *
     * @param gmailSendConfig The associated Gmail Send configuration
     * @return The connected Gmail instance
     * @throws OXException If connect attempt fails
     */
    protected GmailAccess connectTransport(GmailSendConfig gmailSendConfig) throws OXException {
        return connectTransport(gmailSendConfig, false);
    }

    /**
     * Connects to Gmail.
     *
     * @param gmailSendConfig The associated Gmail Send configuration
     * @param forPing <code>true</code> if it is known that a connection is supposed to be established to an external Gmail Send service, otherwise <code>false</code> if not known
     * @return The connected Gmail instance
     * @throws OXException If connect attempt fails
     */
    protected GmailAccess connectTransport(GmailSendConfig gmailSendConfig, boolean forPing) throws OXException {
        Account account = requireAccount(gmailSendConfig);
        if (false == forPing) {
            if (account.isTransportDisabled()) {
                if (account.isTransportOAuthAble() && account.getTransportOAuthId() >= 0) {
                    OAuthService oauthService = Services.optService(OAuthService.class);
                    if (null != oauthService) {
                        OAuthAccount oAuthAccount;
                        try {
                            oAuthAccount = oauthService.getAccount(session, account.getTransportOAuthId());
                        } catch (Exception x) {
                            LOG.warn("Failed to load transport-associated OAuth account", x);
                            oAuthAccount = null;
                        }
                        if (null != oAuthAccount) {
                            throw MailExceptionCode.MAIL_TRANSPORT_DISABLED_OAUTH.create(gmailSendConfig.getServer(), gmailSendConfig.getLogin(), I(session.getUserId()), I(session.getContextId()), oAuthAccount.getDisplayName());
                        }
                    }
                }

                throw MailExceptionCode.MAIL_TRANSPORT_DISABLED.create(gmailSendConfig.getServer(), gmailSendConfig.getLogin(), I(session.getUserId()), I(session.getContextId()));
            }
            if (TransportAuth.considerAsMailTransportAuth(account.getTransportAuth()) && (account instanceof MailAccount)) {
                MailAccount mailAccount = (MailAccount) account;
                if (mailAccount.isMailDisabled()) {
                    if (mailAccount.isMailOAuthAble() && mailAccount.getMailOAuthId() >= 0) {
                        OAuthService oauthService = Services.optService(OAuthService.class);
                        if (null != oauthService) {
                            try {
                                OAuthAccount oAuthAccount = oauthService.getAccount(session, mailAccount.getMailOAuthId());
                                throw MailExceptionCode.MAIL_TRANSPORT_DISABLED_OAUTH.create(gmailSendConfig.getServer(), gmailSendConfig.getLogin(), I(session.getUserId()), I(session.getContextId()), oAuthAccount.getDisplayName());
                            } catch (Exception x) {
                                LOG.warn("Failed to load mail-associated OAuth account", x);
                            }
                        }
                    }

                    throw MailExceptionCode.MAIL_TRANSPORT_DISABLED.create(gmailSendConfig.getServer(), gmailSendConfig.getLogin(), I(session.getUserId()), I(session.getContextId()));
                }
            }
        }

        OAuthAccount oauthAccount = GoogleApiClients.getGoogleAccount(account.getTransportOAuthId(), session, true);
        {
            OAuthAccount newAccount = GoogleApiClients.ensureNonExpiredGoogleAccount(oauthAccount, session);
            if (null != newAccount) {
                oauthAccount = newAccount;
            }
        }
        verifyAccount(oauthAccount, Services.getService(OAuthService.class), OXScope.mail);

        // Generate appropriate credentials for it
        GoogleCredential credentials = GoogleApiClients.getCredentials(oauthAccount, session);

        // Establish Gmail instance
        HttpRequestInitializer httpRequestInitializer = new RetryingHttpRequestInitializer(credentials, gmailSendConfig);
        Gmail gmail = new Gmail.Builder(credentials.getTransport(), credentials.getJsonFactory(), httpRequestInitializer).setApplicationName(GoogleApiClients.getGoogleProductName(session)).build();
        return accessFor(gmail, oauthAccount);
    }

    /**
     * Verifies the specified {@link OAuthAccount} over validity:
     * <ul>
     * <li>accessToken exists?</li>
     * <li>specified scopes are both available and enabled?</li>
     * <li>the user identity is set? (lazy update)</li>
     * </ul>
     *
     * @param account The {@link OAuthAccount} to check for validity
     * @param oauthService The {@link OAuthService}
     * @param scopes The scopes that are required to be available and enabled as well
     * @throws OXException if the account is not valid
     */
    private void verifyAccount(OAuthAccount account, OAuthService oauthService, OXScope... scopes) throws OXException {
        // Verify that the account has an access token
        if (Strings.isEmpty(account.getToken())) {
            API api = account.getAPI();
            throw OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(api.getName(), I(account.getId()), I(session.getUserId()), I(session.getContextId()));
        }

        // Verify that scopes are available and enabled
        OAuthUtil.checkScopesAvailableAndEnabled(account, session, scopes);

        // Verify if the account has the user identity set, lazy update
        if (Strings.isEmpty(account.getUserIdentity())) {
            String userIdentity = account.getMetaData().getUserIdentity(session, account.getId(), account.getToken(), account.getSecret());
            ((DefaultOAuthAccount) account).setUserIdentity(userIdentity);
            oauthService.updateAccount(session, account.getId(), Collections.singletonMap(OAuthConstants.ARGUMENT_IDENTITY, userIdentity));
        }

        // Other checks?
    }

    @Override
    protected ITransportProperties createNewMailProperties() throws OXException {
        MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
        int contextId = session.getContextId();
        int userId = session.getUserId();
        if (storageService.existsMailAccount(accountId, userId, contextId)) {
            return new MailAccountGmailSendProperties(storageService.getMailAccount(accountId, userId, contextId), userId, contextId);
        }

        // Fall-back...
        return new MailAccountGmailSendProperties(accountId, userId, contextId);
    }

    @Override
    public GmailSendConfig getTransportConfig() throws OXException {
        GmailSendConfig tmp = cachedMailSendConfig;
        if (tmp == null) {
            synchronized (this) {
                tmp = cachedMailSendConfig;
                if (tmp == null) {
                    tmp = TransportConfig.getTransportConfig(new GmailSendConfig(), session, accountId);
                    tmp.setTransportProperties(createNewMailProperties());
                    tmp.setSession(session);
                    cachedMailSendConfig = tmp;
                }
            }
        }
        return tmp;
    }

    @Override
    public void ping() throws OXException {
        GmailAccess gmailAccess = connectTransport(getTransportConfig(), true);
        try {
            Gmail gmail = gmailAccess.gmail;
            gmail.users().getProfile("me").execute();
        } catch (final HttpResponseException e) {
            if (401 == e.getStatusCode() || 403 == e.getStatusCode()) {
                // Not authorized...
                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(gmailAccess.oauthAccount.getDisplayName(), I(gmailAccess.oauthAccount.getId()));
            }
            throw MailExceptionCode.PROTOCOL_ERROR.create(e, "HTTP", e.getStatusCode() + " " + e.getStatusMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailMessage sendRawMessage(byte[] asciiBytes, Address[] allRecipients) throws OXException {
        GmailSendConfig gmailSendConfig = getTransportConfig();
        try {
            MimeMessage mimeMessage = new MimeMessage(getJavaMailSession(), MimeHeaderNameChecker.sanitizeHeaderNames(asciiBytes));
            mimeMessage.removeHeader("x-original-headers");

            // Check recipients
            processAddressHeader(mimeMessage);

            // Save changes
            saveChangesSafe(mimeMessage, true);

            // Check if "poisoned"
            boolean poisoned = checkRecipients(allRecipients == null ? mimeMessage.getAllRecipients() : allRecipients);
            if (poisoned) {
                return MimeMessageConverter.convertMessage(mimeMessage);
            }

            MimeMessage sentMimeMessage = transport(mimeMessage, allRecipients, gmailSendConfig, null);
            return MimeMessageConverter.convertMessage(sentMimeMessage);
        } catch (MessagingException e) {
            throw handleMessagingException(e, gmailSendConfig);
        }
    }

    @Override
    public MailMessage sendRawMessage(byte[] asciiBytes, SendRawProperties properties) throws OXException {
        GmailSendConfig gmailSendConfig = getTransportConfig();
        try {
            // Create input stream to read from
            InputStream rfc822IS;
            if (properties.isSanitizeHeaders()) {
                rfc822IS = MimeHeaderNameChecker.sanitizeHeaderNames(asciiBytes);
            } else {
                rfc822IS = new UnsynchronizedByteArrayInputStream(asciiBytes);
            }

            MimeMessage mimeMessage = new MimeMessage(getJavaMailSession(), rfc822IS);

            // Check recipients
            Address[] recipients = properties.getRecipients();
            if (recipients == null) {
                recipients = mimeMessage.getAllRecipients();
            }
            if (properties.isValidateAddressHeaders()) {
                processAddressHeader(mimeMessage);
            }

            // Save changes
            saveChangesSafe(mimeMessage, true);

            // Transport if non-poisoned
            boolean poisoned = checkRecipients(recipients);
            if (poisoned) {
                return MimeMessageConverter.convertMessage(mimeMessage);
            }

            mimeMessage = transport(mimeMessage, recipients, gmailSendConfig, null);
            return MimeMessageConverter.convertMessage(mimeMessage);
        } catch (MessagingException e) {
            throw handleMessagingException(e, gmailSendConfig);
        }
    }

    @Override
    public void sendRawMessage(InputStream stream, Address[] allRecipients) throws OXException {
        GmailSendConfig gmailSendConfig = getTransportConfig();
        try {
            MimeMessage mimeMessage = new MimeMessage(getJavaMailSession(), stream);
            mimeMessage.removeHeader("x-original-headers");

            // Check recipients
            processAddressHeader(mimeMessage);

            // Save changes
            saveChangesSafe(mimeMessage, true);

            // Check if "poisoned"
            boolean poisoned = checkRecipients(allRecipients == null ? mimeMessage.getAllRecipients() : allRecipients);
            if (poisoned) {
                return;
            }

            transport(mimeMessage, allRecipients, gmailSendConfig, null);
        } catch (MessagingException e) {
            throw handleMessagingException(e, gmailSendConfig);
        }
    }

    @Override
    public void sendRawMessage(InputStream stream, SendRawProperties properties) throws OXException {
        if (properties.isSanitizeHeaders()) {
            // Cannot sanitize stream...
            try {
                sendRawMessage(Streams.stream2bytes(stream), properties);
            } catch (IOException e) {
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
            return;
        }

        GmailSendConfig gmailSendConfig = getTransportConfig();
        try {
            MimeMessage mimeMessage = new MimeMessage(getJavaMailSession(), stream);

            // Check recipients
            Address[] recipients = properties.getRecipients();
            if (recipients == null) {
                recipients = mimeMessage.getAllRecipients();
            }
            if (properties.isValidateAddressHeaders()) {
                processAddressHeader(mimeMessage);
            }

            // Save changes
            saveChangesSafe(mimeMessage, true);

            // Transport if non-poisoned
            boolean poisoned = checkRecipients(recipients);
            if (!poisoned) {
                transport(mimeMessage, recipients, gmailSendConfig, null);
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e, gmailSendConfig, session);
        } finally {
            Streams.close(stream);
        }
    }

    private static final String ACK_TEXT = "Reporting-UA: OPEN-XCHANGE - WebMail\r\nFinal-Recipient: rfc822; #FROM#\r\n" + "Original-Message-ID: #MSG ID#\r\nDisposition: manual-action/MDN-sent-manually; displayed\r\n";

    private static final String CT_TEXT_PLAIN = "text/plain; charset=#CS#";

    private static final String CT_READ_ACK = "message/disposition-notification; name=MDNPart1.txt; charset=UTF-8";

    private static final String CD_READ_ACK = "attachment; filename=MDNPart1.txt";

    private static final String MULTI_SUBTYPE_REPORT = "report; report-type=disposition-notification";

    @Override
    public void sendReceiptAck(MailMessage srcMail, String fromAddr) throws OXException {
        GmailSendConfig gmailSendConfig = getTransportConfig();
        try {
            InternetAddress dispNotification = srcMail.getDispositionNotification();
            if (dispNotification == null) {
                InternetAddress[] from = srcMail.getFrom();
                if (from != null && from.length > 0) {
                    dispNotification = from[0];
                }

                if (null == dispNotification) {
                    throw GmailSendExceptionCode.MISSING_NOTIFICATION_HEADER.create(MessageHeaders.HDR_DISP_TO, Long.valueOf(srcMail.getMailId()));
                }
            }

            MimeMessage mimeMessage = new MimeMessage(getJavaMailSession());
            String userMail = user.getMail();

            // Set from
            String from;
            if (fromAddr == null) {
                if ((usm.getSendAddr() == null) && (userMail == null)) {
                    throw GmailSendExceptionCode.NO_SEND_ADDRESS_FOUND.create();
                }
                from = usm.getSendAddr() == null ? userMail : usm.getSendAddr();
            } else {
                from = fromAddr;
            }
            mimeMessage.addFrom(parseAddressList(from, false));

            // Set to
            final Address[] recipients = new Address[] { dispNotification };
            processAddressHeader(mimeMessage);
            checkRecipients(recipients);
            mimeMessage.addRecipients(RecipientType.TO, recipients);

            // Set header
            mimeMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, "3 (normal)");
            mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Normal");

            // Subject
            final Locale locale = UserStorage.getInstance().getUser(session.getUserId(), ctx).getLocale();
            final StringHelper strHelper = StringHelper.valueOf(locale);
            mimeMessage.setSubject(strHelper.getString(MailStrings.ACK_SUBJECT));

            // Sent date in UTC time
            {
                final MailDateFormat mdf = MimeMessageUtility.getMailDateFormat(session);
                synchronized (mdf) {
                    mimeMessage.setHeader("Date", mdf.format(new Date()));
                }
            }

            // Set common headers
            new GmailSendMessageFiller(gmailSendConfig.getGmailSendProperties(), session, ctx, usm).setAccountId(accountId).setCommonHeaders(mimeMessage);

            // Compose body
            String defaultMimeCS = MailProperties.getInstance().getDefaultMimeCharset();
            ContentType ct = new ContentType(CT_TEXT_PLAIN.replaceFirst("#CS#", defaultMimeCS));
            Multipart mixedMultipart = new MimeMultipart(MULTI_SUBTYPE_REPORT);

            // Define text content
            final Date sentDate = srcMail.getSentDate();
            {
                final MimeBodyPart text = new MimeBodyPart();
                final String txt = performLineFolding(strHelper.getString(MailStrings.ACK_NOTIFICATION_TEXT).replaceFirst("#DATE#", sentDate == null ? "" : quoteReplacement(DateFormat.getDateInstance(DateFormat.LONG, locale).format(sentDate))).replaceFirst("#RECIPIENT#", quoteReplacement(from)).replaceFirst("#SUBJECT#", quoteReplacement(srcMail.getSubject())), usm.getAutoLinebreak());
                MessageUtility.setText(txt, defaultMimeCS, text);
                // text.setText(txt,defaultMimeCS);
                text.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                mixedMultipart.addBodyPart(text);
            }

            // Define ack
            ct.setContentType(CT_READ_ACK);
            {
                final MimeBodyPart ack = new MimeBodyPart();
                final String msgId = srcMail.getFirstHeader(MessageHeaders.HDR_MESSAGE_ID);
                final String txt = strHelper.getString(ACK_TEXT).replaceFirst("#FROM#", quoteReplacement(from)).replaceFirst("#MSG ID#", quoteReplacement(msgId));
                MessageUtility.setText(txt, defaultMimeCS, ack);
                // ack.setText(txt,defaultMimeCS);
                ack.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                ack.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                ack.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, CD_READ_ACK);
                mixedMultipart.addBodyPart(ack);
            }

            // Set message content
            MessageUtility.setContent(mixedMultipart, mimeMessage);
            transport(mimeMessage, recipients, gmailSendConfig, null);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e, gmailSendConfig, session);
        }
    }

    @Override
    public MailMessage sendMailMessage(ComposedMailMessage transportMail, ComposeType sendType, Address[] allRecipients) throws OXException {
        GmailSendConfig gmailSendConfig = getTransportConfig();
        Address[] allRecipientz = allRecipients;
        try {
            // Message content available?
            MimeMessage mimeMessage = null;
            if (transportMail instanceof ContentAware) {
                try {
                    final Object content = transportMail.getContent();
                    if (content instanceof MimeMessage) {
                        mimeMessage = (MimeMessage) content;
                        mimeMessage.removeHeader("x-original-headers");

                        // Check for reply
                        final MailPath msgref = transportMail.getMsgref();
                        if (ComposeType.REPLY.equals(sendType) && msgref != null) {
                            setReplyHeaders(mimeMessage, msgref);
                        }

                        // Set common headers
                        final GmailSendMessageFiller gmailSendFiller = createGmailSendMessageFiller(null);
                        gmailSendFiller.setAccountId(accountId);
                        gmailSendFiller.setCommonHeaders(mimeMessage);
                    }
                } catch (final Exception e) {
                    LOG.trace("Failed to extract MIME message from {} instance", ContentAware.class.getName(), e);
                    mimeMessage = null;
                }
            }

            // Fill from scratch if not yet initialized
            if (mimeMessage == null) {
                mimeMessage = new MimeMessage(getJavaMailSession());
                GmailSendMessageFiller gmailSendMessageFiller = createGmailSendMessageFiller(transportMail.getMailSettings());
                gmailSendMessageFiller.setAccountId(accountId);
                transportMail.setFiller(gmailSendMessageFiller);
                try {
                    // Check for reply
                    final MailPath msgref = transportMail.getMsgref();
                    if (ComposeType.REPLY.equals(sendType) && msgref != null) {
                        setReplyHeaders(mimeMessage, msgref);
                    }

                    // Fill message
                    gmailSendMessageFiller.fillMail(transportMail, mimeMessage, sendType);

                    // Check recipients
                    if (allRecipientz == null) {
                        if (transportMail.hasRecipients()) {
                            allRecipientz = transportMail.getRecipients();
                        } else {
                            allRecipientz = mimeMessage.getAllRecipients();
                        }
                    }

                    // Set send headers
                    gmailSendMessageFiller.setSendHeaders(transportMail, mimeMessage);

                    // Drop special "x-original-headers" header
                    mimeMessage.removeHeader("x-original-headers");
                } finally {
                    invokeLater(new MailCleanerTask(transportMail));
                }
            }

            // Check recipients
            processAddressHeader(mimeMessage);

            // Save changes
            saveChangesSafe(mimeMessage, true);

            // Check if "poisoned"
            boolean poisoned = checkRecipients(allRecipientz);
            if (poisoned) {
                return MimeMessageConverter.convertMessage(mimeMessage);
            }

            // Do the transport
            SecuritySettings securitySettings = transportMail.getSecuritySettings();
            MimeMessage sentMimeMessage = transport(mimeMessage, allRecipientz, gmailSendConfig, securitySettings);
            return MimeMessageConverter.convertMessage(sentMimeMessage);
        } catch (MessagingException e) {
            throw handleMessagingException(e, gmailSendConfig);
        } catch (IOException e) {
            throw GmailSendExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private MimeMessage transport(MimeMessage mimeMessage, Address[] recipients, GmailSendConfig gmailSendConfig, SecuritySettings securitySettings) throws OXException {
        // Prepare addresses
        Address[] recipientz = recipients;
        if (recipientz == null) {
            try {
                recipientz = mimeMessage.getAllRecipients();
            } catch (MessagingException e) {
                throw handleMessagingException(e, getTransportConfig());
            }
        }
        prepareAddresses(recipientz);

        // Grab listener chain instance
        ListenerChain listenerChain = ListenerChain.getInstance();

        // Try to send the message
        MimeMessage messageToSend = mimeMessage;
        GmailAccess gmailAccess = null;
        Exception exception = null;
        try {
            // Check if security settings are given and if properly handled
            if (securitySettings != null && securitySettings.anythingSet()) {
                if (false == listenerChain.checkSettings(securitySettings, session)) {
                    // Security settings not considered
                    throw GmailSendExceptionCode.INTERNAL_ERROR.create("Security settings available, but not handled. Guard-backend-plugin installed?");
                }
            }

            // Check listener chain
            Result result = listenerChain.onBeforeMessageTransport(messageToSend, recipientz, securitySettings, session);

            // Examine reply of the listener chain
            {
                Reply reply = result.getReply();
                if (Reply.PROCESSED.ordinal() >= reply.ordinal()) {
                    // Check if denied
                    if (Reply.DENY == reply) {
                        throw MailExceptionCode.SEND_DENIED.create();
                    }

                    // Just return the processed message
                    return result.getMimeMessage();
                }
                // Check recipient list from result
                recipientz = result.getRecipients();
                // If not recipients, no need to continue sending.
                if (recipientz == null || recipientz.length == 0) {
                    return result.getMimeMessage();
                }
            }

            // Grab possibly new MIME message
            {
                MimeMessage resultingMimeMessage = result.getMimeMessage();
                if (null != resultingMimeMessage) {
                    messageToSend = resultingMimeMessage;
                }
            }

            /*-
             *
            {
                ByteArrayOutputStream sink = new ByteArrayOutputStream();
                messageToSend.writeTo(sink);
                String base64 = Base64.encodeBase64URLSafeString(sink.toByteArray());
                System.out.println(base64);
            }
            */

            // Transport
            gmailAccess = connectTransport(gmailSendConfig);
            Gmail gmail = gmailAccess.gmail;
            Send send = gmail.users().messages().send("me", null, new MimeMessageInputStreamContent(messageToSend));
            com.google.api.services.gmail.model.Message message = send.execute();
            if (gmailSendConfig.getGmailSendProperties().isLogTransport()) {
                logMessageTransport(message, mimeMessage, gmailSendConfig);
            }
        } catch (MessagingException e) {
            exception = e;
            throw handleMessagingException(e, gmailSendConfig);
        } catch (OXException e) {
            exception = e;
            throw e;
        } catch (final HttpResponseException e) {
            exception = e;
            if (401 == e.getStatusCode() || 403 == e.getStatusCode()) {
                // Not authorized...
                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(gmailAccess.oauthAccount.getDisplayName(), I(gmailAccess.oauthAccount.getId()));
            }
            throw MailExceptionCode.PROTOCOL_ERROR.create(e, "HTTP", e.getStatusCode() + " " + e.getStatusMessage());
        } catch (IOException e) {
            exception = e;
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            exception = e;
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            listenerChain.onAfterMessageTransport(messageToSend, exception, session);
        }
        return messageToSend;
    }

    private void prepareAddresses(final Address[] addresses) {
        final int length = addresses.length;
        final StringBuilder tmp = new StringBuilder(32);
        for (int i = 0; i < length; i++) {
            final InternetAddress address = (InternetAddress) addresses[i];
            final String sAddress = address.getAddress();
            if (MsisdnCheck.checkMsisdn(sAddress)) {
                final int pos = sAddress.indexOf('/');
                if (pos < 0) {
                    tmp.setLength(0);
                    address.setAddress(tmp.append(sAddress).append("/TYPE=PLMN").toString());
                }
            }
        }
    }

    /**
     * Performs {@link MimeMessage#saveChanges() saveChanges()} on specified message with sanitizing for a possibly corrupt/wrong Content-Type header.
     * <p>
     * Aligns <i>Message-Id</i> header to given host name.
     *
     * @param mimeMessage The MIME message
     * @param keepMessageIdIfPresent Whether to keep a possibly available <i>Message-ID</i> header or to generate a new (unique) one
     * @throws OXException If operation fails
     */
    private void saveChangesSafe(MimeMessage mimeMessage, boolean keepMessageIdIfPresent) throws OXException {
        String hostName = getHostName();
        MimeMessageUtility.saveChanges(mimeMessage, hostName, keepMessageIdIfPresent);
        // Check whether to remove MIME-Version headers from sub-parts
        if (TransportProperties.getInstance().isRemoveMimeVersionInSubParts()) {
            /*-
             *  Note that the MIME-Version header field is required at the top level
             *  of a message.  It is not required for each body part of a multipart
             *  entity.  It is required for the embedded headers of a body of type
             *  "message/rfc822" or "message/partial" if and only if the embedded
             *  message is itself claimed to be MIME-conformant.
             */
            try {
                checkMimeVersionHeader(mimeMessage);
            } catch (final Exception e) {
                LOG.warn("Could not check for proper usage of \"MIME-Version\" header according to RFC2045.", e);
            }
        }
    }

    private String getHostName() {
        final HostnameService hostnameService = Services.getService(HostnameService.class);
        if (null == hostnameService) {
            return getFallbackHostname();
        }

        String hostName;
        User user = this.user;
        if (null == user) {
            hostName = hostnameService.getHostname(ConfigProviderService.NO_USER, ctx.getContextId());
        } else {
            if (user.isGuest()) {
                hostName = hostnameService.getGuestHostname(user.getId(), ctx.getContextId());
            } else {
                hostName = hostnameService.getHostname(user.getId(), ctx.getContextId());
            }
        }
        if (null == hostName) {
            hostName = getFallbackHostname();
        }
        return hostName;
    }

    private void checkMimeVersionHeader(final MimeMessage mimeMessage) throws MessagingException, IOException {
        final String header = mimeMessage.getHeader("Content-Type", null);
        if (null != header && header.toLowerCase().startsWith("multipart/")) {
            final Multipart multipart = (Multipart) mimeMessage.getContent();
            final int count = multipart.getCount();
            for (int i = 0; i < count; i++) {
                checkMimeVersionHeader(multipart.getBodyPart(i));
            }
        }
    }

    private void checkMimeVersionHeader(final Part part) throws MessagingException, IOException {
        final String[] header = part.getHeader("Content-Type");
        if (null != header && header.length > 0 && null != header[0]) {
            final String cts = header[0].toLowerCase();
            if (cts.startsWith("multipart/")) {
                final Multipart multipart = (Multipart) part.getContent();
                final int count = multipart.getCount();
                for (int i = 0; i < count; i++) {
                    checkMimeVersionHeader(multipart.getBodyPart(i));
                }
            } else if (cts.startsWith("message/rfc822") || cts.startsWith("message/partial")) {
                part.setHeader("MIME-Version", "1.0");
                Object content;
                try {
                    content = part.getContent();
                } catch (Exception e) {
                    LOG.trace("Failed to acquire MIME part's content", e);
                    content = null;
                }
                if (content instanceof MimeMessage) {
                    checkMimeVersionHeader((MimeMessage) content);
                }
            } else {
                part.removeHeader("MIME-Version");
            }
        } else {
            part.removeHeader("MIME-Version");
        }
    }

    private String getFallbackHostname() {
        final String serverName = LogProperties.getLogProperty(LogProperties.Name.GRIZZLY_SERVER_NAME);
        return null == serverName ? getStaticHostName() : serverName;
    }

    private static String getStaticHostName() {
        final UnknownHostException warning = warnSpam;
        if (warning != null) {
            LOG.error("Can't resolve my own hostname, using 'localhost' instead, which is certainly not what you want!", warning);
        }
        return staticHostName;
    }

    private void processAddressHeader(final MimeMessage mimeMessage) throws OXException, MessagingException {
        {
            final String str = mimeMessage.getHeader("From", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setFrom(addresses[0]);
            }
        }
        {
            final String str = mimeMessage.getHeader("Sender", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setSender(addresses[0]);
            }
        }
        {
            final String str = mimeMessage.getHeader("To", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setRecipients(RecipientType.TO, addresses);
            }
        }
        {
            final String str = mimeMessage.getHeader("Cc", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setRecipients(RecipientType.CC, addresses);
            }
        }
        {
            final String str = mimeMessage.getHeader("Bcc", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setRecipients(RecipientType.BCC, addresses);
            }
        }
        {
            final String str = mimeMessage.getHeader("Reply-To", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                if ("true".equalsIgnoreCase(str)) {
                    Address[] fromAddresses = mimeMessage.getFrom();
                    if (fromAddresses.length > 0 && fromAddresses[0] != null) {
                        mimeMessage.setHeader("Disposition-Notification-To", fromAddresses[0].toString());
                    }
                } else {
                    final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                    checkRecipients(addresses);
                    mimeMessage.setHeader("Disposition-Notification-To", addresses[0].toString());
                }
            }
        }
        {
            final String str = mimeMessage.getHeader("Disposition-Notification-To", null);
            if (!com.openexchange.java.Strings.isEmpty(str)) {
                final InternetAddress[] addresses = QuotedInternetAddress.parse(str, false);
                checkRecipients(addresses);
                mimeMessage.setHeader("Disposition-Notification-To", addresses[0].toString());
            }
        }
    }

    private boolean checkRecipients(final Address[] recipients) throws OXException {
        if ((recipients == null) || (recipients.length == 0)) {
            throw GmailSendExceptionCode.MISSING_RECIPIENTS.create();
        }
        Boolean poisoned = null;
        final ConfigurationService service = Services.getService(ConfigurationService.class);
        if (null != service) {
            final Filter filter = service.getFilterFromProperty("com.openexchange.mail.transport.redirectWhitelist");
            if (null != filter) {
                for (final Address address : recipients) {
                    if (MimeMessageUtility.POISON_ADDRESS == address) {
                        poisoned = Boolean.TRUE;
                    } else {
                        final InternetAddress internetAddress = (InternetAddress) address;
                        if (!filter.accepts(internetAddress.getAddress())) {
                            throw GmailSendExceptionCode.RECIPIENT_NOT_ALLOWED.create(internetAddress.toUnicodeString());
                        }
                    }
                }
            }
        }
        if (MailProperties.getInstance().isSupportMsisdnAddresses()) {
            InternetAddress internetAddress;
            for (final Address address : recipients) {
                if (MimeMessageUtility.POISON_ADDRESS == address) {
                    poisoned = Boolean.TRUE;
                } else {
                    internetAddress = (InternetAddress) address;
                    final String sAddress = internetAddress.getAddress();
                    if (MsisdnCheck.checkMsisdn(sAddress)) {
                        if (sAddress.indexOf('/') < 0) {
                            // Detected a MSISDN address that misses "/TYPE=" appendix necessary for the MTA
                            internetAddress.setAddress(sAddress + "/TYPE=PLMN");
                        }
                        try {
                            internetAddress.setPersonal("", "US-ASCII");
                        } catch (final UnsupportedEncodingException e) {
                            LOG.trace("\"US-ASCII\" is not supported", e);
                            // Ignore as personal is cleared
                        }
                    }
                }
            }
        }
        return null == poisoned ? isPoisoned(recipients) : poisoned.booleanValue();
    }

    private static final boolean isPoisoned(final Address[] recipients) {
        if ((recipients == null) || (recipients.length == 0)) {
            return false;
        }
        for (final Address address : recipients) {
            if (MimeMessageUtility.POISON_ADDRESS == address) {
                return true;
            }
        }
        return false;
    }

    private javax.mail.Session getJavaMailSession() {
        javax.mail.Session javaMailSession = this.javaMailSession;
        if (null == javaMailSession) {
            synchronized (this) {
                javaMailSession = this.javaMailSession;
                if (null == javaMailSession) {
                    Properties javaMailProps = GmailSendSessionProperties.getDefaultSessionProperties();
                    javaMailProps.put("com.openexchange.mail.maxMailSize", Long.toString(MailProperties.getInstance().getMaxMailSize(session.getUserId(), session.getContextId())));
                    javaMailSession = javax.mail.Session.getInstance(javaMailProps, null);
                    this.javaMailSession = javaMailSession;
                }
            }
        }
        return javaMailSession;
    }

    private void setReplyHeaders(MimeMessage mimeMessage, MailPath msgref) throws OXException, MessagingException {
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

    private GmailSendMessageFiller createGmailSendMessageFiller(UserSettingMail optMailSettings) throws OXException {
        return new GmailSendMessageFiller(getTransportConfig().getGmailSendProperties(), session, ctx, null == optMailSettings ? usm : optMailSettings);
    }

    @Override
    public void close() {
        clearUp();
    }

    private void clearUp() {
        doInvocations();
    }

    /**
     * Executes all tasks queued for execution
     */
    private void doInvocations() {
        for (Runnable task = pendingInvocations.poll(); task != null; task = pendingInvocations.poll()) {
            task.run();
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

    // -----------------------------------------------------------------------------------------------------------------------------

    private static final class MimeMessageInputStreamContent extends AbstractInputStreamContent {

        private final MimeMessage mimeMessage;

        /**
         * Initializes a new {@link Rfc822InputStreamContent}.
         */
        public MimeMessageInputStreamContent(MimeMessage mimeMessage) {
            super("message/rfc822");
            this.mimeMessage = mimeMessage;
        }

        @Override
        public long getLength() throws IOException {
            return -1;
        }

        @Override
        public boolean retrySupported() {
            return true;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return MimeMessageUtility.getStreamFromPart(mimeMessage);
            /*
             * List<InputStream> streams = new ArrayList<InputStream>(3);
             * streams.add(Streams.newByteArrayInputStream("{\"raw\":\"".getBytes(StandardCharsets.UTF_8)));
             * streams.add(new UrlSafeBase64EncodingInputStream(MimeMessageUtility.getStreamFromPart(mimeMessage)));
             * streams.add(Streams.newByteArrayInputStream("\"}".getBytes(StandardCharsets.UTF_8)));
             * Enumeration<InputStream> streamsEnum = com.openexchange.tools.Collections.iter2enum(streams.iterator());
             * return new SequenceInputStream(streamsEnum);
             */
        }
    }

    private static final class MailCleanerTask implements Runnable {

        private final ComposedMailMessage composedMail;

        MailCleanerTask(ComposedMailMessage composedMail) {
            super();
            this.composedMail = composedMail;
        }

        @Override
        public void run() {
            composedMail.cleanUp();
        }

    } // End of class MailCleanerTask

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
