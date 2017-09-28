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

package com.openexchange.ajax.chronos;

import static org.junit.Assert.assertEquals;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.AlarmTrigger;
import com.openexchange.testing.httpclient.models.AlarmTriggerData;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.Trigger;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;
import com.openexchange.testing.httpclient.modules.ChronosApi;

/**
 * {@link AbstractAlarmTriggerTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public abstract class AbstractAlarmTriggerTest extends AbstractAlarmTest {

    protected String folderId;
    protected String folderId2;
    protected UserApi user2;
    protected EventManager eventManager2;

    /**
     * Initializes a new {@link AbstractAlarmTriggerTest}.
     */
    public AbstractAlarmTriggerTest() {
        super();
    }

    protected EventData createSingleEventWithSingleAlarm(String summary, DateTimeData startDate, String duration, RelatedEnum related) throws ParseException {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(defaultUserApi.getCalUser());
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(startDate);

        singleEvent.setEndDate(DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1)));
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setAllDay(false);
        singleEvent.setAlarms(Collections.singletonList(AlarmFactory.createAlarm(duration, related)));
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    private EventData createSeriesEventWithSingleAlarm(String summary, Calendar startDate, String duration, List<Attendee> attendees) {
        EventData seriesEvent = new EventData();
        seriesEvent.setPropertyClass("PUBLIC");
        if (attendees == null) {
            Attendee attendee = new Attendee();
            attendee.entity(defaultUserApi.getCalUser());
            attendee.cuType(CuTypeEnum.INDIVIDUAL);
            attendee.setUri("mailto:" + this.testUser.getLogin());
            seriesEvent.setAttendees(Collections.singletonList(attendee));
        } else {
            seriesEvent.setAttendees(attendees);
        }
        seriesEvent.setStartDate(DateTimeUtil.getDateTime(startDate));
        Calendar endDate = Calendar.getInstance(startDate.getTimeZone());
        endDate.setTimeInMillis(startDate.getTimeInMillis());
        endDate.add(Calendar.HOUR, 1);
        seriesEvent.setEndDate(DateTimeUtil.getDateTime(endDate));
        seriesEvent.setTransp(TranspEnum.OPAQUE);
        seriesEvent.setRrule("FREQ=DAILY;COUNT=4");
        seriesEvent.setAllDay(false);

        Alarm alarm = new Alarm();
        alarm.setAction("display");
        Trigger trigger = new Trigger();
        trigger.setRelated(RelatedEnum.START);
        trigger.setDuration(duration);
        alarm.setTrigger(trigger);
        alarm.setDescription("This is the display message!");
        seriesEvent.setAlarms(Collections.singletonList(alarm));
        seriesEvent.setSummary(summary);
        return seriesEvent;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = createAndRememberNewFolder(defaultUserApi, defaultUserApi.getSession(), getDefaultFolder(), defaultUserApi.getCalUser().intValue());
        ApiClient client = generateClient(testUser2);
        rememberClient(client);
        user2 = new UserApi(client, testUser2);
        folderId2 = getDefaultFolder(user2.getSession(), client);
        eventManager2 = new EventManager(user2, getDefaultFolder(user2.getSession(), client));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Retrieves alarm triggers with a trigger time lower than the given limit and checks if the response contains the correct amount of alarm trigger objects.
     * Its also possible to filter for specific actions.
     *
     * @param until The upper limit
     * @param actions The actions to retrieve
     * @param expected The expected amount of alarm objects
     * @return The {@link AlarmTriggerData}
     * @throws ApiException
     */
    AlarmTriggerData getAndCheckAlarmTrigger(long until, String actions, int expected) throws ApiException {
        AlarmTriggerData triggers = eventManager.getAlarmTrigger(until, actions);
        assertEquals(expected, triggers.size());
        return triggers;
    }

    /**
     * Retrieves alarm triggers from the given time until two days and checks if the response contains the correct amount of alarm trigger objects
     *
     * @param until The upper limit of the request
     * @param expected The amount of expected alarm trigger objects
     * @param api The api client to use
     * @param session The session of the user
     * @return The {@link AlarmTriggerData}
     * @throws ApiException
     */
    AlarmTriggerData getAndCheckAlarmTrigger(long until, int expected, ChronosApi api, String session) throws ApiException {
        AlarmTriggerData triggers = eventManager.getAlarmTrigger(until);
        assertEquals(expected, triggers.size());
        return triggers;
    }

    /**
     * Retrieves alarm triggers until two days and checks if the response contains the correct amount of alarm trigger objects
     *
     * @param from The lower limit of the request
     * @param expected The amount of expected alarm trigger objects
     * @return The {@link AlarmTriggerData}
     * @throws ApiException
     */
    AlarmTriggerData getAndCheckAlarmTrigger(int expected) throws ApiException {
        return getAndCheckAlarmTrigger(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2), expected, defaultUserApi.getChronosApi(), defaultUserApi.getSession());
    }

    /**
     * Creates a single event with the given start time and name.
     * Also handles all necessary tests and remembers the event for later deletion.
     *
     * @param name The name of the event
     * @param startTime The startTime of the event
     * @return The created event
     * @throws ApiException
     * @throws ParseException
     */
    protected EventData createSingleEvent(String name, long startTime) throws ApiException, ParseException {
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(defaultUserApi.getSession(), folderId, createSingleEventWithSingleAlarm(name, DateTimeUtil.getDateTime(startTime), "-PT15M", null), false, false);
        return handleCreation(createEvent);
    }

    /**
     * Creates a single event with the given start time and name.
     * Also handles all necessary tests and remembers the event for later deletion.
     *
     * @param name The name of the event
     * @param startTime The startTime of the event
     * @return The created event
     * @throws ApiException
     * @throws ParseException
     */
    protected EventData createSingleEvent(String name, Calendar startTime) throws ApiException, ParseException {
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(defaultUserApi.getSession(), folderId, createSingleEventWithSingleAlarm(name, DateTimeUtil.getDateTime(startTime), "-PT15M", null), false, false);
        return handleCreation(createEvent);
    }

    /**
     * Creates a event series with the given start time and name.
     * Also handles all necessary tests and remembers the event for later deletion.
     *
     * @param name The name of the event
     * @param startTime The startTime of the event
     * @return The created event
     * @throws ApiException
     */
    protected EventData createSeriesEvent(String name, Calendar startTime) throws ApiException {
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(defaultUserApi.getSession(), folderId, createSeriesEventWithSingleAlarm(name, startTime, "-PT15M", null), false, false);
        return handleCreation(createEvent);
    }

    /**
     * Creates a event series with the given start time and name.
     * Also handles all necessary tests and remembers the event for later deletion.
     *
     * @param name The name of the event
     * @param startTime The startTime of the event
     * @return The created event
     * @throws ApiException
     */
    protected EventData createSeriesEvent(String name, Calendar startTime, List<Attendee> attendees) throws ApiException {
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(defaultUserApi.getSession(), folderId, createSeriesEventWithSingleAlarm(name, startTime, "-PT15M", attendees), false, false);
        return handleCreation(createEvent);
    }

    /**
     * Checks if the trigger is related to the given event and if the trigger time is correct
     *
     * @param trigger The trigger
     * @param eventId The event id
     * @param expectedTime The expected trigger time
     * @throws ParseException
     */
    protected void checkAlarmTime(AlarmTrigger trigger, String eventId, long expectedTime) throws ParseException {
        assertEquals(eventId, trigger.getEventId());
        Date parsedTime = DateTimeUtil.parseZuluDateTime(trigger.getTime());
        assertEquals(expectedTime, parsedTime.getTime());
    }

}
