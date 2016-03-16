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

package com.openexchange.ajax.appointment.recurrence;

import static com.openexchange.calendar.storage.ParticipantStorage.extractExternal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.SearchRequest;
import com.openexchange.ajax.appointment.action.SearchResponse;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.parser.ParticipantParser;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * Checks if the calendar component correctly fills the confirmations JSON appointment attributes.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConfirmationsSeriesTest extends AbstractAJAXSession {

    private static final int[] COLUMNS = { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.CONFIRMATIONS };
    private AJAXClient client;
    private int folderId;
    private TimeZone tz;
    private Appointment appointment;
    private ExternalUserParticipant participant;

    public ConfirmationsSeriesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateAppointmentFolder();
        tz = client.getValues().getTimeZone();
        appointment = new Appointment();
        appointment.setTitle("Test series appointment for testing confirmations");
        Calendar calendar = TimeTools.createCalendar(tz);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setParentFolderID(folderId);
        appointment.setIgnoreConflicts(true);
        participant = new ExternalUserParticipant("external1@example.com");
        participant.setDisplayName("External user");
        appointment.addParticipant(participant);
        participant = new ExternalUserParticipant("external2@example.com");
        participant.setDisplayName("External user 2");
        appointment.addParticipant(participant);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        client.execute(new InsertRequest(appointment, tz)).fillAppointment(appointment);
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(appointment));
        super.tearDown();
    }

    public void testChangeException() throws Throwable {
        GetResponse response1 = client.execute(new GetRequest(appointment.getParentFolderID(), appointment.getObjectID(), 3));
        Appointment occurrence3 = response1.getAppointment(tz);
        Appointment changeException = new Appointment();
        changeException.setObjectID(appointment.getObjectID());
        changeException.setParentFolderID(appointment.getParentFolderID());
        changeException.setLastModified(appointment.getLastModified());
        changeException.setRecurrencePosition(occurrence3.getRecurrencePosition());
        changeException.setTitle("Change exception appointment for testing confirmations");
        changeException.setIgnoreConflicts(true);
        UpdateResponse response2 = client.execute(new UpdateRequest(changeException, tz));
        appointment.setLastModified(response2.getTimestamp());
        GetResponse response3 = client.execute(new GetRequest(appointment));
        checkConfirmations(extractExternal(appointment.getParticipants()), response3.getAppointment(tz).getConfirmations());
        CommonAllResponse response4 = client.execute(new AllRequest(folderId, COLUMNS, occurrence3.getStartDate(), occurrence3.getEndDate(), tz));
        checkConfirmations(extractExternal(appointment.getParticipants()), findConfirmations(response4, response2.getId()));
        CommonListResponse response5 = client.execute(new ListRequest(ListIDs.l(new int[] { folderId, response2.getId() }), COLUMNS));
        checkConfirmations(extractExternal(appointment.getParticipants()), findConfirmations(response5, response2.getId()));
        SearchResponse response6 = client.execute(new SearchRequest("*", folderId, COLUMNS));
        checkConfirmations(extractExternal(appointment.getParticipants()), findConfirmations(response6, response2.getId()));
    }

    private JSONArray findConfirmations(AbstractColumnsResponse response, int id) {
        int objectIdPos = response.getColumnPos(Appointment.OBJECT_ID);
        int confirmationsPos = response.getColumnPos(Appointment.CONFIRMATIONS);
        JSONArray jsonConfirmations = null;
        for (Object[] tmp : response) {
            if (id == ((Integer) tmp[objectIdPos]).intValue()) {
                jsonConfirmations = (JSONArray) tmp[confirmationsPos];
            }
        }
        return jsonConfirmations;
    }

    private void checkConfirmations(ExternalUserParticipant[] expected, JSONArray jsonConfirmations) throws JSONException {
        assertNotNull("Response does not contain confirmations.", jsonConfirmations);
        ParticipantParser parser = new ParticipantParser();
        List<ConfirmableParticipant> confirmations = new ArrayList<ConfirmableParticipant>();
        int length = jsonConfirmations.length();
        for (int i = 0; i < length; i++) {
            JSONObject jsonConfirmation = jsonConfirmations.getJSONObject(i);
            confirmations.add(parser.parseConfirmation(true, jsonConfirmation));
        }
        checkConfirmations(expected, confirmations.toArray(new ConfirmableParticipant[confirmations.size()]));
    }

    private void checkConfirmations(ExternalUserParticipant[] expected, ConfirmableParticipant[] actual) {
        assertNotNull("Response does not contain any confirmations.", actual);
        // Following expected must be one more if internal user participants get its way into the confirmations array.
        assertEquals("Number of external participant confirmations does not match.", expected.length, actual.length);
        Arrays.sort(expected);
        Arrays.sort(actual);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Mailaddress of external participant does not match.", expected[i].getEmailAddress(), actual[i].getEmailAddress());
            assertEquals("Display name of external participant does not match.", expected[i].getDisplayName(), actual[i].getDisplayName());
            assertEquals("Confirm status does not match.", expected[i].getStatus(), actual[i].getStatus());
            assertEquals("Confirm message does not match.", expected[i].getMessage(), actual[i].getMessage());
        }
    }
}
