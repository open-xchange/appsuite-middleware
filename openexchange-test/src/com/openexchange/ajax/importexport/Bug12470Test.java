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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.tasks.Task;

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

    private Date lastModified = null;

    /**
     * Default constructor.
     * @param name test name.
     */
    public Bug12470Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateTaskFolder();
        tz = client.getValues().getTimeZone();
        utc = TimeZone.getTimeZone("UTC");
        importvTodo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        deleteTask();
        super.tearDown();
    }

    public void testDueDate() throws OXException, IOException, SAXException,
        JSONException, OXException {
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

    private void importvTodo() throws OXException, IOException, SAXException,
        JSONException {
        final ICalImportRequest request = new ICalImportRequest(folderId, vTodo);
        final ICalImportResponse response = client.execute(request);
        if (response.hasError()) {
            fail(response.getException().toString());
        }
        final ImportResult result = response.getImports()[0];
        objectId = Integer.parseInt(result.getObjectId());
    }

    private void deleteTask() throws OXException, IOException, SAXException,
        JSONException {
        if (null == lastModified) {
            lastModified = new Date(Long.MAX_VALUE);
        }
        final DeleteRequest request = new DeleteRequest(folderId, objectId, lastModified);
        client.execute(request);
    }

    private static final String vTodo =
        "BEGIN:VCALENDAR\n" +
        "PRODID:-//K Desktop Environment//NONSGML libkcal 3.2//EN\n" +
        "VERSION:2.0\n" +
        "BEGIN:VTODO\n" +
        "DTSTAMP:20070531T093649Z\n" +
        "ORGANIZER;CN=Horst Schmidt:MAILTO:horst.schmidt@example.invalid\n" +
        "CREATED:20070531T093612Z\n" +
        "UID:libkcal-1172232934.1028\n" +
        "SEQUENCE:0\n" +
        "LAST-MODIFIED:20070531T093612Z\n" +
        "DESCRIPTION:das ist ein ical test\n" +
        "SUMMARY:test ical\n" +
        "LOCATION:daheim\n" +
        "CLASS:PUBLIC\n" +
        "PRIORITY:5\n" +
        "DUE;VALUE=DATE:20070731\n" +
        "PERCENT-COMPLETE:30\n" +
        "END:VTODO\n" +
        "END:VCALENDAR\n";
}
