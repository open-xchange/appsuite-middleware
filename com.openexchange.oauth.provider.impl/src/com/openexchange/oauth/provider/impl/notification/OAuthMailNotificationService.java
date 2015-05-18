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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.impl.notification;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientManagementException;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

/**
 * {@link OAuthMailNotificationService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class OAuthMailNotificationService {

    private final TransportProvider transportProvider;
    private final OAuthProviderService oAuthProviderService;

    private static final String INTRO_FIELD = "intro";
    private static final String MESSAGE_FIELD = "message";

    private static final Set<String> NEW_EXTERNAL_APPLICATION_FIELDS = new HashSet<String>();
    static {
        NEW_EXTERNAL_APPLICATION_FIELDS.add(INTRO_FIELD);
        NEW_EXTERNAL_APPLICATION_FIELDS.add(MESSAGE_FIELD);
    }

    public OAuthMailNotificationService(OAuthProviderService oAuthProviderService) {
        super();
        this.oAuthProviderService = oAuthProviderService;
        transportProvider = TransportProviderRegistry.getTransportProvider("smtp");
    }

    public void sendNotification(int userId, int contextId, String clientId, HttpServletRequest request) throws OXException {
        try {
            UserService userService = Services.requireService(UserService.class);
            User user = userService.getUser(userId, contextId);
            InternetAddress address = new InternetAddress(user.getMail());
            ComposedMailMessage mail = buildNewExternalApplicationMail(user, contextId, clientId, address, request);
            MailTransport transport = transportProvider.createNewNoReplyTransport(contextId);
            transport.sendMailMessage(mail, ComposeType.NEW, new Address[] { address });
        } catch (AddressException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e);
        } catch (UnsupportedEncodingException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e);
        } catch (MessagingException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e);
        } catch (JSONException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
    }

    private ComposedMailMessage buildNewExternalApplicationMail(User user, int contextId, String clientId, InternetAddress address, HttpServletRequest request) throws OXException, UnsupportedEncodingException, MessagingException, JSONException {
        Translator translator = Services.requireService(TranslatorFactory.class).translatorFor(user.getLocale());
        ServerConfigService serverConfigService = Services.requireService(ServerConfigService.class);
        String hostname;
        HostnameService hostnameService = Services.optService(HostnameService.class);
        if(hostnameService != null) {
            hostname = hostnameService.getHostname(user.getId(), contextId);
        } else {
            hostname = request.getServerName();
        }
        ServerConfig serverConfig = serverConfigService.getServerConfig(hostname, user.getId(), contextId);
        Client client = getClient(clientId);
        String title = translator.translate(NotificationStrings.NEW_EXTERNAL_APPLICATION_TITLE);
        title = String.format(title, serverConfig.getProductName());
        String intro = translator.translate(NotificationStrings.NEW_EXTERNAL_APPLICATION_INTRO);
        intro = String.format(intro, user.getDisplayName());
        String message = translator.translate(NotificationStrings.NEW_EXTERNAL_APPLICATION_MESSAGE);
        String settingsUrl = getSettingsUrl(request);
        message = String.format(message, client.getName(), serverConfig.getProductName(), settingsUrl);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put(INTRO_FIELD, intro);
        vars.put(MESSAGE_FIELD, message);
        MimeMessage mail = prepareEnvelope(title, address);
        mail.setHeader("Auto-Submitted", "auto-generated");
        mail.setContent(prepareContent("oauth-new-external-application-mail.txt.tmpl", vars, "oauth-new-external-application-mail.html.tmpl", vars));
        mail.saveChanges();
        return new ContentAwareComposedMailMessage(mail, contextId);
    }

    private Client getClient(String clientId) throws OXException {
        try {
            Client client = oAuthProviderService.getClientManagement().getClientById(clientId);
            if (client == null) {
                throw OAuthProviderExceptionCodes.CLIENT_NOT_FOUND.create(clientId);
            }

            return client;
        } catch (ClientManagementException e) {
            if (e.getReason() == com.openexchange.oauth.provider.client.ClientManagementException.Reason.INVALID_CLIENT_ID) {
                throw OAuthProviderExceptionCodes.CLIENT_NOT_FOUND.create(clientId);
            }

            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private MimeMessage prepareEnvelope(String subject, InternetAddress recipient) throws MessagingException {
        MimeMessage mail = new MimeMessage(MimeDefaultSession.getDefaultSession());
        mail.addRecipient(RecipientType.TO, recipient);
        mail.setSubject(subject, "UTF-8");
        return mail;
    }

    private BodyPart prepareTextPart(Writer writer) throws MessagingException {
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
        HtmlService htmlService = Services.requireService(HtmlService.class);
        String conformContent = htmlService.getConformHTML(writer.toString(), "UTF-8");
        htmlPart.setDataHandler(new DataHandler(new MessageDataSource(conformContent, ct)));
        htmlPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);

        return htmlPart;
    }

    private MimeMultipart prepareContent(String txtTemplate, Map<String, Object> txtVars, String htmlTemplate, Map<String, Object> htmlVars) throws MessagingException, OXException, UnsupportedEncodingException {
        TemplateService templateService = Services.requireService(TemplateService.class);
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

    private final String url = "[[protocol]]://[[host]][[uiWebPath]]/#&[[app]]";

    private String getSettingsUrl(HttpServletRequest request) throws OXException {
        String protocol = request.isSecure() ? "https" : "http";
        String host = request.getLocalName();
        String uiWebPath = Services.requireService(ConfigurationService.class).getProperty("com.openexchange.UIWebPath", "/appsuite");
        String settingsUrl = url.replace("[[protocol]]", protocol)
            .replace("[[host]]", host)
            .replace("[[uiWebPath]]", uiWebPath)
            .replace("[[app]]", "app=io.ox/settings");
        return settingsUrl;
    }

}
