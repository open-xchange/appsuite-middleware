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
import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class UserStory1085Test extends AppointmentTest {

    private AJAXClient clientA, clientB, clientC;

    private int userIdA, userIdB, userIdC;

    private FolderObject folder;

    private Appointment appointmenShare, appointmentNormal, appointmentPrivate;

    private Date start, end;

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

        folder = Create.folder(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "UserStory1085Test - " + Long.toString(System.currentTimeMillis()), FolderObject.CALENDAR, FolderObject.PRIVATE, ocl(userIdB, false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), ocl(userIdA, false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));

        final CommonInsertResponse response = clientB.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);

        appointmenShare = CalendarTestManager.createAppointmentObject(folder.getObjectID(), "Full", D("01.02.2009 12:00"), D("01.02.2009 14:00"));
        appointmenShare.setIgnoreConflicts(true);
        CommonInsertResponse insertResponse = clientB.execute(new InsertRequest(appointmenShare, clientB.getValues().getTimeZone()));
        insertResponse.fillObject(appointmenShare);

        /*
         * reset permissions in default calendar folder of user c if required
         */
        com.openexchange.ajax.folder.actions.GetResponse folderGetResponse = clientC.execute(
            new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_OLD, clientC.getValues().getPrivateAppointmentFolder()));
        FolderObject calendarFolderC = folderGetResponse.getFolder();
        if (1 < calendarFolderC.getPermissions().size()) {
            FolderObject folderUpdate = new FolderObject(calendarFolderC.getObjectID());
            folderUpdate.setLastModified(folderGetResponse.getTimestamp());
            folderUpdate.setPermissionsAsArray(new OCLPermission[] { ocl(
                clientC.getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });
            clientC.execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, folderUpdate)).getResponse();
        }

        appointmentPrivate = CalendarTestManager.createAppointmentObject(clientC.getValues().getPrivateAppointmentFolder(), "Title of private flagged appointment", D("01.02.2009 12:00"), D("01.02.2009 14:00"));
        appointmentPrivate.setPrivateFlag(true);
        appointmentPrivate.setIgnoreConflicts(true);
        insertResponse = clientC.execute(new InsertRequest(appointmentPrivate, clientC.getValues().getTimeZone()));
        insertResponse.fillObject(appointmentPrivate);

        appointmentNormal = CalendarTestManager.createAppointmentObject(clientC.getValues().getPrivateAppointmentFolder(), "Title of appointment in not shared folder", D("01.02.2009 12:00"), D("01.02.2009 14:00"));
        appointmentNormal.setIgnoreConflicts(true);
        insertResponse = clientC.execute(new InsertRequest(appointmentNormal, clientC.getValues().getTimeZone()));
        insertResponse.fillObject(appointmentNormal);

        start = D("01.02.2009 00:00");
        end = D("02.02.2009 00:00");
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(3).build();
    }

    @Test
    public void testUserStory1085() throws Exception {
        final Appointment[] appointmentsB = catm.freeBusy(userIdB, Participant.USER, start, end);
        final Appointment[] appointmentsC = catm.freeBusy(userIdC, Participant.USER, start, end);

        boolean foundShare = false;
        boolean foundPrivate = false;
        boolean foundNormal = false;

        for (final Appointment app : appointmentsB) {
            if (app.getObjectID() == appointmenShare.getObjectID()) {
                foundShare = true;
                validateShare(app);
            }
        }

        for (final Appointment app : appointmentsC) {
            if (app.getObjectID() == appointmentNormal.getObjectID()) {
                foundNormal = true;
                validateNormal(app);
            } else if (app.getObjectID() == appointmentPrivate.getObjectID()) {
                foundPrivate = true;
                validatePrivate(app);
            }
        }

        assertTrue("Missing appointment", foundShare);
        assertTrue("Missing appointment", foundPrivate);
        assertTrue("Missing appointment", foundNormal);
    }

    private void validatePrivate(final Appointment app) {
        assertFalse("No title for private flagged appointment expected but found: " + app.getTitle(), appointmentPrivate.getTitle().equals(app.getTitle()));
        assertFalse("No folderId expected", app.containsParentFolderID());
    }

    private void validateNormal(final Appointment app) {
        assertFalse("No title for appointment in not shared folder expected but found: " + app.getTitle(), appointmentNormal.getTitle().equals(app.getTitle()));
        assertFalse("No folderId expected", app.containsParentFolderID());
    }

    private void validateShare(final Appointment app) {
        assertEquals("Missing or wrong folderId in Appointment", folder.getObjectID(), app.getParentFolderID());
    }

}
