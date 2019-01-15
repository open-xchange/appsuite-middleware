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
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.ScheduleStatus;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.common.AbstractMailTransportProvider;
import com.openexchange.chronos.scheduling.common.MailUtils;
import com.openexchange.chronos.scheduling.common.MimeMessageBuilder;
import com.openexchange.chronos.scheduling.common.Utils;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
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
    public @NonNull ScheduleStatus sendMail(Session session, SchedulingMessage message) throws OXException, MessagingException {
        int contextId = session.getContextId();
        if (false == canHandle(message)) {
            return ScheduleStatus.REJECTED;
        }

        if (false == shouldBeSent(message)) {
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
            .setContent(new ExternalMimePartFactory(message, serviceLookup))
            .setAdditionalHeader()
            .setPriority()
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
        List<Event> events = CalendarUtils.sortSeriesMasterFirst(message.getResource().getCalendarObject());
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
}
