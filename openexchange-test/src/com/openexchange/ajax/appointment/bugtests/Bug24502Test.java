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
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
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

    public Bug24502Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = new AJAXClient(User.User2);
        clientC = new AJAXClient(User.User3);

        folder = Create.folder(
            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
            "Folder to test bug 18455",
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
        appointment.setTitle("Bug 18455 Test");
        appointment.setStartDate(D("01.03.2011 08:00"));
        appointment.setEndDate(D("01.03.2011 09:00"));
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setIgnoreConflicts(true);
        appointment.setUsers(new UserParticipant[] {
            new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientC.getValues().getUserId()) });
        appointment.setParticipants(new Participant[] {
            new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientC.getValues().getUserId()) });

        InsertRequest insertRequest = new InsertRequest(appointment, clientA.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientA.execute(insertRequest);
        insertResponse.fillObject(appointment);
    }

    public void testBug() throws Exception {
        DeleteRequest deleteRequest = new DeleteRequest(
            appointment.getObjectID(),
            appointment.getParentFolderID(),
            appointment.getLastModified());
        clientB.execute(deleteRequest);

        GetRequest getRequest = new GetRequest(clientC.getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), false);
        GetResponse getResponse = clientC.execute(getRequest);
        assertTrue("Appointment should be deleted completely.", getResponse.hasError());
        assertTrue("Appointment should be deleted completely.", getResponse.getErrorMessage().contains("not found"));
    }

    @Override
    public void tearDown() throws Exception {
        clientA.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder));
        super.tearDown();
    }

}
