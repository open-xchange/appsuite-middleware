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

package com.openexchange.webdav.xml.task;

import static com.openexchange.java.Autoboxing.L;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.TaskTest;
import com.openexchange.webdav.xml.XmlServlet;

public class ListTest extends TaskTest {

    public ListTest(final String name) {
        super(name);
    }

    public void testPropFindWithModified() throws Exception {
        final Task taskObj = createTask("testPropFindWithModified");
        final int objectId1 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);
        insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);

        // prevent master/slave problem
        Thread.sleep(1000);

        final Task loadTask = loadTask(getWebConversation(), objectId1, taskFolderId, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadTask.getLastModified();

        final Task[] taskArray = listTask(webCon, taskFolderId, decrementDate(modified), true, false, PROTOCOL + hostName, login, password, context);

        assertTrue("check response", taskArray.length >= 2);
    }

    public void testPropFindWithDelete() throws Exception {
        final Task taskObj = createTask("testPropFindWithModified");
        final int objectId1 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);
        final int objectId2 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);

        // prevent master/slave problem
        Thread.sleep(1000);

        final Task loadTask = loadTask(getWebConversation(), objectId1, taskFolderId, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadTask.getLastModified();

        final int[][] objectIdAndFolderId = { { objectId1, taskFolderId }, { objectId2, taskFolderId } };

        deleteTask(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);

        final Task[] taskArray = listTask(webCon, taskFolderId, decrementDate(modified), false, true, PROTOCOL + hostName, login, password, context);

        assertTrue("wrong response array length", taskArray.length >= 2);
    }

    public void testPropFindWithObjectId() throws Exception {
        final Task taskObj = createTask("testPropFindWithObjectId");
        final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);

        loadTask(webCon, objectId, taskFolderId, PROTOCOL + hostName, login, password, context);
    }

    public void testObjectNotFound() throws Exception {
        final Task taskObj = createTask("testObjectNotFound");
        final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);

        try {
            loadTask(webCon, (objectId+1000), taskFolderId, PROTOCOL + hostName, login, password, context);
            fail("object not found exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        final int[][] objectIdAndFolderId = { { objectId ,taskFolderId } };
        deleteTask(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context );
    }

    public void testListWithAllFields() throws Exception {
        final Task taskObj = new Task();
        taskObj.setTitle("testListWithAllFields");
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setParentFolderID(taskFolderId);
        taskObj.setPrivateFlag(true);
        taskObj.setLabel(2);
        taskObj.setNote("note");
        taskObj.setCategories("testcat1,testcat2,testcat3");
        taskObj.setActualCosts(new BigDecimal("1.50"));
        taskObj.setActualDuration(L(210));
        taskObj.setBillingInformation("billing information");
        taskObj.setCompanies("companies");
        taskObj.setCurrency("currency");
        taskObj.setDateCompleted(dateCompleted);
        taskObj.setPercentComplete(50);
        taskObj.setPriority(Task.HIGH);
        taskObj.setStatus(Task.IN_PROGRESS);
        taskObj.setTargetCosts(new BigDecimal("5.50"));
        taskObj.setTargetDuration(L(450));
        taskObj.setTripMeter("trip meter");

        final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);

        // prevent master/slave problem
        Thread.sleep(1000);

        Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadTask.getLastModified();

        final Task[] taskArray = listTask(webCon, taskFolderId, decrementDate(modified), true, false, PROTOCOL + hostName, login, password, context);

        assertEquals("wrong response array length", 1, taskArray.length);

        loadTask = taskArray[0];

        taskObj.setObjectID(objectId);
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        compareObject(taskObj, loadTask);
    }

    public void testListWithAllFieldsOnUpdate() throws Exception {
        Task taskObj = createTask("testListWithAllFieldsOnUpdate");
        final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);

        Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadTask.getLastModified();

        taskObj = new Task();
        taskObj.setTitle("testListWithAllFieldsOnUpdate");
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setPrivateFlag(true);
        taskObj.setLabel(2);
        taskObj.setNote("note");
        taskObj.setCategories("testcat1,testcat2,testcat3");
        taskObj.setActualCosts(new BigDecimal("1.50"));
        taskObj.setActualDuration(L(210));
        taskObj.setBillingInformation("billing information");
        taskObj.setCompanies("companies");
        taskObj.setCurrency("currency");
        taskObj.setDateCompleted(dateCompleted);
        taskObj.setPercentComplete(50);
        taskObj.setPriority(Task.HIGH);
        taskObj.setStatus(Task.IN_PROGRESS);
        taskObj.setTargetCosts(new BigDecimal("5.50"));
        taskObj.setTargetDuration(L(450));
        taskObj.setTripMeter("trip meter");
        taskObj.setParentFolderID(taskFolderId);

        updateTask(webCon, taskObj, objectId, taskFolderId, PROTOCOL + hostName, login, password, context);

        final Task[] taskArray = listTask(webCon, taskFolderId, decrementDate(modified), true, false, PROTOCOL + hostName, login, password, context);

        loadTask = null;
        for (int i = 0; i < taskArray.length && loadTask == null; i++) {
            if (taskArray[i].getObjectID() == objectId) {
                loadTask = taskArray[i];
            }
        }
        assertNotNull("unable to find updated task.", loadTask);

        taskObj.setObjectID(objectId);
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setParentFolderID(taskFolderId);
        compareObject(taskObj, loadTask);
    }

    public void testList() throws Exception {
        final Task taskObj = createTask("testList");
        final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);

        final int[] idArray = listTask(getWebConversation(), taskFolderId, getHostName(), getLogin(), getPassword(), context);

        boolean found = false;
        for (int a = 0; a < idArray.length; a++) {
            if (idArray[a] == objectId) {
                found = true;
                break;
            }
        }

        assertTrue("id " + objectId + " not found in response", found);
        deleteTask(getWebConversation(), objectId, taskFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }
}
