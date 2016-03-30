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

import java.util.Calendar;
import java.util.GregorianCalendar;
import com.openexchange.groupware.calendar.CalendarDataObject;
import static com.openexchange.groupware.calendar.TimeTools.D;

/**
 * {@link Bug15031Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug15031Test extends CalendarSqlTest {

    private CalendarDataObject appointment;
    private CalendarDataObject appointment2;
    private CalendarDataObject appointment2Update;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        String nextYear = Integer.toString(new GregorianCalendar().get(Calendar.YEAR) + 1);

        appointment = appointments.buildAppointmentWithResourceParticipants(resource1);
        appointment.setParentFolderID(appointments.getPrivateFolder());
        appointment.setStartDate(D("01.01."+nextYear+" 08:00"));
        appointment.setEndDate(D("01.01."+nextYear+" 09:00"));
        appointment.setIgnoreConflicts(true);
        appointments.save(appointment);
        clean.add(appointment);

        appointments.switchUser(secondUser);

        appointment2 = appointments.buildAppointmentWithResourceParticipants(resource1);
        appointment2.setParentFolderID(appointments.getPrivateFolder());
        appointment2.setStartDate(D("02.01."+nextYear+" 08:00"));
        appointment2.setEndDate(D("02.01."+nextYear+" 09:00"));
        appointment2.setIgnoreConflicts(true);
        appointments.save(appointment2);
        clean.add(appointment2);

        appointment2Update = appointments.load(appointment2.getObjectID(), appointment2.getParentFolderID());
        appointment2Update.setStartDate(D("01.01."+nextYear+" 08:00"));
        appointment2Update.setEndDate(D("01.01."+nextYear+" 09:00"));
    }

    public void testBug15031() throws Exception {
        CalendarDataObject[] conflicts = appointments.save(appointment2Update);

        if (conflicts == null || conflicts.length != 1) {
            fail("Excpected conflicts");
        }

        assertEquals("Wrong conflic", appointment.getObjectID(), conflicts[0].getObjectID());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
