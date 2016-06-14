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

package com.openexchange.share.notification.impl.mail;

import static com.openexchange.osgi.Tools.requireService;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.notification.mail.MailData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.share.notification.NotificationStrings;
import com.openexchange.share.notification.impl.PasswordResetConfirmNotification;
import com.openexchange.user.UserService;


/**
 * {@link ConfirmPasswordResetMail}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0.
 */
public class ConfirmPasswordResetMail extends ShareNotificationMail {

    private static final String PWRC_GREETING = "pwrc_greeting";
    private static final String PWRC_REQUESTRECEIVED = "pwrc_requestreceived";
    private static final String PWRC_LINK = "pw_reset_confirm_link";
    private static final String PWRC_LINK_LABEL = "pw_reset_confirm_link_label";
    private static final String PWRC_IGNORE = "pwrc_ignore";
    private static final String PWRC_AUTOMATED_MAIL = "pwrc_automated_mail";

    private ConfirmPasswordResetMail(MailData mailData, ServiceLookup services) {
        super(services, mailData);
    }

    public static ConfirmPasswordResetMail init(PasswordResetConfirmNotification<InternetAddress> notification, TransportProvider transportProvider, ServiceLookup services) throws OXException {
        ContextService contextService = requireService(ContextService.class, services);
        UserService userService = requireService(UserService.class, services);
        ServerConfigService serverConfigService = requireService(ServerConfigService.class, services);
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);

        Context context = contextService.getContext(notification.getContextID());
        int guestId = notification.getGuestID();
        User guestUser = userService.getUser(guestId, context);
        Translator translator = translatorFactory.translatorFor(guestUser.getLocale());
        ServerConfig serverConfig = serverConfigService.getServerConfig(
            notification.getHostData().getHost(),
            guestId,
            context.getContextId());

        // Set variables
        Map<String, Object> vars = preparePasswordResetConfirmVars(notification, translator, serverConfig);
        MailData mailData = MailData.newBuilder()
            .setRecipient(notification.getTransportInfo())
            .setSubject(String.format(translator.translate(NotificationStrings.PWRC_SUBJECT), serverConfig.getProductName()))
            .setHtmlTemplate("notify.share.pwreset.confirm.mail.html.tmpl")
            .setTemplateVars(vars)
            .setMailConfig(serverConfig.getNotificationMailConfig())
            .setContext(context)
            .addMailHeader("X-Open-Xchange-Share-Type", notification.getType().getId())
            .addMailHeader("X-Open-Xchange-Share-URL", notification.getShareUrl())
            .addMailHeader("X-Open-Xchange-Share-Reset-PW-URL", notification.getConfirmPasswordResetUrl())
            .build();

        return new ConfirmPasswordResetMail(mailData, services);
    }

    private static Map<String, Object> preparePasswordResetConfirmVars(PasswordResetConfirmNotification<InternetAddress> notification, Translator translator, ServerConfig serverConfig) throws OXException {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put(PWRC_GREETING, translator.translate(NotificationStrings.PWRC_GREETING));
        vars.put(PWRC_REQUESTRECEIVED, translator.translate(NotificationStrings.PWRC_REQUESTRECEIVED));
        vars.put(PWRC_LINK, notification.getConfirmPasswordResetUrl());
        vars.put(PWRC_LINK_LABEL, translator.translate(NotificationStrings.PWRC_LINK_LABEL));
        vars.put(PWRC_IGNORE, translator.translate(NotificationStrings.PWRC_IGNORE));
        vars.put(PWRC_AUTOMATED_MAIL, translator.translate(NotificationStrings.PWRC_AUTOMATED_MAIL));
        return vars;
    }

}
