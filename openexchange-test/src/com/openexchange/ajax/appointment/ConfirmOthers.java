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

package com.openexchange.ajax.appointment;

import static com.openexchange.ajax.folder.Create.ocl;
import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.ConfirmRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ConfirmOthers extends AbstractAJAXSession {

    private AJAXClient clientA, clientB, clientC;

    private int userIdA, userIdB, userIdC;

    private FolderObject folder;

    private Appointment appointment;

    public ConfirmOthers() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = testUser2.getAjaxClient();
        clientC = testContext.acquireUser().getAjaxClient();
        userIdA = clientA.getValues().getUserId();
        userIdB = clientB.getValues().getUserId();
        userIdC = clientC.getValues().getUserId();

        folder = new FolderObject();
        folder.setObjectID(clientA.getValues().getPrivateAppointmentFolder());
        folder.setLastModified(new Date(Long.MAX_VALUE));
        //        folder.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        //        folder.setModule(FolderObject.CALENDAR);
        //        folder.setType(FolderObject.PRIVATE);
        folder.setPermissionsAsArray(new OCLPermission[] { ocl(userIdA, false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), ocl(userIdB, false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });

        Participant external = new ExternalUserParticipant("test@example.invalid");

        CommonInsertResponse response = clientA.execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);

        appointment = new Appointment();
        appointment.setTitle("Confirm others test");
        appointment.setParentFolderID(clientC.getValues().getPrivateAppointmentFolder());
        appointment.setStartDate(Calendar.getInstance().getTime());
        appointment.setEndDate(new Date(appointment.getStartDate().getTime() + 3600000));
        List<Participant> participants = ParticipantTools.createParticipants(userIdA, userIdC);
        participants.add(external);
        appointment.setParticipants(participants);
        appointment.setIgnoreConflicts(true);
        InsertRequest request = new InsertRequest(appointment, clientC.getValues().getTimeZone(), false);
        response = clientC.execute(request);
        response.fillObject(appointment);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(3).build();
    }

    @Test
    public void testConfirmOthersAllowed() throws Exception {
        clientB.execute(new ConfirmRequest(folder.getObjectID(), appointment.getObjectID(), Appointment.ACCEPT, "yap!", userIdA, appointment.getLastModified(), true));
        GetResponse getResponse = clientA.execute(new GetRequest(folder.getObjectID(), appointment.getObjectID()));
        Appointment loadedAppointment = getResponse.getAppointment(clientA.getValues().getTimeZone());
        appointment.setLastModified(getResponse.getTimestamp());
        for (UserParticipant user : loadedAppointment.getUsers()) {
            if (user.getIdentifier() == userIdA) {
                assertEquals("Wrong confirm status.", Appointment.ACCEPT, user.getConfirm());
                assertEquals("Wrong confirm message.", "yap!", user.getConfirmMessage());
            }
        }
    }

    @Test
    public void testConfirmOthersNotAllowed() throws Exception {
        clientC.execute(new ConfirmRequest(folder.getObjectID(), appointment.getObjectID(), Appointment.ACCEPT, "yap!", userIdA, appointment.getLastModified(), false));
        GetResponse getResponse = clientA.execute(new GetRequest(folder.getObjectID(), appointment.getObjectID()));
        Appointment loadedAppointment = getResponse.getAppointment(clientA.getValues().getTimeZone());
        appointment.setLastModified(getResponse.getTimestamp());
        for (UserParticipant user : loadedAppointment.getUsers()) {
            if (user.getIdentifier() == userIdA) {
                assertEquals("Wrong confirm status.", Appointment.NONE, user.getConfirm());
                assertEquals("Wrong confirm message.", null, user.getConfirmMessage());
            }
        }
    }

    @Test
    public void testConfirmExternal() throws Exception {
        clientC.execute(new ConfirmRequest(clientC.getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), Appointment.TENTATIVE, "maybe", "test@example.invalid", appointment.getLastModified(), false));
        GetResponse getResponse = clientC.execute(new GetRequest(clientC.getValues().getPrivateAppointmentFolder(), appointment.getObjectID()));
        Appointment loadedAppointment = getResponse.getAppointment(clientA.getValues().getTimeZone());
        appointment.setLastModified(getResponse.getTimestamp());
        for (ConfirmableParticipant p : loadedAppointment.getConfirmations()) {
            if (p.getEmailAddress().equals("test@example.invalid")) {
                assertEquals("Wrong confirm status.", Appointment.TENTATIVE, p.getConfirm());
                assertEquals("Wrong confirm message.", "maybe", p.getMessage());
            }
        }
    }
}
