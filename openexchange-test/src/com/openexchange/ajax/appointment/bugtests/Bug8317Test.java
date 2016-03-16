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

import java.util.Calendar;
import java.util.Date;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;

public class Bug8317Test extends AppointmentTest {

    public Bug8317Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * INFO: This test case must be done at least today + 1 days because otherwise
     * no conflict resolution is made because past appointments do not conflict!
     *
     * Therefore I changed this to a future date and I fixed the test case.
     *
     * TODO: Create a dynamic date/time in the future for testing.
     */
    public void testBug8317() throws Exception {
        final Calendar calendar = Calendar.getInstance(TimeZones.UTC);
        calendar.setTimeInMillis(startTime);
        calendar.add(Calendar.DATE, 5);

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(year, month, day, 0, 0, 0);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date endDate = calendar.getTime();

        Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug8317");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setFullTime(true);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

        calendar.setTimeZone(timeZone);
        calendar.set(year, month, day, 0, 30, 0);
        startDate = calendar.getTime();

        calendar.set(year, month, day, 1, 0, 0);
        endDate = calendar.getTime();

        appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug8317 II");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setIgnoreConflicts(false);

        try {
            final int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
            deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, getHostName(), getSessionId(), true);
            fail("conflict exception expected!");
        } catch (final OXException exc) {
            // Perfect. The insertAppointment throws a OXException
            // And this is what we expect here !!!
        } finally {
            deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId(), false);
        }
    }
}
