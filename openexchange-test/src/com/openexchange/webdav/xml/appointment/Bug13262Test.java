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
import com.openexchange.webdav.xml.FolderTest;

/**
 * This is a test for bug 13262. Note, that the test passes although the bug isn't fixed. That's because the bug is located in the outlook
 * oxtender. But having additional tests is never a bad idea.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13262Test extends AppointmentTest {

    private int objectId = -1;
    private Appointment appointment;
    private Calendar thirdOccurrence;

    public Bug13262Test(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        FolderTest.clearFolder(webCon, new int[] {appointmentFolderId}, new String[] {"calendar"}, new Date(), PROTOCOL + hostName, login, password, context);

        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(startTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        appointment = new Appointment();
        appointment.setTitle("testBug13262");
        appointment.setStartDate(cal.getTime());
        cal.add(Calendar.DATE, 1);
        appointment.setEndDate(cal.getTime());
        appointment.setFullTime(true);
        appointment.setParentFolderID(appointmentFolderId);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(5);
        appointment.setIgnoreConflicts(true);

        cal.add(Calendar.DATE, 2);
        thirdOccurrence = cal;
    }

    @Override
    public void tearDown() throws Exception {
        if (objectId != -1) {
            deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        }

        super.tearDown();
    }

    public void testBugAsWritten() throws Exception {
        // Create Appointment
        objectId = insertAppointment(getWebConversation(), appointment, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        assertTrue("No object Id returned after creation", objectId > 0);

        // Create Exception with update
        final Appointment exception = createException();
        final int exceptionId = updateAppointment(getWebConversation(), exception, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);

        // Load Appointment
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);
        assertNotNull("Loaded Appointment is null", loadAppointment);

        // Load exception
        final Appointment loadException = loadAppointment(getWebConversation(), exceptionId, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);
        assertNotNull("Loaded Exception is null", loadException);

        // Checks
        assertEquals("Start date of exception is wrong", thirdOccurrence.getTimeInMillis(), loadException.getStartDate().getTime());
    }

    private Appointment createException() {
        final Appointment exception = new Appointment();
        exception.setObjectID(objectId);
        exception.setParentFolderID(appointmentFolderId);
        exception.setLastModified(appointment.getLastModified());
        exception.setStartDate(new Date(thirdOccurrence.getTimeInMillis()));
        exception.setEndDate(new Date(thirdOccurrence.getTimeInMillis() + dayInMillis));
        exception.setFullTime(true);
        exception.setRecurrenceDatePosition(thirdOccurrence.getTime());
        exception.setTitle("testBug13262 - Exception");
        return exception;
    }

}
