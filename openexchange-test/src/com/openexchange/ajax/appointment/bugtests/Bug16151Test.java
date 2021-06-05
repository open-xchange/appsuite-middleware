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

import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * Tests move from shared folder to the private folder.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16151Test extends AbstractAJAXSession {

    private AJAXClient client2;
    private Appointment appointment;
    private TimeZone timeZone2;

    public Bug16151Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client2 = testUser2.getAjaxClient();
        timeZone2 = client2.getValues().getTimeZone();
        // client2 shares folder
        FolderTools.shareFolder(client2, EnumAPI.OX_NEW, client2.getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        // client creates appointment
        appointment = new Appointment();
        appointment.setTitle("Appointment for bug 16151");
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        Calendar calendar = TimeTools.createCalendar(timeZone2);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointment.setEndDate(calendar.getTime());
        InsertRequest request = new InsertRequest(appointment, timeZone2);
        AppointmentInsertResponse response = getClient().execute(request);
        response.fillAppointment(appointment);
    }

    @Test
    public void testMoveFromShared2Private() throws Throwable {
        // client moves from shared folder to private folder
        Appointment moveMe = new Appointment();
        moveMe.setObjectID(appointment.getObjectID());
        moveMe.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        moveMe.setLastModified(appointment.getLastModified());
        moveMe.setIgnoreConflicts(true);
        TimeZone timeZone = getClient().getValues().getTimeZone();
        UpdateRequest uReq = new UpdateRequest(appointment.getParentFolderID(), moveMe, timeZone, true);
        UpdateResponse uResp = getClient().execute(uReq);
        appointment.setLastModified(uResp.getTimestamp());
        appointment.setParentFolderID(moveMe.getParentFolderID());
        // client loads appointment from private folder
        GetRequest gReq = new GetRequest(moveMe.getParentFolderID(), moveMe.getObjectID());
        GetResponse gResp = getClient().execute(gReq);
        // assert participants
        Appointment testAppointment = gResp.getAppointment(timeZone);
        ParticipantTools.assertParticipants(testAppointment.getParticipants(), getClient().getValues().getUserId());
    }
}
