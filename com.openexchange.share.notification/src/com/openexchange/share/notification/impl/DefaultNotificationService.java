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

package com.openexchange.share.notification.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.Entities;
import com.openexchange.share.notification.NotificationStrings;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;
import com.openexchange.share.notification.impl.mail.MailNotifications;
import com.openexchange.share.notification.impl.mail.MailNotifications.ShareCreatedBuilder;
import com.openexchange.user.UserService;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link DefaultNotificationService} - The default share notification service.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class DefaultNotificationService implements ShareNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultNotificationService.class);

    private final ServiceLookup serviceLookup;

    /** The queue for additional handlers */
    private final ConcurrentMap<Transport, ShareNotificationHandler<?>> handlers;

    /**
     * Initializes a new {@link DefaultNotificationService}.
     */
    public DefaultNotificationService(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
        handlers = new ConcurrentHashMap<Transport, ShareNotificationHandler<?>>();
    }

    /**
     * Adds specified handler.
     *
     * @param handler The handler to add
     */
    public void add(ShareNotificationHandler<?> handler) {
        handlers.putIfAbsent(handler.getTransport(), handler);
    }

    /**
     * Removes given handler
     *
     * @param handler The handler to remove
     */
    public void remove(ShareNotificationHandler<?> handler) {
        handlers.remove(handler.getTransport(), handler);
    }

    @Override
    public List<OXException> sendShareCreatedNotifications(Transport transport, Entities entities, String message, ShareTargetPath targetPath, Session session, HostData hostData) {
        return sendShareNotifications0(transport, entities, message, targetPath, session, hostData);
    }

    @Override
    public List<OXException> sendShareNotifications(Transport transport, Entities entities, String message, ShareTargetPath targetPath, Session session, HostData hostData) {
        return sendShareNotifications0(transport, entities, message, targetPath, session, hostData);
    }

    @Override
    public List<OXException> sendLinkNotifications(Transport transport, List<Object> transportInfos, String message, ShareInfo link, Session session, HostData hostData) {
        if (transport != Transport.MAIL) {
            throw new IllegalArgumentException("Transport '" + transport.toString() + "' is not implemented yet!");
        }

        GuestInfo guestInfo = link.getGuest();
        List<OXException> warnings = new ArrayList<OXException>();
        Set<InternetAddress> collectedAddresses = new HashSet<InternetAddress>();

        for (Object transportInfoObj : transportInfos) {
            InternetAddress transportInfo = null;
            try {
                transportInfo = (InternetAddress) transportInfoObj;
                LinkCreatedNotification<InternetAddress> notification = MailNotifications.linkCreated()
                    .setTransportInfo(transportInfo)
                    .setContextID(session.getContextId())
                    .setUserID(guestInfo.getGuestID())
                    .setLocale(guestInfo.getLocale())
                    .setSession(session)
                    .setTarget(link.getTarget())
                    .setMessage(message)
                    .setHostData(hostData)
                    .setShareUrl(link.getShareURL(hostData))
                    .setExpiryDate(guestInfo.getExpiryDate())
                    .setPassword(guestInfo.getPassword())
                    .build();
                send(notification);

                collectedAddresses.add(transportInfo);
            } catch (Exception e) {
                String mailAddress = null;
                if (transportInfo != null) {
                    mailAddress = transportInfo.getAddress();
                }
                collectWarning(warnings, e, mailAddress);
            }
        }

        ContactCollectorService ccs = serviceLookup.getOptionalService(ContactCollectorService.class);
        if ((null != ccs) && !collectedAddresses.isEmpty()) {
            ccs.memorizeAddresses(new ArrayList<InternetAddress>(collectedAddresses), true, session);
        }

        return warnings;
    }

    @Override
    public void sendPasswordResetConfirmationNotification(Transport transport, GuestInfo guestInfo, String confirmToken, HostData hostData) throws OXException {
        if (transport != Transport.MAIL) {
            throw new IllegalArgumentException("Transport '" + transport.toString() + "' is not implemented yet!");
        }

        try {
            UserService userService = serviceLookup.getService(UserService.class);
            String mailAddress = guestInfo.getEmailAddress();
            String displayName = guestInfo.getDisplayName();
            if (null == displayName) {
                displayName = mailAddress;
            }
            User guest = userService.getUser(guestInfo.getGuestID(), guestInfo.getContextID());
            String baseToken = guestInfo.getBaseToken();

            ShareNotification<InternetAddress> notification = MailNotifications.passwordConfirm().setTransportInfo(new QuotedInternetAddress(mailAddress, displayName, "UTF-8")).setContextID(guestInfo.getContextID()).setUserID(guestInfo.getGuestID()).setLocale(guest.getLocale()).setHostData(hostData).setShareUrl(ShareLinks.generateExternal(hostData, baseToken, null)).setConfirmPasswordResetUrl(ShareLinks.generateConfirmPasswordReset(hostData, baseToken, confirmToken)).build();

            send(notification);
        } catch (Exception e) {
            if (e instanceof OXException) {
                throw (OXException) e;
            }

            throw ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private <T> void send(ShareNotification<T> notification) throws OXException {
        @SuppressWarnings("unchecked") ShareNotificationHandler<T> handler = (ShareNotificationHandler<T>) handlers.get(notification.getTransport());
        if (handler == null) {
            throw new OXException(new IllegalArgumentException("No provider exists to handle notifications for transport " + notification.getTransport().toString()));
        }

        handler.send(notification);
    }

    /**
     * Builds a {@link ShareNotification} ready to be sent out to a guest recipient via mail.
     *
     * @return the built ShareNotification
     * @throws OXException
     */
    private ShareNotification<InternetAddress> buildShareCreatedMailNotification(UserDetail userDetail, ShareTarget target, String message, String shareUrl, Session session, HostData hostData) throws OXException {
        User user = userDetail.getUser();
        if (Strings.isEmpty(user.getMail())) {
            String guestName = user.getDisplayName();
            if (Strings.isEmpty(guestName)) {
                guestName = NotificationStrings.UNKNOWN_USER_NAME;
            }

            throw ShareNotifyExceptionCodes.MISSING_MAIL_ADDRESS.create(guestName, user.getId(), session.getContextId());
        }

        try {
            ShareCreatedBuilder shareCreatedBuilder = MailNotifications.shareCreated()
                .setTransportInfo(new QuotedInternetAddress(user.getMail(), true))
                .setContextID(session.getContextId())
                .setUserID(user.getId())
                .setTargetGroup(userDetail.getGroup())
                .setLocale(user.getLocale())
                .setSession(session)
                .setTargets(Collections.singletonList(target))
                .setMessage(message)
                .setHostData(hostData)
                .setShareUrl(shareUrl);

            return shareCreatedBuilder.build();
        } catch (AddressException e) {
            throw ShareNotifyExceptionCodes.INVALID_MAIL_ADDRESS.create(e, user.getMail());
        }
    }

    /**
     * (Re-)Sends notifications about one or more shares to multiple recipients.
     *
     * @param transport The type of {@link Transport} to use when sending notifications
     * @param entities The entities to notify
     * @param message The (optional) additional message for the notification. Can be <code>null</code>.
     * @param targetPath The path to the share target
     * @param session The session of the notifying user
     * @param hostData The host data to generate share links
     * @return Any exceptions occurred during notification, or an empty list if all was fine
     */
    private List<OXException> sendShareNotifications0(Transport transport, Entities entities, String message, ShareTargetPath targetPath, Session session, HostData hostData) {
        if (transport != Transport.MAIL) {
            throw new IllegalArgumentException("Transport '" + transport.toString() + "' is not implemented yet!");
        }

        List<OXException> warnings = new ArrayList<OXException>();
        GroupService groupService = serviceLookup.getService(GroupService.class);
        ContextService contextService = serviceLookup.getService(ContextService.class);
        UserService userService = serviceLookup.getService(UserService.class);
        Context context;
        try {
            context = contextService.getContext(session.getContextId());
        } catch (OXException e) {
            collectWarning(warnings, e);
            return warnings;
        }

       TIntObjectMap<UserDetail> usersById = new TIntObjectHashMap<>();
        for (int userId : entities.getUsers()) {
            User user = null;
            try {
                user = userService.getUser(userId, context);
                usersById.put(userId, new UserDetail(user));
            } catch (OXException e) {
                String mailAddress = null;
                if (user != null) {
                    mailAddress = user.getMail();
                }
                collectWarning(warnings, e, mailAddress);
            }
        }

        for (int groupId : entities.getGroups()) {
            Group group;
            try {
                group = groupService.getGroup(context, groupId);
            } catch (OXException e) {
                collectWarning(warnings, e);
                continue;
            }
            for (int userId : group.getMember()) {
                if (!usersById.containsKey(userId)) {
                    User user = null;
                    try {
                        user = userService.getUser(userId, context);
                        UserDetail userDetail = new UserDetail(user);
                        userDetail.setGroup(group);
                        usersById.put(userId, userDetail);
                    } catch (OXException e) {
                        String mailAddress = null;
                        if (user != null) {
                            mailAddress = user.getMail();
                        }
                        collectWarning(warnings, e, mailAddress);
                    }
                }
            }
        }

        // remove sharing user if he somehow made it into the list of recipients
        usersById.remove(session.getUserId());

        ModuleSupport moduleSupport = serviceLookup.getService(ModuleSupport.class);
        ShareTarget srcTarget = new ShareTarget(targetPath.getModule(), targetPath.getFolder(), targetPath.getItem());
        Set<InternetAddress> collectedAddresses = new HashSet<InternetAddress>();

        TIntObjectIterator<UserDetail> it = usersById.iterator();
        for (int i = usersById.size(); i-- > 0; ) {
            it.advance();
            User user = null;
            try {
                int userId = it.key();
                UserDetail userDetail = it.value();
                user = userDetail.getUser();
                ShareTarget dstTarget = moduleSupport.adjustTarget(srcTarget, session, userId);
                String shareUrl;
                if (user.isGuest()) {
                    if (dstTarget.getModule() == Module.MAIL.getFolderConstant()) {
                        String m = Module.getForFolderConstant(dstTarget.getModule()).getName();
                        throw ShareExceptionCodes.SHARING_NOT_SUPPORTED.create(m == null ? Integer.toString(dstTarget.getModule()) : m);
                    }

                    shareUrl = ShareLinks.generateExternal(hostData, new ShareToken(context.getContextId(), user).getToken(), targetPath);
                    String mail = user.getMail();
                    if (Strings.isNotEmpty(mail)) {
                        collectedAddresses.add(new QuotedInternetAddress(mail));
                    }
                } else {
                    shareUrl = ShareLinks.generateInternal(hostData, dstTarget);
                }
                ShareNotification<InternetAddress> shareNotification = buildShareCreatedMailNotification(userDetail, dstTarget, message, shareUrl, session, hostData);
                send(shareNotification);
            } catch (Exception e) {
                String mailAddress = null;
                if (user != null) {
                    mailAddress = user.getMail();
                }
                collectWarning(warnings, e, mailAddress);
            }
        }

        ContactCollectorService ccs = serviceLookup.getOptionalService(ContactCollectorService.class);
        if (null != ccs) {
            if (!collectedAddresses.isEmpty()) {
                ccs.memorizeAddresses(new ArrayList<InternetAddress>(collectedAddresses), true, session);
            }
        }

        return warnings;
    }

    private static final class UserDetail {

        private final User user;
        private Group group;

        /**
         * Initializes a new {@link UserDetail}.
         */
        UserDetail(User user) {
            super();
            this.user = user;
        }

        User getUser() {
            return user;
        }

        Group getGroup() {
            return group;
        }

        void setGroup(Group group) {
            this.group = group;
        }
    }

    private static void collectWarning(List<OXException> warnings, OXException e, String emailAddress) {
        if (emailAddress == null) {
            emailAddress = "unknown";
        }

        if (e.isPrefix(ShareNotifyExceptionCodes.PREFIX)) {
            warnings.add(e);
        } else {
            LOG.error("Error while sending notification mail to {}", emailAddress, e);
            warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR_FOR_RECIPIENT.create(e, e.getMessage(), emailAddress));
        }
    }

    private static void collectWarning(List<OXException> warnings, Exception e, String emailAddress) {
        if (e instanceof OXException) {
            collectWarning(warnings, (OXException) e, emailAddress);
        } else {
            if (emailAddress == null) {
                emailAddress = "unknown";
            }
            LOG.error("Error while sending notification mail to {}", emailAddress, e);
            warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR_FOR_RECIPIENT.create(e, e.getMessage(), emailAddress));
        }
    }

    private static void collectWarning(List<OXException> warnings, OXException e) {
        if (e.isPrefix(ShareNotifyExceptionCodes.PREFIX)) {
            warnings.add(e);
        } else {
            LOG.error("Error while sending notification mail", e);
            warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
    }

    /**
     * Converts the passed permissions to a folder permission bit mask if necessary
     *
     * @param type
     * @param permissions
     * @return
     */
    private static int adjustPermissions(Entities.Permission permission) {
        switch (permission.getType()) {
            case FOLDER:
                return permission.getPermissions();
            case OBJECT:
                return ObjectPermission.convertFolderPermissionBits(permission.getPermissions());
            default:
                throw new IllegalArgumentException("Unknown permission type: " + permission.getType());
        }
    }
}
