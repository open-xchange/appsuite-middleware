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

package com.openexchange.ajax.task;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;

/**
 * This test ensures that bug 23444 does not occur again. The bug describes the problem that the recurrence information can not be removed
 * from a task. Settings recurrence_type to 0 is not working to remove the recurrence 
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug23444Test extends AbstractTaskTest {

    private AJAXClient client;
    private Task task;
    private TimeZone tz;

    public Bug23444Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = getTimeZone();
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test for bug 23444");
        Calendar cal = TimeTools.createCalendar(TimeZones.UTC);
        cal.set(Calendar.HOUR, 0);
        task.setStartDate(cal.getTime());
        task.setEndDate(cal.getTime());
        task.setRecurrenceType(Task.DAILY);
        task.setInterval(1);
        InsertRequest request = new InsertRequest(task, tz);
        InsertResponse response = client.execute(request);
        response.fillTask(task);
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(task));
        super.tearDown();
    }

    public void testForBug() throws OXException, IOException, JSONException {
        Task update = TaskTools.valuesForUpdate(task);
        update.setRecurrenceType(Task.NO_RECURRENCE);
        UpdateRequest request = new UpdateRequest(update, tz);
        client.execute(request);
        GetResponse response = client.execute(new GetRequest(task, tz));
        Task test = response.getTask(tz);
        task.setLastModified(test.getLastModified());
        assertEquals("Series information can not be removed from a task.", Task.NO_RECURRENCE, test.getRecurrenceType());
    }
}
