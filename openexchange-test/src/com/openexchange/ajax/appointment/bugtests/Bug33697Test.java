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

import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.appointment.AbstractAppointmentTest;
import com.openexchange.ajax.appointment.AppointmentRangeGenerator.AppointmentRange;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.java.util.Pair;
import edu.emory.mathcs.backport.java.util.Arrays;


/**
 * {@link Bug33697Test}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class Bug33697Test extends AbstractAppointmentTest {

   private static final Logger LOG = LoggerFactory.getLogger(Bug33697Test.class);
   
   private User userX, userY, userZ;
   private AJAXClient clientX, clientY, clientZ;
   private UserValues userValuesX, userValuesY, userValuesZ;
   private Appointment bug33697Appointment;
   private FolderObject bug33697SubfolderX, bug33697SubfolderY, bug33697SubfolderZ;

    /**
     * Initializes a new {@link Bug33697Test}.
     * @param name The test name
     */
    public Bug33697Test(String name) {
        super(name);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        userX = User.User1;
        userY = User.User2;
        userZ = User.User3;

        clientX = client;
        clientY = new AJAXClient(userY);
        clientZ = new AJAXClient(userZ);

        userValuesX = clientX.getValues();
        userValuesY = clientY.getValues();
        userValuesZ = clientZ.getValues();

        bug33697SubfolderX = createCalendarSubFolder(clientX, "Bug33697SubfolderX", createOwnerPermission(userValuesX.getUserId()), createAuthorPermission(userValuesY.getUserId()));
        bug33697SubfolderY = createCalendarSubFolder(clientY, "Bug33697SubfolderY", createOwnerPermission(userValuesY.getUserId()));
        bug33697SubfolderZ = createCalendarSubFolder(clientZ, "Bug33697SubfolderZ", createOwnerPermission(userValuesZ.getUserId()), createAuthorPermission(userValuesY.getUserId()));

        AppointmentRange dateRange = appointmentRangeGenerator.getDateRange();
        bug33697Appointment = createSingle(dateRange.startDate, dateRange.endDate, "Bug33697Appointment");
        bug33697Appointment.setParentFolderID(bug33697SubfolderX.getObjectID());
        persistAppointment(clientX, bug33697Appointment);
    }

    @After
    public void tearDown() throws Exception {
        deleteAppointments(clientX, getAppointment(clientX, userValuesX.getPrivateAppointmentFolder(), bug33697Appointment.getObjectID()));
        deleteCalendarFolder(clientX, bug33697SubfolderX);
        deleteCalendarFolder(clientY, bug33697SubfolderY);
        deleteCalendarFolder(clientZ, bug33697SubfolderZ);
        clientX.logout();
        clientY.logout();
        clientZ.logout();
    }

    /*
     * - UserX shares a subfolder named "Bug33697SubfolderX" from his private calendar to UserY with the following permissions:
     *   Folder permissions: create objects and subfolders.
     *   Object permissions: read all objects, edit all objects, delete all objects.
     *   The user has administrative rights: No.
     * 
     * - UserX creates an appointment "Bug33697Appointment" in subfolder "Bug33697SubfolderX" but doesn't invite UserY as participant.
     * 
     * - UserY moves the "Bug33697Appointment" to a private subfolder "Bug33697SubfolderY"
     * 
     * Expected behaviour:
     *  - UserX:
     *    - sees appointment "Bug33697Appointment" in his private calender folder as UserY moved it
     *    - he is still creator/participant
     *  - UserY:
     *    - sees appointment "Bug33697Appointment" in folder "Bug33697AppointmentSubfolderY"
     *    - he became a participant
     */
    public void testMoveFromUserXSharedSubFolderToUserYPrivateSubFolder() throws Exception {
        bug33697Appointment.setParentFolderID(bug33697SubfolderY.getObjectID());
        Pair<Appointment, FolderObject> pair = new Pair<Appointment, FolderObject>(bug33697Appointment, bug33697SubfolderX);
        updateAppointmentsWithOrigin(clientY, Collections.singletonList(pair));

        //assert that the appointment is located in the private calendar of clientX and no longer in the private subfolder
        Appointment appointmentViewForX = null;
        try { 
            appointmentViewForX = getAppointment(clientX, userValuesX.getPrivateAppointmentFolder(), bug33697Appointment.getObjectID());
        } catch (Exception e) {
            LOG.error("Error while getting appointment view for userX", e);
        }
        assertNotNull("Moved appointment should have been found in folder: " + userValuesX.getPrivateAppointmentFolder(), appointmentViewForX);

        //assert that the appointment is located in the subfolder of clientY's private calendar
        Appointment appointmentViewForY = null;
        try {
            appointmentViewForY = getAppointment(clientY, bug33697SubfolderY.getObjectID(), bug33697Appointment.getObjectID());
        } catch (Exception e) {
            LOG.error("Error while getting appointment view for userY", e);
        }
        assertNotNull("Moved appointment should have been found in folder: " + bug33697SubfolderY, appointmentViewForY);

        //assert that both users are still participants
        Participant[] participants = appointmentViewForY.getParticipants();
        assertEquals("Appointment should have exactly 2 participants", 2, participants.length);
        int[] participantIds = {participants[0].getIdentifier(), participants[1].getIdentifier()};
        Arrays.sort(participantIds);
        int[] clientIds = {userValuesX.getUserId(), userValuesY.getUserId()};
        Arrays.sort(clientIds);
        assertTrue("Participants and clients should be equal for the moved appointment", Arrays.equals(participantIds, clientIds));

    }

    /*
     * 
     * Move from SHARED to SHARED folder isn't implemented, yet 
     * com.openexchange.calendar.api.CalendarCollection.detectFolderMoveAction(CalendarDataObject, CalendarDataObject)
     * 
     * 
     * - UserX shares a subfolder named "Bug33697SubfolderX" from his private calendar to UserY with the following permissions:
     *   Folder permissions: create objects and subfolders.
     *   Object permissions: read all objects, edit all objects, delete all objects.
     *   The user has administrative rights: No.
     * 
     * - UserX creates an appointment "Bug33697Appointment" in subfolder "Bug33697SubfolderX" but doesn't invite UserY as participant.
     * 
     * - UserZ shares a subfolder named "Bug33697SubfolderZ" from his private calendar to UserY with the following permissions:
     *   Folder permissions: create objects and subfolders.
     *   Object permissions: read all objects, edit all objects, delete all objects.
     *   The user has administrative rights: No.
     * 
     * - UserY moves the "Bug33697Appointment" from "Bug33697SubfolderX" to the private subfolder "Bug33697SubfolderZ"
     * 
     * Expected behaviour:
     *  - UserX:
     *    - sees appointment "Bug33697Appointment" in his private calender folder as UserY moved it
     *    - he is still creator/participant
     *  -UserY:
     *    - sees appointment "Bug33697Appointment" in "Bug33697SubfolderZ" and no longer in "Bug33697SubfolderX"
     *    - didn't become a participant
     *  - UserZ:
     *    - sees appointment "Bug33697Appointment" in folder "Bug33697AppointmentSubfolderZ"
     *    - became a participant
     
    @Ignore
    public void testMoveFromUserXSharedSubFolderToUserZPrivateSubFolder() throws Exception {
        bug33697Appointment.setParentFolderID(bug33697SubfolderZ.getObjectID());
        Pair<Appointment, FolderObject> pair = new Pair<Appointment, FolderObject>(bug33697Appointment, bug33697SubfolderX);
        updateAppointmentsWithOrigin(clientY, Collections.singletonList(pair));

        //assert that userX sees the appointment in his private calendar and no longer in "Bug33697SubfolderX"
        Appointment appointmentViewForX = null;
        try {
            appointmentViewForX = getAppointment(clientX, userValuesX.getPrivateAppointmentFolder(), bug33697Appointment.getObjectID());
        } catch (Exception e) {
            LOG.error("Error while getting appointment view for userX", e);
        }
        assertNotNull("UserX should see the moved appointment in folder: " + userValuesX.getPrivateAppointmentFolder(), appointmentViewForX);

        //assert that userY sees the appointment in "Bug33697AppointmentSubfolderZ" where he moved it into
        Appointment appointmentViewForY = null;
        try {
            appointmentViewForY = getAppointment(clientY, bug33697SubfolderZ.getObjectID(), bug33697Appointment.getObjectID());
        } catch (Exception e) {
            LOG.error("Error while getting appointment view for userY", e);
        }
        assertNotNull("UserY should see the moved appointment in folder: " + userValuesZ.getPrivateAppointmentFolder(), appointmentViewForY);

        //assert that the appointment is located in the subfolder of clientZ's private calendar
        Appointment appointmentViewForZ = null;
        try {
            appointmentViewForZ = getAppointment(clientZ, bug33697SubfolderZ.getObjectID(), bug33697Appointment.getObjectID());
        } catch (Exception e) {
            LOG.error("Error while getting appointment view for userZ", e);
        }
        assertNotNull("Moved appointment should have been found in folder: " + bug33697SubfolderZ, appointmentViewForZ);

        //assert that userZ became an additional participant but not userY
        Participant[] participants = appointmentViewForZ.getParticipants();
        assertEquals("Appointment should have exactly 2 participants", 2, participants.length);
        int[] participantIds = {participants[0].getIdentifier(), participants[1].getIdentifier()};
        Arrays.sort(participantIds);
        int[] clientIds = {userValuesX.getUserId(), userValuesZ.getUserId()};
        Arrays.sort(clientIds);
        assertTrue("Participants and clients should be equal for the moved appointment", Arrays.equals(participantIds, clientIds));
    }
    */

}
