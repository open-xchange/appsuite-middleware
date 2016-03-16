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

package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.ajax.request.AppointmentRequest;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceGroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.OXTestToolkit;
import com.openexchange.test.TestException;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.URLParameter;

/**
 * {@link AppointmentTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - added parseUserParticipants
 */
public class AppointmentTest extends AbstractAJAXTest {

    public AppointmentTest(final String name) {
        super(name);
    }

    public static final int[] APPOINTMENT_FIELDS = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, CalendarObject.START_DATE,
        CalendarObject.END_DATE, Appointment.LOCATION, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE, CalendarObject.PARTICIPANTS,
        CalendarObject.USERS, Appointment.SHOWN_AS, Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION, Appointment.FULL_TIME,
        Appointment.COLOR_LABEL, Appointment.DELETE_EXCEPTIONS, Appointment.CHANGE_EXCEPTIONS, Appointment.RECURRENCE_START,
        Appointment.ORGANIZER, Appointment.UID, Appointment.SEQUENCE };

    protected static final String APPOINTMENT_URL = "/ajax/calendar";

    protected static int appointmentFolderId = -1;

    protected static long startTime = 0;

    protected static long endTime = 0;

    protected static final long dayInMillis = 86400000;

    protected String userParticipant2 = null;

    protected String userParticipant3 = null;

    protected String groupParticipant = null;

    protected String resourceParticipant = null;

    protected int userId = 0;

    protected TimeZone timeZone = null;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentTest.class);

    private final List<Appointment> clean = new ArrayList<Appointment>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            final FolderObject folderObj = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
            appointmentFolderId = folderObj.getObjectID();
            userId = folderObj.getCreatedBy();

            timeZone = ConfigTools.getTimeZone(getWebConversation(), getHostName(), getSessionId());

            LOG.debug(new StringBuilder().append("use timezone: ").append(timeZone).toString());

            final Calendar c = Calendar.getInstance();
            c.setTimeZone(timeZone);
            c.set(Calendar.HOUR_OF_DAY, 8);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            startTime = c.getTimeInMillis();
            startTime += timeZone.getOffset(startTime);
            endTime = startTime + 3600000;

            userParticipant2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");
            userParticipant3 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");

            groupParticipant = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "group_participant", "");

            resourceParticipant = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "resource_participant", "");
        } catch (final Exception ex) {
            ex.printStackTrace();

            throw new Exception(ex);
        }
    }

    protected void compareObject(final Appointment appointmentObj1, final Appointment appointmentObj2) throws Exception {
        compareObject(appointmentObj1, appointmentObj2, appointmentObj1.getStartDate().getTime(), appointmentObj1.getEndDate().getTime());
    }

    protected void compareObject(final Appointment appointmentObj1, final Appointment appointmentObj2, final long newStartTime, final long newEndTime) throws Exception {
        assertEquals("id", appointmentObj1.getObjectID(), appointmentObj2.getObjectID());
        OXTestToolkit.assertEqualsAndNotNull("title", appointmentObj1.getTitle(), appointmentObj2.getTitle());
        assertEquals("start", newStartTime, appointmentObj2.getStartDate().getTime());
        assertEquals("end", newEndTime, appointmentObj2.getEndDate().getTime());
        OXTestToolkit.assertEqualsAndNotNull("location", appointmentObj1.getLocation(), appointmentObj2.getLocation());
        assertEquals("shown_as", appointmentObj1.getShownAs(), appointmentObj2.getShownAs());
        assertEquals("folder id", appointmentObj1.getParentFolderID(), appointmentObj2.getParentFolderID());
        assertEquals("private flag", appointmentObj1.getPrivateFlag(), appointmentObj2.getPrivateFlag());
        assertEquals("full time", appointmentObj1.getFullTime(), appointmentObj2.getFullTime());
        assertEquals("label", appointmentObj1.getLabel(), appointmentObj2.getLabel());
        assertEquals("recurrence_type", appointmentObj1.getRecurrenceType(), appointmentObj2.getRecurrenceType());
        assertEquals("interval", appointmentObj1.getInterval(), appointmentObj2.getInterval());
        assertEquals("days", appointmentObj1.getDays(), appointmentObj2.getDays());
        assertEquals("month", appointmentObj1.getMonth(), appointmentObj2.getMonth());
        assertEquals("day_in_month", appointmentObj1.getDayInMonth(), appointmentObj2.getDayInMonth());
        assertEquals("until", appointmentObj1.getUntil(), appointmentObj2.getUntil());
        if (appointmentObj1.getOrganizer() != null && appointmentObj2.getOrganizer() != null) {
            assertEquals("organizer", appointmentObj1.getOrganizer(), appointmentObj2.getOrganizer());
        }
        if (appointmentObj1.containsUid()) {
            assertEquals("uid", appointmentObj1.getUid(), appointmentObj2.getUid());
        }
        // assertEquals("sequence", appointmentObj1.getSequence(), appointmentObj2.getSequence());
        OXTestToolkit.assertEqualsAndNotNull("note", appointmentObj1.getNote(), appointmentObj2.getNote());
        OXTestToolkit.assertEqualsAndNotNull("categories", appointmentObj1.getCategories(), appointmentObj2.getCategories());
        OXTestToolkit.assertEqualsAndNotNull(
            "delete_exceptions",
            appointmentObj1.getDeleteException(),
            appointmentObj2.getDeleteException());

        OXTestToolkit.assertEqualsAndNotNull(
            "participants are not equals",
            participants2String(appointmentObj1.getParticipants()),
            participants2String(appointmentObj2.getParticipants()));
    }

    protected Appointment createAppointmentObject(final String title) {
        final Appointment appointmentobject = new Appointment();
        appointmentobject.setTitle(title);
        appointmentobject.setStartDate(new Date(startTime));
        appointmentobject.setEndDate(new Date(endTime));
        appointmentobject.setLocation("Location");
        appointmentobject.setShownAs(Appointment.ABSENT);
        appointmentobject.setParentFolderID(appointmentFolderId);
        appointmentobject.setIgnoreConflicts(true);
        return appointmentobject;
    }

    public static int insertAppointment(final WebConversation webCon, final Appointment appointmentObj, final TimeZone userTimeZone, String host, final String session) throws OXException, Exception {
        host = appendPrefix(host);

        int objectId = 0;

        final StringWriter stringWriter = new StringWriter();

        final JSONObject jsonObj = new JSONObject();
        final AppointmentWriter appointmentwriter = new AppointmentWriter(userTimeZone);
        appointmentwriter.writeAppointment(appointmentObj, jsonObj);

        stringWriter.write(jsonObj.toString());
        stringWriter.flush();

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);

        final ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes(com.openexchange.java.Charsets.UTF_8));
        final WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            throw new TestException("json error: " + response.getErrorMessage());
        }

        final JSONObject data = (JSONObject) response.getData();
        if (data.has(DataFields.ID)) {
            objectId = data.getInt(DataFields.ID);
        }

        if (data.has("conflicts")) {
            throw new OXException().setLogMessage("conflicts found!");
        }

        return objectId;
    }

    public static int updateAppointment(final WebConversation webCon, final Appointment appointmentObj, final int objectId, final int inFolder, final TimeZone userTimeZone, final String host, final String session) throws Exception {
        return updateAppointment(
            webCon,
            appointmentObj,
            objectId,
            inFolder,
            new Date(System.currentTimeMillis() + APPEND_MODIFIED),
            userTimeZone,
            host,
            session);
    }

    public static int updateAppointment(final WebConversation webCon, final Appointment appointmentObj, int objectId, final int inFolder, final Date modified, final TimeZone userTimeZone, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final StringWriter stringWriter = new StringWriter();
        final JSONObject jsonObj = new JSONObject();
        final AppointmentWriter appointmentwriter = new AppointmentWriter(userTimeZone);
        appointmentwriter.writeAppointment(appointmentObj, jsonObj);

        stringWriter.write(jsonObj.toString());
        stringWriter.flush();

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
        parameter.setParameter(DataFields.ID, Integer.toString(objectId));
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, Integer.toString(inFolder));
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);

        final ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes(com.openexchange.java.Charsets.UTF_8));
        final WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            throw new TestException("json error: " + response.getErrorMessage());
        }

        final JSONObject data = (JSONObject) response.getData();
        if (data.has(DataFields.ID)) {
            objectId = data.getInt(DataFields.ID);
        }

        if (data.has("conflicts")) {
            throw new OXException().setLogMessage("conflicts found!");
        }

        return objectId;
    }

    public static void deleteAppointment(final WebConversation webCon, final int id, final int inFolder, final String host, final String session, final boolean ignoreFailure) throws Exception {
        deleteAppointment(webCon, id, inFolder, new Date(System.currentTimeMillis() + APPEND_MODIFIED), host, session, ignoreFailure);
    }

    public static void deleteAppointment(final WebConversation webCon, final int id, final int inFolder, final Date modified, final String host, final String session, final boolean ignoreFailure) throws Exception {
        deleteAppointment(webCon, id, inFolder, 0, modified, host, session, ignoreFailure);
    }

    public static void deleteAppointment(final WebConversation webCon, final int id, final int inFolder, final int recurrencePosition, final String host, final String session, final boolean ignoreFailure) throws Exception {
        deleteAppointment(
            webCon,
            id,
            inFolder,
            recurrencePosition,
            new Date(System.currentTimeMillis() + APPEND_MODIFIED),
            host,
            session,
            ignoreFailure);
    }

    public static void deleteAppointment(final WebConversation webCon, final int id, final int inFolder, final int recurrencePosition, final Date modified, final String host, final String session, final boolean ignoreFailure) throws OXException, IOException, JSONException {
        final AJAXSession ajaxSession = new AJAXSession(webCon, host, session);
        final DeleteRequest deleteRequest = new DeleteRequest(id, inFolder, recurrencePosition, modified);
        deleteRequest.setFailOnError(false);
        final AbstractAJAXResponse response = Executor.execute(ajaxSession, deleteRequest);

        if (!ignoreFailure && response.hasError()) {
            throw new TestException("json error: " + response.getResponse().getErrorMessage());
        }
    }

    public static void deleteAppointment(final WebConversation webCon, final int id, final int inFolder, final Date recurrenceDatePosition, final Date modified, final String host, final String session, final boolean ignoreFailure) throws Exception, OXException, IOException, SAXException {
        final AJAXSession ajaxSession = new AJAXSession(webCon, host, session);
        final DeleteRequest deleteRequest = new DeleteRequest(id, inFolder, recurrenceDatePosition, modified);
        deleteRequest.setFailOnError(false);
        final AbstractAJAXResponse response = Executor.execute(ajaxSession, deleteRequest);

        if (!ignoreFailure && response.hasError()) {
            throw new Exception("json error: " + response.getResponse().getErrorMessage());
        }
    }

    public static void confirmAppointment(final WebConversation webCon, final int objectId, final int folderId, final int confirm, final String confirmMessage, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_CONFIRM);
        parameter.setParameter(DataFields.ID, objectId);
        parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID, folderId);

        final JSONObject jsonObj = new JSONObject();
        jsonObj.put(ParticipantsFields.CONFIRMATION, confirm);
        jsonObj.put(ParticipantsFields.CONFIRM_MESSAGE, confirmMessage);

        final ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
        final WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }
    }

    public static Appointment[] listAppointment(final WebConversation webCon, final int inFolder, final int[] cols, final Date start, final Date end, final TimeZone userTimeZone, final boolean showAll, final String host, final String session) throws JSONException, OXException, IOException, SAXException {
        return listAppointment(webCon, inFolder, cols, start, end, userTimeZone, showAll, false, host, session);
    }

    public static Appointment[] listAppointment(final WebConversation webCon, final int inFolder, final int[] cols, final Date start, final Date end, final TimeZone userTimeZone, final boolean showAll, final boolean recurrenceMaster, final String host, final String session) throws JSONException, OXException, IOException, SAXException {
        return listAppointment(webCon, inFolder, cols, start, end, userTimeZone, showAll, recurrenceMaster, -1, -1, host, session);
    }

    public static Appointment[] listAppointment(final WebConversation webCon, final int inFolder, final int[] cols, final Date start, final Date end, final TimeZone userTimeZone, final boolean showAll, final boolean recurrenceMaster, final int leftHandLimit, final int rightHandLimit, String host, final String session) throws JSONException, OXException, IOException, SAXException {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
        parameter.setParameter(AJAXServlet.PARAMETER_START, start);
        parameter.setParameter(AJAXServlet.PARAMETER_END, end);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        if (recurrenceMaster) {
            parameter.setParameter("recurrence_master", recurrenceMaster);
        }

        if (!showAll) {
            parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
        }

        if (leftHandLimit > -1) {
            parameter.setParameter(AJAXServlet.LEFT_HAND_LIMIT, leftHandLimit);
        }

        if (rightHandLimit > -1) {
            parameter.setParameter(AJAXServlet.RIGHT_HAND_LIMIT, rightHandLimit);
        }

        return listAppointment(webCon, cols, parameter, userTimeZone, host, session);
    }

    public static Appointment[] listAppointment(final WebConversation webCon, final int[] cols, final URLParameter parameter, final TimeZone userTimeZone, String host, final String session) throws JSONException, OXException, IOException, SAXException {
        host = appendPrefix(host);

        final WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        assertEquals(200, resp.getResponseCode());

        return jsonArray2AppointmentArray((JSONArray) response.getData(), cols, userTimeZone);
    }

    public static int resolveUid(final WebConversation webCon, final String host, final String session, final String uid) throws IOException, SAXException, JSONException {
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_RESOLVE_UID);
        parameter.setParameter(AJAXServlet.PARAMETER_UID, uid);

        final WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        final JSONObject jsonResponse = (JSONObject) response.getData();
        assertTrue(jsonResponse.has("id"));

        return jsonResponse.getInt("id");
    }

    public static Appointment[] listAppointment(final WebConversation webCon, final int[][] objectIdAndFolderId, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws Exception {
        final Appointment[] appointmentArray = new Appointment[objectIdAndFolderId.length];
        for (int a = 0; a < appointmentArray.length; a++) {
            appointmentArray[a] = new Appointment();
            appointmentArray[a].setObjectID(objectIdAndFolderId[a][0]);
            appointmentArray[a].setParentFolderID(objectIdAndFolderId[a][1]);
        }

        return listAppointment(webCon, appointmentArray, cols, userTimeZone, host, session);
    }

    public static Appointment[] listAppointment(final WebConversation webCon, final Appointment[] appointmentArray, final int[] cols, final TimeZone userTimeZone, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        final JSONArray jsonArray = new JSONArray();

        for (Appointment element : appointmentArray) {
            final JSONObject jsonObj = new JSONObject();
            jsonObj.put(DataFields.ID, element.getObjectID());
            jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, element.getParentFolderID());

            if (element.containsRecurrencePosition()) {
                jsonObj.put(CalendarFields.RECURRENCE_POSITION, element.getRecurrencePosition());
            }

            jsonArray.put(jsonObj);
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
        final WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse resp = webCon.getResponse(req);
        final Response response = ResponseParser.parse(resp.getText());

        if (response.hasError()) {
            throw new TestException("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        assertEquals(200, resp.getResponseCode());

        return jsonArray2AppointmentArray((JSONArray) response.getData(), cols, userTimeZone);
    }

    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final int inFolder, final TimeZone userTimeZone, final String host, final String session) throws Exception {
        return loadAppointment(webCon, objectId, 0, inFolder, userTimeZone, host, session);
    }

    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final int recurrencePosition, final int inFolder, final TimeZone userTimeZone, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
        parameter.setParameter(DataFields.ID, objectId);
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);

        if (recurrencePosition > 0) {
            parameter.setParameter(CalendarFields.RECURRENCE_POSITION, recurrencePosition);
        }

        final WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        final Appointment appointmentObj = new Appointment();

        final AppointmentParser appointmentParser = new AppointmentParser(true, userTimeZone);
        appointmentParser.parse(appointmentObj, (JSONObject) response.getData());

        return appointmentObj;
    }

    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final Date start, final Date end, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws Exception {

        return loadAppointment(webCon, objectId, 0, start, end, cols, userTimeZone, host, session);
    }

    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final Date start, final Date end, final Date modified, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws Exception {

        return loadAppointment(webCon, objectId, 0, start, end, modified, cols, userTimeZone, host, session);
    }

    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final Date start, final Date end, final Date modified, final int recurrencePosition, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws Exception {

        return loadAppointment(webCon, objectId, 0, start, end, modified, recurrencePosition, cols, userTimeZone, host, session);
    }

    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final int inFolder, final Date start, final Date end, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws JSONException, OXException, IOException, SAXException, OXException {

        final boolean showAll = (inFolder == 0);

        final Appointment[] appointmentArray = listAppointment(webCon, inFolder, cols, start, end, userTimeZone, showAll, host, session);

        for (Appointment element : appointmentArray) {
            if (element.getObjectID() == objectId) {
                return element;
            }
        }

        throw new TestException("object not found");
    }

    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final int inFolder, final Date start, final Date end, final Date modified, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws Exception {

        final Appointment[] appointmentArray = listModifiedAppointment(
            webCon,
            inFolder,
            start,
            end,
            modified,
            cols,
            userTimeZone,
            host,
            session);

        for (Appointment element : appointmentArray) {
            if (element.getObjectID() == objectId) {
                return element;
            }
        }

        // throw new TestException("object not found");
        return null;
    }

    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final int inFolder, final Date start, final Date end, final Date modified, final int recurrencePosition, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws Exception {

        final Appointment[] appointmentArray = listModifiedAppointment(
            webCon,
            inFolder,
            start,
            end,
            modified,
            cols,
            userTimeZone,
            host,
            session);

        for (Appointment element : appointmentArray) {
            if (element.getObjectID() == objectId && element.getRecurrencePosition() == recurrencePosition) {
                return element;
            }
        }

        throw new TestException("object not found");
    }

    public static Appointment[] listModifiedAppointment(final WebConversation webCon, final Date start, final Date end, final Date modified, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws Exception {
        return listModifiedAppointment(webCon, 0, start, end, modified, cols, userTimeZone, host, session);
    }

    public static Appointment[] listModifiedAppointment(final WebConversation webCon, final int inFolder, final Date start, final Date end, final Date modified, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws Exception {
        return listModifiedAppointment(webCon, inFolder, start, end, modified, cols, userTimeZone, false, host, session);
    }

    public static Appointment[] listModifiedAppointment(final WebConversation webCon, final int inFolder, final Date start, final Date end, final Date modified, final int[] cols, final TimeZone userTimeZone, final boolean bRecurrenceMaster, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);

        if (inFolder != 0) {
            parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
        }

        parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "deleted");
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);
        parameter.setParameter(AJAXServlet.PARAMETER_START, start);
        parameter.setParameter(AJAXServlet.PARAMETER_END, end);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        if (bRecurrenceMaster) {
            parameter.setParameter(AppointmentRequest.RECURRENCE_MASTER, true);
        }

        final WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());
        assertTrue("requested timestamp bigger then timestamp in response", response.getTimestamp().getTime() >= modified.getTime());

        assertEquals(200, resp.getResponseCode());

        return jsonArray2AppointmentArray((JSONArray) response.getData(), cols, userTimeZone);
    }

    public static Appointment[] listDeleteAppointment(final WebConversation webCon, final int inFolder, final Date start, final Date end, final Date modified, final TimeZone userTimeZone, String host, final String session) throws OXException, Exception {
        host = appendPrefix(host);

        final int[] cols = new int[] { Appointment.OBJECT_ID };

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);

        if (inFolder != 0) {
            parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
        }

        parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "changed");
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);
        parameter.setParameter(AJAXServlet.PARAMETER_START, start);
        parameter.setParameter(AJAXServlet.PARAMETER_END, end);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        final WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            throw new TestException(response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        assertEquals(200, resp.getResponseCode());

        final JSONArray jsonArray = (JSONArray) response.getData();
        final Appointment[] appointmentArray = new Appointment[jsonArray.length()];
        for (int a = 0; a < jsonArray.length(); a++) {
            appointmentArray[a] = new Appointment();
            appointmentArray[a].setObjectID(jsonArray.getInt(a));
        }

        return appointmentArray;
    }

    public static Appointment[] searchAppointment(final WebConversation webCon, final String searchpattern, final int inFolder, final int[] cols, final TimeZone userTimeZone, final String host, final String session) throws Exception {
        return searchAppointment(webCon, searchpattern, inFolder, null, null, cols, userTimeZone, host, session);
    }

    public static Appointment[] searchAppointment(final WebConversation webCon, final String searchpattern, final int inFolder, final Date start, final Date end, final int[] cols, final TimeZone userTimeZone, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        if (start != null) {
            parameter.setParameter(AJAXServlet.PARAMETER_START, start);
        }

        if (end != null) {
            parameter.setParameter(AJAXServlet.PARAMETER_END, end);
        }

        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("pattern", searchpattern);
        jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);

        final WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), new ByteArrayInputStream(
            jsonObj.toString().getBytes()), "text/javascript");
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        assertEquals(200, resp.getResponseCode());

        return jsonArray2AppointmentArray((JSONArray) response.getData(), cols, userTimeZone);
    }

    public static boolean[] hasAppointments(final WebConversation webCon, final Date start, final Date end, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_HAS);
        parameter.setParameter(AJAXServlet.PARAMETER_START, start);
        parameter.setParameter(AJAXServlet.PARAMETER_END, end);

        final WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        final boolean isArray = ((JSONArray) response.getData()) instanceof JSONArray;
        assertTrue("response object is not an array", isArray);

        final JSONArray jsonArray = ((JSONArray) response.getData());

        final boolean[] hasAppointments = new boolean[jsonArray.length()];
        for (int a = 0; a < hasAppointments.length; a++) {
            hasAppointments[a] = jsonArray.getBoolean(a);
        }

        return hasAppointments;
    }

    public static Appointment[] getFreeBusy(final WebConversation webCon, final int particiantId, final int type, final Date start, final Date end, final TimeZone userTimeZone, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ID, particiantId);
        parameter.setParameter("type", type);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_FREEBUSY);
        parameter.setParameter(AJAXServlet.PARAMETER_START, start);
        parameter.setParameter(AJAXServlet.PARAMETER_END, end);

        final WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = ResponseParser.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        final boolean isArray = response.getData() instanceof JSONArray;
        assertTrue("response object is not an array", isArray);

        final JSONArray jsonArray = (JSONArray) response.getData();
        return jsonArray2AppointmentArray(jsonArray, userTimeZone);
    }

    private static Appointment[] jsonArray2AppointmentArray(final JSONArray jsonArray, final TimeZone userTimeZone) throws Exception {
        final Appointment[] appointmentArray = new Appointment[jsonArray.length()];
        final AppointmentParser appointmentParser = new AppointmentParser(userTimeZone);

        for (int a = 0; a < appointmentArray.length; a++) {
            appointmentArray[a] = new Appointment();
            final JSONObject jObj = jsonArray.getJSONObject(a);

            appointmentParser.parse(appointmentArray[a], jObj);
        }

        return appointmentArray;
    }

    public static Appointment[] jsonArray2AppointmentArray(final JSONArray jsonArray, final int[] cols, final TimeZone userTimeZone) throws JSONException, OXException {
        final Appointment[] appointmentArray = new Appointment[jsonArray.length()];

        for (int a = 0; a < appointmentArray.length; a++) {
            appointmentArray[a] = new Appointment();
            parseCols(cols, jsonArray.getJSONArray(a), appointmentArray[a], userTimeZone);

            if (!appointmentArray[a].getFullTime()) {
                final Date startDate = appointmentArray[a].getStartDate();
                final Date endDate = appointmentArray[a].getEndDate();

                if (startDate != null && endDate != null) {
                    final int startOffset = userTimeZone.getOffset(startDate.getTime());
                    final int endOffset = userTimeZone.getOffset(endDate.getTime());
                    appointmentArray[a].setStartDate(new Date(startDate.getTime() - startOffset));
                    appointmentArray[a].setEndDate(new Date(endDate.getTime() - endOffset));
                }
            }
        }

        return appointmentArray;
    }

    private static void parseCols(final int[] cols, final JSONArray jsonArray, final Appointment appointmentObj, final TimeZone userTimeZone) throws JSONException, OXException {
        if (cols.length != jsonArray.length()) {
            LOG.debug("expected cols: {} recieved cols: {}", StringCollection.convertArray2String(cols), jsonArray.toString());
        }

        assertEquals("compare array size with cols size", cols.length, jsonArray.length());

        for (int a = 0; a < cols.length; a++) {
            parse(a, cols[a], jsonArray, appointmentObj, userTimeZone);
        }
    }

    private static void parse(final int pos, final int field, final JSONArray jsonArray, final Appointment appointmentObj, final TimeZone userTimeZone) throws JSONException, OXException {
        switch (field) {
        case Appointment.OBJECT_ID:
            appointmentObj.setObjectID(jsonArray.getInt(pos));
            break;
        case Appointment.FOLDER_ID:
            appointmentObj.setParentFolderID(jsonArray.getInt(pos));
            break;
        case Appointment.TITLE:
            appointmentObj.setTitle(jsonArray.getString(pos));
            break;
        case Appointment.CREATION_DATE:
            appointmentObj.setCreationDate(new Date(jsonArray.getLong(pos)));
            break;
        case Appointment.LAST_MODIFIED:
            appointmentObj.setLastModified(new Date(jsonArray.getLong(pos)));
            break;
        case Appointment.START_DATE:
            appointmentObj.setStartDate(new Date(jsonArray.getLong(pos)));
            break;
        case Appointment.END_DATE:
            appointmentObj.setEndDate(new Date(jsonArray.getLong(pos)));
            break;
        case Appointment.SHOWN_AS:
            appointmentObj.setShownAs(jsonArray.getInt(pos));
            break;
        case Appointment.LOCATION:
            appointmentObj.setLocation(jsonArray.getString(pos));
            break;
        case Appointment.FULL_TIME:
            appointmentObj.setFullTime(jsonArray.getBoolean(pos));
            break;
        case Appointment.PRIVATE_FLAG:
            appointmentObj.setPrivateFlag(jsonArray.getBoolean(pos));
            break;
        case Appointment.CATEGORIES:
            appointmentObj.setCategories(jsonArray.getString(pos));
            break;
        case Appointment.COLOR_LABEL:
            appointmentObj.setLabel(jsonArray.getInt(pos));
            break;
        case Appointment.NOTE:
            appointmentObj.setNote(jsonArray.getString(pos));
            break;
        case Appointment.RECURRENCE_POSITION:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setRecurrencePosition(jsonArray.getInt(pos));
            }
            break;
        case Appointment.RECURRENCE_TYPE:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setRecurrenceType(jsonArray.getInt(pos));
            }
            break;
        case Appointment.RECURRENCE_ID:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setRecurrenceID(jsonArray.getInt(pos));
            }
            break;
        case Appointment.INTERVAL:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setInterval(jsonArray.getInt(pos));
            }
            break;
        case Appointment.DAYS:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setDays(jsonArray.getInt(pos));
            }
            break;
        case Appointment.DAY_IN_MONTH:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setDayInMonth(jsonArray.getInt(pos));
            }
            break;
        case Appointment.MONTH:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setMonth(jsonArray.getInt(pos));
            }
            break;
        case Appointment.UNTIL:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setUntil(new Date(jsonArray.getLong(pos)));
            }
            break;
        case Appointment.RECURRENCE_COUNT:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setOccurrence(jsonArray.getInt(pos));
            }
            break;
        case Appointment.TIMEZONE:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setTimezone(jsonArray.getString(pos));
            }
            break;
        case Appointment.RECURRENCE_START:
            if (!jsonArray.isNull(pos)) {
                appointmentObj.setRecurringStart(jsonArray.getLong(pos));
            }
            break;
        case Appointment.PARTICIPANTS:
            appointmentObj.setParticipants(parseParticipants(jsonArray.getJSONArray(pos)));
            break;
        case Appointment.USERS:
            appointmentObj.setUsers(parseUserParticipants(jsonArray.getJSONArray(pos)));
            break;
        case Appointment.CHANGE_EXCEPTIONS:
            if (!jsonArray.isNull(pos)) {
                final JSONArray changeExceptions = jsonArray.getJSONArray(pos);
                appointmentObj.setChangeExceptions(parseExceptions(changeExceptions));
            }
            break;
        case Appointment.DELETE_EXCEPTIONS:
            if (!jsonArray.isNull(pos)) {
                final JSONArray deleteExceptions = jsonArray.getJSONArray(pos);
                appointmentObj.setDeleteExceptions(parseExceptions(deleteExceptions));
            }
            break;
        case Appointment.ORGANIZER:
            appointmentObj.setOrganizer(jsonArray.getString(pos));
            break;
        case Appointment.UID:
            appointmentObj.setUid(jsonArray.getString(pos));
            break;
        case Appointment.SEQUENCE:
            appointmentObj.setSequence(jsonArray.getInt(pos));
            break;
        }
    }

    private static Date[] parseExceptions(final JSONArray jsonArray) throws JSONException {
        final Date[] exceptions = new Date[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            exceptions[i] = new Date(jsonArray.getLong(i));
        }
        return exceptions;
    }

    private static UserParticipant[] parseUserParticipants(final JSONArray userParticipantArr) throws JSONException {
        final List<UserParticipant> userParticipants = new LinkedList<UserParticipant>();
        for (int i = 0, size = userParticipantArr.length(); i < size; i++) {
            final JSONObject participantObj = userParticipantArr.getJSONObject(i);
            UserParticipant userParticipant = new UserParticipant(participantObj.getInt("id"));
            if (participantObj.has("confirmation")) {
                userParticipant.setConfirm(participantObj.getInt("confirmation"));
            }
            if (participantObj.has("confirmmessage")) {
                userParticipant.setConfirmMessage(participantObj.getString("confirmmessage"));
            }

            userParticipants.add(userParticipant);
        }
        return userParticipants.toArray(new UserParticipant[userParticipants.size()]);
    }

    private static Participant[] parseParticipants(final JSONArray jsonArray) throws JSONException, OXException {
        final Participant[] participant = new Participant[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jparticipant = jsonArray.getJSONObject(i);
            final int type = jparticipant.getInt("type");
            final int id;
            if (jparticipant.has("id")) {
                id = jparticipant.getInt("id");
            } else {
                id = Participant.NO_ID;
            }
            final String mail = jparticipant.optString("mail");
            Participant p = null;
            switch (type) {
            case Participant.USER:
                if (Participant.NO_ID == id) {
                    throw new JSONException("JSONObject[id] not found.");
                }
                final UserParticipant user = new UserParticipant(id);
                if (jparticipant.has("confirmation")) {
                    user.setConfirm(jparticipant.getInt("confirmation"));
                }
                if (jparticipant.has("confirmmessage")) {
                    user.setConfirmMessage(jparticipant.getString("confirmmessage"));
                }
                p = user;
                break;
            case Participant.GROUP:
                if (Participant.NO_ID == id) {
                    throw new JSONException("JSONObject[id] not found.");
                }
                p = new GroupParticipant(id);
                break;
            case Participant.RESOURCE:
                if (Participant.NO_ID == id) {
                    throw new JSONException("JSONObject[id] not found.");
                }
                p = new ResourceParticipant(id);
                break;
            case Participant.RESOURCEGROUP:
                if (Participant.NO_ID == id) {
                    throw new JSONException("JSONObject[id] not found.");
                }
                p = new ResourceGroupParticipant(id);
                break;
            case Participant.EXTERNAL_USER:
                if (null == mail) {
                    throw new JSONException("JSONObject[mail] not found.");
                }
                p = new ExternalUserParticipant(mail);
                break;
            default:
                throw new OXException().setLogMessage("invalidType");
            }
            participant[i] = p;
        }

        return participant;
    }

    private HashSet participants2String(final Participant[] participant) throws Exception {
        if (participant == null) {
            return null;
        }

        final HashSet hs = new HashSet();

        for (Participant element : participant) {
            hs.add(participant2String(element));
        }

        return hs;
    }

    private String participant2String(final Participant p) throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("T" + p.getType());
        sb.append("ID" + p.getIdentifier());
        sb.append("E" + p.getEmailAddress());
        sb.append("D" + p.getDisplayName());

        return sb.toString();
    }

    protected void create(final Appointment appointment) throws JSONException, IOException, SAXException, OXException {
        final InsertRequest insert = new InsertRequest(appointment, TimeZones.UTC, true);
        getClient().execute(insert).fillAppointment(appointment);
        clean.add(appointment);
    }

    protected void clean() throws JSONException, IOException, SAXException, OXException {
        final AJAXClient client = getClient();
        for (final Appointment appointment : clean) {
            final DeleteRequest delete = new DeleteRequest(appointment);
            client.execute(delete);
        }
    }

    protected AJAXClient getClient() throws JSONException, IOException, OXException {
        return new AJAXClient(new AJAXSession(getWebConversation(), getHostName(), getSessionId()), false);
    }

    protected Appointment link(final Appointment base, final Appointment update) {
        update.setLastModified(base.getLastModified());
        update.setParentFolderID(base.getParentFolderID());
        update.setObjectID(base.getObjectID());
        return update;
    }
}
