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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.UUID;
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
 * {@link PrivateTests}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class PrivateTests extends AbstractAJAXSession {

    private AJAXClient client1;

    private AJAXClient client2;

    private AJAXClient client3;

    private AJAXClient client4;

    private FolderObject folder;

    private Appointment app;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client1 = getClient();
        client2 = testUser2.getAjaxClient();
        client3 = testContext.acquireUser().getAjaxClient();
        client4 = testContext.acquireUser().getAjaxClient();

        folder = Create.folder(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Private Test Folder" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PRIVATE,
            ocl(client1.getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
            ocl(client2.getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
            ocl(client3.getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));
        CommonInsertResponse response = client1.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_NEW, folder));
        response.fillObject(folder);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(4).build();
    }

    @Test
    public void testBasicPrivate() throws Exception {
        app = new Appointment();
        app.setIgnoreConflicts(true);
        app.setTitle("com.openexchange.ajax.appointment.PrivateTests.testBasicPrivate");
        app.setStartDate(D("01.07.2013 08:00"));
        app.setEndDate(D("01.07.2013 09:00"));
        app.setLocation("Hier und da");
        app.setPrivateFlag(true);
        app.setParentFolderID(folder.getObjectID());

        InsertRequest insertRequest = new InsertRequest(app, client1.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = client1.execute(insertRequest);
        insertResponse.fillObject(app);

        GetRequest get = new GetRequest(folder.getObjectID(), app.getObjectID());
        GetResponse getResponse = client2.execute(get);
        Appointment loaded = getResponse.getAppointment(client2.getValues().getTimeZone());
        assertNull("Did not expect data.", loaded.getLocation());

        get = new GetRequest(folder.getObjectID(), app.getObjectID());
        getResponse = client3.execute(get);
        loaded = getResponse.getAppointment(client3.getValues().getTimeZone());
        assertNull("Did not expect data.", loaded.getLocation());

        get = new GetRequest(folder.getObjectID(), app.getObjectID(), false);
        getResponse = client4.execute(get);
        assertTrue("Expected error.", getResponse.hasError());
    }

    @Test
    public void testPrivateWithParticipant() throws Exception {
        app = new Appointment();
        app.setIgnoreConflicts(true);
        app.setTitle("com.openexchange.ajax.appointment.PrivateTests.testBasicPrivate");
        app.setStartDate(D("01.07.2013 08:00"));
        app.setEndDate(D("01.07.2013 09:00"));
        app.setLocation("Hier und da");
        app.setPrivateFlag(true);
        app.setParentFolderID(folder.getObjectID());
        app.setUsers(new UserParticipant[] { new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()), new UserParticipant(client4.getValues().getUserId()) });
        app.setParticipants(new Participant[] { new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()), new UserParticipant(client4.getValues().getUserId()) });

        InsertRequest insertRequest = new InsertRequest(app, client1.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = client1.execute(insertRequest);
        insertResponse.fillObject(app);

        GetRequest get = new GetRequest(folder.getObjectID(), app.getObjectID());
        GetResponse getResponse = client2.execute(get);
        Appointment loaded = getResponse.getAppointment(client2.getValues().getTimeZone());
        assertEquals("Missing data.", app.getLocation(), loaded.getLocation());

        get = new GetRequest(folder.getObjectID(), app.getObjectID());
        getResponse = client3.execute(get);
        loaded = getResponse.getAppointment(client3.getValues().getTimeZone());
        assertNull("Did not expect data.", loaded.getLocation());

        get = new GetRequest(folder.getObjectID(), app.getObjectID(), false);
        getResponse = client4.execute(get);
        assertTrue("Expected error.", getResponse.hasError());

        get = new GetRequest(client4.getValues().getPrivateAppointmentFolder(), app.getObjectID());
        getResponse = client4.execute(get);
        loaded = getResponse.getAppointment(client4.getValues().getTimeZone());
        assertEquals("Missing data.", app.getLocation(), loaded.getLocation());
    }

    @Test
    public void testEditByParticipant() throws Exception {
        app = new Appointment();
        app.setIgnoreConflicts(true);
        app.setTitle("com.openexchange.ajax.appointment.PrivateTests.testBasicPrivate");
        app.setStartDate(D("01.07.2013 08:00"));
        app.setEndDate(D("01.07.2013 09:00"));
        app.setLocation("Hier und da");
        app.setPrivateFlag(true);
        app.setParentFolderID(folder.getObjectID());
        app.setUsers(new UserParticipant[] {
            new UserParticipant(client1.getValues().getUserId()),
            new UserParticipant(client2.getValues().getUserId()),
            new UserParticipant(client4.getValues().getUserId()) });
        app.setParticipants(new Participant[] {
            new UserParticipant(client1.getValues().getUserId()),
            new UserParticipant(client2.getValues().getUserId()),
            new UserParticipant(client4.getValues().getUserId()) });

        InsertRequest insertRequest = new InsertRequest(app, client1.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = client1.execute(insertRequest);
        insertResponse.fillObject(app);

        Appointment updateApp = new Appointment();
        updateApp.setObjectID(app.getObjectID());
        updateApp.setParentFolderID(app.getParentFolderID());
        updateApp.setLastModified(app.getLastModified());
        updateApp.setPrivateFlag(false);
        UpdateRequest update = new UpdateRequest(updateApp, client2.getValues().getTimeZone());
        UpdateResponse updateResponse = client2.execute(update);
        updateResponse.fillObject(app);

        GetRequest get = new GetRequest(folder.getObjectID(), app.getObjectID());
        GetResponse getResponse = client2.execute(get);
        Appointment loaded = getResponse.getAppointment(client2.getValues().getTimeZone());
        assertEquals("Missing data.", app.getLocation(), loaded.getLocation());

        get = new GetRequest(folder.getObjectID(), app.getObjectID());
        getResponse = client3.execute(get);
        loaded = getResponse.getAppointment(client3.getValues().getTimeZone());
        assertEquals("Missing data.", app.getLocation(), loaded.getLocation());

        get = new GetRequest(folder.getObjectID(), app.getObjectID(), false);
        getResponse = client4.execute(get);
        assertTrue("Expected error.", getResponse.hasError());

        get = new GetRequest(client4.getValues().getPrivateAppointmentFolder(), app.getObjectID());
        getResponse = client4.execute(get);
        loaded = getResponse.getAppointment(client4.getValues().getTimeZone());
        assertEquals("Missing data.", app.getLocation(), loaded.getLocation());
    }

}
