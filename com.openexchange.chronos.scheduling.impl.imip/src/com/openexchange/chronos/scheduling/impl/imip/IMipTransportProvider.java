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

package com.openexchange.chronos.scheduling.impl.imip;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.scheduling.ChangeNotification;
import com.openexchange.chronos.scheduling.ScheduleStatus;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.common.AbstractMailTransportProvider;
import com.openexchange.chronos.scheduling.common.Constants;
import com.openexchange.chronos.scheduling.common.ContextSensitiveMessages;
import com.openexchange.chronos.scheduling.common.MailUtils;
import com.openexchange.chronos.scheduling.common.Messages;
import com.openexchange.chronos.scheduling.common.MimeMessageBuilder;
import com.openexchange.chronos.scheduling.common.Utils;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
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
            if (false == shouldBeSent(message)) {
                return ScheduleStatus.DELIVERED;
            }

            return transportMail(session, build(session, message));
        } catch (OXException | MessagingException e) {
            LOGGER.debug("Unable to generate message", e);
        }
        return ScheduleStatus.NOT_DELIVERED;
    }

    private MimeMessage build(Session session, SchedulingMessage message) throws MessagingException, OXException {
        /*
         * Build the message
         */
        //@formatter:off
        MimeMessageBuilder builder = new MimeMessageBuilder()
            .setFrom(message.getOriginator())
            .setTo(message.getRecipient())
            .setSubject(getSubject(serviceLookup.getServiceSafe(UserService.class), session.getContextId(), message))
            .setContent(new ExternalMimePartFactory(message, serviceLookup))
            .setAdditionalHeader((Map<String, String>) message.getAdditional(Constants.ADDITIONAL_HEADER_MAIL_HEADERS, Map.class))
            .setPriority()
            .setMailerInfo(serviceLookup.getOptionalService(VersionService.class))
            .setTracing(message.getResource().getUid())
            .setOrganization(serviceLookup.getOptionalService(ContactService.class), session)
            .setSentDate(Utils.getTimeZone(serviceLookup.getServiceSafe(UserService.class), session.getContextId(), message.getOriginator(), message.getRecipient()))
            .setAutoGenerated();
        //@formatter:on

        Boolean readReceipt = message.getAdditional(Constants.ADDITIONAL_HEADER_READ_RECEIPT, Boolean.class);
        if (null != readReceipt && readReceipt.booleanValue()) {
            builder.setReadReceiptHeader(message.getRecipient());
        }

        return builder.build();
    }

    private boolean canHandle(SchedulingMessage message) {
        if (Utils.isInternalCalendarUser(message.getRecipient())) {
            return false;
        }

        /*
         * Search for an address to send to
         */
        if (Strings.isNotEmpty(message.getRecipient().getUri()) && message.getRecipient().getUri().toLowerCase().startsWith("mailto:")) {
            return true;
        }
        if (Strings.isNotEmpty(message.getRecipient().getEMail())) {
            return true;
        }

        /*
         * Don't send
         */
        return false;
    }

    /**
     * Gets a value indicating whether the message should be sent to the recipient as per
     * {@link SchedulingMessage#getRecipient()}, or not.
     *
     * @param message The {@link SchedulingMessage}
     * @return <code>true</code> if the scheduling message should be sent to the recipient,
     *         <code>false</code> if not. E.g. a message should <b>not</b> be sent to the recipient if it exclusively ends in the past
     *         or if no event fields of interest changed
     * @throws OXException If calculating recurrences for a series fails or the admin user lookup fails
     */
    private boolean shouldBeSent(SchedulingMessage message) throws OXException {
        List<Event> events = CalendarUtils.sortSeriesMasterFirst(message.getResource().getEvents());
        if (null == events || events.size() < 1) {
            // No event(s) to report
            return false;
        }

        if (endsInPast(events)) {
            // No mail for events in the past
            return false;
        }
        return true;
    }

    private String getSubject(UserService userService, int contextId, SchedulingMessage message) throws OXException {
        Locale locale = Utils.getLocale(userService, contextId, message.getOriginator(), message.getRecipient());
        StringHelper helper = StringHelper.valueOf(locale);
        String summary = MailUtils.getSummary(message.getResource().getEvents());
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
                subject = Messages.SUBJECT_STATE_CHANGED;
                //@formatter:off
                    return String.format(
                            helper.getString(subject), 
                            message.getOriginator().getCn(), 
                            ContextSensitiveMessages.getInstance().getDescription(getOriginatorPartStat(message.getOriginator(), message.getResource().getEvents()), locale, ContextSensitiveMessages.Context.VERB),
                            summary
                            ); 
                    //@formatter:on
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

    /**
     * Get the updated {@link ParticipationStatus} of the originator based on the given events by {@link #message}
     * 
     * @return The {@link ParticipationStatus} of the originator
     * @throws OXException In case participant status can't be found
     */
    private ParticipationStatus getOriginatorPartStat(CalendarUser originator, List<Event> events) throws OXException {
        if (Attendee.class.isAssignableFrom(originator.getClass())) {
            return ((Attendee) originator).getPartStat();
        }
        for (Event e : events) {
            Attendee attendee = CalendarUtils.find(e.getAttendees(), originator.getEntity());
            if (null != attendee) {
                return attendee.getPartStat();
            }
        }
        throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Unable to find correct participant status");
    }

}
