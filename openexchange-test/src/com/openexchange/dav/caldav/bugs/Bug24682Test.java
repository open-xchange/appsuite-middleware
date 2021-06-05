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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.Abstract2UserCalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug24682Test}
 *
 * Change excpetions in shared folders sometimes can't be loaded
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug24682Test extends Abstract2UserCalDAVTest {

    private CalendarTestManager[] managers;

    CalendarTestManager userA, userB, userC, userD;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * setup managers for other users
         */
        managers = new CalendarTestManager[3];
        managers[0] = new CalendarTestManager(client2);
        managers[1] = new CalendarTestManager(testContext.acquireUser().getAjaxClient());
        managers[2] = new CalendarTestManager(testContext.acquireUser().getAjaxClient());
        for (CalendarTestManager manager : managers) {
            manager.setFailOnError(true);
        }
        userA = getManager();
        userB = managers[0];
        userC = managers[1];
        userD = managers[2];
        /*
         * As user C, share your calendar to users B and A
         */
        FolderTools.shareFolder(userC.getClient(), EnumAPI.OX_NEW, userC.getPrivateFolder(), userA.getClient().getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        FolderTools.shareFolder(userC.getClient(), EnumAPI.OX_NEW, userC.getPrivateFolder(), userB.getClient().getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
    }

    @Test
    public void testGetChangeExceptionsInSharedFolder() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken(String.valueOf(userC.getPrivateFolder())));
        /*
         * As user B, create a recurring appointment in user C's calendar, inviting users D, E and F
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("Last week at 2pm", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(userC.getPrivateFolder());
        appointment.setUid(randomUID());
        appointment.setTitle(getClass().getName());
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setInterval(1);
        appointment.addParticipant(new UserParticipant(userD.getClient().getValues().getUserId()));
        appointment.setDays(2 ^ (calendar.get(Calendar.DAY_OF_WEEK) - 1));
        appointment = userB.insert(appointment);
        /*
         * As user A, synchronize via iCal
         */
        Map<String, String> eTags = super.syncCollection(syncToken, String.valueOf(userC.getPrivateFolder())).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(String.valueOf(userC.getPrivateFolder()), eTags.keySet());
        ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", appointment.getTitle(), iCalResource.getVEvent().getSummary());
        /*
         * As user B, create a change excpetion of the appoointment series created in step 2
         */
        Appointment exception = userB.createIdentifyingCopy(appointment);
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        exception.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        exception.setEndDate(calendar.getTime());
        exception.setTitle(appointment.getTitle() + "_edit");
        exception.setRecurrencePosition(2);
        userB.update(exception);
        /*
         * As user A, synchronize via iCal
         */
        eTags = super.syncCollection(syncToken, String.valueOf(userC.getPrivateFolder())).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = super.calendarMultiget(String.valueOf(userC.getPrivateFolder()), eTags.keySet());
        iCalResource = assertContains(appointment.getUid(), calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Component vEventException = null;
        for (Component vEvent : iCalResource.getVEvents()) {
            Date recurrenceID = vEvent.getRecurrenceID();
            if (null != recurrenceID) {
                // exception
                assertEquals("SUMMARY wrong", exception.getTitle(), vEvent.getSummary());
                vEventException = vEvent;
            } else {
                // master
                assertEquals("SUMMARY wrong", appointment.getTitle(), vEvent.getSummary());
            }
        }
        assertNotNull("No exception found on client", vEventException);
    }

}
