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

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug16006Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16006Test extends AbstractAJAXSession {

    private AJAXClient client;
    private Task task;
    private Date alarm;

    public Bug16006Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client.execute(new SetRequest(Tree.TimeZone, "Pacific/Honolulu"));
        task = new Task();
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("Test for bug 16006");
        Calendar calendar = TimeTools.createCalendar(TimeZones.UTC);
        alarm = calendar.getTime();
        task.setAlarm(calendar.getTime());
        InsertRequest request = new InsertRequest(task, TimeZones.UTC, true, true);
        InsertResponse response = client.execute(request);
        response.fillTask(task);
    }

    @Test
    public void testAlarm() throws Throwable {
        GetRequest[] requests = new GetRequest[1];
        requests[0] = new GetRequest(task, TimeZones.UTC);
        GetResponse response = client.execute(MultipleRequest.create(requests)).getResponse(0);
        Task testTask = response.getTask(TimeZones.UTC);
        assertEquals("Alarm does not match.", alarm, testTask.getAlarm());
    }
}
