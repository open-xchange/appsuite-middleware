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
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.SetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.tryagain.TryAgain;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class UserStory2173Test extends AbstractAJAXSession {

    private AJAXClient clientA, clientB;

    private FolderObject publicFolder;

    private Appointment appointmentPrivate, appointmentPublic;

    public UserStory2173Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = testUser2.getAjaxClient();

        SetRequest setRequest = new SetRequest(Tree.CalendarDefaultStatusPrivate, I(Appointment.ACCEPT));
        SetResponse respPriv = clientB.execute(setRequest);
        assertFalse(respPriv.getErrorMessage(), respPriv.hasError());
        setRequest = new SetRequest(Tree.CalendarDefaultStatusPublic, I(Appointment.ACCEPT));
        SetResponse respPub = clientB.execute(setRequest);
        assertFalse(respPub.getErrorMessage(), respPub.hasError());

        publicFolder = Create.folder(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "US2173TestFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, FolderObject.PUBLIC, ocl(clientA.getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), ocl(clientB.getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));
        CommonInsertResponse folderResponse = clientA.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, publicFolder));
        assertFalse(folderResponse.getErrorMessage(), folderResponse.hasError());
        folderResponse.fillObject(publicFolder);

        List<Participant> participants = ParticipantTools.createParticipants(clientA.getValues().getUserId(), clientB.getValues().getUserId());
        appointmentPrivate = new Appointment();
        appointmentPrivate.setParentFolderID(clientA.getValues().getPrivateAppointmentFolder());
        appointmentPrivate.setTitle("UserStory2173 Test in private folder");
        appointmentPrivate.setStartDate(new Date(TimeTools.getHour(13, clientA.getValues().getTimeZone())));
        appointmentPrivate.setEndDate(new Date(TimeTools.getHour(14, clientA.getValues().getTimeZone())));
        appointmentPrivate.setParticipants(participants);
        appointmentPrivate.setIgnoreConflicts(true);

        participants = ParticipantTools.createParticipants(clientA.getValues().getUserId(), clientB.getValues().getUserId());
        appointmentPublic = new Appointment();
        appointmentPublic.setParentFolderID(publicFolder.getObjectID());
        appointmentPublic.setTitle("UserStory2173 Test in private folder");
        appointmentPublic.setStartDate(new Date(TimeTools.getHour(13, clientA.getValues().getTimeZone())));
        appointmentPublic.setEndDate(new Date(TimeTools.getHour(14, clientA.getValues().getTimeZone())));
        appointmentPublic.setParticipants(participants);
        appointmentPublic.setIgnoreConflicts(true);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    @TryAgain
    public void testPrivate() throws Exception {
        AppointmentInsertResponse insertResponse = clientA.execute(new InsertRequest(appointmentPrivate, clientA.getValues().getTimeZone()));
        assertFalse(insertResponse.getErrorMessage(), insertResponse.hasError());
        insertResponse.fillAppointment(appointmentPrivate);

        GetRequest getRequest = new GetRequest(clientA.getValues().getPrivateAppointmentFolder(), appointmentPrivate.getObjectID());
        Appointment loadedAppointment = clientA.execute(getRequest).getAppointment(clientA.getValues().getTimeZone());

        for (UserParticipant user : loadedAppointment.getUsers()) {
            if (user.getIdentifier() == clientB.getValues().getUserId()) {
                assertEquals("Wrong confirmation status", Appointment.ACCEPT, user.getConfirm());
            }
        }
    }

    @Test
    @TryAgain
    public void testPublic() throws Exception {
        AppointmentInsertResponse insertResponse = clientA.execute(new InsertRequest(appointmentPublic, clientA.getValues().getTimeZone()));
        assertFalse(insertResponse.getErrorMessage(), insertResponse.hasError());
        insertResponse.fillAppointment(appointmentPublic);

        GetRequest getRequest = new GetRequest(publicFolder.getObjectID(), appointmentPublic.getObjectID());
        Appointment loadedAppointment = clientA.execute(getRequest).getAppointment(clientA.getValues().getTimeZone());

        for (UserParticipant user : loadedAppointment.getUsers()) {
            if (user.getIdentifier() == clientB.getValues().getUserId()) {
                assertEquals("Wrong confirmation status", Appointment.ACCEPT, user.getConfirm());
            }
        }
    }

}