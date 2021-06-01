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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.HasRequest;
import com.openexchange.ajax.appointment.action.HasResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13027Test extends AbstractAJAXSession {

    public Bug13027Test() {
        super();
    }

    @Test
    public void testNegativeTimeZone() throws Exception {
        final int folderId = getClient().getValues().getPrivateAppointmentFolder();
        final TimeZone tz = TimeZone.getTimeZone("America/New York");
        String formerTimeZone = "Europe/Berlin";
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            GetRequest getRequest = new GetRequest(Tree.TimeZone);
            GetResponse getResponse = getClient().execute(getRequest);
            formerTimeZone = getResponse.getString();
            SetRequest setRequest = new SetRequest(Tree.TimeZone, "America/New_York");
            getClient().execute(setRequest);

            // Step 1
            clear(tz, folderId);

            // Step 2
            // Prepare appointment
            appointment.setTitle("Bug 13027 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            appointment.setStartDate(calendar.getTime());
            appointment.setEndDate(calendar.getTime());
            appointment.setFullTime(true);

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 3
            final HasRequest hasRequest = new HasRequest(new Date(1230508800000L), new Date(1230854400000L), tz); // 29.12.08 - 02.01.09
            final HasResponse hasResponse = getClient().execute(hasRequest);
            boolean[] values = hasResponse.getValues();
            assertFalse("No appointment expected.", values[1]); // 30.12.08
            assertFalse("No appointment expected.", values[2]); // 31.12.08
            assertTrue("Appointment expected.", values[3]); // 01.01.09

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                getClient().execute(deleteRequest);
            }

            SetRequest setRequest = new SetRequest(Tree.TimeZone, formerTimeZone);
            getClient().execute(setRequest);
        }
    }

    @Test
    public void testPositiveTimeZone() throws Exception {
        final int folderId = getClient().getValues().getPrivateAppointmentFolder();
        final TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        String formerTimeZone = "Europe/Berlin";
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            GetRequest getRequest = new GetRequest(Tree.TimeZone);
            GetResponse getResponse = getClient().execute(getRequest);
            formerTimeZone = getResponse.getString();
            SetRequest setRequest = new SetRequest(Tree.TimeZone, "Europe/Berlin");
            getClient().execute(setRequest);

            // Step 1
            clear(tz, folderId);

            // Step 2
            // Prepare appointment
            appointment.setTitle("Bug 13027 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(TimeZone.getTimeZone("UTC"));
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            appointment.setStartDate(calendar.getTime());
            appointment.setEndDate(calendar.getTime());
            appointment.setFullTime(true);

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 3
            final HasRequest hasRequest = new HasRequest(new Date(1230508800000L), new Date(1230854400000L), tz); // 29.12.08 - 02.01.09
            final HasResponse hasResponse = getClient().execute(hasRequest);
            boolean[] values = hasResponse.getValues();
            assertFalse("No appointment expected.", values[1]); // 30.12.08
            assertFalse("No appointment expected.", values[2]); // 31.12.08
            assertTrue("Appointment expected.", values[3]); // 01.01.09

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                getClient().execute(deleteRequest);
            }

            SetRequest setRequest = new SetRequest(Tree.TimeZone, formerTimeZone);
            getClient().execute(setRequest);
        }
    }

    @Test
    public void testUTC() throws Exception {
        final int folderId = getClient().getValues().getPrivateAppointmentFolder();
        final TimeZone tz = TimeZone.getTimeZone("UTC");
        String formerTimeZone = "Europe/Berlin";
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            GetRequest getRequest = new GetRequest(Tree.TimeZone);
            GetResponse getResponse = getClient().execute(getRequest);
            formerTimeZone = getResponse.getString();
            SetRequest setRequest = new SetRequest(Tree.TimeZone, "Europe/London");
            getClient().execute(setRequest);

            // Step 1
            clear(tz, folderId);

            // Step 2
            // Prepare appointment
            appointment.setTitle("Bug 13027 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            appointment.setStartDate(calendar.getTime());
            appointment.setEndDate(calendar.getTime());
            appointment.setFullTime(true);

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 3
            final HasRequest hasRequest = new HasRequest(new Date(1230508800000L), new Date(1230854400000L), tz); // 29.12.08 - 02.01.09
            final HasResponse hasResponse = getClient().execute(hasRequest);
            boolean[] values = hasResponse.getValues();
            assertFalse("No appointment expected.", values[1]); // 30.12.08
            assertFalse("No appointment expected.", values[2]); // 31.12.08
            assertTrue("Appointment expected.", values[3]); // 01.01.09

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                getClient().execute(deleteRequest);
            }

            SetRequest setRequest = new SetRequest(Tree.TimeZone, formerTimeZone);
            getClient().execute(setRequest);
        }
    }

    @Test
    public void testBugAsWritten() throws Exception {
        final int folderId = getClient().getValues().getPrivateAppointmentFolder();
        final TimeZone tz = TimeZone.getTimeZone("America/New York");
        String formerTimeZone = "Europe/Berlin";
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            GetRequest getRequest = new GetRequest(Tree.TimeZone);
            GetResponse getResponse = getClient().execute(getRequest);
            formerTimeZone = getResponse.getString();
            SetRequest setRequest = new SetRequest(Tree.TimeZone, "America/New_York");
            getClient().execute(setRequest);

            // Step 1
            clear(tz, folderId);

            // Step 2
            // Prepare appointment
            appointment.setTitle("Bug 13027 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            appointment.setStartDate(calendar.getTime());
            appointment.setEndDate(calendar.getTime());
            appointment.setFullTime(true);

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 3
            final HasRequest hasRequest = new HasRequest(new Date(1230508800000L), new Date(1230854400000L), tz); // 29.12.08 - 02.01.09
            final HasResponse hasResponse = getClient().execute(hasRequest);
            boolean[] values = hasResponse.getValues();
            assertFalse("No appointment expected.", values[1]); // 30.12.08
            assertFalse("No appointment expected.", values[2]); // 31.12.08
            assertTrue("Appointment expected.", values[3]); // 01.01.09

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                getClient().execute(deleteRequest);
            }

            SetRequest setRequest = new SetRequest(Tree.TimeZone, formerTimeZone);
            getClient().execute(setRequest);
        }
    }

    private void clear(TimeZone tz, int folderId) throws Exception {
        Calendar start = new GregorianCalendar();
        start.setTimeZone(tz);
        start.set(Calendar.YEAR, 2008);
        start.set(Calendar.MONTH, Calendar.DECEMBER);
        start.set(Calendar.DAY_OF_MONTH, 30);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = new GregorianCalendar();
        end.setTimeZone(tz);
        end.set(Calendar.YEAR, 2009);
        end.set(Calendar.MONTH, Calendar.JANUARY);
        end.set(Calendar.DAY_OF_MONTH, 2);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        AllRequest request = new AllRequest(folderId, new int[] { Appointment.OBJECT_ID, Appointment.LAST_MODIFIED }, start.getTime(), end.getTime(), tz);
        CommonAllResponse response = getClient().execute(request);

        Object[][] responseColumns = response.getArray();
        for (Object[] obj : responseColumns) {
            getClient().execute(new DeleteRequest(((Integer) obj[0]).intValue(), folderId, new Date(Long.MAX_VALUE)));
        }
    }

}
