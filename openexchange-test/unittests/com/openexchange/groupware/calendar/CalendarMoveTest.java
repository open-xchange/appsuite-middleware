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

package com.openexchange.groupware.calendar;

import java.util.Date;
import java.util.List;
import com.openexchange.groupware.calendar.calendarsqltests.CalendarSqlTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class CalendarMoveTest extends AbstractCalendarTest {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarSqlTest.class);

    /**
     * Calendar fields.
     */
    private final int[] columns = new int[] { CalendarDataObject.OBJECT_ID, CalendarDataObject.PARTICIPANTS, CalendarDataObject.USERS };

    /**
     * Tests a move of an appointment from a private to another private folder.
     *
     * @throws Throwable
     */
    public void testMoveFromPrivateToPrivate() throws Throwable {
        try {
            // Create 2 private calendar folders
            final FolderObject folder1 = folders.createPrivateFolderForSessionUser(session, ctx, "folder1", appointments.getPrivateFolder());
            cleanFolders.add(folder1);
            final FolderObject folder2 = folders.createPrivateFolderForSessionUser(session, ctx, "folder2", appointments.getPrivateFolder());
            cleanFolders.add(folder2);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromPrivateToPrivate");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(folder1.getObjectID());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(folder2.getObjectID());
            appointments.move(appointmentMove, folder1.getObjectID());

            // Search appointment in folders
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);
            final List<Appointment> appointmentsInFolder2 = appointments.getAppointmentsInFolder(folder2.getObjectID(), columns);

            boolean found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder2) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            compareParticipants(appointment, foundAppointment);
            compareUsers(appointment, foundAppointment);

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from a private to a shared folder.
     *
     * @throws Throwable
     */
    public void testMoveFromPrivateToShared() throws Throwable {
        try {
            // Create 1 shared private calendar folder
            final FolderObject folder1 = folders.createPrivateFolderForSessionUser(session, ctx, "folder1", appointments.getPrivateFolder());
            cleanFolders.add(folder1);

            final OCLPermission oclp = new OCLPermission();
            oclp.setAllPermission(
                OCLPermission.CREATE_OBJECTS_IN_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.WRITE_ALL_OBJECTS,
                OCLPermission.DELETE_ALL_OBJECTS);

            folders.sharePrivateFolder(session, ctx, secondUserId, folder1, oclp);

            // Change user
            appointments.switchUser(secondUser);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromSharedToSharedSameUser");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(appointments.getPrivateFolder());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(folder1.getObjectID());
            appointments.move(appointmentMove, appointments.getPrivateFolder());

            // Search appointment in folders
            final List<Appointment> appointmentsInPrivateFolder = appointments.getAppointmentsInFolder(
                appointments.getPrivateFolder(),
                columns);
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);

            boolean found = false;
            for (final Appointment object : appointmentsInPrivateFolder) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            Participant[] expectedParticipants = new Participant[1];
            expectedParticipants[0] = new UserParticipant(userId);
            compareParticipants(expectedParticipants, foundAppointment.getParticipants());
            compareParticipants(expectedParticipants, foundAppointment.getUsers());

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from a private to a public folder.
     *
     * @throws Throwable
     */
    public void testMoveFromPrivateToPublic() throws Throwable {
        try {
            // Create 1 public calendar folder
            final FolderObject folder1 = folders.createPublicFolderFor(
                session,
                ctx,
                "folder1",
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                userId,
                secondUserId);
            cleanFolders.add(folder1);

            // Change user
            appointments.switchUser(secondUser);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromPrivateToPublic");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(appointments.getPrivateFolder());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(folder1.getObjectID());
            appointments.move(appointmentMove, appointments.getPrivateFolder());

            // Search appointment in folders
            final List<Appointment> appointmentsPrivateFolder = appointments.getAppointmentsInFolder(
                appointments.getPrivateFolder(),
                columns);
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);

            boolean found = false;
            for (final Appointment object : appointmentsPrivateFolder) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            compareParticipants(appointment, foundAppointment);
            compareUsers(appointment, foundAppointment);

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from a shared to a public folder.
     *
     * @throws Throwable
     */
    public void testMoveFromSharedToPrivate() throws Throwable {
        try {
            // Create 1 shared private calendar folder
            final FolderObject folder1 = folders.createPrivateFolderForSessionUser(session, ctx, "folder1", appointments.getPrivateFolder());
            cleanFolders.add(folder1);

            final OCLPermission oclp = new OCLPermission();
            oclp.setAllPermission(
                OCLPermission.CREATE_OBJECTS_IN_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.WRITE_ALL_OBJECTS,
                OCLPermission.DELETE_ALL_OBJECTS);

            folders.sharePrivateFolder(session, ctx, secondUserId, folder1, oclp);

            // Change user
            appointments.switchUser(secondUser);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromSharedToSharedSameUser");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(folder1.getObjectID());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(appointments.getPrivateFolder());
            appointments.move(appointmentMove, folder1.getObjectID());

            // Search appointment in folders
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);
            final List<Appointment> appointmentsInPrivateFolder = appointments.getAppointmentsInFolder(
                appointments.getPrivateFolder(),
                columns);

            boolean found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInPrivateFolder) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            Participant[] expectedParticipants = new Participant[1];
            expectedParticipants[0] = new UserParticipant(secondUserId);
            compareParticipants(expectedParticipants, foundAppointment.getParticipants());
            compareParticipants(expectedParticipants, foundAppointment.getUsers());

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from one shared folder to another of the same user.
     *
     * @throws Throwable
     */
    public void testMoveFromSharedToSharedSameUser() throws Throwable {
        try {
            // Create 2 shared private calendar folders
            final FolderObject folder1 = folders.createPrivateFolderForSessionUser(session, ctx, "folder1", appointments.getPrivateFolder());
            cleanFolders.add(folder1);
            final FolderObject folder2 = folders.createPrivateFolderForSessionUser(session, ctx, "folder2", appointments.getPrivateFolder());
            cleanFolders.add(folder2);

            final OCLPermission oclp = new OCLPermission();
            oclp.setAllPermission(
                OCLPermission.CREATE_OBJECTS_IN_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.WRITE_ALL_OBJECTS,
                OCLPermission.DELETE_ALL_OBJECTS);

            folders.sharePrivateFolder(session, ctx, secondUserId, folder1, oclp);
            folders.sharePrivateFolder(session, ctx, secondUserId, folder2, oclp);

            // Change user
            appointments.switchUser(secondUser);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromSharedToSharedSameUser");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(folder1.getObjectID());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(folder2.getObjectID());
            appointments.move(appointmentMove, folder1.getObjectID());

            // Search appointment in folders
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);
            final List<Appointment> appointmentsInFolder2 = appointments.getAppointmentsInFolder(folder2.getObjectID(), columns);

            boolean found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder2) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            compareParticipants(appointment, foundAppointment);
            compareUsers(appointment, foundAppointment);

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from a shared to a public folder.
     *
     * @throws Throwable
     */
    public void testMoveFromSharedToPublic() throws Throwable {
        try {
            // Create 1 shared private and 1 public calendar folder
            final FolderObject folder1 = folders.createPrivateFolderForSessionUser(session, ctx, "folder1", appointments.getPrivateFolder());
            cleanFolders.add(folder1);
            final FolderObject folder2 = folders.createPublicFolderFor(
                session,
                ctx,
                "folder2",
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                userId,
                secondUserId);
            cleanFolders.add(folder2);

            final OCLPermission oclp = new OCLPermission();
            oclp.setAllPermission(
                OCLPermission.CREATE_OBJECTS_IN_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.WRITE_ALL_OBJECTS,
                OCLPermission.DELETE_ALL_OBJECTS);

            folders.sharePrivateFolder(session, ctx, secondUserId, folder1, oclp);

            // Change user
            appointments.switchUser(secondUser);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromSharedToPublic");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(folder1.getObjectID());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(folder2.getObjectID());
            appointments.move(appointmentMove, folder1.getObjectID());

            // Search appointment in folders
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);
            final List<Appointment> appointmentsInFolder2 = appointments.getAppointmentsInFolder(folder2.getObjectID(), columns);

            boolean found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder2) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            compareParticipants(appointment, foundAppointment);
            compareUsers(appointment, foundAppointment);

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from a public to a private folder.
     * The appointment was created by the mover.
     *
     * @throws Throwable
     */
    public void testMoveFromPublicToPrivate1() throws Throwable {
        try {
            // Create 1 public calendar folder
            final FolderObject folder1 = folders.createPublicFolderFor(
                session,
                ctx,
                "folder1",
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                userId,
                secondUserId);
            cleanFolders.add(folder1);

            // Change user
            appointments.switchUser(secondUser);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromPublicToPrivate");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(folder1.getObjectID());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(appointments.getPrivateFolder());
            appointments.move(appointmentMove, folder1.getObjectID());

            // Search appointment in folders
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);
            final List<Appointment> appointmentsInFolder2 = appointments.getAppointmentsInFolder(
                appointments.getPrivateFolder(),
                columns);

            boolean found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder2) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            Participant[] expectedParticipants = new Participant[1];
            expectedParticipants[0] = new UserParticipant(secondUserId);
            compareParticipants(expectedParticipants, foundAppointment.getParticipants());
            compareParticipants(expectedParticipants, foundAppointment.getUsers());

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from a public to a private folder.
     * The appointment was NOT created by the mover.
     *
     * @throws Throwable
     */
    public void testMoveFromPublicToPrivate2() throws Throwable {
        try {
            // Create 1 public calendar folder
            final FolderObject folder1 = folders.createPublicFolderFor(
                session,
                ctx,
                "folder1",
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                userId,
                secondUserId);
            cleanFolders.add(folder1);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromPublicToPrivate");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(folder1.getObjectID());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Change user
            appointments.switchUser(secondUser);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(appointments.getPrivateFolder());
            appointments.move(appointmentMove, folder1.getObjectID());

            // Search appointment in folders
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);
            final List<Appointment> appointmentsInFolder2 = appointments.getAppointmentsInFolder(
                appointments.getPrivateFolder(),
                columns);

            boolean found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder2) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            Participant[] expectedParticipants = new Participant[2];
            expectedParticipants[0] = new UserParticipant(userId);
            expectedParticipants[1] = new UserParticipant(secondUserId);
            compareParticipants(expectedParticipants, foundAppointment.getParticipants());
            compareParticipants(expectedParticipants, foundAppointment.getUsers());

        } catch (final Exception e) {
            throw e;
            //fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from a public to a shared folder.
     *
     * @throws Throwable
     */
    public void testMoveFromPublicToShared() throws Throwable {
        try {
            // Create 1 shared private and 1 public calendar folder
            final FolderObject folder1 = folders.createPublicFolderFor(
                session,
                ctx,
                "folder1",
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                userId,
                secondUserId);
            cleanFolders.add(folder1);
            final FolderObject folder2 = folders.createPrivateFolderForSessionUser(session, ctx, "folder2", appointments.getPrivateFolder());
            cleanFolders.add(folder2);

            final OCLPermission oclp = new OCLPermission();
            oclp.setAllPermission(
                OCLPermission.CREATE_OBJECTS_IN_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.WRITE_ALL_OBJECTS,
                OCLPermission.DELETE_ALL_OBJECTS);

            folders.sharePrivateFolder(session, ctx, secondUserId, folder2, oclp);

            // Change user
            appointments.switchUser(secondUser);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromPublicToShared");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(folder1.getObjectID());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(folder2.getObjectID());
            appointments.move(appointmentMove, folder1.getObjectID());

            // Search appointment in folders
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);
            final List<Appointment> appointmentsInFolder2 = appointments.getAppointmentsInFolder(folder2.getObjectID(), columns);

            boolean found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder2) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            Participant[] expectedParticipants = new Participant[1];
            expectedParticipants[0] = new UserParticipant(userId);
            compareParticipants(expectedParticipants, foundAppointment.getParticipants());
            compareParticipants(expectedParticipants, foundAppointment.getUsers());

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from a Public to another public folder.
     *
     * @throws Throwable
     */
    public void testMoveFromPublicToPublic() throws Throwable {
        try {
            // Create 2 public calendar folders
            final FolderObject folder1 = folders.createPublicFolderFor(
                session,
                ctx,
                "folder1",
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                userId,
                secondUserId);
            cleanFolders.add(folder1);
            final FolderObject folder2 = folders.createPublicFolderFor(
                session,
                ctx,
                "folder2",
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                userId,
                secondUserId);
            cleanFolders.add(folder2);

            // Change user
            appointments.switchUser(secondUser);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromPublicToPublic");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(folder1.getObjectID());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(folder2.getObjectID());
            appointments.move(appointmentMove, folder1.getObjectID());

            // Search appointment in folders
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);
            final List<Appointment> appointmentsInFolder2 = appointments.getAppointmentsInFolder(folder2.getObjectID(), columns);

            boolean found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder2) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            compareParticipants(appointment, foundAppointment);
            compareUsers(appointment, foundAppointment);

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    /**
     * Tests a move of an appointment from a shared to another shared folder of a different user.
     *
     * @throws Throwable
     */
    public void testMoveFromSharedToSharedDifferentUser() throws Throwable {
        try {
            // Create 1 shared private calendar folder with first user
            final FolderObject folder1 = folders.createPrivateFolderForSessionUser(
                session,
                ctx,
                "folder1" + System.currentTimeMillis(),
                appointments.getPrivateFolder());
            cleanFolders.add(folder1);

            OCLPermission oclp = new OCLPermission();
            oclp.setAllPermission(
                OCLPermission.CREATE_OBJECTS_IN_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.WRITE_ALL_OBJECTS,
                OCLPermission.DELETE_ALL_OBJECTS);

            folders.sharePrivateFolder(session, ctx, thirdUserId, folder1, oclp);

            // Create 1 shared private calendar folder with second user
            appointments.switchUser(secondUser);
            final FolderObject folder2 = folders.createPrivateFolderForSessionUser(
                session2,
                ctx,
                "folder2" + System.currentTimeMillis(),
                appointments.getPrivateFolder());
            cleanFolders.add(folder2);

            folders.sharePrivateFolder(session2, ctx, thirdUserId, folder2, oclp);

            // Change user
            appointments.switchUser(thirdUser);

            // Create appointment
            final Date start = new Date(1231596000000L); // 10.01.2009, 14:00 UTC
            final Date end = new Date(1231599600000L); // 10.01.2009, 15:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
            appointment.setTitle("testMoveFromSharedToSharedDifferentUser");
            appointment.setIgnoreConflicts(true);
            appointment.setParentFolderID(folder1.getObjectID());
            appointments.save(appointment);
            final int objectId = appointment.getObjectID();
            clean.add(appointment);

            // Move appointment
            final CalendarDataObject appointmentMove = appointments.createIdentifyingCopy(appointment);
            appointmentMove.setParentFolderID(folder2.getObjectID());
            appointments.move(appointmentMove, folder1.getObjectID());

            // Search appointment in folders
            final List<Appointment> appointmentsInFolder1 = appointments.getAppointmentsInFolder(folder1.getObjectID(), columns);
            final List<Appointment> appointmentsInFolder2 = appointments.getAppointmentsInFolder(folder2.getObjectID(), columns);

            boolean found = false;
            for (final Appointment object : appointmentsInFolder1) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    break;
                }
            }
            assertFalse("Appointment not expected in first folder.", found);

            Appointment foundAppointment = null;
            found = false;
            for (final Appointment object : appointmentsInFolder2) {
                if (object.getObjectID() == objectId) {
                    found = true;
                    foundAppointment = object;
                    break;
                }
            }
            assertTrue("Appointment expected in second folder.", found);
            Participant[] expectedParticipants = new Participant[1];
            expectedParticipants[0] = new UserParticipant(secondUserId);
            compareParticipants(expectedParticipants, foundAppointment.getParticipants());
            compareParticipants(expectedParticipants, foundAppointment.getUsers());

        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
        }
    }

    private void compareParticipants(Appointment expected, Appointment actual) {
        assertEquals("Number of participants do not match", expected.containsParticipants(), actual.containsParticipants());
        if (expected.containsParticipants()) {
            assertEquals("Number of participants do not match.", expected.getParticipants().length, actual.getParticipants().length);
        }
        compareParticipants(expected.getParticipants(), actual.getParticipants());
    }

    private void compareUsers(Appointment expected, Appointment actual) {
        assertEquals("Number of users do not match.", expected.containsUserParticipants(), actual.containsUserParticipants());
        if (expected.containsUserParticipants()) {
            assertEquals("Number of users do not match.", expected.getUsers().length, actual.getUsers().length);
        }
        compareParticipants(expected.getUsers(), actual.getUsers());
    }

    private void compareParticipants(Participant[] expected, Participant[] actual) {
        for (Participant participantExpected : expected) {
            boolean found = false;
            for (Participant participantActual : actual) {
                if (participantActual.getIdentifier() == participantExpected.getIdentifier()) {
                    found = true;
                    break;
                }
            }
            assertTrue(
                "Did not found Participant (id: " + participantExpected.getIdentifier() + ", display name: " + participantExpected.getDisplayName() + ")",
                found);
        }
    }
}
