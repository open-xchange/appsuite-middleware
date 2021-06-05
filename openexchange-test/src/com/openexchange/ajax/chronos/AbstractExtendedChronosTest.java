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

package com.openexchange.ajax.chronos;

import org.junit.Before;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.tools.client.EnhancedApiClient;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.models.UserResponse;

/**
 * {@link AbstractExtendedChronosTest} - Extend with second {@link EventManager}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public class AbstractExtendedChronosTest extends AbstractChronosTest {

    
    protected ApiClient apiClient2;

    protected UserApi userApi2;

    protected EventManager eventManager2;

    protected String folderId2;

    /**
     * Initializes a new {@link AbstractExtendedChronosTest}.
     */
    public AbstractExtendedChronosTest() {
        super();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        apiClient2 = testUser2.getApiClient();
        EnhancedApiClient enhancedClient = getEnhancedApiClient2();
        userApi2 = new UserApi(apiClient2, enhancedClient, testUser2);

        folderId2 = getDefaultFolder(apiClient2);
        eventManager2 = new EventManager(userApi2, folderId2);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(2).useEnhancedApiClients().build();
    }

    // ----------------------------- HELPER -----------------------------

    /**
     * Create an attendee our of given user
     *
     * @param userId The identifier of the user
     * @return an {@link Attendee}
     * @throws ApiException If creating fails
     */
    protected Attendee createAttendee(Integer userId) throws ApiException {
        Attendee attendee = AttendeeFactory.createAttendee(userId, CuTypeEnum.INDIVIDUAL);

        UserData userData = getUserInformation(userId);

        attendee.cn(userData.getDisplayName());
        attendee.email(userData.getEmail1());
        attendee.setUri("mailto:" + userData.getEmail1());
        attendee.entity(Integer.valueOf(userData.getId()));
        return attendee;
    }

    /**
     * Get the user information for the specified user
     *
     * @param userId The identifier of the user
     * @return The {@link UserData} for the suer
     * @throws ApiException
     */
    protected UserData getUserInformation(Integer userId) throws ApiException {
        com.openexchange.testing.httpclient.modules.UserApi api = new com.openexchange.testing.httpclient.modules.UserApi(getApiClient());
        UserResponse userResponse = api.getUser(String.valueOf(userId));
        return userResponse.getData();
    }

    /**
     * Prepares an new (delta) event out of the current event.
     * Sets recurrence ID and the attendees. See {@link #prepareEventUpdate(EventData)}, too.
     *
     * @param occurrence An existing event occurrence
     * @return A new (delta) {@link EventData}
     */
    protected EventData prepareException(EventData occurrence) {
        EventData exception = prepareEventUpdate(occurrence);
        exception.setSummary("Changed summary");
        exception.setRecurrenceId(occurrence.getRecurrenceId());
        exception.setAttendees(occurrence.getAttendees());
        return exception;
    }

    /**
     * Prepares a new (delta) event out of the current event
     * Sets the ID and the last modified timespamp
     *
     * @param data An existing event
     * @return A new (delta) {@link EventData}
     */
    protected EventData prepareEventUpdate(EventData data) {
        EventData eventUpdate = new EventData();
        eventUpdate.setId(data.getId());
        eventUpdate.setLastModified(Long.valueOf(System.currentTimeMillis()));
        return eventUpdate;
    }

}
