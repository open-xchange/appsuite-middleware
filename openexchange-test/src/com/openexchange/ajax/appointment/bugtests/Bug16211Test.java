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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.reminder.ReminderTools;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug16211Test extends AbstractAJAXSession {

    private FolderObject personalAppointmentFolder;

    private FolderObject sharedAppointmentFolder;

    private Appointment appointment;

    private AJAXClient client2;

    private AJAXClient client3;

    private Calendar calendar;

    private TimeZone tz;

    public Bug16211Test(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = new AJAXClient(User.User2);
        client3 = new AJAXClient(User.User3);
        tz = client.getValues().getTimeZone();
        calendar = TimeTools.createCalendar(tz);

        sharedAppointmentFolder = Create.createPublicFolder(
            client2,
            "Bug16211PublicFolder" + System.currentTimeMillis(),
            FolderObject.CALENDAR);
        FolderTools.shareFolder(
            client2,
            EnumAPI.OX_NEW,
            sharedAppointmentFolder.getObjectID(),
            client.getValues().getUserId(),
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);

        appointment = createAppointment();

        // Create and insert the personal folder
        personalAppointmentFolder = Create.createPrivateFolder(
            "Bug16211PersonalFolder",
            FolderObject.CALENDAR,
            client.getValues().getUserId());

        personalAppointmentFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        final com.openexchange.ajax.folder.actions.InsertRequest insertFolderReq = new com.openexchange.ajax.folder.actions.InsertRequest(
            EnumAPI.OX_NEW,
            personalAppointmentFolder,
            false);
        final InsertResponse insertFolderResp = client.execute(insertFolderReq);
        insertFolderResp.fillObject(personalAppointmentFolder);
    }

    public void testMoveToPersonalFolder() throws Exception {
        // Use this to test if reminder of the moving user also has been updated. See Bug 16358
        // -------------
        final Appointment uApp = new Appointment();
        uApp.setParentFolderID(sharedAppointmentFolder.getObjectID());
        uApp.setObjectID(appointment.getObjectID());
        uApp.setAlarm(15);
        uApp.setIgnoreConflicts(true);
        uApp.setTitle(appointment.getTitle());
        uApp.setStartDate(appointment.getStartDate());
        uApp.setEndDate(appointment.getEndDate());
        uApp.setLastModified(appointment.getLastModified());
        final UpdateRequest reminderAppointmentReq = new UpdateRequest(uApp, tz, false);
        final UpdateResponse reminderAppointmentResp = client3.execute(reminderAppointmentReq);
        reminderAppointmentResp.fillObject(appointment);
        // -------------

        // Perform move and check if appointment appears in the right folder
        appointment.setParentFolderID(personalAppointmentFolder.getObjectID());
        final UpdateRequest moveAppointmentReq = new UpdateRequest(sharedAppointmentFolder.getObjectID(), appointment, tz, false);
        final UpdateResponse moveAppointmentResp = client.execute(moveAppointmentReq);
        moveAppointmentResp.fillObject(appointment);

        final GetRequest getAppointmentReq = new GetRequest(appointment, false);
        final GetResponse getAppointmentResp = client.execute(getAppointmentReq);

        final JSONObject respObj = (JSONObject) getAppointmentResp.getData();

        if (respObj == null) {
            fail("Appointment wasn't found in folder");
        }

        // Check if reminders folders are set correctly after move

        final RangeRequest reminderReq = new RangeRequest(appointment.getEndDate());
        final ArrayList<ReminderObject> reminderList = new ArrayList<ReminderObject>();
        reminderList.add(ReminderTools.get(client2, reminderReq).getReminderByTarget(tz, appointment.getObjectID()));
        reminderList.add(ReminderTools.get(client3, reminderReq).getReminderByTarget(tz, appointment.getObjectID()));

        for (final ReminderObject rem : reminderList) {
            if (rem.getTargetId() == appointment.getObjectID()) {
                final int uid = rem.getUser();
                if (uid == client2.getValues().getUserId()) {
                    assertTrue(
                        "Reminder is incorrect after move for User " + uid + ".",
                        rem.getFolder() == client2.getValues().getPrivateAppointmentFolder());
                } else if (uid == client3.getValues().getUserId()) {
                    assertTrue(
                        "Reminder is incorrect after move for User " + uid + ".",
                        rem.getFolder() == client3.getValues().getPrivateAppointmentFolder());
                }
            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        // Delete Appointment
        final GetRequest toDeleteReq = new GetRequest(personalAppointmentFolder.getObjectID(), appointment.getObjectID());
        final GetResponse toDeleteResp = client.execute(toDeleteReq);
        final Appointment toDelete = toDeleteResp.getAppointment(tz);
        if (null != toDelete) {
            client.execute(new com.openexchange.ajax.appointment.action.DeleteRequest(toDelete));
        }

        // Delete folders
        if (null != personalAppointmentFolder) {
            client.execute(new DeleteRequest(EnumAPI.OX_NEW, personalAppointmentFolder.getObjectID(), new Date()));
        }
        if (null != sharedAppointmentFolder) {
            client2.execute(new DeleteRequest(EnumAPI.OX_NEW, sharedAppointmentFolder.getObjectID(), new Date()));
        }

        if (null != client2) {
            client2.logout();
        }
        if (null != client3) {
            client3.logout();
        }

        super.tearDown();
    }

    private Appointment createAppointment() throws OXException, IOException, SAXException, JSONException {
        final Calendar cal = (Calendar) calendar.clone();
        final Appointment appointmentObj = new Appointment();

        appointmentObj.setTitle("testBug16211");
        cal.add(Calendar.DAY_OF_MONTH, 1);
        appointmentObj.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(cal.getTime());
        appointmentObj.setAlarm(15);

        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(sharedAppointmentFolder.getObjectID());
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setNote("");
        appointmentObj.setNotification(true);
        appointmentObj.setPrivateFlag(false);
        appointmentObj.setFullTime(false);

        final UserParticipant newParticipant = new UserParticipant(client2.getValues().getUserId());
        appointmentObj.addParticipant(newParticipant);

        final UserParticipant secondParticipant = new UserParticipant(client3.getValues().getUserId());
        appointmentObj.addParticipant(secondParticipant);

        final InsertRequest insReq = new InsertRequest(appointmentObj, tz, false);
        final AppointmentInsertResponse insResp = client2.execute(insReq);
        insResp.fillAppointment(appointmentObj);

        return appointmentObj;
    }
}
