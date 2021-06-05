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
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * Verifies that the new iTIP/iMIP implementation properly deletes appointment if the last internal participants tries to delete it.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug21264Test extends AbstractAJAXSession {

    private TimeZone timeZone;
    private Appointment app;

    public Bug21264Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        timeZone = getClient().getValues().getTimeZone();
        app = new Appointment();
        app.setTitle("Test for bug 21264");
        final Calendar cal = TimeTools.createCalendar(timeZone);
        app.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        app.setEndDate(cal.getTime());
        app.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        app.setParticipants(new Participant[] { new UserParticipant(getClient().getValues().getUserId()), new ExternalUserParticipant("user1@example.org") });
        app.setOrganizer("user2@example.org");
        app.setIgnoreConflicts(true);
        final AppointmentInsertResponse response = getClient().execute(new com.openexchange.ajax.appointment.action.InsertRequest(app, timeZone));
        response.fillAppointment(app);
    }

    @Test
    public void testDeleteAppointment() throws IOException, JSONException, OXException {
        final CommonDeleteResponse response = getClient().execute(new DeleteRequest(app, false));
        assertFalse("Deleting appointment failed.", response.hasError());
        app = null;
    }
}
