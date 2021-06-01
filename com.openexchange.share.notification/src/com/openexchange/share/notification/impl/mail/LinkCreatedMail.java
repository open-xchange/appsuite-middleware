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
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.mail.internet.InternetAddress;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.notification.FullNameBuilder;
import com.openexchange.notification.mail.MailData;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.notification.NotificationStrings;
import com.openexchange.share.notification.impl.LinkCreatedNotification;
import com.openexchange.share.notification.impl.TextSnippets;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link LinkCreatedMail}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class LinkCreatedMail extends ShareNotificationMail {

    private static final String WILL_EXPIRE = "will_expire";

    private static final String USE_PASSWORD = "use_password";

    private static final String PASSWORD = "password";

    /**
     * Initializes a new {@link LinkCreatedMail}.
     * @param data
     */
    public LinkCreatedMail(MailData data, ServiceLookup services) {
        super(services, data);
    }

    public static LinkCreatedMail init(LinkCreatedNotification<InternetAddress> notification, ServiceLookup services) throws OXException {
        ContextService contextService = requireService(ContextService.class, services);
        UserService userService = requireService(UserService.class, services);
        ServerConfigService serverConfigService = requireService(ServerConfigService.class, services);
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        ModuleSupport moduleSupport = requireService(ModuleSupport.class, services);
        RegionalSettingsService regionalSettingsService = requireService(RegionalSettingsService.class, services);

        Context context = contextService.getContext(notification.getContextID());
        User sharingUser = userService.getUser(notification.getSession().getUserId(), context);
        User targetUser = userService.getUser(notification.getTargetUserID(), context);
        Translator translator = translatorFactory.translatorFor(targetUser.getLocale());
        TextSnippets textSnippets = new TextSnippets(translator);

        ShareTarget target = notification.getShareTarget();
        TargetProxy targetProxy = moduleSupport.load(target, notification.getSession());
        List<TargetProxy> targetProxies = Collections.singletonList(targetProxy);

        String shareOwnerName = FullNameBuilder.buildFullName(sharingUser, translator);

        ServerConfig serverConfig = serverConfigService.getServerConfig(
            notification.getHostData().getHost(),
            targetUser.getId(),
            context.getContextId());

        Map<String, Object> vars = new HashMap<String, Object>();
        boolean hasMessage = Strings.isNotEmpty(notification.getMessage());
        String shareUrl = notification.getShareUrl();
        String email = sharingUser.getMail();

        vars.put(ShareCreatedMail.HAS_SHARED_ITEMS, textSnippets.shareStatementLong(shareOwnerName, email, targetProxies, hasMessage));
        if (hasMessage) {
            vars.put(ShareCreatedMail.USER_MESSAGE, notification.getMessage());
        }

        vars.put(ShareCreatedMail.VIEW_ITEMS_LINK, shareUrl);
        vars.put(ShareCreatedMail.VIEW_ITEMS_LABEL, textSnippets.linkLabel(targetProxies));

        String password = notification.getPassword();
        if (Strings.isNotEmpty(password)) {
            vars.put(USE_PASSWORD, translator.translate(NotificationStrings.USE_PASSWORD));
            vars.put(PASSWORD, password);
        }

        Date expiryDate = notification.getExpiryDate();
        if (expiryDate != null) {
            DateFormat dateFormat;
            if (null != regionalSettingsService) {
                dateFormat = regionalSettingsService.getDateFormat(notification.getContextID(), notification.getTargetUserID(), targetUser.getLocale(), DateFormat.MEDIUM);
            } else {
                dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, targetUser.getLocale());
            }
            Date localExpiry = new Date(expiryDate.getTime() + TimeZone.getTimeZone(targetUser.getTimeZone()).getOffset(expiryDate.getTime()));
            vars.put(WILL_EXPIRE, String.format(translator.translate(NotificationStrings.LINK_EXPIRE), dateFormat.format(localExpiry)));
        }

        MailData mailData = MailData.newBuilder()
            .setSendingUser(sharingUser)
            .setRecipient(notification.getTransportInfo())
            .setSubject(textSnippets.shareStatementShort(shareOwnerName, targetProxies))
            .setHtmlTemplate("notify.share.create.mail.html.tmpl")
            .setTemplateVars(vars)
            .setMailConfig(serverConfig.getNotificationMailConfig())
            .setContext(context)
            .addMailHeader("X-Open-Xchange-Share-Type", notification.getType().getId())
            .addMailHeader("X-Open-Xchange-Share-URL", notification.getShareUrl())
            .build();
        return new LinkCreatedMail(mailData, services);
    }

}
