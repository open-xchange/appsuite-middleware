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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.ArrayList;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug12509Test extends AbstractAJAXSession {

    private AJAXClient clientA, clientB;

    private FolderObject folder;

    private Appointment appointment, exception;

    public Bug12509Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = new AJAXClient(User.User2);

        folder = new FolderObject();
        folder.setFolderName("Bug 12509 Test Folder" + System.currentTimeMillis());
        folder.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        folder.setType(FolderObject.PRIVATE);
        folder.setModule(FolderObject.CALENDAR);
        ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(1);
        OCLPermission oclp = new OCLPermission();
        oclp.setEntity(clientA.getValues().getUserId());
        oclp.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        permissions.add(oclp);
        OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(clientB.getValues().getUserId());
        oclp2.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
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
        exception.setStartDate(D("11.07.2009 13:30"));
        exception.setEndDate(D("11.07.2009 14:30"));
        exception.setLastModified(appointment.getLastModified());
        // The following two fields are responsible for the problem.
        exception.setParticipants(ParticipantTools.createParticipants(clientA.getValues().getUserId(), clientB.getValues().getUserId()));
        exception.setAlarm(15);
    }

    public void testBug12509() throws Exception {
        UpdateRequest appointmentUpdateRequest = new UpdateRequest(exception, clientB.getValues().getTimeZone());
        UpdateResponse appointmentUpdateResponse = clientB.execute(appointmentUpdateRequest);
        exception.setLastModified(appointmentUpdateResponse.getTimestamp());
        exception.setObjectID(appointmentUpdateResponse.getId());
        appointment.setLastModified(appointmentUpdateResponse.getTimestamp());

        GetRequest appointmentGetRequest = new GetRequest(folder.getObjectID(), exception.getObjectID(), true);
        clientA.execute(appointmentGetRequest);
    }

    @Override
    public void tearDown() throws Exception {
        DeleteRequest appointmentDeleteRequest = new DeleteRequest(appointment);
        clientA.execute(appointmentDeleteRequest);
        com.openexchange.ajax.folder.actions.DeleteRequest folderDeleteRequest = new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder);
        clientA.execute(folderDeleteRequest);

        super.tearDown();
    }

}
