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

package com.openexchange.chronos.scheduling.impl.imip;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.scheduling.ChangeNotification;
import com.openexchange.chronos.scheduling.ScheduleStatus;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.common.AbstractMailTransportProvider;
import com.openexchange.chronos.scheduling.common.Constants;
import com.openexchange.chronos.scheduling.common.MimeMessageBuilder;
import com.openexchange.chronos.scheduling.common.Utils;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.version.VersionService;

/**
 * {@link IMipTransportProvider} - Transports a {@link SchedulingMessage} via iTIP
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class IMipTransportProvider extends AbstractMailTransportProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(IMipTransportProvider.class);

    private static final Property PREFER_NO_REPLY_FOR_IMIP = DefaultProperty.valueOf("com.openexchange.calendar.preferNoReplyForIMip", Boolean.FALSE);

    /**
     * Initializes a new {@link IMipTransportProvider}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     */
    public IMipTransportProvider(@NonNull ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public int getRanking() {
        return 100;
    }

    @Override
    @NonNull
    public ScheduleStatus send(@NonNull Session session, @NonNull ChangeNotification notification) {
        return ScheduleStatus.NO_TRANSPORT;
    }

    @Override
    public @NonNull ScheduleStatus send(@NonNull Session session, @NonNull SchedulingMessage message) {
        try {
            if (false == canHandle(message)) {
                return ScheduleStatus.REJECTED;
            }
            return transportMail(session, build(session, message));
        } catch (OXException | MessagingException | UnsupportedEncodingException e) {
            LOGGER.debug("Unable to generate message", e);
        }
        return ScheduleStatus.NOT_DELIVERED;
    }

    @Override
    protected boolean preferNoReplyAccount(Session session) throws OXException {
        LeanConfigurationService configurationService = serviceLookup.getOptionalService(LeanConfigurationService.class);
        if (configurationService == null) {
            return super.preferNoReplyAccount(session);
        }
        boolean preferNoReply = configurationService.getBooleanProperty(-1, session.getContextId(), PREFER_NO_REPLY_FOR_IMIP);
        return preferNoReply || super.preferNoReplyAccount(session);
    }

    private MimeMessage build(Session session, SchedulingMessage message) throws MessagingException, OXException, UnsupportedEncodingException {
        /*
         * Build the message
         */
        //@formatter:off
        MimeMessageBuilder builder = new MimeMessageBuilder()
            .setFrom(message.getOriginator())
            .setTo(message.getRecipient())
            .setSubject(getSubject(message))
            .setContent(new ExternalMimePartFactory(serviceLookup, message))
            .setAdditionalHeader(getAdditionalHeaders(message))
            .setPriority()
            .setMailerInfo(serviceLookup.getOptionalService(VersionService.class))
            .setTracing(message.getResource().getUid())
            .setOrganization(serviceLookup.getOptionalService(ContactService.class), session)
            .setSentDate(message.getRecipientSettings().getTimeZone())
            .setAutoGenerated();
            if (!preferNoReplyAccount(session)) {
                builder.setSender(getUsersMail(session));
            }
        //@formatter:on

        Boolean readReceipt = message.getAdditional(Constants.ADDITIONAL_HEADER_READ_RECEIPT, Boolean.class);
        if (null != readReceipt && readReceipt.booleanValue()) {
            builder.setReadReceiptHeader(message.getRecipient());
        }

        return builder.build();
    }

    private InternetAddress getUsersMail(Session session) throws OXException, AddressException, UnsupportedEncodingException {
        UserService userService = serviceLookup.getOptionalService(UserService.class);
        if (userService == null) {
            return null;
        }
        User user = userService.getUser(session.getUserId(), session.getContextId());
        if (Strings.isNotEmpty(user.getDisplayName())) {
            return new QuotedInternetAddress(user.getMail(), user.getDisplayName(), "UTF-8");
        }
        return new QuotedInternetAddress(user.getMail());
    }

    private boolean canHandle(SchedulingMessage message) {
        if (Utils.isInternalCalendarUser(message.getRecipient())) {
            return false;
        }

        /*
         * Search for an address to send to
         */
        return null != CalendarUtils.optEMailAddress(message.getRecipient().getUri());
    }

    private String getSubject(SchedulingMessage message) {
        Locale locale = message.getRecipientSettings().getLocale();
        StringHelper helper = StringHelper.valueOf(locale);
        Event event = Utils.selectDescribedEvent(message.getResource(), message.getScheduleChange().getChanges());
        String summary = event.getSummary();
        String subject;
        switch (message.getMethod()) {
            case ADD:
                subject = Messages.SUBJECT_CHANGED_APPOINTMENT;
                break;
            case CANCEL:
                subject = Messages.SUBJECT_CANCELLED_APPOINTMENT;
                break;
            case COUNTER:
                subject = Messages.SUBJECT_COUNTER_APPOINTMENT;
                break;
            case DECLINE_COUNTER:
                subject = Messages.SUBJECT_DECLINECOUNTER;
                break;
            case PUBLISH:
                subject = Messages.SUBJECT_NEW_APPOINTMENT;
                break;
            case REFRESH:
                subject = Messages.SUBJECT_REFRESH;
                break;
            case REPLY:
                return getPartStatSubject(message.getOriginator(), message.getScheduleChange().getOriginatorPartStat(), locale, summary);
            case REQUEST:
                switch (message.getScheduleChange().getAction()) {
                    case CREATE:
                        subject = Messages.SUBJECT_NEW_APPOINTMENT;
                        break;
                    default:
                        subject = Messages.SUBJECT_CHANGED_APPOINTMENT;
                        break;
                }
                break;
            default:
                subject = Messages.SUBJECT_NONE;
                break;
        }
        subject = String.format(helper.getString(subject), summary);
        return subject;
    }

}
