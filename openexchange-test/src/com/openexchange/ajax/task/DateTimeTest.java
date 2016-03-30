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

import static com.openexchange.groupware.calendar.TimeTools.D;
import static com.openexchange.java.Autoboxing.I2i;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link DateTimeTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class DateTimeTest extends AbstractAJAXSession {

    private AJAXClient client;
    private TimeZone timeZone;
    private List<Task> tasksToDelete;

    /**
     * Initializes a new {@link DateTimeTest}.
     *
     * @param name The test name
     */
    public DateTimeTest(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        timeZone = client.getValues().getTimeZone();
        tasksToDelete = new ArrayList<Task>();
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        if (null != client && null != tasksToDelete) {
            int folderID = client.getValues().getPrivateTaskFolder();
            Set<Integer> ids = new HashSet<Integer>();
            for (Task task : tasksToDelete) {
                if (folderID != task.getParentFolderID()) {
                    client.execute(new DeleteRequest(task));
                } else {
                    ids.add(Integer.valueOf(task.getObjectID()));
                }
            }
            if (0 < ids.size()) {
                client.execute(new DeleteRequest(folderID, I2i(ids), new Date(Long.MAX_VALUE), false));
            }
        }
        super.tearDown();
    }

    @Test
    public void testCreateWithLegacyClient() throws Exception {
        /*
         * create task as a legacy client would do
         */
        Task task = new Task();
        task.setStartDate(D("09.10.2014 00:00", TimeZone.getTimeZone("UTC")));
        task.setEndDate(D("13.10.2014 00:00", TimeZone.getTimeZone("UTC")));
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("testCreateWithLegacyClient");
        client.execute(new InsertRequest(task, timeZone)).fillTask(task);
        tasksToDelete.add(task);
        /*
         * verify created task, interpreted as legacy client
         */
        GetResponse getResponse = client.execute(new GetRequest(task));
        Task legacyTask = getResponse.getTask(timeZone, true);
        assertNotNull("No task created", legacyTask);
        assertEquals("Start date wrong", task.getStartDate(), legacyTask.getStartDate());
        assertEquals("End date wrong", task.getEndDate(), legacyTask.getEndDate());
        /*
         * verify created task, interpreted as new client
         */
        Task newTask = getResponse.getTask(timeZone, false);
        assertNotNull("No task created", newTask);
        assertTrue("Fulltime wrong", newTask.getFullTime());
        assertEquals("Start date wrong", task.getStartDate(), newTask.getStartDate());
        assertEquals("End date wrong", task.getEndDate(), newTask.getEndDate());
        /*
         * verify raw data
         */
        JSONObject data = (JSONObject) getResponse.getData();
        assertTrue("No fulltime", data.hasAndNotNull(CalendarFields.FULL_TIME));
        assertTrue("Fulltime wrong", data.getBoolean(CalendarFields.FULL_TIME));
        assertTrue("No start time", data.hasAndNotNull(TaskFields.START_TIME));
        assertEquals("Start time wrong", task.getStartDate().getTime(), data.getLong(TaskFields.START_TIME));
        assertTrue("No end time", data.hasAndNotNull(TaskFields.END_TIME));
        assertEquals("End time wrong", task.getEndDate().getTime(), data.getLong(TaskFields.END_TIME));
        assertTrue("No start date", data.hasAndNotNull(CalendarFields.START_DATE));
        assertEquals("Start date wrong", task.getStartDate().getTime(), data.getLong(CalendarFields.START_DATE));
        assertTrue("No end date", data.hasAndNotNull(CalendarFields.END_DATE));
        assertEquals("End date wrong", task.getEndDate().getTime(), data.getLong(CalendarFields.END_DATE));
    }

    @Test
    public void testCreateWithNewClient() throws Exception {
        /*
         * create task as a new client would do
         */
        Task task = new Task();
        task.setStartDate(D("11.11.2014 11:11", timeZone));
        task.setEndDate(D("12.11.2014 04:00", timeZone));
        task.setFullTime(false);
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("testCreateWithNewClient");
        client.execute(new InsertRequest(task, timeZone, false, true, false)).fillTask(task);
        tasksToDelete.add(task);
        /*
         * verify created task, interpreted as legacy client
         */
        GetResponse getResponse = client.execute(new GetRequest(task));
        Task legacyTask = getResponse.getTask(timeZone, true);
        assertNotNull("No task created", legacyTask);
        assertEquals("Start date wrong", task.getStartDate(), legacyTask.getStartDate());
        assertEquals("End date wrong", task.getEndDate(), legacyTask.getEndDate());
        /*
         * verify created task, interpreted as new client
         */
        Task newTask = getResponse.getTask(timeZone, false);
        assertNotNull("No task created", newTask);
        assertFalse("Fulltime wrong", newTask.getFullTime());
        assertEquals("Start date wrong", task.getStartDate(), newTask.getStartDate());
        assertEquals("End date wrong", task.getEndDate(), newTask.getEndDate());
        /*
         * verify raw data
         */
        JSONObject data = (JSONObject) getResponse.getData();
        assertTrue("No fulltime", data.hasAndNotNull(CalendarFields.FULL_TIME));
        assertFalse("Fulltime wrong", data.getBoolean(CalendarFields.FULL_TIME));
        assertTrue("No start time", data.hasAndNotNull(TaskFields.START_TIME));
        assertEquals("Start time wrong",
            task.getStartDate().getTime() + timeZone.getOffset(task.getStartDate().getTime()), data.getLong(TaskFields.START_TIME));
        assertTrue("No end time", data.hasAndNotNull(TaskFields.END_TIME));
        assertEquals("End time wrong",
            task.getEndDate().getTime() + timeZone.getOffset(task.getEndDate().getTime()), data.getLong(TaskFields.END_TIME));
        assertTrue("No start date", data.hasAndNotNull(CalendarFields.START_DATE));
        assertEquals("Start date wrong", task.getStartDate().getTime(), data.getLong(CalendarFields.START_DATE));
        assertTrue("No end date", data.hasAndNotNull(CalendarFields.END_DATE));
        assertEquals("End date wrong", task.getEndDate().getTime(), data.getLong(CalendarFields.END_DATE));
    }

    @Test
    public void testCreateFulltimeWithNewClient() throws Exception {
        /*
         * create fulltime task as a new client would do
         */
        Task task = new Task();
        task.setStartDate(D("20.12.2014 00:00", TimeZone.getTimeZone("UTC")));
        task.setEndDate(D("23.12.2014 00:00", TimeZone.getTimeZone("UTC")));
        task.setFullTime(true);
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("testCreateFulltimeWithNewClient");
        client.execute(new InsertRequest(task, timeZone, false, true, false)).fillTask(task);
        tasksToDelete.add(task);
        /*
         * verify created task, interpreted as legacy client
         */
        GetResponse getResponse = client.execute(new GetRequest(task));
        Task legacyTask = getResponse.getTask(timeZone, true);
        assertNotNull("No task created", legacyTask);
        assertEquals("Start date wrong", task.getStartDate(), legacyTask.getStartDate());
        assertEquals("End date wrong", task.getEndDate(), legacyTask.getEndDate());
        /*
         * verify created task, interpreted as new client
         */
        Task newTask = getResponse.getTask(timeZone, false);
        assertNotNull("No task created", newTask);
        assertTrue("Fulltime wrong", newTask.getFullTime());
        assertEquals("Start date wrong", task.getStartDate(), newTask.getStartDate());
        assertEquals("End date wrong", task.getEndDate(), newTask.getEndDate());
        /*
         * verify raw data
         */
        JSONObject data = (JSONObject) getResponse.getData();
        assertTrue("No fulltime", data.hasAndNotNull(CalendarFields.FULL_TIME));
        assertTrue("Fulltime wrong", data.getBoolean(CalendarFields.FULL_TIME));
        assertTrue("No start time", data.hasAndNotNull(TaskFields.START_TIME));
        assertEquals("Start time wrong", task.getStartDate().getTime(), data.getLong(TaskFields.START_TIME));
        assertTrue("No end time", data.hasAndNotNull(TaskFields.END_TIME));
        assertEquals("End time wrong", task.getEndDate().getTime(), data.getLong(TaskFields.END_TIME));
        assertTrue("No start date", data.hasAndNotNull(CalendarFields.START_DATE));
        assertEquals("Start date wrong", task.getStartDate().getTime(), data.getLong(CalendarFields.START_DATE));
        assertTrue("No end date", data.hasAndNotNull(CalendarFields.END_DATE));
        assertEquals("End date wrong", task.getEndDate().getTime(), data.getLong(CalendarFields.END_DATE));
    }

    @Test
    public void testCreateWithBrokenLegacyClient() throws Exception {
        /*
         * create task as a "broken" legacy client would do, i.e. insert time values not being a multiple of 8.64e7
         */
        Task task = new Task();
        task.setStartDate(D("22.10.2014 07:00", TimeZone.getTimeZone("UTC")));
        task.setEndDate(D("25.10.2014 14:00", TimeZone.getTimeZone("UTC")));
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("testCreateWithBrokenLegacyClient");
        client.execute(new InsertRequest(task, timeZone)).fillTask(task);
        tasksToDelete.add(task);
        /*
         * verify created task, interpreted as legacy client
         */
        GetResponse getResponse = client.execute(new GetRequest(task));
        Task legacyTask = getResponse.getTask(timeZone, true);
        assertNotNull("No task created", legacyTask);
        assertEquals("Start date wrong", task.getStartDate(), legacyTask.getStartDate());
        assertEquals("End date wrong", task.getEndDate(), legacyTask.getEndDate());
        /*
         * verify created task, interpreted as new client
         */
        Task newTask = getResponse.getTask(timeZone, false);
        assertNotNull("No task created", newTask);
        assertTrue("Fulltime wrong", newTask.getFullTime());
        assertEquals("Start date wrong", D("22.10.2014 00:00", TimeZone.getTimeZone("UTC")), newTask.getStartDate());
        assertEquals("End date wrong", D("25.10.2014 00:00", TimeZone.getTimeZone("UTC")), newTask.getEndDate());
        /*
         * verify raw data
         */
        JSONObject data = (JSONObject) getResponse.getData();
        assertTrue("No fulltime", data.hasAndNotNull(CalendarFields.FULL_TIME));
        assertTrue("Fulltime wrong", data.getBoolean(CalendarFields.FULL_TIME));
        assertTrue("No start time", data.hasAndNotNull(TaskFields.START_TIME));
        assertEquals("Start time wrong",
            D("22.10.2014 00:00", TimeZone.getTimeZone("UTC")).getTime(), data.getLong(TaskFields.START_TIME));
        assertTrue("No end time", data.hasAndNotNull(TaskFields.END_TIME));
        assertEquals("End time wrong",
            D("25.10.2014 00:00", TimeZone.getTimeZone("UTC")).getTime(), data.getLong(TaskFields.END_TIME));
        assertTrue("No start date", data.hasAndNotNull(CalendarFields.START_DATE));
        assertEquals("Start date wrong", task.getStartDate().getTime(), data.getLong(CalendarFields.START_DATE));
        assertTrue("No end date", data.hasAndNotNull(CalendarFields.END_DATE));
        assertEquals("End date wrong", task.getEndDate().getTime(), data.getLong(CalendarFields.END_DATE));
    }

    @Test
    public void testUpdateWithLegacyClient() throws Exception {
        /*
         * create task as a new client would do
         */
        Task task = new Task();
        task.setStartDate(D("05.11.2014 20:00", timeZone));
        task.setEndDate(D("05.11.2014 21:00", timeZone));
        task.setFullTime(false);
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("testUpdateWithLegacyClient");
        client.execute(new InsertRequest(task, timeZone, false, true, false)).fillTask(task);
        tasksToDelete.add(task);
        /*
         * update task as legacy client, changing time-related properties
         */
        Task toUpdate = new Task();
        toUpdate.setObjectID(task.getObjectID());
        toUpdate.setParentFolderID(task.getParentFolderID());
        toUpdate.setLastModified(task.getLastModified());
        toUpdate.setStartDate(D("05.11.2014 00:00", TimeZone.getTimeZone("UTC")));
        toUpdate.setEndDate(D("06.11.2014 00:00", TimeZone.getTimeZone("UTC")));
        UpdateResponse updateResponse = client.execute(new UpdateRequest(toUpdate, timeZone));
        assertFalse("Errors in response", updateResponse.hasError());
        /*
         * verify updated task, interpreted as legacy client
         */
        GetResponse getResponse = client.execute(new GetRequest(task));
        Task legacyTask = getResponse.getTask(timeZone, true);
        assertNotNull("No task created", legacyTask);
        assertEquals("Start date wrong", toUpdate.getStartDate(), legacyTask.getStartDate());
        assertEquals("End date wrong", toUpdate.getEndDate(), legacyTask.getEndDate());
        /*
         * verify created task, interpreted as new client
         */
        Task newTask = getResponse.getTask(timeZone, false);
        assertNotNull("No task created", newTask);
        assertTrue("Fulltime wrong", newTask.getFullTime());
        assertEquals("Start date wrong", toUpdate.getStartDate(), newTask.getStartDate());
        assertEquals("End date wrong", toUpdate.getEndDate(), newTask.getEndDate());
        /*
         * verify raw data
         */
        JSONObject data = (JSONObject) getResponse.getData();
        assertTrue("No fulltime", data.hasAndNotNull(CalendarFields.FULL_TIME));
        assertTrue("Fulltime wrong", data.getBoolean(CalendarFields.FULL_TIME));
        assertTrue("No start time", data.hasAndNotNull(TaskFields.START_TIME));
        assertEquals("Start time wrong", toUpdate.getStartDate().getTime(), data.getLong(TaskFields.START_TIME));
        assertTrue("No end time", data.hasAndNotNull(TaskFields.END_TIME));
        assertEquals("End time wrong", toUpdate.getEndDate().getTime(), data.getLong(TaskFields.END_TIME));
        assertTrue("No start date", data.hasAndNotNull(CalendarFields.START_DATE));
        assertEquals("Start date wrong", toUpdate.getStartDate().getTime(), data.getLong(CalendarFields.START_DATE));
        assertTrue("No end date", data.hasAndNotNull(CalendarFields.END_DATE));
        assertEquals("End date wrong", toUpdate.getEndDate().getTime(), data.getLong(CalendarFields.END_DATE));
    }

    @Test
    public void testUpdateWithNewClient() throws Exception {
        /*
         * create task as a legacy client would do
         */
        Task task = new Task();
        task.setStartDate(D("23.10.2014 00:00", TimeZone.getTimeZone("UTC")));
        task.setEndDate(D("23.10.2014 00:00", TimeZone.getTimeZone("UTC")));
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("testUpdateWithNewClient");
        client.execute(new InsertRequest(task, timeZone)).fillTask(task);
        tasksToDelete.add(task);
        /*
         * update task as new client, changing time-related properties
         */
        Task toUpdate = new Task();
        toUpdate.setObjectID(task.getObjectID());
        toUpdate.setParentFolderID(task.getParentFolderID());
        toUpdate.setLastModified(task.getLastModified());
        toUpdate.setStartDate(D("23.10.2014 08:00", timeZone));
        toUpdate.setEndDate(D("24.10.2014 18:00", timeZone));
        toUpdate.setFullTime(false);
        UpdateResponse updateResponse = client.execute(new UpdateRequest(toUpdate, timeZone, true, false));
        assertFalse("Errors in response", updateResponse.hasError());
        /*
         * verify updated task, interpreted as legacy client
         */
        GetResponse getResponse = client.execute(new GetRequest(task));
        Task legacyTask = getResponse.getTask(timeZone, true);
        assertNotNull("No task created", legacyTask);
        assertEquals("Start date wrong", toUpdate.getStartDate(), legacyTask.getStartDate());
        assertEquals("End date wrong", toUpdate.getEndDate(), legacyTask.getEndDate());
        /*
         * verify created task, interpreted as new client
         */
        Task newTask = getResponse.getTask(timeZone, false);
        assertNotNull("No task created", newTask);
        assertFalse("Fulltime wrong", newTask.getFullTime());
        assertEquals("Start date wrong", toUpdate.getStartDate(), newTask.getStartDate());
        assertEquals("End date wrong", toUpdate.getEndDate(), newTask.getEndDate());
        /*
         * verify raw data
         */
        JSONObject data = (JSONObject) getResponse.getData();
        assertTrue("No fulltime", data.hasAndNotNull(CalendarFields.FULL_TIME));
        assertFalse("Fulltime wrong", data.getBoolean(CalendarFields.FULL_TIME));
        assertTrue("No start time", data.hasAndNotNull(TaskFields.START_TIME));
        assertEquals("Start time wrong",
            toUpdate.getStartDate().getTime() + timeZone.getOffset(toUpdate.getStartDate().getTime()), data.getLong(TaskFields.START_TIME));
        assertTrue("No end time", data.hasAndNotNull(TaskFields.END_TIME));
        assertEquals("End time wrong",
            toUpdate.getEndDate().getTime() + timeZone.getOffset(toUpdate.getEndDate().getTime()), data.getLong(TaskFields.END_TIME));
        assertTrue("No start date", data.hasAndNotNull(CalendarFields.START_DATE));
        assertEquals("Start date wrong", toUpdate.getStartDate().getTime(), data.getLong(CalendarFields.START_DATE));
        assertTrue("No end date", data.hasAndNotNull(CalendarFields.END_DATE));
        assertEquals("End date wrong", toUpdate.getEndDate().getTime(), data.getLong(CalendarFields.END_DATE));
    }

    @Test
    public void testUpdateOtherAttribute() throws Exception {
        /*
         * create task as a new client would do
         */
        Task task = new Task();
        task.setStartDate(D("28.10.2014 08:00", timeZone));
        task.setEndDate(D("01.11.2014 16:00", timeZone));
        task.setFullTime(false);
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("testUpdateOtherAttribute");
        client.execute(new InsertRequest(task, timeZone, false, true, false)).fillTask(task);
        tasksToDelete.add(task);
        /*
         * update task, not changing time-related properties
         */
        Task toUpdate = new Task();
        toUpdate.setObjectID(task.getObjectID());
        toUpdate.setParentFolderID(task.getParentFolderID());
        toUpdate.setLastModified(task.getLastModified());
        toUpdate.setNote("added notes");
        UpdateResponse updateResponse = client.execute(new UpdateRequest(toUpdate, timeZone));
        assertFalse("Errors in response", updateResponse.hasError());
        /*
         * verify updated task, interpreted as legacy client
         */
        GetResponse getResponse = client.execute(new GetRequest(task));
        Task legacyTask = getResponse.getTask(timeZone, true);
        assertNotNull("No task created", legacyTask);
        assertEquals("Start date wrong", task.getStartDate(), legacyTask.getStartDate());
        assertEquals("End date wrong", task.getEndDate(), legacyTask.getEndDate());
        assertEquals("Note wrong", toUpdate.getNote(), legacyTask.getNote());
        /*
         * verify created task, interpreted as new client
         */
        Task newTask = getResponse.getTask(timeZone, false);
        assertNotNull("No task created", newTask);
        assertFalse("Fulltime wrong", newTask.getFullTime());
        assertEquals("Start date wrong", task.getStartDate(), newTask.getStartDate());
        assertEquals("End date wrong", task.getEndDate(), newTask.getEndDate());
        assertEquals("Note wrong", toUpdate.getNote(), newTask.getNote());
        /*
         * verify raw data
         */
        JSONObject data = (JSONObject) getResponse.getData();
        assertTrue("No fulltime", data.hasAndNotNull(CalendarFields.FULL_TIME));
        assertFalse("Fulltime wrong", data.getBoolean(CalendarFields.FULL_TIME));
        assertTrue("No start time", data.hasAndNotNull(TaskFields.START_TIME));
        assertEquals("Start time wrong",
            task.getStartDate().getTime() + timeZone.getOffset(task.getStartDate().getTime()), data.getLong(TaskFields.START_TIME));
        assertTrue("No end time", data.hasAndNotNull(TaskFields.END_TIME));
        assertEquals("End time wrong",
            task.getEndDate().getTime() + timeZone.getOffset(task.getEndDate().getTime()), data.getLong(TaskFields.END_TIME));
        assertTrue("No start date", data.hasAndNotNull(CalendarFields.START_DATE));
        assertEquals("Start date wrong", task.getStartDate().getTime(), data.getLong(CalendarFields.START_DATE));
        assertTrue("No end date", data.hasAndNotNull(CalendarFields.END_DATE));
        assertEquals("End date wrong", task.getEndDate().getTime(), data.getLong(CalendarFields.END_DATE));
    }

    @Test
    public void testColumns() throws Exception {
        /*
         * create task as a new client would do
         */
        Task task = new Task();
        task.setStartDate(D("09.12.2014 19:15", timeZone));
        task.setEndDate(D("03.01.2015 14:00", timeZone));
        task.setFullTime(false);
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("testUpdateOtherAttribute");
        client.execute(new InsertRequest(task, timeZone, false, true, false)).fillTask(task);
        tasksToDelete.add(task);
        int[][] folderAndTaskIDs = new int[][] { { task.getParentFolderID(), task.getObjectID() } };
        /*
         * retrieve & check full_time
         */
        CommonListResponse listResponse = client.execute(new ListRequest(folderAndTaskIDs, new int[] { CalendarObject.FULL_TIME }));
        Object value = listResponse.getValue(0, CalendarObject.FULL_TIME);
        assertTrue("Wrong type for fulltime", null != value && Boolean.class.isInstance(value));
        assertFalse("Fulltime wrong", ((Boolean) value).booleanValue());
        /*
         * retrieve & check start_date
         */
        listResponse = client.execute(new ListRequest(folderAndTaskIDs, new int[] { CalendarObject.START_DATE }));
        value = listResponse.getValue(0, CalendarObject.START_DATE);
        assertTrue("Wrong type for start date", null != value && Long.class.isInstance(value));
        assertEquals("Start date wrong", task.getStartDate().getTime(), ((Long) value).longValue());
        /*
         * retrieve & check end_date
         */
        listResponse = client.execute(new ListRequest(folderAndTaskIDs, new int[] { CalendarObject.END_DATE }));
        value = listResponse.getValue(0, CalendarObject.END_DATE);
        assertTrue("Wrong type for end date", null != value && Long.class.isInstance(value));
        assertEquals("End date wrong", task.getEndDate().getTime(), ((Long) value).longValue());
        /*
         * retrieve & check start_time
         */
        listResponse = client.execute(new ListRequest(folderAndTaskIDs, new int[] { Task.START_TIME }));
        value = listResponse.getValue(0, Task.START_TIME);
        assertTrue("Wrong type for start time", null != value && Long.class.isInstance(value));
        assertEquals("Start time time",
            task.getStartDate().getTime() + timeZone.getOffset(task.getStartDate().getTime()), ((Long) value).longValue());
        /*
         * retrieve & check end_date
         */
        listResponse = client.execute(new ListRequest(folderAndTaskIDs, new int[] { Task.END_TIME }));
        value = listResponse.getValue(0, Task.END_TIME);
        assertTrue("Wrong type for end time", null != value && Long.class.isInstance(value));
        assertEquals("End time wrong",
            task.getEndDate().getTime() + timeZone.getOffset(task.getEndDate().getTime()), ((Long) value).longValue());
    }

}
