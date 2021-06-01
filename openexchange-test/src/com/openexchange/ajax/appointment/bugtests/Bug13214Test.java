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

package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13214Test extends AbstractAJAXSession {

    public Bug13214Test() {
        super();
    }

    @Test
    public void testBugAsWritten() throws Exception {
        final int folderId = getClient().getValues().getPrivateAppointmentFolder();
        final TimeZone tz = getClient().getValues().getTimeZone();
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            // Step 1
            // Prepare appointment
            appointment.setTitle("Bug 13214 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 30);
            appointment.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            appointment.setEndDate(calendar.getTime());

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 2
            // Prepare update appointment
            Appointment updateAppointment = new Appointment();
            updateAppointment.setObjectID(objectId);
            updateAppointment.setParentFolderID(folderId);
            updateAppointment.setIgnoreConflicts(true);
            updateAppointment.setLastModified(lastModified);
            calendar.set(Calendar.HOUR_OF_DAY, 11);
            updateAppointment.setStartDate(calendar.getTime());

            // Update
            UpdateRequest updateRequest = new UpdateRequest(updateAppointment, tz, false);
            UpdateResponse updateResponse = getClient().execute(updateRequest);

            try {
                assertTrue("No Exception occurred.", updateResponse.hasError());
                OXException e = updateResponse.getException();
                assertTrue("Wrong Exception", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
            } finally {
                if (!updateResponse.hasError()) {
                    updateAppointment.setLastModified(updateResponse.getTimestamp());
                    lastModified = updateAppointment.getLastModified();
                }
            }

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                getClient().execute(deleteRequest);
            }
        }
    }
}
