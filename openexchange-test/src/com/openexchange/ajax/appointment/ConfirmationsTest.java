/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.SearchRequest;
import com.openexchange.ajax.appointment.action.SearchResponse;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.appointment.helper.ParticipantStorage;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.parser.ParticipantParser;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * Checks if the calendar component correctly fills the confirmations JSON appointment attributes.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConfirmationsTest extends AbstractAJAXSession {

    private static final int[] COLUMNS = { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.CONFIRMATIONS };
    private int folderId;
    private TimeZone tz;
    private Appointment appointment;
    private ExternalUserParticipant participant;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderId = getClient().getValues().getPrivateAppointmentFolder();
        tz = getClient().getValues().getTimeZone();
        appointment = new Appointment();
        appointment.setTitle("Test appointment for testing confirmations");
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
        getClient().execute(new InsertRequest(appointment, tz)).fillAppointment(appointment);
    }

    @Test
    public void testGet() throws Throwable {
        GetResponse response = getClient().execute(new GetRequest(appointment));
        Appointment test = response.getAppointment(tz);
        checkConfirmations(ParticipantStorage.extractExternal(appointment.getParticipants()), test.getConfirmations());
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

    @Test
    public void testAll() throws Throwable {
        Date rangeStart = TimeTools.getAPIDate(tz, appointment.getStartDate(), 0);
        Date rangeEnd = TimeTools.getAPIDate(tz, appointment.getEndDate(), 1);
        CommonAllResponse response = getClient().execute(new AllRequest(folderId, COLUMNS, rangeStart, rangeEnd, tz));
        checkConfirmations(ParticipantStorage.extractExternal(appointment.getParticipants()), findConfirmations(response));
    }

    private JSONArray findConfirmations(AbstractColumnsResponse response) {
        int objectIdPos = response.getColumnPos(Appointment.OBJECT_ID);
        int confirmationsPos = response.getColumnPos(Appointment.CONFIRMATIONS);
        JSONArray jsonConfirmations = null;
        for (Object[] tmp : response) {
            if (appointment.getObjectID() == ((Integer) tmp[objectIdPos]).intValue()) {
                jsonConfirmations = (JSONArray) tmp[confirmationsPos];
            }
        }
        return jsonConfirmations;
    }

    @Test
    public void testList() throws Throwable {
        CommonListResponse response = getClient().execute(new ListRequest(ListIDs.l(new int[] { folderId, appointment.getObjectID() }), COLUMNS));
        checkConfirmations(ParticipantStorage.extractExternal(appointment.getParticipants()), findConfirmations(response));
    }

    @Test
    public void testSearch() throws Throwable {
        SearchResponse response = getClient().execute(new SearchRequest("*", folderId, COLUMNS));
        checkConfirmations(ParticipantStorage.extractExternal(appointment.getParticipants()), findConfirmations(response));
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

    @Test
    public void testUpdate() throws Throwable {
        Appointment updated = new Appointment();
        updated.setObjectID(appointment.getObjectID());
        updated.setParentFolderID(appointment.getParentFolderID());
        updated.setLastModified(appointment.getLastModified());
        updated.setTitle("Updated test appointment for testing confirmations");
        updated.setIgnoreConflicts(true);
        participant = new ExternalUserParticipant("external1@example.com");
        participant.setDisplayName("External user");
        updated.addParticipant(participant);
        participant = new ExternalUserParticipant("external3@example.com");
        participant.setDisplayName("External user 3");
        updated.addParticipant(participant);
        UpdateResponse response = getClient().execute(new UpdateRequest(updated, tz));
        appointment.setLastModified(response.getTimestamp());
        GetResponse response2 = getClient().execute(new GetRequest(appointment));
        checkConfirmations(ParticipantStorage.extractExternal(updated.getParticipants()), response2.getAppointment(tz).getConfirmations());
        Date rangeStart = TimeTools.getAPIDate(tz, appointment.getStartDate(), 0);
        Date rangeEnd = TimeTools.getAPIDate(tz, appointment.getEndDate(), 1);
        CommonAllResponse response3 = getClient().execute(new AllRequest(folderId, COLUMNS, rangeStart, rangeEnd, tz));
        checkConfirmations(ParticipantStorage.extractExternal(updated.getParticipants()), findConfirmations(response3));
        CommonListResponse response4 = getClient().execute(new ListRequest(ListIDs.l(new int[] { folderId, appointment.getObjectID() }), COLUMNS));
        checkConfirmations(ParticipantStorage.extractExternal(updated.getParticipants()), findConfirmations(response4));
        SearchResponse response5 = getClient().execute(new SearchRequest("*", folderId, COLUMNS));
        checkConfirmations(ParticipantStorage.extractExternal(updated.getParticipants()), findConfirmations(response5));
    }
}
