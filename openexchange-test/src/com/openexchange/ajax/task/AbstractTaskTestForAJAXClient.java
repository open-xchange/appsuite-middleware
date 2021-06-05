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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.fail;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TaskTestManager;
import com.openexchange.test.common.groupware.tasks.TaskAsserts;
import com.openexchange.test.common.groupware.tasks.TestTask;

public abstract class AbstractTaskTestForAJAXClient extends AbstractAJAXSession {

    protected AbstractTaskTestForAJAXClient() {
        super();
    }

    public TestTask getNewTask() {
        return getNewTask("Default task, created by " + this.getClass().getName());
    }

    public TestTask getNewTask(String title) {
        TestTask task = new TestTask();
        task.setTitle(title);
        UserValues values = getClient().getValues();
        try {
            task.setTimezone(values.getTimeZone());
            task.setParentFolderID(values.getPrivateTaskFolder());
            task.setCreatedBy(values.getUserId());
            task.setModifiedBy(values.getUserId());
        } catch (Exception e) {
            fail("Setup failed, could not get necessary values for timezone or private folder");
        }
        return task;
    }

    /**
     * Does an insert and compares data from both get and all request
     */
    public void runSimpleInsertTest(Task task) {
        ttm.insertTaskOnServer(task);
        Task resultingTask = ttm.getTaskFromServer(task);
        Set<Integer> ignore = new HashSet<Integer>();
        ignore.add(I(Task.UID));
        ignore.add(I(Task.FULL_TIME)); // mimic legacy behavior
        TaskAsserts.assertAllTaskFieldsMatchExcept(task, resultingTask, ignore);
        ttm.cleanUp();
    }

    /**
     * Does an insert and an update and compares data from both get and all request
     * 
     * @param insertTask Task to insert at first
     * @param updateTask Task used to update insertTask - this tasks gets changed to have the correct LAST_MODIFIED, PARENT_FOLDER and OBJECT_ID, otherwise the update wouldn't work at all
     * @param fieldsThatChange Fields that are expected to change. These are not checked for being equal but for being changed - they are not ignored. The following fields are always ignored: CREATION_DATE, LAST_MODIFIED
     */
    public void runInsertAndUpdateTest(Task insertTask, Task updateTask, int... fieldsThatChange) {
        Set<Integer> changingFields = new HashSet<Integer>();
        for (int field : fieldsThatChange) {
            changingFields.add(Integer.valueOf(field));
        }
        changingFields.add(I(Task.CREATION_DATE));
        changingFields.add(I(Task.LAST_MODIFIED)); //must be different afterwards
        changingFields.add(I(Task.UID));
        changingFields.add(I(Task.FULL_TIME)); // mimic legacy behavior

        TaskTestManager testManager = new TaskTestManager(getClient());
        testManager.insertTaskOnServer(insertTask);

        updateTask.setLastModified(insertTask.getLastModified());
        updateTask.setParentFolderID(insertTask.getParentFolderID());
        updateTask.setObjectID(insertTask.getObjectID());
        testManager.updateTaskOnServer(updateTask);

        Task getResult = testManager.getTaskFromServer(insertTask);
        TaskAsserts.assertAllTaskFieldsMatchExcept(insertTask, getResult, changingFields);
        TaskAsserts.assertTaskFieldsDiffer(insertTask, getResult, changingFields);

        Task[] allResults = testManager.getAllTasksOnServer(insertTask.getParentFolderID());
        Task allResult = testManager.findTaskByID(insertTask.getObjectID(), allResults);
        TaskAsserts.assertAllTaskFieldsMatchExcept(insertTask, allResult, changingFields);
        TaskAsserts.assertTaskFieldsDiffer(insertTask, getResult, changingFields);

        testManager.cleanUp();
    }
}
