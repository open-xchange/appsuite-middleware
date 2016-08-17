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
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.notification.FullNameBuilder;
import com.openexchange.notification.mail.MailData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.notification.NotificationStrings;
import com.openexchange.share.notification.impl.LinkCreatedNotification;
import com.openexchange.share.notification.impl.TextSnippets;
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

    public static LinkCreatedMail init(LinkCreatedNotification<InternetAddress> notification, TransportProvider transportProvider, ServiceLookup services) throws OXException {
        ContextService contextService = requireService(ContextService.class, services);
        UserService userService = requireService(UserService.class, services);
        ServerConfigService serverConfigService = requireService(ServerConfigService.class, services);
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        ModuleSupport moduleSupport = requireService(ModuleSupport.class, services);

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
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, targetUser.getLocale());
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
