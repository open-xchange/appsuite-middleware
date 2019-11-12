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

package com.openexchange.ajax.chronos.itip;

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.ComposeBody;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailComposeMessageModel;
import com.openexchange.testing.httpclient.models.MailComposeResponse;
import com.openexchange.testing.httpclient.models.MailComposeSendResponse;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.modules.MailComposeApi;

/**
 * {@link ITipAnalyzeReplyTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ITipAnalyzeReplyTest extends AbstractITipAnalyzeTest {

    private String summary;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.summary = "ITipAnalyzeReplyTest" + UUID.randomUUID().toString();
    }

    @Test
    public void testSimple() throws Exception {
        EventData eventToCreate = EventFactory.createSingleTwoHourEvent(0, summary);
        Attendee replyingAttendee = prepareCommonAttendees(eventToCreate);
        createdEvent = createEvent(eventToCreate);

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
        EventData eventData = assertSingleEvent(accept(apiClientC2, constructBody(iMip)), createdEvent.getUid());
        assertAttendeePartStat(eventData.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.status);

        /*
         * Receive mail as organizer and check actions
         */
        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 0, SchedulingMethod.REPLY);
        analyze(reply.getId());
        rememberMail(reply);
    }

    @Test
    public void testPartyCrasher() throws Exception {
        EventData eventToCreate = EventFactory.createSingleTwoHourEvent(0, summary);
        Attendee replyingAttendee = prepareCommonAttendees(eventToCreate);
        createdEvent = createEvent(eventToCreate);

        /*
         * Receive mail as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClientC2, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.status);

        /*
         * Forward to other user of same context
         */
        TestUser partyCrasher = context2.acquireUser();
        addTearDownOperation(() -> {
            context2.backUser(partyCrasher);
        });
        String crashersMail = partyCrasher.getLogin();

        /*
         * Receive mail
         */
        MailComposeApi mailComposeApi = new MailComposeApi(apiClientC2);
        ComposeBody composeBody = new ComposeBody();
        composeBody.setId(iMip.getId());
        composeBody.setFolderId(ITipUtil.FOLDER_HUMAN_READABLE);
        MailComposeResponse mailCompose = mailComposeApi.postMailCompose(apiClientC2.getSession(), "forward", Boolean.FALSE, Collections.singletonList(composeBody));
        assertNull(mailCompose.getError());

        /*
         * Set to new recipient
         */
        MailComposeMessageModel data = mailCompose.getData();
        ArrayList<String> toList = new ArrayList<>(2);
        toList.add(partyCrasher.getUser());
        toList.add(crashersMail);
        ArrayList<String> fromList = new ArrayList<>(2);
        fromList.add(userResponseC2.getData().getDisplayName());
        fromList.add(userResponseC2.getData().getEmail1());
        data.setTo(Collections.singletonList(toList));
        data.setFrom(fromList);
        MailComposeSendResponse forwardedMail = mailComposeApi.postMailComposeSend(apiClientC2.getSession(), data.getId(), data.toJson());
        assertNull(forwardedMail.getErrorDesc());

        /*
         * Receive forwarded mail as party crasher
         */
        ApiClient apiClient3 = generateApiClient(partyCrasher);
        rememberClient(apiClient3);
        iMip = receiveIMip(apiClient3, userResponseC2.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST);
        rememberMail(apiClient3, iMip);

        newEvent = assertSingleChange(analyze(apiClient3, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.status);

        /*
         * reply with "accepted" as party crasher
         */
        EventData eventData = assertSingleEvent(accept(apiClient3, constructBody(iMip)), createdEvent.getUid());
        assertAttendeePartStat(eventData.getAttendees(), crashersMail, PartStat.ACCEPTED.status);
        rememberForCleanup(apiClient3, eventData);

        /*
         * Receive mail as organizer and check actions for party crasher
         */
        MailData reply = receiveIMip(apiClient, crashersMail, summary, 0, SchedulingMethod.REPLY);
        analyze(reply.getId(), CustomConsumers.PARTY_CRASHER);
        rememberMail(reply);
    }

}
