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

import static com.openexchange.ajax.framework.ListIDs.l;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;

public class ListTest extends AppointmentTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ListTest.class);

    private AJAXClient client1;

    private CalendarTestManager ctm1;

    private FolderTestManager ftm;

    private FolderObject folder;

    public ListTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = new AJAXClient(User.User1);

        ctm1 = new CalendarTestManager(client1);
        ctm1.setFailOnError(true);
        ftm = new FolderTestManager(client1);

    }

    public void testList() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testList");
        appointmentObj.setIgnoreConflicts(true);
        final Appointment appointmentObj2 = createAppointmentObject("testList");
        appointmentObj2.setIgnoreConflicts(true);
        final Appointment appointmentObj3 = createAppointmentObject("testList");
        appointmentObj3.setIgnoreConflicts(true);


        final Appointment id1 = ctm1.insert(appointmentObj);
        final Appointment id2 = ctm1.insert(appointmentObj2);
        final Appointment id3 = ctm1.insert(appointmentObj3);

        final int[][] objectIdAndFolderId = { { id1.getObjectID(), appointmentFolderId }, { id2.getObjectID(), appointmentFolderId }, { id3.getObjectID(), appointmentFolderId } };

        final int cols[] = new int[] {
            Appointment.OBJECT_ID, Appointment.TITLE, Appointment.CREATED_BY, Appointment.FOLDER_ID, Appointment.USERS };

        final Appointment[] appointmentArray = listAppointment(
            getWebConversation(),
            objectIdAndFolderId,
            cols,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());

        assertEquals("check response array", 3, appointmentArray.length);
    }

    public void testListWithNoEntries() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testList");
        appointmentObj.setIgnoreConflicts(true);
        final Appointment appointmentObj2 = createAppointmentObject("testList");
        appointmentObj2.setIgnoreConflicts(true);
        final Appointment appointmentObj3 = createAppointmentObject("testList");
        appointmentObj3.setIgnoreConflicts(true);


        final Appointment id1 = ctm1.insert(appointmentObj);
        final Appointment id2 = ctm1.insert(appointmentObj2);
        final Appointment id3 = ctm1.insert(appointmentObj3);

        final int[][] objectIdAndFolderId = {};

        final int cols[] = new int[] {
            Appointment.OBJECT_ID, Appointment.TITLE, Appointment.CREATED_BY, Appointment.FOLDER_ID, Appointment.USERS };

        final Appointment[] appointmentArray = listAppointment(
            getWebConversation(),
            objectIdAndFolderId,
            cols,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());

        assertEquals("check response array", 0, appointmentArray.length);
    }

    public void testListWithAllFields() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testListWithAllFields");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setLocation("Location");
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setParentFolderID(appointmentFolderId);
        // appointmentObj.setPrivateFlag(true); // Currently not supported!
        appointmentObj.setFullTime(true);
        appointmentObj.setLabel(2);
        appointmentObj.setNote("note");
        appointmentObj.setCategories("testcat1,testcat2,testcat3");
        appointmentObj.setOrganizer("someone else");
        appointmentObj.setUid("1234567890abcdef" + System.currentTimeMillis());
        appointmentObj.setSequence(0);

        final int userParticipantId = ContactTest.searchContact(
            getWebConversation(),
            userParticipant2,
            FolderObject.SYSTEM_LDAP_FOLDER_ID,
            new int[] { Contact.INTERNAL_USERID },
            PROTOCOL + getHostName(),
            getSessionId())[0].getInternalUserId();
        final int groupParticipantId = GroupTest.searchGroup(
            getWebConversation(),
            groupParticipant,
            PROTOCOL,
            getHostName(),
            getSessionId())[0].getIdentifier();
        final int resourceParticipantId = ResourceTest.searchResource(
            getWebConversation(),
            resourceParticipant,
            PROTOCOL + getHostName(),
            getSessionId())[0].getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
        participants[0] = new UserParticipant(userId);
        participants[1] = new UserParticipant(userParticipantId);
        participants[2] = new GroupParticipant(groupParticipantId);
        participants[3] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);
        appointmentObj.setIgnoreConflicts(true);


        Appointment appointment = ctm1.insert(appointmentObj);

        final int[][] objectIdAndFolderId = { { appointment.getObjectID(), appointmentFolderId } };

        final Appointment[] appointmentArray = listAppointment(
            getWebConversation(),
            objectIdAndFolderId,
            APPOINTMENT_FIELDS,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());

        assertEquals("check response array", 1, appointmentArray.length);

        final Appointment loadAppointment = appointmentArray[0];

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final long newStartTime = c.getTimeInMillis();
        final long newEndTime = newStartTime + 86400000;

        compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);

    }

    public void testListWithRecurrencePosition() throws Exception {
        final int cols[] = new int[] {
            Appointment.OBJECT_ID, Appointment.TITLE, Appointment.CREATED_BY, Appointment.FOLDER_ID, Appointment.USERS,
            Appointment.RECURRENCE_POSITION };



        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testListWithRecurrencePosition" + System.currentTimeMillis());
        folderObj.setParentFolderID(FolderObject.PUBLIC);
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);

        final OCLPermission[] permission = new OCLPermission[] { com.openexchange.webdav.xml.FolderTest.createPermission(
            userId,
            false,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION), };

        folderObj.setPermissionsAsArray(permission);

        folder = ftm.insertFolderOnServer(folderObj);
        final int publicFolderId = folder.getObjectID();

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15 * dayInMillis));

        Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testListWithRecurrencePosition");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(publicFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        Appointment appointment1 = ctm1.insert(appointmentObj);

        Appointment appointmentObj2 = new Appointment();
        appointmentObj2.setTitle("testListWithRecurrencePosition2");
        appointmentObj2.setStartDate(new Date(startTime));
        appointmentObj2.setEndDate(new Date(endTime));
        appointmentObj2.setShownAs(Appointment.ABSENT);
        appointmentObj2.setIgnoreConflicts(true);
        appointmentObj2.setParentFolderID(appointmentFolderId);
        Appointment appointment2 = ctm1.insert(appointmentObj2);

        final Appointment[] appointmentList = new Appointment[3];
        appointmentList[0] = new Appointment();
        appointmentList[0].setObjectID(appointment1.getObjectID());
        appointmentList[0].setParentFolderID(publicFolderId);
        appointmentList[0].setRecurrencePosition(2);
        appointmentList[1] = new Appointment();
        appointmentList[1].setObjectID(appointment1.getObjectID());
        appointmentList[1].setParentFolderID(publicFolderId);
        appointmentList[1].setRecurrencePosition(3);
        appointmentList[2] = new Appointment();
        appointmentList[2].setObjectID(appointment2.getObjectID());
        appointmentList[2].setParentFolderID(appointmentFolderId);

        final Appointment[] appointmentArray = AppointmentTest.listAppointment(
            getWebConversation(),
            appointmentList,
            cols,
            timeZone,
            getHostName(),
            getSessionId());

        assertEquals("3 elements expected", 3, appointmentArray.length);

        boolean found1 = false;
        boolean found2 = false;
        boolean found3 = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == appointment1.getObjectID() && appointmentArray[a].getRecurrencePosition() == 2) {
                found1 = true;
            } else if (appointmentArray[a].getObjectID() == appointment1 .getObjectID()&& appointmentArray[a].getRecurrencePosition() == 3) {
                found2 = true;
            } else if (appointmentArray[a].getObjectID() == appointment2.getObjectID()) {
                found3 = true;
            }
        }

        assertTrue("not all objects in response : " + found1 + ":" + found2 + ":" + found3, (found1 && found2 && found3));
    }

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getHostName(), getSessionId()), false);
        final int cols[] = new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.LAST_MODIFIED_UTC };

        final Appointment appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        appointmentObj.setIgnoreConflicts(true);
        Appointment appointment = ctm1.insert(appointmentObj);
        final ListRequest listRequest = new ListRequest(l(new int[] { appointmentFolderId, appointment.getObjectID() }), cols, true);
        final CommonListResponse response = Executor.execute(client, listRequest);
        final JSONArray arr = (JSONArray) response.getResponse().getData();

        assertNotNull(arr);
        final int size = arr.length();
        assertTrue(size > 0);

        for (int i = 0; i < size; i++) {
            final JSONArray objectData = arr.optJSONArray(i);
            assertNotNull(objectData);
            assertNotNull(objectData.opt(2));
        }
    }

    @Override
    public void tearDown() throws Exception {
        ctm1.cleanUp();
        ftm.cleanUp();
        client1.logout();
        super.tearDown();
    }
}
