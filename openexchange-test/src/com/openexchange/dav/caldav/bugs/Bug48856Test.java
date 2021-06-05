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
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.Abstract2UserCalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug48856Test}
 *
 * Unable to update detached occurrence via CalDAV if there are at least two
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class Bug48856Test extends Abstract2UserCalDAVTest {

    private CalendarTestManager manager2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager2 = new CalendarTestManager(client2);
        manager2.setFailOnError(true);
    }

    @Test
    public void testUpdateDetachedOccurrences() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create appointment series on server as user b
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("last week at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug48856Test");
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new UserParticipant(manager2.getClient().getValues().getUserId()));
        appointment.setParentFolderID(manager2.getPrivateFolder());
        manager2.insert(appointment);
        Date clientLastModified = manager2.getLastModification();
        /*
         * create two change exceptions on server as user b, and invite user a there
         */
        Appointment exception1 = new Appointment();
        exception1.setTitle("Bug48856Test_exception1");
        exception1.setObjectID(appointment.getObjectID());
        exception1.setRecurrencePosition(3);
        exception1.setLastModified(clientLastModified);
        exception1.setParentFolderID(appointment.getParentFolderID());
        exception1.addParticipant(new UserParticipant(manager2.getClient().getValues().getUserId()));
        exception1.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        exception1.setIgnoreConflicts(true);
        manager2.update(exception1);
        clientLastModified = manager2.getLastModification();
        Appointment exception2 = new Appointment();
        exception2.setTitle("Bug48856Test_exception2");
        exception2.setObjectID(appointment.getObjectID());
        exception2.setRecurrencePosition(4);
        exception2.setLastModified(clientLastModified);
        exception2.setParentFolderID(appointment.getParentFolderID());
        exception2.addParticipant(new UserParticipant(manager2.getClient().getValues().getUserId()));
        exception2.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        exception2.setIgnoreConflicts(true);
        manager2.update(exception2);
        clientLastModified = manager2.getLastModification();
        /*
         * verify appointment exceptions on client as user a
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertEquals("unexpected number of VEVENTs", 2, iCalResource.getVEvents().size());
        Component vEventException1 = null;
        Component vEventException2 = null;
        for (Component vEventComponent : iCalResource.getVEvents()) {
            if (uid.equals(vEventComponent.getUID()) && exception1.getTitle().equals(vEventComponent.getSummary())) {
                vEventException1 = vEventComponent;
            }
            if (uid.equals(vEventComponent.getUID()) && exception2.getTitle().equals(vEventComponent.getSummary())) {
                vEventException2 = vEventComponent;
            }
        }
        assertNotNull("No VEVENT for first occurrence in iCal found", vEventException1);
        Property attendeeInException1 = vEventException1.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendeeInException1);
        assertTrue("PARTSTAT wrong", null == attendeeInException1.getAttribute("PARTSTAT") || "NEEDS-ACTION".equals(attendeeInException1.getAttribute("PARTSTAT")));
        assertNotNull("No VEVENT for second occurrence in iCal found", vEventException2);
        Property attendeeInException2 = vEventException2.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendeeInException2);
        assertTrue("PARTSTAT wrong", null == attendeeInException2.getAttribute("PARTSTAT") || "NEEDS-ACTION".equals(attendeeInException2.getAttribute("PARTSTAT")));
        /*
         * "accept" the second occurrence on client
         */
        attendeeInException2.getAttributes().put("PARTSTAT", "ACCEPTED");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment exceptions on client as user a
         */
        eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = calendarMultiget(eTags.keySet());
        iCalResource = assertContains(uid, calendarData);
        assertEquals("unexpected number of VEVENTs", 2, iCalResource.getVEvents().size());
        vEventException1 = null;
        vEventException2 = null;
        for (Component vEventComponent : iCalResource.getVEvents()) {
            if (uid.equals(vEventComponent.getUID()) && exception1.getTitle().equals(vEventComponent.getSummary())) {
                vEventException1 = vEventComponent;
            }
            if (uid.equals(vEventComponent.getUID()) && exception2.getTitle().equals(vEventComponent.getSummary())) {
                vEventException2 = vEventComponent;
            }
        }
        assertNotNull("No VEVENT for first occurrence in iCal found", vEventException1);
        attendeeInException1 = vEventException1.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendeeInException1);
        assertTrue("PARTSTAT wrong", null == attendeeInException1.getAttribute("PARTSTAT") || "NEEDS-ACTION".equals(attendeeInException1.getAttribute("PARTSTAT")));
        assertNotNull("No VEVENT for second occurrence in iCal found", vEventException2);
        attendeeInException2 = vEventException2.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendeeInException2);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendeeInException2.getAttribute("PARTSTAT"));
        /*
         * "accept" the first occurrence on client
         */
        attendeeInException1.getAttributes().put("PARTSTAT", "ACCEPTED");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment exceptions on client as user a
         */
        eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = calendarMultiget(eTags.keySet());
        iCalResource = assertContains(uid, calendarData);
        assertEquals("unexpected number of VEVENTs", 2, iCalResource.getVEvents().size());
        vEventException1 = null;
        vEventException2 = null;
        for (Component vEventComponent : iCalResource.getVEvents()) {
            if (uid.equals(vEventComponent.getUID()) && exception1.getTitle().equals(vEventComponent.getSummary())) {
                vEventException1 = vEventComponent;
            }
            if (uid.equals(vEventComponent.getUID()) && exception2.getTitle().equals(vEventComponent.getSummary())) {
                vEventException2 = vEventComponent;
            }
        }
        assertNotNull("No VEVENT for first occurrence in iCal found", vEventException1);
        attendeeInException1 = vEventException1.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendeeInException1);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendeeInException1.getAttribute("PARTSTAT"));
        assertNotNull("No VEVENT for second occurrence in iCal found", vEventException2);
        attendeeInException2 = vEventException2.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendeeInException2);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendeeInException2.getAttribute("PARTSTAT"));
    }

}
