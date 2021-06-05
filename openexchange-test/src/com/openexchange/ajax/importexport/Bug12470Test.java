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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12470Test extends AbstractAJAXSession {

    private AJAXClient client;

    private int folderId;

    private TimeZone tz;

    private TimeZone utc;

    private int objectId = -1;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateTaskFolder();
        tz = client.getValues().getTimeZone();
        utc = TimeZone.getTimeZone("UTC");
        importvTodo();
    }

    @Test
    public void testDueDate() throws OXException, IOException, JSONException {
        final GetRequest request = new GetRequest(folderId, objectId);
        final GetResponse response = client.execute(request);
        final Task task = response.getTask(tz);
        final Date due = task.getEndDate();
        final Calendar calendar = TimeTools.createCalendar(utc);
        calendar.set(Calendar.YEAR, 2007);
        calendar.set(Calendar.MONTH, Calendar.JULY);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        final Date expected = calendar.getTime();
        assertEquals("Task due dates are not correctly imported.", expected, due);
    }

    private void importvTodo() throws OXException, IOException, JSONException {
        final ICalImportRequest request = new ICalImportRequest(folderId, vTodo);
        final ICalImportResponse response = client.execute(request);
        if (response.hasError()) {
            fail(response.getException().toString());
        }
        final ImportResult result = response.getImports()[0];
        objectId = Integer.parseInt(result.getObjectId());
    }

    private static final String vTodo = "BEGIN:VCALENDAR\n" + "PRODID:-//K Desktop Environment//NONSGML libkcal 3.2//EN\n" + "VERSION:2.0\n" + "BEGIN:VTODO\n" + "DTSTAMP:20070531T093649Z\n" + "ORGANIZER;CN=Horst Schmidt:MAILTO:horst.schmidt@example.invalid\n" + "CREATED:20070531T093612Z\n" + "UID:libkcal-1172232934.1028\n" + "SEQUENCE:0\n" + "LAST-MODIFIED:20070531T093612Z\n" + "DESCRIPTION:das ist ein ical test\n" + "SUMMARY:test ical\n" + "LOCATION:daheim\n" + "CLASS:PUBLIC\n" + "PRIORITY:5\n" + "DUE;VALUE=DATE:20070731\n" + "PERCENT-COMPLETE:30\n" + "END:VTODO\n" + "END:VCALENDAR\n";
}
