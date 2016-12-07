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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.server.impl.OCLPermission;

/**
 * Tests move from shared folder to the private folder.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16151Test extends AbstractAJAXSession {

    private AJAXClient client;
    private AJAXClient client2;
    private Appointment appointment;
    private TimeZone timeZone2;

    public Bug16151Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = new AJAXClient(testContext.acquireUser());
        timeZone2 = client2.getValues().getTimeZone();
        // client2 shares folder
        FolderTools.shareFolder(client2, EnumAPI.OX_NEW, client2.getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        // client creates appointment
        appointment = new Appointment();
        appointment.setTitle("Appointment for bug 16151");
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        Calendar calendar = TimeTools.createCalendar(timeZone2);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointment.setEndDate(calendar.getTime());
        InsertRequest request = new InsertRequest(appointment, timeZone2);
        AppointmentInsertResponse response = getClient().execute(request);
        response.fillAppointment(appointment);
    }

    @After
    public void tearDown() throws Exception {
        try {
            // client deletes appointment
            appointment.setLastModified(new Date(Long.MAX_VALUE));
            getClient().execute(new DeleteRequest(appointment));
            // client2 unshares folder
            FolderTools.unshareFolder(client2, EnumAPI.OX_NEW, client2.getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId());
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testMoveFromShared2Private() throws Throwable {
        // client moves from shared folder to private folder
        Appointment moveMe = new Appointment();
        moveMe.setObjectID(appointment.getObjectID());
        moveMe.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        moveMe.setLastModified(appointment.getLastModified());
        moveMe.setIgnoreConflicts(true);
        TimeZone timeZone = getClient().getValues().getTimeZone();
        UpdateRequest uReq = new UpdateRequest(appointment.getParentFolderID(), moveMe, timeZone, true);
        UpdateResponse uResp = getClient().execute(uReq);
        appointment.setLastModified(uResp.getTimestamp());
        appointment.setParentFolderID(moveMe.getParentFolderID());
        // client loads appointment from private folder
        GetRequest gReq = new GetRequest(moveMe.getParentFolderID(), moveMe.getObjectID());
        GetResponse gResp = getClient().execute(gReq);
        // assert participants
        Appointment testAppointment = gResp.getAppointment(timeZone);
        ParticipantTools.assertParticipants(testAppointment.getParticipants(), getClient().getValues().getUserId());
    }
}
