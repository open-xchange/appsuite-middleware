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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONObject;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;

public class GetTest extends AppointmentTest {

    public GetTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGet() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testGet");
        appointmentObj.setOrganizer(User.User1.name());
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

        final Appointment loadAppointment = loadAppointment(
            getWebConversation(),
            objectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());

        appointmentObj.setObjectID(objectId);
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
    }

    public void testGetWithParticipants() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testGetWithParticipants");
        int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
        final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL, getHostName(), getSessionId())[0].getIdentifier();
        final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
        participants[0] = new UserParticipant(userId);
        participants[1] = new UserParticipant(userParticipantId);
        participants[2] = new GroupParticipant(groupParticipantId);
        participants[3] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

        final Appointment loadAppointment = loadAppointment(
            getWebConversation(),
            objectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());
        appointmentObj.setObjectID(objectId);
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
    }

    public void testGetWithAllFields() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testGetWithAllFields");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setLocation("Location");
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setPrivateFlag(false);
        appointmentObj.setFullTime(true);
        appointmentObj.setLabel(2);
        appointmentObj.setNote("note");
        appointmentObj.setCategories("testcat1,testcat2,testcat3");
        appointmentObj.setIgnoreConflicts(true);

        final int userParticipantId = ContactTest.searchContact(
            getWebConversation(),
            userParticipant3,
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

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

        final Appointment loadAppointment = loadAppointment(
            getWebConversation(),
            objectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final long newStartTime = c.getTimeInMillis();
        final long newEndTime = newStartTime + dayInMillis;

        appointmentObj.setObjectID(objectId);
        compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
    }

    public void testGetWithAllFieldsOnUpdate() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testGetWithAllFieldsOnUpdate");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

        appointmentObj.setLocation("Location");
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setPrivateFlag(false);
        appointmentObj.setFullTime(true);
        appointmentObj.setLabel(2);
        appointmentObj.setNote("note");
        appointmentObj.setCategories("testcat1,testcat2,testcat3");

        final int userParticipantId = ContactTest.searchContact(
            getWebConversation(),
            userParticipant3,
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

        appointmentObj.removeParentFolderID();

        updateAppointment(
            getWebConversation(),
            appointmentObj,
            objectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());

        final Appointment loadAppointment = loadAppointment(
            getWebConversation(),
            objectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final long newStartTime = c.getTimeInMillis();
        final long newEndTime = newStartTime + dayInMillis;

        appointmentObj.setObjectID(objectId);
        appointmentObj.setParentFolderID(appointmentFolderId);
        compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
    }

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getHostName(), getSessionId()), false);

        final Appointment appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
        try {
            final GetRequest getRequest = new GetRequest(appointmentFolderId, objectId);
            final GetResponse response = Executor.execute(client, getRequest);
            final JSONObject appointment = (JSONObject) response.getResponse().getData();

            assertNotNull(appointment);
            assertTrue(appointment.has("last_modified_utc"));

        } finally {
            deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId(), false);
        }
    }
}
