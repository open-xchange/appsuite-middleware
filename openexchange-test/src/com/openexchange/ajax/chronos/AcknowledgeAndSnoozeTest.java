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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
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
 * {@link AcknowledgeAndSnoozeTest} tests acknowledge and snooze actions.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class AcknowledgeAndSnoozeTest extends AbstractAlarmTriggerTest {

    private int previousTriggers;


    /**
     * Initializes a new {@link AcknowledgeAndSnoozeTest}.
     */
    public AcknowledgeAndSnoozeTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        previousTriggers = eventManager.getAlarmTrigger(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)).size();
    }

    /**
     * Tests a simple acknowledgement of an alarm
     */
    @Test
    public void testSimpleAcknowledge() throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        DateTimeData startDate = DateTimeUtil.getDateTime("UTC", cal.getTimeInMillis() + TimeUnit.DAYS.toMillis(1));
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, (cal.getTimeInMillis() + TimeUnit.HOURS.toMillis(2)));

        // Create an event with alarm
        EventData toCreate = EventFactory.createSingleEvent(defaultUserApi.getCalUser(), "testSimpleAcknowledge", startDate, endDate, folderId);
        toCreate.setAlarms(Collections.singletonList(AlarmFactory.createDisplayAlarm("-PT15M")));

        EventData expectedEventData = eventManager.createEvent(toCreate);
        getAndAssertAlarms(expectedEventData, 1, folderId);

        AlarmTriggerData triggerData = getAndCheckAlarmTrigger(1 + previousTriggers);
        AlarmTrigger alarmTrigger = triggerData.get(0);

        EventData getEvent = getAndAssertAlarms(expectedEventData, 1, folderId);
        assertNull(getEvent.getAlarms().get(0).getAcknowledged());

        EventData updated = eventManager.acknowledgeAlarm(expectedEventData.getId(), Integer.valueOf(alarmTrigger.getAlarmId()), folderId);
        Long acknowledged = updated.getAlarms().get(0).getAcknowledged();
        assertNotNull(acknowledged);

        getEvent = getAndAssertAlarms(updated, 1, folderId);
        assertNotNull(getEvent.getAlarms().get(0).getAcknowledged());
        assertEquals(acknowledged, getEvent.getAlarms().get(0).getAcknowledged());
        assertTrue("Acknowdledge time (" + acknowledged.longValue() + ") is not later than the creation date (" + expectedEventData.getCreated().longValue() + ").", acknowledged.longValue() >= expectedEventData.getCreated().longValue());
    }

    /**
     * Tests the snooze function of the alarm
     */
    @Test
    public void testSimpleSnooze() throws Exception {
        DateTimeData startDate = DateTimeUtil.getDateTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)));

        // Create an event with alarm
        EventData toCreate = EventFactory.createSingleEvent(defaultUserApi.getCalUser(), "testSimpleSnooze", startDate, endDate, folderId);
        toCreate.setAlarms(Collections.singletonList(AlarmFactory.createDisplayAlarm("-PT15M")));

        EventData expectedEventData = eventManager.createEvent(toCreate);
        getAndAssertAlarms(expectedEventData, 1, folderId);

        AlarmTriggerData triggerData = getAndCheckAlarmTrigger(1 + previousTriggers);
        AlarmTrigger alarmTrigger = triggerData.get(0);

        EventData getEvent = getAndAssertAlarms(expectedEventData, 1, folderId);
        assertNull(getEvent.getAlarms().get(0).getAcknowledged());
        int alarmId = Integer.valueOf(alarmTrigger.getAlarmId());

        // Snooze the alarm by 5 minutes
        long expectedTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        eventManager.snoozeAlarm(expectedEventData.getId(), alarmId, TimeUnit.MINUTES.toMillis(5), folderId);

        triggerData = getAndCheckAlarmTrigger(2 + previousTriggers);
        AlarmTrigger alarmTrigger2 = triggerData.get(0);
        assertNotEquals(alarmTrigger2.getAlarmId(), alarmTrigger.getAlarmId());
        Date parsedTime = DateTimeUtil.parseZuluDateTime(alarmTrigger2.getTime());
        checkAlarmTimeRoughly(expectedTime, parsedTime.getTime());
        String snoozeAlarmId = alarmTrigger2.getAlarmId();

        // Snooze the alarm again by 10 minutes
        expectedTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);
        eventManager.snoozeAlarm(expectedEventData.getId(), Integer.valueOf(snoozeAlarmId), TimeUnit.MINUTES.toMillis(10), folderId);

        triggerData = getAndCheckAlarmTrigger(2 + previousTriggers);
        alarmTrigger2 = triggerData.get(0);
        assertNotEquals(alarmTrigger2.getAlarmId(), alarmTrigger.getAlarmId());
        assertNotEquals(alarmTrigger2.getAlarmId(), snoozeAlarmId);
        parsedTime = DateTimeUtil.parseZuluDateTime(alarmTrigger2.getTime());
        checkAlarmTimeRoughly(expectedTime, parsedTime.getTime());

    }


    /**
     * Checks the alarm time
     * @param expectedTime The expected time
     * @param value The actual time
     */
    private void checkAlarmTimeRoughly(long expectedTime, long value) {
        Calendar exp = Calendar.getInstance();
        exp.setTimeInMillis(expectedTime);
        exp.set(Calendar.SECOND, 0);
        exp.set(Calendar.MILLISECOND, 0);

        Calendar valCal = Calendar.getInstance();
        valCal.setTimeInMillis(value);
        valCal.set(Calendar.SECOND, 0);
        valCal.set(Calendar.MILLISECOND, 0);

        assertEquals(exp.getTimeInMillis(), valCal.getTimeInMillis());
    }

}
