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
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.util.AssertUtil;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosAttachment;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.Trigger;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;

/**
 *
 * {@link BasicAlarmTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicAlarmTest extends AbstractChronosTest {

    private EventData createSingleEventWithoutAlarms(String summary) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(defaultUserApi.getCalUser());
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(DateTimeUtil.getDateTime(System.currentTimeMillis()));
        singleEvent.setEndDate(DateTimeUtil.getDateTime(System.currentTimeMillis() + 5000));
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setAllDay(false);
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    private EventData createSingleEventWithSingleAlarm(String summary) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(defaultUserApi.getCalUser());
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(DateTimeUtil.getDateTime(System.currentTimeMillis()));
        singleEvent.setEndDate(DateTimeUtil.getDateTime(System.currentTimeMillis() + 5000));
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setAllDay(false);

        Alarm alarm = new Alarm();
        alarm.setAction("display");
        Trigger trigger = new Trigger();
        trigger.setRelated(RelatedEnum.START);
        trigger.setDuration("-PT15M");
        alarm.setTrigger(trigger);
        alarm.setDescription("This is the display message!");
        singleEvent.setAlarms(Collections.singletonList(alarm));
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = createAndRememberNewFolder(defaultUserApi, defaultUserApi.getSession(), getDefaultFolder(), defaultUserApi.getCalUser());
    }

    /**
     * Tests the creation an event with a single alarm
     */
    @Test
    public void testCreateSingleAlarm() throws Exception {
        EventData expectedEventData = eventManager.createEvent(createSingleEventWithSingleAlarm("testCreateSingleAlarm"));
        EventData actualEventData = eventManager.getEvent(expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);
        assertNotNull(actualEventData.getAlarms());
        assertEquals(1, actualEventData.getAlarms().size());
    }

    /**
     * Tests the creation of an event without an alarm and the later addition of one
     */
    @Test
    public void testAddSingleAlarm() throws Exception {
        // Create an event without an alarm
        EventData expectedEventData = eventManager.createEvent(createSingleEventWithoutAlarms("testAddSingleAlarm"));
        EventData actualEventData = eventManager.getEvent(expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);
        assertNotNull(actualEventData.getAlarms());
        assertEquals(0, actualEventData.getAlarms().size());

        // Create the alarm
        Trigger trigger = new Trigger();
        trigger.setRelated(RelatedEnum.START);
        trigger.setDuration("-PT30M");

        Alarm alarm = new Alarm();
        alarm.setAction("display");
        alarm.setTrigger(trigger);
        alarm.setDescription("This is the display message!");

        EventData updateData = new EventData();
        updateData.setAlarms(Collections.singletonList(alarm));
        updateData.setId(actualEventData.getId());

        // Update the event and add the alarm
        expectedEventData = eventManager.updateEvent(updateData);

        // Assert that the alarm was successfully added
        actualEventData = eventManager.getEvent(expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);
        assertNotNull(actualEventData.getAlarms());
        assertEquals(1, actualEventData.getAlarms().size());
    }

    /**
     * Tests the change of the alarm time
     */
    @Test
    public void testChangeAlarmTime() throws Exception {
        EventData expectedEventData = eventManager.createEvent(createSingleEventWithSingleAlarm("testChangeAlarmTime"));
        EventData actualEventData = eventManager.getEvent(expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);
        assertNotNull(actualEventData.getAlarms());
        assertEquals(1, actualEventData.getAlarms().size());

        Trigger trigger = new Trigger();
        trigger.setRelated(RelatedEnum.START);
        trigger.setDuration("-PT30M");

        Alarm alarm = new Alarm();
        alarm.setAction("display");
        alarm.setTrigger(trigger);
        alarm.setDescription("This is the display message!");

        EventData updateData = new EventData();
        updateData.setAlarms(Collections.singletonList(alarm));
        updateData.setId(actualEventData.getId());

        expectedEventData = eventManager.updateEvent(updateData);

        actualEventData = eventManager.getEvent(expectedEventData.getId());
        assertNotNull(actualEventData.getAlarms());
        assertEquals(1, actualEventData.getAlarms().size());
        assertEquals("-PT30M", actualEventData.getAlarms().get(0).getTrigger().getDuration());
    }

    /**
     * Tests the creation of different alarm types (display, mail, audio)
     * 
     * @throws Exception
     */
    @Test
    public void testDifferentAlarmTypes() throws Exception {
        EventData expectedEventData = eventManager.createEvent(createSingleEventWithoutAlarms("testDifferentAlarmTypes"));
        EventData actualEventData = eventManager.getEvent(expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);
        assertNotNull(actualEventData.getAlarms());
        assertEquals(0, actualEventData.getAlarms().size());

        // Test display alarm
        {
            Trigger trigger = new Trigger();
            trigger.setRelated(RelatedEnum.START);
            trigger.setDuration("-PT30M");

            Alarm alarm = new Alarm();
            alarm.setAction("display");
            alarm.setTrigger(trigger);
            alarm.setDescription("This is the display message!");

            EventData updateData = new EventData();
            updateData.setAlarms(Collections.singletonList(alarm));
            updateData.setId(actualEventData.getId());

            expectedEventData = eventManager.updateEvent(updateData);

            actualEventData = eventManager.getEvent(expectedEventData.getId());
            assertNotNull(actualEventData.getAlarms());
            assertEquals(1, actualEventData.getAlarms().size());

            Alarm changedAlarm = actualEventData.getAlarms().get(0);
            alarm.setUid(changedAlarm.getUid());
            assertEquals("The created alarm does not match the expected one.", alarm, changedAlarm);
        }

        // Test mail alarm
        {

            Trigger trigger = new Trigger();
            trigger.setRelated(RelatedEnum.START);
            trigger.setDuration("-PT30M");

            List<Attendee> attendees = new ArrayList<>(1);
            Attendee attendee = new Attendee();
            attendee.setUri("mailto:test@domain.wrong");
            attendee.setEmail("test@domain.wrong");
            attendees.add(attendee);

            Alarm alarm = new Alarm();
            alarm.setAction("mail");
            alarm.setTrigger(trigger);
            alarm.setDescription("This is the mail message!");
            alarm.setSummary("This is the mail subject");
            alarm.setAttendees(attendees);

            EventData updateData = new EventData();
            updateData.setAlarms(Collections.singletonList(alarm));
            updateData.setId(actualEventData.getId());

            expectedEventData = eventManager.updateEvent(updateData);

            actualEventData = eventManager.getEvent(expectedEventData.getId());
            assertNotNull(actualEventData.getAlarms());
            assertEquals(1, actualEventData.getAlarms().size());

            Alarm changedAlarm = actualEventData.getAlarms().get(0);
            alarm.setUid(changedAlarm.getUid());
            assertEquals("The created alarm does not match the expected one.", alarm, changedAlarm);
        }

        // Test AUDIO alarm
        {

            Trigger trigger = new Trigger();
            trigger.setRelated(RelatedEnum.START);
            trigger.setDuration("-PT30M");

            List<ChronosAttachment> attachments = new ArrayList<>(1);
            ChronosAttachment attachment = new ChronosAttachment();
            attachment.setUri("fpt://some.fake.ftp.server/file.mp3");
            attachment.setFmtType("audio/mpeg");
            attachments.add(attachment);

            Alarm alarm = new Alarm();
            alarm.setTrigger(trigger);
            alarm.setAction("audio");
            alarm.setAttachments(attachments);

            EventData updateData = new EventData();
            updateData.setAlarms(Collections.singletonList(alarm));
            updateData.setId(actualEventData.getId());

            expectedEventData = eventManager.updateEvent(updateData);

            actualEventData = eventManager.getEvent(expectedEventData.getId());
            assertNotNull(actualEventData.getAlarms());
            assertEquals(1, actualEventData.getAlarms().size());

            Alarm changedAlarm = actualEventData.getAlarms().get(0);
            alarm.setUid(changedAlarm.getUid());
            assertEquals("The created alarm does not match the expected one.", alarm, changedAlarm);
        }
    }
}
