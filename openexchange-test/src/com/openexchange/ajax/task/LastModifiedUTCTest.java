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

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.ajax.task.actions.SearchRequest;
import com.openexchange.ajax.task.actions.UpdatesRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class LastModifiedUTCTest extends AbstractTaskTest {

    private static final int[] LAST_MODIFIED_UTC = new int[] { Task.LAST_MODIFIED_UTC, Task.OBJECT_ID, Task.FOLDER_ID };

    private int id;

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    public LastModifiedUTCTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final Task task = new Task();
        task.setTitle("lastModifiedUTC");
        task.setParentFolderID(getPrivateFolder());
        final InsertRequest request = new InsertRequest(task, getTimeZone());
        final InsertResponse response = Executor.execute(getClient(), request);
        id = response.getId();
    }

    public int getId() {
        return id;
    }

    @Test
    public void testAll() throws JSONException, OXException, IOException {
        final AllRequest request = new AllRequest(getPrivateFolder(), LAST_MODIFIED_UTC, -1, null);
        checkListSyleRequest(request);
    }

    @Test
    public void testGet() throws JSONException, OXException, IOException {
        final GetRequest getRequest = new GetRequest(getPrivateFolder(), getId());
        final GetResponse getResponse = Executor.execute(getClient(), getRequest);

        final JSONObject object = (JSONObject) getResponse.getData();

        assertTrue(object.has("last_modified_utc"));

    }

    @Test
    public void testList() throws JSONException, OXException, IOException {
        final ListRequest listRequest = new ListRequest(new int[][] { { getPrivateFolder(), getId() } }, LAST_MODIFIED_UTC);
        checkListSyleRequest(listRequest);
    }

    @Test
    public void testSearch() throws JSONException, OXException, IOException {
        final TaskSearchObject search = new TaskSearchObject();
        search.addFolder(getPrivateFolder());
        search.setPattern("*");
        final SearchRequest searchRequest = new SearchRequest(search, LAST_MODIFIED_UTC, true);
        checkListSyleRequest(searchRequest);
    }

    @Test
    public void testUpdates() throws JSONException, OXException, IOException {
        final UpdatesRequest updatesRequest = new UpdatesRequest(getPrivateFolder(), LAST_MODIFIED_UTC, -1, null, new Date(0));
        checkListSyleRequest(updatesRequest);
    }

    private void checkListSyleRequest(final AJAXRequest<? extends AbstractAJAXResponse> request) throws JSONException, OXException, IOException {
        final AbstractAJAXResponse response = Executor.execute(getClient(), request);
        final JSONArray arr = (JSONArray) response.getData();
        final int size = arr.length();
        assertTrue(size > 0);
        for (int i = 0; i < size; i++) {
            final JSONArray row = arr.getJSONArray(i);
            assertTrue(row.toString(), row.length() == 3);
            assertTrue(row.toString(), row.optLong(0) > 0);
        }
    }
}
