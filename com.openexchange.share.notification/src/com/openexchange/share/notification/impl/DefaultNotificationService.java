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

package com.openexchange.share.notification.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.CreatedShare;
import com.openexchange.share.CreatedShares;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
import com.openexchange.share.RequestContext;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.notification.Entities;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;
import com.openexchange.share.notification.impl.mail.MailNotifications;
import com.openexchange.share.notification.impl.mail.MailNotifications.ShareCreatedBuilder;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.user.UserService;

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

    private final NotifyDecision notifyDecision;

    /**
     * Initializes a new {@link DefaultNotificationService}.
     */
    public DefaultNotificationService(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
        handlers = new ConcurrentHashMap<Transport, ShareNotificationHandler<?>>();
        notifyDecision = new DefaultDecision(serviceLookup);
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
    public List<OXException> sendShareCreatedNotifications(Transport transport, CreatedShares createdShares, String message, Session session, RequestContext requestContext) {
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

        Map<Integer, CreatedShare> sharesByUser = new HashMap<>();
        for (ShareRecipient recipient : createdShares.getRecipients()) {
            CreatedShare share = createdShares.getShare(recipient);
            GuestInfo guestInfo = share.getGuestInfo();
            try {
                if (notifyDecision.notifyAboutCreatedShare(transport, share, session)) {
                    if (recipient.isInternal()) {
                        InternalRecipient internalRecipient = recipient.toInternal();
                        if (internalRecipient.isGroup()) {
                            int[] members = groupService.getGroup(context, internalRecipient.getEntity()).getMember();
                            for (int userId : members) {
                                sharesByUser.put(userId, share);
                            }
                        } else {
                            sharesByUser.put(guestInfo.getGuestID(), share);
                        }
                    } else if (recipient.getType() == RecipientType.GUEST) {
                        sharesByUser.put(guestInfo.getGuestID(), share);
                    }
                }
            } catch (Exception e) {
                collectWarning(warnings, e, guestInfo.getEmailAddress());
            }
        }

        for (int userId : sharesByUser.keySet()) {
            User user = null;
            try {
                user = userService.getUser(userId, session.getContextId());
                CreatedShare share = sharesByUser.get(userId);
                ShareNotification<InternetAddress> shareNotification = buildShareCreatedMailNotification(user, toList(share.getTargets()), message, share.getUrl(requestContext), session, requestContext);
                send(shareNotification);
            } catch (Exception e) {
                String mailAddress = null;
                if (user != null) {
                    mailAddress = user.getMail();
                }
                collectWarning(warnings, e, mailAddress);
            }
        }

        return warnings;
    }

    @Override
    public List<OXException> sendShareCreatedNotifications(Transport transport, Entities entities, ShareTarget target, Session session, RequestContext requestContext) {
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

        Map<Integer, User> usersById = new HashMap<>();
        for (int userId : entities.getUsers()) {
            User user = null;
            try {
                user = userService.getUser(userId, context);
                if (notifyDecision.notifyAboutCreatedShare(Transport.MAIL, user, false, entities.getUserPermissionBits(userId), Collections.singletonList(target), session)) {
                    usersById.put(userId, user);
                }
            } catch (OXException e) {
                String mailAddress = null;
                if (user != null) {
                    mailAddress = user.getMail();
                }
                collectWarning(warnings, e, mailAddress);
            }
        }

        for (int groupId : entities.getGroups()) {
            int[] members;
            try {
                members = groupService.getGroup(context, groupId).getMember();
            } catch (OXException e) {
                collectWarning(warnings, e);
                continue;
            }
            for (int userId : members) {
                if (!usersById.containsKey(userId)) {
                    User user = null;
                    try {
                        user = userService.getUser(userId, context);
                        if (notifyDecision.notifyAboutCreatedShare(Transport.MAIL, user, true, entities.getGroupPermissionBits(groupId), Collections.singletonList(target), session)) {
                            usersById.put(userId, user);
                        }
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

        for (int userId : usersById.keySet()) {
            User user = null;
            try {
                user = usersById.get(userId);
                String shareUrl;
                if (user.isGuest()) {
                    shareUrl = ShareLinks.generateExternal(requestContext, new ShareToken(context.getContextId(), user).getToken() + '/' + target.getPath());
                } else {
                    shareUrl = ShareLinks.generateInternal(requestContext, target);
                }
                ShareNotification<InternetAddress> shareNotification = buildShareCreatedMailNotification(user, Collections.singletonList(target), null, shareUrl, session, requestContext);
                send(shareNotification);
            } catch (Exception e) {
                String mailAddress = null;
                if (user != null) {
                    mailAddress = user.getMail();
                }
                collectWarning(warnings, e, mailAddress);
            }
        }

        return warnings;
    }

    @Override
    public void sendPasswordResetConfirmationNotification(Transport transport, GuestShare guestShare, String confirmToken, RequestContext requestContext) throws OXException {
        if (transport != Transport.MAIL) {
            throw new IllegalArgumentException("Transport '" + transport.toString() + "' is not implemented yet!");
        }

        try {
            UserService userService = serviceLookup.getService(UserService.class);
            GuestInfo guestInfo = guestShare.getGuest();
            String mailAddress = guestInfo.getEmailAddress();
            User guest = userService.getUser(guestInfo.getGuestID(), guestInfo.getContextID());
            String baseToken = guestShare.getGuest().getBaseToken();

            ShareNotification<InternetAddress> notification = MailNotifications.passwordConfirm()
                .setTransportInfo(new InternetAddress(mailAddress, true))
                .setContextID(guestInfo.getContextID())
                .setGuestID(guestInfo.getGuestID())
                .setLocale(guest.getLocale())
                .setRequestContext(requestContext)
                .setShareUrl(ShareLinks.generateExternal(requestContext, baseToken))
                .setConfirmPasswordResetUrl(ShareLinks.generateConfirmPasswordReset(requestContext, baseToken, confirmToken))
                .build();

            send(notification);
        } catch (Exception e) {
            if (e instanceof OXException) {
                throw (OXException) e;
            }

            throw ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private <T> void send(ShareNotification<T> notification) throws OXException {
        @SuppressWarnings("unchecked")
        ShareNotificationHandler<T> handler = (ShareNotificationHandler<T>) handlers.get(notification.getTransport());
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
    private ShareNotification<InternetAddress> buildShareCreatedMailNotification(User user, List<ShareTarget> targets, String message, String shareUrl, Session session, RequestContext requestContext) throws OXException {
        if (Strings.isEmpty(user.getMail())) {
            String guestName = user.getDisplayName();
            if (Strings.isEmpty(guestName)) {
                guestName = NotificationStrings.UNKNOWN_USER_NAME;
            }

            throw ShareNotifyExceptionCodes.MISSING_MAIL_ADDRESS.create(guestName, user.getId(), session.getContextId());
        }

        try {
            ShareCreatedBuilder shareCreatedBuilder = MailNotifications.shareCreated()
                .setTransportInfo(new InternetAddress(user.getMail(), true))
                .setContextID(session.getContextId())
                .setGuestID(user.getId())
                .setLocale(user.getLocale())
                .setSession(session)
                .setTargets(targets)
                .setMessage(message)
                .setRequestContext(requestContext)
                .setShareUrl(shareUrl);

            return shareCreatedBuilder.build();
        } catch (AddressException e) {
            throw ShareNotifyExceptionCodes.INVALID_MAIL_ADDRESS.create(user.getMail());
        }
    }

    private static void collectWarning(List<OXException> warnings, Exception e, String emailAddress) {
        if (emailAddress == null) {
            emailAddress = "unknown";
        }

        if (e instanceof OXException) {
            OXException oxe = (OXException) e;
            if (oxe.isPrefix(ShareNotifyExceptionCodes.PREFIX)) {
                warnings.add(oxe);
            } else {
                LOG.error("Error while sending notification mail to {}", emailAddress, oxe);
                warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR_FOR_RECIPIENT.create(oxe, oxe.getMessage(), emailAddress));
            }
        } else {
            LOG.error("Error while sending notification mail to {}", emailAddress, e);
            warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR_FOR_RECIPIENT.create(e, e.getMessage(), emailAddress));
        }
    }

    private static void collectWarning(List<OXException> warnings, Exception e) {
        if (e instanceof OXException) {
            OXException oxe = (OXException) e;
            if (oxe.isPrefix(ShareNotifyExceptionCodes.PREFIX)) {
                warnings.add(oxe);
            } else {
                LOG.error("Error while sending notification mail", oxe);
                warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(oxe, oxe.getMessage()));
            }
        } else {
            LOG.error("Error while sending notification mail", e);
            warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
    }

    private static <T> List<T> toList(Iterable<T> iterable) {
        if (iterable instanceof List<?>) {
            return (List<T>) iterable;
        } else if (iterable instanceof Collection<?>) {
            return new ArrayList<>((Collection<T>) iterable);
        }

        ArrayList<T> list = new ArrayList<>();
        for (T t : iterable) {
            list.add(t);
        }

        return list;
    }

}
