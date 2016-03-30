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

package com.openexchange.ajax.find.tasks;

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
import com.openexchange.ajax.framework.AJAXClient.User;
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
import com.openexchange.test.TaskTestManager;


/**
 * {@link FindTasksQueryTests}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindTasksQueryTests extends AbstractFindTasksTest {

    /**
     * Initializes a new {@link FindTasksQueryTests}.
     */
    public FindTasksQueryTests(String name) {
        super(name);
    }

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
        assertResults(30, Collections.<ActiveFacet>emptyList(), -1, 30);
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
        assertResults(5, Collections.<ActiveFacet>emptyList(), 5, 10);
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
        TaskTestManager manager = new TaskTestManager(client);
        try {
            String t1 = randomUID();
            String t2 = randomUID();
            String t3 = randomUID();
            Task task = manager.insertTaskOnServer(manager.newTask(t1 + " " + t2 + " " + t3));

            SimpleFacet globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.TASKS, t1 + " " + t3));
            List<PropDocument> documents = query(client, Collections.singletonList(createActiveFacet(globalFacet)));
            assertTrue("no task found", 0 < documents.size());
            assertNotNull("task not found", findByProperty(documents, "title", task.getTitle()));

            globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.TASKS, "\"" + t1 + " " + t2 + "\""));
            documents = query(client, Collections.singletonList(createActiveFacet(globalFacet)));
            assertTrue("no task found", 0 < documents.size());
            assertNotNull("task not found", findByProperty(documents, "title", task.getTitle()));

            globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.TASKS, "\"" + t1 + " " + t3 + "\""));
            documents = query(client, Collections.singletonList(createActiveFacet(globalFacet)));
            assertTrue("task found", 0 == documents.size());
        } finally {
            manager.cleanUp();
        }
    }

    @Test
    public void testFolderTypeFacet() throws Exception {
        AJAXClient client2 = new AJAXClient(User.User2);
        TaskTestManager manager = new TaskTestManager(client);
        try {
            FolderType[] typesInOrder = new FolderType[] { FolderType.PRIVATE, FolderType.PUBLIC, FolderType.SHARED };
            AJAXClient[] clients = new AJAXClient[] { client, client, client2 };
            FolderObject[] folders = new FolderObject[3];
            folders[0] = folderManager.insertFolderOnServer(folderManager.generatePrivateFolder(
                randomUID(),
                FolderObject.TASK,
                client.getValues().getPrivateTaskFolder(),
                client.getValues().getUserId()));
            folders[1] = folderManager.insertFolderOnServer(folderManager.generatePublicFolder(
                randomUID(),
                FolderObject.TASK,
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                client.getValues().getUserId()));
            folders[2] = folderManager.insertFolderOnServer(folderManager.generateSharedFolder(
                randomUID(),
                FolderObject.TASK,
                client.getValues().getPrivateTaskFolder(),
                client.getValues().getUserId(),
                client2.getValues().getUserId()));

            Task[] tasks = new Task[3];
            tasks[0] = manager.insertTaskOnServer(manager.newTask(randomUID(), folders[0].getObjectID()));
            tasks[1] = manager.insertTaskOnServer(manager.newTask(randomUID(), folders[1].getObjectID()));
            tasks[2] = manager.insertTaskOnServer(manager.newTask(randomUID(), folders[2].getObjectID()));

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
        } finally {
            manager.cleanUp();
            client2.logout();
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
