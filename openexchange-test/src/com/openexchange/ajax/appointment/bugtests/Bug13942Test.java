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
import java.util.Date;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.ConfirmRequest;
import com.openexchange.ajax.appointment.action.ConfirmResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.UserParticipant;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13942Test extends AbstractAJAXSession {

    private Appointment appointment, updateAppointment;

    private int userIdA, userIdB, userIdC;

    private AJAXClient clientB, clientC;

    public Bug13942Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        userIdA = getClient().getValues().getUserId();
        userIdB = getClientB().getValues().getUserId();
        userIdC = getClientC().getValues().getUserId();

        appointment = new Appointment();
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setTitle("Test Bug 13942");
        appointment.setStartDate(new Date(TimeTools.getHour(0, getClient().getValues().getTimeZone())));
        appointment.setEndDate(new Date(TimeTools.getHour(1, getClient().getValues().getTimeZone())));
        appointment.setParticipants(ParticipantTools.createParticipants(userIdA, userIdB, userIdC));
        appointment.setIgnoreConflicts(true);

        InsertRequest request = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse response = getClient().execute(request);
        response.fillObject(appointment);

        ConfirmRequest confirmRequest = new ConfirmRequest(getClientB().getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), CalendarObject.ACCEPT, "yap", appointment.getLastModified(), true);
        ConfirmResponse confirmResponse = getClientB().execute(confirmRequest);
        appointment.setLastModified(confirmResponse.getTimestamp());

        updateAppointment = new Appointment();
        updateAppointment.setObjectID(appointment.getObjectID());
        updateAppointment.setParentFolderID(getClientB().getValues().getPrivateAppointmentFolder());
        updateAppointment.setLastModified(appointment.getLastModified());
        updateAppointment.setAlarm(30);
    }

    @Override
    public void tearDown() throws Exception {
        if (appointment != null && appointment.getObjectID() != 0) {
            DeleteRequest delete = new DeleteRequest(appointment);
            getClient().execute(delete);
        }
        super.tearDown();
    }

    public void testBug13942() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(updateAppointment, getClientB().getValues().getTimeZone());
        UpdateResponse updateResponse = getClientB().execute(updateRequest);
        appointment.setLastModified(updateResponse.getTimestamp());
        GetRequest getRequest = new GetRequest(getClientB().getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        GetResponse getResponse = getClientB().execute(getRequest);
        Appointment loadedAppointment = getResponse.getAppointment(getClientB().getValues().getTimeZone());
        for (UserParticipant user : loadedAppointment.getUsers()) {
            if (user.getIdentifier() == userIdB) {
                assertEquals("Lost confirmation status", CalendarObject.ACCEPT, user.getConfirm());
            }
        }

    }

    private AJAXClient getClientB() throws OXException, OXException, IOException, SAXException, JSONException {
        if (clientB == null) {
            clientB = new AJAXClient(User.User2);
        }
        return clientB;
    }

    private AJAXClient getClientC() throws OXException, OXException, IOException, SAXException, JSONException {
        if (clientC == null) {
            clientC = new AJAXClient(User.User3);
        }
        return clientC;
    }
}
