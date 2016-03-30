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

package com.openexchange.test;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.ajax.task.actions.TaskUpdatesResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.ajax.task.actions.UpdatesRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TestTask;

/**
 * {@link TaskTestManager}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TaskTestManager implements TestManager{

    protected List<Task> createdEntities;

    private AJAXClient client;

    protected TimeZone timezone;

    protected int taskFolderId;

    private boolean failOnError;

    private AbstractAJAXResponse lastResponse;

    private Throwable lastException;

    public TaskTestManager(AJAXClient client) {
        setFailOnError(true);
        this.setClient(client);
        createdEntities = new LinkedList<Task>();
        try {
            taskFolderId = client.getValues().getPrivateTaskFolder();
        } catch (Exception e) {
            doHandleExeption(e, "getting private task folder from user values");
        }
        try {
            timezone = client.getValues().getTimeZone();
        } catch (OXException e) {
            //no matter, fix it in finally block
        } catch (IOException e) {
            //no matter, fix it in finally block
        } catch (JSONException e) {
            //no matter, fix it in finally block
        } finally {
            if (timezone == null) {
                timezone = TimeZone.getDefault();
            }
        }
    }

    /**
     * Creates a task via HTTP-API and updates it with new id, timestamp and all other information that is updated after such requests.
     */
    public Task insertTaskOnServer(Task taskToCreate) {
        createdEntities.add(taskToCreate);
        InsertRequest request = new InsertRequest(taskToCreate, timezone);
        InsertResponse response = null;
        try {
            response = getClient().execute(request);
            setLastResponse(response);
            response.fillTask(taskToCreate);
        } catch (Exception e) {
            doHandleExeption(e, "NewRequest on folder " + taskToCreate.getParentFolderID());
        }

        return taskToCreate;
    }

    public Task updateTaskOnServer(Task taskToUpdate) {
        UpdateRequest request = new UpdateRequest(taskToUpdate, timezone);
        UpdateResponse response = null;
        try {
            response = getClient().execute(request);
            setLastResponse(response);
        } catch (Exception e) {
            doHandleExeption(e, "UpdateRequest for task ID " + taskToUpdate.getObjectID());
        }
        taskToUpdate.setLastModified(response.getTimestamp());
        for (CalendarObject co : createdEntities) {
            if (taskToUpdate.getObjectID() == co.getObjectID()) {
                co.setLastModified(response.getTimestamp());
                continue;
            }
        }
        return taskToUpdate;
    }

    public Task moveTaskOnServer(Task taskToMove, int sourceFolder) {
        UpdateRequest request = new UpdateRequest(sourceFolder, taskToMove, timezone);
        UpdateResponse response = null;
        try {
            response = getClient().execute(request);
            setLastResponse(response);
            taskToMove.setLastModified(response.getTimestamp());
        } catch (Exception e) {
            doHandleExeption(e, "MoveRequest");
        }
        return taskToMove;
    }

    public void deleteTaskOnServer(Task taskToDelete) {
        deleteTaskOnServer(taskToDelete, true);
    }

    public void deleteTaskOnServer(Task taskToDelete, boolean failOnErrorOverride) {
        DeleteRequest request = new DeleteRequest(taskToDelete, failOnErrorOverride);
        try {
            setLastResponse(getClient().execute(request));
        } catch (Exception e) {
            doHandleExeption(e, "DeleteRequest for " + taskToDelete.getObjectID());
        }
        createdEntities.remove(taskToDelete); // TODO matches the right task?
    }

    public Task getTaskFromServer(int folder, int objectId) {
        GetRequest request = new GetRequest(folder, objectId, getFailOnError());
        GetResponse response = null;
        try {
            response = getClient().execute(request);
            setLastResponse(response);
            return response.getTask(timezone);
        } catch (Exception e) {
            doHandleExeption(e, "TaskRequest for task id " + objectId);
        }
        return null;
    }

    public Task getTaskFromServer(Task task) {
        return getTaskFromServer(task.getParentFolderID(), task.getObjectID());
    }

    public Task[] getUpdatedTasksOnServer(int folder, int[] columns, Date lastModified) {
        UpdatesRequest req = new UpdatesRequest(folder, columns, -1, null, lastModified);
        TaskUpdatesResponse resp = null;
        try {
            resp = getClient().execute(req);
            setLastResponse(resp);
        } catch (Exception e) {
            doHandleExeption(e, "UpdatesRequest");
            return null;
        }
        return resp.getTasks().toArray(new Task[] {});
    }

    /**
     * Performs an AllRequest for all columns on the server and returns the tasks in a requested folder.
     */
    public Task[] getAllTasksOnServer(int folderID, int[] columns) {
        AllRequest allTasksRequest = new AllRequest(folderID, columns, Task.OBJECT_ID, Order.ASCENDING);
        try {
            CommonAllResponse allTasksResponse = getClient().execute(allTasksRequest);
            setLastResponse(allTasksResponse);
            JSONArray jsonTasks = (JSONArray) allTasksResponse.getData();
            List<Task> tasks = new LinkedList<Task>();
            for (int j = 0; j < jsonTasks.length(); j++) {
                JSONArray taskAsArray = (JSONArray) jsonTasks.get(j);
                Task task = transformArrayToTask(taskAsArray, allTasksResponse.getColumns());
                tasks.add(task);
            }
            return tasks.toArray(new Task[tasks.size()]);

        } catch (Exception e) {
            doHandleExeption(e, "AllRequest for folder " + folderID);
        }
        return null;
    }

    /**
     * Performs an AllRequest for all columns on the server and returns the tasks in a requested folder.
     *
     * @param folderID
     * @return
     */
    public Task[] getAllTasksOnServer(int folderID) {
        return getAllTasksOnServer(folderID, Task.ALL_COLUMNS);
    }

    public Task[] listContactsOnServer(final int[][] folderAndTaskIds, final int[] columns) {
        ListRequest req = new ListRequest(folderAndTaskIds, columns);
        CommonListResponse resp = null;
        List<Task> tasks = null;
        try {
            resp = getClient().execute(req);
            setLastResponse(resp);
            tasks = transformArrayToTasks((JSONArray) resp.getData(), resp.getColumns());
        } catch (Exception e) {
            doHandleExeption(e, "ListRequest");
        }
        return (tasks == null) ? null : tasks.toArray(new Task[]{});
    }

    public Task searchForTasksOnServer() {
        return null;
    }

    /**
     * Transforms a value object into whatever is required for that column, e.g. a date object for the column start_date.
     *
     * @param column
     * @param value
     * @return
     */
    protected static Object transformColumn(final int column, final Object value) {
        final Object retval;
        switch (column) {
        case Task.CREATION_DATE:
        case Appointment.LAST_MODIFIED:
        case Task.START_DATE:
        case Task.END_DATE:
        case Task.RECURRENCE_DATE_POSITION:
        case Task.UNTIL:
        case Task.DATE_COMPLETED:
            retval = new Date(((Long) value).longValue());
            break;
        case Task.ACTUAL_DURATION:
        case Task.TARGET_DURATION:
            retval = Long.valueOf((String) value);
            break;
        case Task.ACTUAL_COSTS:
        case Task.TARGET_COSTS:
            retval = new BigDecimal(value.toString());
            break;
//        case Task.PERCENT_COMPLETED:
//            retval = Integer.valueOf(((Long) value).intValue());
//            break;
        case Task.BILLING_INFORMATION:
            retval = value;
            break;
        case Task.PRIORITY:
            retval = Integer.valueOf(String.valueOf(value));
            break;
        default:
            retval = value;
        }
        return retval;
    }

    protected List<Task> transformArrayToTasks(JSONArray tasks, int[] columns) throws JSONException{
        LinkedList<Task> results = new LinkedList<Task>();
        for(int i = 0, length = tasks.length(); i < length; i++){
            results.add(transformArrayToTask( tasks.getJSONArray(i), columns));
        }
        return results;
    }
    /**
     * An AllRequest answers with a JSONArray of JSONArrays, each of which contains a field belonging to a task. This method assembles a
     * task from this array.
     *
     * @return
     * @throws JSONException
     */
    protected static Task transformArrayToTask(JSONArray taskAsArray, int[] columns) throws JSONException {
        Task resultingTask = new Task();

        for (int i = 0; i < columns.length; i++) {
            int column = columns[i];
            Mapper attributeMapping = Mapping.getMapping(column);
            if (taskAsArray.isNull(i) || attributeMapping == null || taskAsArray.get(i) == null) {
                continue;
            }
            // FIXME the following method does not honor, that the backend sends shifted timestamps for JavaScript.
            // FIXME set time zone in frontend to Pacific/Honolulu and some tests fail!
            Object newValue = transformColumn(column, taskAsArray.get(i));
            attributeMapping.set(resultingTask, newValue);
        }

        return resultingTask;
    }

    /**
     * removes all tasks created by this fixture
     */
    @Override
    public void cleanUp() {
        for (Task task : new LinkedList<Task>(createdEntities)) {
            deleteTaskOnServer(task);
        }
    }

    /**
     * Finds a task within a list of tasks. Fails if not found and returns null;
     *
     * @param tasks
     * @return
     */
    public Task findTaskByID(int id, List<Task> tasks) {
        for (Task task : tasks) {
            if (id == task.getObjectID()) {
                return task;
            }
        }
        fail("Task with id=" + id + " not found");
        return null;
    }

    /**
     * Finds a task within an array of tasks. Fails if not found and returns null;
     *
     * @param tasks
     * @return
     */
    public Task findTaskByID(int id, Task[] tasks) {
        return findTaskByID(id, Arrays.asList(tasks));
    }

    /**
     * Constructs a new TestTask with the given title and time zone, parent folder created by and modified by already initialized
     */
    public TestTask newTask(String title) throws OXException, IOException, SAXException, JSONException {
        TestTask task = new TestTask();
        task.setTitle(title);

        UserValues values = getClient().getValues();
        task.setTimezone(values.getTimeZone());
        task.setParentFolderID(values.getPrivateTaskFolder());
        task.setCreatedBy(values.getUserId());
        task.setModifiedBy(values.getUserId());

        return task;
    }

    /**
     * Constructs a new TestTask with the given title and time zone, parent folder created by and modified by already initialized
     */
    public TestTask newTask(String title, int parentFolder) throws OXException, IOException, SAXException, JSONException {
        TestTask task = new TestTask();
        task.setTitle(title);

        UserValues values = getClient().getValues();
        task.setTimezone(values.getTimeZone());
        task.setParentFolderID(parentFolder);
        task.setCreatedBy(values.getUserId());
        task.setModifiedBy(values.getUserId());

        return task;
    }

    private void doHandleExeption(Exception exc, String action) {
        try {
            lastException = exc;
            throw exc;
        } catch (OXException e) {
            if (getFailOnError()) {
                fail("AJAXException during " + action + ": " + e.getMessage());
            }
        } catch (IOException e) {
            if (getFailOnError()) {
                fail("IOException during " + action + ": " + e.getMessage());
            }
        } catch (SAXException e) {
            if (getFailOnError()) {
                fail("SAXException during " + action + ": " + e.getMessage());
            }
        } catch (JSONException e) {
            if (getFailOnError()) {
                fail("JSONException during " + action + ": " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (getFailOnError()) {
                fail("Unexpected exception during " + action + ": " + e.getMessage());
            }
        }
    }

    public void setLastResponse(AbstractAJAXResponse lastResponse) {
        this.lastResponse = lastResponse;
    }

    @Override
    public AbstractAJAXResponse getLastResponse() {
        return lastResponse;
    }

    public void setClient(AJAXClient client) {
        this.client = client;
    }

    public AJAXClient getClient() {
        return client;
    }

    @Override
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public boolean getFailOnError() {
        return failOnError;
    }

    @Override
    public boolean doesFailOnError() {
        return getFailOnError();
    }

    @Override
    public Throwable getLastException() {
        return this.lastException;
    }

    @Override
    public boolean hasLastException() {
        return this.lastException != null;
    }
}
