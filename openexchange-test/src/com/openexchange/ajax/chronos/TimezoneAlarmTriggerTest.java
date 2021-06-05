/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.chronos;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AlarmTrigger;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;

/**
 *
 * {@link TimezoneAlarmTriggerTest} tests alarm triggers for events with different start and end timezones.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class TimezoneAlarmTriggerTest extends AbstractUserTimezoneAlarmTriggerTest {

    /**
     * Initializes a new {@link TimezoneAlarmTriggerTest}.
     */
    public TimezoneAlarmTriggerTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        eventManager = new EventManager(defaultUserApi, folderId);
    }
    
    @Test
    public void testTrigger() throws ApiException, ParseException, ChronosApiException {
        int currentTriggers = getAlarmTriggers().size();
        Calendar start = DateTimeUtil.getUTCCalendar();
        start.add(Calendar.DAY_OF_MONTH, 1);
        DateTimeData startDate = DateTimeUtil.getDateTime(start);

        TimeZone endTimeZone = TimeZone.getTimeZone("Europe/Berlin");
        Calendar end = Calendar.getInstance(endTimeZone);
        end.setTimeInMillis(start.getTimeInMillis());
        end.add(Calendar.HOUR, 10);
        DateTimeData endDate = DateTimeUtil.getDateTime(end);

        /*
         * 1. Test alarm related to start
         */
        {
            EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser().intValue(), "testPositiveDurationTrigger", startDate, endDate, AlarmFactory.createDisplayAlarm("-PT10M"), folderId);
            EventData event = eventManager.createEvent(toCreate, true);
            getAndAssertAlarms(event, 1, folderId);

            // Get alarms within the next two days
            List<AlarmTrigger> triggers = getAndCheckAlarmTrigger(currentTriggers + 1); // one trigger
            AlarmTrigger alarmTrigger = findTrigger(event.getId(), triggers);
            checkAlarmTime(alarmTrigger, event.getId(), start.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));
            EventId eventId = new EventId();
            eventId.setFolder(event.getFolder());
            eventId.setId(event.getId());
            eventId.setRecurrenceId(event.getRecurrenceId());
            eventManager.deleteEvent(eventId);
        }
        /*
         * 2. Test alarm related to end
         */
        {
            EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser().intValue(), "testPositiveDurationTrigger", startDate, endDate, AlarmFactory.createAlarm("-PT10M", RelatedEnum.END), folderId);
            EventData event = eventManager.createEvent(toCreate, true);
            getAndAssertAlarms(event, 1, folderId);

            // Get alarms within the next two days
            List<AlarmTrigger> triggers = getAndCheckAlarmTrigger(currentTriggers + 1); // one trigger
            AlarmTrigger alarmTrigger = findTrigger(event.getId(), triggers);
            checkAlarmTime(alarmTrigger, event.getId(), end.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));
        }
    }

    @Test
    public void testTriggerWithPositiveDuration() throws ApiException, ParseException, ChronosApiException {
        int currentTriggers = getAlarmTriggers().size();
        Calendar start = DateTimeUtil.getUTCCalendar();
        start.add(Calendar.DAY_OF_MONTH, 1);
        DateTimeData startDate = DateTimeUtil.getDateTime(start);

        TimeZone endTimeZone = TimeZone.getTimeZone("Europe/Berlin");
        Calendar end = Calendar.getInstance(endTimeZone);
        end.setTimeInMillis(start.getTimeInMillis());
        end.add(Calendar.HOUR, 10);
        DateTimeData endDate = DateTimeUtil.getDateTime(end);

        /*
         * 1. Test alarm related to start
         */
        {
            EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser().intValue(), "testPositiveDurationTrigger", startDate, endDate, AlarmFactory.createDisplayAlarm("-PT10M"), folderId);
            EventData event = eventManager.createEvent(toCreate, true);
            getAndAssertAlarms(event, 1, folderId);

            // Get alarms within the next two days
            List<AlarmTrigger> triggers = getAndCheckAlarmTrigger(currentTriggers + 1); // one trigger
            AlarmTrigger alarmTrigger = findTrigger(event.getId(), triggers);
            checkAlarmTime(alarmTrigger, event.getId(), start.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));
            EventId eventId = new EventId();
            eventId.setFolder(event.getFolder());
            eventId.setId(event.getId());
            eventId.setRecurrenceId(event.getRecurrenceId());
            eventManager.deleteEvent(eventId);
        }
        /*
         * 2. Test alarm related to end
         */
        {
            EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser().intValue(), "testPositiveDurationTrigger", startDate, endDate, AlarmFactory.createAlarm("PT10M", RelatedEnum.END), folderId);
            EventData event = eventManager.createEvent(toCreate, true);
            getAndAssertAlarms(event, 1, folderId);

            // Get alarms within the next two days
            List<AlarmTrigger> triggers = getAndCheckAlarmTrigger(currentTriggers + 1); // one trigger
            AlarmTrigger alarmTrigger = findTrigger(event.getId(), triggers);
            checkAlarmTime(alarmTrigger, event.getId(), end.getTimeInMillis() + TimeUnit.MINUTES.toMillis(10));
        }
    }
}
