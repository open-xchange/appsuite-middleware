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

package com.openexchange.ajax.task;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.tasks.Task;

public class Bug13173Test extends AbstractAJAXSession {

    Task testTask;

    int folderId;

    TimeZone timezone;

    ArrayList<Task> duplicates;

    public Bug13173Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderId = getClient().getValues().getPrivateTaskFolder();
        timezone = getClient().getValues().getTimeZone();

        testTask = new Task();

        testTask.setTitle("Bug13173Test is testing Bug13173");
        testTask.setParentFolderID(folderId);
        testTask.setStartDate(new Date());
        testTask.setEndDate(new Date());
        testTask.setCreatedBy(getClient().getValues().getUserId());

        testTask.setRecurrenceType(Task.DAILY);
        testTask.setInterval(1);
        testTask.setOccurrence(2);

        testTask.setPercentComplete(75);
        testTask.setStatus(Task.IN_PROGRESS);

    }

    @Test
    public void testBug13173() {
        testTask = ttm.insertTaskOnServer(testTask);
        setTaskComplete(testTask);

        setTaskIncomplete(testTask);

        setTaskComplete(testTask);

        duplicates = getDuplicates();

        assertTrue("There are existing Duplicates", duplicates.size() <= 1);
    }

    private void setTaskComplete(Task task) {
        task.setPercentComplete(100);
        task.setStatus(Task.DONE);
        testTask = ttm.updateTaskOnServer(task);
    }

    private void setTaskIncomplete(Task task) {
        task.setPercentComplete(75);
        task.setStatus(Task.IN_PROGRESS);
        testTask = ttm.updateTaskOnServer(task);
    }

    private ArrayList<Task> getDuplicates() {
        Task[] allTasks = ttm.getAllTasksOnServer(folderId, Task.ALL_COLUMNS);

        ArrayList<Task> returnList = new ArrayList<Task>();
        if (allTasks.length != 0) {
            for (Task t : allTasks) {
                boolean title = null != t.getTitle() && t.getTitle().equals(testTask.getTitle());
                boolean creater = t.getCreatedBy() == testTask.getCreatedBy();
                boolean startDate = null != t.getStartDate() && t.getStartDate().equals(testTask.getStartDate());
                boolean percentComplete = t.getPercentComplete() == testTask.getPercentComplete();
                boolean oid = t.getObjectID() == testTask.getObjectID();

                //                if (!oid && title && creater) {
                if (!oid && title && creater && startDate && percentComplete) {
                    returnList.add(t);
                }
            }
        }

        return returnList;
    }
}
