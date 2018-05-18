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

import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.AlarmTrigger;
import com.openexchange.testing.httpclient.models.AlarmTriggerData;
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
        EventData eventSeries = EventFactory.createSeriesEvent(defaultUserApi.getCalUser(), "testTimeShiftOfEventSeries", 4, folderId);
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
        eventManager.updateOccurenceEvent(event, recurrence);

        AlarmTriggerData triggerData = getAndCheckAlarmTrigger(2);

        Date exceptionTriggerTime = DateTimeUtil.parseDateTime(DateTimeUtil.incrementDateTimeData(oldTriggerTime, TimeUnit.HOURS.toMillis(1)));
        Date expectedTriggerTime = DateTimeUtil.parseDateTime(DateTimeUtil.incrementDateTimeData(oldTriggerTime, TimeUnit.DAYS.toMillis(1)));

        for(AlarmTrigger trigger : triggerData) {
            Date parseZuluDateTime = DateTimeUtil.parseZuluDateTime(trigger.getTime());
            assertTrue("The trigger time is wrong. Expected either " + exceptionTriggerTime + " or " + expectedTriggerTime + " but was " + parseZuluDateTime, parseZuluDateTime.getTime() == exceptionTriggerTime.getTime() || parseZuluDateTime.getTime() == expectedTriggerTime.getTime());
        }
    }
}
