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
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.AlarmTrigger;
import com.openexchange.testing.httpclient.models.AlarmTriggerData;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
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

    @Test
    public void testSimpleAcknowledge() throws Exception {
        // Create an event with alarm
        EventData event = createSingleEvent("testSimpleAcknowledge", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        getAndCheckEvent(event, 1);

        AlarmTriggerData triggerData = getAndCheckAlarmTrigger(1);
        AlarmTrigger alarmTrigger = triggerData.get(0);

        EventData getEvent = getAndCheckEvent(event, 1);
        assertNull(getEvent.getAlarms().get(0).getAcknowledged());

        ChronosCalendarResultResponse acknowledgeAlarm = defaultUserApi.getApi().acknowledgeAlarm(defaultUserApi.getSession(), event.getId(), folderId, Integer.valueOf(alarmTrigger.getAlarmId()));
        CalendarResult checkResponse = checkResponse(acknowledgeAlarm.getError(), acknowledgeAlarm.getErrorDesc(), acknowledgeAlarm.getData());
        assertEquals(1, checkResponse.getUpdated().size());
        EventData updated = checkResponse.getUpdated().get(0);
        Long acknowledged = updated.getAlarms().get(0).getAcknowledged();
        assertNotNull(acknowledged);

        getEvent = getAndCheckEvent(updated, 1);
        assertNotNull(getEvent.getAlarms().get(0).getAcknowledged());
        assertEquals(acknowledged, getEvent.getAlarms().get(0).getAcknowledged());
        assertTrue("Acknowdledge time is not later than the creation date.", acknowledged.longValue() >= event.getCreated().longValue());
    }

    @Test
    public void testSimpleSnooze() throws Exception {
        // Create an event with alarm
        EventData event = createSingleEvent("testSimpleSnooze", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        getAndCheckEvent(event, 1);

        AlarmTriggerData triggerData = getAndCheckAlarmTrigger(1);
        AlarmTrigger alarmTrigger = triggerData.get(0);

        EventData getEvent = getAndCheckEvent(event, 1);
        assertNull(getEvent.getAlarms().get(0).getAcknowledged());
        int alarmId = Integer.valueOf(alarmTrigger.getAlarmId());

        // Snooze the alarm by 5 minutes
        long expectedTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        ChronosCalendarResultResponse snoozeResponse = defaultUserApi.getApi().snoozeAlarm(defaultUserApi.getSession(), event.getId(), folderId, alarmId, TimeUnit.MINUTES.toMillis(5));
        CalendarResult snoozeResult = checkResponse(snoozeResponse.getError(), snoozeResponse.getErrorDesc(), snoozeResponse.getData());
        assertEquals(1, snoozeResult.getUpdated().size());
        EventData updatedEvent = snoozeResult.getUpdated().get(0);
        assertEquals(2, updatedEvent.getAlarms().size());

        triggerData = getAndCheckAlarmTrigger(2);
        AlarmTrigger alarmTrigger2 = triggerData.get(0);
        assertNotEquals(alarmTrigger2.getAlarmId(), alarmTrigger.getAlarmId());
        Date parsedTime = DateTimeUtil.parseZuluDateTime(alarmTrigger2.getTime());
        checkAlarmTimeRoughly(expectedTime, parsedTime.getTime());
        String snoozeAlarmId = alarmTrigger2.getAlarmId();

        // Snooze the alarm again by 10 minutes
        expectedTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);
        snoozeResponse = defaultUserApi.getApi().snoozeAlarm(defaultUserApi.getSession(), event.getId(), folderId, Integer.valueOf(snoozeAlarmId), TimeUnit.MINUTES.toMillis(10));
        snoozeResult = checkResponse(snoozeResponse.getError(), snoozeResponse.getErrorDesc(), snoozeResponse.getData());
        assertEquals(1, snoozeResult.getUpdated().size());
        updatedEvent = snoozeResult.getUpdated().get(0);
        assertEquals(2, updatedEvent.getAlarms().size()); // The previous snooze alarm should be replaced by a new one

        triggerData = getAndCheckAlarmTrigger(2);
        alarmTrigger2 = triggerData.get(0);
        assertNotEquals(alarmTrigger2.getAlarmId(), alarmTrigger.getAlarmId());
        assertNotEquals(alarmTrigger2.getAlarmId(), snoozeAlarmId);
        parsedTime = DateTimeUtil.parseZuluDateTime(alarmTrigger2.getTime());
        checkAlarmTimeRoughly(expectedTime, parsedTime.getTime());

    }

}
