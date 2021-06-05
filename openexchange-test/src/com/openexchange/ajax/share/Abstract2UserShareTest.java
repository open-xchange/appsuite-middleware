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

package com.openexchange.ajax.share;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiClient;

/**
 * {@link Abstract2UserShareTest} extends {@link ShareTest} with a second user
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v8.0.0
 */
public class Abstract2UserShareTest extends ShareTest {

    protected AJAXClient client1;
    protected AJAXClient client2;
    protected ApiClient apiClient1;
    protected ApiClient apiClient2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        apiClient1 = getApiClient();
        apiClient2 = testUser2.getApiClient();
        client1 = getClient();
        client2 = testUser2.getAjaxClient();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().withContexts(2).withUserPerContext(2).build();
    }
}
