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

package com.openexchange.share.notification.impl.mail;

import static com.openexchange.osgi.Tools.requireService;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.mail.internet.InternetAddress;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.notification.BasicNotificationTemplate;
import com.openexchange.notification.BasicNotificationTemplate.FooterImage;
import com.openexchange.notification.FullNameBuilder;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.NotificationMailConfig;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.DriveTargetProxyType;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetProxyType;
import com.openexchange.share.notification.impl.NotificationStrings;
import com.openexchange.share.notification.impl.ShareCreatedNotification;
import com.openexchange.user.UserService;

/**
 * {@link ShareCreatedMail}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareCreatedMail extends NotificationMail {

    private static final String HAS_SHARED_ITEMS = "has_shared_items";
    private static final String PLEASE_CLICK = "please_click";
    private static final String USER_MESSAGE = "user_message";
    private static final String VIEW_ITEMS_LINK = "view_items_link";
    private static final String VIEW_ITEMS_LABLE = "view_items_lable";
    private static final String WILL_EXPIRE = "will_expire";

    protected ShareCreatedMail(MailData data) {
        super(data);
    }

    private static class CollectVarsData {
        ShareCreatedNotification<InternetAddress> notification;
        User sharingUser;
        User targetUser;

        Translator translator;
        String shareOwnerName;
        Set<TargetProxyType> targetProxyTypes;
        HashMap<ShareTarget, TargetProxy> targetProxies;
    }

    public static ShareCreatedMail init(ShareCreatedNotification<InternetAddress> notification, TransportProvider transportProvider, ServiceLookup services) throws OXException {
        ContextService contextService = requireService(ContextService.class, services);
        UserService userService = requireService(UserService.class, services);
        ServerConfigService serverConfigService = requireService(ServerConfigService.class, services);
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        ModuleSupport moduleSupport = requireService(ModuleSupport.class, services);
        ConfigurationService configService = requireService(ConfigurationService.class, services);

        Context context = contextService.getContext(notification.getContextID());
        User sharingUser = userService.getUser(notification.getSession().getUserId(), context);
        User targetUser = userService.getUser(notification.getTargetUserID(), context);
        Translator translator = translatorFactory.translatorFor(targetUser.getLocale());

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
        data.targetUser = targetUser;
        data.translator = translator;
        data.shareOwnerName = FullNameBuilder.buildFullName(sharingUser, translator);
        data.targetProxies = targetProxies;
        data.targetProxyTypes = targetProxyTypes;

        ServerConfig serverConfig = serverConfigService.getServerConfig(
            notification.getRequestContext().getHostname(),
            targetUser.getId(),
            context.getContextId());
        NotificationMailConfig mailConfig = serverConfig.getNotificationMailConfig();
        BasicNotificationTemplate basicTemplate = BasicNotificationTemplate.newInstance(mailConfig);
        Map<String, Object> vars = prepareShareCreatedVars(data);
        basicTemplate.applyStyle(vars);
        FooterImage footerImage = basicTemplate.applyFooter(vars);
        String htmlContent = compileTemplate("notify.share.create.mail.html.tmpl", vars, services);

        MailData mailData = new MailData();
        mailData.sender = getSenderAddress(configService, notification.getSession(), sharingUser);
        mailData.recipient = notification.getTransportInfo();
        mailData.subject = determineSubject(data);
        mailData.htmlContent = htmlContent;
        mailData.footerImage = footerImage;
        mailData.context = context;
        mailData.transportProvider = transportProvider;
        mailData.mailHeaders = new HashMap<>(5);
        mailData.mailHeaders.put("X-Open-Xchange-Share-Type", notification.getType().getId());
        mailData.mailHeaders.put("X-Open-Xchange-Share-URL", notification.getShareUrl());
        return new ShareCreatedMail(mailData);
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
        boolean hasMessage = !Strings.isEmpty(data.notification.getMessage());
        String shareUrl = data.notification.getShareUrl();
        String email = data.sharingUser.getMail();
        List<ShareTarget> shareTargets = data.notification.getShareTargets();
        String fullName = data.shareOwnerName;

        if (shareTargets.size() > 1) {
            int count = shareTargets.size();
            if (data.targetProxyTypes.size() > 1) {//multiple shares of different types
                if (hasMessage) {
                    vars.put(HAS_SHARED_ITEMS, String.format(data.translator.translate(NotificationStrings.HAS_SHARED_ITEMS_AND_MESSAGE), fullName, email, count));
                } else {
                    vars.put(HAS_SHARED_ITEMS, String.format(data.translator.translate(NotificationStrings.HAS_SHARED_ITEMS), fullName, email, count));
                    vars.put(PLEASE_CLICK, data.translator.translate(NotificationStrings.PLEASE_CLICK_THEM));
                }
                addViewItemsToVars(vars, null, data.translator, true, shareUrl);
            } else {//multiple shares of single type
                TargetProxyType targetProxyType = data.targetProxyTypes.iterator().next();
                addSharedItemsToVars(vars, targetProxyType, hasMessage, data.translator, fullName, email, count);
                addViewItemsToVars(vars, targetProxyType, data.translator, true, shareUrl);
            }
        } else {
            ShareTarget shareTarget = shareTargets.get(0);
            TargetProxy targetProxy = data.targetProxies.get(shareTarget);
            TargetProxyType targetProxyType = targetProxy.getProxyType();
            String proxyTitle = targetProxy.getTitle();
            addSharedItemToVars(vars, targetProxyType, hasMessage, data.translator, fullName, email, proxyTitle);
            addViewItemsToVars(vars, targetProxyType, data.translator, false, shareUrl);
        }

        if (hasMessage) {
            vars.put(USER_MESSAGE, data.notification.getMessage());
        }

        Date expiryDate = data.notification.getShareTargets().iterator().next().getExpiryDate();
        if (data.targetUser.isGuest() && expiryDate != null) { // no expiry for internal users yet
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, data.targetUser.getLocale());
            Date localExpiry = new Date(expiryDate.getTime() + TimeZone.getTimeZone(data.targetUser.getTimeZone()).getOffset(expiryDate.getTime()));
            vars.put(WILL_EXPIRE, String.format(data.translator.translate(NotificationStrings.LINK_EXPIRE), dateFormat.format(localExpiry)));
        }

        return vars;
    }

    private static void addSharedItemToVars(Map<String, Object> vars, TargetProxyType targetProxyType, boolean hasMessage, Translator translator, String fullName, String email, String filename) {
        if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_PHOTO_AND_MESSAGE), fullName, email, filename));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_IMAGE), fullName, email, filename));
                vars.put(PLEASE_CLICK, translator.translate(NotificationStrings.PLEASE_CLICK_IT));
            }
        } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FILE_AND_MESSAGE), fullName, email, filename));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FILE), fullName, email, filename));
                vars.put(PLEASE_CLICK, translator.translate(NotificationStrings.PLEASE_CLICK_IT));
            }
        } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {

            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDER_AND_MESSAGE), fullName, email, filename));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDER), fullName, email, filename));
                vars.put(PLEASE_CLICK, translator.translate(NotificationStrings.PLEASE_CLICK_IT));
            }
        } else {
            //fall back to item for other types
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEM_AND_MESSAGE), fullName, email, filename));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEM), fullName, email, filename));
                vars.put(PLEASE_CLICK, translator.translate(NotificationStrings.PLEASE_CLICK_IT));
            }
        }
    }

    private static Map<String, Object> addSharedItemsToVars(Map<String, Object> vars, TargetProxyType targetProxyType, boolean hasMessage, Translator translator, String fullName, String email, int count) {
        if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_IMAGES_AND_MESSAGE), fullName, email, count));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_IMAGES), fullName, email, count));
                vars.put(PLEASE_CLICK, translator.translate(NotificationStrings.PLEASE_CLICK_THEM));
            }

        } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FILES_AND_MESSAGE), fullName, email, count));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FILES), fullName, email, count));
                vars.put(PLEASE_CLICK, translator.translate(NotificationStrings.PLEASE_CLICK_THEM));
            }
        } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDERS_AND_MESSAGE), fullName, email, count));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDERS), fullName, email, count));
                vars.put(PLEASE_CLICK, translator.translate(NotificationStrings.PLEASE_CLICK_THEM));
            }
        } else {
            //fall back to item for other types
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_AND_MESSAGE), fullName, email, count));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS), fullName, email, count));
                vars.put(PLEASE_CLICK, translator.translate(NotificationStrings.PLEASE_CLICK_THEM));
            }
        }
        return vars;
    }

    private static void addViewItemsToVars(Map<String, Object> vars, TargetProxyType targetProxyType, Translator translator, boolean multipleShares, String shareLink) {
        vars.put(VIEW_ITEMS_LINK, shareLink);
        if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
            if (multipleShares) {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_IMAGES));
            } else {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_IMAGE));
            }
        } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
            if (multipleShares) {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_FILES));
            } else {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_FILE));
            }
        } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
            if (multipleShares) {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_FOLDERS));
            } else {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_FOLDER));
            }
        } else {
            //fall back to item for other types
            if (multipleShares) {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_ITEMS));
            } else {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_ITEM));
            }
        }
    }

    private static InternetAddress getSenderAddress(ConfigurationService configService, Session session, User user) throws OXException {
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
            try {
                senderAddress.setPersonal(user.getDisplayName());
            } catch (UnsupportedEncodingException e) {
                // try to send nevertheless
            }
        }

        return senderAddress;
    }

    private static String determineSubject(CollectVarsData data) {
        String fullName = data.shareOwnerName;
        int count = data.notification.getShareTargets().size();
        if (count > 1 && data.targetProxyTypes.size() > 1) {
            return String.format(data.translator.translate(NotificationStrings.SUBJECT_SHARED_ITEMS), fullName, count);
        } else {
            ShareTarget shareTarget = data.notification.getShareTargets().get(0);
            TargetProxy targetProxy = data.targetProxies.get(shareTarget);
            TargetProxyType targetProxyType = targetProxy.getProxyType();
            String itemName = targetProxy.getTitle();
            if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
                if (count == 1) {
                    return String.format(data.translator.translate(NotificationStrings.SUBJECT_SHARED_IMAGE), fullName, itemName);
                } else {
                    return String.format(data.translator.translate(NotificationStrings.SUBJECT_SHARED_IMAGES), fullName, count);
                }
            } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
                if (count == 1) {
                    return String.format(data.translator.translate(NotificationStrings.SUBJECT_SHARED_FILE), fullName, itemName);
                } else {
                    return String.format(data.translator.translate(NotificationStrings.SUBJECT_SHARED_FILES), fullName, count);
                }
            } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
                if (count == 1) {
                    return String.format(data.translator.translate(NotificationStrings.SUBJECT_SHARED_FOLDER), fullName, itemName);
                } else {
                    return String.format(data.translator.translate(NotificationStrings.SUBJECT_SHARED_FOLDERS), fullName, count);
                }
            } else {
                //fall back to item for other types
                if (count == 1) {
                    return String.format(data.translator.translate(NotificationStrings.SUBJECT_SHARED_ITEM), fullName, itemName);
                } else {
                    return String.format(data.translator.translate(NotificationStrings.SUBJECT_SHARED_ITEMS), fullName);
                }
            }
        }
    }

}
