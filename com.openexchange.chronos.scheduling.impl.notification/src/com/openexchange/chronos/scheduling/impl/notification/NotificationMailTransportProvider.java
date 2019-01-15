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

package com.openexchange.chronos.scheduling.impl.notification;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.CalendarObjectResource;
import com.openexchange.chronos.scheduling.ScheduleStatus;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.common.AbstractMailTransportProvider;
import com.openexchange.chronos.scheduling.common.MailUtils;
import com.openexchange.chronos.scheduling.common.MimeMessageBuilder;
import com.openexchange.chronos.scheduling.common.Utils;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;
import com.openexchange.version.VersionService;

/**
 * {@link NotificationMailTransportProvider} - Transports a {@link SchedulingMessage} as internal notification mail
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class NotificationMailTransportProvider extends AbstractMailTransportProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationMailTransportProvider.class);

    /**
     * Initializes a new {@link NotificationMailTransportProvider}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     */
    public NotificationMailTransportProvider(@NonNull ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public @NonNull ScheduleStatus sendMail(Session session, SchedulingMessage message) throws OXException, MessagingException {
        int contextId = session.getContextId();
        if (false == canHandle(message)) {
            return ScheduleStatus.REJECTED;
        }

        if (false == wantsNotification(message, contextId, message.getRecipient().getEntity())) {
            return ScheduleStatus.DELIVERED;
        }
        if (false == shouldBeSent(contextId, message)) {
            return ScheduleStatus.DELIVERED;
        }

        /*
         * Build the message
         */
        //@formatter:off
        MimeMessage mime = new MimeMessageBuilder(contextId, message)
            .setFrom()
            .setTo()
            .setSubject(serviceLookup.getServiceSafe(UserService.class), contextId)
            .setContent(new InternalMimePartFactory(message, serviceLookup))
            .setAdditionalHeader()
            .setPriority()
            .setOXHeader()
            .setMailerInfo(serviceLookup.getOptionalService(VersionService.class))
            .setTracing(MailUtils.getUid(message.getResource().getCalendarObject()))
            .setOrganization(serviceLookup.getOptionalService(ContactService.class), session)
            .setSentDate(serviceLookup.getServiceSafe(UserService.class))
            .setAutoGenerated()
            .build();
        //@formatter:on

        /*
         * Finally transport mail
         */
        return transportMail(session, mime);
    }

    private boolean canHandle(SchedulingMessage message) {
        return Utils.isInternalCalendarUser(message.getRecipient());
    }

    /**
     * Checks if a specific recipient has configurations set, that prohibits to receive the message
     *
     * @param resource The {@link CalendarObjectResource}
     * @param contextId The context identifier
     * @param recipientId The identifier of the recipient.
     * @return <code>true</code> if either the recipient is external or if an internal recipient has configured his account
     *         to receive notification mails
     * @throws OXException
     */
    private boolean wantsNotification(SchedulingMessage message, int contextId, int recipientId) throws OXException {
        // Avoid sending message to user himself
        if (null == message.getOriginator().getSentBy()) {
            if (CalendarUtils.matches(message.getOriginator(), message.getRecipient())) {
                return false;
            }
        } else {
            if (CalendarUtils.matches(message.getOriginator().getSentBy(), message.getRecipient())) {
                return false;
            }
        }

        boolean isInterestedInChanges = true;
        boolean isInterestedInStateChanges = true;
        if (recipientId > 0) {
            UserSettingMailStorage usmStorage = UserSettingMailStorage.getInstance();
            UserSettingMail usm = usmStorage.getUserSettingMail(recipientId, contextId);
            if (null != usm) {
                isInterestedInChanges = usm.isNotifyAppointments();
                if (CalendarUtils.isOrganizerSchedulingResource(message.getResource().getCalendarObject().get(0), recipientId)) {
                    isInterestedInStateChanges = usm.isNotifyAppointmentsConfirmOwner();
                } else {
                    isInterestedInStateChanges = usm.isNotifyAppointmentsConfirmParticipant();
                }
            }
        }

        // Not interested in anything
        if (!isInterestedInChanges && !isInterestedInStateChanges) {
            return false;
        }

        // Interested in state changes, but not in other changes
        boolean aboutStateChanges = isAboutPartStatChanges(message.getDescription());
        if (isInterestedInStateChanges && !isInterestedInChanges) {
            LOGGER.debug("wantsNotification (1), User: {}, {}, {}, {}", I(recipientId), B(isInterestedInStateChanges), B(isInterestedInChanges), B(aboutStateChanges));
            return aboutStateChanges;
        }

        // Interested in other changes, but not in state changes
        if (!isInterestedInStateChanges && isInterestedInChanges) {
            LOGGER.debug("wantsNotification (2), User: {}, {}, {}, {}", I(recipientId), B(isInterestedInStateChanges), B(isInterestedInChanges), B(aboutStateChanges));
            return !aboutStateChanges;
        }

        return true;
    }

    private boolean isAboutPartStatChanges(Description description) {
        return ChangeAction.REPLY.equals(description.getAction());
    }

    /**
     * Gets a value indicating whether the message should be sent to the recipient as per
     * {@link SchedulingMessage#getRecipient()}, or not.
     *
     * @param contextId The context identifier
     * @param message The {@link SchedulingMessage}
     * @return <code>true</code> if the scheduling message should be sent to the recipient,
     *         <code>false</code> if not. E.g. a message should <b>not</b> be sent to the recipient if it exclusively ends in the past
     *         or if no event fields of interest changed
     * @throws OXException If calculating recurrences for a series fails or the admin user lookup fails
     */
    private boolean shouldBeSent(int contextId, SchedulingMessage message) throws OXException {
        List<Event> events = CalendarUtils.sortSeriesMasterFirst(message.getResource().getCalendarObject());
        if (null == events || events.size() < 1) {
            // No event(s) to report
            return false;
        }

        if (endsInPast(events)) {
            // No mail for events in the past
            return false;
        }
        if (Utils.isInternalCalendarUser(message.getRecipient())) {
            if (ChangeAction.CANCEL.equals(message.getDescription().getAction())) {
                // Check if cancel mail is enforced
                ConfigurationService configurationService = serviceLookup.getOptionalService(ConfigurationService.class);
                if (null == configurationService || configurationService.getBoolProperty("notify_participants_on_delete", true)) {
                    return true;
                }
                // Check if recipient is hidden
                if (Attendee.class.isAssignableFrom(message.getRecipient().getClass())) {
                    Attendee attendee = (Attendee) message.getRecipient();
                    if (attendee.isHidden()) {
                        return false;
                    }
                }
            }

            if (!recipientIsOrganizerAndHasNoAccess(message.getRecipient(), events, contextId)) {
                return false;
            }
            Boolean notify = message.getAdditional("notification", Boolean.class);
            if (null != notify && false == notify.booleanValue()) {
                // Don't send notification mails for internal users if the flag is set. See bug 62098.
                return false;
            }
        }

        return true;
    }

    private boolean recipientIsOrganizerAndHasNoAccess(CalendarUser recipient, List<Event> events, int contextId) throws OXException {
        if (Utils.isInternalCalendarUser(recipient)) {
            if (CalendarUtils.isOrganizer(events.get(0), recipient.getEntity())) {
                UserService userService = serviceLookup.getOptionalService(UserService.class);
                if (null != userService && userService.getContext(contextId).getMailadmin() == recipient.getEntity() && MailProperties.getInstance().isAdminMailLoginEnabled() == false) {
                    // Context administrator is recipient but does not have permission to access mail
                    return false;
                }
            }
        }
        return true;
    }

}
