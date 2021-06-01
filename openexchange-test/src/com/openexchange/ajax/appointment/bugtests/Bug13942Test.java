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

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.ConfirmRequest;
import com.openexchange.ajax.appointment.action.ConfirmResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13942Test extends AbstractAJAXSession {

    private Appointment appointment, updateAppointment;

    private int userIdA, userIdB, userIdC;

    private AJAXClient clientB, clientC;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        userIdA = getClient().getValues().getUserId();
        userIdB = getClientB().getValues().getUserId();
        userIdC = getClientC().getValues().getUserId();

        appointment = new Appointment();
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setTitle("Test Bug 13942");
        appointment.setStartDate(new Date(TimeTools.getHour(0, getClient().getValues().getTimeZone())));
        appointment.setEndDate(new Date(TimeTools.getHour(1, getClient().getValues().getTimeZone())));
        appointment.setParticipants(ParticipantTools.createParticipants(userIdA, userIdB, userIdC));
        appointment.setIgnoreConflicts(true);

        InsertRequest request = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse response = getClient().execute(request);
        response.fillObject(appointment);

        ConfirmRequest confirmRequest = new ConfirmRequest(getClientB().getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), CalendarObject.ACCEPT, "yap", appointment.getLastModified(), true);
        ConfirmResponse confirmResponse = getClientB().execute(confirmRequest);
        appointment.setLastModified(confirmResponse.getTimestamp());

        updateAppointment = new Appointment();
        updateAppointment.setObjectID(appointment.getObjectID());
        updateAppointment.setParentFolderID(getClientB().getValues().getPrivateAppointmentFolder());
        updateAppointment.setLastModified(appointment.getLastModified());
        updateAppointment.setAlarm(30);
    }

    @Test
    public void testBug13942() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(updateAppointment, getClientB().getValues().getTimeZone());
        UpdateResponse updateResponse = getClientB().execute(updateRequest);
        appointment.setLastModified(updateResponse.getTimestamp());
        GetRequest getRequest = new GetRequest(getClientB().getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        GetResponse getResponse = getClientB().execute(getRequest);
        Appointment loadedAppointment = getResponse.getAppointment(getClientB().getValues().getTimeZone());
        for (UserParticipant user : loadedAppointment.getUsers()) {
            if (user.getIdentifier() == userIdB) {
                assertEquals("Lost confirmation status", CalendarObject.ACCEPT, user.getConfirm());
            }
        }

    }

    private AJAXClient getClientB() throws OXException, IOException, JSONException {
        if (clientB == null) {
            clientB = testUser2.getAjaxClient();
        }
        return clientB;
    }

    private AJAXClient getClientC() throws OXException, IOException, JSONException {
        if (clientC == null) {
            clientC = testContext.acquireUser().getAjaxClient();
        }
        return clientC;
    }
}
