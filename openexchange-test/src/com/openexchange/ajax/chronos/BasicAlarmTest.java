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
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosAttachment;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.Trigger;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 *
 * {@link BasicAlarmTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicAlarmTest extends AbstractChronosTest {

    private String folderId;

    @SuppressWarnings("unchecked")
    private EventData createSingleEventWithoutAlarms(String summary) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(calUser);
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(getDateTime(System.currentTimeMillis()));
        singleEvent.setEndDate(getDateTime(System.currentTimeMillis()+5000));
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setAllDay(false);
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    @SuppressWarnings("unchecked")
    private EventData createSingleEventWithSingleAlarm(String summary) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(calUser);
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(getDateTime(System.currentTimeMillis()));
        singleEvent.setEndDate(getDateTime(System.currentTimeMillis()+5000));
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
        folderId = getDefaultFolder();
    }

    @Test
    public void testCreateSingleAlarm() throws Exception {
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEventWithSingleAlarm("testCreateSingleAlarm"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), createEvent.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertNotNull(eventResponse.getData().getAlarms());
        assertEquals(1, eventResponse.getData().getAlarms().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddSingleAlarm() throws Exception {
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEventWithoutAlarms("testAddSingleAlarm"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), createEvent.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertNotNull(eventResponse.getData().getAlarms());
        assertEquals(0, eventResponse.getData().getAlarms().size());

        EventData updateData = new EventData();
        Alarm alarm = new Alarm();
        alarm.setAction("display");
        Trigger trigger = new Trigger();
        trigger.setRelated(RelatedEnum.START);
        trigger.setDuration("-PT30M");
        alarm.setTrigger(trigger);
        alarm.setDescription("This is the display message!");
        updateData.setAlarms(Collections.singletonList(alarm));

        ChronosCalendarResultResponse updateEvent = api.updateEvent(session, folderId, event.getId(), updateData, eventResponse.getTimestamp(), null, true, false);
        assertNull(updateEvent.getError(), updateEvent.getErrorDesc());
        assertNotNull(updateEvent.getData());
        assertEquals(1, updateEvent.getData().getUpdated().size());


        EventResponse eventResponse2 = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse2.getError(), eventResponse2.getErrorDesc());
        assertNotNull(eventResponse2.getData());
        assertNotNull(eventResponse2.getData().getAlarms());
        assertEquals(1, eventResponse2.getData().getAlarms().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testChangeAlarmTime() throws Exception {
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEventWithSingleAlarm("testChangeAlarmTime"), false, false);
        assertNull(createEvent.getError(), createEvent.getErrorDesc());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), eventResponse.getErrorDesc());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertNotNull(eventResponse.getData().getAlarms());
        assertEquals(1, eventResponse.getData().getAlarms().size());


        EventData updateData = new EventData();
        Alarm alarm = new Alarm();
        alarm.setAction("display");
        Trigger trigger = new Trigger();
        trigger.setRelated(RelatedEnum.START);
        trigger.setDuration("-PT30M");
        alarm.setTrigger(trigger);
        alarm.setDescription("This is the display message!");
        updateData.setAlarms(Collections.singletonList(alarm));

        ChronosCalendarResultResponse updateEvent = api.updateEvent(session, folderId, event.getId(), updateData, eventResponse.getTimestamp(), null, true, false);
        assertNull(updateEvent.getError(), updateEvent.getErrorDesc());
        assertNotNull(updateEvent.getData());
        assertEquals(1, updateEvent.getData().getUpdated().size());


        EventResponse eventResponse2 = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse2.getError(), eventResponse2.getErrorDesc());
        assertNotNull(eventResponse2.getData());
        assertNotNull(eventResponse2.getData().getAlarms());
        assertEquals(1, eventResponse2.getData().getAlarms().size());
        assertEquals("-PT30M", eventResponse2.getData().getAlarms().get(0).getTrigger().getDuration());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDifferentAlarmTypes() throws Exception {
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEventWithoutAlarms("testDifferentAlarmTypes"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), createEvent.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertNotNull(eventResponse.getData().getAlarms());
        assertEquals(0, eventResponse.getData().getAlarms().size());

        long timestamp = eventResponse.getTimestamp();

        // Test display alarm
        {
            EventData updateData = new EventData();
            Alarm alarm = new Alarm();
            alarm.setAction("display");
            Trigger trigger = new Trigger();
            trigger.setRelated(RelatedEnum.START);
            trigger.setDuration("-PT30M");
            alarm.setTrigger(trigger);
            alarm.setDescription("This is the display message!");
            updateData.setAlarms(Collections.singletonList(alarm));

            ChronosCalendarResultResponse updateEvent = api.updateEvent(session, folderId, event.getId(), updateData, timestamp, null, true, false);
            assertNull(updateEvent.getError(), updateEvent.getErrorDesc());
            assertNotNull(updateEvent.getData());
            assertEquals(1, updateEvent.getData().getUpdated().size());

            EventResponse eventResponse2 = api.getEvent(session, event.getId(), folderId, null, null);
            assertNull(eventResponse2.getError(), eventResponse2.getErrorDesc());
            assertNotNull(eventResponse2.getData());
            assertNotNull(eventResponse2.getData().getAlarms());
            assertEquals(1, eventResponse2.getData().getAlarms().size());
            Alarm changedAlarm = eventResponse2.getData().getAlarms().get(0);
            alarm.setUid(changedAlarm.getUid());
            assertEquals("The created alarm does not match the expected one.", alarm, changedAlarm);
            timestamp = updateEvent.getTimestamp();
        }

        // Test mail alarm
        {
            EventData updateData = new EventData();
            Alarm alarm = new Alarm();
            alarm.setAction("mail");
            Trigger trigger = new Trigger();
            trigger.setRelated(RelatedEnum.START);
            trigger.setDuration("-PT30M");
            alarm.setTrigger(trigger);
            alarm.setDescription("This is the mail message!");
            alarm.setSummary("This is the mail subject");
            List<Attendee> attendees = new ArrayList<>(1);
            Attendee attendee = new Attendee();
            attendee.setUri("mailto:test@domain.wrong");
            attendee.setEmail("test@domain.wrong");
            attendees.add(attendee);
            alarm.setAttendees(attendees);
            updateData.setAlarms(Collections.singletonList(alarm));

            ChronosCalendarResultResponse updateEvent = api.updateEvent(session, folderId, event.getId(), updateData, timestamp, null, true, false);
            assertNull(updateEvent.getError(), updateEvent.getErrorDesc());
            assertNotNull(updateEvent.getData());
            assertEquals(1, updateEvent.getData().getUpdated().size());

            EventResponse eventResponse2 = api.getEvent(session, event.getId(), folderId, null, null);
            assertNull(eventResponse2.getError(), eventResponse2.getErrorDesc());
            assertNotNull(eventResponse2.getData());
            assertNotNull(eventResponse2.getData().getAlarms());
            assertEquals(1, eventResponse2.getData().getAlarms().size());
            Alarm changedAlarm = eventResponse2.getData().getAlarms().get(0);
            alarm.setUid(changedAlarm.getUid());
            assertEquals("The created alarm does not match the expected one.", alarm, changedAlarm);
            timestamp = updateEvent.getTimestamp();
        }

        // Test AUDIO alarm
        {
            EventData updateData = new EventData();
            Alarm alarm = new Alarm();
            alarm.setAction("audio");
            Trigger trigger = new Trigger();
            trigger.setRelated(RelatedEnum.START);
            trigger.setDuration("-PT30M");
            alarm.setTrigger(trigger);
            List<ChronosAttachment> attachments = new ArrayList<>(1);
            ChronosAttachment attachment = new ChronosAttachment();
            attachment.setUri("fpt://some.fake.ftp.server/file.mp3");
            attachment.setFmtType("audio/mpeg");
            attachments.add(attachment);
            alarm.setAttachments(attachments);
            updateData.setAlarms(Collections.singletonList(alarm));

            ChronosCalendarResultResponse updateEvent = api.updateEvent(session, folderId, event.getId(), updateData, timestamp, null, true, false);
            assertNull(updateEvent.getError(), updateEvent.getErrorDesc());
            assertNotNull(updateEvent.getData());
            assertEquals(1, updateEvent.getData().getUpdated().size());

            EventResponse eventResponse2 = api.getEvent(session, event.getId(), folderId, null, null);
            assertNull(eventResponse2.getError(), eventResponse2.getErrorDesc());
            assertNotNull(eventResponse2.getData());
            assertNotNull(eventResponse2.getData().getAlarms());
            assertEquals(1, eventResponse2.getData().getAlarms().size());
            Alarm changedAlarm = eventResponse2.getData().getAlarms().get(0);
            alarm.setUid(changedAlarm.getUid());
            assertEquals("The created alarm does not match the expected one.", alarm, changedAlarm);
            timestamp = updateEvent.getTimestamp();
        }
    }

}
