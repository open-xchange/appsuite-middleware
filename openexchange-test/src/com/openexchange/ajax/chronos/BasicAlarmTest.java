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
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link BasicAlarmTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicAlarmTest extends AbstractAlarmTest {


    /**
     * Initializes a new {@link BasicAlarmTest}.
     */
    public BasicAlarmTest() {
        super();
    }

    /**
     * Tests the creation an event with a single alarm
     */
    @Test
    public void testCreateSingleAlarm() throws Exception {
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), "testCreateSingleAlarm", AlarmFactory.createDisplayAlarm("-PT15M"), folderId));
        getAndAssertAlarms(expectedEventData, 1, folderId);
    }

    /**
     * Tests the creation of an event without an alarm and the later addition of one
     */
    @Test
    public void testAddSingleAlarm() throws Exception {
        // Create an event without an alarm
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testAddSingleAlarm", folderId));
        EventData actualEventData = getAndAssertAlarms(expectedEventData, 0, folderId);

        // Create the alarm as a delta for the event
        EventData updateData = new EventData();
        updateData.setAlarms(Collections.singletonList(AlarmFactory.createDisplayAlarm("-PT30M")));
        updateData.setId(actualEventData.getId());
        updateData.setLastModified(actualEventData.getLastModified());
        updateData.setFolder(folderId);

        // Update the event and add the alarm
        expectedEventData = eventManager.updateEvent(updateData);

        // Assert that the alarm was successfully added
        getAndAssertAlarms(expectedEventData, 1, folderId);
    }

    /**
     * Tests the change of the alarm time
     */
    @Test
    public void testChangeAlarmTime() throws Exception {
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), "testChangeAlarmTime", AlarmFactory.createDisplayAlarm("-PT15M"), folderId));
        EventData actualEventData = getAndAssertAlarms(expectedEventData, 1, folderId);

        EventData updateData = new EventData();
        updateData.setAlarms(Collections.singletonList(AlarmFactory.createDisplayAlarm("-PT30M")));
        updateData.setId(actualEventData.getId());
        updateData.setLastModified(actualEventData.getLastModified());
        updateData.setFolder(folderId);

        expectedEventData = eventManager.updateEvent(updateData);

        actualEventData = getAndAssertAlarms(expectedEventData, 1, folderId);
        assertEquals("-PT30M", actualEventData.getAlarms().get(0).getTrigger().getDuration());
    }

    /**
     * Tests the creation of different alarm types (display, mail, audio)
     *
     * @throws Exception
     */
    @Test
    public void testDifferentAlarmTypes() throws Exception {
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testDifferentAlarmTypes", folderId));
        EventData actualEventData = getAndAssertAlarms(expectedEventData, 0, folderId);

        // Test display alarm
        {
            Alarm alarm = AlarmFactory.createDisplayAlarm("-PT30M");

            EventData updateData = new EventData();
            updateData.setAlarms(Collections.singletonList(alarm));
            updateData.setId(actualEventData.getId());
            updateData.setLastModified(actualEventData.getLastModified());
            updateData.setFolder(folderId);

            expectedEventData = eventManager.updateEvent(updateData);

            actualEventData = getAndAssertAlarms(expectedEventData, 1, folderId);

            Alarm changedAlarm = actualEventData.getAlarms().get(0);
            alarm.setUid(changedAlarm.getUid());
            alarm.setId(changedAlarm.getId());
            assertEquals("The created alarm does not match the expected one.", alarm, changedAlarm);
        }

        // Test mail alarm
        {
            Alarm alarm = AlarmFactory.createMailAlarm("-PT30M", "This is the mail message!", "This is the mail subject");

            EventData updateData = new EventData();
            updateData.setAlarms(Collections.singletonList(alarm));
            updateData.setId(actualEventData.getId());
            updateData.setLastModified(actualEventData.getLastModified());
            updateData.setFolder(folderId);

            expectedEventData = eventManager.updateEvent(updateData);

            actualEventData = getAndAssertAlarms(expectedEventData, 1, folderId);

            Alarm changedAlarm = actualEventData.getAlarms().get(0);

            Assert.assertNotNull(changedAlarm.getAttendees());
            Assert.assertEquals(1, changedAlarm.getAttendees().size());
            Assert.assertEquals(testUser.getLogin(), changedAlarm.getAttendees().get(0).getEmail());
            alarm.setUid(changedAlarm.getUid());
            alarm.setId(changedAlarm.getId());
            alarm.setAttendees(changedAlarm.getAttendees());
            assertEquals("The created alarm does not match the expected one.", alarm, changedAlarm);
        }

        // Test AUDIO alarm
        {
            Alarm alarm = AlarmFactory.createAudioAlarm("-PT30M", "ftp://some.fake.ftp.server/file.mp3");
            EventData updateData = new EventData();
            updateData.setAlarms(Collections.singletonList(alarm));
            updateData.setId(actualEventData.getId());
            updateData.setLastModified(actualEventData.getLastModified());
            updateData.setFolder(folderId);

            expectedEventData = eventManager.updateEvent(updateData);

            actualEventData = getAndAssertAlarms(expectedEventData, 1, folderId);

            Alarm changedAlarm = actualEventData.getAlarms().get(0);
            alarm.setUid(changedAlarm.getUid());
            alarm.setId(changedAlarm.getId());
            assertEquals("The created alarm does not match the expected one.", alarm, changedAlarm);
        }
    }
}
