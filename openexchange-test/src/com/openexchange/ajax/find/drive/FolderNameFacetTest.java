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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.find.drive;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    private ApiClient apiClient2;

    @Test
    public void testSearchForFolderName() throws Exception {
        FindQueryBody body = new FindQueryBody();
        addBasicFacets(body);
        FindQueryResponse response = findApi.doQuery(apiClient.getSession(), Module.FILES.getName(), body, COLUMNS, null);
        assertTrue(Strings.isEmpty(response.getError()));
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
            FindQueryResponse response = findApi.doQuery(apiClient.getSession(), Module.FILES.getName(), body, COLUMNS, null);
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
            FindQueryResponse response = findApi.doQuery(apiClient.getSession(), Module.FILES.getName(), body, COLUMNS, null);
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
            FindQueryResponse response = findApi.doQuery(apiClient.getSession(), Module.FILES.getName(), body, COLUMNS, null);
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
        FindQueryResponse response = findApi.doQuery(apiClient.getSession(), Module.FILES.getName(), body, COLUMNS, null);
        assertTrue(Strings.isEmpty(response.getError()));
        FindQueryResponseData data = response.getData();
        assertEquals(2, i(data.getSize()));

        // Search as another user, result in "Public Files" expected
        FindApi findApi2 = new FindApi(apiClient2);
        response = findApi2.doQuery(apiClient2.getSession(), Module.FILES.getName(), body, COLUMNS, null);
        assertTrue(Strings.isEmpty(response.getError()));
        data = response.getData();
        assertEquals(1, i(data.getSize()));

        // Search in "My Files" only
        FindActiveFacet facet = new FindActiveFacet();
        facet.facet("folder");
        facet.setFilter(null);
        facet.setValue(getPrivateInfostoreFolder(apiClient));
        body.addFacetsItem(facet);
        response = findApi.doQuery(apiClient.getSession(), Module.FILES.getName(), body, COLUMNS, null);
        assertTrue(Strings.isEmpty(response.getError()));
        data = response.getData();
        assertEquals(1, i(data.getSize()));

        // Search as another user, no results expected
        response = findApi2.doQuery(apiClient2.getSession(), Module.FILES.getName(), body, COLUMNS, null);
        assertTrue(Strings.isEmpty(response.getError()));
        data = response.getData();
        assertEquals(0, i(data.getSize()));

        // Search in "Public Files" only
        body = new FindQueryBody();
        addBasicFacets(body);
        facet = new FindActiveFacet();
        facet.facet("folder");
        facet.setFilter(null);
        facet.setValue(String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID));
        body.addFacetsItem(facet);
        response = findApi.doQuery(apiClient.getSession(), Module.FILES.getName(), body, COLUMNS, null);
        assertTrue(Strings.isEmpty(response.getError()));
        data = response.getData();
        assertEquals(1, i(data.getSize()));

        // Search as another user, 1 result expected
        response = findApi2.doQuery(apiClient2.getSession(), Module.FILES.getName(), body, COLUMNS, null);
        assertTrue(Strings.isEmpty(response.getError()));
        data = response.getData();
        assertEquals(1, i(data.getSize()));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        apiClient2 = generateApiClient(testUser2);
        findApi = new FindApi(apiClient);
        foldersApi = new FoldersApi(apiClient);
        createNewFolder(false);
    }

    @Override
    public void tearDown() throws Exception {
        foldersApi.deleteFolders(apiClient.getSession(), createdFolders, "0", L(System.currentTimeMillis()), null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null, Boolean.FALSE);
        super.tearDown();
    }

    private String createNewFolder(boolean isPublic) throws Exception {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary("FolderNameFacetTest_" + UUID.randomUUID().toString());
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        List<FolderPermission> perm = new ArrayList<FolderPermission>();
        FolderPermission p = createPermissionFor(apiClient.getUserId(), BITS_ADMIN, Boolean.FALSE);
        perm.add(p);
        if (isPublic) {
            FolderPermission fp = createPermissionFor(I(0), BITS_VIEWER, Boolean.TRUE);
            perm.add(fp);
        }
        folder.setPermissions(perm);
        body.setFolder(folder);
        FolderUpdateResponse response = foldersApi.createFolder(isPublic ? String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID) : getPrivateInfostoreFolder(apiClient),
            apiClient.getSession(), body, "0", null, null, null);
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
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath(), apiClient.getSession());
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
