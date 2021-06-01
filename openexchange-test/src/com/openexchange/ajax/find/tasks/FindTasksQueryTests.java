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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.exception.OXException;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.ExclusiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.find.tasks.TasksFacetType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link FindTasksQueryTests}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindTasksQueryTests extends AbstractFindTasksTest {

    /**
     * Test with simple query with no filters
     * Should find 30 tasks.
     *
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     *
     * @see {@link FindTasksTestEnvironment.createAndInsertTasks}
     */
    @Test
    public void testWithSimpleQuery() throws OXException, IOException, JSONException {
        assertResults(30, Collections.<ActiveFacet> emptyList(), -1, 30);
    }

    /**
     * Test pagination
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testPagination() throws OXException, IOException, JSONException {
        assertResults(5, Collections.<ActiveFacet> emptyList(), 5, 10);
    }

    /**
     * Test query attachment name
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testQueryAttachmentName() throws OXException, IOException, JSONException {
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(new ActiveFacet(TasksFacetType.TASK_ATTACHMENT_NAME, "attachment", new Filter(Collections.singletonList("attachment"), "cool")));
        assertResults(5, facets);
    }

    @Test
    public void testTokenizedQuery() throws Exception {
        String t1 = randomUID();
        String t2 = randomUID();
        String t3 = randomUID();
        Task task = ttm.insertTaskOnServer(ttm.newTask(t1 + " " + t2 + " " + t3));

        SimpleFacet globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.TASKS, t1 + " " + t3));
        List<PropDocument> documents = query(getClient(), Collections.singletonList(createActiveFacet(globalFacet)));
        assertTrue("no task found", 0 < documents.size());
        assertNotNull("task not found", findByProperty(documents, "title", task.getTitle()));

        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.TASKS, "\"" + t1 + " " + t2 + "\""));
        documents = query(getClient(), Collections.singletonList(createActiveFacet(globalFacet)));
        assertTrue("no task found", 0 < documents.size());
        assertNotNull("task not found", findByProperty(documents, "title", task.getTitle()));

        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.TASKS, "\"" + t1 + " " + t3 + "\""));
        documents = query(getClient(), Collections.singletonList(createActiveFacet(globalFacet)));
        assertTrue("task found", 0 == documents.size());
    }

    @Test
    public void testFolderTypeFacet() throws Exception {
        FolderType[] typesInOrder = new FolderType[] { FolderType.PRIVATE, FolderType.PUBLIC, FolderType.SHARED };
        AJAXClient[] clients = new AJAXClient[] { getClient(), getClient(), client2 };
        FolderObject[] folders = new FolderObject[3];
        folders[0] = ftm.insertFolderOnServer(ftm.generatePrivateFolder(randomUID(), FolderObject.TASK, getClient().getValues().getPrivateTaskFolder(), getClient().getValues().getUserId()));
        folders[1] = ftm.insertFolderOnServer(ftm.generatePublicFolder(randomUID(), FolderObject.TASK, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, getClient().getValues().getUserId()));
        folders[2] = ftm.insertFolderOnServer(ftm.generateSharedFolder(randomUID(), FolderObject.TASK, getClient().getValues().getPrivateTaskFolder(), getClient().getValues().getUserId(), client2.getValues().getUserId()));

        Task[] tasks = new Task[3];
        tasks[0] = ttm.insertTaskOnServer(ttm.newTask(randomUID(), folders[0].getObjectID()));
        tasks[1] = ttm.insertTaskOnServer(ttm.newTask(randomUID(), folders[1].getObjectID()));
        tasks[2] = ttm.insertTaskOnServer(ttm.newTask(randomUID(), folders[2].getObjectID()));

        for (int i = 0; i < 3; i++) {
            FolderType folderType = typesInOrder[i];
            List<Facet> facets = autocomplete(clients[i], "");
            ExclusiveFacet folderTypeFacet = (ExclusiveFacet) findByType(CommonFacetType.FOLDER_TYPE, facets);
            FacetValue typeValue = findByValueId(folderType.getIdentifier(), folderTypeFacet);
            List<PropDocument> docs = query(clients[i], Collections.singletonList(createActiveFacet(folderTypeFacet, typeValue)));
            PropDocument[] foundDocs = new PropDocument[3];
            for (PropDocument doc : docs) {
                Map<String, Object> props = doc.getProps();
                if (tasks[0].getTitle().equals(props.get("title"))) {
                    foundDocs[0] = doc;
                    continue;
                } else if (tasks[1].getTitle().equals(props.get("title"))) {
                    foundDocs[1] = doc;
                    continue;
                } else if (tasks[2].getTitle().equals(props.get("title"))) {
                    foundDocs[2] = doc;
                    continue;
                }
            }

            switch (folderType) {
                case PRIVATE:
                    assertNotNull("Private task not found", foundDocs[0]);
                    assertNull("Public task found but should not", foundDocs[1]);
                    assertNotNull("Shared task not found", foundDocs[2]);
                    break;

                case PUBLIC:
                    assertNull("Private task found but should not", foundDocs[0]);
                    assertNotNull("Public task not found", foundDocs[1]);
                    assertNull("Shared task found but should not", foundDocs[2]);
                    break;

                case SHARED:
                    assertNull("Private task found but should not", foundDocs[0]);
                    assertNull("Public task found but should not", foundDocs[1]);
                    assertNotNull("Shared task not found", foundDocs[2]);
                    break;
            }
        }
    }

    protected List<Facet> autocomplete(AJAXClient client, String prefix) throws Exception {
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.TASKS.getIdentifier());
        AutocompleteResponse autocompleteResponse = client.execute(autocompleteRequest);
        return autocompleteResponse.getFacets();
    }

    protected List<PropDocument> query(AJAXClient client, List<ActiveFacet> facets) throws Exception {
        QueryRequest queryRequest = new QueryRequest(0, Integer.MAX_VALUE, facets, Module.TASKS.getIdentifier());
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }
}
