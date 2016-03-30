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

import java.sql.Connection;
import com.openexchange.calendar.CalendarAdministration;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.server.impl.DBPool;

public class Bug13358Test extends CalendarSqlTest {

    /**
     * Test for <a href="http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=13358">bug #13358</a>
     */
    public void testDeleteUserGroup() throws Throwable {
        final CalendarDataObject appointment = appointments.buildAppointmentWithGroupParticipants(group);
        appointment.setTitle("Bug 13358 Test");
        appointments.save(appointment);
        final int objectId = appointment.getObjectID();
        clean.add(appointment);

        final DeleteEvent deleteEvent = new DeleteEvent(this, groupId, DeleteEvent.TYPE_GROUP, ctx);
        final Connection readcon = DBPool.pickup(ctx);
        final Connection writecon = DBPool.pickupWriteable(ctx);
        final CalendarAdministration ca = new CalendarAdministration();
        ca.deletePerformed(deleteEvent, readcon, writecon);
        Thread.sleep(500);

        final CalendarDataObject loadApp = appointments.load(objectId, folders.getStandardFolder(userId, ctx));
        Participant[] participants = loadApp.getParticipants();
        boolean foundGroup = false;
        boolean foundMember = false;
        for (Participant participant : participants) {
            if (participant.getType() == Participant.GROUP) {
                foundGroup = true;
            } else if (participant.getIdentifier() == secondUserId) {
                foundMember = true;
            }
        }

        assertFalse("Group should not be in the participants: " + toString(participants), foundGroup);
        assertTrue("Member should be in the participants.", foundMember);
    }

    private static String toString(Participant[] participants) {
        StringBuilder sb = new StringBuilder();
        for (Participant participant : participants) {
            sb.append(participant.toString());
            sb.append(':');
            sb.append(participant.getIdentifier());
            sb.append(',');
            sb.append(participant.getType());
            sb.append(';');
        }
        return sb.toString();
    }
}
