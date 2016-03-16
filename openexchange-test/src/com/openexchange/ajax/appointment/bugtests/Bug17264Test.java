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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Calendar;
import java.util.GregorianCalendar;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
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
    public Bug17264Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        clientA = getClient();
        clientB = new AJAXClient(User.User2);

        folder = Create.folder(
            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
            "Folder to test bug 17264",
            FolderObject.CALENDAR,
            FolderObject.PRIVATE,
            ocl(
                clientA.getValues().getUserId(),
                false,
                true,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION),
            ocl(
                clientB.getValues().getUserId(),
                false,
                false,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION));

        CommonInsertResponse response = clientA.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);

        appointment = new Appointment();
        appointment.setUsers(new UserParticipant[] {
            new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientB.getValues().getUserId()) });
        appointment.setParticipants(new Participant[] {
            new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientB.getValues().getUserId()) });
        Calendar cal = new GregorianCalendar();
        int thisYear = cal.get(Calendar.YEAR);
        appointment.setStartDate(D("01.12." + Integer.toString(thisYear+1) + " 08:00"));
        appointment.setEndDate(D("01.12." + Integer.toString(thisYear+1) + " 09:00"));
        appointment.setTitle("Bug 17264 Test");
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setIgnoreConflicts(true);
        appointment.setAlarm(30);
    }

    public void testBug17264() throws Exception {
        InsertRequest insertRequest = new InsertRequest(appointment, clientA.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientA.execute(insertRequest);
        insertResponse.fillObject(appointment);
        checkAlarm(30, 0);

//        appointment.setAlarm(45);
//        appointment.setParentFolderID(clientA.getValues().getPrivateAppointmentFolder());
//        UpdateRequest updateRequest = new UpdateRequest(appointment, clientA.getValues().getTimeZone());
//        UpdateResponse updateResponse = clientA.execute(updateRequest);
//        updateResponse.fillObject(appointment);
//        checkAlarm(45, 0);

        appointment.setAlarm(60);
        appointment.setParentFolderID(folder.getObjectID());
        UpdateRequest updateRequest = new UpdateRequest(appointment, clientA.getValues().getTimeZone());
        UpdateResponse updateResponse = clientA.execute(updateRequest);
        updateResponse.fillObject(appointment);
        checkAlarm(60, 0);

        appointment.setAlarm(120);
        appointment.setParentFolderID(folder.getObjectID());
        updateRequest = new UpdateRequest(appointment, clientB.getValues().getTimeZone());
        updateResponse = clientB.execute(updateRequest);
        updateResponse.fillObject(appointment);
        checkAlarm(120, 0);

        appointment.setAlarm(5);
        appointment.setParentFolderID(clientB.getValues().getPrivateAppointmentFolder());
        updateRequest = new UpdateRequest(appointment, clientB.getValues().getTimeZone());
        updateResponse = clientB.execute(updateRequest);
        updateResponse.fillObject(appointment);
        checkAlarm(120, 5);
    }

    public void testShareCreate() throws Exception {
        appointment.removeObjectID();
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setAlarm(240);
        InsertRequest insertRequest = new InsertRequest(appointment, clientB.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientB.execute(insertRequest);
        insertResponse.fillObject(appointment);
        checkAlarm(240, 0);
    }

    private void checkAlarm(int alarmA, int alarmB) throws Exception {
        //checkAlarm(clientA, clientA.getValues().getPrivateAppointmentFolder(), alarmA);
        checkAlarm(clientA, folder.getObjectID(), alarmA);
        //checkAlarm(clientB, clientB.getValues().getPrivateAppointmentFolder(), alarmB);
        checkAlarm(clientB, folder.getObjectID(), alarmA);
    }

    private void checkAlarm(AJAXClient client, int folderId, int alarm) throws Exception {
        GetRequest getRequest = new GetRequest(folderId, appointment.getObjectID());
        GetResponse getResponse = client.execute(getRequest);
        Appointment app = getResponse.getAppointment(client.getValues().getTimeZone());
        assertEquals("Wrong alarm value", alarm, app.getAlarm());
    }

    @Override
    public void tearDown() throws Exception {
        //clientA.execute(new DeleteRequest(appointment));
        clientA.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder));
        super.tearDown();
    }

}
