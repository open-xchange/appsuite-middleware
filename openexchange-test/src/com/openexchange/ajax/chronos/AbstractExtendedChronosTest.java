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

import org.junit.After;
import org.junit.Before;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
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

        apiClient2 = generateApiClient(testUser2);
        rememberClient(apiClient2);
        EnhancedApiClient enhancedClient = generateEnhancedClient(testUser2);
        rememberClient(enhancedClient);
        userApi2 = new UserApi(apiClient2, enhancedClient, testUser2, true);

        folderId2 = getDefaultFolder(userApi2.getSession(), apiClient2);
        eventManager2 = new EventManager(userApi2, folderId2);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        eventManager2.cleanUp();
        super.tearDown();
    }

    // ----------------------------- HELPER -----------------------------

    /**
     * Create an attendee our of given user
     * 
     * @param userId The identifier of the user
     * @return an {@link Attendee}
     * @throws ApiException If creating fails
     */
    protected Attendee createAttendee(int userId) throws ApiException {
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
    protected UserData getUserInformation(int userId) throws ApiException {
        com.openexchange.testing.httpclient.modules.UserApi api = new com.openexchange.testing.httpclient.modules.UserApi(getApiClient());
        UserResponse userResponse = api.getUser(getApiClient().getSession(), String.valueOf(userId));
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
