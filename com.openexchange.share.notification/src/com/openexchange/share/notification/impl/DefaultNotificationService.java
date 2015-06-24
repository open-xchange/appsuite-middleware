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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
import com.openexchange.share.RequestContext;
import com.openexchange.share.core.performer.CreatedShare;
import com.openexchange.share.core.performer.CreatedShares;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.notification.DefaultDecision;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;
import com.openexchange.share.notification.impl.mail.MailNotifications;
import com.openexchange.share.notification.impl.mail.MailNotifications.ShareCreatedBuilder;
import com.openexchange.share.recipient.GuestRecipient;
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
        List<ShareNotification<InternetAddress>> notifications = new ArrayList<ShareNotification<InternetAddress>>(createdShares.size());
        boolean notifyInternalUsers = serviceLookup.getService(ConfigurationService.class).getBoolProperty("com.openexchange.share.notifyInternal", true);
        GroupService groupService = serviceLookup.getService(GroupService.class);
        ContextService contextService = serviceLookup.getService(ContextService.class);
        for (ShareRecipient recipient : createdShares.getRecipients()) {
            CreatedShare share = createdShares.getShare(recipient);
            GuestInfo guestInfo = share.getGuestInfo();
            try {
                if (notifyDecision.notifyAboutCreatedShare(transport, share, session)) {
                    if (recipient.isInternal() && notifyInternalUsers) {
                        InternalRecipient internalRecipient = recipient.toInternal();
                        if (internalRecipient.isGroup()) {
                            Context context = contextService.getContext(guestInfo.getContextID());
                            int[] members = groupService.getGroup(context, internalRecipient.getEntity()).getMember();
                            for (int userId : members) {
                                InternalRecipient userRecipient = new InternalRecipient();
                                userRecipient.setBits(internalRecipient.getBits());
                                userRecipient.setEntity(userId);
                                userRecipient.setGroup(false);
                                notifications.add(buildInternalShareCreatedMailNotification(userRecipient, share, message, session, requestContext));
                            }
                        } else {
                            notifications.add(buildInternalShareCreatedMailNotification(internalRecipient, share, message, session, requestContext));
                        }
                    } else if (recipient.getType() == RecipientType.GUEST) {
                        notifications.add(buildShareCreatedMailNotification(recipient, share, message, session, requestContext));
                    }
                }
            } catch (Exception e) {
                collectWarning(warnings, e, guestInfo.getEmailAddress());
            }
        }

        for (ShareNotification<InternetAddress> shareNotification : notifications) {
            try {
                send(shareNotification);
            } catch (Exception e) {
                collectWarning(warnings, e, shareNotification.getTransportInfo().toUnicodeString());
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
                .setAccountName(mailAddress)
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
    private ShareNotification<InternetAddress> buildShareCreatedMailNotification(ShareRecipient recipient, CreatedShare share, String message, Session session, RequestContext requestContext) throws OXException {
        GuestInfo guestInfo = share.getGuestInfo();
        if (Strings.isEmpty(guestInfo.getEmailAddress())) {
            String guestName = guestInfo.getDisplayName();
            if (Strings.isEmpty(guestName)) {
                guestName = NotificationStrings.UNKNOWN_USER_NAME;
            }

            throw ShareNotifyExceptionCodes.MISSING_MAIL_ADDRESS.create(guestName, guestInfo.getGuestID(), guestInfo.getContextID());
        }

        try {
            ShareCreatedBuilder shareCreatedBuilder = MailNotifications.shareCreated()
                .setTransportInfo(new InternetAddress(guestInfo.getEmailAddress(), true))
                .setContextID(guestInfo.getContextID())
                .setGuestID(guestInfo.getGuestID())
                .setLocale(guestInfo.getLocale())
                .setSession(session)
                .setTargets(toList(share.getTargets()))
                .setMessage(message)
                .setIntitialShare(isNewGuest(recipient))
                .setRequestContext(requestContext)
                .setShareUrl(share.getUrl(requestContext));

            return shareCreatedBuilder.build();
        } catch (AddressException e) {
            throw ShareNotifyExceptionCodes.INVALID_MAIL_ADDRESS.create(guestInfo.getEmailAddress());
        }
    }

    /**
     * Builds a {@link ShareNotification} ready to be sent out to an internal recipient via mail.
     *
     * @return the built ShareNotification
     * @throws OXException
     */
    private ShareNotification<InternetAddress> buildInternalShareCreatedMailNotification(InternalRecipient recipient, CreatedShare share, String message, Session session, RequestContext requestContext) throws OXException {
        int contextId = session.getContextId();
        User internalUser = serviceLookup.getService(UserService.class).getUser(recipient.getEntity(), contextId);
        try {
            ShareCreatedBuilder shareCreatedBuilder = MailNotifications.shareCreated()
                .setTransportInfo(new InternetAddress(internalUser.getMail(), true))
                .setContextID(contextId)
                .setGuestID(internalUser.getId())
                .setLocale(internalUser.getLocale())
                .setSession(session)
                .setTargets(toList(share.getTargets()))
                .setMessage(message)
                .setIntitialShare(isNewGuest(recipient))
                .setRequestContext(requestContext)
                .setShareUrl(share.getUrl(requestContext));

            return shareCreatedBuilder.build();
        } catch (AddressException e) {
            throw ShareNotifyExceptionCodes.INVALID_MAIL_ADDRESS.create(internalUser.getMail());
        }
    }

    /**
     * Determines if a {@link ShareRecipient} is a newly created {link GuestRecipient}.
     *
     * @param recipient The {@link ShareRecipient}
     * @return true if the recipient is a {link GuestRecipient} that had to be created.
     */
    private boolean isNewGuest(ShareRecipient recipient) {
        if (RecipientType.GUEST.equals(recipient.getType())) {
            GuestRecipient guestRecipient = GuestRecipient.class.cast(recipient);
            return guestRecipient.wasCreated();
        }
        return false;
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
                warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(oxe, oxe.getMessage(), emailAddress));
            }
        } else {
            LOG.error("Error while sending notification mail to {}", emailAddress, e);
            warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage(), emailAddress));
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
