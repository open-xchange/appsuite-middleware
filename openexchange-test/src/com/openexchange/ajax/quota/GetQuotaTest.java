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

package com.openexchange.ajax.quota;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * {@link GetQuotaTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GetQuotaTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link GetQuotaTest}.
     *
     * @param name The test name
     */
    public GetQuotaTest() {
        super();
    }

    @Test
    public void testGetQuota() throws Exception {
        /*
         * get quota from all available modules
         */
        GetQuotaRequest request = new GetQuotaRequest(null, null);
        GetQuotaResponse response = getClient().execute(request);
        JSONObject jsonModules = (JSONObject) response.getData();
        assertNotNull("No response data", jsonModules);
        Set<String> modules = jsonModules.keySet();
        if (null != modules && 0 < modules.size()) {
            /*
             * get quotas from one specific random module
             */
            Random random = new Random();
            String randomModule = modules.toArray(new String[modules.size()])[random.nextInt(modules.size())];
            JSONObject jsonModule = jsonModules.getJSONObject(randomModule);
            assertTrue("No display_name found", jsonModule.hasAndNotNull("display_name"));
            assertTrue("No accounts array found", jsonModule.hasAndNotNull("accounts"));
            request = new GetQuotaRequest(randomModule, null);
            response = getClient().execute(request);
            JSONArray jsonAccounts = (JSONArray) response.getData();
            assertNotNull("No response data", jsonAccounts);
            if (0 < jsonAccounts.length()) {
                /*
                 * get quota from one specific random account
                 */
                JSONObject randomAccount = jsonAccounts.getJSONObject(random.nextInt(jsonAccounts.length()));
                assertTrue("No account_id found", randomAccount.hasAndNotNull("account_id"));
                assertTrue("No account_name found", randomAccount.hasAndNotNull("account_name"));
                assertTrue("No quota or countquota found", randomAccount.hasAndNotNull("quota") || randomAccount.hasAndNotNull("countquota"));
                request = new GetQuotaRequest(randomModule, randomAccount.getString("account_id"));
                response = getClient().execute(request);
                JSONObject jsonAccount = (JSONObject) response.getData();
                assertTrue("No account_id found", jsonAccount.hasAndNotNull("account_id"));
                assertTrue("No account_name found", jsonAccount.hasAndNotNull("account_name"));
                assertTrue("No quota or countquota found", jsonAccount.hasAndNotNull("quota") || randomAccount.hasAndNotNull("countquota"));
            }
        }
    }

}
