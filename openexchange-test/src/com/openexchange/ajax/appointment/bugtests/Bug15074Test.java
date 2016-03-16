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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Calendar;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug15074Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug15074Test extends AbstractAJAXSession {

    private Appointment appointment;

    /**
     * Initializes a new {@link Bug15074Test}.
     *
     * @param name
     */
    public Bug15074Test(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajax.framework.AbstractAJAXSession#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setTitle("Bug 15074 Test");
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setStartDate(D("07.12.2007 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setEndDate(D("08.12.2007 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setFullTime(true);
        appointment.setRecurrenceType(Appointment.YEARLY);
        appointment.setInterval(1);
        appointment.setDayInMonth(7);
        appointment.setMonth(Calendar.DECEMBER);
        appointment.setIgnoreConflicts(true);

        InsertRequest appointmentInsertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse appointmentInsertResponse = getClient().execute(appointmentInsertRequest);
        appointmentInsertResponse.fillAppointment(appointment);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajax.framework.AbstractAJAXSession#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        DeleteRequest appointmentDeleteRequest = new DeleteRequest(appointment);
        getClient().execute(appointmentDeleteRequest);

        super.tearDown();
    }

    public void testBug() throws Exception {

        int[] columns = new int[] { Appointment.OBJECT_ID };

        AllRequest allRequest = new AllRequest(client.getValues().getPrivateAppointmentFolder(),
            columns,
            D("01.12.2009 00:00", TimeZone.getTimeZone("UTC")),
            D("01.01.2010 00:00", TimeZone.getTimeZone("UTC")),
            TimeZone.getTimeZone("UTC"),
            false);

        CommonAllResponse allResponse = client.execute(allRequest);
        Object[][] objects = allResponse.getArray();
        boolean found = false;
        for (Object[] object : objects) {
            if ((Integer)object[0] == appointment.getObjectID()) {
                found = true;
                break;
            }
        }

        assertTrue("Unable to find appointment in this month.", found);

    }

}
