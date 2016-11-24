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
import java.util.List;
import java.util.UUID;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug21620Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug21620Test extends AbstractAJAXSession {

    private FolderObject sharedFolder;
    private Appointment appointment2;
    private AJAXClient clientA;
    private AJAXClient clientB;
    private AJAXClient clientC;

    public Bug21620Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        clientA = getClient();
        clientB = new AJAXClient(User.User2);
        clientC = new AJAXClient(User.User3);

        // as user A, create folder shared to user B
        sharedFolder = Create.createPrivateFolder("shared_" + System.currentTimeMillis(), FolderObject.CALENDAR, client.getValues().getUserId());
        sharedFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        InsertResponse folderInsertResponse = client.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_NEW, sharedFolder));
        sharedFolder.setObjectID(folderInsertResponse.getId());
        sharedFolder.setLastModified(client.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, sharedFolder.getObjectID())).getTimestamp());
        FolderTools.shareFolder(client, EnumAPI.OX_NEW, sharedFolder.getObjectID(), clientB.getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);

        // prepare appointment
        List<UserParticipant> users = new ArrayList<UserParticipant>();
        UserParticipant userB = new UserParticipant(clientB.getValues().getUserId());
        userB.setConfirm(1);
        users.add(userB);

        List<Participant> participants = new ArrayList<Participant>();
        Participant participantB = new UserParticipant(clientB.getValues().getUserId());
        Participant participantC = new UserParticipant(clientC.getValues().getUserId());
        participants.add(participantB);
        participants.add(participantC);

        appointment2 = new Appointment();
        appointment2.setIgnoreConflicts(true);
        appointment2.setUid(UUID.randomUUID().toString());
        appointment2.setParentFolderID(sharedFolder.getObjectID());
        appointment2.setTitle("Bug 21620");
        appointment2.setStartDate(D("14.04.2012 04:00"));
        appointment2.setEndDate(D("14.04.2012 04:30"));
        appointment2.setNumberOfAttachments(0);
        appointment2.setModifiedBy(clientA.getValues().getUserId());
        appointment2.setCreatedBy(clientA.getValues().getUserId());
        appointment2.setFullTime(false);
        appointment2.setPrivateFlag(false);
        appointment2.setTimezone("Europe/Berlin");
        appointment2.setUsers(users);
        appointment2.setParticipants(participants);
        appointment2.setOrganizer(clientB.getValues().getDefaultAddress());
        appointment2.setOrganizerId(clientB.getValues().getUserId());
        appointment2.setPrincipal(clientA.getValues().getDefaultAddress());
        appointment2.setPrincipalId(clientA.getValues().getUserId());
    }

    public void testBug21620() throws Exception {
        /*
         * insert appointment in user a's calendar as user B ("on behalf of user a")
         */
        InsertRequest insertRequest = new InsertRequest(appointment2, clientB.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientB.execute(insertRequest);
        insertResponse.fillObject(appointment2);
        /*
         * verify organizer & principal as user B
         */
        GetRequest getRequest = new GetRequest(appointment2);
        GetResponse getResponse = clientB.execute(getRequest);
        Appointment loadedAppointment = getResponse.getAppointment(clientB.getValues().getTimeZone());
        assertEquals("Wrong organizer ID", clientB.getValues().getUserId(), loadedAppointment.getOrganizerId());
        assertEquals("Wrong principal ID", clientA.getValues().getUserId(), loadedAppointment.getPrincipalId());
        /*
         * verify organizer & principal as user A
         */
        getRequest = new GetRequest(appointment2);
        getResponse = clientB.execute(getRequest);
        loadedAppointment = getResponse.getAppointment(clientA.getValues().getTimeZone());
        assertEquals("Wrong organizer ID", clientB.getValues().getUserId(), loadedAppointment.getOrganizerId());
        assertEquals("Wrong principal ID", clientA.getValues().getUserId(), loadedAppointment.getPrincipalId());
    }

    @Override
    public void tearDown() throws Exception {
        if (null != appointment2 && 0 < appointment2.getObjectID()) {
            getClient().execute(new DeleteRequest(appointment2));
        }
        if (null != sharedFolder && 0 < sharedFolder.getObjectID()) {
            client.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_NEW, sharedFolder));
        }
        if (null != clientB) {
            clientB.logout();
        }
        if (null != clientC) {
            clientC.logout();
        }
        super.tearDown();
    }

}
