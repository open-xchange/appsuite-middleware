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

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.convertToAttendee;
import static com.openexchange.ajax.chronos.itip.ITipUtil.parseICalAttachment;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailData;

/**
 * {@link ITipRequestTests}
 * 
 * Creates an event as user B from context 2, sending a invite to user A from context 1.
 * User A will accept, tentative or decline via iTIP.
 * Afterwards the reply message within the organizers inbox will be checked
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public class ITipRequestTests extends AbstractITipTest {

    //@formatter:off
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // Attendees will be overwritten by setup, so '0' is fine
            { "SingleTwoHourEvent", EventFactory.createSingleTwoHourEvent(0, null) },
            { "SeriesEventFiveOccurences", EventFactory.createSeriesEvent(0, null, 5, null) },
            { "MonthlySeriesEvent", EventFactory.createSeriesEvent(0, null, 5, null, EventFactory.RecurringFrequency.MONTHLY) }
        });
    }
    //@formatter:on

    private final String summary;

    private final EventData event;

    /**
     * Initializes a new {@link ITipRequestTests}.
     * 
     * @param identifier The test identifier
     * @param event The event to to actions on
     * 
     */
    public ITipRequestTests(String identifier, EventData event) {
        super();
        this.summary = identifier;
        this.event = event;
    }

    private MailData mailData;

    /** Organizer from context 2, user B */
    private Attendee organizer;

    /** Attendee from context 1, user A */
    private Attendee attendee;

    /** The event managed by the organizer */
    private EventData organizerInstance;

    private String uniqueSummary;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        List<Attendee> attendees = new LinkedList<>();

        attendee = convertToAttendee(testUser, Integer.valueOf(0));
        attendee.uri("mailto:" + userResponseC1.getData().getEmail1());
        attendee.email(userResponseC1.getData().getEmail1());
        attendees.add(attendee);
        organizer = convertToAttendee(testUserC2, apiClientC2.getUserId());
        organizer.uri("mailto:" + userResponseC2.getData().getEmail1());
        organizer.email(userResponseC2.getData().getEmail1());
        organizer.setPartStat(PartStat.ACCEPTED.toString());
        attendees.add(organizer);

        event.setAttendees(attendees);
        CalendarUser c = new CalendarUser();
        c.uri("mailto:" + userResponseC2.getData().getEmail1());
        c.cn(userResponseC2.getData().getDisplayName());
        c.email(userResponseC2.getData().getEmail1());
        c.entity(Integer.valueOf(userResponseC2.getData().getId()));
        event.setOrganizer(c);
        event.setCalendarUser(c);

        uniqueSummary = summary + UUID.randomUUID().toString();
        event.setSummary(uniqueSummary);

        organizerInstance = createEvent(apiClientC2, event, folderIdC2);
        rememberForCleanup(apiClientC2, organizerInstance);

        mailData = receiveIMip(apiClient, organizer.getEmail(), uniqueSummary, 0, SchedulingMethod.REQUEST);
        rememberMail(mailData);
    }

    @Test
    public void testAccept() throws Exception {
        validate(accept(constructBody(mailData)), ParticipationStatus.ACCEPTED);
    }

    @Test
    public void testTenative() throws Exception {
        validate(tentative(constructBody(mailData)), ParticipationStatus.TENTATIVE);
    }

    @Test
    public void testDecline() throws Exception {
        validate(decline(constructBody(mailData)), ParticipationStatus.DECLINED);
    }

    private void validate(ActionResponse response, ParticipationStatus partStat) throws Exception {
        /*
         * Validate event from attendee perspective
         */
        EventData updatedEvent = assertSingleEvent(response);
        rememberForCleanup(updatedEvent);
        assertThat("Should be the same start date", updatedEvent.getStartDate(), is(event.getStartDate()));
        assertThat("Should be the same end date", updatedEvent.getEndDate(), is(event.getEndDate()));

        assertThat("Should contain attendees", updatedEvent.getAttendees(), notNullValue());
        assertThat("Should be same attendees", Integer.valueOf(updatedEvent.getAttendees().size()), is(Integer.valueOf(2)));
        assertThat("Should be the same organizer", updatedEvent.getOrganizer().getEmail(), is(organizer.getEmail()));

        assertAttendeePartStat(updatedEvent.getAttendees(), attendee.getEmail(), partStat.getValue());

        /*
         * Validate event from organizer perspective
         */
        MailData replyMail = receiveIMip(apiClientC2, attendee.getEmail(), uniqueSummary, 0, SchedulingMethod.REPLY);
        assertNotNull(replyMail);
        rememberMail(apiClientC2, replyMail);

        ImportedCalendar iTipReply = parseICalAttachment(apiClientC2, replyMail);
        assertEquals(SchedulingMethod.REPLY.name(), iTipReply.getMethod());
        assertThat("Only one object should have been handled", Integer.valueOf(iTipReply.getEvents().size()), is(Integer.valueOf(1)));
        Event replyEvent = iTipReply.getEvents().get(0);
        assertAttendeePartStat(replyEvent.getAttendees(), attendee.getEmail(), partStat);
    }

}
