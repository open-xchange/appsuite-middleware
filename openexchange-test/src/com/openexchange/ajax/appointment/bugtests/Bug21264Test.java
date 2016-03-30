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

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;

/**
 * Verifies that the new iTIP/iMIP implementation properly deletes appointment if the last internal participants tries to delete it.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug21264Test extends AbstractAJAXSession {

    @SuppressWarnings("hiding")
    private AJAXClient client;
    private TimeZone timeZone;
    private Appointment app;

    public Bug21264Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        timeZone = client.getValues().getTimeZone();
        app = new Appointment();
        app.setTitle("Test for bug 21264");
        final Calendar cal = TimeTools.createCalendar(timeZone);
        app.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        app.setEndDate(cal.getTime());
        app.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        app.setParticipants(new Participant[] { new UserParticipant(client.getValues().getUserId()), new ExternalUserParticipant("user1@example.org") });
        app.setOrganizer("user2@example.org");
        app.setIgnoreConflicts(true);
        final AppointmentInsertResponse response = client.execute(new com.openexchange.ajax.appointment.action.InsertRequest(app, timeZone));
        response.fillAppointment(app);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != app) {
            client.execute(new DeleteRequest(app));
        }
        super.tearDown();
    }

    public void testDeleteAppointment() throws IOException, JSONException, OXException {
        final CommonDeleteResponse response = client.execute(new DeleteRequest(app, false));
        assertFalse("Deleting appointment failed.", response.hasError());
        app = null;
    }
}
