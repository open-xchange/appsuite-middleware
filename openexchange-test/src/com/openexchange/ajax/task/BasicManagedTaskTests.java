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

import static com.openexchange.groupware.calendar.TimeTools.removeMilliseconds;
import java.util.Date;
import com.openexchange.groupware.tasks.Task;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class BasicManagedTaskTests extends ManagedTaskTest {

    public BasicManagedTaskTests(String name) {
        super(name);
    }

    public void testCreateAndGet() {
        Task expected = generateTask("Create test");
        manager.insertTaskOnServer(expected);
        actual = manager.getTaskFromServer(expected);
        assertEquals("Should have the same title", expected.getTitle(), actual.getTitle());
        assertEquals("Should have the same folder", expected.getParentFolderID(), actual.getParentFolderID());
        assertEquals("Should have the same start date", removeMilliseconds(expected.getStartDate()), actual.getStartDate());
        assertEquals("Should have the same end date", removeMilliseconds(expected.getEndDate()), actual.getEndDate());
        assertEquals("Should have the same last modified", expected.getLastModified(), actual.getLastModified());
    }

    public void testAll() {
        int numberBefore = manager.getAllTasksOnServer(folderID, new int[] { 1, 4, 5, 20, 209 }).length;
        Task expected = generateTask("Create test");
        manager.insertTaskOnServer(expected);
        Task[] allTasksOnServer = manager.getAllTasksOnServer(folderID, new int[] { 1, 4, 5, 20, 209 });
        actual = null;
        for(Task temp: allTasksOnServer){
            if(expected.getObjectID() == temp.getObjectID()) {
                actual = temp;
            }
        }
        assertEquals("Should find one more element than before", numberBefore + 1, allTasksOnServer.length);
        assertNotNull("Should find the newly created element", actual);
        assertEquals("Should have the same field #1 (id)", expected.get(1), actual.get(1));
        //assertEquals("Should have the same field #4 (creation date)", expected.get(4), actual.get(4));
        //assertEquals("Should have the same field #5 (last modified)", expected.get(5), actual.get(5));
        //assertEquals("Should have the same field #20 (folder)", expected.get(20), actual.get(20));
        assertEquals("Should have the same field #209", expected.get(209), actual.get(209));
    }

    public void testUpdateAndReceiveUpdates() {
        Task expected = generateTask("Create test");
        manager.insertTaskOnServer(expected);

        Task updated = generateTask("Updates Test");
        updated.setParentFolderID(expected.getParentFolderID());
        updated.setObjectID(expected.getObjectID());
        updated.setLastModified(expected.getLastModified());

        manager.updateTaskOnServer(updated);

        Date aMillisecondEarlier = new Date(expected.getLastModified().getTime()-1);
        Task[] updates = manager.getUpdatedTasksOnServer(folderID, new int[] { 1, 4, 5, 209 }, aMillisecondEarlier);
        assertEquals("Should find one update only", 1, updates.length);

        actual = updates[0];
        assertEquals("Should have the same field #1", expected.get(1), actual.get(1));
        //assertEquals("Should have the same field #4", expected.get(4), actual.get(4));
        //assertEquals("Should have the same field #5", expected.get(5), actual.get(5));
        //assertEquals("Should have the same field #20", expected.get(20), actual.get(20));
        assertEquals("Should have the same field #209", expected.get(209), actual.get(209));
    }

    public void testCreateAndDelete() {
        Task task = generateTask("Create test");
        manager.insertTaskOnServer(task);

        manager.deleteTaskOnServer(task);
        boolean fail = false;
        try {
            manager.getTaskFromServer(task);
            fail = true;
        } catch (AssertionError e) {

        }
        if (fail) {
            fail("Should fail by not finding task");
        }
    }

}
