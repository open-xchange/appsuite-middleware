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

package com.openexchange.ajax.chronos.bugs;

import static com.openexchange.ajax.chronos.manager.EventManager.filterEventBySummary;
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractSecondUserChronosTest;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.test.common.tools.client.EnhancedApiClient;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.FolderPermission;

/**
 * Checks if series gets changed_from set to 0.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12099Test extends AbstractSecondUserChronosTest {

    /**
     * Default constructor.
     *
     * @param name test name.
     */
    public Bug12099Test() {
        super();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(3).useEnhancedApiClients().build();
    }

    /**
     * Creates a series appointment. Deletes one occurrence and checks if series
     * then has the changed_from set to 0.
     */
    @Test
    public void testSeriesChangedFromIsZero() throws Throwable {
        String summary = "Bug12099Test";
        EventData event = EventFactory.createSeriesEvent(getCalendaruser(), summary, 2, folderId);
        EventData createEvent = eventManager.createEvent(event, true);

        Date start = DateTimeUtil.parseDateTime(event.getStartDate());
        Date end = DateTimeUtil.parseDateTime(event.getEndDate());

        List<EventData> allEvents = eventManager.getAllEvents(folderId, start, new Date(end.getTime() + TimeUnit.DAYS.toMillis(2)), true);
        assertEquals(2, filterEventBySummary(allEvents, summary).size());

        EventId id = new EventId();
        id.setId(createEvent.getId());
        id.setFolder(folderId);
        id.setRecurrenceId(allEvents.get(1).getRecurrenceId());
        eventManager.deleteEvent(id);

        EventData updatedEvent = eventManager.getEvent(folderId, createEvent.getId());
        assertNotNull("Missing modified by", updatedEvent.getModifiedBy());
        assertEquals(defaultUserApi.getCalUser(), updatedEvent.getModifiedBy().getEntity());
    }

    /**
     * A shares his calendar to B with create rights. B creates a series
     * appointment there with C as participant. C deletes an occurrence of that
     * series appointment. A verifies that changed_from of the series is not
     * zero.
     */
    @Test
    public void testSeriesChangedFromIsZero2() throws Throwable {
        List<FolderPermission> permissions = new ArrayList<>();
        // User A
        FolderPermission perm = new FolderPermission();
        perm.setEntity(I(getCalendaruser()));
        perm.setGroup(Boolean.FALSE);
        perm.setBits(I(403710016));
        permissions.add(perm);
        // User B
        perm = new FolderPermission();
        perm.setEntity(userApi2.getCalUser());
        perm.setGroup(Boolean.FALSE);
        perm.setBits(I(4227332));
        permissions.add(perm);
        String sharedFolder = createAndRememberNewFolder(defaultUserApi, folderId, permissions);

        TestUser testUser3 = testContext.acquireUser();
        EnhancedApiClient enhancedApiClient3 = (EnhancedApiClient) testUser3.getApiClient();
        UserApi userApi3 = new UserApi(testUser3.getApiClient(), enhancedApiClient3, testUser3);
        EventManager eventManager3 = new EventManager(userApi3, sharedFolder);

        String summary = "Bug12099Test";
        EventData eventData = EventFactory.createSeriesEvent(userApi2.getCalUser().intValue(), summary, 2, sharedFolder);
        List<Attendee> attendees = eventData.getAttendees();
        attendees.add(AttendeeFactory.createIndividual(userApi3.getCalUser()));
        eventData.setAttendees(attendees);
        EventData createEvent = eventManager2.createEvent(eventData, true);
        assertEquals(3, createEvent.getAttendees().size()); // The two configured attendees + the user owning the folder

        Date from = DateTimeUtil.parseDateTime(eventData.getStartDate());
        Date until = new Date(DateTimeUtil.parseDateTime(eventData.getEndDate()).getTime() + TimeUnit.DAYS.toMillis(1));

        String defaultFolder3 = getDefaultFolder(testUser3.getApiClient());
        List<EventData> allEvents = eventManager3.getAllEvents(from, until, true, defaultFolder3);
        assertEquals(2, allEvents.stream().filter(e -> e.getSummary().equals(summary)).collect(Collectors.toList()).size());

        EventId id = new EventId();
        id.setFolder(defaultFolder3);
        id.setId(createEvent.getId());
        id.setRecurrenceId(allEvents.get(1).getRecurrenceId());
        eventManager3.deleteEvent(id);

        EventData updatedEvent = eventManager2.getEvent(sharedFolder, createEvent.getId());
        assertNotEquals(createEvent.getLastModified(), updatedEvent.getLastModified());
        assertNotNull("Missing modified by", updatedEvent.getModifiedBy());
        assertEquals(userApi3.getCalUser(), updatedEvent.getModifiedBy().getEntity());
    }
}
