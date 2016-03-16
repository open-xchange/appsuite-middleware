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

import static com.openexchange.java.Autoboxing.I;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskAsserts;
import com.openexchange.groupware.tasks.TestTask;
import com.openexchange.test.TaskTestManager;

public abstract class AbstractTaskTestForAJAXClient extends AbstractAJAXSession {

    protected AbstractTaskTestForAJAXClient(String name) {
        super(name);
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
        TaskTestManager testManager = null;
        try {
            testManager = new TaskTestManager(getClient());
        } catch (Exception e) {
            fail("Setup failed, TestManager could not be instantiated");
        }
        testManager.insertTaskOnServer(task);
        Task resultingTask = testManager.getTaskFromServer(task);
        Set<Integer> ignore = new HashSet<Integer>();
        ignore.add(I(Task.UID));
        ignore.add(I(Task.FULL_TIME)); // mimic legacy behavior
        TaskAsserts.assertAllTaskFieldsMatchExcept(task, resultingTask, ignore);
        testManager.cleanUp();
    }

    /**
     * Does an insert and an update and compares data from both get and all request
     * @param insertTask Task to insert at first
     * @param updateTask Task used to update insertTask - this tasks gets changed to have the correct LAST_MODIFIED, PARENT_FOLDER and OBJECT_ID, otherwise the update wouldn't work at all
     * @param fieldsThatChange Fields that are expected to change. These are not checked for being equal but for being changed - they are not ignored. The following fields are always ignored: CREATION_DATE, LAST_MODIFIED
     */
    public void runInsertAndUpdateTest(Task insertTask, Task updateTask, int... fieldsThatChange) {
        Set<Integer> changingFields = new HashSet<Integer>();
        for(int field: fieldsThatChange){
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
