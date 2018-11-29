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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.ICalFacotry;
import com.openexchange.ajax.chronos.factory.ICalFacotry.PartStat;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.ConversionDataSource;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailDestinationData;

/**
 * {@link ITipRequestTests}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public class ITipRequestTests extends AbstractITipTest {

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // Attendees will be overwritten by setup, so '0' is fine
            { "SingleTwoHourEvent", EventFactory.createSingleTwoHourEvent(0, "SingleTwoHourEvent") },
            { "SeriesFiveOccurences", EventFactory.createSeriesEvent(0, "SeriesEventFiveOccurences", 5, null) },
            { "MonthlySeriesFiveOccurences", EventFactory.createSeriesEvent(0, "SeriesEventFiveOccurences", 5, null, EventFactory.RecurringFrequency.MONTHLY) }
        });
    }

    public Map<PartStat, Function<ConversionDataSource, ActionResponse>> map = new HashMap<>(3);
    {
        map.put(PartStat.ACCEPTED, (ConversionDataSource body) -> {
            try {
                return accept(body);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        });
        map.put(PartStat.TENTATIVE, (ConversionDataSource body) -> {
            try {
                return tentative(body);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        });
        map.put(PartStat.DECLINED, (ConversionDataSource body) -> {
            try {
                return decline(body);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private MailDestinationData mailData;

    private EventData updatedEvent;

    private EventData event;

    private Attendee organizer;

    /**
     * Initializes a new {@link ITipRequestTests}.
     * 
     * @param identifier The test identifier
     * @param event The event to to actions on
     * 
     */
    public ITipRequestTests(String identifier, EventData event) {
        super();
        this.event = event;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        List<Attendee> attendees = new LinkedList<>();

        attendees.add(createAttendee(testUser, getApiClient()));
        organizer = createAttendee(testUserC2, apiClientC2);
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
    }

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
    public void testAllRequest() throws Exception {
        for (Entry<PartStat, Function<ConversionDataSource, ActionResponse>> entry : map.entrySet()) {
            validateEvent(entry.getValue().apply(constructBody(mailData.getId())), entry.getKey());
        }
    }

    @Test
    public void testChangeOnAcknowlegedRequest() throws Exception {
        int i = Math.random() <= 0.5 ? 1 : 2;
        Iterator<Entry<PartStat, Function<ConversionDataSource, ActionResponse>>> iterator = map.entrySet().iterator();
        Entry<PartStat, Function<ConversionDataSource, ActionResponse>> entry = iterator.next();
        validateEvent(entry.getValue().apply(constructBody(mailData.getId())), entry.getKey());
        for (int j = 0; j < i; j++) {
            entry = iterator.next();
        }
        validateEvent(entry.getValue().apply(constructBody(mailData.getId())), entry.getKey());
    }

    private void validateEvent(ActionResponse response, PartStat partStat) {
        Assert.assertThat("Only one object should have been returned", Integer.valueOf(response.getData().size()), is(Integer.valueOf(1)));
        updatedEvent = response.getData().get(0);
        Assert.assertThat("Should be the same start date", updatedEvent.getStartDate(), is(event.getStartDate()));
        Assert.assertThat("Should be the same end date", updatedEvent.getEndDate(), is(event.getEndDate()));

        Assert.assertThat("Should contain attendees", updatedEvent.getAttendees(), notNullValue());
        Assert.assertThat("Should be same attendees", Integer.valueOf(updatedEvent.getAttendees().size()), is(Integer.valueOf(2)));
        Assert.assertThat("Should be the same organizer", updatedEvent.getOrganizer().getEmail(), is(organizer.getEmail()));

        // Get acting user
        Attendee attendee = updatedEvent.getAttendees().stream().filter(a -> null != a.getEmail() && false == a.getEmail().equals(organizer.getEmail())).findAny().get();
        Assert.assertThat("Participants status didn't change!!", attendee.getPartStat().toUpperCase(), is(partStat.name().toUpperCase()));
    }

}
