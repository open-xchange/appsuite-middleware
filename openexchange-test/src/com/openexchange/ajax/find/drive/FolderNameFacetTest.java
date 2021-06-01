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

package com.openexchange.ajax.find.drive;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.junit.Assert;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.FindActiveFacet;
import com.openexchange.testing.httpclient.models.FindActiveFacetFilter;
import com.openexchange.testing.httpclient.models.FindQueryBody;
import com.openexchange.testing.httpclient.models.FindQueryResponse;
import com.openexchange.testing.httpclient.models.FindQueryResponseData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.FindApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * {@link FolderNameFacetTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class FolderNameFacetTest extends AbstractAPIClientSession {

    private final Integer BITS_ADMIN = new Integer(403710016);
    private final Integer BITS_VIEWER = new Integer(257);
    private final String COLUMNS = "1,3,5,20,23,700,702,703,704,705,707";
    private final List<String> createdFolders = new ArrayList<String>();

    private FindApi findApi;
    private FoldersApi foldersApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        findApi = new FindApi(getApiClient());
        foldersApi = new FoldersApi(getApiClient());
        createNewFolder(false);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(2).build();
    }

    @Test
    public void testSearchForFolderName() throws Exception {
        FindQueryBody body = new FindQueryBody();
        addBasicFacets(body);
        FindQueryResponse response = findApi.doQuery(Module.FILES.getName(), body, COLUMNS, null);
        checkResponseForErrors(response);
        FindQueryResponseData data = response.getData();
        assertEquals(1, i(data.getSize()));
    }

    @Test
    public void testConflictingFacets() throws Exception {
        {
            //File type facet
            FindActiveFacet facet = new FindActiveFacet();
            facet.setFacet("file_type");
            facet.setValue("audio");
            FindActiveFacetFilter filter = new FindActiveFacetFilter();
            filter.addFieldsItem("file_mimetype");
            filter.addQueriesItem("audio");
            facet.setFilter(filter);
            FindQueryBody body = new FindQueryBody();
            addBasicFacets(body);
            body.addFacetsItem(facet);
            FindQueryResponse response = findApi.doQuery(Module.FILES.getName(), body, COLUMNS, null);
            assertFalse(Strings.isEmpty(response.getError()));
            assertEquals("FIND-0016", response.getCode());
        }
        {
            //File size facet
            FindActiveFacet facet = new FindActiveFacet();
            facet.setFacet("file_size");
            facet.setValue("> 1MB");
            FindActiveFacetFilter filter = new FindActiveFacetFilter();
            filter.addFieldsItem("file_size");
            filter.addQueriesItem("> 1MB");
            facet.setFilter(filter);
            FindQueryBody body = new FindQueryBody();
            addBasicFacets(body);
            body.addFacetsItem(facet);
            FindQueryResponse response = findApi.doQuery(Module.FILES.getName(), body, COLUMNS, null);
            assertFalse(Strings.isEmpty(response.getError()));
            assertEquals("FIND-0016", response.getCode());
        }
        {
            //Folder type facet
            FindActiveFacet facet = new FindActiveFacet();
            facet.setFacet("folder_type");
            facet.setValue("private");
            FindActiveFacetFilter filter = new FindActiveFacetFilter();
            filter.addFieldsItem("folder_type");
            filter.addQueriesItem("private");
            facet.setFilter(filter);
            FindQueryBody body = new FindQueryBody();
            addBasicFacets(body);
            body.addFacetsItem(facet);
            FindQueryResponse response = findApi.doQuery(Module.FILES.getName(), body, COLUMNS, null);
            assertFalse(Strings.isEmpty(response.getError()));
            assertEquals("FIND-0016", response.getCode());
        }
    }

    /**
     * Impossible to inject "old" folder for test (for now)... So no testing possible :-(
    @Test
    public void testDateFacet() throws Exception {
        FindQueryBody body = new FindQueryBody();
        addBasicFacets(body);
        // insertOldFolder();

        // Without date facet, 2 folders expected
        FindQueryResponse response = findApi.doQuery(apiClient.getSession(), Module.FILES.getName(), body, COLUMNS, null);
        assertTrue(Strings.isEmpty(response.getError()));
        FindQueryResponseData data = response.getData();
        assertEquals(2, i(data.getSize()));

        // With date facet, show folders newer than one week
        FindActiveFacet facet = new FindActiveFacet();
        facet.setFacet("date");
        facet.setValue("last_week");
        FindActiveFacetFilter filter = new FindActiveFacetFilter();
        filter.addFieldsItem("date");
        filter.addQueriesItem("last_week");
        facet.setFilter(filter);
        body.addFacetsItem(facet);
        response = findApi.doQuery(apiClient.getSession(), Module.FILES.getName(), body, COLUMNS, null);
        assertTrue(Strings.isEmpty(response.getError()));
        data = response.getData();
        assertEquals(1, i(data.getSize()));
    }
    */

    @Test
    public void testSearchInFolder() throws Exception {
        createNewFolder(true);
        FindQueryBody body = new FindQueryBody();
        addBasicFacets(body);

        // Without folder facet, result in "My Files" and "Public Files" expected
        FindQueryResponse response = findApi.doQuery(Module.FILES.getName(), body, COLUMNS, null);
        checkResponseForErrors(response);
        FindQueryResponseData data = response.getData();
        assertEquals(2, i(data.getSize()));

        // Search as another user, result in "Public Files" expected
        FindApi findApi2 = new FindApi(testUser2.getApiClient());
        response = findApi2.doQuery(Module.FILES.getName(), body, COLUMNS, null);
        checkResponseForErrors(response);
        data = response.getData();
        assertEquals(1, i(data.getSize()));

        // Search in "My Files" only
        FindActiveFacet facet = new FindActiveFacet();
        facet.facet("folder");
        facet.setFilter(null);
        facet.setValue(getPrivateInfostoreFolder(getApiClient()));
        body.addFacetsItem(facet);
        response = findApi.doQuery(Module.FILES.getName(), body, COLUMNS, null);
        checkResponseForErrors(response);
        data = response.getData();
        assertEquals(1, i(data.getSize()));

        // Search in "Public Files" only
        body = new FindQueryBody();
        addBasicFacets(body);
        facet = new FindActiveFacet();
        facet.facet("folder");
        facet.setFilter(null);
        facet.setValue(String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID));
        body.addFacetsItem(facet);
        response = findApi.doQuery(Module.FILES.getName(), body, COLUMNS, null);
        checkResponseForErrors(response);
        data = response.getData();
        assertEquals(1, i(data.getSize()));

        // Search as another user, 1 result expected
        response = findApi2.doQuery(Module.FILES.getName(), body, COLUMNS, null);
        checkResponseForErrors(response);
        data = response.getData();
        assertEquals(1, i(data.getSize()));
    }

    private String createNewFolder(boolean isPublic) throws Exception {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary("FolderNameFacetTest_" + UUID.randomUUID().toString());
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        List<FolderPermission> perm = new ArrayList<FolderPermission>();
        FolderPermission p = createPermissionFor(I(testUser.getUserId()), BITS_ADMIN, Boolean.FALSE);
        perm.add(p);
        if (isPublic) {
            FolderPermission fp = createPermissionFor(I(0), BITS_VIEWER, Boolean.TRUE);
            perm.add(fp);
        }
        folder.setPermissions(perm);
        body.setFolder(folder);
        FolderUpdateResponse response = foldersApi.createFolder(isPublic ? String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID) : getPrivateInfostoreFolder(getApiClient()), body, "0", null, null, null);
        String folderId = response.getData();
        createdFolders.add(folderId);
        return folderId;
    }

    private FolderPermission createPermissionFor(Integer entity, Integer bits, Boolean isGroup) {
        FolderPermission p = new FolderPermission();
        p.setEntity(entity);
        p.setGroup(isGroup);
        p.setBits(bits);
        return p;
    }

    private String getPrivateInfostoreFolder(ApiClient apiClient) throws ApiException {
        ConfigApi configApi = new ConfigApi(apiClient);
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath());
        Object data = checkResponse(configNode);
        if (data != null && !data.toString().equalsIgnoreCase("null")) {
            return String.valueOf(data);
        }
        Assert.fail("It seems that the user doesn't support drive.");
        return null;
    }

    private Object checkResponse(ConfigResponse resp) {
        Assert.assertNull(resp.getErrorDesc(), resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    private void checkResponseForErrors(FindQueryResponse response) {
        if (Strings.isNotEmpty(response.getError())) {
            fail(response.getError());
        }
    }

    private void addBasicFacets(FindQueryBody body) {
        // Folder facet
        FindActiveFacet folderFacet = new FindActiveFacet();
        folderFacet.setFacet("folder");
        folderFacet.setFilter(null);
        folderFacet.setValue(null);
        body.addFacetsItem(folderFacet);

        // Folder name facet
        FindActiveFacet folderNameFacet = new FindActiveFacet();
        folderNameFacet.setFacet("folder_name");
        folderNameFacet.setValue("folder_name:NameFacet");
        FindActiveFacetFilter filter = new FindActiveFacetFilter();
        filter.addFieldsItem("folder_name");
        filter.addQueriesItem("NameFacet");
        folderNameFacet.setFilter(filter);
        body.addFacetsItem(folderNameFacet);
    }

}
