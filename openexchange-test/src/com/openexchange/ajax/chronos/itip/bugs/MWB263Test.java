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

package com.openexchange.ajax.chronos.itip.bugs;

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertChanges;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.itip.AbstractITipAnalyzeTest;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailData;

/**
 * {@link MWB263Test}
 * 
 * 1. Create a series event as user A in context 1.
 * 2. Create a change exception as organizer by moving a specific event
 * 3. Receive mails as attendee B in context 2.
 * 4. Accept series
 * 5. Decline exception
 * 
 * 6. Exception got duplicated
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class MWB263Test extends AbstractITipAnalyzeTest {

    private String summary;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.summary = this.getClass().getSimpleName() + UUID.randomUUID().toString();
    }

    @Test
    public void testBug() throws Exception {
        EventData eventToCreate = EventFactory.createSeriesEvent(getUserId(), summary, 10, folderId);
        Attendee replyingAttendee = prepareCommonAttendees(eventToCreate);

        createdEvent = eventManager.createEvent(eventToCreate, true);

        /*
         * Immediately create exception by updating end date
         */
        List<EventData> events = getAllEventsOfEvent(eventManager, createdEvent);
        EventData secondOccurrence = events.get(2);
        EventData deltaEvent = prepareDeltaEvent(secondOccurrence);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(DateTimeUtil.parseZuluDateTime(secondOccurrence.getEndDate()).getTime() + TimeUnit.HOURS.toMillis(4));
        deltaEvent.setEndDate(DateTimeUtil.getDateTime(date));
        eventManager.updateEvent(deltaEvent);

        /*
         * Receive series mail as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST);
        rememberMail(apiClientC2, iMip);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClientC2, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.getStatus());

        /*
         * Reply with "accepted"
         */
        EventData attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip), null), createdEvent.getUid());
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());
        rememberForCleanup(apiClientC2, attendeeEvent);

        /*
         * Receive exception mail as attendee, status for series still NEEDS_ACTION
         */
        MailData iMipException = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        rememberMail(apiClientC2, iMipException);
        AnalysisChangeNewEvent newException = assertChanges(analyze(apiClientC2, iMipException), 2, 1).getNewEvent();
        assertNotNull(newException);
        assertEquals(createdEvent.getUid(), newException.getUid());
        assertAttendeePartStat(newException.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.getStatus());

        /*
         * Reply with "decline", look up if the exception is created for the series
         */
        EventData attendeeException = assertSingleEvent(accept(apiClientC2, constructBody(iMipException), null), createdEvent.getUid());
        assertAttendeePartStat(attendeeException.getAttendees(), replyingAttendee.getEmail(), PartStat.DECLINED.getStatus());
        assertThat(attendeeException.getSeriesId(), is(attendeeEvent.getId()));

        /*
         * Ensure that there are no duplicates
         */
        List<EventData> allEvents = eventManagerC2.getAllEvents(DateTimeUtil.parseZuluDateTime(eventToCreate.getStartDate()), DateTimeUtil.parseZuluDateTime(deltaEvent.getEndDate()));
        assertThat(I(allEvents.size()), is(I(2)));
    }

}
