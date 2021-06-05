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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
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

/**
 * {@link Bug24502Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug24502Test extends AbstractAJAXSession {

    private AJAXClient clientA;

    private AJAXClient clientB;

    private AJAXClient clientC;

    private FolderObject folder;

    private Appointment appointment;

    public Bug24502Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        clientA = testUser.getAjaxClient();
        clientB = testUser2.getAjaxClient();
        clientC = testContext.acquireUser().getAjaxClient();

        folder = Create.folder(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Folder to test bug 18455" + System.currentTimeMillis(), FolderObject.CALENDAR, FolderObject.PRIVATE, ocl(clientA.getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), ocl(clientB.getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));

        CommonInsertResponse response = clientA.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);

        appointment = new Appointment();
        appointment.setTitle("Bug 18455 Test");
        appointment.setStartDate(D("01.03.2011 08:00"));
        appointment.setEndDate(D("01.03.2011 09:00"));
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setIgnoreConflicts(true);
        appointment.setUsers(new UserParticipant[] { new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientC.getValues().getUserId()) });
        appointment.setParticipants(new Participant[] { new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientC.getValues().getUserId()) });

        InsertRequest insertRequest = new InsertRequest(appointment, clientA.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientA.execute(insertRequest);
        insertResponse.fillObject(appointment);
    }

    @Test
    public void testBug() throws Exception {
        DeleteRequest deleteRequest = new DeleteRequest(appointment.getObjectID(), appointment.getParentFolderID(), appointment.getLastModified());
        clientB.execute(deleteRequest);

        GetRequest getRequest = new GetRequest(clientC.getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), false);
        GetResponse getResponse = clientC.execute(getRequest);
        assertTrue("Appointment should be deleted completely.", getResponse.hasError());
        assertTrue("Appointment should be deleted completely.", getResponse.getErrorMessage().contains("not found"));
    }

}
