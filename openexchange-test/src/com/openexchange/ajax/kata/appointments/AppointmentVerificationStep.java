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

package com.openexchange.ajax.kata.appointments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.HasRequest;
import com.openexchange.ajax.appointment.action.HasResponse;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.SearchRequest;
import com.openexchange.ajax.appointment.action.SearchResponse;
import com.openexchange.ajax.appointment.action.UpdatesRequest;
import com.openexchange.ajax.appointment.action.AppointmentUpdatesResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.kata.NeedExistingStep;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link AppointmentVerificationStep}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AppointmentVerificationStep extends NeedExistingStep<Appointment> {

    private final Appointment entry;

    private CalendarTestManager manager;

    private int expectedFolderId;

    /**
     * Initializes a new {@link AppointmentVerificationStep}.
     *
     * @param entry
     */
    public AppointmentVerificationStep(Appointment entry, String name) {
        super(name, null);
        this.entry = entry;
    }

    @Override
    public void cleanUp() throws Exception {

    }

    @Override
    protected void assumeIdentity(Appointment thing) {
        expectedFolderId = entry.getParentFolderID();
        boolean containsFolderId = entry.containsParentFolderID();
        super.assumeIdentity(entry);
        if (!containsFolderId) {
            expectedFolderId = entry.getParentFolderID();
        }
    }

    @Override
    public void perform(AJAXClient client) throws Exception {
        this.client = client;
        this.manager = new CalendarTestManager(client);
        assumeIdentity(entry);

        checkWithReadMethods(entry);
    }

    private void checkWithReadMethods(Appointment appointment) throws OXException, JSONException, OXException, IOException, SAXException {
        checkViaGet(appointment);
        checkViaAll(appointment);
        checkViaList(appointment);
        checkViaUpdates(appointment);
        checkViaSearch(appointment);
        checkViaHas(appointment);
    }

    private void checkViaGet(Appointment appointment) throws OXException, JSONException {
        Appointment loaded = manager.get(expectedFolderId, appointment.getObjectID());
        compare(appointment, loaded);
    }

    private void checkViaAll(Appointment appointment) throws OXException, IOException, SAXException, JSONException {
        Object[][] rows = getViaAll(appointment);

        checkInList(appointment, rows, Appointment.ALL_COLUMNS);
    }

    private void checkViaList(Appointment appointment) throws OXException, IOException, SAXException, JSONException {
        ListRequest listRequest = new ListRequest(
            ListIDs.l(new int[] { expectedFolderId, appointment.getObjectID() }),
            Appointment.ALL_COLUMNS);
        CommonListResponse response = client.execute(listRequest);

        Object[][] rows = response.getArray();

        checkInList(appointment, rows, Appointment.ALL_COLUMNS);
    }

    private void checkViaUpdates(Appointment appointment) throws OXException, IOException, SAXException, JSONException, OXException {
        UpdatesRequest updates = new UpdatesRequest(expectedFolderId, Appointment.ALL_COLUMNS, new Date(0), true);
        AppointmentUpdatesResponse response = client.execute(updates);

        List<Appointment> appointments = response.getAppointments(getTimeZone());

        checkInList(appointment, appointments);
    }

    private void checkViaSearch(Appointment appointment) throws OXException, IOException, SAXException, JSONException {
        Object[][] rows = getViaSearch(appointment);
        checkInList(appointment, rows, Appointment.ALL_COLUMNS);
    }

    private void checkViaHas(Appointment appointment) throws OXException, IOException, SAXException, JSONException {
        HasRequest hasRequest = new HasRequest(appointment.getStartDate(), appointment.getEndDate(), getTimeZone());
        HasResponse hasResponse = client.execute(hasRequest);
        boolean[] values = hasResponse.getValues();
        for (int i = 0; i < values.length; i++) {
            Assert.assertTrue("Should return true for day " + i + " of the appointment", values[i]);
        }
    }

    private Object[][] getViaAll(Appointment appointment) throws OXException, IOException, SAXException, JSONException {
        long rangeStart = appointment.getStartDate().getTime() - 24 * 3600000;
        long rangeEnd = appointment.getEndDate().getTime() + 24 * 3600000;
        AllRequest all = new AllRequest(
            expectedFolderId,
            Appointment.ALL_COLUMNS,
            new Date(rangeStart),
            new Date(rangeEnd),
            getTimeZone(),
            true);
        CommonAllResponse response = client.execute(all);
        return response.getArray();
    }

    private Object[][] getViaSearch(Appointment appointment) throws OXException, IOException, SAXException, JSONException {
        SearchRequest searchRequest = new SearchRequest(
            "*",
            expectedFolderId,
            new Date(0),
            new Date(Integer.MAX_VALUE),
            Appointment.ALL_COLUMNS,
            -1,
            null,
            false,
            true); // TODO: Tierlieb - fix params
        SearchResponse searchResponse = client.execute(searchRequest);
        return searchResponse.getArray();
    }

    private void compare(Appointment appointment, Appointment loaded) {
        int[] columns = Appointment.ALL_COLUMNS;
        for (int i = 0; i < columns.length; i++) {
            int col = columns[i];

            if (col == CalendarObject.PARTICIPANTS && appointment.containsParticipants()) {
                Participant[] expected = appointment.getParticipants();
                Participant[] actual = loaded.getParticipants();
                if (!compareArrays(expected, actual)) {
                    throw new ParticipantComparisonFailure("Missing participant", expected, actual);
                }
                continue;
            }
            if (col == CalendarObject.USERS && appointment.containsUserParticipants()) {
                UserParticipant[] expected = appointment.getUsers();
                UserParticipant[] actual = loaded.getUsers();
                if (!compareArrays(expected, actual)) {
                    throw new UserParticipantComparisonFailure("Missing user", expected, actual);
                }
                continue;
            }

            if (col == DataObject.LAST_MODIFIED_UTC || col == DataObject.LAST_MODIFIED) {
                continue;
            }
            if (appointment.containsParentFolderID()) {
                assertEquals(name + " : Column " + col + " differs!", expectedFolderId, loaded.getParentFolderID());
                continue;
            }
            if (appointment.contains(col)) {
                assertEquals(name + ": Column " + col + " differs!", appointment.get(col), loaded.get(col));
            }
        }
    }

    private void checkInList(Appointment appointment, Object[][] rows, int[] columns) throws OXException, IOException, SAXException, JSONException {
        int idPos = findIDIndex(columns);

        for (int i = 0; i < rows.length; i++) {
            Object[] row = rows[i];
            int id = ((Integer) row[idPos]).intValue();
            if (id == appointment.getObjectID()) {
                compare(appointment, row, columns);
                return;
            }
        }

        fail("Object not found in response. " + name);

    }

    private void compare(Appointment appointment, Object[] row, int[] columns) throws OXException, IOException, SAXException, JSONException {
        for (int i = 0; i < columns.length; i++) {
            int column = columns[i];
            if (column == DataObject.LAST_MODIFIED_UTC || column == DataObject.LAST_MODIFIED) {
                continue;
            }
            if (column == CalendarObject.PARTICIPANTS && appointment.containsParticipants()) {
                Participant[] expected = appointment.getParticipants();
                Participant[] actual = (Participant[]) transform(column, row[i]);
                if (!compareArrays(expected, actual)) {
                    throw new ParticipantComparisonFailure("", expected, actual);
                }
                continue;
            }
            if (column == CalendarObject.USERS && appointment.containsUserParticipants()) {
                UserParticipant[] expected = appointment.getUsers();
                UserParticipant[] actual = (UserParticipant[]) transform(column, row[i]);
                if (!compareArrays(expected, actual)) {
                    throw new UserParticipantComparisonFailure("", expected, actual);
                }
                continue;
            }
            if( column == CalendarObject.FOLDER_ID){
                assertEquals(name + " Column: " + column, Integer.valueOf(expectedFolderId), row[i]);
                continue;
            }
            if (appointment.contains(column)) {
                Object expected = appointment.get(column);
                Object actual = row[i];
                actual = transform(column, actual);
                assertEquals(name + " Column: " + column, expected, actual);
            }
        }
    }

    private void checkInList(Appointment appointment, List<Appointment> appointments) {
        for (Appointment appointmentFromList : appointments) {
            if (appointmentFromList.getObjectID() == appointment.getObjectID()) {
                compare(appointment, appointmentFromList);
                return;
            }
        }

        fail("Object not found in response. " + name);
    }

    private int findIDIndex(int[] columns) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == Appointment.OBJECT_ID) {
                return i;
            }
        }
        fail("No ID column requested. This won't work. " + name);
        return -1;
    }

    protected <T> boolean compareArrays(T[] expected, T[] actual) {
        if (expected == null && actual == null){
            return true;
        }
        if (expected == null && actual != null){
            return false;
        }
        if (expected != null && actual == null){
            return false;
        }
        Set<T> expectedParticipants = new HashSet<T>(Arrays.asList(expected));
        Set<T> actualParticipants = new HashSet<T>(Arrays.asList(actual));
        if (expectedParticipants.size() != actualParticipants.size()){
            return false;
        }
        if (!expectedParticipants.containsAll(actualParticipants)){
            return false;
        }
        return true;
    }

    protected Object transform(int column, Object actual) throws OXException, IOException, SAXException, JSONException {
        switch (column) {

        case Appointment.START_DATE:
        case Appointment.END_DATE:
            int offset = getTimeZone().getOffset(((Long) actual).longValue());
            return new Date(((Long) actual).longValue() - offset);

        case Appointment.PARTICIPANTS:
            JSONArray participantArr = (JSONArray) actual;
            List<Participant> participants = new LinkedList<Participant>();
            for (int i = 0, size = participantArr.length(); i < size; i++) {
                JSONObject participantObj = participantArr.getJSONObject(i);
                int type = participantObj.getInt("type");
                switch (type) {
                case Participant.USER:
                    participants.add(new UserParticipant(participantObj.getInt("id")));
                    break;
                case Participant.GROUP:
                    participants.add(new GroupParticipant(participantObj.getInt("id")));
                    break;
                case Participant.EXTERNAL_USER:
                    participants.add(new ExternalUserParticipant(participantObj.getString("mail")));
                    break;
                }
            }
            return participants.toArray(new Participant[participants.size()]);

        case Appointment.USERS:
            JSONArray userParticipantArr = (JSONArray) actual;
            List<UserParticipant> userParticipants = new LinkedList<UserParticipant>();
            for (int i = 0, size = userParticipantArr.length(); i < size; i++) {
                JSONObject participantObj = userParticipantArr.getJSONObject(i);
                userParticipants.add(new UserParticipant(participantObj.getInt("id")));
            }
            return userParticipants.toArray(new UserParticipant[userParticipants.size()]);
        }

        return actual;
    }

}
