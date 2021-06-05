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

package com.openexchange.ajax.user;

import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;

public class GetTest extends Abstrac2UserAJAXSession {

    @Test
    public void testGet() throws Exception {
        final GetRequest getRequest = new GetRequest(client2.getValues().getUserId(), getClient().getValues().getTimeZone());
        final GetResponse getResponse = Executor.execute(getClient(), getRequest);

        final JSONObject user = (JSONObject) getResponse.getData();

        assertTrue("No ID", user.hasAndNotNull("id"));
        assertTrue("Wrong ID", user.getInt("id") == client2.getValues().getUserId());

        assertTrue("No aliases. JSON: " + user, user.hasAndNotNull("aliases"));
    }
}
