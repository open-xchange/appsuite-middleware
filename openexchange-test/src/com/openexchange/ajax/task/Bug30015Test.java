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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;

/**
 * Verifies that the next created occurrence does not contain the task_completed attribute.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug30015Test extends AbstractAJAXSession {

    private AJAXClient client1;
    private TimeZone timeZone;
    private Calendar cal;
    private Task task;
    private Task first;
    private Task second;

    public Bug30015Test(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        timeZone = client1.getValues().getTimeZone();
        cal = TimeTools.createCalendar(TimeZones.UTC);
        task = new Task();
        task.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        task.setTitle("Test for bug 30015");
        cal.set(Calendar.HOUR_OF_DAY, 0);
        task.setStartDate(cal.getTime());
        task.setEndDate(cal.getTime());
        task.setRecurrenceType(Task.DAILY);
        task.setInterval(1);
        task.setOccurrence(2);
        client1.execute(new InsertRequest(task, timeZone)).fillTask(task);
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        client1.execute(new DeleteRequest(first));
        client1.execute(new DeleteRequest(second));
        super.tearDown();
    }

    @Test
    public void testForTaskCompletedAttributeInNextOccurrence() throws OXException, IOException, JSONException {
        first = new Task();
        first.setObjectID(task.getObjectID());
        first.setParentFolderID(task.getParentFolderID());
        first.setLastModified(task.getLastModified());
        first.setStatus(Task.DONE);
        first.setPercentComplete(100);
        first.setDateCompleted(cal.getTime());
        first.setLastModified(client1.execute(new UpdateRequest(first, timeZone)).getTimestamp());
        second = Bug21026Test.findNextOccurrence(client1, client1.execute(new GetRequest(first)).getTask(timeZone));
        second = client.execute(new GetRequest(second)).getTask(timeZone);
        Assert.assertFalse("Next occurrence of task must not contain the attribute 'date completed'.", second.containsDateCompleted());
        assertNull("Next occurrence of task must not contain the attribute 'date completed'.", second.getDateCompleted());
    }
}
