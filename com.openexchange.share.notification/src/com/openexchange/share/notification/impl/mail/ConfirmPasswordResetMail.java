/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.notification.mail.MailData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.share.notification.NotificationStrings;
import com.openexchange.share.notification.impl.PasswordResetConfirmNotification;
import com.openexchange.user.User;
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

    public static ConfirmPasswordResetMail init(PasswordResetConfirmNotification<InternetAddress> notification, ServiceLookup services) throws OXException {
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
        Map<String, Object> vars = preparePasswordResetConfirmVars(notification, translator);
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

    private static Map<String, Object> preparePasswordResetConfirmVars(PasswordResetConfirmNotification<InternetAddress> notification, Translator translator) {
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
