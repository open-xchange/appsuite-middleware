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

package com.openexchange.ajax.find.contacts;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.FindActiveFacet;
import com.openexchange.testing.httpclient.models.FindAutoCompleteBody;
import com.openexchange.testing.httpclient.models.FindAutoCompleteData;
import com.openexchange.testing.httpclient.models.FindAutoCompleteResponse;
import com.openexchange.testing.httpclient.models.FindFacetData;
import com.openexchange.testing.httpclient.models.FindFacetValue;
import com.openexchange.testing.httpclient.models.FindOptionsData;
import com.openexchange.testing.httpclient.models.JSlobData;
import com.openexchange.testing.httpclient.models.JSlobsResponse;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.modules.FindApi;
import com.openexchange.testing.httpclient.modules.JSlobApi;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link AutoCompleteShowDepartmentsTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AutoCompleteShowDepartmentsTest extends AbstractConfigAwareAPIClientSession {

    private static final Integer RESULTS_LIMIT = I(6);
    private static final String CONTACTS_JSLOB = "io.ox/contacts";
    private static final String SHOW_DEPARTMENT_JSLOB = "showDepartment";
    private static final String CONTACTS_MODULE = "contacts";

    private Set<TestUser> randomUsers;

    /**
     * Initialises a new {@link AutoCompleteShowDepartmentsTest}.
     */
    public AutoCompleteShowDepartmentsTest() {
        super();
    }

    /**
     * Set up users
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        randomUsers = new HashSet<>();

        // Prepare users
        TestUser randomUser = testContext.getRandomUser();
        randomUsers.add(randomUser);
        prepareUser(randomUser, "Department A");
        randomUser = testContext.getRandomUser();
        randomUsers.add(randomUser);
        prepareUser(randomUser, "Department B");
        CONFIG.put("com.openexchange.contact.showDepartments", Boolean.TRUE.toString());
        super.setUpConfiguration();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(5).build();
    }

    /**
     * Prepares the specified {@link TestUser}
     *
     * @param testUser The {@link TestUser} to prepare
     * @param department The department to set
     */
    private void prepareUser(TestUser testUser, String department) throws ApiException {
        ApiClient client = testUser.getApiClient();
        UserApi userApi = new UserApi(client);
        CommonResponse response = userApi.updateUser(client.getUserId().toString(), L(System.currentTimeMillis()), createUpdateBody(department));
        assertNull(response.getErrorDesc(), response.getError());
    }

    /**
     * Performs a simple auto-complete request and asserts the results.
     * On server side, the property <code>com.openexchange.contact.showDepartments</code>
     * must be set to <code>true</code>.
     */
    @Test
    public void testAutoCompleteShowDepartment() throws Exception {
        FindApi findApi = new FindApi(getApiClient());

        FindOptionsData options = new FindOptionsData();
        options.setAdmin(Boolean.FALSE);
        options.setTimezone("UTC");

        FindAutoCompleteBody body = new FindAutoCompleteBody();
        body.setPrefix("*"); // Check all users
        body.setOptions(options);

        // only check users in the global address book
        FindActiveFacet facet = new FindActiveFacet();
        facet.facet("folder").value(FolderStorage.GLOBAL_ADDRESS_BOOK_ID);
        body.addFacetsItem(facet);

        FindAutoCompleteResponse response = findApi.doAutoComplete(CONTACTS_MODULE, body, RESULTS_LIMIT);
        FindAutoCompleteData data = response.getData();
        List<FindFacetValue> values = null;
        for (FindFacetData findFacetData : data.getFacets()) {
            if (findFacetData.getId().equals("contact")) {
                values = findFacetData.getValues();
                break;
            }
        }
        assertNotNull("No contact results were found.", values);

        for (FindFacetValue value : values) {
            String itemName = value.getItem().getName();
            if (isRandomUser(itemName)) {
                assertTrue("The item '" + itemName + "' is missing the 'department' part from the display name", itemName.contains("Department"));
            } else {
                assertFalse("The item '" + itemName + "' contains the 'department' part in the display name", itemName.contains("Department"));
            }
        }
    }

    /**
     * Checks if the 'showDepartment' jslob is correctly registered
     */
    @Test
    public void testJslob() throws ApiException, JSONException {
        JSlobApi slobApi = new JSlobApi(getApiClient());

        JSlobsResponse response = slobApi.getJSlobList(Collections.singletonList(CONTACTS_JSLOB), null);
        List<JSlobData> data = response.getData();
        assertEquals("Expected 1 jslob data object for '" + CONTACTS_JSLOB + "'", 1, data.size());

        Object obj = data.get(0).getTree();
        assertTrue("The jslob's tree is not a Map", obj instanceof Map);

        JSONObject tree = (JSONObject) JSONCoercion.coerceToJSON(obj);
        assertTrue("The '" + SHOW_DEPARTMENT_JSLOB + "' slob is missing from the tree", tree.hasAndNotNull(SHOW_DEPARTMENT_JSLOB));
        assertTrue("The '" + SHOW_DEPARTMENT_JSLOB + "' slob is set to 'false'.", Boolean.valueOf(tree.get(SHOW_DEPARTMENT_JSLOB).toString()).booleanValue());
    }

    /**
     * Checks if the specified item name is contained within the randomUsers set
     *
     * @param itemName The item name to check
     * @return <code>true</code> if it is contained, <code>false</code> otherwise
     */
    private boolean isRandomUser(String itemName) {
        for (TestUser user : randomUsers) {
            if (itemName.toLowerCase().contains(user.getUser().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the update body with the specified department string
     *
     * @param department The department string
     * @return The {@link UserData}
     */
    private UserData createUpdateBody(String department) {
        UserData updateBody = new UserData();
        updateBody.setDepartment(department);

        // The swagger client sets the following to empty lists
        // and results in a transformation of a user to a distribution list.
        //
        // Therefore we set those to 'null' to avoid sending over the empty
        // lists via the HTTP API.
        updateBody.setDistributionList(null);
        updateBody.setGroups(null);
        updateBody.setAliases(null);

        return updateBody;
    }

    private static final Map<String, String> CONFIG = new HashMap<String, String>();

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }

    @Override
    protected String getScope() {
        return "user";
    }
}
