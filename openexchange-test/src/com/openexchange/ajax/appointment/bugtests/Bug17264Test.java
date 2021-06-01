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

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug17264Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug17264Test extends AbstractAJAXSession {

    private AJAXClient clientA;

    private AJAXClient clientB;

    private FolderObject folder;

    private Appointment appointment;

    /**
     * Initializes a new {@link Bug17264Test}.
     *
     * @param name
     */
    public Bug17264Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clientA = getClient();
        clientB = testUser2.getAjaxClient();

        folder = Create.folder(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Folder to test bug 17264", FolderObject.CALENDAR, FolderObject.PRIVATE, ocl(clientA.getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), ocl(clientB.getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));

        CommonInsertResponse response = clientA.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);

        appointment = new Appointment();
        appointment.setUsers(new UserParticipant[] { new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientB.getValues().getUserId()) });
        appointment.setParticipants(new Participant[] { new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientB.getValues().getUserId()) });
        Calendar cal = new GregorianCalendar();
        int thisYear = cal.get(Calendar.YEAR);
        appointment.setStartDate(D("01.12." + Integer.toString(thisYear + 1) + " 08:00"));
        appointment.setEndDate(D("01.12." + Integer.toString(thisYear + 1) + " 09:00"));
        appointment.setTitle("Bug 17264 Test");
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setIgnoreConflicts(true);
        appointment.setAlarm(30);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug17264() throws Exception {
        InsertRequest insertRequest = new InsertRequest(appointment, clientA.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientA.execute(insertRequest);
        insertResponse.fillObject(appointment);
        checkAlarm(30);

        appointment.setAlarm(60);
        appointment.setParentFolderID(folder.getObjectID());
        UpdateRequest updateRequest = new UpdateRequest(appointment, clientA.getValues().getTimeZone());
        UpdateResponse updateResponse = clientA.execute(updateRequest);
        updateResponse.fillObject(appointment);
        checkAlarm(60);

        appointment.setAlarm(120);
        appointment.setParentFolderID(folder.getObjectID());
        updateRequest = new UpdateRequest(appointment, clientB.getValues().getTimeZone());
        updateResponse = clientB.execute(updateRequest);
        updateResponse.fillObject(appointment);
        checkAlarm(120);

        appointment.setAlarm(5);
        appointment.setParentFolderID(clientB.getValues().getPrivateAppointmentFolder());
        updateRequest = new UpdateRequest(appointment, clientB.getValues().getTimeZone());
        updateResponse = clientB.execute(updateRequest);
        updateResponse.fillObject(appointment);
        checkAlarm(120);
    }

    @Test
    public void testShareCreate() throws Exception {
        appointment.removeObjectID();
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setAlarm(240);
        InsertRequest insertRequest = new InsertRequest(appointment, clientB.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientB.execute(insertRequest);
        insertResponse.fillObject(appointment);
        checkAlarm(240);
    }

    private void checkAlarm(int alarmA) throws Exception {
        //checkAlarm(clientA, clientA.getValues().getPrivateAppointmentFolder(), alarmA);
        checkAlarm(folder.getObjectID(), alarmA);
        //checkAlarm(clientB, clientB.getValues().getPrivateAppointmentFolder(), alarmB);
        checkAlarm(folder.getObjectID(), alarmA);
    }

    private void checkAlarm(int folderId, int alarm) throws Exception {
        GetRequest getRequest = new GetRequest(folderId, appointment.getObjectID());
        GetResponse getResponse = clientA.execute(getRequest);
        Appointment app = getResponse.getAppointment(clientA.getValues().getTimeZone());
        assertEquals("Wrong alarm value", alarm, app.getAlarm());
    }

}
