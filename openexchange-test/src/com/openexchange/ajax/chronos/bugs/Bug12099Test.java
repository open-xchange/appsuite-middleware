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

package com.openexchange.ajax.chronos.bugs;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractSecondUserChronosTest;
import com.openexchange.ajax.chronos.EnhancedApiClient;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
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

    /**
     * Creates a series appointment. Deletes one occurrence and checks if series
     * then has the changed_from set to 0.
     */
    @Test
    public void testSeriesChangedFromIsZero() throws Throwable {
        EventData event = EventFactory.createSeriesEvent(getCalendaruser(), "Bug12099Test", 2, folderId);
        EventData createEvent = eventManager.createEvent(event);

        Date start = DateTimeUtil.parseDateTime(event.getStartDate());
        Date end = DateTimeUtil.parseDateTime(event.getEndDate());

        List<EventData> allEvents = eventManager.getAllEvents(folderId, start, new Date(end.getTime() + TimeUnit.DAYS.toMillis(2)), true);
        assertEquals(2, allEvents.size());

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
        String sharedFolder = createAndRememberNewFolder(defaultUserApi, getSessionId(), folderId, getCalendaruser(), permissions);

        TestUser testUser3 = testContext.acquireUser();
        ApiClient apiClient3 = generateApiClient(testUser3);
        rememberClient(apiClient3);
        EnhancedApiClient enhancedApiClient3 = generateEnhancedClient(testUser3);
        rememberClient(enhancedApiClient3);
        UserApi userApi3 = new UserApi(apiClient3, enhancedApiClient3, testUser3, true);
        EventManager eventManager3 = new EventManager(userApi3, sharedFolder);

        EventData eventData = EventFactory.createSeriesEvent(userApi2.getCalUser().intValue(), "Bug12099Test", 2, sharedFolder);
        List<Attendee> attendees = eventData.getAttendees();
        attendees.add(AttendeeFactory.createIndividual(userApi3.getCalUser()));
        eventData.setAttendees(attendees);
        EventData createEvent = eventManager2.createEvent(eventData, true);
        assertEquals(3, createEvent.getAttendees().size()); // The two configured attendees + the user owning the folder

        Date from = DateTimeUtil.parseDateTime(eventData.getStartDate());
        Date until = new Date(DateTimeUtil.parseDateTime(eventData.getEndDate()).getTime() + TimeUnit.DAYS.toMillis(1));

        String defaultFolder3 = getDefaultFolder(userApi3.getSession(), apiClient3);
        List<EventData> allEvents = eventManager3.getAllEvents(from, until, true, defaultFolder3);
        assertEquals(2, allEvents.size());

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
