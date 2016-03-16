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

package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.data.conversion.ical.ical4j.ICal4JEmitter;
import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.data.conversion.ical.ical4j.internal.UserResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.importexport.formats.Format;
import com.openexchange.tools.URLParameter;

/**
 * @deprecated Use IcalImportRequest/Response or IcalExportRequest/Response
 * and a normal AbstractAjaxTest or a managed one.
 *
 */
@Deprecated
public class AbstractICalTest extends AbstractAJAXTest {

    protected static final String IMPORT_URL = "/ajax/import";

    protected static final String EXPORT_URL = "/ajax/export";

    protected Date startTime = null;

    protected Date endTime = null;

    protected int appointmentFolderId = -1;

    protected int taskFolderId = -1;

    protected int userId = -1;

    protected String emailaddress = null;

    protected TimeZone timeZone = null;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractICalTest.class);

    public AbstractICalTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final FolderObject appointmentFolderObj = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
        appointmentFolderId = appointmentFolderObj.getObjectID();

        final FolderObject taskFolderObj = FolderTest.getStandardTaskFolder(getWebConversation(), getHostName(), getSessionId());
        taskFolderId = taskFolderObj.getObjectID();

        userId = appointmentFolderObj.getCreatedBy();

        timeZone = ConfigTools.getTimeZone(getWebConversation(), getHostName(), getSessionId());

        LOG.debug(new StringBuilder().append("use timezone: ").append(timeZone).toString());

        final Contact contactObj = ContactTest.loadUser(
            getWebConversation(),
            userId,
            FolderObject.SYSTEM_LDAP_FOLDER_ID,
            getHostName(),
            getSessionId());
        emailaddress = contactObj.getEmail1();

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        c.set(Calendar.HOUR_OF_DAY, 8);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        startTime = c.getTime();
        // startTime.setTime(sta + timeZone.getOffset(startTime.getTime()));
        endTime = new Date(startTime.getTime() + 3600000);

        // Remove somewhere ugly injected instances.
        Participants.userResolver = new UserResolver() {

            @Override
            public List<User> findUsers(final List<String> mails, final Context ctx) {
                return new ArrayList<User>();
            }

            @Override
            public User loadUser(final int userId, final Context ctx) {
                return null;
            }
        };
    }

    public static ImportResult[] importICal(final WebConversation webCon, final Appointment[] appointments, final int folderId, final String host, final String session) throws ConversionError, OXException, IOException, SAXException, JSONException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ICalEmitter emitter = new ICal4JEmitter();
        final ICalSession icalSession = emitter.createSession();
        for (int a = 0; a < appointments.length; a++) {
            emitter.writeAppointment(
                icalSession,
                appointments[a],
                null,
                new LinkedList<ConversionError>(),
                new LinkedList<ConversionWarning>());
        }
        emitter.writeSession(icalSession, baos);
        final ByteArrayInputStream input = new ByteArrayInputStream(baos.toByteArray());
        return importICal(webCon, input, folderId, host, session);
    }

    public static ImportResult[] importICal(final WebConversation webCon, final Task[] tasks, final int folderId, final String host, final String session) throws OXException, IOException, SAXException, JSONException, ConversionError {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ICalEmitter emitter = new ICal4JEmitter();
        final ICalSession icalSession = emitter.createSession();
        for (int a = 0; a < tasks.length; a++) {
            emitter.writeTask(icalSession, tasks[a], null, new LinkedList<ConversionError>(), new LinkedList<ConversionWarning>());
        }
        emitter.writeSession(icalSession, baos);
        final ByteArrayInputStream input = new ByteArrayInputStream(baos.toByteArray());
        return importICal(webCon, input, folderId, host, session);
    }

    public static ImportResult[] importICal(final WebConversation webCon, final InputStream input, final int folderId, final String host, final String session) throws OXException, IOException, SAXException, JSONException {
        final AJAXSession aSession = new AJAXSession(webCon, host, session);
        final ICalImportRequest request = new ICalImportRequest(folderId, input);
        final ICalImportResponse iResponse = Executor.execute(aSession, request, host);
        return iResponse.getImports();
    }

    public Appointment[] exportAppointment(final WebConversation webCon, final int folderId, final TimeZone timeZone, final String host, final String session, final Context ctx) throws IOException, SAXException, ConversionWarning, OXException, JSONException {
        final AJAXSession aSession = new AJAXSession(webCon, host, session);
        final ICalExportRequest request = new ICalExportRequest(folderId);
        final ICalExportResponse response = Executor.execute(aSession, request);

        final ICalParser parser = new ICal4JParser();
        final List<ConversionError> errors = new LinkedList<ConversionError>();
        final List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
        final List<CalendarDataObject> exportData = parser.parseAppointments(response.getICal(), timeZone, ctx, errors, warnings);
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
        if (!warnings.isEmpty()) {
            throw warnings.get(0);
        }
        return exportData.toArray(new Appointment[exportData.size()]);
    }

    public Task[] exportTask(final WebConversation webCon, final int inFolder, final String mailaddress, final TimeZone timeZone, String host, final String session, final Context ctx) throws Exception, OXException {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter(true);
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter("action", Format.ICAL.getConstantName());
        parameter.setParameter("folder", taskFolderId);
        parameter.setParameter("type", Types.TASK);

        final WebRequest req = new GetMethodWebRequest(host + EXPORT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        List<Task> exportData = new ArrayList<Task>();

        final ICalParser parser = new ICal4JParser();
        final List<ConversionError> errors = new LinkedList<ConversionError>();
        final List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
        exportData = parser.parseTasks(resp.getInputStream(), timeZone, ctx, errors, warnings);

        return exportData.toArray(new Task[exportData.size()]);
    }
}
