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

package com.openexchange.ajax.importexport;

import java.io.StringReader;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.common.data.conversion.ical.Assert;
import com.openexchange.test.common.data.conversion.ical.ICalFile;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11871Test extends AbstractAJAXSession {

    /**
     * Creates a daily appointment over several days and checks if the DTEND
     * contains the end of the first occurrence and not the end of the series.
     */
    @Test
    public void testDtEndNotOnSeriesEnd() throws Throwable {
        final AJAXClient myClient = getClient();
        final int folderId = myClient.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = myClient.getValues().getTimeZone();
        final FolderObject folder = Create.createPrivateFolder("Bug 11871 test folder " + UUID.randomUUID().toString(), FolderObject.CALENDAR, myClient.getValues().getUserId());
        {
            folder.setParentFolderID(folderId);
            final CommonInsertResponse response = Executor.execute(myClient, new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
            folder.setObjectID(response.getId());
            folder.setLastModified(response.getTimestamp());
        }
        final Appointment appointment = new Appointment();
        final Calendar calendar = Calendar.getInstance(tz);
        {
            appointment.setTitle("Bug 11871 test appointment");
            appointment.setParentFolderID(folder.getObjectID());
            appointment.setIgnoreConflicts(true);
            appointment.setRecurrenceType(Appointment.DAILY);
            appointment.setInterval(1);
            appointment.setRecurrenceCount(5);
            appointment.setTimezone(tz.getID());

            calendar.set(Calendar.YEAR, 2008);
            calendar.set(Calendar.MONTH, Calendar.JULY);
            calendar.set(Calendar.DAY_OF_MONTH, 26);
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            appointment.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 19);
            appointment.setEndDate(calendar.getTime());
            calendar.set(Calendar.MONTH, Calendar.AUGUST);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            appointment.setUntil(calendar.getTime());
            final InsertRequest request = new InsertRequest(appointment, tz);
            final CommonInsertResponse response = Executor.execute(myClient, request);
            appointment.setObjectID(response.getId());
            appointment.setLastModified(response.getTimestamp());
        }
        try {
            final ICalExportResponse response = myClient.execute(new ICalExportRequest(folder.getObjectID()));
            final ICalFile ical = new ICalFile(new StringReader(response.getICal()));
            Assert.assertStandardAppFields(ical, appointment.getStartDate(), appointment.getEndDate(), tz);
        } finally {
            Executor.execute(myClient, new DeleteRequest(appointment.getObjectID(), appointment.getParentFolderID(), appointment.getLastModified()));
            Executor.execute(myClient, new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(), folder.getLastModified()));
        }
    }
}
