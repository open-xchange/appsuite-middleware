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

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.PermissionTools;

/**
 * Tests the move from a public folder to a private folder.
 * https://dev-wiki.open-xchange.com/cgi-bin/twiki/view/Main/Calendar#Fall_7_Kalender_P1_Kalender_A1
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16194Test extends AbstractAJAXSession {

    private AJAXClient client;
    private FolderObject publicFolder;
    private AJAXClient client2;
    private Appointment appointment;
    private TimeZone timeZone;
    private TimeZone timeZone2;
    private int userId;
    private int userId2;

    public Bug16194Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId = client.getValues().getUserId();
        timeZone = client.getValues().getTimeZone();
        client2 = new AJAXClient(User.User2);
        userId2 = client2.getValues().getUserId();
        timeZone2 = client2.getValues().getTimeZone();
        publicFolder = createPublicFolder();
        appointment = new Appointment();
        appointment.setTitle("Appointment for bug 16194");
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(publicFolder.getObjectID());
        Calendar calendar = TimeTools.createCalendar(timeZone);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setParticipants(ParticipantTools.createParticipants(userId));
        InsertRequest request = new InsertRequest(appointment, timeZone);
        AppointmentInsertResponse response = client.execute(request);
        response.fillAppointment(appointment);
    }

    private FolderObject createPublicFolder() throws OXException, IOException, SAXException, JSONException {
        FolderObject folder = new FolderObject();
        folder.setModule(FolderObject.CALENDAR);
        folder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        folder.setPermissions(PermissionTools.P(I(client.getValues().getUserId()), PermissionTools.ADMIN, I(userId2), PermissionTools.ADMIN));
        folder.setFolderName("testFolder4Bug16194");
        com.openexchange.ajax.folder.actions.InsertRequest iReq = new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_NEW, folder);
        com.openexchange.ajax.folder.actions.InsertResponse iResp = client.execute(iReq);
        iResp.fillObject(folder);
        // Unfortunately no timestamp when creating a mail folder through Outlook folder tree.
        folder.setLastModified(new Date());
        return folder;
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(EnumAPI.OX_NEW, publicFolder));
        super.tearDown();
    }

    public void testMoveFromPublic2Private() throws Throwable {
        Appointment moveMe = new Appointment();
        moveMe.setObjectID(appointment.getObjectID());
        moveMe.setLastModified(appointment.getLastModified());
        int destFolder = client2.getValues().getPrivateAppointmentFolder();
        moveMe.setParentFolderID(destFolder);
        moveMe.setIgnoreConflicts(true);
        UpdateRequest uReq = new UpdateRequest(publicFolder.getObjectID(), moveMe, timeZone2, true);
        UpdateResponse uResp = client2.execute(uReq);
        appointment.setLastModified(uResp.getTimestamp());
        appointment.setParentFolderID(destFolder);
        GetRequest request = new GetRequest(destFolder, appointment.getObjectID());
        GetResponse response = client2.execute(request);
        appointment.setLastModified(response.getTimestamp());
        Appointment testAppointment = response.getAppointment(timeZone2);
        ParticipantTools.assertParticipants(testAppointment.getParticipants(), userId, userId2);
    }
}
