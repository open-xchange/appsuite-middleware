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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AttachmentTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;

/**
 * {@link Bug16249Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug16249Test extends AttachmentTest {

    private int folderId;

    private TimeZone timeZone;

    private int appointmentId;

    private static final String APPOINTMENT_URL = "/ajax/calendar";

    public Bug16249Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        folderId = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId()).getObjectID();
        timeZone = ConfigTools.getTimeZone(getWebConversation(), getHostName(), getSessionId());
    }

    public void testBug16249() throws Exception {
        Appointment a = new Appointment();
        a.setTitle("Bug 16249 Test");
        a.setStartDate(D("01.07.2010 08:00"));
        a.setEndDate(D("01.07.2010 09:00"));
        a.setParentFolderID(folderId);
        a.setIgnoreConflicts(true);

        appointmentId = insertAppointment(getWebConversation(), a, timeZone, getHostName(), getSessionId());
        Date beforeAttach = loadAppointment(getWebConversation(), appointmentId, folderId, timeZone, getHostName(), getSessionId()).getLastModified();

        Response res = attach(getWebConversation(), getSessionId(), folderId, appointmentId, Types.APPOINTMENT, testFile);
        int attachmentId = ((JSONArray)res.getData()).getInt(0);

        Date afterAttach = loadAppointment(getWebConversation(), appointmentId, folderId, timeZone, getHostName(), getSessionId()).getLastModified();

        detach(getWebConversation(), getSessionId(), folderId, appointmentId, Types.APPOINTMENT, new int[]{attachmentId});

        Date afterDetach = loadAppointment(getWebConversation(), appointmentId, folderId, timeZone, getHostName(), getSessionId()).getLastModified();

        assertTrue("Wrong last modified after attach", beforeAttach.compareTo(afterAttach) < 0);
        assertTrue("Wrong last modified after detach", beforeAttach.compareTo(afterDetach) < 0);
        assertTrue("Wrong last modified after detach", afterAttach.compareTo(afterDetach) < 0);
    }

    @Override
    public void tearDown() throws Exception {
        deleteAppointment(getWebConversation(), appointmentId, folderId, getHostName(), getSessionId());
        super.tearDown();
    }

    public int insertAppointment(final WebConversation webCon, final Appointment appointmentObj, final TimeZone userTimeZone, String host, final String session) throws OXException, Exception, OXException {
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
            throw OXException.general("conflicts found!");
        }

        return objectId;
    }

    public Appointment loadAppointment(final WebConversation webCon, final int objectId, final int inFolder, final TimeZone userTimeZone, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
        parameter.setParameter(DataFields.ID, objectId);
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);

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

    public void deleteAppointment(final WebConversation webCon, final int id, final int inFolder, String host, final String session) throws Exception, OXException, IOException, SAXException {
        host = appendPrefix(host);

        final AJAXSession ajaxSession = new AJAXSession(webCon, host, session);
        final DeleteRequest deleteRequest = new DeleteRequest(id, inFolder, 0, new Date(Long.MAX_VALUE));
        deleteRequest.setFailOnError(false);
        final AbstractAJAXResponse response = Executor.execute(ajaxSession, deleteRequest);

        if (response.hasError()) {
            throw new Exception("json error: " + response.getResponse().getErrorMessage());
        }
    }

}
