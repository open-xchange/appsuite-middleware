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
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug23167Test}
 *
 * Unable to calculate given position. Seems to be a delete exception or outside range
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug23167Test extends CalDAVTest {

    @Test
    public void testUpdateOldChangeException() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create appointment series on server
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("last week in the morning", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(randomUID());
        appointment.setTitle("Bug23167Test");
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(1 << (calendar.get(Calendar.DAY_OF_WEEK) - 1));
        appointment.setInterval(1);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        appointment.setEndDate(calendar.getTime());
        super.create(appointment);
        Date clientLastModified = getManager().getLastModification();
        /*
         * create appointment exception on server
         */
        Appointment exception = new Appointment();
        exception.setTitle("Bug23167Test_edit");
        exception.setObjectID(appointment.getObjectID());
        exception.setRecurrencePosition(2);
        exception.setLastModified(clientLastModified);
        exception.setParentFolderID(appointment.getParentFolderID());
        super.getManager().update(exception);
        clientLastModified = getManager().getLastModification();
        /*
         * verify appointment series on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("No exception found in iCal", 2, iCalResource.getVEvents().size());
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
        assertNotNull(vEventException);
        /*
         * update exception on client
         */
        vEventException.setProperty("SUMMARY", vEventException.getPropertyValue("SUMMARY") + "_edit2");
        vEventException.setProperty("DTSTAMP", formatAsUTC(new Date()));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify exception on server
         */
        List<Appointment> updates = super.getManager().updates(parse(getDefaultFolderID()), clientLastModified, true);
        assertNotNull("no updates found on server", updates);
        assertTrue("no updated appointments on server", 0 < updates.size());
        exception = null;
        for (Appointment update : updates) {
            if (appointment.getObjectID() != update.getObjectID() && appointment.getUid().equals(update.getUid())) {
                exception = update;
                break;
            }
        }
        assertNotNull("Exception not found", exception);
        assertEquals("Title wrong", vEventException.getSummary(), exception.getTitle());
    }

}
