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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.FindAutoCompleteBody;
import com.openexchange.testing.httpclient.models.FindAutoCompleteData;
import com.openexchange.testing.httpclient.models.FindAutoCompleteResponse;
import com.openexchange.testing.httpclient.models.FindFacetData;
import com.openexchange.testing.httpclient.models.FindFacetValue;
import com.openexchange.testing.httpclient.models.FindOptionsData;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.modules.FindApi;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link AutoCompleteShowDepartmentsTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AutoCompleteShowDepartmentsTest extends AbstractAPIClientSession {

    private static final int LIMIT = 6;
    private static final String CONTACTS_MODULE = "contacts";

    private ApiClient apiClient2;
    private UserApi userApi;
    private UserApi userApi2;

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

        // Login clients
        apiClient.login(testUser.getLogin(), testUser.getPassword());

        apiClient2 = generateClient(testUser2);
        apiClient2.login(testUser2.getLogin(), testUser2.getPassword());
        rememberClient(apiClient2);

        // Prepare users
        userApi = new UserApi(apiClient);
        userApi.updateUser(apiClient.getSession(), Integer.toString(apiClient.getUserId()), System.currentTimeMillis(), createUpdateBody("Department A"));

        userApi2 = new UserApi(apiClient2);
        userApi2.updateUser(apiClient2.getSession(), Integer.toString(apiClient2.getUserId()), System.currentTimeMillis(), createUpdateBody("Department B"));
    }

    /**
     * Resets the users
     */
    @Override
    public void tearDown() throws Exception {
        // Reset users
        userApi.updateUser(apiClient.getSession(), Integer.toString(apiClient.getUserId()), System.currentTimeMillis(), createUpdateBody(""));
        userApi2.updateUser(apiClient2.getSession(), Integer.toString(apiClient2.getUserId()), System.currentTimeMillis(), createUpdateBody(""));

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
        body.setPrefix(apiClient.getUser());
        body.setOptions(options);

        FindAutoCompleteResponse response = findApi.doAutoComplete(apiClient.getSession(), CONTACTS_MODULE, body, LIMIT);
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
            assertTrue("The item is missing the 'department' part from the display name", itemName.contains("Department"));
        }
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
}
