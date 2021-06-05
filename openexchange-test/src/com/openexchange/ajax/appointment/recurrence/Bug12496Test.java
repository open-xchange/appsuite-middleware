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

package com.openexchange.ajax.appointment.recurrence;

import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * Verifies the until date of a full time daily series appointment.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12496Test extends AbstractAJAXSession {

    private TimeZone tz;

    private int folderId;

    private Appointment appointment;

    /**
     * Default constructor.
     * 
     * @param name test name.
     */
    public Bug12496Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final AJAXClient client = getClient();
        tz = client.getValues().getTimeZone();
        folderId = client.getValues().getPrivateAppointmentFolder();
        appointment = createAppointment();
        final InsertRequest request = new InsertRequest(appointment, tz);
        final AppointmentInsertResponse response = client.execute(request);
        response.fillAppointment(appointment);
    }

    @Test
    public void testDailyFullTimeUntil() throws Throwable {
        final AJAXClient client = getClient();
        final Appointment changed = changeAppointment();
        final UpdateRequest request = new SpecialUpdateRequest(changed, tz);
        final UpdateResponse response = client.execute(request);
        appointment.setLastModified(response.getTimestamp());
    }

    private Appointment createAppointment() {
        final Calendar calendar = TimeTools.createCalendar(TimeZone.getTimeZone("UTC"));
        final Appointment appointment = new Appointment();
        appointment.setTitle("test for bug 12496");
        appointment.setParentFolderID(folderId);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.DATE, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setFullTime(true);
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(2);
        return appointment;
    }

    private Appointment changeAppointment() {
        final Calendar calendar = TimeTools.createCalendar(tz);
        final Appointment changed = new Appointment();
        changed.setTitle("test for bug 12496 changed");
        changed.setParentFolderID(folderId);
        changed.setObjectID(appointment.getObjectID());
        changed.setLastModified(appointment.getLastModified());
        changed.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        changed.setFullTime(false);
        changed.setEndDate(calendar.getTime());
        changed.setUntil(null);
        return changed;
    }

    private static final class SpecialUpdateRequest extends UpdateRequest {

        public SpecialUpdateRequest(final Appointment appointment, final TimeZone tz) {
            super(appointment, tz);
        }

        @Override
        public JSONObject getBody() throws JSONException {
            final JSONObject retval = super.getBody();
            retval.put(AppointmentFields.UNTIL, JSONObject.NULL);
            return retval;
        }
    }
}
