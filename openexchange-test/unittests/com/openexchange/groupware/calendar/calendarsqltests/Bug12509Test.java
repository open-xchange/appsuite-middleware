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

package com.openexchange.groupware.calendar.calendarsqltests;

import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.setuptools.TestContextToolkit;


public class Bug12509Test extends CalendarSqlTest {
    /**
     * Test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12509">bug #12509</a><br>
     * <i>Appointment change exception located in wrong folder</i>
     */
    public void testChangeExcResidedInSameFolder() {
        try {
            // Create private folder
            final FolderObject folder = folders.createPrivateFolderForSessionUser(
                session,
                ctx,
                "A nice private folder_" + System.currentTimeMillis(),
                appointments.getPrivateFolder());
            cleanFolders.add(folder);
            // Share to second user
            folders.sharePrivateFolder(session, ctx, secondUserId, folder);
            final TestContextToolkit tools = new TestContextToolkit();
            final int secondParticipantDefaultFolder = folders.getStandardFolder(tools.resolveUser(participant2, ctx), ctx);
            // Create daily recurring appointment in previously created private
            // folder
            final CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user, participant2);
            appointment.setParentFolderID(folder.getObjectID());
            appointment.setStartDate(D("11/03/2008 10:00"));
            appointment.setEndDate(D("11/03/2008 11:00"));
            appointment.setRecurrenceType(CalendarDataObject.DAILY);
            appointment.setInterval(1);
            appointment.setOccurrence(5);
            appointment.setIgnoreConflicts(true);
            appointments.save(appointment);
            clean.add(appointment);
            // Create a change exception on 2nd occurrence
            appointments.switchUser(secondUser);
            final CalendarDataObject update = appointments.createIdentifyingCopy(appointment);
            update.setRecurrencePosition(2);
            update.setStartDate(D("11/04/2008 12:00"));
            update.setEndDate(D("11/04/2008 13:00"));
            update.setIgnoreConflicts(true);
            appointments.save(update);
            clean.add(update);
            // Reload change exception to verify its parent folder
            appointments.switchUser(user);
            final CalendarDataObject reloadedException = appointments.reload(update);
            assertEquals("Change-exception's start NOT changed", D("11/04/2008 12:00"), reloadedException.getStartDate());
            assertEquals("Change-exception's end NOT changed", D("11/04/2008 13:00"), reloadedException.getEndDate());
            final UserParticipant[] users = reloadedException.getUsers();
            for (int i = 0; i < users.length; i++) {
                if (users[i].getIdentifier() == session.getUserId()) {
                    assertEquals(
                        "Change exception NOT located in same folder as recurring appointment",
                        folder.getObjectID(),
                        users[i].getPersonalFolderId());
                } else {
                    assertEquals(
                        "Change exception NOT located in same folder as recurring appointment",
                        secondParticipantDefaultFolder,
                        users[i].getPersonalFolderId());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
