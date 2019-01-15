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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.scheduling.common;

import static com.openexchange.chronos.scheduling.common.MailUtils.saveChangesSafe;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.ScheduleStatus;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.TransportProvider;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractMailTransportProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public abstract class AbstractMailTransportProvider implements TransportProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMailTransportProvider.class);

    protected final @NonNull ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link AbstractMailTransportProvider}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     */
    public AbstractMailTransportProvider(@NonNull ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public int getRanking() {
        return 100;
    }

    @Override
    @NonNull
    public ScheduleStatus send(@NonNull Session session, @NonNull SchedulingMessage message) {
        try {
            return sendMail(session, message);
        } catch (OXException | MessagingException e) {
            LOGGER.error("Unable to send message from {} to {}", message.getOriginator().getEMail(), message.getRecipient().getEMail(), e);
        }
        return ScheduleStatus.NOT_DELIVERED;
    }

    /**
     * 
     * Send the actual mail
     *
     * @param message The {@link SchedulingMessage}
     * @param session The {@link Session}
     * @return The {@link ScheduleStatus} of the message after sending
     * @throws OXException In case of error
     * @throws MessagingException In case mail can't be sent
     */
    public abstract @NonNull ScheduleStatus sendMail(Session session, SchedulingMessage message) throws OXException, MessagingException;

    protected @NonNull ScheduleStatus transportMail(Session session, MimeMessage mime) throws OXException {
        saveChangesSafe(serviceLookup.getOptionalService(HostnameService.class), mime, session.getContextId(), session.getUserId());

        final MailTransport transport = MailTransport.getInstance(session);
        try {
            transport.sendMailMessage(new ContentAwareComposedMailMessage(mime, session, null), ComposeType.NEW);
        } finally {
            transport.close();
        }

        return ScheduleStatus.SENT;
    }

    protected boolean endsInPast(List<Event> events) throws OXException {
        // FIXME Do check for the changed event not the first in the list ..
        return endsInPast(CalendarUtils.sortSeriesMasterFirst(events).get(0));
    }

    private boolean endsInPast(Event event) throws OXException {
        Date now = new Date();
        Date endDate = new Date(event.getEndDate().getTimestamp());

        // In case of series master the date of the last occurrence has to be validated
        if (CalendarUtils.isSeriesMaster(event)) {
            try {
                RecurrenceRule eventRule = new RecurrenceRule(event.getRecurrenceRule());
                Date eventEnd = null;

                RecurrenceService rService = serviceLookup.getServiceSafe(RecurrenceService.class);
                if (eventRule.getUntil() != null) {
                    eventEnd = new Date(eventRule.getUntil().getTimestamp());
                } else if (eventRule.getCount() != null) {
                    Iterator<Event> instances = rService.calculateInstances(event, null, null, null);
                    Event last = null;
                    while (instances.hasNext()) {
                        last = instances.next();
                    }
                    if (null != last) {
                        eventEnd = new Date(last.getEndDate().getTimestamp());
                    }
                } else {
                    // Recurrence rule has no 'limit' defined. 
                    return false;
                }
                if (eventEnd != null) {
                    return eventEnd.before(now);
                }
            } catch (InvalidRecurrenceRuleException e) {
                LOGGER.debug("Invalid recurrence rule. Fallback to notify", e);
            }
        }

        return endDate.before(now);
    }

}
