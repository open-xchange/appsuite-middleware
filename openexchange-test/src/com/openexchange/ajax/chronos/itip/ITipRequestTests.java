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

package com.openexchange.ajax.chronos.itip;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.ICalFacotry;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailDestinationData;

/**
 * {@link ITipRequestTests}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ITipRequestTests extends AbstractITipTest {

    private MailDestinationData mailData;

    private EventData updatedEvent;

    @Override
    public void tearDown() throws Exception {
        try {
            if (null != mailData) {
                removeMail(mailData);
            }
            if (null != updatedEvent) {
                deleteEvent(updatedEvent);
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testAcceptRequest() throws Exception {
        EventData event = EventFactory.createSingleTwoHourEvent(5, "summary");
        List<Attendee> attendees = new LinkedList<>();

        attendees.add(createAttendee(testUser, getApiClient()));
        Attendee organizer = createAttendee(testUserC2, apiClientC2);
        organizer.setPartStat(ICalFacotry.PartStat.ACCEPTED.toString());
        attendees.add(organizer);

        event.setAttendees(attendees);
        CalendarUser c = new CalendarUser();
        c.cn(userResponseC2.getData().getDisplayName());
        c.email(userResponseC2.getData().getEmail1());
        c.entity(Integer.valueOf(userResponseC2.getData().getId()));
        event.setOrganizer(c);

        mailData = createMailInInbox(Collections.singletonList(event));
        Assert.assertThat("No mail created.", mailData.getId(), notNullValue());

        ActionResponse accept = accept(constructBody(mailData.getId()));
        updatedEvent = accept.getData().get(0);
        Assert.assertThat("Should be the same start date", updatedEvent.getStartDate(), is(event.getStartDate()));
    }

}
