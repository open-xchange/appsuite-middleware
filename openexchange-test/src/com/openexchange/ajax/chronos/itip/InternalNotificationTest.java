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
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.convertToAttendee;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.UUID;
import org.jdom2.IllegalDataException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.MailAttachment;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.UserResponse;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link InternalNotificationTest} - Checks that a notification is sent to another internal user when the organizer
 * updates participant status of external attendees
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class InternalNotificationTest extends AbstractITipAnalyzeTest {

    private String summary;

    private EventData attendeeEvent = null;

    private Attendee replyingAttendee;

    private Attendee internalAttendee;

    private TestUser testUser2C1;

    private UserResponse userResponse2C1;

    private ApiClient apiClient2C1;

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
         * Prepare other internal attendee
         */
        testUser2C1 = testContext.acquireUser();
        apiClient2C1 = generateApiClient(testUser2C1);
        rememberClient(apiClient2C1);
        UserApi userApi = new UserApi(apiClient2C1);
        userResponse2C1 = userApi.getUser(apiClient2C1.getSession(), String.valueOf(apiClient2C1.getUserId()));
        if (null == userResponse2C1) {
            throw new IllegalDataException("Need user info for test!");
        }
        internalAttendee = convertToAttendee(testUser2C1, apiClient2C1.getUserId());
        internalAttendee.setPartStat(PartStat.NEEDS_ACTION.getStatus());

        /*
         * Create event
         */
        EventData eventToCreate = EventFactory.createSingleTwoHourEvent(0, summary);
        replyingAttendee = prepareCommonAttendees(eventToCreate);
        eventToCreate.getAttendees().add(internalAttendee);
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

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            testContext.backUser(testUser2C1);
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testInternalNotificationAfterReply() throws Exception {
        MailData notification = ITipUtil.receiveNotification(apiClient2C1, userResponseC1.getData().getEmail1(), summary);
        rememberMail(apiClient2C1, notification);
        assertTrue(null != notification.getAttachment() && notification.getAttachment().booleanValue());
        assertThat(I(notification.getAttachments().size()), is(I(1)));

        MailAttachment mailAttachment = notification.getAttachments().get(0);
        assertThat(mailAttachment, is(not(nullValue())));
        assertThat(mailAttachment.getContent(), is(not(nullValue())));
        assertThat(mailAttachment.getContent(), containsStringIgnoringCase("<span class=\\\"person\\\">" + userResponseC1.getData().getDisplayName() + "</span> has changed an appointment"));
        assertThat(mailAttachment.getContent(), containsStringIgnoringCase("<span class=\\\"person\\\">" + userResponseC2.getData().getLastName() + "</span> has <span class=\\\"status declined\\\">"));
    }

}
