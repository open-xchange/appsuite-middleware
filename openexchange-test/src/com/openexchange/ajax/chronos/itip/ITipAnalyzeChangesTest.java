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
import com.openexchange.configuration.asset.Asset;
import com.openexchange.configuration.asset.AssetType;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.AnalysisChange;
import com.openexchange.testing.httpclient.models.AnalysisChangeCurrentEvent;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
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
        rememberMail(apiClientC2, iMip);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClientC2, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.status);

        /*
         * reply with "accepted"
         */
        attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip), null), createdEvent.getUid());
        rememberForCleanup(apiClientC2, attendeeEvent);
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.status);

        /*
         * Receive mail as organizer and check actions
         */
        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 0, SchedulingMethod.REPLY);
        analyze(reply.getId());
        rememberMail(reply);

        /*
         * Take over accept and check in calendar
         */
        assertSingleEvent(update(constructBody(reply)), createdEvent.getUid());
        EventResponse eventResponse = chronosApi.getEvent(apiClient.getSession(), createdEvent.getId(), createdEvent.getFolder(), createdEvent.getRecurrenceId(), null, null);
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
        String changedSumamry = "New summary";
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setSummary(changedSumamry);
        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that summary has been updated
         */
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL, changedSumamry, 1);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, changedSumamry);
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.NEEDS_ACTION, CustomConsumers.ACTIONS);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment was rescheduled.");
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ACTIONS);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment was rescheduled.");
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.NEEDS_ACTION, CustomConsumers.ACTIONS);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment was rescheduled.");
    }

    @Test
    public void testStartDateTimeZoneChange() throws Exception {
        /*
         * Shift start and end date by two hours as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        Date date = DateTimeUtil.parseDateTime(createdEvent.getStartDate());
        deltaEvent.setStartDate(DateTimeUtil.getDateTime("Europe/Isle_of_Man", date.getTime()));

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that time zone of start date has been updated
         * Note: Due internal handling of a shortened Event, no rescheduling will happen. Thus the participant status is
         * unchanged. For details see com.openexchange.chronos.impl.Utils.coversDifferentTimePeriod(Event, Event) or
         * http://documentation.open-xchange.com/latest/middleware/calendar/implementation_details.html#reset-of-participation-status
         */
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ACTIONS);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The timezone of the appointment's start date was changed.");
    }

    @Test
    public void testLocationChange() throws Exception {
        /*
         * Change location as organizer
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setLocation("Olpe");

        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that location has been updated
         */
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment takes place in a new location");
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The location of the appointment has been removed");
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment will now be shown as");
    }

    @Test
    public void testAddAndRemoveAttachment() throws Exception {
        /*
         * Prepare attachment and update it
         */
        Asset asset = assetManager.getRandomAsset(AssetType.jpg);
        File file = new File(asset.getAbsolutePath());
        String callbackHtml = chronosApi.updateEventWithAttachments( //@formatter:off
            apiClient.getSession(), createdEvent.getFolder(), createdEvent.getId(), now(), 
            prepareJsonForFileUpload(createdEvent.getId(), 
            null == createdEvent.getFolder() ? defaultFolderId : createdEvent.getFolder(), asset.getFilename()), 
            file, null, null, null, null, null); //@formatter:on
        assertNotNull(callbackHtml);
        assertTrue("Should contain attachment name: " + asset.getFilename(), callbackHtml.contains("\"filename\":\"" + asset.getFilename() + "\""));

        /*
         * Check constrains
         */
        int sequenceId = 0;
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL, sequenceId);

        /*
         * Accept changes and check if attachment has been added to the event
         */
        update(apiClientC2, constructBody(receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, sequenceId, SchedulingMethod.REQUEST)));
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment has a new attachment");
        AnalysisChangeCurrentEvent current = analyzeResponse.getData().get(0).getChanges().get(0).getCurrentEvent();

        EventData eventData = eventManagerC2.getEvent(current.getFolder(), current.getId());
        rememberForCleanup(eventData);
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
        byte[] attachmentData = eventManagerC2.getAttachment(eventData.getId(), i(attachment.getManagedId()),  eventData.getFolder());
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
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        rememberMail(apiClientC2, iMip);
        analyzeResponse = analyze(apiClientC2, iMip);
        analyze(analyzeResponse, CustomConsumers.ALL);
        change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The attachment <i>"+ asset.getFilename() + "</i> was removed");
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "The appointment description has changed.");
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

        ChronosCalendarResultResponse calendarResultResponse = chronosApi.updateEvent(apiClient.getSession(), deltaEvent.getFolder(), deltaEvent.getId(), now(), getUpdateBody(deltaEvent), deltaEvent.getRecurrenceId(), null, null, null, null, null, null, fromStr, untilStr, Boolean.TRUE, null);
        assertNull(calendarResultResponse.getError());
        assertTrue(calendarResultResponse.getData().getUpdated().size() == 0);
        assertTrue(calendarResultResponse.getData().getCreated().size() == occurences);
        assertTrue(calendarResultResponse.getData().getDeleted().size() == 1);

        /*
         * Check that end date has been updated
         */
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.NEEDS_ACTION, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertThat("Recurrence ID is not correct.", change.getNewEvent().getRrule(), is(rrule));
    }

    @Test
    public void testAddAttendee() throws Exception {
        /*
         * Add an third attendee
         */
        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        TestUser testUser3 = context2.acquireUser();
        addTearDownOperation(() -> context2.backUser(testUser3));

        Attendee addedAttendee = ITipUtil.convertToAttendee(testUser3, Integer.valueOf(0));
        addedAttendee.setPartStat(PartStat.NEEDS_ACTION.getStatus());
        deltaEvent.getAttendees().add(addedAttendee);
        updateEventAsOrganizer(deltaEvent);

        /*
         * Check that the event has a new attendee
         */
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "has been invited to the appointment");

        /*
         * Check invite mail for new attendee
         */
        ApiClient apiClient3 = generateApiClient(testUser3);
        rememberClient(apiClient3);
        MailData iMip = receiveIMip(apiClient3, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        rememberMail(apiClient3, iMip);
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
        rememberMail(apiClientC2, iMip);
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        analyze(analyzeResponse, CustomConsumers.CANCEL);

        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertTrue(change.getIntroduction().contains("you have been removed as a participant"));
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL, 1);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "access information was changed");
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
        update.setLabel("New lable");
        conferences.add(update);
        deltaEvent.setConferences(conferences);
        updateEventAsOrganizer(deltaEvent);

        EventData updatedEvent = eventManager.getEvent(defaultFolderId, createdEvent.getId());
        assertThat("Should be two conferences!", I(updatedEvent.getConferences().size()), is(I(2)));

        /*
         * Check that conference has been updated
         */
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL, 1);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "access information was changed");
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.EMPTY, 1);
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.EMPTY, 1);
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL, 1);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "access information was removed");
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
        AnalyzeResponse analyzeResponse = receiveUpdateAsAttendee(PartStat.ACCEPTED, CustomConsumers.ALL, 1);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        assertSingleDescription(change, "access information was changed");
    }

    /*
     * ----------------------------- HELPERS ------------------------------
     */

    private void updateEventAsOrganizer(EventData deltaEvent) throws ApiException {
        long now = now().longValue();
        String fromStr = DateTimeUtil.getZuluDateTime(new Date(now - TimeUnit.DAYS.toMillis(1)).getTime()).getValue();
        String untilStr = DateTimeUtil.getZuluDateTime(new Date(now + TimeUnit.DAYS.toMillis(30)).getTime()).getValue();

        ChronosCalendarResultResponse calendarResultResponse = chronosApi.updateEvent(apiClient.getSession(), deltaEvent.getFolder(), deltaEvent.getId(), now(), getUpdateBody(deltaEvent), deltaEvent.getRecurrenceId(), null, null, null, null, null, null, fromStr, untilStr, Boolean.TRUE, null);
        assertNull(calendarResultResponse.getError());
        assertTrue(calendarResultResponse.getData().getUpdated().size() == 1);
        createdEvent = calendarResultResponse.getData().getUpdated().get(0);
    }

    private AnalyzeResponse receiveUpdateAsAttendee(PartStat partStat, CustomConsumers consumer) throws Exception {
        return receiveUpdateAsAttendee(partStat, consumer, "Appointment changed: " + summary, 1);
    }

    private AnalyzeResponse receiveUpdateAsAttendee(PartStat partStat, CustomConsumers consumer, int sequnce) throws Exception {
        return receiveUpdateAsAttendee(partStat, consumer, "Appointment changed: " + summary, sequnce);
    }

    private AnalyzeResponse receiveUpdateAsAttendee(PartStat partStat, CustomConsumers consumer, String summary, int sequnce) throws Exception {
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, sequnce, SchedulingMethod.REQUEST);
        rememberMail(apiClientC2, iMip);
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyzeResponse).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(attendeeEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), partStat.getStatus());
        analyze(analyzeResponse, consumer);
        return analyzeResponse;
    }

}
