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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.notification.FullNameBuilder;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.MailData.Builder;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetProxyType;
import com.openexchange.share.notification.impl.ShareCreatedNotification;
import com.openexchange.share.notification.impl.TextSnippets;
import com.openexchange.user.UserService;

/**
 * {@link ShareCreatedMail}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareCreatedMail extends ShareNotificationMail {

    static final String HAS_SHARED_ITEMS = "has_shared_items";
    static final String USER_MESSAGE = "user_message";
    static final String VIEW_ITEMS_LINK = "view_items_link";
    static final String VIEW_ITEMS_LABEL = "view_items_label";

    protected ShareCreatedMail(MailData data, ServiceLookup services) {
        super(services, data);
    }

    private static class CollectVarsData {
        ShareCreatedNotification<InternetAddress> notification;
        User sharingUser;

        TextSnippets textSnippets;
        String shareOwnerName;
        HashMap<ShareTarget, TargetProxy> targetProxies;
    }

    public static ShareCreatedMail init(ShareCreatedNotification<InternetAddress> notification, TransportProvider transportProvider, ServiceLookup services) throws OXException {
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

        List<ShareTarget> shareTargets = notification.getShareTargets();
        HashMap<ShareTarget, TargetProxy> targetProxies = new HashMap<>(shareTargets.size());
        HashSet<TargetProxyType> targetProxyTypes = new HashSet<>(shareTargets.size());
        for (ShareTarget target : shareTargets) {
            TargetProxy targetProxy = moduleSupport.load(target, notification.getSession());
            TargetProxyType proxyType = targetProxy.getProxyType();
            targetProxies.put(target, targetProxy);
            targetProxyTypes.add(proxyType);
        }

        CollectVarsData data = new CollectVarsData();
        data.notification = notification;
        data.sharingUser = sharingUser;
        data.shareOwnerName = FullNameBuilder.buildFullName(sharingUser, translator);
        data.targetProxies = targetProxies;
        data.textSnippets = textSnippets;

        ServerConfig serverConfig = serverConfigService.getServerConfig(
            notification.getHostData().getHost(),
            targetUser.getId(),
            context.getContextId());

        Map<String, Object> vars = prepareShareCreatedVars(data);
        String date;
        final MailDateFormat mdf = MimeMessageUtility.getMailDateFormat(notification.getSession());
        synchronized (mdf) {
            date = mdf.format(new Date());
        }
        Builder mailData = MailData.newBuilder()
            .setSendingUser(sharingUser)
            .setRecipient(notification.getTransportInfo())
            .setHtmlTemplate("notify.share.create.mail.html.tmpl")
            .setTemplateVars(vars)
            .setMailConfig(serverConfig.getNotificationMailConfig())
            .setContext(context)
            .addMailHeader("Date", date)
            .addMailHeader("X-Open-Xchange-Share-Type", notification.getType().getId())
            .addMailHeader("X-Open-Xchange-Share-URL", notification.getShareUrl());
        if (data.notification.getTargetGroup() == null) {
            mailData.setSubject(textSnippets.shareStatementShort(data.shareOwnerName, data.targetProxies.values()));
        } else {
            mailData.setSubject(textSnippets.shareStatementGroupShort(data.shareOwnerName, data.notification.getTargetGroup().getDisplayName(), data.targetProxies.values()));
        }

        return new ShareCreatedMail(mailData.build(), services);
    }

    /**
     * Prepares a mapping from template keywords to actual textual values that will be used during template rendering.
     *
     * @param data
     *
     * @param data.notification The {@link ShareCreatedNotification} containing infos about the created share
     * @param user The {@link User} that created a new share
     * @param data.translator The {@link Translator} used for adapting the textual template values to the recipients locale
     * @return A mapping from template keywords to actual textual values
     * @throws OXException
     */
    private static Map<String, Object> prepareShareCreatedVars(CollectVarsData data) throws OXException {
        Map<String, Object> vars = new HashMap<String, Object>();
        boolean hasMessage = Strings.isNotEmpty(data.notification.getMessage());
        String shareUrl = data.notification.getShareUrl();
        String email = data.sharingUser.getMail();
        String fullName = data.shareOwnerName;

        String shareStatementLong;
        if (data.notification.getTargetGroup() == null) {
            shareStatementLong = data.textSnippets.shareStatementLong(fullName, email, data.targetProxies.values(), hasMessage);
        } else {
            shareStatementLong = data.textSnippets.shareStatementGroupLong(fullName, email, data.notification.getTargetGroup().getDisplayName(), data.targetProxies.values(), hasMessage);
        }
        vars.put(HAS_SHARED_ITEMS, shareStatementLong);
        if (hasMessage) {
            vars.put(USER_MESSAGE, data.notification.getMessage());
        }

        vars.put(VIEW_ITEMS_LINK, shareUrl);
        vars.put(VIEW_ITEMS_LABEL, data.textSnippets.linkLabel(data.targetProxies.values()));
        return vars;
    }

}
