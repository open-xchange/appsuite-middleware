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

import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;


/**
 * {@link DeleteMultipleTaskTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeleteMultipleTaskTest extends AbstractAJAXSession {

    private AJAXClient client;
    private Task task1, task2;
    private TimeZone timeZone;

    /**
     * Initializes a new {@link DeleteMultipleTaskTest}.
     * @param name
     */
    public DeleteMultipleTaskTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(User.User1);
        timeZone = client.getValues().getTimeZone();

        task1 = new Task();
        task1.setTitle("Test 1");
        task1.setStartDate(new Date());
        task1.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *2));
        task1.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertRequest insReq1 = new InsertRequest(task1, timeZone);
        final InsertResponse insRes1 = client.execute(insReq1);
        insRes1.fillTask(task1);
        
        task2 = new Task();
        task2.setTitle("Test 2");
        task2.setStartDate(new Date());
        task2.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *2));
        task2.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertRequest insReq2 = new InsertRequest(task2, timeZone);
        final InsertResponse insRes2 = client.execute(insReq2);
        insRes2.fillTask(task2);
    }
    
    @Override
    public void tearDown() throws Exception {
        final GetRequest getReq1 = new GetRequest(task1.getParentFolderID(), task1.getObjectID(), false);
        final GetResponse getRes1 = client.execute(getReq1);
        if (!getRes1.hasError()) {
            final DeleteRequest delReq = new DeleteRequest(task1, false);
            client.execute(delReq);
        }

        final GetRequest getReq2 = new GetRequest(task2.getParentFolderID(), task2.getObjectID(), false);
        final GetResponse getRes2 = client.execute(getReq2);
        if (!getRes2.hasError()) {
            final DeleteRequest delReq = new DeleteRequest(task2, false);
            client.execute(delReq);
        }

        super.tearDown();
    }
    
    @Test
    public void testDeleteMultiple() throws Exception {
        final int[] ids = new int[] {task1.getObjectID(), task2.getObjectID()};
        final DeleteRequest delReq = new DeleteRequest(client.getValues().getPrivateTaskFolder(), ids, new Date(System.currentTimeMillis() + 300000), true);
        final CommonDeleteResponse delRes = client.execute(delReq);
        assertFalse("Multiple delete failed: " + delRes.getErrorMessage(), delRes.hasError());
    }

}
