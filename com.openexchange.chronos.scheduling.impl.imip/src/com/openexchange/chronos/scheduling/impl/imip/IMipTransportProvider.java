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

import java.util.Locale;
import javax.mail.MessagingException;
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
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
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
            .setSubject(getSubject(message))
            .setContent(new ExternalMimePartFactory(serviceLookup, message))
            .setAdditionalHeader(getAdditionalHeaders(message))
            .setPriority()
            .setMailerInfo(serviceLookup.getOptionalService(VersionService.class))
            .setTracing(message.getResource().getUid())
            .setOrganization(serviceLookup.getOptionalService(ContactService.class), session)
            .setSentDate(message.getRecipientSettings().getTimeZone())
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

    @Override
    protected boolean preferNoReplyAccount(Session session) {
        return false;
    }
}
