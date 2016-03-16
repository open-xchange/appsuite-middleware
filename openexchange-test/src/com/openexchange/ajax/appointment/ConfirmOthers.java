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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.appointment.action.ConfirmRequest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
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

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ConfirmOthers extends AbstractAJAXSession {

    private AJAXClient clientA, clientB, clientC;

    private int userIdA, userIdB, userIdC;

    private FolderObject folder;

    private Appointment appointment;

    public ConfirmOthers(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = new AJAXClient(User.User2);
        clientC = new AJAXClient(User.User3);
        userIdA = clientA.getValues().getUserId();
        userIdB = clientB.getValues().getUserId();
        userIdC = clientC.getValues().getUserId();

        folder = new FolderObject();
        folder.setObjectID(clientA.getValues().getPrivateAppointmentFolder());
        folder.setLastModified(new Date(Long.MAX_VALUE));
//        folder.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
//        folder.setModule(FolderObject.CALENDAR);
//        folder.setType(FolderObject.PRIVATE);
        folder.setPermissionsAsArray(new OCLPermission[] {
            ocl(
                userIdA,
                false,
                true,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION),
            ocl(
                userIdB,
                false,
                false,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION) });

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
    public void tearDown() throws Exception {
        clientC.execute(new DeleteRequest(appointment.getObjectID(), clientC.getValues().getPrivateAppointmentFolder(), appointment.getLastModified()));
        super.tearDown();
    }

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
