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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import static junit.framework.Assert.fail;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TestTask;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * {@link TaskTestManager}
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TaskTestManager {

    protected List<Task> createdEntities;

    protected AJAXClient client;

    protected TimeZone timezone;

    protected int taskFolderId;

    public TaskTestManager(AJAXClient client) {
        this.client = client;
        createdEntities = new LinkedList<Task>();
        try {
            taskFolderId = client.getValues().getPrivateTaskFolder();
        } catch (AjaxException e) {
            fail("AjaxException during task creation: " + e.getLocalizedMessage());
        } catch (IOException e) {
            fail("IOException during task creation: " + e.getLocalizedMessage());
        } catch (SAXException e) {
            fail("SAXException during task creation: " + e.getLocalizedMessage());
        } catch (JSONException e) {
            fail("JSONException during task creation: " + e.getLocalizedMessage());
        }
        try {
            timezone = client.getValues().getTimeZone();
        } catch (AjaxException e) {
        } catch (IOException e) {
        } catch (SAXException e) {
        } catch (JSONException e) {
        } finally {
            if (timezone == null) {
                timezone = TimeZone.getTimeZone("Europe/Berlin");
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
            response = client.execute(request);
            response.fillTask(taskToCreate);
        } catch (AjaxException e) {
            fail("AjaxException during task creation: " + e.getLocalizedMessage());
        } catch (IOException e) {
            fail("IOException during task creation: " + e.getLocalizedMessage());
        } catch (SAXException e) {
            fail("SAXException during task creation: " + e.getLocalizedMessage());
        } catch (JSONException e) {
            fail("JSONException during task creation: " + e.getLocalizedMessage());
        }

        return taskToCreate;
    }

    public Task updateTaskOnServer(Task taskToUpdate) {
        UpdateRequest request = new UpdateRequest(taskToUpdate, timezone);
        UpdateResponse response = null;
        try {
            response = client.execute(request);
        } catch (AjaxException e) {
            fail("AjaxException during task update: " + e.getLocalizedMessage());
        } catch (IOException e) {
            fail("IOException during task update: " + e.getLocalizedMessage());
        } catch (SAXException e) {
            fail("SAXException during task update: " + e.getLocalizedMessage());
        } catch (JSONException e) {
            fail("JSONException during task update: " + e.getLocalizedMessage());
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
            response = client.execute(request);
        } catch (AjaxException e) {
            fail("AjaxException during task update: " + e.getLocalizedMessage());
        } catch (IOException e) {
            fail("IOException during task update: " + e.getLocalizedMessage());
        } catch (SAXException e) {
            fail("SAXException during task update: " + e.getLocalizedMessage());
        } catch (JSONException e) {
            fail("JSONException during task update: " + e.getLocalizedMessage());
        }
        taskToMove.setLastModified(response.getTimestamp());
        return taskToMove;
    }
    
    public void deleteTaskOnServer(Task taskToDelete) {
        deleteTaskOnServer(taskToDelete, true);
    }
    
    public void deleteTaskOnServer(Task taskToDelete, boolean failOnError) {
        DeleteRequest request = new DeleteRequest(taskToDelete);
        try {
            client.execute(request);
        } catch (AjaxException e) {
            if(failOnError)
                fail("AjaxException during deletion of task " + taskToDelete.getObjectID() + ": " + e.getLocalizedMessage());
        } catch (IOException e) {
            if(failOnError)
                fail("IOException during deletion of task " + taskToDelete.getObjectID() + ": " + e.getLocalizedMessage());
        } catch (SAXException e) {
            if(failOnError)
                fail("SAXException during deletion of task " + taskToDelete.getObjectID() + ": " + e.getLocalizedMessage());
        } catch (JSONException e) {
            if(failOnError)
                fail("JSONException during deletion of task " + taskToDelete.getObjectID() + ": " + e.getLocalizedMessage());
        }
    }

    public Task getTaskFromServer(int folder, int objectId) {
        return getTaskFromServer(folder, objectId, true);
    }

    public Task getTaskFromServer(int folder, int objectId, boolean failOnError) {
        GetRequest request = new GetRequest(folder, objectId, failOnError);
        GetResponse response = null;
        try {
            response = client.execute(request);
            return response.getTask(timezone);
        } catch (AjaxException e) {
            if (failOnError)
                fail("AjaxException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
        } catch (IOException e) {
            if (failOnError)
                fail("IOException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
        } catch (SAXException e) {
            if (failOnError)
                fail("SAXException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
        } catch (JSONException e) {
            if (failOnError)
                fail("JSONException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
        } catch (OXJSONException e) {
            if (failOnError)
                fail("OXJSONException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
        }
        return null;
    }

    public Task getTaskFromServer(Task task) {
        return getTaskFromServer(task.getParentFolderID(), task.getObjectID());
    }

    public Task getTaskFromServer(Task task, boolean failOnError) {
        return getTaskFromServer(task.getParentFolderID(), task.getObjectID(), failOnError);
    }
    /**
     * Performs an AllRequest for all columns on the server and returns the tasks in a requested folder.
     * 
     * @param folderID
     * @return
     */
    public Task[] getAllTasksOnServer(int folderID) {
        AllRequest allTasksRequest = new AllRequest(folderID, Task.ALL_COLUMNS, Task.OBJECT_ID, Order.ASCENDING);
        try {
            CommonAllResponse allTasksResponse = client.execute(allTasksRequest);
            JSONArray jsonTasks = (JSONArray) allTasksResponse.getData();
            List<Task> tasks = new LinkedList<Task>();
            for (int j = 0; j < jsonTasks.length(); j++) {
                JSONArray taskAsArray = (JSONArray) jsonTasks.get(j);
                Task task = transformAllRequestArrayToTask(taskAsArray);
                tasks.add(task);
            }
            return tasks.toArray(new Task[tasks.size()]);

        } catch (AjaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        case AppointmentObject.LAST_MODIFIED:
        case Task.START_DATE:
        case Task.END_DATE:
        case Task.RECURRENCE_DATE_POSITION:
        case Task.UNTIL:
            retval = new Date(((Long) value).longValue());
            break;
        case Task.ACTUAL_DURATION:
        case Task.TARGET_DURATION:
            retval = new Long((String) value);
            break;
        case Task.ACTUAL_COSTS:
        case Task.TARGET_COSTS:
            retval = new Float((String) value);
            break;
        default:
            retval = value;
        }
        return retval;
    }

    /**
     * An AllRequest answers with a JSONArray of JSONArray, each of which contains a field belonging to a task. This method assembles a task
     * from this array.
     * 
     * @return
     * @throws JSONException
     */
    protected static Task transformAllRequestArrayToTask(JSONArray taskAsArray) throws JSONException {
        Task resultingTask = new Task();

        for (int i = 0; i < Task.ALL_COLUMNS.length; i++) {
            int column = Task.ALL_COLUMNS[i];
            Mapper attributeMapping = Mapping.getMapping(column);
            if (taskAsArray.isNull(i) || attributeMapping == null || taskAsArray.get(i) == null)
                continue;

            Object newValue = transformColumn(column, taskAsArray.get(i));
            attributeMapping.set(resultingTask, newValue);
        }

        return resultingTask;
    }

    public Task listContactsOnServer() {
        return null;

    }

    public Task searchForTasksOnServer() {
        return null;

    }

    public Task getUpdatedTasksOnServer() {
        return null;

    }

    /**
     * removes all tasks created by this fixture
     */
    public void cleanUp() {
        for (Task task : createdEntities) {
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
            if (id == task.getObjectID())
                return task;
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
    public TestTask newTask(String title) throws AjaxException, IOException, SAXException, JSONException {
        TestTask task = new TestTask();
        task.setTitle(title);

        UserValues values = client.getValues();
        task.setTimezone(values.getTimeZone());
        task.setParentFolderID(values.getPrivateTaskFolder());
        task.setCreatedBy(values.getUserId());
        task.setModifiedBy(values.getUserId());

        return task;
    }
}
