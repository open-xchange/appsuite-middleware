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
import org.json.JSONArray;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug17175Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug17175Test extends AbstractAJAXSession {

    private Appointment appointment;
    private Appointment updateAppointment;

    /**
     * Initializes a new {@link Bug17175Test}.
     *
     * @param name
     */
    public Bug17175Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setTitle("Bug 17175 Test");
        appointment.setStartDate(D("07.10.2010 09:00"));
        appointment.setEndDate(D("07.10.2010 10:00"));
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(Appointment.THURSDAY);
        appointment.setInterval(1);
        appointment.setOccurrence(2);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        InsertRequest request = new InsertRequest(appointment, client.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = client.execute(request);
        insertResponse.fillObject(appointment);

        updateAppointment = new Appointment();
        insertResponse.fillObject(updateAppointment);
        updateAppointment.setParentFolderID(appointment.getParentFolderID());
        updateAppointment.setRecurrenceType(Appointment.WEEKLY);
        updateAppointment.setDays(Appointment.THURSDAY);
        updateAppointment.setInterval(1);
        updateAppointment.setOccurrence(0);
        updateAppointment.setIgnoreConflicts(true);
    }

    public void testBug17175() throws Exception {
        UpdateResponse updateResponse = client.execute(new UpdateRequest(updateAppointment, client.getValues().getTimeZone()));
        appointment.setLastModified(updateResponse.getTimestamp());

        int[] columns = new int[] { Appointment.OBJECT_ID, Appointment.START_DATE, Appointment.END_DATE };
        AllRequest allRequest = new AllRequest(client.getValues().getPrivateAppointmentFolder(), columns, D("01.11.2010 00:00"), D("01.12.2010 00:00"), client.getValues().getTimeZone(), false);
        CommonAllResponse allResponse = client.execute(allRequest);
        JSONArray json = (JSONArray) allResponse.getData();
        int count = 0;
        for (int i = 0; i < json.length(); i++) {
            if (json.getJSONArray(i).getInt(0) == appointment.getObjectID()) {
                count++;
            }
        }

        assertEquals("Wrong amount of occurrences", 4, count);
    }

    @Override
    protected void tearDown() throws Exception {
        getClient().execute(new DeleteRequest(appointment.getObjectID(), appointment.getParentFolderID(), appointment.getLastModified()));
        super.tearDown();
    }
}
