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
import java.sql.ResultSet;
import java.sql.Statement;
import com.openexchange.calendar.CalendarAdministration;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.server.impl.DBPool;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;


public class Bug12466Test extends CalendarSqlTest {
    // Bug12466
    public void testAutoDeletionOfAppointmentsWithResources() throws Throwable {
        Connection readcon = null;
        Connection writecon = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            final CalendarDataObject appointment = appointments.buildAppointmentWithResourceParticipants(resource1);
            final CalendarDataObject appointment2 = appointments.buildAppointmentWithResourceParticipants(resource2, resource3);
            final CalendarDataObject appointment3 = appointments.buildAppointmentWithUserParticipants(user, participant1);
            appointment.setTitle("testBug12644_1");
            appointment.setIgnoreConflicts(true);
            appointment2.setTitle("testBug12644_2");
            appointment2.setIgnoreConflicts(true);
            appointment3.setTitle("testBug12644_3");
            appointment3.setIgnoreConflicts(true);
            appointments.save(appointment);
            appointments.save(appointment2);
            appointments.save(appointment3);
            clean.add(appointment);
            clean.add(appointment2);
            clean.add(appointment3);

            readcon = DBPool.pickup(ctx);
            writecon = DBPool.pickupWriteable(ctx);
            final SessionObject so = SessionObjectWrapper.createSessionObject(userId, ctx.getContextId(), "deleteAllUserApps");

            final DeleteEvent delEvent = new DeleteEvent(
                this,
                so.getUserId(),
                DeleteEvent.TYPE_USER,
                ContextStorage.getInstance().getContext(so.getContextId()));

            final CalendarAdministration ca = new CalendarAdministration();
            ca.deletePerformed(delEvent, readcon, writecon);

            stmt = readcon.createStatement();
            rs = stmt.executeQuery("SELECT * FROM prg_dates WHERE cid = " + ctx.getContextId() + " AND intfield01 = " + appointment.getObjectID());
            assertFalse("Appointment with resource still exists.", rs.next());
            rs = stmt.executeQuery("SELECT * FROM prg_dates WHERE cid = " + ctx.getContextId() + " AND intfield01 = " + appointment2.getObjectID());
            assertFalse("Appointment with resource still exists.", rs.next());
            rs = stmt.executeQuery("SELECT * FROM prg_dates WHERE cid = " + ctx.getContextId() + " AND intfield01 = " + appointment3.getObjectID());
            assertTrue("Appointment with additional participants was deleted.", rs.next());
            rs.close();
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (readcon != null) {
                DBPool.push(ctx, readcon);
            }
            if (writecon != null) {
                DBPool.pushWrite(ctx, writecon);
            }
        }
    }
}
