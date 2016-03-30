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
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AllTest extends AbstractTaskTest {

    private static final int NUMBER = 10;
    private AJAXClient client;

    public AllTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testAll() throws Throwable {
        final InsertRequest[] inserts = new InsertRequest[NUMBER];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setTitle("Task " + (i + 1));
            task.setParentFolderID(getPrivateFolder());
            task.setAlarm(new Date());
            // TODO add participants
            inserts[i] = new InsertRequest(task, getTimeZone());
        }
        final MultipleResponse<InsertResponse> mInsert = client.execute(MultipleRequest.create(inserts));
        final GetRequest[] gets = new GetRequest[NUMBER];
        for (int i = 0; i < gets.length; i++) {
            final InsertResponse ins = mInsert.getResponse(i);
            gets[i] = new GetRequest(ins);
        }
        final MultipleResponse<GetResponse> mGet = client.execute(MultipleRequest.create(gets));
        // TODO Read Task.ALARM
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.LAST_MODIFIED, Task.FOLDER_ID, Task.PARTICIPANTS };
        client.execute(new AllRequest(getPrivateFolder(), columns, 0, null));
        final DeleteRequest[] deletes = new DeleteRequest[inserts.length];
        for (int i = 0; i < inserts.length; i++) {
            final GetResponse get = mGet.getResponse(i);
            deletes[i] = new DeleteRequest(get.getTask(getTimeZone()));
        }
        client.execute(MultipleRequest.create(deletes));
    }
}
