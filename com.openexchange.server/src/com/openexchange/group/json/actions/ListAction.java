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

package com.openexchange.group.json.actions;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.group.json.GroupAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ListAction extends AbstractGroupAction {

    /**
     * Initializes a new {@link ListAction}.
     * 
     * @param services
     */
    public ListAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final GroupAJAXRequest req) throws OXException, JSONException {
        JSONArray jBody = req.getData();
        if (null == jBody) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        List<Integer> groupIds = new LinkedList<Integer>();

        int length = jBody.length();
        for (int a = 0; a < length; a++) {
            JSONObject jData = jBody.getJSONObject(a);
            groupIds.add(DataParser.checkInt(jData, DataFields.ID));
        }

        GroupService groupService = this.services.getService(GroupService.class);
        Group[] groupsResult = groupService.listGroups(req.getSession().getContext(), groupIds.stream().mapToInt(i -> i).toArray());

        List<Group> groupList = new LinkedList<Group>();
        Date timestamp = new Date(0);
        for (int a = 0; a < groupsResult.length; a++) {
            Group group = groupsResult[a];
            groupList.add(group);

            Date lastModified = group.getLastModified();
            if (null != lastModified && lastModified.after(timestamp)) {
                timestamp = group.getLastModified();
            }
        }
        return new AJAXRequestResult(groupList, timestamp, "group");
    }
}
