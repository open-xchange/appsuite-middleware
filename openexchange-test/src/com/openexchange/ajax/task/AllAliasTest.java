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
import static org.junit.Assert.fail;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link AllAliasTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AllAliasTest extends AbstractTaskTest {

    private AJAXClient client;

    private Task task;

    /**
     * Initializes a new {@link AllAliasTest}.
     *
     * @param name
     */
    public AllAliasTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    @Test
    public void testAll() throws Throwable {
        task = new Task();
        task.setTitle("Task TestAllAlias");
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertRequest insertRequest = new InsertRequest(task, getTimeZone());
        final InsertResponse insertResponse = client.execute(insertRequest);
        insertResponse.fillObject(task);

        final AllRequest aliasRequest = new AllRequest(client.getValues().getPrivateTaskFolder(), "all", 0, Order.NO_ORDER);
        final CommonAllResponse aliasResponse = client.execute(aliasRequest);
        final Object[][] tasksAlias = aliasResponse.getArray();

        final AllRequest request = new AllRequest(client.getValues().getPrivateTaskFolder(), new int[] { 20, 1, 2, 5, 4 }, 0, Order.NO_ORDER);
        final CommonAllResponse response = client.execute(request);
        final Object[][] tasks = response.getArray();

        assertEquals("Arrays' sizes are not equal.", tasksAlias.length, tasks.length);
        for (int i = 0; i < tasksAlias.length; i++) {
            final Object[] o1 = tasksAlias[i];
            final Object[] o2 = tasks[i];
            assertEquals("Objects' sizes are not equal.", o1.length, o2.length);
            for (int j = 0; j < o1.length; j++) {
                if ((o1[j] != null || o2[j] != null)) {
                    if (!(o1[j] instanceof JSONArray) && !(o2[j] instanceof JSONArray)) {
                        assertEquals("Array[" + i + "][" + j + "] not equal.", o1[j], o2[j]);
                    } else {
                        compareArrays((JSONArray) o1[j], (JSONArray) o2[j]);
                    }
                }
            }
        }
    }

    private void compareArrays(final JSONArray o1, final JSONArray o2) throws Exception {
        if (o1.length() != o2.length()) {
            fail("Arrays' sizes are not equal.");
        }
        for (int i = 0; i < o1.length(); i++) {
            if ((o1.get(i) != null || o2.get(i) != null)) {
                if (!(o1.get(i) instanceof JSONArray) && !(o2.get(i) instanceof JSONArray)) {
                    assertEquals("Array[" + i + "] not equal.", o1.get(i).toString(), o2.get(i).toString());
                } else {
                    compareArrays((JSONArray) o1.get(i), (JSONArray) o2.get(i));
                }
            }
        }
    }

}
