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
import java.util.Locale;
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
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.impl.notification.NotificationStrings;
import com.openexchange.share.notification.LinkProvider;
import com.openexchange.share.notification.PasswordResetNotification;
import com.openexchange.share.notification.ShareCreatedNotification;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;


/**
 * {@link MailComposer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailComposer {

    private static final String FIELD_PW_RESET_INTRO = "pw_reset_intro";

    private static final String FIELD_RESET_PW_LINK = "reset_pw_link";

    private static final String FIELD_RESET_PW_LINK_INTRO = "reset_pw_link_intro";

    private static final String FIELD_RECIPIENT_TYPE = "recipient_type";

    private static final String FIELD_PASSWORD = "password";

    private static final String FIELD_USERNAME = "username";

    private static final String FIELD_PASSWORD_FIELD = "password_field";

    private static final String FIELD_USERNAME_FIELD = "username_field";

    private static final String FIELD_CREDENTIALS_INTRO = "credentials_intro";

    private static final String FIELD_LINK = "link";

    private static final String FIELD_LINK_INTRO = "link_intro";

    private static final String FIELD_MESSAGE = "message";

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
        SHARE_CREATED_FIELDS.add(FIELD_RECIPIENT_TYPE);
        SHARE_CREATED_FIELDS.add(FIELD_RESET_PW_LINK);
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

    public MailComposer(ServiceLookup services) throws OXException {
        super();
        this.services = services;
    }

    public ComposedMailMessage buildPasswordResetMail(PasswordResetNotification<InternetAddress> notification) throws OXException, MessagingException, UnsupportedEncodingException {
        Translator translator = getTranslator(notification.getLocale());
        String subject = translator.translate(NotificationStrings.SUBJECT_RESET_PASSWORD);
        Map<String, Object> vars = preparePasswordResetVars(notification, translator);
        MimeMessage mail = prepareEnvelope(subject, null, notification.getTransportInfo());
        mail.addHeader("X-Open-Xchange-Share-Type", "password-reset");
        mail.addHeader("X-Open-Xchange-Share-URL", notification.getLinkProvider().getShareUrl());
        mail.addHeader("X-Open-Xchange-Share-Access", buildAccessHeader(AuthenticationMode.GUEST_PASSWORD, notification.getUsername(), notification.getPassword()));
        mail.setContent(prepareContent(
            "notify.share.pwreset.mail.txt.tmpl",
            vars,
            "notify.share.pwreset.mail.html.tmpl",
            vars));
        mail.saveChanges();
        return new ContentAwareComposedMailMessage(mail, notification.getContextID());
    }

    private Map<String, Object> preparePasswordResetVars(PasswordResetNotification<InternetAddress> notification, Translator translator) throws OXException {
        Map<String, Object> vars = new HashMap<String, Object>();
        LinkProvider linkProvider = notification.getLinkProvider();
        final String shareUrl = linkProvider.getShareUrl();
        final String pwResetIntro = String.format(translator.translate(NotificationStrings.RESET_PASSWORD_INTRO), shareUrl);
        vars.put(FIELD_PW_RESET_INTRO, new PWResetIntro(pwResetIntro, shareUrl));
        vars.put(FIELD_CREDENTIALS_INTRO, translator.translate(NotificationStrings.RESET_CREDENTIALS_INTRO));
        vars.put(FIELD_USERNAME_FIELD, translator.translate(NotificationStrings.USERNAME_FIELD));
        vars.put(FIELD_USERNAME, notification.getUsername());
        vars.put(FIELD_PASSWORD_FIELD, translator.translate(NotificationStrings.PASSWORD_FIELD));
        vars.put(FIELD_PASSWORD, notification.getPassword());

        return vars;
    }

    public static final class PWResetIntro {

        private final String shareUrl;
        private final String pwResetIntro;
        private final String[] pwResetIntroSplit;

        public PWResetIntro(String pwResetIntro, String shareUrl) {
            super();
            this.shareUrl = shareUrl;
            this.pwResetIntro = pwResetIntro;
            this.pwResetIntroSplit = pwResetIntro.split(shareUrl);
        }

        public String pre() {
            return pwResetIntroSplit[0];
        }

        public String in() {
            return shareUrl;
        }

        public String post() {
            if (pwResetIntroSplit.length > 1) {
                return pwResetIntroSplit[1];
            }

            return "";
        }

        @Override
        public String toString() {
            return pwResetIntro;
        }
    }

    public ComposedMailMessage buildShareCreatedMail(ShareCreatedNotification<InternetAddress> notification) throws OXException, UnsupportedEncodingException, MessagingException {
        Translator translator = getTranslator(notification.getLocale());
        List<ShareTarget> targets = notification.getShareTargets();
        String title;
        if (targets.size() == 1) {
            ShareTarget target = targets.get(0);
            TargetProxy proxy = getModuleSupport().load(target, notification.getSession());
            title = proxy.getTitle();
        } else {
            title = translator.translate(String.format(NotificationStrings.GENERIC_TITLE, targets.size()));
        }

        User user = getUserService().getUser(notification.getSession().getUserId(), notification.getSession().getContextId());
        Map<String, Object> vars = prepareShareCreatedVars(notification, user, title, translator);
        String subject = String.format(translator.translate(NotificationStrings.SUBJECT), user.getDisplayName(), title);
        MimeMessage mail = prepareEnvelope(subject, new Address[] { getSenderAddress(notification.getSession(), user) }, notification.getTransportInfo());
        mail.addHeader("X-Open-Xchange-Share-Type", "share-created");
        mail.addHeader("X-Open-Xchange-Share-URL", notification.getLinkProvider().getShareUrl());
        mail.addHeader("X-Open-Xchange-Share-Access", buildAccessHeader(notification.getAuthMode(), notification.getUsername(), notification.getPassword()));
        mail.setContent(prepareContent(
            "notify.share.create.mail.txt.tmpl",
            vars,
            "notify.share.create.mail.html.tmpl",
            vars));
        mail.saveChanges();
        return new ContentAwareComposedMailMessage(mail, notification.getSession(), notification.getSession().getContextId());
    }

    private static String buildAccessHeader(AuthenticationMode authMode, String username, String password) throws OXException {
        String accessHeader = null;
        if (authMode == AuthenticationMode.GUEST_PASSWORD && !Strings.isEmpty(username) && !Strings.isEmpty(password)) {
            accessHeader = com.openexchange.tools.encoding.Base64.encode(username + ':' + password);
        } else if (authMode == AuthenticationMode.ANONYMOUS_PASSWORD && !Strings.isEmpty(password)) {
            accessHeader = com.openexchange.tools.encoding.Base64.encode(password);
        }

        return accessHeader;
    }

    private static Map<String, Object> prepareShareCreatedVars(ShareCreatedNotification<InternetAddress> notification, User user, String title, Translator translator) throws OXException {
        Map<String, Object> vars = new HashMap<String, Object>();
        String message = notification.getMessage();
        if (!Strings.isEmpty(message)) {
            vars.put(FIELD_MESSAGE_INTRO, String.format(translator.translate(NotificationStrings.MESSAGE_INTRO), user.getDisplayName()));
            vars.put(FIELD_MESSAGE, message);
        }

        LinkProvider linkProvider = notification.getLinkProvider();
        vars.put(FIELD_LINK_INTRO, String.format(translator.translate(NotificationStrings.LINK_INTRO), title));
        vars.put(FIELD_LINK, linkProvider.getShareUrl());

        switch (notification.getAuthMode()) {
            case ANONYMOUS:
            {
                vars.put(FIELD_RECIPIENT_TYPE, "anonymous");
                break;
            }

            case ANONYMOUS_PASSWORD:
            {
                vars.put(FIELD_RECIPIENT_TYPE, "anonymous");
                vars.put(FIELD_CREDENTIALS_INTRO, translator.translate(NotificationStrings.ANONYMOUS_PASSWORD_INTRO));
                vars.put(FIELD_PASSWORD_FIELD, translator.translate(NotificationStrings.PASSWORD_FIELD));
                vars.put(FIELD_PASSWORD, notification.getPassword());
                break;
            }

            case GUEST_PASSWORD:
            {
                vars.put(FIELD_RECIPIENT_TYPE, "guest");
                String password = notification.getPassword();
                if (password == null) {
                    vars.put(FIELD_CREDENTIALS_INTRO, translator.translate(NotificationStrings.GUEST_EXISTING_CREDENTIALS_INTRO));
                    vars.put(FIELD_RESET_PW_LINK_INTRO, translator.translate(NotificationStrings.RESET_PW_LINK_INTRO));
                    vars.put(FIELD_RESET_PW_LINK, linkProvider.getPasswordResetUrl());
                } else {
                    vars.put(FIELD_CREDENTIALS_INTRO, translator.translate(NotificationStrings.GUEST_CREDENTIALS_INTRO));
                    vars.put(FIELD_USERNAME_FIELD, translator.translate(NotificationStrings.USERNAME_FIELD));
                    vars.put(FIELD_USERNAME, notification.getUsername());
                    vars.put(FIELD_PASSWORD_FIELD, translator.translate(NotificationStrings.PASSWORD_FIELD));
                    vars.put(FIELD_PASSWORD, password);
                }

                break;
            }

            default:
                break;
        }

        return vars;
    }

    private MimeMessage prepareEnvelope(String subject, Address[] senderAddresses, InternetAddress recipient) throws MessagingException {
        MimeMessage mail = new MimeMessage(MimeDefaultSession.getDefaultSession());
        mail.addFrom(senderAddresses);
        mail.addRecipient(RecipientType.TO, recipient);
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

    private Address getSenderAddress(Session session, User user) throws OXException, UnsupportedEncodingException {
        ConfigurationService configService = getConfigService();
        String fromSource = configService.getProperty("com.openexchange.notification.fromSource", "primaryMail");
        String from = null;
        if ("defaultSenderAddress".equals(fromSource)) {
            UserSettingMail mailSettings = UserSettingMailStorage.getInstance().getUserSettingMail(session);
            if (mailSettings != null) {
                from = mailSettings.getSendAddr();
            }
        }

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

    private Translator getTranslator(Locale locale) throws OXException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        Translator translator = translatorFactory.translatorFor(locale);
        return translator;
    }

}
