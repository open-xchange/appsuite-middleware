/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.chronos.itip;

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveNotification;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.models.UserResponse;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link ITipOnBehalfTest}
 *
 * Scenario:
 * User A from context 1
 * user C from context 1
 *
 * User B from context 2
 *
 * Share a calendar with another user (user C) of the same context (context 1) to check the "On behalf" functionality
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class ITipOnBehalfTest extends AbstractITipAnalyzeTest {

    private String sharedFolder;

    /** The user C's client */
    private ApiClient apiClientC1_2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        /*
         * Get another user of context 1
         */
        apiClientC1_2 = testContext.acquireUser().getApiClient();

        UserApi anotherUserApi = new UserApi(apiClientC1_2);
        UserResponse userResponseC1_2 = anotherUserApi.getUser(String.valueOf(apiClientC1_2.getUserId()));
        assertNull(userResponseC1_2.getError());

        /*
         * Create a new calendar folder to operate on
         */
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        FolderPermission permission = new FolderPermission();
        permission.setEntity(defaultUserApi.getCalUser());
        permission.setGroup(Boolean.FALSE);
        permission.setBits(I(403710016));
        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(permission);
        folder.setPermissions(permissions);
        folder.setModule("event");
        folder.setTitle(this.getClass().getSimpleName() + UUID.randomUUID().toString());
        folder.setSubscribed(Boolean.TRUE);
        body.setFolder(folder);
        sharedFolder = folderManager.createFolder(body);

        /*
         * Share calendar to another user of context 1
         */
        FolderData data = folderManager.getFolder(sharedFolder);
        FolderData folderData = new FolderData();
        permissions = new ArrayList<>(data.getPermissions());
        permission = new FolderPermission();
        permission.setBits(I(4227332));
        permission.setEntity(Integer.valueOf(userResponseC1_2.getData().getId()));
        permission.setGroup(Boolean.FALSE);
        permissions.add(permission);
        folderData.setPermissions(permissions);
        folderData.setId(sharedFolder);
        folderManager.updateFolder(folderData);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withContexts(2).withUserPerContext(3).useEnhancedApiClients().build();
    }

    @Test
    public void testOnBehalfOfInvitation() throws Exception {
        String summary = this.getClass().getSimpleName() + ".testOnBehalfOfInvitation" + UUID.randomUUID();
        EventData data = EventFactory.createSingleTwoHourEvent(getUserId(), summary, defaultFolderId);
        Attendee replyingAttendee = prepareAttendees(data);
        ChronosCalendarResultResponse response = new ChronosApi(apiClientC1_2).createEvent(sharedFolder, data, Boolean.FALSE, null, Boolean.TRUE, null, null, null, Boolean.FALSE, null);
        assertNotNull(response);
        assertNull(response.getError());
        EventData secretaryEvent = response.getData().getCreated().get(0);

        /*
         * Check event within folder of organizer
         */
        createdEvent = eventManager.getEvent(sharedFolder, secretaryEvent.getId());
        assertEquals(secretaryEvent.getUid(), createdEvent.getUid());
        assertAttendeePartStat(createdEvent.getAttendees(), testUser.getLogin(), PartStat.ACCEPTED);
        assertAttendeePartStat(createdEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION);

        /*
         * Check notification mail within organizers inbox
         *
         */
        receiveNotification(apiClient, testUser.getLogin(), summary);

        /*
         * Receive iMIP as attendee and accept
         */
        MailData iMip = receiveIMip(apiClientC2, testUser.getLogin(), summary, 0, SchedulingMethod.REQUEST);
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyzeResponse).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION);
        analyze(analyzeResponse, CustomConsumers.ACTIONS);

        EventData attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip), null), createdEvent.getUid());
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED);

        /*
         * Receive accept as organizer and update event
         */
        iMip = receiveIMip(apiClient, testUserC2.getLogin(), summary, 0, SchedulingMethod.REPLY);
        analyzeResponse = analyze(apiClient, iMip);
        newEvent = assertSingleChange(analyzeResponse).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED);
        analyze(analyzeResponse, CustomConsumers.UPDATE);

        ActionResponse update = update(constructBody(iMip));
        assertThat(update.getData(), is(notNullValue()));
        assertThat(I(update.getData().size()), is(I(1)));
        assertAttendeePartStat(update.getData().get(0).getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED);

        /*
         * Double check in the calendar folder
         */
        createdEvent = eventManager.getEvent(sharedFolder, secretaryEvent.getId());
        assertEquals(secretaryEvent.getUid(), createdEvent.getUid());
        assertAttendeePartStat(createdEvent.getAttendees(), testUser.getLogin(), PartStat.ACCEPTED);
        assertAttendeePartStat(createdEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED);
    }

}
