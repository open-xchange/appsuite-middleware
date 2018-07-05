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
import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug21620Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug21620Test extends AbstractAJAXSession {

    private Appointment appointment;
    private AJAXClient clientA;
    private AJAXClient clientB;
    private AJAXClient clientC;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * prepare clients
         */
        clientA = getClient();
        clientB = getClient2();
        clientC = new AJAXClient(testContext.acquireUser());
        /*
         * as user A, share a calendar folder to user B
         */
        catm.resetDefaultFolderPermissions();
        FolderObject sharedFolder = ftm.insertFolderOnServer(ftm.generateSharedFolder(
            UUIDs.getUnformattedStringFromRandom(), FolderObject.CALENDAR, clientA.getValues().getPrivateAppointmentFolder(), clientA.getValues().getUserId(), clientB.getValues().getUserId()));
        /*
         * prepare appointment with organizer set to user B ("acting user") and principal to user A ("folder owner")
         */
        appointment = new Appointment();
        appointment.setParentFolderID(sharedFolder.getObjectID());
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug 21620");
        appointment.setStartDate(D("next week at 13:00"));
        appointment.setEndDate(D("next week at 13:30"));
        UserParticipant userParticipantB = new UserParticipant(clientB.getValues().getUserId());
        userParticipantB.setConfirm(1);
        appointment.setUsers(new UserParticipant[] { userParticipantB });
        appointment.setParticipants(new Participant[] {
            new UserParticipant(clientA.getValues().getUserId()),
            new UserParticipant(clientC.getValues().getUserId()),
        });
        appointment.setModifiedBy(clientB.getValues().getUserId());
        appointment.setCreatedBy(clientB.getValues().getUserId());
        appointment.setOrganizer(clientB.getValues().getDefaultAddress());
        appointment.setOrganizerId(clientB.getValues().getUserId());
        appointment.setPrincipal(clientA.getValues().getDefaultAddress());
        appointment.setPrincipalId(clientA.getValues().getUserId());
    }

    @Test
    public void testBug21620() throws Exception {
        /*
         * as user B, insert the appointment in user A's calendar
         */
        AppointmentInsertResponse insertResponse = clientB.execute(new InsertRequest(appointment, clientB.getValues().getTimeZone()));
        insertResponse.fillObject(appointment);
        /*
         * as user B, check principal / organizer in created appointment
         */
        GetResponse getResponse = clientB.execute(new GetRequest(appointment));
        Appointment loadedAppointment = getResponse.getAppointment(clientB.getValues().getTimeZone());
        assertEquals("Wrong organizer ID", clientB.getValues().getUserId(), loadedAppointment.getOrganizerId());
        assertEquals("Wrong principal ID", clientA.getValues().getUserId(), loadedAppointment.getPrincipalId());
        /*
         * as user A, also load & check appointment
         */
        loadedAppointment = catm.get(appointment);
        assertEquals("Wrong organizer ID", clientB.getValues().getUserId(), loadedAppointment.getOrganizerId());
        assertEquals("Wrong principal ID", clientA.getValues().getUserId(), loadedAppointment.getPrincipalId());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (null != appointment && 0 < appointment.getObjectID()) {
                clientB.execute(new DeleteRequest(appointment));
            }
        } finally {
            super.tearDown();
        }
    }

}
