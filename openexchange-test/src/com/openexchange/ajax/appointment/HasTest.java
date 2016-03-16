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

package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.HasRequest;
import com.openexchange.ajax.appointment.action.HasResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

public class HasTest extends AbstractAJAXSession {

    private AJAXClient client;

    private int folderId;

    private TimeZone tz;

    public HasTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateAppointmentFolder();
        tz = client.getValues().getTimeZone();
    }

    public void testHasAppointment() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 9); // Not using start of day because of daylight saving time shifting problem.
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        final int hasInterval = 7;
        final Date start = c.getTime();
        c.add(Calendar.DATE, hasInterval);
        final Date end = c.getTime();

        final Appointment appointment = new Appointment();
        appointment.setTitle("testHasAppointment");
        c.setTime(start);
        final int posInArray = 3;
        c.add(Calendar.DATE, posInArray);
        appointment.setStartDate(c.getTime());
        c.add(Calendar.HOUR, 1);
        appointment.setEndDate(c.getTime());
        appointment.setShownAs(Appointment.ABSENT);
        appointment.setParentFolderID(folderId);
        appointment.setIgnoreConflicts(true);
        final AppointmentInsertResponse insertR = client.execute(new InsertRequest(appointment, tz));
        insertR.fillAppointment(appointment);
        try {
            final HasResponse hasR = client.execute(new HasRequest(start, end, tz));
            final boolean[] hasAppointments = hasR.getValues();
            assertEquals("Length of array of action has is wrong.", hasInterval, hasAppointments.length);
            assertEquals("Inserted appointment not found in action has response.", hasAppointments[posInArray], true);
        } finally {
            client.execute(new DeleteRequest(appointment));
        }
    }

    public void testHasAppointmentFullTime() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        final int hasInterval = 7;
        final Date start = c.getTime();
        c.add(Calendar.DATE, hasInterval);
        final Date end = c.getTime();

        final Appointment appointment = new Appointment();
        appointment.setTitle("testHasAppointmentFullTime");
        c.setTime(start);
        final int posInArray = 3;
        c.add(Calendar.DATE, posInArray);
        appointment.setStartDate(c.getTime());
        c.add(Calendar.DATE, 1);
        appointment.setEndDate(c.getTime());
        appointment.setShownAs(Appointment.ABSENT);
        appointment.setFullTime(true);
        appointment.setParentFolderID(folderId);
        appointment.setIgnoreConflicts(true);
        final AppointmentInsertResponse insertR = client.execute(new InsertRequest(appointment, tz));
        insertR.fillAppointment(appointment);
        try {
            final HasResponse hasR = client.execute(new HasRequest(start, end, tz));
            final boolean[] hasAppointments = hasR.getValues();
            assertEquals("Length of array of action has is wrong.", hasInterval, hasAppointments.length);
            assertEquals("Inserted appointment not found in action has response.", hasAppointments[posInArray], true);
        } finally {
            client.execute(new DeleteRequest(appointment));
        }
    }
}
