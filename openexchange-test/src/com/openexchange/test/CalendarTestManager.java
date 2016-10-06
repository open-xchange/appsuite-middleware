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

package com.openexchange.test;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i2I;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.AppointmentUpdatesResponse;
import com.openexchange.ajax.appointment.action.ConfirmRequest;
import com.openexchange.ajax.appointment.action.ConfirmResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetChangeExceptionsRequest;
import com.openexchange.ajax.appointment.action.GetChangeExceptionsResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.HasRequest;
import com.openexchange.ajax.appointment.action.HasResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.NewAppointmentSearchRequest;
import com.openexchange.ajax.appointment.action.NewAppointmentSearchResponse;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.appointment.action.UpdatesRequest;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.parser.ParticipantParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * {@link CalendarTestManager}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a> - basic design
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - extension
 */
public class CalendarTestManager implements TestManager {

    private AJAXClient client;

    private final List<Appointment> createdEntities = new ArrayList<Appointment>();

    private TimeZone timezone;

    private AbstractAJAXResponse lastResponse;

    private boolean failOnError;

    private Exception lastException;

    private Date lastModification;

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public CalendarTestManager(AJAXClient client) {
        this.setClient(client);

        try {
            timezone = client.getValues().getTimeZone();
        } catch (OXException e) {
            // wait for finally block
        } catch (IOException e) {
            // wait for finally block
        } catch (JSONException e) {
            // wait for finally block
        } finally {
            if (timezone == null) {
                timezone = TimeZone.getTimeZone("Europe/Berlin");
            }
        }
    }

    public void setClient(AJAXClient client) {
        this.client = client;
    }

    public AJAXClient getClient() {
        return client;
    }

    public void setLastResponse(AbstractAJAXResponse lastResponse) {
        this.lastResponse = lastResponse;
    }

    @Override
    public AbstractAJAXResponse getLastResponse() {
        return lastResponse;
    }

    @Override
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public boolean getFailOnError() {
        return failOnError;
    }

    @Override
    public boolean doesFailOnError() {
        return getFailOnError();
    }

    public void setLastException(Exception lastException) {
        lastException.printStackTrace();
        this.lastException = lastException;
    }

    @Override
    public Exception getLastException() {
        return lastException;
    }

