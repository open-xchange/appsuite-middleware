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

package com.openexchange.oauth.provider.impl.notification;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.notification.FullNameBuilder;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.oauth.provider.impl.tools.URLHelper;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link OAuthMailNotificationService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class OAuthMailNotificationService {

    private final TransportProvider transportProvider;

    public OAuthMailNotificationService() {
        super();
        transportProvider = TransportProviderRegistry.getTransportProvider("smtp");
    }

    public void sendNotification(ServerSession serverSession, Client client, HttpServletRequest request) throws OXException {
        try {
            ComposedMailMessage mail = buildNewExternalApplicationMail(serverSession, client, request);
            MailTransport transport = transportProvider.createNewNoReplyTransport(serverSession.getContextId());
            transport.sendMailMessage(mail, ComposeType.NEW, mail.getTo());
        } catch (UnsupportedEncodingException | MessagingException | URISyntaxException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
    }

    private ComposedMailMessage buildNewExternalApplicationMail(ServerSession session, Client client, HttpServletRequest request) throws OXException, UnsupportedEncodingException, MessagingException, URISyntaxException {
        User user = session.getUser();
        Translator translator = Services.requireService(TranslatorFactory.class).translatorFor(user.getLocale());
        ServerConfigService serverConfigService = Services.requireService(ServerConfigService.class);
        String hostname = URLHelper.getHostname(request);
        int contextId = session.getContextId();
        ServerConfig serverConfig = serverConfigService.getServerConfig(hostname, user.getId(), contextId);
        String subject = translator.translate(NotificationStrings.SUBJECT);
        subject = String.format(subject, client.getName());
        String salutation = translator.translate(NotificationStrings.SALUTATION);
        String userName = FullNameBuilder.buildFullName(user, translator);
        salutation = String.format(salutation, userName);
        String appConnected = translator.translate(NotificationStrings.APP_CONNECTED);
        appConnected = String.format(appConnected, getLogin(session, user), client.getName());

        // text substitutions
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("salutation", salutation);
        vars.put("appConnected", appConnected);
        vars.put("revokeAccess", translator.translate(NotificationStrings.REVOKE_ACCESS));
        vars.put("gotoSettings", translator.translate(NotificationStrings.GO_TO_SETTINGS));
        vars.put("settingsURL", getSettingsUrl(request));

        InternetAddress recipient = new InternetAddress(user.getMail(), userName);
        MailData mailData = MailData.newBuilder()
            .setRecipient(recipient)
            .setSubject(subject)
            .setHtmlTemplate("notify.oauthprovider.accessgranted.html.tmpl")
            .setTemplateVars(vars)
            .setMailConfig(serverConfig.getNotificationMailConfig())
            .setContext(session.getContext())
            .build();
        return Services.requireService(NotificationMailFactory.class).createMail(mailData);
    }

    private static String getLogin(Session session, User user) {
        String login = session.getLogin();
        if (login == null) {
            login = session.getLoginName();
        }

        if (login == null) {
            login = user.getLoginInfo();
        }

        if (login == null) {
            login = user.getMail();
        }

        if (login == null) {
            login = "";
        }

        return login;
    }

    private String getSettingsUrl(HttpServletRequest request) throws OXException, URISyntaxException {
        String uiWebPath = Services.requireService(ConfigurationService.class).getProperty("com.openexchange.UIWebPath", "/appsuite/");
        URI settingsURI = new URIBuilder()
            .setScheme("https")
            .setHost(URLHelper.getHostname(request))
            .setPath(uiWebPath)
            .setFragment("!&app=io.ox/settings&folder=virtual/settings/external/apps")
            .build();
        return settingsURI.toString();
    }

}
