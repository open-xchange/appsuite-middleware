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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.impl.notification.mail;

import static com.openexchange.osgi.Tools.requireService;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.config.NoReplyConfig;
import com.openexchange.mail.transport.config.NoReplyConfig.SecureMode;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.impl.notification.NotificationStrings;
import com.openexchange.share.notification.mail.MailNotification;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link MailSender}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailSender {

    /**
     *
     */
    private static final String FIELD_PASSWORD = "password";

    /**
     *
     */
    private static final String FIELD_USERNAME = "username";

    /**
     *
     */
    private static final String FIELD_PASSWORD_FIELD = "password_field";

    /**
     *
     */
    private static final String FIELD_USERNAME_FIELD = "username_field";

    /**
     *
     */
    private static final String FIELD_CREDENTIALS_INTRO = "credentials_intro";

    /**
     *
     */
    private static final String FIELD_LINK = "link";

    /**
     *
     */
    private static final String FIELD_LINK_INTRO = "link_intro";

    /**
     *
     */
    private static final String FIELD_MESSAGE = "message";

    /**
     *
     */
    private static final String FIELD_MESSAGE_INTRO = "message_intro";

    private static final Set<String> SHARE_CREATED_FIELDS = new HashSet<String>();
    static {
        SHARE_CREATED_FIELDS.add(FIELD_MESSAGE_INTRO);
        SHARE_CREATED_FIELDS.add(FIELD_MESSAGE);
        SHARE_CREATED_FIELDS.add(FIELD_LINK_INTRO);
        SHARE_CREATED_FIELDS.add(FIELD_LINK);
        SHARE_CREATED_FIELDS.add(FIELD_CREDENTIALS_INTRO);
        SHARE_CREATED_FIELDS.add(FIELD_USERNAME_FIELD);
        SHARE_CREATED_FIELDS.add(FIELD_USERNAME);
        SHARE_CREATED_FIELDS.add(FIELD_PASSWORD_FIELD);
        SHARE_CREATED_FIELDS.add(FIELD_PASSWORD);
    }

    private static final Set<String> PASSWORD_RESET_FIELDS = new HashSet<String>();
    {
        PASSWORD_RESET_FIELDS.add(FIELD_LINK_INTRO);
        PASSWORD_RESET_FIELDS.add(FIELD_LINK);
        PASSWORD_RESET_FIELDS.add(FIELD_CREDENTIALS_INTRO);
        PASSWORD_RESET_FIELDS.add(FIELD_USERNAME_FIELD);
        PASSWORD_RESET_FIELDS.add(FIELD_USERNAME);
        PASSWORD_RESET_FIELDS.add(FIELD_PASSWORD_FIELD);
        PASSWORD_RESET_FIELDS.add(FIELD_PASSWORD);
    }

    private final ServiceLookup services;

    private final MailNotification notification;

    private final ServerSession session;

    private final User user;

    private final Translator translator;

    public MailSender(ServiceLookup services, MailNotification notification, ServerSession session) throws OXException {
        super();
        this.services = services;
        this.notification = notification;
        this.session = session;
        user = session.getUser();
        translator = getTranslator();
    }

    public void send() throws OXException {
        MimeMessage mail = null;
        MailTransport transport = null;

        try {
            switch (notification.getType()) {
                case SHARE_CREATED:
                    mail = buildShareCreatedMail();
                    transport = MailTransport.getInstance(session);
                    break;
                case PASSWORD_RESET:
                    NoReplyConfig noReplyConfig = NoReplyConfig.getInstance(session.getUserId(), session.getContextId());
                    if (!noReplyConfig.isValid()) {
                        // TODO: exception
                    }

                    mail = buildPasswordResetMail(noReplyConfig);
                    TransportProvider transportProvider = com.openexchange.mail.transport.TransportProviderRegistry.getTransportProvider("smtp");
                    transport = transportProvider.createNewMailTransport(session);
                    TransportConfig transportConfig = transport.getTransportConfig();
                    transportConfig.setLogin(noReplyConfig.getLogin());
                    transportConfig.setPassword(noReplyConfig.getPassword());
                    transportConfig.setServer(noReplyConfig.getServer());
                    transportConfig.setPort(noReplyConfig.getPort());
                    SecureMode secureMode = noReplyConfig.getSecureMode();
                    transportConfig.setRequireTls(NoReplyConfig.SecureMode.TLS.equals(secureMode));
                    transportConfig.setSecure(NoReplyConfig.SecureMode.SSL.equals(secureMode));
                    break;
                default:
                    // TODO: exception
            }
        } catch (MessagingException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

        if (null != transport) {
            try {
                transport.sendMailMessage(new ContentAwareComposedMailMessage(mail, session, session.getContext()), ComposeType.NEW);
            } finally {
                try {
                    transport.close();
                } catch (OXException e) {
                    // ignore
                }
            }
        }
    }

    private MimeMessage buildPasswordResetMail(NoReplyConfig noReplyConfig) throws OXException, MessagingException, UnsupportedEncodingException {
        String title = translator.translate(NotificationStrings.TITLE_RESET_PASSWORD);
        MimeMessage mail = prepareEnvelope(title, noReplyConfig.getAddress());
        mail.setContent(prepareContent(
            "notify.share.pwreset.mail.txt.tmpl",
            prepareTemplateVars(null, PASSWORD_RESET_FIELDS, title),
            "notify.share.pwreset.mail.html.tmpl",
            prepareTemplateVars(getHtmlService(), PASSWORD_RESET_FIELDS, title)));
        mail.saveChanges();
        return mail;
    }

    private MimeMessage buildShareCreatedMail() throws OXException, UnsupportedEncodingException, MessagingException {
        List<ShareTarget> targets = notification.getShareTargets();
        String title;
        if (targets.size() == 1) {
            ShareTarget target = targets.get(0);
            TargetProxy proxy = getModuleSupport().load(target, session);
            title = proxy.getTitle();
        } else {
            title = translator.translate(String.format(NotificationStrings.GENERIC_TITLE, targets.size()));
        }

        String subject = String.format(translator.translate(NotificationStrings.SUBJECT), user.getDisplayName(), title);
        MimeMessage mail = prepareEnvelope(subject, getSenderAddress());
        mail.addHeader("X-Open-Xchange-Share", notification.getUrl());
        mail.setContent(prepareContent(
            "notify.share.create.mail.txt.tmpl",
            prepareTemplateVars(null, SHARE_CREATED_FIELDS, title),
            "notify.share.create.mail.html.tmpl",
            prepareTemplateVars(getHtmlService(), SHARE_CREATED_FIELDS, title)));
        mail.saveChanges();
        return mail;
    }

    private MimeMessage prepareEnvelope(String subject, Address senderAddress) throws MessagingException {
        MimeMessage mail = new MimeMessage(MimeDefaultSession.getDefaultSession());
        mail.addFrom(new Address[] { senderAddress });
        mail.addRecipient(RecipientType.TO, notification.getTransportInfo());
        mail.setSubject(subject, "UTF-8");
        return mail;
    }

    private MimeMultipart prepareContent(String txtTemplate, Map<String, Object> txtVars, String htmlTemplate, Map<String, Object> htmlVars) throws MessagingException, OXException, UnsupportedEncodingException {
        TemplateService templateService = getTemplateService();
        OXTemplate template = templateService.loadTemplate(txtTemplate);
        StringWriter writer = new StringWriter();
        template.process(txtVars, writer);
        BodyPart textPart = prepareTextPart(writer);

        template = templateService.loadTemplate(htmlTemplate);
        writer = new StringWriter();
        template.process(htmlVars, writer);
        BodyPart htmlPart = prepareHtmlPart(writer);

        MimeMultipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);
        return multipart;
    }

    private BodyPart prepareTextPart(Writer writer) throws OXException, MessagingException {
        MimeBodyPart textPart = new MimeBodyPart();
        MessageUtility.setText(writer.toString(), "UTF-8", textPart);
        textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        final ContentType ct = new ContentType();
        ct.setPrimaryType("text");
        ct.setSubType("plain");
        ct.setCharsetParameter("UTF-8");
        textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());

        return textPart;
    }

    private BodyPart prepareHtmlPart(Writer writer) throws OXException, UnsupportedEncodingException, MessagingException {
        MimeBodyPart htmlPart = new MimeBodyPart();
        ContentType ct = new ContentType();
        ct.setPrimaryType("text");
        ct.setSubType("html");
        ct.setCharsetParameter("UTF-8");
        String contentType = ct.toString();

        String conformContent = getHtmlService().getConformHTML(writer.toString(), "UTF-8");
        htmlPart.setDataHandler(new DataHandler(new MessageDataSource(conformContent, ct)));
        htmlPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);

        return htmlPart;
    }

    private Map<String, Object> prepareTemplateVars(HtmlService htmlService, Set<String> fields, String title) throws OXException {
//        List<Share> shares = notification.getShares();
        String displayName;
        String message = null;
        String username = null;
        String password = null;
        if (htmlService == null) {
            displayName = user.getDisplayName();
            message = notification.getMessage();
        } else {
            displayName = htmlService.htmlFormat(user.getDisplayName() == null ? "" : user.getDisplayName());
            title = htmlService.htmlFormat(title);
            String tmpMessage = notification.getMessage();
            if (tmpMessage != null) {
                message = htmlService.htmlFormat(tmpMessage);
            }
        }

//        if (share.getAuthentication() != AuthenticationMode.ANONYMOUS) {
//            UserService userService = getUserService();
//            ShareCryptoService shareCryptoService = getShareCryptoService();
//            User guest = userService.getUser(share.getGuest(), share.getContextID());
//            // FIXME!!!
//            String decryptedPassword = "mumpitz!";//shareCryptoService.decrypt(guest.getUserPassword());
//            if (htmlService == null) {
//                username = guest.getMail();
//                password = decryptedPassword;
//            } else {
//                username = htmlService.htmlFormat(guest.getMail());
//                password = htmlService.htmlFormat(decryptedPassword);
//            }
//        }

        Map<String, Object> vars = new HashMap<String, Object>();
        if (!Strings.isEmpty(message)) {
            checkAndPutField(fields, vars, FIELD_MESSAGE_INTRO, String.format(translator.translate(NotificationStrings.MESSAGE_INTRO), displayName));
            checkAndPutField(fields, vars, FIELD_MESSAGE, message);
        }
        checkAndPutField(fields, vars, FIELD_LINK_INTRO, String.format(translator.translate(NotificationStrings.LINK_INTRO), title));
        checkAndPutField(fields, vars, FIELD_LINK, notification.getUrl());
        checkAndPutField(fields, vars, FIELD_CREDENTIALS_INTRO, translator.translate(NotificationStrings.CREDENTIALS_INTRO));
        checkAndPutField(fields, vars, FIELD_USERNAME_FIELD, translator.translate(NotificationStrings.USERNAME_FIELD));
        checkAndPutField(fields, vars, FIELD_PASSWORD_FIELD, translator.translate(NotificationStrings.PASSWORD_FIELD));
        if (username != null && password != null) {
            checkAndPutField(fields, vars, FIELD_USERNAME, username);
            checkAndPutField(fields, vars, FIELD_PASSWORD, password);
        }

        return vars;
    }

    private static void checkAndPutField(Set<String> fields, Map<String, Object> vars, String name, String value) {
        if (fields.contains(name)) {
            vars.put(name, value);
        }
    }

    private Address getSenderAddress() throws OXException, UnsupportedEncodingException {
        ConfigurationService configService = getConfigService();
        String fromSource = configService.getProperty("com.openexchange.notification.fromSource", "primaryMail");
        String from = null;
        if ("defaultSenderAddress".equals(fromSource)) {
            UserSettingMail mailSettings = session.getUserSettingMail();
            if (mailSettings != null) {
                from = mailSettings.getSendAddr();
            }
        }

        User user = session.getUser();
        if (from == null) {
            from = user.getMail();
        }

        InternetAddress[] parsed = MimeMessageUtility.parseAddressList(from, true);
        if (parsed == null || parsed.length == 0) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("User " + user.getId() + " in context " + session.getContextId() + " seems to have no valid mail address.");
        }

        InternetAddress senderAddress = parsed[0];
        if (senderAddress.getPersonal() == null) {
            senderAddress.setPersonal(user.getDisplayName());
        }

        return senderAddress;
    }

    private UserService getUserService() throws OXException {
        return requireService(UserService.class, services);
    }

    private ShareCryptoService getShareCryptoService() throws OXException {
        return requireService(ShareCryptoService.class, services);
    }

    private TemplateService getTemplateService() throws OXException {
        return requireService(TemplateService.class, services);
    }

    private HtmlService getHtmlService() throws OXException {
        return requireService(HtmlService.class, services);
    }

    private ConfigurationService getConfigService() throws OXException {
        return requireService(ConfigurationService.class, services);
    }

    private ModuleSupport getModuleSupport() throws OXException {
        return requireService(ModuleSupport.class, services);
    }

    private Translator getTranslator() throws OXException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        Translator translator = translatorFactory.translatorFor(user.getLocale());
        return translator;
    }

}
