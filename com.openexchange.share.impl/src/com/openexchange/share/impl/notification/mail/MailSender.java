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

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
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
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.Share;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.impl.ShareServiceLookup;
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

    private final MailNotification notification;

    private final ServerSession session;

    private final User user;

    private final StringHelper stringHelper;


    public MailSender(MailNotification notification, ServerSession session) {
        super();
        this.notification = notification;
        this.session = session;
        user = session.getUser();
        stringHelper = StringHelper.valueOf(user.getLocale());
    }

    public void send() throws OXException {
        MimeMessage mail = new MimeMessage(MimeDefaultSession.getDefaultSession());
        try {
            mail.addFrom(new Address[] { getSenderAddress() });
            mail.addRecipient(RecipientType.TO, notification.getTransportInfo());
            mail.addHeader("X-Open-Xchange-Share", notification.getUrl());
            String subject = String.format(stringHelper.getString(MailStrings.SUBJECT), user.getDisplayName(), notification.getTitle());
            mail.setSubject(subject, "UTF-8");
            mail.setContent(prepareContent());
        } catch (MessagingException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

        MailTransport transport = MailTransport.getInstance(session);
        try {
            transport.sendMailMessage(new ContentAwareComposedMailMessage(mail, session, session.getContext()), ComposeType.NEW);
        } finally {
            transport.close();
        }
    }

    private MimeMultipart prepareContent() throws MessagingException, OXException, UnsupportedEncodingException {
        TemplateService templateService = getTemplateService();
        BodyPart textPart = prepareTextPart(templateService);
        BodyPart htmlPart = prepareHtmlPart(templateService);
        MimeMultipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);
        return multipart;
    }

    private BodyPart prepareTextPart(TemplateService templateService) throws OXException, MessagingException {
        OXTemplate template = templateService.loadTemplate("notify.share.create.mail.txt.tmpl");
        StringWriter writer = new StringWriter();
        template.process(prepareTemplateVars(null), writer);

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

    private BodyPart prepareHtmlPart(TemplateService templateService) throws OXException, UnsupportedEncodingException, MessagingException {
        HtmlService htmlService = getHtmlService();
        OXTemplate template = templateService.loadTemplate("notify.share.create.mail.html.tmpl");
        StringWriter writer = new StringWriter();
        template.process(prepareTemplateVars(htmlService), writer);

        MimeBodyPart htmlPart = new MimeBodyPart();
        ContentType ct = new ContentType();
        ct.setPrimaryType("text");
        ct.setSubType("html");
        ct.setCharsetParameter("UTF-8");
        String contentType = ct.toString();

        String conformContent = htmlService.getConformHTML(writer.toString(), "UTF-8");
        htmlPart.setDataHandler(new DataHandler(new MessageDataSource(conformContent, ct)));
        htmlPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);

        return htmlPart;
    }

    private Map<String, Object> prepareTemplateVars(HtmlService htmlService) throws OXException {
        Share share = notification.getShare();
        String displayName;
        String title;
        String message = null;
        String username = null;
        String password = null;
        if (htmlService == null) {
            displayName = user.getDisplayName();
            title = notification.getTitle();
            message = notification.getMessage();
        } else {
            displayName = htmlService.htmlFormat(user.getDisplayName());
            title = htmlService.htmlFormat(notification.getTitle());
            String tmpMessage = notification.getMessage();
            if (tmpMessage != null) {
                message = htmlService.htmlFormat(tmpMessage);
            }
        }

        if (share.getAuthentication() != AuthenticationMode.ANONYMOUS) {
            UserService userService = getUserService();
            ShareCryptoService shareCryptoService = getShareCryptoService();
            User guest = userService.getUser(share.getGuest(), share.getContextID());
            String decryptedPassword = shareCryptoService.decrypt(guest.getUserPassword());
            if (htmlService == null) {
                username = guest.getMail();
                password = decryptedPassword;
            } else {
                username = htmlService.htmlFormat(guest.getMail());
                password = htmlService.htmlFormat(decryptedPassword);
            }
        }

        Map<String, Object> vars = new HashMap<String, Object>();
        if (!Strings.isEmpty(message)) {
            vars.put("message_intro", String.format(stringHelper.getString(MailStrings.MESSAGE_INTRO), displayName));
            vars.put("message", message);
        }
        vars.put("link_intro", String.format(stringHelper.getString(MailStrings.LINK_INTRO), title));
        vars.put("link", notification.getUrl());
        vars.put("credentials_intro", stringHelper.getString(MailStrings.CREDENTIALS_INTRO));
        vars.put("username_field", stringHelper.getString(MailStrings.USERNAME_FIELD));
        vars.put("password_field", stringHelper.getString(MailStrings.PASSWORD_FIELD));
        if (username != null && password != null) {
            vars.put("username", username);
            vars.put("password", password);
        }

        return vars;
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
        return ShareServiceLookup.getService(UserService.class, true);
    }

    private ShareCryptoService getShareCryptoService() throws OXException {
        return ShareServiceLookup.getService(ShareCryptoService.class, true);
    }

    private TemplateService getTemplateService() throws OXException {
        return ShareServiceLookup.getService(TemplateService.class, true);
    }

    private HtmlService getHtmlService() throws OXException {
        return ShareServiceLookup.getService(HtmlService.class, true);
    }

    private ConfigurationService getConfigService() throws OXException {
        return ShareServiceLookup.getService(ConfigurationService.class, true);
    }

}
