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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug32385Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug32385Test extends AbstractAJAXSession {

    private Appointment appointment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        FolderObject sharedFolder = new FolderObject();
        sharedFolder.setObjectID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        sharedFolder.setLastModified(new Date(Long.MAX_VALUE));
        sharedFolder.setPermissionsAsArray(new OCLPermission[] { ocl(getClient().getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), ocl(testUser2.getAjaxClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });

        CommonInsertResponse response = testUser2.getAjaxClient().execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, sharedFolder));
        response.fillObject(sharedFolder);

        appointment = new Appointment();
        appointment.setTitle("Bug 32385 Test");
        appointment.setStartDate(D("01.05.2014 08:00"));
        appointment.setEndDate(D("01.05.2014 09:00"));
        UserParticipant user1 = new UserParticipant(getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        appointment.setParticipants(new Participant[] { user1, user2 });
        appointment.setUsers(new UserParticipant[] { user1, user2 });
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);

        catm.insert(appointment);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug32385() {
        List<Appointment> newappointments = catm.newappointments(D("01.05.2014 00:00", TimeZone.getTimeZone("UTC")), D("02.05.2014 00:00", TimeZone.getTimeZone("UTC")), 999, new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID });

        int count = 0;
        String inFolder = "";
        for (Appointment app : newappointments) {
            if (app.getObjectID() == appointment.getObjectID()) {
                count++;
                inFolder += app.getParentFolderID() + ",";
            }
        }

        assertEquals("Wrong amount of appointments found (in Folder " + inFolder + ")", 1, count);
    }
}
