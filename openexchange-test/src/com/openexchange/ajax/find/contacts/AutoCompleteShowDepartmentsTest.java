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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ajax.find.contacts;

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
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AutoCompleteShowDepartmentsTest.class);

    private static final int AMOUNT_OF_TEST_USERS = 3;
    private static final int RESULTS_LIMIT = 6;
    private static final String CONTACTS_JSLOB = "io.ox/contacts";
    private static final String SHOW_DEPARTMENT_JSLOB = "showDepartment";
    private static final String CONTACTS_MODULE = "contacts";

    private Map<String, TestUser> testUsers;
    private Map<String, ApiClient> clients;
    private Set<String> randomUsers;

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

        testUsers = new HashMap<>();
        clients = new HashMap<>();
        randomUsers = new HashSet<>();

        for (int i = 0; i < AMOUNT_OF_TEST_USERS; i++) {
            TestUser testUser = testContext.acquireUser();
            if (testUser == null) {
                LOG.debug("Could not acquire user");
                continue;
            }
            LOG.info("Acquired user '{}'", testUser);

            ApiClient client = generateApiClient(testUser);
            rememberClient(client);
            testUsers.put(testUser.getUser(), testUser);
            clients.put(testUser.getUser(), client);
        }

        // Prepare users
        prepareUser(pickRandomUser(), "Department A");
        prepareUser(pickRandomUser(), "Department B");
        CONFIG.put("com.openexchange.contact.showDepartments", Boolean.TRUE.toString());
        super.setUpConfiguration();
    }

    /**
     * Picks a random user from the registry
     *
     * @return The random {@link TestUser}
     */
    private TestUser pickRandomUser() {
        String[] keys = testUsers.keySet().toArray(new String[AMOUNT_OF_TEST_USERS]);
        String key;
        do {
            key = keys[(int) (Math.random() * AMOUNT_OF_TEST_USERS)];
        } while (randomUsers.contains(key));

        randomUsers.add(key);
        LOG.info("Picked random user '{}'", key);
        return testUsers.get(key);
    }

    /**
     * Prepares the specified {@link TestUser}
     *
     * @param testUser The {@link TestUser} to prepare
     * @param department The department to set
     */
    private void prepareUser(TestUser testUser, String department) throws ApiException {
        ApiClient client = clients.get(testUser.getUser());
        UserApi userApi = new UserApi(client);
        CommonResponse response = userApi.updateUser(client.getSession(), Integer.toString(client.getUserId()), System.currentTimeMillis(), createUpdateBody(department));
        assertNull(response.getErrorDesc(), response.getError());
    }

    /**
     * Resets the users
     */
    @Override
    public void tearDown() throws Exception {
        // Reset users
        for (String key : randomUsers) {
            ApiClient client = clients.get(key);
            UserApi userApi = new UserApi(client);
            userApi.updateUser(client.getSession(), Integer.toString(client.getUserId()), System.currentTimeMillis(), createUpdateBody(""));
        }
        super.tearDown();
    }

    /**
     * Performs a simple auto-complete request and asserts the results.
     * On server side, the property <code>com.openexchange.contact.showDepartments</code>
     * must be set to <code>true</code>.
     */
    @Test
    public void testAutoCompleteShowDepartment() throws Exception {
        FindApi findApi = new FindApi(apiClient);

        FindOptionsData options = new FindOptionsData();
        options.setAdmin(false);
        options.setTimezone("UTC");

        FindAutoCompleteBody body = new FindAutoCompleteBody();
        body.setPrefix("*"); // Check all users
        body.setOptions(options);

        FindAutoCompleteResponse response = findApi.doAutoComplete(apiClient.getSession(), CONTACTS_MODULE, body, RESULTS_LIMIT);
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
        JSlobApi slobApi = new JSlobApi(apiClient);

        JSlobsResponse response = slobApi.getJSlobList(apiClient.getSession(), Collections.singletonList(CONTACTS_JSLOB), null);
        List<JSlobData> data = response.getData();
        assertEquals("Expected 1 jslob data object for '" + CONTACTS_JSLOB + "'", 1, data.size());

        Object obj = data.get(0).getTree();
        assertTrue("The jslob's tree is not a Map", obj instanceof Map);

        JSONObject tree = (JSONObject) JSONCoercion.coerceToJSON(obj);
        assertTrue("The '" + SHOW_DEPARTMENT_JSLOB + "' slob is missing from the tree", tree.hasAndNotNull(SHOW_DEPARTMENT_JSLOB));
        assertTrue("The '" + SHOW_DEPARTMENT_JSLOB + "' slob is set to 'false'.", Boolean.valueOf(tree.get(SHOW_DEPARTMENT_JSLOB).toString()));
    }

    /**
     * Checks if the specified item name is contained within the randomUsers set
     *
     * @param itemName The item name to check
     * @return <code>true</code> if it is contained, <code>false</code> otherwise
     */
    private boolean isRandomUser(String itemName) {
        for (String user : randomUsers) {
            if (itemName.toLowerCase().contains(user)) {
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
