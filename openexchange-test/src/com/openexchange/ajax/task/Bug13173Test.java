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

import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TaskTestManager;

public class Bug13173Test extends AbstractAJAXSession {

    Task testTask;

    int folderId;

    TimeZone timezone;

    TaskTestManager ttm;

    ArrayList<Task> duplicates;

    public Bug13173Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        folderId = client.getValues().getPrivateTaskFolder();
        timezone = client.getValues().getTimeZone();
        ttm = new TaskTestManager(client);

        testTask = new Task();

        testTask.setTitle("Bug13173Test is testing Bug13173");
        testTask.setParentFolderID(folderId);
        testTask.setStartDate(new Date());
        testTask.setEndDate(new Date());
        testTask.setCreatedBy(client.getValues().getUserId());

        testTask.setRecurrenceType(Task.DAILY);
        testTask.setInterval(1);
        testTask.setOccurrence(2);

        testTask.setPercentComplete(75);
        testTask.setStatus(Task.IN_PROGRESS);

    }

    @Override
    protected void tearDown() throws Exception {
        ttm.cleanUp();
        deleteDuplicates(duplicates);
        super.tearDown();
    }

    public void testBug13173() throws Exception {
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

    private void deleteDuplicates(ArrayList<Task> duplicates) {
        for (Task t : duplicates) {
            t.setParentFolderID(folderId);
            ttm.deleteTaskOnServer(t);
        }
    }

    /**
     * Only for cleaning up the database, if duplicates will not be deleted. Checks only for Task-Title!
     */
    private void deleteAll() {
        Task[] tasks = ttm.getAllTasksOnServer(folderId);
        for (Task t : tasks) {
            if (t.getTitle().equals(testTask.getTitle())) {
                t.setParentFolderID(folderId);
                ttm.deleteTaskOnServer(t);
            }
        }
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
