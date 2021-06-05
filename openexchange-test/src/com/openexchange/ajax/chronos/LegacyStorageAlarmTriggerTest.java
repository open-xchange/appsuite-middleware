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

import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.AlarmTrigger;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link LegacyStorageAlarmTriggerTest} tests the legacy storage layer
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class LegacyStorageAlarmTriggerTest extends AbstractAlarmTriggerTest {

    /**
     * Initializes a new {@link LegacyStorageAlarmTriggerTest}.
     */
    public LegacyStorageAlarmTriggerTest() {
        super();
    }

    /**
     * Tests a simple acknowledgement of an alarm
     */
    @Test
    public void testTimeShiftOfEventSeries() throws Exception {
        // Create an event series with a alarm
        EventData eventSeries = EventFactory.createSeriesEvent(getCalendaruser(), "testTimeShiftOfEventSeries", 4, folderId);
        eventSeries.setStartDate(DateTimeUtil.incrementDateTimeData(eventSeries.getStartDate(), TimeUnit.DAYS.toMillis(1)));
        eventSeries.setEndDate(DateTimeUtil.incrementDateTimeData(eventSeries.getEndDate(), TimeUnit.DAYS.toMillis(1)));
        eventSeries.setAlarms(Collections.singletonList(AlarmFactory.createDisplayAlarm("-PT60M")));

        EventData expectedEventData = eventManager.createEvent(eventSeries);
        getAndAssertAlarms(expectedEventData, 1, folderId);

        AlarmTrigger firstTrigger = getAndCheckAlarmTrigger(1).get(0);

        EventData event = getAndAssertAlarms(expectedEventData, 1, folderId);

        // Create an exception by moving the next occurence to a different time slot
        String recurrence = firstTrigger.getRecurrenceId();
        DateTimeData oldTriggerTime = DateTimeUtil.incrementDateTimeData(eventSeries.getStartDate(), -TimeUnit.MINUTES.toMillis(60));
        // add shift by one hour
        event.setStartDate(DateTimeUtil.incrementDateTimeData(eventSeries.getStartDate(), TimeUnit.HOURS.toMillis(1)));
        event.setEndDate(DateTimeUtil.incrementDateTimeData(eventSeries.getEndDate(), TimeUnit.HOURS.toMillis(1)));
        event.setRecurrenceId(recurrence);
        eventManager.updateOccurenceEvent(event, recurrence, true);

        List<AlarmTrigger> triggerData = getAndCheckAlarmTrigger(2);

        Date exceptionTriggerTime = DateTimeUtil.parseDateTime(DateTimeUtil.incrementDateTimeData(oldTriggerTime, TimeUnit.HOURS.toMillis(1)));
        Date expectedTriggerTime = DateTimeUtil.parseDateTime(DateTimeUtil.incrementDateTimeData(oldTriggerTime, TimeUnit.DAYS.toMillis(1)));

        for(AlarmTrigger trigger : triggerData) {
            Date parseZuluDateTime = DateTimeUtil.parseZuluDateTime(trigger.getTime());
            assertTrue("The trigger time is wrong. Expected either " + exceptionTriggerTime + " or " + expectedTriggerTime + " but was " + parseZuluDateTime, parseZuluDateTime.getTime() == exceptionTriggerTime.getTime() || parseZuluDateTime.getTime() == expectedTriggerTime.getTime());
        }
    }
}
