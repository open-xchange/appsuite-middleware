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

package com.openexchange.webdav.xml.appointment;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class NewTest extends AppointmentTest {

    public NewTest(final String name) {
        super(name);
    }

    public void testNewAppointment() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testNewAppointment");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
        appointmentObj.setObjectID(objectId);

        // prevent master/slave problem
        Thread.sleep(1000);

        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
    }

    public void testNewAppointmentWithAlarm() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testNewAppointmentWithAlarm");
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setAlarm(45);
        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
        appointmentObj.setObjectID(objectId);
        appointmentObj.setAlarmFlag(true);

        // prevent master/slave problem
        Thread.sleep(1000);

        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);
        final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
    }

    public void testNewAppointmentWithParticipants() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testNewAppointmentWithParticipants");
        appointmentObj.setIgnoreConflicts(true);

        final Group[] groupArray = GroupUserTest.searchGroup(getWebConversation(), groupParticipant, new Date(0), PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        assertTrue("group array size is not > 0", groupArray.length > 0);
        final int groupParticipantId = groupArray[0].getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new GroupParticipant(groupParticipantId);

        appointmentObj.setParticipants(participants);

        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
        appointmentObj.setObjectID(objectId);

        // prevent master/slave problem
        Thread.sleep(1000);

        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
    }

    public void testNewAppointmentWithUsers() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testNewAppointmentWithUsers");
        appointmentObj.setIgnoreConflicts(true);

        final int userParticipantId = GroupUserTest.getUserId(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword(), context);
        assertTrue("user participant not found", userParticipantId != -1);

        final UserParticipant[] users = new UserParticipant[2];
        users[0] = new UserParticipant(userId);
        users[0].setConfirm(CalendarObject.ACCEPT);
        users[1] = new UserParticipant(userParticipantId);
        users[1].setConfirm(CalendarObject.DECLINE);

        appointmentObj.setUsers(users);
        appointmentObj.setParticipants(users);

        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
        appointmentObj.setObjectID(objectId);

        // prevent master/slave problem
        Thread.sleep(1000);

        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
    }

    public void testNewAppointmentWithExternalUserParticipants() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testNewAppointmentWithExternalParticipants");
        appointmentObj.setIgnoreConflicts(true);

        final int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        assertTrue("user participant not found", userParticipantId != -1);

        final Participant[] participant = new Participant[2];
        participant[0] = new UserParticipant(userId);
        participant[1] = new ExternalUserParticipant("externaluser@example.org");

        appointmentObj.setParticipants(participant);

        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
        appointmentObj.setObjectID(objectId);
        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
    }

    public void testDailyRecurrence() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDailyRecurrence");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        appointmentObj.setObjectID(objectId);

        // prevent master/slave problem
        Thread.sleep(1000);

        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }

    public void testDailyRecurrenceWithOccurrences() throws Exception {
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTime(startTime);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final int occurrences = 5;

        c.add(Calendar.DAY_OF_MONTH, (occurrences-1));

        final Date until = c.getTime();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDailyRecurrence");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(occurrences);

        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        appointmentObj.setObjectID(objectId);
        appointmentObj.setUntil(until);

        // prevent master/slave problem
        Thread.sleep(1000);

        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }

    public void testDailyFullTimeRecurrenceWithOccurrences() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final int occurrences = 2;

        final Date startDate = c.getTime();
        final Date endDate = new Date(c.getTimeInMillis() + dayInMillis);

        final Date until = new Date(startDate.getTime() + ((occurrences-1)*dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDailyFullTimeRecurrenceWithOccurrences");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setFullTime(true);
        appointmentObj.setOccurrence(occurrences);

        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        appointmentObj.setObjectID(objectId);
        appointmentObj.setUntil(until);

        // prevent master/slave problem
        Thread.sleep(1000);

        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }

    public void testAppointmentInPrivateFlagInPublicFolder() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testAppointmentInPrivateFlagInPublicFolder" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] {
            FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
        };

        folderObj.setPermissionsAsArray( permission );

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testAppointmentInPrivateFlagInPublicFolder");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(parentFolderId);
        appointmentObj.setPrivateFlag(true);
        appointmentObj.setIgnoreConflicts(true);

        try {
            final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
            deleteAppointment(getWebConversation(), objectId, parentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
            fail("conflict exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), "APP-0070");
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostName(), getLogin(), getPassword(), context);
    }

    public void testDailyRecurrenceWithDeletingFirstOccurrence() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final int occurrences = 5;

        final Date recurrenceDatePosition = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, (occurrences-1));

        final Date until = c.getTime();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDailyRecurrenceWithDeletingFirstOccurrence");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(occurrences);

        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        Date modified = loadAppointment.getLastModified();
        appointmentObj.setObjectID(objectId);
        appointmentObj.setUntil(until);
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, modified, recurrenceDatePosition, getHostName(), getLogin(), getPassword(), context);
        appointmentObj.setDeleteExceptions(new Date[] { recurrenceDatePosition } );

        // prevent master/slave problem
        Thread.sleep(1000);

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword(), context);
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }

    public void testAppointmentWithAttachment() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testContactWithAttachment");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
        appointmentObj.setObjectID(objectId);
        // contactObj.setNumberOfAttachments(1);

        final AttachmentMetadata attachmentMeta = new AttachmentImpl();
        attachmentMeta.setAttachedId(objectId);
        attachmentMeta.setFolderId(appointmentFolderId);
        attachmentMeta.setFileMIMEType("text/plain");
        attachmentMeta.setModuleId(Types.APPOINTMENT);
        attachmentMeta.setFilename("test.txt");

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
        AttachmentTest.insertAttachment(webCon, attachmentMeta, byteArrayInputStream, getHostName(), getLogin(), getPassword(), context);

        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);
        final Appointment[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, decrementDate(loadAppointment.getLastModified()), true, false, getHostName(), getLogin(), getPassword(), context);

        boolean found = false;
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                compareObject(appointmentObj, appointmentArray[a]);
                found = true;
            }
        }

        assertTrue("appointment not found" , found);
    }
}
