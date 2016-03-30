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

import java.util.List;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;


public class Bug12662Test extends CalendarSqlTest {
    /**
     * Test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12662">bug #12662</a>
     */
    public void testParticipantRecurrenceDelete() {
        try {
            // Create an "unique" title
            final String uniqueTitle = "testBug12662-" + System.currentTimeMillis();

            // Create daily appointment with two participants
            final CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user, secondUser);
            appointment.setTitle(uniqueTitle);
            appointment.setRecurrenceType(CalendarDataObject.DAILY);
            appointment.setInterval(1);
            appointment.setOccurrence(4);
            appointments.save(appointment);
            clean.add(appointment);

            // Remember series object ID
            final int objectId = appointment.getObjectID();

            // Create a change exception with only its title changed
            final CalendarDataObject changeException = appointments.createIdentifyingCopy(appointment);
            changeException.setTitle(uniqueTitle + " changed");
            changeException.setRecurrencePosition(2);
            appointments.save(changeException);
            clean.add(changeException);

            // Remember change exception's object ID
            final int changeId = changeException.getObjectID();

            // Delete whole series as second participant
            appointments.switchUser(secondUser);
            appointment.setParentFolderID(appointments.getPrivateFolder());
            appointments.delete(appointment);

            {
                // Check that whole series is gone for second participant
                final List<Appointment> list = appointments.getAppointmentsInFolder(appointments.getPrivateFolder());
                boolean occurred = false;
                for (final Appointment cdao : list) {
                    final int cur = cdao.getObjectID();
                    if (cur > 0 && (cur == objectId || cur == changeId)) {
                        occurred = true;
                        break;
                    }
                }
                assertFalse("Previously \"deleted\" daily appointment by second user still visible but shouldn't.", occurred);
            }

            {
                // Check whole series still exists for owner
                appointments.switchUser(user);
                final List<Appointment> list = appointments.getAppointmentsInFolder(appointments.getPrivateFolder());
                int occurred = 0;
                for (final Appointment cdao : list) {
                    final int cur = cdao.getObjectID();
                    if (cur > 0) {
                        if (cur == objectId) {
                            occurred |= 1;
                        } else if (cur == changeId) {
                            occurred |= 2;
                        }
                        if (occurred == 3) {
                            // Both detected
                            break;
                        }
                    }
                }
                assertTrue("Daily appointment not visible to first user but should.", 3 == occurred);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
