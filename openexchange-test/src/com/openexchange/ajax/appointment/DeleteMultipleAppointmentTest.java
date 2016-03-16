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

import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.Appointment;


/**
 * {@link DeleteMultipleAppointmentTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeleteMultipleAppointmentTest extends AppointmentTest {
    
    private AJAXClient client;
    private Appointment appointment1, appointment2;

    /**
     * Initializes a new {@link DeleteMultipleAppointmentTest}.
     * @param name
     */
    public DeleteMultipleAppointmentTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(User.User1);

        appointment1 = new Appointment();
        appointment1.setIgnoreConflicts(true);
        appointment1.setTitle("Test 1");
        appointment1.setTimezone(timeZone.getDisplayName());
        appointment1.setStartDate(new Date());
        appointment1.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *2));
        appointment1.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        InsertRequest insReq1 = new InsertRequest(appointment1, timeZone);
        AppointmentInsertResponse insRes1 = client.execute(insReq1);
        insRes1.fillAppointment(appointment1);
        
        appointment2 = new Appointment();
        appointment2.setIgnoreConflicts(true);
        appointment2.setTitle("Test 2");
        appointment2.setTimezone(timeZone.getDisplayName());
        appointment2.setStartDate(new Date());
        appointment2.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *2));
        appointment2.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        InsertRequest insReq2 = new InsertRequest(appointment2, timeZone);
        AppointmentInsertResponse insRes2 = client.execute(insReq2);
        insRes2.fillAppointment(appointment2);
    }
    
    @Override
    public void tearDown() throws Exception {
        GetRequest getReq1 = new GetRequest(appointment1, false);
        GetResponse getRes1 = client.execute(getReq1);
        if (!getRes1.hasError()) {
            DeleteRequest delReq = new DeleteRequest(appointment1);
            client.execute(delReq);
        }

        GetRequest getReq2 = new GetRequest(appointment2, false);
        GetResponse getRes2 = client.execute(getReq2);
        if (!getRes2.hasError()) {
            DeleteRequest delReq = new DeleteRequest(appointment2);
            client.execute(delReq);
        }

        super.tearDown();
    }
    
    @Test
    public void testDeleteMultiple() throws Exception {
        int[] ids = new int[] {appointment1.getObjectID(), appointment2.getObjectID()};
        DeleteRequest delReq = new DeleteRequest(ids, client.getValues().getPrivateAppointmentFolder(), new Date(System.currentTimeMillis() + 300000L), true);
        CommonDeleteResponse delRes = client.execute(delReq);
        assertFalse("Multiple delete failed: " + delRes.getErrorMessage(), delRes.hasError());
    }
}
