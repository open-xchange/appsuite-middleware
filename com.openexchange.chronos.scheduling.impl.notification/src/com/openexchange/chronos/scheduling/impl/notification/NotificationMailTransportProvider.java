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

package com.openexchange.chronos.scheduling.impl.notification;

import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.scheduling.ChangeNotification;
import com.openexchange.chronos.scheduling.RecipientSettings;
import com.openexchange.chronos.scheduling.ScheduleStatus;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.chronos.scheduling.common.AbstractMailTransportProvider;
import com.openexchange.chronos.scheduling.common.MimeMessageBuilder;
import com.openexchange.chronos.scheduling.common.Utils;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.version.VersionService;

/**
 * {@link NotificationMailTransportProvider} - Transports a {@link SchedulingMessage} as internal notification mail
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class NotificationMailTransportProvider extends AbstractMailTransportProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationMailTransportProvider.class);

    private static final Property PREFER_NO_REPLY_PROPERTY = DefaultProperty.valueOf("com.openexchange.calendar.preferNoReplyForNotifications", Boolean.FALSE);

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
        return sendMail(session, notification.getOriginator(), notification.getRecipient(), notification.getAction(), notification.getScheduleChange(), notification.getResource(), notification.getRecipientSettings(), getAdditionalHeaders(notification));
    }

    @Override
    @NonNull
    public ScheduleStatus send(@NonNull Session session, @NonNull SchedulingMessage message) {
        return sendMail(session, message.getOriginator(), message.getRecipient(), message.getScheduleChange().getAction(), message.getScheduleChange(), message.getResource(), message.getRecipientSettings(), getAdditionalHeaders(message));
    }

    @NonNull
    private ScheduleStatus sendMail(Session session, CalendarUser originator, CalendarUser recipient, ChangeAction action, ScheduleChange scheduleChange, CalendarObjectResource resource, RecipientSettings recipientSettings, Map<String, String> additionals) {
        try {
            if (false == CalendarUtils.isInternal(recipient, recipientSettings.getRecipientType())) {
                return ScheduleStatus.REJECTED;
            }

            /*
             * Build the message
             */
            //@formatter:off
            Event event = Utils.selectDescribedEvent(resource, scheduleChange.getChanges());
            String subject = getSubject(recipientSettings, action, originator, event.getSummary(), scheduleChange.getOriginatorPartStat());
            MimeMessage mime = new MimeMessageBuilder()
                .setFrom(originator)
                .setTo(recipient)
                .setSubject(subject)
                .setContent(new InternalMimePartFactory(serviceLookup, scheduleChange, recipientSettings))
                .setAdditionalHeader(additionals)
                .setPriority()
                .setOXHeader(recipient, action, event, scheduleChange.getOriginatorPartStat())
                .setMailerInfo(serviceLookup.getOptionalService(VersionService.class))
                .setTracing(resource.getUid())
                .setOrganization(serviceLookup.getOptionalService(ContactService.class), session)
                .setSentDate(recipientSettings.getTimeZone())
                .setAutoGenerated()
                .build();
            //@formatter:on

            /*
             * Finally transport mail
             */
            return transportMail(session, mime);
        } catch (OXException | MessagingException e) {
            LOGGER.error("Unable to send message from {} to {}", originator, recipient, e);
        }
        return ScheduleStatus.NOT_DELIVERED;
    }

    private String getSubject(RecipientSettings recipientSettings, ChangeAction action, CalendarUser originator, String summary, ParticipationStatus partStat) {
        StringHelper helper = StringHelper.valueOf(recipientSettings.getLocale());
        String subject;
        switch (action) {
            case CREATE:
                subject = Messages.SUBJECT_NEW_APPOINTMENT;
                break;
            case CANCEL:
                subject = Messages.SUBJECT_CANCELLED_APPOINTMENT;
                break;
            case REPLY:
                return getPartStatSubject(originator, partStat, recipientSettings.getLocale(), summary);
            default:
                subject = Messages.SUBJECT_CHANGED_APPOINTMENT;
                break;
        }
        subject = String.format(helper.getString(subject), summary);
        return subject;
    }


    @Override
    public boolean preferNoReplyAccount(Session session) throws OXException {
        return super.preferNoReplyAccount(session) ||
            serviceLookup.getServiceSafe(LeanConfigurationService.class).getBooleanProperty(session.getUserId(), session.getContextId(), PREFER_NO_REPLY_PROPERTY);
    }
}
