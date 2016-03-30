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
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13788Test extends AbstractAJAXSession {

    private Appointment appointment, update;

    public Bug13788Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setTitle("Bug 13788 Test");
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setStartDate(D("01.10.2009 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setEndDate(D("02.10.2009 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setFullTime(true);
        appointment.setIgnoreConflicts(true);

        InsertRequest appointmentInsertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse appointmentInsertResponse = getClient().execute(appointmentInsertRequest);
        appointmentInsertResponse.fillAppointment(appointment);

        update = new Appointment();
        update.setObjectID(appointment.getObjectID());
        update.setParentFolderID(appointment.getParentFolderID());
        update.setLastModified(appointment.getLastModified());
        update.setIgnoreConflicts(true);
        update.setStartDate(D("03.10.2009 00:00", TimeZone.getTimeZone("UTC")));
        update.setEndDate(D("04.10.2009 00:00", TimeZone.getTimeZone("UTC")));
    }

    public void testBug13788() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(update, client.getValues().getTimeZone());
        UpdateResponse updateResponse = client.execute(updateRequest);
        appointment.setLastModified(updateResponse.getTimestamp());

        GetRequest getRequest = new GetRequest(appointment.getParentFolderID(), appointment.getObjectID());
        GetResponse getResponse = client.execute(getRequest);

        Appointment loadedAppointment = getResponse.getAppointment(client.getValues().getTimeZone());
        assertTrue("Lost fulltime flag.", loadedAppointment.getFullTime());
    }

    @Override
    protected void tearDown() throws Exception {
        DeleteRequest appointmentDeleteRequest = new DeleteRequest(appointment);
        getClient().execute(appointmentDeleteRequest);

        super.tearDown();
    }

}
