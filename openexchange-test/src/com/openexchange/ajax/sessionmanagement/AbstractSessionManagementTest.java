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

package com.openexchange.ajax.sessionmanagement;

import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.AllSessionsResponse;
import com.openexchange.testing.httpclient.modules.SessionmanagementApi;

/**
 * {@link AbstractSessionManagementTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class AbstractSessionManagementTest extends AbstractAPIClientSession {

    protected ApiClient apiClient2;

    private SessionmanagementApi api;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Remove all other sessions to make sure tests run independently
        api = new SessionmanagementApi(getApiClient());
        AllSessionsResponse all = api.all();
        int size = all.getData().size();
        api.clear();
        all = api.all();
        System.out.println("Cleared " + all.getData().size() + " out of " + size + " sessions for the " + this.getClass().getSimpleName() + " test.");

        // For the same user
        apiClient2 = testUser.generateApiClient();
    }

    protected SessionmanagementApi getApi() {
        return api;
    }
}
