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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;

/**
 * {@link Bug21614Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug21614Test extends AbstractAJAXSession {

    private Appointment appointment;

    private AJAXClient clientA;

    private AJAXClient clientB;

    public Bug21614Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = new AJAXClient(User.User2);

        List<Participant> participants = new ArrayList<Participant>();
        Participant p = new UserParticipant(clientB.getValues().getUserId());
        participants.add(p);

        appointment = new Appointment();
        appointment.setParentFolderID(clientA.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug 21614");
        appointment.setStartDate(D("16.04.2012 08:00"));
        appointment.setEndDate(D("16.04.2012 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setOccurrence(5);
        appointment.setInterval(1);
        appointment.setParticipants(participants);
    }
    
    public void testBug21614() throws Exception {
        InsertRequest insertRequest = new InsertRequest(appointment, clientA.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientA.execute(insertRequest);
        insertResponse.fillObject(appointment);
        
        DeleteRequest deleteRequest = new DeleteRequest(appointment.getObjectID(), clientA.getValues().getPrivateAppointmentFolder(), 5, appointment.getLastModified());
        CommonDeleteResponse deleteResponse = clientA.execute(deleteRequest);
        appointment.setLastModified(deleteResponse.getTimestamp());
        
        assertNotFind(clientA);
        assertNotFind(clientB);
        
        deleteRequest = new DeleteRequest(appointment.getObjectID(), clientB.getValues().getPrivateAppointmentFolder(), 2, appointment.getLastModified());
        try {
            deleteResponse = clientB.execute(deleteRequest);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        appointment.setLastModified(new Date(Long.MAX_VALUE));

        assertNotFind(clientA);
        assertNotFind(clientB);
    }

    private void assertNotFind(AJAXClient c) throws IOException, JSONException, OXException {
        AllRequest allRequest = new AllRequest(c.getValues().getPrivateAppointmentFolder(), new int[] {Appointment.OBJECT_ID}, new Date(1334880000000L), new Date(1334966400000L), TimeZone.getTimeZone("UTC"), false);
        CommonAllResponse allResponse = c.execute(allRequest);
        
        boolean found = false;
        Object[][] objects = allResponse.getArray();
        for (Object[] object : objects) {
            if ((Integer)object[0] == appointment.getObjectID()) {
                found = true;
            }
        }
        assertFalse("Should not find the appointment.", found);
    }

    @Override
    public void tearDown() throws Exception {
        getClient().execute(new DeleteRequest(appointment));
        super.tearDown();
    }

}
