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

import static com.openexchange.java.Autoboxing.L;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * This class contains test methods of calendar problems described by Funambol.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FunambolTest extends AbstractAJAXSession {

    private AJAXClient client;
    private int folderId;
    private TimeZone timeZone;
    private List<Appointment> toDelete;

    public FunambolTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateAppointmentFolder();
        timeZone = client.getValues().getTimeZone();
        toDelete = new ArrayList<Appointment>();
    }

    private Appointment getAppointment() {
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(folderId);
        appointment.setTitle("TestCreationTime");
        appointment.setStartDate(new Date(TimeTools.getHour(0, timeZone)));
        appointment.setEndDate(new Date(TimeTools.getHour(1, timeZone)));
        appointment.setIgnoreConflicts(true);
        return appointment;
    }

    @Override
    protected void tearDown() throws Exception {
        for (Appointment app : toDelete) {
            client.execute(new DeleteRequest(app));
        }
        super.tearDown();
    }

    public void testAppointmentCreationTime() throws Throwable {
        Date lastModified = null;
        Date timeAfterCreation = null;
        Date timeBeforeCreation = null;
        Appointment reload = null;
        
        boolean potentialTimestamp = false;
        while (!potentialTimestamp) {
            Appointment appointment = getAppointment();
            // Sometimes requests are really, really fast and time of first insert is same as this time. Maybe it is more a problem of XEN and a
            // frozen clock there.
            timeBeforeCreation = new Date(client.getValues().getServerTime().getTime() - 1);
    
            final CommonInsertResponse insertResponse = client.execute(new InsertRequest(appointment, timeZone));
            insertResponse.fillObject(appointment);
            final GetResponse response = client.execute(new GetRequest(appointment));
            reload = response.getAppointment(timeZone);
            // reload.getCreationDate() does not have milliseconds.
            lastModified = reload.getLastModified();
    
            // This request is responded even faster than creating an appointment. Therefore this must be the second request.
            timeAfterCreation = new Date(client.getValues().getServerTime().getTime() + 1);
            
            System.out.println(lastModified.getTime());
            if (lastModified.getTime() % 1000 >= 500) {
                potentialTimestamp = true;
            }
            toDelete.add(appointment);
        }

        assertTrue("Appointment creation time is not after time request before creation.", lastModified.after(timeBeforeCreation));
        assertTrue("Appointment creation time is not before time request after creation.", lastModified.before(timeAfterCreation));
        assertEquals(
            "Last modified and creation date are different.",
            L(lastModified.getTime() / 1000),
            L(reload.getCreationDate().getTime() / 1000));
    }
}
