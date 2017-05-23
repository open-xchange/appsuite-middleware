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

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link CreatedByTest}
 *
 * Tests if it is possible to inject a value for the created_by field.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class CreatedByTest extends AbstractAJAXSession {

    private Appointment appointment;

    @Before
    public void setUp() throws Exception {
        super.setUp();        

        appointment = new Appointment();
        appointment.setTitle("Created by Test");
        appointment.setStartDate(D("07.12.2010 09:00"));
        appointment.setEndDate(D("07.12.2010 10:00"));
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setCreatedBy(getClient2().getValues().getUserId());
        appointment.setIgnoreConflicts(true);
    }

    @Test
    public void testInjectedCreatedBy() throws Exception {
        InsertRequest request = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(request);
        insertResponse.fillObject(appointment);

        GetRequest getRequest = new GetRequest(appointment);
        GetResponse getResponse = getClient().execute(getRequest);
        Appointment loadAppointment = getResponse.getAppointment(getClient().getValues().getTimeZone());

        assertEquals("Wrong created by", getClient().getValues().getUserId(), loadAppointment.getCreatedBy());
    }

    @After
    public void tearDown() throws Exception {
        try {
            getClient().execute(new DeleteRequest(appointment.getObjectID(), appointment.getParentFolderID(), appointment.getLastModified()));
        } finally {
            super.tearDown();
        }
    }

}
