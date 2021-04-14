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
 *    trademarks of the OX Software GmbH. group of companies.
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

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleDescription;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.checkNoReplyMailReceived;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.prepareJsonForFileUpload;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.ConferenceBuilder;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.RRuleFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.java.Strings;
import com.openexchange.test.common.asset.Asset;
import com.openexchange.test.common.asset.AssetType;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.AnalysisChange;
import com.openexchange.testing.httpclient.models.AnalysisChangeCurrentEvent;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.ChronosAttachment;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.Conference;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.MailData;

/**
 * {@link ITipAnalyzeChangesTest} - Updates different parts of an event and checks changes on attendee side.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class ITipAnalyzeChangesTest extends AbstractITipAnalyzeTest {

    private String summary;

    private EventData attendeeEvent = null;

    private Attendee replyingAttendee;

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withContexts(2).withUserPerContext(3).useEnhancedApiClients().build();
    }

    /**
     * Creates an event as user A in context 1 with external attendee user B from context 2.
     * User B accepts the event and user A takes over the changes.
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        summary = this.getClass().getName() + " " + UUID.randomUUID().toString();
        /*
         * Create event
         */
        EventData eventToCreate = EventFactory.createSingleTwoHourEvent(0, summary);
        replyingAttendee = prepareCommonAttendees(eventToCreate);
        eventToCreate = prepareAttendeeConference(eventToCreate);
        eventToCreate = prepareModeratorConference(eventToCreate);
        createdEvent = eventManager.createEvent(eventToCreate, true);

        /*
         * Receive mail as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClientC2, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.status);

        /*
         * reply with "accepted"
         */
        attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip), null), createdEvent.getUid());
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.status);

        /*
         * Receive mail as organizer and check actions
         */
        MailData reply = receiveIMip(testUser.getApiClient(), replyingAttendee.getEmail(), summary, 0, SchedulingMethod.REPLY);
        analyze(reply.getId());

        /*
         * Take over accept and check in calendar
         */
        assertSingleEvent(update(constructBody(reply)), createdEvent.getUid());
        EventResponse eventResponse = chronosApi.getEvent(createdEvent.getId(), createdEvent.getFolder(), createdEvent.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        createdEvent = eventResponse.getData();
        for (Attendee attendee : createdEvent.getAttendees()) {
            assertThat("Participant status is not correct.", PartStat.ACCEPTED.status, is(attendee.getPartStat()));
        }
    }

    @Test
    public void testSummaryChange() throws Exception {
        /*
         * Change summary as organizer
         */
        String changedSumamry = "New summary" + UUID.randomUUID();
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setSummary(changedSumamry);
        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that summary has been updated
         */
        MailData iMip = receiveMailAsAttendee(changedSumamry);
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, changedSumamry);

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", attendeeEvent.getSummary().equals(changedSumamry));
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, changedSumamry);
    }

    @Test
    public void testStartAndEndDateChange() throws Exception {
        /*
         * Shift start and end date by two hours as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(date.getTimeInMillis() + TimeUnit.HOURS.toMillis(2));
        deltaEvent.setStartDate(DateTimeUtil.getDateTime(date));
        date.setTimeInMillis(date.getTimeInMillis() + TimeUnit.HOURS.toMillis(2));
        deltaEvent.setEndDate(DateTimeUtil.getDateTime(date));

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that dates has been updated
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.NEEDS_ACTION, CustomConsumers.ACTIONS);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment was rescheduled.");

        /*
         * Update attendee's event
         */
        accept(apiClientC2, constructBody(iMip), null);
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", deltaEvent.getEndDate().equals(attendeeEvent.getEndDate()));

        /*
         * Receive mail as organizer and check actions
         */
        MailData reply = receiveIMip(testUser.getApiClient(), replyingAttendee.getEmail(), summary, 1, SchedulingMethod.REPLY);
        analyze(reply.getId());
    }

    @Test
    public void testStartDateChange() throws Exception {
        /*
         * Shift start by two hours as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(date.getTimeInMillis() + TimeUnit.HOURS.toMillis(1));
        deltaEvent.setStartDate(DateTimeUtil.getDateTime(date));

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that start date has been updated
         * Note: Due internal handling of a shortened Event, no rescheduling will happen. Thus the participant status is
         * unchanged. For details see com.openexchange.chronos.impl.Utils.coversDifferentTimePeriod(Event, Event) or
         * http://documentation.open-xchange.com/latest/middleware/calendar/implementation_details.html#reset-of-participation-status
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ACTIONS);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment was rescheduled.");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", deltaEvent.getStartDate().equals(attendeeEvent.getStartDate()));
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testEndDateChange() throws Exception {
        /*
         * Shift end date by two hours as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(date.getTimeInMillis() + TimeUnit.HOURS.toMillis(4));
        deltaEvent.setEndDate(DateTimeUtil.getDateTime(date));

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that end date has been updated
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.NEEDS_ACTION, CustomConsumers.ACTIONS);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment was rescheduled.");

        /*
         * Update attendee's event
         */
        accept(apiClientC2, constructBody(iMip), null);
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", deltaEvent.getEndDate().equals(attendeeEvent.getEndDate()));
        /*
         * Receive mail as organizer and check actions
         */
        MailData reply = receiveIMip(testUser.getApiClient(), replyingAttendee.getEmail(), summary, 1, SchedulingMethod.REPLY);
        analyze(reply.getId());
    }

    @Test
    public void testStartDateTimeZoneChange() throws Exception {
        /*
         * Shift start and end date by two hours as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        Date date = DateTimeUtil.parseDateTime(createdEvent.getStartDate());
        String timeZone = "Europe/Isle_of_Man";
        deltaEvent.setStartDate(DateTimeUtil.getDateTime(timeZone, date.getTime()));

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that time zone of start date has been updated
         * Note: Due internal handling of a shortened Event, no rescheduling will happen. Thus the participant status is
         * unchanged. For details see com.openexchange.chronos.impl.Utils.coversDifferentTimePeriod(Event, Event) or
         * http://documentation.open-xchange.com/latest/middleware/calendar/implementation_details.html#reset-of-participation-status
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ACTIONS);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The timezone of the appointment's start date was changed.");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        accept(apiClientC2, constructBody(iMip), null);
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue(timeZone.equals(attendeeEvent.getStartDate().getTzid()));
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testLocationChange() throws Exception {
        /*
         * Change location as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        String location = "Olpe";
        deltaEvent.setLocation(location);

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that location has been updated
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment takes place in a new location");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", location.equals(attendeeEvent.getLocation()));
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testRemoveLocation() throws Exception {
        /*
         * Remove location as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setLocation("");

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that location has been updated
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The location of the appointment has been removed");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", Strings.isEmpty(attendeeEvent.getLocation()));
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testTransparencyChange() throws Exception {
        /*
         * Shift start and end date by two hours as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setTransp(TranspEnum.TRANSPARENT.equals(createdEvent.getTransp()) ? TranspEnum.OPAQUE : TranspEnum.TRANSPARENT);

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that the event is marked as "free"
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment will now be shown as");
        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", deltaEvent.getTransp().equals(attendeeEvent.getTransp()));
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testAddAndRemoveAttachment() throws Exception {
        /*
         * Prepare attachment and update it
         */
        Asset asset = assetManager.getRandomAsset(AssetType.jpg);
        File file = new File(asset.getAbsolutePath());
        String callbackHtml = chronosApi.updateEventWithAttachments( //@formatter:off
            createdEvent.getFolder(), createdEvent.getId(), now(),
            prepareJsonForFileUpload(createdEvent.getId(),
            null == createdEvent.getFolder() ? defaultFolderId : createdEvent.getFolder(), asset.getFilename()),
            file, null, null, null, null, null); //@formatter:on
        assertNotNull(callbackHtml);
        assertTrue("Should contain attachment name: " + asset.getFilename(), callbackHtml.contains("\"filename\":\"" + asset.getFilename() + "\""));

        /*
         * Check constrains
         */
        int sequenceId = 0;
        MailData iMip = receiveMailAsAttendee("Appointment changed: " + summary, sequenceId);
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);

        /*
         * Accept changes and check if attachment has been added to the event
         */
        update(apiClientC2, constructBody(receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, sequenceId, SchedulingMethod.REQUEST)));
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment has a new attachment");
        AnalysisChangeCurrentEvent current = analyzeResponse.getData().get(0).getChanges().get(0).getCurrentEvent();

        EventData eventData = eventManagerC2.getEvent(current.getFolder(), current.getId());
        assertEquals(createdEvent.getUid(), eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());
        /*
         * check if attachment was imported correctly
         */
        List<ChronosAttachment> attachments = eventData.getAttachments();
        assertTrue(null != attachments && 1 == attachments.size());
        ChronosAttachment attachment = attachments.get(0);
        assertEquals(asset.getFilename(), attachment.getFilename());
        assertEquals("image/jpeg", attachment.getFmtType());
        byte[] attachmentData = eventManagerC2.getAttachment(eventData.getId(), i(attachment.getManagedId()), eventData.getFolder());
        assertNotNull(attachmentData);

        /*
         * Remove attachment as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setAttachments(Collections.emptyList());
        updateEventAsOrganizer(deltaEvent);

        /*
         * Lookup that event has been removed
         */
        EventData updated = eventManager.getEvent(createdEvent.getFolder(), createdEvent.getId());
        assertThat("Should not contain attachments", updated.getAttachments(), empty());

        /*
         * Receive update as attendee and accept changes
         */
        iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        analyzeResponse = analyze(apiClientC2, iMip);
        analyze(analyzeResponse, CustomConsumers.ALL);
        change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The attachment <i>" + asset.getFilename() + "</i> was removed");
        ActionResponse actionResponse = update(apiClientC2, constructBody(iMip));
        updated = actionResponse.getData().get(0);
        updated = eventManagerC2.getEvent(updated.getFolder(), updated.getId());
        assertThat("Should not contain attachments", updated.getAttachments(), empty());
    }

    @Test
    public void testDescriptionChange() throws Exception {
        /*
         * Change description as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setDescription("New description");

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that end date has been updated
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment description has changed.");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", deltaEvent.getDescription().equals(attendeeEvent.getDescription()));
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testRecuccrenceRuleChange() throws Exception {
        /*
         * Convert event to series as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        int occurences = 10;
        String rrule = RRuleFactory.getFrequencyWithOccurenceLimit(RecurringFrequency.DAILY, occurences);
        deltaEvent.setRrule(rrule);

        /*
         * Update as organizer
         */
        long now = now().longValue();
        String fromStr = DateTimeUtil.getZuluDateTime(new Date(now - TimeUnit.DAYS.toMillis(1)).getTime()).getValue();
        String untilStr = DateTimeUtil.getZuluDateTime(new Date(now + TimeUnit.DAYS.toMillis(30)).getTime()).getValue();

        ChronosCalendarResultResponse calendarResultResponse = chronosApi.updateEvent(deltaEvent.getFolder(), deltaEvent.getId(), now(), getUpdateBody(deltaEvent), deltaEvent.getRecurrenceId(), null, null, null, null, null, null, fromStr, untilStr, Boolean.TRUE, null);
        assertNull(calendarResultResponse.getError());
        assertTrue(calendarResultResponse.getData().getUpdated().size() == 0);
        assertTrue(calendarResultResponse.getData().getCreated().size() == occurences);
        assertTrue(calendarResultResponse.getData().getDeleted().size() == 1);

        /*
         * Check that end date has been updated
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.NEEDS_ACTION, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertThat("Recurrence ID is not correct.", change.getNewEvent().getRrule(), is(rrule));

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        accept(apiClientC2, constructBody(iMip), null);
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertThat("Not changed!", attendeeEvent.getRrule(), is(rrule));
        /*
         * Receive mail as organizer and check actions
         */
        MailData reply = receiveIMip(testUser.getApiClient(), replyingAttendee.getEmail(), summary, 1, SchedulingMethod.REPLY);
        analyze(reply.getId());
    }

    @Test
    public void testAddAttendee() throws Exception {
        /*
         * Add an third attendee
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        TestUser testUser3 = context2.acquireUser();

        Attendee addedAttendee = ITipUtil.convertToAttendee(testUser3, Integer.valueOf(0));
        addedAttendee.setPartStat(PartStat.NEEDS_ACTION.getStatus());
        deltaEvent.getAttendees().add(addedAttendee);
        updateEventAsOrganizer(deltaEvent);
        assertTrue("Attendee was not added", deltaEvent.getAttendees().size() == createdEvent.getAttendees().size());

        /*
         * Check that the event has a new attendee
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "has been invited to the appointment");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", deltaEvent.getAttendees().size() == attendeeEvent.getAttendees().size());
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);

        /*
         * Check invite mail for new attendee
         */
        ApiClient apiClient3 = testUser3.getApiClient();
        iMip = receiveIMip(apiClient3, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        analyzeResponse = analyze(apiClient3, iMip);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyzeResponse).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(attendeeEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), addedAttendee.getEmail(), PartStat.NEEDS_ACTION.getStatus());
        analyze(analyzeResponse, CustomConsumers.ACTIONS);
    }

    @Test
    public void testRemoveAttendee() throws Exception {
        /*
         * Remove attendee
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setAttendees(Collections.singletonList(createdEvent.getAttendees().stream().filter(a -> null != a.getEntity() && userResponseC1.getData().getId().equals(a.getEntity().toString())).findFirst().orElseThrow(() -> new Exception("Unable to find organizer"))));

        updateEventAsOrganizer(deltaEvent);

        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), "Appointment canceled: " + summary, 1, SchedulingMethod.CANCEL);
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        analyze(analyzeResponse, CustomConsumers.CANCEL);

        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertTrue(change.getIntroduction().contains("you have been removed as a participant"));

        /*
         * Delete attendee's event and check that no mail as been scheduled
         */
        cancel(testUserC2.getApiClient(), constructBody(iMip), null, false);
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testAddConference() throws Exception {
        /*
         * Add additional conference as organizer
         */
        ConferenceBuilder builder = ConferenceBuilder.newBuilder() //@formatter:off
            .setDefaultFeatures()
            .setLable("Random lable")
            .setVideoChatUri(); //@formatter:on
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setConferences(createdEvent.getConferences());
        deltaEvent.addConferencesItem(builder.build());
        updateEventAsOrganizer(deltaEvent);

        EventData updatedEvent = eventManager.getEvent(defaultFolderId, createdEvent.getId());
        assertThat("Should be three conferences!", I(updatedEvent.getConferences().size()), is(I(3)));

        /*
         * Check that the conference item has been added
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "access information was changed");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", deltaEvent.getConferences().size() == attendeeEvent.getConferences().size());
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testUpdateConference() throws Exception {
        /*
         * Change conference item as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        ArrayList<Conference> conferences = new ArrayList<Conference>(2);
        conferences.add(createdEvent.getConferences().get(1));
        Conference update = ConferenceBuilder.copy(createdEvent.getConferences().get(0));
        String label = "New lable";
        update.setLabel(label);
        conferences.add(update);
        deltaEvent.setConferences(conferences);
        updateEventAsOrganizer(deltaEvent);

        EventData updatedEvent = eventManager.getEvent(defaultFolderId, createdEvent.getId());
        assertThat("Should be two conferences!", I(updatedEvent.getConferences().size()), is(I(2)));

        /*
         * Check that conference has been updated
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "access information was changed");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        boolean changed = false;
        for (Conference conference : attendeeEvent.getConferences()) {
            if (label.equals(conference.getLabel())) {
                changed = true;
            }
        }
        assertTrue("Not changed!", changed);
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testUpdateCallWithoutUpdate() throws Exception {
        /*
         * Change conference item as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that mails has been send (updates still needs to be propagated) without any description
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.EMPTY);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertThat("Should have been no change", change.getDiffDescription(), empty());
    }

    @Test
    public void testUpdateExtendedPropertiesOfConference() throws Exception {
        /*
         * Change conference item as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        ArrayList<Conference> conferences = new ArrayList<Conference>(2);
        for (Conference conference : createdEvent.getConferences()) {
            Conference update = ConferenceBuilder.copy(conference);
            update.setExtendedParameters(null);
            conferences.add(update);
        }
        deltaEvent.setConferences(conferences);
        updateEventAsOrganizer(deltaEvent);

        EventData updatedEvent = eventManager.getEvent(defaultFolderId, createdEvent.getId());
        assertThat("Should be two conferences!", I(updatedEvent.getConferences().size()), is(I(2)));

        /*
         * Check that mails has been send (updates still needs to be propagated) without any description
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.EMPTY);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertThat("Should have been no change", change.getDiffDescription(), empty());
    }

    @Test
    public void testRemoveConference() throws Exception {
        /*
         * Remove conference item as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setConferences(Collections.emptyList());
        updateEventAsOrganizer(deltaEvent);

        EventData updatedEvent = eventManager.getEvent(defaultFolderId, createdEvent.getId());
        assertThat("Should be no conferences!", updatedEvent.getConferences(), empty());

        /*
         * Check that mails has been send
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "access information was removed");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", attendeeEvent.getConferences().isEmpty());
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    @Test
    public void testRemoveOnlyOneConference() throws Exception {
        /*
         * Remove conference item as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setConferences(Collections.singletonList(createdEvent.getConferences().get(0)));
        updateEventAsOrganizer(deltaEvent);

        EventData updatedEvent = eventManager.getEvent(defaultFolderId, createdEvent.getId());
        assertThat("Should be one conference!", I(updatedEvent.getConferences().size()), is(I(1)));

        /*
         * Check that mails has been send, but for changed conferences not removed all
         */
        MailData iMip = receiveMailAsAttendee();
        AnalyzeResponse analyzeResponse = analyzeUpdateAsAttendee(iMip, PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "access information was changed");

        /*
         * Update attendee's event and check that no mail as been scheduled
         */
        update(apiClientC2, constructBody(iMip));
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertTrue("Not changed!", deltaEvent.getConferences().size() == attendeeEvent.getConferences().size());
        checkNoReplyMailReceived(testUser.getApiClient(), replyingAttendee, summary);
    }

    /*
     * ----------------------------- HELPERS ------------------------------
     */

    private void updateEventAsOrganizer(EventData deltaEvent) throws ApiException {
        long now = now().longValue();
        String fromStr = DateTimeUtil.getZuluDateTime(new Date(now - TimeUnit.DAYS.toMillis(1)).getTime()).getValue();
        String untilStr = DateTimeUtil.getZuluDateTime(new Date(now + TimeUnit.DAYS.toMillis(30)).getTime()).getValue();

        ChronosCalendarResultResponse calendarResultResponse = chronosApi.updateEvent(deltaEvent.getFolder(), deltaEvent.getId(), now(), getUpdateBody(deltaEvent), deltaEvent.getRecurrenceId(), null, null, null, null, null, null, fromStr, untilStr, Boolean.TRUE, null);
        CalendarResult result = checkResponse(calendarResultResponse.getError(), calendarResultResponse.getErrorDesc(), calendarResultResponse.getData());
        assertTrue(result.getUpdated().size() == 1);
        createdEvent = result.getUpdated().get(0);
    }

    private AnalyzeResponse analyzeUpdateAsAttendee(MailData iMip, PartStat partStat, CustomConsumers consumer) throws Exception {
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyzeResponse).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(attendeeEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), partStat.getStatus());
        analyze(analyzeResponse, consumer);
        return analyzeResponse;
    }

    private MailData receiveMailAsAttendee() throws Exception {
        return receiveMailAsAttendee("Appointment changed: " + summary);
    }

    private MailData receiveMailAsAttendee(String summary) throws Exception {
        return receiveMailAsAttendee(summary, 1);
    }

    private MailData receiveMailAsAttendee(int sequence) throws Exception {
        return receiveMailAsAttendee(summary, sequence);
    }

    private MailData receiveMailAsAttendee(String summary, int sequnce) throws Exception {
        return receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, sequnce, SchedulingMethod.REQUEST);
    }

}
