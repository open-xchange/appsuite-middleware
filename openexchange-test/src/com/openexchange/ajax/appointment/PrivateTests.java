/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.appointment;

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.groupware.calendar.TimeTools.D;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;

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

    public PrivateTests(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client1 = getClient();
        client2 = new AJAXClient(User.User2);
        client3 = new AJAXClient(User.User3);
        client4 = new AJAXClient(User.User4);

        folder = Create.folder(
            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
            "Private Test Folder" + System.currentTimeMillis(),
            FolderObject.CALENDAR,
            FolderObject.PRIVATE,
            ocl(
                client1.getValues().getUserId(),
                false,
                true,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION),
            ocl(
                client2.getValues().getUserId(),
                false,
                false,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION),
            ocl(
                client3.getValues().getUserId(),
                false,
                false,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION));
        CommonInsertResponse response = client1.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_NEW, folder));
        response.fillObject(folder);
    }

    public void testBasicPrivate() throws Exception {
        app = new Appointment();
        app.setIgnoreConflicts(true);
        app.setTitle("com.openexchange.ajax.appointment.PrivateTests.testBasicPrivate");
        app.setStartDate(D("01.07.2013 08:00"));
        app.setEndDate(D("01.07.2013 09:00"));
        app.setLocation("Hier und da");
        app.setPrivateFlag(true);
        app.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());

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

    public void testPrivateWithParticipant() throws Exception {
        app = new Appointment();
        app.setIgnoreConflicts(true);
        app.setTitle("com.openexchange.ajax.appointment.PrivateTests.testBasicPrivate");
        app.setStartDate(D("01.07.2013 08:00"));
        app.setEndDate(D("01.07.2013 09:00"));
        app.setLocation("Hier und da");
        app.setPrivateFlag(true);
        app.setParentFolderID(folder.getObjectID());
        app.setUsers(new UserParticipant[] {
            new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()),
            new UserParticipant(client4.getValues().getUserId()) });
        app.setParticipants(new Participant[] {
            new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()),
            new UserParticipant(client4.getValues().getUserId()) });

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
            new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()),
            new UserParticipant(client4.getValues().getUserId()) });
        app.setParticipants(new Participant[] {
            new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()),
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

    @Override
    public void tearDown() throws Exception {
        client1.execute(new DeleteRequest(app));
        client1.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_NEW, folder));
        super.tearDown();
    }

}
