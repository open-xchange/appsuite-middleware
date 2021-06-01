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
import static org.junit.Assert.assertNull;
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
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug57313Test}
 *
 * Broken appointment
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug57313Test extends CalDAVTest {

    @Test
    public void testDeleteChangeException() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create appointment series on server
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("last week at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug57313Test");
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setParentFolderID(catm.getPrivateFolder());
        catm.insert(appointment);
        Date clientLastModified = catm.getLastModification();
        /*
         * create a change exception on server
         */
        Appointment exception = new Appointment();
        exception.setTitle("Bug57313Test_exception");
        exception.setObjectID(appointment.getObjectID());
        exception.setRecurrencePosition(3);
        exception.setLastModified(clientLastModified);
        exception.setParentFolderID(appointment.getParentFolderID());
        exception.setIgnoreConflicts(true);
        catm.update(exception);
        clientLastModified = catm.getLastModification();
        /*
         * verify series & exception on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertEquals("unexpected number of VEVENTs", 2, iCalResource.getVEvents().size());
        Component vEventException = null;
        Component vEventSeries = null;
        for (Component vEventComponent : iCalResource.getVEvents()) {
            if (uid.equals(vEventComponent.getUID()) && exception.getTitle().equals(vEventComponent.getSummary())) {
                vEventException = vEventComponent;
            }
            if (uid.equals(vEventComponent.getUID()) && appointment.getTitle().equals(vEventComponent.getSummary())) {
                vEventSeries = vEventComponent;
            }
        }
        assertNotNull("No VEVENT for change exception in iCal found", vEventException);
        assertNotNull("No VEVENT for series master in iCal found", vEventSeries);
        /*
         * delete the change exception on client
         */
        iCalResource.getVCalendar().getComponents().remove(vEventException);
        Property recurrenceIdProperty = vEventException.getProperty("RECURRENCE-ID");
        vEventSeries.setProperty("EXDATE", recurrenceIdProperty.getValue(), recurrenceIdProperty.getAttributes());
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify series & exception on server
         */
        Appointment seriesAppointment = catm.get(appointment);
        assertNotNull("appointment not found on server", seriesAppointment);
        assertTrue("Unexpected change exception dates", null == seriesAppointment.getChangeException() || 0 == seriesAppointment.getChangeException().length);
        assertTrue("Unexpected delete exception dates", null != seriesAppointment.getDeleteException() && 1 == seriesAppointment.getDeleteException().length);
        catm.setFailOnError(false);
        assertNull("Change exception still found", catm.get(exception));
        catm.setFailOnError(true);
    }

}
