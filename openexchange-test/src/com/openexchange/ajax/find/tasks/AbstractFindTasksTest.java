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

package com.openexchange.ajax.find.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link AbstractFindTasksTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(BlockJUnit4ClassRunner.class)
public abstract class AbstractFindTasksTest extends FindTasksTestEnvironment {

    /**
     * Fetch the results from the QueryResponse
     *
     * @param qr the QueryResponse
     * @return the results as a JSONArray, or null if the respond does not contain a results payload
     */
    protected static final JSONArray getResults(QueryResponse qr) {
        JSONArray ret = null;
        if (qr.getData() != null && qr.getData() instanceof JSONObject) {
            ret = ((JSONObject) qr.getData()).optJSONArray("results");
        }
        return ret;
    }

    /**
     * Helper method to assert the query response (no paging)
     *
     * @param expectedResultCount
     * @param f
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    protected final void assertResults(int expectedResultCount, List<ActiveFacet> f) throws OXException, IOException, JSONException {
        assertResults(expectedResultCount, f, -1, -1);
    }

    /**
     * Helper method to assert the query response (with paging)
     *
     * @param expectedResultCount
     * @param f
     * @param start
     * @param size
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    protected final void assertResults(int expectedResultCount, List<ActiveFacet> f, int start, int size) throws OXException, IOException, JSONException {
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(createGlobalFacet());
        facets.addAll(f);
        final QueryResponse queryResponse = getClient().execute(new QueryRequest(start, size, facets, "tasks"));
        assertNotNull(queryResponse);
        JSONArray results = getResults(queryResponse);
        int actualResultCount = results.asList().size();
        assertEquals(expectedResultCount, actualResultCount);

        for (Object o : results.asList()) {
            Map<String, Object> m = (Map<String, Object>) o;
            Integer taskId = (Integer) m.get("id");
            assertNotNull("Task id should not be null", taskId);
            Task t = getTask(taskId.intValue());
            assertNotNull("Expected object not found", t);
            assertEquals("Not the same", t.getTitle(), m.get("title"));
        }
    }
}
