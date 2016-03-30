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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Calendar;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;


/**
 * {@link Bug30361Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug30361Test extends CalendarSqlTest {

    private CalendarDataObject appointment;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        int nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        appointment = appointments.buildAppointmentWithResourceParticipants(resource1);
        appointment.setParentFolderID(appointments.getPrivateFolder());
        appointment.setTitle("Bug 30361 Test");
        appointment.setStartDate(D("01.05." + nextYear + " 08:00"));
        appointment.setEndDate(D("01.05." + nextYear + " 09:00"));
        appointment.setIgnoreConflicts(true);
        appointments.save(appointment);
        clean.add(appointment);
    }
    
    public void testBug30361() throws Exception {
        try {
            CalendarDataObject cdao = appointments.getObjectById(appointment.getObjectID(), appointment.getParentFolderID() + 1);
        } catch (OXException e) {
            assertEquals("Wrong exception.", OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.getNumber(), e.getCode());
            return;
        }
        fail("Exception expected.");
    }


    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
