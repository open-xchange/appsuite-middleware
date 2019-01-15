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

package com.openexchange.chronos.impl.scheduling;

import java.util.List;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.mapping.DefaultEventUpdate;
import com.openexchange.chronos.scheduling.MessageBuilder;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ReplyMessageBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class ReplyMessageBuilder extends AbstractMessageBuilder {

    /**
     * Initializes a new {@link ReplyMessageBuilder}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     * @param session The {@link CalendarSession}
     * @param calendarUser The {@link CalendarUser}
     * @throws OXException In case originator can't be found
     */
    public ReplyMessageBuilder(ServiceLookup serviceLookup, CalendarSession session, CalendarUser calendarUser) throws OXException {
        super(serviceLookup, session, calendarUser);
    }

    /**
     * 
     * Builds an reply message for the organizer
     *
     * @param original The original event
     * @param updated The updated event
     * @return A {@link List} of messages
     * @throws OXException
     */
    public List<SchedulingMessage> build(Event original, Event updated) throws OXException {
        //@formatter:off
        messages.add(new MessageBuilder()
            .setMethod(SchedulingMethod.REPLY)
            .setOriginator(originator)
            .setRecipient(updated.getOrganizer())
            .setResource(new AttachmentSuppressingResource(updated))
            .setDescription(descriptionService.describeReply(
                session.getContextId(), 
                originator, 
                updated.getOrganizer(),
                getCommentForRecipient(),
                DefaultEventUpdate.builder()
                    .considerUnset(false)
                    .originalEvent(original)
                    .updatedEvent(updated)
                    .build()))
            .setAdditionals(getAdditionalsFromSession())
            .build());
        //@formatter:on
        return messages;
    }
}
