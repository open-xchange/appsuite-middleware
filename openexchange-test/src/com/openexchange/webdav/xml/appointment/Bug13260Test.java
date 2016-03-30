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

package com.openexchange.webdav.xml.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.AppointmentTest;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13260Test extends AppointmentTest {

    public Bug13260Test(String name) {
        super(name);
    }

    public void testBugAsWritten() throws Exception {
        test(true, false);
    }

    public void testBugWithEnd() throws Exception {
        test(false, false);
    }

    public void testBugAsWrittenInComment6() throws Exception {
        test(true, true);
    }

    public void testBugWithEndAndFullTime() throws Exception {
        test(false, true);
    }

    private void test(boolean endless, boolean fullTime) throws Exception {
        int objectId = -1;
        try {
            // Create Appointment
            final Appointment appointmentObj = new Appointment();
            appointmentObj.setTitle("testBug13260");
            appointmentObj.setStartDate(startTime);
            appointmentObj.setEndDate(endTime);
            appointmentObj.setFullTime(fullTime);
            appointmentObj.setParentFolderID(appointmentFolderId);
            appointmentObj.setRecurrenceType(Appointment.DAILY);
            appointmentObj.setInterval(1);
            if (!endless) {
                appointmentObj.setOccurrence(10);
            }
            appointmentObj.setIgnoreConflicts(true);
            objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

            // Create DeleteException with update
            Calendar delExceptionDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            delExceptionDate.setTime(startTime);
            delExceptionDate.set(Calendar.HOUR_OF_DAY, 0);
            delExceptionDate.set(Calendar.MINUTE, 0);
            delExceptionDate.set(Calendar.SECOND, 0);
            delExceptionDate.set(Calendar.MILLISECOND, 0);

            appointmentObj.setDeleteExceptions(new Date[] { delExceptionDate.getTime() });
            updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);

            // Load Appointment
            Appointment loadApointment = loadAppointment(
                getWebConversation(),
                objectId,
                appointmentFolderId,
                getHostName(),
                getLogin(),
                getPassword(), context);

            // Checks
            assertNotNull("DeleteException expected.", loadApointment.getDeleteException());
            assertEquals("Exact 1 DeleteException expected.", 1, loadApointment.getDeleteException().length);
        } finally {
            if (objectId != -1) {
                deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
            }
        }
    }
}
