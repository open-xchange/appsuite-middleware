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

import java.util.Calendar;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link Bug38782Test}
 *
 * Verify that recurrence calculation works even if start and end date are missing in the object.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug38782Test extends AbstractAJAXSession {

    private AJAXClient client1;
    private TimeZone timeZone;
    private Task task;

    public Bug38782Test(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        timeZone = client.getValues().getTimeZone();
        task = new Task();
        task.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        task.setTitle("Test for bug 38782");
        task.setRecurrenceType(Task.DAILY);
        task.setInterval(1);
        task.setOccurrence(2);
        Calendar start = TimeTools.createCalendar(timeZone);
        task.setStartDate(start.getTime());
        start.add(Calendar.HOUR_OF_DAY, 2);
        task.setEndDate(start.getTime());
        client1.execute(new InsertRequest(task, timeZone, true)).fillTask(task);
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        client1.execute(new DeleteRequest(task));
        super.tearDown();
    }

    @Test
    public void testGetAllTasks() throws Exception {
        Task test = Create.cloneForUpdate(task);
        test.setStartDate(null);
        test.setEndDate(null);
        UpdateResponse response = client1.execute(new UpdateRequest(test, timeZone, false));
        if (!response.hasError()) {
            task.setLastModified(response.getTimestamp());
            test = Create.cloneForUpdate(task);
            test.setStatus(Task.DONE);
            response = client1.execute(new UpdateRequest(test, timeZone, false));
            if (response.hasError()) {
                assertTrue("This is the expected NullPointerException from the bug report.", AjaxExceptionCodes.UNEXPECTED_ERROR.create().similarTo(response.getException()));
            }
            task.setLastModified(response.getTimestamp());
        } else {
            assertTrue("Expected exception for recurring tasks getting start and end date removed.", TaskExceptionCode.MISSING_RECURRENCE_VALUE.create().similarTo(response.getException()));
        }
    }
}
