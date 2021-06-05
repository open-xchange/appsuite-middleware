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

package com.openexchange.ajax.group;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.group.actions.AbstractGroupResponse;
import com.openexchange.ajax.group.actions.ListRequest;
import com.openexchange.ajax.group.actions.SearchRequest;
import com.openexchange.ajax.group.actions.SearchResponse;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;

/**
 * {@link GroupResolver}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class GroupResolver {

    private final AJAXClient client;

    public GroupResolver(AJAXClient client) {
        this.client = client;
    }

    public Group[] resolveGroup(String pattern) throws OXException, IOException, JSONException {
        SearchRequest req = new SearchRequest(pattern, false);
        SearchResponse response = client.execute(req);
        return response.getGroups();
    }

    public Group[] loadGroups(int... groupIds) throws OXException, IOException, JSONException {
        if (groupIds == null) {
            return new Group[0];
        }
        ListRequest req = new ListRequest(groupIds, true);
        AbstractGroupResponse response = client.execute(req);
        return response.getGroups();
    }
}
