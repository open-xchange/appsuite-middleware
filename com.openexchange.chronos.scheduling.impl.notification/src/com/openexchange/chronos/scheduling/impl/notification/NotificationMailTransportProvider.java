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
import java.util.Locale;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.ChangeNotification;
import com.openexchange.chronos.scheduling.ScheduleStatus;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.chronos.scheduling.common.AbstractMailTransportProvider;
import com.openexchange.chronos.scheduling.common.Constants;
import com.openexchange.chronos.scheduling.common.MailUtils;
import com.openexchange.chronos.scheduling.common.Messages;
import com.openexchange.chronos.scheduling.common.MimeMessageBuilder;
import com.openexchange.chronos.scheduling.common.Utils;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
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
    public int getRanking() {
        return 90;
    }

    @Override
    @NonNull
    public ScheduleStatus send(@NonNull Session session, @NonNull ChangeNotification notification) {
        return sendMail(session, notification.getOriginator(), notification.getRecipient(), notification.getAction(), notification.getScheduleChange(), notification.getResource().getEvents(), (Map<String, String>) notification.getAdditional(Constants.ADDITIONAL_HEADER_MAIL_HEADERS, Map.class), notification.getAdditional("notification", Boolean.class));
    }

    @Override
    @NonNull
    public ScheduleStatus send(@NonNull Session session, @NonNull SchedulingMessage message) {
        return sendMail(session, message.getOriginator(), message.getRecipient(), message.getScheduleChange().getAction(), message.getScheduleChange(), message.getResource().getEvents(), (Map<String, String>) message.getAdditional(Constants.ADDITIONAL_HEADER_MAIL_HEADERS, Map.class), message.getAdditional("notification", Boolean.class));
    }

    @NonNull
    private ScheduleStatus sendMail(Session session, CalendarUser originator, CalendarUser recipient, ChangeAction action, ScheduleChange scheduleChange, List<Event> events, Map<String, String> additionals, Boolean notify) {
        try {
            if (false == canHandle(recipient)) {
                return ScheduleStatus.REJECTED;
            }

            List<Event> sorted = CalendarUtils.sortSeriesMasterFirst(events);
            if (false == wantsNotification(session.getContextId(), originator, recipient, action, sorted.get(0))) {
                return ScheduleStatus.DELIVERED;
            }
            if (false == shouldBeSent(session.getContextId(), recipient, action, sorted, notify)) {
                return ScheduleStatus.DELIVERED;
            }
            /*
             * Build the message
             */
            //@formatter:off
            MimeMessage mime = new MimeMessageBuilder()
                .setFrom(originator)
                .setTo(recipient)
                .setSubject(getSubject(serviceLookup.getServiceSafe(UserService.class), session.getContextId(), action, originator, recipient, MailUtils.getSummary(sorted)))
                .setContent(new InternalMimePartFactory(serviceLookup, scheduleChange))
                .setAdditionalHeader(additionals)
                .setPriority()
                .setOXHeader(recipient, action, sorted.get(0))
                .setMailerInfo(serviceLookup.getOptionalService(VersionService.class))
                .setTracing(sorted.get(0).getUid())
                .setOrganization(serviceLookup.getOptionalService(ContactService.class), session)
                .setSentDate(Utils.getTimeZone(serviceLookup.getServiceSafe(UserService.class), session.getContextId(), originator, recipient))
                .setAutoGenerated()
                .build();
            //@formatter:on

            /*
             * Finally transport mail
             */
            return transportMail(session, mime);
        } catch (OXException | MessagingException e) {
            LOGGER.error("Unable to send message from {} to {}", originator.getEMail(), recipient.getEMail(), e);
        }
        return ScheduleStatus.NOT_DELIVERED;
    }

    private boolean canHandle(CalendarUser recipient) {
        return Utils.isInternalCalendarUser(recipient);
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
    private boolean wantsNotification(int contextId, CalendarUser originator, CalendarUser recipient, ChangeAction action, Event event) throws OXException {
        // Avoid sending message to user himself
        if (null == originator.getSentBy()) {
            if (CalendarUtils.matches(originator, recipient)) {
                return false;
            }
        } else {
            if (CalendarUtils.matches(originator.getSentBy(), recipient)) {
                return false;
            }
        }

        boolean isInterestedInChanges = true;
        boolean isInterestedInStateChanges = true;
        if (recipient.getEntity() > 0) {
            UserSettingMailStorage usmStorage = UserSettingMailStorage.getInstance();
            UserSettingMail usm = usmStorage.getUserSettingMail(recipient.getEntity(), contextId);
            if (null != usm) {
                isInterestedInChanges = usm.isNotifyAppointments();
                if (CalendarUtils.isOrganizerSchedulingResource(event, recipient.getEntity())) {
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
        boolean aboutStateChanges = ChangeAction.REPLY.equals(action);
        if (isInterestedInStateChanges && !isInterestedInChanges) {
            LOGGER.debug("wantsNotification (1), User: {}, {}, {}, {}", I(recipient.getEntity()), B(isInterestedInStateChanges), B(isInterestedInChanges), B(aboutStateChanges));
            return aboutStateChanges;
        }

        // Interested in other changes, but not in state changes
        if (!isInterestedInStateChanges && isInterestedInChanges) {
            LOGGER.debug("wantsNotification (2), User: {}, {}, {}, {}", I(recipient.getEntity()), B(isInterestedInStateChanges), B(isInterestedInChanges), B(aboutStateChanges));
            return !aboutStateChanges;
        }

        return true;
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
    private boolean shouldBeSent(int contextId, CalendarUser recipient, ChangeAction action, List<Event> events, Boolean notify) throws OXException {
        if (null == events || events.size() < 1) {
            // No event(s) to report
            return false;
        }

        if (endsInPast(events)) {
            // No mail for events in the past
            return false;
        }
        if (ChangeAction.CANCEL.equals(action)) {
            // Check if cancel mail is enforced
            ConfigurationService configurationService = serviceLookup.getOptionalService(ConfigurationService.class);
            if (null == configurationService || configurationService.getBoolProperty("notify_participants_on_delete", true)) {
                return true;
            }
            // Check if recipient is hidden
            if (Attendee.class.isAssignableFrom(recipient.getClass())) {
                Attendee attendee = (Attendee) recipient;
                if (attendee.isHidden()) {
                    return false;
                }
            }
        }

        if (!recipientIsOrganizerAndHasNoAccess(recipient, events, contextId)) {
            return false;
        }
        if (null != notify && false == notify.booleanValue()) {
            // Don't send notification mails for internal users if the flag is set. See bug 62098.
            return false;
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

    private String getSubject(UserService userService, int contextId, ChangeAction action, CalendarUser originator, CalendarUser recipient, String summary) {
        Locale locale = Utils.getLocale(userService, contextId, originator, recipient);
        StringHelper helper = StringHelper.valueOf(locale);
        String subject;

        switch (action) {
            case CREATE:
                subject = Messages.SUBJECT_NEW_APPOINTMENT;
                break;
            case CANCEL:
                subject = Messages.SUBJECT_CANCELLED_APPOINTMENT;
                break;
            default:
                subject = Messages.SUBJECT_CHANGED_APPOINTMENT;
                break;
        }
        subject = String.format(helper.getString(subject), summary);
        return subject;
    }

}
