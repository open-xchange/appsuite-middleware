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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link Bug37927Test}
 * 
 * Task all request does not support sorting by column 317 and 316
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug37927Test extends AbstractAJAXSession {

    private AJAXClient client;
    private TimeZone timeZone;
    private List<Task> tasksToDelete;

    /**
     * Initializes a new {@link Bug37927Test}.
     *
     * @param name The test name
     */
    public Bug37927Test(String name) {
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
    public void testGetAllTasks() throws Exception {
        /*
         * create tasks
         */
        Task task1 = new Task();
        task1.setStartDate(D("27.03.2015 11:11", timeZone));
        task1.setEndDate(D("28.03.2015 13:00", timeZone));
        task1.setFullTime(false);
        task1.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task1.setTitle("testCreateWithNewClient");
        client.execute(new InsertRequest(task1, timeZone, false, true, false)).fillTask(task1);
        tasksToDelete.add(task1);
        Task task2 = new Task();
        task2.setStartDate(D("27.03.2015 12:12", timeZone));
        task2.setEndDate(D("28.03.2015 14:00", timeZone));
        task2.setFullTime(false);
        task2.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task2.setTitle("testCreateWithNewClient");
        client.execute(new InsertRequest(task2, timeZone, false, true, false)).fillTask(task2);
        tasksToDelete.add(task2);
        /*
         * get tasks via all, sorted by start_time
         */
        int[] columns = new int[] { Task.OBJECT_ID, Task.START_TIME, Task.END_TIME };
        CommonAllResponse allResponse = client.execute(new AllRequest(client.getValues().getPrivateTaskFolder(), columns, Task.START_TIME, Order.ASCENDING));
        assertFalse(allResponse.getErrorMessage(), allResponse.hasError());
        /*
         * get tasks via all, sorted by end_time
         */
        allResponse = client.execute(new AllRequest(client.getValues().getPrivateTaskFolder(), columns, Task.END_TIME, Order.DESCENDING));
        assertFalse(allResponse.getErrorMessage(), allResponse.hasError());
    }

}
