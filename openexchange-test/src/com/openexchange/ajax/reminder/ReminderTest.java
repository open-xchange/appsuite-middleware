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

package com.openexchange.ajax.reminder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
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
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.ReminderFields;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.OXTestToolkit;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;

public abstract class ReminderTest extends AbstractAJAXTest {

    private static final String REMINDER_URL = "/ajax/reminder";

    public ReminderTest(final String name) {
        super(name);
    }

    /**
     * @deprecated use {@link RangeRequest}.
     */
    @Deprecated
    public static ReminderObject[] listReminder(final WebConversation webConversation, final Date end, final TimeZone timeZone, final String host, final String sessionId) throws IOException, SAXException, JSONException, OXException, OXException {
        final AJAXSession session = new AJAXSession(webConversation, host, sessionId);
        final RangeRequest request = new RangeRequest(end);
        final RangeResponse response = Executor.execute(session, request, host);
        return response.getReminder(timeZone);
    }

    public static ReminderObject[] listUpdates(final WebConversation webConversation, final Date lastModified, String host, final String sessionId, final TimeZone timeZone) throws Exception, OXException {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified);

        final WebRequest webRequest = new GetMethodWebRequest(host + REMINDER_URL + parameter.getURLParameters());
        final WebResponse webResponse = webConversation.getResponse(webRequest);

        assertEquals(200, webResponse.getResponseCode());

        final JSONObject jsonObj = new JSONObject(webResponse.getText());

        if (jsonObj.has(jsonTagError)) {
            throw new TestException("server error: " + jsonObj.get(jsonTagError));
        }

        if (!jsonObj.has(jsonTagData)) {
            throw new TestException("no data in JSON object!");
        }

        final JSONArray jsonArray = jsonObj.getJSONArray(jsonTagData);
        final ReminderObject[] reminderObj = new ReminderObject[jsonArray.length()];
        for (int a = 0; a < jsonArray.length(); a++) {
            final JSONObject jsonReminder = jsonArray.getJSONObject(a);
            reminderObj[a] = new ReminderObject();

            reminderObj[a].setObjectId(DataParser.parseInt(jsonReminder, ReminderFields.ID));
            reminderObj[a].setTargetId(DataParser.parseInt(jsonReminder, ReminderFields.TARGET_ID));
            reminderObj[a].setFolder(DataParser.parseInt(jsonReminder, ReminderFields.FOLDER));
            reminderObj[a].setDate(DataParser.parseTime(jsonReminder, ReminderFields.ALARM, timeZone));
            reminderObj[a].setLastModified(DataParser.parseDate(jsonReminder, ReminderFields.LAST_MODIFIED));
            reminderObj[a].setUser(DataParser.parseInt(jsonReminder, ReminderFields.USER_ID));
            reminderObj[a].setRecurrenceAppointment(DataParser.parseBoolean(jsonReminder, ReminderFields.RECURRENCE_APPOINTMENT));
        }

        return reminderObj;
    }

    public static int[] deleteReminder(final WebConversation webConversation, final int objectId, String host, final String sessionId) throws Exception, OXException {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);

        final JSONObject jsonObj = new JSONObject();
        jsonObj.put(DataFields.ID, objectId);

        final ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
        final WebRequest webRequest = new PutMethodWebRequest(host + REMINDER_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse webResponse = webConversation.getResponse(webRequest);
        new JSONObject(webResponse.getText());

        assertEquals(200, webResponse.getResponseCode());

        final JSONObject jsonResponse = new JSONObject(webResponse.getText());

        if (jsonResponse.has(jsonTagError)) {
            throw new TestException("server error: " + jsonResponse.get(jsonTagError));
        }

        if (jsonResponse.has("data")) {
            final JSONArray jsonArray = jsonResponse.getJSONArray("data");
            final int[] failedObjects = new int[jsonArray.length()];
            for (int a = 0; a < failedObjects.length; a++) {
                failedObjects[a] = jsonArray.getInt(a);
            }
            return failedObjects;
        }

        return new int[] { };
    }

    public static void compareReminder(final ReminderObject reminderObj1, final ReminderObject reminderObj2) throws Exception {
        assertEquals("id", reminderObj1.getObjectId(), reminderObj2.getObjectId());
        OXTestToolkit.assertEqualsAndNotNull("folder", reminderObj1.getFolder(), reminderObj2.getFolder());
        OXTestToolkit.assertEqualsAndNotNull("alarm", reminderObj1.getDate(), reminderObj2.getDate());
    }
}
