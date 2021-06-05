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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractSecondUserChronosTest;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.FolderPermission;

/**
 * Checks if a changed appointment in a shared folder looses all its participants.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public final class Bug10154Test extends AbstractSecondUserChronosTest {

    /**
     * @param name test name.
     */
    public Bug10154Test() {
        super();
    }

    /**
     * A creates a shared folder and an appointment with participants. B changes
     * the participant in the folder and A verifies if its participants get lost.
     */
    @Test
    public void testParticipantsLost() throws Throwable {
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

        EventData eventData = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Test for bug 10154", sharedFolder);
        List<Attendee> attendees = eventData.getAttendees();
        attendees.add(AttendeeFactory.createIndividual(userApi2.getCalUser()));
        eventData.setAttendees(attendees);
        EventData createEvent = eventManager.createEvent(eventData, true);
        assertEquals(2, createEvent.getAttendees().size());

        EventData update = new EventData();
        update.setId(createEvent.getId());
        update.setFolder(createEvent.getFolder());
        update.setStartDate(DateTimeUtil.incrementDateTimeData(createEvent.getStartDate(), TimeUnit.HOURS.toMillis(1)));
        update.setEndDate(DateTimeUtil.incrementDateTimeData(createEvent.getEndDate(), TimeUnit.HOURS.toMillis(1)));
        update.setAttendees(null);

        eventManager2.getEvent(sharedFolder, createEvent.getId()); // update timestamp info
        eventManager2.updateEvent(update);

        EventData event = eventManager.getEvent(sharedFolder, createEvent.getId());
        List<Attendee> atts = event.getAttendees();
        assertNotNull(atts);
        assertEquals(2, atts.size());
        assertTrue(atts.get(0).getEntity().equals(defaultUserApi.getCalUser()) || atts.get(0).getEntity().equals(userApi2.getCalUser()));
        assertTrue(atts.get(1).getEntity().equals(defaultUserApi.getCalUser()) || atts.get(1).getEntity().equals(userApi2.getCalUser()));
    }
}