    @Override
    public boolean hasLastException() {
        return lastException != null;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

    public Date getLastModification() {
        return lastModification;
    }

    public int getPrivateFolder() throws OXException, IOException, SAXException, JSONException {
        return getClient().getValues().getPrivateAppointmentFolder();
    }

    @Override
    public void cleanUp() {
        boolean old = getFailOnError();
        setFailOnError(false); // switching off, because there are other ways to delete an appointment, for example creating enough delete
        // exceptions
        for (Appointment appointment : new ArrayList<Appointment>(createdEntities)) {
            delete(appointment, true);
        }
        setFailOnError(old);
    }

    private <T extends AbstractAJAXResponse> T execute(final AJAXRequest<T> request) {
        try {
            return getClient().execute(request);
        } catch (OXException e) {
            setLastException(e);
            if (failOnError) {
                fail("AjaxException during task creation: " + e.getLocalizedMessage());
            }
        } catch (IOException e) {
            setLastException(e);
            if (failOnError) {
                fail("IOException during task creation: " + e.getLocalizedMessage());
            }
        } catch (JSONException e) {
            setLastException(e);
            if (failOnError) {
                fail("JsonException during task creation: " + e.getLocalizedMessage());
            }
        }
        return null;
    }

    /*
     * Requests
     */
    public Appointment insert(Appointment appointment) {
        InsertRequest insertRequest = new InsertRequest(appointment, timezone, getFailOnError());
        AppointmentInsertResponse insertResponse = execute(insertRequest);
        extractInfo(insertResponse);
        insertResponse.fillAppointment(appointment);
        if (doesFailOnError() || appointment.getObjectID() != 0) {
            createdEntities.add(appointment);
        }
        return appointment;
    }

    public Appointment get(int parentFolderID, int objectID) throws OXException {
        GetRequest get = new GetRequest(parentFolderID, objectID, getFailOnError());
        GetResponse response = execute(get);
        extractInfo(response);
        return response.getAppointment(timezone);
    }

    public Appointment get(Appointment appointment) throws OXException {
        try {
            GetRequest get = new GetRequest(appointment, getFailOnError());
            GetResponse response = execute(get);
            extractInfo(response);
            return response.getAppointment(timezone);
        } catch (OXException e) {
            if (failOnError) {
                throw e;
            }
            return null;
        }
    }

    public Appointment get(int parentFolderID, int objectID, boolean pleaseFailOnError) throws OXException {
        try {
            GetRequest get = new GetRequest(parentFolderID, objectID, pleaseFailOnError);
            GetResponse response = execute(get);
            extractInfo(response);
            return response.getAppointment(timezone);
        } catch (OXException e) {
            if (failOnError) {
                throw e;
            }
            return null;
        }
    }

    public Appointment get(int parentFolderID, int objectID, int recurrencePos) throws OXException {
        try {
            GetRequest get = new GetRequest(parentFolderID, objectID, recurrencePos, getFailOnError());
            GetResponse response = execute(get);
            extractInfo(response);
            return response.getAppointment(timezone);
        } catch (OXException e) {
            if (failOnError) {
                throw e;
            }
            return null;
        }
    }

    public void confirm(Appointment app, int status, String message, int occurrence) {
        confirm(app.getParentFolderID(), app.getObjectID(), app.getLastModified(), status, message, occurrence);
    }

    public void confirm(int folderId, int objectId, Date timestamp, int status, String message, int occurrence) {
        ConfirmRequest confirmRequest = new ConfirmRequest(folderId, objectId, occurrence, status, message, 0, timestamp, getFailOnError());
        ConfirmResponse resp = execute(confirmRequest);
        setLastResponse(resp);
        setLastModification(resp.getTimestamp());
    }

    public void confirm(Appointment app, int status, String message) {
        confirm(app.getParentFolderID(), app.getObjectID(), app.getLastModified(), status, message);
    }

    public void confirm(int folderId, int objectId, Date timestamp, int status, String message) {
        ConfirmRequest confirmRequest = new ConfirmRequest(folderId, objectId, status, message, 0, timestamp, getFailOnError());
        ConfirmResponse resp = execute(confirmRequest);
        setLastResponse(resp);
        setLastModification(resp.getTimestamp());
    }

    public void confirm(Appointment app, int user, int status, String message) {
        confirm(app.getParentFolderID(), app.getObjectID(), app.getLastModified(), user, status, message);
    }

    public void confirm(int folderId, int objectId, Date timestamp, int user, int status, String message) {
        ConfirmRequest confirmRequest = new ConfirmRequest(folderId, objectId, status, message, user, timestamp, getFailOnError());
        ConfirmResponse resp = execute(confirmRequest);
        setLastResponse(resp);
        setLastModification(resp.getTimestamp());
    }

    public void confirmExternal(Appointment app, String mail, int status, String message, int occurrence) {
        confirmExternal(app.getParentFolderID(), app.getObjectID(), app.getLastModified(), mail, status, message, occurrence);
    }

    public void confirmExternal(int folderId, int objectId, Date timestamp, String mail, int status, String message, int occurrence) {
        ConfirmRequest confirmRequest = new ConfirmRequest(folderId, objectId, occurrence, status, message, mail, timestamp, getFailOnError());
        ConfirmResponse resp = execute(confirmRequest);
        setLastResponse(resp);
        setLastModification(resp.getTimestamp());
    }

    public void confirmExternal(Appointment app, String mail, int status, String message) {
        confirmExternal(app.getParentFolderID(), app.getObjectID(), app.getLastModified(), mail, status, message);
    }

    public void confirmExternal(int folderId, int objectId, Date timestamp, String mail, int status, String message) {
        ConfirmRequest confirmRequest = new ConfirmRequest(folderId, objectId, status, message, mail, timestamp, getFailOnError());
        ConfirmResponse resp = execute(confirmRequest);
        setLastResponse(resp);
        setLastModification(resp.getTimestamp());
    }

    public List<Appointment> updates(final int folderId, final Date timestamp, final boolean recurrenceMaster) {
        return updates(folderId, Appointment.ALL_COLUMNS, timestamp, recurrenceMaster);
    }

    public List<Appointment> updates(final int folderId, final int[] columns, final Date timestamp, final boolean recurrenceMaster) {
        UpdatesRequest req = new UpdatesRequest(folderId, columns, timestamp, recurrenceMaster);
        AppointmentUpdatesResponse resp = execute(req);
        extractInfo(resp);
        try {
            return resp.getAppointments(timezone);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Appointment> getChangeExceptions(int folderId, int objectId, int[] columns) {
        GetChangeExceptionsRequest request = new GetChangeExceptionsRequest(folderId, objectId, columns);
        GetChangeExceptionsResponse response = execute(request);
        extractInfo(response);
        try {
            return response.getAppointments(timezone);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void update(int inFolder, Appointment updatedAppointment) {
        UpdateRequest updateRequest = new UpdateRequest(inFolder, updatedAppointment, timezone, getFailOnError());
        UpdateResponse updateResponse = execute(updateRequest);
        extractInfo(updateResponse);
        updatedAppointment.setLastModified(updateResponse.getTimestamp());
        if (updateResponse.getId() != 0) {
            updatedAppointment.setObjectID(updateResponse.getId());
        }
        for (Appointment createdAppoinment : createdEntities) {
            if (createdAppoinment.getObjectID() == updatedAppointment.getObjectID()) {
                createdAppoinment.setLastModified(updatedAppointment.getLastModified());
                continue;
            }
        }

    }

    public void update(Appointment updatedAppointment) {
        update(updatedAppointment.getParentFolderID(), updatedAppointment);
    }

    public List<Appointment> list(ListIDs foldersAndIds, int[] columns) {
        ListRequest req = new ListRequest(foldersAndIds, addNecessaryColumns(columns), getFailOnError());
        CommonListResponse resp = execute(req);
        extractInfo(resp);
        return extractAppointments(resp);
    }

	public List<Appointment> extractAppointments(CommonListResponse resp) {
		List<Appointment> list = new LinkedList<Appointment>();
        int[] cols = resp.getColumns();
        Object[][] arr = resp.getArray();
        for (Object[] values : arr) {
            Appointment temp = new Appointment();
            list.add(temp);
            for (int i = 0; i < cols.length; i++) {
                if (values[i] != null) {
                    temp.set(cols[i], conv(cols[i], values[i]));
                } else {
                    temp.remove(cols[i]);
                }
            }
            fixDates(temp);
        }
        return list;
	}

    public List<Appointment> newappointments(Date start, Date end, int limit, int[] columns) {
        NewAppointmentSearchRequest req = new NewAppointmentSearchRequest(start, end, limit, timezone, columns);
        NewAppointmentSearchResponse resp = execute(req);
        extractInfo(resp);
        try {
			return Arrays.asList(resp.getAppointments());
		} catch (Exception e) {
			lastException = e;
			return null;
		}
    }

    private void fixDates(Appointment temp) {
        if (temp.getFullTime()) {
            return;
        }
        if (temp.containsStartDate()) {
            temp.setStartDate(moveOffset(temp.getStartDate()));
        }
        if (temp.containsEndDate()) {
            temp.setEndDate(moveOffset(temp.getEndDate()));
        }
    }

    private Date moveOffset(Date value) {
        int offset = timezone.getOffset(value.getTime());
        return new Date(value.getTime() - offset);
    }

    private Object conv(int i, Object object) {
        Object value = object;
        switch (i) {
        case Appointment.START_DATE:
        case Appointment.END_DATE:
        case Appointment.UNTIL:
            if (!(object instanceof Date)) {
                value = new Date((Long) object);
            }
        }
        return value;
    }

    private int[] addNecessaryColumns(int[] columns) {
        List<Integer> cols = new LinkedList<Integer>(Arrays.asList(i2I(columns)));
        if (!cols.contains(I(CommonObject.FOLDER_ID))) {
            cols.add(I(CommonObject.FOLDER_ID));
        }
        if (!cols.contains(I(CommonObject.OBJECT_ID))) {
            cols.add(I(CommonObject.OBJECT_ID));
        }
        return I2i(cols);
    }

    public Appointment[] all(int parentFolderID, Date start, Date end, int[] columns, boolean recurrenceMaster) {
        AllRequest request = new AllRequest(parentFolderID, columns, start, end, timezone, recurrenceMaster);
        CommonAllResponse response = execute(request);
        extractInfo(response);
        List<Appointment> appointments = new ArrayList<Appointment>();

        int[] actualColumns = response.getColumns();
        for (Object[] row : response.getArray()) {
            Appointment app = new Appointment();
            appointments.add(app);
            for (int i = 0; i < row.length; i++) {
                if (row[i] == null) {
                    continue;
                }
                if (actualColumns[i] == Appointment.LAST_MODIFIED_UTC) {
                    continue;
                } else if (actualColumns[i] == Appointment.CONFIRMATIONS) {
                    parseConfirmations((JSONArray) row[i], app);
                    continue;
                } else if (actualColumns[i] == Appointment.USERS) {
                    parseUsers((JSONArray) row[i], app);
                } else {
                    try {
                        app.set(actualColumns[i], row[i]);
                    } catch (ClassCastException x) {
                        if (x.getMessage().equals("java.lang.Long")) {
                            if (!tryDate(app, actualColumns[i], (Long) row[i])) {
                                tryInteger(app, actualColumns[i], (Long) row[i]);
                            }
                        } else if (x.getMessage().equals("java.lang.Long cannot be cast to java.util.Date")) {
                            app.set(actualColumns[i], new Date((Long)row[i]));
                        } else if (x.getMessage().equals("java.lang.String cannot be cast to java.lang.Long")) {
                            app.set(actualColumns[i], Long.parseLong((String) row[i]));
                        } else if (x.getMessage().equals("org.json.JSONArray cannot be cast to [Ljava.util.Date;")) {
                            //
                        }
                    }
                }

            }
        }

        return appointments.toArray(new Appointment[appointments.size()]);

    }

    private void parseUsers(JSONArray jUsers, Appointment app) {
        List<UserParticipant> users = new ArrayList<UserParticipant>();
        try {
            for (int i = 0; i < jUsers.length(); i++) {
                final JSONObject jUser = jUsers.getJSONObject(i);
                final UserParticipant user = new UserParticipant(jUser.getInt(ParticipantsFields.ID));
                if (jUser.has(ParticipantsFields.CONFIRMATION)) {
                    user.setConfirm(jUser.getInt(ParticipantsFields.CONFIRMATION));
                }
                if (jUser.has(ParticipantsFields.CONFIRM_MESSAGE)) {
                    user.setConfirmMessage(jUser.getString(ParticipantsFields.CONFIRM_MESSAGE));
                }

                if (jUser.has(CalendarFields.ALARM)) {
                    user.setAlarmDate(new Date(jUser.getLong(CalendarFields.ALARM)));
                }
                users.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        app.setUsers(users);
    }

    private void parseConfirmations(JSONArray confirmations, Appointment app) {
        ParticipantParser parser = new ParticipantParser();
        List<ConfirmableParticipant> confirmableParticipants = new ArrayList<ConfirmableParticipant>();
        for (int j = 0; j < confirmations.length(); j++) {
            JSONObject confirmation;
            try {
                confirmation = confirmations.getJSONObject(j);
                confirmableParticipants.add(parser.parseConfirmation(true, confirmation));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        app.setConfirmations(confirmableParticipants);
    }

    public Appointment[] all(int parentFolderID, Date start, Date end, int[] columns) {
        return all(parentFolderID, start, end, columns, true);
    }


    public Appointment[] all(int parentFolderID, Date start, Date end) {
        AllRequest request = new AllRequest(parentFolderID, Appointment.ALL_COLUMNS, start, end, timezone);
        CommonAllResponse response = execute(request);
        extractInfo(response);
        List<Appointment> appointments = new ArrayList<Appointment>();

        for (Object[] row : response.getArray()) {
            Appointment app = new Appointment();
            appointments.add(app);
            for (int i = 0; i < row.length; i++) {
                if (row[i] == null) {
                    continue;
                }
                if (Appointment.ALL_COLUMNS[i] == Appointment.LAST_MODIFIED_UTC) {
                    continue;
                }
                try {
                    app.set(Appointment.ALL_COLUMNS[i], row[i]);
                } catch (ClassCastException x) {
                    if (x.getMessage() != null && x.getMessage().equals("java.lang.Long")) {
                        if (!tryDate(app, Appointment.ALL_COLUMNS[i], (Long) row[i])) {
                            tryInteger(app, Appointment.ALL_COLUMNS[i], (Long) row[i]);
                        }
                    }
                }
            }
        }

        return appointments.toArray(new Appointment[appointments.size()]);
    }

    public void delete(Appointment appointment, boolean failOnErrorOverride, boolean deleteFromCreatedEntities) {
        if (deleteFromCreatedEntities) {
            createdEntities.remove(appointment); // TODO: Does this remove the right object or does equals() suck?
        }
        DeleteRequest deleteRequest;
        if(appointment.containsRecurrencePosition()){
            deleteRequest = new DeleteRequest(
                appointment.getObjectID(),
                appointment.getParentFolderID(),
                appointment.getRecurrencePosition(),
                new Date(Long.MAX_VALUE),
                failOnErrorOverride);
        } else {
            deleteRequest = new DeleteRequest(
                appointment.getObjectID(),
                appointment.getParentFolderID(),
                new Date(Long.MAX_VALUE),
                failOnErrorOverride);
        }
        CommonDeleteResponse response = execute(deleteRequest);
        if(response != null) {
            extractInfo(response);
        }
    }

    public void delete(Appointment appointment) {
        delete(appointment, getFailOnError(), false);
    }

    public void delete(Appointment appointment, boolean deleteFromCreatedEntities) {
        delete(appointment, getFailOnError(), deleteFromCreatedEntities);
    }

    public void createDeleteException(int folder, int seriesId, int recurrencePos) {
        DeleteRequest deleteRequest = new DeleteRequest(seriesId, folder, recurrencePos, new Date(Long.MAX_VALUE), getFailOnError());
        extractInfo(execute(deleteRequest));
    }

    public void createDeleteException(Appointment master, int recurrencePos) {
        createDeleteException(master.getParentFolderID(), master.getObjectID(), recurrencePos);
        master.setLastModified(getLastModification());
    }

    public boolean[] has(Date startInclusive, Date endExclusive){
        HasResponse response = execute( new HasRequest(startInclusive, endExclusive, getTimezone()));
        lastResponse = response;
        try {
            return response.getValues();
        } catch (JSONException e) {
            lastException = e;
            return null;
        }
    }

    public List<Appointment> getCreatedEntities() {
    	return this.createdEntities;
    }

    /*
     * Helper methods
     */
    public Appointment createIdentifyingCopy(Appointment appointment) {
        Appointment copy = new Appointment();
        copy.setObjectID(appointment.getObjectID());
        copy.setParentFolderID(appointment.getParentFolderID());
        copy.setLastModified(appointment.getLastModified());
        return copy;
    }

    private boolean tryInteger(Appointment app, int field, Long value) {
        try {
            app.set(field, new Integer(value.intValue()));
            return true;
        } catch (ClassCastException x) {
            return false;
        }
    }

    private boolean tryDate(Appointment app, int field, Long value) {
        try {
            app.set(field, new Date(value.longValue()));
            return true;
        } catch (ClassCastException x) {
            return false;
        }
    }

    public void clearFolder(int folderId, Date start, Date end) {
        for (Appointment app : all(folderId, start, end)) {
            delete(app, true);
        }
    }

    protected void extractInfo(AbstractAJAXResponse response) {
        setLastResponse(response);
        setLastModification(response.getTimestamp());
        if (response.hasError()) {
            setLastException(response.getException());
        }
    }

}
