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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * Tests actual and target duration and costs set to 0.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15580Test extends AbstractTaskTest {

    private static final int[] ATTRIBUTE_IDS = { Task.TARGET_DURATION, Task.ACTUAL_DURATION, Task.TARGET_COSTS, Task.ACTUAL_COSTS };

    private AJAXClient client;

    private int folderId;

    private TimeZone tz;

    private Task task;

    public Bug15580Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateTaskFolder();
        tz = client.getValues().getTimeZone();
        task = Create.createWithDefaults(folderId, "Bug 10071 test task");
        for (int attributeId : ATTRIBUTE_IDS) {
            setToNull(Mapping.getMapping(attributeId));
        }
        final InsertResponse insertR = client.execute(new InsertRequest(task, tz));
        insertR.fillTask(task);
    }

    private void setToNull(Mapper<?> mapper) {
        mapper.set(task, null);
    }

    @Test
    public void testDurationAndCostsSetToZero() throws OXException, IOException, JSONException {
        GetRequest request = new GetRequest(task);
        GetResponse response = client.execute(request);
        Task toTest = response.getTask(tz);
        ListIDs ids = ListIDs.l(new int[] { task.getParentFolderID(), task.getObjectID() });
        ListRequest request2 = new ListRequest(ids, ATTRIBUTE_IDS);
        CommonListResponse response2 = client.execute(request2);
        for (int attributeId : ATTRIBUTE_IDS) {
            Mapper<?> mapper = Mapping.getMapping(attributeId);
            assertFalse("Attribute " + mapper.getDBColumnName() + " should not be set in GET.", mapper.isSet(toTest));
            Object value = response2.getValue(0, attributeId);
            assertNull("Attribute " + mapper.getDBColumnName() + " should not be set in LIST.", value);
        }
    }
}
