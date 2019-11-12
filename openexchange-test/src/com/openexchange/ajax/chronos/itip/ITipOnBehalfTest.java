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
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.models.UserResponse;
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

    /** The user C from context 1 */
    private TestUser testUserC1_2;
    /** The user C's client */
    private ApiClient apiClientC1_2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        /*
         * Get another user of context 1
         */
        testUserC1_2 = testContext.acquireUser();
        addTearDownOperation(() -> {
            context2.backUser(testUserC1_2);
        });
        apiClientC1_2 = generateApiClient(testUserC1_2);
        rememberClient(apiClientC1_2);

        UserApi anotherUserApi = new UserApi(apiClientC1_2);
        UserResponse userResponseC1_2 = anotherUserApi.getUser(apiClientC1_2.getSession(), String.valueOf(apiClientC1_2.getUserId()));
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
        folder.setTitle("Shared for " + this.getClass().getSimpleName());
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

    @Test
    public void testOnBehalfOfInvitation() throws Exception {
        String summary = this.getClass().getSimpleName() + ".testOnBehalfOfInvitation";
        EventData data = EventFactory.createSingleTwoHourEvent(getUserId(), summary, defaultFolderId);
        Attendee replyingAttendee = prepareAttendees(data);
        EventData secretaryEvent = createEvent(apiClientC1_2, data, sharedFolder);

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
        rememberMail(receiveIMip(apiClient, testUser.getLogin(), summary, 0, null));

        /*
         * Receive iMIP as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, testUser.getLogin(), summary, 0, SchedulingMethod.REQUEST);
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyzeResponse).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION);
        analyze(analyzeResponse, CustomConsumers.ACTIONS);

        EventData attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip)), createdEvent.getUid());
        rememberForCleanup(apiClientC2, attendeeEvent);
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED);

        /*
         * Receive accept as organizer
         */
        iMip = receiveIMip(apiClient, testUserC2.getLogin(), summary, 0, SchedulingMethod.REPLY);
        rememberMail(iMip);
        analyzeResponse = analyze(apiClient, iMip);
        newEvent = assertSingleChange(analyzeResponse).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED);
        analyze(analyzeResponse, CustomConsumers.UPDATE);
    }

}
