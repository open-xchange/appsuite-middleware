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

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug12509Test extends AbstractAJAXSession {

    private AJAXClient clientA, clientB;

    private FolderObject folder;

    private Appointment appointment, exception;

    public Bug12509Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = testUser2.getAjaxClient();

        folder = new FolderObject();
        folder.setFolderName("Bug 12509 Test Folder" + UUID.randomUUID().toString());
        folder.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        folder.setType(FolderObject.PRIVATE);
        folder.setModule(FolderObject.CALENDAR);
        ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(1);
        OCLPermission oclp = new OCLPermission();
        oclp.setEntity(clientA.getValues().getUserId());
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        permissions.add(oclp);
        OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(clientB.getValues().getUserId());
        oclp2.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp2.setFolderAdmin(false);
        permissions.add(oclp2);
        folder.setPermissions(permissions);

        com.openexchange.ajax.folder.actions.InsertRequest folderInsertRequest = new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder);
        CommonInsertResponse folderInsertResponse = clientA.execute(folderInsertRequest);
        folderInsertResponse.fillObject(folder);

        appointment = new Appointment();
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setTitle("gnunzel");
        appointment.setStartDate(D("10.07.2009 13:00"));
        appointment.setEndDate(D("10.07.2009 14:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(5);
        appointment.setParticipants(ParticipantTools.createParticipants(clientA.getValues().getUserId(), clientB.getValues().getUserId()));
        appointment.setIgnoreConflicts(true);

        InsertRequest appointmentInsertRequest = new InsertRequest(appointment, clientA.getValues().getTimeZone());
        AppointmentInsertResponse appointmentInsertResponse = clientA.execute(appointmentInsertRequest);
        appointmentInsertResponse.fillAppointment(appointment);

        exception = new Appointment();
        exception.setObjectID(appointment.getObjectID());
        exception.setParentFolderID(clientB.getValues().getPrivateAppointmentFolder());
        exception.setRecurrencePosition(2);
        exception.setLastModified(appointment.getLastModified());
        // The following two fields are responsible for the problem.
        exception.setParticipants(ParticipantTools.createParticipants(clientA.getValues().getUserId(), clientB.getValues().getUserId()));
        exception.setAlarm(15);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug12509() throws Exception {
        UpdateRequest appointmentUpdateRequest = new UpdateRequest(exception, clientB.getValues().getTimeZone());
        UpdateResponse appointmentUpdateResponse = clientB.execute(appointmentUpdateRequest);
        exception.setLastModified(appointmentUpdateResponse.getTimestamp());
        exception.setObjectID(appointmentUpdateResponse.getId());
        appointment.setLastModified(appointmentUpdateResponse.getTimestamp());

        GetRequest appointmentGetRequest = new GetRequest(folder.getObjectID(), exception.getObjectID(), true);
        clientA.execute(appointmentGetRequest);
    }

}
